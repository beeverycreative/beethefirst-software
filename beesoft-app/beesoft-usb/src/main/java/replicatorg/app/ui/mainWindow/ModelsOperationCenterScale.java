package replicatorg.app.ui.mainWindow;

import java.text.DecimalFormat;
import javax.swing.ImageIcon;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.machine.model.BuildVolume;
import replicatorg.model.CAMPanel;
import replicatorg.model.Model;
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
public class ModelsOperationCenterScale extends javax.swing.JPanel {

    private boolean check_pressed, mm, percentage, lockedRatio;
    private boolean scaleLocked;
    private final double initialWidth, initialDepth, initialHeight;

    public ModelsOperationCenterScale() {
        initComponents();
        this.check_pressed = false;
        this.mm = true;
        this.percentage = false;

        jLabel26.setVisible(false);
        jLabel27.setVisible(false);
        Base.getMainWindow().getCanvas().setControlTool(3);
        Base.getMainWindow().getCanvas().getControlTool(3).setModelsOperationScale(this);
        setFont();
        setTextLanguage();
        CAMPanel canvas = Base.getMainWindow().getCanvas();
        canvas.setModelationType("scale");
        Model model = canvas.getModel();

        this.initialWidth = model.getEditer().getWidth();
        this.initialHeight = model.getEditer().getHeight();
        this.initialDepth = model.getEditer().getDepth();
        
        if (this.percentage) {
            jTextField4.setText(model.getScaleXinPercentage());
            jTextField5.setText(model.getScaleYinPercentage());
            jTextField6.setText(model.getScaleZinPercentage());
 
        } else {
            
            DecimalFormat df = new DecimalFormat("#.0");
            
            if (ProperDefault.get("measures").equals("inches")) {
                jTextField4.setText(df.format(UnitConverter.millimetersToInches(this.initialWidth)));
                jTextField5.setText(df.format(UnitConverter.millimetersToInches(this.initialDepth)));
                jTextField6.setText(df.format(UnitConverter.millimetersToInches(this.initialHeight)));
            } else {
                jTextField4.setText(df.format(this.initialWidth));
                jTextField5.setText(df.format(this.initialDepth));
                jTextField6.setText(df.format(this.initialHeight));
            }            
        }
    }
    
    public boolean isScalePercentage() {
        return this.percentage;
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
        jLabel27.setFont(GraphicDesignComponents.getSSProRegular("12"));
        l_lockRatio.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jTextField4.setFont(GraphicDesignComponents.getSSProRegular("13"));
        jTextField5.setFont(GraphicDesignComponents.getSSProRegular("13"));
        jTextField6.setFont(GraphicDesignComponents.getSSProRegular("13"));
        jLabel18.setFont(GraphicDesignComponents.getSSProRegular("12"));
    }

    private void setTextLanguage() {
        jLabel1.setText(Languager.getTagValue(1, "MainWindowButtons", "Move"));
        jLabel2.setText(Languager.getTagValue(1, "MainWindowButtons", "Rotate"));
        jLabel3.setText(Languager.getTagValue(1, "ModelDetails", "Model"));
        jLabel4.setText(Languager.getTagValue(1, "MainWindowButtons", "Scale"));
        jLabel5.setText(Languager.getTagValue(1, "MainWindowButtons", "Mirror"));
        jLabel7.setText(Languager.getTagValue(1, "MainWindowButtons", "MoreOptions")); 
        
        if (this.percentage) {
             jLabel12.setText(Languager.getTagValue(1, "MainWindowButtons", "Scale_Extense"));
        } else {
            if (ProperDefault.get("measures").equals("inches")) {
                jLabel12.setText(Languager.getTagValue(1, "MainWindowButtons", "Scale")
                        +" ("+Languager.getTagValue(1, "MainWindowButtons", "Inches")+")");
            } else {
                jLabel12.setText(Languager.getTagValue(1, "MainWindowButtons", "Scale")
                        +" ("+Languager.getTagValue(1, "MainWindowButtons", "MM")+")");
            }
        }

        jLabel27.setText(Languager.getTagValue(1, "MainWindowButtons", "Percentage"));
        l_lockRatio.setText(Languager.getTagValue(1, "MainWindowButtons", "LockRatio"));
        jLabel18.setText(Languager.getTagValue(1, "MainWindowButtons", "ScaleToMax"));
    }

    public void setXValue(String val) {
        this.jTextField4.setText(val);
    }

    public void setYValue(String val) {
        this.jTextField5.setText(val);
    }

    public void setZValue(String val) {
        this.jTextField6.setText(val);
    }

    private void toggleOptions() {
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

    /**
     * Lock scale fields for a unified operation
     *
     * @param b <li> true, if to lock
     * <li> false, if to unlock
     */
    private void lockScale(boolean b) {
        if (b) {
            scaleLocked = true;
        } else {
            scaleLocked = false;
        }
    }
    
    private void lockRatio() {
        if (lockedRatio) {
            lockedRatio = false;
            lockRatio.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_unchecked.png")));
        } else {
            lockedRatio = true;
            lockRatio.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_checked.png")));
        }
        
        if (scaleLocked) {
            lockScale(false);
        } else {
            lockScale(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
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
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        lockRatio = new javax.swing.JLabel();
        l_lockRatio = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(254, 254, 254));
        setMaximumSize(new java.awt.Dimension(185, 375));
        setMinimumSize(new java.awt.Dimension(190, 375));
        setPreferredSize(new java.awt.Dimension(190, 375));
        setRequestFocusEnabled(false);

        jLabel1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(35, 31, 32));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_1.png"))); // NOI18N
        jLabel1.setText("Move");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel1.setName("moveButton"); // NOI18N
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
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_pressed_1.png"))); // NOI18N
        jLabel4.setText("Scale");
        jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel4.setName("scaleButton"); // NOI18N
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
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
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel5MousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel5MouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel5MouseEntered(evt);
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
        jLabel12.setText("Scale");
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
        jTextField4.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField4FocusGained(evt);
            }
        });
        jTextField4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField4KeyReleased(evt);
            }
        });

        jTextField5.setName("yScaleValue"); // NOI18N
        jTextField5.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField5FocusGained(evt);
            }
        });
        jTextField5.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField5KeyReleased(evt);
            }
        });

        jTextField6.setName("zScaleValue"); // NOI18N
        jTextField6.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField6FocusGained(evt);
            }
        });
        jTextField6.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField6KeyReleased(evt);
            }
        });

        jLabel26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/c_checked.png"))); // NOI18N
        jLabel26.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel26MouseClicked(evt);
            }
        });

        jLabel27.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel27.setText("percentage");

        lockRatio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/c_unchecked.png"))); // NOI18N
        lockRatio.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                lockRatioMousePressed(evt);
            }
        });

        l_lockRatio.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        l_lockRatio.setText("Lock aspect ratio");
        l_lockRatio.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                l_lockRatioMousePressed(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_12.png"))); // NOI18N
        jLabel18.setText("Scale to Max");
        jLabel18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel18.setName("xAxisButton"); // NOI18N
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
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel15)
                                    .addComponent(jLabel14)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(jTextField6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                                        .addComponent(jTextField5, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jTextField4, javax.swing.GroupLayout.Alignment.LEADING))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(jLabel26, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(lockRatio))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel27)
                                            .addComponent(l_lockRatio))))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel12)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE))
                .addGap(4, 4, 4)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lockRatio)
                    .addComponent(l_lockRatio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel27)
                    .addComponent(jLabel26))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(17, 17, 17))
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
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
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
                        .addComponent(jLabel3)))
                .addContainerGap(55, Short.MAX_VALUE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(70, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel26MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel26MouseClicked
//            jLabel25.setIcon(new ImageIcon(RESOURCES_PATH+"c_unchecked.png"));
//            jLabel26.setIcon(new ImageIcon(RESOURCES_PATH+"c_checked.png"));
//            mm = false;
//            percentage = true;
    }//GEN-LAST:event_jLabel26MouseClicked

    private void jLabel1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseEntered
        jLabel1.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_1.png")));
    }//GEN-LAST:event_jLabel1MouseEntered

    private void jLabel1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseExited
        jLabel1.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_1.png")));
    }//GEN-LAST:event_jLabel1MouseExited

    private void jLabel2MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseEntered
        jLabel2.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_1.png")));
    }//GEN-LAST:event_jLabel2MouseEntered

    private void jLabel2MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseExited
        jLabel2.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_1.png")));
    }//GEN-LAST:event_jLabel2MouseExited

    private void jLabel5MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseEntered
        jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_1.png")));
    }//GEN-LAST:event_jLabel5MouseEntered

    private void jLabel5MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseExited
        jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_1.png")));
    }//GEN-LAST:event_jLabel5MouseExited

    private void jTextField4KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField4KeyReleased
        boolean modelOnPlatform = Base.getMainWindow().getBed().getFirstPickedModel().getEditer().isOnPlatform();
        Model model = Base.getMainWindow().getBed().getFirstPickedModel();
        double newValuePercentage;
        String textFieldValue = jTextField4.getText();

        if (!(textFieldValue.length() == 0)) {
            if (Base.isNumeric(jTextField4.getText())) {
                if (textFieldValue.contains(",")) {
                    textFieldValue = textFieldValue.replace(",", ".");
                }

                if (this.percentage) {
                    newValuePercentage = Double.parseDouble(textFieldValue);

                } else {

                    if (ProperDefault.get("measures").equals("inches")) {
                        double sizeInches = Double.parseDouble(textFieldValue);

                        double sizeMM = UnitConverter.inchesToMillimeters(sizeInches);
                        newValuePercentage = (sizeMM / this.initialWidth) * 100;
                    } else {
                        double sizeMM = Double.parseDouble(textFieldValue);

                        newValuePercentage = (sizeMM / this.initialWidth) * 100;
                    }
                }

                if (newValuePercentage > 0) {
//                    System.out.println("newValue/model.getXscalePercentage()" + newValuePercentage + "/" + model.getXscalePercentage());
                    if (scaleLocked) {
                        Base.getMainWindow().getBed().getFirstPickedModel().getEditer().scaleX(newValuePercentage, modelOnPlatform, false);
                        Base.getMainWindow().getBed().getFirstPickedModel().getEditer().scaleY(newValuePercentage, modelOnPlatform, false);
                        Base.getMainWindow().getBed().getFirstPickedModel().getEditer().scaleZ(newValuePercentage, modelOnPlatform, false);

                        jTextField5.setText(textFieldValue);
                        jTextField6.setText(textFieldValue);
                    } else {
                        Base.getMainWindow().getBed().getFirstPickedModel().getEditer().scaleX(newValuePercentage, modelOnPlatform, false);
                    }
                }

            } else {
                //jTextField4.setText("");
            }
        }
    }//GEN-LAST:event_jTextField4KeyReleased

    private void jTextField5KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField5KeyReleased

        boolean modelOnPlatform = Base.getMainWindow().getBed().getFirstPickedModel().getEditer().isOnPlatform();
        Model model = Base.getMainWindow().getBed().getFirstPickedModel();
        double newValuePercentage;
        String textFieldValue = jTextField5.getText();

        if (!(textFieldValue.length() == 0)) {
            if (Base.isNumeric(jTextField5.getText())) {
                if (textFieldValue.contains(",")) {
                    textFieldValue = textFieldValue.replace(",", ".");
                }

                if (this.percentage) {
                    newValuePercentage = Double.parseDouble(textFieldValue);
                } else {

                    if (ProperDefault.get("measures").equals("inches")) {
                        double sizeInches = Double.parseDouble(textFieldValue);

                        double sizeMM = UnitConverter.inchesToMillimeters(sizeInches);
                        newValuePercentage = (sizeMM / this.initialWidth) * 100;
                    } else {
                        double sizeMM = Double.parseDouble(textFieldValue);

                        newValuePercentage = (sizeMM / this.initialWidth) * 100;
                    }
                }

                if (newValuePercentage > 0) {
//                    System.out.println("newValue/model.getXscalePercentage()" + newValuePercentage + "/" + model.getYscalePercentage());
                    if (scaleLocked) {
                        Base.getMainWindow().getBed().getFirstPickedModel().getEditer().scaleX(newValuePercentage, modelOnPlatform, false);
                        Base.getMainWindow().getBed().getFirstPickedModel().getEditer().scaleY(newValuePercentage, modelOnPlatform, false);
                        Base.getMainWindow().getBed().getFirstPickedModel().getEditer().scaleZ(newValuePercentage, modelOnPlatform, false);

                        jTextField4.setText(textFieldValue);
                        jTextField6.setText(textFieldValue);
                    } else {
                        Base.getMainWindow().getBed().getFirstPickedModel().getEditer().scaleY(newValuePercentage, modelOnPlatform, false);
                    }
                }

            } else {
                //jTextField4.setText("");
            }
        }

    }//GEN-LAST:event_jTextField5KeyReleased

    private void jTextField6KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField6KeyReleased

        boolean modelOnPlatform = Base.getMainWindow().getBed().getFirstPickedModel().getEditer().isOnPlatform();
        Model model = Base.getMainWindow().getBed().getFirstPickedModel();
        double newValuePercentage;
        String textFieldValue = jTextField6.getText();

        if (!(textFieldValue.length() == 0)) {
            if (Base.isNumeric(jTextField6.getText())) {
                if (textFieldValue.contains(",")) {
                    textFieldValue = textFieldValue.replace(",", ".");
                }

                if (this.percentage) {
                    newValuePercentage = Double.parseDouble(textFieldValue);

                } else {

                    if (ProperDefault.get("measures").equals("inches")) {
                        double sizeInches = Double.parseDouble(textFieldValue);

                        double sizeMM = UnitConverter.inchesToMillimeters(sizeInches);
                        newValuePercentage = (sizeMM / this.initialWidth) * 100;
                    } else {
                        double sizeMM = Double.parseDouble(textFieldValue);

                        newValuePercentage = (sizeMM / this.initialWidth) * 100;
                    }
                }

                if (newValuePercentage > 0) {
//                    System.out.println("newValue/model.getXscalePercentage()" + newValuePercentage + "/" + model.getZscalePercentage());
                    if (scaleLocked) {
                        Base.getMainWindow().getBed().getFirstPickedModel().getEditer().scaleX(newValuePercentage, modelOnPlatform, false);
                        Base.getMainWindow().getBed().getFirstPickedModel().getEditer().scaleY(newValuePercentage, modelOnPlatform, false);
                        Base.getMainWindow().getBed().getFirstPickedModel().getEditer().scaleZ(newValuePercentage, modelOnPlatform, false);

                        jTextField4.setText(textFieldValue);
                        jTextField5.setText(textFieldValue);
                    } else {
                        Base.getMainWindow().getBed().getFirstPickedModel().getEditer().scaleZ(newValuePercentage, modelOnPlatform, false);
                    }
                }

            } else {
                //jTextField4.setText("");
            }
        }

    }//GEN-LAST:event_jTextField6KeyReleased

    private void jLabel4MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MousePressed
        if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenter());
        } else {
            Base.getMainWindow().showFeedBackMessage("modelNotPicked");
        }
    }//GEN-LAST:event_jLabel4MousePressed

    private void jLabel1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MousePressed
//        Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterMoveSimple());
        if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterMove());
        } else {
            Base.getMainWindow().showFeedBackMessage("modelNotPicked");
        }
    }//GEN-LAST:event_jLabel1MousePressed

    private void jLabel2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MousePressed
//        Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterRotateSimple());
        if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterRotate());
        } else {
            Base.getMainWindow().showFeedBackMessage("modelNotPicked");
        }
    }//GEN-LAST:event_jLabel2MousePressed

    private void jLabel5MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MousePressed
//        Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterMirrorSimple());
        if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterMirror());
        } else {
            Base.getMainWindow().showFeedBackMessage("modelNotPicked");
        }
    }//GEN-LAST:event_jLabel5MousePressed

    private void jTextField4FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField4FocusGained
        Model model = Base.getMainWindow().getBed().getFirstPickedModel();

        if (this.percentage) {
            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().setYValue(model.getScaleYinPercentage());
            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().setZValue(model.getScaleZinPercentage());            
        } else {
            DecimalFormat df = new DecimalFormat("#.0");

            double depth = model.getEditer().getDepth();
            if (ProperDefault.get("measures").equals("inches")) {
                depth = UnitConverter.millimetersToInches(depth);
            }
            double height = model.getEditer().getHeight();
            if (ProperDefault.get("measures").equals("inches")) {
                height = UnitConverter.millimetersToInches(height);
            }         
            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().
                    setYValue(String.valueOf(df.format(depth)));
            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().
                    setZValue(String.valueOf(df.format(height)));            
        }        
    }//GEN-LAST:event_jTextField4FocusGained

    private void jTextField5FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField5FocusGained
        Model model = Base.getMainWindow().getBed().getFirstPickedModel();
        if (this.percentage) {
            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().setXValue(model.getScaleXinPercentage());
            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().setZValue(model.getScaleZinPercentage());            

        } else {
            DecimalFormat df = new DecimalFormat("#.0");
            
            double width = model.getEditer().getWidth();
            if (ProperDefault.get("measures").equals("inches")) {
                width = UnitConverter.millimetersToInches(width);
            }
            double height = model.getEditer().getHeight();
            if (ProperDefault.get("measures").equals("inches")) {
                height = UnitConverter.millimetersToInches(height);
            }             
            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().
                    setXValue(String.valueOf(df.format(width)));
            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().
                    setZValue(String.valueOf(df.format(height)));
        }
    }//GEN-LAST:event_jTextField5FocusGained

    private void jTextField6FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField6FocusGained
        Model model = Base.getMainWindow().getBed().getFirstPickedModel();
        if (this.percentage) {
            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().setXValue(model.getScaleXinPercentage());
            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().setYValue(model.getScaleYinPercentage());
        } else {
            DecimalFormat df = new DecimalFormat("#.0");
            
            double width = model.getEditer().getWidth();
            if (ProperDefault.get("measures").equals("inches")) {
                width = UnitConverter.millimetersToInches(width);
            }
            double depth = model.getEditer().getDepth();
            if (ProperDefault.get("measures").equals("inches")) {
                depth = UnitConverter.millimetersToInches(depth);
            }  
            
            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().
                    setXValue(String.valueOf(df.format(width)));
            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().
                    setYValue(String.valueOf(df.format(depth))); 
        }
    }//GEN-LAST:event_jTextField6FocusGained

    private void jLabel16MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel16MousePressed
        toggleOptions();
    }//GEN-LAST:event_jLabel16MousePressed

    private void jLabel7MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MousePressed
        toggleOptions();
    }//GEN-LAST:event_jLabel7MousePressed

    private void lockRatioMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lockRatioMousePressed
       lockRatio();
    }//GEN-LAST:event_lockRatioMousePressed

    private void l_lockRatioMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_l_lockRatioMousePressed
        lockRatio();
    }//GEN-LAST:event_l_lockRatioMousePressed
    private void jLabel18MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel18MouseEntered
        jLabel18.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_12.png")));
    }//GEN-LAST:event_jLabel18MouseEntered

    private void jLabel18MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel18MouseExited
        jLabel18.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_12.png")));
    }//GEN-LAST:event_jLabel18MouseExited

    private void jLabel18MousePressed(java.awt.event.MouseEvent evt) {                                      
        Model model = Base.getMainWindow().getBed().getFirstPickedModel();
        BuildVolume machineVolume = Base.getMainWindow().getMachineInterface().getModel().getBuildVolume();
        
        double scaleX = machineVolume.getX() / model.getEditer().getWidth();
        double scaleY = machineVolume.getY() / model.getEditer().getDepth();
        double scaleZ = machineVolume.getZ() / model.getEditer().getHeight();
        
        double scale = Math.min(scaleX, Math.min(scaleZ, scaleY));
        scale = scale - 0.02; //Small adjustment to avoid the model being out of bounds
        
        model.getEditer().scale(scale, true);
        
        if (this.isScalePercentage()) {
            setXValue(model.getScaleXinPercentage());
            setYValue(model.getScaleYinPercentage());
            setZValue(model.getScaleZinPercentage());
        } else {
            DecimalFormat df = new DecimalFormat("#.0");

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
            setXValue(df.format(width));
            setYValue(df.format(depth));
            setZValue(df.format(height));
        }
    }                                         

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
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
    private javax.swing.JLabel l_lockRatio;
    private javax.swing.JLabel lockRatio;
    // End of variables declaration//GEN-END:variables
}
