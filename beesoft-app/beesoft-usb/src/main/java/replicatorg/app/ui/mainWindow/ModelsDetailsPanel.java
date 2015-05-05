package replicatorg.app.ui.mainWindow;

import java.text.DecimalFormat;
import javax.swing.ImageIcon;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.app.ui.modeling.EditingModel;
import replicatorg.model.Model;
import replicatorg.model.PrintBed;
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
public class ModelsDetailsPanel extends javax.swing.JPanel {

    private PrintBed bed;
    private boolean panelDisabled;
    private boolean oneModel;
    private DimensionsThread updateThread;

    public ModelsDetailsPanel() {
        initComponents();
        setFont();
        panelDisabled = false;
        oneModel = true;
        setTextLanguage();
//        updateThread = new DimensionsThread(this);
//        updateThread.start();
    }

    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProLight("20"));
        jLabel2.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel3.setFont(GraphicDesignComponents.getSSProBold("12"));
        jLabel4.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel5.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel6.setFont(GraphicDesignComponents.getSSProBold("12"));
        jLabel7.setFont(GraphicDesignComponents.getSSProBold("12"));
        jLabel8.setFont(GraphicDesignComponents.getSSProBold("12"));
        deleteButton.setFont(GraphicDesignComponents.getSSProBold("12"));
        jLabel9.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel11.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel12.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel13.setFont(GraphicDesignComponents.getSSProBold("12"));
        jLabel14.setFont(GraphicDesignComponents.getSSProBold("12"));
        jLabel15.setFont(GraphicDesignComponents.getSSProLight("12"));

        jTextField1.setFont(GraphicDesignComponents.getSSProLight("12"));
        jTextArea1.setFont(GraphicDesignComponents.getSSProLight("12"));
        jTextField3.setFont(GraphicDesignComponents.getSSProLight("12"));
        jTextField4.setFont(GraphicDesignComponents.getSSProLight("12"));
        jTextField5.setFont(GraphicDesignComponents.getSSProLight("12"));

    }

    private void setTextLanguage() {
        jLabel1.setText(Languager.getTagValue(1, "ModelDetails", "Model"));
        jLabel15.setText(Languager.getTagValue(1, "ModelDetails", "Model_N"));
        jLabel5.setText(Languager.getTagValue(1, "ModelDetails", "Model_Selected"));
             
        if (ProperDefault.get("measures").equals("inches")) {
            jLabel11.setText(Languager.getTagValue(1, "ModelDetails", "Model_Dimensions")
                +" ("+Languager.getTagValue(1, "MainWindowButtons", "Inches")+")");
        } else {
             jLabel11.setText(Languager.getTagValue(1, "ModelDetails", "Model_Dimensions")
                 +" ("+Languager.getTagValue(1, "MainWindowButtons", "MM")+")");
        }
        jLabel12.setText(Languager.getTagValue(1, "ModelDetails", "Model_Name"));
        jLabel4.setText(Languager.getTagValue(1, "ModelDetails", "Model_Description"));

        jLabel6.setText(Languager.getTagValue(1, "ApplicationMenus", "Edit_PutPlatform"));
        jLabel7.setText(Languager.getTagValue(1, "ApplicationMenus", "Edit_Center"));
        jLabel8.setText(Languager.getTagValue(1, "ApplicationMenus", "Edit_Reset"));
        deleteButton.setText(Languager.getTagValue(1, "ApplicationMenus", "Edit_Delete"));

    }

    public void updateBed(PrintBed bed) {
        this.bed = bed;
        this.updateBedInfo();
        evaluateBed();
    }

    public void updateBedInfo() {
//        evaluateBed();
        Model model = Base.getMainWindow().getBed().getFirstPickedModel();

        DecimalFormat df = new DecimalFormat("#.0");
        EditingModel modelEditer = Base.getMainWindow().getBed().getFirstPickedModel().getEditer();

        if (ProperDefault.get("measures").equals("inches")) {
            jTextField3.setText(String.valueOf(df.format(UnitConverter.millimetersToInches(modelEditer.getWidth()))));
            jTextField4.setText(String.valueOf(df.format(UnitConverter.millimetersToInches(modelEditer.getDepth()))));
            jTextField5.setText(String.valueOf(df.format(UnitConverter.millimetersToInches(modelEditer.getHeight()))));
        } else {
            jTextField3.setText(String.valueOf(df.format(modelEditer.getWidth())));
            jTextField4.setText(String.valueOf(df.format(modelEditer.getDepth())));
            jTextField5.setText(String.valueOf(df.format(modelEditer.getHeight())));
        }

        jTextField1.setText(String.valueOf(model.getName()));
        jTextArea1.setText(String.valueOf(model.getDescription()));
    }

    private void evaluateBed() {
        if (bed.getNumberPickedModels() > 1) {
            jLabel11.setEnabled(false);
            jLabel12.setEnabled(false);
            jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_8.png")));
            jLabel7.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_8.png")));
            jLabel8.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_8.png")));
            deleteButton.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_8.png")));
            jTextField3.setEnabled(false);
            jTextField4.setEnabled(false);
            jTextField5.setEnabled(false);
            jTextField1.setEnabled(false);
            jTextArea1.setEnabled(false);
            panelDisabled = true;
            jLabel9.setText(String.valueOf(bed.getNumberPickedModels()));
            if (bed.getNumberPickedModels() > 1) {
                oneModel = false;
            }
        }
        jLabel9.setText(String.valueOf(bed.getNumberPickedModels()));
    }

    public void updateDimensions() {
        if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            DecimalFormat df = new DecimalFormat("#0.00");
            EditingModel modelEditer = Base.getMainWindow().getBed().getFirstPickedModel().getEditer();

            if (ProperDefault.get("measures").equals("inches")) {
                jTextField3.setText(String.valueOf(df.format(UnitConverter.millimetersToInches(modelEditer.getWidth()))));
                jTextField4.setText(String.valueOf(df.format(UnitConverter.millimetersToInches(modelEditer.getDepth()))));
                jTextField5.setText(String.valueOf(df.format(UnitConverter.millimetersToInches(modelEditer.getHeight()))));
            } else {
                jTextField3.setText(String.valueOf(df.format(modelEditer.getWidth())));
                jTextField4.setText(String.valueOf(df.format(modelEditer.getDepth())));
                jTextField5.setText(String.valueOf(df.format(modelEditer.getHeight())));
            }
        }

    }

    public void killThread() {
        updateThread.stop();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        deleteButton = new javax.swing.JLabel();

        jLabel2.setText("jLabel2");

        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(null);
        setMinimumSize(new java.awt.Dimension(215, 515));
        setPreferredSize(new java.awt.Dimension(215, 515));

        jPanel1.setBackground(new java.awt.Color(240, 243, 244));
        jPanel1.setMinimumSize(new java.awt.Dimension(199, 515));
        jPanel1.setPreferredSize(new java.awt.Dimension(199, 515));

        jTextField1.setText("BEE");
        jTextField1.setName("nameTextField"); // NOI18N
        jTextField1.setPreferredSize(new java.awt.Dimension(40, 27));
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(35, 31, 32));
        jLabel3.setText("X");
        jLabel3.setName("nameTitle"); // NOI18N

        jLabel4.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(35, 31, 32));
        jLabel4.setText("Description");
        jLabel4.setName("descriptionTitle"); // NOI18N

        jLabel1.setFont(new java.awt.Font("Ubuntu", 0, 20)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(145, 145, 145));
        jLabel1.setText("Model");
        jLabel1.setName("sceneTitle"); // NOI18N

        jLabel11.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(35, 31, 32));
        jLabel11.setText("Dimensions");
        jLabel11.setName("nameTitle"); // NOI18N

        jTextField3.setEditable(false);
        jTextField3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField3.setText("0");
        jTextField3.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jTextField3.setMinimumSize(new java.awt.Dimension(18, 27));

        jTextField4.setEditable(false);
        jTextField4.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField4.setText("0");
        jTextField4.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jTextField4.setMinimumSize(new java.awt.Dimension(18, 27));

        jTextField5.setEditable(false);
        jTextField5.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField5.setText("0");
        jTextField5.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));

        jLabel12.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(35, 31, 32));
        jLabel12.setText("Name");
        jLabel12.setName("nameTitle"); // NOI18N

        jLabel13.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(35, 31, 32));
        jLabel13.setText("Y");
        jLabel13.setName("nameTitle"); // NOI18N

        jLabel14.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(35, 31, 32));
        jLabel14.setText("Z");
        jLabel14.setName("nameTitle"); // NOI18N

        jLabel15.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(35, 31, 32));
        jLabel15.setText("Model n");
        jLabel15.setName("nameTitle"); // NOI18N

        jLabel5.setText("Modelo Selecionado");

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_8.png"))); // NOI18N
        jLabel6.setText("Pousar na Plataforma");
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

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_8.png"))); // NOI18N
        jLabel7.setText("Centrar na Plataforma");
        jLabel7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel7MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel7MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel7MousePressed(evt);
            }
        });

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_8.png"))); // NOI18N
        jLabel8.setText("Retomar Posicao Original");
        jLabel8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel8MousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel8MouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel8MouseEntered(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(35, 31, 32));
        jLabel9.setText("X");
        jLabel9.setName("nameTitle"); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextArea1KeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(jTextArea1);

        deleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_8.png"))); // NOI18N
        deleteButton.setText("Apagar");
        deleteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                deleteButtonMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                deleteButtonMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                deleteButtonMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGap(18, 18, 18)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGap(30, 30, 30)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGap(12, 12, 12)
                                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGap(12, 12, 12)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGap(20, 20, 20)
                                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(70, 70, 70)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel15)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(deleteButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(2, 2, 2)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel11)
                .addGap(1, 1, 1)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(1, 1, 1)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14))
                .addGap(12, 12, 12)
                .addComponent(jLabel12)
                .addGap(1, 1, 1)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel4)
                .addGap(1, 1, 1)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 499, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel6MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseEntered
        if (oneModel) {
            jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_8.png")));
        }
    }//GEN-LAST:event_jLabel6MouseEntered

    private void jLabel6MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseExited
        if (oneModel) {
            jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_8.png")));
        }
    }//GEN-LAST:event_jLabel6MouseExited

    private void jLabel7MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MouseEntered
        if (oneModel) {
            jLabel7.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_8.png")));
        }
    }//GEN-LAST:event_jLabel7MouseEntered

    private void jLabel7MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MouseExited
        if (oneModel) {
            jLabel7.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_8.png")));
        }
    }//GEN-LAST:event_jLabel7MouseExited

    private void jLabel8MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MouseEntered
        if (oneModel) {
            jLabel8.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_8.png")));
        }
    }//GEN-LAST:event_jLabel8MouseEntered

    private void jLabel8MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MouseExited
        if (oneModel) {
            jLabel8.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_8.png")));
        }
    }//GEN-LAST:event_jLabel8MouseExited

    private void jLabel8MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MousePressed
        if (!panelDisabled) {
            Base.getMainWindow().getBed().resetTransformation();

            Base.getMainWindow().getBed().getFirstPickedModel().getEditer().updateModelPicked();
            Model model = Base.getMainWindow().getBed().getFirstPickedModel();
            ModelsOperationCenterScale mOCS = Base.getMainWindow().getCanvas().getControlTool(3).getModelsScaleCenter();

            if (mOCS != null) {
                model.resetScale();
                Base.getMainWindow().getCanvas().getModelsPanel().updateDimensions();

                if (mOCS.isScalePercentage()) {
                    mOCS.setXValue(model.getScaleXinPercentage());
                    mOCS.setYValue(model.getScaleYinPercentage());
                    mOCS.setZValue(model.getScaleZinPercentage());
                } else {
                    DecimalFormat df = new DecimalFormat("#.00"); 

                    double width = model.getEditer().getWidth();
                    if (ProperDefault.get("measures").equals("inches")) {
                        width = UnitConverter.millimetersToInches(width);
                    }
                    double depth = model.getEditer().getDepth();
                    if (ProperDefault.get("measures").equals("inches")) {
                        depth = UnitConverter.millimetersToInches(depth);
                    }
                    double height = model.getEditer().getHeight();
                    if (ProperDefault.get("measures").equals("inches")) {
                        height = UnitConverter.millimetersToInches(height);
                    }                                    

                    mOCS.setXValue(df.format(width));
                    mOCS.setYValue(df.format(depth));
                    mOCS.setZValue(df.format(height));                                    
                }
                
                //Sets the initial sizing variables to the new max scale size
                mOCS.resetInitialScaleVariables();
            }
        }
    }//GEN-LAST:event_jLabel8MousePressed

    private void jLabel7MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MousePressed
        if (!panelDisabled) {
            Base.getMainWindow().getBed().getFirstPickedModel().getEditer().center();
            Base.getMainWindow().getBed().getFirstPickedModel().getEditer().updateModelPicked();
        }
    }//GEN-LAST:event_jLabel7MousePressed

    private void jLabel6MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MousePressed
        if (!panelDisabled) {
            for (int i = 0; i < bed.getNumberPickedModels(); i++) {
                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().putOnPlatform();
                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().updateModelPicked();
            }
        }
    }//GEN-LAST:event_jLabel6MousePressed

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
        Model model = Base.getMainWindow().getBed().getFirstPickedModel();
        model.setName(jTextField1.getText());
    }//GEN-LAST:event_jTextField1KeyReleased

    private void jTextArea1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea1KeyReleased
        Model model = Base.getMainWindow().getBed().getFirstPickedModel();
        model.setDescription(String.valueOf(jTextArea1.getText()));
    }//GEN-LAST:event_jTextArea1KeyReleased

    private void deleteButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteButtonMousePressed
        if (!panelDisabled) {            
            Base.getMainWindow().handleCAMDelete();
        }
    }//GEN-LAST:event_deleteButtonMousePressed

    private void deleteButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteButtonMouseExited
        if (oneModel) {
            deleteButton.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_8.png")));
        }
    }//GEN-LAST:event_deleteButtonMouseExited

    private void deleteButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteButtonMouseEntered
        if (oneModel) {
            deleteButton.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_8.png")));
        }
    }//GEN-LAST:event_deleteButtonMouseEntered

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel deleteButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    // End of variables declaration//GEN-END:variables
}

class DimensionsThread extends Thread {

    ModelsDetailsPanel window;

    public DimensionsThread(ModelsDetailsPanel w) {
        super("Dimensions Thread");
        this.window = w;
    }

    @Override
    public void run() {

        while (true) {
            window.updateDimensions();
        }

    }
}
