package replicatorg.app.ui.panels;

import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
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
public class NozzleSwitch3 extends BaseDialog {

    private final Driver driver = Base.getMachineLoader().getMachineInterface().getDriver();
    private final BusyFeedbackThread busyFeedbackThread = new BusyFeedbackThread();

    public NozzleSwitch3() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        super.enableDrag();
        super.centerOnScreen();
        setTextLanguage();
        driver.setBusy(true);
        driver.dispatchCommand("M703", COM.NO_RESPONSE);

        super.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                busyFeedbackThread.start();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                busyFeedbackThread.kill();
                Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
            }
        });

    }

    private void setTextLanguage() {
        
        lTitle.setText("<html>" + Languager.getTagValue("NozzleSwitch", "Title"));
        lFilamentUnloadTitle.setText("<html>" + Languager.getTagValue("NozzleSwitch", "FilamentUnloadTitle"));
        lFilamentUnloadText.setText("<html>" + Languager.getTagValue("NozzleSwitch", "FilamentUnloadText"));

        bNext.setText(Languager.getTagValue("OptionPaneButtons", "Line7"));
        bExit.setText(Languager.getTagValue("OptionPaneButtons", "Line3"));

    }

    @Override
    public void showMessage() {
        //bLoad.setEnabled(false);
        bUnload.setEnabled(false);
        bNext.setEnabled(false);
    }

    @Override
    public void resetFeedbackComponents() {
        //bLoad.setEnabled(true);
        bUnload.setEnabled(true);
        bNext.setEnabled(true);
    }

    private void doCancel() {
        dispose();
        driver.dispatchCommand("G28", COM.NO_RESPONSE);
        Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lTitle = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        iInfographic = new javax.swing.JLabel();
        bUnload = new javax.swing.JLabel();
        jExternalImg = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        lFilamentUnloadTitle = new javax.swing.JLabel();
        lFilamentUnloadText = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel2 = new javax.swing.JPanel();
        bNext = new javax.swing.JLabel();
        bExit = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(248, 248, 248));
        setMinimumSize(null);
        setUndecorated(true);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));
        jPanel1.setMaximumSize(new java.awt.Dimension(567, 501));
        jPanel1.setPreferredSize(new java.awt.Dimension(567, 501));
        jPanel1.setRequestFocusEnabled(false);

        lTitle.setBackground(new java.awt.Color(248, 248, 248));
        lTitle.setFont(new java.awt.Font("Source Sans Pro", 0, 14)); // NOI18N
        lTitle.setText("Nozzle Switch");
        lTitle.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jPanel3.setBackground(new java.awt.Color(248, 248, 248));

        iInfographic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/unload_filament_internal.gif"))); // NOI18N

        bUnload.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        bUnload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_3_inverted.png"))); // NOI18N
        bUnload.setText("Unload");
        bUnload.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_3_inverted.png"))); // NOI18N
        bUnload.setEnabled(false);
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

        jExternalImg.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jExternalImg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/unload_filament_external.gif"))); // NOI18N
        jExternalImg.setMaximumSize(new java.awt.Dimension(270, 221));
        jExternalImg.setMinimumSize(new java.awt.Dimension(270, 221));
        jExternalImg.setPreferredSize(new java.awt.Dimension(270, 221));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jExternalImg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(iInfographic, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(bUnload, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(21, 21, 21))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jExternalImg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(iInfographic)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bUnload)
                .addContainerGap())
        );

        jPanel4.setBackground(new java.awt.Color(248, 248, 248));
        jPanel4.setMinimumSize(new java.awt.Dimension(62, 26));
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lFilamentUnloadTitle.setFont(new java.awt.Font("Source Sans Pro", 1, 14)); // NOI18N
        lFilamentUnloadTitle.setText("Make sure the filament is correctly unloaded before proceeding.");

        lFilamentUnloadText.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        lFilamentUnloadText.setText("<html> To unload the filament, push the Unload button and pull the filament steadily.</html>");
        lFilamentUnloadText.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lFilamentUnloadText.setPreferredSize(new java.awt.Dimension(608, 96));

        jSeparator2.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator2.setForeground(new java.awt.Color(222, 222, 222));
        jSeparator2.setMinimumSize(new java.awt.Dimension(4, 1));
        jSeparator2.setPreferredSize(new java.awt.Dimension(50, 1));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lFilamentUnloadTitle)
                            .addComponent(lFilamentUnloadText, javax.swing.GroupLayout.PREFERRED_SIZE, 552, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 107, Short.MAX_VALUE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lTitle, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lFilamentUnloadTitle)
                .addGap(26, 26, 26)
                .addComponent(lFilamentUnloadText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );

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

        bExit.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        bExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bExit.setText("Cancel");
        bExit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bExit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bExitMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bExitMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bExitMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bExit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bNext)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bNext)
                    .addComponent(bExit))
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 665, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 665, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 516, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bExitMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExitMouseEntered
        bExit.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bExitMouseEntered

    private void bExitMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExitMouseExited
        bExit.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bExitMouseExited

    private void bNextMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNextMouseEntered
        bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bNextMouseEntered

    private void bNextMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNextMouseExited
        bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bNextMouseExited

    private void bNextMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNextMousePressed
        if (bNext.isEnabled()) {
            dispose();
            NozzleSwitch4 p = new NozzleSwitch4();
            p.setVisible(true);
        }
    }//GEN-LAST:event_bNextMousePressed

    private void bExitMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExitMousePressed
        if (bExit.isEnabled()) {
            doCancel();
        }
    }//GEN-LAST:event_bExitMousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        if (jLabel15.isEnabled()) {
            doCancel();
        }
    }//GEN-LAST:event_jLabel15MousePressed

    private void bUnloadMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bUnloadMousePressed
        if (bUnload.isEnabled()) {
            Base.writeLog("Unload filament pressed", this.getClass());
            driver.setCoilText(FilamentControler.NO_FILAMENT);
            driver.setBusy(true);
            driver.dispatchCommand("M702", COM.NO_RESPONSE);
        }
    }//GEN-LAST:event_bUnloadMousePressed

    private void bUnloadMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bUnloadMouseExited
        bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3_inverted.png")));
    }//GEN-LAST:event_bUnloadMouseExited

    private void bUnloadMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bUnloadMouseEntered
        bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_3_inverted.png")));
    }//GEN-LAST:event_bUnloadMouseEntered

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bExit;
    private javax.swing.JLabel bNext;
    private javax.swing.JLabel bUnload;
    private javax.swing.JLabel iInfographic;
    private javax.swing.JLabel jExternalImg;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lFilamentUnloadText;
    private javax.swing.JLabel lFilamentUnloadTitle;
    private javax.swing.JLabel lTitle;
    // End of variables declaration//GEN-END:variables

}
