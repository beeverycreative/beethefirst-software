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

    private boolean check_pressed, lockedRatio;
    private boolean scaleLocked;
    private double initialWidth, initialDepth, initialHeight;
    private final boolean mm, percentage;
    private boolean checkX = true, checkY = true, checkZ = true;
    private double oldX, oldY, oldZ;
    DecimalFormat df = new DecimalFormat("#.0");

    public ModelsOperationCenterScale() {
        initComponents();
        this.check_pressed = false;
        this.mm = true;
        this.percentage = false;

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
            iFieldX.setText(model.getScaleXinPercentage());
            iFieldY.setText(model.getScaleYinPercentage());
            iFieldZ.setText(model.getScaleZinPercentage());

        } else {

            if (ProperDefault.get("measures").equals("inches")) {
                iFieldX.setText(df.format(UnitConverter.millimetersToInches(this.initialWidth)));
                iFieldY.setText(df.format(UnitConverter.millimetersToInches(this.initialDepth)));
                iFieldZ.setText(df.format(UnitConverter.millimetersToInches(this.initialHeight)));
            } else {
                iFieldX.setText(df.format(this.initialWidth));
                iFieldY.setText(df.format(this.initialDepth));
                iFieldZ.setText(df.format(this.initialHeight));
            }
        }

        oldX = Double.parseDouble(iFieldX.getText());
        oldY = Double.parseDouble(iFieldY.getText());
        oldZ = Double.parseDouble(iFieldZ.getText());

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
        jLabel7.setText(Languager.getTagValue(1, "MainWindowButtons", "MoreOptions"));

        if (this.percentage) {
            jLabel12.setText(Languager.getTagValue(1, "MainWindowButtons", "Scale_Extense"));
        } else {
            if (ProperDefault.get("measures").equals("inches")) {
                jLabel12.setText(Languager.getTagValue(1, "MainWindowButtons", "Scale")
                        + " (" + Languager.getTagValue(1, "MainWindowButtons", "Inches") + ")");
            } else {
                jLabel12.setText(Languager.getTagValue(1, "MainWindowButtons", "Scale")
                        + " (" + Languager.getTagValue(1, "MainWindowButtons", "MM") + ")");
            }
        }

        bScaleToMax.setText(Languager.getTagValue(1, "MainWindowButtons", "ScaleToMax"));
        bApply.setText(Languager.getTagValue(1, "MainWindowButtons", "Apply"));

    }

    public void setXValue(String val) {
        this.iFieldX.setText(val);
    }

    public void setYValue(String val) {
        this.iFieldY.setText(val);
    }

    public void setZValue(String val) {
        this.iFieldZ.setText(val);
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
        iFieldX = new javax.swing.JTextField();
        iFieldY = new javax.swing.JTextField();
        iFieldZ = new javax.swing.JTextField();
        bScaleToMax = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        bApply = new javax.swing.JLabel();
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
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel14)
                            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(iFieldZ, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                            .addComponent(iFieldY, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(iFieldX, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(bScaleToMax, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                            .addComponent(bApply, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
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
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel8)
                            .addComponent(jLabel16))
                        .addGap(51, 51, 51)
                        .addComponent(jLabel7))
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel7)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(49, Short.MAX_VALUE))
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

    private void jLabel16MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel16MousePressed
        toggleOptions();
    }//GEN-LAST:event_jLabel16MousePressed

    private void jLabel7MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MousePressed
        toggleOptions();
    }//GEN-LAST:event_jLabel7MousePressed

    private void bApplyMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bApplyMousePressed

        System.out.println("cliicked");
        boolean modelOnPlatform = Base.getMainWindow().getBed().getFirstPickedModel().getEditer().isOnPlatform();
        double ratioX = Double.parseDouble(iFieldX.getText()) / oldX * 100.0;
        System.out.println("ratioZ: "+ratioX);
        double ratioY = Double.parseDouble(iFieldY.getText()) / oldY * 100.0;
        System.out.println("ratioY: "+ratioY);
        double ratioZ = Double.parseDouble(iFieldZ.getText()) / oldZ * 100.0;
        System.out.println("ratioZ: "+ratioZ);

        Base.getMainWindow().getBed().getFirstPickedModel().getEditer().scaleX(ratioX, modelOnPlatform, false);
        Base.getMainWindow().getBed().getFirstPickedModel().getEditer().scaleY(ratioY, modelOnPlatform, false);
        Base.getMainWindow().getBed().getFirstPickedModel().getEditer().scaleZ(ratioZ, modelOnPlatform, false);
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
        scale = UnitConverter.round(scale, 3);

        model.getEditer().centerAndToBed();
        model.getEditer().scale(scale, true, false);

        if (model.getEditer().modelOutBonds()) {
            //Small adjustment to avoid the model being out of bounds
            scale = 0.975;
            model.getEditer().scale(scale, true, true);
        }

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

        //Sets the initial sizing variables to the current values
        this.resetInitialScaleVariables();
        model.resetScale();    }//GEN-LAST:event_bScaleToMaxMousePressed

    private void bScaleToMaxMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bScaleToMaxMouseExited
        bScaleToMax.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_12.png")));
    }//GEN-LAST:event_bScaleToMaxMouseExited

    private void bScaleToMaxMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bScaleToMaxMouseEntered
        bScaleToMax.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_12.png")));
    }//GEN-LAST:event_bScaleToMaxMouseEntered

    private void iFieldZKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_iFieldZKeyReleased

        double zVal = Double.parseDouble(iFieldZ.getText());
        double ratio = zVal / this.oldZ;

        //If i'm unselected do just this one
        if (checkZ == false) {
            this.oldZ = zVal;
            return;
        }

        //Or else, scale or the selected ones
        if (checkX == true) {
            double newX = ratio * Double.parseDouble(iFieldX.getText());
            iFieldX.setText(df.format(newX));
            this.oldX = newX;
        }
        if (checkY == true) {
            double newY = ratio * Double.parseDouble(iFieldY.getText());
            iFieldY.setText(df.format(newY));
            this.oldY = newY;
        }
        if (checkZ == true) {
            this.oldZ = zVal;
        }
    }//GEN-LAST:event_iFieldZKeyReleased

    private void iFieldZFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_iFieldZFocusGained
//        Model model = Base.getMainWindow().getBed().getFirstPickedModel();
//        if (this.percentage) {
//            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().setXValue(model.getScaleXinPercentage());
//            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().setYValue(model.getScaleYinPercentage());
//        } else {
//            DecimalFormat df = new DecimalFormat("#.0");
//
//            double width = model.getEditer().getWidth();
//            if (ProperDefault.get("measures").equals("inches")) {
//                width = UnitConverter.millimetersToInches(width);
//            }
//            double depth = model.getEditer().getDepth();
//            if (ProperDefault.get("measures").equals("inches")) {
//                depth = UnitConverter.millimetersToInches(depth);
//            }
//
//            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().
//            setXValue(String.valueOf(df.format(width)));
//            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().
//            setYValue(String.valueOf(df.format(depth)));
//        }
    }//GEN-LAST:event_iFieldZFocusGained

    private void iFieldYKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_iFieldYKeyReleased

        double yVal = Double.parseDouble(iFieldY.getText());
        double ratio = yVal / this.oldY;

        DecimalFormat df = new DecimalFormat("#.0");

        //If i'm unselected do just this one
        if (checkY == false) {
            this.oldY = yVal;
            return;
        }

        //Or else, scale or the selected ones
        if (checkX == true) {
            double newX = ratio * Double.parseDouble(iFieldX.getText());
            iFieldX.setText(df.format(newX));
            this.oldX = newX;
        }
        if (checkY == true) {
            this.oldY = yVal;
        }
        if (checkZ == true) {
            double newZ = ratio * Double.parseDouble(iFieldZ.getText());
            iFieldZ.setText(df.format(newZ));
            this.oldZ = newZ;
        }
    }//GEN-LAST:event_iFieldYKeyReleased

    private void iFieldYFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_iFieldYFocusGained
//        Model model = Base.getMainWindow().getBed().getFirstPickedModel();
//        if (this.percentage) {
//            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().setXValue(model.getScaleXinPercentage());
//            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().setZValue(model.getScaleZinPercentage());
//
//        } else {
//            DecimalFormat df = new DecimalFormat("#.0");
//
//            double width = model.getEditer().getWidth();
//            if (ProperDefault.get("measures").equals("inches")) {
//                width = UnitConverter.millimetersToInches(width);
//            }
//            double height = model.getEditer().getHeight();
//            if (ProperDefault.get("measures").equals("inches")) {
//                height = UnitConverter.millimetersToInches(height);
//            }
//            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().
//            setXValue(String.valueOf(df.format(width)));
//            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().
//            setZValue(String.valueOf(df.format(height)));
//        }
    }//GEN-LAST:event_iFieldYFocusGained

    private void iFieldXKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_iFieldXKeyReleased

        double xVal = Double.parseDouble(iFieldX.getText());
        double ratio = xVal / this.oldX;

        //If i'm unselected do just this one
        if (checkX == false) {
            this.oldX = xVal;
            return;
        }

        //Or else, scale or the selected ones
        if (checkX == true) {
            this.oldX = xVal;
        }
        if (checkY == true) {
            double newY = ratio * Double.parseDouble(iFieldY.getText());
            iFieldY.setText(df.format(newY));
            this.oldY = newY;
        }
        if (checkZ == true) {
            double newZ = ratio * Double.parseDouble(iFieldZ.getText());
            iFieldZ.setText(df.format(newZ));
            this.oldZ = newZ;
        }
    }//GEN-LAST:event_iFieldXKeyReleased

    private void iFieldXFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_iFieldXFocusGained
//        Model model = Base.getMainWindow().getBed().getFirstPickedModel();
//
//        if (this.percentage) {
//            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().setYValue(model.getScaleYinPercentage());
//            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().setZValue(model.getScaleZinPercentage());
//        } else {
//            DecimalFormat df = new DecimalFormat("#.0");
//
//            double depth = model.getEditer().getDepth();
//            if (ProperDefault.get("measures").equals("inches")) {
//                depth = UnitConverter.millimetersToInches(depth);
//            }
//            double height = model.getEditer().getHeight();
//            if (ProperDefault.get("measures").equals("inches")) {
//                height = UnitConverter.millimetersToInches(height);
//            }
//            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().
//            setYValue(String.valueOf(df.format(depth)));
//            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationScale().
//            setZValue(String.valueOf(df.format(height)));
//        }
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
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel2;
    // End of variables declaration//GEN-END:variables
}
