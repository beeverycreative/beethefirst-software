package replicatorg.app.ui.panels;

import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
import pt.beeverycreative.beesoft.filaments.Nozzle;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.drivers.Driver;

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
public class NozzleSwitch7 extends BaseDialog {
    
    private final Driver driver = Base.getMachineLoader().getMachineInterface().getDriver();

    public NozzleSwitch7() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        setFont();
        setTextLanguage();
        super.centerOnScreen();
        super.enableDrag();
        
    }


    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProLight("33"));
        jProceedFilChange.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bNo.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bYes.setFont(GraphicDesignComponents.getSSProRegular("12"));

    }

    private void setTextLanguage() {
        jProceedFilChange.setText(Languager.getTagValue("NozzleSwitch", "ProceedFilChange"));
        bNo.setText(Languager.getTagValue("OptionPaneButtons", "Line2"));
        bYes.setText(Languager.getTagValue("OptionPaneButtons", "Line1"));
    }


    public void setMessage(String message) {
        jProceedFilChange.setText("<html>" + Languager.getTagValue("Updates", message) + "</html>");
    }

    private void doCancel() {
        Base.writeLog("Filament heating canceled", this.getClass());
        Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
        dispose();
        driver.dispatchCommand("M704", UsbPassthroughDriver.COM.NO_RESPONSE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jProceedFilChange = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        bNo = new javax.swing.JLabel();
        bYes = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(350, 180));
        setUndecorated(true);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));

        jLabel1.setText("BEESOFT");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jPanel4.setBackground(new java.awt.Color(248, 248, 248));
        jPanel4.setMinimumSize(new java.awt.Dimension(62, 26));
        jPanel4.setRequestFocusEnabled(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 64, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 26, Short.MAX_VALUE)
        );

        jProceedFilChange.setText("Proceed to insert filament wizard?");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jProceedFilChange, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 199, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addComponent(jProceedFilChange)
                .addContainerGap(56, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 203, 5));
        jPanel2.setMinimumSize(new java.awt.Dimension(20, 46));

        bNo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_15.png"))); // NOI18N
        bNo.setText("No");
        bNo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bNoMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bNoMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bNoMousePressed(evt);
            }
        });

        bYes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_15.png"))); // NOI18N
        bYes.setText("Yes");
        bYes.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_disabled_15.png"))); // NOI18N
        bYes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bYes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bYesMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bYesMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bYesMousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bYes)
                .addGap(12, 12, 12)
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
        bNo.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_15.png")));
    }//GEN-LAST:event_bNoMouseEntered

    private void bNoMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNoMouseExited
        bNo.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_15.png")));
    }//GEN-LAST:event_bNoMouseExited

    private void bYesMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bYesMouseEntered
        bYes.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_15.png")));
    }//GEN-LAST:event_bYesMouseEntered

    private void bYesMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bYesMouseExited
        bYes.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_15.png")));
    }//GEN-LAST:event_bYesMouseExited

    private void bNoMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNoMousePressed
        if (bNo.isEnabled()) {
            doCancel();
        }
    }//GEN-LAST:event_bNoMousePressed

    private void bYesMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bYesMousePressed
        
        final FilamentCodeInsertion filamentCodeInsertion;

        if (bYes.isEnabled()) {
            driver.setCoilText(FilamentControler.NO_FILAMENT);

            //filamentCodeInsertion = new FilamentCodeInsertion(true);
            filamentCodeInsertion = new FilamentCodeInsertion(false,true);
            dispose();
            filamentCodeInsertion.setVisible(true);
        }
    }//GEN-LAST:event_bYesMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bNo;
    private javax.swing.JLabel bYes;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel jProceedFilChange;
    // End of variables declaration//GEN-END:variables
}
