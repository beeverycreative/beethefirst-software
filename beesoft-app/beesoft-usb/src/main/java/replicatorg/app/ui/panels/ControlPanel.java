package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeTableXYDataset;
import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.machine.MachineInterface;
import replicatorg.machine.model.AxisId;
import replicatorg.util.Point5d;

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
public class ControlPanel extends BaseDialog {

    private MachineInterface machine;
    private double temperatureGoal;
    private DefaultComboBoxModel comboModel;
    private String[] categories;
    private DefaultComboBoxModel comboModel2;
    private String[] categories2;
    private double XYFeedrate;
    private double ZFeedrate;
    private static final String STOP = "M112";
    private static final String HOME = "G28";
    private static final String FAN_OFF = "M107";
    private static final String FAN_ON = "M106";
    private final double safeDistance = 122;
    private final TemperatureThread disposeThread;
    private boolean coolFanPressed;
    private boolean loggingTemperature;
    private boolean freeJogging;
    private File file;
    private FileWriter fw;
    private BufferedWriter bw;
    private static final String REDMEC_TAG = "[TemperatureLog]";
    private boolean jogButtonPressed = false;

    private final TimeTableXYDataset t0MeasuredDataset = new TimeTableXYDataset();
    private final TimeTableXYDataset t0TargetDataset = new TimeTableXYDataset();
    private final TimeTableXYDataset t1MeasuredDataset = new TimeTableXYDataset();
    private final TimeTableXYDataset t1TargetDataset = new TimeTableXYDataset();
    private final TimeTableXYDataset pMeasuredDataset = new TimeTableXYDataset();
    private final TimeTableXYDataset pTargetDataset = new TimeTableXYDataset();

    final private static Color t0TargetColor = Color.MAGENTA;
    final private static Color t0MeasuredColor = Color.RED;
    final private static Color t1TargetColor = Color.CYAN;
    final private static Color t1MeasuredColor = Color.BLUE;
    final private static Color pTargetColor = Color.YELLOW;
    final private static Color pMeasuredColor = Color.GREEN;

    private final long startMillis = System.currentTimeMillis();
    protected long mSpeedLastClicked = 0;
    private Timer inputValidation;

    public ControlPanel() {
        super(Base.getMainWindow(), Dialog.ModalityType.MODELESS);
        initComponents();
        Base.writeLog("Advanced panel opened...");
        setFont();
        setTextLanguage();
        centerOnScreen();
        enableDrag();
        evaluateInitialConditions();
        disposeThread = new TemperatureThread(this, machine);
        disposeThread.start();
        Base.systemThreads.add(disposeThread);

        this.tempPanel.setLayout(new GridBagLayout());
        this.tempPanel.add(this.makeChart());

        //Sets legend colors
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(t0MeasuredColor);
        g.fillRect(0, 0, 10, 10);
        //image.getGraphics().fillRect(0,0,10,10);
        Icon icon1 = new ImageIcon(image);

        this.colorCurrentTemp.setIcon(icon1);
        this.colorCurrentTemp.setText("");

        BufferedImage image2 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics g2 = image2.getGraphics();
        g2.setColor(t0TargetColor);
        g2.fillRect(0, 0, 10, 10);
        Icon icon2 = new ImageIcon(image2);

        this.colorTargetTemp.setIcon(icon2);
        this.colorTargetTemp.setText("");

    }

    private void setFont() {
        TitledBorder border = new TitledBorder(Languager.getTagValue(1, "ControlPanel", "Console_Movement"));
        border.setTitleFont(GraphicDesignComponents.getSSProBold("12"));
        jPanel2.setBorder(border);
        TitledBorder border2 = new TitledBorder(Languager.getTagValue(1, "ControlPanel", "Console_Extrusion"));
        border2.setTitleFont(GraphicDesignComponents.getSSProBold("12"));
        jPanel3.setBorder(border2);

        extrudeCombo.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bCenterX.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bCenterY.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bCenterZ.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bCalibrateA.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bCalibrateB.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bCalibrateC.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bBeep.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bHome.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bSetCalibration.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bCurrentPosition.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bCurrentPosition.setFont(GraphicDesignComponents.getSSProRegular("14"));
        extruderTemperature.setFont(GraphicDesignComponents.getSSProRegular("14"));
        motorSpeed.setFont(GraphicDesignComponents.getSSProRegular("14"));
        extrudeDuration.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bReverse.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bForward.setFont(GraphicDesignComponents.getSSProRegular("14"));
        motorControl.setFont(GraphicDesignComponents.getSSProBold("16"));
        feedRate.setFont(GraphicDesignComponents.getSSProBold("16"));
        logTemperature.setFont(GraphicDesignComponents.getSSProRegular("14"));
        notes.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bOK.setFont(GraphicDesignComponents.getSSProRegular("12"));
        xyFeedrate.setFont(GraphicDesignComponents.getSSProRegular("14"));
        zFeedrate.setFont(GraphicDesignComponents.getSSProRegular("14"));

        enableFreeJog.setFont(GraphicDesignComponents.getSSProRegular("14"));

    }

    private void setTextLanguage() {
        feedRate.setText(Languager.getTagValue(1, "ControlPanel", "Feedrate"));
        bCenterX.setText(Languager.getTagValue(1, "ControlPanel", "CenterX"));
        bCenterY.setText(Languager.getTagValue(1, "ControlPanel", "CenterY"));
        bCenterZ.setText(Languager.getTagValue(1, "ControlPanel", "CenterZ"));
        enableFreeJog.setText(Languager.getTagValue(1, "ControlPanel", "FreeJog"));
        bCurrentPosition.setText(Languager.getTagValue(1, "ControlPanel", "CurrentPosition"));
        bCalibrateA.setText(Languager.getTagValue(1, "ControlPanel", "CalibrateA"));
        bCalibrateB.setText(Languager.getTagValue(1, "ControlPanel", "CalibrateB"));
        bCalibrateC.setText(Languager.getTagValue(1, "ControlPanel", "CalibrateC"));
        extruderTemperature.setText(Languager.getTagValue(1, "ControlPanel", "Current_Temperature"));
        motorSpeed.setText(Languager.getTagValue(1, "ControlPanel", "Motor_Speed"));
        extrudeDuration.setText(Languager.getTagValue(1, "ControlPanel", "Extrude_Duration"));
        bReverse.setText(Languager.getTagValue(1, "ControlPanel", "Reverse"));
        bForward.setText(Languager.getTagValue(1, "ControlPanel", "Foward"));
        motorControl.setText(Languager.getTagValue(1, "ControlPanel", "Motor_Control"));
        logTemperature.setText(Languager.getTagValue(1, "ControlPanel", "Log_Temperature"));
        notes.setText(Languager.getTagValue(1, "BaseDirectories", "Line9"));
        xyFeedrate.setText(Languager.getTagValue(1, "ControlPanel", "XY_Feedrate"));
        zFeedrate.setText(Languager.getTagValue(1, "ControlPanel", "Z_Feedrate"));
        bOK.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line6"));
    }

    private void evaluateInitialConditions() {
        inputValidation = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long currentTime;
                double val;
                boolean changed;

                changed = false;
                currentTime = System.nanoTime();

                // 1/2 sec
                if (currentTime - mSpeedLastClicked > 500000000) {
                    try {
                        val = Double.parseDouble(mSpeed.getText());

                        if (val < 0) {
                            val = -val;
                            changed = true;
                        }

                        if (val > 2000) {
                            val = 2000;
                            changed = true;
                        }

                        if (changed) {
                            mSpeed.setText(String.valueOf(val));
                        }

                    } catch (IllegalArgumentException ex) {

                    }
                }
            }
        });
        inputValidation.setRepeats(true);
        inputValidation.start();

        machine = Base.getMachineLoader().getMachineInterface();
        categories = fullFillCombo();
        comboModel = new DefaultComboBoxModel(categories);
        categories2 = fullFillComboDuration();
        comboModel2 = new DefaultComboBoxModel(categories2);

        extrudeCombo.setModel(comboModel2);
        extrudeCombo.setSelectedIndex(0);
        XYFeedrate = 2000;
        ZFeedrate = 2000;
        temperatureGoal = Double.valueOf(targetTemperatureVal.getText());
        coolFanPressed = false;
        loggingTemperature = false;
        freeJogging = false;

        // set to relative positioning
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G91"));
    }

    private String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
        Date date = new Date();

        return dateFormat.format(date);
    }

    private ChartPanel makeChart() {
        JFreeChart chart = ChartFactory.createXYLineChart(null, null, null,
                t0MeasuredDataset, PlotOrientation.VERTICAL,
                false, false, false);
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(null);

        XYPlot plot = chart.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setLowerMargin(0);
        axis.setFixedAutoRange(3L * 60L * 1000L); // auto range to three minutes

        TickUnits unitSource = new TickUnits();
        unitSource.add(new NumberTickUnit(60L * 1000L)); // minutes
        unitSource.add(new NumberTickUnit(1L * 1000L)); // seconds

        axis.setStandardTickUnits(unitSource);
        axis.setTickLabelsVisible(false); // We don't need to see the millisecond count
        axis = plot.getRangeAxis();
        axis.setRange(0, 300); // set termperature range from 0 to 300 degrees C so you can see overshoots 

        // Tweak L&F of chart
        //((XYAreaRenderer)plot.getRenderer()).setOutline(true);
        XYStepRenderer renderer = new XYStepRenderer();
        plot.setDataset(1, t0TargetDataset);
        plot.setRenderer(1, renderer);
        plot.getRenderer(1).setSeriesPaint(0, t0TargetColor);
        plot.getRenderer(0).setSeriesPaint(0, t0MeasuredColor);

//		if(machine.getModel().getTools().size() > 1)
//		{
//			plot.setDataset(4, t1MeasuredDataset);
//			plot.setRenderer(4, new XYLineAndShapeRenderer(true,false)); 
//			plot.getRenderer(4).setSeriesPaint(0, t1MeasuredColor);
//			plot.setDataset(5, t1TargetDataset);
//			plot.setRenderer(5, new XYStepRenderer()); 
//			plot.getRenderer(5).setSeriesPaint(0, t1TargetColor);
//
//		}
        plot.setDataset(2, pMeasuredDataset);
        plot.setRenderer(2, new XYLineAndShapeRenderer(true, false));
        plot.getRenderer(2).setSeriesPaint(0, pMeasuredColor);
        plot.setDataset(3, pTargetDataset);
        plot.setRenderer(3, new XYStepRenderer());
        plot.getRenderer(3).setSeriesPaint(0, pTargetColor);

        plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 160));
        chartPanel.setOpaque(false);
        return chartPanel;
    }

    private String[] fullFillCombo() {
        String[] moves = {
            "0.05",
            "0.5",
            "1",
            "5",
            "10",
            "15",
            "25",};

        return moves;
    }

    private String[] fullFillComboDuration() {
        String[] duration = {
            "3",
            "6",
            "9",
            "12",
            "15",};

        return duration;
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
                text = text.concat(parts[i]).concat(".");
            }
        }

        return ihtml.concat(text).concat(ehtml);
    }

    private int getStringPixelsWidth(String s) {
        Graphics g = getGraphics();
        FontMetrics fm = g.getFontMetrics(GraphicDesignComponents.getSSProRegular("10"));
        return fm.stringWidth(s);
    }

    private double getDistance() {
        try {
            double eDuration = Double.valueOf(extrudeCombo.getSelectedItem().toString());
            double eSpeed = Double.valueOf(mSpeed.getText());
            return (eSpeed / 60.0) * eDuration;

        } catch (IllegalArgumentException ex) {
            return 0;
        }

    }

    public void updateTemperature() {
        double extruderTemp, blockTemp;
        String extruderTempString, blockTempString;

        machine.runCommand(new replicatorg.drivers.commands.ReadTemperature());

        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        extruderTemp = machine.getModel().currentTool().getExtruderTemperature();
        blockTemp = machine.getModel().currentTool().getBlockTemperature();
        extruderTempString = String.valueOf(extruderTemp);
        blockTempString = String.valueOf(blockTemp);
        extruderTemperatureVal.setText(extruderTempString);
        blockTemperatureVal.setText(blockTempString);

        if (loggingTemperature) {
            try {
                bw.write(extruderTempString);
                bw.write(blockTempString);
                bw.newLine();
            } catch (IOException ex) {
                Base.writeLog("Can't write temperature to file");
            }
        }

        //Graph variables
        Second second = new Second(new Date(System.currentTimeMillis() - startMillis));

        t0MeasuredDataset.add(second, extruderTemp, "a");
        t0TargetDataset.add(second, this.temperatureGoal, "a");

    }

    public double getTargetTemperature() {
        return temperatureGoal;
    }

    private void initFile() {
        this.file = new File(Base.getAppDataDirectory() + "/" + REDMEC_TAG + getDate() + ".txt");
        try {
            this.fw = new FileWriter(file.getAbsoluteFile());
        } catch (IOException ex) {
            Base.writeLog("Can't create file to log temperature");
        }
        this.bw = new BufferedWriter(fw);
    }

    private void cleanLogFiles(boolean force) {
        File logsDir = new File(Base.getAppDataDirectory().getAbsolutePath());
        File[] logsList = logsDir.listFiles();

        for (File logsList1 : logsList) {
            if (force) {
                if (logsList1.getName().contains(REDMEC_TAG) && logsList1.length() == 0) {
                    logsList1.delete();
                }
            } // no need for else
        }
    }

    private void doCancel() {
        Base.getMainWindow().getButtons().updatePressedStateButton("quick_guide");
        Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
        Base.maintenanceWizardOpen = false;
        disposeThread.interrupt();
        inputValidation.stop();
        Base.getMainWindow().setEnabled(true);
        cleanLogFiles(true);

        machine.runCommand(new replicatorg.drivers.commands.SendHome());
        dispose();
        Base.bringAllWindowsToFront();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jExtruderFanValue = new javax.swing.JTextField();
        jExtruderFanTitle = new javax.swing.JLabel();
        jSliderBlowerSpeed = new javax.swing.JSlider();
        jSliderExtruderSpeed = new javax.swing.JSlider();
        bBeep = new javax.swing.JButton();
        jBlowerFanTitle = new javax.swing.JLabel();
        jBlowerFanValue = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        freeJog = new javax.swing.JCheckBox();
        panic = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        zTextFieldValue = new javax.swing.JTextField();
        xTextFieldValue = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        xRIGHT = new javax.swing.JLabel();
        yDOWN = new javax.swing.JLabel();
        yUP = new javax.swing.JLabel();
        enableFreeJog = new javax.swing.JLabel();
        zUP = new javax.swing.JLabel();
        yTextFieldValue = new javax.swing.JTextField();
        xLEFT = new javax.swing.JLabel();
        zDOWN = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        bHome = new javax.swing.JButton();
        bCurrentPosition = new javax.swing.JButton();
        bCenterX = new javax.swing.JButton();
        feedRate = new javax.swing.JLabel();
        xyFeedrate = new javax.swing.JLabel();
        bCenterY = new javax.swing.JButton();
        bSetCalibration = new javax.swing.JButton();
        bCalibrateA = new javax.swing.JButton();
        bCalibrateC = new javax.swing.JButton();
        xyFeed = new javax.swing.JTextField();
        bCalibrateB = new javax.swing.JButton();
        zFeedrate = new javax.swing.JLabel();
        zFeed = new javax.swing.JTextField();
        bCenterZ = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        notes = new javax.swing.JLabel();
        tempPanel = new javax.swing.JPanel();
        tempLabel = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        extruderTemperature1 = new javax.swing.JLabel();
        targetTemperatureVal = new javax.swing.JTextField();
        saveLog = new javax.swing.JLabel();
        extruderTemperature = new javax.swing.JLabel();
        cLogTemperature = new javax.swing.JCheckBox();
        logTemperature = new javax.swing.JLabel();
        blockTemperatureVal = new javax.swing.JTextField();
        extruderTemperatureVal = new javax.swing.JTextField();
        colorCurrentTemp = new javax.swing.JLabel();
        colorTargetTemp = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        bForward = new javax.swing.JButton();
        extrudeCombo = new javax.swing.JComboBox();
        bReverse = new javax.swing.JButton();
        mSpeed = new javax.swing.JTextField();
        motorSpeed = new javax.swing.JLabel();
        extrudeDuration = new javax.swing.JLabel();
        motorControl = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        bOK = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));
        jPanel1.setToolTipText("");

        jPanel2.setBackground(new java.awt.Color(248, 248, 248));
        jPanel2.setToolTipText("");
        jPanel2.setPreferredSize(new java.awt.Dimension(415, 409));

        jPanel7.setOpaque(false);

        jExtruderFanValue.setEditable(false);
        jExtruderFanValue.setText("0");

        jExtruderFanTitle.setText("Extruder fan");

        jSliderBlowerSpeed.setMajorTickSpacing(60);
        jSliderBlowerSpeed.setMaximum(255);
        jSliderBlowerSpeed.setMinorTickSpacing(30);
        jSliderBlowerSpeed.setPaintLabels(true);
        jSliderBlowerSpeed.setPaintTicks(true);
        jSliderBlowerSpeed.setToolTipText("");
        jSliderBlowerSpeed.setValue(0);
        jSliderBlowerSpeed.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jSliderBlowerSpeedMouseReleased(evt);
            }
        });

        jSliderExtruderSpeed.setMajorTickSpacing(60);
        jSliderExtruderSpeed.setMaximum(255);
        jSliderExtruderSpeed.setMinorTickSpacing(30);
        jSliderExtruderSpeed.setPaintLabels(true);
        jSliderExtruderSpeed.setPaintTicks(true);
        jSliderExtruderSpeed.setToolTipText("");
        jSliderExtruderSpeed.setValue(0);
        jSliderExtruderSpeed.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jSliderExtruderSpeedMouseReleased(evt);
            }
        });

        bBeep.setText("Beep");
        bBeep.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bBeepMousePressed(evt);
            }
        });

        jBlowerFanTitle.setText("Blower fan");

        jBlowerFanValue.setEditable(false);
        jBlowerFanValue.setText("0");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bBeep, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(75, 75, 75)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                            .addComponent(jExtruderFanTitle)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jExtruderFanValue, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jSliderExtruderSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                            .addComponent(jBlowerFanTitle)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jBlowerFanValue, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jSliderBlowerSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBlowerFanTitle)
                    .addComponent(jBlowerFanValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSliderBlowerSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jExtruderFanTitle)
                    .addComponent(jExtruderFanValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bBeep))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSliderExtruderSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel8.setOpaque(false);

        freeJog.setBackground(new java.awt.Color(248, 248, 248));
        freeJog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freeJogActionPerformed(evt);
            }
        });

        panic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/panicOver.png"))); // NOI18N
        panic.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                panicMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                panicMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                panicMouseEntered(evt);
            }
        });

        jLabel2.setText("X");

        zTextFieldValue.setEditable(false);
        zTextFieldValue.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                zTextFieldValueKeyPressed(evt);
            }
        });

        xTextFieldValue.setEditable(false);
        xTextFieldValue.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                xTextFieldValueKeyPressed(evt);
            }
        });

        jLabel4.setText("Z");

        jLabel3.setText("Y");

        xRIGHT.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/X+.png"))); // NOI18N
        xRIGHT.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                xRIGHTMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                xRIGHTMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                xRIGHTMouseEntered(evt);
            }
        });

        yDOWN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/Y-.png"))); // NOI18N
        yDOWN.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                yDOWNMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                yDOWNMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                yDOWNMouseEntered(evt);
            }
        });

        yUP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/Y+.png"))); // NOI18N
        yUP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                yUPMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                yUPMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                yUPMouseEntered(evt);
            }
        });

        enableFreeJog.setText("Free jog");

        zUP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/Z+.png"))); // NOI18N
        zUP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                zUPMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                zUPMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                zUPMouseEntered(evt);
            }
        });

        yTextFieldValue.setEditable(false);
        yTextFieldValue.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                yTextFieldValueKeyPressed(evt);
            }
        });

        xLEFT.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/X-.png"))); // NOI18N
        xLEFT.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                xLEFTMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                xLEFTMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                xLEFTMouseEntered(evt);
            }
        });

        zDOWN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/Z-.png"))); // NOI18N
        zDOWN.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                zDOWNMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                zDOWNMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                zDOWNMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(57, 57, 57)
                        .addComponent(yUP))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(xLEFT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(yDOWN)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(panic)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(xRIGHT)))))
                .addGap(18, 18, 18)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(zDOWN)
                    .addComponent(zUP))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(enableFreeJog, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(freeJog))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                                    .addComponent(jLabel4)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                .addGroup(jPanel8Layout.createSequentialGroup()
                                    .addComponent(jLabel3)
                                    .addGap(13, 13, 13)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(13, 13, 13)))
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(xTextFieldValue, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(yTextFieldValue)
                                .addComponent(zTextFieldValue, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(34, 34, 34))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(yUP)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(xLEFT)
                                    .addComponent(panic)
                                    .addComponent(xRIGHT))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yDOWN))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addComponent(zUP)
                                .addGap(14, 14, 14)
                                .addComponent(zDOWN))))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(freeJog)
                            .addComponent(enableFreeJog))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(xTextFieldValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(yTextFieldValue, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(zTextFieldValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))))
                .addContainerGap())
        );

        jPanel9.setOpaque(false);

        bHome.setText("Home XYZ");
        bHome.setPreferredSize(new java.awt.Dimension(177, 29));
        bHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bHomeActionPerformed(evt);
            }
        });

        bCurrentPosition.setText("Make Current position 0");
        bCurrentPosition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCurrentPositionActionPerformed(evt);
            }
        });

        bCenterX.setText("Home X");
        bCenterX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCenterXActionPerformed(evt);
            }
        });

        feedRate.setText("Feedrate");

        xyFeedrate.setText("XY (mm/s)");

        bCenterY.setText("Home Y");
        bCenterY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCenterYActionPerformed(evt);
            }
        });

        bSetCalibration.setText("Set Calibration");
        bSetCalibration.setPreferredSize(new java.awt.Dimension(177, 29));
        bSetCalibration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSetCalibrationActionPerformed(evt);
            }
        });

        bCalibrateA.setText("Calibrate A");
        bCalibrateA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCalibrateAActionPerformed(evt);
            }
        });

        bCalibrateC.setText("Calibrate C");
        bCalibrateC.setPreferredSize(new java.awt.Dimension(145, 29));
        bCalibrateC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCalibrateCActionPerformed(evt);
            }
        });

        xyFeed.setText("1.0");
        xyFeed.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                xyFeedKeyReleased(evt);
            }
        });

        bCalibrateB.setText("Calibrate B");
        bCalibrateB.setPreferredSize(new java.awt.Dimension(145, 29));
        bCalibrateB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCalibrateBActionPerformed(evt);
            }
        });

        zFeedrate.setText("Z (mm/s)");

        zFeed.setText("1.0");
        zFeed.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                zFeedKeyReleased(evt);
            }
        });

        bCenterZ.setText("Home Z");
        bCenterZ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCenterZActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(bCalibrateB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(bCalibrateA, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(bCalibrateC, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(bSetCalibration, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(zFeedrate)
                            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(xyFeedrate, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(feedRate)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(xyFeed)
                            .addComponent(zFeed, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(3, 3, 3)))
                .addGap(75, 75, 75)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(bCurrentPosition, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bCenterZ, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bCenterY, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bCenterX, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bHome, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(bCalibrateA)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bCalibrateB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bCalibrateC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bSetCalibration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(feedRate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(xyFeedrate)
                            .addComponent(xyFeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(bHome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(bCenterX)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bCenterY)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bCenterZ)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bCurrentPosition)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zFeedrate)
                    .addComponent(zFeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(248, 248, 248));

        notes.setText("All files saved under BEESOFT folder in user directory.");
        notes.setToolTipText("");

        tempPanel.setMinimumSize(new java.awt.Dimension(340, 130));

        javax.swing.GroupLayout tempPanelLayout = new javax.swing.GroupLayout(tempPanel);
        tempPanel.setLayout(tempPanelLayout);
        tempPanelLayout.setHorizontalGroup(
            tempPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        tempPanelLayout.setVerticalGroup(
            tempPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 160, Short.MAX_VALUE)
        );

        tempLabel.setText("Temperature Chart");

        jPanel5.setOpaque(false);

        extruderTemperature1.setText("Block temperature");

        targetTemperatureVal.setText("0");
        targetTemperatureVal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                targetTemperatureValKeyReleased(evt);
            }
        });

        saveLog.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/floppy_disk.png"))); // NOI18N
        saveLog.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                saveLogMousePressed(evt);
            }
        });

        extruderTemperature.setText("Extruder temperature");

        cLogTemperature.setBackground(new java.awt.Color(248, 248, 248));
        cLogTemperature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cLogTemperatureActionPerformed(evt);
            }
        });

        logTemperature.setText("Log Temperature");
        logTemperature.setToolTipText("");

        blockTemperatureVal.setEditable(false);
        blockTemperatureVal.setText("0");

        extruderTemperatureVal.setEditable(false);
        extruderTemperatureVal.setText("0");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(logTemperature, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cLogTemperature)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(saveLog))
                    .addComponent(extruderTemperature1)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(extruderTemperature, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(blockTemperatureVal, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                            .addComponent(extruderTemperatureVal))
                        .addGap(6, 6, 6)
                        .addComponent(targetTemperatureVal, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(96, 96, 96))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extruderTemperature)
                    .addComponent(extruderTemperatureVal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(targetTemperatureVal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extruderTemperature1)
                    .addComponent(blockTemperatureVal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(saveLog, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cLogTemperature)
                    .addComponent(logTemperature))
                .addContainerGap())
        );

        colorCurrentTemp.setBackground(new java.awt.Color(204, 204, 204));
        colorCurrentTemp.setLabelFor(extruderTemperatureVal);
        colorCurrentTemp.setText("color1");

        colorTargetTemp.setBackground(new java.awt.Color(204, 204, 204));
        colorTargetTemp.setText("color1");

        jPanel6.setOpaque(false);

        bForward.setText("Foward");
        bForward.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                bForwardMouseReleased(evt);
            }
        });

        extrudeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        bReverse.setText("Reverse");
        bReverse.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                bReverseMouseReleased(evt);
            }
        });

        mSpeed.setText("500");
        mSpeed.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                mSpeedKeyReleased(evt);
            }
        });

        motorSpeed.setText("Speed");

        extrudeDuration.setText("Extrude duration");

        motorControl.setText("Motor control");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(88, 88, 88)
                        .addComponent(bReverse, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(38, 38, 38)
                        .addComponent(bForward, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(motorControl, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(extrudeDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(extrudeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(motorSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(87, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(motorControl)
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(motorSpeed))
                .addGap(17, 17, 17)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extrudeDuration)
                    .addComponent(extrudeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bReverse)
                    .addComponent(bForward))
                .addContainerGap())
        );

        jLabel1.setText("Current extruder temperature");

        jLabel5.setText("Target extruder temperature");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jSeparator1)
                        .addGap(2, 2, 2))
                    .addComponent(notes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(tempPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(tempLabel))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(66, 66, 66)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(colorTargetTemp)
                            .addComponent(colorCurrentTemp))
                        .addGap(39, 39, 39)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel5))))
                .addGap(0, 33, Short.MAX_VALUE))
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addComponent(tempLabel)
                .addGap(1, 1, 1)
                .addComponent(tempPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(colorCurrentTemp)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(colorTargetTemp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5))
                .addGap(16, 16, 16)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(notes)
                .addGap(26, 26, 26))
        );

        jPanel4.setBackground(new java.awt.Color(255, 203, 5));
        jPanel4.setMinimumSize(new java.awt.Dimension(20, 38));
        jPanel4.setPreferredSize(new java.awt.Dimension(567, 38));

        bOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bOK.setText("OK");
        bOK.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bOK.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bOKMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bOKMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bOKMousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(958, 958, 958)
                .addComponent(bOK)
                .addGap(25, 25, 25))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(bOK)
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 544, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 1039, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 726, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void targetTemperatureValKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_targetTemperatureValKeyReleased
        temperatureGoal = Double.valueOf(targetTemperatureVal.getText());
    }//GEN-LAST:event_targetTemperatureValKeyReleased

    private void bOKMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bOKMouseEntered
        bOK.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bOKMouseEntered

    private void bOKMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bOKMouseExited
        bOK.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bOKMouseExited

    private void bOKMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bOKMousePressed
        doCancel();
    }//GEN-LAST:event_bOKMousePressed

    private void cLogTemperatureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cLogTemperatureActionPerformed
        if (loggingTemperature) {
            this.loggingTemperature = false;
            cleanLogFiles(true);

        } else {
            this.loggingTemperature = true;
            initFile();
            cleanLogFiles(false);
        }


    }//GEN-LAST:event_cLogTemperatureActionPerformed

    private void saveLogMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveLogMousePressed
        if (loggingTemperature) {
            try {
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            initFile();
        }
    }//GEN-LAST:event_saveLogMousePressed

    private void freeJogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freeJogActionPerformed
        if (freeJogging) {
            freeJog.setSelected(false);
            freeJogging = false;
        } else {
            freeJog.setSelected(true);
            freeJogging = true;
        }
    }//GEN-LAST:event_freeJogActionPerformed

    private void zFeedKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_zFeedKeyReleased
        ZFeedrate = Double.valueOf(zFeed.getText()) * 60;
    }//GEN-LAST:event_zFeedKeyReleased

    private void xyFeedKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_xyFeedKeyReleased
        XYFeedrate = Double.valueOf(xyFeed.getText()) * 60;
    }//GEN-LAST:event_xyFeedKeyReleased

    private void bSetCalibrationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSetCalibrationActionPerformed
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M603"));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M601"));
    }//GEN-LAST:event_bSetCalibrationActionPerformed

    private void bCalibrateCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCalibrateCActionPerformed
        Point5d current = machine.getDriver().getCurrentPosition(false);
        Point5d c = machine.getTablePoints("C");
        Point5d raise = new Point5d(current.x(), current.y(), current.z() + 10);
        Point5d cRaise = new Point5d(c.x(), c.y(), c.z() + 10);

        double acLow = machine.getAcceleration("acLow");
        double acHigh = machine.getAcceleration("acHigh");
        double spHigh = machine.getFeedrate("spHigh");
        double spMedium = machine.getFeedrate("spMedium");

        machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spHigh));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acLow));
        machine.runCommand(new replicatorg.drivers.commands.QueuePoint(raise));
        machine.runCommand(new replicatorg.drivers.commands.QueuePoint(cRaise));
        machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spMedium));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + spMedium));
        machine.runCommand(new replicatorg.drivers.commands.QueuePoint(c));
    }//GEN-LAST:event_bCalibrateCActionPerformed

    private void bCalibrateBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCalibrateBActionPerformed
        Point5d current = machine.getDriver().getCurrentPosition(false);

        machine.runCommand(new replicatorg.drivers.commands.SetCurrentPosition(new Point5d(current.x(), current.y(), 0)));
        current = machine.getDriver().getCurrentPosition(false);
        Point5d b = machine.getTablePoints("B");
        Point5d raise = new Point5d(current.x(), current.y(), current.z() + 10);
        Point5d bRaise = new Point5d(b.x(), b.y(), b.z() + 10);

        double acLow = machine.getAcceleration("acLow");
        double acHigh = machine.getAcceleration("acHigh");
        double spHigh = machine.getFeedrate("spHigh");
        double spMedium = machine.getFeedrate("spMedium");

        machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spHigh));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acLow));
        machine.runCommand(new replicatorg.drivers.commands.QueuePoint(raise));
        machine.runCommand(new replicatorg.drivers.commands.QueuePoint(bRaise));
        machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spMedium));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acHigh));
        machine.runCommand(new replicatorg.drivers.commands.QueuePoint(b));
    }//GEN-LAST:event_bCalibrateBActionPerformed

    private void bCalibrateAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCalibrateAActionPerformed
        Point5d current;

        Base.writeLog("Initializing and Calibrating A");

        machine.getDriver().setMachineReady(false);
        machine.getDriver().setBusy(true);
        machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(2000));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28", COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.GetPosition());
        machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));

        /**
         *  //This is important! without this loop, the following line may not
         * work properly current =
         * machine.getDriver().getCurrentPosition(false);
         */
        while (!machine.getDriver().getMachineStatus() && machine.getDriver().isBusy()) {
            try {
                Thread.sleep(100);
                machine.runCommand(new replicatorg.drivers.commands.ReadStatus());

            } catch (InterruptedException ex) {
                Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        //This line is crucial!!

        current = machine.getDriver().getCurrentPosition(false);

        AxisId axis = AxisId.valueOf("Z");
        Point5d a = machine.getTablePoints("A");

        //            System.out.println("current:"+current);
        current.setAxis(axis, (current.axis(axis) - (safeDistance)));
        current.setX(a.x());
        current.setY(a.y());

        double acLow = machine.getAcceleration("acLow");
        double acHigh = machine.getAcceleration("acHigh");
        double spHigh = machine.getFeedrate("spHigh");
        machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spHigh));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acLow));
        machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acHigh));
    }//GEN-LAST:event_bCalibrateAActionPerformed

    private void bHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bHomeActionPerformed
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand(HOME, COM.BLOCK));
    }//GEN-LAST:event_bHomeActionPerformed

    private void bCurrentPositionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCurrentPositionActionPerformed
        machine.runCommand(new replicatorg.drivers.commands.SetCurrentPosition(new Point5d()));
    }//GEN-LAST:event_bCurrentPositionActionPerformed

    private void bCenterZActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCenterZActionPerformed
        //        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F"+XYFeedrate+" Z0",COM.DEFAULT));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28 Z", COM.BLOCK));
    }//GEN-LAST:event_bCenterZActionPerformed

    private void bCenterYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCenterYActionPerformed
        //        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F"+XYFeedrate+" Y0",COM.DEFAULT));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28 Y", COM.BLOCK));
    }//GEN-LAST:event_bCenterYActionPerformed

    private void bCenterXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCenterXActionPerformed
        //        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F"+XYFeedrate+" X0",COM.DEFAULT));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28 X", COM.BLOCK));
    }//GEN-LAST:event_bCenterXActionPerformed

    private void zTextFieldValueKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_zTextFieldValueKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            parseAndJogZ();
        }
    }//GEN-LAST:event_zTextFieldValueKeyPressed

    private void yTextFieldValueKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_yTextFieldValueKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            parseAndJogY();
        }
    }//GEN-LAST:event_yTextFieldValueKeyPressed

    private void xTextFieldValueKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_xTextFieldValueKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            parseAndJogX();
        }
    }//GEN-LAST:event_xTextFieldValueKeyPressed

    private void zDOWNMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zDOWNMousePressed
        jogButtonPressed = true;
        double jogValue = Double.valueOf(comboModel.getSelectedItem().toString());
        Point5d current = machine.getDriver().getActualPosition();
        AxisId axis = AxisId.valueOf("Z");
        Base.writeLog("Calibrating table in negative axis");

        machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(ZFeedrate));

        if (!freeJogging) {
            current.setAxis(axis, (current.axis(axis) + (+jogValue)));
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
            //            machine.getDriver().dispatchCommandBypass("G1 X"+current.x()+" Y"+current.y()+" Z"+current.z());
        } else {
            double target = Double.valueOf(zTextFieldValue.getText());
            double actual = machine.getDriver().getActualPosition().z();
            jogValue = target - actual;
            current.setAxis(axis, (current.axis(axis) + (jogValue)));
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
            //            machine.getDriver().dispatchCommandBypass("G1 X"+current.x()+" Y"+current.y()+" Z"+current.z());
        }
        jogButtonPressed = false;
    }//GEN-LAST:event_zDOWNMousePressed

    private void zDOWNMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zDOWNMouseExited
        zDOWN.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "Z-.png")));
    }//GEN-LAST:event_zDOWNMouseExited

    private void zDOWNMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zDOWNMouseEntered
        zDOWN.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "Z-Over.png")));
    }//GEN-LAST:event_zDOWNMouseEntered

    private void zUPMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zUPMousePressed
        parseAndJogZ();
    }//GEN-LAST:event_zUPMousePressed

    private void zUPMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zUPMouseExited
        zUP.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "Z+.png")));
    }//GEN-LAST:event_zUPMouseExited

    private void zUPMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zUPMouseEntered
        zUP.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "Z+Over.png")));
    }//GEN-LAST:event_zUPMouseEntered

    private void panicMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panicMouseEntered
        panic.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "panic.png")));
    }//GEN-LAST:event_panicMouseEntered

    private void panicMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panicMouseExited
        panic.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "panicOver.png")));
    }//GEN-LAST:event_panicMouseExited

    private void panicMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panicMousePressed
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand(STOP, COM.DEFAULT));
    }//GEN-LAST:event_panicMousePressed

    private void yDOWNMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yDOWNMouseEntered
        yDOWN.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "Y-Over.png")));
    }//GEN-LAST:event_yDOWNMouseEntered

    private void yDOWNMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yDOWNMouseExited
        yDOWN.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "Y-.png")));
    }//GEN-LAST:event_yDOWNMouseExited

    private void yDOWNMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yDOWNMousePressed
        jogButtonPressed = true;
        double jogValue = Double.valueOf(comboModel.getSelectedItem().toString());
        Point5d current = machine.getDriver().getActualPosition();
        AxisId axis = AxisId.valueOf("Y");
        Base.writeLog("Calibrating table in negative axis");

        machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(XYFeedrate));

        if (!freeJogging) {
            current.setAxis(axis, (current.axis(axis) + (+jogValue)));
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
        } else {
            double target = Double.valueOf(yTextFieldValue.getText());
            double actual = machine.getDriver().getActualPosition().y();
            jogValue = target - actual;
            current.setAxis(axis, (current.axis(axis) + (jogValue)));
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
        }
        jogButtonPressed = false;
    }//GEN-LAST:event_yDOWNMousePressed

    private void xRIGHTMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xRIGHTMouseEntered
        xRIGHT.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "X+Over.png")));
    }//GEN-LAST:event_xRIGHTMouseEntered

    private void xRIGHTMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xRIGHTMouseExited
        xRIGHT.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "X+.png")));
    }//GEN-LAST:event_xRIGHTMouseExited

    private void xRIGHTMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xRIGHTMousePressed
        parseAndJogX();
    }//GEN-LAST:event_xRIGHTMousePressed

    private void yUPMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yUPMouseEntered
        yUP.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "Y+Over.png")));
    }//GEN-LAST:event_yUPMouseEntered

    private void yUPMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yUPMouseExited
        yUP.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "Y+.png")));
    }//GEN-LAST:event_yUPMouseExited

    private void yUPMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yUPMousePressed
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G0 Y1.0 F" + xyFeed.getText()));
    }//GEN-LAST:event_yUPMousePressed

    private void xLEFTMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xLEFTMouseEntered
        xLEFT.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "X-Over.png")));
    }//GEN-LAST:event_xLEFTMouseEntered

    private void xLEFTMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xLEFTMouseExited
        xLEFT.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "X-.png")));
    }//GEN-LAST:event_xLEFTMouseExited

    private void xLEFTMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xLEFTMousePressed
        jogButtonPressed = true;
        double jogValue = Double.valueOf(comboModel.getSelectedItem().toString());
        Point5d current = machine.getDriver().getActualPosition();
        AxisId axis = AxisId.valueOf("X");
        Base.writeLog("Calibrating table in negative axis");

        machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(XYFeedrate));

        if (!freeJogging) {
            current.setAxis(axis, (current.axis(axis) + (-jogValue)));
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
        } else {
            double target = Double.valueOf(xTextFieldValue.getText());
            double actual = machine.getDriver().getActualPosition().x();
            jogValue = target - actual;
            current.setAxis(axis, (current.axis(axis) + (jogValue)));
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
        }
        jogButtonPressed = false;
    }//GEN-LAST:event_xLEFTMousePressed

    private void bBeepMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bBeepMousePressed
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300", COM.DEFAULT));
    }//GEN-LAST:event_bBeepMousePressed

    private void jSliderBlowerSpeedMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jSliderBlowerSpeedMouseReleased
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M106 S" + jSliderBlowerSpeed.getValue(), COM.DEFAULT));
        jBlowerFanValue.setText(String.valueOf(jSliderBlowerSpeed.getValue()));
    }//GEN-LAST:event_jSliderBlowerSpeedMouseReleased

    private void jSliderExtruderSpeedMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jSliderExtruderSpeedMouseReleased
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M126 S" + jSliderExtruderSpeed.getValue(), COM.DEFAULT));
        jExtruderFanValue.setText(String.valueOf(jSliderExtruderSpeed.getValue()));
    }//GEN-LAST:event_jSliderExtruderSpeedMouseReleased

    private void bForwardMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bForwardMouseReleased
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G92 E0", COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F" + Double.valueOf(mSpeed.getText()) + " E" + getDistance(), COM.BLOCK));
    }//GEN-LAST:event_bForwardMouseReleased

    private void bReverseMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bReverseMouseReleased
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G92 E0", COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F" + Double.valueOf(mSpeed.getText()) + " E-" + getDistance(), COM.BLOCK));
    }//GEN-LAST:event_bReverseMouseReleased

    private void mSpeedKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mSpeedKeyReleased
        mSpeedLastClicked = System.nanoTime();
    }//GEN-LAST:event_mSpeedKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBeep;
    private javax.swing.JButton bCalibrateA;
    private javax.swing.JButton bCalibrateB;
    private javax.swing.JButton bCalibrateC;
    private javax.swing.JButton bCenterX;
    private javax.swing.JButton bCenterY;
    private javax.swing.JButton bCenterZ;
    private javax.swing.JButton bCurrentPosition;
    private javax.swing.JButton bForward;
    private javax.swing.JButton bHome;
    private javax.swing.JLabel bOK;
    private javax.swing.JButton bReverse;
    private javax.swing.JButton bSetCalibration;
    private javax.swing.JTextField blockTemperatureVal;
    private javax.swing.JCheckBox cLogTemperature;
    private javax.swing.JLabel colorCurrentTemp;
    private javax.swing.JLabel colorTargetTemp;
    private javax.swing.JLabel enableFreeJog;
    private javax.swing.JComboBox extrudeCombo;
    private javax.swing.JLabel extrudeDuration;
    private javax.swing.JLabel extruderTemperature;
    private javax.swing.JLabel extruderTemperature1;
    private javax.swing.JTextField extruderTemperatureVal;
    private javax.swing.JLabel feedRate;
    private javax.swing.JCheckBox freeJog;
    private javax.swing.JLabel jBlowerFanTitle;
    private javax.swing.JTextField jBlowerFanValue;
    private javax.swing.JLabel jExtruderFanTitle;
    private javax.swing.JTextField jExtruderFanValue;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSlider jSliderBlowerSpeed;
    private javax.swing.JSlider jSliderExtruderSpeed;
    private javax.swing.JLabel logTemperature;
    private javax.swing.JTextField mSpeed;
    private javax.swing.JLabel motorControl;
    private javax.swing.JLabel motorSpeed;
    private javax.swing.JLabel notes;
    private javax.swing.JLabel panic;
    private javax.swing.JLabel saveLog;
    private javax.swing.JTextField targetTemperatureVal;
    private javax.swing.JLabel tempLabel;
    private javax.swing.JPanel tempPanel;
    private javax.swing.JLabel xLEFT;
    private javax.swing.JLabel xRIGHT;
    private javax.swing.JTextField xTextFieldValue;
    private javax.swing.JTextField xyFeed;
    private javax.swing.JLabel xyFeedrate;
    private javax.swing.JLabel yDOWN;
    private javax.swing.JTextField yTextFieldValue;
    private javax.swing.JLabel yUP;
    private javax.swing.JLabel zDOWN;
    private javax.swing.JTextField zFeed;
    private javax.swing.JLabel zFeedrate;
    private javax.swing.JTextField zTextFieldValue;
    private javax.swing.JLabel zUP;
    // End of variables declaration//GEN-END:variables

    private void parseAndJogX() {
        jogButtonPressed = true;
        double jogValue = Double.valueOf(comboModel.getSelectedItem().toString());
        Point5d current = machine.getDriver().getActualPosition();
        AxisId axis = AxisId.valueOf("X");
        Base.writeLog("Calibrating table in negative axis");

        machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(XYFeedrate));

        if (!freeJogging) {
            current.setAxis(axis, (current.axis(axis) + (+jogValue)));
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
        } else {
            double target = Double.valueOf(xTextFieldValue.getText());
            double actual = machine.getDriver().getActualPosition().x();
            jogValue = target - actual;
            current.setAxis(axis, (current.axis(axis) + (jogValue)));
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
        }
        jogButtonPressed = false;
    }

    private void parseAndJogY() {
        jogButtonPressed = true;
        double jogValue = Double.valueOf(comboModel.getSelectedItem().toString());
        Point5d current = machine.getDriver().getActualPosition();
        AxisId axis = AxisId.valueOf("Y");
        Base.writeLog("Calibrating table in negative axis");

        machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(XYFeedrate));

        if (!freeJogging) {
            current.setAxis(axis, (current.axis(axis) + (-jogValue)));
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
        } else {
            double target = Double.valueOf(yTextFieldValue.getText());
            double actual = machine.getDriver().getActualPosition().y();
            jogValue = target - actual;
            current.setAxis(axis, (current.axis(axis) + (jogValue)));
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
        }
        jogButtonPressed = false;
    }

    private void parseAndJogZ() {
        jogButtonPressed = true;
        double jogValue = Double.valueOf(comboModel.getSelectedItem().toString());
        Point5d current = machine.getDriver().getActualPosition();
        AxisId axis = AxisId.valueOf("Z");
        Base.writeLog("Calibrating table in negative axis");

        machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(ZFeedrate));

        if (!freeJogging) {
            current.setAxis(axis, (current.axis(axis) + (-jogValue)));
//            machine.getDriver().dispatchCommandBypass("G1 X"+current.x()+" Y"+current.y()+" Z"+current.z());
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
        } else {
            double target = Double.valueOf(zTextFieldValue.getText());
            double actual = machine.getDriver().getActualPosition().z();
            jogValue = target - actual;
            current.setAxis(axis, (current.axis(axis) + (jogValue)));
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
//            machine.getDriver().dispatchCommandBypass("G1 X"+current.x()+" Y"+current.y()+" Z"+current.z());
        }
        jogButtonPressed = false;
    }

    public boolean isJogging() {
        return jogButtonPressed;
    }
}

class TemperatureThread extends Thread {

    private final MachineInterface machine;
    private final ControlPanel controlPanel;

    public TemperatureThread(ControlPanel cPanel, MachineInterface mach) {
        super("Cleanup Thread");
        this.machine = mach;
        this.controlPanel = cPanel;
    }

    @Override
    public void run() {

        while (this.isInterrupted() == false) {
            if (controlPanel.isJogging() == false) {
                machine.runCommand(new replicatorg.drivers.commands.SetTemperature(controlPanel.getTargetTemperature()));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
                }

                controlPanel.updateTemperature();
            }
        }
    }
}
