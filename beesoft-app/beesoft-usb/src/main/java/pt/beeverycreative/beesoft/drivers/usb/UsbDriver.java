package pt.beeverycreative.beesoft.drivers.usb;

import de.ailis.usb4java.AbstractDevice;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import javax.usb.UsbClaimException;
import javax.usb.UsbConfiguration;
import javax.usb.UsbConst;
import javax.usb.UsbDevice;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbServices;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbIrp;
import javax.usb.UsbNotClaimedException;
import javax.usb.UsbPipe;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
import replicatorg.app.Base;

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

    protected AbstractDevice m_usbDevice;

    private final short BEEVERYCREATIVE_VENDOR_ID = (short) 0xffff;
    private final short BEEVERYCREATIVE_NEW_VENDOR_ID = (short) 0x29c9;

    private UsbServices usbServices = null;
    private UsbHub usbRootHub = null;

    protected PrinterInfo connectedDevice = PrinterInfo.UNKNOWN;

    //check this, maybe delete
    protected boolean isNewVendorID = false;

    protected ArrayList<AbstractDevice> m_usbDeviceList = new ArrayList<AbstractDevice>();
    /**
     * Lock for multi-threaded access to this driver's serial port.
     */
    private final ReentrantReadWriteLock m_usbLock = new ReentrantReadWriteLock();
    /**
     * Locks the serial object as in use so that it cannot be disposed until it
     * is unlocked. Multiple threads can hold this lock concurrently.
     */
    public final ReadLock m_usbInUse = m_usbLock.readLock();
    protected UsbPipes pipes;
    /**
     * Variables for extruded material management
     */
    protected boolean transferMode = false;
    protected boolean isONShutdown = false;

    private boolean isBusy = true;
    protected boolean isAlive = false;
    private int readyCount = 0;
    protected static final Lock dispatchCommandLock = new ReentrantLock();
    private String lastStatusMessage;

    /**
     * USBDriver high level definition.
     *
     */
    protected UsbDriver() {

        try {
            if (usbServices == null) {
                usbServices = UsbHostManager.getUsbServices();
            }

            if (usbRootHub == null) {
                usbRootHub = usbServices.getRootUsbHub();
            }

        } catch (UsbException ex) {
            setInitialized(false);
            //Base.writeLog("*initUsbDevice* <UsbException> " + ex.getMessage(), this.getClass());
        } catch (SecurityException ex) {
            setInitialized(false);
            Base.writeLog("*initUsbDevice* <SecurityException> " + ex.getMessage(), this.getClass());
        } catch (UsbDisconnectedException ex) {
            setInitialized(false);
            Base.writeLog("*initUsbDevice* <UsbDisconnectedException> " + ex.getMessage(), this.getClass());
        }
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

    /**
     * Inits USB device.
     *
     * @param device USB device from descriptor.
     */
    private void initUSBDevice(UsbDevice device) {

        try {
            if (device.isUsbHub()) {
                UsbHub hub = (UsbHub) device;

                for (UsbDevice child : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
                    UsbDriver.this.initUSBDevice(child);
                }

            } else {
                addIfCompatible(device);
            }
        } catch (UsbException ex) {
            m_usbDevice = null;
            //Base.writeLog("*initUsbDevice(device)* <UsbException> " + ex.getMessage(), this.getClass());
        } catch (UnsupportedEncodingException ex) {
            m_usbDevice = null;
            Base.writeLog("*initUsbDevice(device)* <UnsupportedEncodingException> " + ex.getMessage(), this.getClass());
        } catch (UsbDisconnectedException ex) {
            m_usbDevice = null;
            Base.writeLog("*initUsbDevice(device)* <UsbDisconnectedException> " + ex.getMessage(), this.getClass());
        }
    }

    public boolean addIfCompatible(UsbDevice device) throws UsbException, UnsupportedEncodingException {

        UsbDeviceDescriptor descriptor;
        short idVendor, idProduct;

        descriptor = device.getUsbDeviceDescriptor();
        idVendor = descriptor.idVendor();
        idProduct = descriptor.idProduct();

        // verify if it's one of BEE's printers, else ignore it;
        // getting more information without knowing what we're dealing with
        // can cause ugly libusb crashes
        if (PrinterInfo.getDevice(idVendor + ":" + idProduct) == PrinterInfo.UNKNOWN) {
            return false;
        }

        //manufacturerString = device.getManufacturerString();
        //productString = device.getProductString();
        //serialNumberString = device.getSerialNumberString().trim();
        Base.writeLog("*** Adding to candidate list ***", this.getClass());
        Base.writeLog("Vendor ID: " + Integer.toHexString(idVendor & 0xFFFF), this.getClass());
        Base.writeLog("Product ID: " + Integer.toHexString(idProduct & 0xFFFF), this.getClass());
        //Base.writeLog("Manufacturer string: " + manufacturerString, this.getClass());
        //Base.writeLog("Product string: " + productString, this.getClass());
        //Base.writeLog("Serial number: " + serialNumberString, this.getClass());
        Base.writeLog("********************************", this.getClass());
        m_usbDeviceList.add((AbstractDevice) device);
        return true;
    }

    /**
     * Scans descriptor and inits usb device if match.
     */
    public void initUSBDevice() {
        m_usbDeviceList.clear();

        UsbDriver.this.initUSBDevice(usbRootHub);

        if (m_usbDeviceList.isEmpty()) {
            m_usbDevice = null;
            //Base.writeLog("Failed to find USB device.");
            setInitialized(false);
        } else {
            //Chose the device to Initialize
            m_usbDevice = m_usbDeviceList.get(0);
            short idProduct = m_usbDevice.getUsbDeviceDescriptor().idProduct();
            short idVendor = m_usbDevice.getUsbDeviceDescriptor().idVendor();
            connectedDevice = PrinterInfo.getDevice(idVendor + ":" + idProduct);
            Base.getMainWindow().getButtons().setLogo(connectedDevice.iconFilename());

            // early load of the list of filaments, to save time later on
            if (connectedDevice != PrinterInfo.UNKNOWN) {
                FilamentControler.initFilamentList(connectedDevice);
            }

            if (Base.isMacOS()) {
                ((AbstractDevice) m_usbDevice).setActiveUsbConfigurationNumber();
            }

            if (m_usbDeviceList.size() == 1) {
                Base.writeLog("Found 1 device, connecting...", this.getClass());
            } else {
                Base.writeLog("Multiple machines found. "
                        + "Connecting to the first of the list...", this.getClass());
            }

            UsbDeviceDescriptor descriptor;
            descriptor = m_usbDevice.getUsbDeviceDescriptor();
            if (descriptor.idVendor() == BEEVERYCREATIVE_NEW_VENDOR_ID) {
                isNewVendorID = true;
            } //no need for else

            if (descriptor.idVendor() == BEEVERYCREATIVE_VENDOR_ID) {
                isNewVendorID = false;
            } //no need for else
        }
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * Gets pipe from a USB device
     *
     * @param device USB device connected.
     * @return USB Pipes with Endpoints set.
     */
    protected UsbPipes GetPipe(UsbDevice device) {

        UsbPipes returnPipes;
        UsbConfiguration config;

        if (device != null) {
            if (device.isConfigured()) {
                config = device.getActiveUsbConfiguration();
            } else {
                Base.writeLog("Couldn't obtain valid USB configuration. Obtaining pipes failed", this.getClass());
                return null;
            }

            if (pipes == null || !isInitialized()) {
                Base.writeLog("No pipes were found, or testPipes failed. Creating new ones", this.getClass());
                returnPipes = new UsbPipes();
            } else {
                Base.writeLog("testPipes returned true, returning current pipes", this.getClass());
                return pipes;
            }

            List interfaces = config.getUsbInterfaces();
            for (Object ifaceObj : interfaces) {
                UsbInterface iface = (UsbInterface) ifaceObj;
                if (iface.isClaimed() || !iface.isActive()) {
                    continue;
                }

                List endpoints = iface.getUsbEndpoints();
                for (Object endpointObj : endpoints) {
                    UsbEndpoint endpoint = (UsbEndpoint) endpointObj;

                    if (endpoint.getType() != UsbConst.ENDPOINT_TYPE_BULK) {
                        continue;
                    }

                    if (endpoint.getDirection() == UsbConst.ENDPOINT_DIRECTION_OUT) {
                        Base.writeLog("Setting out direction endpoint", this.getClass());
                        returnPipes.setUsbPipeWrite(endpoint.getUsbPipe());
                    }

                    if (endpoint.getDirection() == UsbConst.ENDPOINT_DIRECTION_IN) {
                        Base.writeLog("Setting in direction endpoint", this.getClass());
                        returnPipes.setUsbPipeRead(endpoint.getUsbPipe());
                    }
                }

                if (returnPipes.getUsbPipeRead() != null && returnPipes.getUsbPipeWrite() != null) {
                    Base.writeLog("Returning new pipes", this.getClass());
                    return returnPipes;
//                }
                }
            }
        }

        Base.writeLog("Failed initializing new pipes! Returning null", this.getClass());
        return null;
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
     * @param pipes
     * @return <li> true, if available
     * <li> false, if not
     *
     */
    protected boolean testPipes(UsbPipes pipes) {

        byte[] readBuffer = new byte[1024];
        UsbPipe pipeRead, pipeWrite;
        UsbIrp irpWrite;
        int ansBytes;
        long elapsedTimeMilliseconds;
        String status;
        boolean validStatus = false, mismatchDetected = false;

        if (pipes == null) {
            return false;
        }

//        // Confirm the USB device it's ok and working properly
        if (pipes.getUsbPipeWrite() == null || pipes.getUsbPipeRead() == null) {
            try {
                pipes.close();
                return false;
            } catch (Exception ex) {
                Base.writeLog("*testPipes1* <Exception> " + ex.getMessage(), this.getClass());
                return false;
            }
        }

        pipeRead = pipes.getUsbPipeRead();
        pipeWrite = pipes.getUsbPipeWrite();
        irpWrite = pipes.getUsbPipeWrite().createUsbIrp();
        irpWrite.setData("M625\n".getBytes());

        try {
            if (dispatchCommandLock.tryLock(500, TimeUnit.MILLISECONDS)) {
                try {
                    try {
                        if (!pipes.getUsbPipeRead().getUsbEndpoint().getUsbInterface().isClaimed()) {
                            pipes.getUsbPipeRead().getUsbEndpoint().getUsbInterface().claim();
                        }
                        if (!pipes.isOpen()) {
                            pipes.open();
                        }

                        pipeWrite.syncSubmit(irpWrite);

                    } catch (IllegalArgumentException ex) {
                        Base.writeLog("*testPipes2* <IllegalArgumentException> " + ex.getMessage(), this.getClass());
                    } catch (Exception ex) {
                        Base.writeLog("*testPipes2* <Exception> " + ex.getMessage(), this.getClass());
                        setInitialized(false);
                        return false;
                    }

                    // clean up
                    try {
                        while (validStatus == false) {
                            elapsedTimeMilliseconds = System.currentTimeMillis();
                            ansBytes = pipeRead.syncSubmit(readBuffer);
                            elapsedTimeMilliseconds = System.currentTimeMillis() - elapsedTimeMilliseconds;

                            if (elapsedTimeMilliseconds > 500) {
                                isBusy = true;
                                break;
                            }

                            if (ansBytes > 0) {
                                try {
                                    status = new String(readBuffer, 0, ansBytes, "UTF-8").trim();

                                    // when printer is in bootloader, M625 returns bad code
                                    if (!status.contains("S:") && !status.contains("Bad")) {
                                        mismatchDetected = true;
                                    } else {
                                        validStatus = true;

                                        // throw away the status message if there was a problem
                                        // since it may now be outdated
                                        if (!mismatchDetected) {
                                            processStatus(status);
                                        }
                                    }
                                } catch (UnsupportedEncodingException ex) {
                                    Base.writeLog("Unsupported encoding! (system doesn't support UTF-8?)", this.getClass());
                                }
                            }
                        }
                    } catch (IllegalArgumentException ex) {
                        Base.writeLog("*testPipes3* <IllegalArgumentException> " + ex.getMessage(), this.getClass());
                    } catch (Exception ex) {
                        Base.writeLog("*testPipes3* <Exception> " + ex.getMessage(), this.getClass());
                        setInitialized(false);
                        return false;
                    }

                } finally {
                    dispatchCommandLock.unlock();
                }
            } else {
                isBusy = true;
            }
        } catch (InterruptedException ex) {
        }

        return true;
    }

    private void processStatus(String status) {

        final boolean machineReady, machinePaused, machineShutdown, machinePrinting, machinePowerSaving, machineOperational;
        lastStatusMessage = status;

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

        machineReady = status.contains("S:3");
        machinePowerSaving = status.contains("Power_Saving");
        machineShutdown = status.contains("S:9") || status.toLowerCase().contains("shutdown");
        machinePrinting = status.contains("S:5");
        machinePaused = status.contains("Pause");
        machineOperational = machineReady || machineShutdown || machinePrinting || machinePaused;

        machine.setLastStatusString(status);
        machine.setMachineReady(machineReady);
        machine.setMachinePaused(machinePaused);
        machine.setMachinePowerSaving(machinePowerSaving);
        machine.setMachineShutdown(machineShutdown);
        machine.setMachinePrinting(machinePrinting);
        machine.setMachineOperational(machineOperational);
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

    /**
     *
     * @param pipes
     * @throws UsbNotActiveException
     * @throws UsbDisconnectedException
     */
    protected void openPipe(UsbPipes pipes) {
        try {
            if (!pipes.getUsbEndpoint().getUsbInterface().isClaimed()) {
                pipes.getUsbEndpoint().getUsbInterface().claim();
            }
            if (!pipes.isOpen()) {
                pipes.open();
            }
            setInitialized(true);
            Base.writeLog("Pipes have been opened", this.getClass());
        } catch (UsbClaimException ex) {
            Base.writeLog("*openPipe* <UsbClaimException> " + ex.getMessage(), this.getClass());
            setInitialized(false);
        } catch (UsbException ex) {
            Base.writeLog("*openPipe* <UsbException> " + ex.getMessage(), this.getClass());
            setInitialized(false);
        } catch (UsbNotActiveException ex) {
            Base.writeLog("*openPipe* <UsbNotActiveException> " + ex.getMessage(), this.getClass());
            setInitialized(false);
        } catch (UsbDisconnectedException ex) {
            Base.writeLog("*openPipe* <UsbDisconnectedException> " + ex.getMessage(), this.getClass());
            setInitialized(false);
        } catch (UsbNotClaimedException ex) {
            Base.writeLog("*openPipe* <UsbNotClaimedException> " + ex.getMessage(), this.getClass());
            setInitialized(false);
        } catch (Exception ex) {
            Base.writeLog("*openPipe* <Unknown> " + ex.getMessage(), this.getClass());
            setInitialized(false);
        }
    }

    /**
     *
     * @param pipes
     * @throws UsbNotActiveException
     * @throws UsbDisconnectedException
     */
    protected void closePipe(UsbPipes pipes) {
        try {
            setInitialized(false);
            pipes.close();
            pipes.getUsbEndpoint().getUsbInterface().release();
        } catch (Exception ex) {
            Base.writeLog("*closePipe* <Exception> " + ex.getMessage(), this.getClass());
        }
    }

    @Override
    public String getLastStatusMessage() {
        return lastStatusMessage;
    }

    @Override
    public void dispose() {
        m_usbDevice = null;
        connectedDevice = PrinterInfo.UNKNOWN;
        setMachine(new MachineModel());
    }

    /**
     * Sleeps driver for 1 nano second.
     */
    @Override
    public void hiccup() {
        //sleep for a nano second just for luck
        hiccup(0, 1);
    }

    /**
     * Sleeps driver for specified duration.
     *
     * @param mili miliseconds to sleep.
     * @param nano nanoseconds to sleep.
     */
    @Override
    public void hiccup(int mili, int nano) {

        try {
            Thread.sleep(mili, nano);
        } catch (InterruptedException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
