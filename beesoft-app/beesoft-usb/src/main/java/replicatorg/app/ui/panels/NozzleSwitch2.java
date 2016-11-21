package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.drivers.Driver;

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
public class NozzleSwitch2 extends BaseDialog {

    private final TemperatureThread temperatureThread = new TemperatureThread();
    private final Driver driver = Base.getMachineLoader()
            .getMachineInterface().getDriver();

    public NozzleSwitch2() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        super.enableDrag();
        super.centerOnScreen();
        setTextLanguage();
        evaluateInitialConditions();

        super.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                temperatureThread.start();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                temperatureThread.kill();
            }
        });

    }

    private void setTextLanguage() {
        lTitle.setText(Languager.getTagValue("NozzleSwitch", "Title"));
        lInfo1.setText(Languager.getTagValue("NozzleSwitch", "NozzleHeatingInfo1"));
        lInfo2.setText(Languager.getTagValue("NozzleSwitch", "NozzleHeatingInfo2"));
        pWarning.setText("<html>" + Languager.getTagValue("NozzleSwitch", "HeatingMessage"));
        lWarning.setText("<html>" + Languager.getTagValue("NozzleSwitch", "Info_Warning"));
        bNext.setText(Languager.getTagValue("OptionPaneButtons", "Line7"));
        bQuit.setText(Languager.getTagValue("OptionPaneButtons", "Line3"));
    }

    @Override
    protected void updateHeatBar(int nozzleTemperature) {
        if (nozzleTemperature > jProgressBar1.getValue()) {
            jProgressBar1.setValue(nozzleTemperature);
        }
        double temperatureError = (GENERIC_TEMPERATURE_GOAL - nozzleTemperature)/(double)GENERIC_TEMPERATURE_GOAL;
        if (temperatureError < 0.01) {
            Base.writeLog("Temperature achieved...", this.getClass());
            temperatureThread.kill();
            disableMessageDisplay();
            driver.setTemperature(GENERIC_TEMPERATURE_GOAL);
            driver.dispatchCommand("M300");
            bNext.setEnabled(true);
        }
    }

    private void enableMessageDisplay() {
        jPanel3.setBackground(new Color(255, 205, 3));
        pWarning.setForeground(new Color(0, 0, 0));
    }

    public void disableMessageDisplay() {
        jPanel3.setBackground(new Color(248, 248, 248));
        pWarning.setForeground(new Color(248, 248, 248));
    }

    @Override
    public void showMessage() {
        enableMessageDisplay();
    }

    private void evaluateInitialConditions() {
        disableMessageDisplay();
        jProgressBar1.setMaximum(GENERIC_TEMPERATURE_GOAL);

        driver.readTemperature();
        updateHeatBar(driver.getMachine().currentTool().getExtruderTemperature());
    }

    private void doCancel() {
        dispose();
        driver.dispatchCommand("G28", COM.NO_RESPONSE);
        Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        bNext = new javax.swing.JLabel();
        bQuit = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        lTitle = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jSeparator2 = new javax.swing.JSeparator();
        lInfo1 = new javax.swing.JLabel();
        lWarning = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        pWarning = new javax.swing.JLabel();
        lInfo2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        jPanel2.setBackground(new java.awt.Color(255, 203, 5));
        jPanel2.setMinimumSize(new java.awt.Dimension(20, 38));
        jPanel2.setPreferredSize(new java.awt.Dimension(567, 38));

        bNext.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        bNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bNext.setText("Next");
        bNext.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_21.png"))); // NOI18N
        bNext.setEnabled(false);
        bNext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNext.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bNextMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bNextMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bNextMouseEntered(evt);
            }
        });

        bQuit.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        bQuit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bQuit.setText("Cancel");
        bQuit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bQuit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bQuitMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bQuitMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bQuitMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bQuit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 411, Short.MAX_VALUE)
                .addComponent(bNext)
                .addGap(12, 12, 12))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bNext)
                    .addComponent(bQuit))
                .addGap(20, 20, 20))
        );

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));

        lTitle.setFont(new java.awt.Font("Source Sans Pro", 0, 14)); // NOI18N
        lTitle.setText("Nozzle Switch");
        lTitle.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        lTitle.setMaximumSize(null);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/bico_extrusao.png"))); // NOI18N

        jProgressBar1.setBackground(new java.awt.Color(186, 186, 186));
        jProgressBar1.setForeground(new java.awt.Color(255, 203, 5));
        jProgressBar1.setPreferredSize(new java.awt.Dimension(150, 18));

        jSeparator2.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator2.setForeground(new java.awt.Color(222, 222, 222));
        jSeparator2.setMinimumSize(new java.awt.Dimension(4, 1));
        jSeparator2.setPreferredSize(new java.awt.Dimension(50, 1));

        lInfo1.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        lInfo1.setText("<html> The extruder is currently heating, please wait.");

        lWarning.setFont(new java.awt.Font("Source Sans Pro", 1, 12)); // NOI18N
        lWarning.setText("<html>WARNING: BE CAREFUL NOT TO TOUCH THE NOZZLE WITH YOUR SKIN DURING THIS PROCESS.");
        lWarning.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jPanel4.setBackground(new java.awt.Color(248, 248, 248));
        jPanel4.setMinimumSize(new java.awt.Dimension(62, 26));
        jPanel4.setPreferredSize(new java.awt.Dimension(70, 30));
        jPanel4.setRequestFocusEnabled(false);

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_pressed_9.png"))); // NOI18N
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
                .addContainerGap(9, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 203, 5));
        jPanel3.setPreferredSize(new java.awt.Dimension(169, 17));

        pWarning.setFont(new java.awt.Font("Source Sans Pro", 0, 14)); // NOI18N
        pWarning.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        pWarning.setText("Heating...");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pWarning, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(pWarning)
                .addGap(0, 0, 0))
        );

        lInfo2.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        lInfo2.setText("<html> When the extruder has reached the ideal temperature, the progress bar will be full.");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(62, 62, 62)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 547, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(lWarning)))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(96, 96, 96)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lInfo1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lInfo2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lInfo1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lInfo2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(lWarning, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 571, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bQuitMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bQuitMouseEntered
        bQuit.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bQuitMouseEntered

    private void bQuitMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bQuitMouseExited
        bQuit.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bQuitMouseExited

    private void bNextMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNextMouseEntered
        if (bNext.isEnabled()) {
            bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
        }
    }//GEN-LAST:event_bNextMouseEntered

    private void bNextMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNextMouseExited
        bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bNextMouseExited

    private void bNextMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNextMousePressed
        if (bNext.isEnabled()) {
            dispose();
            NozzleSwitch3 p = new NozzleSwitch3();
            p.setVisible(true);
        }
    }//GEN-LAST:event_bNextMousePressed

    private void bQuitMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bQuitMousePressed
        if (bQuit.isEnabled()) {
            doCancel();
        }
    }//GEN-LAST:event_bQuitMousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        if (jLabel15.isEnabled()) {
            doCancel();
        }
    }//GEN-LAST:event_jLabel15MousePressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bNext;
    private javax.swing.JLabel bQuit;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lInfo1;
    private javax.swing.JLabel lInfo2;
    private javax.swing.JLabel lTitle;
    private javax.swing.JLabel lWarning;
    private javax.swing.JLabel pWarning;
    // End of variables declaration//GEN-END:variables
}
