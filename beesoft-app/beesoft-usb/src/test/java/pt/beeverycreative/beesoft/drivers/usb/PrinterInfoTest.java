package pt.beeverycreative.beesoft.drivers.usb;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jgrego
 */
public class PrinterInfoTest {

    private static final String FIRMWARE_VERSION = "10.5.10";

    public PrinterInfoTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of values method, of class PrinterInfo.
     */
    @Test
    public void testValues() {
        System.out.println("values");
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

    /**
     * Test of valueOf method, of class PrinterInfo.
     */
    @Test
    public void testValueOf() {
        final Map<String, PrinterInfo> printerMap = new HashMap<>();
        printerMap.put("BEETHEFIRST0", PrinterInfo.BEETHEFIRST0);
        printerMap.put("BEETHEFIRST", PrinterInfo.BEETHEFIRST);
        printerMap.put("BEETHEFIRST_PLUS", PrinterInfo.BEETHEFIRST_PLUS);
        printerMap.put("BEEME", PrinterInfo.BEEME);
        printerMap.put("BEEINSCHOOL", PrinterInfo.BEEINSCHOOL);
        printerMap.put("BEETHEFIRST_PLUS_A", PrinterInfo.BEETHEFIRST_PLUS_A);
        printerMap.put("BEEINSCHOOL_A", PrinterInfo.BEEINSCHOOL_A);
        printerMap.put("UNKNOWN", PrinterInfo.UNKNOWN);

        if (printerMap.size() != PrinterInfo.values().length) {
            fail("The printer map isn't of the same size as the PrinterInfo enum values");
        }

        for (final Entry<String, PrinterInfo> printer : printerMap.entrySet()) {
            final PrinterInfo expResult, result;

            expResult = printer.getValue();
            result = PrinterInfo.valueOf(printer.getKey());
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of filamentCode method, of class PrinterInfo.
     */
    @Test
    public void testFilamentCode() {
        final Map<PrinterInfo, String> printerMap = new HashMap<>();
        printerMap.put(PrinterInfo.BEETHEFIRST0, "BEETHEFIRST");
        printerMap.put(PrinterInfo.BEETHEFIRST, "BEETHEFIRST");
        printerMap.put(PrinterInfo.BEETHEFIRST_PLUS, "BEETHEFIRSTPLUS");
        printerMap.put(PrinterInfo.BEEME, "BEEME");
        printerMap.put(PrinterInfo.BEEINSCHOOL, "BEEINSCHOOL");
        printerMap.put(PrinterInfo.BEETHEFIRST_PLUS_A, "BEETHEFIRSTPLUS");
        printerMap.put(PrinterInfo.BEEINSCHOOL_A, "BEEINSCHOOL");
        printerMap.put(PrinterInfo.UNKNOWN, "UNKNOWN");

        if (printerMap.size() != PrinterInfo.values().length) {
            fail("The printer map isn't of the same size as the PrinterInfo enum values");
        }

        for (final Entry<PrinterInfo, String> printer : printerMap.entrySet()) {
            final String expResult, result;

            expResult = printer.getValue();
            result = printer.getKey().filamentCode();
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of presentationName method, of class PrinterInfo.
     */
    @Test
    public void testPresentationName() {
        final Map<PrinterInfo, String> printerMap = new HashMap<>();
        printerMap.put(PrinterInfo.BEETHEFIRST0, "BEETHEFIRST");
        printerMap.put(PrinterInfo.BEETHEFIRST, "BEETHEFIRST");
        printerMap.put(PrinterInfo.BEETHEFIRST_PLUS, "BEETHEFIRST+");
        printerMap.put(PrinterInfo.BEEME, "BEEME");
        printerMap.put(PrinterInfo.BEEINSCHOOL, "BEEINSCHOOL");
        printerMap.put(PrinterInfo.BEETHEFIRST_PLUS_A, "BEETHEFIRST+");
        printerMap.put(PrinterInfo.BEEINSCHOOL_A, "BEEINSCHOOL");
        printerMap.put(PrinterInfo.UNKNOWN, "UNKNOWN");

        if (printerMap.size() != PrinterInfo.values().length) {
            fail("The printer map isn't of the same size as the PrinterInfo enum values");
        }

        for (final Entry<PrinterInfo, String> printer : printerMap.entrySet()) {
            final String expResult, result;

            expResult = printer.getValue();
            result = printer.getKey().presentationName();
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of productID method, of class PrinterInfo.
     */
    @Test
    public void testProductID() {
        final Map<PrinterInfo, Short> printerMap = new HashMap<>();
        printerMap.put(PrinterInfo.BEETHEFIRST0, (short) 0x014e);
        printerMap.put(PrinterInfo.BEETHEFIRST, (short) 0x0001);
        printerMap.put(PrinterInfo.BEETHEFIRST_PLUS, (short) 0x0002);
        printerMap.put(PrinterInfo.BEEME, (short) 0x0003);
        printerMap.put(PrinterInfo.BEEINSCHOOL, (short) 0x0004);
        printerMap.put(PrinterInfo.BEETHEFIRST_PLUS_A, (short) 0x0005);
        printerMap.put(PrinterInfo.BEEINSCHOOL_A, (short) 0x0006);
        printerMap.put(PrinterInfo.UNKNOWN, (short) 0xffff);

        if (printerMap.size() != PrinterInfo.values().length) {
            fail("The printer map isn't of the same size as the PrinterInfo enum values");
        }

        for (final Entry<PrinterInfo, Short> printer : printerMap.entrySet()) {
            short expResult, result;

            expResult = printer.getValue();
            result = printer.getKey().productID();
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of vendorID method, of class PrinterInfo.
     */
    @Test
    public void testVendorID() {
        for (PrinterInfo printerInfo : PrinterInfo.values()) {
            final short expResult, result;

            if (printerInfo == PrinterInfo.BEETHEFIRST0 || printerInfo == PrinterInfo.UNKNOWN) {
                expResult = (short) 0xffff;
            } else {
                expResult = (short) 0x29c9;
            }

            result = printerInfo.vendorID();
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of firmwareFilename method, of class PrinterInfo.
     */
    // REDO! SHOULDN'T ACESS FILE SYSTEM
    @Test
    public void testFirmwareFilename() {
        /*
        final File firmwareFolder;
        final FilenameFilter fileFilter;
        final String[] filenames;

        fileFilter = (File dir, String name) -> name.endsWith(".BIN");
        firmwareFolder = new File("firmware");
        filenames = firmwareFolder.list(fileFilter);

        // exclude PrinterInfo.UNKNOWN
        if (filenames.length != PrinterInfo.values().length - 1) {
            fail("Inconsistent number of firmware files! " + filenames.length + " files");
        }

        for (PrinterInfo printerInfo : PrinterInfo.values()) {
            if (printerInfo != PrinterInfo.UNKNOWN) {
                assertTrue(ArrayUtils.contains(filenames, printerInfo.firmwareFilename()));
            }
        }
        */
    }

    /**
     * Test of bootloaderString method, of class PrinterInfo.
     */
    @Test
    public void testBootloaderString() {
        final Map<PrinterInfo, String> printerMap = new HashMap<>();
        printerMap.put(PrinterInfo.BEETHEFIRST0, "BEEVC-BEETHEFIRST0-" + FIRMWARE_VERSION);
        printerMap.put(PrinterInfo.BEETHEFIRST, "BEEVC-BEETHEFIRST-" + FIRMWARE_VERSION);
        printerMap.put(PrinterInfo.BEETHEFIRST_PLUS, "BEEVC-BEETHEFIRST_PLUS-" + FIRMWARE_VERSION);
        printerMap.put(PrinterInfo.BEEME, "BEEVC-BEEME-" + FIRMWARE_VERSION);
        printerMap.put(PrinterInfo.BEEINSCHOOL, "BEEVC-BEEINSCHOOL-" + FIRMWARE_VERSION);
        printerMap.put(PrinterInfo.BEETHEFIRST_PLUS_A, "BEEVC-BEETHEFIRST_PLUS_A-" + FIRMWARE_VERSION);
        printerMap.put(PrinterInfo.BEEINSCHOOL_A, "BEEVC-BEEINSCHOOL_A-" + FIRMWARE_VERSION);
        printerMap.put(PrinterInfo.UNKNOWN, "UNKNOWN-UNKNOWN-0.0.0");

        if (printerMap.size() != PrinterInfo.values().length) {
            fail("The printer map isn't of the same size as the PrinterInfo enum values");
        }

        for (final Entry<PrinterInfo, String> printer : printerMap.entrySet()) {
            final String expResult, result;

            expResult = printer.getValue();
            result = printer.getKey().bootloaderString();
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of iconFilename method, of class PrinterInfo.
     */
    // REDO! SHOULDN'T ACCESS FILE SYSTEM
    @Test
    public void testIconFilename() {
        /*
        final String[] imageFilenames;
        final File iconDir;

        iconDir = new File("app_resources/mainWindow");
        imageFilenames = iconDir.list();
        for (PrinterInfo printerInfo : PrinterInfo.values()) {
            if (printerInfo != PrinterInfo.UNKNOWN) {
                assertTrue(ArrayUtils.contains(imageFilenames, printerInfo.iconFilename()));
            }
        }
        */
    }

    /**
     * Test of versionString method, of class PrinterInfo.
     */
    @Test
    public void testVersionString() {
        for (PrinterInfo printerInfo : PrinterInfo.values()) {
            final String expResult, result;

            expResult = printerInfo.vendorID() + ":" + printerInfo.productID();
            result = printerInfo.versionString();
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of getDevice method, of class PrinterInfo.
     */
    @Test
    public void testGetDevice() {
        for (PrinterInfo printerInfo : PrinterInfo.values()) {
            final PrinterInfo result;

            result = PrinterInfo.getDevice(printerInfo.versionString());
            assertEquals(printerInfo, result);
        }
    }

    /**
     * Test of getDeviceByFormalName method, of class PrinterInfo.
     */
    @Test
    public void testGetDeviceByFormalName() {
        final Map<String, PrinterInfo> printerMap = new HashMap<>();
        printerMap.put("BEETHEFIRST", PrinterInfo.BEETHEFIRST0);
        printerMap.put("BEETHEFIRST+", PrinterInfo.BEETHEFIRST_PLUS);
        printerMap.put("BEEME", PrinterInfo.BEEME);
        printerMap.put("BEEINSCHOOL", PrinterInfo.BEEINSCHOOL);

        
        for(Entry<String, PrinterInfo> printer : printerMap.entrySet()) {
            final PrinterInfo expResult, result;
            
            expResult = printer.getValue();
            result = PrinterInfo.getDeviceByFormalName(printer.getKey());
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of filamentCodeToFormalString method, of class PrinterInfo.
     */
    @Test
    public void testFilamentCodeToFormalString() {
        for (PrinterInfo printerInfo : PrinterInfo.values()) {
            final String expResult, result;

            expResult = printerInfo.presentationName();
            result = PrinterInfo.filamentCodeToFormalString(printerInfo.filamentCode());
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of toString method, of class PrinterInfo.
     */
    @Test
    public void testToString() {
        for (PrinterInfo printerInfo : PrinterInfo.values()) {
            final String expResult, result;

            expResult = printerInfo.presentationName();
            result = printerInfo.toString();
            assertEquals(expResult, result);
        }
    }

}
