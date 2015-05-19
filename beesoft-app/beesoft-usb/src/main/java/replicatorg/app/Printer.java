package replicatorg.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import replicatorg.app.ui.MainWindow;
import replicatorg.machine.model.MachineModel;
import replicatorg.model.PrintBed;
import replicatorg.plugin.toolpath.cura.CuraGenerator;
import replicatorg.util.Tuple;

/**
 * Copyright (c) 2013 BEEVC - Electronic Systems This file is part of BEESOFT
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. BEESOFT is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with
 * BEESOFT. If not, see <http://www.gnu.org/licenses/>.
 */
public class Printer {

    private MainWindow mainWindow;
    private PrintBed bed;
    private File gcode;
    private ArrayList<String> params;
    private ArrayList<CuraGenerator.CuraEngineOption> options;
    private File stl;
    private CuraGenerator generator;
    private boolean isAutonomous;

    public Printer(ArrayList<String> printParams) {
        this.mainWindow = Base.getMainWindow();
        this.bed = mainWindow.getBed();
        this.gcode = null;
        this.params = printParams;
        this.options = new ArrayList<CuraGenerator.CuraEngineOption>();
        generator = new CuraGenerator();
    }

    /**
     * Generates GCode based on the print parameters choosen.
     *
     * @param params parameters list
     * @return estimated time for gcode generated or error code
     */
    public String generateGCode(ArrayList<String> params) {

        stl = generateSTL();
        Base.writeLog("STL generated with success");
        // params.get(0) - Profile name
        // params.get(1) - Color ratio
        // params.get(2) - infillRatio
        // params.get(3) - Raft:T/F
        // params.get(4) - Support:T/F

        String profile = "";

        profile = parseProfile(params);
        String ini_file = generator.preparePrint(profile);
        generator.setProfile(ini_file);
        generator.readINI();
        options = parseParameters(params);

        gcode = runToolpathGenerator(mainWindow, options);

        // Estimate print duration
//        PrintEstimator.estimateTime(gcode);

        if (gcode != null && gcode.canRead() && gcode.length() != 0) {
            Base.writeLog("GCode generated");
//            parseGCodeToSave();
        } else {
            if (!gcode.canRead() || gcode.length() == 0) {
                // handles with mesh error
                Base.getMainWindow().showFeedBackMessage("modelMeshError");
                return "-2";
            } else {
                // handles with no permission error cancelling print and setting error message
                return "-1";
            }
        }
        return PrintEstimator.getEstimatedTime();
    }

    /**
     * Destroy gcode generation
     */
    public void endGCodeGeneration() {
        if (generator != null) {
            generator.destroyProcess();
        }
    }

    /**
     * Set gcode file if already generated.
     * @param gFile gcode file.
     */
    public void setGCodeFile(File gFile) {
        this.gcode = gFile;
    }

    /**
     * Set print parameters.
     * @param prefs print parameters.
     */
    public void setPreferences(ArrayList<String> prefs) {
        this.params = prefs;
    }

    /**
     * Generate STL from Scene loaded.
     *
     * @return path for STL file
     */
    private File generateSTL() {

        File stl = new File(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/" + bed.getPrintBedFile().getName().split(".bee")[0]
                + System.currentTimeMillis() + ".stl");
        HashMap<Integer, File> stlFiles = new HashMap<Integer, File>();

        boolean oneSTL = (bed.getNumberModels() == 1);

        for (int i = 0; i < bed.getNumberModels(); i++) {
            stlFiles.put(i, PrintBed.toFile(bed.getModel(i).getStream(), "temp" + i + ".stl"));
        }

        FileWriter output = null;
        try {
            output = new FileWriter(stl);
        } catch (IOException ex) {
            Logger.getLogger(Printer.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            for (int i = 0; i < stlFiles.size(); i++) {
                BufferedReader objBufferedReader = new BufferedReader(new FileReader(stlFiles.get(i)));

                boolean lastFile = (i == stlFiles.size() - 1);

                String line;
                while ((line = objBufferedReader.readLine()) != null) {

                    /**
                     * Just one model on bed
                     */
                    if (oneSTL) {
                        output.write(line);
                        output.write("\n");
                    } else {
                        /**
                         * First file must not have endsolid Default tag
                         */
                        if ((i == 0 && !line.toLowerCase().contains("endsolid Default"))) {
                            output.write(line);
                            output.write("\n");
                        }
                        /**
                         * Other files excluding last one must not have any tag.
                         * else if: last file has closes the file with the tag
                         */
                        if (((i > 0) && (!line.toLowerCase().contains("solid Default") || !line.toLowerCase().contains("endsolid Default")))) {
                            output.write(line);
                            output.write("\n");
                        } else if (lastFile && line.toLowerCase().contains("endsolid Default")) {
                            output.write(line);
                            output.write("\n");
                        }
                    }

                }
                objBufferedReader.close();
            }
            output.close();
        } catch (Exception e) {
            Base.writeLog("Error while generating STL");
        }

        return stl;
    }

    /**
     * Run GCode generator.
     *
     * @param mw MainWindow object
     * @param options CuraEngine options
     * @return path for gcode file
     */
    private File runToolpathGenerator(MainWindow mw, ArrayList<CuraGenerator.CuraEngineOption> options) {

        return generator.generateToolpath(stl, options);
    }

    /**
     * Get generated GCode.
     *
     * @return
     */
    public File getGCode() {
        //Save GCode if we are not printing locally
//        if (!Boolean.valueOf(ProperDefault.get("localPrint"))) {
//            parseGCodeToSave();
//        }
        return gcode;

    }

    /**
     * Calculates number of lines in GCode
     *
     * @return number of lines of GCode.
     */
    public int getGCodeNLines() {
        LineNumberReader lnrGCode = null;
        try {
            lnrGCode = new LineNumberReader(new FileReader(gcode));
            lnrGCode.skip(Long.MAX_VALUE);
        } catch (FileNotFoundException ex) {
            Base.writeLog("Cant calculate GCode Number of lines; EX:" + ex.getMessage());
        } catch (IOException ex) {
            Base.writeLog("Cant calculate GCode Number of lines; EX:" + ex.getMessage());
        } catch (NullPointerException ex) {
            Base.writeLog("Cant calculate GCode Number of lines; EX:" + ex.getMessage());
        }

        if (lnrGCode == null) {
            return 0;
        } else {
            return lnrGCode.getLineNumber();
        }
    }

    /**
     * Parses profile from parameters to determine the profile type.
     *
     * @param params print parameters.
     * @return profile type
     */
    private String parseProfile(ArrayList<String> params) {
        String profile = "";
        MachineModel machine = Base.getMainWindow().getMachine().getModel();
        String color = params.get(1);
        String resolution = params.get(0);
        String curaFileForced = ProperDefault.get("curaFile");

        if (curaFileForced.contains("none")) {

            if (machine.getCoilCode().equalsIgnoreCase(FilamentControler.NO_FILAMENT_CODE)) {
                profile += FilamentControler.getBEECode(FilamentControler.NO_FILAMENT_CODE).toLowerCase(); //To allow base estimation without filament
            } else {
                profile += machine.getCoilCode().toLowerCase();
            }

            //Delimiter
            profile += ":";

            /**
             * Resolution
             */
            if (resolution.equalsIgnoreCase("low")) {
                profile += "lowRes";
            } else if (resolution.equalsIgnoreCase("medium")) {
                profile += "mediumRes";
            } else if (resolution.equalsIgnoreCase("high")) {
                profile += "highRes";
            } else if (resolution.equalsIgnoreCase("shigh")) {
                profile += "shighRes";
            }

        } else {
            profile = curaFileForced;
        }
        return profile;
    }

    /**
     * Parses parameters based on print parameters.
     *
     * @param params print parameters.
     * @return list of CuraEngine options to be passed to CuraEngine bin.
     */
    private ArrayList<CuraGenerator.CuraEngineOption> parseParameters(ArrayList<String> params) {

        ArrayList<CuraGenerator.CuraEngineOption> options = new ArrayList<CuraGenerator.CuraEngineOption>();
        String colorRatio = params.get(1);
        String infill = params.get(2);
        String raft = params.get(3);
        String support = params.get(4);
        isAutonomous = params.get(5).equalsIgnoreCase("true");

        /**
         * Density 
        */
        options.add(new CuraGenerator.CuraEngineOption("sparseInfillLineDistance", generator.getSparseLineDistance(Integer.valueOf(infill))));

        /**
         * Raft and Support
         */
        if (raft.equalsIgnoreCase("true")) {
            options.add(new CuraGenerator.CuraEngineOption("raftMargin", "1500"));
            options.add(new CuraGenerator.CuraEngineOption("raftLineSpacing", "1000"));
            options.add(new CuraGenerator.CuraEngineOption("raftBaseThickness", "300"));
            options.add(new CuraGenerator.CuraEngineOption("raftBaseLinewidth", "700"));
            options.add(new CuraGenerator.CuraEngineOption("raftInterfaceThickness", "200"));
            options.add(new CuraGenerator.CuraEngineOption("raftInterfaceLinewidth", "400"));
        } else {
            options.add(new CuraGenerator.CuraEngineOption("raftBaseThickness", "0"));
            options.add(new CuraGenerator.CuraEngineOption("raftInterfaceThickness", "0"));
        }

        if (support.equalsIgnoreCase("true")) {
            options.add(new CuraGenerator.CuraEngineOption("supportAngle", "20"));
            options.add(new CuraGenerator.CuraEngineOption("supportEverywhere", "1"));
        } else {
            options.add(new CuraGenerator.CuraEngineOption("supportAngle", "-1"));
            options.add(new CuraGenerator.CuraEngineOption("supportEverywhere", "0"));
        }

        //Find bed center                   
        PrintBed bed = Base.getMainWindow().getBed();
        Tuple center = bed.getBedCenter();
        options.add(new CuraGenerator.CuraEngineOption("posx", center.x.toString()));
        options.add(new CuraGenerator.CuraEngineOption("posY", center.y.toString()));

        //Sets polygons resolution for type of print: Autonomy or via USB
        String polygonL1Resolution = "";
        String polygonL2Resolution = "";

        if (isAutonomous) {
            polygonL1Resolution = "125";
            polygonL2Resolution = "2500";
        } else {
            polygonL1Resolution = "500";
            polygonL2Resolution = "2500";
        }
        options.add(new CuraGenerator.CuraEngineOption("polygonL1Resolution", polygonL1Resolution));
        options.add(new CuraGenerator.CuraEngineOption("polygonL2Resolution", polygonL2Resolution));

        return options;
    }
}


//    private void parseGCodeToSave() {
//        StringBuffer code = new StringBuffer();
//        try {
//
//            BufferedReader br = new BufferedReader(new FileReader(gcode));
//            String line = "";
//            while ((line = br.readLine()) != null) {
//                code.append(line);
//                code.append(Base.GCODE_DELIMITER);
//            }
//            Base.getMainWindow().getBed().setGcode(code);
//            br.close();
//        } catch (Exception e) {
//            Base.writeLog("Error saving GCode to scene " + bed.getPrintBedFile() + " . Exception: " + e.getMessage());
//        }
//
//        saveGCode();
//    }
//
//    private void saveGCode() {
//        ObjectOutputStream oos;
//
//        try {
//            oos = new ObjectOutputStream(new FileOutputStream(bed.getPrintBedFile()));
//            oos.writeObject(bed);
//            oos.close();
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }