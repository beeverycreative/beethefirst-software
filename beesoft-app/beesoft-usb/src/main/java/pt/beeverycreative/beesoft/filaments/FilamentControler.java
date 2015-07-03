package pt.beeverycreative.beesoft.filaments;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import replicatorg.app.Base;

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

    private static List<Filament> filamentList;
    
    public static String NO_FILAMENT = "NO_FILAMENT";
    public static String NO_FILAMENT_CODE = "A000";
    
    private static final String filamentsDir = Base.getApplicationDirectory() + "/filaments/";
    
    /**
     * Returns the current list of Filament objects
     * 
     * @return 
     */
    public static List<Filament> getFilamentList() {
        
        if (filamentList == null) {
            fetchFilaments();
        }
        
        return filamentList;
    }
    
    /**
     * Gets the current list of Filament objects present in the filaments directory
     * parsing the available xml files    
     */
    private static void fetchFilaments() {
        
        // get all the files from a directory
        File directory = new File(filamentsDir);
        File[] fList = directory.listFiles();
        
        if (fList != null ) {
            List<File> filamentFiles = new ArrayList<File>();

            for (File file : fList) {
                if (file.isFile() && file.getName().endsWith(".xml")) {
                    filamentFiles.add(file);
                } 
            }

            List<Filament> availableFilaments = new ArrayList<Filament>();
            
            JAXBContext jc;
            Unmarshaller unmarshaller;
            try {
                jc = JAXBContext.newInstance(Filament.class);
                unmarshaller = jc.createUnmarshaller();   

                //Parses all the files
                for (File ff : filamentFiles) {                               

                    Filament fil;
                    try {
                        fil = (Filament) unmarshaller.unmarshal(ff);                    
                        availableFilaments.add(fil);
                    } catch (JAXBException ex) {
                        Logger.getLogger(FilamentManager.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }            
            } catch (JAXBException ex) {
                Logger.getLogger(FilamentManager.class.getName()).log(Level.SEVERE, null, ex);                                            
            }
                       
            filamentList = availableFilaments;
        }                
    } 
    
    /**
     * Finds a color name in the filaments List using a passed color code
     * 
     * @param colorCode
     * @return 
     */
    private static String findColor(String colorCode) {
        
        if (filamentList == null) {
            fetchFilaments();
        }
        
        for (Filament filament : filamentList) {
            
            if (filament.getCode().equals(colorCode)) {
                return filament.getName();
            }
        }
        
        return null;
    }
    
    /**
     * Get Color Copy from coil code.
     * 
     * @param coilCode spool code.
     * 
     * @return color copy.
     */
    public static String getColor(String coilCode) {

        String color = findColor(coilCode);
        
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
                
        if (filamentList == null) {
            fetchFilaments();
        }
        
        String[]colors = new String[filamentList.size()];
        
        if (!filamentList.isEmpty()) {
            int i=0;
            for (Filament fil : filamentList) {
                colors[i] = fil.getName();
                i++;
            }                             
        }

        return colors;
    }
    
    /**
     * Gets an HashMap of available color codes and their copy.
     * @return array of colors.
     */
    public static Map<String,String> getColorsMap() {
                
        if (filamentList == null) {
            fetchFilaments();
        }
        
        HashMap<String, String> colorsMap = new HashMap<String, String>();
        for (Filament fil : filamentList) {
            colorsMap.put(fil.getCode(), fil.getName());
        }                                

        return colorsMap;
    }    
    
    /**
     * Get array of available filament codes
     * 
     * @return array of codes.
     */
    public static String[] getFilamentCodes() {
                
                
        if (filamentList == null) {
            fetchFilaments();
        }
        
        String[] colorCodes = new String[filamentList.size()];
        int i = 0;
        for (Filament fil : filamentList) {
            colorCodes[i] = fil.getCode().toUpperCase();
            i++;
        }                                
        return colorCodes;
    }    

    /**
     * Get color name and copy based on coil code.
     * 
     * @param code coil code.
     * @return color copy and code.
     */
    public static String getFilamentType(String code) {

        if (code != null) {
            return findColor(code);
        }

        return "N/A";
    }

    /**
     * Get color ratio from coil code.
     * 
     * @param coilCode coil code.
     * @param resolution resolution
     * @param printerId printer identification
     * 
     * @return ratio for each color. 
     */
    public static double getColorRatio(String coilCode, String resolution, String printerId) {

        double result = 1.00; //Default
               
        if (filamentList == null) {
            fetchFilaments();
        }
        
        if (!filamentList.isEmpty()) {
            for (Filament fil: filamentList) {
                if (fil.getCode().equals(coilCode)) {
                    
                    for (SlicerConfig sc : fil.getSupportedPrinters()) {
                        if (printerId.toLowerCase().contains(sc.getPrinterName().toLowerCase())) {
                            
                            for (Resolution res : sc.getResolutions()) {
                                if (res.getType().equals(resolution)) {
                                    double colorRatio = res.getFilamentFlow().getValue() / 100.0;
                                    
                                    return colorRatio;
                                }
                            }
                        }
                    } 
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

