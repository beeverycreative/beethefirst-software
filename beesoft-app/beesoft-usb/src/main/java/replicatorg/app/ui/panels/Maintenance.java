package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import javax.swing.ImageIcon;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.drivers.Driver;
import replicatorg.machine.model.MachineModel;

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

    private final MachineModel model = Base.getMachineLoader()
            .getMachineInterface().getDriver().getMachine();
    private final ControlStatus ctrlStatus = new ControlStatus();

    public Maintenance() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        Base.writeLog("Maintenance panel opened", this.getClass());
        initComponents();
        setTextLanguage();
        super.centerOnScreen();
        disableMessageDisplay();
        evaluateInitialConditions();

        super.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                ctrlStatus.kill();
            }
        });
    }

    private void setTextLanguage() {
        l_tittle.setText("<html>" + Languager.getTagValue("MaintenancePanel", "Title").toUpperCase() + "</html>");
        lChangeFilament.setText("<html>" + Languager.getTagValue("MaintenancePanel", "Filament_Title") + "</html>");
        lChangeFilamentDesc.setText("<html>" + Languager.getTagValue("MaintenancePanel", "Filament_Intro") + "</html>");
        bChangeFilament.setText(Languager.getTagValue("MaintenancePanel", "FilamentChange_button"));
        lCalibration_warn.setText("<html>" + String.valueOf(ProperDefault.get("nTotalPrints")) + Languager.getTagValue("MaintenancePanel", "Calibration_Info").split("x")[1] + "</html>");
        bCalibration.setText(Languager.getTagValue("MaintenancePanel", "CalibrationChange_button"));
        lCalibration_desc.setText("<html>" + Languager.getTagValue("MaintenancePanel", "Calibration_Intro") + "</html>");
        lCalibration.setText("<html>" + Languager.getTagValue("MaintenancePanel", "Calibration_Title") + "</html>");
        lExtruderMaintenance.setText("<html>" + Languager.getTagValue("MaintenancePanel", "ExtruderMaintenance_Title") + "</html>");
        bExtruderMaintenance.setText(Languager.getTagValue("MaintenancePanel", "ExtruderMaintenance_button"));
        lExtruderMaintenanceDesc.setText("<html>" + Languager.getTagValue("MaintenancePanel", "ExtruderMaintenance_Intro") + "</html>");
        lNozzleSwitch.setText("<html>" + Languager.getTagValue("MaintenancePanel", "NozzleSwitch_Title") + "</html>");
        bNozzleSwitch.setText(Languager.getTagValue("MaintenancePanel", "NozzleSwitch_button"));
        lNozzleSwitchDesc.setText("<html>" + Languager.getTagValue("MaintenancePanel", "NozzleSwitch_Intro") + "</html>");
        lAdditionalNozzleText.setText("<html>" + Languager.getTagValue("MaintenancePanel", "NozzleSwitch_AdditionalNozzle") + "</html>");
        lAdditionalNozzleLink.setText("<html><a href=\"\">" + Languager.getTagValue("MaintenancePanel", "NozzleSwitch_AdditionalNozzleButton") + "</a>.</html>");
        lSupportSwitch.setText("<html>" + Languager.getTagValue("MaintenancePanel", "SupportSwitch_Title") + "</html>");
        bSupportSwitch.setText(Languager.getTagValue("MaintenancePanel", "SupportSwitch_button"));
        lSupportSwitchDesc.setText("<html>" + Languager.getTagValue("MaintenancePanel", "SupportSwitch_Intro") + "</html>");
        bCancel.setText(Languager.getTagValue("OptionPaneButtons", "Line6"));
        jLabel18.setText(Languager.getTagValue("FeedbackLabel", "MovingMessage"));
    }

    private void evaluateInitialConditions() {
        ctrlStatus.start();
        lCurrentNozzle.setText("<html>" + String.format(Locale.US, Languager.getTagValue("MaintenancePanel", "NozzleSwitch_CurrentNozzle"), model.getNozzleType() / 1000.0f));
    }

    private void disableMessageDisplay() {
        l_machine_status_warn.setBackground(new Color(248, 248, 248));
        jLabel18.setForeground(new Color(248, 248, 248));
        bCalibration.setEnabled(true);
        bChangeFilament.setEnabled(true);
        bExtruderMaintenance.setEnabled(true);
        bNozzleSwitch.setEnabled(true);
        bSupportSwitch.setEnabled(true);
    }

    private void enableMessageDisplay() {
        l_machine_status_warn.setBackground(new Color(255, 205, 3));
        jLabel18.setForeground(new Color(0, 0, 0));
        bCalibration.setEnabled(false);
        bChangeFilament.setEnabled(false);
        bExtruderMaintenance.setEnabled(false);
        bNozzleSwitch.setEnabled(false);
        bSupportSwitch.setEnabled(false);
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
        lChangeFilamentDesc = new javax.swing.JLabel();
        lChangeFilament = new javax.swing.JLabel();
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
        lCurrentNozzle = new javax.swing.JLabel();
        lAdditionalNozzleText = new javax.swing.JLabel();
        lAdditionalNozzleLink = new javax.swing.JLabel();
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

        pMaintenance.setBackground(new java.awt.Color(248, 248, 248));

        pTop.setBackground(new java.awt.Color(248, 248, 248));

        l_tittle.setFont(new java.awt.Font("Source Sans Pro", 0, 14)); // NOI18N
        l_tittle.setText("MAINTENANCE");
        l_tittle.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        l_machine_status_warn.setBackground(new java.awt.Color(255, 203, 5));

        jLabel18.setFont(new java.awt.Font("Source Sans Pro", 0, 14)); // NOI18N
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

        bChangeFilament.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        bChangeFilament.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_12.png"))); // NOI18N
        bChangeFilament.setText("Change filament now");
        bChangeFilament.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_12.png"))); // NOI18N
        bChangeFilament.setEnabled(false);
        bChangeFilament.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bChangeFilament.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bChangeFilamentMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bChangeFilamentMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bChangeFilamentMouseEntered(evt);
            }
        });

        lChangeFilamentDesc.setFont(new java.awt.Font("Source Sans Pro Light", 0, 12)); // NOI18N
        lChangeFilamentDesc.setText("This operation is needed if you want to print with a different color or if the filament available in the spool is not enough.");

        lChangeFilament.setFont(new java.awt.Font("Source Sans Pro", 1, 14)); // NOI18N
        lChangeFilament.setText("Change filament");

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
                            .addComponent(bChangeFilament))
                        .addGap(413, 413, 413))
                    .addGroup(pChangeFilamentLayout.createSequentialGroup()
                        .addComponent(lChangeFilamentDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        pChangeFilamentLayout.setVerticalGroup(
            pChangeFilamentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pChangeFilamentLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lChangeFilament)
                .addGap(2, 2, 2)
                .addComponent(lChangeFilamentDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bChangeFilament)
                .addGap(0, 0, 0))
        );

        jSeparator1.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator1.setForeground(new java.awt.Color(222, 222, 222));
        jSeparator1.setMinimumSize(new java.awt.Dimension(4, 1));
        jSeparator1.setPreferredSize(new java.awt.Dimension(1, 1));

        pCalibration.setBackground(new java.awt.Color(248, 248, 248));

        bCalibration.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        bCalibration.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_12.png"))); // NOI18N
        bCalibration.setText("Calibrate now");
        bCalibration.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_12.png"))); // NOI18N
        bCalibration.setEnabled(false);
        bCalibration.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bCalibration.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bCalibrationMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bCalibrationMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bCalibrationMouseEntered(evt);
            }
        });

        lCalibration_desc.setFont(new java.awt.Font("Source Sans Pro Light", 0, 12)); // NOI18N
        lCalibration_desc.setText("This operation is needed to ensure the quality of your 3D prints.");

        lCalibration.setFont(new java.awt.Font("Source Sans Pro", 1, 14)); // NOI18N
        lCalibration.setText("Calibrate");

        lCalibration_warn.setFont(new java.awt.Font("Source Sans Pro Light", 0, 12)); // NOI18N
        lCalibration_warn.setText("10 prints have been made since the last calibration.");

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

        bExtruderMaintenance.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        bExtruderMaintenance.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_12.png"))); // NOI18N
        bExtruderMaintenance.setText("Start extruder maintenance");
        bExtruderMaintenance.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_12.png"))); // NOI18N
        bExtruderMaintenance.setEnabled(false);
        bExtruderMaintenance.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bExtruderMaintenance.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bExtruderMaintenanceMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bExtruderMaintenanceMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bExtruderMaintenanceMouseEntered(evt);
            }
        });

        lExtruderMaintenanceDesc.setFont(new java.awt.Font("Source Sans Pro Light", 0, 12)); // NOI18N
        lExtruderMaintenanceDesc.setText("This operation enables you to unclog the nozzle  using the Maintenance Kit.");

        lExtruderMaintenance.setFont(new java.awt.Font("Source Sans Pro", 1, 14)); // NOI18N
        lExtruderMaintenance.setText("Extruder Maintenance");

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
                        .addGap(0, 0, Short.MAX_VALUE))))
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

        bNozzleSwitch.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        bNozzleSwitch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_12.png"))); // NOI18N
        bNozzleSwitch.setText("Switch nozzle now");
        bNozzleSwitch.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_12.png"))); // NOI18N
        bNozzleSwitch.setEnabled(false);
        bNozzleSwitch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNozzleSwitch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bNozzleSwitchMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bNozzleSwitchMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bNozzleSwitchMouseEntered(evt);
            }
        });

        lNozzleSwitchDesc.setFont(new java.awt.Font("Source Sans Pro Light", 0, 12)); // NOI18N
        lNozzleSwitchDesc.setText("It is necessary to change the nozzle when it's clogged or if you need to print with a nozzle of a different diameter, ex: TPU-FLEX.");

        lNozzleSwitch.setFont(new java.awt.Font("Source Sans Pro", 1, 14)); // NOI18N
        lNozzleSwitch.setText("Switch nozzle");

        lCurrentNozzle.setFont(new java.awt.Font("Source Sans Pro Light", 0, 12)); // NOI18N
        lCurrentNozzle.setText("Current nozzle:");
        lCurrentNozzle.setMaximumSize(new java.awt.Dimension(150, 16));

        lAdditionalNozzleText.setFont(new java.awt.Font("Source Sans Pro Light", 0, 12)); // NOI18N
        lAdditionalNozzleText.setText("<html> If you need additional nozzles please click</html>");

        lAdditionalNozzleLink.setFont(new java.awt.Font("Source Sans Pro Light", 0, 12)); // NOI18N
        lAdditionalNozzleLink.setText("<html>\n<a href=\"\">here</a>. \n</html>");
        lAdditionalNozzleLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lAdditionalNozzleLinkMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout pNozzleSwitchLayout = new javax.swing.GroupLayout(pNozzleSwitch);
        pNozzleSwitch.setLayout(pNozzleSwitchLayout);
        pNozzleSwitchLayout.setHorizontalGroup(
            pNozzleSwitchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pNozzleSwitchLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pNozzleSwitchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lNozzleSwitchDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pNozzleSwitchLayout.createSequentialGroup()
                        .addComponent(lNozzleSwitch)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(pNozzleSwitchLayout.createSequentialGroup()
                        .addComponent(bNozzleSwitch)
                        .addGap(18, 18, 18)
                        .addComponent(lCurrentNozzle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(148, 148, 148)
                        .addComponent(lAdditionalNozzleText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(lAdditionalNozzleLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(44, Short.MAX_VALUE))))
        );
        pNozzleSwitchLayout.setVerticalGroup(
            pNozzleSwitchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pNozzleSwitchLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lNozzleSwitch)
                .addGap(2, 2, 2)
                .addComponent(lNozzleSwitchDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pNozzleSwitchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bNozzleSwitch)
                    .addComponent(lCurrentNozzle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lAdditionalNozzleText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lAdditionalNozzleLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        bSupportSwitch.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        bSupportSwitch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_12.png"))); // NOI18N
        bSupportSwitch.setText("Change support");
        bSupportSwitch.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_12.png"))); // NOI18N
        bSupportSwitch.setEnabled(false);
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

        lSupportSwitchDesc.setFont(new java.awt.Font("Source Sans Pro Light", 0, 12)); // NOI18N
        lSupportSwitchDesc.setText("A simple tutorial that guides you in the installation of the top spool support.");

        lSupportSwitch.setFont(new java.awt.Font("Source Sans Pro", 1, 14)); // NOI18N
        lSupportSwitch.setText("Install top spool support");

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

        pBottom.setBackground(new java.awt.Color(255, 203, 5));
        pBottom.setMinimumSize(new java.awt.Dimension(20, 26));
        pBottom.setPreferredSize(new java.awt.Dimension(139, 26));

        bCancel.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        bCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_18.png"))); // NOI18N
        bCancel.setText("Ok");
        bCancel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bCancelMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bCancelMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bCancelMouseEntered(evt);
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
            .addComponent(pBottom, javax.swing.GroupLayout.DEFAULT_SIZE, 757, Short.MAX_VALUE)
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
                .addGap(12, 12, 12)
                .addComponent(pBottom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pMaintenance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pMaintenance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
            NozzleSwitch2 p = new NozzleSwitch2();
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
            dispose();
            p.setVisible(true);
        }
    }//GEN-LAST:event_bSupportSwitchMousePressed

    private void lAdditionalNozzleLinkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lAdditionalNozzleLinkMouseClicked
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI("https://www.beeverycreative.com/3d-printing-accessories-plugs/"));
            } catch (IOException | URISyntaxException ex) {
                Base.writeLog(ex.getClass().getName() + " when attempting to open the additional nozzles link", this.getClass());
                Base.writeLog(ex.getMessage(), this.getClass());
            }
        } else {
            Base.writeLog("Desktop isn't available or browse action isn't supported. "
                    + "This usually happens on Linux distributions that don't use GNOME.", this.getClass());
        }
    }//GEN-LAST:event_lAdditionalNozzleLinkMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bCalibration;
    private javax.swing.JLabel bCancel;
    private javax.swing.JLabel bChangeFilament;
    private javax.swing.JLabel bExtruderMaintenance;
    private javax.swing.JLabel bNozzleSwitch;
    private javax.swing.JLabel bSupportSwitch;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JLabel lAdditionalNozzleLink;
    private javax.swing.JLabel lAdditionalNozzleText;
    private javax.swing.JLabel lCalibration;
    private javax.swing.JLabel lCalibration_desc;
    private javax.swing.JLabel lCalibration_warn;
    private javax.swing.JLabel lChangeFilament;
    private javax.swing.JLabel lChangeFilamentDesc;
    private javax.swing.JLabel lCurrentNozzle;
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
