package pt.beeverycreative.beesoft.drivers.usb;

import replicatorg.app.Base;

/**
 *
 * @author dev
 */
public enum PrinterInfo {
    BEETHEFIRST0    ((short) 0xffff, (short) 0x014e, "BEEVC-BEETHEFIRST0-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN", "logo_beethefirst.png"),
    BEETHEFIRST     ((short) 0x29c9, (short) 0x0001, "BEEVC-BEETHEFIRST-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN", "logo_beethefirst.png"),
    BEETHEFIRSTPLUS ((short) 0x29c9, (short) 0x0002, "BEEVC-BEETHEFIRSTPLUS-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN", "logo_beethefirst_plus.png"),
    BEEME           ((short) 0x29c9, (short) 0x0003, "BEEVC-BEEME-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN", "logo_beeme.png"),
    BEEINSCHOOL     ((short) 0x29c9, (short) 0x0004, "BEEVC-BEEINSCHOOL-Firmware-"+Base.VERSION_FIRMWARE_FINAL+".BIN", "logo_beeinschool.png"),
    UNKNOWN         ((short) 0x0000, (short) 0x0000, "", "");
    
    private final short productID;
    private final short vendorID;
    private final String firmwareFilename;
    private final String iconFilename;
    
    PrinterInfo(short vendorID, short productID, String firmwareFilename, String iconFilename) {
        this.productID = productID;
        this.vendorID = vendorID;
        this.firmwareFilename = firmwareFilename;
        this.iconFilename = iconFilename;
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
    
}
