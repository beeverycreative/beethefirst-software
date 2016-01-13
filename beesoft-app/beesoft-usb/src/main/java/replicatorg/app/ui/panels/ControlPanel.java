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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.Timer;
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

    private enum MovDir {

        Z_PLUS, Z_MINUS, Y_PLUS, Y_MINUS, X_PLUS, X_MINUS
    };

    private MachineInterface machine = Base.getMachineLoader().getMachineInterface();
    protected double temperatureGoal = 0;
    protected double zHome = -1;
    private DefaultComboBoxModel comboModel2;
    private String[] categories2;
    private final TemperatureThread disposeThread = new TemperatureThread(this, machine);
    private final InputValidationThread inputValidationThread = new InputValidationThread(this);
    private final GetInitialValuesThread getInitialValuesThread = new GetInitialValuesThread(this, machine);
    private static boolean loggingTemperature = false;
    private File file;
    private FileWriter fw;
    private BufferedWriter bw;
    private static final String REDMEC_TAG = "[TemperatureLog]";

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
    private Timer setPollDataTrue;
    private Timer showBeepLabel;
    protected Timer movButtonHoldDown;
    private static final int movCommandInterval = 25;
    private static final double movCommandStep = 0.6;
    protected volatile boolean canPollData = true;
    protected volatile boolean canMove = true;

    protected boolean validPosition = false;

    public ControlPanel() {
        super(Base.getMainWindow(), Dialog.ModalityType.MODELESS);
        initComponents();
        Base.writeLog("Control panel opened...", this.getClass());
        setFont();
        setTextLanguage();
        centerOnScreen();
        enableDrag();
        evaluateInitialConditions();

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

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (movButtonHoldDown != null && movButtonHoldDown.isRunning()) {
                    movButtonHoldDown.stop();
                }
                machine.getDriver().dispatchCommand("M1110 S0", COM.DEFAULT);

                if (loggingTemperature == false) {
                    disposeThread.cancel();
                }
                inputValidationThread.cancel();
                getInitialValuesThread.cancel();
            }
        });
    }

    private void setFont() {
        extrudeCombo.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bBeep.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bCurrentPosition.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bCurrentPosition.setFont(GraphicDesignComponents.getSSProRegular("14"));
        extruderTemperatureLabel.setFont(GraphicDesignComponents.getSSProRegular("14"));
        blockTemperatureLabel.setFont(GraphicDesignComponents.getSSProRegular("14"));
        jLabel1.setFont(GraphicDesignComponents.getSSProRegular("14"));
        jLabel5.setFont(GraphicDesignComponents.getSSProRegular("14"));
        motorSpeed.setFont(GraphicDesignComponents.getSSProRegular("14"));
        extrudeDuration.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bReverse.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bForward.setFont(GraphicDesignComponents.getSSProRegular("14"));
        logTemperature.setFont(GraphicDesignComponents.getSSProRegular("14"));
        notes.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bOK.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bTurnOnLEDs.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bTurnOffLEDs.setFont(GraphicDesignComponents.getSSProRegular("14"));
        labelLastPrintTime.setFont(GraphicDesignComponents.getSSProRegular("14"));
        jLabel6.setFont(GraphicDesignComponents.getSSProRegular("14"));
    }

    private void setTextLanguage() {
        //bCurrentPosition.setText(Languager.getTagValue(1, "ControlPanel", "CurrentPosition"));
        extruderTemperatureLabel.setText(Languager.getTagValue(1, "ControlPanel", "Current_Temperature"));
        motorSpeed.setText(Languager.getTagValue(1, "ControlPanel", "Motor_Speed"));
        extrudeDuration.setText(Languager.getTagValue(1, "ControlPanel", "Extrude_Duration"));
        bReverse.setText(Languager.getTagValue(1, "ControlPanel", "Reverse"));
        bForward.setText(Languager.getTagValue(1, "ControlPanel", "Foward"));
        logTemperature.setText(Languager.getTagValue(1, "ControlPanel", "Log_Temperature"));
        notes.setText(Languager.getTagValue(1, "BaseDirectories", "Line9"));
        bOK.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line6"));
    }

    private void evaluateInitialConditions() {
        getPosition();

        disposeThread.start();
        sleep(100);
        inputValidationThread.start();
        sleep(100);
        getInitialValuesThread.start();
        sleep(100);

        // set to relative positioning
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G91"));
        sleep(100);

        // enable debug mode
        machine.getDriver().dispatchCommand("M1110 S1", COM.DEFAULT);
        sleep(100);

        setPollDataTrue = new Timer(0, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (movButtonHoldDown.isRunning() == false) {
                    canPollData = true;
                }
            }
        });
        setPollDataTrue.setInitialDelay(200);
        setPollDataTrue.setRepeats(false);

        showBeepLabel = new Timer(0, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                labelBeepValidation.setVisible(false);
            }
        });
        showBeepLabel.setInitialDelay(2000);
        showBeepLabel.setRepeats(false);

        categories2 = fullFillComboDuration();
        comboModel2 = new DefaultComboBoxModel(categories2);

        extrudeCombo.setModel(comboModel2);
        extrudeCombo.setSelectedIndex(0);

    }

    protected String mSpeedGetText() {
        return mSpeed.getText();
    }

    protected void mSpeedSetText(String val) {
        mSpeed.setText(val);
    }

    protected void textFieldLastPrintTimeSetText(String val) {
        textFieldLastPrintTime.setText(val);
    }

    protected double getTargetTemperature() {
        try {
            return Double.valueOf(targetTemperatureVal.getText());
        } catch (IllegalArgumentException ex) {
            return -1;
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    protected void updateTemperature() {
        double extruderTemp, blockTemp;
        String extruderTempString, blockTempString;

        extruderTemp = machine.getModel().currentTool().getExtruderTemperature();
        blockTemp = machine.getModel().currentTool().getBlockTemperature();
        extruderTempString = String.valueOf(extruderTemp);
        blockTempString = String.valueOf(blockTemp);
        extruderTemperatureVal.setText(extruderTempString);
        blockTemperatureVal.setText(blockTempString);

        if (loggingTemperature) {
            try {
                bw.write(extruderTempString + "\t");
                bw.write(blockTempString);
                bw.newLine();
                bw.flush();
            } catch (IOException ex) {
                Base.writeLog("Can't write temperature to file", this.getClass());
            }
        }

        //Graph variables
        Second second = new Second(new Date(System.currentTimeMillis() - startMillis));

        t0MeasuredDataset.add(second, extruderTemp, "a");
        t0TargetDataset.add(second, temperatureGoal, "a");
    }

    protected void updatePosition(Point5d pos) {
        //Point5d pos;

        //pos = machine.getDriver().getCurrentPosition(false);
        Base.writeLog("Setting coordinates", this.getClass());
        Base.writeLog("X: " + pos.x(), this.getClass());
        Base.writeLog("Y: " + pos.y(), this.getClass());
        Base.writeLog("Z: " + pos.z(), this.getClass());
        xTextFieldValue.setText(String.format(Locale.US, "%3.3f", pos.x()));
        yTextFieldValue.setText(String.format(Locale.US, "%3.3f", pos.y()));
        zTextFieldValue.setText(String.format(Locale.US, "%3.3f", pos.z()));
        canMove = true;
    }

    private void initFile() {
        this.file = new File(Base.getAppDataDirectory() + "/" + REDMEC_TAG + getDate() + ".txt");
        try {
            this.fw = new FileWriter(file.getAbsoluteFile());
        } catch (IOException ex) {
            Base.writeLog("Can't create file to log temperature", this.getClass());
        }
        this.bw = new BufferedWriter(fw);
    }

    private void cleanLogFiles() {
        File logsDir = new File(Base.getAppDataDirectory().getAbsolutePath());
        File[] logsList = logsDir.listFiles();

        for (File logsList1 : logsList) {
            if (loggingTemperature == false) {
                if (logsList1.getName().contains(REDMEC_TAG) && logsList1.length() == 0) {
                    logsList1.delete();
                }
            } // no need for else
        }
    }

    private void doCancel() {
        Base.getMainWindow().getButtons().updatePressedStateButton("quick_guide");
        Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
        if (loggingTemperature == false) {
            disposeThread.cancel();
            cleanLogFiles();

            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        inputValidationThread.cancel();
        getInitialValuesThread.cancel();
        Base.getMainWindow().setEnabled(true);

        machine.runCommand(new replicatorg.drivers.commands.SendHome());
        dispose();
        Base.bringAllWindowsToFront();
    }

    private void startMovementTimer(MovDir whereTo) {
        ActionListener listener;

        if (canMove == false) {
            Base.writeLog("*** Can't move at the moment ***", this.getClass());
            return;
        }

        setPollDataTrue.stop();

        movButtonHoldDown = new Timer(movCommandInterval, null);
        movButtonHoldDown.setRepeats(true);
        canPollData = false;

        switch (whereTo) {
            case Z_PLUS:
                listener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        double val;

                        machine.getDriver().dispatchCommand("G0 Z" + movCommandStep, COM.DEFAULT);
                        val = Double.parseDouble(zTextFieldValue.getText()) + movCommandStep;
                        zTextFieldValue.setText(String.format(Locale.US, "%3.3f", val));
                    }
                };
                movButtonHoldDown.addActionListener(listener);
                movButtonHoldDown.start();
                break;
            case Z_MINUS:
                listener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        double val;

                        machine.getDriver().dispatchCommand("G0 Z-" + movCommandStep, COM.DEFAULT);
                        val = Double.parseDouble(zTextFieldValue.getText()) - movCommandStep;
                        zTextFieldValue.setText(String.format(Locale.US, "%3.3f", val));;
                    }
                };
                movButtonHoldDown.addActionListener(listener);
                movButtonHoldDown.start();
                break;
            case Y_PLUS:
                listener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        double val;

                        machine.getDriver().dispatchCommand("G0 Y" + movCommandStep, COM.DEFAULT);
                        val = Double.parseDouble(yTextFieldValue.getText()) + movCommandStep;
                        yTextFieldValue.setText(String.format(Locale.US, "%3.3f", val));
                    }
                };
                movButtonHoldDown.addActionListener(listener);
                movButtonHoldDown.start();
                break;
            case Y_MINUS:
                listener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        double val;

                        machine.getDriver().dispatchCommand("G0 Y-" + movCommandStep, COM.DEFAULT);
                        val = Double.parseDouble(yTextFieldValue.getText()) - movCommandStep;
                        yTextFieldValue.setText(String.format(Locale.US, "%3.3f", val));
                    }
                };
                movButtonHoldDown.addActionListener(listener);
                movButtonHoldDown.start();
                break;
            case X_PLUS:
                listener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        double val;

                        machine.getDriver().dispatchCommand("G0 X" + movCommandStep, COM.DEFAULT);
                        val = Double.parseDouble(xTextFieldValue.getText()) + movCommandStep;
                        xTextFieldValue.setText(String.format(Locale.US, "%3.3f", val));
                    }
                };
                movButtonHoldDown.addActionListener(listener);
                movButtonHoldDown.start();
                break;
            case X_MINUS:
                listener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        double val;

                        machine.getDriver().dispatchCommand("G0 X-" + movCommandStep, COM.DEFAULT);
                        val = Double.parseDouble(xTextFieldValue.getText()) - movCommandStep;
                        xTextFieldValue.setText(String.format(Locale.US, "%3.3f", val));
                    }
                };
                movButtonHoldDown.addActionListener(listener);
                movButtonHoldDown.start();
                break;
            default:
                break;
        }

    }

    private void getPosition() {
        String ans, xValStr, yValStr, zValStr;
        String[] parts;
        int tries = 3;
        double xVal = -1, yVal = -1, zVal = -1;

        do {
            if (tries-- < 0) {
                Base.writeLog("Failed obtaining valid position!", this.getClass());
                return;
            }

            ans = machine.getDriver().dispatchCommand("M121");
            parts = ans.split(" ");
        } while (parts.length < 4 || !ans.contains("X:"));

        Base.writeLog("Obtained valid position!", this.getClass());

        try {

            for (String part : parts) {

                if (part.contains("X:")) {
                    xValStr = part.substring(2);
                    xVal = Double.parseDouble(xValStr);

                } else if (part.contains("Y:")) {
                    yValStr = part.substring(2);
                    yVal = Double.parseDouble(yValStr);

                } else if (part.contains("Z:")) {
                    zValStr = part.substring(2);
                    zVal = Double.parseDouble(zValStr);

                    if (zHome == -1) {
                        zHome = zVal;
                    }
                }
            }
            updatePosition(new Point5d(xVal, yVal, zVal));

        } catch (NumberFormatException ex) {

        }

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        bHomeXY = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        zTextFieldValue = new javax.swing.JTextField();
        xTextFieldValue = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        xRIGHT = new javax.swing.JLabel();
        yDOWN = new javax.swing.JLabel();
        yUP = new javax.swing.JLabel();
        zUP = new javax.swing.JLabel();
        yTextFieldValue = new javax.swing.JTextField();
        xLEFT = new javax.swing.JLabel();
        zDOWN = new javax.swing.JLabel();
        bCurrentPosition = new javax.swing.JButton();
        bHomeZ = new javax.swing.JLabel();
        xTextFieldGoal = new javax.swing.JTextField();
        yTextFieldGoal = new javax.swing.JTextField();
        zTextFieldGoal = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        bBeep = new javax.swing.JButton();
        bTurnOffLEDs = new javax.swing.JButton();
        labelBeepValidation = new javax.swing.JLabel();
        bTurnOnLEDs = new javax.swing.JButton();
        labelLastPrintTime = new javax.swing.JLabel();
        textFieldLastPrintTime = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        notes = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        blockTemperatureLabel = new javax.swing.JLabel();
        targetTemperatureVal = new javax.swing.JTextField();
        extruderTemperatureLabel = new javax.swing.JLabel();
        cLogTemperature = new javax.swing.JCheckBox();
        logTemperature = new javax.swing.JLabel();
        blockTemperatureVal = new javax.swing.JTextField();
        extruderTemperatureVal = new javax.swing.JTextField();
        tempPanel = new javax.swing.JPanel();
        tempLabel = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        colorTargetTemp = new javax.swing.JLabel();
        colorCurrentTemp = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jBlowerFanValue = new javax.swing.JTextField();
        jBlowerFanTitle = new javax.swing.JLabel();
        jSliderBlowerSpeed = new javax.swing.JSlider();
        jSliderExtruderSpeed = new javax.swing.JSlider();
        jExtruderFanValue = new javax.swing.JTextField();
        jExtruderFanTitle = new javax.swing.JLabel();
        mSpeed = new javax.swing.JTextField();
        extrudeDuration = new javax.swing.JLabel();
        bForward = new javax.swing.JButton();
        extrudeCombo = new javax.swing.JComboBox();
        motorSpeed = new javax.swing.JLabel();
        bReverse = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        bOK = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));
        jPanel1.setToolTipText("");
        jPanel1.setPreferredSize(new java.awt.Dimension(1063, 791));

        jPanel2.setBackground(new java.awt.Color(248, 248, 248));
        jPanel2.setToolTipText("");
        jPanel2.setPreferredSize(new java.awt.Dimension(499, 620));

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Movement", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, GraphicDesignComponents.getSSProBold("12")));
        jPanel8.setOpaque(false);

        bHomeXY.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/home2.png"))); // NOI18N
        bHomeXY.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                bHomeXYMouseReleased(evt);
            }
        });

        jLabel2.setText("X");

        zTextFieldValue.setEditable(false);
        zTextFieldValue.setText("0.0");

        xTextFieldValue.setEditable(false);
        xTextFieldValue.setText("0.0");

        jLabel4.setText("Z");

        jLabel3.setText("Y");

        xRIGHT.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/X+.png"))); // NOI18N
        xRIGHT.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                xRIGHTMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                xRIGHTMouseReleased(evt);
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
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                yDOWNMouseReleased(evt);
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
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                yUPMouseReleased(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                yUPMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                yUPMouseEntered(evt);
            }
        });

        zUP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/Z+.png"))); // NOI18N
        zUP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                zUPMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                zUPMouseReleased(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                zUPMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                zUPMouseEntered(evt);
            }
        });

        yTextFieldValue.setEditable(false);
        yTextFieldValue.setText("0.0");

        xLEFT.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/X-.png"))); // NOI18N
        xLEFT.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                xLEFTMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                xLEFTMouseReleased(evt);
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
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                zDOWNMouseReleased(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                zDOWNMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                zDOWNMouseEntered(evt);
            }
        });

        bCurrentPosition.setText("Calibration set 0");
        bCurrentPosition.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                bCurrentPositionMouseReleased(evt);
            }
        });

        bHomeZ.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/home2.png"))); // NOI18N
        bHomeZ.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                bHomeZMouseReleased(evt);
            }
        });

        xTextFieldGoal.setText("0.0");
        xTextFieldGoal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                xTextFieldGoalKeyPressed(evt);
            }
        });

        yTextFieldGoal.setText("0.0");
        yTextFieldGoal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                yTextFieldGoalKeyPressed(evt);
            }
        });

        zTextFieldGoal.setText("0.0");
        zTextFieldGoal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                zTextFieldGoalKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(bCurrentPosition, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(yDOWN)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(xLEFT)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bHomeXY)
                                .addGap(6, 6, 6))
                            .addComponent(yUP))
                        .addGap(0, 0, 0)
                        .addComponent(xRIGHT)
                        .addGap(30, 30, 30)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(zDOWN)
                            .addComponent(zUP)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(bHomeZ)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 69, Short.MAX_VALUE)
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
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(yTextFieldValue, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                            .addComponent(xTextFieldValue, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(zTextFieldValue))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(xTextFieldGoal, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yTextFieldGoal, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(zTextFieldGoal, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(33, 33, 33))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(yUP)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(xLEFT)
                            .addComponent(xRIGHT)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bHomeXY)))
                        .addComponent(yDOWN))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(xTextFieldValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(xTextFieldGoal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3)
                            .addComponent(yTextFieldGoal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yTextFieldValue))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(zTextFieldValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(zTextFieldGoal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(zUP)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bHomeZ)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zDOWN)))
                .addGap(3, 3, 3)
                .addComponent(bCurrentPosition)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Other", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, GraphicDesignComponents.getSSProBold("12")));
        jPanel7.setToolTipText("");
        jPanel7.setOpaque(false);

        bBeep.setText("Beep");
        bBeep.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bBeepMousePressed(evt);
            }
        });

        bTurnOffLEDs.setText("Turn off LEDs");
        bTurnOffLEDs.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                bTurnOffLEDsMouseReleased(evt);
            }
        });

        labelBeepValidation.setVisible(false);
        labelBeepValidation.setForeground(new java.awt.Color(28, 181, 28));
        labelBeepValidation.setText("OK");

        bTurnOnLEDs.setText("Turn on LEDs");
        bTurnOnLEDs.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                bTurnOnLEDsMouseReleased(evt);
            }
        });

        labelLastPrintTime.setText("Last Print time");

        textFieldLastPrintTime.setEditable(false);
        textFieldLastPrintTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        textFieldLastPrintTime.setPreferredSize(new java.awt.Dimension(74, 27));

        jLabel6.setText("(hh:mm:ss)");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(bTurnOnLEDs, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelBeepValidation, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(bTurnOffLEDs, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textFieldLastPrintTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(labelLastPrintTime)
                    .addComponent(bBeep, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(bTurnOnLEDs)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bTurnOffLEDs))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bBeep)
                            .addComponent(labelBeepValidation))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelLastPrintTime)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel6)
                        .addGap(1, 1, 1)
                        .addComponent(textFieldLastPrintTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(12, 12, 12))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(248, 248, 248));

        notes.setText("All files saved under BEESOFT folder in user directory.");
        notes.setToolTipText("");

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Extrusion", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, GraphicDesignComponents.getSSProBold("12")));
        jPanel9.setOpaque(false);
        jPanel9.setPreferredSize(new java.awt.Dimension(471, 591));

        jPanel5.setOpaque(false);

        blockTemperatureLabel.setText("Block temperature");

        targetTemperatureVal.setText("0");
        targetTemperatureVal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                targetTemperatureValKeyPressed(evt);
            }
        });

        extruderTemperatureLabel.setText("Extruder temperature");

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

        jPanel6.setOpaque(false);

        jLabel1.setLabelFor(colorCurrentTemp);
        jLabel1.setText("Current extruder temperature");

        colorTargetTemp.setBackground(new java.awt.Color(204, 204, 204));
        colorTargetTemp.setText("color1");
        colorTargetTemp.setMaximumSize(new java.awt.Dimension(10, 10));
        colorTargetTemp.setMinimumSize(new java.awt.Dimension(10, 10));
        colorTargetTemp.setPreferredSize(new java.awt.Dimension(10, 10));

        colorCurrentTemp.setBackground(new java.awt.Color(204, 204, 204));
        colorCurrentTemp.setText("color1");
        colorCurrentTemp.setMaximumSize(new java.awt.Dimension(10, 10));
        colorCurrentTemp.setMinimumSize(new java.awt.Dimension(10, 10));
        colorCurrentTemp.setPreferredSize(new java.awt.Dimension(10, 10));

        jLabel5.setLabelFor(colorTargetTemp);
        jLabel5.setText("Target extruder temperature");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(colorTargetTemp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39)
                        .addComponent(jLabel5))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(colorCurrentTemp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39)
                        .addComponent(jLabel1)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(colorCurrentTemp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(colorTargetTemp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5))
                .addContainerGap())
        );

        jBlowerFanValue.setEditable(false);
        jBlowerFanValue.setText("0");

        jBlowerFanTitle.setText("Blower fan");

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

        jExtruderFanValue.setEditable(false);
        jExtruderFanValue.setText("0");

        jExtruderFanTitle.setText("Extruder fan");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(logTemperature, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cLogTemperature))
                            .addComponent(blockTemperatureLabel)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(extruderTemperatureLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(blockTemperatureVal, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                                    .addComponent(extruderTemperatureVal))
                                .addGap(6, 6, 6)
                                .addComponent(targetTemperatureVal, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(tempLabel)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tempPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(40, 40, 40)
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(53, 53, 53))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                                    .addComponent(jBlowerFanTitle)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jBlowerFanValue, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jSliderBlowerSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                            .addComponent(jSliderExtruderSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addGap(226, 226, 226)
                            .addComponent(jExtruderFanTitle)
                            .addGap(18, 18, 18)
                            .addComponent(jExtruderFanValue, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extruderTemperatureLabel)
                    .addComponent(extruderTemperatureVal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(targetTemperatureVal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(blockTemperatureLabel)
                    .addComponent(blockTemperatureVal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cLogTemperature)
                    .addComponent(logTemperature))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tempLabel)
                .addGap(1, 1, 1)
                .addComponent(tempPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jBlowerFanTitle)
                        .addComponent(jBlowerFanValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jExtruderFanValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jExtruderFanTitle)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSliderBlowerSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSliderExtruderSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        mSpeed.setText("500");
        mSpeed.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                mSpeedKeyReleased(evt);
            }
        });

        extrudeDuration.setText("Extrude duration");

        bForward.setText("Foward");
        bForward.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                bForwardMouseReleased(evt);
            }
        });

        extrudeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        motorSpeed.setText("Speed");

        bReverse.setText("Reverse");
        bReverse.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                bReverseMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel9Layout.createSequentialGroup()
                                .addComponent(extrudeDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(extrudeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel9Layout.createSequentialGroup()
                                .addComponent(motorSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(bForward, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bReverse, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(motorSpeed)
                    .addComponent(bForward))
                .addGap(17, 17, 17)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extrudeDuration)
                    .addComponent(extrudeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bReverse)))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(notes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(notes)
                .addContainerGap())
        );

        jPanel4.setBackground(new java.awt.Color(255, 203, 5));
        jPanel4.setMinimumSize(new java.awt.Dimension(20, 38));
        jPanel4.setPreferredSize(new java.awt.Dimension(567, 38));

        bOK.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bOK)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(bOK)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 994, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 994, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 658, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            cleanLogFiles();

        } else {
            this.loggingTemperature = true;
            initFile();
            cleanLogFiles();
        }


    }//GEN-LAST:event_cLogTemperatureActionPerformed

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

    private void zDOWNMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zDOWNMouseEntered
        zDOWN.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "Z-Over.png")));
    }//GEN-LAST:event_zDOWNMouseEntered

    private void zDOWNMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zDOWNMouseExited
        zDOWN.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "Z-.png")));
    }//GEN-LAST:event_zDOWNMouseExited

    private void zDOWNMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zDOWNMouseReleased
        movButtonHoldDown.stop();
        if (setPollDataTrue.isRunning() == false) {
            setPollDataTrue.start();
        }
    }//GEN-LAST:event_zDOWNMouseReleased

    private void zDOWNMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zDOWNMousePressed
        startMovementTimer(MovDir.Z_PLUS);
    }//GEN-LAST:event_zDOWNMousePressed

    private void xLEFTMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xLEFTMouseEntered
        xLEFT.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "X-Over.png")));
    }//GEN-LAST:event_xLEFTMouseEntered

    private void xLEFTMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xLEFTMouseExited
        xLEFT.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "X-.png")));
    }//GEN-LAST:event_xLEFTMouseExited

    private void xLEFTMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xLEFTMouseReleased
        movButtonHoldDown.stop();
        if (setPollDataTrue.isRunning() == false) {
            setPollDataTrue.start();
        }
    }//GEN-LAST:event_xLEFTMouseReleased

    private void xLEFTMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xLEFTMousePressed
        startMovementTimer(MovDir.X_MINUS);
    }//GEN-LAST:event_xLEFTMousePressed

    private void zUPMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zUPMouseEntered
        zUP.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "Z+Over.png")));
    }//GEN-LAST:event_zUPMouseEntered

    private void zUPMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zUPMouseExited
        zUP.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "Z+.png")));
    }//GEN-LAST:event_zUPMouseExited

    private void zUPMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zUPMouseReleased
        movButtonHoldDown.stop();
        if (setPollDataTrue.isRunning() == false) {
            setPollDataTrue.start();
        }
    }//GEN-LAST:event_zUPMouseReleased

    private void zUPMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zUPMousePressed
        startMovementTimer(MovDir.Z_MINUS);
    }//GEN-LAST:event_zUPMousePressed

    private void yUPMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yUPMouseEntered
        yUP.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "Y+Over.png")));
    }//GEN-LAST:event_yUPMouseEntered

    private void yUPMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yUPMouseExited
        yUP.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "Y+.png")));
    }//GEN-LAST:event_yUPMouseExited

    private void yUPMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yUPMouseReleased
        movButtonHoldDown.stop();
        if (setPollDataTrue.isRunning() == false) {
            setPollDataTrue.start();
        }
    }//GEN-LAST:event_yUPMouseReleased

    private void yUPMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yUPMousePressed
        startMovementTimer(MovDir.Y_MINUS);
    }//GEN-LAST:event_yUPMousePressed

    private void yDOWNMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yDOWNMouseEntered
        yDOWN.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "Y-Over.png")));
    }//GEN-LAST:event_yDOWNMouseEntered

    private void yDOWNMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yDOWNMouseExited
        yDOWN.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "Y-.png")));
    }//GEN-LAST:event_yDOWNMouseExited

    private void yDOWNMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yDOWNMouseReleased
        movButtonHoldDown.stop();
        if (setPollDataTrue.isRunning() == false) {
            setPollDataTrue.start();
        }
    }//GEN-LAST:event_yDOWNMouseReleased

    private void yDOWNMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yDOWNMousePressed
        startMovementTimer(MovDir.Y_PLUS);
    }//GEN-LAST:event_yDOWNMousePressed

    private void xRIGHTMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xRIGHTMouseEntered
        xRIGHT.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "X+Over.png")));
    }//GEN-LAST:event_xRIGHTMouseEntered

    private void xRIGHTMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xRIGHTMouseExited
        xRIGHT.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "X+.png")));
    }//GEN-LAST:event_xRIGHTMouseExited

    private void xRIGHTMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xRIGHTMouseReleased
        movButtonHoldDown.stop();
        if (setPollDataTrue.isRunning() == false) {
            setPollDataTrue.start();
        }
    }//GEN-LAST:event_xRIGHTMouseReleased

    private void xRIGHTMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xRIGHTMousePressed
        startMovementTimer(MovDir.X_PLUS);
    }//GEN-LAST:event_xRIGHTMousePressed

    private void bTurnOnLEDsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bTurnOnLEDsMouseReleased
        machine.getDriver().dispatchCommand("M5", COM.DEFAULT);
    }//GEN-LAST:event_bTurnOnLEDsMouseReleased

    private void bTurnOffLEDsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bTurnOffLEDsMouseReleased
        machine.getDriver().dispatchCommand("M6", COM.DEFAULT);
    }//GEN-LAST:event_bTurnOffLEDsMouseReleased

    private void bBeepMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bBeepMousePressed
        String answer;

        answer = machine.getDriver().dispatchCommand("M300", COM.DEFAULT);

        if (answer.contains("ok")) {
            labelBeepValidation.setVisible(true);
            showBeepLabel.start();
        }
    }//GEN-LAST:event_bBeepMousePressed

    private void jSliderExtruderSpeedMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jSliderExtruderSpeedMouseReleased
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M126 S" + jSliderExtruderSpeed.getValue(), COM.DEFAULT));
        jExtruderFanValue.setText(String.valueOf(jSliderExtruderSpeed.getValue()));
    }//GEN-LAST:event_jSliderExtruderSpeedMouseReleased

    private void jSliderBlowerSpeedMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jSliderBlowerSpeedMouseReleased
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M106 S" + jSliderBlowerSpeed.getValue(), COM.DEFAULT));
        jBlowerFanValue.setText(String.valueOf(jSliderBlowerSpeed.getValue()));
    }//GEN-LAST:event_jSliderBlowerSpeedMouseReleased

    private void bHomeZMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bHomeZMouseReleased
        machine.getDriver().dispatchCommand("G28 Z");
        getPosition();
        machine.getDriver().dispatchCommand("G91");
    }//GEN-LAST:event_bHomeZMouseReleased

    private void bHomeXYMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bHomeXYMouseReleased
        machine.getDriver().dispatchCommand("G28 XY");
        getPosition();
        machine.getDriver().dispatchCommand("G91");
    }//GEN-LAST:event_bHomeXYMouseReleased

    private void bCurrentPositionMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCurrentPositionMouseReleased
        machine.getDriver().dispatchCommand("M603");
        zHome -= Double.parseDouble(zTextFieldValue.getText());
        zTextFieldValue.setText(String.format(Locale.US, "%3.3f", 0.0));
    }//GEN-LAST:event_bCurrentPositionMouseReleased

    private void zTextFieldGoalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_zTextFieldGoalKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                double xVal, yVal, zVal;

                xVal = Double.parseDouble(xTextFieldValue.getText());
                yVal = Double.parseDouble(yTextFieldValue.getText());
                zVal = Double.parseDouble(zTextFieldGoal.getText());

                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G90"));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G0 X" + xVal + " Y" + yVal + " Z" + zVal + " F5000"));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G91"));

                zTextFieldValue.setText(String.format(Locale.US, "%3.3f", zVal));
            } catch (NumberFormatException ex) {
                Base.writeLog("Invalid input on zTextFieldGoal", this.getClass());
            }
        }
    }//GEN-LAST:event_zTextFieldGoalKeyPressed

    private void xTextFieldGoalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_xTextFieldGoalKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            try {

                double xVal, yVal, zVal;

                xVal = Double.parseDouble(xTextFieldGoal.getText());
                yVal = Double.parseDouble(yTextFieldValue.getText());
                zVal = Double.parseDouble(zTextFieldValue.getText());

                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G90"));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G0 X" + xVal + " Y" + yVal + " Z" + zVal + " F5000"));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G91"));

                xTextFieldValue.setText(String.format(Locale.US, "%3.3f", xVal));
            } catch (NumberFormatException ex) {
                Base.writeLog("Invalid input on xTextFieldGoal", this.getClass());
            }
        }
    }//GEN-LAST:event_xTextFieldGoalKeyPressed

    private void yTextFieldGoalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_yTextFieldGoalKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                double xVal, yVal, zVal;

                xVal = Double.parseDouble(xTextFieldValue.getText());
                yVal = Double.parseDouble(yTextFieldGoal.getText());
                zVal = Double.parseDouble(zTextFieldValue.getText());

                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G90"));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G0 X" + xVal + " Y" + yVal + " Z" + zVal + " F5000"));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G91"));

                yTextFieldValue.setText(String.format(Locale.US, "%3.3f", yVal));
            } catch (NumberFormatException ex) {
                Base.writeLog("Invalid input on yTextFieldGoal", this.getClass());
            }
        }
    }//GEN-LAST:event_yTextFieldGoalKeyPressed

    private void targetTemperatureValKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_targetTemperatureValKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            double targetTemperature;

            targetTemperature = getTargetTemperature();

            if (targetTemperature != temperatureGoal) {
                if (targetTemperature != -1) {
                    machine.runCommand(new replicatorg.drivers.commands.SetTemperature(targetTemperature));
                    temperatureGoal = targetTemperature;
                } else {
                    targetTemperatureVal.setText(String.valueOf(temperatureGoal));
                }
            }
        }
    }//GEN-LAST:event_targetTemperatureValKeyPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBeep;
    private javax.swing.JButton bCurrentPosition;
    private javax.swing.JButton bForward;
    private javax.swing.JLabel bHomeXY;
    private javax.swing.JLabel bHomeZ;
    private javax.swing.JLabel bOK;
    private javax.swing.JButton bReverse;
    private javax.swing.JButton bTurnOffLEDs;
    private javax.swing.JButton bTurnOnLEDs;
    private javax.swing.JLabel blockTemperatureLabel;
    private javax.swing.JTextField blockTemperatureVal;
    private javax.swing.JCheckBox cLogTemperature;
    private javax.swing.JLabel colorCurrentTemp;
    private javax.swing.JLabel colorTargetTemp;
    private javax.swing.JComboBox extrudeCombo;
    private javax.swing.JLabel extrudeDuration;
    private javax.swing.JLabel extruderTemperatureLabel;
    private javax.swing.JTextField extruderTemperatureVal;
    private javax.swing.JLabel jBlowerFanTitle;
    private javax.swing.JTextField jBlowerFanValue;
    private javax.swing.JLabel jExtruderFanTitle;
    private javax.swing.JTextField jExtruderFanValue;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JSlider jSliderBlowerSpeed;
    private javax.swing.JSlider jSliderExtruderSpeed;
    private javax.swing.JLabel labelBeepValidation;
    private javax.swing.JLabel labelLastPrintTime;
    private javax.swing.JLabel logTemperature;
    private javax.swing.JTextField mSpeed;
    private javax.swing.JLabel motorSpeed;
    private javax.swing.JLabel notes;
    private javax.swing.JTextField targetTemperatureVal;
    private javax.swing.JLabel tempLabel;
    private javax.swing.JPanel tempPanel;
    private javax.swing.JTextField textFieldLastPrintTime;
    private javax.swing.JLabel xLEFT;
    private javax.swing.JLabel xRIGHT;
    private javax.swing.JTextField xTextFieldGoal;
    private javax.swing.JTextField xTextFieldValue;
    private javax.swing.JLabel yDOWN;
    private javax.swing.JTextField yTextFieldGoal;
    private javax.swing.JTextField yTextFieldValue;
    private javax.swing.JLabel yUP;
    private javax.swing.JLabel zDOWN;
    private javax.swing.JTextField zTextFieldGoal;
    private javax.swing.JTextField zTextFieldValue;
    private javax.swing.JLabel zUP;
    // End of variables declaration//GEN-END:variables

}

class TemperatureThread extends Thread {

    private final MachineInterface machine;
    private final ControlPanel controlPanel;
    private boolean stop = false;

    public TemperatureThread(ControlPanel cPanel, MachineInterface mach) {
        super("Temperature Thread");
        this.machine = mach;
        this.controlPanel = cPanel;
    }

    @Override
    public void run() {

        while (stop == false) {
            if (controlPanel.canPollData && machine.getDriver().isTransferMode() == false) {

                if(Base.isPrinting == false) {
                    machine.runCommand(new replicatorg.drivers.commands.ReadTemperature());
                }
                controlPanel.updateTemperature();

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TemperatureThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void cancel() {
        stop = true;
    }
}

class InputValidationThread extends Thread {

    private final ControlPanel controlPanel;
    private boolean stop = false;

    public InputValidationThread(ControlPanel controlPanel) {
        this.controlPanel = controlPanel;
    }

    @Override
    public void run() {
        long currentTime;
        double val;
        boolean changed;

        while (stop == false) {
            changed = false;
            currentTime = System.nanoTime();

            // 1/2 sec
            if (currentTime - controlPanel.mSpeedLastClicked
                    > 500000000) {
                try {
                    val = Double.parseDouble(controlPanel.mSpeedGetText());

                    if (val < 0) {
                        val = -val;
                        changed = true;
                    }

                    if (val > 2000) {
                        val = 2000;
                        changed = true;
                    }

                    if (changed) {
                        controlPanel.mSpeedSetText(String.valueOf(val));
                    }

                } catch (IllegalArgumentException ex) {

                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                break;
            }
        }
    }

    public void cancel() {
        stop = true;
    }
}

class GetInitialValuesThread extends Thread {

    private final ControlPanel controlPanel;
    private final MachineInterface machine;
    private boolean stop = false;

    public GetInitialValuesThread(ControlPanel controlPanel, MachineInterface machine) {
        this.controlPanel = controlPanel;
        this.machine = machine;
    }

    @Override
    public void run() {
        String answer;
        int beginIndex, endIndex;
        long ms, hours, mins, secs;

        while (stop == false) {
            answer = machine.getDriver().dispatchCommand("M1002", COM.DEFAULT);
            beginIndex = answer.lastIndexOf("Time: ");
            endIndex = answer.lastIndexOf("\nok");

            if (beginIndex > -1 && endIndex > -1) {
                beginIndex += 6;
                answer = answer.substring(beginIndex, endIndex);
                try {
                    ms = Long.parseLong(answer);

                    hours = TimeUnit.MILLISECONDS.toHours(ms);
                    ms -= TimeUnit.HOURS.toMillis(hours);
                    mins = TimeUnit.MILLISECONDS.toMinutes(ms);
                    ms -= TimeUnit.MINUTES.toMillis(mins);
                    secs = TimeUnit.MILLISECONDS.toSeconds(ms);

                    answer = String.format("%02d:%02d:%02d", hours, mins, secs);
                    controlPanel.textFieldLastPrintTimeSetText(answer);
                    break;
                } catch (IllegalArgumentException ex) {
                    answer = "-1";
                    controlPanel.textFieldLastPrintTimeSetText(answer);
                }

            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                break;
            }
        }
    }

    public void cancel() {
        stop = true;
    }
}
