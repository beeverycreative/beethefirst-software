package pt.beeverycreative.beesoft.drivers.usb;

import replicatorg.app.Base;

/**
 *
 * @author jpgrego
 */
public enum PrinterInfo {

    BEETHEFIRST0("BEETHEFIRST", (short) 0xffff, (short) 0x014e, Base.CONFIG_PROPERTIES.getFirmwareProperty("firmware.beethefirst0"), "logo_beethefirst.png"),
    BEETHEFIRST("BEETHEFIRST", (short) 0x29c9, (short) 0x0001, Base.CONFIG_PROPERTIES.getFirmwareProperty("firmware.beethefirst"), "logo_beethefirst.png"),
    BEETHEFIRST_PLUS("BEETHEFIRSTPLUS", "BEETHEFIRST+", (short) 0x29c9, (short) 0x0002, Base.CONFIG_PROPERTIES.getFirmwareProperty("firmware.beethefirstplus"), "logo_beethefirst_plus.png"),
    BEEME("BEEME", (short) 0x29c9, (short) 0x0003, Base.CONFIG_PROPERTIES.getFirmwareProperty("firmware.beeme"), "logo_beeme.png"),
    BEEINSCHOOL("BEEINSCHOOL", (short) 0x29c9, (short) 0x0004, Base.CONFIG_PROPERTIES.getFirmwareProperty("firmware.beeinschool"), "logo_beeinschool.png"),
    BEETHEFIRST_PLUS_A("BEETHEFIRSTPLUS", "BEETHEFIRST+", (short) 0x29c9, (short) 0x0005, Base.CONFIG_PROPERTIES.getFirmwareProperty("firmware.beethefirstplusa"), "logo_beethefirst_plusA.png"),
    BEEINSCHOOL_A("BEEINSCHOOL", (short) 0x29c9, (short) 0x0006, Base.CONFIG_PROPERTIES.getFirmwareProperty("firmware.beeinschoola"), "logo_beeinschool_A.png"),
    UNKNOWN("UNKNOWN", (short) 0xffff, (short) 0xffff, "", "");

    private final String filamentCode, firmwareFilename, iconFilename, presentationName;
    private final short productID;
    private final short vendorID;

    PrinterInfo(String filamentCode, short vendorID, short productID, String firmwareFilename, String iconFilename) {
        this.filamentCode = filamentCode;
        this.presentationName = filamentCode;
        this.productID = productID;
        this.vendorID = vendorID;
        this.firmwareFilename = firmwareFilename;
        this.iconFilename = iconFilename;
    }

    PrinterInfo(String filamentCode, String presentationName, short vendorID, short productID, String firmwareFilename, String iconFilename) {
        this.filamentCode = filamentCode;
        this.presentationName = presentationName;
        this.productID = productID;
        this.vendorID = vendorID;
        this.firmwareFilename = firmwareFilename;
        this.iconFilename = iconFilename;
    }

    public String filamentCode() {
        return filamentCode;
    }

    public String presentationName() {
        return presentationName;
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

    public String bootloaderString() {
        return Version.fromFilename(firmwareFilename);
    }

    public String iconFilename() {
        return iconFilename;
    }

    public String versionString() {
        return vendorID + ":" + productID;
    }

    public static PrinterInfo getDevice(String versionString) {
        for (PrinterInfo printer : PrinterInfo.values()) {
            if (printer.versionString().equalsIgnoreCase(versionString)) {
                return printer;
            }
        }
        return UNKNOWN;
    }

    public static PrinterInfo getDeviceByFormalName(String formalName) {
        for (PrinterInfo printer : PrinterInfo.values()) {
            if (printer.presentationName.equals(formalName)) {
                return printer;
            }
        }

        return UNKNOWN;
    }

    public static String filamentCodeToFormalString(String filamentCode) {

        if (filamentCode.equals(PrinterInfo.BEETHEFIRST_PLUS.filamentCode)) {
            return PrinterInfo.BEETHEFIRST_PLUS.presentationName;
        }

        // filamentCode == presentationName
        return filamentCode;
    }

    @Override
    public String toString() {
        return presentationName;
    }

}
