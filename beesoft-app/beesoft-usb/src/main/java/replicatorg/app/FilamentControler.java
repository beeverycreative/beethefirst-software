package replicatorg.app;

import java.util.ArrayList;
import java.util.List;

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
    public static String NO_FILAMENT_CODE = "A0";
    
    /**
     * Get Color Copy from coil code.
     * @param coilCode spool code.
     * @return color copy.
     */
    public static String getColor(String coilCode) {

        String color = Languager.getTagValue(1,"CoilColors", coilCode);
        
        if (color == null) {
            color = NO_FILAMENT;
        }
        
        return color;
    }

    /**
     * Get array of available colors and their copy.
     * @return array of colors.
     */
    public static String[] getColors() {
                
        //Gets list of colors
        List<String> colorTags = Languager.getTagList(2, "colors");
        
        List<String> colorLabels = new ArrayList<String>();
        for (String colorTag : colorTags) {
            colorLabels.add(Languager.getTagValue(1, "CoilColors", colorTag));
        }                                

        return ((String []) colorLabels.toArray());
    }

    /**
     * Get color code and copy based on coil code.
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
     * @return ratio for each color. 
     */
    public static double getColorRatio(String coilCode) {

        //A001-TRANSPARENT
        if (coilCode.contains("001")) {
            return 0.91;
        }
        //A002-WHITE
        if (coilCode.contains("002")) {
            return 0.89;
        }
        //A003-BLACK
        if (coilCode.contains("003")) {
            return 0.88;
        }
        //A004-RED
        if (coilCode.contains("004")) {
            return 0.96;
        }
        //A005-YELLOW
        if (coilCode.contains("005")) {
            return 0.96;
        }
        //A006-BLUE
        if (coilCode.contains("006")) {
            return 0.92;
        }
        //A007-BRONZE
        if (coilCode.contains("007")) {
            return 0.88;
        }
        //A008-PINK
        if (coilCode.contains("008")) {
            return 0.90;
        }
        //A009-SILVER
        if (coilCode.contains("009")) {
            return 0.91;
        }
        //A010-TURQUOISE
        if (coilCode.contains("010")) {
            return 0.86;
        }
        //A011-NEON GREEN
        if (coilCode.contains("011")) {
            return 0.86;
        }
        //A012-ORANGE  
        if (coilCode.contains("012")) {
            return 0.88;
        }
      
        if (coilCode.contains("331") || coilCode.contains("301")) {
            return 0.93;
        }
        if (coilCode.contains("332") || coilCode.contains("302")) {
            return 0.90;
        }
        if (coilCode.contains("333") || coilCode.contains("303")) {
            return 0.86;
        }
        if (coilCode.contains("334") || coilCode.contains("304")) {
            return 0.86;
        }
        if (coilCode.contains("335") || coilCode.contains("305")) {
            return 0.90;
        }
        if (coilCode.contains("336") || coilCode.contains("306")) {
            return 0.96;
        }
        if (coilCode.contains("337") || coilCode.contains("321")) {
            return 0.91;
        }
        if (coilCode.contains("338") || coilCode.contains("322")) {
            return 0.98;
        }
        return 0.92;
    }
    
    /**
     * Get coil code from color name.
     * @param color color name.
     * @return coil code.
     */
    public static String getBEECode(String color) {
        if (color.contains("WHITE")) {
            return "A301";
        }
        if (color.contains("BLACK")) {
            return "A302";
        }
        if (color.contains("YELLOW")) {
            return "A303";
        }
        if (color.contains("RED")) {
            return "A304";
        }
        if (color.contains("TURQUOISE")) {
            return "A305";
        }
        if (color.contains("TRANSPARENT")) {
            return "A306";
        }
        if (color.contains("GREEN")) {
            return "A321";
        }
        if (color.contains("ORANGE")) {
            return "A322";
        }
        
        return "A302"; 
    }

    /**
     * Enum with all coils code.
     */
    public enum FilamentCodes {

        A001, // FILLKEMPT - Transparent
        A002, // FILLKEMPT - White
        A003, // FILLKEMPT - Black
        A004, // FILLKEMPT - Red
        A005, // FILLKEMPT - Yellow
        A006, // FILLKEMPT - Blue
        A007, // FILLKEMPT - Bronze
        A008, // FILLKEMPT - Fuchsia
        A009, // FILLKEMPT - Silver
        A010, // FILLKEMPT - Turquoise
        A011, // FILLKEMPT - Neon Green
        A012, // FILLKEMPT - Orange
        
        A301, // WHITE
        A302, // BLACK
        A303, //YELLOW
        A304, //RED
        A305, // VIOLET
        A306, //TRANSPARENT
        A321, // GREEN
        A322,  //ORANGE
//        
//        A331, // WHITE
//        A332, // BLACK
//        A333, //YELLOW
//        A334, //RED
//        A335, // VIOLET
//        A336, //TRANSPARENT
//        A337, // GREEN
//        A338, //ORANGE
    }

}
