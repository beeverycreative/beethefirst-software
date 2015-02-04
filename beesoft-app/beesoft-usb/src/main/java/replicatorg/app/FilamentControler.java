package replicatorg.app;

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

    public static String getColor(String coilCode) {
        String color = "NO_FILAMENT";

        if (coilCode.contains("331") || coilCode.contains("301")) {
            color = Languager.getTagValue("CoilColors", "WHITE");
        }
        if (coilCode.contains("332") || coilCode.contains("302")) {
            color = Languager.getTagValue("CoilColors", "BLACK");
        }
        if (coilCode.contains("333") || coilCode.contains("303")) {
            color = Languager.getTagValue("CoilColors", "YELLOW");
        }
        if (coilCode.contains("334")|| coilCode.contains("304")) {
            color = Languager.getTagValue("CoilColors", "RED");
        }
        if (coilCode.contains("335") || coilCode.contains("305")) {
            color = Languager.getTagValue("CoilColors", "TURQUOISE");
        }
        if (coilCode.contains("336") || coilCode.contains("306")) {
            color = Languager.getTagValue("CoilColors", "TRANSPARENT");
        }
        if (coilCode.contains("337") || coilCode.contains("321")) {
            color = Languager.getTagValue("CoilColors", "GREEN");
        }
        if (coilCode.contains("338") || coilCode.contains("322")) {
            color = Languager.getTagValue("CoilColors", "ORANGE");
        }

        return color;
    }

    public static String[] getColors() {

        String[] colors = {
            FilamentCodes.A301 + " - " + Languager.getTagValue("CoilColors", "WHITE"),
            FilamentCodes.A302 + " - " + Languager.getTagValue("CoilColors", "BLACK"),
            FilamentCodes.A303 + " - " + Languager.getTagValue("CoilColors", "YELLOW"),
            FilamentCodes.A304 + " - " + Languager.getTagValue("CoilColors", "RED"),
            FilamentCodes.A305 + " - " + Languager.getTagValue("CoilColors", "TURQUOISE"),
            FilamentCodes.A306 + " - " + Languager.getTagValue("CoilColors", "TRANSPARENT"),
            FilamentCodes.A321 + " - " + Languager.getTagValue("CoilColors", "GREEN"),
            FilamentCodes.A322 + " - " + Languager.getTagValue("CoilColors", "ORANGE"),          
//            FilamentCodes.A331 + " - " + Languager.getTagValue("CoilColors", "WHITE"),
//            FilamentCodes.A332 + " - " + Languager.getTagValue("CoilColors", "BLACK"),
//            FilamentCodes.A333 + " - " + Languager.getTagValue("CoilColors", "YELLOW"),
//            FilamentCodes.A334 + " - " + Languager.getTagValue("CoilColors", "RED"),
//            FilamentCodes.A335 + " - " + Languager.getTagValue("CoilColors", "TURQUOISE"),
//            FilamentCodes.A336 + " - " + Languager.getTagValue("CoilColors", "TRANSPARENT"),
//            FilamentCodes.A337 + " - " + Languager.getTagValue("CoilColors", "GREEN"),
//            FilamentCodes.A338 + " - " + Languager.getTagValue("CoilColors", "ORANGE")
        };

        return colors;
    }

    public static String getFilamentType(String code) {
        if (code.contains("331")) {
            return FilamentCodes.A331 + " - " + Languager.getTagValue("CoilColors", "WHITE");
        }
        if (code.contains("332")) {
            return FilamentCodes.A332 + " - " + Languager.getTagValue("CoilColors", "BLACK");
        }
        if (code.contains("333")) {
            return FilamentCodes.A333 + " - " + Languager.getTagValue("CoilColors", "YELLOW");
        }
        if (code.contains("334")) {
            return FilamentCodes.A334 + " - " + Languager.getTagValue("CoilColors", "RED");
        }
        if (code.contains("335")) {
            return FilamentCodes.A335 + " - " + Languager.getTagValue("CoilColors", "TURQUOISE");
        }
        if (code.contains("336")) {
            return FilamentCodes.A336 + " - " + Languager.getTagValue("CoilColors", "TRANSPARENT");
        }
        if (code.contains("337")) {
            return FilamentCodes.A337 + " - " + Languager.getTagValue("CoilColors", "GREEN");
        }
        if (code.contains("338")) {
            return FilamentCodes.A338 + " - " + Languager.getTagValue("CoilColors", "ORANGE");
        }
        
        //OLD
        
        if (code.contains("301")) {
            return FilamentCodes.A301 + " - " + Languager.getTagValue("CoilColors", "WHITE");
        }
        if (code.contains("302")) {
            return FilamentCodes.A302 + " - " + Languager.getTagValue("CoilColors", "BLACK");
        }
        if (code.contains("303")) {
            return FilamentCodes.A303 + " - " + Languager.getTagValue("CoilColors", "YELLOW");
        }
        if (code.contains("304")) {
            return FilamentCodes.A304 + " - " + Languager.getTagValue("CoilColors", "RED");
        }
        if (code.contains("305")) {
            return FilamentCodes.A305 + " - " + Languager.getTagValue("CoilColors", "TURQUOISE");
        }
        if (code.contains("306")) {
            return FilamentCodes.A306 + " - " + Languager.getTagValue("CoilColors", "TRANSPARENT");
        }
        if (code.contains("321")) {
            return FilamentCodes.A321 + " - " + Languager.getTagValue("CoilColors", "GREEN");
        }
        if (code.contains("322")) {
            return FilamentCodes.A322 + " - " + Languager.getTagValue("CoilColors", "ORANGE");
        }

        return "N/A";
    }

    public static double getColorRatio(String coilCode) {
        
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

    public enum FilamentCodes {

        A301, // WHITE
        A302, // BLACK
        A303, //YELLOW
        A304, //RED
        A305, // VIOLET
        A306, //TRANSPARENT
        A321, // GREEN
        A322,  //ORANGE
        A331, // WHITE
        A332, // BLACK
        A333, //YELLOW
        A334, //RED
        A335, // VIOLET
        A336, //TRANSPARENT
        A337, // GREEN
        A338  //ORANGE
    }

//    public enum ColorsSupported {
//
//        BLACK,
//        GREEN,
//        ORANGE,
//        RED,
//        TURQUOISE,
//        TRANSPARENT,
//        WHITE,
//        YELLOW
//    };
}
