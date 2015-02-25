package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import static java.awt.Frame.ICONIFIED;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
public class NozzleClean extends javax.swing.JFrame {

    private MachineInterface machine;
    private int posX = 0, posY = 0;
    private boolean achievement;
    private double heatTemperature = 120;
    private DisposeFeedbackThread6 disposeThread;

    public NozzleClean() {
        initComponents();
        setFont();
        setTextLanguage();
        centerOnScreen();
        machine = Base.getMachineLoader().getMachineInterface();
        achievement = false;
        machine.getDriver().resetToolTemperature();
        evaluateInitialConditions();
//        enableDrag();
        initializeHeatNClean();
        disposeThread = new DisposeFeedbackThread6(this, machine);
        disposeThread.start();
        Base.systemThreads.add(disposeThread);
        Base.maintenanceWizardOpen = true;
        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());

    }

    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProRegular("14"));
        jLabel4.setFont(GraphicDesignComponents.getSSProBold("12"));
        jLabel7.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel5.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel17.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel18.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel19.setFont(GraphicDesignComponents.getSSProRegular("12"));

    }

    private void setTextLanguage() {
        jLabel1.setText(Languager.getTagValue(1, "ExtruderCleanWizard", "Title1"));
        jLabel4.setText(Languager.getTagValue(1, "ExtruderCleanWizard", "Title2"));
        String warning = "<html><br><b>" + Languager.getTagValue(1, "ExtruderCleanWizard", "Info_Warning") + "</b></html>";
        jLabel5.setText(splitString(Languager.getTagValue(1, "ExtruderCleanWizard", "Info") + warning));
        jLabel7.setText(Languager.getTagValue(1, "FeedbackLabel", "HeatingNozzleMessage"));
        jLabel17.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line4"));
        jLabel18.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line7"));
        jLabel19.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line3"));

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

    private void enableMessageDisplay() {
        jPanel3.setBackground(new Color(255, 205, 3));
        jLabel7.setForeground(new Color(0, 0, 0));
    }

    private void disableMessageDisplay() {
        jPanel3.setBackground(new Color(248, 248, 248));
        jLabel7.setForeground(new Color(248, 248, 248));
    }

    public void showMessage() {
        enableMessageDisplay();
        jLabel7.setText(Languager.getTagValue(1, "FeedbackLabel", "HeatingNozzleMessage"));
    }

    public void resetFeedbackComponents() {
        disableMessageDisplay();

    }

    private void initializeHeatNClean() {
        jLabel4.setVisible(false);
        Base.writeLog("Cleaning Nozzle");
        showMessage();
        if (ProperDefault.get("maintenance").equals("1")) {
            Point5d rest = machine.getTablePoints("rest");
            double acLow = machine.getAcceleration("acLow");
            double acHigh = machine.getAcceleration("acHigh");
            double spHigh = machine.getFeedrate("spHigh");

            machine.runCommand(new replicatorg.drivers.commands.SetTemperature(heatTemperature));
            if (Base.printPaused == false) {
                machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
                machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spHigh));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28", COM.BLOCK));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acLow));
                machine.runCommand(new replicatorg.drivers.commands.QueuePoint(rest));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acHigh));
                //turn off blower before heating
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M107"));
                machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
            } else {
                machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acLow));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F" + spHigh + " X-85 Y-65"));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acHigh));
                machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
            }
        }
    }

    private void evaluateInitialConditions() {
        Base.getMainWindow().setEnabled(false);
        disableMessageDisplay();
        if (ProperDefault.get("maintenance").equals("1")) {
            jLabel17.setVisible(false);
            jLabel17.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
            jLabel18.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_18.png")));
            jLabel18.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line6"));
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

    public boolean getAchievement() {
        machine.runCommand(new replicatorg.drivers.commands.ReadTemperature());
        double temperature = machine.getDriver().getTemperature();

        if (temperature < heatTemperature) {
            achievement = false;
        } else {
            achievement = true;
        }
        return achievement;
    }

    public void sinalizeHeatSuccess() {
        disableMessageDisplay();
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300"));
        if (ProperDefault.get("maintenance").equals("1")) {
            jLabel17.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
            jLabel18.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_18.png")));
            jLabel18.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line6"));
        }

    }

    private void doExit() {
        dispose();
        disposeThread.stop();
        Base.bringAllWindowsToFront();
        Base.writeLog("Nozzle cleaned");
        Base.getMainWindow().getButtons().updatePressedStateButton("quick_guide");
        Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
        Base.enableAllOpenWindows();

        machine.runCommand(new replicatorg.drivers.commands.SetTemperature(0));

        if (!Base.printPaused) {
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

        if (ProperDefault.get("maintenance").equals("1")) {
            ProperDefault.remove("maintenance");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setPreferredSize(new java.awt.Dimension(567, 501));
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));

        jPanel4.setBackground(new java.awt.Color(255, 203, 5));
        jPanel4.setMinimumSize(new java.awt.Dimension(62, 26));
        jPanel4.setPreferredSize(new java.awt.Dimension(62, 30));
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
                .addGap(8, 8, 8)
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

        jLabel1.setText("CLEAN EXTRUDER");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/limpeza.png"))); // NOI18N

        jSeparator2.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator2.setForeground(new java.awt.Color(222, 222, 222));
        jSeparator2.setMinimumSize(new java.awt.Dimension(4, 1));
        jSeparator2.setPreferredSize(new java.awt.Dimension(50, 1));

        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("Limpeza do bico de extrusao");

        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("Suspendisse potenti.");
        jLabel5.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jPanel3.setBackground(new java.awt.Color(255, 203, 5));
        jPanel3.setPreferredSize(new java.awt.Dimension(169, 17));

        jLabel7.setForeground(new java.awt.Color(0, 0, 0));
        jLabel7.setText("Heating...Please wait.");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel7)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(11, 11, 11))
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                .addGap(43, 43, 43))
        );

        jPanel2.setBackground(new java.awt.Color(255, 203, 5));
        jPanel2.setMinimumSize(new java.awt.Dimension(20, 27));
        jPanel2.setPreferredSize(new java.awt.Dimension(567, 27));

        jLabel17.setForeground(new java.awt.Color(0, 0, 0));
        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        jLabel17.setText("ANTERIOR");
        jLabel17.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel17.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel17MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel17MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel17MousePressed(evt);
            }
        });

        jLabel18.setForeground(new java.awt.Color(0, 0, 0));
        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        jLabel18.setText("SEGUINTE");
        jLabel18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel18.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel18MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel18MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel18MousePressed(evt);
            }
        });

        jLabel19.setForeground(new java.awt.Color(0, 0, 0));
        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_18.png"))); // NOI18N
        jLabel19.setText("SAIR");
        jLabel19.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel19.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel19MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel19MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel19MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 353, Short.MAX_VALUE)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel18)
                .addGap(12, 12, 12))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(jLabel18)
                    .addComponent(jLabel19))
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel18MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel18MouseEntered
        if (achievement) {
            if (!ProperDefault.get("maintenance").equals("1")) {
                jLabel18.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
            } else {
                jLabel18.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_18.png")));
            }
        }
    }//GEN-LAST:event_jLabel18MouseEntered

    private void jLabel18MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel18MouseExited
        if (achievement) {
            if (!ProperDefault.get("maintenance").equals("1")) {
                jLabel18.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
            } else {
                jLabel18.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_18.png")));
            }
        }
    }//GEN-LAST:event_jLabel18MouseExited

    private void jLabel17MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel17MouseEntered
        if (!ProperDefault.get("maintenance").equals("1") && achievement) {
            jLabel17.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
        }
    }//GEN-LAST:event_jLabel17MouseEntered

    private void jLabel17MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel17MouseExited
        if (!ProperDefault.get("maintenance").equals("1") && achievement) {
            jLabel17.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
        }
    }//GEN-LAST:event_jLabel17MouseExited

    private void jLabel19MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel19MouseEntered
        jLabel19.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_18.png")));
    }//GEN-LAST:event_jLabel19MouseEntered

    private void jLabel19MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel19MouseExited
        jLabel19.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_18.png")));
    }//GEN-LAST:event_jLabel19MouseExited

    private void jLabel18MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel18MousePressed
        if (!ProperDefault.get("maintenance").equals("1") && achievement) {
            dispose();
            disposeThread.stop();
            machine.runCommand(new replicatorg.drivers.commands.SetTemperature(0));
            CalibrationWelcome p = new CalibrationWelcome(false);
            p.setVisible(true);
        } else if (achievement) {
            dispose();
            disposeThread.stop();
            Base.bringAllWindowsToFront();
            Base.maintenanceWizardOpen = false;
            Point5d b = machine.getTablePoints("safe");
            double acLow = machine.getAcceleration("acLow");
            double acHigh = machine.getAcceleration("acHigh");
            double spHigh = machine.getFeedrate("spHigh");

            if (Base.printPaused == false) {
                machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acLow));
                machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spHigh));
                machine.runCommand(new replicatorg.drivers.commands.QueuePoint(b));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acHigh));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28", COM.BLOCK));
                machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
            }
            Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
            Base.enableAllOpenWindows();
        }
    }//GEN-LAST:event_jLabel18MousePressed

    private void jLabel17MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel17MousePressed
        if (!ProperDefault.get("maintenance").equals("1") && achievement) {
            dispose();
            FilamentCodeInsertion p = new FilamentCodeInsertion(machine.getModel().getCoilCode());
            p.setVisible(true);
        }
    }//GEN-LAST:event_jLabel17MousePressed

    private void jLabel19MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel19MousePressed
        doExit();
    }//GEN-LAST:event_jLabel19MousePressed

    private void jLabel13MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel13MousePressed
        setState(ICONIFIED);
    }//GEN-LAST:event_jLabel13MousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        doExit();
    }//GEN-LAST:event_jLabel15MousePressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator2;
    // End of variables declaration//GEN-END:variables
}

class DisposeFeedbackThread6 extends Thread {

    private MachineInterface machine;
    private NozzleClean nozzlePanel;

    public DisposeFeedbackThread6(NozzleClean filIns, MachineInterface mach) {
        super("Nozzle Clean Thread");
        this.machine = mach;
        this.nozzlePanel = filIns;
    }

    @Override
    public void run() {
        boolean temperatureAchieved = false;
        // we'll break on interrupts
        while (!temperatureAchieved) {
            try {
                temperatureAchieved = nozzlePanel.getAchievement();
                Thread.sleep(500);
            } catch (Exception e) {
                Base.writeLog("Exception occured while reading Temperature ...");
                this.stop();
                break;
            }
        }
        Base.writeLog("Temperature achieved...");
        nozzlePanel.sinalizeHeatSuccess();
        this.stop();

    }
}