package replicatorg.app.ui.panels;

import replicatorg.app.ui.popups.InformationTooltip;
import java.awt.Dialog;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import replicatorg.app.Base;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.app.ui.popups.Warning;

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

    private final Timer resetClickCount = new Timer(1000, (ActionEvent e) -> {
        clickCount = 0;
    });
    private boolean cpEnabled = Boolean.parseBoolean(ProperDefault.get("controlpanel.enable"));
    private InformationTooltip info;
    private int clickCount = 0;

    public About() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        resetClickCount.setRepeats(false);
        initComponents();
        setFont();
        super.enableDrag();
        super.centerOnScreen();
        setTextLanguage();
        setValues();
        super.enableDrag();
    }

    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProLight("33"));
        lVersionTitle.setFont(GraphicDesignComponents.getSSProBold("12"));
        lFirmwareTitle.setFont(GraphicDesignComponents.getSSProBold("12"));
        lBootloaderTitle.setFont(GraphicDesignComponents.getSSProBold("12"));
        lFilamentTitle.setFont(GraphicDesignComponents.getSSProBold("12"));
        lSerialTitle.setFont(GraphicDesignComponents.getSSProBold("12"));

        lVersionValue.setFont(GraphicDesignComponents.getSSProLight("12"));
        lFirmwareValue.setFont(GraphicDesignComponents.getSSProLight("12"));
        lBootloaderValue.setFont(GraphicDesignComponents.getSSProLight("12"));
        lFilamentValue.setFont(GraphicDesignComponents.getSSProLight("12"));
        lSerialValue.setFont(GraphicDesignComponents.getSSProLight("12"));

        jLabel18.setFont(GraphicDesignComponents.getSSProRegular("12"));

    }

    private void setTextLanguage() {
        lVersionTitle.setText(Languager.getTagValue(1, "AboutSoftware", "About_SoftwareVersion"));
        lFirmwareTitle.setText(Languager.getTagValue(1, "AboutSoftware", "About_FirmwareVersion"));
        lBootloaderTitle.setText(Languager.getTagValue(1, "AboutSoftware", "About_BootloaderVersion"));
        lFilamentTitle.setText(Languager.getTagValue(1, "AboutSoftware", "About_FilamentColor"));
        jLabel18.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line6"));
    }

    private void setValues() {
        String beesoft = Base.VERSION_BEESOFT;
        String bootloader = Base.VERSION_BOOTLOADER;
        String firmware = Base.FIRMWARE_IN_USE;
        String coilCode = parseCoilCode();
        String serialNumber = Base.SERIAL_NUMBER;

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

        if (serialNumber == null || serialNumber.equals("9999999999")) {
            serialNumber = Languager.getTagValue(1, "AboutSoftware",
                    "About_NotAvailable");
        }

        lVersionValue.setText(beesoft);
        lFirmwareValue.setText(firmware);
        lBootloaderValue.setText(bootloader);
        lFilamentValue.setText(coilCode);
        lSerialValue.setText(serialNumber);
    }

    private String parseCoilCode() {
        String code = "N/A";

        if (Base.getMachineLoader().isConnected()) {
            code = Base.getMainWindow().getMachineInterface().getDriver().getMachine().getCoilText();
        }

        if (code.equals(FilamentControler.NO_FILAMENT)
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
        lVersionTitle = new javax.swing.JLabel();
        lFirmwareTitle = new javax.swing.JLabel();
        lBootloaderTitle = new javax.swing.JLabel();
        lFilamentTitle = new javax.swing.JLabel();
        lSerialTitle = new javax.swing.JLabel();
        lVersionValue = new javax.swing.JLabel();
        lFirmwareValue = new javax.swing.JLabel();
        lBootloaderValue = new javax.swing.JLabel();
        lFilamentValue = new javax.swing.JLabel();
        jBootloaderTooltip = new javax.swing.JLabel();
        jBootloaderTooltip.setVisible(false);
        lSerialValue = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel1MouseClicked(evt);
            }
        });

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

        lVersionTitle.setText("Version");

        lFirmwareTitle.setText("Firmware");

        lBootloaderTitle.setText("Bootloader");

        lFilamentTitle.setText("Filament type");

        lSerialTitle.setText("Serial number");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lVersionTitle)
                    .addComponent(lFirmwareTitle)
                    .addComponent(lBootloaderTitle)
                    .addComponent(lFilamentTitle)
                    .addComponent(lSerialTitle))
                .addGap(0, 16, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(lVersionTitle)
                .addGap(20, 20, 20)
                .addComponent(lFirmwareTitle)
                .addGap(20, 20, 20)
                .addComponent(lBootloaderTitle)
                .addGap(20, 20, 20)
                .addComponent(lFilamentTitle)
                .addGap(18, 18, 18)
                .addComponent(lSerialTitle)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        lVersionValue.setText("2.0.0");

        lFirmwareValue.setText("2.0.0");

        lBootloaderValue.setLabelFor(lBootloaderValue);
        lBootloaderValue.setText("3.0.0");

        lFilamentValue.setText("A021 - Transparent");

        jBootloaderTooltip.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/alerta.png"))); // NOI18N
        jBootloaderTooltip.setLabelFor(lBootloaderValue);
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

        lSerialValue.setText("9999999999");

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
                            .addComponent(lVersionValue)
                            .addComponent(lFirmwareValue)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lBootloaderValue)
                                .addGap(18, 18, 18)
                                .addComponent(jBootloaderTooltip))
                            .addComponent(lFilamentValue, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lSerialValue, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
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
                        .addComponent(lVersionValue)
                        .addGap(20, 20, 20)
                        .addComponent(lFirmwareValue)
                        .addGap(20, 20, 20)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lBootloaderValue)
                            .addComponent(jBootloaderTooltip))
                        .addGap(18, 18, 18)
                        .addComponent(lFilamentValue)
                        .addGap(18, 18, 18)
                        .addComponent(lSerialValue)
                        .addContainerGap(30, Short.MAX_VALUE))))
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
                .addContainerGap(376, Short.MAX_VALUE)
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
    }//GEN-LAST:event_jLabel18MousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        dispose();
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

    private void jPanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseClicked
        resetClickCount.stop();
        clickCount++;

        if (clickCount >= 10) {
            final Warning warning;
            if (cpEnabled == false) {
                ProperDefault.put("controlpanel.enable", "true");
                Base.getMainWindow().setCPVisibility(true);
                warning = new Warning("CPEnableText", false);
                warning.setVisible(true);
                cpEnabled = !cpEnabled;
                clickCount = 0;
            } else {
                ProperDefault.put("controlpanel.enable", "false");
                Base.getMainWindow().setCPVisibility(false);
                warning = new Warning("CPDisableText", false);
                warning.setVisible(true);
                cpEnabled = !cpEnabled;
                clickCount = 0;
            }
        }

        resetClickCount.start();
    }//GEN-LAST:event_jPanel1MouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jBootloaderTooltip;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel lBootloaderTitle;
    private javax.swing.JLabel lBootloaderValue;
    private javax.swing.JLabel lFilamentTitle;
    private javax.swing.JLabel lFilamentValue;
    private javax.swing.JLabel lFirmwareTitle;
    private javax.swing.JLabel lFirmwareValue;
    private javax.swing.JLabel lSerialTitle;
    private javax.swing.JLabel lSerialValue;
    private javax.swing.JLabel lVersionTitle;
    private javax.swing.JLabel lVersionValue;
    // End of variables declaration//GEN-END:variables

}
