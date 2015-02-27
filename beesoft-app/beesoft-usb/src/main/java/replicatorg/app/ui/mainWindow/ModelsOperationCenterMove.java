package replicatorg.app.ui.mainWindow;

import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.util.UnitConverter;

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
public class ModelsOperationCenterMove extends javax.swing.JPanel {

    private boolean check_pressed, fiveMM_pressed;
    
    public ModelsOperationCenterMove() {
        initComponents();
        this.check_pressed = false;
        this.fiveMM_pressed = false;
        Base.getMainWindow().getCanvas().setControlTool(1);
        setFont();
        this.fiveMM_pressed = Base.getMainWindow().getCanvas().getControlTool(1).getAdvOption();
        if (fiveMM_pressed) {
            fiveMM_pressed = false; //trigger value for handler to work
            handleAdvancedOption();
        }
        setTextLanguage();
        Base.getMainWindow().getCanvas().setModelationType("move");
    }

    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel2.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel3.setFont(GraphicDesignComponents.getSSProLight("33"));
        jLabel4.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel5.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel7.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel12.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel13.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel14.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel15.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel23.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jTextField4.setFont(GraphicDesignComponents.getSSProRegular("13"));
        jTextField5.setFont(GraphicDesignComponents.getSSProRegular("13"));
        jTextField5.setFont(GraphicDesignComponents.getSSProRegular("13"));
    }

    private void setTextLanguage() {
        int fileKey = 1;
        jLabel1.setText(Languager.getTagValue(fileKey, "MainWindowButtons", "Move"));
        jLabel2.setText(Languager.getTagValue(fileKey, "MainWindowButtons", "Rotate"));
        jLabel3.setText(Languager.getTagValue(fileKey, "ModelDetails", "Model"));
        jLabel4.setText(Languager.getTagValue(fileKey, "MainWindowButtons", "Scale"));
        jLabel5.setText(Languager.getTagValue(fileKey, "MainWindowButtons", "Mirror"));
        jLabel7.setText(Languager.getTagValue(fileKey, "MainWindowButtons", "MoreOptions"));
        jLabel12.setText(Languager.getTagValue(fileKey, "MainWindowButtons", "Move"));
//        jLabel13.setText(Languager.getTagValue("", ""));
//        jLabel14.setText(Languager.getTagValue("", ""));        
//        jLabel15.setText(Languager.getTagValue("", ""));    
        if (ProperDefault.get("measures").equals("inches")) {
            jLabel23.setText(Languager.getTagValue(fileKey, "MainWindowButtons", "Move") + " 0.2 " + Languager.getTagValue(fileKey, "MainWindowButtons", "Inches"));
        } else {
            jLabel23.setText(Languager.getTagValue(fileKey, "MainWindowButtons", "Move") + " 5 " + Languager.getTagValue(fileKey, "MainWindowButtons", "MM"));
        } 
    }

    private void toggle5MM() {
        if (!fiveMM_pressed) {
            jLabel22.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_checked.png")));
            fiveMM_pressed = true;
            Base.getMainWindow().getCanvas().getControlTool(1).setAdvOption(true);
        } else {
            jLabel22.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_unchecked.png")));
            fiveMM_pressed = false;
            Base.getMainWindow().getCanvas().getControlTool(1).setAdvOption(false);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(254, 254, 254));
        setMinimumSize(new java.awt.Dimension(0, 0));
        setPreferredSize(new java.awt.Dimension(190, 375));

        jLabel1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(35, 31, 32));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_pressed_1.png"))); // NOI18N
        jLabel1.setText("Move");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel1.setName("moveButton"); // NOI18N
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel1MousePressed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(35, 31, 32));
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_1.png"))); // NOI18N
        jLabel2.setText("Rotate");
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel2.setName("rotateButton"); // NOI18N
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel2MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel2MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel2MousePressed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Ubuntu", 0, 33)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(35, 31, 32));
        jLabel3.setText("Model");

        jLabel4.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(35, 31, 32));
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_1.png"))); // NOI18N
        jLabel4.setText("Scale");
        jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel4.setName("scaleButton"); // NOI18N
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

        jLabel5.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(35, 31, 32));
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_1.png"))); // NOI18N
        jLabel5.setText("Mirror");
        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel5.setName("mirrorButton"); // NOI18N
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

        jLabel6.setName("moreOptionsCheckbox"); // NOI18N

        jLabel7.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(35, 31, 32));
        jLabel7.setText("More Options");
        jLabel7.setName("moreOptionsTitle"); // NOI18N
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel7MousePressed(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(240, 243, 244));

        jLabel12.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel12.setText("Move");
        jLabel12.setName("moveTitle"); // NOI18N

        jLabel13.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel13.setText("X");
        jLabel13.setName("xAxisButton"); // NOI18N

        jLabel14.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel14.setText("Y");
        jLabel14.setName("yAxisButton"); // NOI18N

        jLabel15.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel15.setText("Z");
        jLabel15.setName("zAxisButton"); // NOI18N

        jTextField4.setName("xScaleValue"); // NOI18N
        jTextField4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField4KeyPressed(evt);
            }
        });

        jTextField5.setName("yScaleValue"); // NOI18N
        jTextField5.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField5KeyPressed(evt);
            }
        });

        jTextField6.setName("zScaleValue"); // NOI18N
        jTextField6.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField6KeyPressed(evt);
            }
        });

        jLabel23.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel23.setText("Move 5mm");
        jLabel23.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel23MousePressed(evt);
            }
        });

        jLabel22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/c_unchecked.png"))); // NOI18N
        jLabel22.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel22MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, 13, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField4)
                            .addComponent(jTextField5)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel22)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel23)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jTextField6)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(4, 4, 4)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addGap(12, 12, 12)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel22)
                    .addComponent(jLabel23))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/c_checked.png"))); // NOI18N
        jLabel16.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel16MousePressed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel8)
                            .addComponent(jLabel16))
                        .addGap(51, 51, 51)
                        .addComponent(jLabel7))
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(12, 12, 12))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel3)
                .addGap(24, 24, 24)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel7)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField4KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField4KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            int nPickedModels = Base.getMainWindow().getBed().getPickedModels().size();

            if (nPickedModels > 0) {
                double xValue = 0.0;

                if (!(jTextField4.getText().length() == 0)) {

                    if (Base.isNumeric(jTextField4.getText())) {
                        xValue = Double.parseDouble(jTextField4.getText());
                    } else {
                        jTextField4.setText("");
                    }
                }
                    
                if (ProperDefault.get("measures").equals("inches")) {
                    xValue = UnitConverter.inchesToMillimeters(xValue);
                }

                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().translateObject(xValue, 0, 0);
            } else {
                Base.getMainWindow().showFeedBackMessage("modelNotPicked");
            }

            jTextField4.setText("");
        }
    }//GEN-LAST:event_jTextField4KeyPressed

    private void jTextField5KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField5KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            int nPickedModels = Base.getMainWindow().getBed().getPickedModels().size();

            if (nPickedModels > 0) {
                double yValue = 0.0;

                if (!(jTextField5.getText().length() == 0)) {
                    if (Base.isNumeric(jTextField5.getText())) {
                        yValue = Double.parseDouble(jTextField5.getText());
                    } else {
                        jTextField5.setText("");
                    }
                }
                
                if (ProperDefault.get("measures").equals("inches")) {
                    yValue = UnitConverter.inchesToMillimeters(yValue);
                }
                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().translateObject(0, yValue, 0);
            } else {
                Base.getMainWindow().showFeedBackMessage("modelNotPicked");
            }

            jTextField5.setText("");
        }
    }//GEN-LAST:event_jTextField5KeyPressed

    private void jTextField6KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField6KeyPressed

        if (evt.getKeyCode() == KeyEvent.VK_ENTER && !Base.getMainWindow().getCanvas().getControlTool(1).heightLocked()) {
            int nPickedModels = Base.getMainWindow().getBed().getPickedModels().size();

            if (nPickedModels > 0) {
                double zValue = 0.0;

                if (!(jTextField6.getText().length() == 0)) {
                    if (Base.isNumeric(jTextField6.getText())) {
                        zValue = Double.parseDouble(jTextField6.getText());
                    } else {
                        jTextField6.setText("");
                    }
                }

                if (ProperDefault.get("measures").equals("inches")) {
                    zValue = UnitConverter.inchesToMillimeters(zValue);
                }                
                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().translateObject(0, 0, zValue);
            } else {
                Base.getMainWindow().showFeedBackMessage("modelNotPicked");
            }

            jTextField6.setText("");
        }
    }//GEN-LAST:event_jTextField6KeyPressed

    private void jLabel2MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseEntered
        jLabel2.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_1.png")));
    }//GEN-LAST:event_jLabel2MouseEntered

    private void jLabel2MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseExited
        jLabel2.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_1.png")));
    }//GEN-LAST:event_jLabel2MouseExited

    private void jLabel4MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseEntered
        jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_1.png")));
    }//GEN-LAST:event_jLabel4MouseEntered

    private void jLabel4MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseExited
        jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_1.png")));
    }//GEN-LAST:event_jLabel4MouseExited

    private void jLabel5MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseEntered
        jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_1.png")));
    }//GEN-LAST:event_jLabel5MouseEntered

    private void jLabel5MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseExited
        jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_1.png")));
    }//GEN-LAST:event_jLabel5MouseExited

    private void jLabel2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MousePressed
//        Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterRotateSimple());
        if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterRotate());
        } else {
            Base.getMainWindow().showFeedBackMessage("modelNotPicked");
        }
    }//GEN-LAST:event_jLabel2MousePressed

    private void jLabel4MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MousePressed
//        Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterScaleSimple());
        if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterScale());
        } else {
            Base.getMainWindow().showFeedBackMessage("modelNotPicked");
        }
    }//GEN-LAST:event_jLabel4MousePressed

    private void jLabel5MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MousePressed
//        Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterMirrorSimple());
        if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterMirror());
        } else {
            Base.getMainWindow().showFeedBackMessage("modelNotPicked");
        }
    }//GEN-LAST:event_jLabel5MousePressed

    private void jLabel1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MousePressed
//        Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterMirrorSimple());
        if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenter());
        } else {
            Base.getMainWindow().showFeedBackMessage("modelNotPicked");
        }
    }//GEN-LAST:event_jLabel1MousePressed

    private void jLabel22MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel22MousePressed
        toggle5MM();
    }//GEN-LAST:event_jLabel22MousePressed

    private void jLabel23MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel23MousePressed
        toggle5MM();
    }//GEN-LAST:event_jLabel23MousePressed

    private void jLabel16MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel16MousePressed
        showOptions();
    }//GEN-LAST:event_jLabel16MousePressed

    private void jLabel7MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MousePressed
        showOptions();
    }//GEN-LAST:event_jLabel7MousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    // End of variables declaration//GEN-END:variables

    private void handleAdvancedOption() {
        if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            if (!fiveMM_pressed) {
                jLabel22.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_checked.png")));
                fiveMM_pressed = true;
                Base.getMainWindow().getCanvas().getControlTool(2).setAdvOption(true);
            } else {
                jLabel22.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_unchecked.png")));
                fiveMM_pressed = false;
                Base.getMainWindow().getCanvas().getControlTool(2).setAdvOption(false);
            }
        } else {
            Base.getMainWindow().showFeedBackMessage("modelNotPicked");
        }
    }

    private void showOptions() {
        if (check_pressed) {
            jPanel2.setVisible(false);
            check_pressed = false;
            jLabel16.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_unchecked.png")));
        } else {
            jPanel2.setVisible(true);
            check_pressed = true;
            jLabel16.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_checked.png")));
        }
    }
}
