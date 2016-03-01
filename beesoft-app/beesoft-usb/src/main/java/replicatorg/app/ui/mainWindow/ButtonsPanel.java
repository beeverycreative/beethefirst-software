package replicatorg.app.ui.mainWindow;

import javax.swing.ImageIcon;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import static replicatorg.app.ui.GraphicDesignComponents.getSSProLight;
import static replicatorg.app.ui.GraphicDesignComponents.getSSProRegular;
import replicatorg.app.ui.panels.EstimateExportPanel;
import replicatorg.app.ui.panels.PrintPanel;
import replicatorg.app.ui.panels.TourWelcome;
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
public class ButtonsPanel extends javax.swing.JPanel {

    private static final int NUMBER_PRINTS_LIMIT = 15;
    private final replicatorg.app.ui.MainWindow editor;
    private boolean printPanel = false;

    public ButtonsPanel(replicatorg.app.ui.MainWindow mainWindow) {
        initComponents();
        setFont();
        editor = mainWindow;
        setTextLanguage();
    }

    private void setTextLanguage() {
        bModels.setText(Languager.getTagValue(1, "MainWindowButtons", "Models"));
        bMaintenance.setText(Languager.getTagValue(1, "MainWindowButtons", "Maintenance"));
        bQuickGuide.setText(Languager.getTagValue(1, "MainWindowButtons", "QuickWizard"));
        bPrint.setText(Languager.getTagValue(1, "MainWindowButtons", "Print"));
    }

    public void setLogo(String iconPath) {
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/" + iconPath)));
    }

    public void setMessage(String message) {
        if (message.equals("is connecting")) {
            jLabel3.setText(Languager.getTagValue(1, "FeedbackLabel", "PrinterStatusConnecting"));
            bPrint.setEnabled(false);
        } else if (message.equals("is connected")) {
            jLabel3.setText(Languager.getTagValue(1, "FeedbackLabel", "PrinterStatusReady"));
            bPrint.setText(Languager.getTagValue(1, "MainWindowButtons", "Print"));
            printPanel = true;
            bPrint.setEnabled(true);
        } else if (message.equals("power saving")) {
            jLabel3.setText(Languager.getTagValue(1, "FeedbackLabel", "PrinterStatusPowerSaving"));
        } else if (message.equals("is disconnected")) {
            jLabel3.setText(Languager.getTagValue(1, "FeedbackLabel", "PrinterStatusDisconnected"));
            bPrint.setText("Estimate / Export");
            printPanel = false;
            bPrint.setEnabled(true);
        }
    }

    private void setFont() {
        jLabel3.setFont(getSSProLight("18"));
        bModels.setFont(getSSProLight("13"));
        bQuickGuide.setFont(getSSProLight("13"));
        bMaintenance.setFont(getSSProLight("13"));
        bPrint.setFont(getSSProRegular("13"));

    }

    public void updatePressedStateButton(String button) {
        if (button.equals("models")) {
            bModels.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
        }

        if (button.equals("maintenance")) {
            if (Integer.valueOf(ProperDefault.get("nTotalPrints")) <= NUMBER_PRINTS_LIMIT) {
                bMaintenance.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
            } else if (Integer.valueOf(ProperDefault.get("nTotalPrints")) > NUMBER_PRINTS_LIMIT) {
                bMaintenance.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7_btn.png")));
            }
        }

        if (button.equals("quick_guide")) {
            bQuickGuide.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
        }

        if (button.equals("print")) {
            bPrint.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_2.png")));
        }

    }

    public void connect() {
        updateFromState(Base.getMainWindow().getMachineInterface());
    }

    private void updateFromState(final MachineInterface s) {

        if (!s.isConnected()) {
            setMessage("is disconnected");
        } else {

            bQuickGuide.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));

            if (Integer.valueOf(ProperDefault.get("nTotalPrints")) <= NUMBER_PRINTS_LIMIT) {
                bMaintenance.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
            } else if (Integer.valueOf(ProperDefault.get("nTotalPrints")) > NUMBER_PRINTS_LIMIT) {
                bMaintenance.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7_btn.png")));
            }

            bPrint.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_2.png")));
            setMessage("is connected");
        }
    }

    public void updateFromMachine(final MachineInterface machine) {
        if (machine != null) {
            updateFromState(machine);
        }
    }

    public void bMaintenanceSetEnabled(boolean enabled) {
        bMaintenance.setEnabled(enabled);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        bPrint = new javax.swing.JLabel();
        bMaintenance = new javax.swing.JLabel();
        bQuickGuide = new javax.swing.JLabel();
        bModels = new javax.swing.JLabel();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(240, 243, 244)));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/logo_beethefirst.png"))); // NOI18N

        jLabel3.setText("default: not connected");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                .addGap(18, 18, 18))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(240, 243, 244)));
        jPanel3.setPreferredSize(new java.awt.Dimension(681, 48));

        bPrint.setForeground(new java.awt.Color(255, 255, 255));
        bPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_2.png"))); // NOI18N
        bPrint.setText("PRINT");
        bPrint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bPrint.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bPrintMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bPrintMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bPrintMouseEntered(evt);
            }
        });

        bMaintenance.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_7.png"))); // NOI18N
        bMaintenance.setText("Maintenance");
        bMaintenance.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bMaintenance.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bMaintenanceMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bMaintenanceMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bMaintenanceMousePressed(evt);
            }
        });

        bQuickGuide.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_7.png"))); // NOI18N
        bQuickGuide.setText("Quick Guide");
        bQuickGuide.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bQuickGuide.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bQuickGuideMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bQuickGuideMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bQuickGuideMouseEntered(evt);
            }
        });

        bModels.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_7.png"))); // NOI18N
        bModels.setText("Models");
        bModels.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bModels.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bModelsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bModelsMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bModelsMousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(56, Short.MAX_VALUE)
                .addComponent(bModels)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bMaintenance)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bQuickGuide)
                .addGap(6, 6, 6)
                .addComponent(bPrint))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(0, 24, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bPrint)
                    .addComponent(bQuickGuide)
                    .addComponent(bMaintenance)
                    .addComponent(bModels)))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 685, Short.MAX_VALUE)
                .addGap(15, 15, 15))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void bModelsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bModelsMouseEntered
        bModels.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_7.png")));
    }//GEN-LAST:event_bModelsMouseEntered

    private void bModelsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bModelsMouseExited
        bModels.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
    }//GEN-LAST:event_bModelsMouseExited

    private void bMaintenanceMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bMaintenanceMouseEntered
        bMaintenance.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_7.png")));
    }//GEN-LAST:event_bMaintenanceMouseEntered

    private void bMaintenanceMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bMaintenanceMouseExited
        bMaintenance.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
    }//GEN-LAST:event_bMaintenanceMouseExited

    private void bQuickGuideMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bQuickGuideMouseEntered
        bQuickGuide.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_7.png")));
    }//GEN-LAST:event_bQuickGuideMouseEntered

    private void bQuickGuideMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bQuickGuideMouseExited
        bQuickGuide.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
    }//GEN-LAST:event_bQuickGuideMouseExited

    private void bPrintMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPrintMouseEntered
        bPrint.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_2.png")));
    }//GEN-LAST:event_bPrintMouseEntered

    private void bPrintMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPrintMouseExited
        bPrint.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_2.png")));
    }//GEN-LAST:event_bPrintMouseExited

    private void bModelsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bModelsMousePressed
        if (bModels.isEnabled()) {
            bModels.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_pressed_7.png")));
            editor.updateModelsOperationCenter(new ModelsOperationCenter());
            SceneDetailsPanel sceneDP = new SceneDetailsPanel();
            sceneDP.updateBed(Base.getMainWindow().getBed());
            editor.updateDetailsCenter(sceneDP);
            Base.getMainWindow().getCanvas().unPickAll();
            editor.handleNewModel();
        }
    }//GEN-LAST:event_bModelsMousePressed

    private void bMaintenanceMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bMaintenanceMousePressed
        if (bMaintenance.isEnabled()) {
            editor.updateModelsOperationCenter(new ModelsOperationCenter());
            SceneDetailsPanel sceneDP = new SceneDetailsPanel();
            sceneDP.updateBed(Base.getMainWindow().getBed());
            editor.updateDetailsCenter(sceneDP);
            Base.getMainWindow().getCanvas().unPickAll();
            editor.handleMaintenance();
        }
    }//GEN-LAST:event_bMaintenanceMousePressed

    private void bQuickGuideMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bQuickGuideMousePressed
        if (bQuickGuide.isEnabled()) {
            Base.writeLog("BEESOFT tour loaded ... ", this.getClass());
            TourWelcome p = new TourWelcome();
            p.setVisible(true);
            bQuickGuide.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_pressed_7.png")));
        }
    }//GEN-LAST:event_bQuickGuideMousePressed

    private void bPrintMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPrintMousePressed
        if (bPrint.isEnabled() && editor.validatePrintConditions()) {
            if (printPanel) {
                PrintPanel p = new PrintPanel();
                p.setVisible(true);
            } else {
                EstimateExportPanel p = new EstimateExportPanel();
                p.setVisible(true);
            }
            bPrint.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_pressed_2.png")));
        }
    }//GEN-LAST:event_bPrintMousePressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bMaintenance;
    private javax.swing.JLabel bModels;
    private javax.swing.JLabel bPrint;
    private javax.swing.JLabel bQuickGuide;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    // End of variables declaration//GEN-END:variables
}
