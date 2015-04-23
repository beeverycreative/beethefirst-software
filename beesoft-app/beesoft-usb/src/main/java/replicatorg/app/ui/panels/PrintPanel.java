package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import replicatorg.app.Base;
import replicatorg.app.FilamentControler;
import replicatorg.app.Languager;
import replicatorg.app.PrintEstimator;
import replicatorg.app.Printer;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.app.util.ExtensionFilter;
import replicatorg.drivers.Driver;

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
public class PrintPanel extends BaseDialog {

    private JLabel quality_prototype;
    private JLabel quality_normal;
    private JLabel quality_artwork;
    private JLabel quality_low;
    private JLabel quality_medium;
    private JLabel quality_solid;
    private final ArrayList<String> prefs;
    private boolean raftPressed, supportPressed, autonomousPressed, gcodeSavePressed;
    private final Driver driver = Base.getMainWindow().getMachineInterface().getDriver();
    private boolean no_Filament = false;
    private static final String NOK = "NOK";
    private Thread t = null;
    private static final String FORMAT = "%2d:%2d";
    private String colorCode = FilamentControler.NO_FILAMENT_CODE;
    private PrintEstimationThread estimationThread = null;
    private boolean isRunning = true;
    private boolean lastUsedRaft;
    private String lastUsedDensity;
    private String lastUsedResolution;
    private boolean lastUsedSupport;
    private boolean lastUsedAutonomous;
    private boolean lastSelectedRaft;
    private String lastSelectedDensity;
    private String lastSelectedResolution;
    private boolean lastUsedGCodeSave;
    boolean lastSelectedSupport;
    boolean lastSelectedAutonomous;
    boolean gcodeOK;
    private boolean estimatePressed;
    private Hashtable<Integer, JLabel> labelTable2;
    private boolean printerAvailable;
    private String gcodeToPrint = null;

    public PrintPanel() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        initSlidersLabels();
        setFont();
        setTextLanguage();
        initSliderConfigs();
        centerOnScreen();
        estimationThread = new PrintEstimationThread(this);
        getCoilCode();
        prefs = new ArrayList<String>();
        raftPressed = false;
        supportPressed = false;
        autonomousPressed = (Boolean.valueOf(ProperDefault.get("autonomy")) == true);
        evaluateConditions();
        matchChanges();
        enableDrag();
        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());
    }

    /**
     * Set font for all UI elements.
     */
    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProRegular("14"));
        jLabel2.setFont(GraphicDesignComponents.getSSProBold("14"));
        jLabel3.setFont(GraphicDesignComponents.getSSProBold("14"));
        jLabel5.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel7.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel8.setFont(GraphicDesignComponents.getSSProRegular("10"));
        jLabel9.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel10.setFont(GraphicDesignComponents.getSSProRegular("10"));
        bCancel.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel12.setFont(GraphicDesignComponents.getSSProRegular("12"));
        b_changeFilament.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bEstimate.setFont(GraphicDesignComponents.getSSProRegular("12"));
        lDensity.setFont(GraphicDesignComponents.getSSProRegular("12"));
        filamentType.setFont(GraphicDesignComponents.getSSProRegular("10"));
        estimatedPrintTime.setFont(GraphicDesignComponents.getSSProRegular("10"));
        estimatedMaterial.setFont(GraphicDesignComponents.getSSProRegular("10"));
        printTime.setFont(GraphicDesignComponents.getSSProRegular("10"));
        materialCost.setFont(GraphicDesignComponents.getSSProRegular("10"));
        jLabel22.setFont(GraphicDesignComponents.getSSProRegular("10"));
        jLabel23.setFont(GraphicDesignComponents.getSSProRegular("10"));

        //Resolution
        b_lowRes.setFont(GraphicDesignComponents.getSSProRegular("12"));
        b_mediumRes.setFont(GraphicDesignComponents.getSSProRegular("12"));
        b_highRes.setFont(GraphicDesignComponents.getSSProRegular("12"));
        b_highPlusRes.setFont(GraphicDesignComponents.getSSProRegular("12"));

        //Density
        quality_low.setFont(GraphicDesignComponents.getSSProRegular("12"));
        quality_medium.setFont(GraphicDesignComponents.getSSProRegular("12"));
        quality_solid.setFont(GraphicDesignComponents.getSSProRegular("12"));

    }

    /**
     * Set copy for all UI elements.
     */
    private void setTextLanguage() {
        jLabel1.setText(Languager.getTagValue(1, "MainWindowButtons", "Print"));
        jLabel2.setText(Languager.getTagValue(1, "Print", "Print_Quality"));
        jLabel3.setText(Languager.getTagValue(1, "Print", "Print_Density"));
        jLabel7.setText(Languager.getTagValue(1, "Print", "Print_Raft"));
        jLabel8.setText(splitString(Languager.getTagValue(1, "Print", "Print_Raft_Info")));
        jLabel9.setText(Languager.getTagValue(1, "Print", "Print_Support"));
        jLabel10.setText(splitString(Languager.getTagValue(1, "Print", "Print_Support_Info")));
        bCancel.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line3"));
        bEstimate.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line15"));
        jLabel12.setText(Languager.getTagValue(1, "ToolPath", "Line21"));
        b_changeFilament.setText(Languager.getTagValue(1, "Print", "Print_ChangeFilament"));
        filamentType.setText(Languager.getTagValue(1, "MaintenancePanel", "Filament_Type"));
        estimatedPrintTime.setText(Languager.getTagValue(1, "Print", "Print_EstimationPrintTime"));
        estimatedMaterial.setText(Languager.getTagValue(1, "Print", "Print_EstimationMaterial"));
        lDensity.setText(Languager.getTagValue(1, "Print", "Print_Density_Insertion"));
        jLabel23.setText("");


        b_lowRes.setText(Languager.getTagValue(1, "Print", "Print_Quality_Low"));
        b_mediumRes.setText(Languager.getTagValue(1, "Print", "Print_Quality_Medium"));
        b_highRes.setText(Languager.getTagValue(1, "Print", "Print_Quality_High"));
//        String artwork = "<html>"+Languager.getTagValue(1,"Print", "Print_Quality_SHigh").split(" ")[0]+"<br>"+Languager.getTagValue(1,"Print", "Print_Quality_SHigh").split(" ")[1]+"</html>";
        b_highPlusRes.setText(Languager.getTagValue(1, "Print", "Print_Quality_SHigh"));

        quality_low.setText(Languager.getTagValue(1, "Print", "Print_Density_Low"));
        quality_medium.setText(Languager.getTagValue(1, "Print", "Print_Density_Medium"));
        quality_solid.setText(Languager.getTagValue(1, "Print", "Print_Density_High"));

    }

    /**
     * Get coil code from machine and system. Also prepares label with coil code
     * info.
     */
    private void getCoilCode() {

        String code;

        if (Base.getMachineLoader().isConnected() == false) {
            code = Base.getMainWindow().getMachine().getModel().getCoilCode();
        } else {
            Base.getMachineLoader().getMachineInterface().getDriver().updateCoilCode();
            code = Base.getMainWindow().getMachine().getModel().getCoilCode();
        }
        Base.writeLog("Print panel coil code: " + code);

        if (code.equals(FilamentControler.NO_FILAMENT_CODE) || code.equals("NOK")) {
            no_Filament = true;
            jLabel22.setFont(GraphicDesignComponents.getSSProBold("10"));
            code = Languager.getTagValue(1, "Print", "Print_Splash_Info9").toUpperCase();

            jLabel22.setText(" " + code);
            jLabel23.setText(Languager.getTagValue(1, "Print", "Print_Splash_Info11"));

        } else {

            code = FilamentControler.getColor(Base.getMainWindow().getMachine().getModel().getCoilCode());

            //Checks if user besides having a BEECode, it is one up-to-date.
            //BEECodes may have been changed
            if (code.equals(FilamentControler.NO_FILAMENT) == false) {
                colorCode = code;
            } else {
                no_Filament = true;
            }
            jLabel22.setText(" " + code);
        }

        if (no_Filament == true) {
            jLabel22.setText(" " + code);
            jLabel23.setText(Languager.getTagValue(1, "Print", "Print_Splash_Info11"));
        } else {
            jLabel22.setText(" " + code);
        }

    }

    /**
     * Center window on screen.
     */
    private void centerOnScreen() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        // Determine the new location of the window
        int w = this.getSize().width;
        int h = this.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;

        // Move the window
        this.setLocation(x, y);
        this.setLocationRelativeTo(Base.getMainWindow());
        Base.setMainWindowNOK();
    }

    /**
     * Splits string foreach dot.
     *
     * @param s stering to be splitted
     * @return string splitted on dot.
     */
    private String splitString(String s) {
        int width = 340;
        return buildString(s.split("\\."), width);
    }

    /**
     * Builds a text string based on form properties from several minor strings.
     *
     * @param parts text to be embedded
     * @param width form width
     * @return final string
     */
    private String buildString(String[] parts, int width) {
        String text = "";
        String ihtml = "<html>";
        String ehtml = "</html>";
        String br = "<br>";

        for (int i = 0; i < parts.length; i++) {
            if (i + 1 < parts.length) {
                if (getStringPixelsWidth(parts[i]) + getStringPixelsWidth(parts[i + 1]) < width) {
                    text = text.concat(parts[i]).concat(".").concat(parts[i + 1]).concat(".").concat(br);
                    i++;
                } else {
                    text = text.concat(parts[i]).concat(".").concat(br);
                }
            } else {
                text = text.concat(parts[i]); //.concat(".");
            }
        }

        return ihtml.concat(text).concat(".").concat(ehtml);
    }

    /**
     * Get String size in pixels
     *
     * @param s Text to be processed
     * @return size in pixels of the given string
     */
    private int getStringPixelsWidth(String s) {
        Graphics g = getGraphics();
        FontMetrics fm = g.getFontMetrics(GraphicDesignComponents.getSSProRegular("10"));
        return fm.stringWidth(s);
    }

    /**
     * Stores gcode path of the file to be printed
     *
     * @param gcodeFilePath path for the gcode to be printed
     */
    public void setGCodePath(String gcodeFilePath) {

        if (gcodeFilePath != null) {
            this.gcodeToPrint = gcodeFilePath;
            Base.isPrintingFromGCode = true;
        } else {
            this.gcodeToPrint = null;
            Base.isPrintingFromGCode = false;
        }
    }

    /**
     * Inits Density slider configuration and values.
     */
    private void initSliderConfigs() {
//        Hashtable labelTable1 = new Hashtable();
//        labelTable1.put(new Integer(0), quality_prototype);
//        labelTable1.put(new Integer(50), quality_normal);
//        labelTable1.put( new Integer(100), quality_artwork );
//        jSlider1.setLabelTable(labelTable1);

        labelTable2 = new Hashtable<Integer, JLabel>();

        JLabel a = new JLabel("O");
        JLabel a1 = new JLabel(Languager.getTagValue(1, "Print", "Print_Density_Low"));
        JLabel b = new JLabel("10");
        b.setBorder(new EmptyBorder(0, 10, 0, 0));
        JLabel c = new JLabel(Languager.getTagValue(1, "Print", "Print_Density_Medium"));
        c.setBorder(new EmptyBorder(0, 5, 0, 0));
        JLabel d = new JLabel("30");
        JLabel e = new JLabel(Languager.getTagValue(1, "Print", "Print_Density_High"));
        JLabel f = new JLabel("5O");
        JLabel g = new JLabel("6O");
        JLabel h = new JLabel("7O");
        JLabel i = new JLabel("8O");
        JLabel j = new JLabel("9O");
        JLabel k = new JLabel("10O");

        a.setFont(GraphicDesignComponents.getSSProRegular("12"));
        a1.setFont(GraphicDesignComponents.getSSProBold("10"));
        b.setFont(GraphicDesignComponents.getSSProRegular("12"));
        c.setFont(GraphicDesignComponents.getSSProBold("10"));
        d.setFont(GraphicDesignComponents.getSSProRegular("12"));
        e.setFont(GraphicDesignComponents.getSSProBold("10"));
        f.setFont(GraphicDesignComponents.getSSProRegular("12"));
        g.setFont(GraphicDesignComponents.getSSProRegular("12"));
        h.setFont(GraphicDesignComponents.getSSProRegular("12"));
        i.setFont(GraphicDesignComponents.getSSProRegular("12"));
        j.setFont(GraphicDesignComponents.getSSProRegular("12"));
        k.setFont(GraphicDesignComponents.getSSProRegular("12"));
        f.setForeground(Color.red);
        g.setForeground(Color.red);
        h.setForeground(Color.red);
        i.setForeground(Color.red);
        j.setForeground(Color.red);
        k.setForeground(Color.red);
        labelTable2.put(0, a);
        labelTable2.put(5, a1);
        labelTable2.put(10, b);
        labelTable2.put(20, c);
        labelTable2.put(30, d);
        labelTable2.put(40, e);
        labelTable2.put(50, f);
        labelTable2.put(60, g);
        labelTable2.put(70, h);
        labelTable2.put(80, i);
        labelTable2.put(90, j);
        labelTable2.put(100, k);

        densitySlider.setLabelTable(labelTable2);

    }

    /**
     * Inits Density and resolution fields labels.
     */
    private void initSlidersLabels() {
        quality_prototype = new JLabel("prototype");
        quality_normal = new JLabel("normal");
        quality_artwork = new JLabel("artwork");
        quality_low = new JLabel("light");
        quality_medium = new JLabel("medium");
        quality_solid = new JLabel("solid");
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Parses resolution buttons.
     *
     * @return resolution active button value
     */
    private String parseSlider1() {

        for (Enumeration<AbstractButton> buttons = b_resButtonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                String labelText = button.getText();

                    if (labelText.contains("LOW")) {
                        return "LOW";
                    }
                    if (labelText.contains("Medium")) {
                        return "MEDIUM";
                    }
                    if (labelText.contains("High+")) {
                        return "SHIGH";
                    }
                    if (labelText.contains("High")) {
                        return "HIGH";
                    }
                }
            }

        return "LOW";
    }

    /**
     * Parses density slider.
     *
     * @return density slider value
     */
    private String parseSlider2() {
        return String.valueOf(densitySlider.getValue());
    }

    /**
     * Evaluates initial conditions for on form load.
     */
    private void evaluateConditions() {

        lastUsedRaft = Base.getMainWindow().getBed().isLasRaft();
        lastUsedDensity = Base.getMainWindow().getBed().getLastDensity();
        lastUsedResolution = Base.getMainWindow().getBed().getLastResolution();
        lastUsedSupport = Base.getMainWindow().getBed().isLastSupport();
        lastUsedAutonomous = Base.getMainWindow().getBed().isLastAutonomous();
        gcodeOK = Base.getMainWindow().getBed().isGcodeOK();

        materialCost.setText("N/A");
        showLoadingIcon(false);
        updateOldSettings();

        //|| gramsCalculator(Double.valueOf(ProperDefault.get("filamentCoilRemaining"))) < 100
        if (no_Filament) // Less than a meter(100 grams)
        {
            jLabel12.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_15.png")));
            jLabel22.setForeground(Color.red);
            jLabel23.setForeground(Color.red);
        }

        printerAvailable = Base.getMachineLoader().isConnected() && !Base.isPrinting;

        if (printerAvailable && no_Filament == false) {
            jLabel12.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_15.png")));
        }

        b_resButtonGroup.add(b_lowRes);
        b_resButtonGroup.add(b_highPlusRes);
        b_resButtonGroup.add(b_highRes);
        b_resButtonGroup.add(b_mediumRes);

    }

    /**
     * Updates estimation panel with print info.
     *
     * @param time estimated duration
     * @param cost estimated material cost
     */
    public void updateEstimationPanel(String time, String cost) {

        if (!time.contains("NA") || !time.contains("N/A")) {
            printTime.setText(buildTimeEstimationString(time));
        }

        if (!cost.contains("NA") && !cost.contains("N/A")) {
            materialCost.setText(tuneCost(cost));
        }

    }

    /**
     * Calculates the grams based on the meters read from the gcode
     *
     * @param meters meters of filament to be wasted
     * @return grams conversion
     */
    private String gramsCalculator(double meters) {
        DecimalFormat df = new DecimalFormat("#.00");
        double grams = meters * 12 / 4;

        if (grams > 0) {
            return df.format(meters * 12 / 4);
        }

        return "N/A";
    }

    /**
     * Tunes from meters to grams.
     *
     * @param cost meters from gcode
     * @return estimated material cost in grams.
     */
    private String tuneCost(String cost) {
        double meters = Double.valueOf(cost);
        String result = gramsCalculator(meters / 1000.0);
        return result + " " + Languager.getTagValue(1, "Print", "Print_GramsTag");
    }

    /**
     * Calculates minutes in hours
     *
     * @param t minutes
     * @return hours corresponding for the input minutes
     */
    private String minutesToHours(int t) {
        int hours = t / 60; //since both are ints, you get an int
        int minute = t % 60;

        return String.format(FORMAT, hours, minute);
    }

    /**
     * Converts based estimation to minutes.
     *
     * @param durT estimated duration
     * @return minutes equivalent to duration
     */
    private int estimatorTimeToMinutes(String durT) {
        if (durT.contains(":")) {
            String[] cells = durT.split(":");
            int hours = Integer.valueOf(cells[0]) * 60;
            int minutes = Integer.valueOf(cells[1]);

            return hours + minutes;
        } else {
            return Integer.valueOf(durT);
        }
    }

    /**
     * Builds estimation string based on duration.
     *
     * @param durT estimated duration
     * @return complete text build with based duration.
     */
    private String buildTimeEstimationString(String durT) {
        String text;
        if (!durT.equals("NA")) {
            int duration = estimatorTimeToMinutes(durT);
            String hours = minutesToHours(duration).split("\\:")[0];
            String minutes = minutesToHours(duration).split("\\:")[1];
            int min = Integer.valueOf(minutes.trim());

            if (duration >= 120) {
                if (min > 1) {
                    text = (hours + " " + Languager.getTagValue(1, "Print", "PrintHours") + " " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinutes"));
                } else if (min == 1) {
                    text = (hours + " " + Languager.getTagValue(1, "Print", "PrintHours") + " " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinute"));
                } else {
                    text = (hours + " " + Languager.getTagValue(1, "Print", "PrintHour"));
                }
            } else if (duration >= 60 && duration < 120) {
                if (min > 1) {
                    text = (hours + " " + Languager.getTagValue(1, "Print", "PrintHour") + " " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinutes"));
                } else if (min == 1) {
                    text = (hours + " " + Languager.getTagValue(1, "Print", "PrintHour") + " " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinute"));
                } else {
                    text = (hours + " " + Languager.getTagValue(1, "Print", "PrintHour"));
                }
            } else {
                text = (" " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinutes"));
            }
        } else {
            text = (durT);
        }


        return text;

    }

    /**
     * Select GCode file to open.
     *
     * @return path to file
     */
    private File selectFile() {
        File directory = null;
        String loadDir = ProperDefault.get("ui.open_dir0");

        if (loadDir != null) {
            directory = new File(loadDir);
        }
        JFileChooser fc = new JFileChooser(directory);
        FileFilter defaultFilter;


        String[] extensions = {".gcode"};
        fc.addChoosableFileFilter(defaultFilter = new ExtensionFilter(extensions, "GCode files"));
        fc.addChoosableFileFilter(new ExtensionFilter(".gcode", "GCode files"));
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(defaultFilter);
        fc.setDialogTitle("Open a model file...");
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setFileHidingEnabled(false);
        int rv = fc.showOpenDialog(this);
        if (rv == JFileChooser.APPROVE_OPTION) {
            fc.getSelectedFile().getName();
            ProperDefault.put("ui.open_dir0", fc.getCurrentDirectory().getAbsolutePath());
            Base.writeLog("GCode File selected " + fc.getSelectedFile().getAbsolutePath());
            return fc.getSelectedFile();
        } else {
            return null;
        }

    }

    public void showLoadingIcon(boolean show) {
        loading.setVisible(show);

        if (show) {
            materialCost.setText("N/A");
            printTime.setText("N/A");
            bEstimate.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_15.png")));
        } else {
            bEstimate.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_15.png")));
            estimatePressed = false;
        }
    }

    /**
     * Updates all fields to stored setting, on form load.
     */
    private void matchChanges() {

        lastUsedRaft = Base.getMainWindow().getBed().isLasRaft();
        lastUsedDensity = Base.getMainWindow().getBed().getLastDensity();
        lastUsedResolution = Base.getMainWindow().getBed().getLastResolution();
        lastUsedSupport = Base.getMainWindow().getBed().isLastSupport();
        lastUsedAutonomous = Base.getMainWindow().getBed().isLastAutonomous();

        if (lastUsedRaft) {
            jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_checked.png")));
            raftPressed = true;
        } else {
            jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_unchecked.png")));
            raftPressed = false;
        }

        if (lastUsedSupport) {
            jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_checked.png")));
            supportPressed = true;
        } else {
            jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_unchecked.png")));
            supportPressed = false;
        }

//        if (lastUsedAutonomous) {
//            jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_checked.png")));
//            autonomousPressed = true;
//        } else {
//            jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_unchecked.png")));
//            autonomousPressed = false;
//        }


        int density;
        try {
            density = Integer.parseInt(lastUsedDensity);
        } catch (NumberFormatException e) {
            density = 5;
        }

        densitySlider.setValue(density);

        if (lastUsedResolution.contains("LOW")) {
            b_lowRes.setSelected(true);
        }
        if (lastUsedResolution.contains("MEDIUM")) {
            b_mediumRes.setSelected(true);
        }
        if (lastUsedResolution.contains("HIGH")) {
            b_highRes.setSelected(true);
        }
        if (lastUsedResolution.contains("SHIGH")) {
            b_highPlusRes.setSelected(true);
        }

        checkChanges();
    }

    /**
     * Checks if current settings match old ones.
     * @return 
     */
    public boolean checkChanges() {
        boolean equal = false;

        lastUsedRaft = Base.getMainWindow().getBed().isLasRaft();
        lastUsedDensity = Base.getMainWindow().getBed().getLastDensity();
        lastUsedResolution = Base.getMainWindow().getBed().getLastResolution();
        lastUsedSupport = Base.getMainWindow().getBed().isLastSupport();
        lastUsedAutonomous = Base.getMainWindow().getBed().isLastAutonomous();
        gcodeOK = Base.getMainWindow().getBed().isGcodeOK();
        lastUsedGCodeSave = Base.getMainWindow().getBed().isGCodeSave();

        if (parseSlider1().equals(lastUsedResolution)
                && parseSlider2().equals(lastUsedDensity)
                && raftPressed == lastUsedRaft
                && supportPressed == lastUsedSupport
                && autonomousPressed == lastUsedAutonomous
                && gcodeSavePressed == lastUsedGCodeSave
                && gcodeOK) {
            equal = true;
        }

        return equal;
    }

    /**
     * Checks if settings that affect gcode were changed.
     * @return 
     */
    public boolean settingsChanged() {

        return parseSlider1().equals(lastSelectedResolution)
                && parseSlider2().equals(lastSelectedDensity)
                && raftPressed == lastSelectedRaft
                && supportPressed == lastSelectedSupport;
    }

    /**
     * Updates old print settings with new ones.
     */
    public void updateOldSettings() {
        lastSelectedRaft = raftPressed;
        lastSelectedDensity = parseSlider2();
        lastSelectedResolution = parseSlider1();
        lastSelectedSupport = supportPressed;
        lastSelectedAutonomous = autonomousPressed;
    }

    /**
     * Cancel event.
     */
    private void doCancel() {
        dispose();
        Base.cleanDirectoryTempFiles(Base.getAppDataDirectory().getAbsolutePath() + "/" + Base.MODELS_FOLDER + "/");
        Base.getMainWindow().getButtons().updatePressedStateButton("print");
        Base.bringAllWindowsToFront();
        isRunning = false;
        estimationThread.stop();
        if (t != null) {
            t.stop();
        }

    }

    /**
     * Gets print parameters from selected configurations on window.
     *
     * @return print parameters.
     */
    public ArrayList<String> getPreferences() {
        ArrayList<String> prefs = new ArrayList<String>();

        prefs.add(parseSlider1());
        prefs.add(FilamentControler.getColor(colorCode));
        prefs.add(parseSlider2());
        prefs.add(String.valueOf(raftPressed));
        prefs.add(String.valueOf(supportPressed));
        prefs.add(String.valueOf(autonomousPressed));

        return prefs;
    }

    /**
     * Handles Density slider event.
     */
    private void checkDensitySliderValue(int val) {

        labelTable2.get(5).setForeground(Color.BLACK);
        labelTable2.get(20).setForeground(Color.BLACK);
        labelTable2.get(40).setForeground(Color.BLACK);

        if (val == 5) {
            labelTable2.get(5).setForeground(new Color(255, 203, 5));
        } else if (val == 20) {
            labelTable2.get(20).setForeground(new Color(255, 203, 5));
        } else if (val == 40) {
            labelTable2.get(40).setForeground(new Color(255, 203, 5));
        }
    }

    /**
     * Handles Raft checkbox event.
     */
    private void triggerRaft() {
        lastSelectedRaft = raftPressed;

        if (!raftPressed) {
            jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_checked.png")));
            raftPressed = true;
        } else {
            jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_unchecked.png")));
            raftPressed = false;
        }

        checkChanges();
    }

    /**
     * Handles Support checkbox event.
     */
    private void triggerSupport() {
        lastSelectedSupport = supportPressed;

        if (!supportPressed) {
            jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_checked.png")));
            supportPressed = true;
        } else {
            jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_unchecked.png")));
            supportPressed = false;
        }

        checkChanges();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        b_resButtonGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        densitySlider = new javax.swing.JSlider();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        filamentType = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        estimatedPrintTime = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        estimatedMaterial = new javax.swing.JLabel();
        printTime = new javax.swing.JLabel();
        materialCost = new javax.swing.JLabel();
        loading = new javax.swing.JLabel();
        bEstimate = new javax.swing.JLabel();
        tfDensity = new javax.swing.JTextField();
        lDensity = new javax.swing.JLabel();
        b_lowRes = new javax.swing.JRadioButton();
        b_mediumRes = new javax.swing.JRadioButton();
        b_highRes = new javax.swing.JRadioButton();
        b_highPlusRes = new javax.swing.JRadioButton();
        b_changeFilament = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        bCancel = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));
        jPanel1.setPreferredSize(new java.awt.Dimension(374, 460));

        jPanel4.setBackground(new java.awt.Color(255, 203, 5));
        jPanel4.setMinimumSize(new java.awt.Dimension(62, 26));
        jPanel4.setPreferredSize(new java.awt.Dimension(70, 30));
        jPanel4.setRequestFocusEnabled(false);

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_9.png"))); // NOI18N
        jLabel15.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel15MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, 13, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(9, Short.MAX_VALUE))
        );

        jLabel1.setText("IMPRIMIR");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jLabel2.setText("Qualidade");

        jLabel3.setText("Densidade");

        densitySlider.setBackground(new java.awt.Color(248, 248, 248));
        densitySlider.setMajorTickSpacing(1);
        densitySlider.setPaintLabels(true);
        densitySlider.setSnapToTicks(true);
        densitySlider.setValue(0);
        densitySlider.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        densitySlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                densitySliderMouseReleased(evt);
            }
        });
        densitySlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                densitySliderStateChanged(evt);
            }
        });

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/c_unchecked.png"))); // NOI18N
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel4MousePressed(evt);
            }
        });

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/c_unchecked.png"))); // NOI18N
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel5MousePressed(evt);
            }
        });

        jLabel7.setText("Raft");
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel7MousePressed(evt);
            }
        });

        jLabel8.setText("Suspendisse potenti.");
        jLabel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel8MousePressed(evt);
            }
        });

        jLabel9.setText("Support");
        jLabel9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel9MousePressed(evt);
            }
        });

        jLabel10.setText("Suspendisse potenti.");
        jLabel10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel10MousePressed(evt);
            }
        });

        filamentType.setText("Filament Type");

        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/arrow_ajuda_2.png"))); // NOI18N

        jLabel22.setText("NO_FILAMENT");

        jLabel23.setText("a");

        jLabel24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/arrow_ajuda_2.png"))); // NOI18N

        estimatedPrintTime.setText("Print time:");

        jLabel26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/arrow_ajuda_2.png"))); // NOI18N

        estimatedMaterial.setText("Material cost:");

        printTime.setText("N/A");

        materialCost.setText("N/A");

        loading.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/loading.gif"))); // NOI18N

        bEstimate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_15.png"))); // NOI18N
        bEstimate.setText("Estimate");
        bEstimate.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        bEstimate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bEstimate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bEstimateMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bEstimateMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bEstimateMousePressed(evt);
            }
        });

        tfDensity.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfDensity.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfDensityKeyReleased(evt);
            }
        });

        lDensity.setText("Density value (%) :");

        b_lowRes.setBackground(new java.awt.Color(248, 248, 248));
        b_lowRes.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        b_lowRes.setText("LOW");
        b_lowRes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_lowResActionPerformed(evt);
            }
        });

        b_mediumRes.setBackground(new java.awt.Color(248, 248, 248));
        b_mediumRes.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        b_mediumRes.setSelected(true);
        b_mediumRes.setText("MEDIUM");
        b_mediumRes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_mediumResActionPerformed(evt);
            }
        });

        b_highRes.setBackground(new java.awt.Color(248, 248, 248));
        b_highRes.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        b_highRes.setText("HIGH");
        b_highRes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_highResActionPerformed(evt);
            }
        });

        b_highPlusRes.setBackground(new java.awt.Color(248, 248, 248));
        b_highPlusRes.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        b_highPlusRes.setText("HIGH + (beta)");
        b_highPlusRes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_highPlusResActionPerformed(evt);
            }
        });

        b_changeFilament.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_16.png"))); // NOI18N
        b_changeFilament.setText("Mudar filamento agora");
        b_changeFilament.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        b_changeFilament.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b_changeFilamentMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                b_changeFilamentMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                b_changeFilamentMousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(4, 4, 4)
                                        .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(filamentType)
                                        .addGap(17, 17, 17)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel23)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel22)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(loading))))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel8)
                                            .addComponent(jLabel9)
                                            .addComponent(jLabel10))
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(estimatedMaterial, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(estimatedPrintTime, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)))
                                        .addGap(15, 15, 15)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(printTime)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(bEstimate))
                                            .addComponent(materialCost)))
                                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jSeparator2))))
                        .addGap(19, 19, 19))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(densitySlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(b_lowRes)
                                .addGap(25, 25, 25)
                                .addComponent(b_mediumRes)
                                .addGap(25, 25, 25)
                                .addComponent(b_highRes)
                                .addGap(25, 25, 25)
                                .addComponent(b_highPlusRes))
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel7))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lDensity)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfDensity, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(42, Short.MAX_VALUE))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(137, 137, 137)
                .addComponent(b_changeFilament)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(b_lowRes)
                    .addComponent(b_highRes)
                    .addComponent(b_mediumRes)
                    .addComponent(b_highPlusRes))
                .addGap(8, 8, 8)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(densitySlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfDensity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lDensity))
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addGap(12, 12, 12)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(loading, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(filamentType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(estimatedPrintTime)
                        .addComponent(printTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(bEstimate)))
                .addGap(4, 4, 4)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(estimatedMaterial)
                        .addComponent(materialCost, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(b_changeFilament)
                .addContainerGap(69, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 203, 5));
        jPanel2.setMinimumSize(new java.awt.Dimension(20, 26));
        jPanel2.setPreferredSize(new java.awt.Dimension(20, 26));

        bCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bCancel.setText("CANCELAR");
        bCancel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bCancelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bCancelMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bCancelMousePressed(evt);
            }
        });

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_15.png"))); // NOI18N
        jLabel12.setText("IMPRIMIR");
        jLabel12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel12MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel12MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel12MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel12)
                .addGap(12, 12, 12))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bCancel)
                    .addComponent(jLabel12)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 435, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 549, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void densitySliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_densitySliderStateChanged

        int val = densitySlider.getValue();
        tfDensity.setText(String.valueOf(val));

        checkDensitySliderValue(val);
        checkChanges();
    }//GEN-LAST:event_densitySliderStateChanged

    private void jLabel12MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel12MouseEntered
        if (!no_Filament && printerAvailable) {
            jLabel12.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_15.png")));
        }
    }//GEN-LAST:event_jLabel12MouseEntered

    private void jLabel12MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel12MouseExited
        if (!no_Filament && printerAvailable) {
            jLabel12.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_15.png")));
        }
    }//GEN-LAST:event_jLabel12MouseExited

    private void bCancelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseEntered
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bCancelMouseEntered

    private void bCancelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseExited
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bCancelMouseExited

    private void jLabel12MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel12MousePressed

        if (t != null) {
            t.stop();
        }
        estimationThread.stop();

        if (no_Filament == false && Base.isPrinting == false) {
            /*
             * prefs[0] - profile (LOW,HIGH) 
             * prefs[1] - colorRation
             * prefs[2] - infill LOW,MEDIUM,HIGH) 
             * prefs[3] - RAFT (T/F)
             * prefs[4] - SUPPORT (T/F)
             */

            if (!checkChanges()) {
                Base.getMainWindow().getBed().setGcodeOK(false);
            }

            prefs.add(parseSlider1());
//            prefs.add(parseCoilCode());
            prefs.add(FilamentControler.getColor(colorCode));
            prefs.add(parseSlider2());
            prefs.add(String.valueOf(raftPressed));
            prefs.add(String.valueOf(supportPressed));
            prefs.add(String.valueOf(autonomousPressed));
            prefs.add(String.valueOf(gcodeToPrint));

            Base.getMainWindow().getBed().setLasRaft(raftPressed);
            Base.getMainWindow().getBed().setLastDensity(parseSlider2());
            Base.getMainWindow().getBed().setLastResolution(parseSlider1());
            Base.getMainWindow().getBed().setLastSupport(supportPressed);
            Base.getMainWindow().getBed().setLastAutonomous(autonomousPressed);

            Base.isPrinting = true;
            Base.getMainWindow().getButtons().blockModelsButton(true);
            Base.cleanDirectoryTempFiles(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER);
            dispose();
            Base.getMainWindow().getCanvas().unPickAll();
            isRunning = false;
            Base.getMainWindow().getButtons().updatePressedStateButton("print");
            Base.turnOnPowerSaving(false);

            if (autonomousPressed == false) {
                final PrintSplashSimple p = new PrintSplashSimple(prefs);
                p.setVisible(true);

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {

                        if (Base.getMainWindow().getBed().isSceneDifferent()) {
                            Base.getMainWindow().handleSave(true);
                        }
                        p.startConditions();
                    }
                });
            } else {
                final PrintSplashAutonomous p = new PrintSplashAutonomous(false, prefs);
                p.setVisible(true);

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {

                        if (Base.getMainWindow().getBed().isSceneDifferent()) {
                            Base.getMainWindow().handleSave(true);
                        }
                        p.startConditions();
                    }
                });
            }

        } else {
            t = new Thread(new Runnable() {
                private int counter = 0;
                private int tries = 0;

                @Override
                public void run() {

                    /**
                     * Possible cases:
                     *
                     * 1- noFilament -> Alert with red blinking text 2-
                     * isPrinting -> Alert with printing message 3- isDisconenct
                     * -> Alert with disconnect message
                     */
                    if (Base.getMachineLoader().isConnected() == false) {
                        Base.getMainWindow().showFeedBackMessage("btfDisconnect");
                    } else if (Base.isPrinting) {
                        Base.getMainWindow().showFeedBackMessage("btfPrinting");
                    }

                    while (tries < 3) {
                        counter++;
//                        SwingUtilities.invokeLater(new Runnable(){
//                            public void run() {
                        if (counter % 2 == 0) {
                            jLabel22.setForeground(Color.red);
                            jLabel23.setForeground(Color.red);
                            counter = 0;
                            tries++;
                        } else {
                            jLabel22.setForeground(new Color(0, 0, 0));
                            jLabel23.setForeground(new Color(0, 0, 0));
                        }
//                            }
//                        });
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Base.writeLog(e.getMessage());
                        }
                    }
                }
            });
            t.start();

        }
    }//GEN-LAST:event_jLabel12MousePressed

    private void bCancelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMousePressed
        doCancel();
    }//GEN-LAST:event_bCancelMousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        doCancel();
    }//GEN-LAST:event_jLabel15MousePressed

    private void jLabel4MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MousePressed
        triggerRaft();
    }//GEN-LAST:event_jLabel4MousePressed

    private void jLabel5MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MousePressed
        triggerSupport();
    }//GEN-LAST:event_jLabel5MousePressed

    private void jLabel7MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MousePressed
        triggerRaft();
    }//GEN-LAST:event_jLabel7MousePressed

    private void jLabel8MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MousePressed
        triggerRaft();
    }//GEN-LAST:event_jLabel8MousePressed

    private void jLabel9MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MousePressed
        triggerSupport();
    }//GEN-LAST:event_jLabel9MousePressed

    private void jLabel10MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10MousePressed
        triggerSupport();
    }//GEN-LAST:event_jLabel10MousePressed

    private void densitySliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_densitySliderMouseReleased
        lastSelectedDensity = parseSlider2();
    }//GEN-LAST:event_densitySliderMouseReleased

    private void bEstimateMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bEstimateMouseEntered
        if (estimatePressed == false) {
            bEstimate.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_15.png")));
        }
    }//GEN-LAST:event_bEstimateMouseEntered

    private void bEstimateMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bEstimateMouseExited
        if (estimatePressed == false) {
            bEstimate.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_15.png")));
        }
    }//GEN-LAST:event_bEstimateMouseExited

    private void bEstimateMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bEstimateMousePressed

        if (estimatePressed == false) {

            estimationThread = new PrintEstimationThread(this);
            bEstimate.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_15.png")));
            estimatePressed = true;
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    estimationThread.start();
                    Base.systemThreads.add(estimationThread);
                }
            });
        }
    }//GEN-LAST:event_bEstimateMousePressed

    private void tfDensityKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfDensityKeyReleased
        String tfDesty_value = tfDensity.getText();
        boolean isNumber = true;
        try {
            double d = Double.parseDouble(tfDesty_value);
        } catch (NumberFormatException nfe) {
            isNumber = false;
        }

        if (tfDesty_value.equals("") == false && isNumber) {
            int densityValue = Integer.valueOf(tfDensity.getText());
            if (densityValue < 0 || densityValue > 100) {
                densityValue = 5;
            }
            densitySlider.setValue(densityValue);
            checkDensitySliderValue(densityValue);
        }
    }//GEN-LAST:event_tfDensityKeyReleased

    private void b_mediumResActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_mediumResActionPerformed
        b_mediumRes.setSelected(true);
    }//GEN-LAST:event_b_mediumResActionPerformed

    private void b_highPlusResActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_highPlusResActionPerformed
        b_highPlusRes.setSelected(true);
    }//GEN-LAST:event_b_highPlusResActionPerformed

    private void b_lowResActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_lowResActionPerformed
        b_lowRes.setSelected(true);
    }//GEN-LAST:event_b_lowResActionPerformed

    private void b_highResActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_highResActionPerformed
        b_highRes.setSelected(true);
    }//GEN-LAST:event_b_highResActionPerformed

    private void b_changeFilamentMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_b_changeFilamentMouseEntered
        b_changeFilament.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_16.png")));
    }//GEN-LAST:event_b_changeFilamentMouseEntered

    private void b_changeFilamentMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_b_changeFilamentMouseExited
        b_changeFilament.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_16.png")));
    }//GEN-LAST:event_b_changeFilamentMouseExited

    private void b_changeFilamentMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_b_changeFilamentMousePressed
        dispose();
        Base.getMainWindow().getButtons().updatePressedStateButton("print");
        Base.getMainWindow().getButtons().goFilamentChange();
        ProperDefault.put("maintenance", "1");
        FilamentHeating p = new FilamentHeating();
        p.setVisible(true);
    }//GEN-LAST:event_b_changeFilamentMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bCancel;
    private javax.swing.JLabel bEstimate;
    private javax.swing.JLabel b_changeFilament;
    private javax.swing.JRadioButton b_highPlusRes;
    private javax.swing.JRadioButton b_highRes;
    private javax.swing.JRadioButton b_lowRes;
    private javax.swing.JRadioButton b_mediumRes;
    private javax.swing.ButtonGroup b_resButtonGroup;
    private javax.swing.JSlider densitySlider;
    private javax.swing.JLabel estimatedMaterial;
    private javax.swing.JLabel estimatedPrintTime;
    private javax.swing.JLabel filamentType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lDensity;
    private javax.swing.JLabel loading;
    private javax.swing.JLabel materialCost;
    private javax.swing.JLabel printTime;
    private javax.swing.JTextField tfDensity;
    // End of variables declaration//GEN-END:variables
}

class PrintEstimationThread extends Thread {

    private final PrintPanel printPanel;
    private int nTimes;

    public PrintEstimationThread(PrintPanel panel) {
        super("Print panel Estimation Thread");
        this.printPanel = panel;
        this.nTimes = 0;
    }

    /**
     * Runs GCode generator process. Updates window fields to display print
     * model cost estimation.
     */
    public void runEstimator() {
        ArrayList<String> preferences = printPanel.getPreferences();
        Printer prt = new Printer(preferences);
        prt.generateGCode(preferences);
        File gcode = prt.getGCode();
        //Estimate time and cost
        PrintEstimator.estimateTime(gcode);
        printPanel.updateEstimationPanel(PrintEstimator.getEstimatedTime(), PrintEstimator.getEstimatedCost());
    }

    @Override
    public void run() {

        while (printPanel.isRunning()) {
            try {
                Thread.sleep(5, 0);
            } catch (InterruptedException ex) {
                Logger.getLogger(PrintEstimationThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (!printPanel.settingsChanged() || nTimes == 0) {
                printPanel.showLoadingIcon(true);

                if (Base.getMainWindow().isOkToGoOnSave() == false) {
                    Base.getMainWindow().handleSave(true);
                }
                runEstimator();
                printPanel.updateOldSettings();
                nTimes++;
                printPanel.showLoadingIcon(false);
                Base.cleanDirectoryTempFiles(Base.getAppDataDirectory().getAbsolutePath() + "/" + Base.MODELS_FOLDER + "/");
                this.stop();

            } else {
                Base.cleanDirectoryTempFiles(Base.getAppDataDirectory().getAbsolutePath() + "/" + Base.MODELS_FOLDER + "/");
            }


        }
    }
}
