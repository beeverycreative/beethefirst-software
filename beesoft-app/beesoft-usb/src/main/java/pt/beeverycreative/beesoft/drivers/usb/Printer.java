package pt.beeverycreative.beesoft.drivers.usb;

import replicatorg.app.Base;

/**
 *
 * @author dev
 */
public enum Printer {
    BEETF_0         ((short) 0xffff, (short) 0x014e, "BVC-BEETHEFIRST_OLD-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN"),
    BEETF_1         ((short) 0x29c9, (short) 0x0001, "BVC-BEETHEFIRST-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN"),
    BEE_PLUS        ((short) 0x29c9, (short) 0x0002, "BVC-BEE_PLUS-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN"),
    BEE_ME          ((short) 0x29c9, (short) 0x0003, "BVC-BEE_ME-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN"),
    BEE_IN_SCHOOL   ((short) 0x29c9, (short) 0x0004, "BVC-BEE_SHOOL-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN"),
    UNKNOWN         ((short) 0x0000, (short) 0x0000, "");
    
    private final short productID;
    private final short vendorID;
    private final String firmwareFilename;
    
    Printer(short vendorID, short productID, String firmwareFilename) {
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
    
    public static Printer getDevice(String versionString) {
        for(Printer printer : Printer.values()) {
            if(printer.versionString().equalsIgnoreCase(versionString)) {
                return printer;
            }
        }
        return UNKNOWN;
    }  
    
}
