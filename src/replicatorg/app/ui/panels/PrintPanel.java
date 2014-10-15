package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import static java.awt.Frame.ICONIFIED;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import replicatorg.app.Base;
import replicatorg.app.FilamentControler;
import replicatorg.app.Languager;
import replicatorg.app.PrintEstimator;
import replicatorg.app.Printer;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
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
public class PrintPanel extends javax.swing.JFrame {

    private JLabel quality_prototype;
    private JLabel quality_normal;
    private JLabel quality_artwork;
    private JLabel quality_low;
    private JLabel quality_medium;
    private JLabel quality_solid;
    private ArrayList<String> prefs;
    private boolean raftPressed, supportPressed, autonomousPressed;
    private int posX = 0, posY = 0;
    private Driver driver = Base.getMainWindow().getMachineInterface().getDriver();
    boolean no_Filament = false;
    private static final String NOK = "NOK";
    private Thread t = null;
    private static final String FORMAT = "%2d:%2d";
    private String colorCode = FilamentControler.NO_FILAMENT_CODE;
    private PrintEstimationThread estimationThread = null;
    private boolean isRunning = true;
    boolean lastUsedRaft;
    String lastUsedDensity;
    String lastUsedResolution;
    boolean lastUsedSupport;
    boolean lastUsedAutonomous;
    boolean lastSelectedRaft;
    String lastSelectedDensity;
    String lastSelectedResolution;
    boolean lastSelectedSupport;
    boolean lastSelectedAutonomous;
    boolean gcodeOK;
    private boolean estimatePressed;
    private Hashtable<Integer, JLabel> labelTable2;

    public PrintPanel() {
        initComponents();
        initSlidersLables();
        setFont();
        setTextLanguage();
        initSliderConfigs();
        centerOnScreen();
        estimationThread = new PrintEstimationThread(this);
        getCoilCode();
        prefs = new ArrayList<String>();
        raftPressed = false;
        supportPressed = false;
        autonomousPressed = true;
        jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_checked.png")));
        evaluateConditions();
        matchChanges();
        enableDrag();
        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());
    }

    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProRegular("14"));
        jLabel2.setFont(GraphicDesignComponents.getSSProBold("14"));
        jLabel3.setFont(GraphicDesignComponents.getSSProBold("14"));
        jLabel5.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel7.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel8.setFont(GraphicDesignComponents.getSSProRegular("10"));
        jLabel9.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel10.setFont(GraphicDesignComponents.getSSProRegular("10"));
        jLabel11.setFont(GraphicDesignComponents.getSSProRegular("10"));
        bCancel.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel12.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel16.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel19.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bEstimate.setFont(GraphicDesignComponents.getSSProRegular("12"));
        lDensity.setFont(GraphicDesignComponents.getSSProRegular("12"));
        filamentType.setFont(GraphicDesignComponents.getSSProRegular("10"));
        estimatedPrintTime.setFont(GraphicDesignComponents.getSSProRegular("10"));
        estimatedMaterial.setFont(GraphicDesignComponents.getSSProRegular("10"));
        printTime.setFont(GraphicDesignComponents.getSSProRegular("10"));
        materialCost.setFont(GraphicDesignComponents.getSSProRegular("10"));
        jLabel22.setFont(GraphicDesignComponents.getSSProRegular("10"));
        jLabel23.setFont(GraphicDesignComponents.getSSProRegular("10"));

        quality_prototype.setFont(GraphicDesignComponents.getSSProRegular("12"));
        quality_normal.setFont(GraphicDesignComponents.getSSProRegular("12"));
        quality_artwork.setFont(GraphicDesignComponents.getSSProRegular("12"));
        quality_low.setFont(GraphicDesignComponents.getSSProRegular("12"));
        quality_medium.setFont(GraphicDesignComponents.getSSProRegular("12"));
        quality_solid.setFont(GraphicDesignComponents.getSSProRegular("12"));
    }

    private void setTextLanguage() {
        jLabel1.setText(Languager.getTagValue("MainWindowButtons", "Print"));
        jLabel2.setText(Languager.getTagValue("Print", "Print_Quality"));
        jLabel3.setText(Languager.getTagValue("Print", "Print_Density"));
        jLabel7.setText(Languager.getTagValue("Print", "Print_Raft"));
        jLabel8.setText(splitString(Languager.getTagValue("Print", "Print_Raft_Info")));
        jLabel9.setText(Languager.getTagValue("Print", "Print_Support"));
        jLabel10.setText(splitString(Languager.getTagValue("Print", "Print_Support_Info")));
        jLabel11.setText(splitString(Languager.getTagValue("Print", "Print_Autonomy_Info")));
        bCancel.setText(Languager.getTagValue("OptionPaneButtons", "Line3"));
        bEstimate.setText(Languager.getTagValue("OptionPaneButtons", "Line15"));
        jLabel12.setText(Languager.getTagValue("ToolPath", "Line21"));
        jLabel16.setText(Languager.getTagValue("Print", "Print_Autonomy"));
        jLabel19.setText(Languager.getTagValue("MaintenancePanel", "FilamentChange_button"));
        filamentType.setText(Languager.getTagValue("MaintenancePanel", "Filament_Type"));
        estimatedPrintTime.setText(Languager.getTagValue("Print", "Print_EstimationPrintTime"));
        estimatedMaterial.setText(Languager.getTagValue("Print", "Print_EstimationMaterial"));
        lDensity.setText(Languager.getTagValue("Print", "Print_Density_Insertion"));
        jLabel23.setText("");


        quality_prototype.setText(Languager.getTagValue("Print", "Print_Quality_Low"));
        quality_normal.setText(Languager.getTagValue("Print", "Print_Quality_High"));
        String artwork = "<html>"+Languager.getTagValue("Print", "Print_Quality_SHigh").split(" ")[0]+"<br>"+Languager.getTagValue("Print", "Print_Quality_SHigh").split(" ")[1]+"</html>";
        quality_artwork.setText(artwork);
        quality_low.setText(Languager.getTagValue("Print", "Print_Density_Low"));
        quality_medium.setText(Languager.getTagValue("Print", "Print_Density_Medium"));
        quality_solid.setText(Languager.getTagValue("Print", "Print_Density_High"));

    }

    private void getCoilCode() {

        String code = Base.getMainWindow().getMachine().getModel().getCoilCode();
        Base.writeLog("Print panel coil code: "+code);
        try {
            ;//do nothing
            //driver.updateCoilCode();            
        } catch (Exception e) {
            Base.writeLog("driver.update coil code failed: " + e.getMessage());
        }


        if (code.equals(FilamentControler.NO_FILAMENT_CODE) || code.equals("NOK")) {
            no_Filament = true;
            jLabel22.setFont(GraphicDesignComponents.getSSProBold("10"));
            code = Languager.getTagValue("Print", "Print_Splash_Info9").toUpperCase();

            jLabel22.setText(" " + code);
            jLabel23.setText(Languager.getTagValue("Print", "Print_Splash_Info11"));

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
            jLabel23.setText(Languager.getTagValue("Print", "Print_Splash_Info11"));
        } else {
            jLabel22.setText(" " + code);
        }

    }

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

    private String splitString(String s) {
        int width = 332;
        return buildString(s.split("\\."), width);
    }

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

    private int getStringPixelsWidth(String s) {
        Graphics g = getGraphics();
        FontMetrics fm = g.getFontMetrics(GraphicDesignComponents.getSSProRegular("10"));
        return fm.stringWidth(s);
    }

    private void initSliderConfigs() {
        Hashtable labelTable1 = new Hashtable();
        labelTable1.put(new Integer(0), quality_prototype);
        labelTable1.put(new Integer(50), quality_normal);
        labelTable1.put( new Integer(100), quality_artwork );
        jSlider1.setLabelTable(labelTable1);

        labelTable2 = new Hashtable<Integer, JLabel>();
        
        JLabel a = new JLabel("O");
        JLabel a1 = new JLabel(Languager.getTagValue("Print", "Print_Density_Low"));
        JLabel b = new JLabel("10");
        b.setBorder(new EmptyBorder(0,10,0,0));
        JLabel c = new JLabel(Languager.getTagValue("Print", "Print_Density_Medium"));
        c.setBorder(new EmptyBorder(0,5,0,0));
        JLabel d = new JLabel("30");
        JLabel e = new JLabel(Languager.getTagValue("Print", "Print_Density_High"));
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

    private void initSlidersLables() {
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

    private String parseSlider1() {

        if (jSlider1.getValue() == 0) {
            return "LOW";
        }
        if (jSlider1.getValue() == 50) {
            return "HIGH";
        }
        if (jSlider1.getValue() == 100) {
            return "SHIGH";
        }

        return "LOW";
    }

    private String parseSlider2() {
        return String.valueOf(densitySlider.getValue());
    }

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
            //jLabel12.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_15.png")));
            jLabel22.setForeground(Color.red);
            jLabel23.setForeground(Color.red);
        }

    }

    public void updateEstimationPanel(String time, String cost) {

        if (!time.contains("NA") || !time.contains("N/A")) {
            printTime.setText(buildTimeEstimationString(time));
        }

        if (!cost.contains("NA") && !cost.contains("N/A")) {
            materialCost.setText(tuneCost(cost));
        }

    }

    private String gramsCalculator(double meters) {
        DecimalFormat df = new DecimalFormat("#.00");
        double grams = meters * 12 / 4;

        if (grams > 0) {
            return df.format(meters * 12 / 4);
        }

        return "N/A";
    }

    private String tuneCost(String cost) {
        double meters = Double.valueOf(cost);
        String result = gramsCalculator(meters / 1000.0);
        return result + " " + Languager.getTagValue("Print", "Print_GramsTag");
    }

    private String minutesToHours(int t) {
        int hours = t / 60; //since both are ints, you get an int
        int minute = t % 60;

        return String.format(FORMAT, hours, minute);
    }

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

    private String buildTimeEstimationString(String durT) {
        String text = "N/A";
        if (!durT.equals("NA")) {
            int duration = estimatorTimeToMinutes(durT);
            String hours = minutesToHours(duration).split("\\:")[0];
            String minutes = minutesToHours(duration).split("\\:")[1];
            int min = Integer.valueOf(minutes.trim());

            if (duration >= 120) {
                if (min > 1) {
                    text = (hours + " " + Languager.getTagValue("Print", "PrintHours") + " " + minutes + " " + Languager.getTagValue("Print", "PrintMinutes"));
                } else if (min == 1) {
                    text = (hours + " " + Languager.getTagValue("Print", "PrintHours") + " " + minutes + " " + Languager.getTagValue("Print", "PrintMinute"));
                } else {
                    text = (hours + " " + Languager.getTagValue("Print", "PrintHour"));
                }
            } else if (duration >= 60 && duration < 120) {
                if (min > 1) {
                    text = (hours + " " + Languager.getTagValue("Print", "PrintHour") + " " + minutes + " " + Languager.getTagValue("Print", "PrintMinutes"));
                } else if (min == 1) {
                    text = (hours + " " + Languager.getTagValue("Print", "PrintHour") + " " + minutes + " " + Languager.getTagValue("Print", "PrintMinute"));
                } else {
                    text = (hours + " " + Languager.getTagValue("Print", "PrintHour"));
                }
            } else {
                text = (" " + minutes + " " + Languager.getTagValue("Print", "PrintMinutes"));
            }
        } else {
            text = (durT);
        }


        return text;

    }

    private void enableDrag() {
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                posX = e.getX();
                posY = e.getY();
            }
        });


        this.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent evt) {
                //sets frame position when mouse dragged			
                setLocation(evt.getXOnScreen() - posX, evt.getYOnScreen() - posY);
            }
        });
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

        if (lastUsedAutonomous) {
            jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_checked.png")));
            autonomousPressed = true;
        } else {
            jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_unchecked.png")));
            autonomousPressed = false;
        }

        int density;
        try {
            density = Integer.parseInt(lastUsedDensity);
        } catch (NumberFormatException e) {
            density = 5;
        }

        densitySlider.setValue(density);

        if (lastUsedResolution.equalsIgnoreCase("LOW")) {
            jSlider1.setValue(0);
        } else if (lastUsedResolution.equalsIgnoreCase("HIGH")) {
            jSlider1.setValue(50);
        } else {
            jSlider1.setValue(100);
        }

        checkChanges();
    }

    public boolean checkChanges() {
        boolean equal = false;

        lastUsedRaft = Base.getMainWindow().getBed().isLasRaft();
        lastUsedDensity = Base.getMainWindow().getBed().getLastDensity();
        lastUsedResolution = Base.getMainWindow().getBed().getLastResolution();
        lastUsedSupport = Base.getMainWindow().getBed().isLastSupport();
        lastUsedAutonomous = Base.getMainWindow().getBed().isLastAutonomous();
        gcodeOK = Base.getMainWindow().getBed().isGcodeOK();

        if (parseSlider1().equals(lastUsedResolution)
                && parseSlider2().equals(lastUsedDensity)
                && raftPressed == lastUsedRaft
                && supportPressed == lastUsedSupport
                && autonomousPressed == lastUsedAutonomous
                && gcodeOK) {
            equal = true;
        }

        return equal;
    }

    public boolean settingsChanged() {

        if (parseSlider1().equals(lastSelectedResolution)
                && parseSlider2().equals(lastSelectedDensity)
                && raftPressed == lastSelectedRaft
                && supportPressed == lastSelectedSupport) {
            return true;
        }

        return false;
    }

    public void updateOldSettings() {
        lastSelectedRaft = raftPressed;
        lastSelectedDensity = parseSlider2();
        lastSelectedResolution = parseSlider1();
        lastSelectedSupport = supportPressed;
        lastSelectedAutonomous = autonomousPressed;
    }

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

    private void triggerAutonomous() {
        if (!autonomousPressed) {
            jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_checked.png")));
            autonomousPressed = true;
        } else {
            jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_unchecked.png")));
            autonomousPressed = false;
        }

        checkChanges();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSlider1 = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        densitySlider = new javax.swing.JSlider();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel19 = new javax.swing.JLabel();
        filamentType = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
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
        jPanel2 = new javax.swing.JPanel();
        bCancel = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));
        jPanel1.setPreferredSize(new java.awt.Dimension(374, 440));

        jPanel4.setBackground(new java.awt.Color(255, 203, 5));
        jPanel4.setMinimumSize(new java.awt.Dimension(62, 26));
        jPanel4.setPreferredSize(new java.awt.Dimension(70, 30));
        jPanel4.setRequestFocusEnabled(false);

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_11.png"))); // NOI18N
        jLabel13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel13MousePressed(evt);
            }
        });

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_10.png"))); // NOI18N

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
                .addGap(12, 12, 12)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 7, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, 13, Short.MAX_VALUE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("IMPRIMIR");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("Qualidade");

        jSlider1.setBackground(new java.awt.Color(248, 248, 248));
        jSlider1.setForeground(new java.awt.Color(0, 0, 0));
        jSlider1.setPaintLabels(true);
        jSlider1.setSnapToTicks(true);
        jSlider1.setValue(0);
        jSlider1.setPreferredSize(new java.awt.Dimension(360, 54));
        jSlider1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jSlider1MouseReleased(evt);
            }
        });
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });

        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("Densidade");

        densitySlider.setBackground(new java.awt.Color(248, 248, 248));
        densitySlider.setForeground(new java.awt.Color(0, 0, 0));
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

        jLabel7.setForeground(new java.awt.Color(0, 0, 0));
        jLabel7.setText("Raft");
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel7MousePressed(evt);
            }
        });

        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setText("Suspendisse potenti.");
        jLabel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel8MousePressed(evt);
            }
        });

        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setText("Support");
        jLabel9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel9MousePressed(evt);
            }
        });

        jLabel10.setForeground(new java.awt.Color(0, 0, 0));
        jLabel10.setText("Suspendisse potenti.");
        jLabel10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel10MousePressed(evt);
            }
        });

        jLabel19.setForeground(new java.awt.Color(0, 0, 0));
        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_16.png"))); // NOI18N
        jLabel19.setText("Mudar filamento agora");
        jLabel19.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel19.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel19MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel19MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel19MousePressed(evt);
            }
        });

        filamentType.setForeground(new java.awt.Color(0, 0, 0));
        filamentType.setText("Filament Type");

        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/arrow_ajuda_2.png"))); // NOI18N

        jLabel22.setForeground(new java.awt.Color(0, 0, 0));
        jLabel22.setText("NO_FILAMENT");

        jLabel23.setForeground(new java.awt.Color(0, 0, 0));
        jLabel23.setText("a");

        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setText("Suspendisse potenti.");
        jLabel11.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel11MousePressed(evt);
            }
        });

        jLabel16.setForeground(new java.awt.Color(0, 0, 0));
        jLabel16.setText("Autonomous");
        jLabel16.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel16MousePressed(evt);
            }
        });

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/c_unchecked.png"))); // NOI18N
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel6MousePressed(evt);
            }
        });

        jLabel24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/arrow_ajuda_2.png"))); // NOI18N

        estimatedPrintTime.setForeground(new java.awt.Color(0, 0, 0));
        estimatedPrintTime.setText("Print time:");

        jLabel26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/arrow_ajuda_2.png"))); // NOI18N

        estimatedMaterial.setForeground(new java.awt.Color(0, 0, 0));
        estimatedMaterial.setText("Material cost:");

        printTime.setForeground(new java.awt.Color(0, 0, 0));
        printTime.setText("N/A");

        materialCost.setForeground(new java.awt.Color(0, 0, 0));
        materialCost.setText("N/A");

        loading.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/loading.gif"))); // NOI18N

        bEstimate.setForeground(new java.awt.Color(0, 0, 0));
        bEstimate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_15.png"))); // NOI18N
        bEstimate.setText("Estimate");
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(106, 106, 106)
                .addComponent(jLabel19)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(4, 4, 4)
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
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(4, 4, 4)
                                        .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel10)
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
                                                .addComponent(loading)))))))
                        .addGap(19, 19, 19))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(densitySlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel16)
                                    .addComponent(jLabel11))
                                .addGap(0, 0, Short.MAX_VALUE)))))
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
                .addGap(9, 9, 9)
                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(filamentType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addComponent(loading))
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
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel19)
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(255, 203, 5));
        jPanel2.setMinimumSize(new java.awt.Dimension(20, 26));
        jPanel2.setPreferredSize(new java.awt.Dimension(20, 26));

        bCancel.setForeground(new java.awt.Color(0, 0, 0));
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

        jLabel12.setForeground(new java.awt.Color(0, 0, 0));
        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_15.png"))); // NOI18N
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
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged

        if (jSlider1.getValue() > 1 && jSlider1.getValue() <= 49) {
            jSlider1.setValue(50);
        } else if (jSlider1.getValue() >= 51) {
            jSlider1.setValue(100);
        } else if (jSlider1.getValue() <= 1) {
            jSlider1.setValue(0);
        }

        checkChanges();
    }//GEN-LAST:event_jSlider1StateChanged

    private void densitySliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_densitySliderStateChanged
    
        int val = densitySlider.getValue();
        tfDensity.setText(String.valueOf(val));
        
        checkDensitySliderValue(val);        
        checkChanges();
    }//GEN-LAST:event_densitySliderStateChanged

    private void jLabel19MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel19MouseEntered
        jLabel19.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_16.png")));
    }//GEN-LAST:event_jLabel19MouseEntered

    private void jLabel19MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel19MouseExited
        jLabel19.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_16.png")));
    }//GEN-LAST:event_jLabel19MouseExited

    private void jLabel12MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel12MouseEntered
        if (!no_Filament) {
            jLabel12.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_15.png")));
        }
    }//GEN-LAST:event_jLabel12MouseEntered

    private void jLabel12MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel12MouseExited
        if (!no_Filament) {
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

        if (!no_Filament) {
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

                public void run() {
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
                            e.printStackTrace();
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

    private void jLabel19MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel19MousePressed
        dispose();
        Base.getMainWindow().getButtons().updatePressedStateButton("print");
        Base.getMainWindow().getButtons().goFilamentChange();
        ProperDefault.put("maintenance", "1");
        FilamentHeating p = new FilamentHeating();
        p.setVisible(true);
    }//GEN-LAST:event_jLabel19MousePressed

    private void jLabel13MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel13MousePressed
        setState(ICONIFIED);
    }//GEN-LAST:event_jLabel13MousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        doCancel();
    }//GEN-LAST:event_jLabel15MousePressed

    private void jLabel6MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MousePressed
        triggerAutonomous();
    }//GEN-LAST:event_jLabel6MousePressed

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

    private void jLabel16MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel16MousePressed
        triggerAutonomous();
    }//GEN-LAST:event_jLabel16MousePressed

    private void jLabel11MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel11MousePressed
        triggerAutonomous();
    }//GEN-LAST:event_jLabel11MousePressed

    private void jSlider1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jSlider1MouseReleased
        lastSelectedResolution = parseSlider1();
    }//GEN-LAST:event_jSlider1MouseReleased

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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bCancel;
    private javax.swing.JLabel bEstimate;
    private javax.swing.JSlider densitySlider;
    private javax.swing.JLabel estimatedMaterial;
    private javax.swing.JLabel estimatedPrintTime;
    private javax.swing.JLabel filamentType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JLabel lDensity;
    private javax.swing.JLabel loading;
    private javax.swing.JLabel materialCost;
    private javax.swing.JLabel printTime;
    private javax.swing.JTextField tfDensity;
    // End of variables declaration//GEN-END:variables
}

class PrintEstimationThread extends Thread {

    private PrintPanel printPanel;
    private int nTimes;

    public PrintEstimationThread(PrintPanel panel) {
        super("Print panel Estimation Thread");
        this.printPanel = panel;
        this.nTimes = 0;
    }

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
