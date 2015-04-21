package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.machine.MachineInterface;

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
public class Maintenance extends javax.swing.JDialog {

    private int posX = 0, posY = 0;
    private final int NUMBER_PRINTS_LIMIT = 10;
    private boolean moving;
    private final ControlStatus ctrlStatus;
    private boolean isConnected = true;

    public Maintenance() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        setFont();
        setTextLanguage();
        centerOnScreen();
        ProperDefault.put("maintenance", "1");
        enableDrag();
        disableMessageDisplay();
        evaluateInitialConditions();
        Base.maintenanceOpened = true;
        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());
        ctrlStatus = new ControlStatus(this, Base.getMainWindow().getMachineInterface());
        ctrlStatus.start();
        Base.systemThreads.add(ctrlStatus);
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
        bCancel.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel18.setFont(GraphicDesignComponents.getSSProRegular("14"));
    }

    private void setTextLanguage() {
        int fileKey = 1;
        l_tittle.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "Title").toUpperCase());
        lChangeFilament.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "Filament_Title"));
        lChangeFilamentDesc.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "Filament_Intro"));
        bChangeFilament.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "FilamentChange_button"));

//        int grams = gramsCalculator((Double.valueOf(ProperDefault.get("filamentCoilRemaining")) / 1000));
//        
//        if(grams > 0)
//        {
//            jLabel5.setText(Languager.getTagValue("MaintenancePanel", "Filament_Info").split("x")[0]
//                    + String.valueOf(grams)
//                    + Languager.getTagValue("MaintenancePanel", "Filament_Info").split("x")[1].split("\\(")[0]);
//        }
//        else
//        {
//            jLabel5.setText(Languager.getTagValue("MaintenancePanel", "Filament_Info_None"));
//        }
        lCalibration_warn.setText(String.valueOf(ProperDefault.get("nTotalPrints")) + Languager.getTagValue(fileKey, "MaintenancePanel", "Calibration_Info").split("x")[1]);
        bCalibration.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "CalibrationChange_button"));
        lCalibration_desc.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "Calibration_Intro"));
        lCalibration.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "Calibration_Title"));
//        jLabel10.setText("(" + Languager.getTagValue("MaintenancePanel", "Filament_Info").split("x")[1].split("\\(")[1]);                           
        lExtruderMaintenance.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "ExtruderMaintenance_Title"));
        bExtruderMaintenance.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "ExtruderMaintenance_button"));
        lExtruderMaintenanceDesc.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "ExtruderMaintenance_Intro"));
        
        lNozzleSwitch.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "NozzleSwitch_Title"));
        bNozzleSwitch.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "NozzleSwitch_button"));
        lNozzleSwitchDesc.setText(Languager.getTagValue(fileKey, "MaintenancePanel", "NozzleSwitch_Intro"));
//        jLabel16.setText(Languager.getTagValue("MaintenancePanel", ""));
        bCancel.setText(Languager.getTagValue(fileKey, "OptionPaneButtons", "Line6"));
        jLabel18.setText(Languager.getTagValue(fileKey, "FeedbackLabel", "MovingMessage"));

    }

    private int gramsCalculator(double meters) {
        int grams = (int) meters * 12 / 4;

        if (grams > 0) {
            return (int) meters * 12 / 4;
        }

        return 0;
    }

    private void evaluateInitialConditions() {
        moving = true;
        lChangeFilament_warn.setVisible(false);
        jLabel10.setVisible(false);
//        if (gramsCalculator(Double.valueOf(ProperDefault.get("filamentCoilRemaining"))) < 100) {
//            jLabel5.setForeground(Color.red);
//        } else 
        if (Integer.valueOf(ProperDefault.get("nTotalPrints")) > NUMBER_PRINTS_LIMIT) {
            lCalibration_warn.setForeground(Color.red);
        }

        if (Base.printPaused) {
            bCalibration.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_12.png")));
        }

        if (!Base.getMainWindow().getMachineInterface().isConnected()) {
            this.isConnected = false;
            bCalibration.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_12.png")));
            bChangeFilament.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_12.png")));
            bExtruderMaintenance.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_12.png")));
            bNozzleSwitch.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_12.png")));
        }        

        //Disable Power saving in case
        Base.turnOnPowerSaving(false);
    }

    private void centerOnScreen() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        // Determine the new location of the window
        int w = this.getSize().width;
        int h = this.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;

        // Move the window
        this.setLocation(x, y);
        this.setLocationRelativeTo(Base.getMainWindow());
    }

    private void enableDrag() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                posX = e.getX();
                posY = e.getY();
            }
        });

        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent evt) {
                //sets frame position when mouse dragged			
                setLocation(evt.getXOnScreen() - posX, evt.getYOnScreen() - posY);

            }
        });
    }

    public void disableMessageDisplay() {
        l_machine_status_warn.setBackground(new Color(248, 248, 248));
        jLabel18.setForeground(new Color(248, 248, 248));
    }

    private void enableMessageDisplay() {
        l_machine_status_warn.setBackground(new Color(255, 205, 3));
        jLabel18.setForeground(new Color(0, 0, 0));
    }

    public void setBusy() {
        moving = true;
        enableMessageDisplay();
    }

    public void setFree() {
        moving = false;
        disableMessageDisplay();
    }

    private void doExit() {
        dispose();
        ctrlStatus.stop();
        Base.maintenanceOpened = false;
        ProperDefault.remove("maintenance");
        Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
        Base.enableAllOpenWindows();
        Base.bringAllWindowsToFront();

        /**
         * If not autonomous then turn power saving on again
         */
        if (Base.getMainWindow().getMachine().getDriver().isAutonomous() == false) {
            /**
             * Power saving
             */
            Base.turnOnPowerSaving(true);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pMaintenance = new javax.swing.JPanel();
        pTop = new javax.swing.JPanel();
        l_tittle = new javax.swing.JLabel();
        p_controls = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
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
        lCalibration_warn = new javax.swing.JLabel();
        bCalibration = new javax.swing.JLabel();
        lCalibration_desc = new javax.swing.JLabel();
        lCalibration = new javax.swing.JLabel();
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
        pBottom = new javax.swing.JPanel();
        bCancel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(747, 565));
        setUndecorated(true);
        setPreferredSize(new java.awt.Dimension(747, 577));
        setResizable(false);

        pMaintenance.setBackground(new java.awt.Color(248, 248, 248));

        pTop.setBackground(new java.awt.Color(248, 248, 248));

        l_tittle.setForeground(new java.awt.Color(0, 0, 0));
        l_tittle.setText("MANUTENCAO");
        l_tittle.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        p_controls.setBackground(new java.awt.Color(255, 203, 5));
        p_controls.setMinimumSize(new java.awt.Dimension(62, 30));
        p_controls.setRequestFocusEnabled(false);

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

        javax.swing.GroupLayout p_controlsLayout = new javax.swing.GroupLayout(p_controls);
        p_controls.setLayout(p_controlsLayout);
        p_controlsLayout.setHorizontalGroup(
            p_controlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(p_controlsLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 7, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        p_controlsLayout.setVerticalGroup(
            p_controlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(p_controlsLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(p_controlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(p_controlsLayout.createSequentialGroup()
                        .addGroup(p_controlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, 13, Short.MAX_VALUE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        l_machine_status_warn.setBackground(new java.awt.Color(255, 203, 5));

        jLabel18.setForeground(new java.awt.Color(0, 0, 0));
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(p_controls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pTopLayout.setVerticalGroup(
            pTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pTopLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(l_tittle)
                    .addComponent(l_machine_status_warn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(p_controls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        pChangeFilament.setBackground(new java.awt.Color(248, 248, 248));

        bChangeFilament.setForeground(new java.awt.Color(0, 0, 0));
        bChangeFilament.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_12.png"))); // NOI18N
        bChangeFilament.setText("Mudar filamento agora");
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

        lChangeFilament_warn.setForeground(new java.awt.Color(0, 0, 0));
        lChangeFilament_warn.setText("Tem 5 metros de filamento");

        lChangeFilamentDesc.setForeground(new java.awt.Color(0, 0, 0));
        lChangeFilamentDesc.setText("Lorem ipsum dolor sit amet.");

        lChangeFilament.setForeground(new java.awt.Color(0, 0, 0));
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

        lCalibration_warn.setForeground(new java.awt.Color(0, 0, 0));
        lCalibration_warn.setText("Foram feitas 10 impressoes desde a ultima calibracao");

        bCalibration.setForeground(new java.awt.Color(0, 0, 0));
        bCalibration.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_12.png"))); // NOI18N
        bCalibration.setText("Calibrar agora");
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

        lCalibration_desc.setForeground(new java.awt.Color(0, 0, 0));
        lCalibration_desc.setText("Lorem ipsum dolor sit amet.");

        lCalibration.setForeground(new java.awt.Color(0, 0, 0));
        lCalibration.setText("Calibrar");

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
                                .addGap(10, 10, 10)
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

        bExtruderMaintenance.setForeground(new java.awt.Color(0, 0, 0));
        bExtruderMaintenance.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_12.png"))); // NOI18N
        bExtruderMaintenance.setText("Limpar bico agora");
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

        lExtruderMaintenanceDesc.setForeground(new java.awt.Color(0, 0, 0));
        lExtruderMaintenanceDesc.setText("Lorem ipsum dolor sit amet.");

        lExtruderMaintenance.setForeground(new java.awt.Color(0, 0, 0));
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
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        pExtruderMaintenanceLayout.setVerticalGroup(
            pExtruderMaintenanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pExtruderMaintenanceLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lExtruderMaintenance)
                .addGap(2, 2, 2)
                .addComponent(lExtruderMaintenanceDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bExtruderMaintenance)
                .addGap(0, 0, 0))
        );

        pNozzleSwitch.setBackground(new java.awt.Color(248, 248, 248));

        bNozzleSwitch.setForeground(new java.awt.Color(0, 0, 0));
        bNozzleSwitch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_12.png"))); // NOI18N
        bNozzleSwitch.setText("Limpar bico agora");
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

        lNozzleSwitchDesc.setForeground(new java.awt.Color(0, 0, 0));
        lNozzleSwitchDesc.setText("Lorem ipsum dolor sit amet.");

        lNozzleSwitch.setForeground(new java.awt.Color(0, 0, 0));
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
                .addGap(24, 24, 24))
        );

        jSeparator3.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator3.setForeground(new java.awt.Color(222, 222, 222));
        jSeparator3.setMinimumSize(new java.awt.Dimension(4, 1));
        jSeparator3.setPreferredSize(new java.awt.Dimension(1, 1));

        javax.swing.GroupLayout pMaintenanceLayout = new javax.swing.GroupLayout(pMaintenance);
        pMaintenance.setLayout(pMaintenanceLayout);
        pMaintenanceLayout.setHorizontalGroup(
            pMaintenanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pMaintenanceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pMaintenanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pExtruderMaintenance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pCalibration, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pChangeFilament, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pNozzleSwitch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pMaintenanceLayout.setVerticalGroup(
            pMaintenanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pMaintenanceLayout.createSequentialGroup()
                .addComponent(pTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pChangeFilament, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pCalibration, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pExtruderMaintenance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pNozzleSwitch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        pBottom.setBackground(new java.awt.Color(255, 203, 5));
        pBottom.setMinimumSize(new java.awt.Dimension(20, 26));
        pBottom.setPreferredSize(new java.awt.Dimension(139, 26));

        bCancel.setForeground(new java.awt.Color(0, 0, 0));
        bCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_18.png"))); // NOI18N
        bCancel.setText("CANCELAR");
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
            .addComponent(pMaintenance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pBottom, javax.swing.GroupLayout.DEFAULT_SIZE, 747, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pMaintenance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(pBottom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bExtruderMaintenanceMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExtruderMaintenanceMousePressed

        if (!moving && isConnected) {
            dispose();
            ctrlStatus.stop();
            ExtruderMaintenance1 p = new ExtruderMaintenance1();
            p.setVisible(true);

            Base.getMainWindow().getCanvas().unPickAll();
        }
    }//GEN-LAST:event_bExtruderMaintenanceMousePressed

    private void bExtruderMaintenanceMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExtruderMaintenanceMouseExited
    if (isConnected){
        bExtruderMaintenance.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_12.png")));
    }
    }//GEN-LAST:event_bExtruderMaintenanceMouseExited

    private void bExtruderMaintenanceMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExtruderMaintenanceMouseEntered
        if (isConnected) {
            bExtruderMaintenance.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_12.png")));
        }
    }//GEN-LAST:event_bExtruderMaintenanceMouseEntered

    private void bCancelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMousePressed
        doExit();
    }//GEN-LAST:event_bCancelMousePressed

    private void bCancelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseExited
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_18.png")));
    }//GEN-LAST:event_bCancelMouseExited

    private void bCancelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseEntered
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_18.png")));
    }//GEN-LAST:event_bCancelMouseEntered

    private void bNozzleSwitchMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNozzleSwitchMouseEntered
        if (isConnected) {
            bNozzleSwitch.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_12.png")));
        }
    }//GEN-LAST:event_bNozzleSwitchMouseEntered

    private void bNozzleSwitchMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNozzleSwitchMouseExited
        if (isConnected){
            bNozzleSwitch.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_12.png")));
        }
    }//GEN-LAST:event_bNozzleSwitchMouseExited


    private void bNozzleSwitchMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNozzleSwitchMousePressed

        if (!moving && isConnected) {
            dispose();
            ctrlStatus.stop();
            ExtruderSwitch1 p = new ExtruderSwitch1();
            p.setVisible(true);

            Base.getMainWindow().getCanvas().unPickAll();

        }    }//GEN-LAST:event_bNozzleSwitchMousePressed

    private void bCalibrationMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCalibrationMousePressed

        if (!moving && !Base.printPaused && isConnected) {
            dispose();
            ctrlStatus.stop();
            //            Base.getMainWindow().getMachineInterface().runCommand(new replicatorg.drivers.commands.SetBusy(true));
            //            Base.getMainWindow().getMachineInterface().runCommand(new replicatorg.drivers.commands.DispatchCommand("G28",COM.BLOCK));
            //            Base.getMainWindow().getMachineInterface().runCommand(new replicatorg.drivers.commands.SetBusy(false));
            CalibrationWelcome p = new CalibrationWelcome(false);
            p.setVisible(true);
            Base.getMainWindow().getCanvas().unPickAll();
        }
    }//GEN-LAST:event_bCalibrationMousePressed

    private void bCalibrationMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCalibrationMouseExited
        if (!Base.printPaused && isConnected) {
            bCalibration.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_12.png")));
        }
    }//GEN-LAST:event_bCalibrationMouseExited

    private void bCalibrationMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCalibrationMouseEntered
        if (!Base.printPaused && isConnected) {
            bCalibration.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_12.png")));
        }
    }//GEN-LAST:event_bCalibrationMouseEntered

    private void bChangeFilamentMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bChangeFilamentMousePressed

        if (!moving && isConnected) {
            dispose();
            ctrlStatus.stop();
            if (Base.printPaused) {
                FilamentInsertion p = new FilamentInsertion();
                p.setVisible(true);
            } else {
                FilamentHeating p = new FilamentHeating();
                p.setVisible(true);
            }
            Base.getMainWindow().getCanvas().unPickAll();
        }
    }//GEN-LAST:event_bChangeFilamentMousePressed

    private void bChangeFilamentMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bChangeFilamentMouseExited
        if (isConnected){
            bChangeFilament.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_12.png")));
        }
    }//GEN-LAST:event_bChangeFilamentMouseExited

    private void bChangeFilamentMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bChangeFilamentMouseEntered
        if (isConnected) {
            bChangeFilament.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_12.png")));
        }
    }//GEN-LAST:event_bChangeFilamentMouseEntered

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        doExit();
    }//GEN-LAST:event_jLabel15MousePressed

    private void jLabel13MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel13MousePressed
        //setState(ICONIFIED);
    }//GEN-LAST:event_jLabel13MousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bCalibration;
    private javax.swing.JLabel bCancel;
    private javax.swing.JLabel bChangeFilament;
    private javax.swing.JLabel bExtruderMaintenance;
    private javax.swing.JLabel bNozzleSwitch;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
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
    private javax.swing.JPanel l_machine_status_warn;
    private javax.swing.JLabel l_tittle;
    private javax.swing.JPanel pBottom;
    private javax.swing.JPanel pCalibration;
    private javax.swing.JPanel pChangeFilament;
    private javax.swing.JPanel pExtruderMaintenance;
    private javax.swing.JPanel pMaintenance;
    private javax.swing.JPanel pNozzleSwitch;
    private javax.swing.JPanel pTop;
    private javax.swing.JPanel p_controls;
    // End of variables declaration//GEN-END:variables
}

class ControlStatus extends Thread {

    private MachineInterface machine;
    private Maintenance maintenancePanel;

    public ControlStatus(Maintenance filIns, MachineInterface mach) {
        super("Maintenance Thread");
        this.machine = mach;
        this.maintenancePanel = filIns;
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
                maintenancePanel.setBusy();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DisposeFeedbackThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //!machine.getDriver().isBusy()
            if (!machine.getModel().getMachineBusy()) {
                maintenancePanel.setFree();
            }

        }
    }
}
