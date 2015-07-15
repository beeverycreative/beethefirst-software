package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import replicatorg.app.Languager;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.app.Base;
import replicatorg.app.DoNotSleep;
import replicatorg.app.FilamentControler;
import replicatorg.app.Printer;
import replicatorg.app.PrintEstimator;
import replicatorg.app.ProperDefault;
import replicatorg.drivers.Driver;
import replicatorg.machine.MachineInterface;
import replicatorg.model.PrintBed;
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
public class PrintSplashSimpleWaiting extends BaseDialog implements WindowListener {

    private final Printer prt;
    private boolean printEnded;
    private double startTimeMillis;
    private double startTimeMillis2;
    private final UpdateThread3 ut;
    private int progression;
    private static final String FORMAT = "%2d:%2d";
    private int remainingTime = 0;
    private boolean isPaused;
    private boolean unloadPressed;
    private Point5d pausePos;
    private static final String RESUME_PRINT = "M300";
    private static final String PAUSE_PRINT = "M300";
    final MachineInterface machine = Base.getMachineLoader().getMachineInterface();
    private double temperatureGoal = 220;
    private final double ambientTemperature = 20.0;
    private boolean firstUnloadStep = false;

    public PrintSplashSimpleWaiting(ArrayList<String> preferences, Printer p) {
        super(Base.getMainWindow(), Dialog.ModalityType.MODELESS);
        initComponents();
        setFont();
        setTextLanguage();
        centerOnScreen();
        prt = p;
        setProgressBarColor();
        printEnded = false;
        unloadPressed = printEnded;
        isPaused = false;
        tInfo6.setText("");
        tInfo7.setText("");
        bPause.setVisible(false);
        bOk.setVisible(false);
        bCancel.setVisible(false);
        bUnload.setVisible(false);
        enableDrag();
        addWindowListener(this);
        ut = new UpdateThread3(this);
        ut.start();
        Base.systemThreads.add(ut);
        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());

    }

    private void setFont() {
        tInfo2.setFont(GraphicDesignComponents.getSSProBold("14"));
        tEstimation.setFont(GraphicDesignComponents.getSSProRegular("11"));
        tRemaining.setFont(GraphicDesignComponents.getSSProRegular("11"));
        vEstimation.setFont(GraphicDesignComponents.getSSProRegular("11"));
        vRemaining.setFont(GraphicDesignComponents.getSSProRegular("11"));
        bCancel.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bOk.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bPause.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bUnload.setFont(GraphicDesignComponents.getSSProRegular("12"));
        tInfo6.setFont(GraphicDesignComponents.getSSProRegular("11"));
        tInfo7.setFont(GraphicDesignComponents.getSSProRegular("11"));
    }

    private void setTextLanguage() {
        tInfo2.setText(Languager.getTagValue(1,"Print", "Print_Splash_Info2"));
        tEstimation.setText(Languager.getTagValue(1,"Print", "Print_Estimation"));
        tRemaining.setText(Languager.getTagValue(1,"Print", "Print_Remaining"));
        bCancel.setText(Languager.getTagValue(1,"OptionPaneButtons", "Line3"));
        bOk.setText(Languager.getTagValue(1,"OptionPaneButtons", "Line10"));
        bPause.setText(Languager.getTagValue(1,"OptionPaneButtons", "Line11"));
        bUnload.setText(Languager.getTagValue(1,"OptionPaneButtons", "Line3"));
    }

    public void setProgression(int prog) {
        progression = prog;
    }

    private void setProgressBarColor() {
        jProgressBar1.setForeground(new Color(255, 203, 5));
    }

    public void updatePrintBar(double progression) {
        jProgressBar1.setValue((int) (progression));

    }

    private String minutesToHours(int minutes, boolean format) {
        int hoursTrunked = 0;
        int minutesTrunked = minutes;
        double quocient;
        final int BASE = 60;

        if (format) {
            int hours = minutes / 60;
            int minute = minutes % 60;
            return String.format(FORMAT, hours, minute);
        } else {
            if (minutes > BASE) {
                quocient = minutes / 60;
                int hoursCarry = (int) quocient;

                hoursTrunked += hoursCarry;
                minutesTrunked = (int) Math.round(minutes % BASE);

                if (hoursTrunked >= 1) {
                    return String.valueOf(hoursTrunked).concat(":").concat(String.valueOf(minutesTrunked));
                }
            }
            return String.valueOf(minutesTrunked);
        }
    }

    public File getPrintFile() {

        PrintBed bed = Base.getMainWindow().getBed();
//        StringBuffer gcode = Base.getMainWindow().getBed().getGcode();
//        File gcodeFile = new File(Base.getAppDataDirectory() + "/"+Base.MODELS_FOLDER+"/"+Base.GCODE_TEMP_FILENAME);
//        
//        if (bed.isGcodeOK()) {
//            PrintWriter out = null;
//            
//            try {
//                out = new PrintWriter(gcodeFile);                
//                String[] code = gcode.toString().split(Base.GCODE_DELIMITER);
//                
//                for (int i = 0; i < code.length; i++) {
//                    out.println(code[i]);
//                }
//                
//            } catch (IOException ex) {
//                Logger.getLogger(PrintSplashSimpleWaiting.class.getName()).log(Level.SEVERE, null, ex);
//            } finally {
//                out.close();
//            }
//            prt.setGCodeFile(gcodeFile);
//            
//        } else {
        return prt.getGCode();
//        }
//        return gcodeFile;
    }

    /**
     * Gets Start GCode to be appended to the GCode file
     *
     * @return startGCode File
     */
    public File getStartCode() {
        File gcode = null;
        PrintWriter pw;

        try {
            gcode = new File(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/start.gcode");
            pw = new PrintWriter(new FileOutputStream(gcode));

            String[] code = Languager.getGCodeArray(4, "operationCode", "startCode");

            for (String code1 : code) {
                pw.println(code1.trim());
            }

            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CalibrationPrintTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        return gcode;
    }

    /**
     * Gets End GCode to be appended to the GCode file
     *
     * @return endGCode File
     */
    public File getEndCode() {
        File gcode = null;
        PrintWriter pw;

        try {
            gcode = new File(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/end.gcode");
            pw = new PrintWriter(new FileOutputStream(gcode));

            String[] code = Languager.getGCodeArray(4, "operationCode", "endCode");

            for (String code1 : code) {
                pw.println(code1.trim());
            }

            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CalibrationPrintTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        return gcode;
    }

    public void enableSleep() {
        try {

            DoNotSleep ds = new DoNotSleep();
            ds.EnabledSleep();

            Base.writeLog("Sleep started!");

        } catch (Exception ex) {
            Base.writeLog("Error starting Sleep!");
        }
    }

    public void disableSleep() {
        try {

            DoNotSleep ds = new DoNotSleep();
            ds.DisableSleep();

            Base.writeLog("Sleep stoped!");

        } catch (Exception ex) {
            Base.writeLog("Error stoping Sleep!");
        }
    }
    
    public int getNGCodeLines() {
        return prt.getGCodeNLines();
    }

    public void startPrintCounter() {
        startTimeMillis = System.currentTimeMillis();
        startTimeMillis2 = System.currentTimeMillis();
    }

    private String printTime() {
        return String.valueOf((int) (Math.ceil((System.currentTimeMillis() - startTimeMillis) / 1000) / 60) + 2);
    }

    public void checkTimeElapsed() {

        if ((int) (Math.ceil((System.currentTimeMillis() - startTimeMillis2) / 1000)) > 60) {

            // Not able to estimate print time
            // 00 - error code
            if (remainingTime == 00) {
                tRemaining.setText(Languager.getTagValue(1,"Print", "Print_Info"));
                vRemaining.setVisible(false);
            } else {
                // Subtract one minute to remaining time
                remainingTime -= 1;

                if (remainingTime > 0) {
                    updatePrintEstimation(String.valueOf(remainingTime), false);
                } else {
                    tRemaining.setText(Languager.getTagValue(1,"Print", "Print_Info2"));
                    vRemaining.setVisible(false);
                }
            }
            startTimeMillis2 = System.currentTimeMillis();
        }
    }

    public boolean isUnloadPressed() {
        return unloadPressed;
    }

    public void setPrintEnded(boolean status) {
        printEnded = status;
        unloadPressed = false;

        if (printEnded) {
//            jPanel5.setVisible(false);
            tInfo6.setText("");
            int duration = Integer.valueOf(printTime());
            String minToHour = minutesToHours(duration, true);
            String hours = minToHour.split("\\:")[0];
            String minutes = minToHour.split("\\:")[1];

            vEstimation.setVisible(false);
            vRemaining.setVisible(false);
            bCancel.setVisible(false);
            bPause.setVisible(false);
            bOk.setVisible(true);
            bUnload.setText(Languager.getTagValue(1,"FilamentWizard", "UnloadButton"));

            jProgressBar1.setValue(100);
            jProgressBar1.setVisible(false);

            if (duration >= 2 && duration > 60) {

                tInfo2.setText(Languager.getTagValue(1,"Print", "Print_BuildFinished"));
                tEstimation.setText(Languager.getTagValue(1,"Print", "Print_Completion") + " " + hours + " "
                        + Languager.getTagValue(1,"Print", "PrintHours") + " " + minutes + " " + Languager.getTagValue(1,"Print", "PrintMinutes"));
                bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                tRemaining.setText(Languager.getTagValue(1,"Print", "Print_Splash_Info5"));

            } else {
                tInfo2.setText(Languager.getTagValue(1,"Print", "Print_BuildFinished"));
                tEstimation.setText(Languager.getTagValue(1,"Print", "Print_Completion") + " " + minutes + " " + Languager.getTagValue(1,"Print", "PrintMinutes"));
                bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                tRemaining.setText(Languager.getTagValue(1,"Print", "Print_Splash_Info5"));

            }
            ProperDefault.put("durationLastPrint", String.valueOf(duration));
            int nPrints = Integer.valueOf(ProperDefault.get("nTotalPrints")) + 1;
            ProperDefault.put("nTotalPrints", String.valueOf(nPrints));

            /**
             * Power saving
             */
            Base.turnOnPowerSaving(true);

        } else if (!printEnded) {
            abortPrint();
        }
    }

    public void abortPrint() {
        dispose();
        Base.getMainWindow().setEnabled(true);
        Base.getMainWindow().doPreheat(false);
        Base.getMainWindow().handleStop();
//        Base.getMainWindow().doStop();
        Base.getMainWindow().getButtons().updatePressedStateButton("print");
        enableSleep();
        machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
    }

    public void updatePrintEstimation(String printEstimation, boolean cutout) {
        String[] duration;
        String textE;
        String textR = "";

        if (!printEstimation.contains("NA")) {
            if (printEstimation.contains(":")) {
                duration = printEstimation.split(":");
                int hours = Integer.valueOf(duration[0]);
                int minutes = Integer.valueOf(duration[1]);

                remainingTime = hours * 60 + minutes;

                if (hours > 1) {
                    if (cutout) {

                        if (minutes > 1) {
                            textE = hours
                                    + " " + Languager.getTagValue(1,"Print", "PrintHours")
                                    + " " + minutes
                                    + " " + Languager.getTagValue(1,"Print", "PrintMinutes");
                            textR = hours
                                    + " " + Languager.getTagValue(1,"Print", "PrintHours")
                                    + " " + minutes
                                    + " " + Languager.getTagValue(1,"Print", "PrintMinutes");
                        } else {
                            textE = hours
                                    + " " + Languager.getTagValue(1,"Print", "PrintHours")
                                    + " " + minutes
                                    + " " + Languager.getTagValue(1,"Print", "PrintMinute");
                            textR = hours
                                    + " " + Languager.getTagValue(1,"Print", "PrintHours")
                                    + " " + minutes
                                    + " " + Languager.getTagValue(1,"Print", "PrintMinute");
                        }
                        vRemaining.setText(textR);
                        vEstimation.setText(textE);
                    } else {

                        if (minutes > 1) {
                            textR = hours
                                    + " " + Languager.getTagValue(1,"Print", "PrintHours")
                                    + " " + minutes
                                    + " " + Languager.getTagValue(1,"Print", "PrintMinutes");
                        } else {
                            textR = hours
                                    + " " + Languager.getTagValue(1,"Print", "PrintHours")
                                    + " " + minutes
                                    + " " + Languager.getTagValue(1,"Print", "PrintMinute");
                        }
                        vRemaining.setText(textR);
                    }
                } else {
                    if (cutout) {

                        if (minutes > 1) {

                            textE = hours
                                    + " " + Languager.getTagValue(1,"Print", "PrintHour")
                                    + " " + minutes
                                    + " " + Languager.getTagValue(1,"Print", "PrintMinutes");

                            textR = hours
                                    + " " + Languager.getTagValue(1,"Print", "PrintHour")
                                    + " " + minutes
                                    + " " + Languager.getTagValue(1,"Print", "PrintMinutes");
                        } else if (minutes == 2) {
                            //In case PrintEstimator fails, dont appear 2min                            
                            vEstimation.setVisible(false);
                            tEstimation.setText(Languager.getTagValue(1,"Print", "Print_EstimatorError"));
                            return;
                        } else {
                            textE = hours
                                    + " " + Languager.getTagValue(1,"Print", "PrintHour")
                                    + " " + minutes
                                    + " " + Languager.getTagValue(1,"Print", "PrintMinute");

                            textR = hours
                                    + " " + Languager.getTagValue(1,"Print", "PrintHour")
                                    + " " + minutes
                                    + " " + Languager.getTagValue(1,"Print", "PrintMinute");
                        }

                        vEstimation.setText(textE);
                        vRemaining.setText(textR);
                    } else {
                        if (minutes > 1 || minutes == 0) {
                            textR = hours
                                    + " " + Languager.getTagValue(1,"Print", "PrintHour")
                                    + " " + minutes
                                    + " " + Languager.getTagValue(1,"Print", "PrintMinutes");
                        } else {
                            textR = hours
                                    + " " + Languager.getTagValue(1,"Print", "PrintHour")
                                    + " " + minutes
                                    + " " + Languager.getTagValue(1,"Print", "PrintMinute");
                        }
                        vRemaining.setText(textR);
                    }
                }
            } else {
                remainingTime = Integer.valueOf(printEstimation);
                int hours = 0;
                int minutes = 0;

                if (printEstimation.contains(":")) {
                    duration = printEstimation.split(":");
                    hours = Integer.valueOf(duration[0]);
                    minutes = Integer.valueOf(duration[1]);
                } else {
                    if (remainingTime > 60) {
                        printEstimation = minutesToHours(remainingTime, false);
                        duration = printEstimation.split(":");
                        hours = Integer.valueOf(duration[0]);
                        minutes = Integer.valueOf(duration[1]);
                    }
                }

                if (cutout) {
//                    textE = printEstimation
//                            + " " + Languager.getTagValue(1,"Print", "PrintMinutes");

                    if (remainingTime > 60) {
                        if (minutes > 1 && hours > 1) {
                            textR = hours
                                    + " " + Languager.getTagValue(1,"Print", "PrintHours")
                                    + " " + minutes
                                    + " " + Languager.getTagValue(1,"Print", "PrintMinutes");
                        } else {
                            textR = hours
                                    + " " + Languager.getTagValue(1,"Print", "PrintHour")
                                    + " " + minutes
                                    + " " + Languager.getTagValue(1,"Print", "PrintMinutes");
                        }
                    } else {
                        if (remainingTime > 1) {
                            textR = remainingTime
                                    + " " + Languager.getTagValue(1,"Print", "PrintMinutes");
                        } else if (remainingTime == 2) {
                            //In case PrintEstimator fails, dont appear 2min                          
                            vEstimation.setVisible(false);
                            tRemaining.setVisible(false);
                            vRemaining.setVisible(false);
                            tEstimation.setText(Languager.getTagValue(1,"Print", "Print_EstimatorError"));
                            return;
                        } else {
                            textR = remainingTime
                                    + " " + Languager.getTagValue(1,"Print", "PrintMinute");
                        }
                    }
                    vEstimation.setText(textR);
                    vRemaining.setText(textR);
                } else {
                    if (remainingTime > 60) {

                        if ((minutes > 1 || minutes == 0)) {
                            textR = hours
                                    + " " + Languager.getTagValue(1,"Print", "PrintHours")
                                    + " " + minutes
                                    + " " + Languager.getTagValue(1,"Print", "PrintMinutes");
                        } else {

                            textR = hours
                                    + " " + Languager.getTagValue(1,"Print", "PrintHour")
                                    + " " + minutes
                                    + " " + Languager.getTagValue(1,"Print", "PrintMinutes");

                        }
                    }
                    if (remainingTime > 1 && remainingTime <= 60) {
                        textR = remainingTime
                                + " " + Languager.getTagValue(1,"Print", "PrintMinutes");
                    } else if (remainingTime == 1) {
                        textR = remainingTime
                                + " " + Languager.getTagValue(1,"Print", "PrintMinute");
                    }
                    // no need for else. Parse is being made before calling thin method

                    vRemaining.setText(textR);

                }
            }
        } else {
            remainingTime = 00;
            tEstimation.setText(Languager.getTagValue(1,"Print", "Print_EstimatorError"));
            vEstimation.setText("");
            tRemaining.setText("");
            vRemaining.setText("");
        }

    }

    private void setPreparingNewFilamentInfo() {
        tInfo2.setText(Languager.getTagValue(1,"Print", "Print_Pausing"));
        tRemaining.setVisible(false);
        vRemaining.setVisible(false);
        tEstimation.setVisible(false);
        vEstimation.setVisible(false);
        jProgressBar1.setVisible(false);
    }

    private void disablePreparingNewFilamentInfo() {
        tInfo2.setText(Languager.getTagValue(1,"Print", "Print_Splash_Info2"));
        tRemaining.setVisible(true);
        vRemaining.setVisible(true);
        tEstimation.setVisible(true);
        vEstimation.setVisible(true);
        jProgressBar1.setVisible(true);
        bPause.setText(Languager.getTagValue(1,"OptionPaneButtons", "Line11"));
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
        Base.getMainWindow().setState(JFrame.ICONIFIED);
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        jProgressBar1.setValue(progression);
        Base.getMainWindow().setExtendedState(JFrame.MAXIMIZED_BOTH);

    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    public boolean isPrintPaused() {
        return isPaused;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator1 = new javax.swing.JSeparator();
        jPanel2 = new javax.swing.JPanel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jPanel4 = new javax.swing.JPanel();
        bClose = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        tInfo6 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        tRemaining = new javax.swing.JLabel();
        vRemaining = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        tEstimation = new javax.swing.JLabel();
        vEstimation = new javax.swing.JLabel();
        tInfo2 = new javax.swing.JLabel();
        tInfo7 = new javax.swing.JLabel();
        iPrinting = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        bCancel = new javax.swing.JLabel();
        bOk = new javax.swing.JLabel();
        bPause = new javax.swing.JLabel();
        bUnload = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(248, 248, 248));
        setUndecorated(true);
        setResizable(false);

        jPanel2.setBackground(new java.awt.Color(248, 248, 248));
        jPanel2.setPreferredSize(new java.awt.Dimension(475, 169));

        jProgressBar1.setBackground(new java.awt.Color(186, 186, 186));

        jPanel4.setBackground(new java.awt.Color(248, 248, 248));
        jPanel4.setMinimumSize(new java.awt.Dimension(62, 26));
        jPanel4.setRequestFocusEnabled(false);

        bClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_pressed_9.png"))); // NOI18N
        bClose.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bCloseMousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(bClose, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(15, 15, 15))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bClose, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(8, Short.MAX_VALUE))
        );

        jPanel7.setBackground(new java.awt.Color(248, 248, 248));
        jPanel7.setPreferredSize(new java.awt.Dimension(360, 142));

        tInfo6.setText("tInfo6");

        jPanel5.setBackground(new java.awt.Color(248, 248, 248));

        tRemaining.setText("tRemaining");

        vRemaining.setText("NA");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(tRemaining)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(vRemaining)
                .addGap(214, 214, 214))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tRemaining)
                    .addComponent(vRemaining))
                .addGap(0, 0, 0))
        );

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));
        jPanel1.setPreferredSize(new java.awt.Dimension(117, 22));

        tEstimation.setText("tEstimation");

        vEstimation.setText("NA");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(tEstimation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(vEstimation)
                .addGap(214, 214, 214))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tEstimation)
                    .addComponent(vEstimation))
                .addContainerGap())
        );

        tInfo2.setText("A PROCESSAR... POR FAVOR AGUARDE");

        tInfo7.setText("tInfo7");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tInfo2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(tInfo6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 12, Short.MAX_VALUE))
                    .addComponent(tInfo7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addComponent(tInfo2)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tInfo6)
                .addGap(6, 6, 6)
                .addComponent(tInfo7)
                .addContainerGap())
        );

        iPrinting.setPreferredSize(new java.awt.Dimension(75, 75));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 372, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(iPrinting, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 687, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(iPrinting, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(255, 203, 5));
        jPanel3.setMinimumSize(new java.awt.Dimension(20, 46));
        jPanel3.setPreferredSize(new java.awt.Dimension(423, 127));

        bCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_18.png"))); // NOI18N
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

        bOk.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_21.png"))); // NOI18N
        bOk.setText("OK");
        bOk.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bOk.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bOkMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bOkMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bOkMousePressed(evt);
            }
        });

        bPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bPause.setText("PAUSE");
        bPause.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bPause.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bPauseMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bPauseMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bPauseMousePressed(evt);
            }
        });

        bUnload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bUnload.setText("Unload");
        bUnload.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bUnload.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bUnloadMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bUnloadMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bUnloadMousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bPause)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bOk)
                .addGap(12, 12, 12)
                .addComponent(bUnload)
                .addGap(12, 12, 12))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bCancel)
                    .addComponent(bPause)
                    .addComponent(bUnload)
                    .addComponent(bOk))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bCloseMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCloseMousePressed
        doCancel();
    }//GEN-LAST:event_bCloseMousePressed

    private void bUnloadMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bUnloadMousePressed

        //first time you press unload after print is over
        if (printEnded && unloadPressed == false && firstUnloadStep == false) {
            unloadPressed = true;
            System.out.println("First time unload");
            temperatureGoal = 220;
            machine.runCommand(new replicatorg.drivers.commands.ReadTemperature());
            bOk.setVisible(false);
            bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_21.png")));
            iPrinting.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "retirar_filamento-02.png")));
            tInfo2.setText(Languager.getTagValue(1,"Print", "Unloading_Title"));
            tRemaining.setText(Languager.getTagValue(1,"Print", "Print_Unloading"));
            jPanel1.setVisible(false);
            jProgressBar1.setVisible(true);
            jProgressBar1.setValue(0);
            return;
        } // no need for else

        //any time you press unload after the first time
        if (printEnded && unloadPressed == false && firstUnloadStep) {
            unloadPressed = true;
            bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_21.png")));
            System.out.println("Second time unload");
            startUnload();
            return;
        } // no need for else

        if (printEnded == false) {
            doCancel();
        } // no need for else

    }//GEN-LAST:event_bUnloadMousePressed

    private void bUnloadMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bUnloadMouseExited
        if (!unloadPressed) {
            bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
        }
    }//GEN-LAST:event_bUnloadMouseExited

    private void bUnloadMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bUnloadMouseEntered
        if (!unloadPressed) {
            bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
        }
    }//GEN-LAST:event_bUnloadMouseEntered

    private void bPauseMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPauseMousePressed
        final MachineInterface mchn = Base.getMachineLoader().getMachineInterface();
        final Driver driver = Base.getMainWindow().getMachineInterface().getDriver();

        if (!isPaused) {
            mchn.stopwatch();
            setPreparingNewFilamentInfo();
            Base.printPaused = true;
            isPaused = true;

            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    pausePos = driver.getActualPosition();

                    driver.dispatchCommand("G28", COM.BLOCK);
                    driver.dispatchCommand("G1 F1000");
                    driver.dispatchCommand("M206 x400");
                    driver.dispatchCommand("M300");

                    String status = "";

                    while (!status.contains("S:3")) {
                        try {
                            Thread.sleep(1, 0);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(PrintSplashAutonomous.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        status += driver.dispatchCommand("M625");
                    }

                    //Flags firmware to stop queued commands
                    driver.dispatchCommand(PAUSE_PRINT, COM.DEFAULT);

                    mchn.getModel().setMachineBusy(false);

                    Maintenance p = new Maintenance();
                    p.setVisible(true);
                    bPause.setText(Languager.getTagValue(1,"OptionPaneButtons", "Line12"));
                    tInfo2.setText(Languager.getTagValue(1,"Print", "Print_Waiting"));
                }
            });
        } else {
            isPaused = false;
            tInfo2.setText(Languager.getTagValue(1,"Print", "Print_Resuming"));
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    //Little retraction to avoid filament break
                    driver.dispatchCommand("G1 F300 E50", COM.BLOCK);
                    driver.dispatchCommand("G92 E", COM.BLOCK);
                    driver.dispatchCommand("G1 F6000 E-2", COM.BLOCK);
//                    driver.dispatchCommand("G1 F6000 E0", COM.BLOCK);

                    double colorRatio = FilamentControler.getColorRatio(mchn.getModel().getCoilCode(), mchn.getModel().getResolution());
                    /**
                     * Signals FW about the color ratio between previous and
                     * actual color
                     */
                    driver.dispatchCommand("M642 W" + colorRatio, COM.NO_RESPONSE);

                    //Go slowly to pause position
                    //Special attention to models on bed
                    driver.dispatchCommand("G1 F2000 X" + pausePos.x() + " Y" + pausePos.y(), COM.NO_RESPONSE);
                    driver.dispatchCommand("G1 F1000 Z" + (pausePos.z() + 10) + " E-1", COM.NO_RESPONSE);
                    driver.dispatchCommand("G1 F1000 Z" + pausePos.z() + " E0", COM.NO_RESPONSE);
                    driver.dispatchCommand("G92 E" + pausePos.a(), COM.BLOCK);

//            //Sets previous feedrate
                    driver.dispatchCommand("G1 " + mchn.getLastFeedrate(), COM.NO_RESPONSE);
//            //Sets last acceleration
                    driver.dispatchCommand(mchn.getLastAcceleration(), COM.NO_RESPONSE);

                    //Flags firmware to resume queued commands
                    driver.dispatchCommand(RESUME_PRINT, COM.NO_RESPONSE);

                    //Resume printing
                    mchn.resumewatch();
                    Base.printPaused = false;
                    disablePreparingNewFilamentInfo();
                }
            });
        }
    }//GEN-LAST:event_bPauseMousePressed

    private void bPauseMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPauseMouseExited
        bPause.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bPauseMouseExited

    private void bPauseMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPauseMouseEntered
        bPause.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bPauseMouseEntered

    public boolean updateHeatBar() {

        machine.runCommand(new replicatorg.drivers.commands.ReadTemperature());
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M104 S" + temperatureGoal, COM.DEFAULT));

        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(PrintSplashSimple.class.getName()).log(Level.SEVERE, null, ex);
        }

        double temperature = machine.getDriver().getTemperature();

        if (temperature > (int) (jProgressBar1.getValue() * 2)) {
            int val = (int) (temperature / 2.15);
            if (val > jProgressBar1.getValue()) {
                jProgressBar1.setValue(val);
            }
        }

        if (temperature >= temperatureGoal) {
            jProgressBar1.setVisible(false);
            return true;
        }

        return false;
    }

    private void bOkMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bOkMousePressed
        if (printEnded) {

            if (firstUnloadStep) {
                bUnload.setVisible(false);
                iPrinting.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "retirar_filamento-07.png")));
                bOk.setText(Languager.getTagValue(1,"OptionPaneButtons", "Line6"));
                firstUnloadStep = false;
                tRemaining.setText(Languager.getTagValue(1,"Print", "Print_Unloaded2"));
                tInfo6.setVisible(false);

            } else {
                dispose();
                ut.stop();
                Base.getMainWindow().setEnabled(true);
                Base.isPrinting = false;
                enableSleep();
                Base.getMachineLoader().getMachineInterface().runCommand(new replicatorg.drivers.commands.SetTemperature(0));
                Base.getMainWindow().getButtons().updatePressedStateButton("print");
                Base.cleanDirectoryTempFiles(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/");
                Base.bringAllWindowsToFront();
                machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
                machine.getModel().setMachineBusy(false);
            }
        }
    }//GEN-LAST:event_bOkMousePressed

    private void bOkMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bOkMouseExited
        if (printEnded) {
            bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
        }
    }//GEN-LAST:event_bOkMouseExited

    private void bOkMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bOkMouseEntered
        if (printEnded) {
            bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
        }
    }//GEN-LAST:event_bOkMouseEntered

    private void bCancelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMousePressed
        doCancel();
    }//GEN-LAST:event_bCancelMousePressed

    private void bCancelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseExited
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_18.png")));
    }//GEN-LAST:event_bCancelMouseExited

    private void bCancelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseEntered
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_18.png")));
    }//GEN-LAST:event_bCancelMouseEntered
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bCancel;
    private javax.swing.JLabel bClose;
    private javax.swing.JLabel bOk;
    private javax.swing.JLabel bPause;
    private javax.swing.JLabel bUnload;
    private javax.swing.JLabel iPrinting;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel tEstimation;
    private javax.swing.JLabel tInfo2;
    private javax.swing.JLabel tInfo6;
    private javax.swing.JLabel tInfo7;
    private javax.swing.JLabel tRemaining;
    private javax.swing.JLabel vEstimation;
    private javax.swing.JLabel vRemaining;
    // End of variables declaration//GEN-END:variables

    public void startUnload() {
        jProgressBar1.setVisible(false);
        iPrinting.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "rsz_unload-01.png")));
        tRemaining.setText(Languager.getTagValue(1,"FilamentWizard", "Exchange_Info3"));

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {

                if (!machine.getDriver().isBusy()) {
                    unloadPressed = true;
                    while (!machine.getDriver().getMachineStatus()) {
                        machine.runCommand(new replicatorg.drivers.commands.ReadStatus());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(DisposeFeedbackThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    Point5d rest = machine.getTablePoints("rest");

                    double acLow = machine.getAcceleration("acLow");
                    double acHigh = machine.getAcceleration("acHigh");
                    double spHigh = machine.getFeedrate("spHigh");

                    machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acLow));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F" + spHigh + " X" + rest.a() + " Y" + rest.b()));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acHigh));

                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G92 E", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 P500", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 S0 P500", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 P500", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 S0 P500", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 P500", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 S0 P500", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F250 E50", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G92 E", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F1000 E-23", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F800 E2", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F2000 E-23", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F200 E-50", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G92 E", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.ReadStatus());

                    Base.getMainWindow().getMachineInterface().getModel().setMachineReady(false);

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DisposeFeedbackThread.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    while (!machine.getDriver().getMachineStatus()) {
                        machine.runCommand(new replicatorg.drivers.commands.ReadStatus());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(DisposeFeedbackThread.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }

                bUnload.setVisible(false);
                iPrinting.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "retirar_filamento-01.png")));
                machine.runCommand(new replicatorg.drivers.commands.SetCoilCode(FilamentControler.NO_FILAMENT_CODE));
                tRemaining.setText(Languager.getTagValue(1,"Print", "Print_Unloaded1"));
                bOk.setVisible(true);
                firstUnloadStep = true;
                bOk.setText(Languager.getTagValue(1,"OptionPaneButtons", "Line7"));
                tInfo6.setText("");
                tInfo2.setText(Languager.getTagValue(1,"Print", "Unload_BuildFinished"));
                tInfo6.setText(Languager.getTagValue(1,"Print", "Print_Unloaded3"));
                unloadPressed = false;
                bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));

            }
        });
    }

    public boolean checkIFReady() {
        boolean ready = false;

        while (true) {
            machine.runCommand(new replicatorg.drivers.commands.ReadStatus());
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(DisposeFeedbackThread.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (machine.getDriver().getMachineStatus()) {
                ready = true;
                break;
            }
        }

        return ready;
    }

    private void doCancel() {
        dispose();
        MachineInterface mchn = Base.getMainWindow().getMachine();
        Base.bringAllWindowsToFront();
        Base.getMainWindow().handleStop();
        Base.getMainWindow().setEnabled(true);
        Base.isPrinting = false;
        Base.printPaused = false;
        Base.isPrintingFromGCode = false;
        Base.gcodeToSave = false;        
        Base.getMainWindow().getButtons().updatePressedStateButton("print");
        ut.stop();
        Base.turnOnPowerSaving(true);
        Base.cleanDirectoryTempFiles(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/");

        enableSleep();
        Point5d b = mchn.getTablePoints("safe");
        double acLow = mchn.getAcceleration("acLow");
        double acHigh = mchn.getAcceleration("acHigh");
        double spHigh = mchn.getFeedrate("spHigh");

        // Sleep before home after M112
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(PrintSplashSimpleWaiting.class.getName()).log(Level.SEVERE, null, ex);
        }

        mchn.runCommand(new replicatorg.drivers.commands.SetBusy(true));
        mchn.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28 Z", COM.BLOCK));
        mchn.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28 XY", COM.BLOCK));
        mchn.runCommand(new replicatorg.drivers.commands.SetBusy(false));

    }
}

class UpdateThread3 extends Thread {

    PrintSplashSimpleWaiting window;

    public UpdateThread3(PrintSplashSimpleWaiting w) {
        super("Print Splash Simple Waiting Thread");
        window = w;
    }

    @Override
    public void run() {
        try {
            window.startPrintCounter();
            int nLines;
            boolean finished = false;
            BufferedReader readerGCode = null;
            String sCurrentLinereaderGCode;
            BufferedReader readerStartGCode = null;
            String sCurrentLinereaderStartGCode;
            BufferedReader readerEndGCode = null;
            String sCurrentLinereaderEndGCode;
            MachineInterface machine = Base.getMachineLoader().getMachineInterface();

            try {
                // Gets GCode file
                File gcode = window.getPrintFile();

                readerGCode = new BufferedReader(new FileReader(gcode));

                readerStartGCode = new BufferedReader(new FileReader(window.getStartCode()));
                readerEndGCode = new BufferedReader(new FileReader(window.getEndCode()));
//                PrintEstimator.estimateTime(gcode);
                window.updatePrintEstimation(PrintEstimator.getEstimatedTime(), true);
            } catch (FileNotFoundException ex) {
                Base.writeLog("Can't read gCode file to print");
            }

            nLines = window.getNGCodeLines();
            Base.getMachineLoader().getMachineInterface().setStopwatch(0);
            machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));

            /**
             * Handler for START GCODE
             */
            while ((sCurrentLinereaderStartGCode = readerStartGCode.readLine()) != null) {
                ProperDefault.put("transferingGCode", String.valueOf(true));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand(sCurrentLinereaderStartGCode, COM.NO_RESPONSE));
            }

            /**
             * Ensure G92 E is sent. Sometimes happens a retraction
             */
            machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G92 E", COM.BLOCK));

            /**
             * Handler for GCODE
             */
            while ((sCurrentLinereaderGCode = readerGCode.readLine()) != null) {
                ProperDefault.put("transferingGCode", String.valueOf(true));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand(sCurrentLinereaderGCode, COM.NO_RESPONSE));
            }

            /**
             * Handler for END GCODE
             */
            while ((sCurrentLinereaderEndGCode = readerEndGCode.readLine()) != null) {
                ProperDefault.put("transferingGCode", String.valueOf(true));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand(sCurrentLinereaderEndGCode, COM.BLOCK, true));
            }

            ProperDefault.put("transferingGCode", String.valueOf(false));
            ProperDefault.remove("transferingGCode");

            double lines;

            /**
             * Update visual elements
             */
            while (true) {

                if (!window.isPrintPaused()) {
                    lines = (double) machine.getStopwatch();
                    window.updatePrintBar((lines / (double) nLines) * 100);
                    window.setProgression((int) ((lines / (double) nLines) * 100));

                    window.checkTimeElapsed();

                    if (machine.getStopwatch() >= nLines) {
                        finished = true;
                        break;
                    }
                }
            }

            readerGCode.close();
            readerEndGCode.close();          
            readerStartGCode.close();

            /**
             * Check if machine is ready to continue
             */
            if (window.checkIFReady()) {
                window.setPrintEnded(finished);
                Base.setPrintEnded(true);
                machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
            }


            while (true) {
                boolean tempReached;

                if (window.isUnloadPressed()) {
                    System.out.println("Unload Pressed");
                    tempReached = window.updateHeatBar();
                    if (tempReached) {
                        window.startUnload();
                        break;
                    }
                }
            }
            machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
            this.stop();
        } catch (IOException ex) {
            Logger.getLogger(UpdateThread3.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
