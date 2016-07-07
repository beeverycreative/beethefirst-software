package pt.beeverycreative.beesoft.drivers.usb;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Node;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
import replicatorg.app.Base;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.popups.Feedback;
import replicatorg.app.ui.panels.PauseMenu;
import replicatorg.app.ui.panels.PrintSplashAutonomous;
import replicatorg.app.ui.panels.SerialNumberInput;
import replicatorg.app.ui.panels.ShutdownMenu;
import replicatorg.app.ui.popups.Warning;
import replicatorg.app.util.AutonomousData;
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
    private static final String GET_POSITION = "M121";
    private static final String LAUNCH_FIRMWARE = "M630";
    private static final String LAUNCH_BOOTLOADER = "M609";
    private static final String SET_FIRMWARE_VERSION = "M114 A";
    private static final String INVALID_FIRMWARE_VERSION = "0.0.0";
    private static final String GET_FIRMWARE_VERSION = "M115";
    private static final String STATUS_OK = "S:3";
    private static final String STATUS_SDCARD = "s:5";
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
    private boolean stopTransfer = false;
    private int transferProgress = 0;
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
     *
     */
    public UsbPassthroughDriver() {
        super();
        // init our variables.
        setInitialized(false);
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
        Base.keepFeedbackOpen = false;

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
                super.isBootloader = true;
                Base.writeLog("Launching firmware!", this.getClass());
                feedbackWindow.setFeedback2(Feedback.LAUNCHING_MESSAGE);
                Base.keepFeedbackOpen = true;
                dispatchCommand(LAUNCH_FIRMWARE); // Launch firmware
                cleanLibUsbDevice();
            } else {
                feedbackWindow.setFeedback2(Feedback.RESTART_PRINTER);
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
                Warning firmwareOutDate = new Warning("FirmwareOutDateVersion", true);
                firmwareOutDate.setVisible(true);

                Base.getMainWindow().setEnabled(false);
                // Sleep forever, until restart.
                while (true) {
                    hiccup(100);
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

    @Override
    public String dispatchCommand(String next) {
        if (next.charAt(0) == 'G') {
            return dispatchCommand(next, 30000);
        } else {
            return dispatchCommand(next, TIMEOUT);
        }
    }

    @Override
    public String dispatchCommand(String next, int timeout) {

        String ans;
        int retryNum;

        ans = "";
        retryNum = 5;

        if (next == null) {
            return "";
        }

        if (connectedDeviceHandle == null) {
            return "";
        }

        dispatchCommandLock.lock();
        try {
            sendCommand(next);

            if (comLog) {
                Base.writeComLog(System.currentTimeMillis() - startTS, "SENT: " + next);
            }

            while (retryNum > 0) {
                hiccup(100);
                ans += receiveAnswer();

                if (ans.contains("ok")) {
                    break;
                } else {
                    retryNum--;
                }
            }

            if (comLog) {
                Base.writeComLog(System.currentTimeMillis() - startTS, "RECEIVE: " + ans + "\n");
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

        if (connectedDeviceHandle == null) {
            return "";
        }

        dispatchCommandLock.lock();
        try {
            sendCommand(next);
            if (comLog) {
                Base.writeComLog(System.currentTimeMillis() - startTS, "SENT: " + next);
            }
            ans = receiveAnswer();

            if (comLog) {
                Base.writeComLog(System.currentTimeMillis() - startTS, "RECEIVE: " + ans + "\n");
            }

        } finally {
            dispatchCommandLock.unlock();
        }
        return ans;
    }

    private void dispatchCommandNoResponse(String next) {
        if (next == null) {
            return;
        }

        if (connectedDeviceHandle == null) {
            return;
        }

        dispatchCommandLock.lock();
        try {
            sendCommand(next, 30000);
            if (comLog) {
                Base.writeComLog(System.currentTimeMillis() - startTS, "SENT: " + next);
                Base.writeComLog(System.currentTimeMillis() - startTS, "RECEIVE: (no response)\n");
            }
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
                    hiccup(QUEUE_WAIT);

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

    public int getTransferProgress() {
        return transferProgress;
    }

    /**
     * Transfers a given GCode file to the printer's SDCard.
     *
     * @param gcodeFile
     * @param header
     * @param panel
     * @return
     */
    @Override
    public boolean transferGCode(File gcodeFile, String header, PrintSplashAutonomous panel) {

        final long fileSize, totalMessages, totalBlocks;
        final RandomAccessFile randomAccessFile;
        long messagesSent, elapsedTimeMilliseconds;
        int blockPointer, bytesRead, numMessages, messageLength, timeout, byteCounter;
        byte[] blockBuffer, messageBuffer;
        String answer;

        if (!gcodeFile.isFile() || !gcodeFile.canRead()) {
            Base.writeLog("GCode file not found or unreadable", this.getClass());
            return false;
        }

        try {
            randomAccessFile = new RandomAccessFile(gcodeFile, "r");
        } catch (FileNotFoundException ex) {
            Base.writeLog("File to be transferred was not found.", this.getClass());
            return false;
        }

        blockPointer = 0;
        blockBuffer = new byte[MAX_BLOCK_SIZE];
        fileSize = gcodeFile.length();
        messagesSent = 0;
        totalMessages = fileSize / SD_CARD_MESSAGE_SIZE + ((fileSize % SD_CARD_MESSAGE_SIZE == 0) ? 0 : 1);
        totalBlocks = totalMessages / MESSAGES_IN_BLOCK + ((totalMessages % MESSAGES_IN_BLOCK == 0) ? 0 : 1);

        transferMode = true;
        dispatchCommandLock.lock();
        elapsedTimeMilliseconds = System.currentTimeMillis();
        try {
            if (dispatchCommand(INIT_SDCARD, COM.TRANSFER).contains(ERROR)) {
                Base.writeLog("(" + INIT_SDCARD.trim() + ") SDCard init has failed", this.getClass());
                return false;
            } else {
                Base.writeLog("(" + INIT_SDCARD.trim() + ") SDCard init has been successful", this.getClass());
            }

            if (dispatchCommand(SET_FILENAME + fileName, COM.TRANSFER).contains(FILE_CREATED) == false) {
                Base.writeLog("(" + SET_FILENAME.trim() + ") File creation failed", this.getClass());
                return false;
            } else {
                Base.writeLog("(" + SET_FILENAME.trim() + ") File creation has succeeded", this.getClass());
            }

            if (header != null && !header.equals("") && header.length() < MAX_BLOCK_SIZE) {
                if (allocateSDCardSpace(blockPointer, (blockPointer + header.length()) - 1).contains(ERROR)) {
                    Base.writeLog("SDCard space allocation for header failed", this.getClass());
                    return false;
                } else {
                    Base.writeLog("SDCard space for header allocated with success", this.getClass());
                    blockPointer += header.length();
                }

                sendCommand(header, 100);
                hiccup(10);

                if (receiveAnswer().contains("tog") == false) {
                    Base.writeLog("Header transfer failure, 0 bytes sent.", this.getClass());
                    return false;
                } else {
                    Base.writeLog("Header transferred successfully.", this.getClass());
                }
            }

            // Send all blocks
            for (long block = 0; block < totalBlocks && stopTransfer == false; ++block) {
                try {
                    bytesRead = randomAccessFile.read(blockBuffer, 0, MAX_BLOCK_SIZE);
                } catch (IOException ex) {
                    Base.writeLog("IOException thrown when attempting to obtain a block from the file.", this.getClass());
                    return false;
                }

                if (allocateSDCardSpace(blockPointer, blockPointer + bytesRead - 1).contains(ERROR)) {
                    Base.writeLog("SDCard space allocation for " + block + " block failed", this.getClass());
                    return false;
                } else {
                    blockPointer += MAX_BLOCK_SIZE;
                }

                numMessages = bytesRead / SD_CARD_MESSAGE_SIZE + ((bytesRead % SD_CARD_MESSAGE_SIZE == 0) ? 0 : 1);

                byteCounter = 0;
                for (int message = 0; message < numMessages; ++message) {
                    for (int i = 0; i < SD_CARD_MESSAGE_SIZE / TRANSFER_MESSAGE_SIZE; ++i) {
                        messageLength = Math.min(TRANSFER_MESSAGE_SIZE, bytesRead);
                        messageBuffer = new byte[messageLength];
                        System.arraycopy(blockBuffer, byteCounter, messageBuffer, 0, messageLength);
                        sendCommandBytes(messageBuffer);

                        bytesRead -= messageLength;
                        byteCounter += messageLength;

                        //hiccup(1);
                    }

                    answer = "";
                    timeout = 100;
                    do {
                        answer += new String(receiveAnswerBytes(4, timeout));
                        timeout *= 2;
                    } while (answer.contains("tog") == false && timeout < TIMEOUT);

                    if (answer.contains("tog") == false) {
                        Base.writeLog("Transfer failure, 0 bytes sent.", this.getClass());

                        if (panel != null) {
                            panel.setError();

                            while (panel.isVisible()) {
                                hiccup(1000);
                            }
                        }
                        return false;
                    } else {
                        transferProgress = Math.toIntExact(messagesSent++ * 100 / totalMessages);
                        if (panel != null) {
                            panel.updatePrintBar(transferProgress);
                        }

                    }
                }
            }

        } finally {
            dispatchCommandLock.unlock();
            transferMode = false;
        }

        elapsedTimeMilliseconds = System.currentTimeMillis() - elapsedTimeMilliseconds;
        Base.writeLog("Successfully transferred " + gcodeFile.length() + " bytes in " + elapsedTimeMilliseconds / 1000 + " seconds.", this.getClass());
        Base.writeLog("Transfer rate: " + gcodeFile.length() / 1000 / (elapsedTimeMilliseconds / 1000.0) + "KBps", this.getClass());

        return true;
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

    private String allocateSDCardSpace(int srcPos, int destPos) {

        String command, response;

        command = TRANSFER_BLOCK + "A" + srcPos + " D" + destPos;

        response = dispatchCommand(command);

        if (response.contains(RESPONSE_OK)) {
            return RESPONSE_OK;
        } else {
            return ERROR + response + "\n" + TRANSFER_BLOCK + "failed. Response not OK";
        }

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
        return testComm();
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
                hiccup(100);
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

    private boolean flashAndCheck(String filename) {

        final BufferedInputStream bis;
        final File firmwareFile = new File(filename);
        byte[] buffer = new byte[64], result, temp;
        int readBytes, sent, offset = 0;

        if (!firmwareFile.isFile() || !firmwareFile.canRead()) {
            Base.writeLog("Firmware file not found or unreadable.", this.getClass());
            return false;
        }

        try {
            bis = new BufferedInputStream(new FileInputStream(firmwareFile));
        } catch (FileNotFoundException ex) {
            Base.writeLog("Firmware file not found or unreadable.", this.getClass());
            return false;
        }

        if (dispatchCommand("M650 A" + firmwareFile.length()).contains("ok") == false) {
            Base.writeLog("M650 A" + firmwareFile.length() + " failed", this.getClass());
            return false;
        }

        dispatchCommandLock.lock();
        try {
            try {
                while ((readBytes = bis.read(buffer)) != -1) {
                    try {
                        buffer = Arrays.copyOf(buffer, readBytes);
                        sent = sendCommandBytes(buffer);

                        if (sent != readBytes) {
                            Base.writeLog("Transfer failure, incorrect number of bytes sent.", this.getClass());
                            return false;
                        }

                        result = new byte[readBytes];
                        while (offset < readBytes) {
                            hiccup(1);
                            temp = receiveAnswerBytes(readBytes, 200);
                            System.arraycopy(temp, 0, result, offset, temp.length);
                            offset += temp.length;
                        }

                        offset = 0;

                        if (!Arrays.equals(result, buffer)) {

                            System.out.println("*** result *** ");
                            for (byte a : result) {
                                System.out.print(" " + a);
                            }

                            System.out.println();
                            System.out.println("*** buffer *** ");
                            for (byte a : buffer) {
                                System.out.print(" " + a);
                            }
                            System.out.println();

                            Base.writeLog("Transmission error found, reboot BEETHEFIRST.", this.getClass());
                            return false;
                        }

                    } catch (ArrayIndexOutOfBoundsException ex) {
                        return false;
                    }
                }
            } catch (IOException ex) {
                Base.writeLog("IOException while reading firmware file.", this.getClass());
                Base.writeLog(ex.getMessage(), this.getClass());
                return false;
            }

            return true;
        } finally {
            dispatchCommandLock.unlock();
        }
    }

    private void updateBootloaderInfo() {

        String bootloader, firmware;
        boolean validSerial = false;
        boolean cancelPressed;

        bootloader = dispatchCommand("M116");
        bootloaderVersion = Version.bootloaderVersion(bootloader);

        //Default
        firmwareVersion = new Version();

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
        hiccup(100);
        firmware = dispatchCommand(GET_FIRMWARE_VERSION);

        if (isNewVendorID) {
            firmwareVersion = Version.fromMachine(firmware);
        } else {
            firmwareVersion = Version.fromMachineOld(firmware);
        }
    }

    private boolean isSerialValid() {
        final int printerId;
        final boolean resetSN = Boolean.parseBoolean(ProperDefault.get("serialnumber.reset"));
        int productId;

        if (resetSN) {
            ProperDefault.put("serialnumber.reset", "false");
            dispatchCommand("M118 T9999999999");
            return false;
        }

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

        //get firmware version
        //check first for un-initialized serial or firmware version
        while (firmwareVersion.getPrinter() == PrinterInfo.UNKNOWN && retry > 0) {
            firmware = dispatchCommand(GET_FIRMWARE_VERSION);
            firmwareVersion = Version.fromMachineAtFirmware(firmware);
            retry--;
        }
        System.out.println("firmware_version: " + firmwareVersion);
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
            if (flashAndCheck(firmwareFile.getAbsolutePath()) == true) {
                Base.writeLog("Firmware successfully updated", this.getClass());
                Base.writeLog("Setting firmware version to: " + versionToCompare, this.getClass());
                dispatchCommand(SET_FIRMWARE_VERSION + versionToCompare);

            } else {
                Base.writeLog("Firmware update failed", this.getClass());
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
        Base.keepFeedbackOpen = true;
        dispatchCommand(LAUNCH_FIRMWARE);

        //hiccup(3000);
        // reestablish connection
        if (establishConnection() == false) {
            Base.writeLog("Establishing connection after changing into firmware failed", this.getClass());
            return false;
        }

        Base.keepFeedbackOpen = false;

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
        Base.keepFeedbackOpen = true;
        dispatchCommand(LAUNCH_BOOTLOADER);
        Base.keepFeedbackOpen = false;

        //hiccup(3000);
        if (establishConnection() == false) {
            Base.writeLog("Couldn't establish connection after attempting to go back to bootloader, requesting user to restart", this.getClass());

            // Warn user to restart BTF and restart BEESOFT.
            Warning firmwareOutDate = new Warning("FirmwareOutDateVersion", true);
            firmwareOutDate.setVisible(true);

            Base.getMainWindow().setEnabled(false);
            // Sleep forever, until restart.
            while (true) {
                hiccup(3000);
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

                if (connectedDeviceHandle != null) {
                    cleanLibUsbDevice();
                }

                while (initPrinter() == false) {
                    hiccup(100);
                }

                if (isInitialized()) {
                    Base.getMainWindow().getButtons().setMessage("is connecting");
                    ready = true;
                } else {
                    Base.writeLog("Failed in establishing connection, trying again in 1 second...", this.getClass());
                    hiccup(1000);
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

                if (connectedDeviceHandle != null) {
                    cleanLibUsbDevice();
                }

                while (initPrinter() == false) {
                    hiccup(100);
                }

                if (isInitialized()) {
                    hiccup(100);
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
                    hiccup(1000);
                }

            } catch (Exception ex) {
            }
        } while (ready == false);

        return true;
    }
}
