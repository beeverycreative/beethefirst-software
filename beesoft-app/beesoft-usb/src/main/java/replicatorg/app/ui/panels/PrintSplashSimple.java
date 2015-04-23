package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import static java.awt.Frame.ICONIFIED;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
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
import replicatorg.app.PrintEstimator;
import replicatorg.app.Printer;
import replicatorg.app.ProperDefault;
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
public class PrintSplashSimple extends BaseDialog implements WindowListener {

    private final Printer prt;
    private boolean printSoonPressed;
    private final ArrayList<String> preferences;
    private boolean gcodeEnded;
    private long updateSleep;
    private MachineInterface machine;
    private double temperatureGoal = 220; //default
    private final GCodeGenerationThread ut1;
    private final UpdateThread2 ut;
    private double progression;
    private boolean temperatureAchieved;
    private final boolean autonomousMode;

    public PrintSplashSimple(ArrayList<String> prefs) {
        super(Base.getMainWindow(), Dialog.ModalityType.MODELESS);
        initComponents();
        setFont();
        setTextLanguage();
        centerOnScreen();
        printSoonPressed = false;
        preferences = prefs;
        autonomousMode = preferences.get(5).equalsIgnoreCase("true");
        prt = new Printer(preferences);
        updateSleep = 700;
        progression = 0.0;
        disableSleep();
        enableDrag();
        ut1 = new GCodeGenerationThread(this);
        ut = new UpdateThread2(this, ut1);
        evaluateInitialConditions();
        addWindowListener(this);
        //turn off blower before heating
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M107"));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M104 S" + temperatureGoal, COM.BLOCK));
        setProgressBarColor();
        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());
    }

    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProBold("14"));
        jLabel2.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel4.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel5.setFont(GraphicDesignComponents.getSSProRegular("10"));
        jLabel11.setFont(GraphicDesignComponents.getSSProRegular("12"));
    }

    private void setTextLanguage() {
        jLabel1.setText(Languager.getTagValue(1,"Print", "Print_Processing"));
        jLabel2.setText(Languager.getTagValue(1,"Print", "Print_Info"));
        jLabel4.setText(Languager.getTagValue(1,"Print", "Print_InfoLowStatus"));
        jLabel5.setText("");
        jLabel11.setText(Languager.getTagValue(1,"OptionPaneButtons", "Line3"));
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
    }

    private void setProgressBarColor() {
        jProgressBar1.setForeground(new Color(255, 203, 5));
    }

    public void setSleepValue(long val) {
        updateSleep = val;
    }

    public void setProgression(double prog) {
        progression = prog;
    }

    private void evaluateInitialConditions() {
        temperatureGoal = 220;
        machine = Base.getMachineLoader().getMachineInterface();
        jLabel3.setVisible(false);
        jLabel4.setVisible(false);
        printSoonPressed = true;
        temperatureAchieved = false;
        updateHeatingInfo(10);
    }

    public void startConditions() {
        boolean localPrint = Boolean.valueOf(ProperDefault.get("localPrint"));

        if (localPrint) {
            gcodeEnded = true;
            ut1.setGCodeDone(true);

            if (localPrint) {
                String filePath = Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/" + ProperDefault.get("localPrintFileName");
                File gcode = new File(filePath);
                prt.setGCodeFile(gcode);
                PrintEstimator.estimateTime(gcode);
            }
        } else {
            gcodeEnded = false;
            ut1.start();
        }

        ut.start();
    }

    public void updateHeatingInfo(int val) {
        jLabel5.setText(Languager.getTagValue(1,"FeedbackLabel", "HeatingMessage2"));
        jProgressBar1.setValue(val);
    }

    public void updateGenerationInfo() {
        jLabel5.setText(Languager.getTagValue(1,"FeedbackLabel", "GCodeGeneration"));
        jProgressBar1.setIndeterminate(true);
    }

    public boolean evaluateProgress() {
        machine.runCommand(new replicatorg.drivers.commands.ReadTemperature());

        try {
            Thread.sleep(updateSleep);
        } catch (InterruptedException ex) {
            Logger.getLogger(PrintSplashSimple.class.getName()).log(Level.SEVERE, null, ex);
        }

        double temperature = machine.getDriver().getTemperature();
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M104 S" + temperatureGoal));


        int val = jProgressBar1.getValue();
        int temp_val = (int) (temperature / 2.3);
        if ((temperature > (int) (jProgressBar1.getValue() * 2)) && (temp_val > (int) (jProgressBar1.getValue()))) {
            val = temp_val;
        }

        if (temperature >= temperatureGoal) {
            temperatureAchieved = true;
            Base.writeLog("Temperature achieved");
        }

        if (!temperatureAchieved) {
            updateHeatingInfo(val);
            return false;
        } else {
            if (!gcodeEnded) {
                updateGenerationInfo();
                return false;
            } else {
                firePrint();
                return true;
            }
        }

    }

    private void firePrint() {
        if (printSoonPressed) {

//           if(!autonomousMode)
//           {
            PrintSplashSimpleWaiting p = new PrintSplashSimpleWaiting(preferences, prt);
            p.setVisible(true);
            Base.getMainWindow().getButtons().blockModelsButton(false);
//           }
//           else
//           {
//                PrintSplashAutonomous p = new PrintSplashAutonomous(false,prt,preferences);
//                p.setVisible(true);
//           }

            dispose();
            disableSleep();
        } else {
            jLabel11.setText(Languager.getTagValue(1,"ToolPath", "Line19"));
            jLabel1.setVisible(false);
            jLabel2.setFont(GraphicDesignComponents.getSSProBold("14"));
            jLabel2.setText(Languager.getTagValue(1,"Print", "Print_Splash_Title"));
        }
        /**
         * Stop Threads.
         */
        ut.stop();
        ut1.stop();
    }

    public int getBarActualValue() {
        return jProgressBar1.getValue();
    }

    public String generateGCode() {
        return prt.generateGCode(preferences);
    }

    private void terminateCura() {
        prt.endGCodeGeneration();
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

    public void setGCodeFinish(boolean state) {
        this.gcodeEnded = state;
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

    public void cancelProcess() {
        dispose();
        enableSleep();
        Base.bringAllWindowsToFront();
        Base.cleanDirectoryTempFiles(Base.getAppDataDirectory().getAbsolutePath() + "/" + Base.MODELS_FOLDER);
        Base.getMainWindow().handleStop();
        Base.getMainWindow().setEnabled(true);
        Base.isPrinting = false;
        Base.getMainWindow().getButtons().updatePressedStateButton("print");
        Base.getMainWindow().getMachine().runCommand(new replicatorg.drivers.commands.SetTemperature(0));
        ut.stop();
        ut1.stop();
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

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jPanel4 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        jPanel2.setBackground(new java.awt.Color(248, 248, 248));

        jLabel1.setText("A PROCESSAR... POR FAVOR AGUARDE");

        jLabel2.setText("Este processo podera levar varios minutos");

        jProgressBar1.setBackground(new java.awt.Color(186, 186, 186));

        jPanel4.setBackground(new java.awt.Color(255, 203, 5));
        jPanel4.setMinimumSize(new java.awt.Dimension(62, 26));
        jPanel4.setRequestFocusEnabled(false);

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
                .addGap(51, 51, 51)
                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel5.setText("Operation in course");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(424, 424, 424)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(150, 150, 150)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );

        jPanel3.setBackground(new java.awt.Color(255, 203, 5));
        jPanel3.setMinimumSize(new java.awt.Dimension(20, 46));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/c_unchecked.png"))); // NOI18N
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel3MouseClicked(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel4.setText("INCIAR A IMPRESSAO LOGO QUE POSSIVEL");

        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_13.png"))); // NOI18N
        jLabel11.setText("CANCELAR");
        jLabel11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel11.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel11MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel11MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel11MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel3)
                .addGap(6, 6, 6)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel3))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel11))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseClicked
        if (!printSoonPressed) {
            jLabel3.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_checked.png")));
            printSoonPressed = true;
        } else {
            jLabel3.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_unchecked.png")));
            printSoonPressed = false;
        }
    }//GEN-LAST:event_jLabel3MouseClicked

    private void jLabel11MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel11MouseEntered
        jLabel11.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_13.png")));
    }//GEN-LAST:event_jLabel11MouseEntered

    private void jLabel11MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel11MouseExited
        jLabel11.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_13.png")));
    }//GEN-LAST:event_jLabel11MouseExited

    private void jLabel11MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel11MousePressed
        cancelProcess();
    }//GEN-LAST:event_jLabel11MousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        cancelProcess();
    }//GEN-LAST:event_jLabel15MousePressed

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
        jProgressBar1.setValue((int) progression);
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JProgressBar jProgressBar1;
    // End of variables declaration//GEN-END:variables
}

class UpdateThread2 extends Thread {

    PrintSplashSimple window;
    GCodeGenerationThread ut1;

    public UpdateThread2(PrintSplashSimple w, GCodeGenerationThread utN) {
        super("PrintSplash simple Thread");
        this.window = w;
        this.ut1 = utN;
    }

    @Override
    public void run() {
        boolean gcodeGenerated = false;

        while (!gcodeGenerated) {
            try {

                if (window.evaluateProgress()) {
                    gcodeGenerated = true;
                }

                if (ut1.getGCodeDone()) {
                    window.setGCodeFinish(true);
                    window.setSleepValue(100);
                }
            } catch (Exception e) {
                break;
            }
        }

    }
}

class GCodeGenerationThread extends Thread {

    PrintSplashSimple window;
    boolean finish = false;
    double estimatedTime = 0;
    boolean gCodeDone = false;

    public GCodeGenerationThread(PrintSplashSimple w) {
        super("GCode Generation Thread");
        window = w;
    }

    public boolean isReady() {
        return finish;
    }

    public double getGenerationTime() {
        return estimatedTime;
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

        estimatedTime = 2;
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
        }
        //no need for else

        Base.writeLog("New GCode generated ...");
        gCodeDone = true;
    }
}
