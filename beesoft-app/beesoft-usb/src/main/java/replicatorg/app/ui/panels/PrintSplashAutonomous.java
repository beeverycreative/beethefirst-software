package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.ExecutionException;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
import replicatorg.app.Languager;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.app.Base;
import pt.beeverycreative.beesoft.filaments.PrintPreferences;
import replicatorg.app.PrintEstimator;
import replicatorg.app.Printer;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.popups.Query;
import replicatorg.app.util.AutonomousData;
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
public class PrintSplashAutonomous extends BaseDialog {

    private final Driver driver = Base.getMachineLoader().getMachineInterface().getDriver();
    private final MachineModel model = driver.getMachine();
    private final Printer prt;
    private final PrintPreferences preferences;
    private boolean printEnded;
    private final GCodeGenWorker gcodeGenerator = new GCodeGenWorker();
    private final PrintingThread ut = new PrintingThread();
    private final boolean alreadyPrinting;
    private boolean errorOccurred = false;
    private boolean firstUnloadDone = false;
    private boolean lastPanel;
    private boolean isPaused = false;
    private boolean isShutdown = false;
    protected final int temperatureGoal;
    private final Object mutex = new Object();
    private final Timer temperatureTimer = new Timer(3000, new TemperatureActionListener());
    private final Timer monitorPrintTimer = new Timer(500, new MonitorPrintListener());
    private PauseAssistantThread pauseThread;
    private HeatAndUnloadThread heatAndUnloadThread;

    public PrintSplashAutonomous(PrintPreferences prefs) {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        setFont();
        setTextLanguage();
        super.centerOnScreen();
        preferences = prefs;
        prt = new Printer(preferences);
        temperatureGoal = prt.getFilamentTemperature();
        printEnded = false;
        bOk.setVisible(false);
        bUnload.setVisible(false);
        bPause.setVisible(false);
        alreadyPrinting = false;
        jProgressBar1.setForeground(new Color(255, 203, 5));
        jProgressBar1.setIndeterminate(true);
        jProgressBar1.setMaximum(100);
        this.setName("Autonomous");

        temperatureTimer.setInitialDelay(0);
        if (Base.isPrintingFromGCode) {
            prt.setGCodeFile(new File(preferences.getGcodeToPrint()));
        }

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                ut.start();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                ut.kill();
                temperatureTimer.stop();
                monitorPrintTimer.stop();
                if (pauseThread != null) {
                    pauseThread.kill();
                }
                if (heatAndUnloadThread != null) {
                    heatAndUnloadThread.kill();
                }
                Base.isPrinting = false;
                Base.getMainWindow().getButtons().updatePressedStateButton("print");
                Base.cleanDirectoryTempFiles(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/");
            }
        });
    }

    //This constructor is to be used if there's already an ongoing print
    public PrintSplashAutonomous(boolean isPaused, boolean isShutdown) {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        setFont();
        setTextLanguage();
        super.centerOnScreen();
        preferences = new PrintPreferences();
        prt = new Printer(preferences);
        temperatureGoal = prt.getFilamentTemperature();
        printEnded = false;
        bOk.setVisible(false);
        bUnload.setVisible(false);
        bPause.setVisible(false);
        alreadyPrinting = true;
        jProgressBar1.setForeground(new Color(255, 203, 5));
        jProgressBar1.setIndeterminate(true);
        jProgressBar1.setMaximum(100);
        this.setName("Autonomous");

        temperatureTimer.setInitialDelay(0);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                ut.start();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                ut.kill();
                temperatureTimer.stop();
                monitorPrintTimer.stop();
                if (pauseThread != null) {
                    pauseThread.kill();
                }
                Base.isPrinting = false;
                Base.getMainWindow().getButtons().updatePressedStateButton("print");
                Base.cleanDirectoryTempFiles(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/");
            }
        });

        this.isPaused = isPaused;
        this.isShutdown = isShutdown;
    }

    private void setFont() {
        tInfo2.setFont(GraphicDesignComponents.getSSProBold("14"));
        tInfo3.setFont(GraphicDesignComponents.getSSProBold("12"));
        tEstimation.setFont(GraphicDesignComponents.getSSProRegular("11"));
        tRemaining.setFont(GraphicDesignComponents.getSSProRegular("11"));
        vEstimate.setFont(GraphicDesignComponents.getSSProRegular("11"));
        vRemaining.setFont(GraphicDesignComponents.getSSProRegular("11"));
        bCancel.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bOk.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bPause.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bUnload.setFont(GraphicDesignComponents.getSSProRegular("12"));
        tInfo6.setFont(GraphicDesignComponents.getSSProRegular("11"));
        tInfo7.setFont(GraphicDesignComponents.getSSProRegular("11"));
    }

    private void setTextLanguage() {
        tInfo2.setText(Languager.getTagValue("Print", "Print_Splash_Info2"));
        tInfo3.setText(Languager.getTagValue("Print", "Print_Splash_Info4"));
        tEstimation.setText(Languager.getTagValue("Print", "Print_Estimation"));
        tRemaining.setText(Languager.getTagValue("Print", "Print_Remaining"));
        bCancel.setText(Languager.getTagValue("OptionPaneButtons", "Line3"));
        bOk.setText(Languager.getTagValue("OptionPaneButtons", "Line10"));
        bPause.setText(Languager.getTagValue("OptionPaneButtons", "Line11"));
        bUnload.setText(Languager.getTagValue("OptionPaneButtons", "Line14"));
        setProcessingInfo();
    }

    public void updatePrintBar(int progression) {
        jProgressBar1.setIndeterminate(false);
        jProgressBar1.setValue(progression);
    }

    /**
     * Used to abort UI panel and inform user.
     */
    public void setError() {
        errorOccurred = true;
        updateInformationsByError();
        jProgressBar1.setIndeterminate(false);
        jProgressBar1.setValue(0);
    }

    /**
     * Cancel ongoing operation and idles BEESOFT to a machine idle state
     */
    protected void doCancel() {
        if (driver.isTransferMode()) {
            driver.stopTransfer();

            if (errorOccurred == false) {
                driver.setTemperature(0);
            }
        } else {
            driver.dispatchCommand("M112", COM.NO_RESPONSE);
        }

        Base.resetPrintingFlags();
        Base.getMainWindow().getButtons().updatePressedStateButton("print");
        Base.cleanDirectoryTempFiles(Base.getAppDataDirectory() + "/3DModels/");
        dispose();
    }

    protected void doResume() {
        bPause.setText(Languager.getTagValue("OptionPaneButtons", "Line12"));
        tInfo2.setText(Languager.getTagValue("Print", "Print_Resuming"));
        tInfo3.setVisible(false);
        pauseThread = new PauseAssistantThread();
        pauseThread.start();
    }

    private void restoreAfterPauseResume() {
        disablePreparingNewFilamentInfo();
        isPaused = false;
        isShutdown = false;
        Base.printPaused = false;
        bOk.setEnabled(true);
        bCancel.setEnabled(true);
        setPrintInfo();
    }

    /**
     * Get file to print either from Printer or from selected GCode
     *
     * @return file to be printed
     */
    private File getPrintFile() {

        if (Base.isPrintingFromGCode == false) {
            //return prt.getGCode();
            return new File(Base.GCODE2PRINTER_PATH);
        } else {
            return new File(preferences.getGcodeToPrint());
        }

    }

    private void updateInformationsByError() {
        tRemaining.setVisible(true);
        tInfo6.setVisible(false);
        vRemaining.setVisible(false);
        vEstimate.setVisible(false);
        tInfo7.setVisible(false);
        tInfo3.setVisible(true);
        tInfo2.setText(Languager.getTagValue("Print", "Print_BuildAborted"));
        tInfo3.setText(Languager.getTagValue("Print", "Print_BuildAborted2"));
        tEstimation.setText(Languager.getTagValue("Print", "Print_BuildAborted3"));
        tRemaining.setText(Languager.getTagValue("Print", "Print_BuildAborted4"));
        jProgressBar1.setVisible(false);
        monitorPrintTimer.stop();
    }

    private void setPrintElements() {
        tInfo2.setText(Languager.getTagValue("Print", "Print_Splash_Info3"));
        tInfo3.setText(Languager.getTagValue("Print", "Print_Splash_Info4"));
        tEstimation.setVisible(true);
        tRemaining.setVisible(true);
        vEstimate.setVisible(true);
        vRemaining.setVisible(true);
        bPause.setVisible(true);
    }

    private void updatePrintTimes(int estimatedTime, int remainingTime) {
        /*
         * Set estimated time
         */
        if (estimatedTime > -1) {
            vEstimate.setIcon(null);
            vEstimate.setText(generateTimeString(estimatedTime));
        }

        /*
         * Set remaining time
         */
        if (remainingTime > -1) {
            vRemaining.setIcon(null);
            vRemaining.setText(generateTimeString(remainingTime));
        }
    }

    private void activateLoadingIcons() {
        ImageIcon loadingIcon = new ImageIcon(
                getClass().getResource("/replicatorg/app/ui/panels/loading.gif")
        );

        vEstimate.setText("");
        vRemaining.setText("");
        vEstimate.setIcon(loadingIcon);
        vRemaining.setIcon(loadingIcon);
    }

    private void setTransferInfo() {
        jProgressBar1.setIndeterminate(false);
        jProgressBar1.setMaximum(100);
        tInfo2.setText(Languager.getTagValue("Print", "Print_Transfering"));
        tEstimation.setText(Languager.getTagValue("Print", "Print_Estimation"));
        vEstimate.setText(estimateTransferTime());
        vEstimate.setVisible(true);
        tInfo6.setVisible(false);
        tInfo7.setVisible(false);
        bCancel.setEnabled(true);
    }

    private void setProcessingInfo() {
        tInfo2.setText(Languager.getTagValue("Print", "Print_Processing"));
        tEstimation.setText(Languager.getTagValue("Print", "Print_Info"));
        tRemaining.setVisible(false);
        vEstimate.setVisible(false);
        vRemaining.setVisible(false);
        tInfo6.setVisible(false);
        tInfo7.setVisible(false);
        tInfo3.setVisible(false);
        bCancel.setEnabled(true);
    }

    private void setHeatingInfo() {
        bPause.setVisible(false);
        tInfo7.setVisible(false);
        tRemaining.setVisible(true);
        tEstimation.setVisible(true);
        vRemaining.setVisible(false);
        vEstimate.setVisible(false);
        bCancel.setEnabled(true);
        tInfo2.setText(Languager.getTagValue("FeedbackLabel", "HeatingMessage"));
        tEstimation.setText(Languager.getTagValue("Print", "Print_Info"));
        tRemaining.setText(Languager.getTagValue("FeedbackLabel", "HeatingMessage2"));
        jProgressBar1.setVisible(true);
        jProgressBar1.setMaximum(temperatureGoal);
        iPrinting.setIcon(null);
    }

    private void resetProgressBar() {
        jProgressBar1.setIndeterminate(false);
        jProgressBar1.setValue(0);
    }

    private void setPrintInfo() {
        jProgressBar1.setVisible(true);
        jProgressBar1.setMaximum(100);
        updatePrintBar(0);
        tInfo2.setText(Languager.getTagValue("Print", "Print_Splash_Info3"));
        tInfo3.setText(Languager.getTagValue("Print", "Print_Splash_Info4"));
        tEstimation.setText(Languager.getTagValue("Print", "Print_Estimation"));
        tRemaining.setText(Languager.getTagValue("Print", "Print_Remaining"));
        tInfo3.setVisible(true);
        tRemaining.setVisible(true);
        tEstimation.setVisible(true);
        vEstimate.setVisible(true);
        vRemaining.setVisible(true);
        bPause.setVisible(true);
    }

    private void setPreparingNewFilamentInfo() {
        tInfo2.setText(Languager.getTagValue("Print", "Print_Pausing"));
        tInfo3.setVisible(false);
        tRemaining.setVisible(false);
        vRemaining.setVisible(false);
        tEstimation.setVisible(false);
        vEstimate.setVisible(false);
        jProgressBar1.setVisible(false);
    }

    private void disablePreparingNewFilamentInfo() {
        tInfo2.setText(Languager.getTagValue("Print", "Print_Splash_Info3"));
        tInfo3.setText(Languager.getTagValue("Print", "Print_Splash_Info4"));
        tInfo3.setVisible(true);
        tEstimation.setVisible(true);
        tRemaining.setVisible(true);
        vEstimate.setVisible(true);
        vRemaining.setVisible(true);
        jProgressBar1.setVisible(true);
        bPause.setText(Languager.getTagValue("OptionPaneButtons", "Line11"));
    }

    private String estimateTransferTime() {
        double gcodeSize = getPrintFile().length() / 1000; //in bytes
        double transferSpeed = Double.valueOf(ProperDefault.get("transferSpeed")); //speed = 42 KB/s
        double estimatedTime = Math.round(gcodeSize / transferSpeed);
        int timeInMinutes = (int) (estimatedTime / 60) + 1;
        return generateTimeString(timeInMinutes);
    }

    private void updateValueOnProgressBar(int temperature) {
        if (temperature > jProgressBar1.getValue()) {
            jProgressBar1.setValue(temperature);
        }
    }

    private void cancelProcess() {
        dispose();
        Base.cleanDirectoryTempFiles(Base.getAppDataDirectory().getAbsolutePath() + "/" + Base.MODELS_FOLDER);
        driver.dispatchCommand("M112", COM.NO_RESPONSE);
        Base.getMainWindow().getButtons().updatePressedStateButton("print");
        //Clears GCode saved on scene
        Base.getMainWindow().getBed().setGcode(new StringBuffer(""));
        Base.getMainWindow().getBed().setGcodeOK(false);
        prt.endGCodeGeneration();
    }

    private void setPrintEnded(int elapsedMinutes) {
        monitorPrintTimer.stop();
        printEnded = true;
        tInfo6.setText("");
        tInfo2.setText(Languager.getTagValue("Print", "Print_BuildFinished"));
        vEstimate.setVisible(false);
        vRemaining.setVisible(false);
        tInfo3.setVisible(false);
        bCancel.setVisible(false);
        bPause.setVisible(false);
        bOk.setVisible(true);
        bUnload.setVisible(true);
        bUnload.setText(Languager.getTagValue("FilamentWizard", "UnloadButton"));
        jProgressBar1.setVisible(false);
        bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_20.png")));
        bOk.setDisabledIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_20.png")));
        bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
        tRemaining.setText(Languager.getTagValue("Print", "Print_Splash_Info5"));
        tEstimation.setText(Languager.getTagValue("Print", "Print_Completion")
                + " " + generateTimeString(elapsedMinutes));
        driver.setTemperature(0);
    }

    private String generateTimeString(int totalMinutes) {
        String temp;
        int hours, minutes;

        hours = totalMinutes / 60;
        minutes = totalMinutes % 60;

        if (hours >= 2) {
            if (minutes != 1) {
                temp = hours + " " + Languager.getTagValue("Print", "PrintHours") + " "
                        + minutes + " " + Languager.getTagValue("Print", "PrintMinutes");
            } else {
                temp = hours + " " + Languager.getTagValue("Print", "PrintHours") + " "
                        + minutes + " " + Languager.getTagValue("Print", "PrintMinute");
            }
        } else if (hours == 1) {
            if (minutes != 1) {
                temp = hours + " " + Languager.getTagValue("Print", "PrintHour") + " "
                        + minutes + " " + Languager.getTagValue("Print", "PrintMinutes");
            } else {
                temp = hours + " " + Languager.getTagValue("Print", "PrintHour") + " "
                        + minutes + " " + Languager.getTagValue("Print", "PrintMinute");
            }
        } else if (minutes != 1) {
            temp = minutes + " " + Languager.getTagValue("Print", "PrintMinutes");
        } else {
            temp = minutes + " " + Languager.getTagValue("Print", "PrintMinute");
        }

        return temp;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator1 = new javax.swing.JSeparator();
        jPanel2 = new javax.swing.JPanel();
        iPrinting = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        tInfo6 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        tRemaining = new javax.swing.JLabel();
        vRemaining = new javax.swing.JLabel();
        tInfo2 = new javax.swing.JLabel();
        tInfo7 = new javax.swing.JLabel();
        tEstimation = new javax.swing.JLabel();
        vEstimate = new javax.swing.JLabel();
        tInfo3 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jPanel3 = new javax.swing.JPanel();
        bCancel = new javax.swing.JLabel();
        bOk = new javax.swing.JLabel();
        bUnload = new javax.swing.JLabel();
        bPause = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        jPanel2.setBackground(new java.awt.Color(248, 248, 248));
        jPanel2.setPreferredSize(new java.awt.Dimension(475, 169));

        iPrinting.setPreferredSize(new java.awt.Dimension(75, 75));

        jPanel7.setBackground(new java.awt.Color(248, 248, 248));
        jPanel7.setPreferredSize(new java.awt.Dimension(360, 142));

        tInfo6.setText("tInfo6");

        jPanel5.setBackground(new java.awt.Color(248, 248, 248));

        tRemaining.setText("tRemaining");

        vRemaining.setText("N/A");

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

        tInfo2.setText("A PROCESSAR... POR FAVOR AGUARDE");

        tInfo7.setText("tInfo7");

        tEstimation.setText("tEstimation");

        vEstimate.setText("N/A");

        tInfo3.setText("A PROCESSAR... POR FAVOR AGUARDE");

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
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel7Layout.createSequentialGroup()
                                .addComponent(tEstimation)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(vEstimate))
                            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 19, Short.MAX_VALUE))
                    .addComponent(tInfo7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tInfo3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addComponent(tInfo2)
                .addGap(5, 5, 5)
                .addComponent(tInfo3)
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tEstimation)
                    .addComponent(vEstimate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tInfo6)
                .addGap(6, 6, 6)
                .addComponent(tInfo7)
                .addContainerGap())
        );

        jProgressBar1.setBackground(new java.awt.Color(186, 186, 186));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 687, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 372, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(iPrinting, javax.swing.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(iPrinting, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4))
        );

        jPanel3.setBackground(new java.awt.Color(255, 203, 5));
        jPanel3.setMinimumSize(new java.awt.Dimension(20, 46));
        jPanel3.setPreferredSize(new java.awt.Dimension(423, 127));

        bCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bCancel.setText("CANCELAR");
        bCancel.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_21.png"))); // NOI18N
        bCancel.setEnabled(false);
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

        bOk.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_14.png"))); // NOI18N
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

        bUnload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bUnload.setText("Unload");
        bUnload.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bUnload.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bUnloadMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bUnloadMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bUnloadMouseEntered(evt);
            }
        });

        bPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bPause.setText("PAUSE");
        bPause.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_21.png"))); // NOI18N
        bPause.setEnabled(false);
        bPause.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bPause.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bPauseMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bPauseMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bPauseMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bPause)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bCancel)
                .addGap(6, 6, 6)
                .addComponent(bOk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bUnload)
                .addGap(12, 12, 12))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bOk)
                    .addComponent(bUnload)
                    .addComponent(bCancel)
                    .addComponent(bPause))
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

    private void bOkMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bOkMouseEntered
        if (printEnded) {
            bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_20.png")));
        } else {
            bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
        }
    }//GEN-LAST:event_bOkMouseEntered

    private void bOkMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bOkMouseExited
        if (printEnded) {
            bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_20.png")));
        } else {
            bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
        }
    }//GEN-LAST:event_bOkMouseExited

    private void bCancelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseEntered
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bCancelMouseEntered

    private void bCancelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseExited
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bCancelMouseExited

    private void bCancelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMousePressed
        final Query cancelPrintQuery;

        if (bCancel.isEnabled()) {
            if (errorOccurred == false) {
                cancelPrintQuery = new Query("CancelPrintText", this::doCancel, null);
                cancelPrintQuery.setVisible(true);
            } else {
                Base.getMainWindow().getButtons().setMessage("is disconnected");
                doCancel();
            }
        }

    }//GEN-LAST:event_bCancelMousePressed

    private void bOkMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bOkMousePressed
        if (bOk.isEnabled()) {
            if (printEnded) {
                if (firstUnloadDone) {
                    bOk.setVisible(false);
                    iPrinting.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "retirar_filamento-07.png")));
                    bUnload.setText(Languager.getTagValue("OptionPaneButtons", "Line6"));
                    lastPanel = true;
                    firstUnloadDone = false;
                    tRemaining.setText(Languager.getTagValue("Print", "Print_Unloaded2"));
                    tInfo6.setVisible(false);
                } else {
                    driver.setTemperature(0);
                    dispose();
                }
            } else if (errorOccurred) {
                driver.setTemperature(0);
                dispose();
                PrintSplashAutonomous p = new PrintSplashAutonomous(preferences);
                p.setVisible(true);
            }
        }
    }//GEN-LAST:event_bOkMousePressed

    private void jLabel11MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel11MouseClicked
    }//GEN-LAST:event_jLabel11MouseClicked

    private void bUnloadMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bUnloadMouseEntered
        bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bUnloadMouseEntered

    private void bUnloadMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bUnloadMouseExited
        bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bUnloadMouseExited

    private void bUnloadMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bUnloadMousePressed
        if (bUnload.isEnabled()) {
            //second next overloads unload button for OK
            if (lastPanel) {
                driver.setTemperature(0);
                dispose();
            } else {           //first time you press unload after print is over    
                if (heatAndUnloadThread != null) {
                    heatAndUnloadThread.kill();
                }

                // just in case printer is in power saving mode
                driver.setTemperature(temperatureGoal);

                heatAndUnloadThread = new HeatAndUnloadThread();
                heatAndUnloadThread.start();
            }
            //} else {             //any time you press unload after the first time
            //    doUnload();
            //}
        }
    }//GEN-LAST:event_bUnloadMousePressed

    private void bPauseMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPauseMouseEntered
        bPause.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bPauseMouseEntered

    private void bPauseMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPauseMouseExited
        bPause.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bPauseMouseExited

    private void bPauseMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPauseMousePressed
        if (bPause.isEnabled()) {
            bPause.setVisible(false);
            bOk.setEnabled(false);
            bCancel.setEnabled(false);

            setPreparingNewFilamentInfo();
            Base.printPaused = true;
            isPaused = true;
            //Pause print
            driver.dispatchCommand("M640");
            driver.setBusy(true);

            EventQueue.invokeLater(() -> {
                while (driver.isBusy() || !model.getMachineReady()) {
                    Base.hiccup(500);
                }
                PauseMenu pause = new PauseMenu();
                dispose();
                pause.setVisible(true);
            });
        }
    }//GEN-LAST:event_bPauseMousePressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bCancel;
    private javax.swing.JLabel bOk;
    private javax.swing.JLabel bPause;
    private javax.swing.JLabel bUnload;
    private javax.swing.JLabel iPrinting;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel tEstimation;
    private javax.swing.JLabel tInfo2;
    private javax.swing.JLabel tInfo3;
    private javax.swing.JLabel tInfo6;
    private javax.swing.JLabel tInfo7;
    private javax.swing.JLabel tRemaining;
    private javax.swing.JLabel vEstimate;
    private javax.swing.JLabel vRemaining;
    // End of variables declaration//GEN-END:variables

    private class PauseAssistantThread extends Thread {

        private boolean stop = false;

        public PauseAssistantThread() {
        }

        public void kill() {
            stop = true;
            this.interrupt();
        }

        @Override
        public void run() {
            driver.setTemperature(temperatureGoal + 5);
            setHeatingInfo();
            resetProgressBar();
            synchronized (mutex) {
                try {
                    temperatureTimer.start();
                    mutex.wait();
                    temperatureTimer.stop();
                } catch (InterruptedException ex) {
                    if (stop) {
                        return;
                    }
                }
            }

            //Resume printing
            driver.dispatchCommand("M643 S" + temperatureGoal, COM.NO_RESPONSE);

            if (isShutdown) {
                activateLoadingIcons();
            }

            while (driver.getMachinePaused()) {
                Base.hiccup(100);
            }

            restoreAfterPauseResume();
        }
    }

    private class PrintingThread extends Thread {

        private boolean stop = false;
        private File gcode = null;

        public PrintingThread() {
            super("Autonomous Thread");
        }

        public void kill() {
            stop = true;
            this.interrupt();
        }

        private void doResume() {
            bPause.setText(Languager.getTagValue("OptionPaneButtons", "Line12"));
            tInfo2.setText(Languager.getTagValue("Print", "Print_Resuming"));
            tInfo3.setVisible(false);
            driver.setTemperature(temperatureGoal + 5);
            setHeatingInfo();
            resetProgressBar();
            synchronized (mutex) {
                try {
                    temperatureTimer.start();
                    mutex.wait();
                    temperatureTimer.stop();
                } catch (InterruptedException ex) {
                    if (stop) {
                        return;
                    }
                }
            }

            //Resume printing
            driver.dispatchCommand("M643 S" + temperatureGoal);

            if (isShutdown) {
                activateLoadingIcons();
            }

            driver.setBusy(true);
            while (driver.isBusy()) {
                Base.hiccup(100);
            }

            restoreAfterPauseResume();
        }

        @Override
        public void run() {

            final PrintEstimator estimator;
            final String headerStr;

            if (alreadyPrinting) {
                Base.writeLog("Autonomous print resumed", this.getClass());
                Base.isPrinting = true;
                activateLoadingIcons();

                if (isShutdown || isPaused) {
                    doResume();
                }

                setPrintInfo();
                setPrintElements();
                monitorPrintTimer.start();
            } else {
                driver.setTemperature(temperatureGoal + 5);
                /**
                 * If not printing directly from GCode, generates it.
                 */
                if (Base.isPrintingFromGCode == false) {
                    try {
                        gcodeGenerator.execute();
                        gcodeGenerator.get();
                        gcode = getPrintFile();
                    } catch (InterruptedException ex) {
                        if (stop) {
                            return;
                        }
                    } catch (ExecutionException ex) {
                        setError();
                        stop = true;
                        return;
                    }
                } else {
                    try {
                        gcode = getPrintFile();
                        gcodeGenerator.execute();
                        gcodeGenerator.get();
                    } catch (InterruptedException ex) {
                        if (stop) {
                            return;
                        }
                    } catch (ExecutionException ex) {
                        // non-critical?
                    }
                }

                estimator = new PrintEstimator(gcode);
                headerStr = "M31 A" + estimator.getEstimatedMinutes() + '\n';
                setTransferInfo();
                if (driver.transferGCode(gcode, headerStr, PrintSplashAutonomous.this)) {
                    // THIS REPEATED HEATING IS DONE JUST IN CASE, BECAUSE OF THE POWER SAVING FEATURE
                    // if the transfer takes a long time, printer may enter in power saving mode
                    // supposed to be fixed in firmware already, but... just in case
                    driver.setTemperature(temperatureGoal + 5);

                    /**
                     * Controls temperature for proper print
                     */
                    setHeatingInfo();
                    resetProgressBar();
                    synchronized (mutex) {
                        try {
                            temperatureTimer.start();
                            mutex.wait();
                            temperatureTimer.stop();
                        } catch (InterruptedException ex) {
                            if (stop) {
                                return;
                            }
                        }
                    }

                    driver.setTemperature(temperatureGoal);

                    /**
                     * Set UI elements
                     */
                    updatePrintTimes(estimator.getEstimatedMinutes(), estimator.getEstimatedMinutes());
                    setPrintInfo();
                    driver.startPrintAutonomous();
                    //activateLoadingIcons();
                    bPause.setEnabled(false);
                    bCancel.setEnabled(false);
                    Base.hiccup(5000);
                    setPrintElements();
                    monitorPrintTimer.start();
                } else {
                    setError();
                }
            }
        }
    }

    private class HeatAndUnloadThread extends Thread {

        private boolean stop = false;

        public HeatAndUnloadThread() {
            super("Heat and unload thread");
        }

        @Override
        public void run() {
            final int temperature;

            bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_21.png")));
            bUnload.setEnabled(false);

            driver.readTemperature();
            temperature = model.currentTool().getExtruderTemperature();

            if (temperature < temperatureGoal - 5) {
                driver.setTemperature(temperatureGoal + 5);
                setHeatingInfo();
                resetProgressBar();
                bOk.setVisible(false);
                bUnload.setVisible(false);
                bCancel.setVisible(true);

                synchronized (mutex) {
                    try {
                        temperatureTimer.start();
                        mutex.wait();
                        temperatureTimer.stop();
                    } catch (InterruptedException ex) {
                        if (stop) {
                            return;
                        }
                    }
                }
                driver.setTemperature(temperatureGoal);
                jProgressBar1.setVisible(false);
            }

            bCancel.setVisible(false);
            tInfo2.setText(Languager.getTagValue("Print", "Unloading_Title"));
            doUnload();
        }

        public void kill() {
            stop = true;
            this.interrupt();
        }

        private void doUnload() {
            if (!driver.isBusy()) {
                bOk.setVisible(false);
                bUnload.setVisible(true);
                jProgressBar1.setVisible(false);
                iPrinting.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "rsz_unload-01.png")));
                tRemaining.setText(Languager.getTagValue("FilamentWizard", "Exchange_Info3"));
                driver.setBusy(true);
                driver.dispatchCommand("M702");
                driver.setCoilText(FilamentControler.NO_FILAMENT);

                while (driver.isBusy()) {
                    Base.hiccup(100);
                }

                bUnload.setVisible(true);
                iPrinting.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "retirar_filamento-01.png")));
                tRemaining.setText(Languager.getTagValue("Print", "Print_Unloaded1"));
                bOk.setVisible(true);
                firstUnloadDone = true;
                bOk.setText(Languager.getTagValue("OptionPaneButtons", "Line7"));
                tInfo6.setText("");
                tInfo2.setText(Languager.getTagValue("Print", "Unload_BuildFinished"));
                tInfo6.setText(Languager.getTagValue("Print", "Print_Unloaded3"));
                bOk.setVisible(true);
                bUnload.setEnabled(true);
                bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
            }
        }

    }

    private class GCodeGenWorker extends SwingWorker<Boolean, Void> {

        @Override
        protected Boolean doInBackground() throws Exception {
            final boolean success;

            Base.writeLog("Estimating printing time...", this.getClass());

            if (Base.isPrintingFromGCode) {
                success = true;
                //success = estimateGCodeFromFile();
            } else if (prt.isReadyToGenerateGCode()) {
                success = prt.generateGCode();
            } else {
                Base.writeLog("generateGCode(): failed GCode generation", this.getClass());
                success = false;
            }

            if (!success) {
                // Error occurred - permissions maybe
                // Cancel print and setting message
                // 5000 ms delay to ensure user reads it
                Base.getMainWindow().showFeedBackMessage("gcodeGeneration");
                Base.hiccup(5000);
                cancelProcess();
                Base.writeLog("Printing estimation failed...", this.getClass());
            } else {
                Base.writeLog("Printing estimation successful...", this.getClass());
            }

            return success;
        }
    }

    private class TemperatureActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            final int temperature;

            driver.readTemperature();
            temperature = model.currentTool().getExtruderTemperature();

            updateValueOnProgressBar(temperature);
            if (temperature >= (temperatureGoal - 1)) {
                synchronized (mutex) {
                    mutex.notifyAll();
                }
            }
        }
    }

    private class MonitorPrintListener implements ActionListener {

        private final MachineModel model = driver.getMachine();
        private int elapsedMinutes;
        private boolean donePrintingFile = false;

        private void finalizePrint() {
            //End print session
            setPrintEnded(elapsedMinutes);
        }

        private void getLinesAndTime() {
            AutonomousData variables;
            int estimatedMinutes, remainingMinutes, currentLines, totalLines;
            double progression;
            String estimatedTime, elapsedTime;

            try {
                driver.getPrintSessionsVariables();
                variables = model.getAutonomousData();

                if (variables == null) {
                    return;
                }

                estimatedTime = variables.getEstimatedTime().toString();
                elapsedTime = variables.getElapsedTime().toString();
                totalLines = Integer.valueOf(variables.getNLines().toString());
                currentLines = Integer.valueOf(variables.getCurrentNLines().toString());

                estimatedMinutes = Integer.parseInt(estimatedTime);
                elapsedMinutes = Integer.valueOf(elapsedTime) / 60000;
                remainingMinutes = estimatedMinutes - elapsedMinutes;

                updatePrintTimes(estimatedMinutes, remainingMinutes);

                if (totalLines > 0) {
                    progression = ((double) currentLines / totalLines) * 100;
                    updatePrintBar((int) progression);
                }
            } catch (NumberFormatException e) {
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final boolean machineFinished;

            if (!driver.isBusy()) {
                if (!isPaused && !isShutdown) {
                    getLinesAndTime();
                }
                bPause.setEnabled(true);
                bCancel.setEnabled(true);
            } else {
                bPause.setEnabled(false);
                bCancel.setEnabled(false);
            }

            // the progress bar is 100% if current lines == total lines. just 
            // one more confirmation before declaring that the print has 
            // finished
            machineFinished = !model.getMachinePrinting() && !driver.isBusy()
                    && !model.getMachinePaused() && !model.getMachineShutdown()
                    && jProgressBar1.getValue() >= 100;

            if (errorOccurred || machineFinished) {
                if (errorOccurred) {
                    updateInformationsByError();
                } else if (machineFinished) {
                    finalizePrint();
                }
            }
        }
    }
}
