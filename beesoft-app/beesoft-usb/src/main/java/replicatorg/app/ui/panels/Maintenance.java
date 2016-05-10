package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
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
public class Maintenance extends BaseDialog {

    private final ControlStatus ctrlStatus = new ControlStatus();

    public Maintenance() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        Base.writeLog("Maintenance panel opened", this.getClass());
        initComponents();
        setFont();
        setTextLanguage();
        centerOnScreen();
        disableMessageDisplay();
        evaluateInitialConditions();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                ctrlStatus.kill();
            }
        });
    }

    private void setFont() {
        l_tittle.setFont(GraphicDesignComponents.getSSProRegular("14"));
        lChangeFilament.setFont(GraphicDesignComponents.getSSProBold("14"));
        lChangeFilamentDesc.setFont(GraphicDesignComponents.getSSProLight("12"));
        bChangeFilament.setFont(GraphicDesignComponents.getSSProRegular("12"));
        lChangeFilament_warn.setFont(GraphicDesignComponents.getSSProRegular("10"));
        lCalibration_warn.setFont(GraphicDesignComponents.getSSProRegular("10"));
        bCalibration.setFont(GraphicDesignComponents.getSSProRegular("12"));
        lCalibration_desc.setFont(GraphicDesignComponents.getSSProLight("12"));
        lCalibration.setFont(GraphicDesignComponents.getSSProBold("14"));
        jLabel10.setFont(GraphicDesignComponents.getSSProRegular("10"));
        bExtruderMaintenance.setFont(GraphicDesignComponents.getSSProRegular("11"));
        lExtruderMaintenanceDesc.setFont(GraphicDesignComponents.getSSProLight("12"));
        lExtruderMaintenance.setFont(GraphicDesignComponents.getSSProBold("14"));
        bNozzleSwitch.setFont(GraphicDesignComponents.getSSProRegular("12"));
        lNozzleSwitchDesc.setFont(GraphicDesignComponents.getSSProLight("12"));
        lNozzleSwitch.setFont(GraphicDesignComponents.getSSProBold("14"));
        bSupportSwitch.setFont(GraphicDesignComponents.getSSProRegular("12"));
        lSupportSwitchDesc.setFont(GraphicDesignComponents.getSSProLight("12"));
        lSupportSwitch.setFont(GraphicDesignComponents.getSSProBold("14"));
        bCancel.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel18.setFont(GraphicDesignComponents.getSSProRegular("14"));
    }

    private void setTextLanguage() {
        int fileKey = 1;
        l_tittle.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "Title").toUpperCase());
        lChangeFilament.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "Filament_Title"));
        lChangeFilamentDesc.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "Filament_Intro"));
        bChangeFilament.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "FilamentChange_button"));
        lCalibration_warn.setText(String.valueOf(ProperDefault.get("nTotalPrints")) + Languager.getTagValue(fileKey, "MaintenancePanel", "Calibration_Info").split("x")[1]);
        bCalibration.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "CalibrationChange_button"));
        lCalibration_desc.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "Calibration_Intro"));
        lCalibration.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "Calibration_Title"));
        lExtruderMaintenance.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "ExtruderMaintenance_Title"));
        bExtruderMaintenance.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "ExtruderMaintenance_button"));
        lExtruderMaintenanceDesc.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "ExtruderMaintenance_Intro"));
        lNozzleSwitch.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "NozzleSwitch_Title"));
        bNozzleSwitch.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "NozzleSwitch_button"));
        lNozzleSwitchDesc.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "NozzleSwitch_Intro"));
        lSupportSwitch.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "SupportSwitch_Title"));
        bSupportSwitch.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "SupportSwitch_button"));
        lSupportSwitchDesc.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "SupportSwitch_Intro"));
        bCancel.setText(Languager.getTagValue(fileKey, "OptionPaneButtons", "Line6"));
        jLabel18.setText(Languager.getTagValue(fileKey, "FeedbackLabel", "MovingMessage"));
    }

    private void evaluateInitialConditions() {
        ctrlStatus.start();
        lChangeFilament_warn.setVisible(false);
        jLabel10.setVisible(false);
    }

    private void disableMessageDisplay() {
        l_machine_status_warn.setBackground(new Color(248, 248, 248));
        jLabel18.setForeground(new Color(248, 248, 248));
        bCalibration.setEnabled(true);
        bChangeFilament.setEnabled(true);
        bExtruderMaintenance.setEnabled(true);
        bNozzleSwitch.setEnabled(true);
    }

    private void enableMessageDisplay() {
        l_machine_status_warn.setBackground(new Color(255, 205, 3));
        jLabel18.setForeground(new Color(0, 0, 0));
        bCalibration.setEnabled(false);
        bChangeFilament.setEnabled(false);
        bExtruderMaintenance.setEnabled(false);
        bNozzleSwitch.setEnabled(false);
    }

    public void setBusy() {
        enableMessageDisplay();
    }

    public void setFree() {
        disableMessageDisplay();
    }

    private void doExit() {
        dispose();
        Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
        Base.enableAllOpenWindows();
        Base.bringAllWindowsToFront();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pMaintenance = new javax.swing.JPanel();
        pTop = new javax.swing.JPanel();
        l_tittle = new javax.swing.JLabel();
        l_machine_status_warn = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        pChangeFilament = new javax.swing.JPanel();
        bChangeFilament = new javax.swing.JLabel();
        lChangeFilament_warn = new javax.swing.JLabel();
        lChangeFilamentDesc = new javax.swing.JLabel();
        lChangeFilament = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        pCalibration = new javax.swing.JPanel();
        bCalibration = new javax.swing.JLabel();
        lCalibration_desc = new javax.swing.JLabel();
        lCalibration = new javax.swing.JLabel();
        lCalibration_warn = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        pExtruderMaintenance = new javax.swing.JPanel();
        bExtruderMaintenance = new javax.swing.JLabel();
        lExtruderMaintenanceDesc = new javax.swing.JLabel();
        lExtruderMaintenance = new javax.swing.JLabel();
        pNozzleSwitch = new javax.swing.JPanel();
        bNozzleSwitch = new javax.swing.JLabel();
        lNozzleSwitchDesc = new javax.swing.JLabel();
        lNozzleSwitch = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        pSupportSwitch = new javax.swing.JPanel();
        bSupportSwitch = new javax.swing.JLabel();
        lSupportSwitchDesc = new javax.swing.JLabel();
        lSupportSwitch = new javax.swing.JLabel();
        pBottom = new javax.swing.JPanel();
        bCancel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setPreferredSize(new java.awt.Dimension(747, 714));
        setResizable(false);

        pMaintenance.setBackground(new java.awt.Color(248, 248, 248));

        pTop.setBackground(new java.awt.Color(248, 248, 248));

        l_tittle.setText("MANUTENCAO");
        l_tittle.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        l_machine_status_warn.setBackground(new java.awt.Color(255, 203, 5));

        jLabel18.setText("Moving...Please wait.");
        jLabel18.setMaximumSize(new java.awt.Dimension(140, 17));
        jLabel18.setMinimumSize(new java.awt.Dimension(140, 17));
        jLabel18.setPreferredSize(new java.awt.Dimension(140, 17));

        javax.swing.GroupLayout l_machine_status_warnLayout = new javax.swing.GroupLayout(l_machine_status_warn);
        l_machine_status_warn.setLayout(l_machine_status_warnLayout);
        l_machine_status_warnLayout.setHorizontalGroup(
            l_machine_status_warnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(l_machine_status_warnLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        l_machine_status_warnLayout.setVerticalGroup(
            l_machine_status_warnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(l_machine_status_warnLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout pTopLayout = new javax.swing.GroupLayout(pTop);
        pTop.setLayout(pTopLayout);
        pTopLayout.setHorizontalGroup(
            pTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pTopLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(l_tittle)
                .addGap(183, 183, 183)
                .addComponent(l_machine_status_warn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pTopLayout.setVerticalGroup(
            pTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pTopLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(l_tittle)
                    .addComponent(l_machine_status_warn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13))
        );

        pChangeFilament.setBackground(new java.awt.Color(248, 248, 248));

        bChangeFilament.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_12.png"))); // NOI18N
        bChangeFilament.setText("Mudar filamento agora");
        bChangeFilament.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_12.png"))); // NOI18N
        bChangeFilament.setEnabled(false);
        bChangeFilament.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bChangeFilament.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bChangeFilamentMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bChangeFilamentMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bChangeFilamentMousePressed(evt);
            }
        });

        lChangeFilament_warn.setText("Tem 5 metros de filamento");

        lChangeFilamentDesc.setText("Lorem ipsum dolor sit amet.");

        lChangeFilament.setText("Mudar FIlamento");

        jLabel10.setForeground(new java.awt.Color(139, 139, 139));
        jLabel10.setText("(estimativa)");

        javax.swing.GroupLayout pChangeFilamentLayout = new javax.swing.GroupLayout(pChangeFilament);
        pChangeFilament.setLayout(pChangeFilamentLayout);
        pChangeFilamentLayout.setHorizontalGroup(
            pChangeFilamentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pChangeFilamentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pChangeFilamentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pChangeFilamentLayout.createSequentialGroup()
                        .addGroup(pChangeFilamentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lChangeFilament)
                            .addGroup(pChangeFilamentLayout.createSequentialGroup()
                                .addComponent(bChangeFilament)
                                .addGap(10, 10, 10)
                                .addComponent(lChangeFilament_warn)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel10)))
                        .addGap(188, 188, 188))
                    .addComponent(lChangeFilamentDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pChangeFilamentLayout.setVerticalGroup(
            pChangeFilamentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pChangeFilamentLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lChangeFilament)
                .addGap(2, 2, 2)
                .addComponent(lChangeFilamentDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pChangeFilamentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bChangeFilament)
                    .addComponent(lChangeFilament_warn)
                    .addComponent(jLabel10))
                .addGap(0, 0, 0))
        );

        jSeparator1.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator1.setForeground(new java.awt.Color(222, 222, 222));
        jSeparator1.setMinimumSize(new java.awt.Dimension(4, 1));
        jSeparator1.setPreferredSize(new java.awt.Dimension(1, 1));

        pCalibration.setBackground(new java.awt.Color(248, 248, 248));

        bCalibration.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_12.png"))); // NOI18N
        bCalibration.setText("Calibrar agora");
        bCalibration.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_12.png"))); // NOI18N
        bCalibration.setEnabled(false);
        bCalibration.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bCalibration.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bCalibrationMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bCalibrationMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bCalibrationMousePressed(evt);
            }
        });

        lCalibration_desc.setText("Lorem ipsum dolor sit amet.");

        lCalibration.setText("Calibrar");

        lCalibration_warn.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
        lCalibration_warn.setText("Foram feitas 10 impressoes desde a ultima calibracao");

        javax.swing.GroupLayout pCalibrationLayout = new javax.swing.GroupLayout(pCalibration);
        pCalibration.setLayout(pCalibrationLayout);
        pCalibrationLayout.setHorizontalGroup(
            pCalibrationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pCalibrationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pCalibrationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lCalibration_desc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pCalibrationLayout.createSequentialGroup()
                        .addGroup(pCalibrationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lCalibration)
                            .addGroup(pCalibrationLayout.createSequentialGroup()
                                .addComponent(bCalibration)
                                .addGap(18, 18, 18)
                                .addComponent(lCalibration_warn)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pCalibrationLayout.setVerticalGroup(
            pCalibrationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pCalibrationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lCalibration)
                .addGap(2, 2, 2)
                .addComponent(lCalibration_desc, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pCalibrationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bCalibration)
                    .addComponent(lCalibration_warn))
                .addGap(0, 0, 0))
        );

        jSeparator2.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator2.setForeground(new java.awt.Color(222, 222, 222));
        jSeparator2.setMinimumSize(new java.awt.Dimension(4, 1));
        jSeparator2.setPreferredSize(new java.awt.Dimension(1, 1));

        pExtruderMaintenance.setBackground(new java.awt.Color(248, 248, 248));

        bExtruderMaintenance.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_12.png"))); // NOI18N
        bExtruderMaintenance.setText("Limpar bico agora");
        bExtruderMaintenance.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_12.png"))); // NOI18N
        bExtruderMaintenance.setEnabled(false);
        bExtruderMaintenance.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bExtruderMaintenance.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bExtruderMaintenanceMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bExtruderMaintenanceMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bExtruderMaintenanceMousePressed(evt);
            }
        });

        lExtruderMaintenanceDesc.setText("Lorem ipsum dolor sit amet.");

        lExtruderMaintenance.setText("Limpar bico de extrusao");

        javax.swing.GroupLayout pExtruderMaintenanceLayout = new javax.swing.GroupLayout(pExtruderMaintenance);
        pExtruderMaintenance.setLayout(pExtruderMaintenanceLayout);
        pExtruderMaintenanceLayout.setHorizontalGroup(
            pExtruderMaintenanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pExtruderMaintenanceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pExtruderMaintenanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pExtruderMaintenanceLayout.createSequentialGroup()
                        .addComponent(bExtruderMaintenance)
                        .addGap(575, 575, 575))
                    .addComponent(lExtruderMaintenanceDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pExtruderMaintenanceLayout.createSequentialGroup()
                        .addComponent(lExtruderMaintenance)
                        .addGap(0, 582, Short.MAX_VALUE))))
        );
        pExtruderMaintenanceLayout.setVerticalGroup(
            pExtruderMaintenanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pExtruderMaintenanceLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lExtruderMaintenance)
                .addGap(2, 2, 2)
                .addComponent(lExtruderMaintenanceDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bExtruderMaintenance)
                .addGap(0, 0, 0))
        );

        pNozzleSwitch.setBackground(new java.awt.Color(248, 248, 248));

        bNozzleSwitch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_12.png"))); // NOI18N
        bNozzleSwitch.setText("Limpar bico agora");
        bNozzleSwitch.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_12.png"))); // NOI18N
        bNozzleSwitch.setEnabled(false);
        bNozzleSwitch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNozzleSwitch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bNozzleSwitchMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bNozzleSwitchMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bNozzleSwitchMousePressed(evt);
            }
        });

        lNozzleSwitchDesc.setText("Lorem ipsum dolor sit amet.");

        lNozzleSwitch.setText("Limpar bico de extrusao");

        javax.swing.GroupLayout pNozzleSwitchLayout = new javax.swing.GroupLayout(pNozzleSwitch);
        pNozzleSwitch.setLayout(pNozzleSwitchLayout);
        pNozzleSwitchLayout.setHorizontalGroup(
            pNozzleSwitchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pNozzleSwitchLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pNozzleSwitchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pNozzleSwitchLayout.createSequentialGroup()
                        .addComponent(bNozzleSwitch)
                        .addGap(575, 575, 575))
                    .addComponent(lNozzleSwitchDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pNozzleSwitchLayout.createSequentialGroup()
                        .addComponent(lNozzleSwitch)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        pNozzleSwitchLayout.setVerticalGroup(
            pNozzleSwitchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pNozzleSwitchLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lNozzleSwitch)
                .addGap(2, 2, 2)
                .addComponent(lNozzleSwitchDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bNozzleSwitch)
                .addContainerGap())
        );

        jSeparator3.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator3.setForeground(new java.awt.Color(222, 222, 222));
        jSeparator3.setMinimumSize(new java.awt.Dimension(4, 1));
        jSeparator3.setPreferredSize(new java.awt.Dimension(1, 1));

        jSeparator4.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator4.setForeground(new java.awt.Color(222, 222, 222));
        jSeparator4.setMinimumSize(new java.awt.Dimension(4, 1));
        jSeparator4.setPreferredSize(new java.awt.Dimension(1, 1));

        pSupportSwitch.setBackground(new java.awt.Color(248, 248, 248));

        bSupportSwitch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_12.png"))); // NOI18N
        bSupportSwitch.setText("Mudar suporte");
        bSupportSwitch.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_12.png"))); // NOI18N
        bSupportSwitch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bSupportSwitch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bSupportSwitchMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bSupportSwitchMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bSupportSwitchMouseEntered(evt);
            }
        });

        lSupportSwitchDesc.setText("Lorem ipsum dolor sit amet.");

        lSupportSwitch.setText("Mudar suporte");

        javax.swing.GroupLayout pSupportSwitchLayout = new javax.swing.GroupLayout(pSupportSwitch);
        pSupportSwitch.setLayout(pSupportSwitchLayout);
        pSupportSwitchLayout.setHorizontalGroup(
            pSupportSwitchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pSupportSwitchLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pSupportSwitchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pSupportSwitchLayout.createSequentialGroup()
                        .addComponent(bSupportSwitch)
                        .addGap(575, 575, 575))
                    .addComponent(lSupportSwitchDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pSupportSwitchLayout.createSequentialGroup()
                        .addComponent(lSupportSwitch)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        pSupportSwitchLayout.setVerticalGroup(
            pSupportSwitchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pSupportSwitchLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lSupportSwitch)
                .addGap(2, 2, 2)
                .addComponent(lSupportSwitchDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bSupportSwitch)
                .addContainerGap())
        );

        javax.swing.GroupLayout pMaintenanceLayout = new javax.swing.GroupLayout(pMaintenance);
        pMaintenance.setLayout(pMaintenanceLayout);
        pMaintenanceLayout.setHorizontalGroup(
            pMaintenanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pMaintenanceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pMaintenanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pExtruderMaintenance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pCalibration, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pChangeFilament, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pNozzleSwitch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pSupportSwitch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSeparator4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pMaintenanceLayout.setVerticalGroup(
            pMaintenanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pMaintenanceLayout.createSequentialGroup()
                .addComponent(pTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pChangeFilament, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pCalibration, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pExtruderMaintenance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pNozzleSwitch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pSupportSwitch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pBottom.setBackground(new java.awt.Color(255, 203, 5));
        pBottom.setMinimumSize(new java.awt.Dimension(20, 26));
        pBottom.setPreferredSize(new java.awt.Dimension(139, 26));

        bCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_18.png"))); // NOI18N
        bCancel.setText("Ok");
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

        javax.swing.GroupLayout pBottomLayout = new javax.swing.GroupLayout(pBottom);
        pBottom.setLayout(pBottomLayout);
        pBottomLayout.setHorizontalGroup(
            pBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pBottomLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bCancel)
                .addGap(12, 12, 12))
        );
        pBottomLayout.setVerticalGroup(
            pBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pBottomLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(bCancel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pMaintenance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(pBottom, javax.swing.GroupLayout.DEFAULT_SIZE, 796, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pMaintenance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(pBottom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bExtruderMaintenanceMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExtruderMaintenanceMousePressed
        if (bExtruderMaintenance.isEnabled()) {
            dispose();
            ExtruderMaintenance1 p = new ExtruderMaintenance1();
            p.setVisible(true);
            Base.getMainWindow().getCanvas().unPickAll();
        }
    }//GEN-LAST:event_bExtruderMaintenanceMousePressed

    private void bExtruderMaintenanceMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExtruderMaintenanceMouseExited
        bExtruderMaintenance.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_12.png")));
    }//GEN-LAST:event_bExtruderMaintenanceMouseExited

    private void bExtruderMaintenanceMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExtruderMaintenanceMouseEntered
        bExtruderMaintenance.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_12.png")));
    }//GEN-LAST:event_bExtruderMaintenanceMouseEntered

    private void bCancelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMousePressed
        if (bCancel.isEnabled()) {
            doExit();
        }
    }//GEN-LAST:event_bCancelMousePressed

    private void bCancelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseExited
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_18.png")));
    }//GEN-LAST:event_bCancelMouseExited

    private void bCancelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseEntered
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_18.png")));
    }//GEN-LAST:event_bCancelMouseEntered

    private void bNozzleSwitchMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNozzleSwitchMouseEntered
        bNozzleSwitch.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_12.png")));
    }//GEN-LAST:event_bNozzleSwitchMouseEntered

    private void bNozzleSwitchMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNozzleSwitchMouseExited
        bNozzleSwitch.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_12.png")));
    }//GEN-LAST:event_bNozzleSwitchMouseExited


    private void bNozzleSwitchMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNozzleSwitchMousePressed
        if (bNozzleSwitch.isEnabled()) {
            dispose();
            ExtruderSwitch1 p = new ExtruderSwitch1();
            p.setVisible(true);
            Base.getMainWindow().getCanvas().unPickAll();

        }
    }//GEN-LAST:event_bNozzleSwitchMousePressed

    private void bCalibrationMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCalibrationMousePressed
        if (bCalibration.isEnabled()) {
            dispose();
            CalibrationWelcome p = new CalibrationWelcome(false);
            p.setVisible(true);
            Base.getMainWindow().getCanvas().unPickAll();
        }
    }//GEN-LAST:event_bCalibrationMousePressed

    private void bCalibrationMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCalibrationMouseExited
        bCalibration.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_12.png")));
    }//GEN-LAST:event_bCalibrationMouseExited

    private void bCalibrationMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCalibrationMouseEntered
        bCalibration.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_12.png")));
    }//GEN-LAST:event_bCalibrationMouseEntered

    private void bChangeFilamentMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bChangeFilamentMousePressed
        if (bChangeFilament.isEnabled()) {
            dispose();
            FilamentCodeInsertion p = new FilamentCodeInsertion();
            p.setVisible(true);
            Base.getMainWindow().getCanvas().unPickAll();
        }
    }//GEN-LAST:event_bChangeFilamentMousePressed

    private void bChangeFilamentMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bChangeFilamentMouseExited
        bChangeFilament.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_12.png")));
    }//GEN-LAST:event_bChangeFilamentMouseExited

    private void bChangeFilamentMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bChangeFilamentMouseEntered
        bChangeFilament.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_12.png")));
    }//GEN-LAST:event_bChangeFilamentMouseEntered

    private void bSupportSwitchMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bSupportSwitchMouseEntered
        bSupportSwitch.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_12.png")));
    }//GEN-LAST:event_bSupportSwitchMouseEntered

    private void bSupportSwitchMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bSupportSwitchMouseExited
        bSupportSwitch.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_12.png")));
    }//GEN-LAST:event_bSupportSwitchMouseExited

    private void bSupportSwitchMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bSupportSwitchMousePressed
        if (bSupportSwitch.isEnabled()) {
            SupportSwitch1 p = new SupportSwitch1();
            p.setVisible(true);
        }
    }//GEN-LAST:event_bSupportSwitchMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bCalibration;
    private javax.swing.JLabel bCancel;
    private javax.swing.JLabel bChangeFilament;
    private javax.swing.JLabel bExtruderMaintenance;
    private javax.swing.JLabel bNozzleSwitch;
    private javax.swing.JLabel bSupportSwitch;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JLabel lCalibration;
    private javax.swing.JLabel lCalibration_desc;
    private javax.swing.JLabel lCalibration_warn;
    private javax.swing.JLabel lChangeFilament;
    private javax.swing.JLabel lChangeFilamentDesc;
    private javax.swing.JLabel lChangeFilament_warn;
    private javax.swing.JLabel lExtruderMaintenance;
    private javax.swing.JLabel lExtruderMaintenanceDesc;
    private javax.swing.JLabel lNozzleSwitch;
    private javax.swing.JLabel lNozzleSwitchDesc;
    private javax.swing.JLabel lSupportSwitch;
    private javax.swing.JLabel lSupportSwitchDesc;
    private javax.swing.JPanel l_machine_status_warn;
    private javax.swing.JLabel l_tittle;
    private javax.swing.JPanel pBottom;
    private javax.swing.JPanel pCalibration;
    private javax.swing.JPanel pChangeFilament;
    private javax.swing.JPanel pExtruderMaintenance;
    private javax.swing.JPanel pMaintenance;
    private javax.swing.JPanel pNozzleSwitch;
    private javax.swing.JPanel pSupportSwitch;
    private javax.swing.JPanel pTop;
    // End of variables declaration//GEN-END:variables

    private class ControlStatus extends Thread {

        private final Driver machine = Base.getMainWindow().getMachineInterface().getDriver();
        private boolean stop = false;

        public ControlStatus() {
            super("Maintenance Thread");
        }

        @Override
        public void run() {
            while (stop == false) {
                if (machine.isBusy()) {
                    setBusy();
                } else {
                    setFree();
                }

                Base.hiccup(100);
            }
        }

        public void kill() {
            stop = true;
            this.interrupt();
        }
    }

}
