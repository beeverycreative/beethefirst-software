package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
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
public class CalibrationFinish extends BaseDialog {

    private final MachineInterface machine;

    private boolean jLabel5MouseClickedReady = true;
    private final boolean jLabel7MouseClickedReady = true;
    private final DisposeFeedbackThread5 disposeThread;
    private boolean moving = false;

    public CalibrationFinish() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        setFont();
        setTextLanguage();

        machine = Base.getMachineLoader().getMachineInterface();
        evaluateInitialConditions();
        enableDrag();        
        centerOnScreen();
        disposeThread = new DisposeFeedbackThread5(this, machine);
        disposeThread.start();
        Base.systemThreads.add(disposeThread);
        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());
    }

    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProRegular("14"));
        jLabel4.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bCalibrationTest.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel6.setFont(GraphicDesignComponents.getSSProRegular("14"));
        jLabel23.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel24.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel25.setFont(GraphicDesignComponents.getSSProRegular("12"));

    }

    private void setTextLanguage() {
        jLabel1.setText(Languager.getTagValue(1, "CalibrationWizard", "FinalStage_Title"));
        String note = "<html><br><b>" + Languager.getTagValue(1, "CalibrationWizard", "Test_Info_Warning") + "</b></html>";
        jLabel4.setText(splitString(Languager.getTagValue(1, "CalibrationWizard", "Test_Info")) + note);
        bCalibrationTest.setText(Languager.getTagValue(1, "CalibrationWizard", "Test_button"));
        jLabel6.setText(Languager.getTagValue(1, "FeedbackLabel", "MovingMessage"));
        jLabel23.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line4"));
        jLabel24.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line6"));
        jLabel25.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line3"));

    }

    private void centerOnScreen() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        // Determine the new location of the window
        int w = this.getSize().width;
        int h = this.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;

        // Move the window
//        this.setLocation(x,y);
        this.setLocationRelativeTo(null);
        this.setLocationRelativeTo(Base.getMainWindow());
    }

    private String splitString(String s) {
        int width = 436;
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

    private void evaluateInitialConditions() {
        jLabel23.setVisible(false);
        Base.getMainWindow().setEnabled(false);
        disableMessageDisplay();
        if (Boolean.valueOf(ProperDefault.get("firstTime"))) {
            jLabel24.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
        }
        if (ProperDefault.get("maintenance").equals("1")) {
//            jLabel24.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels","b_simple_21.png")));
            jLabel24.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line6"));
        }


        // Base.writeLog("move to rest...");

        Point5d p = machine.getTablePoints("rest");

        double acHigh = machine.getAcceleration("acHigh");
        double acMedium = machine.getAcceleration("acMedium");
        double spHigh = machine.getFeedrate("spHigh");

        machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acMedium, COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spHigh));
        machine.runCommand(new replicatorg.drivers.commands.QueuePoint(p));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acHigh, COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));


    }

    private void enableMessageDisplay() {
        jPanel2.setBackground(new Color(255, 205, 3));
        jLabel6.setForeground(new Color(0, 0, 0));
    }

    private void disableMessageDisplay() {
        jPanel2.setBackground(new Color(248, 248, 248));
        jLabel6.setForeground(new Color(248, 248, 248));
    }

    public void showMessage() {
        enableMessageDisplay();
        jLabel6.setText(Languager.getTagValue(1, "FeedbackLabel", "MovingMessage"));
        moving = true;
    }

    public void resetFeedbackComponents() {
        if (!jLabel5MouseClickedReady) {
            bCalibrationTest.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_16.png")));
            jLabel5MouseClickedReady = true;
        }

        disableMessageDisplay();
        moving = false;
    }

    private File getPrintFile() {
        File gcode = null;
        PrintWriter pw;

        try {
            gcode = new File(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/" + Base.GCODE_TEMP_FILENAME);
            pw = new PrintWriter(new FileOutputStream(gcode));

            String[] code = Base.getMachineLoader().getMachineInterface().getGCodePrintTest("calibration").split(",");

            for (String code1 : code) {
                pw.println(code1.trim());
            }

            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CalibrationPrintTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        return gcode;
    }

    private void doCancel() {
        dispose();
        Base.bringAllWindowsToFront();
        Base.maintenanceWizardOpen = false;
        disposeThread.stop();
        machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
        machine.runCommand(new replicatorg.drivers.commands.SetTemperature(0));
        Point5d b = machine.getTablePoints("safe");
        double acLow = machine.getAcceleration("acLow");
        double acHigh = machine.getAcceleration("acHigh");
        double spHigh = machine.getFeedrate("spHigh");

        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acLow));
        machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spHigh));
        machine.runCommand(new replicatorg.drivers.commands.QueuePoint(b));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acHigh));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28", COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));

        Base.getMainWindow().getButtons().updatePressedStateButton("quick_guide");
        Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
        Base.getMainWindow().setEnabled(true);

        if (ProperDefault.get("maintenance").equals("1")) {
            ProperDefault.remove("maintenance");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        bCalibrationTest = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(572, 493));
        setUndecorated(true);
        setPreferredSize(new java.awt.Dimension(572, 490));

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));

        jPanel5.setBackground(new java.awt.Color(248, 248, 248));
        jPanel5.setMinimumSize(new java.awt.Dimension(62, 26));
        jPanel5.setPreferredSize(new java.awt.Dimension(70, 30));
        jPanel5.setRequestFocusEnabled(false);

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_pressed_9.png"))); // NOI18N
        jLabel15.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel15MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(9, Short.MAX_VALUE))
        );

        jSeparator2.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator2.setForeground(new java.awt.Color(222, 222, 222));
        jSeparator2.setMinimumSize(new java.awt.Dimension(4, 1));
        jSeparator2.setPreferredSize(new java.awt.Dimension(50, 1));

        jLabel1.setText("FIM");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/etapa_final.png"))); // NOI18N

        jLabel4.setText("Suspendisse potenti.");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        bCalibrationTest.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_16.png"))); // NOI18N
        bCalibrationTest.setText("Teste de Calibracao");
        bCalibrationTest.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bCalibrationTest.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bCalibrationTestMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bCalibrationTestMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bCalibrationTestMouseEntered(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(255, 203, 5));
        jPanel2.setPreferredSize(new java.awt.Dimension(169, 17));

        jLabel6.setText("Moving...Please wait.");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel6)
                .addContainerGap(17, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel6)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(208, 208, 208)
                .addComponent(bCalibrationTest)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 129, Short.MAX_VALUE)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(bCalibrationTest)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jPanel6.setBackground(new java.awt.Color(255, 203, 5));
        jPanel6.setMinimumSize(new java.awt.Dimension(20, 38));
        jPanel6.setPreferredSize(new java.awt.Dimension(567, 38));

        jLabel23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        jLabel23.setText("ANTERIOR");
        jLabel23.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel23.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel23MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel23MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel23MousePressed(evt);
            }
        });

        jLabel24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        jLabel24.setText("SEGUINTE");
        jLabel24.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel24.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel24MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel24MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel24MousePressed(evt);
            }
        });

        jLabel25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        jLabel25.setText("SAIR");
        jLabel25.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel25.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel25MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel25MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel25MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel25)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel24)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(jLabel24)
                    .addComponent(jLabel25))
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel25MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel25MouseEntered
        jLabel25.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_jLabel25MouseEntered

    private void jLabel25MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel25MouseExited
        jLabel25.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_jLabel25MouseExited

    private void jLabel23MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel23MouseEntered
        jLabel23.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_jLabel23MouseEntered

    private void jLabel23MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel23MouseExited
        jLabel23.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_jLabel23MouseExited

    private void jLabel24MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel24MouseEntered
        jLabel24.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_jLabel24MouseEntered

    private void jLabel24MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel24MouseExited
        jLabel24.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_jLabel24MouseExited

    private void bCalibrationTestMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCalibrationTestMouseEntered
        bCalibrationTest.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_16.png")));
    }//GEN-LAST:event_bCalibrationTestMouseEntered

    private void bCalibrationTestMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCalibrationTestMouseExited
        bCalibrationTest.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_16.png")));
    }//GEN-LAST:event_bCalibrationTestMouseExited

    private void bCalibrationTestMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCalibrationTestMousePressed

//        if (!moving) {
        bCalibrationTest.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_16.png")));
        CalibrationPrintTest calPT = new CalibrationPrintTest();
        dispose();              // moved here because setVisible blocks for some reason
        disposeThread.stop();
        calPT.setVisible(true);
        //turn off blower before heating
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M107"));
        machine.runCommand(new replicatorg.drivers.commands.SetTemperature(220));
//        }
    }//GEN-LAST:event_bCalibrationTestMousePressed

    private void jLabel24MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel24MousePressed

        dispose();
        disposeThread.stop();
        Base.maintenanceWizardOpen = false;

        ProperDefault.put("nTotalPrints", String.valueOf(0));
        int nCalibrations = Integer.valueOf(ProperDefault.get("nCalibrations"));
        if ((nCalibrations + 1) == 10) {
            ProperDefault.put("nCalibrations", String.valueOf(0));
        } else {
            ProperDefault.put("nCalibrations", String.valueOf(nCalibrations++));
        }

        Base.getMainWindow().getButtons().updatePressedStateButton("quick_guide");
        Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
        Base.getMainWindow().setEnabled(true);

//        if (!machine.getDriver().isBusy()) {
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
//        }
    }//GEN-LAST:event_jLabel24MousePressed

    private void jLabel23MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel23MousePressed
        if (!machine.getDriver().isBusy()) {
            dispose();
            CalibrationSkrew2 p = new CalibrationSkrew2();
            p.setVisible(true);
        }
    }//GEN-LAST:event_jLabel23MousePressed

    private void jLabel25MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel25MousePressed
        doCancel();
    }//GEN-LAST:event_jLabel25MousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        doCancel();
    }//GEN-LAST:event_jLabel15MousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bCalibrationTest;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JSeparator jSeparator2;
    // End of variables declaration//GEN-END:variables
}

class DisposeFeedbackThread5 extends Thread {

    private final MachineInterface machine;
    private final CalibrationFinish calibrationPanel;

    public DisposeFeedbackThread5(CalibrationFinish filIns, MachineInterface mach) {
        super("Calibration Finish Thread");
        this.machine = mach;
        this.calibrationPanel = filIns;
    }

    @Override
    public void run() {

        while (true) {
            machine.runCommand(new replicatorg.drivers.commands.ReadStatus());
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
                Logger.getLogger(DisposeFeedbackThread.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (machine.getModel().getMachineBusy()) {
                calibrationPanel.showMessage();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DisposeFeedbackThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (!machine.getModel().getMachineBusy()) {
                calibrationPanel.resetFeedbackComponents();
            }

        }
    }
}
