package replicatorg.app.ui.mainWindow;

import javax.swing.ImageIcon;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ui.GraphicDesignComponents;

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
public class ModelsOperationCenter extends javax.swing.JPanel {

    public ModelsOperationCenter() {
        initComponents();
        Base.getMainWindow().getCanvas().setControlTool(0);
        setFont();
        setTextLanguage();
        bMoreOptions.setVisible(false);
        jLabel9.setVisible(false);
    }

    private void setFont() {
        bMove.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bRotate.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel3.setFont(GraphicDesignComponents.getSSProLight("33"));
        bScale.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bMirror.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bMoreOptions.setFont(GraphicDesignComponents.getSSProRegular("12"));
    }

    private void setTextLanguage() {
        bMove.setText(Languager.getTagValue(1, "MainWindowButtons", "Move"));
        bRotate.setText(Languager.getTagValue(1, "MainWindowButtons", "Rotate"));
        jLabel3.setText(Languager.getTagValue(1, "ModelDetails", "Model"));
        bScale.setText(Languager.getTagValue(1, "MainWindowButtons", "Scale"));
        bMirror.setText(Languager.getTagValue(1, "MainWindowButtons", "Mirror"));
        bMoreOptions.setText(Languager.getTagValue(1, "MainWindowButtons", "MoreOptions"));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bMove = new javax.swing.JLabel();
        bRotate = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        bScale = new javax.swing.JLabel();
        bMirror = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        bMoreOptions = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(190, 265));

        bMove.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        bMove.setForeground(new java.awt.Color(35, 31, 32));
        bMove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_1.png"))); // NOI18N
        bMove.setText("Move");
        bMove.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bMove.setName("moveButton"); // NOI18N
        bMove.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bMoveMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bMoveMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bMoveMouseEntered(evt);
            }
        });

        bRotate.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        bRotate.setForeground(new java.awt.Color(35, 31, 32));
        bRotate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_1.png"))); // NOI18N
        bRotate.setText("Rotate");
        bRotate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bRotate.setName("rotateButton"); // NOI18N
        bRotate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bRotateMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bRotateMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bRotateMousePressed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Ubuntu", 0, 33)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(35, 31, 32));
        jLabel3.setText("Model");

        bScale.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        bScale.setForeground(new java.awt.Color(35, 31, 32));
        bScale.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_1.png"))); // NOI18N
        bScale.setText("Scale");
        bScale.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bScale.setName("scaleButton"); // NOI18N
        bScale.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bScaleMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bScaleMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bScaleMousePressed(evt);
            }
        });

        bMirror.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        bMirror.setForeground(new java.awt.Color(35, 31, 32));
        bMirror.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_simple_1.png"))); // NOI18N
        bMirror.setText("Mirror");
        bMirror.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bMirror.setName("mirrorButton"); // NOI18N
        bMirror.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bMirrorMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bMirrorMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bMirrorMousePressed(evt);
            }
        });

        jLabel6.setName("moreOptionsCheckbox"); // NOI18N

        bMoreOptions.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        bMoreOptions.setForeground(new java.awt.Color(35, 31, 32));
        bMoreOptions.setText("More Options");
        bMoreOptions.setName("moreOptionsTitle"); // NOI18N

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/c_disabled.png"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bMove, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(24, 24, 24)
                                .addComponent(jLabel8))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(bMirror, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(bScale, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(bRotate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addGap(51, 51, 51)
                                .addComponent(bMoreOptions)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel3)
                .addGap(24, 24, 24)
                .addComponent(bMove, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bRotate, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bScale, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bMirror, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel8))
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bMoreOptions)
                    .addComponent(jLabel9))
                .addContainerGap(18, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void bMoveMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bMoveMouseEntered
        bMove.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_1.png")));
    }//GEN-LAST:event_bMoveMouseEntered

    private void bMoveMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bMoveMouseExited
        bMove.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_1.png")));
    }//GEN-LAST:event_bMoveMouseExited

    private void bRotateMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bRotateMouseEntered
        bRotate.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_1.png")));
    }//GEN-LAST:event_bRotateMouseEntered

    private void bRotateMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bRotateMouseExited
        bRotate.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_1.png")));
    }//GEN-LAST:event_bRotateMouseExited

    private void bScaleMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bScaleMouseEntered
        bScale.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_1.png")));
    }//GEN-LAST:event_bScaleMouseEntered

    private void bScaleMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bScaleMouseExited
        bScale.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_1.png")));
    }//GEN-LAST:event_bScaleMouseExited

    private void bMirrorMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bMirrorMouseEntered
        bMirror.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_hover_1.png")));
    }//GEN-LAST:event_bMirrorMouseEntered

    private void bMirrorMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bMirrorMouseExited
        bMirror.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_1.png")));
    }//GEN-LAST:event_bMirrorMouseExited

    private void bMoveMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bMoveMousePressed
//      Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterMoveSimple());
        if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterMove());
        } else {
            Base.getMainWindow().showFeedBackMessage("modelNotPicked");
        }
    }//GEN-LAST:event_bMoveMousePressed

    private void bRotateMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bRotateMousePressed
//        Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterRotateSimple());
        if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterRotate());
        } else {
            Base.getMainWindow().showFeedBackMessage("modelNotPicked");
        }
    }//GEN-LAST:event_bRotateMousePressed

    private void bScaleMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bScaleMousePressed
//        Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterScaleSimple());
        if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterScale());
        } else {
            Base.getMainWindow().showFeedBackMessage("modelNotPicked");
        }
    }//GEN-LAST:event_bScaleMousePressed

    private void bMirrorMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bMirrorMousePressed
//        Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterMirrorSimple());
        if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterMirror());
        } else {
            Base.getMainWindow().showFeedBackMessage("modelNotPicked");
        }
    }//GEN-LAST:event_bMirrorMousePressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bMirror;
    private javax.swing.JLabel bMoreOptions;
    private javax.swing.JLabel bMove;
    private javax.swing.JLabel bRotate;
    private javax.swing.JLabel bScale;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    // End of variables declaration//GEN-END:variables
}
