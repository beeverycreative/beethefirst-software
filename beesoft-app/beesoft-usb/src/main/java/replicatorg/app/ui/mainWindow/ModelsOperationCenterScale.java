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
import replicatorg.util.Units_and_Numbers;
import static replicatorg.util.Units_and_Numbers.sGetDecimalStringAnyLocaleAsDouble;

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
    
    private double initialWidth, initialDepth, initialHeight;
    private final boolean mm;
    private boolean checkX = true, checkY = true, checkZ = true;
    private double oldX, oldY, oldZ;
    DecimalFormat df = new DecimalFormat("#0.00");
    static Double X_MIN = sGetDecimalStringAnyLocaleAsDouble(ProperDefault.get("editor.xmin"));
    static Double Y_MIN = sGetDecimalStringAnyLocaleAsDouble(ProperDefault.get("editor.ymin"));
    static Double Z_MIN = sGetDecimalStringAnyLocaleAsDouble(ProperDefault.get("editor.zmin"));
    static Double X_MAX = sGetDecimalStringAnyLocaleAsDouble(ProperDefault.get("editor.xmax"));
    static Double Y_MAX = sGetDecimalStringAnyLocaleAsDouble(ProperDefault.get("editor.ymax"));
    static Double Z_MAX = sGetDecimalStringAnyLocaleAsDouble(ProperDefault.get("editor.zmax"));

    public ModelsOperationCenterScale() {
        initComponents();
        this.mm = true;

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

        if (ProperDefault.get("measures").equals("inches")) {
            iFieldX.setText(df.format(Units_and_Numbers.millimetersToInches(this.initialWidth)));
            iFieldY.setText(df.format(Units_and_Numbers.millimetersToInches(this.initialDepth)));
            iFieldZ.setText(df.format(Units_and_Numbers.millimetersToInches(this.initialHeight)));
        } else if(ProperDefault.get("measures").equals("mm")) {
            iFieldX.setText(df.format(this.initialWidth));
            iFieldY.setText(df.format(this.initialDepth));
            iFieldZ.setText(df.format(this.initialHeight));
        }

        oldX = sGetDecimalStringAnyLocaleAsDouble(iFieldX.getText());
        oldY = sGetDecimalStringAnyLocaleAsDouble(iFieldY.getText());
        oldZ = sGetDecimalStringAnyLocaleAsDouble(iFieldZ.getText());

    }


    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel2.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel3.setFont(GraphicDesignComponents.getSSProLight("33"));
        jLabel4.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel5.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel12.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel13.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel14.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel15.setFont(GraphicDesignComponents.getSSProRegular("12"));
        iFieldX.setFont(GraphicDesignComponents.getSSProRegular("13"));
        iFieldY.setFont(GraphicDesignComponents.getSSProRegular("13"));
        iFieldZ.setFont(GraphicDesignComponents.getSSProRegular("13"));
        bScaleToMax.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bApply.setFont(GraphicDesignComponents.getSSProRegular("12"));
    }

    private void setTextLanguage() {
        jLabel1.setText(Languager.getTagValue(1, "MainWindowButtons", "Move"));
        jLabel2.setText(Languager.getTagValue(1, "MainWindowButtons", "Rotate"));
        jLabel3.setText(Languager.getTagValue(1, "ModelDetails", "Model"));
        jLabel4.setText(Languager.getTagValue(1, "MainWindowButtons", "Scale"));
        jLabel5.setText(Languager.getTagValue(1, "MainWindowButtons", "Mirror"));


            if (ProperDefault.get("measures").equals("inches")) {
                jLabel12.setText(Languager.getTagValue(1, "MainWindowButtons", "Scale")
                        + " (" + Languager.getTagValue(1, "MainWindowButtons", "Inches") + ")");
            } else if (ProperDefault.get("measures").equals("mm")){
                jLabel12.setText(Languager.getTagValue(1, "MainWindowButtons", "Scale")
                        + " (" + Languager.getTagValue(1, "MainWindowButtons", "MM") + ")");
            }

        bScaleToMax.setText(Languager.getTagValue(1, "MainWindowButtons", "ScaleToMax"));
        bApply.setText(Languager.getTagValue(1, "MainWindowButtons", "Apply"));

    }

    public void setXValue(String val) {
        this.iFieldX.setText(val);
        oldX = sGetDecimalStringAnyLocaleAsDouble(val);
    }

    public void setYValue(String val) {
        this.iFieldY.setText(val);
        oldY = sGetDecimalStringAnyLocaleAsDouble(val);
    }

    public void setZValue(String val) {
        this.iFieldZ.setText(val);
        oldZ = sGetDecimalStringAnyLocaleAsDouble(val);
    }

    private void toggleX() {
        if (checkX) {
            checkX = false;
            jLabel17.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_unchecked.png")));
        } else {
            checkX = true;
            jLabel17.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_checked.png")));
        }
    }

    private void toggleY() {
        if (checkY) {
            checkY = false;
            jLabel19.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_unchecked.png")));
        } else {
            checkY = true;
            jLabel19.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_checked.png")));
        }
    }

    private void toggleZ() {
        if (checkZ) {
            checkZ = false;
            jLabel20.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_unchecked.png")));
        } else {
            checkZ = true;
            jLabel20.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_checked.png")));
        }
    }

    public boolean isXLocked() {
        return this.checkX;
    }

    public boolean isYLocked() {
        return this.checkY;
    }

    public boolean isZLocked() {
        return this.checkZ;
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
        jPanel2 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        iFieldX = new javax.swing.JTextField();
        iFieldY = new javax.swing.JTextField();
        iFieldZ = new javax.swing.JTextField();
        bScaleToMax = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        bApply = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();

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

        iFieldX.setName("xScaleValue"); // NOI18N
        iFieldX.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                iFieldXFocusGained(evt);
            }
        });
        iFieldX.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                iFieldXKeyReleased(evt);
            }
        });

        iFieldY.setName("yScaleValue"); // NOI18N
        iFieldY.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                iFieldYFocusGained(evt);
            }
        });
        iFieldY.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                iFieldYKeyReleased(evt);
            }
        });

        iFieldZ.setName("zScaleValue"); // NOI18N
        iFieldZ.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                iFieldZFocusGained(evt);
            }
        });
        iFieldZ.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                iFieldZKeyReleased(evt);
            }
        });

        bScaleToMax.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        bScaleToMax.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_12.png"))); // NOI18N
        bScaleToMax.setText("Scale to Max");
        bScaleToMax.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bScaleToMax.setName("xAxisButton"); // NOI18N
        bScaleToMax.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bScaleToMaxMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bScaleToMaxMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bScaleToMaxMousePressed(evt);
            }
        });

        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/c_checked.png"))); // NOI18N
        jLabel17.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel17MousePressed(evt);
            }
        });

        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/c_checked.png"))); // NOI18N
        jLabel19.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel19MousePressed(evt);
            }
        });

        jLabel20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/c_checked.png"))); // NOI18N
        jLabel20.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel20MousePressed(evt);
            }
        });

        bApply.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        bApply.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_12.png"))); // NOI18N
        bApply.setText("Apply");
        bApply.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bApply.setName("xAxisButton"); // NOI18N
        bApply.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bApplyMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bApplyMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bApplyMousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(iFieldZ, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                            .addComponent(iFieldY, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(iFieldX, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel19))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(bScaleToMax, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(bApply, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(6, 6, 6))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel12)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(iFieldX, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(4, 4, 4)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(iFieldY, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(iFieldZ, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel15))
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bApply, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(bScaleToMax, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(17, 17, 17))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel6)
                        .addComponent(jLabel8))
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(jLabel8)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

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

    private void bApplyMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bApplyMousePressed
    
        boolean modelOnPlatform = Base.getMainWindow().getBed().getFirstPickedModel().getEditer().isOnPlatform();
        double valX = Units_and_Numbers.sGetDecimalStringAnyLocaleAsDouble(iFieldX.getText());
        double valY = Units_and_Numbers.sGetDecimalStringAnyLocaleAsDouble(iFieldY.getText());
        double valZ = Units_and_Numbers.sGetDecimalStringAnyLocaleAsDouble(iFieldZ.getText());                
           
        if(ProperDefault.get("measures").equals("inches")){
            valX = Units_and_Numbers.inchesToMillimeters(valX);
            valY = Units_and_Numbers.inchesToMillimeters(valY);
            valZ = Units_and_Numbers.inchesToMillimeters(valZ);            
        }                               
        
        Base.getMainWindow().getBed().getFirstPickedModel().getEditer().updateDimensions(valX, valY, valZ, modelOnPlatform);        
    }//GEN-LAST:event_bApplyMousePressed

    private void bApplyMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bApplyMouseExited
        System.out.println("exited");
        bApply.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_12.png")));
    }//GEN-LAST:event_bApplyMouseExited

    private void bApplyMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bApplyMouseEntered
        System.out.println("entered");
        bApply.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_12.png")));
    }//GEN-LAST:event_bApplyMouseEntered

    private void jLabel20MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel20MousePressed
        toggleZ();
    }//GEN-LAST:event_jLabel20MousePressed

    private void jLabel19MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel19MousePressed
        toggleY();
    }//GEN-LAST:event_jLabel19MousePressed

    private void jLabel17MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel17MousePressed
        toggleX();
    }//GEN-LAST:event_jLabel17MousePressed

    private void bScaleToMaxMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bScaleToMaxMousePressed
        Model model = Base.getMainWindow().getBed().getFirstPickedModel();
        BuildVolume machineVolume = Base.getMainWindow().getMachineInterface().getModel().getBuildVolume();

        double scaleX = machineVolume.getX() / model.getEditer().getWidth();
        double scaleY = machineVolume.getY() / model.getEditer().getDepth();
        double scaleZ = machineVolume.getZ() / model.getEditer().getHeight();

        double scale = Math.min(scaleX, Math.min(scaleZ, scaleY));
        scale = Units_and_Numbers.round(scale, 3);

        //model.getEditer().centerAndToBed();
        model.getEditer().center();
        model.getEditer().scale(scale, true, false);

        if (model.getEditer().modelOutBonds()) {
            //Small adjustment to avoid the model being out of bounds
            scale = 0.975;
            model.getEditer().scale(scale, true, true);
        }         

        //Sets the initial sizing variables to the current values
        this.resetInitialScaleVariables();
        model.resetScale();
        
        this.update_iFields();
    }//GEN-LAST:event_bScaleToMaxMousePressed

    private void bScaleToMaxMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bScaleToMaxMouseExited
        bScaleToMax.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_12.png")));
    }//GEN-LAST:event_bScaleToMaxMouseExited

    private void bScaleToMaxMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bScaleToMaxMouseEntered
        bScaleToMax.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_12.png")));
    }//GEN-LAST:event_bScaleToMaxMouseEntered

    private void iFieldZKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_iFieldZKeyReleased

        double zVal;

        try {
            zVal = sGetDecimalStringAnyLocaleAsDouble(iFieldZ.getText());
            //Protects against 0 scale
            if (zVal <= 0.0) {
                return;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        double ratio = zVal / this.oldZ;
        double newZ = zVal;
        this.oldZ = newZ;

        //If i'm unselected do just this one
        if (checkZ == false) {
            return;
        }//no need for else

        //Or else, scale or the selected ones                            
        if (checkY == true) {
            double yVal;
            try {
                yVal = sGetDecimalStringAnyLocaleAsDouble(iFieldY.getText());
            } catch (Exception e) {
                yVal = this.oldY;
                iFieldY.setText(df.format(yVal));
            }
            double newY = ratio * yVal;
            iFieldY.setText(df.format(newY));
            this.oldY = newY;
        }
        if (checkX == true) {
            double xVal;
            try {
                xVal = sGetDecimalStringAnyLocaleAsDouble(iFieldX.getText());
            } catch (Exception e) {
                xVal = this.oldX;
                iFieldX.setText(df.format(xVal));

            }
            double newX = ratio * xVal;
            iFieldX.setText(df.format(newX));
            this.oldX = newX;
        }
    }//GEN-LAST:event_iFieldZKeyReleased

    private void iFieldZFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_iFieldZFocusGained

        refresh_iFields();
    }//GEN-LAST:event_iFieldZFocusGained

    private void iFieldYKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_iFieldYKeyReleased

        double yVal;
        try {
            yVal = sGetDecimalStringAnyLocaleAsDouble(iFieldY.getText());
            //Protects against 0 scale
            if (yVal <= 0.0) {
                return;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        double ratio = yVal / this.oldY;
        double newY =yVal;
        this.oldY = newY;

        //If i'm unselected do just this one
        if (checkY == false) {
            return;
        }//no need for else

        //Or else, scale or the selected ones                            
        if (checkX == true) {
            double xVal;
            try {
                xVal = sGetDecimalStringAnyLocaleAsDouble(iFieldX.getText());
            } catch (Exception e) {
                xVal = this.oldX;
                iFieldX.setText(df.format(xVal));
            }
            double newX = ratio * xVal;
            iFieldX.setText(df.format(newX));
            this.oldX = newX;
        }
        if (checkZ == true) {
            double zVal;
            try {
                zVal = sGetDecimalStringAnyLocaleAsDouble(iFieldZ.getText());
            } catch (Exception e) {
                zVal = this.oldZ;
                iFieldY.setText(df.format(zVal));

            }
            double newZ = ratio * zVal;
            iFieldZ.setText(df.format(newZ));
            this.oldZ = newZ;
        }
    }//GEN-LAST:event_iFieldYKeyReleased

    private void iFieldYFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_iFieldYFocusGained

        refresh_iFields();

    }//GEN-LAST:event_iFieldYFocusGained

    private void iFieldXKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_iFieldXKeyReleased

        
        double xVal;
        try {
            xVal = sGetDecimalStringAnyLocaleAsDouble(iFieldX.getText());
            
            //Protects against 0 scale
            if (xVal <= 0.0) {
                return;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        double ratio = xVal / this.oldX;
        double newX = xVal;
        this.oldX = newX;

        //If i'm unselected do just this one
        if (checkX == false) {
            return;
        }//no need for else

        //Or else, scale or the selected ones                            
        if (checkY == true) {
            double yVal;
            try {
                yVal = sGetDecimalStringAnyLocaleAsDouble(iFieldY.getText());
            } catch (Exception e) {
                yVal = this.oldY;
                iFieldY.setText(df.format(yVal));
            }
            double newY = ratio * yVal;
            iFieldY.setText(df.format(newY));
            this.oldY = newY;
        }
        if (checkZ == true) {
            double zVal;
            try {
                zVal = sGetDecimalStringAnyLocaleAsDouble(iFieldZ.getText());
            } catch (Exception e) {
                zVal = this.oldZ;
                iFieldY.setText(df.format(zVal));

            }
            double newZ = ratio * zVal;
            iFieldZ.setText(df.format(newZ));
            this.oldZ = newZ;
        }
    }//GEN-LAST:event_iFieldXKeyReleased

    private void iFieldXFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_iFieldXFocusGained

        refresh_iFields();
    }//GEN-LAST:event_iFieldXFocusGained

    /**
     * Sets the initial sizing variables to the new max scale size
     */
    public void resetInitialScaleVariables() {

        Model model = Base.getMainWindow().getBed().getFirstPickedModel();

        this.initialWidth = model.getEditer().getWidth();
        this.initialHeight = model.getEditer().getHeight();
        this.initialDepth = model.getEditer().getDepth();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bApply;
    private javax.swing.JLabel bScaleToMax;
    private javax.swing.JTextField iFieldX;
    private javax.swing.JTextField iFieldY;
    private javax.swing.JTextField iFieldZ;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel2;
    // End of variables declaration//GEN-END:variables

    private void refresh_iFields() {
        
//        double x, y, z;
//        if(ProperDefault.get("measures").equals("inches")) {
//            x = Units_and_Numbers.millimetersToInches(this.oldX);
//            y = Units_and_Numbers.millimetersToInches(this.oldY);
//            z = Units_and_Numbers.millimetersToInches(this.oldZ);
//        } else {
//            x = this.oldX;
//            y = this.oldY;
//            z = this.oldZ;
//        }
                
        try {

            iFieldX.setText(df.format(this.oldX));
        } catch (Exception e) {
        }
        try {

            iFieldY.setText(df.format(this.oldY));
        } catch (Exception e) {
        }
        try {

            iFieldZ.setText(df.format(this.oldZ));
        } catch (Exception e) {
        }
    }
    
    /**
     * Updates the axis value fields with the current size values
     */
    private void update_iFields() {
            
        double width, depth, height;
        Model model = Base.getMainWindow().getBed().getFirstPickedModel();
        
        if(ProperDefault.get("measures").equals("inches")) {
            width = Units_and_Numbers.millimetersToInches(model.getEditer().getWidth());
            depth = Units_and_Numbers.millimetersToInches(model.getEditer().getDepth());
            height = Units_and_Numbers.millimetersToInches(model.getEditer().getHeight());
        } else {
            width = model.getEditer().getWidth();
            depth = model.getEditer().getDepth();
            height = model.getEditer().getHeight();
        }

        try {

            iFieldX.setText(df.format(width));
            this.oldX = model.getEditer().getWidth();
        } catch (Exception e) {
        }
        try {

            iFieldY.setText(df.format(depth));
            this.oldY = model.getEditer().getDepth();
        } catch (Exception e) {
        }
        try {

            iFieldZ.setText(df.format(height));
            this.oldZ = model.getEditer().getHeight();
        } catch (Exception e) {
        }
    }    
}
