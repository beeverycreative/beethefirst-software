package replicatorg.app.ui.panels;

import java.awt.Dialog;
import java.awt.MouseInfo;
import java.awt.Point;
import javax.swing.ImageIcon;
import replicatorg.app.Base;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
import replicatorg.app.Languager;
import replicatorg.app.ui.GraphicDesignComponents;

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
public class About extends BaseDialog {

    private InformationTooltip info;

    public About() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        setFont();
        enableDrag();
        centerOnScreen();
        setTextLanguage();
        setValues();
        enableDrag();
        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());
    }

    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProLight("33"));
        jLabel2.setFont(GraphicDesignComponents.getSSProBold("12"));
        jLabel3.setFont(GraphicDesignComponents.getSSProBold("12"));
        jLabel4.setFont(GraphicDesignComponents.getSSProBold("12"));
        jLabel5.setFont(GraphicDesignComponents.getSSProBold("12"));

        jLabel6.setFont(GraphicDesignComponents.getSSProLight("12"));
        jLabel7.setFont(GraphicDesignComponents.getSSProLight("12"));
        jLabel8.setFont(GraphicDesignComponents.getSSProLight("12"));
        jLabel9.setFont(GraphicDesignComponents.getSSProLight("12"));

        jLabel18.setFont(GraphicDesignComponents.getSSProRegular("12"));

    }

    private void setTextLanguage() {
        jLabel2.setText(Languager.getTagValue(1, "AboutSoftware", "About_SoftwareVersion"));
        jLabel3.setText(Languager.getTagValue(1, "AboutSoftware", "About_FirmwareVersion"));
        jLabel4.setText(Languager.getTagValue(1, "AboutSoftware", "About_BootloaderVersion"));
        jLabel5.setText(Languager.getTagValue(1, "AboutSoftware", "About_FilamentColor"));
        jLabel18.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line6"));
    }

    private void setValues() {
        String beesoft = Base.VERSION_BEESOFT;
        String bootloader = Base.VERSION_BOOTLOADER;
        String firmware = Base.FIRMWARE_IN_USE;
        String coilCode = parseCoilCode();

        if (bootloader == null || bootloader.equals("0.0.0")) {
            bootloader = Languager.getTagValue(1, "AboutSoftware",
                    "About_NotAvailable");

            if (Base.getMachineLoader().isConnected()) {
                jBootloaderTooltip.setVisible(true);
            }
        }

        if (firmware == null || firmware.contains("UNKNOWN")) {
            firmware = Languager.getTagValue(1, "AboutSoftware",
                    "About_NotAvailable");
        }

        if (coilCode.equals("N/A")) {
            coilCode = Languager.getTagValue(1, "AboutSoftware",
                    "About_NotAvailable");
        }

        jLabel6.setText(beesoft);
        jLabel7.setText(firmware);
        jLabel8.setText(bootloader);
        jLabel9.setText(coilCode);
    }

    private String parseCoilCode() {
        String code = "N/A";

        if (Base.getMachineLoader().isConnected()) {
            code = Base.getMainWindow().getMachineInterface().getDriver().getMachine().getCoilText();
        }

        if(code.equals(FilamentControler.NO_FILAMENT)
                || code.contains(FilamentControler.NO_FILAMENT_2)) {
            return "No filament currently loaded";
        }
        
        return code;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jBootloaderTooltip = new javax.swing.JLabel();
        jBootloaderTooltip.setVisible(false);
        jPanel2 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));

        jLabel1.setText("BEESOFT");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

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

        jPanel3.setBackground(new java.awt.Color(248, 248, 248));

        jLabel2.setText("Version");

        jLabel3.setText("Firmware");

        jLabel4.setText("Bootloader");

        jLabel5.setText("Serial Number");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addGap(0, 14, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(20, 20, 20)
                .addComponent(jLabel3)
                .addGap(20, 20, 20)
                .addComponent(jLabel4)
                .addGap(20, 20, 20)
                .addComponent(jLabel5)
                .addGap(0, 30, Short.MAX_VALUE))
        );

        jLabel6.setText("2.0.0");

        jLabel7.setText("2.0.0");

        jLabel8.setLabelFor(jLabel8);
        jLabel8.setText("3.0.0");

        jLabel9.setText("000000000014");

        jBootloaderTooltip.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/alerta.png"))); // NOI18N
        jBootloaderTooltip.setLabelFor(jLabel8);
        jBootloaderTooltip.setToolTipText("");
        jBootloaderTooltip.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBootloaderTooltip.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jBootloaderTooltipMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jBootloaderTooltipMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jBootloaderTooltipMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addGap(18, 18, 18)
                                .addComponent(jBootloaderTooltip))
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 68, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(20, 20, 20)
                        .addComponent(jLabel7)
                        .addGap(20, 20, 20)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jBootloaderTooltip))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel9)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(0, 0, 0))
        );

        jPanel2.setBackground(new java.awt.Color(255, 203, 5));
        jPanel2.setMinimumSize(new java.awt.Dimension(20, 46));

        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_18.png"))); // NOI18N
        jLabel18.setText("OK");
        jLabel18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel18.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel18MousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel18MouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel18MouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(364, Short.MAX_VALUE)
                .addComponent(jLabel18)
                .addGap(12, 12, 12))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jLabel18)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel18MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel18MouseEntered
        jLabel18.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_18.png")));
    }//GEN-LAST:event_jLabel18MouseEntered

    private void jLabel18MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel18MouseExited
        jLabel18.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_18.png")));
    }//GEN-LAST:event_jLabel18MouseExited

    private void jLabel18MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel18MousePressed
        dispose();
        Base.bringAllWindowsToFront();
    }//GEN-LAST:event_jLabel18MousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        dispose();
        Base.bringAllWindowsToFront();
    }//GEN-LAST:event_jLabel15MousePressed

    private void jBootloaderTooltipMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBootloaderTooltipMouseEntered
        Point cursorLocation = MouseInfo.getPointerInfo().getLocation();
        cursorLocation.setLocation(cursorLocation.getX() + 20, cursorLocation.getY() - 2);
        info = new InformationTooltip(this, Languager.getTagValue(1,
                "AboutSoftware", "About_BootloaderVersionNotAvailableText"));
        info.setLocation(cursorLocation);
        info.setVisible(true);
    }//GEN-LAST:event_jBootloaderTooltipMouseEntered

    private void jBootloaderTooltipMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBootloaderTooltipMouseExited
        info.dispose();
    }//GEN-LAST:event_jBootloaderTooltipMouseExited

    private void jBootloaderTooltipMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBootloaderTooltipMouseClicked
        // not working
        this.setFocusable(false);
        info.requestFocusInWindow();
        info.toFront();
        this.toBack();
    }//GEN-LAST:event_jBootloaderTooltipMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jBootloaderTooltip;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    // End of variables declaration//GEN-END:variables
}
