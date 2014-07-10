package pt.beeverycreative.beesoft.drivers.usb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
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
import static pt.beeverycreative.beesoft.drivers.usb.UsbDriver.m_usbDevice;
import static pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM.BLOCK;

import replicatorg.app.Base;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.panels.PrintSplashAutonomous;
import replicatorg.app.ui.panels.Warning;
import replicatorg.app.util.AutonomousData;
import replicatorg.drivers.RetryException;
import replicatorg.drivers.VersionException;
import replicatorg.machine.model.AxisId;
import replicatorg.machine.model.ToolModel;
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
    private static final String REBOOT_FIRMWARE_INTO_BOOTLOADER = "M609";
    private static final String SET_FIRMWARE_VERSION = "M114 A";
    private static final String GET_FIRMWARE_VERSION = "M115";
    private static final String GET_BOOTLOADER_VERSION = "M116";
    private static final String GET_FIRMWARE_OK = "M651";
    private static final String FIRMWARE_IS_OK = "ok  N:0 0";
    private static final String DUMMY = "M637";
    private static final String GET_LINE_NUMBER = "M638";
    private static final String ECHO = "M639";
    private static final String STATUS_OK = "S:3";
    private static final String STATUS_X = "S:";
    private static final String STATUS_SDCARD = "s:5";
    private static final String RESPONSE_TRANSFER_ON_GOING = "tog";
    private static final String ERROR = "error";
    private static final String BEGIN_PRINT = "M33";
    private static final String fileName = "abcde";
    private static final String RESPONSE_OK = "ok";
    private static final String RESPONSE_TRANSFER_COMPLETED = "transfer completed";
    private static final String FILE_CREATED = "File created";
    private static final String SET_FILENAME = "M30 ";
    private static final String OPEN_FILE = "M23 ";
    private static final String SET_VARIABLES = "M31 ";
    private static final String READ_VARIABLES = "M32";
    private static final String TRANSFER_BLOCK = "M28 ";
    private static final String INIT_SDCARD = "M21 ";
    private static final String CLOSE_SDCARD = "M22 ";
    private static final String GET_MD5 = "M34 ";
    private static final int MESSAGE_SIZE = 512;
    private static final int MESSAGES_IN_BLOCK = 512;
    //BLOCK_SIZE is: how many bytes in each M28 block transfers
    private static final int MAX_BLOCK_SIZE = MESSAGE_SIZE * MESSAGES_IN_BLOCK;
    //private static final String GET_SERIAL = "M117";
    private static final String SET_SERIAL = "M118 T";
    private static final String COILCODE = "M400 ";
    private static final String SAVE_CONFIG = "M601 ";
    private static final String RESET_AXIS = "G92";
    private static final String NOK = "NOK";
    private static final int QUEUE_LIMIT = 85;
    private static final int QUEUE_WAIT = 30;
    private static final int SEND_WAIT = 10; //2 did not work
    private int queue_size;
    private Queue<QueueCommand> resendQueue = new LinkedList<QueueCommand>();
    private long lastDispathTime;
    private Version bootloader_version = new Version();
    private Version firmware_version = new Version();
    private String serialNumberString = NO_SERIAL_NO_FIRMWARE;
    private boolean machineReady;
    private boolean machineIsBeingUsed;
    private long startTS;
    private int ID = 0;
    private boolean isAutonomous;
    private double transferPercentage = 0;
    private boolean driverError = false;
    private String driverErrorDescription;
    private boolean stopTranfer = false;

    public enum COM {

        DEFAULT, WAIT, NONBLOCK, BLOCK, NO_RESPONSE, COM_LOST, TRANSFER
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
     * To keep track of outstanding commands
     */
    private Queue<Integer> commands;
    /**
     * the size of the buffer on the GCode host TEST VALUE FOR USB
     */
    private int maxBufferSize = 60;
    /**
     * the amount of data we've sent and is in the buffer.
     */
    private int bufferSize = 0;
    /**
     * What did we get back from serial?
     */
    private String result = "";
    private DecimalFormat df;
    private boolean showMessage = true;
    private int commandNumber = 1;
    private int counter = 0;
    private boolean comLog;

    /**
     *
     */
    public UsbPassthroughDriver() {
        super();

        // init our variables.
        commands = new LinkedList<Integer>();
        bufferSize = 0;
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

    /**
     * Send any gcode needed to synchronize the driver type and the firmware
     * type. Sent on startup and if we see "start" indicating an uncommanded
     * firmware reset.
     */
    public void sendInitializationGcode() throws VersionException {
        hiccup(1000, 0);
        String type = checkFirmwareType();
        
         super.isBootloader = true;

        System.out.println("type: " + type);

        if (type.contains("bootloader")) {
            updateBootloaderInfo();
            updateFirmware();
            lastDispathTime = System.currentTimeMillis();
            resendQueue.clear();
            Base.writeLog("Launching firmware!");

            System.out.println("bootloader_version: " + bootloader_version);
            System.out.println("firmware_version: " + firmware_version);
            System.out.println("serialNumberString: " + serialNumberString);

            super.isBootloader = true;

            sendCommand(LAUNCH_FIRMWARE); // Launch firmware
            hiccup(100, 0);
            closePipe(pipes);
            return;
        }
        
        if (type.contains("autonomous")) {
            
            serialNumberString = "0000000000";
            lastDispathTime = System.currentTimeMillis();

            super.isBootloader = false;


            Base.getMainWindow().setEnabled(false);
            PrintSplashAutonomous p = new PrintSplashAutonomous(true, null);
            p.setVisible(true);
            p.startConditions();
            return;
        }
        if (type.contains("firmware")) {
            serialNumberString = "0000000000";
            lastDispathTime = System.currentTimeMillis();
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

            System.out.println("bootloader_version: " + bootloader_version);
            System.out.println("firmware_version: " + firmware_version);
            System.out.println("serialNumberString: " + serialNumberString);

            System.out.println("Base.VERSION_FIRMWARE: " + Base.VERSION_FIRMWARE_FINAL);
            System.out.println("Base.VERSION_FIRMWARE_OldVID: " + Base.VERSION_FIRMWARE_FINAL_OLD);

            System.out.println("firmware_version.getVerionString(): " + firmware_version.getVerionString());

            boolean firmwareOK = false;

            if (Base.VERSION_FIRMWARE_FINAL.contains(firmware_version.getVerionString())) {
                firmwareOK = true;
            } //else

            if (Base.VERSION_FIRMWARE_FINAL_OLD.contains(firmware_version.getVerionString())) {
                firmwareOK = true;
            } //else

            if (!firmwareOK) {
                Base.writeLog("firmware not ok");
//                System.out.println("firmware not ok");

                /**
                 * Warn user to restart BTF and BEESOFT
                 *///                
                hiccup(5000, 1);
                Warning firmwareOutDate = new Warning("close");
                firmwareOutDate.setMessage("FirmwareOutDateVersion");
                firmwareOutDate.setVisible(true);

                while (true) {
                    hiccup();
                    Base.getMainWindow().setEnabled(false);
                }
//                sendCommand(REBOOT_FIRMWARE_INTO_BOOTLOADER); // Launch firmware
//                closePipe(pipes);
//                hiccup(2000, 1);
//                throw(new VersionException());
            } //no need for else

            //dispatchCommand("M29"); // Shuts down current USB-print
            setBusy(true);
            updateCoilCode();
            dispatchCommand("M107", COM.DEFAULT); // Shut downs Blower 
            dispatchCommand("M104 S0", COM.DEFAULT); //Extruder and Table heat
            dispatchCommand("G92", COM.DEFAULT);

            dispatchCommand("G28 Z", COM.BLOCK);
            dispatchCommand("G28 X Y", COM.BLOCK);
            setBusy(false);

            return;
        }

        if (type.contains("error")) {
            setBusy(true);
            setInitialized(false);
            int tries = 0;
//            System.out.println("type: error");
            sendCommand(GET_STATUS_FROM_ERROR);
            String status = readResponse();
//            System.out.println("status: " + status + " found in " + tries + ".");

            while (status.contains(NOK) && (tries < MESSAGES_IN_BLOCK)) {
                tries++;
                sendCommand(GET_STATUS_FROM_ERROR);
                status = readResponse();
//                System.out.println("status: " + status + " found in " + tries + ".");
            }            
            
            isAutonomous = status.contains(STATUS_SDCARD);
            
            if (status.contains(RESPONSE_TRANSFER_ON_GOING)) {
                
                //Printer still waiting to transfer end of block to SDCard
                //Solution: asking for STATUS to close the block and so abort
                //When recovered, initialize driver again
                //System.out.println("isOnTransfer: " + isOnTransfer);
                System.out.println("isOnTransfer");
                //recoverFromSDCardTransfer();
                while (!status.contains(RESPONSE_OK)&& tries < MESSAGES_IN_BLOCK) {
                    tries++;
                    status = dispatchCommand(GET_STATUS_FROM_ERROR);
                }

                transferMode = false;
                setBusy(false);
                initialize();
                return;
            } else if (status.contains(STATUS_OK)) {
                setBusy(false);
                return;
            }
            
            //
            System.out.println("Could not recover com from type: error.");
            Base.writeLog("Could not recover com from type: error.");
            setBusy(false);
            closePipe(pipes);
            
            
        }
    }

    /**
     * Initialize USB Device and performs some Initialization GCode
     *
     * @throws java.lang.Exception
     */
    @Override
    public void initialize() throws VersionException {
        try {
            // wait till we're initialized
            if (!isInitialized()) {

                // record our start time.
                Date date = new Date();
                //long end = date.getTime() + 10000;

                Base.writeLog("Initializing usb device.");
                while (!isInitialized()) {

                    try {
                        InitUsbDevice();
                        if (m_usbDevice != null) {
                            Base.writeLog("Device ready to be used");
                            pipes = GetPipe(m_usbDevice);
                            Base.writeLog("Pipes created");
                            showMessage = false;
                        } else {
                            setInitialized(false);
                            if (showMessage) {
                                Base.writeLog("Error while initializing device. Device not present.");
                            }
                        }
                        openPipe(pipes);
                        Base.writeLog("Pipes open");
                    } catch (Exception e) {
                        try {
                            Thread.sleep(500); // sleep 500 ms
                        } catch (InterruptedException ex) {
                            Base.writeLog("Error while sleeping in open usb connection initialization: " + ex.getMessage());
                        }
                    }

                    // record our current time
                    date = new Date();
                    long now = date.getTime();

                    if (showMessage) {
                        Base.getMachineLoader().getMachineInterface().connect("");
                        Base.writeLog("USB device non-responsive. Please plug in the USB device !");
                        //Stop showing the message 1 time is enough :) 
                        showMessage = false;
                    }
                }
                Base.writeLog("USB Driver initialized");
            }

            sendInitializationGcode();
        } catch (VersionException e) {
            throw (e);
        }

    }

    @Override
    public boolean isPassthroughDriver() {
        return true;
    }

    /**
     * Actually execute the GCode we just parsed.
     */
    @Override
    public void executeGCodeLine(String code) {
        dispatchCommand(code);
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
        int sent;
        int tries = 30;
        String response;
        response = "";
        ByteRead res;
        int bytesRead = 0;
        byte[] buf = DUMMY.getBytes();
        int nBytes = buf.length;

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

    private int getQfromSpecial(String txt) {

        String re1 = "(ok)";	// Variable Name 1
        String re2 = "(\\s+)";	// White Space 1
        String re3 = "(Q)";	// Variable Name 2
        String re4 = "(:)";	// Any Single Character 1
        String re5 = "(\\d+)";	// Integer Number 1
        String re6 = "(\\s+)";	// White Space 2
        String re7 = "(N)";	// Variable Name 3
        String re8 = "(:)";	// Any Single Character 2
        String re9 = "(\\d+)$";	// Integer Number 2

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(txt);
        if (m.find()) {
            String var1 = m.group(1);
            String ws1 = m.group(2);
            String var2 = m.group(3);
            String c1 = m.group(4);
            String int1 = m.group(5);
            String ws2 = m.group(6);
            String var3 = m.group(7);
            String c2 = m.group(8);
            String int2 = m.group(9);

            return Integer.parseInt(int1);
        }
        return QUEUE_LIMIT;
    }

    private String blindDispatchCommand(String next) {

        // blind tag - no response expected.
        sendCommandWOTest(next);
        queue_size += 1;

        return "ok";
    }

    @Override
    public String dispatchCommand(String next, Enum e) {

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
            //System.out.println("_dispatchCommand:" + GET_STATUS);
            String response;
            String expected;

            response = _dispatchCommand(GET_STATUS);
            expected = STATUS_X;

            // Necessary to handle disconnect during readResponse
            if (!isInitialized()) {
                return NOK;
            }

            if (!response.contains(expected)) {
                recoverCOM();
                break;
            } else {
                // everything is ok, flush resendQueue
                resendQueue.clear();
            }

            queue_size = getQfromReverse(response);

            if (comLog) {
                Base.writecomLog((System.currentTimeMillis() - startTS), " Response:" + response);
                Base.writecomLog((System.currentTimeMillis() - startTS), " Queue:" + queue_size);
            }

        }

        /**
         * Parses Bad GCodes or pipes garbage
         */
        if (next.startsWith(";")) {
            return next + " did not send";
        }
        if (next.length() < 3 || BlackListGCodes.contains(next)) {
            return next + " did not send";
        } else if (next.contains(RESET_AXIS)) {
            COMTYPE = COM.BLOCK;
        }

        String answer = "";
        switch (COMTYPE) {
            case NO_RESPONSE:
                answer = blindDispatchCommand(next);
                break;
            case DEFAULT:
                answer = dispatchCommand(next);
                break;
            case TRANSFER:
                answer = dispatchCommand(next);
                break;
            case BLOCK:
                while (!dispatchCommand(GET_STATUS).contains(STATUS_OK)) {
                    hiccup(QUEUE_WAIT, 0);

                    if (!isInitialized()) {
                        setInitialized(false);
                        return NOK;
                    }
                }
                answer = dispatchCommand(next);
                break;
        }

        if (!answer.contains(NOK)) {

            return answer;
            // tests COMMS and resends all the missing commands

        } else {
            return recoverCOM();
        }

    }

    private String recoverCOM() {
        boolean comLost = true;

        while (comLost) {

            int myID = (int) (Math.random() * 100.0);
            String ans = NOK;

            String message = ECHO + " E" + myID;
            String expected = "E" + String.valueOf(myID);
            String response = "############";

            while (true) {
                response = _dispatchCommand(message);
                if (comLog) {
                    Base.writecomLog(System.currentTimeMillis() - startTS, "Trying to recover COM. R: " + response + " E: " + expected);
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
            ans = _dispatchCommand(GET_LINE_NUMBER);

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
                        Base.writecomLog(-1, "top = " + top.toString() + "Queue size = " + resendQueue.size());
                    }

                    if (lineNumber < top.getLineNumber() && !top.command.contains(ECHO)) {
                        ans = dispatchCommand(top.getCommand());

                        if (comLog) {
                            Base.writecomLog(-1, "Resend top = " + top.toString());
                        }

                        if (!ans.contains(NOK)) {
                        } else {
                            comLost = true;
                            break;
                        }

                    } else {
                        // Command was previously send. No resend required.
                        if (comLog) {
                            Base.writecomLog(-1, "Command skipped = " + top.toString());
                        }
                    }
                }
            } else {
                comLost = true;
            }
        }

        queue_size = QUEUE_LIMIT;

        return "ok";

    }

    private String _dispatchCommand(String next) {

        QueueCommand temp = null;

        if (next != DUMMY) {
            sendCommand(next);

            /**
             * Avoid double home
             */
            if (!next.contains("G28")) {
                temp = new QueueCommand(next, ++ID);
                resendQueue.add(temp);
            }

        } else {
            // Dummy command - no answer expected
            sendCommand(DUMMY);
        }

        String ans = readResponse();

        if (!ans.contains("ok") && !ans.contains("\n")) {
            return NOK;
        } else {
            if (temp != null) {
                resendQueue.remove(temp);
            }
            return ans;
        }

    }

    @Override
    public String dispatchCommand(String next) {

        String ans = _dispatchCommand(next);

        queue_size = getQfromStatus(ans);

        return ans;
    }

    @Override
    public boolean isReady(boolean forceCheck) {
        //TODO: read a remove var update from here
        if (forceCheck) {
            machineReady = dispatchCommand(GET_STATUS).contains(STATUS_OK);
        }
        return machineReady;
    }

    @Override
    public void setBusy(boolean busy) {
        machine.setMachineBusy(busy);

    }

    public String getFirmwareVersion() {
        return firmware_version.getVerionString();
    }

    public String getBootloaderVersion() {
        return bootloader_version.toString();
    }

    public String getSerialNumber() {
        return serialNumberString.trim();
    }

    @Override
    @Deprecated
    public double read() {

        String temp = dispatchCommand("M105");

//       T:215.0 B:1.0 ok
        String re1 = "(T)";	// Variable Name 1
        String re2 = "(:)";	// Any Single Character 1
        String re3 = "([+-]?\\d*\\.\\d+)(?![-+0-9\\.])";	// Float 1
        String re4 = "(\\s+)";	// White Space 1
        String re5 = "((?:[a-z][a-z0-9_]*))";	// Variable Name 2
        String re6 = "(.)";	// Any Single Character 2
        String re7 = "([+-]?\\d*\\.\\d+)(?![-+0-9\\.])";	// Float 2

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(temp);
        if (m.find()) {
            String var1 = m.group(1);
            String c1 = m.group(2);
            String float1 = m.group(3);
            String ws1 = m.group(4);
            String var2 = m.group(5);
            String c2 = m.group(6);
            String float2 = m.group(7);
            return Double.valueOf(float1);
        } else {
            return 0;
        }
    }

    @Override
    public void setCoilCode(String coilCode) {
        String response = dispatchCommand(COILCODE + coilCode, COM.BLOCK);

        if (response.toLowerCase().contains("ok")) {
            machine.setCoilCode(coilCode);
        } else {
            machine.setCoilCode("NOK");
        }

        /**
         * Save config
         */
        dispatchCommand(SAVE_CONFIG, COM.BLOCK);
    }

    /**
     * Gets the CoilCode from printer AXXX for code, A0 for none NOK for error
     *
     * @return
     */
    @Override
    public void updateCoilCode() {

        //EX : String txt="bcode:A301 ok Q:0";
        String response = dispatchCommand(COILCODE, COM.BLOCK);

        Base.writeLog("Coil code: " + response);
        int ntries = 5;

        while (!response.toLowerCase().contains("ok") && !response.toLowerCase().contains("bcode:A")) {
            response += dispatchCommand(COILCODE, COM.BLOCK);
            ntries--;

            if (ntries == 0) {
                machine.setCoilCode(NOK);
                break;
            }
            hiccup(1, 0);
        }

        String re1 = ".*?";	// Non-greedy match on filler
        String re2 = "(bcode:A)";	// Any Single Word Character (Not Whitespace) 1
        String re3 = "(\\d+)";	// Integer Number 1
        Pattern p = Pattern.compile(re1 + re2 + re3, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(response);
        if (m.find()) {
            String w1 = m.group(1);
            String int1 = m.group(2);

            machine.setCoilCode("A" + int1);

        } else {
            //Default A0 / None
            machine.setCoilCode("A0");
        }
    }

    @Override
    public boolean isAutonomous() {
        return isAutonomous;
    }

    @Override
    public void setAutonomous(boolean auto) {
        isAutonomous = auto;
    }
    
    @Override
    public void stopTransfer() {
        stopTranfer = true;        
    }
    
    
    @Override
    public void readZValue() {
        String temp = dispatchCommand("M600");
        String home_pos_z = "0.0";
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

    public double getTransferPercentage() {
        return transferPercentage;
    }

    public String gcodeTransfer(File gcode, String estimatedTime, int nLines, PrintSplashAutonomous psAutonomous) {

        long loop = System.currentTimeMillis();
        long time;
        int file_size;
        byte[] gcodeBytes;
        int srcPos = 0;
        int offset = MESSAGE_SIZE;
        int destPos = 0;
        int totalMessages = 0;
        int message = 0;
        int totalBytes = 0;
        int totalBlocks = 0;
        String logTransfer = "";

        file_size = (int) gcode.length();
        totalMessages = (int) Math.ceil((double) file_size / (double) MESSAGE_SIZE);
        totalBlocks = (int) Math.ceil((double) file_size / (double) (MESSAGES_IN_BLOCK * MESSAGE_SIZE));

        String fileSizeInfo = "File size: " + file_size + " bytes";
        logTransfer += fileSizeInfo + "\n";
        System.out.println(fileSizeInfo);
        String s_totalMessages = "Number of messages to Transfer: " + totalMessages;
        System.out.println(s_totalMessages);
        logTransfer += s_totalMessages + "\n";
        String s_totalBlocks = "Number of blocks to Transfer: " + totalBlocks;
        System.out.println(s_totalBlocks);
        logTransfer += s_totalBlocks + "\n";

        transferMode = true;

        if (dispatchCommand(INIT_SDCARD, COM.TRANSFER).contains(ERROR)) {
            driverError = true;
            driverErrorDescription = ERROR + ":INIT_SDCARD failed";
            transferMode = false;

            return driverErrorDescription;
        } else {
            Base.writeLog("SD Card init successful");
        }

        //Set file at SDCard
        if (createSDCardFile(gcode).contains(ERROR)) {
            driverError = true;
            driverErrorDescription = ERROR + ":createSDCardFile failed";
            transferMode = false;

            return driverErrorDescription;
        } else {
            Base.writeLog("SD Card file created with success");
        }

        //Set print sessions variables
        if (setVariables(estimatedTime, nLines).contains(ERROR)) {
            driverError = true;
            driverErrorDescription = ERROR + ":setVariables failed";
            transferMode = false;

            return driverErrorDescription;
        } else {
            Base.writeLog("SD Card variables set with success");
        }

        //Stores file in byte array
        gcodeBytes = getBytesFromFile(gcode);
        byte[] iMessage;

        //Send the file 1 block at a time, only send full blocks
        //Each block is MESSAGES_IN_BLOCK messages long        
        for (int block = 1; block < totalBlocks; block++) {
            
            //check if the transfer was canceled
            if (stopTranfer == true) {
                Base.writeLog("Transfer canceled.");
                driverErrorDescription = ERROR + ":Transfer canceled.";
                dispatchCommand("G28");
                transferMode = false;
                isAutonomous = false;
                stopTranfer = false;                
                return driverErrorDescription;
            }

            // size is MAX_BLOCK_SIZE of file_size
            if (setTransferSize(destPos, (destPos + MAX_BLOCK_SIZE) - 1).contains(ERROR)) {
                driverError = true;
                driverErrorDescription = ERROR + ":setTransferSize failed";
                transferMode = false;

                return driverErrorDescription;
            } else {
                Base.writeLog("SDCard space allocated with success");
            }
//            System.out.println("block:" + block + "M28 A" + srcPos + " D" + ((srcPos + MAX_BLOCK_SIZE) - 1));
            for (int i = 0; i < MESSAGES_IN_BLOCK; i++) {

                message++;
                //Get byte array with MESSAGE_SIZE
                time = System.currentTimeMillis();

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

                System.out.println("Message " + message + "/" + totalMessages + " in " + (System.currentTimeMillis() - time) + "ms");
                transferPercentage = (message * 1.0 / totalMessages * 1.0) * 100;
                psAutonomous.updatePrintBar(transferPercentage);
                
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
        if (stopTranfer == true) {
            Base.writeLog("Transfer canceled.");
            driverErrorDescription = ERROR + ":Transfer canceled.";
            dispatchCommand("G28");
            transferMode = false;
            isAutonomous = false;
            stopTranfer = false;
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
            Base.writeLog("SDCard space allocated with success");
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
            transferPercentage = (message * 1.0 / totalMessages * 1.0) * 100;
            psAutonomous.updatePrintBar(transferPercentage);
        }
        System.out.println("Block " + totalBlocks + "/" + totalBlocks);

//        System.out.println("Message " + totalMessages + "/" + totalMessages + " with " + lastMessage.length + " bytes transfered with success");
        loop = System.currentTimeMillis() - loop;
        String statistics = "Transmission sucessfull " + totalBytes + " bytes in " + loop / 1000.0
                + "s : " + totalBytes / (loop / 1000.0) + "kbps\n";
        logTransfer += statistics;
        Base.writeStatistics(logTransfer);
        System.out.println(statistics);

        transferMode = false;

        return RESPONSE_OK;
    }

    public String startPrintAutonomous() {
        //Begin print from SDCard
        if (dispatchCommand(BEGIN_PRINT, COM.BLOCK).contains(ERROR)) {
            driverError = true;
            driverErrorDescription = ERROR + ":BEGIN_PRINT failed";
            transferMode = false;

            return driverErrorDescription;
        } else {
            Base.writeLog("Print begun with success");
        }

        return RESPONSE_OK;
    }

    public AutonomousData getPrintSessionsVariables() {

//        Data:
//        
//        estimatedTime - [0];
//        elapsedTime - [1];
//        nLines - [2];
//        currentNumberLines - [3];
       
        sendCommand(READ_VARIABLES);
        String printSession = readResponse();
        
        String[] data = parseData(printSession);

        return new AutonomousData(data[0], data[1], data[2], data[3], 0);
    }

    private String[] parseData(String printSession) {
        String[] variables = new String[4];

        String re1 = ".*?";	// Non-greedy match on filler
        String re2 = "(\\d+)";	// Integer Number 1
        String re3 = ".*?";	// Non-greedy match on filler
        String re4 = "(\\d+)";	// Integer Number 2
        String re5 = ".*?";	// Non-greedy match on filler
        String re6 = "(\\d+)";	// Integer Number 3
        String re7 = ".*?";	// Non-greedy match on filler
        String re8 = "(\\d+)";	// Integer Number 4
        String re9 = ".*?";	// Non-greedy match on filler
        String re10 = "(ok)";	// Word 1

//        System.out.println("printSession: "+printSession);
        
        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9 + re10, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(printSession);
        if (m.find()) {
            String int1 = m.group(1);
            String int2 = m.group(2);
            String int3 = m.group(3);
            String int4 = m.group(4);
            String word1 = m.group(5);

            variables[0] = int1;
            variables[1] = int2;
            variables[2] = int3;
            variables[3] = int4;
        } else {
            //Matcher failed
            variables[0] = "0";
            variables[1] = "0";
            variables[2] = "0";
            variables[3] = "0";
        }

        return variables;
    }

    private String transferMessage(byte[] iBlock) {
        int sent;
        String command;
        int i = 0;
        String out = "";
        int tries = 10;
        String response;
        response = "";
        ByteRead res;
        long loop = 0;
        int nBytes = iBlock.length;

        out += "Sending \n";

        sent = sendCommandBytes(iBlock);
//        System.out.println("send bytes: " + sent);

        tries = 11;
        while (tries > 0) {
            tries--;
            try {

                out += (response = readResponse()) + "\n";

//                System.out.println("Transfer Response: " + out);

                if (sent == iBlock.length && response.toLowerCase().contains(RESPONSE_TRANSFER_ON_GOING)) {
                    out += response + "\n";
                    break;
                } else if (driverError) {
                    Base.writeLog("recoverFromSDCardTransfer");
                    recoverFromSDCardTransfer();
                    return ERROR + out;
                }

            } catch (Exception ex) {
                if (!(tries > 0)) {
                    out += "Timeout after " + tries + ".\n";
                    Base.writeLog("Transfer to SDCard failed. " + out);
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
            Base.writeLog("Impossible to read GCode file for transfer: " + err);
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
                //test for file created in response
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
            Base.writeLog("M30 failed. File not created " + out);
            return ERROR + out + "M30 failed. File not created";
        } else {
            return FILE_CREATED;
        }

    }

    private String setVariables(String estimatedTime, int nLines) {
        String out = "";
        String response = "";
        int tries = 10;

        String setVariables = SET_VARIABLES + "A" + estimatedTime + " L" + nLines;
        out = dispatchCommand(setVariables, COM.TRANSFER);

        while (tries > 0) {
            try {

                //test for ok in response
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
                    Base.writeLog("Error setting variables " + out);
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
            Base.writeLog("M31 failed. Variables not set: " + out);
            return ERROR + out + "M31 failed. Variables not set";
        }
        return RESPONSE_OK;
    }

    private String setTransferSize(int srcPos, int destPos) {
        int sent;
        String command;
        int i = 0;
        String out = "";
        int tries = 10;
        String response;
        response = "";
        ByteRead res;
        long loop = 0;

        command = TRANSFER_BLOCK + "A" + srcPos + " D" + destPos;
        out += command + "\n";

        out = dispatchCommand(command);

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
                //test for ok in response
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
                    Base.writeLog("Transfer to SDCard failed. " + out);
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
        FileInputStream in = null;

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
     * @param srcBegin The beginning index (inclusive)
     * @param srcEnd The ending index (exclusive)
     * @return The new, populated byte array
     */
    public byte[] subbytes(byte[] source, int srcBegin, int srcEnd) {
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
    public void getBytes(byte[] source, int srcBegin, int srcEnd, byte[] destination,
            int dstBegin) {
        System.arraycopy(source, srcBegin, destination, dstBegin, srcEnd - srcBegin);
    }

    protected int sendCommandWOTest(String next) {

        while (SEND_WAIT > (System.currentTimeMillis() - lastDispathTime)) {
            try {
                Thread.sleep(1, 1);
            } catch (InterruptedException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        lastDispathTime = System.currentTimeMillis();

        //next = clean(next);
        int cmdlen = 0;
        int i = 0;
        // skip empty commands.
        if (next.length() == 0) {
            return 0;
        }
        pipes = GetPipe(m_usbDevice);

        // do the actual send.
        String message = "";

        getEValue(next); // Process each command for extruded material calculation

        message = next + " B N:" + (++ID) + "\n";

        resendQueue.add(new QueueCommand(message, ID));

        try {
            synchronized (m_usbDevice) {
                if (!pipes.isOpen()) {
                    openPipe(pipes);
                }
                pipes.getUsbPipeWrite().syncSubmit(message.getBytes());
                cmdlen = next.length() + 1;
//                System.out.println("SENTWO: " + message.trim())
            }
        } catch (Exception ex) {
            Base.writeLog("Error while sending command " + next + " : " + ex.getMessage() + " : " + ex.getLocalizedMessage());
        }

        if (ProperDefault.get("debugMode").contains("true") && !message.contains("M625")) {
            Base.writeLog("SENT: " + message.trim());
//            
        }

        if (comLog) {
            Base.writecomLog((System.currentTimeMillis() - startTS), "SENT: " + message.trim());
        }

        return cmdlen;
    }

    /**
     * Actually sends command over USB.
     */
    protected int sendCommand(String next) {

        while (SEND_WAIT > (System.currentTimeMillis() - lastDispathTime)) {
            try {
                Thread.sleep(1, 1);
            } catch (InterruptedException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        lastDispathTime = System.currentTimeMillis();
        //next = clean(next);
        int cmdlen = 0;
        int i = 0;
        // skip empty commands.
        if (next.length() == 0) {
            return 0;
        }
        pipes = GetPipe(m_usbDevice);

        // do the actual send.
        String message = next + "\n";
        getEValue(next); // Process each command for extruded material calculation

        if (comLog) {
            long time = (System.currentTimeMillis() - startTS);
//                Base.writecomLog(time,"Error while sending command to BEETHEFIRST: " + ex.getMessage());
        }
//        }

        try {
            synchronized (m_usbDevice) {
                if (!pipes.isOpen()) {
                    openPipe(pipes);
                }
                pipes.getUsbPipeWrite().syncSubmit(message.getBytes());
                cmdlen = next.length() + 1;
//                System.out.println("SENT: " + message.trim());
            }
        } catch (Exception ex) {
//            System.out.println("Error while sending command " + next + " : " + ex.getMessage());
            Base.writeLog("Error while sending command " + next + " : " + ex.getMessage());
        }

        if (ProperDefault.get("debugMode").contains("true") && !message.contains("M625")) {
            Base.writeLog("SENT: " + message.trim());
//            System.err.println
        }

        if (comLog) {
            Base.writecomLog((System.currentTimeMillis() - startTS), "SENT: " + message.trim());
        }

        return cmdlen;
    }

    public String readResponse() {

        while (SEND_WAIT > (System.currentTimeMillis() - lastDispathTime)) {
            try {
                Thread.sleep(1, 1);
            } catch (InterruptedException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        lastDispathTime = System.currentTimeMillis();
        result = "timeout";
        byte[] readBuffer = new byte[1024];

        int nBits = 0;
        try {
            synchronized (m_usbDevice) {

                if (pipes != null) {
                    nBits = pipes.getUsbPipeRead().syncSubmit(readBuffer);
                } else {
                    Base.writeLog("PIPES NULL");
                    System.out.println("PIPES NULL");
                    /**
                     * REDSOFT: Was this causing prints to stop ????
                     */
//                    setInitialized(false);
                    return NOK;
//                    throw new UsbException("Pipe not found");
                }
            }
//            System.out.println(nBits);
        } catch (UsbException ex) {
            // Cable removable
            if (ex.getMessage().contains("LIBUSB_ERROR_NO_DEVICE")) {
                try {
                    Base.writeLog("LIBUSB_ERROR_NO_DEVICE");
                    pipes.close();
                    setInitialized(false);
                } catch (UsbException ex1) {
                    Base.writeLog("USB exception [readResponse]: " + ex1.getMessage());
                } catch (UsbNotActiveException ex1) {
                    Base.writeLog("USB communication not active [readResponse]:" + ex1.getMessage());
                } catch (UsbNotOpenException ex1) {
                    Base.writeLog("USB communication is down [readResponse]:" + ex1.getMessage());
                } catch (UsbDisconnectedException ex1) {
                    Base.writeLog("USB disconnected exception [readResponse]:" + ex1.getMessage());
                }
            }

        } catch (UsbNotActiveException ex) {
            try {
                pipes.close();
            } catch (UsbException ex1) {
                if (comLog) {
                    Base.writecomLog((System.currentTimeMillis() - startTS), "UsbException");
                }
            } catch (UsbNotActiveException ex1) {
                if (comLog) {
                    Base.writecomLog((System.currentTimeMillis() - startTS), "UsbNotActiveException");
                }
            } catch (UsbNotOpenException ex1) {
                if (comLog) {
                    Base.writecomLog((System.currentTimeMillis() - startTS), "UsbNotOpenException");
                }
            } catch (UsbDisconnectedException ex1) {
                if (comLog) {
                    Base.writecomLog((System.currentTimeMillis() - startTS), "UsbDisconnectedException");
                }
            }
        } catch (UsbNotOpenException ex) {
            try {
                pipes.close();
                setInitialized(false);
            } catch (UsbException ex1) {
                if (comLog) {
                    Base.writecomLog((System.currentTimeMillis() - startTS), "UsbException");
                }
            } catch (UsbNotActiveException ex1) {
                if (comLog) {
                    Base.writecomLog((System.currentTimeMillis() - startTS), "UsbNotActiveException");
                }
            } catch (UsbNotOpenException ex1) {
                if (comLog) {
                    Base.writecomLog((System.currentTimeMillis() - startTS), "UsbNotOpenException");
                }
            } catch (UsbDisconnectedException ex1) {
                if (comLog) {
                    Base.writecomLog((System.currentTimeMillis() - startTS), "UsbDisconnectedException");
                }
            }
        } catch (IllegalArgumentException ex) {
            try {
                pipes.close();
                setInitialized(false);
            } catch (UsbException ex1) {
                if (comLog) {
                    Base.writecomLog((System.currentTimeMillis() - startTS), "UsbException");
                }
            } catch (UsbNotActiveException ex1) {
                if (comLog) {
                    Base.writecomLog((System.currentTimeMillis() - startTS), "UsbNotActiveException");
                }
            } catch (UsbNotOpenException ex1) {
                if (comLog) {
                    Base.writecomLog((System.currentTimeMillis() - startTS), "UsbNotOpenException");
                }
            } catch (UsbDisconnectedException ex1) {
                if (comLog) {
                    Base.writecomLog((System.currentTimeMillis() - startTS), "UsbDisconnectedException");
                }
            }
        }

        // 0 is now an acceptable value; it merely means that we timed out
        // waiting for input
        if (nBits < 0) {
        } else {
            try {
                result = new String(readBuffer, 0, nBits, "US-ASCII").trim();
//                System.out.println("nBits -> "+nBits + " result -> "+result);
                //System.out.println(new String(result).trim());
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (ProperDefault.get("debugMode").contains("true") && !result.contains("S:")) {
            Base.writeLog("RECEIVE: " + result.trim() + "\n");
//           System.err.println("RECEIVE: " + result.trim() + "\n");

        }

//        System.out.println("RECEIVE: " + result.trim()+ "\n");
        if (comLog) {
            Base.writecomLog((System.currentTimeMillis() - startTS), "RECEIVE: " + result.trim() + "\n");
            Base.writecomLog((System.currentTimeMillis() - startTS), "\n");
        }

        return result;
    }

    @Override
    public void setDriverError(boolean errorOccured) {
        this.transferMode = false;
        this.driverError = errorOccured;
    }

    public boolean isDriverError() {
        return driverError;
    }

    public boolean isTransferMode() {
        return this.transferMode;
    }

    public String clean(String str) {
        String clean = str;

        // trim whitespace
        clean = clean.trim();

        // remove spaces
        clean = clean.replaceAll(" ", "");

        return clean;
    }

    @Override
    public boolean isFinished() {
        return isBufferEmpty();
    }

    /**
     * Is our buffer empty? If don't have a buffer, its always true.
     */
    @Override
    public boolean isBufferEmpty() {
        /*try
         {
         readResponse();
         } catch (Exception e) {
         }*/
        return (bufferSize == 0);
    }

    @Override
    public void dispose() {
        super.dispose();
        commands = null;
    }

    /**
     * *************************************************************************
     * commands for interfacing with the driver directly
     *
     * @throws RetryException
     * @throws UsbException
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

        sendCommand(cmd);

        cmd = "G1 X" + df.format(p.x()) + " Y" + df.format(p.y()) + " Z"
                + df.format(p.z()) + " F" + df.format(getCurrentFeedrate());

        sendCommand(cmd);
        try {
            super.queuePoint(p);
        } catch (RetryException ex) {
            //Logger.getLogger(UsbPassthroughDriver.class.getName()).log(Level.SEVERE, null, ex);
        }

//        System.out.println("QP: "+readResponse()+" qp");
        readResponse();

    }

    // FIXME: 5D port
    @Override
    public void setCurrentPosition(Point5d p) throws RetryException {
        sendCommand("G92 X" + df.format(p.x()) + " Y" + df.format(p.y()) + " Z"
                + df.format(p.z()));

        super.setCurrentPosition(p);
    }

    public void homeAxes(EnumSet<AxisId> axes) throws RetryException {
        StringBuilder buf = new StringBuilder("G28 ");
        if (axes.contains(AxisId.X)) {
            buf.append("X");
        }
        if (axes.contains(AxisId.Y)) {
            buf.append("Y");
        }
        if (axes.contains(AxisId.Z)) {
            buf.append("Z");
        }
        sendCommand(buf.toString());

        super.homeAxes(axes, false, 0);
    }

    @Override
    public void delay(long millis) {
        int seconds = Math.round(millis / 1000);

        sendCommand("G4 P" + seconds);

        // no super call requried.
    }

    @Override
    public void openClamp(int clampIndex) {
        sendCommand("M11 Q" + clampIndex);

        super.openClamp(clampIndex);
    }

    @Override
    public void closeClamp(int clampIndex) {
        sendCommand("M10 Q" + clampIndex);

        super.closeClamp(clampIndex);
    }

    @Override
    public void enableDrives() throws RetryException {
        sendCommand("M17");

        super.enableDrives();
    }

    @Override
    public void disableDrives() {
        sendCommand("M18");

        try {
            super.disableDrives();
        } catch (UsbNotActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UsbNotOpenException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UsbDisconnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RetryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void changeGearRatio(int ratioIndex) {
        // gear ratio codes are M40-M46
        int code = 40 + ratioIndex;
        code = Math.max(40, code);
        code = Math.min(46, code);

        sendCommand("M" + code);

        super.changeGearRatio(ratioIndex);
    }

    private String _getToolCode() {
        return "T" + machine.currentTool().getIndex() + " ";
    }

    /**
     * *************************************************************************
     * Motor interface functions
     * ************************************************************************
     */
    @Override
    public void setMotorRPM(double rpm) {
        sendCommand(_getToolCode() + "M108 R" + df.format(rpm));
        try {
            super.setMotorRPM(rpm);
        } catch (RetryException ex) {
            Logger.getLogger(UsbPassthroughDriver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setMotorSpeedPWM(int pwm) throws RetryException {
        sendCommand(_getToolCode() + "M108 S" + df.format(pwm));

        super.setMotorSpeedPWM(pwm);
    }

    @Override
    public void enableMotor() throws RetryException {
        String command = _getToolCode();

        if (machine.currentTool().getMotorDirection() == ToolModel.MOTOR_CLOCKWISE) {
            command += "M101";
        } else {
            command += "M102";
        }

        sendCommand(command);

        super.enableMotor();
    }

    @Override
    public void disableMotor() throws RetryException {
        sendCommand(_getToolCode() + "M103");

        super.disableMotor();
    }

    /**
     * *************************************************************************
     * Spindle interface functions
     *
     * @throws RetryException
     * ************************************************************************
     */
    @Override
    public void setSpindleRPM(double rpm) throws RetryException {
        sendCommand(_getToolCode() + "S" + df.format(rpm));

        super.setSpindleRPM(rpm);
    }

    @Override
    public void enableSpindle() throws RetryException {
        String command = _getToolCode();

        if (machine.currentTool().getSpindleDirection() == ToolModel.MOTOR_CLOCKWISE) {
            command += "M3";
        } else {
            command += "M4";
        }

        sendCommand(command);

        super.enableSpindle();
    }

    @Override
    public void disableSpindle() throws RetryException {
        sendCommand(_getToolCode() + "M5");

        super.disableSpindle();
    }

    /**
     * *************************************************************************
     * Temperature interface functions
     *
     * @throws RetryException
     * ************************************************************************
     */
    @Override
    public void setTemperature(double temperature) throws RetryException {
        //sendCommand("M104 S" + df.format(temperature));        
        dispatchCommand("M104 S" + df.format(temperature));
        super.setTemperature(temperature);
    }

    @Override
    public void readStatus() {

        machineReady = dispatchCommand(GET_STATUS).contains(STATUS_OK);
//        System.out.println("machineReady: "+machineReady);
        //machine.currentTool().setCurrentTemperature(temperature);        
        machine.setMachineReady(machineReady);
    }

    @Override
    public Point5d getPosition() {

        Point5d myCurrentPosition = null;

        String position = dispatchCommand(GET_POSITION, COM.BLOCK);
//        System.out.println("position "+position);
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
            String w1 = m.group(1);
            String c1 = m.group(2);
            String ws1 = m.group(3);
            String w2 = m.group(4);
            String c2 = m.group(5);
            String float1 = m.group(6);
            String ws2 = m.group(7);
            String w3 = m.group(8);
            String c3 = m.group(9);
            String float2 = m.group(10);
            String ws3 = m.group(11);
            String w4 = m.group(12);
            String c4 = m.group(13);
            String float3 = m.group(14);
            String ws4 = m.group(15);
            String w5 = m.group(16);
            String float4 = m.group(17);
            myCurrentPosition = new Point5d(Double.valueOf(float1), Double.valueOf(float2), Double.valueOf(float3), Double.valueOf(float4), 0);
        }
//        synchronized (currentPosition) {
        currentPosition.set(myCurrentPosition);
//        }

        return myCurrentPosition;
    }

    @Override
    public void readTemperature() {
        //sendCommand(_getToolCode() + "M105");
        String temp = dispatchCommand("M105");
        double temperature = 0.0;

        String re1 = "(T)";	// Variable Name 1
        String re2 = "(:)";	// Any Single Character 1
        String re3 = "([+-]?\\d*\\.\\d+)(?![-+0-9\\.])";	// Float 1
        String re4 = "(\\s+)";	// White Space 1
        String re5 = "((?:[a-z][a-z0-9_]*))";	// Variable Name 2
        String re6 = "(.)";	// Any Single Character 2
        String re7 = "([+-]?\\d*\\.\\d+)(?![-+0-9\\.])";	// Float 2

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(temp);
        if (m.find()) {
            String var1 = m.group(1);
            String c1 = m.group(2);
            String float1 = m.group(3);
            String ws1 = m.group(4);
            String var2 = m.group(5);
            String c2 = m.group(6);
            String float2 = m.group(7);

            temperature = Double.valueOf(float1);
        } else {
            temperature = -1.0;
        }

        machine.currentTool().setCurrentTemperature(temperature);

    }

    /**
     * *************************************************************************
     * Flood Coolant interface functions
     * ************************************************************************
     */
    @Override
    public void enableFloodCoolant() {
        sendCommand(_getToolCode() + "M7");

        super.enableFloodCoolant();
    }

    @Override
    public void disableFloodCoolant() {
        sendCommand(_getToolCode() + "M9");

        super.disableFloodCoolant();
    }

    /**
     * *************************************************************************
     * Mist Coolant interface functions
     * ************************************************************************
     */
    @Override
    public void enableMistCoolant() {
        sendCommand(_getToolCode() + "M8");

        super.enableMistCoolant();
    }

    @Override
    public void disableMistCoolant() {
        sendCommand(_getToolCode() + "M9");

        super.disableMistCoolant();
    }

    /**
     * *************************************************************************
     * Fan interface functions
     *
     * @throws RetryException
     * ************************************************************************
     */
    @Override
    public void enableFan() throws RetryException {
        sendCommand(_getToolCode() + "M106");

        super.enableFan();
    }

    @Override
    public void disableFan() throws RetryException {
        sendCommand(_getToolCode() + "M107");

        super.disableFan();
    }

    /**
     * *************************************************************************
     * Valve interface functions
     *
     * @throws RetryException
     * ************************************************************************
     */
    @Override
    public void openValve() throws RetryException {
        sendCommand(_getToolCode() + "M126");

        super.openValve();
    }

    @Override
    public void closeValve() throws RetryException {
        sendCommand(_getToolCode() + "M127");

        super.closeValve();
    }

    /**
     * *************************************************************************
     * Collet interface functions
     *
     * @throws UsbException
     * @throws UsbDisconnectedException
     * @throws IllegalArgumentException
     * @throws UsbNotOpenException
     * @throws UsbNotActiveException
     * ************************************************************************
     */
    @Override
    public void openCollet() {
        sendCommand(_getToolCode() + "M21");

        super.openCollet();
    }

    @Override
    public void closeCollet() {
        sendCommand(_getToolCode() + "M22");

        super.closeCollet();
    }

    @Override
    public void reset() {
        Base.logger.info("Reset.");
        setInitialized(false);
        initialize();
    }

    @Override
    protected Point5d reconcilePosition() {
        return new Point5d();
    }

    @Override
    public boolean isInitialized() {
        if (super.isInitialized() && testPipes(pipes)) {
            return true;
        } else {
            return false;
        }
    }

    private String checkFirmwareType() {
        Base.writeLog("testing bootloader");
        //Clear message stack
        
        sendCommand(GET_STATUS);
        hiccup(30, 0);
        String res = readResponse();
        if (res.toLowerCase().contains(STATUS_SDCARD)) {
            Base.writeLog("Is not bootloader - autonomous: "+STATUS_SDCARD);
//            System.out.println("autonomous");
            return "autonomous";
        }else if (res.isEmpty()) {
            return "error";
        }
        sendCommand("M300");
        hiccup(30, 0);
        readResponse();
        
//        System.out.println(res);
        if (res.toLowerCase().contains("bad")) {
            sendCommand("M116");
            hiccup(10, 0);
            res = readResponse();
            if (res.toLowerCase().contains("3.")) {
                Base.writeLog("Is bootloader 3");
                return "bootloader";
            }
            if (res.toLowerCase().contains("4.")) {
                Base.writeLog("Is bootloader 4");
                return "new bootloader";
            } else {
                Base.writeLog(" Not bootloader available");
                return "error";
            }

        } else if (res.toLowerCase().contains("ok") && !res.toLowerCase().contains("bad")) {
            Base.writeLog("Is not bootloader");
            return "firmware";
        }
        return "error";
    }

    private void setFirmware(String firmwareName) {
        String file_name = "";
        if (firmwareName.contains("LT")) {
            Base.writeLog("Flashing..");

            file_name = Base.getApplicationDirectory() + "/firmware/firmware_usb_lt.bin";
            int res = flashAndCheck(file_name, -1);
//            System.out.println(res);
        } else if (firmwareName.contains("V1")) {
            file_name = Base.getApplicationDirectory() + "/firmware/firmware2.0.1.bin";
            Base.writeLog("Flashing..");

            int res = flashAndCheck(file_name, -1);
//            System.out.println(res);
        }
    }

    private int flashAndCheck(String filename, int nBytes) {

        FileInputStream in = null;
        long loop;
        int file_size;
        int c = 0;
        int sent;
        ByteRead res;
        boolean state;
        String command;

        String out = "";

        File f = new File(filename);
//        System.out.println();
        if (!f.isFile() || !f.canRead()) {
            Base.writeLog("File not found or unreadable for flash.\n");
        }

        file_size = (int) f.length();

        try {
            in = new FileInputStream(f);
        } catch (FileNotFoundException ex) {
            Base.writeLog("File not found or unreadable.");
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

        //test for ok in response
        if (!response.toLowerCase().contains("ok")) {
//            System.out.println("M650 failed. Response not OK");
        }

//         System.out.println("Sending");
        loop = System.nanoTime();
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
//                     System.out.println("Sent " + sent + " of " + byteMessage.size + " bytes.");
//                    System.out.println("Transfer failure.");
                }

//                System.out.println("Sent " + sent + " of " + byteMessage.size + " bytes.");
//                System.out.println("Verifying");
//                System.out.println(out);
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
                                Base.writeLog("Transmission error found, reboot R2C2.");
                                return -1;
                            } else {
//                                System.out.println("ok");
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        if (!(tries > 0)) {
                            Base.writeLog("Timeout after " + tries);
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

//        loop = System.nanoTime() - loop;
//        System.out.println("Transmission sucessfull " + file_size + "bytes in " + loop / 1000000000.0
//                + "s : " + file_size / (loop / 1000000.0) + "kbps");
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

        pipes = GetPipe(m_usbDevice);

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

        int nBits = 0;

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

        //get bootloader version
        sendCommand(GET_BOOTLOADER_VERSION);
        hiccup(QUEUE_WAIT, 0);
        String bootloader = readResponse();

        if (isNewVendorID) {
            bootloader_version = new Version().fromMachine4(bootloader);
        } else {
            bootloader_version = new Version().fromMachine3(bootloader);
        }

        System.out.println(GET_BOOTLOADER_VERSION + ": " + bootloader + " : " + bootloader_version.toString());

        //Default
        firmware_version = new Version().fromMachine3("No firmware version available.");

        //get serial number - Must have exactly 10 chars!
        serialNumberString = "0000000000";
        try {
            serialNumberString = m_usbDevice.getSerialNumberString();
            System.out.println("Serial Number: " + serialNumberString);
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
            sendCommand(GET_FIRMWARE_VERSION);
            hiccup(10, 0);
            String firmware = readResponse();

            if (isNewVendorID) {
                firmware_version = new Version().fromMachine4(firmware);
            } else {
                firmware_version = new Version().fromMachine3(firmware);
            }

            System.out.println("firmware_version: " + firmware_version);
        } else {
            firmware_version = new Version().fromMachine3("No firmware version available.");
        }

        //check if the firmware is properly installed
        sendCommand(GET_FIRMWARE_OK);
        hiccup(QUEUE_WAIT, 0);
        String firmware_is_ok = readResponse();
        if (firmware_is_ok.contains(FIRMWARE_IS_OK) == false) {
            firmware_version = new Version().fromMachine3("No firmware version available.");
        } //no need for else

    }

    private void updateMachineInfo() {

//        //get bootloader version
//        sendCommand(GET_BOOTLOADER_VERSION);
//        hiccup(10, 0);
//        String bootloader = readResponse();
//        bootloader_version = new Version().fromMachine3(bootloader);
//
//        System.out.println(GET_BOOTLOADER_VERSION + ": " + bootloader + ":" + bootloader_version.toString());
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
            sendCommand(GET_FIRMWARE_VERSION);
            hiccup(10, 0);
            String firmware = readResponse();
            firmware_version = new Version().fromMachineAtFirmware(firmware);
//            System.out.println("firmware_version: " + firmware_version);

        } else {
            firmware_version = new Version().fromMachineAtFirmware("No firmware version available.");
        }
    }

    private void updateFirmware() {

        String versionToCompare = "no_version_available";
        if (isNewVendorID) {
            versionToCompare = Base.VERSION_FIRMWARE_FINAL;
        } else {
            versionToCompare = Base.VERSION_FIRMWARE_FINAL_OLD;
        }

        //check if the firmware is the right one
        if (firmware_version.getVerionString().contains(versionToCompare) == true) {
            return;
        } // else carry on updating

        File folder = new File(Base.getApplicationDirectory().toString() + "/firmware/");
        File[] firmwarelist = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".bin");
            }
        });

        File firmwareFile = null;

        for (int i = 0; i < firmwarelist.length; i++) {
            System.out.println(firmwarelist[i].getAbsoluteFile());

            if (firmwarelist[i].getName().contains(versionToCompare)) {
                firmwareFile = firmwarelist[i];
                Base.writeLog("Firmware update necessary." + "f:" + firmwareFile);
                break;
            }
        }

        if (firmwareFile != null) {
            Base.writeLog("Starting Firmware update.");
            if (flashAndCheck(firmwareFile.getAbsolutePath(), -1) > 0) {
                Base.writeLog("Firmware succefully updated");
                //setting version
                sendCommand(SET_FIRMWARE_VERSION + versionToCompare);
                firmware_version = new Version().fromFile(firmwareFile.getName());
                hiccup(QUEUE_WAIT, 0);
                readResponse();

                if (serialNumberString.contains(NO_SERIAL_NO_FIRMWARE)) {
                    setSerial(NO_SERIAL_FIRMWARE_OK);
                    hiccup();

                }//no need for else
            } else {
                Base.writeLog("Firmware update failed");
                Base.errorOccured = true;
            }
        }

    }

    private void setSerial(String serial) {

        if (serial.length() == SERIAL_NUMBER_SIZE) {
            try {
                Integer.parseInt(serial);
                sendCommand(SET_SERIAL + serial);
                hiccup(10, 0);
                readResponse();
            } catch (Exception ex) {
            }
        }
    }
}
