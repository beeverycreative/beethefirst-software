package replicatorg.app;

import java.util.HashMap;

/**
* Copyright (c) 2013 BEEVC - Electronic Systems
* This file is part of BEESOFT software: you can redistribute it and/or modify 
* it under the terms of the GNU General Public License as published by the 
* Free Software Foundation, either version 3 of the License, or (at your option)
* any later version. BEESOFT is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
* or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
* for more details. You should have received a copy of the GNU General
* Public License along with BEESOFT. If not, see <http://www.gnu.org/licenses/>.
*/
public class DefaultProperties {
    
    private static HashMap<String,String> defaultHash = new HashMap<String, String>();;

    public DefaultProperties() {   
    }
    
    public static void init()
    {
        defaultHash.put("mru_list", String.valueOf(""));
        defaultHash.put("replicatorg.useLogFile", String.valueOf(false));
        defaultHash.put("ui.preferSystemTrayNotifications", String.valueOf(false));
        defaultHash.put("replicatorg.parser.curve_segment_mm", String.valueOf(1.0));
        defaultHash.put("editor.caret.blink",String.valueOf(true));
        defaultHash.put("editor.linehighlight", String.valueOf(true));
        defaultHash.put("editor.brackethighlight", String.valueOf(true));
        defaultHash.put("editor.eolmarkers", String.valueOf(false));
        defaultHash.put("editor.invalid", String.valueOf(false));
        defaultHash.put("replicatorg.editor.antialiasing",String.valueOf(true));
        defaultHash.put("editor.divider.size", String.valueOf(5));
        defaultHash.put("replicatorg.initialopenbehavior",String.valueOf(Base.InitialOpenBehavior.OPEN_LAST.ordinal()));
        defaultHash.put("editor.linehighlight",String.valueOf(true));
        defaultHash.put("build.preheatTool0", String.valueOf(220));      
        defaultHash.put("build.autoGenerateGcode", String.valueOf(true));
        defaultHash.put("build.autoGenerateGcode2", String.valueOf(false));
        defaultHash.put("build.doPreheat", String.valueOf(true));
        defaultHash.put("console.auto_clear", String.valueOf(true));
        defaultHash.put("ui.open_dir", Base.getAppDataDirectory()+"/"+Base.MODELS_FOLDER);
        defaultHash.put("ui.open_dir0", Base.getAppDataDirectory()+"/"+Base.MODELS_FOLDER);
        defaultHash.put("console.auto_clear2", String.valueOf(false));
        defaultHash.put("console.length",String.valueOf(100));
        defaultHash.put("console.lines", String.valueOf(8));     
        defaultHash.put("ui.modelColor",String.valueOf(-19635));
        defaultHash.put("ui.backgroundColor",String.valueOf(0));
        defaultHash.put("replicatorg.parser.curve_segment_mm",String.valueOf(1.0) );      
        defaultHash.put("replicatorg.skeinforge.timeout", String.valueOf(-1));
        defaultHash.put("editor.font","Monospaced,plain,12");
        defaultHash.put("temperature.acceptedLimit", String.valueOf(260));
        defaultHash.put("extruderpanel.extrudetime", String.valueOf("5"));
        defaultHash.put("controlpanel.jogmode", String.valueOf("1"));
        defaultHash.put("extruderpanel.extrudetime", String.valueOf("5"));
        defaultHash.put("build.runSafetyChecks", String.valueOf(true));
        defaultHash.put("build.runSafetyChecks", String.valueOf(true));
        defaultHash.put("machine.showExperimental", String.valueOf(false));
        defaultHash.put("build.monitor_temp", String.valueOf(false));
        defaultHash.put("machinecontroller.simulator", String.valueOf(true));
        defaultHash.put("replicatorg.generator.name", String.valueOf("Skeinforge (50)"));
        defaultHash.put("lastGeneratorProfileSelected", String.valueOf("---"));  
        defaultHash.put("replicatorg.skeinforge.profile", String.valueOf(""));
        defaultHash.put("firstTime", String.valueOf(true));
        defaultHash.put("lastSession_totalExtruded", String.valueOf(0));
        defaultHash.put("filamentCoilRemaining", String.valueOf(105000));
        defaultHash.put("coilCode", String.valueOf("A302"));
        defaultHash.put("totalExtruded", String.valueOf(0));
        defaultHash.put("keyValue", String.valueOf("0.64"));
        defaultHash.put("defaultSceneDir", Base.getAppDataDirectory().toString().concat("/"+Base.MODELS_FOLDER+"/"));
        defaultHash.put("dateLastPrint", "NA");
        defaultHash.put("durationLastPrint", "NA");
        defaultHash.put("maintenance", "0");
        defaultHash.put("lockHeight", String.valueOf(true));
        defaultHash.put("language", "EN");
        defaultHash.put("nCalibrations", String.valueOf(0));
        defaultHash.put("nTotalPrints", String.valueOf(0));
        defaultHash.put("debugMode", String.valueOf(false));
        defaultHash.put("comLog", String.valueOf(false));
        defaultHash.put("flashFirmware", String.valueOf(false));
        defaultHash.put("localPrint", String.valueOf(false));
        defaultHash.put("localPrintFileName", "abcde.gcode");
        defaultHash.put("curaFile", "none");
    }

    public static String getDefault(String value) {
        return defaultHash.get(value);
    }

    public static void setDefault(String key, String value) {
        defaultHash.put(key, value);
    }
    
    
    
}
