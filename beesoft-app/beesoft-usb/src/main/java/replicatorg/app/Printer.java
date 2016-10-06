package replicatorg.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
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
    private final PrintPreferences params;
    private final CuraGenerator generator;
    private File gcode;
    private ArrayList<CuraGenerator.CuraEngineOption> options;

    public Printer(PrintPreferences printParams) {
        this.mainWindow = Base.getMainWindow();
        this.bed = mainWindow.getBed();
        this.params = printParams;
        this.options = new ArrayList<>();
        this.generator = new CuraGenerator(params, Base.GCODE2PRINTER_PATH);
    }

    public Printer(PrintPreferences printParams, String targetGCodePath) {
        this.mainWindow = Base.getMainWindow();
        this.bed = mainWindow.getBed();
        this.params = printParams;
        this.options = new ArrayList<>();
        this.generator = new CuraGenerator(params, targetGCodePath);
    }

    /**
     * Generates GCode based on the print parameters chosen.
     *
     * @return true if GCode generation was successful, false otherwise
     */
    public boolean generateGCode() {
        final File stl = generateSTL();
        Base.writeLog("STL generated with success", this.getClass());

        options = getRaftAndSupportParameters();

        for (Object option : options) {
            System.out.println(((CuraGenerator.CuraEngineOption) option).getArgument());
        }

        appendStartAndEndGCode();
        gcode = generator.generateToolpath(stl, options);
        stl.delete();

        if (gcode != null) {
            if (gcode.canRead() && gcode.length() != 0) {
                //replaceLineInFile(gcode.getPath(), "M31 A0");
                Base.writeLog("GCode generated", this.getClass());
            } else {
                Base.getMainWindow().showFeedBackMessage("modelMeshError");
                return false;
            }
        } else {
            // handles with no permission error cancelling print and setting error message
            return false;
        }

        return true;
    }

    private void appendStartAndEndGCode() {
        String[] startCode, endCode;
        StringBuilder codeStringBuilder;

        startCode = Languager.getGCodeArray("startCode");
        endCode = Languager.getGCodeArray("endCode");
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
            Base.writeLog("Error while generating STL", this.getClass());
        }

        return stlFile;
    }

    /**
     * Get generated GCode.
     *
     * @return
     */
    public File getGCode() {
        return gcode;
    }

    /**
     * Calculates number of lines in GCode
     *
     * @return number of lines of GCode.
     */
    public int getGCodeNLines() {
        Base.writeLog("Calculating number of lines", this.getClass());
        int retVal;
        LineNumberReader lnrGCode = null;
        try {
            lnrGCode = new LineNumberReader(new FileReader(gcode));
            lnrGCode.skip(Long.MAX_VALUE);
        } catch (FileNotFoundException ex) {
            Base.writeLog("Can't calculate GCode number of lines; EX:" + ex.getMessage(), this.getClass());
        } catch (IOException ex) {
            Base.writeLog("Can't calculate GCode number of lines; EX:" + ex.getMessage(), this.getClass());
        } catch (NullPointerException ex) {
            Base.writeLog("Can't calculate GCode number of lines; EX:" + ex.getMessage(), this.getClass());
        }

        if (lnrGCode == null) {
            Base.writeLog("lnrGCode variable is null, returning 0 lines", this.getClass());
            return 0;
        } else {
            retVal = lnrGCode.getLineNumber();
            Base.writeLog("Counted " + retVal + " lines", this.getClass());
            return retVal;
        }
    }

    /**
     * Parses parameters based on print parameters.
     *
     * @return list of CuraEngine options to be passed to CuraEngine bin.
     */
    private ArrayList<CuraGenerator.CuraEngineOption> getRaftAndSupportParameters() {

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
            opts.add(new CuraGenerator.CuraEngineOption("raftAirGapLayer0", "220"));
            opts.add(new CuraGenerator.CuraEngineOption("raftLineSpacing", "3000"));
            opts.add(new CuraGenerator.CuraEngineOption("raftBaseLinewidth", "1000"));
            opts.add(new CuraGenerator.CuraEngineOption("raftSurfaceLayers", "2"));
            opts.add(new CuraGenerator.CuraEngineOption("raftSurfaceLinewidth", "400"));
            opts.add(new CuraGenerator.CuraEngineOption("raftInterfaceLinewidth", "400"));
            opts.add(new CuraGenerator.CuraEngineOption("raftInterfaceThickness", "270"));
            opts.add(new CuraGenerator.CuraEngineOption("raftAirGap", "0"));
            opts.add(new CuraGenerator.CuraEngineOption("raftMargin", "5000"));
            opts.add(new CuraGenerator.CuraEngineOption("raftBaseThickness", "300"));
            opts.add(new CuraGenerator.CuraEngineOption("raftSurfaceLineSpacing", "400"));
            opts.add(new CuraGenerator.CuraEngineOption("raftInterfaceLineSpacing", "800"));
            opts.add(new CuraGenerator.CuraEngineOption("raftBaseSpeed", "20"));
            opts.add(new CuraGenerator.CuraEngineOption("raftSurfaceSpeed", "20"));
            opts.add(new CuraGenerator.CuraEngineOption("raftFanSpeed", "0"));
            opts.add(new CuraGenerator.CuraEngineOption("raftSurfaceThickness", "270"));
        } else {
            opts.add(new CuraGenerator.CuraEngineOption("raftBaseThickness", "0"));
            opts.add(new CuraGenerator.CuraEngineOption("raftInterfaceThickness", "0"));
        }

        if (params.isSupportPressed()) {
            opts.add(new CuraGenerator.CuraEngineOption("supportXYDistance", "700"));
            opts.add(new CuraGenerator.CuraEngineOption("supportExtruder", "-1"));
            opts.add(new CuraGenerator.CuraEngineOption("supportType", "1"));
            opts.add(new CuraGenerator.CuraEngineOption("supportZDistance", "150"));
            opts.add(new CuraGenerator.CuraEngineOption("supportEverywhere", "1"));
            opts.add(new CuraGenerator.CuraEngineOption("supportAngle", "60"));
            opts.add(new CuraGenerator.CuraEngineOption("supportLineDistance", "2666"));
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

    public int getFilamentTemperature() {
        return Integer.parseInt(generator.getValue("print_temperature"));
    }

    public boolean isReadyToGenerateGCode() {
        return generator.isReadyToGenerateGCode();
    }
}
