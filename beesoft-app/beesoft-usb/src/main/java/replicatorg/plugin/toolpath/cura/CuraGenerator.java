package replicatorg.plugin.toolpath.cura;

/**
 * Copyright (c) 2013 BEEVC - Electronic Systems
 */
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.Oracle;
import replicatorg.app.PrintEstimator;
import replicatorg.app.ProperDefault;
import replicatorg.app.util.StreamLoggerThread;
import replicatorg.plugin.toolpath.ToolpathGenerator;

public class CuraGenerator extends ToolpathGenerator {

    private static String CURA_BIN_PATH = Base.getApplicationDirectory().getAbsolutePath().concat("/curaEngine/bin/CuraEngine");
    private static String CURA_CONFIGURATION_FILE_PATH = Base.getAppDataDirectory() + "/configs/";
    private static String ERROR_MESSAGE = "Can't run Cura!";
    private static String BIN_PATH_UNIX = "python";
    private static String BIN_PATH_WINDOWS = "C:\\Python27\\python.exe";
    private static final String gallery = Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER;
    private CuraEngineConfigurator curaGenerator = new CuraEngineConfigurator();
    boolean configSuccess = false;
    String profile = null;
    List<String> preferences;
    Process process;
    StreamLoggerThread ist;
    StreamLoggerThread est;

    /**
     * Sets Cura generator paths for bin and gcode export.
     */
    public CuraGenerator() {
        preferences = getPreferences();

        if (Base.isLinux() || Base.isMacOS()) {
            CURA_BIN_PATH = Base.getApplicationDirectory().getAbsolutePath().concat("/curaEngine/bin/CuraEngine");
            CURA_CONFIGURATION_FILE_PATH = Base.getAppDataDirectory() + "/configs/";
        } else {
            /**
             * Different call depending on windows arch. CuraEngine is compiled
             * in x86 and x64 and it requires different libs
             */
            if (Base.isx86_64()) {
                CURA_BIN_PATH = Base.getApplicationDirectory().getAbsolutePath().concat("\\curaEngine\\bin\\x64\\CuraEngine.exe");
            } else {
                CURA_BIN_PATH = Base.getApplicationDirectory().getAbsolutePath().concat("\\curaEngine\\bin\\x86\\CuraEngine.exe");
            }
            CURA_CONFIGURATION_FILE_PATH = Base.getAppDataDirectory() + "\\configs\\";
        }
    }

    /**
     * Gets print parameters.
     *
     * @return print parameters
     */
    public List<String> getPreferences() {
        return preferences;
    }

    /**
     * Destroys generator process.
     */
    @Override
    public void destroyProcess() {
        if (ist != null && est != null) {
            ist.stop();
            est.stop();
        }
        process.destroy();
    }

    /**
     * Calculates Sparse line distance value from density percentage.
     *
     * @param densityPercentage density percentage.
     * @return Sparse line distance for CFG file.
     */
    public String getSparseLineDistance(int densityPercentage) {
        return curaGenerator.getDensity(densityPercentage);
    }

    /**
     * Sets profile type for the Cura generator.
     *
     * @param profile2 profile type.
     */
    public void setProfile(String profile2) {
        this.profile = CURA_CONFIGURATION_FILE_PATH + profile2;
    }

    /**
     * Process INI file from profile type.
     */
    public void readINI() {
        curaGenerator.processINI(profile);
    }

    /**
     * Pre gcode generation setup. Creates the INI file to be passed for the CFG
     * creator.
     *
     * @param profile profile type.
     * @return path for the INI file created
     */
    public String preparePrint(String profile) {

        String bee_code = profile.split(":")[0];
        String resol = profile.split(":")[1];
        HashMap<String, String> overload_values = Languager.getTagValues(2, bee_code, resol);
        
        if(overload_values == null){
            Base.getMainWindow().showFeedBackMessage("reinstallError");
            return null;
        }        
        
        return curaGenerator.setupINI(overload_values, bee_code, resol);
    }

    /**
     * Generates GCode from STL and print parameters.
     *
     * @param stl stl file.
     * @param prefs print parameters.
     * @return gcode file.
     */
    @Override
    public File generateToolpath(File stl, List<CuraEngineOption> prefs) {

        //Tests if CuraEngine has +x permissions or if it does exist
        File curaBin = new File(CURA_BIN_PATH);
        if (curaBin.canExecute() == false || curaBin.exists() == false) {
            Base.writeLog("CuraEngine no execute permissions");
            Base.getMainWindow().showFeedBackMessage("gcodeGeneration");
            return null;
        }

        // Builds files paths
        String path = stl.getAbsolutePath();
//        String fileName = stl.getAbsolutePath().substring(0, stl.getAbsolutePath().lastIndexOf("."));
        String gcodePath = path.replaceAll(".stl", ".gcode");
        String cfgFilePath = curaGenerator.dotheWork(new File(profile)).getAbsolutePath();

        // Signals Oracle that GCode generation has started
        Oracle.setTic();

        //Process parameters for session
        List<String> arguments = new LinkedList<String>();

        // The -u makes python output unbuffered. Oh joyous day.
        /**
         *
         * ./CuraEngine -v -s print-temperature=220 layer-height=0.1 -o
         * ultifoot.gcode ultifoot.stl /cura.py -i /home/jb/curaProfile.ini -s
         * /home/jb/CuraEngine/ultifoot.stl
         *
         */
        String[] baseArguments = {CURA_BIN_PATH, "-v", "-c", cfgFilePath};
        String[] filesArguments = {"-o", gcodePath, path};

        // Adds base arguments to the process
        for (String arg : baseArguments) {
            arguments.add(arg);
        }

        /**
         * Adds overload parameters: - resolution - density- raft - support
         */
        if (prefs != null && ProperDefault.get("curaFile").contains("none")) {
            //Flag for overload
            for (Object option : prefs) {
                arguments.add("-s");
                arguments.add(((CuraEngineOption) option).getArgument());
            }
        }

        // Adds files arguments to the process
        for (String arg : filesArguments) {
            arguments.add(arg);
        }
//        System.out.println("********************");
//        // Prints arguments
        Base.writeLog("Cura Path " + CURA_BIN_PATH);
        Base.writeLog("Cura prefs path " + CURA_CONFIGURATION_FILE_PATH);
//        for (String arg : arguments) {
//            Base.writeLog(arg+" ");
//        }
//        System.out.println("********************");

        ProcessBuilder pb = new ProcessBuilder(arguments);
        pb.directory(new File(gallery));
        process = null;
        try {
            process = pb.start();
            Base.writeLog("Starting ProcessBuilder");
            ist = new StreamLoggerThread(
                    process.getInputStream()) {
                @Override
                protected void logMessage(String line) {
                    emitUpdate(line);
                    Base.writeLog(line);
                    super.logMessage(line);
                }
            };
            est = new StreamLoggerThread(
                    process.getErrorStream());
            est.setDefaultLevel(Level.SEVERE);
            ist.setDefaultLevel(Level.FINE);
            ist.start();
            est.start();
            int value = process.waitFor();
            if (value != 0) {
                Base.writeLog("Unrecognized error code returned by CuraEngine.");
                // Throw ToolpathGeneratorException
                return null;
            }
        } catch (IOException ioe) {
            Base.logger.log(Level.SEVERE, ERROR_MESSAGE, ioe);
            Base.writeLog(ERROR_MESSAGE);
            process.destroy();
            ist.stop();
            est.stop();
            // Throw ToolpathGeneratorException
            return null;
        } catch (InterruptedException ex) {
            Base.logger.log(Level.SEVERE, ERROR_MESSAGE, ex);
            Base.writeLog(ERROR_MESSAGE);
            process.destroy();
            ist.stop();
            est.stop();
            // Throw ToolpathGeneratorException
            return null;
        }

        int lastIdx = path.lastIndexOf('.');
        String root = (lastIdx >= 0) ? path.substring(0, lastIdx) : path;

        // Signals Oracle that GCode generation has finished
        Oracle.setToc();

        Base.writeLog("File " + root + ".gcode created with success");
        File gcode = new File(gcodePath);

        ist.stop();
        est.stop();


        // Estimates print time
        PrintEstimator.estimateTime(gcode);

        return gcode;
    }

    @Override
    public File getGeneratedToolpath() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Cura options to pass on bin.
     */
    public static class CuraEngineOption {

        final String parameter;
        final String value;

        /**
         * CuraEngine options constructor.
         *
         * @param param option attribute
         * @param val option value
         */
        public CuraEngineOption(String param, String val) {
            this.parameter = param;
            this.value = val;
        }

        /**
         * CuraEngine options constructor. Empty value.
         *
         * @param param option attribute
         */
        public CuraEngineOption(String parameter) {
            this.parameter = parameter;
            this.value = "";
        }

        /**
         * Reads set parameters.
         *
         * @return CuraEngine option parameter.
         */
        public String getParameter() {
            return this.parameter;
        }

        /**
         * Reads set value.
         *
         * @return CuraEngine option value.
         */
        public String getValue() {
            return this.value;
        }

        /**
         * Reads CuraEngine attribute=value.
         *
         * @return CuraEngine option string.
         */
        public String getArgument() {
            return this.parameter + "=" + this.value;
        }
    }
}
