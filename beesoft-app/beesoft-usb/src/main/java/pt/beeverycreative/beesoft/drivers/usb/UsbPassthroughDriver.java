package pt.beeverycreative.beesoft.drivers.usb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbNotOpenException;
import org.w3c.dom.Node;
import pt.beeverycreative.beesoft.filaments.PrintPreferences;
import replicatorg.app.Base;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.panels.Feedback;
import replicatorg.app.ui.panels.PrintSplashAutonomous;
import replicatorg.app.ui.panels.Warning;
import replicatorg.app.util.AutonomousData;
import replicatorg.drivers.RetryException;
import replicatorg.util.Point5d;

/**
 * Copyright (c) 2013 BEEVC - Electronic Systems This file is part of BEESOFT
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. BEESOFT is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with
 * BEESOFT. If not, see <http://www.gnu.org/licenses/>.
 */
public final class UsbPassthroughDriver extends UsbDriver {

    private static final int SERIAL_NUMBER_SIZE = 10;
    private static final String NO_SERIAL_NO_FIRMWARE = "0000000000";
    private static final String NO_SERIAL_FIRMWARE_OK = "0000000001";
    private static final String GET_STATUS = "M625";
    private static final String GET_STATUS_FROM_ERROR = GET_STATUS + new String(new char[507]).replace("\0", "A");
    private static final String GET_POSITION = "M121";
    private static final String LAUNCH_FIRMWARE = "M630";
    private static final String SET_FIRMWARE_VERSION = "M114 A";
    private static final String INVALID_FIRMWARE_VERSION = "0.0.0";
    private static final String GET_FIRMWARE_VERSION = "M115";
    private static final String GET_FIRMWARE_OK = "M651";
    private static final String DUMMY = "M637";
    private static final String GET_LINE_NUMBER = "M638";
    private static final String ECHO = "M639";
    private static final String STATUS_OK = "S:3";
    private static final String STATUS_X = "S:";
    private static final String STATUS_PAUSED = "Pause";
    private static final String STATUS_SDCARD = "s:5";
    private static final String RESPONSE_TRANSFER_ON_GOING = "tog";
    private static final String STATUS_SHUTDOWN = "s:9";
    private static final String ERROR = "error";
    private static final String BEGIN_PRINT = "M33";
    private static final String fileName = "abcde";
    private static final String RESPONSE_OK = "ok";
    private static final String FILE_CREATED = "File created";
    private static final String SET_FILENAME = "M30 ";
    private static final String READ_VARIABLES = "M32";
    private static final String TRANSFER_BLOCK = "M28 ";
    private static final String INIT_SDCARD = "M21 ";
    private static final int MESSAGE_SIZE = 512;
    private static final int MESSAGES_IN_BLOCK = 512;
    //BLOCK_SIZE is: how many bytes in each M28 block transfers
    private static final int MAX_BLOCK_SIZE = MESSAGE_SIZE * MESSAGES_IN_BLOCK;
    //private static final String GET_SERIAL = "M117";
    private static final String SET_SERIAL = "M118 T";
    private static final String SET_COIL_TEXT = "M1000 ";
    private static final String GET_COIL_TEXT = "M1001";
    private static final String GET_NOZZLE_TYPE = "M1025";
    private static final String SAVE_CONFIG = "M601 ";
    private static final String NOK = "NOK";
    private static final int QUEUE_LIMIT = 85;
    private static final int QUEUE_WAIT = 1000;
    private static final int SEND_WAIT = 2; //2 did not work
    private int queue_size = 0;
    private final Queue<QueueCommand> resendQueue = new LinkedList<QueueCommand>();
    private long lastDispatchTime;
    private static Version bootloaderVersion = new Version();
    private Version firmwareVersion = new Version();
    private String serialNumberString = NO_SERIAL_NO_FIRMWARE;
    private boolean machineReady;
    private boolean machinePaused;
    private boolean machineShutdown;
    private boolean machinePowerSaving;
    private boolean machinePrinting;
    private long startTS;
    private boolean driverError = false;
    private String driverErrorDescription;
    private boolean stopTransfer = false;
    private boolean isBusy = true;
    private int readyCount = 0;
    private static boolean bootedFromBootloader = false;
    private static boolean backupConfig = false;
    private static String backupCoilText = "";
    private static double backupZVal = 123.495;
    private static final Object dispatchCommandMutex = new Object();
    private static final Feedback feedbackWindow = new Feedback();
    private FeedbackThread feedbackThread = new FeedbackThread(feedbackWindow);

    public enum COM {

        DEFAULT, BLOCK, TRANSFER, NO_RESPONSE
    }

    class QueueCommand {

        String command;
        int lineNumber;

        public QueueCommand(String command, int lineNumber) {
            this.command = command;
            this.lineNumber = lineNumber;
        }

        public String getCommand() {
            return command;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        @Override
        public String toString() {
            return command + " N:" + lineNumber;
        }
    }

    /**
     * What did we get back from serial?
     */
    private String result = "";
    private final DecimalFormat df;
    private final boolean comLog;

    /**
     *
     */
    public UsbPassthroughDriver() {
        super();

        // init our variables.
        setInitialized(false);

        //Thank you Alexey (http://replicatorg.lighthouseapp.com/users/166956)
        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
        dfs.setDecimalSeparator('.');
        df = new DecimalFormat("#.######", dfs);

        // Communication debug
        /**
         * *****************************
         */
        comLog = Boolean.valueOf(ProperDefault.get("comLog"));
        if (comLog) {
            startTS = System.currentTimeMillis();
        }
    }

    @Override
    public void loadXML(Node xml) {
        super.loadXML(xml);
    }

    @Override
    public void resetBootloaderAndFirmwareVersion() {
        if (bootedFromBootloader == false) {
            bootloaderVersion = new Version();
        }

        firmwareVersion = new Version();

        Base.FIRMWARE_IN_USE = null;
        Base.VERSION_BOOTLOADER = null;
    }

    @Override
    public void closeFeedback() {
        if (feedbackThread != null && feedbackThread.isAlive()) {
            feedbackThread.cancel();
        }
        feedbackWindow.dispose();
        feedbackThread = null;
    }

    @Override
    public int getQueueSize() {
        return queue_size;
    }

    @Override
    public boolean isBusy() {
        return isBusy;
    }

    /**
     * Send any gcode needed to synchronize the driver type and the firmware
     * type. Sent on startup and if we see "start" indicating an uncommanded
     * firmware reset.
     */
    public void sendInitializationGcode() {
        hiccup(1000, 0);
        Base.writeLog("Sending initialization GCodes", this.getClass());
        String status = checkPrinterStatus();

        super.isBootloader = true;

        //initialize serial to NO SERIAL
        serialNumberString = NO_SERIAL_NO_FIRMWARE;

        if (status.contains("bootloader")) {

            bootedFromBootloader = true;
            updateBootloaderInfo();
            if (updateFirmware() >= 0) {
                lastDispatchTime = System.currentTimeMillis();
                resendQueue.clear();

                Base.writeLog("Bootloader version: " + bootloaderVersion, this.getClass());
                Base.writeLog("Firmware version: " + firmwareVersion, this.getClass());
                Base.writeLog("Serial number: " + serialNumberString, this.getClass());

                super.isBootloader = true;

                Base.writeLog("Launching firmware!", this.getClass());
                feedbackWindow.setFeedback2(Feedback.LAUNCHING_MESSAGE);
                Base.rebootingIntoFirmware = true;
                dispatchCommand(LAUNCH_FIRMWARE); // Launch firmware
                hiccup(100, 0);
                closePipe(pipes);
                return;
            } else {
                status = "error";
            }
        }

        if (status.contains("autonomous")) {
            lastDispatchTime = System.currentTimeMillis();
            super.isBootloader = false;

            Base.getMainWindow().setEnabled(true);
            Base.bringAllWindowsToFront();
            /**
             * Does not show PSAutonomous until MainWindows is visible
             */
            boolean mwVisible = Base.getMainWindow().isVisible();
            while (mwVisible == false) {
                try {
                    Thread.sleep(100, 0);
                } catch (InterruptedException ex) {
                    Logger.getLogger(UsbPassthroughDriver.class.getName()).log(Level.SEVERE, null, ex);
                }
                mwVisible = Base.getMainWindow().isVisible();//polls
            }

            updateCoilText();
            PrintPreferences prefs = new PrintPreferences();

            PrintSplashAutonomous p = new PrintSplashAutonomous(
                    true, Base.printPaused, this.isONShutdown, prefs
            );

            if (Base.printPaused == false && this.isONShutdown == false) {
                p.setVisible(true);
            }

            p.startConditions();
            Base.updateVersions();

            return;
        }

        if (status.contains("firmware")) {

            // this is made just to be sure that the bootloader version
            // isn't inconsistent
            if (bootedFromBootloader == false) {
                bootloaderVersion = new Version();
            } else {
                bootedFromBootloader = false;
            }

            lastDispatchTime = System.currentTimeMillis();
            resendQueue.clear();
            super.isBootloader = false;

            try {
                serialNumberString = m_usbDevice.getSerialNumberString();
            } catch (UsbException ex) {
                Logger.getLogger(UsbPassthroughDriver.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(UsbPassthroughDriver.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UsbDisconnectedException ex) {
                Logger.getLogger(UsbPassthroughDriver.class.getName()).log(Level.SEVERE, null, ex);
            }

            updateMachineInfo();

            Base.writeLog("Bootloader version: " + bootloaderVersion, this.getClass());
            Base.writeLog("Firmware version " + firmwareVersion, this.getClass());
            Base.writeLog("Serial number: " + serialNumberString, this.getClass());

            if (firmwareVersion.getVersionString().equals(connectedDevice.bootloaderString()) == false) {
                Base.writeLog("Firmware is not OK", this.getClass());
                Base.writeLog("Firmware version string: "
                        + firmwareVersion.getVersionString(), this.getClass());
                Base.writeLog("Soliciting user to restart BEESOFT and the printer", this.getClass());
                // Warn user to restart BTF and restart BEESOFT.
                Warning firmwareOutDate = new Warning("close");
                firmwareOutDate.setMessage("FirmwareOutDateVersion");
                firmwareOutDate.setVisible(true);

                Base.getMainWindow().setEnabled(false);
                // Sleep forever, until restart.
                while (true) {
                    hiccup(100, 0);
                }

            } //no need for else

            setBusy(true);

            if (backupConfig) {
                setCoilText(backupCoilText);
                dispatchCommand("M604 Z" + backupZVal);
                backupConfig = false;
                backupZVal = 123.495;
                backupCoilText = "";
            } else {
                updateCoilText();
            }

            Base.isPrinting = false;

            dispatchCommand("M104 S0", COM.DEFAULT); //Extruder and Table heat
            dispatchCommand("G92", COM.DEFAULT);

            //Set PID values
            dispatchCommand("M130 T6 U1.3 V80", COM.DEFAULT);

            dispatchCommand("G28", COM.DEFAULT);

            dispatchCommand("M601", COM.DEFAULT);
            setBusy(false);

            Base.updateVersions();
            return;
        }

        if (status.contains("error")) {
            setBusy(true);
            setInitialized(false);
            int tries = 0;
            String response = dispatchCommand(GET_STATUS_FROM_ERROR);

            while (response.contains(NOK) && (tries < MESSAGES_IN_BLOCK)) {
                tries++;
                response = dispatchCommand(GET_STATUS_FROM_ERROR);
            }

            if (response.contains(RESPONSE_TRANSFER_ON_GOING)) {

                //Printer still waiting to transfer end of block to SDCard
                //Solution: asking for STATUS to close the block and so abort
                //When recovered, initialize driver again
                //System.out.println("isOnTransfer: " + isOnTransfer);
                //recoverFromSDCardTransfer();
                while (!response.contains(RESPONSE_OK) && tries < MESSAGES_IN_BLOCK) {
                    tries++;
                    response = dispatchCommand(GET_STATUS_FROM_ERROR);
                }

                transferMode = false;
                setBusy(false);
                initialize();
                return;
            } else if (response.contains(STATUS_OK)) {
                setBusy(false);
                return;
            }

            Base.writeLog("Could not recover COM from type: error.", this.getClass());
            setBusy(false);
            closePipe(pipes);
            pipes = null;

        }
    }

    /**
     * Initialize USB Device and performs some Initialization GCode
     */
    @Override
    public void initialize() {

        // wait till we're initialized
        if (!isInitialized()) {
            Base.writeLog("Initializing search for printer.", this.getClass());
            establishConnection();
            Base.writeLog("USB Driver initialized", this.getClass());
        }

        if (Base.welcomeSplashVisible == false) {
            Base.disposeAllOpenWindows();
        }

        sendInitializationGcode();
    }

    private int getQfromReverse(String txt) {

        String resversed_txt = new StringBuilder(txt).reverse().toString();
        String re4 = "(\\d+)";	// Integer Number 1
        String re3 = "(:)";	// Any Single Character 1
        String re2 = "(Q)";	// Variable Name 1
        String re1 = ".*?";	// Non-greedy match on filler

        Pattern p = Pattern.compile(re4 + re3 + re2 + re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(resversed_txt);
        if (m.find()) {

            String var1 = m.group(1);
            String c1 = m.group(2);

            return Integer.parseInt(new StringBuilder(var1).reverse().toString());
        }
        return QUEUE_LIMIT;
    }

    private void recoverFromSDCardTransfer() {
        String out = "";

        //Waits for a Status OK to unlock Transfer
        while (!out.toLowerCase().contains(STATUS_OK)
                || !out.toLowerCase().contains(ERROR)) {

//                sent = sendCommandBytes(byteMessage.byte_array);
            out += dispatchCommand(GET_STATUS_FROM_ERROR);

//            System.out.println("out: "+out);
            try {
                Thread.sleep(5, 0); //sleep for a five milli second just for luck
            } catch (InterruptedException ex) {
                Logger.getLogger(PrintSplashAutonomous.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //End transfer mode
        transferMode = false;
    }

    private int getQfromStatus(String txt) {

        String re1 = ".*?";	// Non-greedy match on filler
        String re2 = "(Q)";	// Variable Name 1
        String re3 = "(:)";	// Any Single Character 1
        String re4 = "(\\d+)$";	// Integer Number 1

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(txt);
        if (m.find()) {
            String var1 = m.group(1);
            String c1 = m.group(2);
            String int1 = m.group(3);

            return Integer.parseInt(int1);
        }
        return QUEUE_LIMIT;
    }

    @Override
    public String dispatchCommand(String next) {

        //QueueCommand temp = null;
        if (next != null && !next.equals(DUMMY)) {
            sendCommand(next);

            /**
             * Avoid double home
             */
            /*
             if (!next.contains("G28")) {
             temp = new QueueCommand(next, ++ID);
             resendQueue.add(temp);
             }
             */
        } else {
            // Dummy command - no answer expected
            sendCommand(DUMMY);
        }

        String ans = readResponse();

        /*
         if (temp != null) {
         resendQueue.remove(temp);
         }
         */
        queue_size = getQfromStatus(ans);
        return ans;
        //}
    }

    @Override
    public String dispatchCommand(String next, Enum e) {

        String tResponse;
        String tExpected;

        /**
         * Blocks non-transfer while in TRANSFER mode
         */
        if (e != COM.TRANSFER) {
            if (transferMode) {
                return "NOK: Dispatch locked in transfer mode.";
            }
        }

        COM COMTYPE = (COM) e;
        //check if the queue is getting full EX: ok Q:0
        while (queue_size >= QUEUE_LIMIT) {
            hiccup(QUEUE_WAIT, 0);

            tResponse = dispatchCommand(GET_STATUS);
            tExpected = STATUS_X;

            // Necessary to handle disconnect during readResponse
            if (!isInitialized()) {
                return NOK;
            }

            if (!tResponse.contains(tExpected)) {
                recoverCOM();
                break;
            } else {
                // everything is ok, flush resendQueue
                resendQueue.clear();
            }

            queue_size = getQfromReverse(tResponse);

            if (comLog) {
                Base.writeComLog((System.currentTimeMillis() - startTS), " Response:" + tResponse);
                Base.writeComLog((System.currentTimeMillis() - startTS), " Queue:" + queue_size);
            }
        }

        /**
         * Parses Bad GCodes or pipes garbage
         */
        if (next.startsWith(";")) {
            return next + " did not send";
        }

        String answer = "";
        switch (COMTYPE) {
            case NO_RESPONSE:
            case DEFAULT:
            case TRANSFER:
                answer = dispatchCommand(next);
                break;
            case BLOCK:

                //Checks if machine is ready before sending               
                while (!dispatchCommand(GET_STATUS).contains(STATUS_OK)) {
                    hiccup(QUEUE_WAIT, 0);

                    if (!isInitialized()) {
                        setInitialized(false);
                        return NOK;
                    }
                }

                //Sends blocked command
                //if (next.contains("G28")) {
                //    sendCommand(next);
                //} else {
                answer = dispatchCommand(next);
                //}

                break;

        }

        if (answer.contains(NOK) == false) {
            return answer;
        } else {
            return recoverCOM();
        }
    }

    private String recoverCOM() {
        boolean comLost = false;

        while (comLost) {

            int myID = (int) (Math.random() * 100.0);
            String ans;

            String message = ECHO + " E" + myID;
            String expected = "E" + String.valueOf(myID);
            String response;

            while (true) {
                response = dispatchCommand(message);
                if (comLog) {
                    Base.writeComLog(System.currentTimeMillis() - startTS, "Trying to recover COM. R: " + response + " E: " + expected);
                }

                /**
                 * Necessary to handle disconnect during readResponse. Breaks
                 * the loop.
                 */
                if (!isInitialized()) {
                    return "error";
                }

                if (response.contains(expected)) {
                    break;
                } else if (response.contains(NOK)) {
                    //received a timeout, will try again.
                    myID++;
                    message = ECHO + " E" + myID;
                    expected = "E" + myID;
                } else {
                    //received something try to read the same expected
                    //dummy - does nothing
                    message = DUMMY;
                }

            }

            comLost = false;
            ans = dispatchCommand(GET_LINE_NUMBER);

            String re1 = "(last)";	// Variable Name 1
            String re2 = "(\\s+)";	// White Space 1
            String re3 = "(N)";	// Variable Name 2
            String re4 = "(:)";	// Any Single Character 1
            String re5 = "(\\d+)";	// Integer Number 1

            Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(ans);
            if (m.find()) {

                int lineNumber = Integer.valueOf(m.group(5));

                while (!resendQueue.isEmpty()) {
                    QueueCommand top = resendQueue.poll();

                    if (comLog) {
                        Base.writeComLog(-1, "top = " + top.toString() + "Queue size = " + resendQueue.size());
                    }

                    if (lineNumber < top.getLineNumber() && !top.command.contains(ECHO)) {
                        ans = dispatchCommand(top.getCommand());

                        if (comLog) {
                            Base.writeComLog(-1, "Resend top = " + top.toString());
                        }

                        if (!ans.contains(NOK)) {
                        } else {
                            comLost = true;
                            break;
                        }

                    } else {
                        // Command was previously send. No resend required.
                        if (comLog) {
                            Base.writeComLog(-1, "Command skipped = " + top.toString());
                        }
                    }
                }
            } else {
                comLost = true;
            }
        }

        //queue_size = QUEUE_LIMIT;
        return "ok";
    }

    @Override
    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    @Override
    public String getFirmwareVersion() {
        return firmwareVersion.getVersionString();
    }

    @Override
    public String getBootloaderVersion() {
        return bootloaderVersion.toString();
    }

    @Override
    public String getSerialNumber() {
        return serialNumberString.trim();
    }

    @Override
    public void setCoilText(String coilText) {
        String response = dispatchCommand(SET_COIL_TEXT + coilText, COM.BLOCK);

        if (response.toLowerCase().contains("ok")) {
            machine.setCoilText(coilText);
        } else {
            machine.setCoilText("");
        }

        /**
         * Save config
         */
        dispatchCommand(SAVE_CONFIG, COM.BLOCK);
    }

    @Override
    public void setInstalledNozzleSize(int microns) {
        String response;

        response = dispatchCommand("M1024 S" + microns, COM.BLOCK);

        if (response.toLowerCase().contains("ok")) {
            dispatchCommand(SAVE_CONFIG, COM.BLOCK);
        }
    }

    /**
     * Gets the CoilCode from printer AXXX for code, A000 for none NOK for error
     *
     */
    @Override
    public void updateCoilText() {
        String coilText, coilTextLowerCase;

        coilText = dispatchCommand(GET_COIL_TEXT, COM.DEFAULT);
        coilTextLowerCase = coilText.toLowerCase();

        try {
            if (coilTextLowerCase.contains("ok")
                    && coilTextLowerCase.contains("bad") == false) {
                coilText = coilText.substring(
                        coilText.indexOf('\'') + 1, coilText.lastIndexOf('\'')
                );
            } else {
                coilText = "none";
            }
        } catch (StringIndexOutOfBoundsException e) {
            coilText = "none";
        }

        Base.writeLog("Coil text: " + coilText, this.getClass());
        machine.setCoilText(coilText);
    }
    
    @Override
    public void updateNozzleType() {
        String nozzleType;
        String[] splitted;
        
        nozzleType = dispatchCommand(GET_NOZZLE_TYPE);
        splitted = nozzleType.split("\n")[0].split(":");
        
        Base.writeLog("Nozzle type: " + splitted[1], this.getClass());
        machine.setNozzleType(splitted[1]);
    }

    @Override
    public boolean isONShutdown() {
        return isONShutdown;
    }

    @Override
    public void stopTransfer() {
        stopTransfer = true;
        while (stopTransfer == true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(UsbPassthroughDriver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void readZValue() {
        String temp = "", response, home_pos_z = "123.495";
        //sendCommand("M600");
        dispatchCommand("M600");

        hiccup(100, 0);

        while ((response = readResponse()).equals("") == false) {
            temp += response;
            hiccup(10, 0);
        }

        String re1 = ".*?";	// Non-greedy match on filler
        String re2 = "(home_pos_z)";	// Variable Name 1
        String re3 = "(\\s+)";	// White Space 1
        String re4 = "(=)";	// Any Single Character 1
        String re5 = "(\\s+)";	// White Space 2
        String re6 = "([+-]?\\d*\\.\\d+)(?![-+0-9\\.])";	// Float 1

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(temp);
        if (m.find()) {
            String var1 = m.group(1);
            String ws1 = m.group(2);
            String c1 = m.group(3);
            String ws2 = m.group(4);
            String float1 = m.group(5);

            home_pos_z = float1;
        }

        machine.setzValue(Double.valueOf(home_pos_z));
        try {
            Thread.sleep(1, 1);
        } catch (InterruptedException ex) {
            Logger.getLogger(UsbPassthroughDriver.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public String gcodeTransfer(File gcode, PrintSplashAutonomous psAutonomous) {

        long time, loop = System.currentTimeMillis();
        byte[] gcodeBytes;
        int file_size, srcPos, totalBlocks;
        int offset = MESSAGE_SIZE;
        int destPos = 0;
        int totalMessages;
        int message = 0;
        int totalBytes = 0;
        double transferPercentage;

        file_size = (int) gcode.length();
        totalMessages = (int) Math.ceil((double) file_size / (double) MESSAGE_SIZE);
        totalBlocks = (int) Math.ceil((double) file_size / (double) (MESSAGES_IN_BLOCK * MESSAGE_SIZE));

        String fileSizeInfo = "File size: " + file_size + " bytes";
        System.out.println(fileSizeInfo);
        String s_totalMessages = "Number of messages to Transfer: " + totalMessages;
        System.out.println(s_totalMessages);
        String s_totalBlocks = "Number of blocks to Transfer: " + totalBlocks;
        System.out.println(s_totalBlocks);

        transferMode = true;

        if (dispatchCommand(INIT_SDCARD, COM.TRANSFER).contains(ERROR)) {
            driverError = true;
            driverErrorDescription = ERROR + ":INIT_SDCARD failed";
            transferMode = false;

            return driverErrorDescription;
        } else {
            Base.writeLog("SD Card init successful", this.getClass());
        }

        //Set file at SDCard
        if (createSDCardFile(gcode).contains(ERROR)) {
            driverError = true;
            driverErrorDescription = ERROR + ":createSDCardFile failed";
            transferMode = false;

            return driverErrorDescription;
        } else {
            Base.writeLog("SD Card file created with success", this.getClass());
        }

        //Stores file in byte array
        gcodeBytes = getBytesFromFile(gcode);
        byte[] iMessage;

        //Send the file 1 block at a time, only send full blocks
        //Each block is MESSAGES_IN_BLOCK messages long        
        for (int block = 1; block < totalBlocks; block++) {

            //check if the transfer was canceled
            if (stopTransfer == true) {
                Base.writeLog("Transfer canceled.", this.getClass());
                driverErrorDescription = ERROR + ":Transfer canceled.";
                //dispatchCommand("G28");
                transferMode = false;
                stopTransfer = false;
                return driverErrorDescription;
            }

            // size is MAX_BLOCK_SIZE of file_size
            if (setTransferSize(destPos, (destPos + MAX_BLOCK_SIZE) - 1).contains(ERROR)) {
                driverError = true;
                driverErrorDescription = ERROR + ":setTransferSize failed";
                transferMode = false;

                return driverErrorDescription;
            } else {
                Base.writeLog("SDCard space allocated with success", this.getClass());
            }
//            System.out.println("block:" + block + "M28 A" + srcPos + " D" + ((srcPos + MAX_BLOCK_SIZE) - 1));
            for (int i = 0; i < MESSAGES_IN_BLOCK; i++) {

                message++;

                // Updates variables
                srcPos = destPos;
                destPos = srcPos + offset;
                iMessage = subbytes(gcodeBytes, srcPos, destPos);
                //Transfer each Block
                if (transferMessage(iMessage).contains(ERROR)) {
                    driverError = true;
                    driverErrorDescription = ERROR + ":512B message transfer failed";
                    transferMode = false;

                    return driverErrorDescription;
                } else {
                    totalBytes += iMessage.length;
//                Base.writeLog("512B block transfered with success");
                }

                //System.out.println("Message " + message + "/" + totalMessages + " in " + (System.currentTimeMillis() - time) + "ms");
                transferPercentage = ((double) message / totalMessages) * 100;

                if (Base.printPaused == false) {
                    psAutonomous.updatePrintBar((int) transferPercentage);
                }

//                System.out.println("\tmessage: "+message);
//                System.out.println("\tsrc: "+srcPos+"\tdst: "+destPos);
            }
            System.out.println("Block " + block + "/" + totalBlocks);
        }

        //Do the last Block!
        // Updates variables
        srcPos = destPos;

//        System.out.println("last block; src: "+srcPos+"\tdst: "+destPos);
        //check if the transfer was canceled
        if (stopTransfer == true) {
            Base.writeLog("Transfer canceled.", this.getClass());
            driverErrorDescription = ERROR + ":Transfer canceled.";
            //dispatchCommand("G28");
            transferMode = false;
            stopTransfer = false;
            return driverErrorDescription;
        }

        // last block is special
        //destpos is MAX_BLOCK_SIZE+src or file_size
        if (setTransferSize(srcPos, Math.min(srcPos + MAX_BLOCK_SIZE, file_size) - 1).contains(ERROR)) {
            driverError = true;
            driverErrorDescription = ERROR + ":setTransferSize failed";
            transferMode = false;

            return driverErrorDescription;
        } else {
            Base.writeLog("SDCard space allocated with success", this.getClass());
        }

        for (; srcPos < file_size; srcPos += offset) {
            message++;
            //Get byte array with MESSAGE_SIZE
            time = System.currentTimeMillis();

            iMessage = subbytes(gcodeBytes, srcPos, Math.min(srcPos + offset, file_size));
            //Transfer each Block
            if (transferMessage(iMessage).contains(ERROR)) {
                driverError = true;
                driverErrorDescription = ERROR + ":512B message transfer failed";
                transferMode = false;

                return driverErrorDescription;
            } else {
                totalBytes += iMessage.length;
//                Base.writeLog("512B block transfered with success");
            }

            System.out.println("Message " + message + "/" + totalMessages + " in " + (System.currentTimeMillis() - time) + "ms");
            transferPercentage = ((double) message / totalMessages) * 100;
            psAutonomous.updatePrintBar((int) transferPercentage);
        }

        loop = System.currentTimeMillis() - loop;
        double transferSpeed = totalBytes / (loop / 1000.0);
        Base.writeLog("Transmission sucessfull " + totalBytes + " bytes in " + loop / 1000.0
                + "s : " + transferSpeed + "kbps\n", this.getClass());

        transferMode = false;

        return RESPONSE_OK;
    }

    @Override
    public void startPrintAutonomous() {
        dispatchCommand(BEGIN_PRINT, COM.DEFAULT);
    }

    @Override
    public void getPrintSessionsVariables() {
//        Data:
//        
//        estimatedTime - [0];
//        elapsedTime - [1];
//        nLines - [2];
//        currentNumberLines - [3];
        String printSession;
        String[] data;
        int tries;

        printSession = dispatchCommand(READ_VARIABLES);

        tries = 0;
        while (printSession.contains("A") == false
                || printSession.contains("B") == false
                || printSession.contains("C") == false
                || printSession.contains("D") == false) {
            printSession = readResponse();

            if (tries++ >= 3) {
                break;
            }
        }

        data = parseData(printSession);

        machine.setAutonomousData(new AutonomousData(data[0], data[1], data[2], data[3], 0));
    }

    private String[] parseData(String printSession) {
        String[] variables = new String[4];

        String[] res = printSession.split(" ");

        String aTag = "A";
        String bTag = "B";
        String cTag = "C";
        String dTag = "D";
        String aValue = "0";
        String bValue = "0";
        String cValue = "0";
        String dValue = "0";

        for (String re : res) {
            if (re.contains(aTag)) {
                aValue = re.substring(1);
            } else if (re.contains(bTag)) {
                bValue = re.substring(1);
            } else if (re.contains(cTag)) {
                cValue = re.substring(1);
            } else if (re.contains(dTag) && !re.equalsIgnoreCase("Done")) {
                dValue = re.substring(1);
            }
        }

        variables[0] = aValue;
        variables[1] = bValue;
        variables[2] = cValue;
        variables[3] = dValue;
        return variables;
    }

    private String transferMessage(byte[] iBlock) {
        int sent;
        String out = "";
        int tries;
        String response;
        response = "";

        out += "Sending \n";

        sent = sendCommandBytes(iBlock);
//        System.out.println("send bytes: " + sent);

        tries = 11;
        while (tries > 0) {
            tries--;
            try {

                out += (response = readResponse()) + "\n";

                if (sent == iBlock.length && out.toLowerCase().contains(RESPONSE_TRANSFER_ON_GOING)) {
                    out += response + "\n";
                    break;
                } else if (sent == iBlock.length && out.replace("\n", "").toLowerCase().contains(RESPONSE_TRANSFER_ON_GOING)) {
                    out += response + "\n";
                    break;
                } else if (driverError) {
                    Base.writeLog("recoverFromSDCardTransfer", this.getClass());
                    recoverFromSDCardTransfer();
                    return ERROR + out;
                }

            } catch (Exception ex) {
                if (!(tries > 0)) {
                    out += "Timeout after " + tries + ".\n";
                    Base.writeLog("Transfer to SDCard failed. " + out, this.getClass());
                    return ERROR + out;
                }
                Logger.getLogger(PrintSplashAutonomous.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                Thread.sleep(0, 1); //sleep for a nano second just for luck
            } catch (InterruptedException ex) {
                Logger.getLogger(PrintSplashAutonomous.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (!(tries > 0)) {
            out += response + "\n";
            return ERROR + out + "Transfer to SDCard failed. Response not OK";
        }

        return RESPONSE_OK;
    }

    private String createSDCardFile(File gcode) {

        int tries = 5;
        String out = "";
        String response = "";

        if (!gcode.isFile() || !gcode.canRead()) {
            String err = out + "File not found or unreadable.\n";
            Base.writeLog("Impossible to read GCode file for transfer: " + err, this.getClass());
            return err;
        }

        //sleep for a nano second just for luck
        try {
            Thread.sleep(0, 1);
        } catch (InterruptedException ex) {
            Logger.getLogger(PrintSplashAutonomous.class.getName()).log(Level.SEVERE, null, ex);
        }

        out = dispatchCommand(SET_FILENAME + fileName, COM.TRANSFER);

        while (tries > 0) {
            try {
                //test for file created in tempresponse
                if (!out.contains(FILE_CREATED)) {
                } else {
                    out += response + "\n";
                    break;
                }
                tries--;
                out += (response = readResponse()) + "\n";

            } catch (Exception ex) {
                Logger.getLogger(PrintSplashAutonomous.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                Thread.sleep(0, 1); //sleep for a nano second just for luck
            } catch (InterruptedException ex) {
                Logger.getLogger(PrintSplashAutonomous.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (!(tries > 0)) {
            out += response + "\n";
            Base.writeLog("M30 failed. File not created " + out, this.getClass());
            return ERROR + out + "M30 failed. File not created";
        } else {
            return FILE_CREATED;
        }

    }

    private String setTransferSize(int srcPos, int destPos) {

        String command;
        String out = "";
        int tries = 10;
        String response;

        command = TRANSFER_BLOCK + "A" + srcPos + " D" + destPos;
        out += command + "\n";

        out = dispatchCommand(command);
        //sendCommand(command);
        //hiccup(100, 0);
        //out = readResponse();

//        System.err.println("Source Pos = " + srcPos);
//        System.err.println("Destination Pos = " + destPos);
        response = "";
        while (tries > 0) {
            try {

                try {
                    Thread.sleep(1, 0); //sleep for a milli second just for luck
                } catch (InterruptedException ex) {
                    Logger.getLogger(PrintSplashAutonomous.class.getName()).log(Level.SEVERE, null, ex);
                }

//                System.out.println("Transfer ACK: " + out);
                //test for ok in tempresponse
                if (!out.contains(RESPONSE_OK)) {
                } else {
                    out += response + "\n";
                    break;
                }
                tries--;

                out += (response = readResponse()) + "\n";

            } catch (Exception ex) {
                if (!(tries > 0)) {
                    out += "Timeout after " + tries + ".\n";
                    Base.writeLog("Transfer to SDCard failed. " + out, this.getClass());
                    return ERROR + out;
                }
                Logger.getLogger(PrintSplashAutonomous.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                Thread.sleep(0, 1); //sleep for a nano second just for luck
            } catch (InterruptedException ex) {
                Logger.getLogger(PrintSplashAutonomous.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (!(tries > 0)) {
            out += response + "\n";
            return ERROR + out + "M28 failed. Response not OK";
        }

        return RESPONSE_OK;
    }

    private byte[] getBytesFromFile(File gcode) {
        FileInputStream in;

        byte[] bytes = new byte[(int) gcode.length()];

        //Open stream to read File
        try {
            in = new FileInputStream(gcode);
            in.read(bytes);
            in.close();
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }

        return bytes;
    }

    /**
     * Return a new byte array containing a sub-portion of the source array
     *
     * @param source
     * @param srcBegin The beginning index (inclusive)
     * @param srcEnd The ending index (exclusive)
     *
     * @return The new, populated byte array
     */
    private byte[] subbytes(byte[] source, int srcBegin, int srcEnd) {
        byte destination[];

        destination = new byte[srcEnd - srcBegin];
        getBytes(source, srcBegin, srcEnd, destination, 0);

        return destination;
    }

    /**
     * Copies bytes from the source byte array to the destination array
     *
     * @param source The source array
     * @param srcBegin Index of the first source byte to copy
     * @param srcEnd Index after the last source byte to copy
     * @param destination The destination array
     * @param dstBegin The starting offset in the destination array
     */
    private void getBytes(byte[] source, int srcBegin, int srcEnd, byte[] destination,
            int dstBegin) {
        System.arraycopy(source, srcBegin, destination, dstBegin, srcEnd - srcBegin);
    }

    /**
     * Actually sends command over USB.
     *
     * @param next
     * @return
     */
    private int sendCommand(String next) {

        lastDispatchTime = System.currentTimeMillis();
        //next = clean(next);
        int cmdlen = 0;
        int i = 0;
        // skip empty commands.
        if (next.length() == 0) {
            return 0;
        }
        //pipes = GetPipe(m_usbDevice);

        // do the actual send.
        String message = next + "\n";

        try {
            if (m_usbDevice != null) {
                synchronized (dispatchCommandMutex) {
                    try {
                        if (!pipes.isOpen()) {
                            openPipe(pipes);
                        }
                    } catch (NullPointerException ex) {
                        return -1;
                    }
                    pipes.getUsbPipeWrite().syncSubmit(message.getBytes());
                    cmdlen = next.length() + 1;

                }
            }
        } catch (UsbException ex) {
            Base.writeLog("*sendCommand* <UsbException> Error while sending command " + next + " : " + ex.getMessage(), this.getClass());
        } catch (UsbNotActiveException ex) {
            Base.writeLog("*sendCommand* <UsbNotActiveException> Error while sending command " + next + " : " + ex.getMessage(), this.getClass());
        } catch (UsbNotOpenException ex) {
            Base.writeLog("*sendCommand* <UsbNotOpenException> Error while sending command " + next + " : " + ex.getMessage(), this.getClass());
        } catch (IllegalArgumentException ex) {
            Base.writeLog("*sendCommand* <IllegalArgumentException> Error while sending command " + next + " : " + ex.getMessage(), this.getClass());
        } catch (UsbDisconnectedException ex) {
            Base.writeLog("*sendCommand* <UsbDisconnectedException> Error while sending command " + next + " : " + ex.getMessage(), this.getClass());
        }

        if (comLog && message.contains("M625") == false) {
            Base.writeComLog((System.currentTimeMillis() - startTS), "SENT: " + message.trim());
        }

        return cmdlen;
    }

    @Override
    public String readResponse() {

        while (SEND_WAIT > (System.currentTimeMillis() - lastDispatchTime)) {
            try {
                Thread.sleep(1, 1);
            } catch (InterruptedException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        lastDispatchTime = System.currentTimeMillis();
        result = "timeout";
        byte[] readBuffer = new byte[1024];

        int nBits = 0;
        try {
            if (m_usbDevice != null) {
                synchronized (m_usbDevice) {

                    if (pipes != null) {
                        nBits = pipes.getUsbPipeRead().syncSubmit(readBuffer);
                    } else {
                        Base.writeLog("PIPES NULL", this.getClass());
                        setInitialized(false);
                        return NOK;
                        //throw new UsbException("Pipe was null");
                    }
                }
            }
        } catch (UsbException ex) {
            // Cable removable
            if (ex.getMessage().contains("LIBUSB_ERROR_NO_DEVICE")) {
                try {
                    Base.writeLog("LIBUSB_ERROR_NO_DEVICE", this.getClass());
                    pipes.close();
                    setInitialized(false);
                } catch (UsbException ex1) {
                    Base.writeLog("USB exception [readResponse]: " + ex1.getMessage(), this.getClass());
                } catch (UsbNotActiveException ex1) {
                    Base.writeLog("USB communication not active [readResponse]:" + ex1.getMessage(), this.getClass());
                } catch (UsbNotOpenException ex1) {
                    Base.writeLog("USB communication is down [readResponse]:" + ex1.getMessage(), this.getClass());
                } catch (UsbDisconnectedException ex1) {
                    Base.writeLog("USB disconnected exception [readResponse]:" + ex1.getMessage(), this.getClass());
                }
            }

        } catch (UsbNotActiveException ex) {
            try {
                pipes.close();
            } catch (UsbException ex1) {
                if (comLog) {
                    Base.writeComLog((System.currentTimeMillis() - startTS), "UsbException");
                }
            } catch (UsbNotActiveException ex1) {
                if (comLog) {
                    Base.writeComLog((System.currentTimeMillis() - startTS), "UsbNotActiveException");
                }
            } catch (UsbNotOpenException ex1) {
                if (comLog) {
                    Base.writeComLog((System.currentTimeMillis() - startTS), "UsbNotOpenException");
                }
            } catch (UsbDisconnectedException ex1) {
                if (comLog) {
                    Base.writeComLog((System.currentTimeMillis() - startTS), "UsbDisconnectedException");
                }
            }
        } catch (UsbNotOpenException ex) {
            try {
                pipes.close();
                setInitialized(false);
            } catch (UsbException ex1) {
                if (comLog) {
                    Base.writeComLog((System.currentTimeMillis() - startTS), "UsbException");
                }
            } catch (UsbNotActiveException ex1) {
                if (comLog) {
                    Base.writeComLog((System.currentTimeMillis() - startTS), "UsbNotActiveException");
                }
            } catch (UsbNotOpenException ex1) {
                if (comLog) {
                    Base.writeComLog((System.currentTimeMillis() - startTS), "UsbNotOpenException");
                }
            } catch (UsbDisconnectedException ex1) {
                if (comLog) {
                    Base.writeComLog((System.currentTimeMillis() - startTS), "UsbDisconnectedException");
                }
            }
        } catch (IllegalArgumentException ex) {
            try {
                pipes.close();
                setInitialized(false);
            } catch (UsbException ex1) {
                if (comLog) {
                    Base.writeComLog((System.currentTimeMillis() - startTS), "UsbException");
                }
            } catch (UsbNotActiveException ex1) {
                if (comLog) {
                    Base.writeComLog((System.currentTimeMillis() - startTS), "UsbNotActiveException");
                }
            } catch (UsbNotOpenException ex1) {
                if (comLog) {
                    Base.writeComLog((System.currentTimeMillis() - startTS), "UsbNotOpenException");
                }
            } catch (UsbDisconnectedException ex1) {
                if (comLog) {
                    Base.writeComLog((System.currentTimeMillis() - startTS), "UsbDisconnectedException");
                }
            }
        }

        // 0 is now an acceptable value; it merely means that we timed out
        // waiting for input
        if (nBits < 0) {
        } else {
            try {
                result = new String(readBuffer, 0, nBits, "US-ASCII").trim();
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (comLog && result.equals("") == false && result.contains("S:") == false) {
            Base.writeComLog((System.currentTimeMillis() - startTS), "RECEIVE (" + result.length() + "): " + result.trim() + "\n");
        }

        return result;
    }

    @Override
    public boolean isTransferMode() {
        return this.transferMode;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * *************************************************************************
     * commands for interfacing with the driver directly
     *
     * @param p
     *
     * @throws UsbDisconnectedException
     * @throws IllegalArgumentException
     * @throws UsbNotOpenException
     * @throws UsbNotActiveException
     * ************************************************************************
     */
    // FIXME: 5D port
    @Override
    public void queuePoint(Point5d p) {
        // Redundant feedrate send added in Ultimaker merge. TODO: ask Erik, D1plo1d about this. 
        String cmd = "G1 F" + df.format(getCurrentFeedrate());

        dispatchCommand(cmd);

        cmd = "G1 X" + df.format(p.x()) + " Y" + df.format(p.y()) + " Z"
                + df.format(p.z()) + " F" + df.format(getCurrentFeedrate());

        dispatchCommand(cmd);
        try {
            super.queuePoint(p);
        } catch (RetryException ex) {
            //Logger.getLogger(UsbPassthroughDriver.class.getName()).log(Level.SEVERE, null, ex);
        }

        //readResponse();
    }

    // FIXME: 5D port
    @Override
    public void setCurrentPosition(Point5d p) throws RetryException {
        //sendCommand("G92 X" + df.format(p.x()) + " Y" + df.format(p.y()) + " Z"
        //        + df.format(p.z()));
        dispatchCommand("G92 X" + df.format(p.x()) + " Y" + df.format(p.y()) + " Z"
                + df.format(p.z()));

        super.setCurrentPosition(p);
    }

    /**
     * *************************************************************************
     * Temperature interface functions
     *
     * @param temperature
     * @throws RetryException
     * ************************************************************************
     */
    @Override
    public void setTemperature(int temperature) throws RetryException {
        dispatchCommand("M104 S" + temperature);
        super.setTemperature(temperature);
    }

    @Override
    public void setTemperatureBlocking(int temperature) throws RetryException {
        dispatchCommand("M109 S" + temperature);
        super.setTemperature(temperature);
    }

    @Override
    public void readStatus() {
        String status;

        status = dispatchCommand(GET_STATUS);

        if (status.contains("S:") == false) {
            isBusy = true;
            return;
        } else {
            if (readyCount == 5) {
                isBusy = false;
                readyCount = 0;
            } else {
                readyCount++;
                return;
            }
        }

        if (status.contains(STATUS_OK)) {
            machineReady = true;
            if (queue_size == 0) {
                setBusy(false);
            }
        } else {
            machineReady = false;
        }

        machinePowerSaving = status.contains("Power_Saving");
        machineShutdown = status.toLowerCase().contains(STATUS_SHUTDOWN) || status.toLowerCase().contains("shutdown");
        machinePrinting = status.toLowerCase().contains(STATUS_SDCARD);

        if (machinePaused == false) {
            machinePaused = status.contains(STATUS_PAUSED);
        } else {
            machinePaused = status.contains(STATUS_PAUSED) || status.contains("NOK");
        }

//        System.out.println("machineReady: "+machineReady);
        //machine.currentTool().setCurrentTemperature(temperature);        
        machine.setLastStatusString(status);
        machine.setMachineReady(machineReady);
        machine.setMachinePaused(machinePaused);
        machine.setMachinePowerSaving(machinePowerSaving);
        machine.setMachineShutdown(machineShutdown);
        machine.setMachinePrinting(machinePrinting);
    }

    @Override
    public Point5d getPosition() {

        Point5d myCurrentPosition = null;

        String position = dispatchCommand(GET_POSITION);

//        Base.writeLog("position "+position);
        /**
         * Example_ String txt="C: X:-96.000 Y:-74.500 Z:123.845 E:0.000 ok
         * Q:0";
         */
        String re1 = "([a-z])";	// Any Single Word Character (Not Whitespace) 1
        String re2 = "(.)";	// Any Single Character 1
        String re3 = "(\\s+)";	// White Space 1
        String re4 = "([a-z])";	// Any Single Word Character (Not Whitespace) 2
        String re5 = "(.)";	// Any Single Character 2
        String re6 = "([+-]?\\d*\\.\\d+)(?![-+0-9\\.])";	// Float 1
        String re7 = "(\\s+)";	// White Space 2
        String re8 = "([a-z])";	// Any Single Word Character (Not Whitespace) 3
        String re9 = "(.)";	// Any Single Character 3
        String re10 = "([+-]?\\d*\\.\\d+)(?![-+0-9\\.])";	// Float 2
        String re11 = "(\\s+)";	// White Space 3
        String re12 = "([a-z])";	// Any Single Word Character (Not Whitespace) 4
        String re13 = "(.)";	// Any Single Character 4
        String re14 = "([+-]?\\d*\\.\\d+)(?![-+0-9\\.])";	// Float 3
        String re15 = "(\\s+)";	// White Space 4
        String re16 = "([a-z])";	// Any Single Word Character (Not Whitespace) 5
        String re17 = ".*?";	// Non-greedy match on filler
        String re18 = "([+-]?\\d*\\.\\d+)(?![-+0-9\\.])";	// Float 4

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9 + re10 + re11 + re12 + re13 + re14 + re15 + re16 + re17 + re18, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(position);
        if (m.find()) {
            String float1 = m.group(6);
            String float2 = m.group(10);
            String float3 = m.group(14);
            String float4 = m.group(17);
            myCurrentPosition = new Point5d(Double.valueOf(float1), Double.valueOf(float2), Double.valueOf(float3), Double.valueOf(float4), 0);
            Base.getMachineLoader().getMachineInterface().setLastPrintedPoint(myCurrentPosition);
        }

        synchronized (currentPosition) {
            currentPosition.set(myCurrentPosition);
            posAvailable = true;
            currentPosition.notifyAll();
        }

        return myCurrentPosition;
    }

    @Override
    public void readTemperature() {
        int extruderTemperature, blockTemperature;
        String temp, re1, re2, re3, re4, re5, re6, re7;
        Pattern p;
        Matcher m;

        temp = dispatchCommand("M105");
        re1 = "(T)";	// Variable Name 1
        re2 = "(:)";	// Any Single Character 1
        re3 = "([+-]?\\d*\\.\\d+)(?![-+0-9\\.])";	// Float 1
        re4 = "(\\s+)";	// White Space 1
        re5 = "(B)";	// Variable Name 2
        re6 = "(:)";	// Any Single Character 2
        re7 = "([+-]?\\d*\\.\\d+)(?![-+0-9\\.])";	// Float 2
        p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        m = p.matcher(temp);

        if (m.find()) {
            extruderTemperature = Double.valueOf(m.group(3)).intValue();
            blockTemperature = Double.valueOf(m.group(7)).intValue();
        } else {
            extruderTemperature = -1;
            blockTemperature = -1;
        }

        machine.currentTool().setCurrentTemperature(extruderTemperature, blockTemperature);
    }

    @Override
    public boolean isInitialized() {
        return (super.isInitialized() && testPipes(pipes));
    }

    private String checkPrinterStatus() {
        int tries = 0;
        String res;
        boolean possibleBootloader = false;

        Base.writeLog("Verifying what is the current status of the printer", this.getClass());

        //recoverEcho();
        //sendCommand(GET_STATUS)
        do {
            res = dispatchCommand(GET_STATUS);
            Base.writeLog("Attempt " + ++tries, this.getClass());
            Base.writeLog("Response: " + res, this.getClass());

            if (res.toLowerCase().contains("bad")) {
                possibleBootloader = true;
                break;
            } else if (res.contains("Pause")) {
                Base.writeLog("Printer is in pause mode", this.getClass());
                Base.printPaused = true;
                return "autonomous";
            } else if (res.toLowerCase().contains(STATUS_SDCARD)) {
                Base.writeLog("Printer is actively printing", this.getClass());
                return "autonomous";
            } else if (res.toLowerCase().contains(STATUS_SHUTDOWN) || res.toLowerCase().contains("shutdown")) {
                Base.writeLog("Printer is in shutdown mode", this.getClass());
                Base.printPaused = true;
                this.isONShutdown = true;
                return "autonomous";
            } else if (res.toLowerCase().contains("ok")) {
                Base.writeLog("Printer is in firmware mode and idle", this.getClass());
                return "firmware";
            } else if (res.isEmpty()) {
                Base.writeLog("Couldn't determine the printer status", this.getClass());
                return "error";
            }
        } while (tries < 10);

        tries = 0;

        if (possibleBootloader) {
            Base.writeLog("Requesting bootloader version", this.getClass());

            do {
                hiccup(100, 0);
                res = dispatchCommand("M116");
                Base.writeLog("Attempt " + ++tries, this.getClass());
                Base.writeLog("Response: " + res, this.getClass());

                if (res.contains("ok")) {
                    Base.writeLog("Bootloader appears to be OK", this.getClass());
                    return "bootloader";
                } else {
                    Base.writeLog("Couldn't find what bootloader version this printer has", this.getClass());
                    return "error";
                }
            } while (tries < 10);
        }

        Base.writeLog("Returning error by default, this shouldn't happen.", this.getClass());
        return "error";
    }

    private int flashAndCheck(String filename, int nBytes) {

        FileInputStream in = null;
        int file_size;
        int sent;
        ByteRead res;
        boolean state;
        String command;

        File f = new File(filename);
//        System.out.println();
        if (!f.isFile() || !f.canRead()) {
            Base.writeLog("File not found or unreadable for flash.\n", this.getClass());
            return -1;
        }

        file_size = (int) f.length();

        try {
            in = new FileInputStream(f);
        } catch (FileNotFoundException ex) {
            Base.writeLog("File not found or unreadable.", this.getClass());
            return -1;
        }

//        System.out.println("M650 A" + file_size);
        command = "M650 A" + file_size;
//        System.out.println("File: " + filename + "; size:" + file_size);
//        System.out.println("Sending: " + command);

        sendCommand(command);

        //sleep for a nano second just for luck
        try {
            Thread.sleep(0, 1);
        } catch (InterruptedException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }

        String response = readResponse();
        if (response.toLowerCase().contains("ok") == false) {
            return -1;
        }

//         System.out.println("Sending");
        int bytesRead = 0;

        try {
            byte[] byteTemp = new byte[64];
            ByteRead byteMessage;
            byteMessage = new ByteRead(64, new byte[0]);
            while (((byteMessage.size = in.read(byteTemp)) != -1)
                    && (nBytes == -1 || (bytesRead < nBytes))) {
                bytesRead += byteMessage.size;
                byteMessage = new ByteRead(byteMessage.size, new byte[byteMessage.size]);
                System.arraycopy(byteTemp, 0, byteMessage.byte_array, 0, byteMessage.size);

                sent = sendCommandBytes(byteMessage.byte_array);

                try {
                    Thread.sleep(0, 1); //sleep for a nano second just for luck
                } catch (InterruptedException ex) {
                }

                if (sent == 0) {
                    Base.writeLog("Transfer failure, 0 bytes sent.", this.getClass());
                    return -1;
                }

                int tries = 3;
                res = new ByteRead(0, new byte[byteMessage.size]);
                while (tries > 0) {
                    try {
                        tries--;

                        //sleep for a nano second just for luck
                        try {
                            Thread.sleep(0, 10);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        ByteRead tempMessage = readBytes();
                        //sleep for a nano second just for luck
                        try {
                            Thread.sleep(0, 10);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        System.arraycopy(tempMessage.byte_array, 0,
                                res.byte_array, res.size, tempMessage.size);
                        res.size += tempMessage.size;

                        if (res.size == byteMessage.size) {
                            state = Arrays.equals(res.byte_array, byteMessage.byte_array);
                            if (!state) {
                                Base.writeLog("Transmission error found, reboot BEETHEFIRST.", this.getClass());
                                return -1;
                            } else {
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        if (!(tries > 0)) {
                            Base.writeLog("Timeout after 3 tries.", this.getClass());
                            return -1;
                        }
                        Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }

        return 1;
    }

    /**
     * Send command to Machine
     *
     * @param next Command
     * @return command length
     */
    @Override
    public int sendCommandBytes(byte[] next) {

        int cmdlen = 0;
        int i = 0;

        //pipes = GetPipe(m_usbDevice);
        try {
            synchronized (m_usbDevice) {
                if (!pipes.isOpen()) {
                    openPipe(pipes);
                }
                cmdlen = pipes.getUsbPipeWrite().syncSubmit(next);
            }
        } catch (Exception ex) {
            //System.out.println("Error while sending command " + next + " : " + ex.getMessage());
        }
        return cmdlen;
    }

    public ByteRead readBytes() throws UsbException {

        byte[] result_local = new byte[64];
        byte[] readBuffer = new byte[64];

        int nBits;

        nBits = pipes.getUsbPipeRead().syncSubmit(readBuffer);

        // 0 is now an acceptable value; it merely means that we timed out
        // waiting for input
        if (nBits < 0) {
        } else if (nBits > 0) {
            result_local = readBuffer;
        } else {
            System.err.println("Timeout!");
        }

        return new ByteRead(nBits, result_local);

    }

    //dispacher does not work in bootloader!!!
    private void updateBootloaderInfo() {

        String bootloader, firmware;
        //sendCommand(GET_BOOTLOADER_VERSION);
        //hiccup(QUEUE_WAIT, 0);
        //String bootloader = readResponse();
        bootloader = dispatchCommand("GET_BOOTLOADER_VERSION");

        bootloaderVersion = Version.bootloaderVersion(bootloader);

        //Default
        firmwareVersion = new Version();

        //get serial number - Must have exactly 10 chars!
        serialNumberString = "0000000000";
        try {
            serialNumberString = m_usbDevice.getSerialNumberString();
        } catch (UsbException ex) {
            Logger.getLogger(UsbPassthroughDriver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(UsbPassthroughDriver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UsbDisconnectedException ex) {
            Logger.getLogger(UsbPassthroughDriver.class.getName()).log(Level.SEVERE, null, ex);
        }

        //get firmware version
        //check first for un-initialized serial or firmware version
        if (!serialNumberString.contains("0000000000")) {
            //sendCommand(GET_FIRMWARE_VERSION);
            //hiccup(10, 0);
            //String firmware = readResponse();
            firmware = dispatchCommand(GET_FIRMWARE_VERSION);

            if (isNewVendorID) {
                firmwareVersion = Version.fromMachine(firmware);
            } else {
                firmwareVersion = Version.fromMachineOld(firmware);
            }
        } else {
            // No serial means, no firmware. We must update!
            firmwareVersion = new Version();
            return;
        }
        if (_checkFirmwareIntegrity() == false) {
            //Integrity test failed
            firmwareVersion = new Version();
        }
    }

    private boolean _checkFirmwareIntegrity() {
        String firmware_is_ok;
        int tries = 3;
        //check if the firmware is properly installed
        do {
            //sendCommand(GET_FIRMWARE_OK);
            //hiccup(QUEUE_WAIT, 0);
            //String firmware_is_ok = readResponse();
            firmware_is_ok = dispatchCommand(GET_FIRMWARE_OK);

            Base.writeLog("Checking firmware integrity: " + result, this.getClass());
            if ((firmware_is_ok.contains("ok"))) {
                // Everything is ok!
                Base.writeLog("Firmware integrity check: OK", this.getClass());
                return true;
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(UsbPassthroughDriver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } while (tries-- > 0);

        Base.writeLog("Firmware integrity check: failed", this.getClass());
        return false;

    }

    private void updateMachineInfo() {
        String firmware;
        serialNumberString = "0000000000";
        try {
            serialNumberString = m_usbDevice.getSerialNumberString();
        } catch (UsbException ex) {
            Logger.getLogger(UsbPassthroughDriver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(UsbPassthroughDriver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UsbDisconnectedException ex) {
            Logger.getLogger(UsbPassthroughDriver.class.getName()).log(Level.SEVERE, null, ex);
        }

        //get firmware version
        //check first for un-initialized serial or firmware version
        if (!serialNumberString.contains("0000000000")) {
            //sendCommand(GET_FIRMWARE_VERSION);
            //hiccup(10, 0);
            //String firmware = readResponse();
            firmware = dispatchCommand(GET_FIRMWARE_VERSION);
            firmwareVersion = Version.fromMachineAtFirmware(firmware);
            System.out.println("firmware_version: " + firmwareVersion);

        } else {
            // no firmware version available
            firmwareVersion = new Version();
        }
    }

    /*@return -1 - update failed 
     0 - no update necessary
     1 - update sucessful    
     */
    private int updateFirmware() {

        String versionToCompare = connectedDevice.bootloaderString();
        Base.writeLog("Firmware should be: " + versionToCompare, this.getClass());

        //check if the firmware is the same
        String machineFirmware = firmwareVersion.getVersionString();
        Base.writeLog("Firmware is: " + machineFirmware, this.getClass());

        if (machineFirmware.equals(versionToCompare) == true) {
            Base.writeLog("No update necessary, firmware is as it should be", this.getClass());
            return 0; // NO UPDATE NECESSARY
        } // else carry on updating

        Base.writeLog("Update was necessary, firmware version string is not what was expected", this.getClass());

        File folder = new File(Base.getApplicationDirectory().toString() + "/firmware/");

        File[] firmwareList = folder.listFiles();
        File firmwareFile = null;

        for (File firmwareFileTemp : firmwareList) {
            if (firmwareFileTemp.getName().equalsIgnoreCase(connectedDevice.firmwareFilename())) {
                firmwareFile = firmwareFileTemp;
                Base.writeLog("Candidate file found:" + firmwareFile, this.getClass());
                break;
            }
        }

        if (firmwareFile == null) {
            Base.writeLog("No firmware file found.", this.getClass());
            return -1;
        } else {

            if (feedbackThread != null) {
                feedbackThread = null;
            }

            feedbackThread = new FeedbackThread(feedbackWindow);

            if (feedbackThread.isAlive() == false) {
                feedbackThread.start();
            }

            feedbackWindow.setFeedback1(Feedback.FLASHING_MAIN_MESSAGE);

            if (firmwareVersion.equals(new Version()) == false) {
                backupConfig();
            }

            Base.writeLog("Carrying on with firmware flash", this.getClass());

            //sendCommand(SET_FIRMWARE_VERSION + INVALID_FIRMWARE_VERSION);
            //hiccup(QUEUE_WAIT, 0);
            //readResponse();
            dispatchCommand(SET_FIRMWARE_VERSION + INVALID_FIRMWARE_VERSION);

            //if (firmwareFile.getName().length() > 45) {
            if (backupConfig) {
                feedbackWindow.setFeedback2(Feedback.FLASHING_SUB_MESSAGE);
            } else {
                feedbackWindow.setFeedback2(Feedback.FLASHING_SUB_MESSAGE_NO_CALIBRATION);
            }
            //} else {
            //    feedbackWindow.setFeedback2("Flashing firmware " + firmwareFile.getName());
            //}

            Base.writeLog("Starting Firmware update.", this.getClass());
            if (flashAndCheck(firmwareFile.getAbsolutePath(), -1) > 0) {
                Base.writeLog("Firmware successfully updated", this.getClass());

                if (serialNumberString.contains(NO_SERIAL_NO_FIRMWARE)) {
                    setSerial(NO_SERIAL_FIRMWARE_OK);
                    hiccup();
                    return 1;
                }//no need for else

                if (_checkFirmwareIntegrity() == false) {
                    //Integrity test failed. setting firmware_version as 0.0.0
                    firmwareVersion = new Version();
                    return -1;
                }

                Base.writeLog("Setting firmware version to: " + versionToCompare, this.getClass());
                //sendCommand(SET_FIRMWARE_VERSION + versionToCompare);
                //hiccup(QUEUE_WAIT, 0);
                //readResponse();
                dispatchCommand(SET_FIRMWARE_VERSION + versionToCompare);

            } else {
                Base.writeLog("Firmware update failed", this.getClass());
                Base.errorOccured = true;
                return -1;
            }
        }
        return 0; // correct this
    }

    private boolean backupConfig() {
        String response;

        Base.writeLog("Acquiring Z value and loaded filament before flashing new firmware", this.getClass());
        feedbackWindow.setFeedback2(Feedback.SAVING_MESSAGE);

        // change into firmware
        dispatchCommand("M630");

        hiccup(3000, 0);

        // reestablish connection
        if (establishConnection() == false) {
            Base.writeLog("Establishing connection after changing into firmware failed", this.getClass());
            return false;
        }

        // going into firmware may have failed
        response = dispatchCommand("M625").toLowerCase();
        if (response.contains("bad")) {
            Base.writeLog("Something is wrong with the firmware, flashing without saving calibration", this.getClass());
            establishConnectionToBootloader();
            return false;
        }

        // request data
        readZValue();
        updateCoilText();
        backupZVal = machine.getzValue();
        backupCoilText = machine.getCoilText();

        // change back into bootloader
        dispatchCommand("M609");

        hiccup(3000, 0);

        if (establishConnection() == false) {
            Base.writeLog("Couldn't establish connection after attempting to go back to bootloader, requesting user to restart", this.getClass());

            // Warn user to restart BTF and restart BEESOFT.
            Warning firmwareOutDate = new Warning("close");
            firmwareOutDate.setMessage("FirmwareOutDateVersion");
            firmwareOutDate.setVisible(true);

            Base.getMainWindow().setEnabled(false);
            // Sleep forever, until restart.
            while (true) {
                hiccup(3000, 0);
            }
        }

        response = dispatchCommand("M116").toLowerCase();
        if (response.contains("bad")) {
            Base.writeLog("Couldn't go back to bootloader, asking user to restart printer", this.getClass());
            establishConnectionToBootloader();
        }

        Base.writeLog("Acquired Z value and loaded filament with success!", this.getClass());
        backupConfig = true;
        return true;

    }

    private boolean establishConnection() {
        try {
            boolean ready;
            int tries = 0;

            ready = false;

            do {

                if (pipes != null && pipes.isOpen()) {
                    closePipe(pipes);
                }

                if (m_usbDevice != null) {
                    m_usbDevice.close();
                    m_usbDevice = null;
                }

                pipes = null;
                while (m_usbDevice == null) {
                    InitUsbDevice();
                    hiccup(100, 0);
                }

                pipes = GetPipe(m_usbDevice);

                if (pipes != null) {
                    openPipe(pipes);
                } else {
                    continue;
                }

                if (isInitialized() && testPipes(pipes)) {
                    Base.getMainWindow().getButtons().setMessage("is connecting");
                    ready = true;
                } else {
                    Base.writeLog("Failed in establishing connection, trying again in 1 second...", this.getClass());
                    Thread.sleep(1000);
                }

                if (tries++ >= 10) {
                    feedbackWindow.setFeedback3(Feedback.RESTART_PRINTER);
                }
            } while (ready == false);

        } catch (Exception ex) {
            Base.writeLog("Exception on establishConnection()", this.getClass());
            return false;
        }

        return true;
    }

    private boolean establishConnectionToBootloader() {
        boolean ready;
        String response;

        ready = false;

        do {
            try {

                if (pipes != null && pipes.isOpen()) {
                    closePipe(pipes);
                }

                if (m_usbDevice != null) {
                    m_usbDevice.close();
                    m_usbDevice = null;
                }

                pipes = null;
                while (m_usbDevice == null) {
                    InitUsbDevice();
                    hiccup(100, 0);
                }

                pipes = GetPipe(m_usbDevice);

                if (pipes != null) {
                    openPipe(pipes);
                } else {
                    continue;
                }

                if (isInitialized() && testPipes(pipes)) {

                    hiccup(100, 0);

                    int i = 100;
                    while (readResponse().equals("") == false) {
                        hiccup(10, 0);
                    }

                    //sendCommand("M116");
                    //hiccup(100, 0);
                    //response = readResponse().toLowerCase();
                    response = dispatchCommand("M116").toLowerCase();

                    if (response.equals("") == false
                            && response.contains("bad") == false) {
                        //Base.getMainWindow().getButtons().setMessage("is connecting");
                        ready = true;
                    } else {
                        feedbackWindow.setFeedback2(Feedback.RESTART_PRINTER);
                    }
                } else {
                    Base.writeLog("Failed in establishing connection, trying again in 1 second...", this.getClass());
                    Thread.sleep(1000);
                }

            } catch (Exception ex) {
            }
        } while (ready == false);

        return true;
    }

    private void setSerial(String serial) {

        if (serial.length() == SERIAL_NUMBER_SIZE) {
            try {
                Integer.parseInt(serial);
                dispatchCommand(SET_SERIAL + serial);
                hiccup(10, 0);
                readResponse();
            } catch (Exception ex) {
            }
        }
    }

    private class FeedbackThread extends Thread {

        private final Feedback feedbackWindow;
        private boolean stop = false;

        public FeedbackThread(Feedback feedbackWindow) {
            super("FeedbackThread");
            this.feedbackWindow = feedbackWindow;
        }

        @Override
        public void run() {
            while (feedbackWindow.isVisible() == false && stop == false) {

                if (Base.welcomeSplashVisible == false) {
                    feedbackWindow.setVisible(true);
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FeedbackThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        public void cancel() {
            stop = true;
        }
    }

}
