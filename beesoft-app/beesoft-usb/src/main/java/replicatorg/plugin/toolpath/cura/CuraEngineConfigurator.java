package replicatorg.plugin.toolpath.cura;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import pt.beeverycreative.beesoft.filaments.PrintPreferences;
import replicatorg.app.Base;
import replicatorg.plugin.toolpath.cura.CuraGenerator.CuraEngineOption;

/**
 *
 * @author rui
 */
public class CuraEngineConfigurator {

    private final Map<String, String> curaIni;
    private final Map<String, String> curaCfg;
    private final String CURA_CONFIG_DIR;
    private final String CURA_INI_PATH;
    private final String DEBUG_FILENAME;

    public CuraEngineConfigurator(PrintPreferences prefs, Map<String, String> paramValues) {
        if (Base.isLinux() || Base.isMacOS()) {
            CURA_CONFIG_DIR = Base.getAppDataDirectory() + "/configs/";
        } else {
            CURA_CONFIG_DIR = Base.getAppDataDirectory() + "\\configs\\";
        }
        String coilText = prefs.getCoilText();
        coilText = coilText.replaceAll("-", "");
        coilText = coilText.trim().replaceAll(" +", " ");
        coilText = coilText.replaceAll(" ", "_");
        DEBUG_FILENAME = prefs.getPrinter().filamentCode() + "-" + coilText + "-" + prefs.getNozzleSize() + "-" + prefs.getResolution();
        CURA_INI_PATH = CURA_CONFIG_DIR + DEBUG_FILENAME + ".ini";
        curaIni = paramValues;
        curaCfg = createCfg();
        createINIFile();
    }

    /**
     * Builds CFG map with default values.
     */
    private Map<String, String> createCfg() {
        Map<String, String> result = new LinkedHashMap<String, String>();
        result.put("initialSpeedupLayers", "4");
        result.put("minimalFeedrate", "10");
        result.put("supportXYDistance", "700");
        result.put("insetXSpeed", "40");
        result.put("retractionZHop", "0");
        result.put("extruderOffset[3].X", "0");
        result.put("extruderOffset[3].Y", "0");
        result.put("retractionSpeed", "125");
        result.put("filamentFlow", "100");
        result.put("infillOverlap", "15");
        result.put("skinSpeed", "40");
        result.put("inset0Speed", "40");
        result.put("coolHeadLift", "0");
        result.put("extrusionWidth", "400");
        result.put("upSkinCount", "10");
        result.put("initialLayerSpeed", "20");
        result.put("minimalLayerTime", "5");
        result.put("infillSpeed", "60");
        result.put("supportExtruder", "-1");
        result.put("fanSpeedMax", "100");
        result.put("supportType", "1");
        result.put("enableCombing", "1");
        result.put("fanSpeedMin", "100");
        result.put("supportZDistance", "150");
        result.put("supportEverywhere", "0");
        result.put("filamentDiameter", "1750");
        result.put("initialLayerThickness", "300");
        result.put("supportAngle", "-1");
        result.put("fanFullOnLayerNr", "2");
        result.put("extruderOffset[1].X", "0");
        result.put("extruderOffset[1].Y", "21600");
        result.put("layerThickness", "100");
        result.put("minimalExtrusionBeforeRetraction", "20");
        result.put("retractionMinimalDistance", "1500");
        result.put("skirtMinLength", "150000");
        result.put("objectSink", "0");
        result.put("retractionAmount", "750");
        result.put("nozzleSize", "400");
        result.put("skirtLineCount", "0");
        result.put("skirtDistance", "3000");
        result.put("extruderOffset[2].Y", "0");
        result.put("extruderOffset[2].X", "0");
        result.put("perimeterBeforeInfill", "0");
        result.put("printSpeed", "40");
        result.put("fixHorrible", "1");
        result.put("layer0extrusionWidth", "400");
        result.put("moveSpeed", "100");
        result.put("supportLineDistance", "2666");
        result.put("retractionAmountExtruderSwitch", "16500");
        result.put("sparseInfillLineDistance", "8000");
        result.put("insetCount", "2");
        result.put("downSkinCount", "10");
        result.put("multiVolumeOverlap", "150");
        return result;
    }
    
    public boolean isReadyToGenerateGCode() {
        return curaIni != null;
    }

    public String getINIValue(String key) {
        return curaIni.get(key);
    }

    /**
     * Maps INI attributes to a CFG file to be passed to CuraEngine.
     *
     * @param prefs Overrides in relation to a possible raft or support choice
     * @return map containing CFG settings
     */
    public Map<String, String> mapIniToCFG(List<CuraEngineOption> prefs) {

        String cfgKey;

        // Calculate cfg parameters using the values present in cfgIni
        for (Map.Entry<String, String> pairs : curaCfg.entrySet()) {
            cfgKey = pairs.getKey();
            curaCfg.put(cfgKey, getCfgValue(cfgKey));
        }

        // overriding, put method replaces old value
        for (CuraEngineOption opt : prefs) {
            curaCfg.put(opt.getParameter(), opt.getValue());
        }

        createCfgFile();
        return curaCfg;
    }

    /**
     * Creates the CFG file (last.cfg) based on the internal curaCfg map. For
     * debugging purposes.
     *
     * @return CFG file
     */
    private void createCfgFile() {

        File cfgFile, cfgDir = new File(CURA_CONFIG_DIR);
        BufferedWriter bw;

        try {
            // If config dir does not exist, create it
            if (!cfgDir.exists()) {
                cfgDir.mkdir();
            }

            // Creates CFG file with the same name as the INI
            cfgFile = new File(CURA_CONFIG_DIR + DEBUG_FILENAME + ".cfg");

            // if file exists, delete it first
            if (cfgFile.exists()) {
                cfgFile.delete();
            }

            bw = new BufferedWriter(new FileWriter(cfgFile));

            for (Entry<String, String> opt : curaCfg.entrySet()) {
                bw.write(opt.getKey() + "=" + opt.getValue() + "\n");
            }
            bw.close();

        } catch (IOException e) {
            Base.writeLog("IOException when creating debug cfg file", this.getClass());
        }
    }

    /**
     * Creates the INI file. For debugging purposes.
     *
     */
    private void createINIFile() {

        File ini;

        if (curaIni == null) {
            Base.writeLog("createINIFile(): curaIni is null", this.getClass());
            return;
        }

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

            bw.write("[profile]");
            bw.write("\n");
            for (Map.Entry<String, String> pairs : curaIni.entrySet()) {
                bw.write(pairs.getKey() + "=" + pairs.getValue());
                bw.write("\n");
            }
            bw.close();
            Base.writeLog("createINIFile(): INI file generated successfully", this.getClass());
        } catch (IOException e) {
            Base.writeLog("IOException on createINIFile()", this.getClass());
        }
    }

    /**
     * Get a cfg value from a cfg key. The value is calculated from the internal
     * cfgIni map.
     *
     * @param cfgKey CFG key
     * @return CFG value
     */
    private String getCfgValue(String cfgKey) {

        try {
            
            if(cfgKey.equals("nozzleSize")) {
                return String.valueOf((int) (Float.parseFloat(curaIni.get("nozzle_size")) * 1000.0));
            }

            //'layerThickness': int(profile.getProfileSettingFloat('layer_height') * 1000.0),
            if (cfgKey.equals("layerThickness")) {

                return String.valueOf((int) (Float.parseFloat(curaIni.get("layer_height")) * 1000.0));
            }

//            if(key.equals("pointsClipDistance"))
//            {
//                return String.valueOf((int) (Float.parseFloat(curaIni.get("clip_distance")) * 1000.0));
//            }
            if (cfgKey.equals("sparseInfillLineDistance")) {

                float density = Float.valueOf(curaIni.get("fill_density"));

                if (density == 0) {
                    return "-1";
                }

                if (density == 100) {
                    return getCfgValue("extrusionWidth");
                } else {
                    float edgeWidth = Float.valueOf(calculateEdgeWidth()) * 100 * 1000;
                    int result = (int) (edgeWidth / density);

                    return String.valueOf(result);
                }

            }

            if (cfgKey.equals("initialLayerThickness")) {
                return getInitialLayerThickness();
            }

            if (cfgKey.equals("filamentDiameter")) {

                return String.valueOf((int) (Float.parseFloat(curaIni.get("filament_diameter")) * 1000.0));
            }
            if (cfgKey.equals("filamentFlow")) {

                return String.valueOf((int) Float.parseFloat(curaIni.get("filament_flow")));
            }

            if (cfgKey.equals("extrusionWidth")) {
                return String.valueOf((int) (Float.parseFloat(calculateEdgeWidth()) * 1000.0));
            }
            if (cfgKey.equals("insetCount")) {
                return calculateLineCount();
            }
            if (cfgKey.equals("downSkinCount")) {
                return calculateSkinCount("solid_bottom");
            }
            if (cfgKey.equals("upSkinCount")) {
                return calculateSkinCount("solid_top");
            }

            if (cfgKey.equals("infillSpeed")) {
                return getSpeedOrDefault("infill_speed");
            }
            if (cfgKey.equals("inset0Speed")) {
                return getSpeedOrDefault("inset0_speed");
            }
            if (cfgKey.equals("insetXSpeed")) {
                return getSpeedOrDefault("insetx_speed");
            }

            if (cfgKey.equals("supportLineDistance")) {
                float sFillRate = Float.valueOf(curaIni.get("support_fill_rate"));
                if (sFillRate > 0) {
                    float eWidth = Float.valueOf(calculateEdgeWidth());
                    return String.valueOf((int) (100 * eWidth * 1000.0 / sFillRate));
                } else {
                    return "-1";
                }
            }

            if (cfgKey.equals("skirtDistance")) {
                return getSkirt("skirtDistance");
            }
            if (cfgKey.equals("skirtLineCount")) {
                return getSkirt("skirtLineCount");
            }
            if (cfgKey.equals("skirtMinLength")) {
                return String.valueOf((int) (Double.parseDouble(curaIni.get("skirt_minimal_length")) * 1000.0));
            }
            //Weird, this value never changes WHY????? default in engine = 4 
            if (cfgKey.equals("initialSpeedupLayers")) {
                return "4";
            }

            if (cfgKey.equals("fanFullOnLayerNr")) {
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
            if (cfgKey.equals("wipeTowerSize")) {
                return "0";
            }

            if (cfgKey.equals("fixHorrible")) {
                return getFixHorrible();
            }

            //always use defaul, GCODE_FLAVOR_REPRAP
            if (cfgKey.equals("gcodeFlavor")) {
                //return "GCODE_FLAVOR_REPRAP";
                return "RepRap (Marlin/Sprinter)";
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
            if (cfgKey.equals("supportType")) {
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
            if (cfgKey.equals("supportAngle")) {
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
            if (cfgKey.equals("supportEverywhere")) {
                if (curaIni.get("support").contains("Everywhere")) {
                    return "1";
                } else {
                    return "0";
                }
            }

            if (cfgKey.equals("supportXYDistance")) {
                return String.valueOf((int) (Double.parseDouble(curaIni.get("support_xy_distance")) * 1000.0));
            }
            if (cfgKey.equals("supportZDistance")) {
                return String.valueOf((int) (Double.parseDouble(curaIni.get("support_z_distance")) * 1000.0));
            }

            if (cfgKey.equals("infillOverlap")) {

                return curaIni.get("fill_overlap");
            }

            if (cfgKey.equals("raftMargin")) {
                return String.valueOf((int) (Double.parseDouble(curaIni.get("raft_margin")) * 1000.0));
            }
            if (cfgKey.equals("raftLineSpacing")) {
                return String.valueOf((int) (Double.parseDouble(curaIni.get("raft_line_spacing")) * 1000.0));
            }
            if (cfgKey.equals("raftBaseThickness")) {
                return String.valueOf((int) (Double.parseDouble(curaIni.get("raft_base_thickness")) * 1000.0));
            }
            if (cfgKey.equals("raftBaseLinewidth")) {
                return String.valueOf((int) (Double.parseDouble(curaIni.get("raft_base_linewidth")) * 1000.0));
            }
            if (cfgKey.equals("raftInterfaceThickness")) {
                return String.valueOf((int) (Double.parseDouble(curaIni.get("raft_interface_thickness")) * 1000.0));
            }
            if (cfgKey.equals("raftInterfaceLinewidth")) {
                return String.valueOf((int) (Double.parseDouble(curaIni.get("raft_interface_linewidth")) * 1000.0));
            }

            if (cfgKey.equals("initialLayerSpeed")) {
                return curaIni.get("bottom_layer_speed");
            }
            if (cfgKey.equals("printSpeed")) {
                return curaIni.get("print_speed");
            }
            if (cfgKey.equals("moveSpeed")) {
                return curaIni.get("travel_speed");
            }
            if (cfgKey.equals("fanSpeedMin")) {
                return curaIni.get("fan_speed");
            }
            if (cfgKey.equals("fanSpeedMax")) {
                return curaIni.get("fan_speed_max");
            }

            if (cfgKey.equals("retractionAmount")) {

                return String.valueOf((int) (Float.parseFloat(curaIni.get("retraction_amount")) * 1000.0));
            }
            if (cfgKey.equals("retractionSpeed")) {

                return String.valueOf((int) (Float.parseFloat(curaIni.get("retraction_speed"))));
            }
            if (cfgKey.equals("retractionMinimalDistance")) {
                return String.valueOf((int) (Float.valueOf(curaIni.get("retraction_min_travel")) * 1000));
            }

            if (cfgKey.equals("retractionAmountExtruderSwitch")) {

                return String.valueOf((int) (Float.parseFloat(curaIni.get("retraction_dual_amount")) * 1000.0));
            }

            if (cfgKey.equals("retractionZHop")) {

                return String.valueOf(Float.parseFloat(curaIni.get("retraction_hop")) * 1000.0);
            }

            if (cfgKey.equals("minimalExtrusionBeforeRetraction")) {

                return String.valueOf((int) (Float.parseFloat(curaIni.get("retraction_minimal_extrusion")) * 1000.0) + 1);
            }

            if (cfgKey.equals("enableCombing")) {
                if (curaIni.get("retraction_combing").contains("True")) {
                    return "1";
                } else if (curaIni.get("retraction_combing").contains("False")) {
                    return "0";
                }
            }

            if (cfgKey.equals("multiVolumeOverlap")) {

                return String.valueOf((int) (Float.parseFloat(curaIni.get("overlap_dual")) * 1000.0));
            }

            if (cfgKey.equals("objectSink")) {

                return String.valueOf(Math.max(0, (int) (Float.parseFloat(curaIni.get("object_sink")) * 1000.0)));
            }

            if (cfgKey.equals("minimalLayerTime")) {
                return curaIni.get("cool_min_layer_time");
            }
            if (cfgKey.equals("minimalFeedrate")) {
                return curaIni.get("cool_min_feedrate");
            }
            if (cfgKey.equals("coolHeadLift")) {
                return curaIni.get("cool_head_lift");
            }

            if (cfgKey.equals("enableOozeShield")) {
                if (curaIni.get("ooze_shield").contains("True")) {
                    return "1";
                }
            }
            if (cfgKey.equals("spiralizeMode")) {
                if (curaIni.get("spiralize").contains("True")) {
                    return "1";
                }
            }
        } catch (Exception e) {
            Base.writeLog("Cura configurator error on translating values: " + e.getMessage(), this.getClass());
            return curaCfg.get(cfgKey) + "# Generated from exception " + e.toString();
        }

        //If not available in INI, uses default
        return curaCfg.get(cfgKey);
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
            return (int) (Math.ceil(Math.max(solidThickness / (layerHeight - 0.0001), 0))) - 1;
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
            fixHorrible = fixHorrible | 0x0001;
        }
        if (curaIni.get("fix_horrible_union_all_type_b").contains("True")) {
            fixHorrible = fixHorrible | 0x0002;
        }
        if (curaIni.get("fix_horrible_use_open_bits").contains("True")) {
            fixHorrible = fixHorrible | 0x0010;
        }
        if (curaIni.get("fix_horrible_extensive_stitching").contains("True")) {
            fixHorrible = fixHorrible | 0x0004;
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
