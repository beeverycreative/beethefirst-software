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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.zip.CRC32;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import replicatorg.app.ui.MainWindow;
import replicatorg.app.ui.NotificationHandler;
import replicatorg.app.ui.WelcomeSplash;
import replicatorg.machine.MachineLoader;
import replicatorg.util.ConfigProperties;

/**
 * Primary role of this class is for platform identification and general
 * interaction with the system (launching URLs, loading files and images, etc)
 * that comes from that.
 */
public class Base {

    public static final Image BEESOFT_ICON = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("images/icon.png"));
    private static final String NEW_LINE = System.getProperty("line.separator");

    /**
     * enum for fast/easy OS checking
     */
    public enum Platform {

        WINDOWS, MACOS9, MACOSX, LINUX, OTHER
    }

    /**
     * Current platform in use
     */
    /**
     * Current platform in use.
     * <P>
     * Equivalent to System.getProperty("os.name"), just used internally.
     */
    private static final Platform PLATFORM;

    static {
        final String PLATFORM_NAME = System.getProperty("os.name");

        // figure out which operating system
        // this has to be first, since editor needs to know
        if (PLATFORM_NAME.toLowerCase().contains("mac")) {
            // can only check this property if running on a mac
            // on a pc it throws a security exception and kills the applet
            // (but on the mac it does just fine)
            if (System.getProperty("mrj.version") != null) { // running on a
                // mac
                PLATFORM = (PLATFORM_NAME.equals("Mac OS X")) ? Platform.MACOSX : Platform.MACOS9;
            } else {
                PLATFORM = Platform.MACOSX;
            }
        } else if (PLATFORM_NAME.contains("Windows")) {
            PLATFORM = Platform.WINDOWS;
        } else if (PLATFORM_NAME.equals("Linux")) { // true for the ibm vm
            PLATFORM = Platform.LINUX;
        } else {
            PLATFORM = Platform.OTHER;
        }
    }

    public static final ConfigProperties CONFIG_PROPERTIES = new ConfigProperties();
    public static boolean printPaused = false;
    public static boolean isPrinting = false;
    private static boolean welcomeSplashVisible = true;
    public static final Object WELCOME_SPLASH_MONITOR = new Object();
    public static boolean isPrintingFromGCode = false;
    public static boolean keepFeedbackOpen = false;

    public enum InitialOpenBehavior {

        OPEN_LAST,
        OPEN_NEW,
        OPEN_SPECIFIC_FILE
    };

    public static int ID = 0;

    public static final String VERSION_BEESOFT;

    static {
        final String releaseType, applicationVersion, buildNumber;

        releaseType = CONFIG_PROPERTIES.getBuildProperty("release.type");
        applicationVersion = CONFIG_PROPERTIES.getBuildProperty("application.version");
        buildNumber = CONFIG_PROPERTIES.getBuildProperty("build.number");

        if (releaseType.contains("alpha")) {
            VERSION_BEESOFT = applicationVersion + "-" + releaseType + "-" + buildNumber;
        } else if (releaseType.contains("beta")) {
            VERSION_BEESOFT = applicationVersion + "-" + releaseType;
        } else {
            VERSION_BEESOFT = applicationVersion;
        }

    }

    public static final String PROGRAM = "BEESOFT";
    public static String VERSION_BOOTLOADER;
    ;
    
    //public static final String VERSION_FIRMWARE_FINAL = configProperties.getAppProperty("firmware.current.version");
    public static String FIRMWARE_IN_USE;
    public static String SERIAL_NUMBER = "9999999999";
    private final static String VERSION_JAVA = System.getProperty("java.version");
    public static String VERSION_MACHINE = "000000000000";
    public static final String GCODE_DELIMITER = "--";
    public static final String GCODE_TEMP_FILENAME = "temp.gcode";
    public static final String MODELS_FOLDER = "3DModels";
    public static final String GCODE2PRINTER_PATH = Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/gcodeToPrinter.gcode";

    /**
     * The textual representation of this version (4 digits, zero padded).
     */
    //public static final String VERSION_NAME = String.format("%deta",VERSION);
    public static final String VERSION_NAME = VERSION_BEESOFT;
    /**
     * The machine controller in use.
     */
    private static MachineLoader machineLoader;
    /**
     * The general-purpose logging object.
     */
    public static final Logger LOGGER = Logger.getLogger("replicatorg.log");
    public static FileHandler logFileHandler = null;
    public static String logFilePath = null;

    /**
     * Properties file
     */
    private static Properties propertiesFile = null;
    /* Date time instance variables */
    private static final DateFormat DATEFORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static final File BEELOG_FILE = new File(getAppDataDirectory().toString() + "/BEELOG.txt");
    private static final File COMLOG_FILE = new File(getAppDataDirectory().toString() + "/comLog.txt");
    private static final BufferedWriter LOGBW = initLog(BEELOG_FILE);
    private static final BufferedWriter COMLOGBW = initLog(COMLOG_FILE);

    /**
     * Path of filename opened on the command line, or via the MRJ open document
     * handler.
     */
    public static String openedAtStartup;
    /**
     * This is the name of the alternate preferences set that this instance of
     * SimpleG uses. If null, this instance will use the default preferences
     * set.
     */
    private static final String ALTERNATE_PREFS = null;

    /**
     * Get the preferences node for SimpleG.
     */
    static Preferences getUserPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(Base.class);
        if (ALTERNATE_PREFS != null) {
            prefs = prefs.node("alternate/" + ALTERNATE_PREFS);
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
    public static File getUserDirectory() {
        String path = System.getProperty("user.home") + File.separator + ".replicatorg";
        if (ALTERNATE_PREFS != null) {
            path = path + File.separator + ALTERNATE_PREFS;
        }
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
            if (!dir.exists()) { // we failed to create our user dir. Log the failure, try to continue
                //Base.logger.log(Level.SEVERE, "We could not create a user directory at: {0}", path);
                return null;
            }
        }

        return dir;
    }

    public static int getModelID() {
        ID++;
        return ID;
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
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static void closeLogs() {
        try {
            LOGBW.close();
            COMLOGBW.close();
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
    public static File getApplicationDirectory() {
        return new File(System.getProperty("user.dir"));
    }

    public static File getAppDataDirectory() {
        File f = new File(System.getProperty("user.home").concat("/BEESOFT"));
        File models = new File(System.getProperty("user.home").concat("/BEESOFT/" + Base.MODELS_FOLDER));

        if (!f.exists()) {
            // create BEESOFT dir
            f.mkdir();
        }

        if (!models.exists()) {
            // create models dir inside BEESOFT
            models.mkdir();
        }

        return f;
    }

    private static Map<String, String> getGalleryMap(File[] models) {
        Map<String, String> mod = new HashMap<>();

        for (File model : models) {
            mod.put(model.getName(), model.getAbsolutePath());
        }
        return mod;
    }

    private static void copyFolderRecursively(final File sourceDir, final File destDir) throws IOException {

        final String[] files;
        File srcFile, destFile;

        files = sourceDir.list();

        if (sourceDir.isDirectory()) {
            if (destDir.exists() == false) {
                destDir.mkdir();
            }

            for (String file : files) {
                srcFile = new File(sourceDir, file);
                destFile = new File(destDir, file);
                copyFolderRecursively(srcFile, destFile);
            }
        } else {
            try {
                Files.copy(sourceDir.toPath(), destDir.toPath());
            } catch (FileAlreadyExistsException ex) {
                // ignore in case a file with the same name already exists, and
                // keep going
                final long file1crc, file2crc;

                file1crc = calculateCRC(sourceDir);
                file2crc = calculateCRC(destDir);

                if (file1crc > 0 && file2crc > 0 && file1crc != file2crc) {
                    Files.copy(sourceDir.toPath(), destDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

    }

    // http://crackingjavainterviews.blogspot.pt/2013/05/efficient-way-to-calculate-checksum-of.html
    private static long calculateCRC(File filename) {
        final int SIZE = 16 * 1024;
        int length;
        FileChannel channel;
        CRC32 crc;
        byte[] bytes;

        try (FileInputStream in = new FileInputStream(filename);) {
            channel = in.getChannel();
            crc = new CRC32();
            length = (int) channel.size();
            MappedByteBuffer mb = channel.map(FileChannel.MapMode.READ_ONLY, 0, length);
            bytes = new byte[SIZE];
            int nGet;
            while (mb.hasRemaining()) {
                nGet = Math.min(mb.remaining(), SIZE);
                mb.get(bytes, 0, nGet);
                crc.update(bytes, 0, nGet);
            }
            return crc.getValue();
        } catch (Exception ex) {
            Base.writeLog(ex.getClass().getName() + " while calculating CRC: " + ex.getMessage());
        }

        return -1;
    }

    public static void copy3DFiles() {
        final File sourceDir;
        final File destDir;

        sourceDir = new File(Base.getApplicationDirectory() + "/" + Base.MODELS_FOLDER);
        destDir = new File(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER);

        try {
            copyFolderRecursively(sourceDir, destDir);
        } catch (IOException ex) {
            Base.writeLog(ex.getMessage(), Base.class);
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
            LOGBW.newLine();
            LOGBW.newLine();
            LOGBW.write("*************** CONNECTED PRINTER ****************" + NEW_LINE);
            LOGBW.write("Bootloader version: " + VERSION_BOOTLOADER + NEW_LINE);
            LOGBW.write("Firmware version: " + FIRMWARE_IN_USE + NEW_LINE);
            LOGBW.write("Serial number: " + SERIAL_NUMBER + NEW_LINE);
            LOGBW.write("**************************************************" + NEW_LINE);
            LOGBW.flush();
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void writeLogHeader() {
        try {
            LOGBW.newLine();
            LOGBW.write("**************************************************" + NEW_LINE);
            LOGBW.write(PROGRAM + " " + VERSION_BEESOFT + NEW_LINE);
            LOGBW.write("Java version: " + VERSION_JAVA + NEW_LINE);
            LOGBW.write("Operating system: " + PLATFORM.name() + NEW_LINE);
            LOGBW.write("Arch: " + System.getProperty("os.arch") + NEW_LINE);
            LOGBW.write("**************************************************" + NEW_LINE);
            LOGBW.flush();
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void writeLog(String message) {

        /**
         * *** Date and Time procedure ****
         */
        Calendar calendar = Calendar.getInstance();
        String date = DATEFORMAT.format(calendar.getTime());

        try {
            LOGBW.newLine();
            LOGBW.write("[" + date + "]" + " " + message);
            LOGBW.flush();
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void writeLog(String message, Class logClass) {

        if (BEELOG_FILE.length() > 10000000) {
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
        String date = DATEFORMAT.format(calendar.getTime());

        try {
            LOGBW.newLine();
            LOGBW.write("[" + date + "]" + " (" + logClass.getSimpleName() + ") " + message);
            LOGBW.flush();
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

        if (COMLOG_FILE.length() > 10000000) {
            return;
        }

        try {
            if (!message.equals("\n")) {
                COMLOGBW.write("Timestamp: " + timeStamp + " | " + message + NEW_LINE);
                COMLOGBW.flush();
            } else {
                COMLOGBW.newLine();
                COMLOGBW.flush();
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

    public static File getApplicationFile(String path) {
        return new File(getApplicationDirectory(), path);
    }

    public static File getUserFile(String path) {
        return getUserFile(path, true);
    }

    public static File getUserDir(String path) {
        return getUserDir(path, true);
    }

    /**
     * Clear all STL and GCODE files to avoid user access
     *
     * @param dirPath Path to directory containing such files
     */
    public static void cleanDirectoryTempFiles(String dirPath) {
        File dir = new File(dirPath);
        for (File file : dir.listFiles()) {
            if (file.getName().contains(".gcode") && file.getName().contains("gcodeToPrinter") == false) {
                file.delete();
            }
        }
    }

    public static void disposeAllOpenWindows() {
        for (final Window window : Base.getMainWindow().getOwnedWindows()) {
            final String name;

            name = window.getName();

            if (!name.equals("mainWindow") && !(name.equals("FeedbackDialog")
                    && Base.keepFeedbackOpen)) {
                if (window.isDisplayable()) {
                    window.setVisible(false);
                }
            }
        }
        editor.setEnabled(true);
    }

    public static void enableAllOpenWindows() {
        for (final Window window : Base.getMainWindow().getOwnedWindows()) {
            if (window.getName().equals("mainWindow")) {
                //|| win[i].getName().equals("Autonomous")
                if (printPaused) {
                    window.setEnabled(false);
                } else {
                    window.setEnabled(true);
                }
            } else {
                window.setEnabled(true);
            }
        }

    }

    public static void bringAllWindowsToFront() {
        bringMainWindowOK();
        for (final Window window : Base.getMainWindow().getOwnedWindows()) {
            window.toFront();
            window.requestFocusInWindow();
        }
    }

    public static void bringMainWindowOK() {
        Base.getMainWindow().setFocusable(true);
        Base.getMainWindow().setFocusableWindowState(true);
    }

    public static void setMainWindowNOK() {
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

    public static NumberFormat getLocalFormat() {
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

    public static NumberFormat getGcodeFormat() {
        return gcodeNF;
    }

    /**
     *
     * @param path The relative path to the file in the .replicatorG directory
     * @param autoCopy If true, copy over the file of the same name in the
     * application directory if none is found in the prefs directory.
     * @return
     */
    public static File getUserFile(String path, boolean autoCopy) {
        if (path.contains("..")) {
            //Base.logger.log(Level.INFO, "Attempted to access parent directory in {0}, skipping", path);
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
                    //Base.logger.log(Level.SEVERE, "Couldn't copy " + path + " to your local .replicatorG directory", f);
                }
            }
        }
        return f;
    }

    public static File getUserDir(String path, boolean autoCopy) {
        if (path.contains("..")) {
            //Base.logger.log(Level.INFO, "Attempted to access parent directory in {0}, skipping", path);
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
                    //Base.logger.log(Level.SEVERE, "Couldn't copy " + path + " to your local .replicatorG directory", f);
                }
            }
        }
        return f;
    }

    public static Font getFontPref(String name, String defaultValue) {
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

    public static Color getColorPref(String name, String defaultValue) {
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

    public static void main(String args[]) {

        if (Base.isMacOS()) {
            // Default to sun's XML parser, PLEASE.  Some apps are installing some janky-ass xerces.
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                    "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name",
                    "BEESOFT");
            System.setProperty("java.awt.headless", "false");
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
                        // do nothing
                    }
                }
                if (debugLevelArg == 0) {
                    LOGGER.setLevel(Level.INFO);
                    LOGGER.info("Debug level is 'INFO'");
                } else if (debugLevelArg == 1) {
                    LOGGER.setLevel(Level.FINE);
                    LOGGER.info("Debug level is 'FINE'");
                } else if (debugLevelArg == 2) {
                    LOGGER.setLevel(Level.FINER);
                    LOGGER.info("Debug level is 'FINER'");
                } else if (debugLevelArg == 3) {
                    LOGGER.setLevel(Level.FINEST);
                    LOGGER.info("Debug level is 'FINEST'");
                } else if (debugLevelArg >= 4) {
                    LOGGER.setLevel(Level.ALL);
                    LOGGER.info("Debug level is 'ALL'");
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

        // Create the new application "Base" class.
        new Base();
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
                Set<Object> includes = new HashSet<>();
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
            }
        } catch (Exception e) {
            writeLog(e.getMessage(), this.getClass());
        }

        // use native popups so they don't look so crappy on osx
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        SwingUtilities.invokeLater(() -> {
            final WelcomeSplash welcomeSplash;
            
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
            editor.loadMachine();
            writeLog("Machine Loaded", this.getClass());
            // show the window
            editor.setVisible(false);
            welcomeSplash = new WelcomeSplash(editor);
            welcomeSplash.setVisible(true);

//                buildLogFile(false);
            writeLog("Log file created", this.getClass());
        });
    }

    // .................................................................
    /**
     * returns true if the ReplicatorG is running on a Mac OS machine,
     * specifically a Mac OS X machine because it doesn't run on OS 9 anymore.
     *
     * @return
     */
    public static boolean isMacOS() {
        return PLATFORM == Platform.MACOSX;
    }

    /**
     * returns true if running on windows.
     *
     * @return
     */
    public static boolean isWindows() {
        return PLATFORM == Platform.WINDOWS;
    }

    /**
     * true if running on linux.
     *
     * @return
     */
    public static boolean isLinux() {
        return PLATFORM == Platform.LINUX;
    }

    /**
     * Registers key events for a Ctrl-W and ESC with an ActionListener that
     * will take care of disposing the window.
     *
     * @param root
     * @param disposer
     */
    public static void registerWindowCloseKeys(JRootPane root, // Window
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
    public static void quitWithError(String title, String message, Throwable e) {

        notificationHandler.showError(title, message, e);

        if (e != null) {
            Base.writeLog(e.getMessage(), Base.class);
        }
        System.exit(1);
    }

    public static String getContents(String what) {
        File appBase = Base.getApplicationDirectory();
        return appBase.getAbsolutePath() + File.separator + what;
    }

    public static String getLibContents(String what) {
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
    public static Image getDirectImage(String name, Component who) {
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

    public static BufferedImage getImage(String name, Component who) {
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
        } catch (InterruptedException | IOException | IllegalArgumentException e) {
            //Base.logger.log(Level.FINE, "Could not load image: " + name, e);
        }
        //Base.logger.log(Level.FINE, "Could not load image: " + name, ioe);
        //Base.logger.log(Level.FINE, "Could not load image: " + name, iae);

        return image;
    }

    public static InputStream getStream(String filename) throws IOException {
        return new FileInputStream(getLibContents(filename));
    }

    // ...................................................................
    private static void copyFile(File afile, File bfile) throws IOException {
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

    public static void copyDir(File sourceDir, File targetDir)
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
     * Get a reference to the currently selected machine
     *
     *
     * @return
     */
    public static MachineLoader getMachineLoader() {
        if (machineLoader == null) {
            machineLoader = new MachineLoader();
        }
        return machineLoader;
    }

    public static MainWindow getMainWindow() {
        return editor;
    }

    public static void resetPrintingFlags() {
        isPrinting = false;
        printPaused = false;
        isPrintingFromGCode = false;
    }

    /**
     * To be used when catch isn't supposed to do anything
     *
     * @param ms time in ms to wait
     */
    public static void hiccup(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            System.out.println("interrupt");
        }
    }

    public static boolean isWelcomeSplashVisible() {
        return welcomeSplashVisible;
    }

    public static void setWelcomeSplashVisible(boolean isVisible) {
        if (isVisible == false) {
            synchronized (WELCOME_SPLASH_MONITOR) {
                WELCOME_SPLASH_MONITOR.notifyAll();
            }
        }
        welcomeSplashVisible = isVisible;
    }
}
