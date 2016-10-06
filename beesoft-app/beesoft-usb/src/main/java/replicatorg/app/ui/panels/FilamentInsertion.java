package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import pt.beeverycreative.beesoft.filaments.Filament;
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
public class FilamentInsertion extends BaseDialog {

    private final Driver driver = Base.getMachineLoader().getMachineInterface().getDriver();
    private final BusyFeedbackThread disposeThread = new BusyFeedbackThread();
    private final Filament selectedFilament;
    private final ImageIcon extLoadImage = new ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/load_filament_external.gif"));
    private final ImageIcon extUnloadImage = new ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/unload_filament_external.gif"));
    private final ImageIcon intLoadImage = new ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/load_filament_internal.gif"));
    private final ImageIcon intUnloadImage = new ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/unload_filament_internal.gif"));

    public FilamentInsertion(Filament selectedFilament) {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        Base.writeLog("Last step of filament change operation", this.getClass());
        initComponents();
        setTextLanguage();
        super.centerOnScreen();
        moveToPosition();
        this.selectedFilament = selectedFilament;

        if (this.selectedFilament != null) {
            if (this.selectedFilament.getMaterial() != Filament.Material.PLA) {
                jPanel6.remove(jInternalImg);
                jPanel6.remove(filler1);
                jPanel6.setPreferredSize(new Dimension(560, 221));
            }
        }

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                disposeThread.kill();
            }
        });
    }

    @Override
    public void resetFeedbackComponents() {
        bLoad.setEnabled(true);
        bUnload.setEnabled(true);
        bNext.setEnabled(true);
        disableMessageDisplay();
    }

    private void setTextLanguage() {
        jLabel1.setText(Languager.getTagValue("FilamentWizard", "Title2"));
        jLabel3.setText(Languager.getTagValue("FilamentWizard", "Exchange_Info_Title"));
        jLabel4.setText("<html><br>" + Languager.getTagValue("FilamentWizard", "Exchange_Info") + "</html>");
        bLoad.setText(Languager.getTagValue("FilamentWizard", "LoadButton"));
        bUnload.setText(Languager.getTagValue("FilamentWizard", "UnloadButton"));
        jLabel7.setText(Languager.getTagValue("FeedbackLabel", "MovingMessage"));
        jLabel7.setHorizontalAlignment(SwingConstants.CENTER);
        bNext.setText(Languager.getTagValue("OptionPaneButtons", "Line6"));
        bExit.setText(Languager.getTagValue("OptionPaneButtons", "Line3"));
    }

    @Override
    public void showMessage() {
        bLoad.setEnabled(false);
        bUnload.setEnabled(false);
        bNext.setEnabled(false);
        enableMessageDisplay();
    }

    private void enableMessageDisplay() {
        jPanel5.setBackground(new Color(255, 205, 3));
        jLabel7.setForeground(new Color(0, 0, 0));
    }

    private void disableMessageDisplay() {
        jPanel5.setBackground(new Color(248, 248, 248));
        jLabel7.setForeground(new Color(248, 248, 248));
    }

    private void moveToPosition() {
        Base.writeLog("Moving to load/unload position", this.getClass());
        driver.setBusy(true);
        driver.dispatchCommand("M703", COM.NO_RESPONSE);
        disposeThread.start();
    }

    private void doCancel() {
        Base.writeLog("Filament load/unload canceled", this.getClass());
        Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
        driver.dispatchCommand("M704", COM.NO_RESPONSE);
        dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        bNext = new javax.swing.JLabel();
        bExit = new javax.swing.JLabel();
        bX = new javax.swing.JLabel();
        bLoad = new javax.swing.JLabel();
        bUnload = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel6 = new javax.swing.JPanel();
        jExternalImg = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0));
        jInternalImg = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMaximumSize(null);
        setMinimumSize(null);
        setUndecorated(true);
        setPreferredSize(null);
        setSize(new java.awt.Dimension(0, 0));

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));

        jLabel1.setFont(new java.awt.Font("Source Sans Pro", 0, 14)); // NOI18N
        jLabel1.setText("Filament Load/Unload");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jLabel3.setFont(new java.awt.Font("Source Sans Pro", 1, 12)); // NOI18N
        jLabel3.setText("Make sure the filament is correctly unloaded before proceeding.");

        jLabel4.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        jLabel4.setText("<html> To unload the filament, push the Unload button and pull the filament steadily.  To load the filament insert it in the inlet hole and push it until it reaches the end. Then click on the Load button, and push the filament a little more until the printer pulls it and extrudes a little filament.  </html>");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel4.setPreferredSize(new java.awt.Dimension(608, 96));

        jPanel5.setBackground(new java.awt.Color(255, 203, 5));
        jPanel5.setPreferredSize(new java.awt.Dimension(169, 17));

        jLabel7.setFont(new java.awt.Font("Source Sans Pro", 0, 14)); // NOI18N
        jLabel7.setText("Moving...Please wait.");
        jLabel7.setPreferredSize(new java.awt.Dimension(137, 18));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 203, 5));
        jPanel2.setMaximumSize(new java.awt.Dimension(648, 26));
        jPanel2.setMinimumSize(new java.awt.Dimension(648, 26));
        jPanel2.setPreferredSize(new java.awt.Dimension(641, 26));

        bNext.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        bNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bNext.setText("Ok");
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

        bX.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_pressed_9.png"))); // NOI18N
        bX.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bXMousePressed(evt);
            }
        });

        bLoad.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        bLoad.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_3.png"))); // NOI18N
        bLoad.setText("Load");
        bLoad.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_3.png"))); // NOI18N
        bLoad.setEnabled(false);
        bLoad.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bLoad.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bLoadMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bLoadMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bLoadMouseEntered(evt);
            }
        });

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

        jSeparator2.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator2.setForeground(new java.awt.Color(222, 222, 222));
        jSeparator2.setMinimumSize(new java.awt.Dimension(4, 1));
        jSeparator2.setPreferredSize(null);

        jPanel6.setBackground(new java.awt.Color(255, 0, 0));
        jPanel6.setOpaque(false);
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        jExternalImg.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jExternalImg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/unload_filament_external.gif"))); // NOI18N
        jExternalImg.setMaximumSize(new java.awt.Dimension(270, 221));
        jExternalImg.setMinimumSize(new java.awt.Dimension(270, 221));
        jExternalImg.setPreferredSize(new java.awt.Dimension(270, 221));
        jPanel6.add(jExternalImg);
        jPanel6.add(filler1);

        jInternalImg.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jInternalImg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/unload_filament_internal.gif"))); // NOI18N
        jInternalImg.setMaximumSize(new java.awt.Dimension(270, 221));
        jInternalImg.setMinimumSize(new java.awt.Dimension(270, 221));
        jInternalImg.setPreferredSize(new java.awt.Dimension(270, 221));
        jPanel6.add(jInternalImg);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(93, 93, 93)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(bX)
                        .addGap(21, 21, 21))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(bUnload)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel3)
                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 552, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(8, 8, 8))
                                .addComponent(bLoad)
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 575, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(26, 26, 26))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(bX, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bLoad)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bUnload)
                .addGap(25, 25, 25)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
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
            Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");

            if (selectedFilament != null) {
                driver.setCoilText(selectedFilament.getName());
            }

            driver.dispatchCommand("G28", COM.NO_RESPONSE);
            dispose();
        }
    }//GEN-LAST:event_bNextMousePressed

    private void bExitMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExitMousePressed
        if (bExit.isEnabled()) {
            doCancel();
        }
    }//GEN-LAST:event_bExitMousePressed

    private void bXMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bXMousePressed
        if (bX.isEnabled()) {
            doCancel();
        }
    }//GEN-LAST:event_bXMousePressed

    private void bLoadMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bLoadMousePressed
        if (bLoad.isEnabled()) {
            Base.writeLog("Loading filament", this.getClass());
            if (this.selectedFilament != null) {
                jInternalImg.setIcon(intLoadImage);
                jExternalImg.setIcon(extLoadImage);
            }
            driver.setBusy(true);
            driver.dispatchCommand("M701", COM.NO_RESPONSE);
        }
    }//GEN-LAST:event_bLoadMousePressed

    private void bUnloadMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bUnloadMousePressed
        if (bUnload.isEnabled()) {
            Base.writeLog("Unloading Filament", this.getClass());
            if (this.selectedFilament != null) {
                jInternalImg.setIcon(intUnloadImage);
                jExternalImg.setIcon(extUnloadImage);
            }
            driver.setCoilText(FilamentControler.NO_FILAMENT);
            driver.setBusy(true);
            driver.dispatchCommand("M702", COM.NO_RESPONSE);
        }
    }//GEN-LAST:event_bUnloadMousePressed

    private void bLoadMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bLoadMouseEntered
        bLoad.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_3.png")));
    }//GEN-LAST:event_bLoadMouseEntered

    private void bLoadMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bLoadMouseExited
        bLoad.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3.png")));
    }//GEN-LAST:event_bLoadMouseExited

    private void bUnloadMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bUnloadMouseEntered
        bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_3_inverted.png")));
    }//GEN-LAST:event_bUnloadMouseEntered

    private void bUnloadMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bUnloadMouseExited
        bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3_inverted.png")));
    }//GEN-LAST:event_bUnloadMouseExited

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bExit;
    private javax.swing.JLabel bLoad;
    private javax.swing.JLabel bNext;
    private javax.swing.JLabel bUnload;
    private javax.swing.JLabel bX;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel jExternalImg;
    private javax.swing.JLabel jInternalImg;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JSeparator jSeparator2;
    // End of variables declaration//GEN-END:variables
}
