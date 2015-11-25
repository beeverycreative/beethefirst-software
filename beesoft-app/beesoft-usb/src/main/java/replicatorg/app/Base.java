/*
 Base.java

 Main class for the app.

 Part of the ReplicatorG project - http://www.replicat.org
 Copyright (c) 2008 Zach Smith

 Forked from Arduino: http://www.arduino.cc
 
 Based on Processing http://www.processing.org
 Copyright (c) 2004-05 Ben Fry and Casey Reas
 Copyright (c) 2001-04 Massachusetts Institute of Technology
 Copyright (c) 2013 BEEVC - Electronic Systems

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package replicatorg.app;

import ch.randelshofer.quaqua.QuaquaManager;
import com.apple.eawt.Application;
import com.apple.mrj.MRJApplicationUtils;
import com.apple.mrj.MRJOpenDocumentHandler;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import replicatorg.app.ui.MainWindow;
import replicatorg.app.ui.NotificationHandler;
import replicatorg.app.ui.WelcomeSplash;
import replicatorg.app.util.RSMonitor;
import replicatorg.machine.MachineLoader;
import replicatorg.util.ConfigProperties;

/**
 * Primary role of this class is for platform identification and general
 * interaction with the system (launching URLs, loading files and images, etc)
 * that comes from that.
 */
public class Base {

    private static final String newLine = System.getProperty("line.separator");

    /**
     * enum for fast/easy OS checking
     */
    public enum Platform {

        WINDOWS, MACOS9, MACOSX, LINUX, OTHER
    }

    /**
     * enum for fast/easy arch checking
     */
    public enum Arch {

        x86_64, x86, ARM, PPC, OTHER
    }
    /**
     * Full name of the Java version (i.e. 1.5.0_11). Prior to 0125, this was
     * only the first three digits.
     */
    public static final String javaVersionName = System.getProperty("java.version");
    /**
     * Version of Java that's in use, whether 1.1 or 1.3 or whatever, stored as
     * a float.
     * <P>
     * Note that because this is stored as a float, the values may not be
     * <EM>exactly</EM>
     * 1.3 or 1.4. Instead, make sure you're comparing against 1.3f or 1.4f,
     * which will have the same amount of error (i.e. 1.40000001). This could
     * just be a double, but since Processing only uses floats, it's safer for
     * this to be a float because there's no good way to specify a double with
     * the preproc.
     */
    public static final float javaVersion = Float.parseFloat(javaVersionName.substring(0, 3));
    /**
     * Current platform in use
     */
    static public Platform platform;
    static public Arch arch;
    /**
     * Current platform in use.
     * <P>
     * Equivalent to System.getProperty("os.name"), just used internally.
     */
    static public String platformName = System.getProperty("os.name");

    static {
        // figure out which operating system
        // this has to be first, since editor needs to know

        if (platformName.toLowerCase().contains("mac")) {
            // can only check this property if running on a mac
            // on a pc it throws a security exception and kills the applet
            // (but on the mac it does just fine)
            if (System.getProperty("mrj.version") != null) { // running on a
                // mac
                platform = (platformName.equals("Mac OS X")) ? Platform.MACOSX : Platform.MACOS9;
            } else {
                platform = Platform.MACOSX;
            }

        } else {
            String osname = System.getProperty("os.name");

            if (osname.contains("Windows")) {
                platform = Platform.WINDOWS;

            } else if (osname.equals("Linux")) { // true for the ibm vm
                platform = Platform.LINUX;

            } else {
                platform = Platform.OTHER;
            }
            String aString = System.getProperty("os.arch");
            COMPUTER_ARCHITECTURE = aString;
            if ("i386".equals(aString)) {
                arch = Arch.x86;
            } else if ("x86_64".equals(aString) || "amd64".equals(aString)) {
                arch = Arch.x86_64;
            } else if ("universal".equals(aString) || "ppc".equals(aString)) {
                arch = Arch.OTHER;
                throw new RuntimeException("Can not use use arch: '" + arch + "'");
            }
        }
    }

    public static final ConfigProperties configProperties = new ConfigProperties();
    public static boolean statusThreadDied = false;
    public static boolean errorOccured = false;
    public static boolean printPaused = false;
    public static boolean isPrinting = false;
    public static boolean welcomeSplashVisible = false;
    public static double originalColorRatio = 1;
    private static String COMPUTER_ARCHITECTURE;
    public static boolean gcodeToSave = false;
    public static boolean isPrintingFromGCode = false;
    public static boolean rebootingIntoFirmware = false;

    public enum InitialOpenBehavior {

        OPEN_LAST,
        OPEN_NEW,
        OPEN_SPECIFIC_FILE
    };

    public static int ID = 0;

    public static final String VERSION_BEESOFT = setVersionString();

    public static final String PROGRAM = "BEESOFT";
    public static String VERSION_BOOTLOADER;
    ;
    
    //public static final String VERSION_FIRMWARE_FINAL = configProperties.getAppProperty("firmware.current.version");
    public static String FIRMWARE_IN_USE;
    private final static String VERSION_JAVA = System.getProperty("java.version");
    public static String VERSION_MACHINE = "000000000000";
    public static String language = "en";
    public static String MACHINE_NAME = "BEETHEFIRST";
    public static final String GCODE_DELIMITER = "--";
    public static final String GCODE_TEMP_FILENAME = "temp.gcode";
    public static final String MODELS_FOLDER = "3DModels";
    public static final String GCODE2PRINTER_PATH = Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/gcodeToPrinter.gcode";

    /**
     * The textual representation of this version (4 digits, zero padded).
     */
    //public static final String VERSION_NAME = String.format("%deta",VERSION);
    public static final String VERSION_NAME = VERSION_BEESOFT;
    public static boolean THREAD_KEEP_ALIVE = false;
    /**
     * The machine controller in use.
     */
    private static MachineLoader machineLoader;
    /**
     * The general-purpose logging object.
     */
    public static final Logger logger = Logger.getLogger("replicatorg.log");
    public static FileHandler logFileHandler = null;
    public static String logFilePath = null;

    /**
     * Properties file
     */
    private static Properties propertiesFile = null;
    /* Date time instance variables */
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public static ArrayList<Thread> systemThreads;

    private static final File BEELOGfile = new File(getAppDataDirectory().toString() + "/BEELOG.txt");
    private static final File comLogFile = new File(getAppDataDirectory().toString() + "/comLog.txt");
    private static final BufferedWriter logBW = initLog(BEELOGfile);
    private static final BufferedWriter comLogBW = initLog(comLogFile);

    /**
     * Path of filename opened on the command line, or via the MRJ open document
     * handler.
     */
    static public String openedAtStartup;
    /**
     * This is the name of the alternate preferences set that this instance of
     * SimpleG uses. If null, this instance will use the default preferences
     * set.
     */
    private static final String alternatePrefs = null;

    /**
     * Get the preferences node for SimpleG.
     */
    static Preferences getUserPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(Base.class);
        if (alternatePrefs != null) {
            prefs = prefs.node("alternate/" + alternatePrefs);
        }
        return prefs;
    }

    /**
     * Get the the user preferences and profiles directory. By default this is
     * ~/.replicatorg; if an alternate preferences set is selected, it will
     * instead be ~/.replicatorg/alternatePrefs/<i>alternate_prefs_name</i>.
     *
     * @return
     */
    static public File getUserDirectory() {
        String path = System.getProperty("user.home") + File.separator + ".replicatorg";
        if (alternatePrefs != null) {
            path = path + File.separator + alternatePrefs;
        }
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
            if (!dir.exists()) { // we failed to create our user dir. Log the failure, try to continue
                Base.logger.log(Level.SEVERE, "We could not create a user directory at: {0}", path);
                return null;
            }
        }

        return dir;
    }

    public static int getModelID() {
        ID++;
        return ID;
    }

    private static BufferedWriter initLog(String fileName) {
        BufferedWriter bw;
        OutputStreamWriter osw;
        FileOutputStream fos;
        File file;

        try {
            file = new File(getAppDataDirectory().toString() + "/" + fileName);

            if (file.exists()) {
                file.delete();
            }

            fos = new FileOutputStream(file);
            osw = new OutputStreamWriter(fos, "UTF-8");
            bw = new BufferedWriter(osw);

            return bw;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private static BufferedWriter initLog(File file) {
        BufferedWriter bw;
        OutputStreamWriter osw;
        FileOutputStream fos;

        try {
            if (file.exists()) {
                file.delete();
            }

            fos = new FileOutputStream(file);
            osw = new OutputStreamWriter(fos, "UTF-8");
            bw = new BufferedWriter(osw);

            return bw;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static void closeLogs() {
        try {
            logBW.close();
            comLogBW.close();
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Retrieves the application data directory via OS specific voodoo. Defaults
     * to the current directory if no os specific settings exist,
     *
     * @return File object pointing to the OS specific ApplicationsDirectory
     */
    static public File getApplicationDirectory() {
        if (isMacOS()) {
            try {
                File x = new File(".");
                String baseDir = x.getCanonicalPath();
                //baseDir = baseDir + "/ReplicatorG.app/Contents/Resources";
                //Base.logger.severe("OSX AppDir at " + baseDir );
                //we want to use ReplicatorG.app/Content as our app dir.
                if (new File(baseDir + "/BEESOFT.app/Contents/Resources").exists()) {
                    return new File(baseDir + "/BEESOFT.app/Contents/Resources");
                } else {
                    Base.logger.log(Level.SEVERE, "{0}/BEESOFT.app not found, using {1}/replicatorg/", new Object[]{baseDir, baseDir});
                }
                return new File(baseDir + "/replicatorg");
            } catch (java.io.IOException e) {
                // This space intentionally left blank. Fall through.
            }
        }
        return new File(System.getProperty("user.dir"));
    }

    static public File getAppDataDirectory() {
        File f = new File(System.getProperty("user.home").concat("/BEESOFT"));
        File models = new File(System.getProperty("user.home").concat("/BEESOFT/" + Base.MODELS_FOLDER));

        if (!f.exists()) {
            // create BEESOFT dir
            f.mkdir();
        } else if (!models.exists()) {
            // create models dir inside BEESOFT
            models.mkdir();
        }

        return f;
    }

    private static byte[] readAllBytes(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    private static Map<String, String> getGalleryMap(File[] models) {
        Map<String, String> mod = new HashMap<String, String>();

        for (File model : models) {
            mod.put(model.getName(), model.getAbsolutePath());
        }
        return mod;
    }

    public static void copy3DFiles() {
        InputStream inStream;
        OutputStream outStream;

        File[] models = new File(Base.getApplicationDirectory() + "/" + Base.MODELS_FOLDER).listFiles();
        Map<String, String> galleryModels = getGalleryMap(new File(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER).listFiles());

        for (File model : models) {
            try {
                File afile = new File(model.getAbsolutePath());
                File bfile = new File(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/" + model.getName());
                if (!galleryModels.containsKey(afile.getName())) {

                    inStream = new FileInputStream(afile);
                    outStream = new FileOutputStream(bfile);

                    byte[] buffer = new byte[1024];

                    int length;
                    //copy the file content in bytes 
                    while ((length = inStream.read(buffer)) > 0) {

                        outStream.write(buffer, 0, length);
                    }

                    inStream.close();
                    outStream.close();
                }
            } catch (IOException e) {
                Base.writeLog(e.getMessage(), Base.class);
            }
        }

    }

    public static void updateVersions() {
        VERSION_BOOTLOADER = editor.getMachineInterface().getDriver().getBootloaderVersion();
        FIRMWARE_IN_USE = editor.getMachineInterface().getDriver().getFirmwareVersion();
        VERSION_MACHINE = editor.getMachineInterface().getDriver().getSerialNumber();

        //buildLogFile(true);
        writePrinterInfo();
    }

    private static void writePrinterInfo() {
        try {
            logBW.newLine();
            logBW.newLine();
            logBW.write("*************** CONNECTED PRINTER ****************" + newLine);
            logBW.write("Bootloader version: " + VERSION_BOOTLOADER + newLine);
            logBW.write("Firmware version: " + FIRMWARE_IN_USE + newLine);
            logBW.write("**************************************************" + newLine);
            logBW.flush();
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void writeLogHeader() {
        try {
            logBW.newLine();
            logBW.write("**************************************************" + newLine);
            logBW.write(PROGRAM + " " + VERSION_BEESOFT + newLine);
            logBW.write("Java version: " + VERSION_JAVA + newLine);
            logBW.write("Architecture: " + COMPUTER_ARCHITECTURE + newLine);
            logBW.write("Machine name: BEETHEFIRST" + newLine);
            logBW.write("Company name: BEEVERYCREATIVE" + newLine);
            logBW.write("**************************************************" + newLine);
            logBW.flush();
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void writeLog(String message) {

        /**
         * *** Date and Time procedure ****
         */
        Calendar calendar = Calendar.getInstance();
        String date = dateFormat.format(calendar.getTime());

        try {
            logBW.newLine();
            logBW.write("[" + date + "]" + " " + message);
            logBW.flush();
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void writeLog(String message, Class logClass) {

        if (BEELOGfile.length() > 10000000) {
            return;
        }

        if (logClass == null) {
            writeLog(message);
            return;
        }

        /**
         * *** Date and Time procedure ****
         */
        Calendar calendar = Calendar.getInstance();
        String date = dateFormat.format(calendar.getTime());

        try {
            logBW.newLine();
            logBW.write("[" + date + "]" + " (" + logClass.getSimpleName() + ") " + message);
            logBW.flush();
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /*
     public static void writeStatistics(String message) {

     Calendar cal = Calendar.getInstance();
     String date = dateFormat.format(cal.getTime());

     FileWriter fw = null;
     try {
     fw = new FileWriter(statistics.getAbsoluteFile(), true);
     } catch (IOException ex) {
     Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
     }
     BufferedWriter bw = new BufferedWriter(fw);
     try {
     bw.write(date);
     bw.newLine();
     bw.write("-------------------------------\n");
     bw.write(message);
     bw.flush();
     bw.newLine();
     bw.close();
     } catch (IOException ex) {
     Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
     }
     try {
     if (fw != null) {
     fw.close();
     }
     } catch (IOException ex) {
     Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
     }
     }
     */
    public static void writeComLog(long timeStamp, String message) {

        if (comLogFile.length() > 10000000) {
            return;
        }

        try {
            if (!message.equals("\n")) {
                comLogBW.write("Timestamp: " + timeStamp + " | " + message + newLine);
                comLogBW.flush();
            } else {
                comLogBW.newLine();
                comLogBW.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static Properties openFileProperties() {

        String filePath = getAppDataDirectory().toString().concat("/config.properties");
        File f = new File(filePath);
        Properties props = new Properties();
        BufferedReader fis = null;
        if (f.exists()) {

            /**
             * FileInputStream for Properties usage
             */
            try {
                fis = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));

                props.load(fis);
            } catch (IOException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);

            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return props;
    }

    /**
     * Write properties to the config file
     */
    public static void writeConfig() {

        String filePath = getAppDataDirectory().toString().concat("/config.properties");

        try {
            propertiesFile.store(new OutputStreamWriter(
                    new FileOutputStream(filePath), "UTF-8"), null);

        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void storeProperty(String key, String value) {
        propertiesFile.setProperty(key, value);
    }

    public static void removeProperty(String key) {
        propertiesFile.remove(key);
    }

    /**
     * Reads a property from the config file
     *
     * @param param atribute to be loaded from configs
     * @return
     */
    public static String readConfig(String param) {

        if (propertiesFile == null) {
            return null;
        }

        return propertiesFile.getProperty(param);

    }

    public static void loadProperties() {

        String filePath = getAppDataDirectory().toString().concat("/config.properties");
        File f = new File(filePath);
        BufferedReader fis = null;
        if (f.exists()) {

            /**
             * FileInputStream for Properties usage
             */
            try {
                fis = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));

                propertiesFile.load(fis);
            } catch (IOException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);

            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    static public File getApplicationFile(String path) {
        return new File(getApplicationDirectory(), path);
    }

    static public File getUserFile(String path) {
        return getUserFile(path, true);
    }

    static public File getUserDir(String path) {
        return getUserDir(path, true);
    }

    /**
     * Clear all STL and GCODE files to avoid user access
     *
     * @param dirPath Path to directory containing such files
     */
    static public void cleanDirectoryTempFiles(String dirPath) {
        boolean isBeesoftAlpha = Base.VERSION_BEESOFT.contains("alpha");

        File dir = new File(dirPath);
        for (File file : dir.listFiles()) {
            if (file.getName().contains(".stl") || (file.getName().contains(".gcode") && file.getName().contains("gcodeToPrinter") == false)) {
                file.delete();
            }
        }
    }

    public static void turnOnPowerSaving(boolean turnOn) {
        /*
         if (turnOn) {
         //            editor.getMachine().getDriver().dispatchCommand("M641 A1");
         editor.getMachine().runCommand(new replicatorg.drivers.commands.DispatchCommand("M641 A1", UsbPassthroughDriver.COM.NO_RESPONSE));
         } else {
         //            editor.getMachine().getDriver().dispatchCommand("M641 A0");+
         editor.getMachine().runCommand(new replicatorg.drivers.commands.DispatchCommand("M641 A0", UsbPassthroughDriver.COM.NO_RESPONSE));
         }
         */
    }

    static public void disposeAllOpenWindows() {
        String name;

        java.awt.Window win[] = java.awt.Window.getWindows();
        for (Window win1 : win) {
            name = win1.getName();
            if (!name.equals("mainWindow") && !name.equals("FeedbackDialog")) {
                win1.dispose();
            }
//            System.out.println(win[i].getName());
        }
        editor.setEnabled(true);
        editor.getButtons().resetVariables();
        THREAD_KEEP_ALIVE = false;

        for (Thread systemThread : systemThreads) {
            systemThread.stop();
        }

    }

    static public void enableAllOpenWindows() {
        java.awt.Window win[] = java.awt.Window.getWindows();
        for (Window win1 : win) {
            if (win1.getName().equals("mainWindow")) {
                //|| win[i].getName().equals("Autonomous")
                if (printPaused) {
                    win1.setEnabled(false);
                } else {
                    win1.setEnabled(true);
                }
            } else {
                win1.setEnabled(true);
            }
//            }
//            System.out.println(win[i].getName());
        }

    }

    static public void bringAllWindowsToFront() {
        bringMainWindowOK();
        java.awt.Window win[] = java.awt.Window.getOwnerlessWindows();
        for (Window win1 : win) {
            win1.toFront();
            win1.requestFocusInWindow();
        }
    }

    static public void bringMainWindowOK() {
        Base.getMainWindow().setFocusable(true);
        Base.getMainWindow().setFocusableWindowState(true);
    }

    static public void setMainWindowNOK() {
        Base.getMainWindow().setFocusable(false);
        Base.getMainWindow().setFocusableWindowState(false);
    }

    /**
     * Checks if string is a numeric number
     *
     * @param str
     * @return boolean <li> true, if it is numeric
     * <li> false, if not
     */
    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
        //match a number with optional '-' and decimal '.'
        // not working with arabic digits
    }
    /**
     * Singleton NumberFormat used for parsing and displaying numbers to GUI in
     * the localized format. Use for all non-GCode, numbers output and input.
     */
    private static final NumberFormat localNF = NumberFormat.getInstance();

    static public NumberFormat getLocalFormat() {
        return localNF;
    }
    /**
     * Singleton Gcode NumberFormat: Unsed for writing the correct precision
     * strings when generating gcode (minimum one decimal places) using . as
     * decimal separator
     */
    private static final NumberFormat gcodeNF;

    static {
        // We don't use DFS.getInstance here to maintain compatibility with Java 5
        DecimalFormatSymbols dfs;
        gcodeNF = new DecimalFormat("##0.0##");
        dfs = ((DecimalFormat) gcodeNF).getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
    }

    static public NumberFormat getGcodeFormat() {
        return gcodeNF;
    }

    /**
     *
     * @param path The relative path to the file in the .replicatorG directory
     * @param autoCopy If true, copy over the file of the same name in the
     * application directory if none is found in the prefs directory.
     * @return
     */
    static public File getUserFile(String path, boolean autoCopy) {
        if (path.contains("..")) {
            Base.logger.log(Level.INFO, "Attempted to access parent directory in {0}, skipping", path);
            return null;
        }
        // First look in the user's local .replicatorG directory for the path.
        File f = new File(getUserDirectory(), path);
        // Make the parent file if not already there
        File dir = f.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (autoCopy && !f.exists()) {
            // Check if there's an application-level version
            File original = getApplicationFile(path);
            // If so, copy it over
            if (original.exists()) {
                try {
                    Base.copyFile(original, f);
                } catch (IOException ioe) {
                    Base.logger.log(Level.SEVERE, "Couldn't copy " + path + " to your local .replicatorG directory", f);
                }
            }
        }
        return f;
    }

    static public File getUserDir(String path, boolean autoCopy) {
        if (path.contains("..")) {
            Base.logger.log(Level.INFO, "Attempted to access parent directory in {0}, skipping", path);
            return null;
        }
        // First look in the user's local .replicatorG directory for the path.
        File f = new File(getUserDirectory(), path);
        // Make the parent file if not already there
        File dir = f.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (autoCopy && !f.exists()) {
            // Check if there's an application-level version
            File original = getApplicationFile(path);
            // If so, copy it over
            if (original.exists()) {
                try {
                    Base.copyDir(original, f);
                } catch (IOException ioe) {
                    Base.logger.log(Level.SEVERE, "Couldn't copy " + path + " to your local .replicatorG directory", f);
                }
            }
        }
        return f;
    }

    static public Font getFontPref(String name, String defaultValue) {
        String s = defaultValue;
        //preferences.get(name, defaultValue);
        StringTokenizer st = new StringTokenizer(s, ",");
        String fontname = st.nextToken();
        String fontstyle = st.nextToken();
        return new Font(fontname,
                ((fontstyle.contains("bold")) ? Font.BOLD : 1)
                | ((fontstyle.contains("italic")) ? Font.ITALIC
                        : 0), Integer.parseInt(st.nextToken()));
    }

    static public Color getColorPref(String name, String defaultValue) {
        String s = defaultValue;
        //preferences.get(name, defaultValue);
        Color parsed = null;
        if ((s != null) && (s.indexOf("#") == 0)) {
            try {
                int v = Integer.parseInt(s.substring(1), 16);
                parsed = new Color(v);
            } catch (Exception e) {
            }
        }
        return parsed;
    }
    /**
     * The main UI window.
     */
    static MainWindow editor = null;

    public static MainWindow getEditor() {
        return editor;
    }
    private static final NotificationHandler notificationHandler = null;
    private static final String[] supportedExtensions = {
        "stl", "bee"
    };

    /**
     * Return the extension of a path, converted to lowercase.
     *
     * @param path The path to check.
     * @return The extension suffix, sans ".".
     */
    public static String getExtension(String path) {
        String[] split = path.split("\\.");
        return split[split.length - 1];
    }

    public static boolean supportedExtension(String path) {
        String suffix = getExtension(path);
        for (final String s : supportedExtensions) {
            if (s.equals(suffix)) {
                return true;
            }
        }
        return false;
    }

    static public void main(String args[]) {

        if (Base.isMacOS()) {
            // Default to sun's XML parser, PLEASE.  Some apps are installing some janky-ass xerces.
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                    "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name",
                    "BEESOFT");
        }

        boolean cleanPrefs = false;

        // parse command line input
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--alternate-prefs")) {
                if ((i + 1) < args.length) {
                    i++;
                    //setAlternatePrefs(args[i]);
                }
            } else if (args[i].equals("--clean-prefs")) {
                cleanPrefs = true;
            } else if (args[i].equals("--debug")) {
                // Allow for [--debug] [DEBUGLEVEL]
                int debugLevelArg = 2;
                if ((i + 1) < args.length) {
                    try {
                        debugLevelArg = Integer.parseInt(args[i + 1]);
                        i++;
                    } catch (NumberFormatException e) {
                    }
                }
                if (debugLevelArg == 0) {
                    logger.setLevel(Level.INFO);
                    logger.info("Debug level is 'INFO'");
                } else if (debugLevelArg == 1) {
                    logger.setLevel(Level.FINE);
                    logger.info("Debug level is 'FINE'");
                } else if (debugLevelArg == 2) {
                    logger.setLevel(Level.FINER);
                    logger.info("Debug level is 'FINER'");
                } else if (debugLevelArg == 3) {
                    logger.setLevel(Level.FINEST);
                    logger.info("Debug level is 'FINEST'");
                } else if (debugLevelArg >= 4) {
                    logger.setLevel(Level.ALL);
                    logger.info("Debug level is 'ALL'");
                }
            } else if (args[i].startsWith("-")) {
                System.out.println("Usage: ./replicatorg [--debug DEBUGLEVEL] [--alternate-prefs ALTERNATE_PREFS_NAME] [--clean-prefs] [filename.stl]");
                System.exit(1);
            } else if (supportedExtension(args[i])) {
                // grab any opened file from the command line
                Base.openedAtStartup = args[i];
            }
        }

        // Use the default system proxy settings
        System.setProperty("java.net.useSystemProxies", "true");
        // Use antialiasing implicitly
        // System.setProperty("j3d.implicitAntialiasing", "true");

        // MAC OS X ONLY:
        // register a temporary/early version of the mrj open document handler,
        // because the event may be lost (sometimes, not always) by the time
        // that MainWindow is properly constructed.
        MRJOpenDocumentHandler startupOpen = new MRJOpenDocumentHandler() {
            @Override
            public void handleOpenFile(File file) {
                // this will only get set once.. later will be handled
                // by the MainWindow version of this fella
                if (Base.openedAtStartup == null) {
                    Base.openedAtStartup = file.getAbsolutePath();
                }
            }
        };
        MRJApplicationUtils.registerOpenDocumentHandler(startupOpen);
        
    
        //new Base();
       // Base.rsm = new RSMonitor();
       // rsm.start();
        
        Base.rsm = new RSMonitor();
        rsm.start();
        // Create the new application "Base" class.
        new Base();
    }

    static RSMonitor rsm;
    static public RSMonitor getRSMonitor (){
        return Base.rsm;
    } 
    
    private File openStatsFile() {
        /**
         * Put log file near app folder *
         */
        String path = getAppDataDirectory().toString();
        File f = new File(path + "/Statistics.txt");

        return f;
    }

    public static String getDefaultCharEncoding() {
        byte[] bArray = {'w'};
        InputStream is = new ByteArrayInputStream(bArray);
        InputStreamReader reader = new InputStreamReader(is);
        String defaultCharacterEncoding = reader.getEncoding();
        return defaultCharacterEncoding;
    }

    /**
     *
     */
    public Base() {
        // Log autonomous statistics file
        // statistics = openStatsFile();
        
        writeLogHeader();

        // Properties file init
        propertiesFile = openFileProperties();

        // Loads properties at the beginning
        loadProperties();

        // Loads language
        language = ProperDefault.get("language").toLowerCase();

        systemThreads = new ArrayList<Thread>();

        /*
         try {
         System.setErr(new PrintStream(new FileOutputStream(getAppDataDirectory().toString() + "/err.txt")));
         } catch (FileNotFoundException ex) {
         Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
         }
         */
        // set the look and feel before opening the window
        try {
            if (Base.isMacOS()) {
                // Only override the IU's necessary for ColorChooser and
                // FileChooser:
                Set<Object> includes = new HashSet<Object>();
                includes.add("ColorChooser");
                includes.add("FileChooser");
                includes.add("Component");
                includes.add("Browser");
                includes.add("Tree");
                includes.add("SplitPane");
                QuaquaManager.setIncludedUIs(includes);

                System.setProperty("apple.laf.useScreenMenuBar", "true");

                // create an instance of the Mac Application class, so i can handle the 
                // mac quit event with the Mac ApplicationAdapter
                Application macApplication = Application.getApplication();
                MacOSHandler macAdapter = new MacOSHandler(this);
                macApplication.addApplicationListener(macAdapter);

                // need to enable the preferences option manually
//                macApplication.setEnabledPreferencesMenu(true);
                writeLog("Operating System: Mac OS", this.getClass());

            } else if (Base.isLinux()) {
                writeLog("Operating System: Linux", this.getClass());
            } else {
                writeLog("Operating System: Windows", this.getClass());
            }
        } catch (Exception e) {
            writeLog(e.getMessage(), this.getClass());
        }

        // use native popups so they don't look so crappy on osx
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ProperDefault.put("machine.name", MACHINE_NAME);
                String machineName = ProperDefault.get("machine.name");
                // build the editor object
                editor = new MainWindow();
                writeLog("Main Window initialized", this.getClass());
//                notificationHandler = NotificationHandler.Factory.getHandler(editor, Boolean.valueOf(ProperDefault.get("ui.preferSystemTrayNotifications")));
                editor.restorePreferences();
                writeLog("Preferences restored", this.getClass());
                // add shutdown hook to store preferences
                Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {
                    final private MainWindow w = editor;

                    @Override
                    public void run() {
                        w.onShutdown();
                    }
                });
//                Languager.printXML();
                editor.loadMachine(machineName, false);
                writeLog("Machine Loaded", this.getClass());
                // show the window
                editor.setVisible(false);
                WelcomeSplash splash = new WelcomeSplash(editor);
                splash.setVisible(true);

//                buildLogFile(false);
                writeLog("Log file created", this.getClass());
            }
        });
        ProperDefault.put("machine.name", MACHINE_NAME);
        String machineName = ProperDefault.get("machine.name");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }

        // quite ugly, but it works for now
        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (Base.statusThreadDied) {
                editor.reloadMachine(machineName, false);
            }
        }

    }

    // .................................................................
    /**
     * returns true if the ReplicatorG is running on a Mac OS machine,
     * specifically a Mac OS X machine because it doesn't run on OS 9 anymore.
     *
     * @return
     */
    static public boolean isMacOS() {
        return platform == Platform.MACOSX;
    }

    /**
     * returns true if running on windows.
     *
     * @return
     */
    static public boolean isWindows() {
        return platform == Platform.WINDOWS;
    }

    /**
     * true if running on linux.
     *
     * @return
     */
    static public boolean isLinux() {
        return platform == Platform.LINUX;
    }

    static public boolean isx86_64() {
        return arch == Arch.x86_64;
    }

    static public boolean isx86() {
        return arch == Arch.x86;
    }

    /**
     * Registers key events for a Ctrl-W and ESC with an ActionListener that
     * will take care of disposing the window.
     *
     * @param root
     * @param disposer
     */
    static public void registerWindowCloseKeys(JRootPane root, // Window
            // window,
            ActionListener disposer) {

        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        root.registerKeyboardAction(disposer, stroke,
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        stroke = KeyStroke.getKeyStroke('W', modifiers);
        root.registerKeyboardAction(disposer, stroke,
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /**
     * Show an error message that's actually fatal to the program. This is an
     * error that can't be recovered. Use showWarning() for errors that allow
     * ReplicatorG to continue running.
     *
     * @param title
     * @param message
     * @param e
     */
    static public void quitWithError(String title, String message, Throwable e) {

        notificationHandler.showError(title, message, e);

        if (e != null) {
            Base.writeLog(e.getMessage(), Base.class);
        }
        System.exit(1);
    }

    static public String getContents(String what) {
        File appBase = Base.getApplicationDirectory();
        return appBase.getAbsolutePath() + File.separator + what;
    }

    static public String getLibContents(String what) {
        /*
         * On MacOSX, the replicatorg.app-resources property points to the
         * resources directory inside the app bundle. On other platforms it's
         * not set.
         */
        String appResources = System.getProperty("replicatorg.app-resources");
        if (appResources != null) {
            return appResources + File.separator + what;
        } else {
            return getContents("lib" + File.separator + what);
        }
    }

    /**
     * We need to load animated .gifs through this mechanism vs. getImage due to
     * a number of bugs in Java's image loading routines.
     *
     * @param name The path of the image
     * @param who The component that will use the image
     * @return the loaded image object
     */
    static public Image getDirectImage(String name, Component who) {
        Image image = null;

        // try to get the URL as a system resource
        URL url = ClassLoader.getSystemResource(name);
        try {
            image = Toolkit.getDefaultToolkit().createImage(url);
            MediaTracker tracker = new MediaTracker(who);
            tracker.addImage(image, 0);
            tracker.waitForAll();
        } catch (InterruptedException e) {
        }
        return image;
    }

    static public BufferedImage getImage(String name, Component who) {
        BufferedImage image = null;

        // try to get the URL as a system resource
        URL url = ClassLoader.getSystemResource(name);
        try {
            image = ImageIO.read(url);
            MediaTracker tracker = new MediaTracker(who);
            tracker.addImage(image, 0);
            tracker.waitForAll();
            BufferedImage img2 = new BufferedImage(image.getWidth(null),
                    image.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB);
            img2.getGraphics().drawImage(image, 0, 0, null);
            image = img2;
        } catch (InterruptedException e) {
            Base.logger.log(Level.FINE, "Could not load image: " + name, e);
        } catch (IOException ioe) {
            Base.logger.log(Level.FINE, "Could not load image: " + name, ioe);
        } catch (IllegalArgumentException iae) {
            Base.logger.log(Level.FINE, "Could not load image: " + name, iae);
        }
        return image;
    }

    static public InputStream getStream(String filename) throws IOException {
        return new FileInputStream(getLibContents(filename));
    }

    // ...................................................................
    static public void copyFile(File afile, File bfile) throws IOException {
        InputStream from = new BufferedInputStream(new FileInputStream(afile));
        OutputStream to = new BufferedOutputStream(new FileOutputStream(bfile));
        byte[] buffer = new byte[16 * 1024];
        int bytesRead;
        while ((bytesRead = from.read(buffer)) != -1) {
            to.write(buffer, 0, bytesRead);
        }
        to.flush();
        from.close(); // ??
        to.close(); // ??

        bfile.setLastModified(afile.lastModified()); // jdk13+ required
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
    }

    /**
     * Grab the contents of a file as a string.
     *
     * @param file
     * @return
     * @throws java.io.IOException
     */
    static public String loadFile(File file) throws IOException {
        Base.logger.log(Level.INFO, "Load file : {0}", file.getAbsolutePath());
        // empty code file.. no worries, might be getting filled up later
        if (file.length() == 0) {
            return "";
        }

        InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
        BufferedReader reader = new BufferedReader(isr);

        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
            buffer.append('\n');
        }
        reader.close();
        return buffer.toString();
    }

    /**
     * Spew the contents of a String object out to a fil
     *
     * @param str
     * @param file
     * @throws java.io.IOException
     */
    static public void saveFile(String str, File file) throws IOException {
        Base.logger.log(Level.INFO, "Saving as {0}", file.getCanonicalPath());

        ByteArrayInputStream bis = new ByteArrayInputStream(str.getBytes());
        InputStreamReader isr = new InputStreamReader(bis);
        BufferedReader reader = new BufferedReader(isr);

        FileWriter fw = new FileWriter(file);
        PrintWriter wrtr = new PrintWriter(new BufferedWriter(fw));

        String line;
        while ((line = reader.readLine()) != null) {
            wrtr.println(line);
        }
        wrtr.flush();
        wrtr.close();
    }

    static public void copyDir(File sourceDir, File targetDir)
            throws IOException {
        targetDir.mkdirs();
        String files[] = sourceDir.list();
        for (String file : files) {
            if (file.equals(".") || file.equals("..")) {
                continue;
            }
            File source = new File(sourceDir, file);
            File target = new File(targetDir, file);
            if (source.isDirectory()) {
                // target.mkdirs();
                copyDir(source, target);
                target.setLastModified(source.lastModified());
            } else {
                copyFile(source, target);
            }
        }
    }

    /**
     * Gets a list of all files within the specified folder, and returns a list
     * of their relative paths. Ignores any files/folders prefixed with a dot.
     *
     * @param path
     * @param relative
     * @return
     */
    static public String[] listFiles(String path, boolean relative) {
        return listFiles(new File(path), relative);
    }

    static public String[] listFiles(File folder, boolean relative) {
        String path = folder.getAbsolutePath();
        Vector<String> vector = new Vector<String>();
        addToFileList(relative ? (path + File.separator) : "", path, vector);
        String outgoing[] = new String[vector.size()];
        vector.copyInto(outgoing);
        return outgoing;
    }

    static protected void addToFileList(String basePath, String path,
            Vector<String> fileList) {
        File folder = new File(path);
        String list[] = folder.list();
        if (list == null) {
            return;
        }

        for (String list1 : list) {
            if (list1.charAt(0) == '.') {
                continue;
            }
            File file = new File(path, list1);
            String newPath = file.getAbsolutePath();
            if (newPath.startsWith(basePath)) {
                newPath = newPath.substring(basePath.length());
            }
            fileList.add(newPath);
            if (file.isDirectory()) {
                addToFileList(basePath, newPath, fileList);
            }
        }
    }

    /**
     * Get a reference to the currently selected machine
     *
     *
     * @return
     */
    static public MachineLoader getMachineLoader() {
        if (machineLoader == null) {
            machineLoader = new MachineLoader();
        }
        return machineLoader;
    }

    static public MainWindow getMainWindow() {
        return editor;
    }
    static boolean printEnded = false;

    static public void setPrintEnded(boolean val) {
        printEnded = val;
    }

    static public boolean getPrintEnded() {
        return printEnded;
    }

    public static void resetPrintingFlags() {
        isPrinting = false;
        printPaused = false;
        isPrintingFromGCode = false;
        gcodeToSave = false;
    }

    private static String setVersionString() {
        String releaseType, applicationVersion, buildNumber;

        releaseType = configProperties.getBuildProperty("release.type");
        applicationVersion = configProperties.getBuildProperty("application.version");
        buildNumber = configProperties.getBuildProperty("build.number");

        if (releaseType.equals("alpha")) {
            return applicationVersion + "-" + releaseType + "-" + buildNumber;
        } else if (releaseType.contains("beta")) {
            return applicationVersion + "-" + releaseType;
        } else {
            return applicationVersion;
        }
    }
    
    public static void hiccup(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Base.writeLog("Interrupted during hiccup", Base.class);
        }
    }
}
