package replicatorg.app.ui.popups;

import java.awt.Dialog;
import replicatorg.app.Base;
import replicatorg.app.Languager;
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
public class Feedback extends BaseDialog {

    public static final int FLASHING_MAIN_MESSAGE = 0;
    public static final int FLASHING_SUB_MESSAGE = 1;
    public static final int FLASHING_SUB_MESSAGE_NO_CALIBRATION = 2;
    public static final int LAUNCHING_MESSAGE = 3;
    public static final int SAVING_MESSAGE = 4;
    public static final int RESTART_PRINTER = 5;

    private static final int FILE_KEY = 1;
    private static final String ROOT_TAG = "FeedbackPanel";

    public Feedback() {
        super(Base.getMainWindow(), Dialog.ModalityType.MODELESS);
        setName("FeedbackDialog");
        initComponents();
        setFont();
        centerOnScreen();
        enableDrag();
        lFeedbackMain.setText("");
        lFeedbackSub.setText("");
        lFeedbackRestart.setText("");
    }

    public void setFeedback1(int index) {
        switch (index) {
            case FLASHING_MAIN_MESSAGE:
                lFeedbackMain.setText(Languager.getTagValue(FILE_KEY, ROOT_TAG, "FlashingMainMessage"));
                break;
            default:
                break;
        }
    }

    public void setFeedback2(int index) {
        switch (index) {
            case FLASHING_SUB_MESSAGE:
                lFeedbackSub.setText(Languager.getTagValue(FILE_KEY, ROOT_TAG, "FlashingSubMessage"));
                break;
            case FLASHING_SUB_MESSAGE_NO_CALIBRATION:
                lFeedbackSub.setText(Languager.getTagValue(FILE_KEY, ROOT_TAG, "FlashingSubMessageNoCalibration"));
                break;
            case LAUNCHING_MESSAGE:
                lFeedbackSub.setText(Languager.getTagValue(FILE_KEY, ROOT_TAG, "LaunchingMessage"));
                break;
            case SAVING_MESSAGE:
                lFeedbackSub.setText(Languager.getTagValue(FILE_KEY, ROOT_TAG, "SavingMessage"));
                break;
            case RESTART_PRINTER:
                lFeedbackSub.setText(Languager.getTagValue(FILE_KEY, ROOT_TAG, "RestartMessage"));
                break;
            default:
                break;
        }
    }
    
    public void setFeedback3(int index) {
        switch(index) {
            case RESTART_PRINTER:
                lFeedbackSub.setText(Languager.getTagValue(FILE_KEY, ROOT_TAG, "RestartMessageTimeout"));
                break;
            default: 
                break;
        }
    }

    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProLight("33"));
        lFeedbackMain.setFont(GraphicDesignComponents.getSSProBold("12"));
        lFeedbackSub.setFont(GraphicDesignComponents.getSSProRegular("12"));
        lFeedbackRestart.setFont(GraphicDesignComponents.getSSProRegular("12"));

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        lFeedbackMain = new javax.swing.JLabel();
        lFeedbackSub = new javax.swing.JLabel();
        loading = new javax.swing.JLabel();
        lFeedbackRestart = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(350, 150));
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
            .addGap(0, 37, Short.MAX_VALUE)
        );

        lFeedbackMain.setText("Feedback1");

        lFeedbackSub.setText("Feedback2");

        loading.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/loading.gif"))); // NOI18N

        lFeedbackRestart.setText("Feedback3");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lFeedbackRestart)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(lFeedbackMain))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 182, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lFeedbackSub)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(loading)
                        .addGap(75, 75, 75))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(lFeedbackMain))
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lFeedbackSub)
                    .addComponent(loading))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lFeedbackRestart)
                .addContainerGap(56, Short.MAX_VALUE))
        );

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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel lFeedbackMain;
    private javax.swing.JLabel lFeedbackRestart;
    private javax.swing.JLabel lFeedbackSub;
    private javax.swing.JLabel loading;
    // End of variables declaration//GEN-END:variables
}
