/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package replicatorg.plugin.toolpath.cura;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import replicatorg.app.Base;

/**
 *
 * @author rui
 */
public class CuraEngineConfigurator {

    private HashMap<String, String> curaIni = new HashMap<String, String>();
    private HashMap<String, String> curaCfg = createCfg();
    private final String ON_ALTERATIONS = "[alterations]";
    private String CURA_CONFIG_DIR = Base.getAppDataDirectory() + "/configs/";
    private String CURA_CONFIG_PATH = "";
    private String CURA_INI_PATH = "";
    private String CFG_FILENAME = "";
    private String INI_FILENAME = "";
    private String CFG_EXTENSION = ".cfg";
    private String INI_EXTENSION = ".ini";
    private final String NO_ATTRIBUTE = "Error: Value not existent";

    public CuraEngineConfigurator() {
        if (Base.isLinux() || Base.isMacOS()) {
            CURA_CONFIG_DIR = Base.getAppDataDirectory() + "/configs/";
        } else {
            CURA_CONFIG_DIR = Base.getAppDataDirectory() + "\\configs\\";
        }

    }

    /**
     * Builds CFG map with default values.
     */
    private HashMap<String, String> createCfg() {
        HashMap<String, String> result = new HashMap<String, String>();

        result.put("layerThickness", "100");
        result.put("initialLayerThickness", "300");
        result.put("filamentDiameter", "2890");
        result.put("filamentFlow", "100");
        result.put("layer0extrusionWidth", "600");
        result.put("extrusionWidth", "400");
        result.put("insetCount", "2");
        result.put("downSkinCount", "6");
        result.put("upSkinCount", "6");
        result.put("sparseInfillLineDistance", "2000"); //100 * extrusionWidth / 20
        result.put("infillOverlap", "15");
        result.put("skirtDistance", "6000");
        result.put("skirtLineCount", "1");
        result.put("skirtMinLength", "0");
        result.put("initialSpeedupLayers", "4");
        result.put("initialLayerSpeed", "20");
        result.put("printSpeed", "50");
        result.put("infillSpeed", "50");
        result.put("inset0Speed", "50");
//        result.put("pointsClipDistance", "440");
        result.put("insetXSpeed", "50");
        result.put("moveSpeed", "150");
        result.put("fanFullOnLayerNr", "2");
        result.put("supportType", "1");
        result.put("supportAngle", "-1");
        result.put("supportEverywhere", "0");
        result.put("supportLineDistance", "2000");
        result.put("supportXYDistance", "700");
        result.put("supportZDistance", "150");
        result.put("supportExtruder", "-1");
        result.put("retractionAmount", "500");
        result.put("retractionAmountPrime", "0");
        result.put("retractionSpeed", "45");
        result.put("retractionAmountExtruderSwitch", "14500");
        result.put("retractionMinimalDistance", "1500");
        result.put("minimalExtrusionBeforeRetraction", "100");
        result.put("retractionZHop", "0");
        result.put("enableCombing", "1");
        result.put("enableOozeShield", "0");
        result.put("wipeTowerSize", "0");
        result.put("multiVolumeOverlap", "0");
        result.put("raftMargin", "5000");
        result.put("raftLineSpacing", "1000.0");
        result.put("raftBaseThickness", "0");
        result.put("raftBaseLinewidth", "0");
        result.put("raftInterfaceThickness", "0");
        result.put("raftInterfaceLinewidth", "0");
        result.put("raftInterfaceLineSpacing", "250");
        result.put("raftAirGap", "0");
        result.put("raftBaseSpeed", "0");
        result.put("raftFanSpeed", "0");
        result.put("raftSurfaceThickness", "0");
        result.put("raftSurfaceLinewidth", "0");
        result.put("raftSurfaceLineSpacing", "0");
        result.put("raftSurfaceLayers", "0");
        result.put("raftSurfaceSpeed", "0");
        result.put("minimalLayerTime", "5");
        result.put("minimalFeedrate", "10");
        result.put("coolHeadLift", "0");
        result.put("fanSpeedMin", "100");
        result.put("fanSpeedMax", "100");
        result.put("fixHorrible", "0");
        result.put("spiralizeMode", "0");
        result.put("gcodeFlavor", "GCODE_FLAVOR_REPRAP");
        result.put("objectSink", "0");
        result.put("posx", "0");
        result.put("posy", "0");

        /**
         * Do not use START and END GCODE It will be printed on PSSW
         *
         * NOTE: This supresses the code
         */
        result.put("startCode", ";startCode");
        result.put("endCode", ";endCode");

//        result.put("cfg_debug", "False");

        return result;
    }

    /**
     * Fulfills a map with a INI file parameters to be overloaded later for each
     * profile choosen,
     *
     * @return map of ini parameters
     */
    private HashMap<String, String> createINI() {
        HashMap<String, String> result = new HashMap<String, String>();

        result.put("layer_height", "0.3");
        result.put("wall_thickness", "1.0");
        result.put("retraction_enable", "True");
        result.put("solid_layer_thickness", "0.9");
        result.put("fill_density", "5");
        result.put("clip_distance", "0.440");
        result.put("nozzle_size", "0.4");
        result.put("print_speed", "60");
        result.put("print_temperature", "220");
        result.put("print_temperature2", "220"); //100 * extrusionWidth / 20
        result.put("print_temperature3", "220");
        result.put("print_temperature4", "220");
        result.put("print_bed_temperature", "70");
        result.put("platform_adhesion", "None");
        result.put("support_dual_extrusion", "Both");
        result.put("wipe_tower", "False");
        result.put("wipe_tower_volume", "15");
        result.put("ooze_shield", "False");
        result.put("filament_diameter", "1.75");
        result.put("filament_diameter2", "0");
        result.put("filament_diameter3", "0");
        result.put("filament_diameter4", "0");
        result.put("filament_flow", "88.0");
        result.put("retraction_speed", "100");
        result.put("retraction_amount", "0.5");
        result.put("retraction_dual_amount", "16.5");
        result.put("retraction_min_travel", "1.0");
        result.put("retraction_combing", "True");
        result.put("retraction_minimal_extrusion", "0.2");
        result.put("retraction_hop", "0.0");
        result.put("bottom_thickness", "0.3");
        result.put("object_sink", "0.0");
        result.put("overlap_dual", "0.15");
        result.put("travel_speed", "100");
        result.put("bottom_layer_speed", "15");
        result.put("infill_speed", "60");
        result.put("inset0_speed", "60");
        result.put("insetx_speed", "60");
        result.put("cool_min_layer_time", "5");
        result.put("fan_enabled", "True");
        result.put("skirt_line_count", "0");
        result.put("skirt_gap", "3.0");
        result.put("skirt_minimal_length", "150.0");
        result.put("fan_full_height", "0.5");
        result.put("fan_speed", "100");
        result.put("fan_speed_max", "100");
        result.put("cool_min_feedrate", "15");
        result.put("cool_head_lift", "False");
        result.put("solid_top", "True");
        result.put("solid_bottom", "True");
        result.put("fill_overlap", "15");
        result.put("support_type", "Lines");
        result.put("support_fill_rate", "10");
        result.put("support_xy_distance", "0.7");
        result.put("support_z_distance", "0.15");
        result.put("spiralize", "False");
        result.put("brim_line_count", "20");
        result.put("raft_margin", "5");
        result.put("raft_line_spacing", "1.0");
        result.put("raft_base_thickness", "0.3");
        result.put("raft_base_linewidth", "0.7");
        result.put("raft_interface_linewidth", "0.2");
        result.put("fix_horrible_union_all_type_a", "True");
        result.put("fix_horrible_union_all_type_b", "False");
        result.put("fix_horrible_use_open_bits", "False");
        result.put("fix_horrible_extensive_stitching", "False");
        result.put("plugin_config", "");
        result.put("object_center_x", "-1");
        result.put("object_center_y", "-1");

        return result;
    }

    /**
     * Read a INI file to a MAP.
     *
     * @param curaIniFile path for INI file.
     * @return operation code.
     */
    public int readIni(File curaIniFile) {
        String line = null;
        int error = 0;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(curaIniFile);

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            boolean onAlterations = false;

            while ((line = bufferedReader.readLine()) != null) {

                // Non-Empty line  
                if (!line.isEmpty()) {
                    // Value set line
                    if (line.contains("=") && !onAlterations) {
                        try {
                            String[] parameters = line.split("=");
                            String attribute = parameters[0].trim();
                            String value = parameters[1].trim();

                            // Fills Map with CURA parameters
                            curaIni.put(attribute, value);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            Base.writeLog("Error parsing INI file. " + NO_ATTRIBUTE + " Key<>Value may be empty");
                            //Do nothing, skip the line
                        }

                    } else if (line.contains(ON_ALTERATIONS)) {
                        onAlterations = true;
                    }
                }
            }
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            Base.writeLog("Unable to open file '" + curaIniFile.getAbsolutePath() + "'");
            error = -1;
        } catch (IOException ex) {
            Base.writeLog("Error reading file '" + curaIniFile.getAbsolutePath() + "'");
            error = -1;
        }
        return error;
    }

    /**
     * Maps INI attributes to a CFG file to be passed to CuraEngine.
     */
    public void mapIniToCFG() {
        Iterator it = curaCfg.entrySet().iterator();

        // Reads CURA CFG Map to map parameter
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String cfgKey = pairs.getKey().toString();
            //(pairs.getKey() + "=" + pairs.getValue());

            curaCfg.put(cfgKey, getTranslatedValue(cfgKey));
        }
    }

    private void printINI() {
        Iterator it = curaIni.entrySet().iterator();

        System.out.println("*****************************************");
        // Reads CURA CFG Map to map parameter
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();

            String cfgKey = pairs.getKey().toString();
            System.out.println(pairs.getKey() + "=" + pairs.getValue());

        }

        System.out.println("*****************************************");
    }

    private void printCFG() {
        Iterator it = curaCfg.entrySet().iterator();

        // Reads CURA CFG Map to map parameter
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();

            String cfgKey = pairs.getKey().toString();
            System.out.println(pairs.getKey() + "=" + pairs.getValue());

        }
    }

    /**
     * Overrides internal INI with the values from the XML and creates new INI
     * file.
     *
     * @param overload_values INI map with the parameters to override
     * @param bCode actual beecode of the BTF
     * @param res choosen resolution for the current print
     * @return path for the INI file created
     */
    public String setupINI(HashMap<String, String> overload_values, String bCode, String res) {
        INI_FILENAME = bCode + "-" + res;
        CURA_INI_PATH = CURA_CONFIG_DIR + INI_FILENAME + INI_EXTENSION;

        HashMap<String, String> ini = createINI();

        Iterator it = overload_values.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            //Replace existing pairs by the new ones.
            // overload_values is a mirrored map with keys and values switched
            ini.put(pairs.getValue().toString(), pairs.getKey().toString());
            it.remove(); // avoids a ConcurrentModificationException
        }

        return createINIFile(ini).getName();
    }

    /**
     * Process existing INI file to map for a CFG file
     *
     * @param profile path for the INI file
     */
    public void processINI(String profile) {
        File f = new File(profile);
        readIni(f);
    }

    /**
     * Maps INI file to CFG file
     *
     * @param profile path for the INI file
     * @return CFG file
     */
    public File dotheWork(File profile) {
        CFG_FILENAME = profile.getName().split(".ini")[0] + CFG_EXTENSION;
        CURA_CONFIG_PATH = CURA_CONFIG_DIR + CFG_FILENAME;
        mapIniToCFG();
        return createCfgFile();
    }

    /**
     * Creates the CFG file based on the internal MAP
     *
     * @return CFG file
     */
    public File createCfgFile() {

        File cfgFile = null;
        try {

            // Creates CFG file with the same name as the INI
            cfgFile = new File(CURA_CONFIG_PATH);
            // CFG dir        
            File config_dir = new File(CURA_CONFIG_DIR);

            // If config dir does not exist, create it
            if (!config_dir.exists()) {
                config_dir.mkdir();
            }
            // if file exists, delete it first
            if (cfgFile.exists()) {
                cfgFile.delete();
            }
//            System.out.println(cfgFile.getAbsolutePath());

            FileWriter fw = new FileWriter(cfgFile.getAbsolutePath());
            BufferedWriter bw = new BufferedWriter(fw);
            Iterator it = curaCfg.entrySet().iterator();

            // Reads CURA CFG Map to write to file
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                bw.write(pairs.getKey() + "=" + pairs.getValue());
                bw.write("\n");
            }

            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return cfgFile;
    }

    /**
     * Creates the INI file based on a MAP.
     *
     * @param iniMap MAP that has all the print parameters to be in the INI file
     * and therefore converted into the CFG file
     * @return INI file
     */
    public File createINIFile(HashMap<String, String> iniMap) {

        File ini = null;
        try {

            // Creates INI file with the same name as the INI
            ini = new File(CURA_INI_PATH);
            // if file exists, delete it first
            if (ini.exists()) {
                ini.delete();
            }

            /**
             * Check if configs dir exists
             */
            File config_dir = new File(CURA_CONFIG_DIR);

            // If config dir does not exist, create it
            if (!config_dir.exists()) {
                config_dir.mkdir();
            }

            FileWriter fw = new FileWriter(ini.getAbsolutePath());
            BufferedWriter bw = new BufferedWriter(fw);
            Iterator it = iniMap.entrySet().iterator();

            bw.write("[profile]");
            bw.write("\n");

            // Reads INI Map to write to file
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                bw.write(pairs.getKey() + "=" + pairs.getValue());
                bw.write("\n");
            }

            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return ini;
    }

    /**
     * Translates INI parameter to CFG parameter
     *
     * @param key CFG value that will be used to search for the INI
     * corresponding value
     * @return INI value from file to override the current match CFG key<>value
     */
    private String getTranslatedValue(String key) {

        try {


            // Ini flag for debug
            if (key.equals("cfg_debug")) {
                printINI();
                return curaIni.get("iniDebug");
            }

            //'layerThickness': int(profile.getProfileSettingFloat('layer_height') * 1000.0),
            if (key.equals("layerThickness")) {

                return String.valueOf((int) (Float.parseFloat(curaIni.get("layer_height")) * 1000.0));
            }

//            if(key.equals("pointsClipDistance"))
//            {
//                return String.valueOf((int) (Float.parseFloat(curaIni.get("clip_distance")) * 1000.0));
//            }

            if (key.equals("sparseInfillLineDistance")) {

                float density = Float.valueOf(curaIni.get("fill_density"));

                if (density == 0) {
                    return "-1";
                }

                if (density == 100) {
                    return getTranslatedValue("extrusionWidth");
                } else {
                    float edgeWidth = Float.valueOf(calculateEdgeWidth()) * 100 * 1000;
                    int result = (int) (edgeWidth / density);

                    return String.valueOf(result);
                }

            }

            if (key.equals("initialLayerThickness")) {
                return getInitialLayerThickness();
            }

            if (key.equals("filamentDiameter")) {

                return String.valueOf((int) (Float.parseFloat(curaIni.get("filament_diameter")) * 1000.0));
            }
            if (key.equals("filamentFlow")) {

                return String.valueOf((int) Float.parseFloat(curaIni.get("filament_flow")));
            }

            if (key.equals("extrusionWidth")) {
                return String.valueOf((int) (Float.parseFloat(calculateEdgeWidth()) * 1000.0));
            }
            if (key.equals("insetCount")) {
                return calculateLineCount();
            }
            if (key.equals("downSkinCount")) {
                return calculateSkinCount("solid_bottom");
            }
            if (key.equals("upSkinCount")) {
                return calculateSkinCount("solid_top");
            }


            if (key.equals("infillSpeed")) {
                return getSpeedOrDefault("infill_speed");
            }
            if (key.equals("inset0Speed")) {
                return getSpeedOrDefault("inset0_speed");
            }
            if (key.equals("insetXSpeed")) {
                return getSpeedOrDefault("insetx_speed");
            }

            if (key.equals("supportLineDistance")) {
                float sFillRate = Float.valueOf(curaIni.get("support_fill_rate"));
                if (sFillRate > 0) {
                    float eWidth = Float.valueOf(calculateEdgeWidth());
                    return String.valueOf((int) (100 * eWidth * 1000.0 / sFillRate));
                } else {
                    return "-1";
                }
            }

            if (key.equals("skirtDistance")) {
                return getSkirt("skirtDistance");
            }
            if (key.equals("skirtLineCount")) {
                return getSkirt("skirtLineCount");
            }
            if (key.equals("skirtMinLength")) {
                return String.valueOf((int) (Double.parseDouble(curaIni.get("skirt_minimal_length")) * 1000.0));
            }
            //Weird, this value never changes WHY????? default in engine = 4 
            if (key.equals("initialSpeedupLayers")) {
                return "4";
            }


            if (key.equals("fanFullOnLayerNr")) {
                float fanFullHeight = Float.valueOf(curaIni.get("fan_full_height")) * 1000;
                float initialLayerThickness = Float.parseFloat(getInitialLayerThickness());
                float layerThickness = Float.parseFloat(curaIni.get("layer_height")) * 1000;

                int fanFull;
                fanFull = (int) Math.ceil((fanFullHeight - initialLayerThickness - 1)
                        / (layerThickness + 1));

                if (fanFull > 0) {
                    return String.valueOf(fanFull);
                } else {
                    return "0";

                }
            }

            //never use wipeTower
            if (key.equals("wipeTowerSize")) {
                return "0";
            }

            if (key.equals("fixHorrible")) {
                return getFixHorrible();
            }

            //always use defaul, GCODE_FLAVOR_REPRAP
            if (key.equals("gcodeFlavor")) {
                return "GCODE_FLAVOR_REPRAP";
            }

            //<not_in_Cura>        
            /**
             * default in engine=0.6mm if
             * (key.equals("layer0extrusionWidth")){return curaIni.get(key);}
             *
             * default in engine= 100 * extrusionWidth / 20 if
             * (key.equals("sparseInfillLineDistance")) {return
             * curaIni.get(key);}
             *
             * //TODO: WHY????? default in engine = 4
             *
             */
            //<\not_in_Cura>    
            /**
             * Cura support_type 0(default) - grid; 1 - lines
             */
            if (key.equals("supportType")) {
                String type = curaIni.get("support_type");
                if (type.toLowerCase().contains("lines")) {
                    return "1";
                } else if (type.toLowerCase().contains("grid")) {
                    return "0";
                } else {
                    return "1";
                }
            }


            /**
             * Support is off by default; angle=-1
             */
            if (key.equals("supportAngle")) {
                if (curaIni.get("support").contains("None")) {
                    return "-1";
                } else {
                    return curaIni.get("support_angle");
                }
            }

            /**
             * Support everywhere is off by default; supportEverywhere=0 (off)
             * supportEverywhere=1 (on)
             */
            if (key.equals("supportEverywhere")) {
                if (curaIni.get("support").contains("Everywhere")) {
                    return "1";
                } else {
                    return "0";
                }
            }

            if (key.equals("supportXYDistance")) {
                return String.valueOf((int) (Double.parseDouble(curaIni.get("support_xy_distance")) * 1000.0));
            }
            if (key.equals("supportZDistance")) {
                return String.valueOf((int) (Double.parseDouble(curaIni.get("support_z_distance")) * 1000.0));
            }

            if (key.equals("infillOverlap")) {

                return curaIni.get("fill_overlap");
            }

            if (key.equals("raftMargin")) {
                return String.valueOf((int) (Double.parseDouble(curaIni.get("raft_margin")) * 1000.0));
            }
            if (key.equals("raftLineSpacing")) {
                return String.valueOf((int) (Double.parseDouble(curaIni.get("raft_line_spacing")) * 1000.0));
            }
            if (key.equals("raftBaseThickness")) {
                return String.valueOf((int) (Double.parseDouble(curaIni.get("raft_base_thickness")) * 1000.0));
            }
            if (key.equals("raftBaseLinewidth")) {
                return String.valueOf((int) (Double.parseDouble(curaIni.get("raft_base_linewidth")) * 1000.0));
            }
            if (key.equals("raftInterfaceThickness")) {
                return String.valueOf((int) (Double.parseDouble(curaIni.get("raft_interface_thickness")) * 1000.0));
            }
            if (key.equals("raftInterfaceLinewidth")) {
                return String.valueOf((int) (Double.parseDouble(curaIni.get("raft_interface_linewidth")) * 1000.0));
            }

            if (key.equals("initialLayerSpeed")) {
                return curaIni.get("bottom_layer_speed");
            }
            if (key.equals("printSpeed")) {
                return curaIni.get("print_speed");
            }
            if (key.equals("moveSpeed")) {
                return curaIni.get("travel_speed");
            }
            if (key.equals("fanSpeedMin")) {
                return curaIni.get("fan_speed");
            }
            if (key.equals("fanSpeedMax")) {
                return curaIni.get("fan_speed_max");
            }

            if (key.equals("retractionAmount")) {

                return String.valueOf((int) (Float.parseFloat(curaIni.get("retraction_amount")) * 1000.0));
            }
            if (key.equals("retractionSpeed")) {

                return String.valueOf((int) (Float.parseFloat(curaIni.get("retraction_speed"))));
            }
            if (key.equals("retractionMinimalDistance")) {
                return String.valueOf((int) (Float.valueOf(curaIni.get("retraction_min_travel")) * 1000));
            }

            if (key.equals("retractionAmountExtruderSwitch")) {

                return String.valueOf((int) (Float.parseFloat(curaIni.get("retraction_dual_amount")) * 1000.0));
            }

            if (key.equals("retractionZHop")) {

                return String.valueOf(Float.parseFloat(curaIni.get("retraction_hop")) * 1000.0);
            }

            if (key.equals("minimalExtrusionBeforeRetraction")) {

                return String.valueOf((int) (Float.parseFloat(curaIni.get("retraction_minimal_extrusion")) * 1000.0));
            }

            if (key.equals("enableCombing")) {
                if (curaIni.get("retraction_combing").contains("True")) {
                    return "1";
                } else if (curaIni.get("retraction_combing").contains("False")) {
                    return "0";
                }
            }

            if (key.equals("multiVolumeOverlap")) {

                return String.valueOf((int) (Float.parseFloat(curaIni.get("overlap_dual")) * 1000.0));
            }


            if (key.equals("objectSink")) {

                return String.valueOf(Math.max(0, (int) (Float.parseFloat(curaIni.get("object_sink")) * 1000.0)));
            }

            if (key.equals("minimalLayerTime")) {
                return curaIni.get("cool_min_layer_time");
            }
            if (key.equals("minimalFeedrate")) {
                return curaIni.get("cool_min_feedrate");
            }
            if (key.equals("coolHeadLift")) {
                return curaIni.get("cool_head_lift");
            }

            if (key.equals("enableOozeShield")) {
                if (curaIni.get("ooze_shield").contains("True")) {
                    return "1";
                }
            }
            if (key.equals("spiralizeMode")) {
                if (curaIni.get("spiralize").contains("True")) {
                    return "1";
                }
            }
        } catch (Exception e) {
            Base.writeLog("Cura configurator error on translating values: " + e.getMessage());
            return curaCfg.get(key) + "# Generated from exception " + e.toString();
        }

        //If not available in INI, uses default
        return curaCfg.get(key);
    }

    private String calculateEdgeWidth() {

        float wallThickness = Float.valueOf(curaIni.get("wall_thickness"));
        float nozzleSize = Float.valueOf(curaIni.get("nozzle_size"));

        if (curaIni.get("spiralize").contains("True")) {
            return curaIni.get("wall_thickness");
        }//no need for else

        if (wallThickness < 0.01) {
            return curaIni.get("wall_thickness");
        }//no need for else


        if (wallThickness < nozzleSize) {
            return curaIni.get("wall_thickness");
        }//no need for else

        int lineCount = (int) (wallThickness / (nozzleSize - 0.0001));

        if (lineCount == 0) {
            return curaIni.get("nozzle_size");
        }//no need for else

        float lineWidth = wallThickness / (float) lineCount;
        float lineWidthAlt = wallThickness / (float) (lineCount + 1);

        if (lineWidth > (nozzleSize * 1.5)) {
            return String.valueOf(lineWidthAlt);
        }//no need for else


        return String.valueOf(lineWidth);
    }

    private String calculateLineCount() {

        float wallThickness = Float.valueOf(curaIni.get("wall_thickness"));
        float nozzleSize = Float.valueOf(curaIni.get("nozzle_size"));

        if (wallThickness < 0.01) {
            return "0";
        }//no need for else

        if (wallThickness < nozzleSize) {
            return "1";
        }//no need for else

        if (curaIni.get("spiralize").contains("True")) {
            return "1";
        }//no need for else

        int lineCount = (int) (wallThickness / (nozzleSize - 0.0001));

        if (lineCount < 1) {
            lineCount = 1;
        }//no need for else

        float lineWidth = wallThickness / lineCount;

        if (lineWidth > nozzleSize * 1.5) {
            return String.valueOf(lineCount + 1);
        }//no need for else

        return String.valueOf(lineCount);
    }

    private String calculateSkinCount(String key) {

        if (curaIni.get(key).contains("True")) {
            return String.valueOf(calculateSolidLayerCount());
        }
        if (curaIni.get("fill_density").equals("100")) {
            return "1000.00";
        } else {
            return "0";
        }

    }

    private int calculateSolidLayerCount() {
        float layerHeight = Float.valueOf(curaIni.get("layer_height"));
        float solidThickness = Float.valueOf(curaIni.get("solid_layer_thickness"));

        if (0.0 > layerHeight && layerHeight < 0.001) {
            return 1;
        } else {
            return (int) (Math.ceil(Math.max(solidThickness / (layerHeight - 0.0001), 0)));
        }

    }

    private String getSpeedOrDefault(String key) {
        if (Float.valueOf(curaIni.get(key)) > 0) {
            return curaIni.get(key);
        } else {
            return curaIni.get("print_speed");
        }
    }

    private String getSkirt(String var) {

        String platform_adhesion = curaIni.get("platform_adhesion");
        int skirtDistance;
        int skirtLineCount;


        if (platform_adhesion.contains("Brim")) {
            skirtDistance = 0;
            skirtLineCount = (int) Double.parseDouble(curaIni.get("brim_line_count"));
        } else if (platform_adhesion.contains("Raft")) {
            skirtDistance = 0;
            skirtLineCount = 0;
        } else {
            skirtDistance = (int) (Double.parseDouble(curaIni.get("skirt_gap")) * 1000);
            skirtLineCount = (int) Double.parseDouble(curaIni.get("skirt_line_count"));
        }

        if (var.contains("skirtDistance")) {
            return String.valueOf(skirtDistance);
        } else if (var.contains("skirtLineCount")) {
            return String.valueOf(skirtLineCount);
        } else {
            return "error:getSkirt(String var): This should never happen";
        }
    }

    private String getInitialLayerThickness() {
        if (Float.parseFloat(curaIni.get("bottom_thickness")) > 0.0) {
            return String.valueOf((int) (Float.parseFloat(curaIni.get("bottom_thickness")) * 1000.0));
        } else {
            return String.valueOf((int) (Float.parseFloat(curaIni.get("layer_height")) * 1000.0));
        }
    }

    private String getFixHorrible() {

        int fixHorrible = 0x0000;

        if (curaIni.get("fix_horrible_union_all_type_a").contains("True")) {
            fixHorrible = fixHorrible & 0x0001;
        }
        if (curaIni.get("fix_horrible_union_all_type_b").contains("True")) {
            fixHorrible = fixHorrible & 0x0002;
        }
        if (curaIni.get("fix_horrible_use_open_bits").contains("True")) {
            fixHorrible = fixHorrible & 0x0010;
        }
        if (curaIni.get("fix_horrible_extensive_stitching").contains("True")) {
            fixHorrible = fixHorrible & 0x0004;
        }
        return String.valueOf(fixHorrible);
    }

    public String getDensity(float density) {

        if (density == 0) {
            return "-1";
        }

        if (density == 100) {
            return String.valueOf((int) (Float.parseFloat(calculateEdgeWidth()) * 1000.0));
        } else {

            float edgeWidth = Float.valueOf(calculateEdgeWidth()) * 100 * 1000;
            int result = (int) (edgeWidth / density);

            return String.valueOf(result);
        }

    }
}
