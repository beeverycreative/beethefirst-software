package replicatorg.app.ui.panels;

import java.awt.Dialog;
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
public class CancelPrint extends BaseDialog {

    private boolean cancel = false;

    public CancelPrint() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        enableDrag();
        centerOnScreen();
        enableDrag();
    }

    public boolean isCancelTrue() {
        return cancel;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jCancelText = new javax.swing.JLabel();
        jCancelTitle = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        bNo = new javax.swing.JLabel();
        bYes = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        setUndecorated(true);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));

        jPanel4.setBackground(new java.awt.Color(248, 248, 248));
        jPanel4.setMinimumSize(new java.awt.Dimension(62, 26));
        jPanel4.setPreferredSize(new java.awt.Dimension(70, 30));
        jPanel4.setRequestFocusEnabled(false);

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_pressed_9.png"))); // NOI18N
        jLabel15.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel15MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(9, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(248, 248, 248));

        jCancelText.setFont(GraphicDesignComponents.getSSProBold("12"));
        jCancelText.setText("<html><p>" + Languager.getTagValue(1, "CancelPrint", "CancelPrintText") + "</p></html>");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(78, 78, 78)
                .addComponent(jCancelText, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(78, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(jCancelText)
                .addContainerGap(94, Short.MAX_VALUE))
        );

        jCancelTitle.setFont(GraphicDesignComponents.getSSProRegular("14"));
        jCancelTitle.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 18, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jCancelTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCancelTitle))
                .addGap(26, 26, 26)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 203, 5));
        jPanel2.setMinimumSize(new java.awt.Dimension(20, 46));

        bNo.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bNo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_18.png"))); // NOI18N
        bNo.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line2"));
        bNo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bNoMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bNoMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bNoMouseEntered(evt);
            }
        });

        bYes.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bYes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_18.png"))); // NOI18N
        bYes.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line1"));
        bYes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bYes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bYesMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bYesMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bYesMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bYes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bNo)
                .addGap(12, 12, 12))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bNo)
                    .addComponent(bYes))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bNoMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNoMouseEntered
        bNo.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_18.png")));
    }//GEN-LAST:event_bNoMouseEntered

    private void bNoMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNoMouseExited
        bNo.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_18.png")));
    }//GEN-LAST:event_bNoMouseExited

    private void bNoMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNoMousePressed
        cancel = false;
        dispose();
        Base.bringAllWindowsToFront();
    }//GEN-LAST:event_bNoMousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        cancel = false;
        dispose();
        Base.bringAllWindowsToFront();
    }//GEN-LAST:event_jLabel15MousePressed

    private void bYesMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bYesMousePressed
        cancel = true;
        dispose();
    }//GEN-LAST:event_bYesMousePressed

    private void bYesMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bYesMouseExited
        bYes.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_18.png")));
    }//GEN-LAST:event_bYesMouseExited

    private void bYesMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bYesMouseEntered
        bYes.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_18.png")));
    }//GEN-LAST:event_bYesMouseEntered

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bNo;
    private javax.swing.JLabel bYes;
    private javax.swing.JLabel jCancelText;
    private javax.swing.JLabel jCancelTitle;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    // End of variables declaration//GEN-END:variables
}
