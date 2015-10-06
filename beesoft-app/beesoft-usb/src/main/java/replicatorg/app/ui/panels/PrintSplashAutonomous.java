package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import replicatorg.app.Languager;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.app.Base;
import replicatorg.app.DoNotSleep;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
import pt.beeverycreative.beesoft.filaments.PrintPreferences;
import replicatorg.app.PrintEstimator;
import replicatorg.app.Printer;
import replicatorg.app.ProperDefault;
import replicatorg.app.util.AutonomousData;
import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;
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
public class PrintSplashAutonomous extends BaseDialog implements WindowListener {

    private final Printer prt;
    private final PrintPreferences preferences;
    private boolean printEnded;
    private double startTimeMillis;
    private double startTimeMillis2;
    private final MachineInterface machine;
    private final UpdateThread4 ut;
    private final TransferControlThread gcodeGenerator;
    private int progression;
    private static final String FORMAT = "%2d:%2d";
    private int remainingTime = 0;
    private boolean alreadyPrinting;
    private boolean errorOccured = false;
    private boolean unloadPressed;
    private boolean firstUnloadStep = false;
    private boolean lastPanel;
    private boolean isPaused = false;
    private Point5d pausePos;
    private boolean isShutdown = false;
    private boolean userDecision;

    public PrintSplashAutonomous(boolean printingState, PrintPreferences prefs) {
        super(Base.getMainWindow(), Dialog.ModalityType.MODELESS);
        initComponents();
        setFont();
        setTextLanguage();
        centerOnScreen();
        preferences = prefs;
        prt = new Printer(preferences);
        machine = Base.getMachineLoader().getMachineInterface();
        setProgressBarColor();
        printEnded = false;
        bOk.setVisible(false);
        bUnload.setVisible(false);
        bPause.setVisible(false);
        alreadyPrinting = printingState;
        jProgressBar1.setIndeterminate(true);
        enableDrag();
        addWindowListener(this);
        gcodeGenerator = new TransferControlThread(this);
        ut = new UpdateThread4(this, gcodeGenerator);
        Base.systemThreads.add(ut);
//        Base.getMainWindow().setEnabled(false);
        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());
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
        machine = Base.getMachineLoader().getMachineInterface();
        setProgressBarColor();
        printEnded = false;
        bOk.setVisible(false);
        bUnload.setVisible(false);
        bPause.setVisible(false);
        alreadyPrinting = printingState;
        jProgressBar1.setIndeterminate(true);
        enableDrag();
        addWindowListener(this);
        gcodeGenerator = new TransferControlThread(this);
        ut = new UpdateThread4(this, gcodeGenerator);
        Base.systemThreads.add(ut);
//        Base.getMainWindow().setEnabled(false);
        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());
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

    public void setProgression(int prog) {
        progression = prog;
    }

    private void setProgressBarColor() {
        jProgressBar1.setForeground(new Color(255, 203, 5));
    }

    public void updatePrintBar(double progression) {
        jProgressBar1.setIndeterminate(false);
        final int val = (int) (progression);
        jProgressBar1.setValue(val);

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
        bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
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

    /**
     * Cancel ongoing operation and idles BEESOFT to a machine idle state
     */
    protected void doCancel() {
        //machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));    // not sure if necessary
        if (machine.getDriver().isTransferMode()) {
            // stopTransfer() blocks while the process isn't concluded
            machine.getDriver().stopTransfer();
        } else {
            machine.killSwitch();
            machine.runCommand(new replicatorg.drivers.commands.EmergencyStop());
            machine.runCommand(new replicatorg.drivers.commands.ReloadConfig());
            machine.runCommand(new replicatorg.drivers.commands.SendHome("Z"));
            machine.runCommand(new replicatorg.drivers.commands.SendHome("XY"));

            Base.getMainWindow().doStop();

            // I KNOW IT'S DEPRECATED
            // .interrupt() doesn't work
            gcodeGenerator.stop();
            ut.stop();
        }

        userDecision = true;
        machine.resumewatch();
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

        final double colorRatio = FilamentControler.getColorRatio(
                machine.getModel().getCoilText(),
                machine.getModel().getResolution(),
                machine.getDriver().getConnectedDevice().filamentCode()
        );
        final double filamentTemperature = getFilamentTemperature();

        PauseAssistantThread pauseThread = new PauseAssistantThread(colorRatio,
                filamentTemperature, this, machine);

        pauseThread.start();
    }

    protected void disableButtons() {
        bOk.setEnabled(false);
        bPause.setEnabled(false);
    }

    protected void restoreAfterPauseResume() {
        disablePreparingNewFilamentInfo();
        isPaused = false;
        isShutdown = false;
        Base.printPaused = false;
        machine.resumewatch();
        machine.unpause();
        bOk.setEnabled(true);
        bPause.setEnabled(true);
    }

    protected void doShutdown() {
        //machine.runCommand(new replicatorg.drivers.commands.Shutdown());
        /*else {
         // resume from shutdown
         machine.getDriver().dispatchCommand("M21", COM.DEFAULT);
         machine.getDriver().dispatchCommand("M35", COM.DEFAULT);
         pausePos = machine.getDriver().getShutdownPosition();
         userDecision = true;
         if (shutdownFromDisconnect == false) {
         //                    resetProgressBar();
         //                    setHeatingInfo();
         bShutdown.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_21.png")));
         jProgressBar1.setVisible(false);
         resumeFromShutdown();
         }
         }
         }*/
    }

    /**
     * Get file to print either from Printer or from selected gcode
     *
     * @return file to be printed
     */
    public File getPrintFile() {

        if (Base.isPrintingFromGCode == false) {
            //return prt.getGCode();
            return new File(prt.getGCode().getPath() + "_modified");
        } else {
            return new File(preferences.getGcodeToPrint());
        }

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

    void updateInformationsByError() {
        tRemaining.setVisible(true);
        tInfo6.setVisible(false);
        vRemaining.setVisible(false);
        vEstimation.setVisible(false);
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

    protected AutonomousData getAutonomousData() {
        try {
            machine.runCommand(new replicatorg.drivers.commands.GetStandaloneVariables());
            return machine.getAutonomousData();
        } catch (InterruptedException ex) {
            Logger.getLogger(PrintSplashAutonomous.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    protected double getFilamentTemperature() {
        return prt.getFilamentTemperature();
    }

    public void startPrintCounter() {
        startTimeMillis = System.currentTimeMillis();
        startTimeMillis2 = System.currentTimeMillis();
    }

    public void setPrintElements() {
        tInfo2.setText(Languager.getTagValue(1, "Print", "Print_Splash_Info3"));
        tInfo3.setText(Languager.getTagValue(1, "Print", "Print_Splash_Info4"));
        tEstimation.setVisible(true);
        tRemaining.setVisible(true);
        vEstimation.setVisible(true);
        vRemaining.setVisible(true);
        bPause.setVisible(true);
    }

    public String printTime(boolean autonomous, long startMillis) {

        if (!autonomous) {
            return String.valueOf((int) (Math.ceil((System.currentTimeMillis() - startTimeMillis) / 1000) / 60) + 1);
        }

        return String.valueOf((int) (Math.ceil((startMillis) / 1000) / 60) + 1);
    }

    public String generateGCode() {
        return prt.generateGCode();
    }

    public void updatePrintEstimation(String printEstimation, boolean cutout) {
        String[] duration = null;
        String textE = "";
        String textR = "";

        if (!printEstimation.contains("NA")) {
            if (printEstimation.contains(":")) {
                duration = printEstimation.split(":");
                int hours = Integer.valueOf(duration[0]);
                int minutes = Integer.valueOf(duration[1]);

                remainingTime = hours * 60 + minutes;

                if (hours > 1) {
                    if (cutout) {
                        textE = hours
                                + " " + Languager.getTagValue(1, "Print", "PrintHours")
                                + " " + minutes
                                + " " + Languager.getTagValue(1, "Print", "PrintMinutes");
                        textR = hours
                                + " " + Languager.getTagValue(1, "Print", "PrintHours")
                                + " " + minutes
                                + " " + Languager.getTagValue(1, "Print", "PrintMinutes");

                        vEstimation.setText(textR);
                    } else {
                        textR = hours
                                + " " + Languager.getTagValue(1, "Print", "PrintHours")
                                + " " + minutes
                                + " " + Languager.getTagValue(1, "Print", "PrintMinutes");

                    }
                } else {
                    if (cutout) {
                        textE = hours
                                + " " + Languager.getTagValue(1, "Print", "PrintHour")
                                + " " + minutes
                                + " " + Languager.getTagValue(1, "Print", "PrintMinutes");

                        vEstimation.setText(textE);
                    } else {
                        textR = hours
                                + " " + Languager.getTagValue(1, "Print", "PrintHour")
                                + " " + minutes
                                + " " + Languager.getTagValue(1, "Print", "PrintMinutes");

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
                    textE = printEstimation
                            + " " + Languager.getTagValue(1, "Print", "PrintMinutes");
                    if (remainingTime > 60) {
                        if (hours == 1) {
                            textR = hours
                                    + " " + Languager.getTagValue(1, "Print", "PrintHour")
                                    + " " + minutes
                                    + " " + Languager.getTagValue(1, "Print", "PrintMinutes");
                        } else {
                            textR = hours
                                    + " " + Languager.getTagValue(1, "Print", "PrintHours")
                                    + " " + minutes
                                    + " " + Languager.getTagValue(1, "Print", "PrintMinutes");
                        }

                    } else if (remainingTime == -100000) {
                        //In case PrintEstimator fails
                        textE = "";
                        vEstimation.setText("");
                        tRemaining.setText("");
                        vEstimation.setText("");
                        vRemaining.setText("");
                        tInfo6.setText("");
                        tInfo7.setText("");
                        tEstimation.setText(Languager.getTagValue(1, "Print", "Print_EstimatorError"));
                        return;
                    } else {
                        textR = remainingTime
                                + " " + Languager.getTagValue(1, "Print", "PrintMinutes");
                    }
                    vEstimation.setText(textR);
                } else {
                    if (remainingTime > 60) {
                        textR = hours
                                + " " + Languager.getTagValue(1, "Print", "PrintHours")
                                + " " + minutes
                                + " " + Languager.getTagValue(1, "Print", "PrintMinutes");
                    }
                    if (remainingTime > 1 && remainingTime <= 60) {
                        textR = remainingTime
                                + " " + Languager.getTagValue(1, "Print", "PrintMinutes");
                    }
                    if (remainingTime == 1) {
                        textR = remainingTime
                                + " " + Languager.getTagValue(1, "Print", "PrintMinute");
                    }
                    if (remainingTime == -100000) {
                        //In case PrintEstimator fails, dont appear 2min
                        textE = "";
                        vEstimation.setText("");
                        tRemaining.setText("");
                        vEstimation.setText("");
                        vRemaining.setText("");
                        tEstimation.setText(Languager.getTagValue(1, "Print", "Print_EstimatorError"));
                        return;
                    }

                    if (remainingTime < 1) {
                        tRemaining.setText(Languager.getTagValue(1, "Print", "Print_Info2"));
                        vRemaining.setText("");
                        return;
                    }

                    // no need for else. Parse is being made before calling thin method
                    vRemaining.setText(textR);
                }
            }
        } else {
            remainingTime = 00;
        }

    }

    public void setTransferInfo() {
        jProgressBar1.setIndeterminate(false);
        tInfo2.setText(Languager.getTagValue(1, "Print", "Print_Transfering"));
        tEstimation.setText(Languager.getTagValue(1, "Print", "Print_Estimation"));
        vEstimation.setText(estimateTransferTime());
        vEstimation.setVisible(true);
        tInfo6.setVisible(false);
        tInfo7.setVisible(false);
    }

    public void setProcessingInfo() {

        tInfo2.setText(Languager.getTagValue(1, "Print", "Print_Processing"));
        tEstimation.setText(Languager.getTagValue(1, "Print", "Print_Info"));
        tRemaining.setVisible(false);
        vEstimation.setVisible(false);
        vRemaining.setVisible(false);
        tInfo6.setVisible(false);
        tInfo7.setVisible(false);
        tInfo3.setVisible(false);
    }

    public void setHeatingInfo() {
        tInfo7.setVisible(false);
        tRemaining.setVisible(true);
        tEstimation.setVisible(true);
        vRemaining.setVisible(false);
        vEstimation.setVisible(false);
        tInfo2.setText(Languager.getTagValue(1, "Print", "Print_Processing"));
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
        vEstimation.setVisible(true);
        vRemaining.setVisible(true);
        bPause.setVisible(false);
    }

    private void setPreparingNewFilamentInfo() {
        tInfo2.setText(Languager.getTagValue(1, "Print", "Print_Pausing"));
//        tInfo3.setText(Languager.getTagValue("Print", "Print_Info"));
        tInfo3.setVisible(false);
        tRemaining.setVisible(false);
        vRemaining.setVisible(false);
        tEstimation.setVisible(false);
        vEstimation.setVisible(false);
        jProgressBar1.setVisible(false);
    }

    private void setPreparingShutdown() {
        tInfo2.setText(Languager.getTagValue(1, "Print", "Print_Processing"));
//        tInfo3.setText(Languager.getTagValue("Print", "Print_Info"));
        tInfo3.setVisible(false);
        tRemaining.setVisible(false);
        vRemaining.setVisible(false);
        tEstimation.setVisible(false);
        vEstimation.setVisible(false);
        jProgressBar1.setVisible(false);
    }

    protected void disablePreparingNewFilamentInfo() {
        tInfo2.setText(Languager.getTagValue(1, "Print", "Print_Splash_Info3"));
        tInfo3.setText(Languager.getTagValue(1, "Print", "Print_Splash_Info4"));
        tInfo3.setVisible(true);
        tEstimation.setVisible(true);
        tRemaining.setVisible(true);
        vEstimation.setVisible(true);
        vRemaining.setVisible(true);
        jProgressBar1.setVisible(true);
        bPause.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line11"));

    }

    public String estimateTransferTime() {
        double gcodeSize = getPrintFile().length() / 1000; //in bytes
        double transferSpeed = Double.valueOf(ProperDefault.get("transferSpeed")); //speed = 42 KB/s
        double estimatedTime = Math.round(gcodeSize / transferSpeed);
        int timeInMinutes = (int) (estimatedTime / 60) + 1;
        return buildTimeEstimationString(String.valueOf(timeInMinutes));
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
                    int indexAtL = line.indexOf('L');
                    return line.substring(indexAtA + 1, indexAtL - 1);
                }
            }
        } catch (FileNotFoundException ex) {
            Base.writeLog("Error estimating from GCode file: file not found");
        } catch (IOException ex) {
            Base.writeLog("Error estimating from GCode file: error while "
                    + "reading GCode");
        }

        PrintEstimator.estimateTime(new File(preferences.getGcodeToPrint()));
        return PrintEstimator.getEstimatedTime();
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
            } else if (duration == 1) {
                text = (" " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinute"));
            } else {
                text = (" " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinutes"));
            }
        } else {
            text = (durT);
        }

        return text;

    }

    public int updateTimeElapsed() {
        int nLines = -1;
        boolean noEstimationAvailable = false;

        int elapsedTime = (int) (Math.ceil((System.currentTimeMillis() - startTimeMillis2) / 1000));

        // Case noEstimation from Estimator
        // Launches updatePrintEstimation to handle situation\
        // -100000 - error code
        if (remainingTime == -100000) {
            updatePrintEstimation(String.valueOf(remainingTime), false);
            noEstimationAvailable = true;
        }

        nLines = pollCurrentLines();

        if (elapsedTime > 60) {
            // Not able to estimate print time
            // 00 - error code
            if (remainingTime == 00) {
                tRemaining.setText(Languager.getTagValue(1, "Print", "Print_Info"));
                vRemaining.setVisible(false);
            } else {
                // Subtract one minute to remaining time
                remainingTime -= 1;

                if (remainingTime > 0) {
                    updatePrintEstimation(String.valueOf(remainingTime), false);
                } else {
                    if (noEstimationAvailable == false) {
                        tRemaining.setText(Languager.getTagValue(1, "Print", "Print_Info2"));
                        vRemaining.setVisible(false);
                    }
                }
            }
            startTimeMillis2 = System.currentTimeMillis();
        }

        if (nLines > 0) {
            return nLines;
        }

        return -1;
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

    private void terminateCura() {
        prt.endGCodeGeneration();
    }

    public void cancelProcess() {
        dispose();
        enableSleep();
        Base.bringAllWindowsToFront();
        Base.cleanDirectoryTempFiles(Base.getAppDataDirectory().getAbsolutePath() + "/" + Base.MODELS_FOLDER);
        Base.getMainWindow().handleStop();
        Base.getMainWindow().setEnabled(true);
        Base.getMainWindow().getButtons().updatePressedStateButton("print");
        Base.getMainWindow().getMachine().runCommand(new replicatorg.drivers.commands.SetTemperature(0));
        ut.stop();
        gcodeGenerator.stop();
        //Clears GCode saved on scene
        Base.getMainWindow().getBed().setGcode(new StringBuffer(""));
        Base.getMainWindow().getBed().setGcodeOK(false);
        terminateCura();
        Point5d b = machine.getTablePoints("safe");
        double acLow = machine.getAcceleration("acLow");
        double acHigh = machine.getAcceleration("acHigh");
        double spHigh = machine.getFeedrate("spHigh");

        machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 X" + acLow));
        machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spHigh));
        machine.runCommand(new replicatorg.drivers.commands.QueuePoint(b));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 X" + acHigh));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28", COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
    }

    private String getPrintTime() {
        return String.valueOf((int) (Math.ceil((System.currentTimeMillis() - startTimeMillis) / 1000) / 60) + 2);
    }

    public int pollCurrentLines() {
        String nLines;
        AutonomousData variables;

        variables = getAutonomousData();
        nLines = variables.getCurrentNLines().toString();

        if (nLines.equals("") == false) {
            return Integer.valueOf(nLines);
        }

        return -1;
    }

    public void setPrintEnded(boolean status) {
        printEnded = status;
        unloadPressed = false;

        if (printEnded) {
//            jPanel5.setVisible(false);
            tInfo6.setText("");
            int duration = Integer.valueOf(getPrintTime());
            String minToHour = minutesToHours(duration, true);
            String hours = minToHour.split("\\:")[0];
            String minutes = minToHour.split("\\:")[1];

            vEstimation.setVisible(false);
            vRemaining.setVisible(false);
            tInfo3.setVisible(false);
            bCancel.setVisible(false);
            bPause.setVisible(false);
            bOk.setVisible(true);
            bUnload.setVisible(true);
            bUnload.setText(Languager.getTagValue(1, "FilamentWizard", "UnloadButton"));

            jProgressBar1.setValue(100);
            jProgressBar1.setVisible(false);

            if (duration >= 2 && duration > 60) {

                tInfo2.setText(Languager.getTagValue(1, "Print", "Print_BuildFinished"));
                if (duration >= 60 && duration < 120) {
                    tEstimation.setText(Languager.getTagValue(1, "Print", "Print_Completion") + " " + hours + " "
                            + Languager.getTagValue(1, "Print", "PrintHour") + " " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinutes"));
                } else {
                    tEstimation.setText(Languager.getTagValue(1, "Print", "Print_Completion") + " " + hours + " "
                            + Languager.getTagValue(1, "Print", "PrintHours") + " " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinutes"));
                }
                bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                tRemaining.setText(Languager.getTagValue(1, "Print", "Print_Splash_Info5"));

            } else {
                tInfo2.setText(Languager.getTagValue(1, "Print", "Print_BuildFinished"));
                tEstimation.setText(Languager.getTagValue(1, "Print", "Print_Completion") + " " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinutes"));
                bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                tRemaining.setText(Languager.getTagValue(1, "Print", "Print_Splash_Info5"));

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
    }

    public void startUnload() {
        jProgressBar1.setVisible(false);
        iPrinting.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "rsz_unload-01.png")));
        tRemaining.setText(Languager.getTagValue(1, "FilamentWizard", "Exchange_Info3"));

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
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 X" + acLow));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F" + spHigh + " X" + rest.a() + " Y" + rest.b()));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 X" + acHigh));

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

                bUnload.setVisible(true);
                iPrinting.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "retirar_filamento-01.png")));
                machine.runCommand(new replicatorg.drivers.commands.SetCoilText("none"));
                tRemaining.setText(Languager.getTagValue(1, "Print", "Print_Unloaded1"));
                bOk.setVisible(true);
                firstUnloadStep = true;
                bOk.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line7"));
                tInfo6.setText("");
                tInfo2.setText(Languager.getTagValue(1, "Print", "Unload_BuildFinished"));
                tInfo6.setText(Languager.getTagValue(1, "Print", "Print_Unloaded3"));
                unloadPressed = false;
                bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));

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
        Base.getMachineLoader().getMachineInterface().runCommand(new replicatorg.drivers.commands.SetTemperature(0));
        Base.getMainWindow().getButtons().updatePressedStateButton("print");
        Base.cleanDirectoryTempFiles(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/");
        Base.bringAllWindowsToFront();
        machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
        machine.getModel().setMachineBusy(false);
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean waitForDecision() {
        return userDecision;
    }

    @Override
    public void windowOpened(WindowEvent e) {
        return;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        return;
    }

    @Override
    public void windowClosed(WindowEvent e) {
        return;
    }

    @Override
    public void windowIconified(WindowEvent e) {
        Base.getMainWindow().setState(JFrame.ICONIFIED);
        return;
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        jProgressBar1.setValue(progression);
        Base.getMainWindow().setExtendedState(JFrame.MAXIMIZED_BOTH);

    }

    @Override
    public void windowActivated(WindowEvent e) {
        return;
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        return;
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
        vEstimation = new javax.swing.JLabel();
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

        tInfo2.setText("A PROCESSAR... POR FAVOR AGUARDE");

        tInfo7.setText("tInfo7");

        tEstimation.setText("tEstimation");

        vEstimation.setText("NA");

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
                                .addComponent(vEstimation))
                            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 36, Short.MAX_VALUE))
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
                    .addComponent(vEstimation))
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
            bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
        } else {
            if (errorOccured) {
                bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
            }
        }
    }//GEN-LAST:event_bOkMouseEntered

    private void bOkMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bOkMouseExited
        if (printEnded) {
            bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
        } else {
            if (errorOccured) {
                bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
            }
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
            } else {
                if (errorOccured) {
                    dispose();
                    PrintSplashAutonomous p = new PrintSplashAutonomous(false, preferences);
                    p.setVisible(true);
                    p.startConditions();
                }
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

                machine.runCommand(new replicatorg.drivers.commands.ReadTemperature());
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
                bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
                startUnload();
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

            bPause.setEnabled(false);
            bOk.setEnabled(false);

            setPreparingNewFilamentInfo();
            Base.printPaused = true;
            machine.stopwatch();
            isPaused = true;
            //Pause print
            machine.runCommand(new replicatorg.drivers.commands.Pause());
            //Disable power saving to avoid temperature lowering down
            Base.turnOnPowerSaving(false);

            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {

                    do {
                        machine.runCommand(new replicatorg.drivers.commands.ReadStatus());
                        try {
                            Thread.sleep(500, 0);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(PrintSplashAutonomous.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } while (machine.getDriver().getMachineStatus() == false);

                    machine.getDriver().setBusy(false);
                    bPause.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line12"));
                    machine.pause();

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
    private javax.swing.JLabel vEstimation;
    private javax.swing.JLabel vRemaining;
    // End of variables declaration//GEN-END:variables

}

class UpdateThread4 extends Thread {

    private final PrintSplashAutonomous window;
    private final Driver driver = Base.getMainWindow().getMachineInterface().getDriver();
    private final TransferControlThread gcodeGenerator;
    private static final String ERROR = "error";
    private final double temperatureGoal;
    private static final MachineInterface machine = Base.getMachineLoader().getMachineInterface();
    private int nLines = 0;
    private double lines = 0.0;
    private boolean finished = false;
    private BufferedReader reader = null;
    private final long updateSleep = 500;
    private String estimatedTime;
    private boolean isOnShutdownRecover;
    private File gcode = null;

    public UpdateThread4(PrintSplashAutonomous w, TransferControlThread gcodeGen) {
        super("Autonomous Thread");
        this.window = w;
        this.gcodeGenerator = gcodeGen;
        this.temperatureGoal = window.getFilamentTemperature();
    }

    private void transferGCode() {
        try {

            window.setTransferInfo();

            try {
                reader = new BufferedReader(new FileReader(gcode));
            } catch (FileNotFoundException ex) {
                Base.writeLog("Can't read gCode file to print in autonomous mode");
            }

            //Calculate number of lines of GCode
            nLines = window.getNGCodeLines();
            // Transfer GCode
            if (driver.gcodeTransfer(gcode, estimatedTime, nLines, window).toLowerCase().contains(ERROR)) {
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

    private void monitorPrintFromSDCard() {

        window.startPrintCounter();
        window.setPrintElements();

        window.updatePrintEstimation(estimatedTime, true);
        window.updatePrintEstimation(estimatedTime, false);

        //Updates elements while visible
        while (!window.isAtErrorState()) {
            // Enables test of pipes to handle cable disconnection
            //driver.setAutonomous(true);

            if (!window.isPaused()) {

                // Updates estimation with 1 min periodicity
                int numbLines = window.updateTimeElapsed();

                if (numbLines != -1) {
                    lines = numbLines;
                }

                window.updatePrintBar((lines / (double) nLines) * 100);
                window.setProgression((int) ((lines / (double) nLines) * 100));
            }

            if (lines >= nLines) {
                //finished = true;
                //break;
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Logger.getLogger(UpdateThread4.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //what to do at error?
    }

    private void finalizePrint() {
        //End print session
        Base.setPrintEnded(true);
        //Read build time
//        AutonomousData variables = driver.getPrintSessionsVariables();
//        long dur = Long.parseLong(variables.getElapsedTime().toString());
        window.setPrintEnded(finished);

    }

    public boolean evaluateTemperature() {
        machine.runCommand(new replicatorg.drivers.commands.ReadTemperature());

        try {
            Thread.sleep(updateSleep);
        } catch (InterruptedException ex) {
            Logger.getLogger(UpdateThread4.class.getName()).log(Level.SEVERE, null, ex);
        }

        double temperature = machine.getDriver().getTemperature();
        window.updateTemperatureOnProgressBar(temperature, temperatureGoal / 100);

        if (temperature >= (temperatureGoal - 1)) {
            window.updateTemperatureOnProgressBar(100, temperatureGoal / 100);
            Base.writeLog("Temperature achieved");
            return true;
        }

        return false;

    }

    public boolean isOnShutdownRecover() {
        return this.isOnShutdownRecover;
    }

    private boolean checkIFReady() {
        boolean ready = false;

        while (true) {
            machine.runCommand(new replicatorg.drivers.commands.ReadStatus());
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
                Logger.getLogger(DisposeFeedbackThread.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (machine.getDriver().getMachineStatus() && finished) {
                ready = true;
                break;
            }
        }

        return ready;
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

            Base.writeLog("Autonomous print resumed");
            Base.isPrinting = true;

            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(UpdateThread4.class.getName()).log(Level.SEVERE, null, ex);
            }

            //Parse of the answer
            AutonomousData variables = window.getAutonomousData();
            window.resetProgressBar();

            estimatedTime = variables.getEstimatedTime().toString();
            String elapsedTime = variables.getElapsedTime().toString();
            nLines = Integer.valueOf(variables.getNLines().toString());
            int currentNumberLines = Integer.valueOf(variables.getCurrentNLines().toString());

            int time = (int) Math.ceil((1.0 * Integer.valueOf(elapsedTime)) / 60000);
            String timeRemaining = String.valueOf(Integer.valueOf(estimatedTime) - time);

            System.out.println("*****************");
            System.out.println("Estimated Time: " + estimatedTime);
            System.out.println("Elapsed Time: " + elapsedTime);
            System.out.println("Difference Time: " + timeRemaining);
            System.out.println("NLines: " + nLines);
            System.out.println("Current NLines: " + currentNumberLines);
            System.out.println("*****************");
            window.setPrintInfo();

            window.updatePrintEstimation(estimatedTime, true);
            window.updatePrintEstimation(timeRemaining, false);
            window.setPrintElements();
            window.startPrintCounter();

            if (window.isPaused()) {
                window.disableButtons();
                PauseMenu pause = new PauseMenu(window);
                pause.setVisible(true);
            }

            if (window.isShutdown()) {
                window.disableButtons();
                ShutdownMenu shutdown = new ShutdownMenu(window);
                shutdown.setVisible(true);

                //window.setShutdownStatus();
                //set pause UI elements
                //window.restorePauseElements();

                /*
                 //Control var to Shutdown button lock
                 this.isOnShutdownRecover = true;

                 boolean temperatureAchieved = false;
                 Base.getMainWindow().getButtons().blockModelsButton(false);
                 window.setHeatingInfo();
                 window.resetProgressBar();

                 while (temperatureAchieved == false) {
                 try {
                 temperatureAchieved = evaluateTemperature();
                 Thread.sleep(3000);
                 } catch (InterruptedException ex) {
                 Logger.getLogger(UpdateThread4.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 }

                 //resume from shutdown
                 window.resumeFromShutdown();
                 this.isOnShutdownRecover = false;
                 */
            }

            //Updates elements while visible
            while (true) {

                if (!window.isPaused() && !window.isShutdown()) {
                    // Updates estimation with 1 min periodicity
                    int numbLines = window.updateTimeElapsed();

                    if (numbLines != -1) {
                        currentNumberLines = numbLines;
                    }

                    window.updatePrintBar((currentNumberLines / (double) nLines) * 100);
                    window.setProgression((int) ((currentNumberLines / (double) nLines) * 100));
                }
                if (currentNumberLines >= nLines) {
                    //finished = true;
                    //break;
                }
                //no need for else

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(UpdateThread4.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } else { // First run in Autonomous mode

            /**
             * Heat
             */
            machine.runCommand(new replicatorg.drivers.commands.SetTemperature(temperatureGoal + 5));

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
            }
            estimatedTime = String.valueOf((int) gcodeGenerator.getGenerationTime());

            /**
             * GCode generated; set driver to autonomous and transfer it
             */
            driver.setAutonomous(true);
            transferGCode();

            /**
             * Controls temperature for proper print
             */
            window.setHeatingInfo();
            boolean temperatureAchieved = false;
            Base.getMainWindow().getButtons().blockModelsButton(false);

            window.resetProgressBar();
            while (temperatureAchieved == false) {
                temperatureAchieved = evaluateTemperature();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(UpdateThread4.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            /**
             * Set UI elements
             */
            window.setPrintInfo();

            /**
             * Reset elapsed time of Autonomous to 0
             */
            //machine.runCommand(new replicatorg.drivers.commands.SendStandaloneVariables());
            /**
             * Start printing from SDCard
             */
            machine.runCommand(new replicatorg.drivers.commands.StartPrint());

            /**
             * GCode transfered so start print
             */
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

        /**
         * Check if machine is ready to continue
         */
        if (checkIFReady()) {
            finalizePrint();
        }

        while (true) {
            boolean tempReached = false;

            if (window.isUnloadPressed()) {
                tempReached = evaluateTemperature();
                if (tempReached) {
                    window.startUnload();
                    break;
                }
            }
        }

        this.stop();
    }
}

class TransferControlThread extends Thread {

    PrintSplashAutonomous window;
    boolean finish = false;
    double estimatedTime = 0;
    boolean gCodeDone = false;

    public TransferControlThread(PrintSplashAutonomous w) {
        super("Autonomous gcode generation Thread");
        window = w;
    }

    public boolean isReady() {
        return finish;
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
        Base.originalColorRatio = FilamentControler.getColorRatio(Base.getMainWindow().getMachine().getModel().getCoilText(),
                Base.getMainWindow().getMachine().getModel().getResolution(),
                Base.getMainWindow().getMachine().getDriver().getConnectedDevice().filamentCode());

    }

    private boolean estimate(boolean printingFromGCode) {

        String assumedTime;

        Base.writeLog("Estimating printing time...");

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
            Base.writeLog("Printing estimation failed...");
            return false;
        } else {
            if (assumedTime.contains(":")) {
                String[] timeValues = assumedTime.split(":");
                estimatedTime = Integer.valueOf(timeValues[0]) * 60 + Integer.valueOf(timeValues[1]);
            } else {
                estimatedTime = Integer.valueOf(assumedTime);
            }

            Base.writeLog("Printing estimation successful...");
            return true;
        }
    }
}

class PauseAssistantThread extends Thread {

    private final double colorRatio, temperatureGoal;
    private final PrintSplashAutonomous printPanel;
    private final MachineInterface machine;

    public PauseAssistantThread(double colorRatio, double temperatureGoal,
            PrintSplashAutonomous printPanel, MachineInterface machine) {
        this.colorRatio = colorRatio;
        this.temperatureGoal = temperatureGoal;
        this.printPanel = printPanel;
        this.machine = machine;
    }

    @Override
    public void run() {
        boolean temperatureAchieved = false;

        try {
            machine.getDriver().setTemperature(temperatureGoal + 5);
        } catch (RetryException ex) {
        }
        printPanel.setHeatingInfo();

        printPanel.resetProgressBar();
        while (temperatureAchieved == false) {
            temperatureAchieved = evaluateTemperature();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Logger.getLogger(UpdateThread4.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //Resume printing
        machine.runCommand(
                new replicatorg.drivers.commands.ResumePause(
                        colorRatio, temperatureGoal
                )
        );

        do {
            machine.runCommand(new replicatorg.drivers.commands.ReadStatus());
            try {
                Thread.sleep(500, 0);
            } catch (InterruptedException ex) {
                Logger.getLogger(PrintSplashAutonomous.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (machine.getDriver().getMachinePaused());

        printPanel.restoreAfterPauseResume();
    }

    private boolean evaluateTemperature() {
        machine.getDriver().readTemperature();

        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(UpdateThread4.class.getName()).log(Level.SEVERE, null, ex);
        }

        double temperature = machine.getDriver().getTemperature();
        printPanel.updateTemperatureOnProgressBar(temperature, temperatureGoal / 100);

        if (temperature >= (temperatureGoal - 1)) {
            printPanel.updateTemperatureOnProgressBar(100, temperatureGoal / 100);
            Base.writeLog("Temperature achieved");
            return true;
        }

        return false;

    }
}
