package pt.beeverycreative.beesoft.drivers.usb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbNotOpenException;
import org.w3c.dom.Node;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
import replicatorg.app.Base;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.panels.Feedback;
import replicatorg.app.ui.panels.PauseMenu;
import replicatorg.app.ui.panels.PrintSplashAutonomous;
import replicatorg.app.ui.panels.SerialNumberInput;
import replicatorg.app.ui.panels.ShutdownMenu;
import replicatorg.app.ui.panels.Warning;
import replicatorg.app.util.AutonomousData;
import replicatorg.drivers.RetryException;
import replicatorg.machine.model.MachineModel;
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

    private static final String GET_STATUS = "M625";
    private static final String GET_STATUS_FROM_ERROR = GET_STATUS + new String(new char[507]).replace("\0", "A");
    private static final String GET_POSITION = "M121";
    private static final String LAUNCH_FIRMWARE = "M630";
    private static final String SET_FIRMWARE_VERSION = "M114 A";
    private static final String INVALID_FIRMWARE_VERSION = "0.0.0";
    private static final String GET_FIRMWARE_VERSION = "M115";
    private static final String STATUS_OK = "S:3";
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
    private static final String SET_COIL_TEXT = "M1000 ";
    private static final String GET_COIL_TEXT = "M1001";
    private static final String GET_NOZZLE_TYPE = "M1028";
    private static final String SAVE_CONFIG = "M601 ";
    private static final String NOK = "NOK";
    private static final int QUEUE_WAIT = 1000;
    private static Version bootloaderVersion = new Version();
    private Version firmwareVersion = new Version();
    private String serialNumberString = "9999999999";
    private long startTS;
    private String driverErrorDescription;
    private boolean stopTransfer = false;
    private static boolean bootedFromBootloader = false;
    private static boolean backupConfig = false;
    private static int backupNozzleSize = FilamentControler.DEFAULT_NOZZLE_SIZE;
    private static String backupCoilText = FilamentControler.NO_FILAMENT;
    private static double backupZVal = 123.495;
    private static final Feedback feedbackWindow = new Feedback();

    public enum COM {

        DEFAULT, BLOCK, TRANSFER, NO_RESPONSE, NO_OK
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
        Base.SERIAL_NUMBER = null;
    }

    @Override
    public void closeFeedback() {
        Base.rebootingIntoFirmware = false;

        if (feedbackWindow != null) {
            feedbackWindow.dispose();
        }
    }

    @Override
    public int getQueueSize() {
        return 0;
    }

    /**
     * Send any gcode needed to synchronize the driver type and the firmware
     * type. Sent on startup and if we see "start" indicating an uncommanded
     * firmware reset.
     */
    public void sendInitializationGcode() {
        Base.writeLog("Sending initialization GCodes", this.getClass());
        String status = checkPrinterStatus();
        setMachine(new MachineModel());

        super.isBootloader = true;

        if (status.contains("bootloader")) {
            bootedFromBootloader = true;
            updateBootloaderInfo();
            if (updateFirmware() >= 0) {

                Base.writeLog("Bootloader version: " + bootloaderVersion, this.getClass());
                Base.writeLog("Firmware version: " + firmwareVersion, this.getClass());
                Base.writeLog("Serial number: " + serialNumberString, this.getClass());

                super.isBootloader = true;

                Base.writeLog("Launching firmware!", this.getClass());
                feedbackWindow.setFeedback2(Feedback.LAUNCHING_MESSAGE);
                Base.rebootingIntoFirmware = true;
                hiccup(3000, 0);
                dispatchCommand(LAUNCH_FIRMWARE, COM.NO_RESPONSE); // Launch firmware
                hiccup(3000, 0);
                closePipe(pipes);
            } else {
                status = "error";
            }
        } else if (status.contains("autonomous")) {
            super.isBootloader = false;

            updateCoilText();
            updateNozzleType();
            closeFeedback();
            Base.updateVersions();

            // we want it to be document modal (that is, to block the underlaying windows)
            // but we don't want it to be stuck here. is there a better way?
            Thread t = new Thread(() -> {
                if (isONShutdown) {
                    ShutdownMenu shutdown = new ShutdownMenu();
                    shutdown.setVisible(true);
                } else if (Base.printPaused) {
                    PauseMenu pause = new PauseMenu();
                    pause.setVisible(true);
                } else {
                    PrintSplashAutonomous p = new PrintSplashAutonomous(
                            Base.printPaused, isONShutdown
                    );
                    p.setVisible(true);
                }
            });
            t.start();

        } else if (status.contains("firmware")) {

            // this is made just to be sure that the bootloader version
            // isn't inconsistent
            if (bootedFromBootloader == false) {
                bootloaderVersion = new Version();
            } else {
                bootedFromBootloader = false;
            }

            super.isBootloader = false;

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
                Warning firmwareOutDate = new Warning(true);
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
                setInstalledNozzleSize(backupNozzleSize);
                dispatchCommand("M604 Z" + backupZVal);
                backupConfig = false;
                backupZVal = 123.495;
                backupCoilText = FilamentControler.NO_FILAMENT;
                backupNozzleSize = FilamentControler.DEFAULT_NOZZLE_SIZE;
            } else {
                updateCoilText();
                updateNozzleType();
            }

            Base.isPrinting = false;

            dispatchCommand("G28", COM.DEFAULT);
            closeFeedback();
            setBusy(false);

            Base.updateVersions();
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
            sendInitializationGcode();
        }
    }

    private String dispatchCommand(byte[] byteArray) {
        String ans;
        int retryNum;

        ans = "";
        retryNum = 30;

        if (byteArray == null) {
            return "";
        }

        dispatchCommandLock.lock();
        try {
            sendCommandBytes(byteArray);

            while (retryNum > 0) {
                ans += readResponseTog();

                if (ans.contains("tog")) {
                    break;
                } else {
                    Base.hiccup(1000);
                    retryNum--;
                }
            }
        } finally {
            dispatchCommandLock.unlock();
        }

        return ans;
    }

    @Override
    public String dispatchCommand(String next) {

        String ans;
        int retryNum;

        ans = "";
        retryNum = 5;

        if (next == null) {
            return "";
        }

        dispatchCommandLock.lock();
        try {
            sendCommand(next);

            while (retryNum > 0) {
                ans += readResponse();

                if (ans.contains("ok")) {
                    break;
                } else {
                    Base.hiccup(100);
                    retryNum--;
                }
            }

        } finally {
            dispatchCommandLock.unlock();
        }
        return ans;
    }

    private String dispatchCommandNoOK(String next) {

        String ans;

        ans = "";

        if (next == null) {
            return "";
        }

        dispatchCommandLock.lock();
        try {
            sendCommand(next);
            ans = readResponse();
        } finally {
            dispatchCommandLock.unlock();
        }
        return ans;
    }

    private void dispatchCommandNoResponse(String next) {
        if (next == null) {
            return;
        }

        dispatchCommandLock.lock();
        try {
            sendCommand(next);
        } finally {
            dispatchCommandLock.unlock();
        }
    }

    @Override
    public String dispatchCommand(final String next, COM comType) {
        //check if the queue is getting full EX: ok Q:0

        /**
         * Parses Bad GCodes or pipes garbage
         */
        if (next.startsWith(";")) {
            return next + " did not send";
        }

        String answer = "";
        switch (comType) {
            case NO_RESPONSE:
                new Thread(() -> {
                    dispatchCommandNoResponse(next);
                }).start();
                return null;
            case NO_OK:
                answer = dispatchCommandNoOK(next);
                break;
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

                answer = dispatchCommand(next);

                break;

        }

        if (answer.contains(NOK) == false) {
            return answer;
        } else {
            return ERROR;
        }
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
        return serialNumberString;
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

    /**
     * Define what nozzle size is installed in the printer
     *
     * @param microns size of the nozzle in microns
     */
    @Override
    public void setInstalledNozzleSize(int microns) {
        String response;

        response = dispatchCommand("M1027 S" + microns, COM.BLOCK);

        if (response.toLowerCase().contains("ok")) {
            machine.setNozzleType(microns);
        } else {
            machine.setNozzleType(0);
        }

        dispatchCommand(SAVE_CONFIG, COM.BLOCK);
    }

    /**
     * Gets the filament name from the printer
     *
     */
    @Override
    public void updateCoilText() {
        String coilText, coilTextLowerCase;

        coilText = dispatchCommand(GET_COIL_TEXT);
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

    /**
     * Reads the nozzle size obtained from the printer and sets it in the
     * machine model
     */
    @Override
    public void updateNozzleType() {
        Pattern p;
        Matcher m;
        String nozzleType, re1, re2, re3, re4, re5;
        int nozzleSizeMicrons;

        nozzleType = dispatchCommand(GET_NOZZLE_TYPE);
        re1 = "(Nozzle)";
        re2 = "(\\s+)";
        re3 = "(Size)";
        re4 = "(:)";
        re5 = "(\\d+)";
        p = Pattern.compile(re1 + re2 + re3 + re4 + re5, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        m = p.matcher(nozzleType);
        if (m.find()) {
            nozzleSizeMicrons = Integer.parseInt(m.group(5));
        } else {
            nozzleSizeMicrons = FilamentControler.DEFAULT_NOZZLE_SIZE;
        }

        Base.writeLog("Nozzle type: " + nozzleSizeMicrons, this.getClass());
        machine.setNozzleType(nozzleSizeMicrons);
    }

    /**
     * Sets the stopTransfer flag to true.
     */
    @Override
    public void stopTransfer() {
        stopTransfer = true;
    }

    /**
     * Obtain the home_pos_z value from the output of M600 command. This value
     * is how the calibration of the printer is defined.
     */
    @Override
    public void readZValue() {
        String response, home_pos_z, re1, re2, re3, re4, re5, re6;
        Pattern p;
        Matcher m;

        response = dispatchCommand("M600");
        re1 = ".*?";	// Non-greedy match on filler
        re2 = "(home_pos_z)";	// Variable Name 1
        re3 = "(\\s+)";	// White Space 1
        re4 = "(=)";	// Any Single Character 1
        re5 = "(\\s+)";	// White Space 2
        re6 = "([+-]?\\d*\\.\\d+)(?![-+0-9\\.])";	// Float 1

        p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        m = p.matcher(response);
        if (m.find()) {
            home_pos_z = m.group(5);
        } else {
            home_pos_z = "123.495";
        }

        machine.setzValue(Double.valueOf(home_pos_z));
    }

    /**
     * Transfers a GCode file. A PrintSplashAutonomous object is requested in
     * order to provide transfer progress feedback to that dialog. TODO: the
     * panel should obtain feedback another way!
     *
     * @param gcode the GCode file that is to be transferred
     * @param psAutonomous PrintSplashAutonomous object
     * @param header header to be included in the file when transferred (e.g.:
     * M31). If null, no header is included
     * @return error message
     */
    @Override
    public String gcodeTransfer(File gcode, PrintSplashAutonomous psAutonomous, String header) {

        long loop = System.currentTimeMillis();
        byte[] gcodeBytes;
        int file_size, srcPos, totalBlocks, offset = MESSAGE_SIZE, destPos = 0,
                totalMessages, message = 0, totalBytes = 0;
        double transferPercentage, transferSpeed;

        file_size = (int) gcode.length();
        totalMessages = (int) Math.ceil((double) file_size / (double) MESSAGE_SIZE);
        totalBlocks = (int) Math.ceil((double) file_size / (double) (MESSAGES_IN_BLOCK * MESSAGE_SIZE));

        transferMode = true;

        dispatchCommandLock.lock();
        try {
            if (dispatchCommand(INIT_SDCARD, COM.TRANSFER).contains(ERROR)) {
                driverErrorDescription = ERROR + ":INIT_SDCARD failed";
                transferMode = false;
                return driverErrorDescription;
            } else {
                Base.writeLog("SD Card init successful", this.getClass());
            }

            //Set file at SDCard
            if (createSDCardFile(gcode).contains(ERROR)) {
                driverErrorDescription = ERROR + ":createSDCardFile failed";
                transferMode = false;
                return driverErrorDescription;
            } else {
                Base.writeLog("SD Card file created with success", this.getClass());
            }

            //Stores file in byte array
            gcodeBytes = getBytesFromFile(gcode);
            byte[] iMessage;

            if (header != null && !header.equals("") && header.length() < MAX_BLOCK_SIZE) {
                if (setTransferSize(destPos, (destPos + header.length()) - 1).contains(ERROR)) {
                    driverErrorDescription = ERROR + ":setTransferSize failed";
                    transferMode = false;
                    return driverErrorDescription;
                } else {
                    Base.writeLog("SDCard space for header allocated with success", this.getClass());
                    destPos += header.length();
                }

                if (dispatchCommand(header.getBytes()).contains("tog") == false) {
                    Base.writeLog("Header transfer failure, 0 bytes sent.", this.getClass());
                    return driverErrorDescription;
                }
            }

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
                    driverErrorDescription = ERROR + ":setTransferSize failed";
                    transferMode = false;
                    return driverErrorDescription;
                } else {
                    Base.writeLog("SDCard space allocated with success", this.getClass());
                }

                Base.hiccup(100);

//            System.out.println("block:" + block + "M28 A" + srcPos + " D" + ((srcPos + MAX_BLOCK_SIZE) - 1));
                for (int i = 0; i < MESSAGES_IN_BLOCK; i++) {

                    message++;

                    // Updates variables
                    srcPos = destPos;
                    destPos = srcPos + offset;
                    iMessage = subbytes(gcodeBytes, srcPos, destPos);
                    if (dispatchCommand(iMessage).contains("tog") == false) {
                        Base.writeLog("Transfer failure, 0 bytes sent.", this.getClass());
                        return driverErrorDescription;
                    }

                    //System.out.println("Message " + message + "/" + totalMessages + " in " + (System.currentTimeMillis() - time) + "ms");
                    transferPercentage = ((double) message / totalMessages) * 100;

                    if (Base.printPaused == false && psAutonomous != null) {
                        psAutonomous.updatePrintBar((int) transferPercentage);
                    }
                }

            }

            srcPos = destPos;

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
                driverErrorDescription = ERROR + ":setTransferSize failed";
                transferMode = false;

                return driverErrorDescription;
            } else {
                Base.writeLog("SDCard space allocated with success", this.getClass());
            }

            Base.hiccup(100);

            for (; srcPos < file_size; srcPos += offset) {
                message++;
                //Get byte array with MESSAGE_SIZE
                iMessage = subbytes(gcodeBytes, srcPos, Math.min(srcPos + offset, file_size));

                //sendCommandBytes(iMessage);
                if (dispatchCommand(iMessage).contains("tog") == false) {
                    Base.writeLog("Transfer failure, 0 bytes sent.", this.getClass());
                    return driverErrorDescription;
                }

                transferPercentage = ((double) message / totalMessages) * 100;
                if (psAutonomous != null) {
                    psAutonomous.updatePrintBar((int) transferPercentage);
                }
            }

            loop = System.currentTimeMillis() - loop;
            transferSpeed = totalBytes / (loop / 1000.0);
            Base.writeLog("Transmission sucessfull " + totalBytes + " bytes in " + loop / 1000.0
                    + "s : " + transferSpeed + "kbps\n", this.getClass());

            transferMode = false;
        } finally {
            dispatchCommandLock.unlock();
        }

        return RESPONSE_OK;
    }

    @Override
    public void startPrintAutonomous() {
        dispatchCommand(BEGIN_PRINT);
    }

    @Override
    public void getPrintSessionsVariables() {
//        Data:
//        estimatedTime - [0];
//        elapsedTime - [1];
//        nLines - [2];
//        currentNumberLines - [3];
        String printSession;
        String[] data;

        printSession = dispatchCommand(READ_VARIABLES, COM.NO_OK);
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
        String aValue = "-1";
        String bValue = "-1";
        String cValue = "-1";
        String dValue = "-1";

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

    private String createSDCardFile(File gcode) {

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

        if (out.contains(FILE_CREATED) == false) {
            out += response + "\n";
            Base.writeLog("M30 failed. File not created " + out, this.getClass());
            return ERROR + out + "M30 failed. File not created";
        } else {
            return FILE_CREATED;
        }

    }

    private String setTransferSize(int srcPos, int destPos) {

        String command, response;

        command = TRANSFER_BLOCK + "A" + srcPos + " D" + destPos;

        response = dispatchCommand(command);
        //sendCommand(command);
        //hiccup(100, 0);
        //out = readResponse();

        if (response.contains(RESPONSE_OK)) {
            return RESPONSE_OK;
        } else {
            return ERROR + response + "\nM28 failed. Response not OK";
        }

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

        if (comLog) {
            Base.writeComLog((System.currentTimeMillis() - startTS), "SENT: " + message.trim());
        }

        return cmdlen;
    }

    @Override
    public String readResponse() {

        result = "timeout";
        byte[] readBuffer = new byte[1024];

        int nBits = 0;
        try {
            if (m_usbDevice != null) {

                if (pipes != null) {
                    nBits = pipes.getUsbPipeRead().syncSubmit(readBuffer);
                } else {
                    Base.writeLog("PIPES NULL", this.getClass());
                    setInitialized(false);
                    return NOK;
                    //throw new UsbException("Pipe was null");
                }
            }
        } catch (Exception ex) {
            setInitialized(false);
        }

        // 0 is now an acceptable value; it merely means that we timed out
        // waiting for input
        if (nBits >= 0) {
            try {
                result = new String(readBuffer, 0, nBits, "US-ASCII").trim();
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //if (comLog && result.equals("") == false && result.contains("S:") == false) {
        Base.writeComLog((System.currentTimeMillis() - startTS), "RECEIVE (" + result.length() + "): " + result.trim() + "\n");
        //}

        return result;
    }

    private String readResponseTog() {

        result = "timeout";
        byte[] readBuffer = new byte[1024];

        int nBits = 0;
        try {
            if (m_usbDevice != null) {

                if (pipes != null) {
                    nBits = pipes.getUsbPipeRead().syncSubmit(readBuffer);
                } else {
                    Base.writeLog("PIPES NULL", this.getClass());
                    setInitialized(false);
                    return NOK;
                    //throw new UsbException("Pipe was null");
                }
            }
        } catch (Exception ex) {
            setInitialized(false);
        }

        // 0 is now an acceptable value; it merely means that we timed out
        // waiting for input
        if (nBits >= 0) {
            try {
                result = new String(readBuffer, 0, nBits, "US-ASCII").trim();
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //if (comLog && result.equals("") == false && result.contains("S:") == false) {
        //Base.writeComLog((System.currentTimeMillis() - startTS), "RECEIVE (" + result.length() + "): " + result.trim() + "\n");
        //}
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
     * ************************************************************************
     */
    @Override
    public void setTemperature(int temperature) {
        dispatchCommand("M104 S" + temperature);
        super.setTemperature(temperature);
    }

    @Override
    public void setTemperatureBlocking(int temperature) {
        dispatchCommand("M109 S" + temperature);
        super.setTemperature(temperature);
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
        isAlive = testPipes(pipes);

        if (!isAlive) {
            if (Base.rebootingIntoFirmware == false) {
                Base.getMainWindow().getButtons().setMessage("is disconnected");
                Base.disposeAllOpenWindows();
            }
            Base.isPrinting = false;
            Base.printPaused = false;
            resetBootloaderAndFirmwareVersion();
            dispose();
        }

        return isAlive;
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

        FileInputStream in;
        int file_size, sent;
        ByteRead res;
        String command;

        File f = new File(filename);
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

        command = "M650 A" + file_size;
        String response = dispatchCommand(command);
        if (response.toLowerCase().contains("ok") == false) {
            return -1;
        }

        int bytesRead = 0;

        byte[] byteTemp = new byte[64];
        ByteRead byteMessage;
        byteMessage = new ByteRead(64, new byte[0]);
        dispatchCommandLock.lock();
        try {
            try {
                while (((byteMessage.size = in.read(byteTemp)) != -1)
                        && (nBytes == -1 || (bytesRead < nBytes))) {
                    bytesRead += byteMessage.size;
                    byteMessage = new ByteRead(byteMessage.size, new byte[byteMessage.size]);
                    System.arraycopy(byteTemp, 0, byteMessage.byte_array, 0, byteMessage.size);

                    sent = sendCommandBytes(byteMessage.byte_array);
                    if (sent != byteMessage.size) {
                        Base.writeLog("Transfer failure, incorrect number of bytes sent.", this.getClass());
                        feedbackWindow.dispose();
                        return -1;
                    }

                    res = new ByteRead(0, new byte[byteMessage.size]);
                    try {
                        ByteRead tempMessage = readBytes(sent);
                        System.arraycopy(tempMessage.byte_array, 0,
                                res.byte_array, res.size, tempMessage.size);
                        res.size += tempMessage.size;

                        if (res.size == byteMessage.size) {
                            if (!Arrays.equals(res.byte_array, byteMessage.byte_array)) {
                                Base.writeLog("Transmission error found, reboot BEETHEFIRST.", this.getClass());
                                return -1;
                            }
                        }
                    } catch (Exception ex) {
                    }
                }
            } catch (IOException ex) {
            }

            return 1;
        } finally {
            dispatchCommandLock.unlock();
        }
    }

    /**
     * Send command to Machine
     *
     * @param next Command
     * @return command length
     */
    private int sendCommandBytes(byte[] next) {

        int cmdlen = 0;

        dispatchCommandLock.lock();
        try {
            if (!pipes.isOpen()) {
                openPipe(pipes);
            }
            cmdlen = pipes.getUsbPipeWrite().syncSubmit(next);
        } catch (UsbException | UsbNotActiveException | UsbNotOpenException | IllegalArgumentException | UsbDisconnectedException ex) {
            // do nothing
        } finally {
            dispatchCommandLock.unlock();
        }
        return cmdlen;
    }

    private ByteRead readBytes(int responseSize) throws UsbException {

        int indexRead, nBits;
        byte[] resultByteArray = new byte[responseSize];
        byte[] readBuffer = new byte[responseSize];

        indexRead = 0;
        do {
            nBits = pipes.getUsbPipeRead().syncSubmit(readBuffer);
            System.arraycopy(readBuffer, 0, resultByteArray, indexRead, nBits);
            indexRead += nBits;
        } while (indexRead < responseSize);

        return new ByteRead(nBits, resultByteArray);
    }

    private void updateBootloaderInfo() {

        String bootloader, firmware;
        boolean validSerial = false;
        boolean cancelPressed;

        bootloader = dispatchCommand("M116");
        bootloaderVersion = Version.bootloaderVersion(bootloader);

        //Default
        firmwareVersion = new Version();

        try {
            serialNumberString = m_usbDevice.getSerialNumberString().trim();

        } catch (UsbException | UnsupportedEncodingException | UsbDisconnectedException ex) {
            Logger.getLogger(UsbPassthroughDriver.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        if (isSerialValid() == false) {
            Base.writeLog("Current serial number of this printer is invalid, requesting number to user...", this.getClass());
            while (validSerial == false) {

                SerialNumberInput serialNumberInput = new SerialNumberInput();

                serialNumberInput.setVisible(true);
                validSerial = serialNumberInput.isSerialValid();
                cancelPressed = serialNumberInput.hasCancelBeenPressed();

                if (validSerial) {
                    dispatchCommand("M118 T" + serialNumberInput.getSerialString());
                    serialNumberString = serialNumberInput.getSerialString();
                }

                if (cancelPressed) {
                    Base.writeLog("Serial number input has been cancelled, proceeding...", this.getClass());
                    break;
                }
            }
        }

        Base.SERIAL_NUMBER = serialNumberString;

        // for some reason, requesting firmware version too fast after setting the serial number
        // causes the answer to be just "ok"
        Base.hiccup(100);
        firmware = dispatchCommand(GET_FIRMWARE_VERSION);

        if (isNewVendorID) {
            firmwareVersion = Version.fromMachine(firmware);
        } else {
            firmwareVersion = Version.fromMachineOld(firmware);
        }
    }

    private boolean isSerialValid() {
        int printerId, productId;

        if (serialNumberString.length() != 10) {
            return false;
        }

        try {
            printerId = Integer.parseInt(serialNumberString.substring(0, 2));
        } catch (NumberFormatException nf) {
            return false;
        }

        for (PrinterInfo printer : PrinterInfo.values()) {
            productId = printer.productID();
            if (printerId == productId) {
                return true;
            }
        }

        return false;
    }

    private void updateMachineInfo() {
        String firmware;
        int retry = 3;
        serialNumberString = "9999999999";
        try {
            serialNumberString = m_usbDevice.getSerialNumberString();
        } catch (UsbException | UnsupportedEncodingException | UsbDisconnectedException ex) {
            Logger.getLogger(UsbPassthroughDriver.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        Base.SERIAL_NUMBER = serialNumberString;

        //get firmware version
        //check first for un-initialized serial or firmware version
        if (!serialNumberString.contains("9999999999")) {
            while (firmwareVersion.getPrinter() == PrinterInfo.UNKNOWN && retry > 0) {
                firmware = dispatchCommand(GET_FIRMWARE_VERSION);
                firmwareVersion = Version.fromMachineAtFirmware(firmware);
                retry--;
            }
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

            feedbackWindow.setVisible(true);
            feedbackWindow.setFeedback1(Feedback.FLASHING_MAIN_MESSAGE);

            if (firmwareVersion.equals(new Version()) == false) {
                backupConfig();
            }

            Base.writeLog("Carrying on with firmware flash", this.getClass());

            dispatchCommand(SET_FIRMWARE_VERSION + INVALID_FIRMWARE_VERSION);

            if (backupConfig) {
                feedbackWindow.setFeedback2(Feedback.FLASHING_SUB_MESSAGE);
            } else {
                feedbackWindow.setFeedback2(Feedback.FLASHING_SUB_MESSAGE_NO_CALIBRATION);
            }

            Base.writeLog("Starting Firmware update.", this.getClass());
            if (flashAndCheck(firmwareFile.getAbsolutePath(), -1) > 0) {
                Base.writeLog("Firmware successfully updated", this.getClass());
                Base.writeLog("Setting firmware version to: " + versionToCompare, this.getClass());
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

        Base.writeLog("Acquiring Z value loaded,filament and nozzle size before flashing new firmware", this.getClass());
        feedbackWindow.setFeedback2(Feedback.SAVING_MESSAGE);

        // change into firmware
        dispatchCommand("M630", COM.NO_RESPONSE);

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
        updateNozzleType();
        backupZVal = machine.getzValue();
        backupCoilText = machine.getCoilText();
        backupNozzleSize = machine.getNozzleType();

        // change back into bootloader
        dispatchCommand("M609", COM.NO_RESPONSE);

        hiccup(3000, 0);

        if (establishConnection() == false) {
            Base.writeLog("Couldn't establish connection after attempting to go back to bootloader, requesting user to restart", this.getClass());

            // Warn user to restart BTF and restart BEESOFT.
            Warning firmwareOutDate = new Warning(true);
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

        Base.writeLog("Acquired Z value, loaded filament and nozzle size with success!", this.getClass());
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
                    initUSBDevice();
                    hiccup(100, 0);
                }

                pipes = GetPipe(m_usbDevice);

                if (pipes != null) {
                    openPipe(pipes);
                } else {
                    continue;
                }

                if (isInitialized()) {
                    Base.getMainWindow().getButtons().setMessage("is connecting");
                    ready = true;
                } else {
                    Base.writeLog("Failed in establishing connection, trying again in 1 second...", this.getClass());
                    Base.hiccup(1000);
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
                    initUSBDevice();
                    hiccup(100, 0);
                }

                pipes = GetPipe(m_usbDevice);

                if (pipes != null) {
                    openPipe(pipes);
                } else {
                    continue;
                }

                if (isInitialized()) {
                    hiccup(100, 0);
                    //while (readResponse().equals("") == false) {
                    //    hiccup(10, 0);
                    //}

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
                    Base.hiccup(1000);
                }

            } catch (Exception ex) {
            }
        } while (ready == false);

        return true;

    }
}
