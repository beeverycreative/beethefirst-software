package replicatorg.app.ui.mainWindow;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import static replicatorg.app.ui.GraphicDesignComponents.getSSProLight;
import static replicatorg.app.ui.GraphicDesignComponents.getSSProRegular;
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

    private boolean models_pressed, maintenance_pressed, quickGuide_pressed, print_pressed;
    private replicatorg.app.ui.MainWindow editor;
    private boolean jLabel1Bool = false;
    private boolean jLabel5Bool = false;
    private boolean jLabel4Bool = false;
    private boolean jLabel6Bool = true;
    private final int NUMBER_PRINTS_LIMIT = 15;
    private boolean mainWindowEnabled;

    public ButtonsPanel(replicatorg.app.ui.MainWindow mainWindow) {
        initComponents();
        setFont();
        editor = mainWindow;
        setTextLanguage();
        evaluateMaintenanceStatus();
        models_pressed = false;
        maintenance_pressed = false;
        quickGuide_pressed = false;
        print_pressed = false;
    }

    private void setTextLanguage() {
        jLabel6.setText(Languager.getTagValue(1, "MainWindowButtons", "Models"));
        jLabel4.setText(Languager.getTagValue(1, "MainWindowButtons", "Maintenance"));
        jLabel5.setText(Languager.getTagValue(1, "MainWindowButtons", "QuickWizard"));
        jLabel1.setText(Languager.getTagValue(1, "MainWindowButtons", "Print"));
    }

    public void setMessage(String message) {

        if (message.equals("is connected")) {
            jLabel3.setText(Languager.getTagValue(1, "FeedbackLabel", "BEETHEFIRST_STATUS3"));
        }
        if (message.equals("is disconnected")) {
            jLabel3.setText(Languager.getTagValue(1, "FeedbackLabel", "BEETHEFIRST_STATUS2"));
        } else {
            // REDSOFT: CONSIDER OTHER SITUATION ?
        }

    }

    public void setFont() {
        jLabel3.setFont(getSSProLight("18"));
        jLabel6.setFont(getSSProLight("13"));
        jLabel5.setFont(getSSProLight("13"));
        jLabel4.setFont(getSSProLight("13"));
        jLabel1.setFont(getSSProRegular("13"));

    }

    public void resetVariables() {
        models_pressed = false;
        maintenance_pressed = false;
        quickGuide_pressed = false;
        print_pressed = false;
    }

    public void goFilamentChange() {
        // && gramsCalculator(Double.valueOf(ProperDefault.get("filamentCoilRemaining"))) > 100
        if (!maintenance_pressed) {
            if (Integer.valueOf(ProperDefault.get("nTotalPrints")) < NUMBER_PRINTS_LIMIT) {
                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_pressed_7.png")));
                jLabel4Bool = false;
            } else if (Integer.valueOf(ProperDefault.get("nTotalPrints")) > NUMBER_PRINTS_LIMIT) {
                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_pressed_7_btn.png")));
                jLabel4Bool = false;
            }

//            else if (gramsCalculator(Double.valueOf(ProperDefault.get("filamentCoilRemaining"))) < 100) {
//                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_pressed_7_btn.png")));
//                jLabel4Bool = false;
//            }

            maintenance_pressed = true;
        }
    }

    private double gramsCalculator(double meters) {
        double grams = meters * 12 / 4;

        if (grams > 0) {
            return (int) meters * 12 / 4;
        }

        return 0;
    }

    private void evaluateMaintenanceStatus() {
        // && gramsCalculator(Double.valueOf(ProperDefault.get("filamentCoilRemaining"))) > 100
        if (Integer.valueOf(ProperDefault.get("nTotalPrints")) < NUMBER_PRINTS_LIMIT) {
            jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
        } else if (Integer.valueOf(ProperDefault.get("nTotalPrints")) > NUMBER_PRINTS_LIMIT) {
            jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7_btn.png")));
        }
//        else if (gramsCalculator(Double.valueOf(ProperDefault.get("filamentCoilRemaining"))) < 100) {
//            jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7_btn.png")));
//        }
    }

    public void updatePressedStateButton(String button) {
        if (button.equals("models")) {
            jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
            jLabel6Bool = false;
            models_pressed = false;
        }
        // && gramsCalculator(Double.valueOf(ProperDefault.get("filamentCoilRemaining"))) > 100
        if (button.equals("maintenance")) {
            if (Integer.valueOf(ProperDefault.get("nTotalPrints")) <= NUMBER_PRINTS_LIMIT) {
                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
                jLabel4Bool = true;
            } else if (Integer.valueOf(ProperDefault.get("nTotalPrints")) > NUMBER_PRINTS_LIMIT) {
                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7_btn.png")));
                jLabel4Bool = true;
            }
//            else if (gramsCalculator(Double.valueOf(ProperDefault.get("filamentCoilRemaining"))) < 100) {
//                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7_btn.png")));
//                jLabel4Bool = true;
//            }
            maintenance_pressed = false;
        }

        if (button.equals("quick_guide")) {
            jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
            jLabel5Bool = true;
            quickGuide_pressed = false;
        }

        if (button.equals("print")) {
            jLabel1.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_2.png")));
            jLabel1Bool = true;
            print_pressed = false;
        }

    }

    public void connect() {
        updateFromState(Base.getMainWindow().getMachineInterface(), null);
    }

    private void updateFromState(final MachineInterface s, final MachineInterface machine) {

        if (!s.isConnected()) {
//            jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_disabled_7.png")));
//            jLabel5Bool = false;
//            jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_disabled_7.png")));
//            jLabel4Bool = false;
//            jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
//            jLabel6Bool = false;
//            jLabel1.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_2.png")));
//            jLabel1Bool = false;
            setMessage("is disconnected");
        } else {

            jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
            jLabel5Bool = true;

            // && gramsCalculator(Double.valueOf(ProperDefault.get("filamentCoilRemaining"))) > 100
            if (Integer.valueOf(ProperDefault.get("nTotalPrints")) <= NUMBER_PRINTS_LIMIT) {
                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
                jLabel4Bool = true;
            } else if (Integer.valueOf(ProperDefault.get("nTotalPrints")) > NUMBER_PRINTS_LIMIT) {
                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7_btn.png")));
                jLabel4Bool = true;
            }
//            else if (gramsCalculator(Double.valueOf(ProperDefault.get("filamentCoilRemaining"))) < 100) {
//                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7_btn.png")));
//                jLabel4Bool = true;
//            }

            jLabel1.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_2.png")));
            jLabel1Bool = true;
            setMessage("is connected");
        }
    }

    public void updateFromMachine(final MachineInterface machine) {
        if (machine != null) {
            updateFromState(machine, machine);
        }
    }

    public void setMainWindowEnabled(boolean editorEnabled) {
        this.mainWindowEnabled = editorEnabled;
    }

    public void blockModelsButton(boolean block) {
        if (block) {
            models_pressed = true;
        } else {
            models_pressed = false;
        }
    }

    public boolean areIOFunctionsBlocked() {
        return models_pressed;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(240, 243, 244)));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/logo_beethefirst.png"))); // NOI18N

        jLabel3.setText("is not connected");

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

        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_2.png"))); // NOI18N
        jLabel1.setText("PRINT");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel1MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel1MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel1MousePressed(evt);
            }
        });

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_7.png"))); // NOI18N
        jLabel4.setText("Maintenance");
        jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel4MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel4MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel4MousePressed(evt);
            }
        });

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_7.png"))); // NOI18N
        jLabel5.setText("Quick Guide");
        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel5MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel5MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel5MousePressed(evt);
            }
        });

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_7.png"))); // NOI18N
        jLabel6.setText("Models");
        jLabel6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel6MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel6MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel6MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(56, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addGap(6, 6, 6)
                .addComponent(jLabel1))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(0, 24, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6)))
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

    private void jLabel6MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseEntered
        if (jLabel6Bool == false) {
            jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_7.png")));
        }
    }//GEN-LAST:event_jLabel6MouseEntered

    private void jLabel6MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseExited
        if (jLabel6Bool == false) {
            jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
        }
    }//GEN-LAST:event_jLabel6MouseExited

    private void jLabel4MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseEntered
        // && gramsCalculator(Double.valueOf(ProperDefault.get("filamentCoilRemaining"))) > 100
        if (jLabel4Bool) {
            if (Integer.valueOf(ProperDefault.get("nTotalPrints")) <= NUMBER_PRINTS_LIMIT) {
                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_7.png")));
            } else if (Integer.valueOf(ProperDefault.get("nTotalPrints")) > NUMBER_PRINTS_LIMIT) {
                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_7_btn.png")));
            }
//            else if (gramsCalculator(Double.valueOf(ProperDefault.get("filamentCoilRemaining"))) < 100) {
//                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_7_btn.png")));
//            }
        }
    }//GEN-LAST:event_jLabel4MouseEntered

    private void jLabel4MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseExited
        //&& gramsCalculator(Double.valueOf(ProperDefault.get("filamentCoilRemaining"))) > 100
        if (jLabel4Bool) {
            if (Integer.valueOf(ProperDefault.get("nTotalPrints")) <= NUMBER_PRINTS_LIMIT) {
                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
            } else if (Integer.valueOf(ProperDefault.get("nTotalPrints")) > NUMBER_PRINTS_LIMIT) {
                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7_btn.png")));
            }
//            else if (gramsCalculator(Double.valueOf(ProperDefault.get("filamentCoilRemaining"))) < 100) {
//                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7_btn.png")));
//            }
        }
    }//GEN-LAST:event_jLabel4MouseExited

    private void jLabel5MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseEntered
        if (jLabel5Bool) {
            jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_7.png")));
        }
    }//GEN-LAST:event_jLabel5MouseEntered

    private void jLabel5MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseExited
        if (jLabel5Bool) {
            jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
        }
    }//GEN-LAST:event_jLabel5MouseExited

    private void jLabel1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseEntered
        if (jLabel1Bool) {
            jLabel1.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_2.png")));
        }
    }//GEN-LAST:event_jLabel1MouseEntered

    private void jLabel1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseExited
        if (jLabel1Bool) {
            jLabel1.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_2.png")));
        }
    }//GEN-LAST:event_jLabel1MouseExited

    private void jLabel6MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MousePressed

        if (!models_pressed) {
            jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_pressed_7.png")));
            jLabel6Bool = true;
            models_pressed = true;
            editor.updateModelsOperationCenter(new ModelsOperationCenter());
            SceneDetailsPanel sceneDP = new SceneDetailsPanel();
            sceneDP.updateBed(Base.getMainWindow().getBed());
            editor.updateDetailsCenter(sceneDP);
            Base.getMainWindow().getCanvas().unPickAll();
//            editor.handleGallery();
            editor.handleNewModel();
        }

    }//GEN-LAST:event_jLabel6MousePressed

    private void jLabel4MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MousePressed
        boolean connected = Base.getMachineLoader().isConnected();

//        if (connected) {
//            if(!Boolean.valueOf(ProperDefault.get("firstTime")))
//             {
            if (Base.isPrinting == false) {
                //&& gramsCalculator(Double.valueOf(ProperDefault.get("filamentCoilRemaining"))) > 100
                if (maintenance_pressed == false) {
                    if (Integer.valueOf(ProperDefault.get("nTotalPrints")) < 10) {
                        jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_pressed_7.png")));
                        jLabel4Bool = false;
                    } else if (Integer.valueOf(ProperDefault.get("nTotalPrints")) > 10) {
                        jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_pressed_7_btn.png")));
                        jLabel4Bool = false;
                    }
//                else if (gramsCalculator(Double.valueOf(ProperDefault.get("filamentCoilRemaining"))) < 100) {
//                    jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_pressed_7_btn.png")));
//                    jLabel4Bool = false;
//                }


                    maintenance_pressed = true;
                    editor.updateModelsOperationCenter(new ModelsOperationCenter());
                    SceneDetailsPanel sceneDP = new SceneDetailsPanel();
                    sceneDP.updateBed(Base.getMainWindow().getBed());
                    editor.updateDetailsCenter(sceneDP);
                    Base.getMainWindow().getCanvas().unPickAll();
                    editor.handleMaintenance();
                }
            }
//            }
//            else
//            {
//                editor.showFeedBackMessage("firstTime");
//            }
//        } else {
//            Base.getMainWindow().showFeedBackMessage("btfDisconnect");
//        }

    }//GEN-LAST:event_jLabel4MousePressed

    private void jLabel5MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MousePressed
//        boolean connected = Base.getMachineLoader().isConnected();

//        if (connected) {
//            if (quickGuide_pressed == false && Base.isPrinting == false) {
//                jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_pressed_7.png")));
//                jLabel5Bool = false;
//                editor.updateModelsOperationCenter(new ModelsOperationCenter());
//                SceneDetailsPanel sceneDP = new SceneDetailsPanel();
//                sceneDP.updateBed(Base.getMainWindow().getBed());
//                editor.updateDetailsCenter(sceneDP);
//                Base.getMainWindow().getCanvas().unPickAll();
//                editor.handleQuickStartWizard();
                Base.getMainWindow().launchBrowser("https://beeverycreative.com/");
//            }
//        } else {
//            Base.getMainWindow().showFeedBackMessage("btfDisconnect");
//        }
    }//GEN-LAST:event_jLabel5MousePressed

    private void jLabel1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MousePressed
        boolean connected = Base.getMachineLoader().isConnected();

//        if (connected) {
        MachineInterface machine = Base.getMainWindow().getMachineInterface();

        if (machine.getModel().getMachineBusy()) {
            editor.showFeedBackMessage("moving");
        } else {//&& Base.isPrinting == false 
            if (!print_pressed && editor.validatePrintConditions() && Base.getMainWindow().getBed().getNumberModels() > 0
                    || Boolean.valueOf(ProperDefault.get("localPrint"))) {
                jLabel1.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_pressed_2.png")));
                jLabel1Bool = false;
                print_pressed = true;
                editor.handlePrintPanel();
            }
        }

//        } else {
//            Base.getMainWindow().showFeedBackMessage("btfDisconnect");
//        }

    }//GEN-LAST:event_jLabel1MousePressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    // End of variables declaration//GEN-END:variables
}
