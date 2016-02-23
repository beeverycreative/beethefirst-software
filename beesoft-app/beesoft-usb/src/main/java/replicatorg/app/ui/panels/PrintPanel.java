package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import pt.beeverycreative.beesoft.drivers.usb.PrinterInfo;
import pt.beeverycreative.beesoft.filaments.Filament;
import replicatorg.app.Base;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
import pt.beeverycreative.beesoft.filaments.PrintPreferences;
import pt.beeverycreative.beesoft.filaments.Resolution;
import replicatorg.app.Languager;
import replicatorg.app.PrintEstimator;
import replicatorg.app.Printer;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.drivers.Driver;
import replicatorg.machine.model.MachineModel;

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

    private static final String FORMAT = "%2d:%2d";
    private static final Driver driver = Base.getMachineLoader().getMachineInterface().getDriver();
    private static final MachineModel machineModel = Base.getMainWindow().getMachine().getModel();
    private final boolean isPrinterConnected = Base.getMachineLoader().isConnected();
    private final int nozzleType;
    private final Hashtable<Integer, JLabel> labelTable2 = new Hashtable<Integer, JLabel>();
    private JLabel lowQuality, mediumQuality, solidQuality;
    private boolean gcodeSavePressed, lastUsedRaft, lastUsedSupport,
            lastSelectedRaft, lastUsedGCodeSave, estimatePressed, exportPressed,
            printerAvailable, lastSelectedSupport, gcodeOK, noFilament = false,
            noNozzle = false, raftPressed = false, supportPressed = false,
            atLeastOneResEnabled = false;
    private String lastUsedResolution, lastSelectedResolution,
            gcodeToPrint = null, filament;
    private int lastUsedDensity, nModels = 0;
    private PrintEstimationThread estimationThread;
    private GCodeExportThread exportThread;
    private PrinterInfo selectedPrinter = PrinterInfo.BEETHEFIRST;

    public PrintPanel() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        nozzleType = getNozzleType();
        filament = getCoilCode();
        verifyCompatibleResolutions();
        initSlidersLabels();
        setFont();
        setTextLanguage();
        initSliderConfigs();
        centerOnScreen();
        evaluateConditions();
        matchChanges();
        enableDrag();
        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (estimationThread != null) {
                    estimationThread.kill();
                }
                if (exportThread != null) {
                    exportThread.kill();
                }
            }
        });
    }

    private void verifyCompatibleResolutions() {
        Filament fil;
        List<Resolution> resList;

        fil = FilamentControler.getMatchingFilament(filament);

        if (fil != null) {
            resList = fil.getSupportedResolutions(driver.getConnectedDevice().filamentCode(), nozzleType);

            if (resList != null) {
                for (Resolution res : resList) {
                    if (res.getType().equals("low")) {
                        atLeastOneResEnabled = true;
                        bLowRes.setEnabled(true);
                    } else if (res.getType().equals("medium")) {
                        atLeastOneResEnabled = true;
                        bMediumRes.setEnabled(true);
                    } else if (res.getType().equals("high")) {
                        atLeastOneResEnabled = true;
                        bHighRes.setEnabled(true);
                    } else if (res.getType().equals("high+")) {
                        atLeastOneResEnabled = true;
                        bHighPlusRes.setEnabled(true);
                    }
                }
            }
        } else {
            atLeastOneResEnabled = true;
            bLowRes.setEnabled(true);
            bMediumRes.setEnabled(true);
            bHighRes.setEnabled(true);
            bHighPlusRes.setEnabled(true);
        }

        if (bMediumRes.isEnabled()) {
            bMediumRes.setSelected(true);
        } else if (bLowRes.isEnabled()) {
            bLowRes.setSelected(true);
        } else if (bHighRes.isEnabled()) {
            bHighRes.setSelected(true);
        } else if (bHighPlusRes.isEnabled()) {
            bHighPlusRes.setSelected(true);
        }
    }

    /**
     * Set font for all UI elements.
     */
    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProRegular("14"));
        jLabel2.setFont(GraphicDesignComponents.getSSProBold("14"));
        jLabel3.setFont(GraphicDesignComponents.getSSProBold("14"));
        jLabel5.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel7.setFont(GraphicDesignComponents.getSSProBold("12"));
        jLabel8.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel9.setFont(GraphicDesignComponents.getSSProBold("12"));
        jLabel10.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bCancel.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bPrint.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bChangeFilament.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bEstimate.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bExport.setFont(GraphicDesignComponents.getSSProRegular("12"));
        lDensity.setFont(GraphicDesignComponents.getSSProRegular("12"));

        //Resolution
        bLowRes.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bMediumRes.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bHighRes.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bHighPlusRes.setFont(GraphicDesignComponents.getSSProRegular("12"));

        lowHeightLabel.setFont(GraphicDesignComponents.getSSProRegular("12"));
        medHeightLabel.setFont(GraphicDesignComponents.getSSProRegular("12"));
        highHeightLabel.setFont(GraphicDesignComponents.getSSProRegular("12"));
        highPlusHeightLabel.setFont(GraphicDesignComponents.getSSProRegular("12"));

        //Density
        lowQuality.setFont(GraphicDesignComponents.getSSProRegular("12"));
        mediumQuality.setFont(GraphicDesignComponents.getSSProRegular("12"));
        solidQuality.setFont(GraphicDesignComponents.getSSProRegular("12"));

    }

    /**
     * Set copy for all UI elements.
     */
    private void setTextLanguage() {
        jLabel1.setText(Languager.getTagValue(1, "MainWindowButtons", "Print"));
        jLabel2.setText(Languager.getTagValue(1, "Print", "Print_Quality"));
        jLabel3.setText(Languager.getTagValue(1, "Print", "Print_Density"));
        jLabel7.setText(Languager.getTagValue(1, "Print", "Print_Raft"));
        jLabel8.setText("<html>" + Languager.getTagValue(1, "Print", "Print_Raft_Info") + "</html>");
        jLabel9.setText(Languager.getTagValue(1, "Print", "Print_Support"));
        jLabel10.setText("<html>" + Languager.getTagValue(1, "Print", "Print_Support_Info") + "</html>");
        bCancel.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line3"));
        bEstimate.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line15"));
        bExport.setText(Languager.getTagValue(1, "OptionPaneButtons", "Export"));
        bPrint.setText(Languager.getTagValue(1, "ToolPath", "Line21"));
        bChangeFilament.setText(Languager.getTagValue(1, "Print", "Print_ChangeFilament"));
        filamentType.setText(Languager.getTagValue(1, "MaintenancePanel", "Filament_Type"));
        estimatedPrintTime.setText(Languager.getTagValue(1, "Print", "Print_EstimationPrintTime"));
        estimatedMaterial.setText(Languager.getTagValue(1, "Print", "Print_EstimationMaterial"));
        lDensity.setText(Languager.getTagValue(1, "Print", "Print_Density_Insertion"));

        bLowRes.setText(Languager.getTagValue(1, "Print", "Print_Quality_Low"));
        bMediumRes.setText(Languager.getTagValue(1, "Print", "Print_Quality_Medium"));
        bHighRes.setText(Languager.getTagValue(1, "Print", "Print_Quality_High"));
        bHighPlusRes.setText(Languager.getTagValue(1, "Print", "Print_Quality_SHigh"));

        lowQuality.setText(Languager.getTagValue(1, "Print", "Print_Density_Low"));
        mediumQuality.setText(Languager.getTagValue(1, "Print", "Print_Density_Medium"));
        solidQuality.setText(Languager.getTagValue(1, "Print", "Print_Density_High"));

    }

    /**
     * Get coil code from machine and system. Also prepares label with coil code
     * info.
     */
    private String getCoilCode() {

        String code;

        code = FilamentControler.NO_FILAMENT;

        if (isPrinterConnected) {
            code = machineModel.getCoilText();
        } //no need for else

        Base.writeLog("Print panel coil code: " + code, this.getClass());

        if (code.equals(FilamentControler.NO_FILAMENT)
                || code.contains(FilamentControler.NO_FILAMENT_2)) {
            noFilament = true;
            jLabel22.setFont(GraphicDesignComponents.getSSProBold("10"));
            code = Languager.getTagValue(1, "Print", "Print_Splash_Info9").toUpperCase();
            jLabel22.setText(code);
            jLabel23.setText(Languager.getTagValue(1, "Print", "Print_Splash_Info11"));
        } else if (FilamentControler.colorExistsLocally(code) == false) {
            noFilament = true;
            jLabel22.setFont(GraphicDesignComponents.getSSProBold("10"));
            jLabel22.setText(code);

            UnknownFilament unkFilPanel = new UnknownFilament();
            unkFilPanel.setVisible(true);
        } else {
            jLabel22.setText(code);
        }

        return code;
    }

    private int getNozzleType() {
        int nozzle;

        nozzle = FilamentControler.NO_NOZZLE;

        if (isPrinterConnected) {
            nozzle = machineModel.getNozzleType();
        }

        Base.writeLog("Nozzle type: " + nozzle, this.getClass());

        if (nozzle == 0) {
            noNozzle = true;
            nozzleTypeValue.setFont(GraphicDesignComponents.getSSProBold("12"));
            nozzleTypeValue.setText("0");
        } else {
            nozzleTypeValue.setText(Float.toString(nozzle / 1000.0f)); // from microns to mm
        }

        return nozzle;
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
        lowQuality = new JLabel("light");
        mediumQuality = new JLabel("medium");
        solidQuality = new JLabel("solid");
    }

    /**
     * Parses resolution buttons.
     *
     * @return resolution active button value
     */
    private String parseSlider1() {

        for (Enumeration<AbstractButton> buttons = bResButtonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                String labelText = button.getText();
                if (labelText.contains(Languager.getTagValue(1, "Print", "Print_Quality_Low"))) {
                    return "low";
                } else if (labelText.contains(Languager.getTagValue(1, "Print", "Print_Quality_Medium"))) {
                    return "medium";
                } else if (labelText.contains(Languager.getTagValue(1, "Print", "Print_Quality_SHigh"))) {
                    return "high+";
                } else if (labelText.contains(Languager.getTagValue(1, "Print", "Print_Quality_High"))) {
                    return "high";
                }
            }
        }

        return "low";
    }

    /**
     * Parses density slider.
     *
     * @return density slider value
     */
    private int parseSlider2() {
        return densitySlider.getValue();
    }

    /**
     * Evaluates initial conditions for on form load.
     */
    private void evaluateConditions() {

        lastUsedRaft = Base.getMainWindow().getBed().isLastRaft();
        lastUsedDensity = Base.getMainWindow().getBed().getLastDensity();
        lastUsedResolution = Base.getMainWindow().getBed().getLastResolution();
        lastUsedSupport = Base.getMainWindow().getBed().isLastSupport();
        gcodeOK = Base.getMainWindow().getBed().isGcodeOK();
        nModels = Base.getMainWindow().getBed().getNumberModels();

        materialCost.setText("N/A");
        showLoadingIcon(false);
        updateOldSettings();

        if (noFilament) {
            jLabel22.setForeground(Color.red);
            jLabel23.setForeground(Color.red);
        }

        printerAvailable = Base.getMachineLoader().isConnected() && !Base.isPrinting;

        if (nModels > 0 && atLeastOneResEnabled) {
            if (printerAvailable && noFilament == false && noNozzle == false && Base.isPrinting == false) {
                bPrint.setEnabled(true);
            }
            bEstimate.setEnabled(true);
            bExport.setEnabled(true);
        }

        if (printerAvailable) {
            bChangeFilament.setEnabled(true);
        }

        bResButtonGroup.add(bLowRes);
        bResButtonGroup.add(bHighPlusRes);
        bResButtonGroup.add(bHighRes);
        bResButtonGroup.add(bMediumRes);
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

    public void showLoadingIcon(boolean show) {
        loading.setEnabled(show);

        if (show) {
            materialCost.setText("N/A");
            printTime.setText("N/A");
            bEstimate.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_15.png")));
        } else {
            bEstimate.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_15.png")));
            estimatePressed = false;
        }
    }

    public void showLoadingIconExport(boolean show) {
        loading.setEnabled(show);

        if (show) {
            materialCost.setText("N/A");
            printTime.setText("N/A");
            exportPressed = true;
            bExport.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_19.png")));
        } else {
            bExport.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_19.png")));
            exportPressed = false;
        }
    }

    /**
     * Updates all fields to stored setting, on form load.
     */
    private void matchChanges() {

        lastUsedRaft = Base.getMainWindow().getBed().isLastRaft();
        lastUsedDensity = Base.getMainWindow().getBed().getLastDensity();
        lastUsedResolution = Base.getMainWindow().getBed().getLastResolution();
        lastUsedSupport = Base.getMainWindow().getBed().isLastSupport();

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

        densitySlider.setValue(lastUsedDensity);

        if (lastUsedResolution.contains("medium") && bMediumRes.isEnabled()) {
            bMediumRes.setSelected(true);
        } else if (lastUsedResolution.contains("low") && bLowRes.isEnabled()) {
            bLowRes.setSelected(true);
        } else if (lastUsedResolution.contains("high") && bHighRes.isEnabled()) {
            bHighRes.setSelected(true);
        } else if (lastUsedResolution.contains("high+") && bHighPlusRes.isEnabled()) {
            bHighPlusRes.setSelected(true);
        }

        checkChanges();
    }

    /**
     * Checks if current settings match old ones.
     *
     * @return
     */
    public boolean checkChanges() {
        boolean equal = false;

        lastUsedRaft = Base.getMainWindow().getBed().isLastRaft();
        lastUsedDensity = Base.getMainWindow().getBed().getLastDensity();
        lastUsedResolution = Base.getMainWindow().getBed().getLastResolution();
        lastUsedSupport = Base.getMainWindow().getBed().isLastSupport();
        gcodeOK = Base.getMainWindow().getBed().isGcodeOK();
        lastUsedGCodeSave = Base.getMainWindow().getBed().isGCodeSave();

        if (parseSlider1().equals(lastUsedResolution)
                && parseSlider2() == lastUsedDensity
                && raftPressed == lastUsedRaft
                && supportPressed == lastUsedSupport
                && gcodeSavePressed == lastUsedGCodeSave
                && gcodeOK) {
            equal = true;
        }

        return equal;
    }

    /**
     * Checks if settings that affect gcode were changed.
     *
     * @return
     */
    public boolean settingsChanged() {

        return parseSlider1().equals(lastSelectedResolution)
                && parseSlider2() == lastUsedDensity
                && raftPressed == lastSelectedRaft
                && supportPressed == lastSelectedSupport;
    }

    /**
     * Updates old print settings with new ones.
     */
    public void updateOldSettings() {
        lastSelectedRaft = raftPressed;
        lastUsedDensity = parseSlider2();
        lastSelectedResolution = parseSlider1();
        lastSelectedSupport = supportPressed;
    }

    /**
     * Cancel event.
     */
    private void doCancel() {
        dispose();
        Base.cleanDirectoryTempFiles(Base.getAppDataDirectory().getAbsolutePath() + "/" + Base.MODELS_FOLDER + "/");
        Base.getMainWindow().getButtons().updatePressedStateButton("print");
        Base.bringAllWindowsToFront();
    }

    /**
     * Gets print parameters from selected configurations on window.
     *
     * @return print parameters.
     */
    public PrintPreferences getPreferences() {
        PrintPreferences preferences;
        String resolution;
        int density;

        resolution = parseSlider1();
        density = parseSlider2();

        if (gcodeToPrint != null && printerAvailable == false) {
            preferences = new PrintPreferences(resolution, filament, density, nozzleType, raftPressed, supportPressed, gcodeToPrint, selectedPrinter);
        } else if (gcodeToPrint != null) {
            preferences = new PrintPreferences(resolution, filament, density, nozzleType, raftPressed, supportPressed, gcodeToPrint);
        } else if (printerAvailable == false) {
            preferences = new PrintPreferences(resolution, filament, density, nozzleType, raftPressed, supportPressed, selectedPrinter);
        } else {
            preferences = new PrintPreferences(resolution, filament, density, nozzleType, raftPressed, supportPressed);
        }

        return preferences;
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

    private void flashNoFilamentMessage() {
        int tries = 0, counter = 0;

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
                Base.writeLog(e.getMessage(), this.getClass());
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bResButtonGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        densitySlider = new javax.swing.JSlider();
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
        bLowRes = new javax.swing.JRadioButton();
        bMediumRes = new javax.swing.JRadioButton();
        bHighRes = new javax.swing.JRadioButton();
        bHighPlusRes = new javax.swing.JRadioButton();
        lowHeightLabel = new javax.swing.JLabel();
        medHeightLabel = new javax.swing.JLabel();
        highHeightLabel = new javax.swing.JLabel();
        highPlusHeightLabel = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        bChangeFilament = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        bCancel = new javax.swing.JLabel();
        bPrint = new javax.swing.JLabel();
        bExport = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        nozzleTypeLabel = new javax.swing.JLabel();
        nozzleTypeValue = new javax.swing.JLabel();
        mmLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(248, 248, 248));
        setUndecorated(true);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));

        jPanel4.setBackground(new java.awt.Color(248, 248, 248));
        jPanel4.setMinimumSize(new java.awt.Dimension(62, 26));
        jPanel4.setPreferredSize(new java.awt.Dimension(70, 30));
        jPanel4.setRequestFocusEnabled(false);

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_pressed_9.png"))); // NOI18N
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
                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        densitySlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                densitySliderStateChanged(evt);
            }
        });
        densitySlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                densitySliderMouseReleased(evt);
            }
        });

        filamentType.setFont(GraphicDesignComponents.getSSProRegular("12"));
        filamentType.setText("Filament Type");
        filamentType.setMaximumSize(new java.awt.Dimension(100, 13));
        filamentType.setMinimumSize(new java.awt.Dimension(100, 13));

        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/arrow_ajuda_2.png"))); // NOI18N

        jLabel22.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel22.setText("NO_FILAMENT");

        jLabel23.setFont(GraphicDesignComponents.getSSProRegular("10"));
        jLabel23.setText(" ");

        jLabel24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/arrow_ajuda_2.png"))); // NOI18N

        estimatedPrintTime.setFont(GraphicDesignComponents.getSSProRegular("12"));
        estimatedPrintTime.setText("Print time:");
        estimatedPrintTime.setMaximumSize(new java.awt.Dimension(100, 18));
        estimatedPrintTime.setMinimumSize(new java.awt.Dimension(100, 18));
        estimatedPrintTime.setPreferredSize(new java.awt.Dimension(70, 18));

        jLabel26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/arrow_ajuda_2.png"))); // NOI18N

        estimatedMaterial.setFont(GraphicDesignComponents.getSSProRegular("12"));
        estimatedMaterial.setText("Material cost:");
        estimatedMaterial.setMaximumSize(new java.awt.Dimension(100, 13));
        estimatedMaterial.setMinimumSize(new java.awt.Dimension(100, 13));
        estimatedMaterial.setPreferredSize(new java.awt.Dimension(100, 13));

        printTime.setFont(GraphicDesignComponents.getSSProRegular("12"));
        printTime.setText("N/A");

        materialCost.setFont(GraphicDesignComponents.getSSProRegular("12"));
        materialCost.setText("N/A");

        loading.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/loading.gif"))); // NOI18N
        loading.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/loading-disabled.gif"))); // NOI18N
        loading.setEnabled(false);
        loading.setFocusable(false);
        loading.setIconTextGap(0);
        loading.setRequestFocusEnabled(false);
        loading.setVerifyInputWhenFocusTarget(false);

        bEstimate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_15.png"))); // NOI18N
        bEstimate.setText("Estimate");
        bEstimate.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        bEstimate.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_15.png"))); // NOI18N
        bEstimate.setEnabled(false);
        bEstimate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bEstimate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bEstimateMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bEstimateMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bEstimateMouseEntered(evt);
            }
        });

        tfDensity.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfDensity.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfDensityKeyReleased(evt);
            }
        });

        lDensity.setText("Density value (%) :");

        bLowRes.setBackground(new java.awt.Color(248, 248, 248));
        bLowRes.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        bLowRes.setText("LOW");
        bLowRes.setEnabled(false);
        bLowRes.setPreferredSize(new java.awt.Dimension(100, 23));
        bLowRes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bLowResActionPerformed(evt);
            }
        });

        bMediumRes.setBackground(new java.awt.Color(248, 248, 248));
        bMediumRes.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        bMediumRes.setText("MEDIUM");
        bMediumRes.setEnabled(false);
        bMediumRes.setPreferredSize(new java.awt.Dimension(100, 23));
        bMediumRes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bMediumResActionPerformed(evt);
            }
        });

        bHighRes.setBackground(new java.awt.Color(248, 248, 248));
        bHighRes.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        bHighRes.setText("HIGH");
        bHighRes.setEnabled(false);
        bHighRes.setPreferredSize(new java.awt.Dimension(100, 23));
        bHighRes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bHighResActionPerformed(evt);
            }
        });

        bHighPlusRes.setBackground(new java.awt.Color(248, 248, 248));
        bHighPlusRes.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        bHighPlusRes.setText("HIGH+");
        bHighPlusRes.setEnabled(false);
        bHighPlusRes.setPreferredSize(new java.awt.Dimension(100, 23));
        bHighPlusRes.setRequestFocusEnabled(false);
        bHighPlusRes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bHighPlusResActionPerformed(evt);
            }
        });

        lowHeightLabel.setText("0.3 mm");

        medHeightLabel.setText("0.2 mm");

        highHeightLabel.setText("0.1 mm");

        highPlusHeightLabel.setText("0.05 mm");

        jPanel6.setMaximumSize(new java.awt.Dimension(176, 150));
        jPanel6.setMinimumSize(new java.awt.Dimension(176, 150));
        jPanel6.setOpaque(false);
        jPanel6.setPreferredSize(new java.awt.Dimension(176, 150));

        jLabel10.setText("Suspendisse potenti.");
        jLabel10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel10MousePressed(evt);
            }
        });

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/c_unchecked.png"))); // NOI18N
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel4MousePressed(evt);
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

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/c_unchecked.png"))); // NOI18N
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel5MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10)))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addGap(6, 6, 6)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel9))
                .addGap(6, 6, 6)
                .addComponent(jLabel10)
                .addContainerGap())
        );

        bChangeFilament.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_16.png"))); // NOI18N
        bChangeFilament.setText("Change filament");
        bChangeFilament.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_16.png"))); // NOI18N
        bChangeFilament.setEnabled(false);
        bChangeFilament.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bChangeFilament.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bChangeFilamentMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bChangeFilamentMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bChangeFilamentMouseEntered(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(255, 203, 5));
        jPanel2.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel2.setPreferredSize(new java.awt.Dimension(20, 26));

        bCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bCancel.setText("CANCELAR");
        bCancel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bCancelMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bCancelMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bCancelMouseEntered(evt);
            }
        });

        bPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_15.png"))); // NOI18N
        bPrint.setText("IMPRIMIR");
        bPrint.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_15.png"))); // NOI18N
        bPrint.setEnabled(false);
        bPrint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bPrint.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bPrintMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bPrintMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bPrintMouseEntered(evt);
            }
        });

        bExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_19.png"))); // NOI18N
        bExport.setText("Export G-code");
        bExport.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        bExport.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_19.png"))); // NOI18N
        bExport.setEnabled(false);
        bExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bExport.setMaximumSize(new java.awt.Dimension(120, 23));
        bExport.setMinimumSize(new java.awt.Dimension(115, 23));
        bExport.setPreferredSize(new java.awt.Dimension(115, 23));
        bExport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bExportMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bExportMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bExportMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(bCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bExport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bPrint)
                .addGap(2, 2, 2))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bCancel)
                    .addComponent(bPrint)
                    .addComponent(bExport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jLabel25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/arrow_ajuda_2.png"))); // NOI18N

        nozzleTypeLabel.setFont(GraphicDesignComponents.getSSProRegular("12"));
        nozzleTypeLabel.setText("Nozzle type:");
        nozzleTypeLabel.setMaximumSize(new java.awt.Dimension(100, 18));
        nozzleTypeLabel.setMinimumSize(new java.awt.Dimension(100, 18));
        nozzleTypeLabel.setPreferredSize(new java.awt.Dimension(70, 18));

        nozzleTypeValue.setFont(GraphicDesignComponents.getSSProRegular("12"));
        nozzleTypeValue.setText("0.0");

        mmLabel.setFont(GraphicDesignComponents.getSSProRegular("12"));
        mmLabel.setText("mm");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(estimatedMaterial, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(estimatedPrintTime, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(15, 15, 15)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(printTime)
                                            .addComponent(materialCost)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(nozzleTypeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(15, 15, 15)
                                        .addComponent(nozzleTypeValue)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(mmLabel)))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(filamentType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(15, 15, 15)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(bEstimate))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel22)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(loading)))))
                        .addGap(19, 19, 19))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(densitySlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lDensity)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfDensity, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(34, 34, 34))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(bLowRes, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(21, 21, 21)
                                        .addComponent(lowHeightLabel)))
                                .addGap(8, 8, 8)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(21, 21, 21)
                                        .addComponent(medHeightLabel))
                                    .addComponent(bMediumRes, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(8, 8, 8)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(bHighRes, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(21, 21, 21)
                                        .addComponent(highHeightLabel)))))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addComponent(highPlusHeightLabel)
                                .addGap(0, 28, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addComponent(bHighPlusRes, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())))))
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(148, 148, 148)
                .addComponent(bChangeFilament, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
                    .addComponent(jSeparator1))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bLowRes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bHighRes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bMediumRes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bHighPlusRes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lowHeightLabel)
                    .addComponent(medHeightLabel)
                    .addComponent(highHeightLabel)
                    .addComponent(highPlusHeightLabel))
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(densitySlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfDensity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lDensity))
                .addGap(16, 16, 16)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(loading, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(filamentType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bEstimate)
                    .addComponent(jLabel23))
                .addGap(0, 0, 0)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(nozzleTypeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(nozzleTypeValue)
                        .addComponent(mmLabel))
                    .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(estimatedPrintTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(printTime))
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(estimatedMaterial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(materialCost)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addComponent(bChangeFilament)
                .addGap(18, 27, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bPrintMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPrintMouseEntered
        bPrint.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_15.png")));
    }//GEN-LAST:event_bPrintMouseEntered

    private void bPrintMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPrintMouseExited
        bPrint.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_15.png")));
    }//GEN-LAST:event_bPrintMouseExited

    private void bCancelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseEntered
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bCancelMouseEntered

    private void bCancelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseExited
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bCancelMouseExited

    private void bPrintMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPrintMousePressed
        if (bPrint.isEnabled()) {
            if (!checkChanges()) {
                Base.getMainWindow().getBed().setGcodeOK(false);
            }

            Base.getMainWindow().getBed().setLastRaft(raftPressed);
            Base.getMainWindow().getBed().setLastDensity(parseSlider2());
            Base.getMainWindow().getBed().setLastResolution(parseSlider1());
            Base.getMainWindow().getBed().setLastSupport(supportPressed);

            Base.isPrinting = true;
            Base.getMainWindow().getButtons().blockModelsButton(true);
            Base.cleanDirectoryTempFiles(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER);
            dispose();
            Base.getMainWindow().getCanvas().unPickAll();
            Base.getMainWindow().getButtons().updatePressedStateButton("print");
            Base.turnOnPowerSaving(false);

            final PrintSplashAutonomous p = new PrintSplashAutonomous(false, getPreferences());
            p.setVisible(true);

            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {

                    if (Base.getMainWindow().getBed().isSceneDifferent()) {
                        Base.getMainWindow().handleSave(true);
                    }
                }
            });

        } else {
            EventQueue.invokeLater(new Runnable() {
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
                    } else if (nModels == 0) {
                        Base.getMainWindow().showFeedBackMessage("noModelError");
                    } else if (noFilament) {
                        flashNoFilamentMessage();
                    }
                }
            });
        }
    }//GEN-LAST:event_bPrintMousePressed

    private void bCancelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMousePressed
        doCancel();
    }//GEN-LAST:event_bCancelMousePressed

    private void bExportMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExportMousePressed

        // if there are any loaded models, export
        if (exportPressed == false && bExport.isEnabled()) {

            if (printerAvailable == false
                    || filament.equals(FilamentControler.NO_FILAMENT)
                    || filament.contains(FilamentControler.NO_FILAMENT_2)
                    || FilamentControler.colorExistsLocally(filament) == false) {
                ProfileAndPrinter selection
                        = new ProfileAndPrinter(this, printerAvailable);
                selection.setVisible(true);
                filament = selection.getCoilText();
                selectedPrinter = selection.getSelectedPrinter();
            }

            // if the ProfileAndPrinter panel was closed on X button,
            // cancel the estimation process
            if (filament.equals(FilamentControler.NO_FILAMENT)
                    || filament.contains(FilamentControler.NO_FILAMENT_2)
                    || FilamentControler.colorExistsLocally(filament) == false
                    || selectedPrinter == PrinterInfo.UNKNOWN) {
                return;
            }

            JFileChooser saveFile = new JFileChooser();
            saveFile.setSelectedFile(new File("export-"
                    + System.currentTimeMillis() + ".gcode"));
            int rVal = saveFile.showSaveDialog(null);

            if (rVal == JFileChooser.APPROVE_OPTION) {
                exportThread = new GCodeExportThread(saveFile.getSelectedFile());

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        exportThread.start();
                    }
                });
            }
        } else if (bExport.isEnabled() == false) { // otherwise warn the user that there are no models loaded
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (printerAvailable == false) {
                        Base.getMainWindow().showFeedBackMessage("btfDisconnect");
                    } else if (nModels == 0) {
                        Base.getMainWindow().showFeedBackMessage("noModelError");
                    } else if (noFilament) {
                        flashNoFilamentMessage();
                    }
                }
            });
            thread.start();
        }
    }//GEN-LAST:event_bExportMousePressed

    private void bExportMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExportMouseExited
        // don't change the icon if button is disabled due to the absence of loaded models
        if (exportPressed == false && bExport.isEnabled()) {
            bExport.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_19.png")));
        }
    }//GEN-LAST:event_bExportMouseExited

    private void bExportMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExportMouseEntered
        // don't change the icon if button is disabled due to the absence of loaded models
        if (exportPressed == false && bExport.isEnabled()) {
            bExport.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_19.png")));
        }
    }//GEN-LAST:event_bExportMouseEntered

    private void bChangeFilamentMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bChangeFilamentMouseEntered
        if (bChangeFilament.isEnabled()) {
            bChangeFilament.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_16.png")));
        }
    }//GEN-LAST:event_bChangeFilamentMouseEntered

    private void bChangeFilamentMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bChangeFilamentMouseExited
        if (bChangeFilament.isEnabled()) {
            bChangeFilament.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_16.png")));
        }
    }//GEN-LAST:event_bChangeFilamentMouseExited

    private void bChangeFilamentMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bChangeFilamentMousePressed
        // if the printer is available, display the filament change screen
        if (bChangeFilament.isEnabled()) {
            dispose();
            Base.getMainWindow().getButtons().updatePressedStateButton("print");
            Base.getMainWindow().getButtons().goFilamentChange();
            ProperDefault.put("maintenance", "1");
            FilamentCodeInsertion p = new FilamentCodeInsertion();
            p.setVisible(true);
        } else {
            // otherwise display the appropriate status message
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (Base.getMachineLoader().isConnected() == false) {
                        Base.getMainWindow().showFeedBackMessage("btfDisconnect");
                    } else if (Base.isPrinting) {
                        Base.getMainWindow().showFeedBackMessage("btfPrinting");
                    }
                }
            });
            thread.start();
        }
    }//GEN-LAST:event_bChangeFilamentMousePressed

    private void bHighPlusResActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bHighPlusResActionPerformed
        bHighPlusRes.setSelected(true);
    }//GEN-LAST:event_bHighPlusResActionPerformed

    private void bHighResActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bHighResActionPerformed
        bHighRes.setSelected(true);
    }//GEN-LAST:event_bHighResActionPerformed

    private void bMediumResActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bMediumResActionPerformed
        bMediumRes.setSelected(true);
    }//GEN-LAST:event_bMediumResActionPerformed

    private void bLowResActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bLowResActionPerformed
        bLowRes.setSelected(true);
    }//GEN-LAST:event_bLowResActionPerformed

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

    private void bEstimateMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bEstimateMouseEntered
        // don't change the icon if button is disabled due to the absence of loaded models
        if (estimatePressed == false && bEstimate.isEnabled()) {
            bEstimate.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_15.png")));
        }
    }//GEN-LAST:event_bEstimateMouseEntered

    private void bEstimateMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bEstimateMouseExited
        // don't change the icon if button is disabled due to the absence of loaded models
        if (estimatePressed == false && bEstimate.isEnabled()) {
            bEstimate.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_15.png")));
        }
    }//GEN-LAST:event_bEstimateMouseExited

    private void bEstimateMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bEstimateMousePressed

        // if there are any loaded model, do the estimation
        if (estimatePressed == false && bEstimate.isEnabled()) {
            if (printerAvailable == false
                    || filament.equals(FilamentControler.NO_FILAMENT)
                    || filament.contains(FilamentControler.NO_FILAMENT_2)
                    || FilamentControler.colorExistsLocally(filament) == false) {
                Base.writeLog("No printer available, opening selection panel", this.getClass());
                ProfileAndPrinter selection
                        = new ProfileAndPrinter(this, printerAvailable);
                selection.setVisible(true);
                filament = selection.getCoilText();
                selectedPrinter = selection.getSelectedPrinter();
                Base.writeLog(filament + " and " + selectedPrinter + " selected", this.getClass());
            }

            // if the ProfileAndPrinter panel was closed on X button,
            // cancel the estimation process
            if (filament.equals(FilamentControler.NO_FILAMENT)
                    || filament.contains(FilamentControler.NO_FILAMENT_2)
                    || FilamentControler.colorExistsLocally(filament) == false
                    || selectedPrinter == PrinterInfo.UNKNOWN) {
                return;
            }

            estimationThread = new PrintEstimationThread();
            bEstimate.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_15.png")));
            estimatePressed = true;
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    estimationThread.start();
                }
            });
        } else if (bEstimate.isEnabled() == false) { // otherwise warn the user that there are no models loaded
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (nModels == 0) {
                        Base.getMainWindow().showFeedBackMessage("noModelError");
                    }
                }
            });
            thread.start();
        }
    }//GEN-LAST:event_bEstimateMousePressed

    private void densitySliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_densitySliderMouseReleased
        lastUsedDensity = parseSlider2();
    }//GEN-LAST:event_densitySliderMouseReleased

    private void densitySliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_densitySliderStateChanged

        int val = densitySlider.getValue();
        tfDensity.setText(String.valueOf(val));

        checkDensitySliderValue(val);
        checkChanges();
    }//GEN-LAST:event_densitySliderStateChanged

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        doCancel();
    }//GEN-LAST:event_jLabel15MousePressed

    private void jLabel5MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MousePressed
        triggerSupport();
    }//GEN-LAST:event_jLabel5MousePressed

    private void jLabel9MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MousePressed
        triggerSupport();
    }//GEN-LAST:event_jLabel9MousePressed

    private void jLabel8MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MousePressed
        triggerRaft();
    }//GEN-LAST:event_jLabel8MousePressed

    private void jLabel7MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MousePressed
        triggerRaft();
    }//GEN-LAST:event_jLabel7MousePressed

    private void jLabel4MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MousePressed
        triggerRaft();
    }//GEN-LAST:event_jLabel4MousePressed

    private void jLabel10MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10MousePressed
        triggerSupport();
    }//GEN-LAST:event_jLabel10MousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bCancel;
    private javax.swing.JLabel bChangeFilament;
    private javax.swing.JLabel bEstimate;
    private javax.swing.JLabel bExport;
    private javax.swing.JRadioButton bHighPlusRes;
    private javax.swing.JRadioButton bHighRes;
    private javax.swing.JRadioButton bLowRes;
    private javax.swing.JRadioButton bMediumRes;
    private javax.swing.JLabel bPrint;
    private javax.swing.ButtonGroup bResButtonGroup;
    private javax.swing.JSlider densitySlider;
    private javax.swing.JLabel estimatedMaterial;
    private javax.swing.JLabel estimatedPrintTime;
    private javax.swing.JLabel filamentType;
    private javax.swing.JLabel highHeightLabel;
    private javax.swing.JLabel highPlusHeightLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
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
    private javax.swing.JPanel jPanel6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lDensity;
    private javax.swing.JLabel loading;
    private javax.swing.JLabel lowHeightLabel;
    private javax.swing.JLabel materialCost;
    private javax.swing.JLabel medHeightLabel;
    private javax.swing.JLabel mmLabel;
    private javax.swing.JLabel nozzleTypeLabel;
    private javax.swing.JLabel nozzleTypeValue;
    private javax.swing.JLabel printTime;
    private javax.swing.JTextField tfDensity;
    // End of variables declaration//GEN-END:variables

    private class PrintEstimationThread extends Thread {

        private int nTimes = 0;
        private boolean stop = false;

        public PrintEstimationThread() {
            super("Print panel Estimation Thread");
        }

        /**
         * Runs GCode generator process. Updates window fields to display print
         * model cost estimation.
         */
        private void runEstimator() {
            Printer prt;
            prt = new Printer(getPreferences());
            if (prt.isReadyToGenerateGCode()) {
                prt.generateGCode();
                File gcode = prt.getGCode();
                //Estimate time and cost
                PrintEstimator.estimateTime(gcode);
                updateEstimationPanel(PrintEstimator.getEstimatedTime(), PrintEstimator.getEstimatedCost());
            } else {
                Base.writeLog("runEstimator(): failed estimation", this.getClass());
            }
        }

        @Override
        public void run() {
            while (stop == false) {
                if (!settingsChanged() || nTimes == 0) {
                    showLoadingIcon(true);
                    if (Base.getMainWindow().isOkToGoOnSave() == false) {
                        Base.getMainWindow().handleSave(true);
                    }
                    runEstimator();
                    updateOldSettings();
                    nTimes++;
                    showLoadingIcon(false);
                    Base.cleanDirectoryTempFiles(Base.getAppDataDirectory().getAbsolutePath() + "/" + Base.MODELS_FOLDER + "/");
                    filament = Base.getMainWindow().getMachine().getModel().getCoilText();
                    stop = true;
                } else {
                    Base.cleanDirectoryTempFiles(Base.getAppDataDirectory().getAbsolutePath() + "/" + Base.MODELS_FOLDER + "/");
                }
            }
        }

        public void kill() {
            stop = true;
            this.interrupt();
        }
    }

    private class GCodeExportThread extends Thread {

        private final File saveFile;
        private int nTimes = 0;
        private boolean stop = false;

        public GCodeExportThread(File saveFile) {
            super("Print panel Export Thread");
            this.saveFile = saveFile;
        }

        /**
         * Runs GCode generator process. Updates window fields to display print
         * model cost estimation.
         */
        private void runExport() {
            Printer prt = new Printer(getPreferences());

            if (prt.isReadyToGenerateGCode() == false) {
                Base.writeLog("runEstimator(): failed export", this.getClass());
                return;
            }

            prt.generateGCode();
            File gcode = prt.getGCode();
            //Estimate time and cost
            PrintEstimator.estimateTime(gcode);
            updateEstimationPanel(PrintEstimator.getEstimatedTime(),
                    PrintEstimator.getEstimatedCost());

            //Writes the file
            BufferedInputStream input = null;
            BufferedOutputStream output = null;

            try {

                input = new BufferedInputStream(new FileInputStream(Base.GCODE2PRINTER_PATH));
                output = new BufferedOutputStream(new FileOutputStream(this.saveFile));

                byte[] buf = new byte[1024];
                int bytesRead;

                while ((bytesRead = input.read(buf)) > 0) {
                    output.write(buf, 0, bytesRead);
                }

            } catch (Exception ex) {

                Base.writeLog(ex.getMessage(), this.getClass());
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }

                    if (output != null) {
                        output.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(PrintPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            filament = Base.getMainWindow().getMachine().getModel().getCoilText();
        }

        @Override
        public void run() {
            while (stop == false) {
                if (!settingsChanged() || nTimes == 0) {
                    showLoadingIconExport(true);

                    if (Base.getMainWindow().isOkToGoOnSave() == false) {
                        Base.getMainWindow().handleSave(true);
                    }

                    runExport();
                    updateOldSettings();
                    nTimes++;
                    showLoadingIconExport(false);
                    stop = true;
                }
            }
            Base.cleanDirectoryTempFiles(Base.getAppDataDirectory().getAbsolutePath() + "/" + Base.MODELS_FOLDER + "/");
        }

        public void kill() {
            stop = true;
            this.interrupt();
        }
    }
}
