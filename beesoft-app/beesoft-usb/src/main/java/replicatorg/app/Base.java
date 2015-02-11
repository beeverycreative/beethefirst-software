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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashSet;
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
import replicatorg.machine.MachineLoader;
import ch.randelshofer.quaqua.QuaquaManager;
import com.apple.eawt.Application;

import com.apple.mrj.MRJApplicationUtils;
import com.apple.mrj.MRJOpenDocumentHandler;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import replicatorg.app.ui.WelcomeSplash;

/**
 * Primary role of this class is for platform identification and general
 * interaction with the system (launching URLs, loading files and images, etc)
 * that comes from that.
 */
public class Base {

    public static boolean status_thread_died = false;
    public static boolean errorOccured = false;
    public static boolean printPaused = false;
    public static double originalColorRatio = 1;
    private static String COMPUTER_ARCHITECTURE;

    public enum InitialOpenBehavior {

        OPEN_LAST,
        OPEN_NEW,
        OPEN_SPECIFIC_FILE
    };
    public static int ID = 0;
    public static final String VERSION_BEESOFT = "3.10.0_beta1-2014-08-12";
//    public static final String VERSION_BEESOFT = "3.8.0-beta_2014-05-01";
    public static final String PROGRAM = "BEESOFT";
    public static String VERSION_BOOTLOADER = "Bootloader v3.1.1-beta";
    public static String firmware_version_in_use = "BEETHEFIRST-4.35.0.bin";
    public static final String VERSION_FIRMWARE_FINAL = "4.35.0";
    public static final String VERSION_FIRMWARE_FINAL_OLD = "3.35.0";
    private static String VERSION_JAVA = "";//System.getProperty("java.version");
    public static String VERSION_MACHINE = "000000000000";
    public static String language = "en";
    public static String MACHINE_NAME = "BEETHEFIRST";
    public static String GCODE_DELIMITER = "--";
    public static String GCODE_TEMP_FILENAME = "temp.gcode";
    public static String MODELS_FOLDER = "3DModels";
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
    public static Logger logger = Logger.getLogger("replicatorg.log");
    public static FileHandler logFileHandler = null;
    public static String logFilePath = null;
    /**
     * Global LOG file *
     */
    private static File log;
    /**
     * Autonomous statistics file
     */
    private static File statistics = null;
    /**
     * Properties file
     */
    private static Properties propertiesFile = null;
    private static FileOutputStream writer;
    private static FileInputStream read;
    /* Date time instance variables */
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public static boolean maintenanceOpened = false;
    public static boolean maintenanceWizardOpen = false;
    public static ArrayList<Thread> systemThreads;

    /*
     * expands ~ as per python os.path.expanduser
     */
    public static String expanduser(String path) {
        String user = System.getProperty("user.home");

        return path.replaceFirst("~", user);
    }

    /**
     * Start logging on the given path. If the path is null, stop file logging.
     *
     * @param path The path to log messages to
     */
    public static void setLogFile(String path) {


        if (logFileHandler != null) {
            logger.removeHandler(logFileHandler);
            logFileHandler = null;
        }

    }
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
    static private String alternatePrefs = null;

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
     * Back up the preferences
     *
     * @return
     */
    static public String getToolsPath() {
        String toolsDir = System.getProperty("replicatorg.toolpath");
        if (toolsDir == null || (toolsDir.length() == 0)) {
            File appDir = Base.getApplicationDirectory();
            toolsDir = appDir.getAbsolutePath() + File.separator + "tools";
        }
        return toolsDir;
    }

    /**
     * Get the the user preferences and profiles directory. By default this is
     * ~/.replicatorg; if an alternate preferences set is selected, it will
     * instead be ~/.replicatorg/alternatePrefs/<i>alternate_prefs_name</i>.
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
                Base.logger.severe("We could not create a user directory at: " + path);
                return null;
            }
        }

        return dir;
    }

    public static int getModelID() {
        ID++;
        return ID;
    }

    /**
     * Lists all threads running on the JVM.
     */
    public static void listAllJVMThreads() {

        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);

        for (int i = 0; i < threadArray.length; i++) {
            System.out.println("Thread " + (i + 1) + " > " + threadArray[i]);
        }
        System.out.println("\n");
        System.out.println("Total Threads > "+threadArray.length);
        System.out.println("*****************");
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

    public static void buildLogFile(boolean force) {
        String content = " ";
        File f = new File(log.getAbsolutePath());

        try {
            byte[] bytes = readAllBytes(f);
            content = new String(bytes, "UTF-8");
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        clearFileContent(f);

        if (force) {
            writeLogHeader();
        }

        writeLog(content);

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

    public static void clearFileContent(File inputFile) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(inputFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
        writer.print("");
        writer.close();
    }

    private static Map<String, String> getGalleryMap(File[] models) {
        Map<String, String> mod = new HashMap<String, String>();

        for (int i = 0; i < models.length; i++) {
            mod.put(models[i].getName(), models[i].getAbsolutePath());
        }
        return mod;
    }

    public static void copy3DFiles() {
        InputStream inStream = null;
        OutputStream outStream = null;

        File[] models = new File(Base.getApplicationDirectory() + "/" + Base.MODELS_FOLDER).listFiles();
        Map<String, String> galleryModels = getGalleryMap(new File(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER).listFiles());

        for (int i = 0; i < models.length; i++) {
            try {

                File afile = new File(models[i].getAbsolutePath());
                File bfile = new File(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/" + models[i].getName());

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
                e.printStackTrace();
            }
        }

    }

    public static void updateVersions() {
        VERSION_BOOTLOADER = editor.getMachineInterface().getDriver().getBootloaderVersion();
        firmware_version_in_use = editor.getMachineInterface().getDriver().getFirmwareVersion();
        VERSION_MACHINE = editor.getMachineInterface().getDriver().getSerialNumber();

        buildLogFile(true);
    }

    private static void writeLogHeader() {

        VERSION_BOOTLOADER = editor.getMachineInterface().getDriver().getBootloaderVersion();
        firmware_version_in_use = editor.getMachineInterface().getDriver().getFirmwareVersion();
        VERSION_MACHINE = editor.getMachineInterface().getDriver().getSerialNumber();

        FileWriter fw = null;
        try {
            fw = new FileWriter(log.getAbsoluteFile(), true);
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedWriter bw = new BufferedWriter(fw);
        try {
            bw.newLine();
            bw.write("**************************************************\n");
            bw.write(PROGRAM + " " + VERSION_BEESOFT + "\n");
            bw.write("Bootloader version: " + VERSION_BOOTLOADER + "\n");
            bw.write("Firmware version: " + firmware_version_in_use + "\n");
            bw.write("Java version: " + VERSION_JAVA + "\n");
            bw.write("Architecture: " + COMPUTER_ARCHITECTURE + "\n");
//            bw.write("Serial Number:" + " " + VERSION_MACHINE+ "\n");
            bw.write("Machine name: BEETHEFIRST\n");
            bw.write("Company name: BEEVERYCREATIVE\n");
            bw.write("**************************************************\n");
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void writeLog(String message) {

        /**
         * *** Date and Time procedure ****
         */
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());

        FileWriter fw = null;
        try {
            fw = new FileWriter(log.getAbsoluteFile(), true);
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedWriter bw = new BufferedWriter(fw);
        try {
            bw.newLine();
            bw.write("[" + date + "]" + " " + message);
            bw.flush();
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void writeStatistics(String message) {

        /**
         * *** Date and Time procedure ****
         */
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
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void writecomLog(long timeStamp, String message) {

        File f = new File(getAppDataDirectory() + "/comLog.txt");
        boolean canCreateFile = f.canRead() && f.canWrite();

        FileWriter fw = null;
        try {
            fw = new FileWriter(new File(getAppDataDirectory() + "/comLog.txt"), true);
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedWriter bw = new BufferedWriter(fw);
        try {
            if (!message.equals("\n")) {
                bw.write("Timestamp: " + timeStamp + " | " + message + "\n");
                bw.flush();
                bw.close();
            } else {
                bw.newLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static Properties openFileProperties() {

        File f = new File(getAppDataDirectory().toString().concat("/config.properties"));
        Properties nw = new Properties();

        if (!(f.exists())) {
            /**
             * Does this to avoid FileNotFoundException Therefore, file exists
             * and writes a empty string to validate existence
             */
            PrintWriter w = null;
            try {
                w = new PrintWriter(f.getAbsolutePath());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            }
            w.print("");
            w.close();

            FileInputStream fileInput = null;
            try {
                fileInput = new FileInputStream(f);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                nw.load(fileInput);
                fileInput.close();
            } catch (IOException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        // no need for else. File already exists and loads its properties

        return nw;
    }

    /**
     * Write properties to the config file
     */
    public static void writeConfig() {

        File f = new File(getAppDataDirectory().toString().concat("/config.properties"));

        PrintWriter w = null;
        try {
            w = new PrintWriter(f.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
        w.print("");
        w.close();

        /**
         * FileOutputStream for Properties usage
         */
        try {
            writer = new FileOutputStream(f.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            propertiesFile.store(writer, null);
            writer.flush();
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
     * Write new property to the config file
     *
     * @param param new atribute to be added to configs
     * @param value atribute value
     */
    public static String readConfig(String param) {

        if (propertiesFile == null) {
            return null;
        }

        return propertiesFile.getProperty(param);
    }

    public static void loadProperties() {

        File f = new File(getAppDataDirectory().toString().concat("/config.properties"));

        if (f.exists()) {

            /**
             * FileInputStream for Properties usage
             */
            try {
                read = new FileInputStream(f.getAbsolutePath());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                propertiesFile.load(read);
            } catch (IOException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
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
        File dir = new File(dirPath);
        for (File file : dir.listFiles()) {
            if (file.getName().contains(".stl") || file.getName().contains(".gcode")) {
                file.delete();
            }
        }
    }
    
    public static void turnOnPowerSaving() {
        editor.getMachine().getDriver().dispatchCommand("M641");
    }

    private static void getJavaVersion() {
        String[] command = {"java", "-version"};
        ProcessBuilder probuilder = new ProcessBuilder(command);
        probuilder.redirectErrorStream(true);

        Process process = null;
        try {
            process = probuilder.start();
        } catch (IOException ex) {
        }

        //Read out dir output
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = "";
        String out = "";
        try {
            while ((line = br.readLine()) != null) {
                out += line + "\n";
            }
        } catch (IOException ex) {
        }
        //Wait to get exit value
        try {
            int exitValue = process.waitFor();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        VERSION_JAVA = out.trim();
    }

    static public void diposeAllOpenWindows() {
        java.awt.Window win[] = java.awt.Window.getWindows();
        for (int i = 0; i < win.length; i++) {
            if (!win[i].getName().equals("mainWindow")) {
                win[i].dispose();
            }
//            System.out.println(win[i].getName());
        }
        editor.setEnabled(true);
        editor.getButtons().resetVariables();
        THREAD_KEEP_ALIVE = false;

        for (int i = 0; i < systemThreads.size(); i++) {
            systemThreads.get(i).stop();
        }

    }
    
    static public void enableAllOpenWindows() {
        java.awt.Window win[] = java.awt.Window.getWindows();
        for (int i = 0; i < win.length; i++) {
            if (win[i].getName().equals("mainWindow")) { //|| win[i].getName().equals("Autonomous") 
                if (printPaused) {
                    win[i].setEnabled(false);
                } else {
                    win[i].setEnabled(true);
                }

            } else {
                win[i].setEnabled(true);
            }
//            }
//            System.out.println(win[i].getName());
        }

    }

    static public void bringAllWindowsToFront() {
        bringMainWindowOK();
        java.awt.Window win[] = java.awt.Window.getOwnerlessWindows();
        for (int i = 0; i < win.length; i++) {
            win[i].toFront();
            win[i].requestFocusInWindow();
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
     * @param s string to validate
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
    static private NumberFormat localNF = NumberFormat.getInstance();

    static public NumberFormat getLocalFormat() {
        return localNF;
    }
    /**
     * Singleton Gcode NumberFormat: Unsed for writing the correct precision
     * strings when generating gcode (minimum one decimal places) using . as
     * decimal separator
     */
    static private NumberFormat gcodeNF;

    {
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
            Base.logger.info("Attempted to access parent directory in " + path + ", skipping");
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
            Base.logger.info("Attempted to access parent directory in " + path + ", skipping");
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
                ((fontstyle.indexOf("bold") != -1) ? Font.BOLD : 1)
                | ((fontstyle.indexOf("italic") != -1) ? Font.ITALIC
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
    private static NotificationHandler notificationHandler = null;
    private static final String[] supportedExtensions = {
        "stl", "bee"};

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
                    };
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
        System.setProperty("j3d.implicitAntialiasing", "true");

        // MAC OS X ONLY:
        // register a temporary/early version of the mrj open document handler,
        // because the event may be lost (sometimes, not always) by the time
        // that MainWindow is properly constructed.
        MRJOpenDocumentHandler startupOpen = new MRJOpenDocumentHandler() {
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

    /**
     * Creates a new BEELOG file for print log
     *
     * @return file
     */
    private File openFileLog() {
        /**
         * Put log file near app folder *
         */
        String path = getAppDataDirectory().toString();
        File f = new File(path + "/BEELOG.txt");

        if (f.exists()) {
            f.delete();
            return new File(path + "/BEELOG.txt");
        } else {
            return f;
        }

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
     * @param cleanPrefs Before starting ReplicatorG proper, erase the user
     * preferences.
     */
    public Base() {

        // Log and messages queue init
        log = openFileLog();

        // Log autonomous statistics file
        statistics = openStatsFile();

        // Properties file init
        propertiesFile = openFileProperties();

        // Loads properties at the beginning
        loadProperties();

        File comLog = new File(getAppDataDirectory() + "/comLog.txt");
        if (comLog.exists()) {
            comLog.delete();
        }

        getJavaVersion();

        // Loads language
        language = ProperDefault.get("language").toLowerCase();

        systemThreads = new ArrayList<Thread>();

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

                writeLog("Operating System: Mac OS");

            } else if (Base.isLinux()) {
                writeLog("Operating System: Linux");
            } else {
                writeLog("Operating System: Windows");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // use native popups so they don't look so crappy on osx
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ProperDefault.put("machine.name", MACHINE_NAME);
                String machineName = ProperDefault.get("machine.name");
                // build the editor object
                editor = new MainWindow();
                writeLog("Main Window initialized");
                notificationHandler = NotificationHandler.Factory.getHandler(editor, Boolean.valueOf(ProperDefault.get("ui.preferSystemTrayNotifications")));
                editor.restorePreferences();
                writeLog("Preferences restored");
                // add shutdown hook to store preferences
                Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {
                    final private MainWindow w = editor;

                    public void run() {
                        w.onShutdown();
                    }
                });
//                Languager.printXML();
                editor.loadMachine(machineName, false);
                writeLog("Machine Loaded");
                // show the window
                editor.setVisible(false);
                WelcomeSplash splash = new WelcomeSplash(editor);
                splash.setVisible(true);

//                buildLogFile(false);
                writeLog("Log file created");
            }
        });
    }

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
    public static final float javaVersion = new Float(javaVersionName.substring(0, 3)).floatValue();
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

        if (platformName.toLowerCase().indexOf("mac") != -1) {
            // can only check this property if running on a mac
            // on a pc it throws a security exception and kills the applet
            // (but on the mac it does just fine)
            if (System.getProperty("mrj.version") != null) { // running on a
                // mac
                platform = (platformName.equals("Mac OS X")) ? Platform.MACOSX : Platform.MACOS9;
            }

        } else {
            String osname = System.getProperty("os.name");

            if (osname.indexOf("Windows") != -1) {
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

    // .................................................................
    /**
     * returns true if the ReplicatorG is running on a Mac OS machine,
     * specifically a Mac OS X machine because it doesn't run on OS 9 anymore.
     */
    static public boolean isMacOS() {
        return platform == Platform.MACOSX;
    }

    /**
     * returns true if running on windows.
     */
    static public boolean isWindows() {
        return platform == Platform.WINDOWS;
    }

    /**
     * true if running on linux.
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
     * "No cookie for you" type messages. Nothing fatal or all that much of a
     * bummer, but something to notify the user about.
     */
    static public void showMessage(String title, String message) {
        if (notificationHandler == null) {
            notificationHandler = NotificationHandler.Factory.getHandler(null, false);
        }
        notificationHandler.showMessage(title, message);
    }

    /**
     * Non-fatal error message with optional stack trace side dish.
     */
    static public void showWarning(String title, String message, Exception e) {
        if (notificationHandler == null) {
            notificationHandler = NotificationHandler.Factory.getHandler(null, false);
        }
        notificationHandler.showWarning(title, message, e);

        if (e != null) {
            e.printStackTrace();
        }
    }

    /**
     * Show an error message that's actually fatal to the program. This is an
     * error that can't be recovered. Use showWarning() for errors that allow
     * ReplicatorG to continue running.
     */
    static public void quitWithError(String title, String message, Throwable e) {

        notificationHandler.showError(title, message, e);

        if (e != null) {
            e.printStackTrace();
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
        from = null;
        to.close(); // ??
        to = null;

        bfile.setLastModified(afile.lastModified()); // jdk13+ required
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
    }

    /**
     * Grab the contents of a file as a string.
     */
    static public String loadFile(File file) throws IOException {
        Base.logger.info("Load file : " + file.getAbsolutePath());
        // empty code file.. no worries, might be getting filled up later
        if (file.length() == 0) {
            return "";
        }

        InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
        BufferedReader reader = new BufferedReader(isr);

        StringBuffer buffer = new StringBuffer();
        String line = null;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
            buffer.append('\n');
        }
        reader.close();
        return buffer.toString();
    }

    /**
     * Spew the contents of a String object out to a file.
     */
    static public void saveFile(String str, File file) throws IOException {
        Base.logger.info("Saving as " + file.getCanonicalPath());

        ByteArrayInputStream bis = new ByteArrayInputStream(str.getBytes());
        InputStreamReader isr = new InputStreamReader(bis);
        BufferedReader reader = new BufferedReader(isr);

        FileWriter fw = new FileWriter(file);
        PrintWriter writer = new PrintWriter(new BufferedWriter(fw));

        String line = null;
        while ((line = reader.readLine()) != null) {
            writer.println(line);
        }
        writer.flush();
        writer.close();
    }

    static public void copyDir(File sourceDir, File targetDir)
            throws IOException {
        targetDir.mkdirs();
        String files[] = sourceDir.list();
        for (int i = 0; i < files.length; i++) {
            if (files[i].equals(".") || files[i].equals("..")) {
                continue;
            }
            File source = new File(sourceDir, files[i]);
            File target = new File(targetDir, files[i]);
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

        for (int i = 0; i < list.length; i++) {
            if (list[i].charAt(0) == '.') {
                continue;
            }

            File file = new File(path, list[i]);
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
     * Get a reference to the currently selected machine *
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
}