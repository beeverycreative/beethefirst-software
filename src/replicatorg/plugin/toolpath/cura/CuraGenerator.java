package replicatorg.plugin.toolpath.cura;

/**
 * Copyright (c) 2013 BEEVC - Electronic Systems
 */
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import replicatorg.app.Base;
import replicatorg.app.Oracle;
import replicatorg.app.PrintEstimator;
import replicatorg.app.ProperDefault;
import replicatorg.app.util.PythonUtils;
import replicatorg.app.util.StreamLoggerThread;
import replicatorg.plugin.toolpath.ToolpathGenerator;

public class CuraGenerator extends ToolpathGenerator {

    private static String CURA_BIN_PATH = Base.getApplicationDirectory().getAbsolutePath().concat("/curaEngine/bin/CuraEngine");
    private static String CURA_CONFIGURATION_File_PATH = Base.getApplicationDirectory().getAbsolutePath().concat("/curaEngine/prefs/");
    private static String ERROR_MESSAGE = "Can't run Cura!";
    private static String BIN_PATH_UNIX = "python";
    private static String BIN_PATH_WINDOWS = "C:\\Python27\\python.exe";
    private static final String gallery = Base.getAppDataDirectory() + "/"+Base.MODELS_FOLDER;
    private CuraEngineConfigurator curaGenerator = new CuraEngineConfigurator();
    boolean configSuccess = false;
    String profile = null;
    List<String> preferences;
    Process process;
    StreamLoggerThread ist;
    StreamLoggerThread est;

    // "skein_engines/skeinforge-0006","sf_profiles");
    public CuraGenerator() {
        preferences = getPreferences();
        
        if(Base.isLinux() || Base.isMacOS())
        {
            CURA_BIN_PATH = Base.getApplicationDirectory().getAbsolutePath().concat("/curaEngine/bin/CuraEngine");
            CURA_CONFIGURATION_File_PATH = Base.getApplicationDirectory().getAbsolutePath().concat("/curaEngine/prefs/");
        }
        else 
        {
            /**
             * Different call depending on windows arch.
             * CuraEngine is compiled in x86 and x64 and it requires different libs
             */
            if(Base.isx86())
            {
                CURA_BIN_PATH = Base.getApplicationDirectory().getAbsolutePath().concat("\\curaEngine\\bin\\x86\\CuraEngine.exe");
            }
            else 
            {
                CURA_BIN_PATH = Base.getApplicationDirectory().getAbsolutePath().concat("\\curaEngine\\bin\\x64\\CuraEngine.exe");
            }
            CURA_CONFIGURATION_File_PATH = Base.getApplicationDirectory().getAbsolutePath().concat("\\curaEngine\\prefs\\");   
        }
    }

    public List<String> getPreferences() {
        return preferences;
    }

    @Override
    public void destroyProcess() {
       if(ist!= null && est != null)
       {
        ist.stop();
        est.stop();
       }
        process.destroy();
    }

    private boolean check() {
        File[] files = new File(gallery).listFiles();

        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().contains(".gcode")) {
                return true;
            }
        }
        return false;
    }
    
    public String getSparseLineDistance(int densityPercentage)
    {
        return curaGenerator.getDensity(densityPercentage);
    }
    
    public void setProfile(String profile2)
    {
        this.profile = CURA_CONFIGURATION_File_PATH + profile2;
    }
    
    public void readINI()
    {
        curaGenerator.processINI(profile);
    }

    @Override
    public File generateToolpath(File stl, List<CuraEngineOption> prefs) {
        
//        System.out.println("File size: "+stl.length());

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
        if (prefs != null && ProperDefault.get("curaFile").contains("none") ) {
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
            // Throw ToolpathGeneratorException
            return null;
        } catch (InterruptedException ex) {
            Base.logger.log(Level.SEVERE, ERROR_MESSAGE, ex);
            Base.writeLog(ERROR_MESSAGE);
            process.destroy();
            // Throw ToolpathGeneratorException
            return null;
        }

        int lastIdx = path.lastIndexOf('.');
        String root = (lastIdx >= 0) ? path.substring(0, lastIdx) : path;

        // Signals Oracle that GCode generation has finished
        Oracle.setToc();
        
        Base.writeLog("File " + root + ".gcode created with success");
        File gcode = new File(gcodePath);
        
        /**
         * Estimates only if scene is different or parameters have changed
         */
        if(Base.getMainWindow().getBed().isSceneDifferent() || !Base.getMainWindow().getBed().isGcodeOK())
        {
            // Estimates print time
            PrintEstimator.estimateTime(gcode);
            Base.getMainWindow().getBed().setEstimatedTime(PrintEstimator.getEstimatedTime());
            Base.getMainWindow().getBed().setSceneDifferent(false);
            Base.getMainWindow().getBed().setGcodeOK(true);
        }
        return gcode;
    }

    @Override
    public File getGeneratedToolpath() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //Cura options to pass on bin
    public static class CuraEngineOption {
        final String parameter;
        final String value;

        public CuraEngineOption(String param, String val) {
            this.parameter = param;
            this.value = val;
        }

        public CuraEngineOption(String parameter) {
            this.parameter = parameter;
            this.value = "";
        }

        public String getParameter() {
            return this.parameter;
        }

        public String getValue() {
            return this.value;
        }

        public String getArgument() {
            return this.parameter + "=" + this.value;
        }
    }
}
