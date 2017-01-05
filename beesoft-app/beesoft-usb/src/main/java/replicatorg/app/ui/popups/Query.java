package replicatorg.app.ui.popups;

import java.awt.Dialog;
import javax.swing.ImageIcon;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.app.ui.panels.BaseDialog;

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
public class Query extends BaseDialog {

    private final BaseDialog operationToDisplay;
    private final Runnable runnableToExecute;
    private final String configToBeSet;

    /**
     * Query constructor that opens another dialog as soon as the Yes button has
     * been pressed
     *
     * @param messageToDisplay the key of the message that is to be displayed
     * @param operationToDisplay dialog that is to be displayed
     * @param configToBeSet if this is not null, a checkbox will be displayed,
     * allowing the user to toggle a given configuration
     */
    public Query(final String messageToDisplay, final BaseDialog operationToDisplay, String configToBeSet) {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        setTextLanguage();
        centerOnScreen();
        enableDrag();
        this.operationToDisplay = operationToDisplay;
        this.runnableToExecute = null;
        this.configToBeSet = configToBeSet;

        if (configToBeSet == null) {
            jCheckboxNotAgain.setVisible(false);
        }

        jLabel2.setText("<html>" + Languager.getTagValue(1, "StatusMessages", messageToDisplay) + "</html>");
    }

    /**
     * Query constructor that executes a given runnable as as soon as the Yes
     * button has been pressed
     *
     * @param messageToDisplay the key of the message that is to be displayed
     * @param runnable runnable that is to be executed
     * @param configToBeSet if this is not null, a checkbox will be displayed,
     * allowing the user to toggle a given configuration
     */
    public Query(final String messageToDisplay, final Runnable runnable, String configToBeSet) {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        setTextLanguage();
        centerOnScreen();
        enableDrag();
        this.operationToDisplay = null;
        this.runnableToExecute = runnable;
        this.configToBeSet = configToBeSet;

        if (configToBeSet == null) {
            jCheckboxNotAgain.setVisible(false);
        }

        jLabel2.setText("<html>" + Languager.getTagValue(1, "StatusMessages", messageToDisplay) + "</html>");
    }

    private void setTextLanguage() {
        bYes.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line1"));
        bNo.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line2"));
        jCheckboxNotAgain.setText(Languager.getTagValue(1, "OptionPaneButtons", "DontDisplayAgain"));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lTitle = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        bX = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        bYes = new javax.swing.JLabel();
        bNo = new javax.swing.JLabel();
        jCheckboxNotAgain = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(10, 10));
        setUndecorated(true);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));

        lTitle.setFont(new java.awt.Font("Source Sans Pro", 0, 15)); // NOI18N
        lTitle.setText("BEESOFT");
        lTitle.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jPanel4.setBackground(new java.awt.Color(248, 248, 248));
        jPanel4.setMinimumSize(new java.awt.Dimension(62, 26));
        jPanel4.setRequestFocusEnabled(false);

        bX.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_pressed_9.png"))); // NOI18N
        bX.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bXMousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(bX, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(15, 15, 15))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bX, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        jLabel2.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Functionality not yet supported...");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lTitle)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(255, 203, 5));
        jPanel2.setMinimumSize(new java.awt.Dimension(20, 46));

        bYes.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        bYes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_18.png"))); // NOI18N
        bYes.setText("Yes");
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

        bNo.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        bNo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_18.png"))); // NOI18N
        bNo.setText("No");
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

        jCheckboxNotAgain.setBackground(new java.awt.Color(255, 203, 5));
        jCheckboxNotAgain.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        jCheckboxNotAgain.setText("Don't show this again");
        jCheckboxNotAgain.setBorder(null);
        jCheckboxNotAgain.setContentAreaFilled(false);
        jCheckboxNotAgain.setFocusPainted(false);
        jCheckboxNotAgain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckboxNotAgainActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckboxNotAgain)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bYes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bNo)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bYes)
                    .addComponent(bNo)
                    .addComponent(jCheckboxNotAgain, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void bYesMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bYesMouseEntered
        bYes.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_18.png")));
    }//GEN-LAST:event_bYesMouseEntered

    private void bYesMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bYesMouseExited
        bYes.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_18.png")));
    }//GEN-LAST:event_bYesMouseExited

    private void bYesMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bYesMousePressed
        if (bYes.isEnabled()) {
            if (operationToDisplay != null) {
                dispose();
                operationToDisplay.setVisible(true);
            } else if (runnableToExecute != null) {
                dispose();
                runnableToExecute.run();
            } else {
                dispose();
            }
        }
    }//GEN-LAST:event_bYesMousePressed

    private void bXMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bXMousePressed
        if (bX.isEnabled()) {
            dispose();
        }
    }//GEN-LAST:event_bXMousePressed

    private void bNoMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNoMousePressed
        if (bNo.isEnabled()) {
            dispose();
        }
    }//GEN-LAST:event_bNoMousePressed

    private void bNoMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNoMouseEntered
        bNo.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_18.png")));

    }//GEN-LAST:event_bNoMouseEntered

    private void bNoMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNoMouseExited
        bNo.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_18.png")));
    }//GEN-LAST:event_bNoMouseExited

    private void jCheckboxNotAgainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckboxNotAgainActionPerformed
        if(jCheckboxNotAgain.isEnabled()) {
            ProperDefault.put(configToBeSet, String.valueOf(jCheckboxNotAgain.isSelected()));
        }
    }//GEN-LAST:event_jCheckboxNotAgainActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bNo;
    private javax.swing.JLabel bX;
    private javax.swing.JLabel bYes;
    private javax.swing.JCheckBox jCheckboxNotAgain;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel lTitle;
    // End of variables declaration//GEN-END:variables
}
