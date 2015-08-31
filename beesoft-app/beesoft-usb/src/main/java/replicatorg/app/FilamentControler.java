package replicatorg.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class FilamentControler {

    public static String NO_FILAMENT = "NO_FILAMENT";
    public static String NO_FILAMENT_CODE = "A000";
    
    /**
     * Get Color Copy from coil code.
     * 
     * @param coilCode spool code.
     * 
     * @return color copy.
     */
    public static String getColor(String coilCode) {

        String color = Languager.getTagValue(1,"CoilColors", coilCode.toLowerCase());
        
        if (color == null) {
            color = NO_FILAMENT;
        }
        
        return color;
    }

    /**
     * Gets an array of the available colors copy.
     * 
     * @return array of colors.
     */
    public static String[] getColors() {
                
        //Gets list of colors
        List<String> colorTags = Languager.getTagList(2, "colors");
        
        String[]colors = new String[colorTags.size()];
        int i=0;
        for (String colorTag : colorTags) {
            colors[i] = Languager.getTagValue(1, "CoilColors", colorTag);
            i++;
        }                                

        return colors;
    }
    
    /**
     * Gets an HashMap of available color codes and their copy.
     * @return array of colors.
     */
    public static Map<String,String> getColorsMap() {
                
        //Gets list of colors
        List<String> colorTags = Languager.getTagList(2, "colors");
        
        HashMap<String, String> colorsMap = new HashMap<String, String>();
        for (String colorTag : colorTags) {
            colorsMap.put(colorTag, Languager.getTagValue(1, "CoilColors", colorTag));
        }                                

        return colorsMap;
    }    
    
    /**
     * Get array of available filament codes
     * 
     * @return array of codes.
     */
    public static String[] getFilamentCodes() {
                
        //Gets list of colors
        List<String> colorTags = Languager.getTagList(2, "colors");
        
        String[] colorCodes = new String[colorTags.size()];
        int i = 0;
        for (String colorTag : colorTags) {
            colorCodes[i] = colorTag.toUpperCase();
            i++;
        }                                
        return colorCodes;
    }    

    /**
     * Get color name and copy based on coil code.
     * @param code coil code.
     * @return color copy and code.
     */
    public static String getFilamentType(String code) {

        if (code != null) {
            return Languager.getTagValue(1,"CoilColors", code);
        }

        return "N/A";
    }

    /**
     * Get color ratio from coil code.
     * @param coilCode coil code.
     * @param resolution resolution
     * @return ratio for each color. 
     */
    public static double getColorRatio(String coilCode, String resolution) {

        double result = 1.00; //Default
        HashMap<String, String> tagValues = Languager.getTagValues(2, coilCode.toLowerCase(), resolution);
        
        if (tagValues != null && !tagValues.isEmpty()) {
            for (Map.Entry pair : tagValues.entrySet()) {
                if (pair.getValue().equals("filament_flow")) {                    
                    return Double.parseDouble((String)pair.getKey()) / 100.0 ;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Get coil code from color name.
     * @param color color name.
     * @return coil code.
     */
    public static String getBEECode(String color) {

        Map<String, String> colorsMap = getColorsMap();
        
        for (Map.Entry pair : colorsMap.entrySet()) {
            String colorName = (String) pair.getValue();
            
            if (colorName.contains(color)) {
                return ((String) pair.getKey()).toUpperCase();
            }
        }        
        // default value is black
        return "A000"; 
    }
}

