package replicatorg.app.ui.panels;

import java.awt.Dialog;
import javax.swing.ImageIcon;
import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
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
public class ExtruderMaintenance1 extends BaseDialog {
    
    private final Driver driver = Base.getMachineLoader().getMachineInterface().getDriver();
    private final int temperatureGoal = 200;
    
    public ExtruderMaintenance1() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        setFont();
        setTextLanguage();
        enableDrag();
        centerOnScreen();
        
        if (!driver.getLastStatusMessage().contains("W:")) {
            moveToPosition();
        }
        
    }
    
    private void setFont() {
        lTitle.setFont(GraphicDesignComponents.getSSProRegular("14"));
        pText1.setFont(GraphicDesignComponents.getSSProBold("12"));
        pText2.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bNext.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bQuit.setFont(GraphicDesignComponents.getSSProRegular("12"));
    }
    
    private void setTextLanguage() {
        lTitle.setText(Languager.getTagValue(1, "ExtruderMaintenance", "Title1"));
        
        String text1 = "<html>"
                + Languager.getTagValue(1, "ExtruderMaintenance", "Info1a")
                + "<br>"
                + Languager.getTagValue(1, "ExtruderMaintenance", "Info1b")
                + "</html>";
        pText1.setText(text1);
        
        String warning = "<html><b>" + Languager.getTagValue(1, "ExtruderMaintenance", "Info_Warning1") + "</b></html>";
        
        pText2.setText(warning);
        
        bNext.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line7"));
        bQuit.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line3"));
    }
    
    private void moveToPosition() {
        Base.writeLog("Heating...", this.getClass());
        driver.dispatchCommand("M703 S" + (temperatureGoal + 5), COM.NO_RESPONSE);
        driver.setBusy(true);
    }
    
    private void doCancel() {
        dispose();
        Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
        driver.dispatchCommand("M704", COM.NO_RESPONSE);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lTitle = new javax.swing.JLabel();
        pExtruder = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        pText1 = new javax.swing.JLabel();
        pText2 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        pButtons = new javax.swing.JPanel();
        bNext = new javax.swing.JLabel();
        bQuit = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));

        lTitle.setText("EM_Start");
        lTitle.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        pExtruder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/bico_extrusao.png"))); // NOI18N

        jSeparator2.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator2.setForeground(new java.awt.Color(222, 222, 222));
        jSeparator2.setMinimumSize(new java.awt.Dimension(4, 1));
        jSeparator2.setPreferredSize(new java.awt.Dimension(50, 1));

        pText1.setText("Temperatura da Cabeca de Impressao");
        pText1.setMaximumSize(new java.awt.Dimension(999, 999));

        pText2.setText("Suspendisse potenti.");
        pText2.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        pText2.setMaximumSize(new java.awt.Dimension(152, 25));
        pText2.setMinimumSize(new java.awt.Dimension(152, 25));
        pText2.setPreferredSize(new java.awt.Dimension(152, 25));

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

        pButtons.setBackground(new java.awt.Color(255, 203, 5));
        pButtons.setMinimumSize(new java.awt.Dimension(20, 38));
        pButtons.setPreferredSize(new java.awt.Dimension(567, 26));

        bNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bNext.setText("SEGUINTE");
        bNext.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_21.png"))); // NOI18N
        bNext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNext.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bNextMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bNextMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bNextMousePressed(evt);
            }
        });

        bQuit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bQuit.setText("SAIR");
        bQuit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bQuit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bQuitMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bQuitMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bQuitMousePressed(evt);
            }
        });

        javax.swing.GroupLayout pButtonsLayout = new javax.swing.GroupLayout(pButtons);
        pButtons.setLayout(pButtonsLayout);
        pButtonsLayout.setHorizontalGroup(
            pButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bQuit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bNext)
                .addGap(12, 12, 12))
        );
        pButtonsLayout.setVerticalGroup(
            pButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pButtonsLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(pButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bNext)
                    .addComponent(bQuit))
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(186, 186, 186)
                .addComponent(pExtruder)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(pButtons, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(pText2, javax.swing.GroupLayout.PREFERRED_SIZE, 545, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pText1, javax.swing.GroupLayout.PREFERRED_SIZE, 545, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lTitle, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(42, 42, 42)
                .addComponent(pExtruder)
                .addGap(86, 86, 86)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pText1, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pText2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pText2.getAccessibleContext().setAccessibleDescription("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bQuitMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bQuitMouseEntered
        bQuit.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bQuitMouseEntered

    private void bQuitMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bQuitMouseExited
        bQuit.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bQuitMouseExited

    private void bNextMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNextMouseEntered
        bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bNextMouseEntered

    private void bNextMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNextMouseExited
        bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bNextMouseExited

    private void bNextMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNextMousePressed
        if (bNext.isEnabled()) {
            dispose();
            ExtruderMaintenance2 p = new ExtruderMaintenance2();
            p.setVisible(true);
        }
    }//GEN-LAST:event_bNextMousePressed

    private void bQuitMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bQuitMousePressed
        if (bQuit.isEnabled()) {
            doCancel();
        }
    }//GEN-LAST:event_bQuitMousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        if (jLabel15.isEnabled()) {
            doCancel();
        }
    }//GEN-LAST:event_jLabel15MousePressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bNext;
    private javax.swing.JLabel bQuit;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lTitle;
    private javax.swing.JPanel pButtons;
    private javax.swing.JLabel pExtruder;
    private javax.swing.JLabel pText1;
    private javax.swing.JLabel pText2;
    // End of variables declaration//GEN-END:variables
}
