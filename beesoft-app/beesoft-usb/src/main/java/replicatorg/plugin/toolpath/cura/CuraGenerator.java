package replicatorg.plugin.toolpath.cura;

/**
 * Copyright (c) 2013 BEEVC - Electronic Systems
 */
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
import pt.beeverycreative.beesoft.filaments.PrintPreferences;

import replicatorg.app.Base;
import replicatorg.app.Oracle;
import replicatorg.app.PrintEstimator;
import replicatorg.app.util.StreamLoggerThread;
import replicatorg.plugin.toolpath.ToolpathGenerator;

public class CuraGenerator extends ToolpathGenerator {

    private static String CURA_BIN_PATH = Base.getApplicationDirectory().getAbsolutePath().concat("/curaEngine/bin/CuraEngine");
    private static String CURA_CONFIGURATION_FILE_PATH = Base.getAppDataDirectory() + "/configs/";
    private static String ERROR_MESSAGE = "Can't run Cura!";
    private static final String gallery = Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER;
    private CuraEngineConfigurator curaGenerator = new CuraEngineConfigurator();
    private String profile = null;
    private Process process;
    private StreamLoggerThread ist;
    private StreamLoggerThread est;
    private final PrintPreferences prefs;

    /**
     * Sets Cura generator paths for bin and gcode export.
     * @param prefs printing preferences
     */
    public CuraGenerator(PrintPreferences prefs) {
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
        
        this.prefs = prefs;
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

    public String getValue(String key) {
        return curaGenerator.getValue(key);
    }

    /**
     * Pre gcode generation setup. Creates the INI file to be passed for the CFG
     * creator.
     *
     */
    public boolean preparePrint() {

        HashMap<String, String> overload_values
                = FilamentControler.getFilamentSettings(prefs.getCoilText(), 
                        prefs.getResolution(), prefs.getPrinter().filamentCode());

        if (overload_values == null) {
            Base.getMainWindow().showFeedBackMessage("unknownColor");
            return false;
        }

        this.profile = CURA_CONFIGURATION_FILE_PATH 
                + curaGenerator.setupINI(overload_values, prefs.getCoilText(), 
                        prefs.getResolution());
        curaGenerator.processINI(profile);
        
        return true;
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

        StringBuilder curaEngineCmd = new StringBuilder();
        String stlPath, gcodePath;
        List<String> arguments;
        Map<String,String> cfgMap;
        
        //Tests if CuraEngine has +x permissions or if it does exist
        File curaBin = new File(CURA_BIN_PATH);
        if (curaBin.canExecute() == false || curaBin.exists() == false) {
            Base.writeLog("CuraEngine no execute permissions", this.getClass());
            Base.getMainWindow().showFeedBackMessage("gcodeGeneration");
            return null;
        }

        // Builds files paths
        stlPath = stl.getAbsolutePath();
//        String fileName = stl.getAbsolutePath().substring(0, stl.getAbsolutePath().lastIndexOf("."));
        gcodePath = stlPath.replaceAll(".stl", ".gcode");
        cfgMap = curaGenerator.mapIniToCFG(prefs);

        //Process parameters for session
        arguments = new LinkedList<String>();
        
        String[] baseArguments = {CURA_BIN_PATH, "-v", "-p"};
        String[] filesArguments = {"-o", gcodePath, stlPath};

        // Adds base arguments to the process
        for (String s : baseArguments) {
            arguments.add(s);
            curaEngineCmd.append(" ");
            curaEngineCmd.append(s);
        }
        // Adds files arguments to the process
        for (String s: filesArguments) {
            arguments.add(s);
            curaEngineCmd.append(" ");
            curaEngineCmd.append(s);
        }
        
        for(Map.Entry arg : cfgMap.entrySet()) {
            arguments.add("-s");
            arguments.add(arg.getKey() + "=" + arg.getValue());
            curaEngineCmd.append(" -s ");
            curaEngineCmd.append(arg.getKey());
            curaEngineCmd.append("=");
            curaEngineCmd.append(arg.getValue());
        }

        // Prints arguments
        Base.writeLog(curaEngineCmd.toString(), this.getClass());
        
        // Signals Oracle that GCode generation has started
        Oracle.setTic();
        
        ProcessBuilder pb = new ProcessBuilder(arguments);
        pb.directory(new File(gallery));
        process = null;
        try {
            process = pb.start();
            Base.writeLog("Starting ProcessBuilder", this.getClass());
            ist = new StreamLoggerThread(
                    process.getInputStream()) {
                        @Override
                        protected void logMessage(String line) {
                            emitUpdate(line);
                            Base.writeLog(line, this.getClass());
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
                Base.writeLog("Unrecognized error code returned by CuraEngine.", this.getClass());
                // Throw ToolpathGeneratorException
                return null;
            }
        } catch (IOException ioe) {
            Base.logger.log(Level.SEVERE, ERROR_MESSAGE, ioe);
            Base.writeLog(ERROR_MESSAGE, this.getClass());
            process.destroy();
            ist.stop();
            est.stop();
            // Throw ToolpathGeneratorException
            return null;
        } catch (InterruptedException ex) {
            Base.logger.log(Level.SEVERE, ERROR_MESSAGE, ex);
            Base.writeLog(ERROR_MESSAGE, this.getClass());
            process.destroy();
            ist.stop();
            est.stop();
            // Throw ToolpathGeneratorException
            return null;
        }

        int lastIdx = stlPath.lastIndexOf('.');
        String root = (lastIdx >= 0) ? stlPath.substring(0, lastIdx) : stlPath;

        // Signals Oracle that GCode generation has finished
        Oracle.setToc();

        Base.writeLog("File " + root + ".gcode created with success", this.getClass());
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
         * @param parameter
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
