package pt.beeverycreative.beesoft.drivers.usb;

import replicatorg.app.Base;

/**
 *
 * @author dev
 */
public enum PrinterInfo {
    BEETHEFIRST0    ("BEETHEFIRST", (short) 0xffff, (short) 0x014e, "BEEVC-BEETHEFIRST0-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN", "logo_beethefirst.png"),
    BEETHEFIRST     ("BEETHEFIRST", (short) 0x29c9, (short) 0x0001, "BEEVC-BEETHEFIRST-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN", "logo_beethefirst.png"),
    BEETHEFIRST_PLUS ("BEETHEFIRSTPLUS", (short) 0x29c9, (short) 0x0002, "BEEVC-BEETHEFIRST-PLUS-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN", "logo_beethefirst_plus.png"),
    BEEME           ("BEEME", (short) 0x29c9, (short) 0x0003, "BEEVC-BEEME-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN", "logo_beeme.png"),
    BEEINSCHOOL     ("BEEINSCHOOL", (short) 0x29c9, (short) 0x0004, "BEEVC-BEEINSCHOOL-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN", "logo_beeinschool.png"),
    BEETHEFIRST_PLUS_A ("BEETHEFIRSTPLUS", (short) 0x29c9, (short) 0x0005, "BEEVC-BEETHEFIRST-PLUS-A-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN", "logo_beethefirst_plus.png"),
    UNKNOWN         ("BEETHEFIRST", (short) 0x0000, (short) 0x0000, "", "");
    
    private final String filamentCode;
    private final short productID;
    private final short vendorID;
    private final String firmwareFilename;
    private final String iconFilename;
    
    PrinterInfo(String filamentCode, short vendorID, short productID, String firmwareFilename, String iconFilename) {
        this.filamentCode = filamentCode;
        this.productID = productID;
        this.vendorID = vendorID;
        this.firmwareFilename = firmwareFilename;
        this.iconFilename = iconFilename;
    }
    
    public String filamentCode() {
        return filamentCode;
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
    
    public String iconFilename() {
        return iconFilename;
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
    
    public static PrinterInfo getDeviceByName(String printerName) {
        for(PrinterInfo printer : PrinterInfo.values()) {
            if(printer.toString().equals(printerName)) {
                return printer;
            }
        }
        
        return UNKNOWN;
    }
}
