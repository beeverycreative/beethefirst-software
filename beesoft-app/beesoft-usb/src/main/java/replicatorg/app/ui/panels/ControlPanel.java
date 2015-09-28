package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
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

    private TimeTableXYDataset t0MeasuredDataset = new TimeTableXYDataset();
	private TimeTableXYDataset t0TargetDataset = new TimeTableXYDataset();
	private TimeTableXYDataset t1MeasuredDataset = new TimeTableXYDataset();
	private TimeTableXYDataset t1TargetDataset = new TimeTableXYDataset();
	private TimeTableXYDataset pMeasuredDataset = new TimeTableXYDataset();
	private TimeTableXYDataset pTargetDataset = new TimeTableXYDataset();
    
    final private static Color t0TargetColor = Color.MAGENTA;
	final private static Color t0MeasuredColor = Color.RED;
	final private static Color t1TargetColor = Color.CYAN;
	final private static Color t1MeasuredColor = Color.BLUE;
	final private static Color pTargetColor = Color.YELLOW;
	final private static Color pMeasuredColor = Color.GREEN;
        
    long startMillis = System.currentTimeMillis();
    
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
        BufferedImage image = new BufferedImage(10,10,BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		g.setColor(t0MeasuredColor);
		g.fillRect(0,0,10,10);
		//image.getGraphics().fillRect(0,0,10,10);
		Icon icon1 = new ImageIcon(image);
        
        this.colorCurrentTemp.setIcon(icon1);
        this.colorCurrentTemp.setText("");
        
        BufferedImage image2 = new BufferedImage(10,10,BufferedImage.TYPE_INT_RGB);
		Graphics g2 = image2.getGraphics();
        g2.setColor(t0TargetColor);
		g2.fillRect(0,0,10,10);        
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
        bHome.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bSetCalibration.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bCurrentPosition.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bCurrentPosition.setFont(GraphicDesignComponents.getSSProRegular("14"));
        targetTemperature.setFont(GraphicDesignComponents.getSSProRegular("14"));
        currentTemperature.setFont(GraphicDesignComponents.getSSProRegular("14"));
        motorSpeed.setFont(GraphicDesignComponents.getSSProRegular("14"));
        extrudeDuration.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bReverse.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bFoward.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bStop.setFont(GraphicDesignComponents.getSSProRegular("14"));
        motorControl.setFont(GraphicDesignComponents.getSSProBold("16"));
        feedRate.setFont(GraphicDesignComponents.getSSProBold("16"));
        coolFan.setFont(GraphicDesignComponents.getSSProRegular("14"));
        logTemperature.setFont(GraphicDesignComponents.getSSProRegular("14"));
        notes.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bCancel.setFont(GraphicDesignComponents.getSSProRegular("12"));
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
        targetTemperature.setText(Languager.getTagValue(1, "ControlPanel", "Target_Temperature"));
        currentTemperature.setText(Languager.getTagValue(1, "ControlPanel", "Current_Temperature"));
        motorSpeed.setText(Languager.getTagValue(1, "ControlPanel", "Motor_Speed"));
        extrudeDuration.setText(Languager.getTagValue(1, "ControlPanel", "Extrude_Duration"));
        bReverse.setText(Languager.getTagValue(1, "ControlPanel", "Reverse"));
        bStop.setText(Languager.getTagValue(1, "ControlPanel", "Stop"));
        bFoward.setText(Languager.getTagValue(1, "ControlPanel", "Foward"));
        motorControl.setText(Languager.getTagValue(1, "ControlPanel", "Motor_Control"));
        coolFan.setText(Languager.getTagValue(1, "ControlPanel", "Cooling_Fan"));
        logTemperature.setText(Languager.getTagValue(1, "ControlPanel", "Log_Temperature"));
        notes.setText(Languager.getTagValue(1, "BaseDirectories", "Line9"));
        xyFeedrate.setText(Languager.getTagValue(1, "ControlPanel", "XY_Feedrate"));
        zFeedrate.setText(Languager.getTagValue(1, "ControlPanel", "Z_Feedrate"));
        bCancel.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line3"));
        bOK.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line6"));
    }

    private void evaluateInitialConditions() {
        machine = machine = Base.getMachineLoader().getMachineInterface();
        categories = fullFillCombo();
        comboModel = new DefaultComboBoxModel(categories);
        categories2 = fullFillComboDuration();
        comboModel2 = new DefaultComboBoxModel(categories2);

        extrudeCombo.setModel(comboModel2);
        extrudeCombo.setSelectedIndex(0);
        XYFeedrate = 2000;
        ZFeedrate = 2000;
        temperatureGoal = Double.valueOf(tTargetTemperature.getText());
        coolFanPressed = false;
        loggingTemperature = false;
        freeJogging = false;
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
		axis.setFixedAutoRange(3L*60L*1000L); // auto range to three minutes
        
		TickUnits unitSource = new TickUnits();
		unitSource.add(new NumberTickUnit(60L*1000L)); // minutes
		unitSource.add(new NumberTickUnit(1L*1000L)); // seconds
        
		axis.setStandardTickUnits(unitSource);
		axis.setTickLabelsVisible(false); // We don't need to see the millisecond count
		axis = plot.getRangeAxis();
		axis.setRange(0,300); // set termperature range from 0 to 300 degrees C so you can see overshoots 
        
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
		chartPanel.setPreferredSize(new Dimension(400,160));
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
            "5",
            "10",
            "15",
            "20",
            "25",
            "30",};

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
        double eDuration = Double.valueOf(extrudeCombo.getSelectedItem().toString());
        double eSpeed = Double.valueOf(mSpeed.getText());

        return (eSpeed / 60.0) * eDuration;
    }

    public void updateTemperature() {

        machine.runCommand(new replicatorg.drivers.commands.ReadTemperature());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        double temperature = machine.getDriver().getTemperature();
        String vTemperature = String.valueOf(temperature);
        cTargetTemperature.setText(vTemperature);

        if (loggingTemperature) {
            try {
                bw.write(vTemperature);
                bw.newLine();
            } catch (IOException ex) {
                Base.writeLog("Can't write temperature to file");
            }
        }
        
        //Graph variables
        Second second = new Second(new Date(System.currentTimeMillis() - startMillis));
        
		t0MeasuredDataset.add(second, temperature,"a");
		t0TargetDataset.add(second, this.temperatureGoal ,"a");		

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
        dispose();
//        Base.getMainWindow().handleStop();
        Base.bringAllWindowsToFront();
        Base.getMainWindow().getButtons().updatePressedStateButton("quick_guide");
        Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
        Base.maintenanceWizardOpen = false;
        disposeThread.stop();
        Base.getMainWindow().setEnabled(true);
        cleanLogFiles(true);
//        Point5d b = machine.getTablePoints("safe");
//        double acLow = machine.getAcceleration("acLow");
//        double acHigh = machine.getAcceleration("acHigh");
//        double spHigh = machine.getFeedrate("spHigh");
//
//        machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
//        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acLow));
//        machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spHigh));
//        machine.runCommand(new replicatorg.drivers.commands.QueuePoint(b));
//        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acHigh));
//        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28", COM.BLOCK));
//        machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
//
//        if (ProperDefault.get("maintenance").equals("1")) {
//            ProperDefault.remove("maintenance");
//        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        xLEFT = new javax.swing.JLabel();
        yUP = new javax.swing.JLabel();
        xRIGHT = new javax.swing.JLabel();
        yDOWN = new javax.swing.JLabel();
        panic = new javax.swing.JLabel();
        zUP = new javax.swing.JLabel();
        zDOWN = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        xTextFieldValue = new javax.swing.JTextField();
        yTextFieldValue = new javax.swing.JTextField();
        zTextFieldValue = new javax.swing.JTextField();
        bCenterX = new javax.swing.JButton();
        bCenterY = new javax.swing.JButton();
        bCenterZ = new javax.swing.JButton();
        bCurrentPosition = new javax.swing.JButton();
        bHome = new javax.swing.JButton();
        bCalibrateA = new javax.swing.JButton();
        bCalibrateB = new javax.swing.JButton();
        bCalibrateC = new javax.swing.JButton();
        bSetCalibration = new javax.swing.JButton();
        feedRate = new javax.swing.JLabel();
        xyFeedrate = new javax.swing.JLabel();
        zFeedrate = new javax.swing.JLabel();
        xyFeed = new javax.swing.JTextField();
        zFeed = new javax.swing.JTextField();
        enableFreeJog = new javax.swing.JLabel();
        freeJog = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        targetTemperature = new javax.swing.JLabel();
        currentTemperature = new javax.swing.JLabel();
        tTargetTemperature = new javax.swing.JTextField();
        cTargetTemperature = new javax.swing.JTextField();
        bReverse = new javax.swing.JButton();
        mSpeed = new javax.swing.JTextField();
        motorSpeed = new javax.swing.JLabel();
        extrudeDuration = new javax.swing.JLabel();
        extrudeCombo = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        bStop = new javax.swing.JButton();
        bFoward = new javax.swing.JButton();
        motorControl = new javax.swing.JLabel();
        coolFan = new javax.swing.JLabel();
        cCoolFan = new javax.swing.JCheckBox();
        logTemperature = new javax.swing.JLabel();
        cLogTemperature = new javax.swing.JCheckBox();
        saveLog = new javax.swing.JLabel();
        notes = new javax.swing.JLabel();
        tempPanel = new javax.swing.JPanel();
        tempLabel = new javax.swing.JLabel();
        colorCurrentTemp = new javax.swing.JLabel();
        colorTargetTemp = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        bOK = new javax.swing.JLabel();
        bCancel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));
        jPanel1.setToolTipText("");

        jPanel2.setBackground(new java.awt.Color(248, 248, 248));
        jPanel2.setToolTipText("");
        jPanel2.setPreferredSize(new java.awt.Dimension(415, 409));

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

        jLabel2.setText("X");

        jLabel3.setText("Y");

        jLabel4.setText("Z");

        xTextFieldValue.setEditable(false);
        xTextFieldValue.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                xTextFieldValueKeyPressed(evt);
            }
        });

        yTextFieldValue.setEditable(false);
        yTextFieldValue.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                yTextFieldValueKeyPressed(evt);
            }
        });

        zTextFieldValue.setEditable(false);
        zTextFieldValue.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                zTextFieldValueKeyPressed(evt);
            }
        });

        bCenterX.setText("Home X");
        bCenterX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCenterXActionPerformed(evt);
            }
        });

        bCenterY.setText("Home Y");
        bCenterY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCenterYActionPerformed(evt);
            }
        });

        bCenterZ.setText("Home Z");
        bCenterZ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCenterZActionPerformed(evt);
            }
        });

        bCurrentPosition.setText("Make Current position 0");
        bCurrentPosition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCurrentPositionActionPerformed(evt);
            }
        });

        bHome.setText("Home XYZ");
        bHome.setPreferredSize(new java.awt.Dimension(177, 29));
        bHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bHomeActionPerformed(evt);
            }
        });

        bCalibrateA.setText("Calibrate A");
        bCalibrateA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCalibrateAActionPerformed(evt);
            }
        });

        bCalibrateB.setText("Calibrate B");
        bCalibrateB.setPreferredSize(new java.awt.Dimension(145, 29));
        bCalibrateB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCalibrateBActionPerformed(evt);
            }
        });

        bCalibrateC.setText("Calibrate C");
        bCalibrateC.setPreferredSize(new java.awt.Dimension(145, 29));
        bCalibrateC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCalibrateCActionPerformed(evt);
            }
        });

        bSetCalibration.setText("Set Calibration");
        bSetCalibration.setPreferredSize(new java.awt.Dimension(177, 29));
        bSetCalibration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSetCalibrationActionPerformed(evt);
            }
        });

        feedRate.setText("Feedrate");

        xyFeedrate.setText("XY (mm/s)");

        zFeedrate.setText("Z (mm/s)");

        xyFeed.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                xyFeedKeyReleased(evt);
            }
        });

        zFeed.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                zFeedKeyReleased(evt);
            }
        });

        enableFreeJog.setText("Free jog");

        freeJog.setBackground(new java.awt.Color(248, 248, 248));
        freeJog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freeJogActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(zFeedrate)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(xyFeedrate, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(feedRate)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(xyFeed, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                            .addComponent(zFeed)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(bCalibrateB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bCalibrateA, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bCalibrateC, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bSetCalibration, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(56, 56, 56)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(bCurrentPosition, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                            .addComponent(bCenterZ, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bCenterY, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bCenterX, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bHome, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 62, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(enableFreeJog, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(freeJog))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                            .addComponent(jLabel4)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                            .addComponent(jLabel3)
                                            .addGap(13, 13, 13)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addGap(13, 13, 13)))
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(xTextFieldValue, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(yTextFieldValue)
                                        .addComponent(zTextFieldValue, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)))))
                        .addGap(87, 87, 87))))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(57, 57, 57)
                        .addComponent(yUP))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(xLEFT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(yDOWN)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(panic)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(xRIGHT)))))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(zDOWN)
                    .addComponent(zUP))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel2)
                                    .addComponent(xTextFieldValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(yTextFieldValue, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(zTextFieldValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4))
                                .addGap(55, 55, 55))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(freeJog)
                                    .addComponent(enableFreeJog))
                                .addGap(150, 150, 150)
                                .addComponent(bHome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(12, 12, 12)
                        .addComponent(bCenterX))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(yUP)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(xLEFT)
                                    .addComponent(panic)
                                    .addComponent(xRIGHT))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yDOWN))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(40, 40, 40)
                                .addComponent(zUP)
                                .addGap(14, 14, 14)
                                .addComponent(zDOWN)))
                        .addGap(18, 18, 18)
                        .addComponent(bCalibrateA)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                        .addComponent(bCalibrateB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bCalibrateC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(feedRate)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bCenterY)
                            .addComponent(bSetCalibration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bCenterZ)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(xyFeedrate)
                    .addComponent(xyFeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bCurrentPosition))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zFeedrate)
                    .addComponent(zFeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(206, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(248, 248, 248));

        targetTemperature.setText("Target temperature");

        currentTemperature.setText("Current temperature");

        tTargetTemperature.setText("0");
        tTargetTemperature.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tTargetTemperatureKeyReleased(evt);
            }
        });

        cTargetTemperature.setEditable(false);
        cTargetTemperature.setText("0");

        bReverse.setText("Reverse");
        bReverse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bReverseActionPerformed(evt);
            }
        });

        mSpeed.setText("0");

        motorSpeed.setText("Motor Speed");

        extrudeDuration.setText("Extrude duration");

        extrudeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        bStop.setText("Stop");
        bStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bStopActionPerformed(evt);
            }
        });

        bFoward.setText("Foward");
        bFoward.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bFowardActionPerformed(evt);
            }
        });

        motorControl.setText("Motor control");

        coolFan.setText("Cooling fan");

        cCoolFan.setBackground(new java.awt.Color(248, 248, 248));
        cCoolFan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cCoolFanActionPerformed(evt);
            }
        });

        logTemperature.setText("Log Temperature");
        logTemperature.setToolTipText("");

        cLogTemperature.setBackground(new java.awt.Color(248, 248, 248));
        cLogTemperature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cLogTemperatureActionPerformed(evt);
            }
        });

        saveLog.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/floppy_disk.png"))); // NOI18N
        saveLog.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                saveLogMousePressed(evt);
            }
        });

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

        colorCurrentTemp.setBackground(new java.awt.Color(204, 204, 204));
        colorCurrentTemp.setText("color1");

        colorTargetTemp.setBackground(new java.awt.Color(204, 204, 204));
        colorTargetTemp.setText("color1");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(targetTemperature, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(currentTemperature, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE))
                                .addGap(12, 12, 12)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(tTargetTemperature, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                                    .addComponent(cTargetTemperature))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(colorCurrentTemp)
                                    .addComponent(colorTargetTemp))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(2, 2, 2))
                    .addComponent(notes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(motorControl, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(logTemperature, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                                    .addComponent(coolFan, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(cLogTemperature)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(saveLog))
                                    .addComponent(cCoolFan))))
                        .addGap(0, 221, Short.MAX_VALUE))))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(extrudeDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(extrudeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(bReverse, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                                .addComponent(bStop, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)))
                        .addGap(17, 17, 17)
                        .addComponent(bFoward, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(motorSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(tempPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(tempLabel)))
                .addGap(0, 31, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(targetTemperature)
                    .addComponent(tTargetTemperature, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(colorTargetTemp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(6, 6, 6)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(currentTemperature)
                        .addComponent(cTargetTemperature, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(colorCurrentTemp)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cCoolFan)
                    .addComponent(coolFan))
                .addGap(8, 8, 8)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(saveLog, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cLogTemperature)
                    .addComponent(logTemperature))
                .addGap(18, 18, 18)
                .addComponent(tempLabel)
                .addGap(1, 1, 1)
                .addComponent(tempPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(motorSpeed))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extrudeDuration)
                    .addComponent(extrudeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addComponent(motorControl)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bReverse)
                    .addComponent(bStop)
                    .addComponent(bFoward))
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

        bCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_18.png"))); // NOI18N
        bCancel.setText("Cancel");
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

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bCancel)
                .addGap(903, 903, 903)
                .addComponent(bOK)
                .addGap(25, 25, 25))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bOK)
                    .addComponent(bCancel))
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
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 1037, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 642, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void bStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bStopActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bStopActionPerformed

    private void bReverseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bReverseActionPerformed

        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G92 E", COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 P500", COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 S0 P500", COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 P500", COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 S0 P500", COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 P500", COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 S0 P500", COM.BLOCK));

        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F" + Double.valueOf(mSpeed.getText()) + " E-" + getDistance(), COM.BLOCK));
        System.out.println("G1 F" + Double.valueOf(mSpeed.getText()) + " E-" + getDistance());
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G92 E", COM.BLOCK));
    }//GEN-LAST:event_bReverseActionPerformed

    private void bFowardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bFowardActionPerformed
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G92 E"));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 P500", COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 S0 P500", COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 P500", COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 S0 P500", COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 P500", COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 S0 P500", COM.BLOCK));
        System.out.println("G1 F" + Double.valueOf(mSpeed.getText()) + " E" + getDistance());
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F" + Double.valueOf(mSpeed.getText()) + " E" + getDistance(), COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G92 E", COM.BLOCK));
    }//GEN-LAST:event_bFowardActionPerformed

    private void tTargetTemperatureKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tTargetTemperatureKeyReleased
        temperatureGoal = Double.valueOf(tTargetTemperature.getText());
    }//GEN-LAST:event_tTargetTemperatureKeyReleased

    private void cCoolFanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cCoolFanActionPerformed
        if (coolFanPressed) {
            machine.runCommand(new replicatorg.drivers.commands.DispatchCommand(FAN_OFF, COM.BLOCK));
            cCoolFan.setSelected(false);
            coolFanPressed = false;
        } else {
            machine.runCommand(new replicatorg.drivers.commands.DispatchCommand(FAN_ON, COM.BLOCK));
            cCoolFan.setSelected(true);
            coolFanPressed = true;
        }
    }//GEN-LAST:event_cCoolFanActionPerformed

    private void bOKMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bOKMouseEntered
        bOK.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bOKMouseEntered

    private void bOKMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bOKMouseExited
        bOK.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bOKMouseExited

    private void bOKMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bOKMousePressed
        doCancel();
    }//GEN-LAST:event_bOKMousePressed

    private void bCancelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseEntered
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_18.png")));
    }//GEN-LAST:event_bCancelMouseEntered

    private void bCancelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseExited
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_18.png")));
    }//GEN-LAST:event_bCancelMouseExited

    private void bCancelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMousePressed
        doCancel();
    }//GEN-LAST:event_bCancelMousePressed

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
        parseAndJogY();
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bCalibrateA;
    private javax.swing.JButton bCalibrateB;
    private javax.swing.JButton bCalibrateC;
    private javax.swing.JLabel bCancel;
    private javax.swing.JButton bCenterX;
    private javax.swing.JButton bCenterY;
    private javax.swing.JButton bCenterZ;
    private javax.swing.JButton bCurrentPosition;
    private javax.swing.JButton bFoward;
    private javax.swing.JButton bHome;
    private javax.swing.JLabel bOK;
    private javax.swing.JButton bReverse;
    private javax.swing.JButton bSetCalibration;
    private javax.swing.JButton bStop;
    private javax.swing.JCheckBox cCoolFan;
    private javax.swing.JCheckBox cLogTemperature;
    private javax.swing.JTextField cTargetTemperature;
    private javax.swing.JLabel colorCurrentTemp;
    private javax.swing.JLabel colorTargetTemp;
    private javax.swing.JLabel coolFan;
    private javax.swing.JLabel currentTemperature;
    private javax.swing.JLabel enableFreeJog;
    private javax.swing.JComboBox extrudeCombo;
    private javax.swing.JLabel extrudeDuration;
    private javax.swing.JLabel feedRate;
    private javax.swing.JCheckBox freeJog;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel logTemperature;
    private javax.swing.JTextField mSpeed;
    private javax.swing.JLabel motorControl;
    private javax.swing.JLabel motorSpeed;
    private javax.swing.JLabel notes;
    private javax.swing.JLabel panic;
    private javax.swing.JLabel saveLog;
    private javax.swing.JTextField tTargetTemperature;
    private javax.swing.JLabel targetTemperature;
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

        while (true) {
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
