package pt.beeverycreative.beesoft.filaments;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import pt.beeverycreative.beesoft.drivers.usb.PrinterInfo;
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

    private static Set<Filament> filamentList;
    private static PrinterInfo currentPrinterFilamentList = null;

    public static String NO_FILAMENT = "none";
    public static String NO_FILAMENT_2 = "_no_file";
    public static String NO_FILAMENT_CODE = "A000";

    private static final String filamentsDir = Base.getApplicationDirectory() + "/filaments/";

    /**
     * Initializes list of filaments for a given printer. Does nothing if the
     * current list contains the filaments for the given printer
     *
     * @param printer Printer for which the list of filaments will be
     * initialized
     */
    public static void initFilamentList(PrinterInfo printer) {
        Base.writeLog("Initial filament fetch for printer " + printer, FilamentControler.class);
        Base.writeLog("Printer code for filament purposes: " + printer.filamentCode(), FilamentControler.class);

        if (currentPrinterFilamentList == null
                || !currentPrinterFilamentList.filamentCode().equals(printer.filamentCode())) {
            fetchFilaments();
            currentPrinterFilamentList = printer;
        } else {
            Base.writeLog("No fetch is necessary, list already contains the "
                    + "filaments for this printer", FilamentControler.class);
        }
    }

    /**
     * Gets the current list of Filament objects present in the filaments
     * directory parsing the available xml files
     */
    private static void fetchFilaments() {

        String connectedPrinter = Base.getMainWindow().getMachine()
                .getDriver().getConnectedDevice().filamentCode();

        // get all the files from a directory
        File directory = new File(filamentsDir);
        File[] fList = directory.listFiles();

        if (fList != null) {
            List<File> filamentFiles = new ArrayList<File>();

            for (File file : fList) {
                if (file.isFile() && file.getName().endsWith(".xml")) {
                    filamentFiles.add(file);
                }
            }

            Set<Filament> availableFilaments = new TreeSet<Filament>();

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

                        // only add to available filaments if it is supported by
                        // the printer, or if no printer is connected
                        for (SlicerConfig sc : fil.getSupportedPrinters()) {
                            if (connectedPrinter.equals(sc.getPrinterName())
                                    || connectedPrinter.equals("UNKNOWN")) {
                                availableFilaments.add(fil);
                                break;
                            }
                        }

                    } catch (JAXBException ex) {
                        Logger.getLogger(FilamentControler.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            } catch (JAXBException ex) {
                Logger.getLogger(FilamentControler.class.getName()).log(Level.SEVERE, null, ex);
            }

            Base.writeLog("Acquired " + availableFilaments.size() + " filaments", FilamentControler.class);

            if (filamentList != null) {
                filamentList.clear();
                filamentList = null;
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

            if (filament.getName().equals(colorCode)) {
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

        String[] colors = new String[filamentList.size()];

        if (!filamentList.isEmpty()) {
            int i = 0;
            for (Filament fil : filamentList) {
                colors[i] = fil.getName();
                i++;
            }
        }

        return colors;
    }

    public static String[] forceFetch() {
        fetchFilaments();

        String[] colors = new String[filamentList.size()];

        if (!filamentList.isEmpty()) {
            int i = 0;
            for (Filament fil : filamentList) {
                colors[i] = fil.getName();
                i++;
            }
        }

        return colors;
    }

    /**
     * Gets an HashMap of available color codes and their copy.
     *
     * @return array of colors.
     */
    public static Map<String, String> getColorsMap() {

        if (filamentList == null) {
            fetchFilaments();
        }

        HashMap<String, String> colorsMap = new HashMap<String, String>();
        for (Filament fil : filamentList) {
            colorsMap.put(fil.getName(), fil.getName());
        }

        return colorsMap;
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
            for (Filament fil : filamentList) {
                if (fil.getName().equals(coilCode)) {

                    for (SlicerConfig sc : fil.getSupportedPrinters()) {
                        if (printerId.toLowerCase().contains(sc.getPrinterName().toLowerCase())) {

                            for (Resolution res : sc.getResolutions()) {
                                if (res.getType().equals(resolution)) {
                                    for (SlicerParameter parameter : res.getParameters()) {
                                        if (parameter.getName().equals("filament_flow")) {
                                            return Double.parseDouble(parameter.getValue()) / 100.0;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    public static double getColorTemperature(String coilCode, String resolution, String printerId) {
        double result = 220; //Default

        if (filamentList == null) {
            fetchFilaments();
        }

        if (!filamentList.isEmpty()) {
            for (Filament fil : filamentList) {
                if (fil.getName().equals(coilCode)) {

                    for (SlicerConfig sc : fil.getSupportedPrinters()) {
                        if (printerId.toLowerCase().contains(sc.getPrinterName().toLowerCase())) {

                            for (Resolution res : sc.getResolutions()) {
                                if (res.getType().equals(resolution)) {
                                    for (SlicerParameter parameter : res.getParameters()) {
                                        if (parameter.getName().equals("print_temperature")) {
                                            return Double.parseDouble(parameter.getValue());
                                        }
                                    }
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
     * Get the Hash map with the filament settings for a specific resolution and
     * printer
     *
     * @param coilCode coil code.
     * @param resolution resolution
     * @param printerId printer identification
     *
     * @return ratio for each color.
     */
    public static HashMap<String, String> getFilamentSettings(String coilCode,
            String resolution, String printerId) {

        if (filamentList == null) {
            fetchFilaments();
        }

        if (!filamentList.isEmpty()) {
            for (Filament fil : filamentList) {
                if (fil.getName().toLowerCase().equals(coilCode.toLowerCase())) {

                    for (SlicerConfig sc : fil.getSupportedPrinters()) {
                        if (printerId.equals(sc.getPrinterName())) {
                            for (Resolution res : sc.getResolutions()) {
                                if (res.getType().toLowerCase().equals(resolution.toLowerCase())) {

                                    return res.getSlicerParameters();
                                }
                            }
                        }
                    }
                }
            }
        }
        // Defaults to an empty map
        return new HashMap<String, String>();
    }

    /**
     * Get coil code from color name.
     *
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

    public static boolean colorExistsLocally(String name) {
        if (filamentList == null) {
            fetchFilaments();
        }

        for (Filament fil : filamentList) {
            if (fil.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    public static Filament getMatchingFilament(String filamentText) {
        if (filamentList == null) {
            fetchFilaments();
        }

        for (Filament fil : filamentList) {
            if (fil.getName().equalsIgnoreCase(filamentText)) {
                return fil;
            }
        }

        return null;
    }

}
