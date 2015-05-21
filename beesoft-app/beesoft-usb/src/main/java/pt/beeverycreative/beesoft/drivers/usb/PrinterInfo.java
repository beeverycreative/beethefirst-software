package pt.beeverycreative.beesoft.drivers.usb;

import replicatorg.app.Base;

/**
 *
 * @author dev
 */
public enum PrinterInfo {
    BEETF0         ((short) 0xffff, (short) 0x014e, "BVC-BEETHEFIRST_OLD-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN"),
    BEETF1         ((short) 0x29c9, (short) 0x0001, "BVC-BEETHEFIRST-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN"),
    BEEPLUS        ((short) 0x29c9, (short) 0x0002, "BVC-BEETHEFIRST_PLUS-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN"),
    BEEME          ((short) 0x29c9, (short) 0x0003, "BVC-BEE_ME-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN"),
    BEEINSCHOOL   ((short) 0x29c9, (short) 0x0004, "BVC-BEE_IN_SCHOOL-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN"),
    UNKNOWN         ((short) 0x0000, (short) 0x0000, "");
    
    private final short productID;
    private final short vendorID;
    private final String firmwareFilename;
    
    PrinterInfo(short vendorID, short productID, String firmwareFilename) {
        this.productID = productID;
        this.vendorID = vendorID;
        this.firmwareFilename = firmwareFilename;
    }
    
    public short productID() {
        return productID;
    }
    
    public short vendorID() {
        return vendorID;
    }
    
    public String firmwareFilename() {
        return firmwareFilename;
    }
    
    public String versionString() {
        return vendorID + ":" + productID;
    }
    
    public static PrinterInfo getDevice(String versionString) {
        for(PrinterInfo printer : PrinterInfo.values()) {
            if(printer.versionString().equalsIgnoreCase(versionString)) {
                return printer;
            }
        }
        return UNKNOWN;
    }  
    
}
