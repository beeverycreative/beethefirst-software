package pt.beeverycreative.beesoft.drivers.usb;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.usb4java.ConfigDescriptor;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.EndpointDescriptor;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
import replicatorg.app.Base;
import replicatorg.app.ProperDefault;

import replicatorg.drivers.DriverBaseImplementation;
import replicatorg.machine.model.MachineModel;

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
public class UsbDriver extends DriverBaseImplementation {

    protected static final int TIMEOUT = 2500;
    private final short BEEVERYCREATIVE_NEW_VENDOR_ID = (short) 0x29c9;
    private final Context context;
    protected final boolean comLog = Boolean.valueOf(ProperDefault.get("comLog"));
    protected final long startTS;
    protected String serialNumberString = "9999999999";
    private byte serialNumberIndex;
    protected DeviceHandle connectedDeviceHandle = null;
    protected PrinterInfo connectedDevice = PrinterInfo.UNKNOWN;

    //check this, maybe delete
    protected boolean isNewVendorID = false;

    /**
     * Variables for extruded material management
     */
    protected boolean transferMode = false;
    protected boolean isONShutdown = false;

    private boolean isBusy = true;
    protected static final Lock DISPATCHCOMMAND_LOCK = new ReentrantLock();
    private String lastStatusMessage;

    protected static final int TRANSFER_MESSAGE_SIZE = Base.isLinux() ? 128 : 32;
    protected static final int MESSAGE_SIZE = 512;
    protected static final int SD_CARD_MESSAGE_SIZE = 512;
    protected static final int MESSAGES_IN_BLOCK = 512;

    private byte ENDPOINT_IN_ADDRESS = LibUsb.ENDPOINT_IN | 0x02;
    private byte ENDPOINT_OUT_ADDRESS = LibUsb.ENDPOINT_OUT | 0x05;

    /**
     * USBDriver low level definition.
     *
     */
    protected UsbDriver() {
        final int result;

        context = new Context();
        result = LibUsb.init(context);

        if (result != LibUsb.SUCCESS) {
            Base.writeLog("Unable to initialize libusb. Error: " + LibUsb.errorName(result), this.getClass());
            throw new LibUsbException("Unable to initialize libusb.", result);
        }

        startTS = System.currentTimeMillis();
        super.setInitialized(false);
    }

    /**
     * Gets the info object on the currently connected printer
     *
     * @return
     */
    @Override
    public PrinterInfo getConnectedDevice() {
        return connectedDevice;
    }

    protected boolean initPrinter() {
        final Device device;

        device = findDevice();

        if (device != null) {
            connectedDeviceHandle = obtainHandleFromDevice(device);
            claimDeviceInterface(connectedDeviceHandle, 0);
            serialNumberString = LibUsb.getStringDescriptor(connectedDeviceHandle, serialNumberIndex);
            Base.SERIAL_NUMBER = serialNumberString;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Attempts to find a BEEVERYCREATIVE printer.
     *
     * @return the libusb device object representing the printer
     */
    private Device findDevice() {

        final DeviceList deviceList = new DeviceList();
        DeviceDescriptor deviceDescriptor;
        int result;
        short idVendor, idProduct;

        result = LibUsb.getDeviceList(context, deviceList);

        if (result < 0) {
            Base.writeLog("Unable to get device list. Error: " + LibUsb.errorName(result), this.getClass());
            throw new LibUsbException("Unable to get device list", result);
        }

        try {
            for (Device device : deviceList) {
                deviceDescriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, deviceDescriptor);

                if (result != LibUsb.SUCCESS) {
                    Base.writeLog("Unable to read device descriptor. Error: " + LibUsb.errorName(result), this.getClass());
                    throw new LibUsbException("Unable to read device descriptor", result);
                }

                idVendor = deviceDescriptor.idVendor();
                idProduct = deviceDescriptor.idProduct();
                connectedDevice = PrinterInfo.getDevice(idVendor + ":" + idProduct);

                if (connectedDevice != PrinterInfo.UNKNOWN) {
                    Base.writeLog("Found device " + Integer.toHexString(idVendor) + ":" + Integer.toHexString(idProduct), this.getClass());
                    FilamentControler.initFilamentList(connectedDevice);
                    isNewVendorID = idVendor == BEEVERYCREATIVE_NEW_VENDOR_ID;
                    serialNumberIndex = deviceDescriptor.iSerialNumber();
                    Base.getMainWindow().getButtons().setLogo(connectedDevice.iconFilename());
                    return device;
                }
            }
        } finally {
            // for some reason, if we free the device list on windows
            // LIBUSB_ERROR_NO_DEVICE is thrown when we attempt to obtain the
            // handle
            if (Base.isWindows() == false) {
                LibUsb.freeDeviceList(deviceList, true);
            }
        }

        return null;
    }

    private DeviceHandle obtainHandleFromDevice(Device device) {
        final DeviceHandle deviceHandle = new DeviceHandle();
        int result;

        result = LibUsb.open(device, deviceHandle);

        if (result != LibUsb.SUCCESS) {
            Base.writeLog("Unable to open USB device. Error: " + LibUsb.errorName(result), this.getClass());
            throw new LibUsbException("Unable to open USB device", result);
        }

        if (Base.isMacOS()) {
            result = LibUsb.setConfiguration(deviceHandle, 0);

            if (result != LibUsb.SUCCESS) {
                // not critical
                Base.writeLog("Failed setting configuration 0", this.getClass());
            }
        }

        return deviceHandle;
    }

    private boolean claimDeviceInterface(DeviceHandle deviceHandle, int interfaceNumber) {
        final boolean detach;
        final ConfigDescriptor configDescriptor = new ConfigDescriptor();
        int result;

        detach = LibUsb.hasCapability(LibUsb.CAP_SUPPORTS_DETACH_KERNEL_DRIVER)
                && LibUsb.kernelDriverActive(deviceHandle, interfaceNumber) == 1;

        if (detach) {
            result = LibUsb.detachKernelDriver(deviceHandle, interfaceNumber);
            if (result != LibUsb.SUCCESS) {
                Base.writeLog("Unable to detach kernel driver. Error: " + LibUsb.errorName(result), this.getClass());
                throw new LibUsbException("Unable to detach kernel driver", result);
            }
        }

        result = LibUsb.claimInterface(deviceHandle, interfaceNumber);

        if (result != LibUsb.SUCCESS) {
            Base.writeLog("Unable to claim interface. Error: " + LibUsb.errorName(result), this.getClass());
            throw new LibUsbException("Unable to claim interface", result);
        }

        // if it can't get the active config descriptor assume the default ENDPOINT addresses
        result = LibUsb.getActiveConfigDescriptor(LibUsb.getDevice(deviceHandle), configDescriptor);
        if (result == LibUsb.SUCCESS) {
            try {
                for (EndpointDescriptor e : configDescriptor.iface()[0].altsetting()[0].endpoint()) {
                    byte endpointAddress = e.bEndpointAddress();
                    if ((endpointAddress & LibUsb.ENDPOINT_DIR_MASK) == LibUsb.ENDPOINT_IN) {
                        ENDPOINT_IN_ADDRESS = endpointAddress;
                    } else if ((endpointAddress & LibUsb.ENDPOINT_DIR_MASK) == LibUsb.ENDPOINT_OUT) {
                        ENDPOINT_OUT_ADDRESS = endpointAddress;
                    }
                }
            } finally {
                LibUsb.freeConfigDescriptor(configDescriptor);
            }
        } else {
            Base.writeLog("Failed obtaining active config descriptor, assuming default ENDPOINT addresses...", this.getClass());
        }

        return true;
    }

    /**
     * Checks if printer is in bootloader.
     *
     * @return true, if in bootloader. false, if not.
     */
    @Override
    public boolean isBootloader() {
        return super.isBootloader;
    }

    /**
     * Test pipe for USB device detection
     *
     * @return <li> true, if available
     * <li> false, if not
     *
     */
    protected boolean testComm() {

        //final String testMsg = "M625" + new String(new char[MESSAGE_SIZE - 6]) + "\n";
        final String testMsg = "M625\n";
        int ansBytes, tries = 10;
        long elapsedTimeMilliseconds;
        byte[] cleanBuffer;
        String status;
        boolean validStatus = false, strContainsStatus;

        if (connectedDeviceHandle == null) {
            return false;
        }

        // this way we can easily recover if printer is stuck in transfer mode
        // and it works even if it isn't
        try {
            if (DISPATCHCOMMAND_LOCK.tryLock(50, TimeUnit.MILLISECONDS)) {
                try {

                    while ((cleanBuffer = receiveAnswerBytes(MESSAGE_SIZE, 100)).length > 0) {
                        final String trashString;

                        trashString = new String(cleanBuffer);

                        if (trashString.contains("Loading Config Override")) {
                            Base.writeLog("ERROR LOADING CONFIG, LOADING CONFIG OVERRIDES", this.getClass());
                        }

                        hiccup(50);
                    }

                    ansBytes = sendCommand(testMsg, 50);

                    if (ansBytes == 0) {
                        Base.writeLog("Couldn't send test message", this.getClass());
                        return false;
                    }

                    hiccup(100);

                    // clean up
                    while (validStatus == false && --tries > 0) {
                        elapsedTimeMilliseconds = System.currentTimeMillis();
                        status = receiveAnswer(50);
                        elapsedTimeMilliseconds = System.currentTimeMillis() - elapsedTimeMilliseconds;

                        if (elapsedTimeMilliseconds > 500) {
                            isBusy = true;
                            break;
                        }

                        if (status.length() > 0) {

                            // when printer is in bootloader, M625 returns bad code
                            if ((strContainsStatus = status.contains("S:")) || status.contains("Bad")) {
                                validStatus = true;

                                if (strContainsStatus) {
                                    processStatus(status);
                                }

                            } else {
                                isBusy = true;
                            }
                        }
                    }
                } finally {
                    DISPATCHCOMMAND_LOCK.unlock();
                }
            } else {
                isBusy = true;
            }
        } catch (InterruptedException ex) {
        }

        return true;
    }

    /**
     * Process the string status message provided by the printer. This method
     * assumes a valid status message is being received as argument.
     *
     * @param status the string containing the status message (e.g.: S:3)
     */
    private void processStatus(String status) {

        final boolean machineReady, machinePaused, machineShutdown, machinePrinting, machinePowerSaving, machineOperational;
        lastStatusMessage = status;

        machineReady = status.contains("S:3");
        machinePowerSaving = status.contains("Power_Saving");
        machineShutdown = status.contains("S:9") || status.toLowerCase().contains("shutdown");
        machinePrinting = status.contains("S:5");
        machinePaused = status.contains("Pause");
        machineOperational = machineReady || machineShutdown || machinePrinting || machinePaused;

        try {
            machine.setLastStatusString(status);
            machine.setMachineReady(machineReady);
            machine.setMachinePaused(machinePaused);
            machine.setMachinePowerSaving(machinePowerSaving);
            machine.setMachineShutdown(machineShutdown);
            machine.setMachinePrinting(machinePrinting);
            machine.setMachineOperational(machineOperational);
        } catch (NullPointerException ex) {
            Base.writeLog("Machine was null", this.getClass());
        }

        isBusy = false;
    }

    @Override
    public boolean isBusy() {
        return isBusy;
    }

    @Override
    public void setBusy(boolean busy) {
        // TODO: change this to accept no arguments and set only to true
        if (busy == true) {
            isBusy = busy;
        }
    }

    @Override
    public String getLastStatusMessage() {
        return lastStatusMessage;
    }

    @Override
    public void dispose() {
        cleanLibUsbDevice();
        LibUsb.exit(context);
        connectedDevice = PrinterInfo.UNKNOWN;
        setMachine(new MachineModel());
    }

    protected void cleanLibUsbDevice() {
        final int result;

        if (connectedDeviceHandle != null) {
            result = LibUsb.releaseInterface(connectedDeviceHandle, 0);

            if (result != LibUsb.SUCCESS) {
                Base.writeLog("Unable to release interface. Error: " + LibUsb.errorName(result), this.getClass());
            }

            LibUsb.close(connectedDeviceHandle);
            connectedDeviceHandle = null;
        }
    }

    protected int sendCommand(String next) {
        next += '\n';
        return sendCommandBytes(next.getBytes(), TIMEOUT);
    }

    /**
     * Actually sends command over USB.
     *
     * @param next
     * @param timeout
     * @return
     */
    protected int sendCommand(String next, int timeout) {
        next += '\n';
        return sendCommandBytes(next.getBytes(), timeout);
    }

    protected int sendCommandBytes(byte[] next) {
        return sendCommandBytes(next, TIMEOUT);
    }

    /**
     * Send command to Machine
     *
     * @param next Command
     * @param timeout
     * @return command length
     */
    protected int sendCommandBytes(byte[] next, int timeout) {

        final ByteBuffer buffer;
        final IntBuffer transfered = IntBuffer.allocate(1);
        final int result;

        // skip empty commands.
        if (next.length == 0) {
            return 0;
        }

        buffer = ByteBuffer.allocateDirect(next.length);
        buffer.put(next);

        DISPATCHCOMMAND_LOCK.lock();
        try {
            if (connectedDeviceHandle != null) {
                result = LibUsb.bulkTransfer(connectedDeviceHandle, ENDPOINT_OUT_ADDRESS, buffer, transfered, timeout);

                if (result != LibUsb.SUCCESS) {

                    if (result != LibUsb.ERROR_TIMEOUT) {
                        Base.writeLog("Bulk send failed. Error: " + LibUsb.errorName(result), this.getClass());
                        resetBEESOFTstatus();
                    }

                    /*
                     if (result == LibUsb.ERROR_NO_DEVICE || result == LibUsb.ERROR_IO || result == LibUsb.ERROR_OTHER) {
                     resetBEESOFTstatus();
                     }
                    
                     } else {
                     throw new LibUsbException("Bulk transfer failed", result);
                     }
                     */
                }

                return transfered.get();
            }
        } finally {
            DISPATCHCOMMAND_LOCK.unlock();
        }

        return 0;
    }

    protected String receiveAnswer() {
        final byte[] answer;

        answer = receiveAnswerBytes();

        return new String(answer).trim();
    }

    protected String receiveAnswer(int timeout) {
        final byte[] answer;

        answer = receiveAnswerBytes(MESSAGE_SIZE, timeout);

        return new String(answer).trim();
    }

    protected byte[] receiveAnswerBytes() {
        return receiveAnswerBytes(MESSAGE_SIZE, TIMEOUT);
    }

    protected byte[] receiveAnswerBytes(int expectedSize) {
        return receiveAnswerBytes(expectedSize, TIMEOUT);
    }

    protected byte[] receiveAnswerBytes(int expectedSize, int timeout) {
        final ByteBuffer buffer;
        final IntBuffer transfered = IntBuffer.allocate(1);
        final byte[] retArray;
        int result;

        buffer = ByteBuffer.allocateDirect(expectedSize);

        DISPATCHCOMMAND_LOCK.lock();
        try {
            if (connectedDeviceHandle != null) {
                result = LibUsb.bulkTransfer(connectedDeviceHandle, ENDPOINT_IN_ADDRESS, buffer, transfered, timeout);

                if (result != LibUsb.SUCCESS) {

                    if (result != LibUsb.ERROR_TIMEOUT) {
                        Base.writeLog("Bulk receive failed. Error: " + LibUsb.errorName(result), this.getClass());
                        resetBEESOFTstatus();
                    }

                    /*
                     if (result == LibUsb.ERROR_NO_DEVICE || result == LibUsb.ERROR_IO || result == LibUsb.ERROR_OTHER) {
                     resetBEESOFTstatus();
                     }
                    
                     } else {
                     throw new LibUsbException("Bulk transfer failed", result);
                     }
                     */
                }

                retArray = new byte[transfered.get()];
                buffer.rewind();
                buffer.get(retArray);
                return retArray;
            }
        } finally {
            DISPATCHCOMMAND_LOCK.unlock();
        }

        return new byte[0];
    }

    /**
     * Sleeps driver for specified duration.
     *
     * @param mili milliseconds to sleep.
     */
    protected void hiccup(long mili) {
        try {
            Thread.sleep(mili);
        } catch (InterruptedException ex) {
        }
    }

    private void resetBEESOFTstatus() {
        if (Base.keepFeedbackOpen == false) {
            if (machine != null) {
                machine.setMachineOperational(false);
            }
            Base.getMainWindow().getButtons().setMessage("is disconnected");
            Base.disposeAllOpenWindows();
        }
        Base.isPrinting = false;
        Base.printPaused = false;
        resetBootloaderAndFirmwareVersion();
        cleanLibUsbDevice();
    }
}
