package pt.beeverycreative.beesoft.drivers.usb;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 *
 * @author jgrego
 */
@RunWith(JUnitParamsRunner.class)
public class PrinterInfoTest {

    private static final String FIRMWARE_VERSION = "10.5.11";

    private Object[] parametersForTestValueOf() {
        return new Object[]{
            new Object[]{PrinterInfo.BEETHEFIRST0, "BEETHEFIRST0"},
            new Object[]{PrinterInfo.BEETHEFIRST, "BEETHEFIRST"},
            new Object[]{PrinterInfo.BEETHEFIRST_PLUS, "BEETHEFIRST_PLUS"},
            new Object[]{PrinterInfo.BEEME, "BEEME"},
            new Object[]{PrinterInfo.BEEINSCHOOL, "BEEINSCHOOL"},
            new Object[]{PrinterInfo.BEETHEFIRST_PLUS_A, "BEETHEFIRST_PLUS_A"},
            new Object[]{PrinterInfo.BEEINSCHOOL_A, "BEEINSCHOOL_A"},
            new Object[]{PrinterInfo.UNKNOWN, "UNKNOWN"}
        };
    }

    private Object[] parametersForTestFilamentCode() {
        return new Object[]{
            new Object[]{"BEETHEFIRST", PrinterInfo.BEETHEFIRST0},
            new Object[]{"BEETHEFIRST", PrinterInfo.BEETHEFIRST},
            new Object[]{"BEETHEFIRSTPLUS", PrinterInfo.BEETHEFIRST_PLUS},
            new Object[]{"BEEME", PrinterInfo.BEEME},
            new Object[]{"BEEINSCHOOL", PrinterInfo.BEEINSCHOOL},
            new Object[]{"BEETHEFIRSTPLUS", PrinterInfo.BEETHEFIRST_PLUS_A},
            new Object[]{"BEEINSCHOOL", PrinterInfo.BEEINSCHOOL_A},
            new Object[]{"UNKNOWN", PrinterInfo.UNKNOWN}
        };
    }

    private Object[] parametersForTestPresentationName() {
        return new Object[]{
            new Object[]{"BEETHEFIRST", PrinterInfo.BEETHEFIRST0},
            new Object[]{"BEETHEFIRST", PrinterInfo.BEETHEFIRST},
            new Object[]{"BEETHEFIRST+", PrinterInfo.BEETHEFIRST_PLUS},
            new Object[]{"BEEME", PrinterInfo.BEEME},
            new Object[]{"BEEINSCHOOL", PrinterInfo.BEEINSCHOOL},
            new Object[]{"BEETHEFIRST+", PrinterInfo.BEETHEFIRST_PLUS_A},
            new Object[]{"BEEINSCHOOL", PrinterInfo.BEEINSCHOOL_A},
            new Object[]{"UNKNOWN", PrinterInfo.UNKNOWN}
        };
    }

    private Object[] parametersForTestProductID() {
        return new Object[]{
            new Object[]{(short) 0x014e, PrinterInfo.BEETHEFIRST0},
            new Object[]{(short) 0x0001, PrinterInfo.BEETHEFIRST},
            new Object[]{(short) 0x0002, PrinterInfo.BEETHEFIRST_PLUS},
            new Object[]{(short) 0x0003, PrinterInfo.BEEME},
            new Object[]{(short) 0x0004, PrinterInfo.BEEINSCHOOL},
            new Object[]{(short) 0x0005, PrinterInfo.BEETHEFIRST_PLUS_A},
            new Object[]{(short) 0x0006, PrinterInfo.BEEINSCHOOL_A},
            new Object[]{(short) 0xffff, PrinterInfo.UNKNOWN}
        };
    }

    private Object[] parametersForTestVendorID() {
        return new Object[]{
            new Object[]{(short) 0xffff, PrinterInfo.BEETHEFIRST0},
            new Object[]{(short) 0x29c9, PrinterInfo.BEETHEFIRST},
            new Object[]{(short) 0x29c9, PrinterInfo.BEETHEFIRST_PLUS},
            new Object[]{(short) 0x29c9, PrinterInfo.BEEME},
            new Object[]{(short) 0x29c9, PrinterInfo.BEEINSCHOOL},
            new Object[]{(short) 0x29c9, PrinterInfo.BEETHEFIRST_PLUS_A},
            new Object[]{(short) 0x29c9, PrinterInfo.BEEINSCHOOL_A},
            new Object[]{(short) 0xffff, PrinterInfo.UNKNOWN}
        };
    }

    private Object[] parametersForTestFirmwareFilename() {
        return new Object[]{
            new Object[]{"BEEVC-BEETHEFIRST0-" + FIRMWARE_VERSION + ".BIN", PrinterInfo.BEETHEFIRST0},
            new Object[]{"BEEVC-BEETHEFIRST-" + FIRMWARE_VERSION + ".BIN", PrinterInfo.BEETHEFIRST},
            new Object[]{"BEEVC-BEETHEFIRST_PLUS-" + FIRMWARE_VERSION + ".BIN", PrinterInfo.BEETHEFIRST_PLUS},
            new Object[]{"BEEVC-BEEME-" + FIRMWARE_VERSION + ".BIN", PrinterInfo.BEEME},
            new Object[]{"BEEVC-BEETHEFIRST_PLUS_A-" + FIRMWARE_VERSION + ".BIN", PrinterInfo.BEETHEFIRST_PLUS_A},
            new Object[]{"BEEVC-BEEINSCHOOL_A-" + FIRMWARE_VERSION + ".BIN", PrinterInfo.BEEINSCHOOL_A},
            new Object[]{"", PrinterInfo.UNKNOWN}
        };
    }

    private Object[] parametersForTestBootloaderString() {
        return new Object[]{
            new Object[]{"BEEVC-BEETHEFIRST0-" + FIRMWARE_VERSION, PrinterInfo.BEETHEFIRST0},
            new Object[]{"BEEVC-BEETHEFIRST-" + FIRMWARE_VERSION, PrinterInfo.BEETHEFIRST},
            new Object[]{"BEEVC-BEETHEFIRST_PLUS-" + FIRMWARE_VERSION, PrinterInfo.BEETHEFIRST_PLUS},
            new Object[]{"BEEVC-BEEME-" + FIRMWARE_VERSION, PrinterInfo.BEEME},
            new Object[]{"BEEVC-BEEINSCHOOL-" + FIRMWARE_VERSION, PrinterInfo.BEEINSCHOOL},
            new Object[]{"BEEVC-BEETHEFIRST_PLUS_A-" + FIRMWARE_VERSION, PrinterInfo.BEETHEFIRST_PLUS_A},
            new Object[]{"BEEVC-BEEINSCHOOL_A-" + FIRMWARE_VERSION, PrinterInfo.BEEINSCHOOL_A},
            new Object[]{"UNKNOWN-UNKNOWN-0.0.0", PrinterInfo.UNKNOWN}
        };
    }

    private Object[] parametersForTestIconFilename() {
        return new Object[]{
            new Object[]{"logo_beethefirst.png", PrinterInfo.BEETHEFIRST0},
            new Object[]{"logo_beethefirst.png", PrinterInfo.BEETHEFIRST},
            new Object[]{"logo_beethefirst_plus.png", PrinterInfo.BEETHEFIRST_PLUS},
            new Object[]{"logo_beeme.png", PrinterInfo.BEEME},
            new Object[]{"logo_beeinschool.png", PrinterInfo.BEEINSCHOOL},
            new Object[]{"logo_beethefirst_plusA.png", PrinterInfo.BEETHEFIRST_PLUS_A},
            new Object[]{"logo_beeinschool_A.png", PrinterInfo.BEEINSCHOOL_A},
            new Object[]{"", PrinterInfo.UNKNOWN}
        };
    }

    private Object[] parametersForTestVersionString() {
        return new Object[]{
            new Object[]{"-1:334", PrinterInfo.BEETHEFIRST0},
            new Object[]{"10697:1", PrinterInfo.BEETHEFIRST},
            new Object[]{"10697:2", PrinterInfo.BEETHEFIRST_PLUS},
            new Object[]{"10697:3", PrinterInfo.BEEME},
            new Object[]{"10697:4", PrinterInfo.BEEINSCHOOL},
            new Object[]{"10697:5", PrinterInfo.BEETHEFIRST_PLUS_A},
            new Object[]{"10697:6", PrinterInfo.BEEINSCHOOL_A},
            new Object[]{"-1:-1", PrinterInfo.UNKNOWN}
        };
    }

    private Object[] parametersForTestGetDevice() {
        return new Object[]{
            new Object[]{PrinterInfo.BEETHEFIRST0, "-1:334"},
            new Object[]{PrinterInfo.BEETHEFIRST, "10697:1",},
            new Object[]{PrinterInfo.BEETHEFIRST_PLUS, "10697:2"},
            new Object[]{PrinterInfo.BEEME, "10697:3"},
            new Object[]{PrinterInfo.BEEINSCHOOL, "10697:4"},
            new Object[]{PrinterInfo.BEETHEFIRST_PLUS_A, "10697:5"},
            new Object[]{PrinterInfo.BEEINSCHOOL_A, "10697:6"},
            new Object[]{PrinterInfo.UNKNOWN, "-1:-1"}
        };
    }

    private Object[] parametersForTestGetDeviceByFormalName() {
        return new Object[]{
            new Object[]{PrinterInfo.BEETHEFIRST0, "BEETHEFIRST"},
            new Object[]{PrinterInfo.BEETHEFIRST_PLUS, "BEETHEFIRST+"},
            new Object[]{PrinterInfo.BEEME, "BEEME"},
            new Object[]{PrinterInfo.BEEINSCHOOL, "BEEINSCHOOL"}
        };
    }

    private Object[] parametersForTestToString() {
        return new Object[]{
            new Object[]{"BEETHEFIRST", PrinterInfo.BEETHEFIRST0},
            new Object[]{"BEETHEFIRST", PrinterInfo.BEETHEFIRST},
            new Object[]{"BEETHEFIRST+", PrinterInfo.BEETHEFIRST_PLUS},
            new Object[]{"BEEME", PrinterInfo.BEEME},
            new Object[]{"BEEINSCHOOL", PrinterInfo.BEEINSCHOOL},
            new Object[]{"BEETHEFIRST+", PrinterInfo.BEETHEFIRST_PLUS_A},
            new Object[]{"BEEINSCHOOL", PrinterInfo.BEEINSCHOOL_A},
            new Object[]{"UNKNOWN", PrinterInfo.UNKNOWN},};
    }

    /**
     * Test of values method, of class PrinterInfo.
     */
    @Test
    public void testValues() {
        final PrinterInfo[] expResult = {
            PrinterInfo.BEETHEFIRST0,
            PrinterInfo.BEETHEFIRST,
            PrinterInfo.BEETHEFIRST_PLUS,
            PrinterInfo.BEEME,
            PrinterInfo.BEEINSCHOOL,
            PrinterInfo.BEETHEFIRST_PLUS_A,
            PrinterInfo.BEEINSCHOOL_A,
            PrinterInfo.UNKNOWN
        };
        final PrinterInfo[] result = PrinterInfo.values();
        assertArrayEquals(expResult, result);
    }

    @Test
    @Parameters
    public void testValueOf(PrinterInfo expResult, String printerName) {
        assertEquals(expResult, PrinterInfo.valueOf(printerName));
    }

    @Test
    @Parameters
    public void testFilamentCode(String expResult, PrinterInfo printerInfo) {
        assertEquals(expResult, printerInfo.filamentCode());
    }

    @Test
    @Parameters
    public void testPresentationName(String expResult, PrinterInfo printerInfo) {
        assertEquals(expResult, printerInfo.presentationName());
    }

    @Test
    @Parameters
    public void testProductID(short expResult, PrinterInfo printerInfo) {
        assertEquals(expResult, printerInfo.productID());
    }

    @Test
    @Parameters
    public void testVendorID(short expResult, PrinterInfo printerInfo) {
        assertEquals(expResult, printerInfo.vendorID());
    }

    @Test
    @Parameters
    public void testFirmwareFilename(String expResult, PrinterInfo printerInfo) {
        assertEquals(expResult, printerInfo.firmwareFilename());
    }

    @Test
    @Parameters
    public void testBootloaderString(String expResult, PrinterInfo printerInfo) {
        assertEquals(expResult, printerInfo.bootloaderString());
    }

    @Test
    @Parameters
    public void testIconFilename(String expResult, PrinterInfo printerInfo) {
        assertEquals(expResult, printerInfo.iconFilename());
    }

    @Test
    @Parameters
    public void testVersionString(String expResult, PrinterInfo printerInfo) {
        assertEquals(expResult, printerInfo.versionString());
    }

    @Test
    @Parameters
    public void testGetDevice(PrinterInfo expResult, String versionString) {
        assertEquals(expResult, PrinterInfo.getDevice(versionString));
    }

    @Test
    @Parameters
    public void testGetDeviceByFormalName(PrinterInfo expResult, String formalName) {
        assertEquals(expResult, PrinterInfo.getDeviceByFormalName(formalName));
    }

    @Test
    @Parameters({
        "BEETHEFIRST, BEETHEFIRST",
        "BEETHEFIRST+, BEETHEFIRSTPLUS",
        "BEEME, BEEME",
        "BEEINSCHOOL, BEEINSCHOOL"
    })
    public void testFilamentCodeToFormalString(String expResult, String filamentCode) {
        assertEquals(expResult, PrinterInfo.filamentCodeToFormalString(filamentCode));
    }

    @Test
    @Parameters
    public void testToString(String expResult, PrinterInfo printerInfo) {
        assertEquals(expResult, printerInfo.toString());
    }

}
