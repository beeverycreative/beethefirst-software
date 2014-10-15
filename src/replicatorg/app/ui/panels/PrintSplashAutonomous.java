package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import static java.awt.Frame.ICONIFIED;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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
import replicatorg.app.ProperDefault;
import replicatorg.app.util.AutonomousData;
import replicatorg.drivers.Driver;
import replicatorg.drivers.commands.DriverCommand;
import replicatorg.machine.MachineInterface;
import replicatorg.model.PrintBed;
import replicatorg.plugin.toolpath.cura.XMLGCoder;
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
public class PrintSplashAutonomous extends javax.swing.JFrame implements WindowListener {

    private Printer prt;
    private ArrayList<String> preferences;
    private boolean printEnded;
    private int posX = 0, posY = 0;
    private double startTimeMillis;
    private double startTimeMillis2;
    private MachineInterface machine;
    private UpdateThread4 ut;
    private TransferControlThread gcodeGenerator;
    private int progression;
    private static final String FORMAT = "%2d:%2d";
    private int remainingTime = 0;
    private boolean alreadyPrinting;
    private boolean errorOccured = false;
    private boolean unloadPressed;
    private boolean firstUnloadStep = false;
    private double temperatureGoal = 220;
    private boolean lastPanel;
    private boolean isPaused;
    private static final String PAUSE_PRINT = "M640";
    private Point5d pausePos;
    private double newEvalue = 0;
    private Double newFvalue;
    private long pausedTime = 0;

    public PrintSplashAutonomous(boolean printingState, ArrayList<String> prefs) {
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

    public void startConditions() {
        ut.start();
    }

    private void centerOnScreen() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        // Determine the new location of the window
        int w = this.getSize().width;
        int h = this.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;

        // Move the window
        this.setLocationRelativeTo(null);
        this.setLocationRelativeTo(Base.getMainWindow());
        Base.setMainWindowNOK();
    }

    public void setProgression(int prog) {
        progression = prog;
    }

    private void setProgressBarColor() {
        jProgressBar1.setForeground(new Color(255, 203, 5));
    }

    public void updatePrintBar(double progression) {
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
        bOk.setText(Languager.getTagValue("OptionPaneButtons", "Line13"));
        bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }

    public boolean isAtErrorState() {
        return errorOccured;
    }

    public boolean isPrinting() {
        return alreadyPrinting;
    }

    public void setPrintState(boolean state) {
        alreadyPrinting = state;
    }

    private String minutesToHours(int minutes, boolean format) {
        int hoursTrunked = 0;
        int minutesTrunked = minutes;
        double quocient = 0;
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

    private void doCancel() {
        setError(true);
        dispose();
        Base.bringAllWindowsToFront();
        Base.getMainWindow().setEnabled(true);
        Base.isPrinting = false;
        Base.printPaused = false;
        Base.getMainWindow().getButtons().updatePressedStateButton("print");
        enableSleep();

        Base.cleanDirectoryTempFiles(Base.getAppDataDirectory() + "/3DModels/");

        if (machine.getDriver().isTransferMode()) {
            machine.getDriver().stopTransfer();
            while (machine.getDriver().isAutonomous()) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PrintSplashAutonomous.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } else {
            Base.getMainWindow().handleStop();

            Point5d b = machine.getTablePoints("safe");
            double acLow = machine.getAcceleration("acLow");
            double acHigh = machine.getAcceleration("acHigh");
            double spHigh = machine.getFeedrate("spHigh");

            // Sleep before home after M112
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(PrintSplashAutonomous.class.getName()).log(Level.SEVERE, null, ex);
            }

            machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28"));
            gcodeGenerator.stop();
            ut.stop();
        }

        // ut.stop();

//        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M23"));
    }

    public File getPrintFile() {

        return prt.getGCode();
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

    void updateInformationsByError() {
        tRemaining.setVisible(true);
        tInfo6.setVisible(false);
        vRemaining.setVisible(false);
        vEstimation.setVisible(false);
        tInfo7.setVisible(false);
        tInfo3.setVisible(true);
        tInfo2.setText(Languager.getTagValue("Print", "Print_BuildAborted"));
        tInfo3.setText(Languager.getTagValue("Print", "Print_BuildAborted2"));
        tEstimation.setText(Languager.getTagValue("Print", "Print_BuildAborted3"));
        tRemaining.setText(Languager.getTagValue("Print", "Print_BuildAborted4"));
        jProgressBar1.setVisible(false);

    }

    public int getNGCodeLines() {
        return prt.getGCodeNLines();
    }

    public void startPrintCounter() {
        startTimeMillis = System.currentTimeMillis();
        startTimeMillis2 = System.currentTimeMillis();
    }

    public void setPrintElements() {
        tInfo2.setText(Languager.getTagValue("Print", "Print_Splash_Info3"));
        tInfo6.setText(Languager.getTagValue("Print", "Print_Splash_Info4"));
        tEstimation.setVisible(true);
        tRemaining.setVisible(true);
        vEstimation.setVisible(true);
        vRemaining.setVisible(true);
    }

    public String printTime(boolean autonomous, long startMillis) {

        if (!autonomous) {
            return String.valueOf((int) (Math.ceil((System.currentTimeMillis() - startTimeMillis) / 1000) / 60) + 1);
        }

        return String.valueOf((int) (Math.ceil((startMillis) / 1000) / 60) + 1);
    }

    public String generateGCode() {
        return prt.generateGCode(preferences);
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
                                + " " + Languager.getTagValue("Print", "PrintHours")
                                + " " + minutes
                                + " " + Languager.getTagValue("Print", "PrintMinutes");
                        textR = hours
                                + " " + Languager.getTagValue("Print", "PrintHours")
                                + " " + minutes
                                + " " + Languager.getTagValue("Print", "PrintMinutes");

                        vEstimation.setText(textR);
                    } else {
                        textR = hours
                                + " " + Languager.getTagValue("Print", "PrintHours")
                                + " " + minutes
                                + " " + Languager.getTagValue("Print", "PrintMinutes");

                    }
                } else {
                    if (cutout) {
                        textE = hours
                                + " " + Languager.getTagValue("Print", "PrintHour")
                                + " " + minutes
                                + " " + Languager.getTagValue("Print", "PrintMinutes");

                        vEstimation.setText(textE);
                    } else {
                        textR = hours
                                + " " + Languager.getTagValue("Print", "PrintHour")
                                + " " + minutes
                                + " " + Languager.getTagValue("Print", "PrintMinutes");

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
                            + " " + Languager.getTagValue("Print", "PrintMinutes");

                    if (remainingTime > 60) {
                        if (hours == 1) {
                            textR = hours
                                    + " " + Languager.getTagValue("Print", "PrintHour")
                                    + " " + minutes
                                    + " " + Languager.getTagValue("Print", "PrintMinutes");
                        } else {
                            textR = hours
                                    + " " + Languager.getTagValue("Print", "PrintHours")
                                    + " " + minutes
                                    + " " + Languager.getTagValue("Print", "PrintMinutes");
                        }
                        
                    } else if (remainingTime == 2) {
                        //In case PrintEstimator fails, dont appear 2min
                        textE = "";
                        vEstimation.setVisible(false);
                        tRemaining.setVisible(false);
                        vRemaining.setVisible(false);
                        tEstimation.setText(Languager.getTagValue("Print", "Print_EstimatorError"));
                        return;
                    } else {
                        textR = remainingTime
                                + " " + Languager.getTagValue("Print", "PrintMinutes");
                    }
                    vEstimation.setText(textR);
                } else {
                    if (remainingTime > 60) {
                        textR = hours
                                + " " + Languager.getTagValue("Print", "PrintHours")
                                + " " + minutes
                                + " " + Languager.getTagValue("Print", "PrintMinutes");
                    }
                    if (remainingTime < 1) {
                        tRemaining.setText(Languager.getTagValue("Print", "Print_Info2"));
                        vRemaining.setVisible(false);
                        return;
                    } else if (remainingTime > 1 && remainingTime <= 60) {
                        textR = remainingTime
                                + " " + Languager.getTagValue("Print", "PrintMinutes");
                    } else if (remainingTime == 1) {
                        textR = remainingTime
                                + " " + Languager.getTagValue("Print", "PrintMinute");
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
        tInfo2.setText(Languager.getTagValue("Print", "Print_Transfering"));
        tEstimation.setText(Languager.getTagValue("Print", "Print_Estimation"));
        vEstimation.setText(estimateTransferTime());
        vEstimation.setVisible(true);
        tInfo6.setVisible(false);
        tInfo7.setVisible(false);
    }

    public void setProcessingInfo() {
        tInfo2.setText(Languager.getTagValue("Print", "Print_Processing"));
        tEstimation.setText(Languager.getTagValue("Print", "Print_Info"));
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
        vRemaining.setVisible(false);
        vEstimation.setVisible(false);
        tInfo2.setText(Languager.getTagValue("Print", "Print_Processing"));
        tEstimation.setText(Languager.getTagValue("Print", "Print_Info"));
        tRemaining.setText(Languager.getTagValue("FeedbackLabel", "HeatingMessage2"));
    }

    public void resetProgressBar() {
        jProgressBar1.setIndeterminate(false);
        jProgressBar1.setValue(0);
    }

    public void setPrintInfo() {
        updatePrintBar(0);
        tInfo2.setText(Languager.getTagValue("Print", "Print_Splash_Info3"));
        tInfo3.setText(Languager.getTagValue("Print", "Print_Splash_Info4"));
        tEstimation.setText(Languager.getTagValue("Print", "Print_Estimation"));
        tRemaining.setText(Languager.getTagValue("Print", "Print_Remaining"));
        tInfo3.setVisible(true);
        tRemaining.setVisible(true);
        vEstimation.setVisible(true);
        vRemaining.setVisible(true);
        bPause.setVisible(true);
    }

    private void setPreparingNewFilamentInfo() {
        tInfo2.setText(Languager.getTagValue("Print", "Print_Pausing"));
//        tInfo3.setText(Languager.getTagValue("Print", "Print_Info"));
        tInfo3.setVisible(false);
        tRemaining.setVisible(false);
        vRemaining.setVisible(false);
        tEstimation.setVisible(false);
        vEstimation.setVisible(false);
        jProgressBar1.setVisible(false);
    }

    private void disablePreparingNewFilamentInfo() {
        tInfo2.setText(Languager.getTagValue("Print", "Print_Processing"));
//        tInfo3.setText(Languager.getTagValue("Print", "Print_Splash_Info4"));
        tInfo3.setVisible(true);
        tRemaining.setVisible(true);
        vRemaining.setVisible(true);
        tEstimation.setVisible(true);
        vEstimation.setVisible(true);
        jProgressBar1.setVisible(true);
        bPause.setText(Languager.getTagValue("OptionPaneButtons", "Line11"));
    }

    public String estimateTransferTime() {
        double gcodeSize = getPrintFile().length() / 1000; //in bytes
        double transferSpeed = Double.valueOf(ProperDefault.get("transferSpeed")); //speed = 42 KB/s
        double estimatedTime = Math.round(gcodeSize / transferSpeed);
        int timeInMinutes = (int) (estimatedTime / 60) + 1;
        return buildTimeEstimationString(String.valueOf(timeInMinutes));
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
            } else if (duration == 1) {
                text = (" " + minutes + " " + Languager.getTagValue("Print", "PrintMinute"));
            } else {
                text = (" " + minutes + " " + Languager.getTagValue("Print", "PrintMinutes"));
            }
        } else {
            text = (durT);
        }


        return text;

    }

    public int updateTimeElapsed() {
        int nLines = -1;

        int elapsedTime = (int) (Math.ceil((System.currentTimeMillis() - startTimeMillis2) / 1000));

        if (elapsedTime > 60) {
            nLines = pollCurrentLines();
            
            // Not able to estimate print time
            // 00 - error code
            if (remainingTime == 00) {
                tRemaining.setText(Languager.getTagValue("Print", "Print_Info"));
                vRemaining.setVisible(false);
            } else {
                // Subtract one minute to remaining time
                remainingTime -= 1;

                if (remainingTime > 0) {
                    updatePrintEstimation(String.valueOf(remainingTime), false);
                } else {
                    tRemaining.setText(Languager.getTagValue("Print", "Print_Info2"));
                    vRemaining.setVisible(false);
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

    public void updateTemperatureOnProgressBar(double temperature) {
        int val = jProgressBar1.getValue();
        int temp_val = (int) (temperature / 2.3);
        if ((temperature > (int) (jProgressBar1.getValue() * 2)) && (temp_val > (int) (jProgressBar1.getValue()))) {
            val = temp_val;
        }
        jProgressBar1.setValue(val);
    }

    private void terminateCura() {
        prt.endGCodeGeneration();
    }
    
    public long getPausedTime()
    {
        return this.pausedTime;
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
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acLow));
        machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spHigh));
        machine.runCommand(new replicatorg.drivers.commands.QueuePoint(b));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acHigh));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28", COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
    }

    private String getPrintTime() {
        return String.valueOf((int) (Math.ceil((System.currentTimeMillis() - startTimeMillis) / 1000) / 60) + 2);
    }

    public int pollCurrentLines() {
        AutonomousData variables = machine.getDriver().getPrintSessionsVariables();
        String nLines = variables.getCurrentNLines().toString();

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
            bUnload.setText(Languager.getTagValue("FilamentWizard", "UnloadButton"));

            jProgressBar1.setValue(100);
            jProgressBar1.setVisible(false);


            if (duration >= 2 && duration > 60) {

                tInfo2.setText(Languager.getTagValue("Print", "Print_BuildFinished"));
                if (duration >= 60 && duration < 120) {
                    tEstimation.setText(Languager.getTagValue("Print", "Print_Completion") + " " + hours + " "
                            + Languager.getTagValue("Print", "PrintHour") + " " + minutes + " " + Languager.getTagValue("Print", "PrintMinutes"));
                } else {
                    tEstimation.setText(Languager.getTagValue("Print", "Print_Completion") + " " + hours + " "
                            + Languager.getTagValue("Print", "PrintHours") + " " + minutes + " " + Languager.getTagValue("Print", "PrintMinutes"));
                }
                bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                tRemaining.setText(Languager.getTagValue("Print", "Print_Splash_Info5"));

            } else {
                tInfo2.setText(Languager.getTagValue("Print", "Print_BuildFinished"));
                tEstimation.setText(Languager.getTagValue("Print", "Print_Completion") + " " + minutes + " " + Languager.getTagValue("Print", "PrintMinutes"));
                bOk.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                tRemaining.setText(Languager.getTagValue("Print", "Print_Splash_Info5"));

            }
            ProperDefault.put("durationLastPrint", String.valueOf(duration));
            int nPrints = Integer.valueOf(ProperDefault.get("nTotalPrints"))+1;
            ProperDefault.put("nTotalPrints", String.valueOf(nPrints));

            /**
             * Power saving
             */
            Base.turnOnPowerSaving();

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
        tRemaining.setText(Languager.getTagValue("FilamentWizard", "Exchange_Info3"));

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

                bUnload.setVisible(true);
                iPrinting.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "retirar_filamento-01.png")));
                machine.runCommand(new replicatorg.drivers.commands.SetCoilCode(FilamentControler.NO_FILAMENT_CODE));
                tRemaining.setText(Languager.getTagValue("Print", "Print_Unloaded1"));
                bOk.setVisible(true);
                firstUnloadStep = true;
                bOk.setText(Languager.getTagValue("OptionPaneButtons", "Line7"));
                tInfo6.setText("");
                tInfo2.setText(Languager.getTagValue("Print", "Unload_BuildFinished"));
                tInfo6.setText(Languager.getTagValue("Print", "Print_Unloaded3"));
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
        Base.getMachineLoader().getMachineInterface().runCommand(new replicatorg.drivers.commands.UpdateCoilCode());
        Base.cleanDirectoryTempFiles(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/");
        Base.bringAllWindowsToFront();
        machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
        machine.getModel().setMachineBusy(false);
    }

    public boolean isPaused() {
        return isPaused;
    }

    private String getColor() {
        String coilCode = machine.getModel().getCoilCode();

        return FilamentControler.getColor(coilCode);

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
        jPanel4 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        iPrinting = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        tInfo6 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        tRemaining = new javax.swing.JLabel();
        vRemaining = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        jPanel2.setBackground(new java.awt.Color(248, 248, 248));
        jPanel2.setPreferredSize(new java.awt.Dimension(475, 169));

        jPanel4.setBackground(new java.awt.Color(255, 203, 5));
        jPanel4.setMinimumSize(new java.awt.Dimension(62, 26));
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

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));
        jPanel1.setPreferredSize(new java.awt.Dimension(117, 22));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 348, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
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
                                .addComponent(vEstimation)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE))
                            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tEstimation)
                            .addComponent(vEstimation))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
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
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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

        bCancel.setForeground(new java.awt.Color(0, 0, 0));
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

        bOk.setForeground(new java.awt.Color(0, 0, 0));
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

        bUnload.setForeground(new java.awt.Color(0, 0, 0));
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

        bPause.setForeground(new java.awt.Color(0, 0, 0));
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

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bPause)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bCancel)
                .addGap(12, 12, 12)
                .addComponent(bOk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bUnload)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(2, 2, 2)
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
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_18.png")));
    }//GEN-LAST:event_bCancelMouseEntered

    private void bCancelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseExited
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_18.png")));
    }//GEN-LAST:event_bCancelMouseExited

    private void bCancelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMousePressed
        doCancel();
    }//GEN-LAST:event_bCancelMousePressed

    private void bOkMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bOkMousePressed

        if (printEnded) {

            if (firstUnloadStep) {
                bOk.setVisible(false);
                iPrinting.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "retirar_filamento-07.png")));
                bUnload.setText(Languager.getTagValue("OptionPaneButtons", "Line6"));
                lastPanel = true;
                firstUnloadStep = false;
                tRemaining.setText(Languager.getTagValue("Print", "Print_Unloaded2"));
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
    }//GEN-LAST:event_bOkMousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        doCancel();
    }//GEN-LAST:event_jLabel15MousePressed

    private void jLabel13MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel13MousePressed
        setState(ICONIFIED);
        Base.getMainWindow().deactivateCameraControls();
    }//GEN-LAST:event_jLabel13MousePressed

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

        //second next overloads unload button for OK
        if (lastPanel) {
            disposePanel();
        }

        //first time you press unload after print is over
        if (printEnded && unloadPressed == false && firstUnloadStep == false) {
            unloadPressed = true;

            temperatureGoal = 220;
            machine.runCommand(new replicatorg.drivers.commands.ReadTemperature());
            bOk.setVisible(false);
            bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_21.png")));
            iPrinting.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "retirar_filamento-02.png")));
            tInfo2.setText(Languager.getTagValue("Print", "Unloading_Title"));
            tRemaining.setText(Languager.getTagValue("Print", "Print_Unloading"));
            jPanel1.setVisible(false);
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
    }//GEN-LAST:event_bUnloadMousePressed

    private void bPauseMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPauseMouseEntered
        bPause.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bPauseMouseEntered

    private void bPauseMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPauseMouseExited
        bPause.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bPauseMouseExited

    private void bPauseMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPauseMousePressed
        final MachineInterface machine = Base.getMachineLoader().getMachineInterface();
        final Driver driver = Base.getMainWindow().getMachineInterface().getDriver(); 
        
        if (!isPaused) {
            pausedTime = System.currentTimeMillis();
            setPreparingNewFilamentInfo();
            this.setEnabled(false);
            Base.printPaused = true;
            machine.stopwatch();
            isPaused = true;
            driver.dispatchCommand(PAUSE_PRINT);

            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {

                    pausePos = driver.getActualPosition();

                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28", COM.BLOCK));

                    String status = driver.dispatchCommand("M625");

                    while (!status.contains("S:3")) {
                        try {
                            Thread.sleep(10, 0);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(PrintSplashAutonomous.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        status += driver.dispatchCommand("M625");
                    }

                    machine.getDriver().setBusy(false);
                    Maintenance p = new Maintenance();
                    p.setVisible(true);
                    bPause.setText(Languager.getTagValue("OptionPaneButtons", "Line12"));
                    tInfo2.setText(Languager.getTagValue("Print", "Print_Waiting"));
                }
            });

        } else {
            Base.getMainWindow().setEnabled(false);
//            pausePos = machine.getLastPrintedPoint();
//            int lastLineNumber = driver.getLastLineNumber();
            bPause.setText(Languager.getTagValue("OptionPaneButtons", "Line12"));
            tInfo2.setText(Languager.getTagValue("Print", "Print_Resuming"));
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    //Little retraction to avoid filament break
                    driver.dispatchCommand("G1 F300 E50", COM.BLOCK);
                    driver.dispatchCommand("G92 E", COM.BLOCK);
                    driver.dispatchCommand("G1 F6000 E-2", COM.BLOCK);
//                    driver.dispatchCommand("G1 F6000 E0", COM.BLOCK);
                    
                    
                    double actualColorRatio = FilamentControler.getColorRatio(machine.getModel().getCoilCode());
                    double colorRatio = actualColorRatio / Base.originalColorRatio;
                    /**
                     * Signals FW about the color ratio between previous and
                     * actual color
                     */
                    driver.dispatchCommand("M642 W" + colorRatio, COM.NO_RESPONSE);

                    /**
                     * Go to print position
                     */
                    driver.dispatchCommand("G1 F2000 X" + pausePos.x() + " Y" + pausePos.y(), COM.NO_RESPONSE);
                    driver.dispatchCommand("G1 F1000 Z" + (pausePos.z()+10) + " E-1", COM.NO_RESPONSE);
                    driver.dispatchCommand("G1 F1000 Z" + pausePos.z() + " E0", COM.NO_RESPONSE);
                    driver.dispatchCommand("G92 E"+pausePos.a(), COM.BLOCK);
                    
                    String status = driver.dispatchCommand("M625", COM.BLOCK);
                    
                    pausedTime = System.currentTimeMillis() - pausedTime;
                    AutonomousData variables = driver.getPrintSessionsVariables();
                    String elapsedTime = variables.getElapsedTime().toString();
                    driver.setElapsedTime((Long.valueOf(elapsedTime)-pausedTime));
                    
                    while (!status.contains("S:3")) {
                        try {
                            Thread.sleep(10, 0);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(PrintSplashAutonomous.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        status += driver.dispatchCommand("M625", COM.BLOCK);
                    }
                   
                   
                    //Resume printing
                    driver.startPrintAutonomous();
                    isPaused = false;
                    disablePreparingNewFilamentInfo();
                    Base.printPaused = false;
                    machine.resumewatch();
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
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
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
    private javax.swing.JLabel tInfo3;
    private javax.swing.JLabel tInfo6;
    private javax.swing.JLabel tInfo7;
    private javax.swing.JLabel tRemaining;
    private javax.swing.JLabel vEstimation;
    private javax.swing.JLabel vRemaining;
    // End of variables declaration//GEN-END:variables
}

class UpdateThread4 extends Thread {

    private PrintSplashAutonomous window;
    private Driver driver = Base.getMainWindow().getMachineInterface().getDriver();
    private TransferControlThread gcodeGenerater;
    private static final String ERROR = "error";
    private double temperatureGoal = 220; //default
    private static final MachineInterface machine = Base.getMachineLoader().getMachineInterface();
    private int nLines = 0;
    private double lines = 0.0;
    private boolean finished = false;
    private BufferedReader reader = null;
    private long updateSleep = 500;
    private String estimatedTime;
    File gcode = null;

    public UpdateThread4(PrintSplashAutonomous w, TransferControlThread gcodeGen) {
        super("Autonomous Thread");
        this.window = w;
        this.gcodeGenerater = gcodeGen;
    }

    private void appendSpecialGCode() {
        PrintWriter pw = null;
        File gcodeCura = window.getPrintFile();
        String gcodeName = gcodeCura.getName().substring(0, gcodeCura.getName().lastIndexOf(".")).concat("STEDCD");
        gcode = new File(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/" + gcodeName + ".gcode");
        String[] startCode = XMLGCoder.getGCode("startCode");
        String[] endCode = XMLGCoder.getGCode("endCode");
        BufferedReader br = null;
        String line = "";

        try {
            pw = new PrintWriter(new FileOutputStream(gcode));
            br = new BufferedReader(new FileReader(gcodeCura));

            // Start GCode
            for (int i = 0; i < startCode.length; i++) {
                pw.println(startCode[i].trim());
            }

            // GCode
            while ((line = br.readLine()) != null) {
                pw.println(line);
            }

            // End GCode
            for (int i = 0; i < endCode.length; i++) {
                pw.println(endCode[i].trim());
            }

            br.close();
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CalibrationPrintTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UpdateThread4.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void transferGCode() {
        try {

            window.setTransferInfo();
            String ans = "";

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
            Logger.getLogger(UpdateThread3.class.getName()).log(Level.SEVERE, null, ex);
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
            driver.setAutonomous(true);

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
                finished = true;
                break;
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

    private boolean evaluateTemperature() {
        machine.runCommand(new replicatorg.drivers.commands.ReadTemperature());
        boolean temperatureAchieved = false;
        window.setHeatingInfo();

        try {
            Thread.sleep(updateSleep);
        } catch (InterruptedException ex) {
            Logger.getLogger(PrintSplashSimple.class.getName()).log(Level.SEVERE, null, ex);
        }

        double temperature = machine.getDriver().getTemperature();
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M104 S" + temperatureGoal));

        window.updateTemperatureOnProgressBar(temperature);

        if (temperature >= temperatureGoal) {
            window.updateTemperatureOnProgressBar(100);
            temperatureAchieved = true;
            Base.writeLog("Temperature achieved");
            return true;
        }

        if (!temperatureAchieved) {
            return false;
        }

        return false;

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

        if (window.isPrinting()) {
            Base.writeLog("Autonomous print resumed");
            Base.isPrinting = true;
            //Parse of the answer
            AutonomousData variables = driver.getPrintSessionsVariables();
            window.resetProgressBar();

            String estimatedTime = variables.getEstimatedTime().toString();
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
//            System.out.println("{debug} - "+time);
            /**
             * Set UI elements
             */
            window.setPrintInfo();

            //Update visual elements
            window.setPrintElements();
            window.updatePrintEstimation(estimatedTime, true);
            window.updatePrintEstimation(timeRemaining, false);

            window.startPrintCounter();

            //Updates elements while visible
            while (true) {

                if (!window.isPaused()) {
                    // Updates estimation with 1 min periodicity
                    int numbLines = window.updateTimeElapsed();

                    if (numbLines != -1) {
                        currentNumberLines = numbLines;
                    }

                    window.updatePrintBar((currentNumberLines / (double) nLines) * 100);
                    window.setProgression((int) ((currentNumberLines / (double) nLines) * 100));
                }
                if (currentNumberLines >= nLines) {
                    finished = true;
                    break;
                }
                //no need for else
            }

        } else { // First run in Autonomous mode

            /**
             * Heat
             */
            machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M104 S" + temperatureGoal));

            /**
             * Generate GCode
             */
            gcodeGenerater.start();
            boolean gcodeDone = false;

            while (gcodeDone == false) {
                try {
                    Thread.sleep(1, 0);
                } catch (InterruptedException ex) {
                    Logger.getLogger(UpdateThread4.class.getName()).log(Level.SEVERE, null, ex);
                }
                gcodeDone = gcodeGenerater.getGCodeDone();
                estimatedTime = String.valueOf((int) gcodeGenerater.getGenerationTime());
            }

            /**
             * Free JVM
             */
            gcodeGenerater.stop();

            /**
             * Append start and end GCode generated one
             */
            appendSpecialGCode();

            /**
             * GCode generated; set driver to autonomous and transfer it
             */
            driver.setAutonomous(true);
            transferGCode();

            /**
             * Controls temperature for proper print
             */
            boolean temperatureAchieved = false;
            Base.getMainWindow().getButtons().blockModelsButton(false);

            window.resetProgressBar();
            while (temperatureAchieved == false) {
                temperatureAchieved = evaluateTemperature();
            }

            /**
             * Set UI elements
             */
            window.setPrintInfo();

            /**
             * Reset elapsed time of Autonomous to 0
             */
            machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M32 "+FilamentControler.NO_FILAMENT_CODE));
            
            /**
             * Start printing from SDCard
             */
            machine.runCommand(new replicatorg.drivers.commands.StartPrintAutonomous());

            /**
             * Clean temp files created - gcode
             */
            Base.cleanDirectoryTempFiles(Base.getAppDataDirectory().getAbsolutePath() + "/" + Base.MODELS_FOLDER + "/");

            /**
             * GCode transfered so start print
             */
            monitorPrintFromSDCard();

            if (window.isAtErrorState()) {
                //Inform user about that an error happened
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

//        
//        //Set print sessions variables for accurate re-print keeping GCode
//        if (setVariables(window.getPrintTime(true, dur),Integer.valueOf(variables[2])).contains(ERROR) && !window.isAtErrorState()) {
//            window.setError(true);
//            this.stop();
//        }
//        else
//            Base.writeLog("Print variables set with success to SDCard");

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
        Base.writeLog("GCode will be generated ...");

        String assumedTime = window.generateGCode();

        if (assumedTime.equals("-1")) {
            // Error occurred - permissions maybe
            // Cancel print and setting message
            // 5000 ms delay to ensure user reads it

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(GCodeGenerationThread.class.getName()).log(Level.SEVERE, null, ex);
            }

            Base.getMainWindow().showFeedBackMessage("gcodeGeneration");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(GCodeGenerationThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            window.cancelProcess();
        } else {
            if (assumedTime.contains(":")) {
                String[] timeValues = assumedTime.split(":");
                estimatedTime = Integer.valueOf(timeValues[0]) * 60 + Integer.valueOf(timeValues[1]);
            } else {
                estimatedTime = Integer.valueOf(assumedTime);
            }
        }

        Base.writeLog("New GCode generated ...");
        gCodeDone = true;
        Base.originalColorRatio = FilamentControler.getColorRatio(Base.getMainWindow().getMachine().getModel().getCoilCode());
    }
}
