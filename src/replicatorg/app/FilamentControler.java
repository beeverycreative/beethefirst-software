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

    public static String NO_FILAMENT = "NO_FILAMENT";
    public static String NO_FILAMENT_CODE = "A0";
    
    public static String getColor(String coilCode) {
        String color = NO_FILAMENT;
        if (coilCode.contains("336")
                || coilCode.contains("306")
                || coilCode.contains("001")) {
            color = Languager.getTagValue(1,"CoilColors", "TRANSPARENT");
        }
        if (coilCode.contains("331")
                || coilCode.contains("301")
                || coilCode.contains("002")) {
            color = Languager.getTagValue(1,"CoilColors", "WHITE");
        }
        if (coilCode.contains("332")
                || coilCode.contains("302")
                || coilCode.contains("003")) {
            color = Languager.getTagValue(1,"CoilColors", "BLACK");
        }
        if (coilCode.contains("334")
                || coilCode.contains("304")
                || coilCode.contains("004")) {
            color = Languager.getTagValue(1,"CoilColors", "RED");
        }
        if (coilCode.contains("333")
                || coilCode.contains("303")
                || coilCode.contains("005")) {
            color = Languager.getTagValue(1,"CoilColors", "YELLOW");
        }
        if (coilCode.contains("006")) {
            color = Languager.getTagValue(1,"CoilColors", "BLUE");
        }
        if (coilCode.contains("007")) {
            color = Languager.getTagValue(1,"CoilColors", "OLIVE");
        }
        if (coilCode.contains("008")) {//Fuchsia
            color = Languager.getTagValue(1,"CoilColors", "PINK");
        }
        if (coilCode.contains("009")) {
            color = Languager.getTagValue(1,"CoilColors", "SILVER");
        }
        if (coilCode.contains("335")
                || coilCode.contains("305")
                || coilCode.contains("010")) {
            color = Languager.getTagValue(1,"CoilColors", "TURQUOISE");
        }
        if (coilCode.contains("337")
                || coilCode.contains("321")
                || coilCode.contains("011")) {
            color = Languager.getTagValue(1,"CoilColors", "GREEN");
        }
        if (coilCode.contains("338")
                || coilCode.contains("322")
                || coilCode.contains("012")) {
            color = Languager.getTagValue(1,"CoilColors", "ORANGE");
        }

        return color;
    }

    public static String[] getColors() {

        String[] colors = {
            
            Languager.getTagValue(1,"CoilColors", "BLACK"),
            Languager.getTagValue(1,"CoilColors", "BLACK") + " - " + FilamentCodes.A003,
            Languager.getTagValue(1,"CoilColors", "BLUE"),
            Languager.getTagValue(1,"CoilColors", "BLUE") + " - " + FilamentCodes.A006,
            Languager.getTagValue(1,"CoilColors", "GREEN"),
            Languager.getTagValue(1,"CoilColors", "GREEN") + " - " + FilamentCodes.A011,
            Languager.getTagValue(1,"CoilColors", "OLIVE"),
            Languager.getTagValue(1,"CoilColors", "OLIVE") + " - " + FilamentCodes.A007,
            Languager.getTagValue(1,"CoilColors", "ORANGE"),
            Languager.getTagValue(1,"CoilColors", "ORANGE") + " - " + FilamentCodes.A012,
            Languager.getTagValue(1,"CoilColors", "PINK") + " - " + FilamentCodes.A008,
            Languager.getTagValue(1,"CoilColors", "RED"),
            Languager.getTagValue(1,"CoilColors", "RED") + " - " + FilamentCodes.A004,
            Languager.getTagValue(1,"CoilColors", "SILVER") + " - " + FilamentCodes.A009,
            Languager.getTagValue(1,"CoilColors", "TRANSPARENT"),
            Languager.getTagValue(1,"CoilColors", "TRANSPARENT") + " - " + FilamentCodes.A001,
            Languager.getTagValue(1,"CoilColors", "TURQUOISE"),
            Languager.getTagValue(1,"CoilColors", "TURQUOISE") + " - " + FilamentCodes.A010,
            Languager.getTagValue(1,"CoilColors", "WHITE"),
            Languager.getTagValue(1,"CoilColors", "WHITE") + " - " + FilamentCodes.A002,
            Languager.getTagValue(1,"CoilColors", "YELLOW"),
            Languager.getTagValue(1,"CoilColors", "YELLOW") + " - " + FilamentCodes.A005,
            
            
//            FilamentCodes.A301 + " - " + Languager.getTagValue(1,"CoilColors", "WHITE"),
//            FilamentCodes.A302 + " - " + Languager.getTagValue(1,"CoilColors", "BLACK"),
//            FilamentCodes.A303 + " - " + Languager.getTagValue(1,"CoilColors", "YELLOW"),
//            FilamentCodes.A304 + " - " + Languager.getTagValue(1,"CoilColors", "RED"),
//            FilamentCodes.A305 + " - " + Languager.getTagValue(1,"CoilColors", "TURQUOISE"),
//            FilamentCodes.A306 + " - " + Languager.getTagValue(1,"CoilColors", "TRANSPARENT"),
//            FilamentCodes.A321 + " - " + Languager.getTagValue(1,"CoilColors", "GREEN"),
//            FilamentCodes.A322 + " - " + Languager.getTagValue(1,"CoilColors", "ORANGE"), 
        };

        return colors;
    }

    public static String getFilamentType(String code) {
        //FILKEMP
        if (code.contains("001")) {
            return Languager.getTagValue(1,"CoilColors", "TRANSPARENT")+" - "+ FilamentCodes.A001;
        }
        if (code.contains("002")) {
            return Languager.getTagValue(1,"CoilColors", "WHITE")+" - "+ FilamentCodes.A002 ;
        }
        if (code.contains("003")) {
            return Languager.getTagValue(1,"CoilColors", "BLACK")+" - "+ FilamentCodes.A003 ;
        }
        if (code.contains("004")) {
            return Languager.getTagValue(1,"CoilColors", "RED")+" - "+ FilamentCodes.A003;
        }
        if (code.contains("005")) {
            return Languager.getTagValue(1,"CoilColors", "YELLOW")+" - "+ FilamentCodes.A005;
        }
        if (code.contains("006")) {
            return Languager.getTagValue(1,"CoilColors", "BLUE")+" - "+ FilamentCodes.A006;
        }
        if (code.contains("007")) {
            return Languager.getTagValue(1,"CoilColors", "OLIVE")+" - "+ FilamentCodes.A007 ;
        }
        if (code.contains("008")) {
            return Languager.getTagValue(1,"CoilColors", "PINK")+" - "+ FilamentCodes.A008;
        }
        if (code.contains("009")) {
            return Languager.getTagValue(1,"CoilColors", "SILVER")+" - "+ FilamentCodes.A009;
        }
        if (code.contains("010")) {
            return Languager.getTagValue(1,"CoilColors", "TURQUOISE")+" - "+ FilamentCodes.A010 ;
        }
        if (code.contains("011")) {
            return Languager.getTagValue(1,"CoilColors", "GREEN")+" - "+ FilamentCodes.A011 ;
        }
        if (code.contains("012")) {
            return Languager.getTagValue(1,"CoilColors", "ORANGE")+" - "+ FilamentCodes.A012;
        }
//         //KDI-NEW
//        if (code.contains("331")) {
//            return FilamentCodes.A331 + " - " + Languager.getTagValue(1,"CoilColors", "WHITE");
//        }
//        if (code.contains("332")) {
//            return FilamentCodes.A332 + " - " + Languager.getTagValue(1,"CoilColors", "BLACK");
//        }
//        if (code.contains("333")) {
//            return FilamentCodes.A333 + " - " + Languager.getTagValue(1,"CoilColors", "YELLOW");
//        }
//        if (code.contains("334")) {
//            return FilamentCodes.A334 + " - " + Languager.getTagValue(1,"CoilColors", "RED");
//        }
//        if (code.contains("335")) {
//            return FilamentCodes.A335 + " - " + Languager.getTagValue(1,"CoilColors", "TURQUOISE");
//        }
//        if (code.contains("336")) {
//            return FilamentCodes.A336 + " - " + Languager.getTagValue(1,"CoilColors", "TRANSPARENT");
//        }
//        if (code.contains("337")) {
//            return FilamentCodes.A337 + " - " + Languager.getTagValue(1,"CoilColors", "GREEN");
//        }
//        if (code.contains("338")) {
//            return FilamentCodes.A338 + " - " + Languager.getTagValue(1,"CoilColors", "ORANGE");
//        }
        
         //KDI-OLD
        
        if (code.contains("301")) {
            return Languager.getTagValue(1,"CoilColors", "WHITE");
        }
        if (code.contains("302")) {
            return Languager.getTagValue(1,"CoilColors", "BLACK");
        }
        if (code.contains("303")) {
            return Languager.getTagValue(1,"CoilColors", "YELLOW");
        }
        if (code.contains("304")) {
            return Languager.getTagValue(1,"CoilColors", "RED");
        }
        if (code.contains("305")) {
            return Languager.getTagValue(1,"CoilColors", "TURQUOISE");
        }
        if (code.contains("306")) {
            return Languager.getTagValue(1,"CoilColors", "TRANSPARENT");
        }
        if (code.contains("321")) {
            return Languager.getTagValue(1,"CoilColors", "GREEN");
        }
        if (code.contains("322")) {
            return Languager.getTagValue(1,"CoilColors", "ORANGE");
        }

        return "N/A";
    }

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
