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
import org.w3c.dom.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbIrp;
import javax.usb.UsbNotOpenException;
import javax.usb.UsbPipe;
import static pt.beeverycreative.beesoft.drivers.usb.UsbDriver.m_usbDevice;
import replicatorg.app.ProperDefault;
import replicatorg.app.Base;

import replicatorg.app.tools.XML;
import replicatorg.drivers.DriverBaseImplementation;

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

    protected static UsbDevice m_usbDevice;

    private static String m_manufacturer = "BEEVERYCREATIVE";
    private static String m_productNew = "BEETHEFIRST";
    protected static String m_productOld = "BEETHEFIRST - ";
    protected static String oldCompatibleVersion = "BEETHEFIRST-3.";
    private final short BEEVERYCREATIVE_VENDOR_ID = (short) 0xffff;
    private final short BEEVERYCREATIVE_NEW_VENDOR_ID = (short) 0x29c9;
    
    protected PrinterInfo connectedDevice;
    
    //check this, maybe delete
    protected boolean isNewVendorID = false;
    
    protected ArrayList<UsbDevice> m_usbDeviceList = new ArrayList<UsbDevice>();
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
    private double extrudedDistance = 0;
    private double totalExtrudedDistance = 0;
    private final double extruderLimit = 100000; // average string lenght/bobine 105 +- 10 meters
    protected boolean transferMode = false;
    protected boolean isONShutdown = false;

    /**
     * USBDriver high level definition.
     *
     */
    protected UsbDriver() {
    }
    
    /**
     * Gets the info object on the currently connected printer
     * 
     * @return 
     */
    public PrinterInfo getConnectedDevice() {
        return this.connectedDevice;
    }

    /**
     * Loads machine xml.
     *
     * @param xml XML file with machine properties.
     */
    public void loadXml(Node xml) {
        super.loadXML(xml);

        //load from our XML config, if we have it
        if (XML.hasChildNode(xml, "manufacturer")) {
            m_manufacturer = XML.getChildNodeValue(xml, "manufacturer");
        }

        if (XML.hasChildNode(xml, "product")) {
            m_productNew = XML.getChildNodeValue(xml, "product");
        }
    }

    /**
     * Reads E value form each G1 and G92 command Calculates extrudedDistance
     * and totalExtrudedDistance
     *
     * @param command command sent to machine
     */
    protected void getEValue(String command) {
        double c_Value;

        /**
         * It can consider two possible switch cases: G1 X-100.0 Y-20.0 Z5.0
         * F3000 - G1 X31.8854 Y-38.825 E194.37539
         */
        if (command.contains("G1") && command.contains("E")) // Movement and extrusion command
        {
            int indexDot = command.indexOf(";");
            if (indexDot > 0) {
                command = command.substring(0, indexDot);
            }

            c_Value = Double.parseDouble(command.split("E")[1].split("N")[0]); // Gets E value  
            if (c_Value > extrudedDistance) // Compares E value with previous stored for update 
            {
                extrudedDistance = c_Value; // Updates local and parcial variable with current extruded value
            }
        } else if ((command.contains("G92") && command.contains("E")) || command.contains("G92")) {
            // Updates local and total variable with current extruded value
            totalExtrudedDistance += extrudedDistance;

            //System.out.println(totalExtrudedDistance);
            /**
             * Stores totalExtruded for this print session
             */
            ProperDefault.put("lastSession_totalExtruded", String.valueOf(totalExtrudedDistance));
            /**
             * Stores filamentRemaing in coil after this print session
             * ATTENTION: It uses the value inserted from the user, with the PLA
             * remaining
             */
            ProperDefault.put("filamentCoilRemaining", String.valueOf(Double.valueOf(ProperDefault.get("filamentCoilRemaining")) - totalExtrudedDistance));
            /**
             * Stores total extruded after this print session
             */
            ProperDefault.put("totalExtruded", String.valueOf(Double.valueOf(ProperDefault.get("totalExtruded")) + totalExtrudedDistance));

//        System.out.println("lastSession_totalExtruded "+String.valueOf(totalExtrudedDistance)+
//                "lastSession_filamentRemaining " + String.valueOf(Double.valueOf(ProperDefault.get("filamentCoilRemaining"))-totalExtrudedDistance)+
//                "totalExtruded " +String.valueOf(Double.valueOf(ProperDefault.get("totalExtruded"))+totalExtrudedDistance));
        }

    }

    /**
     * Resets extrusion variables.
     */
    @Override
    public void resetExtrudeSession() {
        totalExtrudedDistance = 0;
        extrudedDistance = 0;
    }

    /**
     * Returns totalExtrudedValue.
     *
     * @return extrudedValue
     */
    @Override
    public double getTotalExtrudedValue() {
        return totalExtrudedDistance;
    }

    /**
     * Inits USB device.
     *
     * @param device USB device from descriptor.
     */
    public void InitUsbDevice(UsbDevice device) {
        
        UsbDeviceDescriptor descriptor;
        descriptor = device.getUsbDeviceDescriptor();

        try {
            Base.writeLog("Device found - " + descriptor.idVendor() + ":" + descriptor.idProduct());

            if (device.isUsbHub()) {
                Base.writeLog("Found a USB hub");
                UsbHub hub = (UsbHub) device;

                for (UsbDevice child : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
                    InitUsbDevice(child);
                }
                
                
            } else {

                //Try to add using the new way, then using the old way.
                if (addIfCompatible(device) == false) {
                    addIfCompatible_Legacy(device);
                }
            }
        } catch (UsbException ex) {
            m_usbDevice = null;
            Base.writeLog("Could not verify or add device:"
                    + ex.getMessage() + ":" + ex.toString());
        } catch (UnsupportedEncodingException ex) {
            m_usbDevice = null;
            Base.writeLog("Could not verify or add device:"
                    + ex.getMessage() + ":" + ex.toString());
        } catch (UsbDisconnectedException ex) {
            m_usbDevice = null;
            Base.writeLog("Could not verify or add device:"
                    + ex.getMessage() + ":" + ex.toString());
        }
    }

    
    public boolean addIfCompatible(UsbDevice device) throws UsbException, UnsupportedEncodingException {
        
        UsbDeviceDescriptor descriptor;

        descriptor = device.getUsbDeviceDescriptor();

        short idVendor = descriptor.idVendor();
        short idProduct = descriptor.idProduct();
        
        String sDevice = idVendor+":"+idProduct;
        
        // candidate
        connectedDevice = PrinterInfo.getDevice(sDevice);
        
        if(connectedDevice == PrinterInfo.UNKNOWN) {
            return false;
        } else {
            m_usbDeviceList.add(device);
            return true;
        }
    }
    
    public boolean addIfCompatible_Legacy(UsbDevice device) throws UsbException, UnsupportedEncodingException {

        UsbDeviceDescriptor descriptor;

        descriptor = device.getUsbDeviceDescriptor();

        short idVendor = descriptor.idVendor();
        short idProduct = descriptor.idProduct();

        

        if (idVendor == BEEVERYCREATIVE_VENDOR_ID) {

            String manufacturerString = device.getManufacturerString();
            String productString = device.getProductString();
            String SerialNumberString = device.getSerialNumberString().trim();

            if (manufacturerString.contains(m_manufacturer)
                    || productString.contains(m_productOld)) {

                Base.writeLog("Adding to candidate list.");

                m_usbDeviceList.add(device);

                Base.writeLog("Device - " + idVendor + ":" + idProduct);
                Base.writeLog(manufacturerString);
                Base.writeLog(productString);
                Base.writeLog(SerialNumberString);
                return true;

            }//else{System.out.println("No need for else.");}
        }
        if (idVendor == BEEVERYCREATIVE_NEW_VENDOR_ID) {

            String manufacturerString = device.getManufacturerString();
            String productString = device.getProductString();
            String SerialNumberString = device.getSerialNumberString().trim();

            if (manufacturerString.contains(m_manufacturer)
                    || productString.contains(m_productNew)) {

                Base.writeLog("Adding to candidate list.");

                m_usbDeviceList.add(device);
                Base.writeLog("Device - " + idVendor + ":" + idProduct);
                Base.writeLog(manufacturerString);
                Base.writeLog(productString);
                Base.writeLog(SerialNumberString);
                return true;


            }//else{System.out.println("No need for else.");}
        }
        return false;
    }

    /**
     * Scans descriptor and inits usb device if match.
     */
    public void InitUsbDevice() throws UsbException, UnsupportedEncodingException {
        m_usbDeviceList.clear();

        try {
            Base.writeLog("Getting device list.");

            UsbServices services = UsbHostManager.getUsbServices();
            Base.writeLog("USB Serviced obtained ");
            UsbHub rootHub = services.getRootUsbHub();
            Base.writeLog("rootUSB obtained ");
            InitUsbDevice(rootHub);
            Base.writeLog("RootUSB device inited");

        } catch (UsbException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UsbDisconnectedException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            setInitialized(false);
        }
        
        if (m_usbDeviceList.isEmpty()) {
            m_usbDevice = null;
            Base.writeLog("Failed to find USB device.");
            setInitialized(false);
        } else {
            //Chose the device to Initialize
            m_usbDevice = m_usbDeviceList.get(0);
            short idProduct = m_usbDevice.getUsbDeviceDescriptor().idProduct();
            short idVendor = m_usbDevice.getUsbDeviceDescriptor().idVendor();
            connectedDevice = PrinterInfo.getDevice(idVendor + ":" + idProduct);
            Base.getMainWindow().getButtons().setLogo(connectedDevice.iconFilename());
            if (Base.isMacOS()) {
                ((AbstractDevice) m_usbDevice).setActiveUsbConfigurationNumber();
            }
            
            if(m_usbDeviceList.size() == 1){
                Base.writeLog("Found 1 device, connecting.");
            } else {
                Base.writeLog("Multiple machines found connecting to the "
                    + "most recently connected one");
            }
        }
        
        
        UsbDeviceDescriptor descriptor;
        descriptor = m_usbDevice.getUsbDeviceDescriptor();
        if(descriptor.idVendor() == BEEVERYCREATIVE_NEW_VENDOR_ID){
            isNewVendorID = true;
        } //no need for else
        
        if(descriptor.idVendor() == BEEVERYCREATIVE_VENDOR_ID){
            isNewVendorID = false;
        } //no need for else
    }

    /**
     * Checks if device is connected.
     *
     * @return <li> true, if it is connected.
     * <li> false, if not.
     */
    public boolean deviceFound() {
        if (m_usbDevice == null) {
            Base.writeLog("USB Device not found");
            setInitialized(false);
            return false;
        } else {
            try {
                if (m_usbDevice.getManufacturerString() == null) {
                    setInitialized(false);
                    Base.writeLog("USB Device not found");
                    return false;
                }
            } catch (UsbException ex) {
                setInitialized(false);
                Base.writeLog("USB error: " + ex.getMessage());
            } catch (UnsupportedEncodingException ex) {
                setInitialized(false);
                Base.writeLog("USB unsupported encoding exception: " + ex.getMessage());
            } catch (UsbDisconnectedException ex) {
                setInitialized(false);
                Base.writeLog("USB disconnected exception: " + ex.getMessage());
            }
        }

        return true;
    }

    /**
     * Gets pipe from a USB device
     *
     * @param device USB device connected.
     * @return USB Pipes with Endpoints set.
     */
    protected UsbPipes GetPipe(UsbDevice device) {

        if (device != null) {
            UsbConfiguration config = device.getActiveUsbConfiguration();

            if (pipes == null || !testPipes(pipes)) {
                pipes = new UsbPipes();

            } else {
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
                        this.pipes.setUsbPipeWrite(endpoint.getUsbPipe());
                    }

                    if (endpoint.getDirection() == UsbConst.ENDPOINT_DIRECTION_IN) {
                        this.pipes.setUsbPipeRead(endpoint.getUsbPipe());
                    }
                }
                if (pipes.getUsbPipeRead() != null && pipes.getUsbPipeWrite() != null) {

//                if (testPipes(pipes)) {
                    return pipes;
//                } else {
//                    continue;
//                }

                } else {
                    pipes.setUsbPipeWrite(null);
                    pipes.setUsbPipeRead(null);
                }
            }
        }

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

//        // Confirm the USB device it's ok and working properly
        if (pipes.getUsbPipeWrite() == null || pipes.getUsbPipeRead() == null) {
            try {
                pipes.close();
                return false;
            } catch (UsbException ex) {
                Base.writeLog("USB exception: " + ex.getMessage());
                return false;
            } catch (UsbNotActiveException ex) {
                Base.writeLog("USB communication not active " + ex.getMessage());
                return false;
            } catch (UsbNotOpenException ex) {
                Base.writeLog("USB communication is down " + ex.getMessage());
                return false;
            } catch (UsbDisconnectedException ex) {
                Base.writeLog("USB disconnected exception: " + ex.getMessage());
                return false;
            }

        }

        UsbIrp usbIrp = pipes.getUsbPipeWrite().createUsbIrp();
        //DUMMY COMMAND
        usbIrp.setData("M637\n".getBytes());
        UsbIrp readUsbIrp = pipes.getUsbPipeRead().createUsbIrp();
        readUsbIrp.setAcceptShortPacket(true);

        try {

            if (!pipes.getUsbPipeRead().getUsbEndpoint().getUsbInterface().isClaimed()) {
                pipes.getUsbPipeRead().getUsbEndpoint().getUsbInterface().claim();
            }
            if (!pipes.isOpen()) {
                pipes.open();
            }

            if (!isBootloader) {
                if (!transferMode) {
                    if (pipes != null) {
                        UsbPipe pipeWrite = pipes.getUsbPipeWrite();
                        if (pipeWrite != null) {
                            if (usbIrp != null) {
                                pipeWrite.syncSubmit(usbIrp);
                            }
                        }
                    }
                }
            }

        } catch (UsbException ex) {
            Base.writeLog("USB exception: " + ex.getMessage());
            //Logger.getLogger(UsbDriver.class.getName()).log(Level.SEVERE, null, ex);
            setInitialized(false);
            return false;
        } catch (UsbNotActiveException ex) {
            Base.writeLog("USB communication is not active: " + ex.getMessage());
            setInitialized(false);
            return false;
        } catch (UsbNotOpenException ex) {
            Base.writeLog("USB communication is down " + ex.getMessage());
            setInitialized(false);
            return false;
        } catch (IllegalArgumentException ex) {
            Base.writeLog("USB exception: " + ex.getMessage());
            //Logger.getLogger(UsbDriver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UsbDisconnectedException ex) {
            Base.writeLog("USB disconnected exception: " + ex.getMessage());
            setInitialized(false);
            return false;
        } catch (Exception ex) {
            Base.writeLog("Exception test pipes: " + ex.getMessage());
//            setInitialized(false);
//            return false;
        }
        return true;

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
        } catch (UsbClaimException ex) {
            Base.writeLog("USB Claim Exception [openPipe]: " + ex.getMessage());
            setInitialized(false);
        } catch (UsbException ex) {
            Base.writeLog("USB exception [openPipe]: " + ex.getMessage());
            //Logger.getLogger(UsbDriver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UsbNotActiveException ex) {
            Base.writeLog("USB communication not active [openPipe]:" + ex.getMessage());
            setInitialized(false);
        } catch (UsbDisconnectedException ex) {
            Base.writeLog("USB disconnected exception [openPipe]:" + ex.getMessage());
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
        } catch (UsbException ex) {
            Base.writeLog("USB exception: " + ex.getMessage());
            //Logger.getLogger(UsbDriver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UsbNotActiveException ex) {
            Base.writeLog("USB communication not active " + ex.getMessage());
            //Logger.getLogger(UsbDriver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UsbNotOpenException ex) {
            Base.writeLog("USB communication is down " + ex.getMessage());
            //Logger.getLogger(UsbDriver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UsbDisconnectedException ex) {
            Base.writeLog("USB disconnected exception: " + ex.getMessage());
            //Logger.getLogger(UsbDriver.class.getName()).log(Level.SEVERE, null, ex);
        }
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
