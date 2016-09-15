package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import pt.beeverycreative.beesoft.drivers.usb.PrinterInfo;
import pt.beeverycreative.beesoft.filaments.Filament;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
import pt.beeverycreative.beesoft.filaments.Nozzle;
import replicatorg.app.Base;
import pt.beeverycreative.beesoft.filaments.PrintPreferences;
import pt.beeverycreative.beesoft.filaments.Resolution;
import pt.beeverycreative.beesoft.filaments.SlicerConfig;
import replicatorg.app.Languager;
import replicatorg.app.ui.GraphicDesignComponents;

public class EstimateExportPanel extends BasePrintEstimateExport {

    private static final String FORMAT = "%2d:%2d";
    private final Hashtable<Integer, JLabel> labelTable2 = new Hashtable<>();
    private final int nModels = Base.getMainWindow().getBed().getNumberModels();
    private final boolean lockedPrinter;
    private JLabel lowQuality, mediumQuality, solidQuality;
    private boolean raftPressed = false, supportPressed = false,
            atLeastOneResEnabled = false;
    private GCodeEstimateExportThread estimateExportThread;
    private PrinterInfo selectedPrinter;
    private Filament selectedFilament;
    private Nozzle selectedNozzle;

    public EstimateExportPanel() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        lockedPrinter = false;
        commonConstructor();
    }

    public EstimateExportPanel(PrinterInfo selectedPrinter) {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        this.selectedPrinter = selectedPrinter;
        lockedPrinter = true;
        commonConstructor();
    }

    private void commonConstructor() {
        initComponents();
        initSlidersLabels();
        setFont();
        setTextLanguage();
        initSliderConfigs();
        centerOnScreen();
        evaluateConditions();
        densitySlider.setValue(5);

        populatePrinterComboBox();
        populateFilamentComboBox();
        populateNozzleComboBox();

        // disabled for now
        jLabel25.setVisible(false);
        nozzleTypeLabel.setVisible(false);
        nozzleComboBox.setVisible(false);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (estimateExportThread != null) {
                    estimateExportThread.kill();
                }
            }
        });
    }

    private void populatePrinterComboBox() {
        DefaultComboBoxModel<String> printerModel;
        Set<String> printerNames;
        String[] printers;
        ComboBoxEditor editor;
        JTextField textField;

        printerNames = new HashSet<>();

        if (!lockedPrinter) {
            for (PrinterInfo printer : PrinterInfo.values()) {
                if (printer != PrinterInfo.UNKNOWN) {
                    printerNames.add(printer.presentationName());
                }
            }
            printers = printerNames.toArray(new String[printerNames.size()]);
        } else {
            printers = new String[]{selectedPrinter.presentationName()};
            printerComboBox.setEditable(true);
            editor = printerComboBox.getEditor();
            textField = (JTextField) editor.getEditorComponent();
            textField.setDisabledTextColor(UIManager.getColor("ComboBox.foreground"));
            textField.setBackground(new Color(242, 242, 242));
            printerComboBox.setEnabled(false);
        }

        printerModel = new DefaultComboBoxModel<>(printers);
        printerComboBox.setModel(printerModel);
        printerComboBox.setSelectedIndex(0);
        selectedPrinter = (PrinterInfo) PrinterInfo.getDeviceByFormalName((String) printerComboBox.getSelectedItem());
    }

    private void populateFilamentComboBox() {
        DefaultComboBoxModel<Filament> filaments;
        Filament[] filamentList;

        FilamentControler.forceFetch(selectedPrinter);
        filamentList = FilamentControler.getFilamentArray();
        filaments = new DefaultComboBoxModel<>(filamentList);
        filamentComboBox.setModel(filaments);
        selectedFilament = (Filament) filamentComboBox.getSelectedItem();
    }

    private void populateNozzleComboBox() {
        DefaultComboBoxModel<Nozzle> nozzleComboModel;
        List<Nozzle> nozzleList;
        Nozzle[] nozzleArray;

        for (SlicerConfig sc : selectedFilament.getSupportedPrinters()) {
            if (sc.getPrinterName().equals(selectedPrinter.filamentCode())) {
                nozzleList = sc.getNozzles();
                nozzleArray = nozzleList.toArray(new Nozzle[nozzleList.size()]);
                nozzleComboModel = new DefaultComboBoxModel<>(nozzleArray);
                nozzleComboBox.setModel(nozzleComboModel);
                break;
            }
        }

        selectedNozzle = (Nozzle) nozzleComboBox.getSelectedItem();
        verifyCompatibleResolutions();
    }

    private void verifyCompatibleResolutions() {
        final List<Resolution> resList;
        final JRadioButton[] radioButtons = {
            bLowRes, bMediumRes, bHighRes, bHighPlusRes
        };
        JRadioButton selectedRes = null;

        for (JRadioButton button : radioButtons) {
            if (button.isSelected()) {
                selectedRes = button;
                break;
            }
        }

        bLowRes.setEnabled(false);
        bMediumRes.setEnabled(false);
        bHighRes.setEnabled(false);
        bHighPlusRes.setEnabled(false);

        if (selectedFilament != null) {
            resList = selectedFilament.getSupportedResolutions(selectedPrinter.filamentCode(), selectedNozzle.getSizeInMicrons());

            if (resList != null) {
                for (Resolution res : resList) {
                    switch (res.getType()) {
                        case "low":
                            atLeastOneResEnabled = true;
                            bLowRes.setEnabled(true);
                            break;
                        case "medium":
                            atLeastOneResEnabled = true;
                            bMediumRes.setEnabled(true);
                            break;
                        case "high":
                            atLeastOneResEnabled = true;
                            bHighRes.setEnabled(true);
                            break;
                        case "high+":
                            atLeastOneResEnabled = true;
                            bHighPlusRes.setEnabled(true);
                            break;
                        default:
                            break;
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

        if (selectedRes == null || !selectedRes.isEnabled()) {
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

        printerComboBox.setFont(GraphicDesignComponents.getSSProLight("12"));
        nozzleComboBox.setFont(GraphicDesignComponents.getSSProLight("12"));
        filamentComboBox.setFont(GraphicDesignComponents.getSSProLight("12"));
    }

    /**
     * Set copy for all UI elements.
     */
    private void setTextLanguage() {
        //jLabel1.setText(Languager.getTagValue(1, "MainWindowButtons", "Print"));
        jLabel2.setText(Languager.getTagValue(1, "Print", "Print_Quality"));
        jLabel3.setText(Languager.getTagValue(1, "Print", "Print_Density"));
        jLabel7.setText(Languager.getTagValue(1, "Print", "Print_Raft"));
        jLabel8.setText("<html>" + Languager.getTagValue(1, "Print", "Print_Raft_Info") + "</html>");
        jLabel9.setText(Languager.getTagValue(1, "Print", "Print_Support"));
        jLabel10.setText("<html>" + Languager.getTagValue(1, "Print", "Print_Support_Info") + "</html>");
        bCancel.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line3"));
        bEstimate.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line15"));
        bExport.setText(Languager.getTagValue(1, "OptionPaneButtons", "Export"));
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
        showLoadingIcon(false);

        if (nModels > 0 && atLeastOneResEnabled) {
            bEstimate.setEnabled(true);
            bExport.setEnabled(true);
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
    @Override
    protected void updateEstimationPanel(String time, String cost) {
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

    @Override
    protected void showLoadingIcon(boolean show) {
        if (show) {
            materialCost.setText("");
            printTime.setText("");
            materialCost.setIcon(loadingIcon);
            printTime.setIcon(loadingIcon);
            bEstimate.setEnabled(false);
            bExport.setEnabled(false);
        } else {
            materialCost.setIcon(null);
            printTime.setIcon(null);
            bEstimate.setEnabled(true);
            bExport.setEnabled(true);
        }
    }

    /**
     * Cancel event.
     */
    private void doCancel() {
        dispose();
        Base.cleanDirectoryTempFiles(Base.getAppDataDirectory().getAbsolutePath() + "/" + Base.MODELS_FOLDER + "/");
        Base.getMainWindow().getButtons().updatePressedStateButton("print");
    }

    /**
     * Gets print parameters from selected configurations on window.
     *
     * @return print parameters.
     */
    @Override
    protected PrintPreferences getPreferences() {
        PrintPreferences preferences;
        String resolution;
        int density;

        resolution = parseSlider1();
        density = parseSlider2();

        preferences = new PrintPreferences(resolution, selectedFilament.getName(), density, selectedNozzle.getSizeInMicrons(), raftPressed, supportPressed, selectedPrinter);

        return preferences;
    }

    /**
     * Handles Density slider event.
     */
    private void checkDensitySliderValue(int val) {

        labelTable2.get(5).setForeground(Color.BLACK);
        labelTable2.get(20).setForeground(Color.BLACK);
        labelTable2.get(40).setForeground(Color.BLACK);

        switch (val) {
            case 5:
                labelTable2.get(5).setForeground(new Color(255, 203, 5));
                break;
            case 20:
                labelTable2.get(20).setForeground(new Color(255, 203, 5));
                break;
            case 40:
                labelTable2.get(40).setForeground(new Color(255, 203, 5));
                break;
            default:
                break;
        }
    }

    /**
     * Handles Raft checkbox event.
     */
    private void triggerRaft() {
        if (!raftPressed) {
            jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_checked.png")));
            raftPressed = true;
        } else {
            jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_unchecked.png")));
            raftPressed = false;
        }
    }

    /**
     * Handles Support checkbox event.
     */
    private void triggerSupport() {
        if (!supportPressed) {
            jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_checked.png")));
            supportPressed = true;
        } else {
            jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_unchecked.png")));
            supportPressed = false;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bResButtonGroup = new javax.swing.ButtonGroup();
        jPanel2 = new javax.swing.JPanel();
        bCancel = new javax.swing.JLabel();
        bExport = new javax.swing.JLabel();
        bEstimate = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        densitySlider = new javax.swing.JSlider();
        filamentType = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel24 = new javax.swing.JLabel();
        estimatedPrintTime = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        estimatedMaterial = new javax.swing.JLabel();
        printTime = new javax.swing.JLabel();
        materialCost = new javax.swing.JLabel();
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
        jLabel25 = new javax.swing.JLabel();
        nozzleTypeLabel = new javax.swing.JLabel();
        filamentComboBox = new javax.swing.JComboBox();
        nozzleComboBox = new javax.swing.JComboBox();
        printerComboBox = new javax.swing.JComboBox();
        printerLabel = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(248, 248, 248));
        setUndecorated(true);
        setPreferredSize(null);

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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(bCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bEstimate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bExport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bCancel)
                    .addComponent(bExport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bEstimate)))
        );

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));
        jPanel1.setPreferredSize(new java.awt.Dimension(455, 630));

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

        jLabel1.setText("Estimate / Export");
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

        filamentType.setFont(GraphicDesignComponents.getSSProRegular("12"));
        filamentType.setText("Filament Type");
        filamentType.setMaximumSize(new java.awt.Dimension(100, 13));
        filamentType.setMinimumSize(new java.awt.Dimension(100, 13));

        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/arrow_ajuda_2.png"))); // NOI18N

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

        jLabel25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/arrow_ajuda_2.png"))); // NOI18N

        nozzleTypeLabel.setFont(GraphicDesignComponents.getSSProRegular("12"));
        nozzleTypeLabel.setText("Nozzle type:");
        nozzleTypeLabel.setMaximumSize(new java.awt.Dimension(100, 18));
        nozzleTypeLabel.setMinimumSize(new java.awt.Dimension(100, 18));
        nozzleTypeLabel.setPreferredSize(new java.awt.Dimension(70, 18));

        filamentComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        filamentComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                filamentComboBoxItemStateChanged(evt);
            }
        });

        nozzleComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        nozzleComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                nozzleComboBoxItemStateChanged(evt);
            }
        });

        printerComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        printerComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                printerComboBoxItemStateChanged(evt);
            }
        });

        printerLabel.setFont(GraphicDesignComponents.getSSProRegular("12"));
        printerLabel.setText("Printer:");
        printerLabel.setMaximumSize(new java.awt.Dimension(100, 18));
        printerLabel.setMinimumSize(new java.awt.Dimension(100, 18));
        printerLabel.setPreferredSize(new java.awt.Dimension(70, 18));

        jLabel27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/arrow_ajuda_2.png"))); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                                .addGap(0, 32, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addComponent(bHighPlusRes, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(densitySlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(lDensity)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfDensity, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(4, 4, 4)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel27)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(printerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(42, 42, 42)
                                                .addComponent(printerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(filamentType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(156, 156, 156)
                                                .addComponent(filamentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(estimatedMaterial, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(estimatedPrintTime, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(42, 42, 42)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(materialCost)
                                                    .addComponent(printTime)))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel25)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(nozzleTypeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(42, 42, 42)
                                                .addComponent(nozzleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator2)
                .addGap(19, 19, 19))
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
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(printerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(printerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(filamentComboBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filamentType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel25, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nozzleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nozzleTypeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(estimatedPrintTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(estimatedMaterial, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(printTime)
                        .addGap(6, 6, 6)
                        .addComponent(materialCost)))
                .addGap(10, 10, 10))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 635, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bEstimateMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bEstimateMouseEntered
        bEstimate.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_15.png")));
    }//GEN-LAST:event_bEstimateMouseEntered

    private void bEstimateMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bEstimateMouseExited
        bEstimate.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_15.png")));
    }//GEN-LAST:event_bEstimateMouseExited

    private void bEstimateMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bEstimateMousePressed
        // if there are any loaded model, do the estimation
        if (bEstimate.isEnabled()) {
            estimateExportThread = new GCodeEstimateExportThread();
            estimateExportThread.start();
        } else if (bEstimate.isEnabled() == false) { // otherwise warn the user that there are no models loaded
            Base.getMainWindow().showFeedBackMessage("noModelError");
        }
    }//GEN-LAST:event_bEstimateMousePressed

    private void bExportMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExportMouseEntered
        // don't change the icon if button is disabled due to the absence of loaded models
        bExport.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_19.png")));
    }//GEN-LAST:event_bExportMouseEntered

    private void bExportMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExportMouseExited
        // don't change the icon if button is disabled due to the absence of loaded models
        bExport.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_19.png")));
    }//GEN-LAST:event_bExportMouseExited

    private void bExportMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExportMousePressed
        int rVal;
        // if there are any loaded models, export
        if (bExport.isEnabled() && nModels > 0) {
            JFileChooser saveFile = new JFileChooser();
            saveFile.setSelectedFile(new File("export-"
                    + System.currentTimeMillis() + ".gcode"));
            rVal = saveFile.showSaveDialog(null);

            if (rVal == JFileChooser.APPROVE_OPTION) {
                estimateExportThread = new GCodeEstimateExportThread(saveFile.getSelectedFile().getAbsolutePath());
                estimateExportThread.start();
            }
        } else if (nModels <= 0) { // otherwise warn the user that there are no models loaded
            Base.getMainWindow().showFeedBackMessage("noModelError");
        }
    }//GEN-LAST:event_bExportMousePressed

    private void bCancelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseEntered
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bCancelMouseEntered

    private void bCancelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseExited
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bCancelMouseExited

    private void bCancelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMousePressed
        doCancel();
    }//GEN-LAST:event_bCancelMousePressed

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
        String tfDensityValueText;

        tfDensityValueText = tfDensity.getText();

        try {
            Double.parseDouble(tfDensityValueText);
            if (tfDensityValueText.equals("") == false) {
                int densityValue = Integer.valueOf(tfDensity.getText());
                if (densityValue < 0 || densityValue > 100) {
                    densityValue = 5;
                }
                densitySlider.setValue(densityValue);
                checkDensitySliderValue(densityValue);
            }
        } catch (NumberFormatException nfe) {
        }
    }//GEN-LAST:event_tfDensityKeyReleased

    private void densitySliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_densitySliderStateChanged
        int val;

        val = densitySlider.getValue();

        tfDensity.setText(String.valueOf(val));
        checkDensitySliderValue(val);
    }//GEN-LAST:event_densitySliderStateChanged

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        doCancel();
    }//GEN-LAST:event_jLabel15MousePressed

    private void filamentComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_filamentComboBoxItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            selectedFilament = (Filament) filamentComboBox.getSelectedItem();
            populateNozzleComboBox();
        }
    }//GEN-LAST:event_filamentComboBoxItemStateChanged

    private void printerComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_printerComboBoxItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            selectedPrinter = PrinterInfo.getDeviceByFormalName((String) printerComboBox.getSelectedItem());
            populateFilamentComboBox();
            populateNozzleComboBox();
        }
    }//GEN-LAST:event_printerComboBoxItemStateChanged

    private void nozzleComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_nozzleComboBoxItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            selectedNozzle = (Nozzle) nozzleComboBox.getSelectedItem();
            verifyCompatibleResolutions();
        }
    }//GEN-LAST:event_nozzleComboBoxItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bCancel;
    private javax.swing.JLabel bEstimate;
    private javax.swing.JLabel bExport;
    private javax.swing.JRadioButton bHighPlusRes;
    private javax.swing.JRadioButton bHighRes;
    private javax.swing.JRadioButton bLowRes;
    private javax.swing.JRadioButton bMediumRes;
    private javax.swing.ButtonGroup bResButtonGroup;
    private javax.swing.JSlider densitySlider;
    private javax.swing.JLabel estimatedMaterial;
    private javax.swing.JLabel estimatedPrintTime;
    private javax.swing.JComboBox filamentComboBox;
    private javax.swing.JLabel filamentType;
    private javax.swing.JLabel highHeightLabel;
    private javax.swing.JLabel highPlusHeightLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
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
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lDensity;
    private javax.swing.JLabel lowHeightLabel;
    private javax.swing.JLabel materialCost;
    private javax.swing.JLabel medHeightLabel;
    private javax.swing.JComboBox nozzleComboBox;
    private javax.swing.JLabel nozzleTypeLabel;
    private javax.swing.JLabel printTime;
    private javax.swing.JComboBox printerComboBox;
    private javax.swing.JLabel printerLabel;
    private javax.swing.JTextField tfDensity;
    // End of variables declaration//GEN-END:variables
}
