package replicatorg.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.beeverycreative.beesoft.filaments.PrintPreferences;
import replicatorg.app.ui.MainWindow;
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

    private final MainWindow mainWindow;
    private final PrintBed bed;
    private File gcode;
    private final PrintPreferences params;
    private ArrayList<CuraGenerator.CuraEngineOption> options;
    private File stl;
    private final CuraGenerator generator;
    private final boolean printPrepared;

    public Printer(PrintPreferences printParams) {
        this.mainWindow = Base.getMainWindow();
        this.bed = mainWindow.getBed();
        this.gcode = null;
        this.params = printParams;
        this.options = new ArrayList<CuraGenerator.CuraEngineOption>();
        generator = new CuraGenerator(params);

        if (params != null) {
            printPrepared = generator.preparePrint();
        } else {
            printPrepared = false;
        }
    }

    /**
     * Generates GCode based on the print parameters choosen.
     *
     * @return estimated time for gcode generated or error code
     */
    public String generateGCode() {

        stl = generateSTL();
        Base.writeLog("STL generated with success");

        options = parseParameters();
        appendStartAndEndGCode();
        gcode = runToolpathGenerator(mainWindow, options);
        replaceLineInFile(gcode.getPath(), "M31 A0 L0");

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

    private void replaceLineInFile(String pathString, String textToReplace) {

        String m31String;
        File fileToRead = new File(pathString);
        File fileToWrite = new File(pathString + "_temp");

        m31String = "M31 A" + PrintEstimator.getEstimatedMinutes()
                + " L" + getGCodeNLines();

        try {
            Reader reader = new InputStreamReader(
                    new FileInputStream(fileToRead), "UTF-8");
            BufferedReader fin = new BufferedReader(reader);
            Writer writer = new OutputStreamWriter(
                    new FileOutputStream(fileToWrite), "UTF-8");
            BufferedWriter fout = new BufferedWriter(writer);
            String s;
            while ((s = fin.readLine()) != null) {
                String replaced = s.replaceAll(textToReplace, m31String);
                fout.write(replaced);
                fout.newLine();
            }

            //Remember to call close. 
            //calling close on a BufferedReader/BufferedWriter 
            // will automatically call close on its underlying stream 
            fin.close();
            fout.close();

            // Moves the new temp file to the original one
            if (fileToRead.delete()) {
                fileToWrite.renameTo(new File(pathString));
            }

        } catch (IOException e) {
            Logger.getLogger(Printer.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void appendStartAndEndGCode() {
        String[] startCode, endCode;
        StringBuilder codeStringBuilder;

        startCode = Languager.getGCodeArray(4, "operationCode", "startCode");
        endCode = Languager.getGCodeArray(4, "operationCode", "endCode");
        codeStringBuilder = new StringBuilder();

        codeStringBuilder.append(";startGCode");
        codeStringBuilder.append(System.getProperty("line.separator"));
        for (String code : startCode) {

            if (code.contains("M109")) {
                code = "M109 S" + getFilamentTemperature();
            } else if (code.contains("M642 W")) {
                float filamentFlow = Float.parseFloat(
                        generator.getValue("filament_flow")
                ) / 100;
                filamentFlow = Float.parseFloat(
                        String.format(Locale.US, "%.3f", filamentFlow)
                );
                code = "M642 W" + filamentFlow;
            }

            codeStringBuilder.append(code.trim());
            codeStringBuilder.append(System.getProperty("line.separator"));

        }

        options.add(new CuraGenerator.CuraEngineOption("startCode", codeStringBuilder.toString()));

        codeStringBuilder = new StringBuilder();

        codeStringBuilder.append(";endGCode");
        codeStringBuilder.append(System.getProperty("line.separator"));
        for (String code : endCode) {
            codeStringBuilder.append(code.trim());
            codeStringBuilder.append(System.getProperty("line.separator"));
        }

        options.add(new CuraGenerator.CuraEngineOption("endCode", codeStringBuilder.toString()));
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
     *
     * @param gFile gcode file.
     */
    public void setGCodeFile(File gFile) {
        this.gcode = gFile;
    }

    /**
     * Generate STL from Scene loaded.
     *
     * @return path for STL file
     */
    private File generateSTL() {

        File stlFile = new File(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/" + bed.getPrintBedFile().getName().split(".bee")[0]
                + System.currentTimeMillis() + ".stl");
        HashMap<Integer, File> stlFiles = new HashMap<Integer, File>();

        boolean oneSTL = (bed.getNumberModels() == 1);

        for (int i = 0; i < bed.getNumberModels(); i++) {
            stlFiles.put(i, PrintBed.toFile(bed.getModel(i).getStream(), "temp" + i + ".stl"));
        }

        FileWriter output = null;
        try {
            output = new FileWriter(stlFile);
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

        return stlFile;
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
     * Parses parameters based on print parameters.
     *
     * @return list of CuraEngine options to be passed to CuraEngine bin.
     */
    private ArrayList<CuraGenerator.CuraEngineOption> parseParameters() {

        ArrayList<CuraGenerator.CuraEngineOption> opts;
        opts = new ArrayList<CuraGenerator.CuraEngineOption>();

        /**
         * Density
         */
        opts.add(new CuraGenerator.CuraEngineOption("sparseInfillLineDistance",
                generator.getSparseLineDistance(params.getDensity())));

        /**
         * Raft and Support
         */
        if (params.isRaftPressed()) {
            opts.add(new CuraGenerator.CuraEngineOption("raftMargin", "1500"));
            opts.add(new CuraGenerator.CuraEngineOption("raftLineSpacing", "1000"));
            opts.add(new CuraGenerator.CuraEngineOption("raftBaseThickness", "300"));
            opts.add(new CuraGenerator.CuraEngineOption("raftBaseLinewidth", "700"));
            opts.add(new CuraGenerator.CuraEngineOption("raftInterfaceThickness", "200"));
            opts.add(new CuraGenerator.CuraEngineOption("raftInterfaceLinewidth", "400"));
        } else {
            opts.add(new CuraGenerator.CuraEngineOption("raftBaseThickness", "0"));
            opts.add(new CuraGenerator.CuraEngineOption("raftInterfaceThickness", "0"));
        }

        if (params.isSupportPressed()) {
            opts.add(new CuraGenerator.CuraEngineOption("supportAngle", "20"));
            opts.add(new CuraGenerator.CuraEngineOption("supportEverywhere", "1"));
        } else {
            opts.add(new CuraGenerator.CuraEngineOption("supportAngle", "-1"));
            opts.add(new CuraGenerator.CuraEngineOption("supportEverywhere", "0"));
        }

        //Find bed center                   
        PrintBed ptrBed = Base.getMainWindow().getBed();
        Tuple center = ptrBed.getBedCenter();
        opts.add(new CuraGenerator.CuraEngineOption("posx", center.x.toString()));
        opts.add(new CuraGenerator.CuraEngineOption("posY", center.y.toString()));

        opts.add(new CuraGenerator.CuraEngineOption("polygonL1Resolution", "125"));
        opts.add(new CuraGenerator.CuraEngineOption("polygonL2Resolution", "2500"));

        return opts;
    }

    public double getFilamentTemperature() {
        if (printPrepared) {
            return Double.parseDouble(generator.getValue("print_temperature"));
        } else {
            return -1;
        }
    }
}
