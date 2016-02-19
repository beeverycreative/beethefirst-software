package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import replicatorg.app.Languager;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.app.Base;
import replicatorg.app.DoNotSleep;
import pt.beeverycreative.beesoft.filaments.PrintPreferences;
import replicatorg.app.PrintEstimator;
import replicatorg.app.Printer;
import replicatorg.app.ProperDefault;
import replicatorg.app.util.AutonomousData;
import replicatorg.drivers.Driver;
import replicatorg.machine.MachineInterface;
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
    private final MachineModel model = Base.getMachineLoader().getMachineInterface().getModel();
    private final Printer prt;
    private final PrintPreferences preferences;
    private boolean printEnded;
    private final UpdateThread4 ut;
    private final TransferControlThread gcodeGenerator;
    private boolean alreadyPrinting;
    private boolean errorOccured = false;
    private boolean unloadPressed;
    private boolean firstUnloadStep = false;
    private boolean lastPanel;
    private boolean isPaused = false;
    private boolean isShutdown = false;
    private boolean userDecision;
    private int machinePrintingCount = 0;
    protected final int temperatureGoal;

    public PrintSplashAutonomous(boolean printingState, PrintPreferences prefs) {
        super(Base.getMainWindow(), Dialog.ModalityType.MODELESS);
        initComponents();
        setFont();
        setTextLanguage();
        centerOnScreen();
        preferences = prefs;
        prt = new Printer(preferences);
        temperatureGoal = getFilamentTemperature();
        setProgressBarColor();
        printEnded = false;
        bOk.setVisible(false);
        bUnload.setVisible(false);
        bPause.setVisible(false);
        alreadyPrinting = printingState;
        jProgressBar1.setIndeterminate(true);
        enableDrag();
        //addWindowListener(this);
        gcodeGenerator = new TransferControlThread(this);
        ut = new UpdateThread4(this, gcodeGenerator);
//        Base.getMainWindow().setEnabled(false);
//        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());
        this.setName("Autonomous");
        Base.bringAllWindowsToFront();
    }

    public PrintSplashAutonomous(boolean printingState, boolean isPaused, boolean isShutdown, PrintPreferences prefs) {
        super(Base.getMainWindow(), Dialog.ModalityType.MODELESS);
        initComponents();
        setFont();
        setTextLanguage();
        centerOnScreen();
        preferences = prefs;
        prt = new Printer(preferences);
        temperatureGoal = getFilamentTemperature();
        setProgressBarColor();
        printEnded = false;
        bOk.setVisible(false);
        bUnload.setVisible(false);
        bPause.setVisible(false);
        alreadyPrinting = printingState;
        jProgressBar1.setIndeterminate(true);
        enableDrag();
        //addWindowListener(this);
        gcodeGenerator = new TransferControlThread(this);
        ut = new UpdateThread4(this, gcodeGenerator);
//        Base.getMainWindow().setEnabled(false);
//        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());
        this.setName("Autonomous");
        Base.bringAllWindowsToFront();
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
        int fileKey = 1;
        tInfo2.setText(Languager.getTagValue(fileKey, "Print", "Print_Splash_Info2"));
        tInfo3.setText(Languager.getTagValue(fileKey, "Print", "Print_Splash_Info4"));
        tEstimation.setText(Languager.getTagValue(fileKey, "Print", "Print_Estimation"));
        tRemaining.setText(Languager.getTagValue(fileKey, "Print", "Print_Remaining"));
        bCancel.setText(Languager.getTagValue(fileKey, "OptionPaneButtons", "Line3"));
        bOk.setText(Languager.getTagValue(fileKey, "OptionPaneButtons", "Line10"));
        bPause.setText(Languager.getTagValue(fileKey, "OptionPaneButtons", "Line11"));
        bUnload.setText(Languager.getTagValue(fileKey, "OptionPaneButtons", "Line14"));
        setProcessingInfo();
    }

    public void startConditions() {
        if (Base.isPrintingFromGCode) {
            prt.setGCodeFile(new File(preferences.getGcodeToPrint()));
        }
        ut.start();
    }

    private void setProgressBarColor() {
        jProgressBar1.setForeground(new Color(255, 203, 5));
    }

    public void updatePrintBar(int progression) {
        jProgressBar1.setIndeterminate(false);
        jProgressBar1.setValue(progression);
    }

    /**
     * Error variable for the possible situations of failure during transfer.
     * Used to abort UI panel and inform user.
     *
     * @param newError
     */
    public void setError(boolean newError) {
        errorOccured = newError;
        updateInformationsByError();
        jProgressBar1.setIndeterminate(false);
        jProgressBar1.setValue(0);
        //In case of error, next time its necessary to clean File
//        Base.getMainWindow().getBed().setGcodeOK(false);

        // Restart Button
        bOk.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line13"));
        bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_20.png")));
    }

    public boolean isAtErrorState() {
        return errorOccured;
    }

    public boolean isPrinting() {
        return alreadyPrinting;
    }

    public boolean isShutdown() {
        return isShutdown;
    }

    public void setPrintState(boolean state) {
        alreadyPrinting = state;
    }

    /**
     * Cancel ongoing operation and idles BEESOFT to a machine idle state
     */
    protected void doCancel() {
        if (driver.isTransferMode()) {
            // stopTransfer() blocks while the process isn't concluded
            driver.stopTransfer();
        } else {
            driver.dispatchCommand("M112");
            Base.getMainWindow().doStop();
            gcodeGenerator.stop();
            ut.stop();
        }

        userDecision = true;
        Base.resetPrintingFlags();
        Base.getMainWindow().getButtons().updatePressedStateButton("print");
        Base.cleanDirectoryTempFiles(Base.getAppDataDirectory() + "/3DModels/");
        dispose();
        Base.bringAllWindowsToFront();
        Base.getMainWindow().setEnabled(true);
    }

    protected void doResume() {
        bPause.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line12"));
        tInfo2.setText(Languager.getTagValue(1, "Print", "Print_Resuming"));
        tInfo3.setVisible(false);
        PauseAssistantThread pauseThread = new PauseAssistantThread();
        pauseThread.start();
    }

    protected void disableButtons() {
        bOk.setEnabled(false);
        bPause.setVisible(false);
        bCancel.setEnabled(false);
    }

    protected void restoreAfterPauseResume() {
        disablePreparingNewFilamentInfo();
        isPaused = false;
        isShutdown = false;
        Base.printPaused = false;
        bOk.setEnabled(true);
        bCancel.setEnabled(true);
        setPrintInfo();
    }

    /**
     * Get file to print either from Printer or from selected gcode
     *
     * @return file to be printed
     */
    public File getPrintFile() {

        if (Base.isPrintingFromGCode == false) {
            //return prt.getGCode();
            return new File(Base.GCODE2PRINTER_PATH);
        } else {
            return new File(preferences.getGcodeToPrint());
        }

    }

    public void enableSleep() {
        try {

            DoNotSleep ds = new DoNotSleep();
            ds.EnabledSleep();

            Base.writeLog("Sleep started!", this.getClass());

        } catch (Exception ex) {
            Base.writeLog("Error starting Sleep!", this.getClass());
        }
    }

    public void disableSleep() {
        try {

            DoNotSleep ds = new DoNotSleep();
            ds.DisableSleep();

            Base.writeLog("Sleep stoped!", this.getClass());

        } catch (Exception ex) {
            Base.writeLog("Error stoping Sleep!", this.getClass());
        }
    }

    void updateInformationsByError() {
        tRemaining.setVisible(true);
        tInfo6.setVisible(false);
        vRemaining.setVisible(false);
        vEstimate.setVisible(false);
        tInfo7.setVisible(false);
        tInfo3.setVisible(true);
        tInfo2.setText(Languager.getTagValue(1, "Print", "Print_BuildAborted"));
        tInfo3.setText(Languager.getTagValue(1, "Print", "Print_BuildAborted2"));
        tEstimation.setText(Languager.getTagValue(1, "Print", "Print_BuildAborted3"));
        tRemaining.setText(Languager.getTagValue(1, "Print", "Print_BuildAborted4"));
        jProgressBar1.setVisible(false);

    }

    public int getNGCodeLines() {
        return prt.getGCodeNLines();
    }

    private int getFilamentTemperature() {
        return prt.getFilamentTemperature();
    }

    public void setPrintElements() {
        tInfo2.setText(Languager.getTagValue(1, "Print", "Print_Splash_Info3"));
        tInfo3.setText(Languager.getTagValue(1, "Print", "Print_Splash_Info4"));
        tEstimation.setVisible(true);
        tRemaining.setVisible(true);
        vEstimate.setVisible(true);
        vRemaining.setVisible(true);
        bPause.setVisible(true);
    }

    public String generateGCode() {
        if (prt.isReadyToGenerateGCode()) {
            return prt.generateGCode();
        } else {
            Base.writeLog("generateGCode(): failed GCode generation", this.getClass());
            return "-1";
        }
    }

    protected void updatePrintTimes(int estimatedTime, int remainingTime) {
        if (model.getMachinePrinting() == false
                && (isPaused() || isShutdown())) {
            activateLoadingIcons();
            machinePrintingCount = 0;
        } else {
            machinePrintingCount++;

            if (machinePrintingCount < 2 && estimatedTime <= 0) {
                return;
            }

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
    }

    protected void activateLoadingIcons() {
        ImageIcon loadingIcon = new ImageIcon(
                getClass().getResource("/replicatorg/app/ui/panels/loading.gif")
        );

        vEstimate.setText("");
        vRemaining.setText("");
        vEstimate.setIcon(loadingIcon);
        vRemaining.setIcon(loadingIcon);
    }

    public void setTransferInfo() {
        jProgressBar1.setIndeterminate(false);
        tInfo2.setText(Languager.getTagValue(1, "Print", "Print_Transfering"));
        tEstimation.setText(Languager.getTagValue(1, "Print", "Print_Estimation"));
        vEstimate.setText(estimateTransferTime());
        vEstimate.setVisible(true);
        tInfo6.setVisible(false);
        tInfo7.setVisible(false);
    }

    public void setProcessingInfo() {

        tInfo2.setText(Languager.getTagValue(1, "Print", "Print_Processing"));
        tEstimation.setText(Languager.getTagValue(1, "Print", "Print_Info"));
        tRemaining.setVisible(false);
        vEstimate.setVisible(false);
        vRemaining.setVisible(false);
        tInfo6.setVisible(false);
        tInfo7.setVisible(false);
        tInfo3.setVisible(false);
    }

    public void setHeatingInfo() {
        bPause.setVisible(false);
        tInfo7.setVisible(false);
        tRemaining.setVisible(true);
        tEstimation.setVisible(true);
        vRemaining.setVisible(false);
        vEstimate.setVisible(false);
        bCancel.setEnabled(true);
        tInfo2.setText(Languager.getTagValue(1, "FeedbackLabel", "HeatingMessage"));
        tEstimation.setText(Languager.getTagValue(1, "Print", "Print_Info"));
        tRemaining.setText(Languager.getTagValue(1, "FeedbackLabel", "HeatingMessage2"));
        jProgressBar1.setVisible(true);
    }

    public void resetProgressBar() {
        jProgressBar1.setIndeterminate(false);
        jProgressBar1.setValue(0);
    }

    public void setPrintInfo() {
        jProgressBar1.setVisible(true);
        updatePrintBar(0);
        tInfo2.setText(Languager.getTagValue(1, "Print", "Print_Splash_Info3"));
        tInfo3.setText(Languager.getTagValue(1, "Print", "Print_Splash_Info4"));
        tEstimation.setText(Languager.getTagValue(1, "Print", "Print_Estimation"));
        tRemaining.setText(Languager.getTagValue(1, "Print", "Print_Remaining"));
        tInfo3.setVisible(true);
        tRemaining.setVisible(true);
        tEstimation.setVisible(true);
        vEstimate.setVisible(true);
        vRemaining.setVisible(true);
        bPause.setVisible(true);
    }

    private void setPreparingNewFilamentInfo() {
        tInfo2.setText(Languager.getTagValue(1, "Print", "Print_Pausing"));
        tInfo3.setVisible(false);
        tRemaining.setVisible(false);
        vRemaining.setVisible(false);
        tEstimation.setVisible(false);
        vEstimate.setVisible(false);
        jProgressBar1.setVisible(false);
    }

    protected void disablePreparingNewFilamentInfo() {
        tInfo2.setText(Languager.getTagValue(1, "Print", "Print_Splash_Info3"));
        tInfo3.setText(Languager.getTagValue(1, "Print", "Print_Splash_Info4"));
        tInfo3.setVisible(true);
        tEstimation.setVisible(true);
        tRemaining.setVisible(true);
        vEstimate.setVisible(true);
        vRemaining.setVisible(true);
        jProgressBar1.setVisible(true);
        bPause.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line11"));

    }

    public String estimateTransferTime() {
        double gcodeSize = getPrintFile().length() / 1000; //in bytes
        double transferSpeed = Double.valueOf(ProperDefault.get("transferSpeed")); //speed = 42 KB/s
        double estimatedTime = Math.round(gcodeSize / transferSpeed);
        int timeInMinutes = (int) (estimatedTime / 60) + 1;
        return generateTimeString(timeInMinutes);
    }

    /**
     * Runs estimator for selected gcode file
     *
     * @return estimation for given gcode
     */
    public String estimateGCodeFromFile() {

        String line;
        int lines;
        File gcodeFile;

        lines = 0;
        gcodeFile = new File(preferences.getGcodeToPrint());

        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(gcodeFile));

            // read only the first 100 lines searching for M31, no point in
            // searching further than that
            while ((line = reader.readLine()) != null && lines < 100) {
                lines++;
                if (line.contains("M31")) {
                    int indexAtA = line.indexOf('A');
                    return line.substring(indexAtA + 1);
                }
            }
        } catch (FileNotFoundException ex) {
            Base.writeLog("Error estimating from GCode file: file not found", this.getClass());
        } catch (IOException ex) {
            Base.writeLog("Error estimating from GCode file: error while "
                    + "reading GCode", this.getClass());
        }

        PrintEstimator.estimateTime(new File(preferences.getGcodeToPrint()));
        return PrintEstimator.getEstimatedTime();
    }

    public boolean isUnloadPressed() {
        return unloadPressed;
    }

    public void updateTemperatureOnProgressBar(double temperature, double goal) {
        int val = jProgressBar1.getValue();
        int temp_val = (int) (temperature / goal);

        if ((temperature > (int) (jProgressBar1.getValue() * 2)) && (temp_val > (int) (jProgressBar1.getValue()))) {
            val = temp_val;
        }
        jProgressBar1.setValue(val);
    }

    public void cancelProcess() {
        dispose();
        enableSleep();
        Base.bringAllWindowsToFront();
        Base.cleanDirectoryTempFiles(Base.getAppDataDirectory().getAbsolutePath() + "/" + Base.MODELS_FOLDER);
        driver.dispatchCommand("M112", COM.NO_RESPONSE);
        Base.getMainWindow().setEnabled(true);
        Base.getMainWindow().getButtons().updatePressedStateButton("print");
        ut.stop();
        gcodeGenerator.stop();
        //Clears GCode saved on scene
        Base.getMainWindow().getBed().setGcode(new StringBuffer(""));
        Base.getMainWindow().getBed().setGcodeOK(false);
        prt.endGCodeGeneration();
    }

    public void setPrintEnded(int elapsedMinutes) {
        int nPrints;

        printEnded = true;
        unloadPressed = false;
        tInfo6.setText("");
        tInfo2.setText(Languager.getTagValue(1, "Print", "Print_BuildFinished"));
        vEstimate.setVisible(false);
        vRemaining.setVisible(false);
        tInfo3.setVisible(false);
        bCancel.setVisible(false);
        bPause.setVisible(false);
        bOk.setVisible(true);
        bUnload.setVisible(true);
        bUnload.setText(Languager.getTagValue(1, "FilamentWizard", "UnloadButton"));
        jProgressBar1.setVisible(false);
        bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_20.png")));
        bOk.setDisabledIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_20.png")));
        bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
        tRemaining.setText(Languager.getTagValue(1, "Print", "Print_Splash_Info5"));

        nPrints = Integer.valueOf(ProperDefault.get("nTotalPrints")) + 1;
        ProperDefault.put("nTotalPrints", String.valueOf(nPrints));

        tEstimation.setText(Languager.getTagValue(1, "Print", "Print_Completion")
                + " " + generateTimeString(elapsedMinutes));

    }

    private String generateTimeString(int totalMinutes) {
        String temp;
        int hours, minutes;

        hours = totalMinutes / 60;
        minutes = totalMinutes % 60;

        if (hours >= 2) {
            if (minutes != 1) {
                temp = hours + " " + Languager.getTagValue(1, "Print", "PrintHours") + " "
                        + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinutes");
            } else {
                temp = hours + " " + Languager.getTagValue(1, "Print", "PrintHours") + " "
                        + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinute");
            }
        } else if (hours == 1) {
            if (minutes != 1) {
                temp = hours + " " + Languager.getTagValue(1, "Print", "PrintHour") + " "
                        + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinutes");
            } else {
                temp = hours + " " + Languager.getTagValue(1, "Print", "PrintHour") + " "
                        + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinute");
            }
        } else {
            if (minutes != 1) {
                temp = minutes + " " + Languager.getTagValue(1, "Print", "PrintMinutes");
            } else {
                temp = minutes + " " + Languager.getTagValue(1, "Print", "PrintMinute");
            }
        }

        return temp;
    }

    public void startUnload() {
        jProgressBar1.setVisible(false);
        iPrinting.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "rsz_unload-01.png")));
        tRemaining.setText(Languager.getTagValue(1, "FilamentWizard", "Exchange_Info3"));

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {

                if (!driver.isBusy()) {
                    unloadPressed = true;
                    driver.setBusy(true);
                    driver.dispatchCommand("M702");

                    bUnload.setVisible(true);
                    iPrinting.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "retirar_filamento-01.png")));
                    driver.setCoilText("none");
                    tRemaining.setText(Languager.getTagValue(1, "Print", "Print_Unloaded1"));
                    bOk.setVisible(true);
                    firstUnloadStep = true;
                    bOk.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line7"));
                    tInfo6.setText("");
                    tInfo2.setText(Languager.getTagValue(1, "Print", "Unload_BuildFinished"));
                    tInfo6.setText(Languager.getTagValue(1, "Print", "Print_Unloaded3"));
                    unloadPressed = false;
                    bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                }

            }
        });
    }

    private void disposePanel() {
        dispose();
        ut.stop();
        gcodeGenerator.stop();
        Base.getMainWindow().setEnabled(true);
        Base.isPrinting = false;
        enableSleep();
        driver.setTemperature(0);
        Base.getMainWindow().getButtons().updatePressedStateButton("print");
        Base.cleanDirectoryTempFiles(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/");
        Base.bringAllWindowsToFront();
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean waitForDecision() {
        return userDecision;
    }

    protected boolean evaluateTemperature() {
        final double temperature;
        final boolean achieved;

        driver.readTemperature();
        temperature = driver.getTemperature();
        achieved = temperature >= (temperatureGoal - 1);

        Runnable thread = new Runnable() {
            @Override
            public void run() {
                if (temperature >= (temperatureGoal - 1)) {
                    updateTemperatureOnProgressBar(100, temperatureGoal / 100);
                    Base.writeLog("Temperature " + temperatureGoal + " achieved", this.getClass());
                } else {
                    updateTemperatureOnProgressBar(temperature, temperatureGoal / 100);
                }
            }
        };

        thread.run();
        return achieved;
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
                        .addGap(0, 32, Short.MAX_VALUE))
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

        bPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bPause.setText("PAUSE");
        bPause.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_21.png"))); // NOI18N
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
        CancelPrint cancelPanel = new CancelPrint(this);
        cancelPanel.setVisible(true);
    }//GEN-LAST:event_bCancelMousePressed

    private void bOkMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bOkMousePressed
        if (bOk.isEnabled()) {
            if (printEnded) {
                if (firstUnloadStep) {
                    bOk.setVisible(false);
                    iPrinting.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "retirar_filamento-07.png")));
                    bUnload.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line6"));
                    lastPanel = true;
                    firstUnloadStep = false;
                    tRemaining.setText(Languager.getTagValue(1, "Print", "Print_Unloaded2"));
                    tInfo6.setVisible(false);

                } else {
                    disposePanel();
                }
            } else if (errorOccured) {
                dispose();
                PrintSplashAutonomous p = new PrintSplashAutonomous(false, preferences);
                p.setVisible(true);
                p.startConditions();
            }
        }
    }//GEN-LAST:event_bOkMousePressed

    private void jLabel11MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel11MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel11MouseClicked

    private void bUnloadMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bUnloadMouseEntered
        if (!unloadPressed) {
            bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
        }
    }//GEN-LAST:event_bUnloadMouseEntered

    private void bUnloadMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bUnloadMouseExited
        if (!unloadPressed) {
            bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
        }
    }//GEN-LAST:event_bUnloadMouseExited

    private void bUnloadMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bUnloadMousePressed
        if (bUnload.isEnabled()) {

            //second next overloads unload button for OK
            if (lastPanel) {
                disposePanel();
            }

            //first time you press unload after print is over
            if (printEnded && unloadPressed == false && firstUnloadStep == false) {
                unloadPressed = true;

                driver.readTemperature();
                bOk.setVisible(false);
                bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_21.png")));
                iPrinting.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "retirar_filamento-02.png")));
                tInfo2.setText(Languager.getTagValue(1, "Print", "Unloading_Title"));
                tRemaining.setText(Languager.getTagValue(1, "Print", "Print_Unloading"));
//            jPanel1.setVisible(false);
                jProgressBar1.setVisible(true);
                jProgressBar1.setValue(0);
                return;
            } // no need for else

            //any time you press unload after the first time
            if (printEnded && unloadPressed == false && firstUnloadStep) {
                unloadPressed = true;
                bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_21.png")));
                bOk.setEnabled(false);
                startUnload();
                bOk.setEnabled(true);
                return;
            } // no need for else

            if (printEnded == false) {
                doCancel();
                return;
            } // no need for else
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
            final PrintSplashAutonomous parent = this;

            bPause.setVisible(false);
            bOk.setEnabled(false);
            bCancel.setEnabled(false);

            setPreparingNewFilamentInfo();
            Base.printPaused = true;
            isPaused = true;
            //Pause print
            driver.dispatchCommand("M640", COM.NO_RESPONSE);
            //Disable power saving to avoid temperature lowering down
            Base.turnOnPowerSaving(false);

            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {

                    while (driver.getMachineReady() == false) {
                        Base.hiccup(500);
                    }

                    driver.setBusy(false);
                    bPause.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line12"));

                    PauseMenu pause = new PauseMenu(parent);
                    pause.setVisible(true);

                    //setPauseElements();
                    //bPause.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                }
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

    class PauseAssistantThread extends Thread {


        public PauseAssistantThread() {
        }

        @Override
        public void run() {
            boolean temperatureAchieved = false;

            driver.setTemperatureBlocking(temperatureGoal + 5);
            setHeatingInfo();

            resetProgressBar();
            while (temperatureAchieved == false) {
                temperatureAchieved = evaluateTemperature();
                Base.hiccup(3000);
            }

            //Resume printing
            driver.dispatchCommand("M643 S" + temperatureGoal, COM.NO_RESPONSE);

            if (isShutdown()) {
                activateLoadingIcons();
            }

            while (driver.getMachinePaused()) {
                Base.hiccup(100);
            }

            restoreAfterPauseResume();
        }
    }

}

class UpdateThread4 extends Thread {

    private final PrintSplashAutonomous window;
    private final Driver driver = Base.getMainWindow().getMachineInterface().getDriver();
    private final TransferControlThread gcodeGenerator;
    private static final String ERROR = "error";
    private static final MachineInterface machine = Base.getMachineLoader().getMachineInterface();
    private boolean finished = false;
    private BufferedReader reader = null;
    private File gcode = null;
    private int elapsedMinutes = 0;

    public UpdateThread4(PrintSplashAutonomous w, TransferControlThread gcodeGen) {
        super("Autonomous Thread");
        this.window = w;
        this.gcodeGenerator = gcodeGen;
    }

    private void transferGCode() {
        try {

            window.setTransferInfo();

            try {
                reader = new BufferedReader(new FileReader(gcode));
            } catch (FileNotFoundException ex) {
                Base.writeLog("Can't read gCode file to print in autonomous mode", this.getClass());
            }

            //Calculate number of lines of GCode
            //totalLines = window.getNGCodeLines();
            // Transfer GCode
            if (driver.gcodeTransfer(gcode, window).toLowerCase().contains(ERROR)) {
                window.setError(true);
                reader.close();
                this.stop();
            }
            // no need for else

            reader.close();

        } catch (IOException ex) {
            Logger.getLogger(UpdateThread4.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private AutonomousData getAutonomousData() {
        try {
            driver.getPrintSessionsVariables();
            return machine.getAutonomousData(); // threads wait if value isn't available yet
        } catch (InterruptedException ex) {
            Logger.getLogger(PrintSplashAutonomous.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private void monitorPrintFromSDCard() {

        boolean machineFinished;
        MachineModel machineModel;

        machineModel = machine.getModel();
        window.setPrintElements();

        while (!window.isAtErrorState()) {

            if (!window.isPaused() && !window.isShutdown()) {
                getLinesAndTime();
            }

            String status = machineModel.getLastStatusString();

            machineFinished = machineModel.getMachineReady()
                    && machineModel.getMachinePaused() == false
                    && machineModel.getMachineShutdown() == false
                    && status.contains("W:") == false;             // W:Waiting4File

            if (machineFinished) {
                finished = true;
                break;
            }

            Base.hiccup(100);
            driver.readTemperature();

            Base.hiccup(3000);
        }
    }

    private void finalizePrint() {
        //End print session
        Base.setPrintEnded(true);
        //Read build time
//        AutonomousData variables = driver.getPrintSessionsVariables();
//        long dur = Long.parseLong(variables.getElapsedTime().toString());
        window.setPrintEnded(elapsedMinutes);

    }

    private void getLinesAndTime() {
        AutonomousData variables;
        int estimatedMinutes, remainingMinutes, currentLines, totalLines;
        double progression;
        String estimatedTime, elapsedTime;

        try {
            variables = getAutonomousData();
            //window.resetProgressBar();

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

            window.updatePrintTimes(estimatedMinutes, remainingMinutes);

            if (totalLines > 0) {
                progression = ((double) currentLines / totalLines) * 100;
                window.updatePrintBar((int) progression);
            }
        } catch (NumberFormatException e) {

        }
    }

    @Override
    public void run() {

        /**
         * If already printing when BEESOFT opens
         */
        if (window.isPrinting()) {

            if (window.isPaused()) {
                machine.stopwatch();
            }

            Base.writeLog("Autonomous print resumed", this.getClass());
            Base.isPrinting = true;

            if (window.isShutdown()) {
                window.disableButtons();
                ShutdownMenu shutdown = new ShutdownMenu(window);
                shutdown.setVisible(true);
            } else if (window.isPaused()) {
                window.disableButtons();
                PauseMenu pause = new PauseMenu(window);
                pause.setVisible(true);
            }

            window.setPrintInfo();
            window.setPrintElements();
            monitorPrintFromSDCard();
        } else { // First run in Autonomous mode

            /**
             * Heat
             */
            driver.setTemperatureBlocking(window.temperatureGoal + 5);

            /**
             * If not printing directly from gcode, generates it.
             */
            if (Base.isPrintingFromGCode == false) {
                /**
                 * Generate GCode
                 */
                gcodeGenerator.start();
                boolean gcodeDone = false;

                while (gcodeDone == false) {
                    try {
                        Thread.sleep(250, 0);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(UpdateThread4.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    gcodeDone = gcodeGenerator.getGCodeDone();
                }

                /**
                 * Free JVM
                 */
                gcodeGenerator.stop();

                gcode = window.getPrintFile();
            } else {
                gcode = window.getPrintFile();

                /**
                 * Estimates GCode printing time
                 */
                gcodeGenerator.start();
                boolean gcodeDone = false;

                while (gcodeDone == false) {
                    Base.hiccup(3000);
                    gcodeDone = gcodeGenerator.getGCodeDone();
                }

                /**
                 * Free JVM
                 */
                gcodeGenerator.stop();
            }

            // THIS REPEATED HEATING IS DONE JUST IN CASE, BECAUSE OF THE POWER SAVING FEATURE
            driver.setTemperatureBlocking(window.temperatureGoal + 5);
            transferGCode();
            driver.setTemperatureBlocking(window.temperatureGoal + 5);

            /**
             * Controls temperature for proper print
             */
            window.setHeatingInfo();
            boolean temperatureAchieved = false;
            Base.getMainWindow().getButtons().blockModelsButton(false);

            window.resetProgressBar();
            while (temperatureAchieved == false) {
                temperatureAchieved = window.evaluateTemperature();
                Base.hiccup(3000);
            }

            /**
             * Set UI elements
             */
            window.setPrintInfo();

            driver.startPrintAutonomous();
            window.activateLoadingIcons();

            monitorPrintFromSDCard();

            /**
             * Clean temp files created - gcode
             */
            Base.cleanDirectoryTempFiles(Base.getAppDataDirectory().getAbsolutePath() + "/" + Base.MODELS_FOLDER + "/");

            if (window.isAtErrorState()) {
                //Inform user that an error happened
                // USB Disconnect is handled by the driver
                window.updateInformationsByError();
            }

        }

        if (finished) {
            finalizePrint();
        }

        while (true) {
            boolean tempReached;

            if (window.isUnloadPressed()) {
                driver.setTemperature(window.temperatureGoal + 5);
                tempReached = window.evaluateTemperature();
                if (tempReached) {
                    window.startUnload();
                    break;
                }
            }

            Base.hiccup(3000);
        }

    }
}

class TransferControlThread extends Thread {

    PrintSplashAutonomous window;
    double estimatedTime = 0;
    boolean gCodeDone = false, stop = false;

    public TransferControlThread(PrintSplashAutonomous w) {
        super("Autonomous gcode generation Thread");
        window = w;
    }

    public int getGenerationTime() {
        return (int) estimatedTime;
    }

    public boolean getGCodeDone() {
        return gCodeDone;
    }

    public void setGCodeDone(boolean state) {
        gCodeDone = state;
    }

    @Override
    public void run() {
        gCodeDone = estimate(Base.isPrintingFromGCode);
    }

    private boolean estimate(boolean printingFromGCode) {

        String assumedTime;

        Base.writeLog("Estimating printing time...", this.getClass());

        if (printingFromGCode) {
            assumedTime = window.estimateGCodeFromFile();
        } else {
            assumedTime = window.generateGCode();
        }

        if (assumedTime.equals("-1")) {
            try {
                // Error occurred - permissions maybe
                // Cancel print and setting message
                // 5000 ms delay to ensure user reads it

                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(TransferControlThread.class.getName()).log(Level.SEVERE, null, ex);
            }

            Base.getMainWindow().showFeedBackMessage("gcodeGeneration");

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(TransferControlThread.class.getName()).log(Level.SEVERE, null, ex);
            }

            window.cancelProcess();
            Base.writeLog("Printing estimation failed...", this.getClass());
            return false;
        } else {
            if (assumedTime.contains(":")) {
                String[] timeValues = assumedTime.split(":");
                estimatedTime = Integer.valueOf(timeValues[0]) * 60 + Integer.valueOf(timeValues[1]);
            } else {
                estimatedTime = Integer.valueOf(assumedTime);
            }

            Base.writeLog("Printing estimation successful...", this.getClass());
            return true;
        }
    }
}
