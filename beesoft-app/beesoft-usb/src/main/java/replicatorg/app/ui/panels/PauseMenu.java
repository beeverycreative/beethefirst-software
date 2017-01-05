package replicatorg.app.ui.panels;

import java.awt.Dialog;
import javax.swing.ImageIcon;
import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.app.ui.popups.Query;
import replicatorg.drivers.Driver;

public class PauseMenu extends BaseDialog {

    private static final int FILE_KEY = 1;

    private final Driver driver = Base.getMachineLoader().getMachineInterface().getDriver();

    public PauseMenu() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        showNoFilamentLabel();
        setFont();
        setTextLanguage();
        super.centerOnScreen();
    }

    private void setFont() {
        lTitle.setFont(GraphicDesignComponents.getSSProRegular("14"));
        lSubTitle.setFont(GraphicDesignComponents.getSSProRegular("12"));
        lChangeFilament.setFont(GraphicDesignComponents.getSSProBold("14"));
        lChangeFilamentDesc.setFont(GraphicDesignComponents.getSSProLight("12"));
        bChangeFilament.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bShutdown.setFont(GraphicDesignComponents.getSSProRegular("12"));
        lShutdownDesc.setFont(GraphicDesignComponents.getSSProLight("12"));
        lShutdown.setFont(GraphicDesignComponents.getSSProBold("14"));
        bCancel.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bResume.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabelNoFilament.setFont(GraphicDesignComponents.getSSProRegular("12"));
    }

    private void setTextLanguage() {
        lTitle.setText(Languager.getTagValue(FILE_KEY, "PausePanel", "Title").toUpperCase());
        lSubTitle.setText(Languager.getTagValue(FILE_KEY, "PausePanel", "SubTitle"));
        lChangeFilament.setText(Languager.getTagValue(FILE_KEY, "PausePanel", "FilamentTitle"));
        lChangeFilamentDesc.setText(Languager.getTagValue(FILE_KEY, "PausePanel", "FilamentIntro"));
        bChangeFilament.setText(Languager.getTagValue(FILE_KEY, "PausePanel", "bFilamentChange"));
        bShutdown.setText(Languager.getTagValue(FILE_KEY, "PausePanel", "bShutdown"));
        lShutdownDesc.setText(Languager.getTagValue(FILE_KEY, "PausePanel", "ShutdownIntro"));
        lShutdown.setText(Languager.getTagValue(FILE_KEY, "PausePanel", "ShutdownTitle"));
        jLabelNoFilament.setText(Languager.getTagValue(FILE_KEY, "PausePanel", "NoFilament").toUpperCase());
        bCancel.setText(Languager.getTagValue(FILE_KEY, "OptionPaneButtons", "Line3"));
        bResume.setText(Languager.getTagValue(FILE_KEY, "OptionPaneButtons", "Line12"));
    }

    private void showNoFilamentLabel() {
        String coilText;
        boolean showLabel;

        coilText = driver.getCoilText();
        showLabel = coilText.equals(FilamentControler.NO_FILAMENT)
                || coilText.contains(FilamentControler.NO_FILAMENT_2);

        jLabelNoFilament.setVisible(showLabel);
        bResume.setEnabled(!showLabel);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pMaintenance = new javax.swing.JPanel();
        pTop = new javax.swing.JPanel();
        lTitle = new javax.swing.JLabel();
        lSubTitle = new javax.swing.JLabel();
        pChangeFilament = new javax.swing.JPanel();
        bChangeFilament = new javax.swing.JLabel();
        lChangeFilamentDesc = new javax.swing.JLabel();
        lChangeFilament = new javax.swing.JLabel();
        jLabelNoFilament = new javax.swing.JLabel();
        jLabelNoFilament.setVisible(false);
        jSeparator1 = new javax.swing.JSeparator();
        pShutdown = new javax.swing.JPanel();
        bShutdown = new javax.swing.JLabel();
        lShutdownDesc = new javax.swing.JLabel();
        lShutdown = new javax.swing.JLabel();
        pBottom = new javax.swing.JPanel();
        bCancel = new javax.swing.JLabel();
        bResume = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setPreferredSize(new java.awt.Dimension(772, 356));
        setResizable(false);

        pMaintenance.setBackground(new java.awt.Color(248, 248, 248));

        pTop.setBackground(new java.awt.Color(248, 248, 248));

        lTitle.setText("Pause");
        lTitle.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        lSubTitle.setText("Subtitle");

        javax.swing.GroupLayout pTopLayout = new javax.swing.GroupLayout(pTop);
        pTop.setLayout(pTopLayout);
        pTopLayout.setHorizontalGroup(
            pTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pTopLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lTitle)
                    .addComponent(lSubTitle))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pTopLayout.setVerticalGroup(
            pTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pTopLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lSubTitle)
                .addGap(30, 30, 30))
        );

        pChangeFilament.setBackground(new java.awt.Color(248, 248, 248));

        bChangeFilament.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_12.png"))); // NOI18N
        bChangeFilament.setText("Mudar filamento agora");
        bChangeFilament.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bChangeFilament.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bChangeFilamentMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bChangeFilamentMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bChangeFilamentMousePressed(evt);
            }
        });

        lChangeFilamentDesc.setText("Lorem ipsum dolor sit amet.");

        lChangeFilament.setText("Mudar FIlamento");

        jLabelNoFilament.setForeground(new java.awt.Color(255, 0, 0));
        jLabelNoFilament.setText("NO FILAMENT SET");

        javax.swing.GroupLayout pChangeFilamentLayout = new javax.swing.GroupLayout(pChangeFilament);
        pChangeFilament.setLayout(pChangeFilamentLayout);
        pChangeFilamentLayout.setHorizontalGroup(
            pChangeFilamentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pChangeFilamentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pChangeFilamentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pChangeFilamentLayout.createSequentialGroup()
                        .addComponent(lChangeFilament)
                        .addGap(591, 629, Short.MAX_VALUE))
                    .addGroup(pChangeFilamentLayout.createSequentialGroup()
                        .addGroup(pChangeFilamentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lChangeFilamentDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(pChangeFilamentLayout.createSequentialGroup()
                                .addComponent(bChangeFilament)
                                .addGap(18, 18, 18)
                                .addComponent(jLabelNoFilament, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        pChangeFilamentLayout.setVerticalGroup(
            pChangeFilamentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pChangeFilamentLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(lChangeFilament)
                .addGap(2, 2, 2)
                .addComponent(lChangeFilamentDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pChangeFilamentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bChangeFilament)
                    .addComponent(jLabelNoFilament))
                .addGap(0, 0, 0))
        );

        jSeparator1.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator1.setForeground(new java.awt.Color(222, 222, 222));
        jSeparator1.setMinimumSize(new java.awt.Dimension(4, 1));
        jSeparator1.setPreferredSize(new java.awt.Dimension(1, 1));

        pShutdown.setBackground(new java.awt.Color(248, 248, 248));

        bShutdown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_12.png"))); // NOI18N
        bShutdown.setText("Shutdown");
        bShutdown.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_12.png"))); // NOI18N
        bShutdown.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bShutdown.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bShutdownMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bShutdownMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bShutdownMouseEntered(evt);
            }
        });

        lShutdownDesc.setText("Lorem ipsum dolor sit amet.");

        lShutdown.setText("Shutdown");

        javax.swing.GroupLayout pShutdownLayout = new javax.swing.GroupLayout(pShutdown);
        pShutdown.setLayout(pShutdownLayout);
        pShutdownLayout.setHorizontalGroup(
            pShutdownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pShutdownLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pShutdownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lShutdownDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 724, Short.MAX_VALUE)
                    .addGroup(pShutdownLayout.createSequentialGroup()
                        .addGroup(pShutdownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lShutdown)
                            .addComponent(bShutdown))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pShutdownLayout.setVerticalGroup(
            pShutdownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pShutdownLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lShutdown)
                .addGap(2, 2, 2)
                .addComponent(lShutdownDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bShutdown)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout pMaintenanceLayout = new javax.swing.GroupLayout(pMaintenance);
        pMaintenance.setLayout(pMaintenanceLayout);
        pMaintenanceLayout.setHorizontalGroup(
            pMaintenanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pMaintenanceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pMaintenanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pShutdown, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pChangeFilament, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pMaintenanceLayout.setVerticalGroup(
            pMaintenanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pMaintenanceLayout.createSequentialGroup()
                .addComponent(pTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pChangeFilament, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pShutdown, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(30, 30, 30))
        );

        pBottom.setBackground(new java.awt.Color(255, 203, 5));
        pBottom.setMinimumSize(new java.awt.Dimension(20, 26));
        pBottom.setPreferredSize(new java.awt.Dimension(139, 26));

        bCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bCancel.setText("Cancel");
        bCancel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bCancel.setMaximumSize(new java.awt.Dimension(69, 21));
        bCancel.setMinimumSize(new java.awt.Dimension(69, 21));
        bCancel.setPreferredSize(new java.awt.Dimension(69, 21));
        bCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bCancelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bCancelMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bCancelMousePressed(evt);
            }
        });

        bResume.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bResume.setText("Resume");
        bResume.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_21.png"))); // NOI18N
        bResume.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bResume.setMaximumSize(new java.awt.Dimension(69, 21));
        bResume.setMinimumSize(new java.awt.Dimension(69, 21));
        bResume.setPreferredSize(new java.awt.Dimension(69, 21));
        bResume.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bResumeMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bResumeMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bResumeMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout pBottomLayout = new javax.swing.GroupLayout(pBottom);
        pBottom.setLayout(pBottomLayout);
        pBottomLayout.setHorizontalGroup(
            pBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pBottomLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bResume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );
        pBottomLayout.setVerticalGroup(
            pBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pBottomLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(pBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bResume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pBottom, javax.swing.GroupLayout.DEFAULT_SIZE, 788, Short.MAX_VALUE)
            .addComponent(pMaintenance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pMaintenance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(pBottom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bCancelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMousePressed
        final Runnable action;
        final Query cancelPrintQuery;
        
        if (bCancel.isEnabled()) {
            action = () -> {
                dispose();
                driver.dispatchCommand("M112", COM.NO_RESPONSE);
            };
            cancelPrintQuery = new Query("CancelPrintText", action, null);
            cancelPrintQuery.setVisible(true);
        }
    }//GEN-LAST:event_bCancelMousePressed

    private void bCancelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseExited
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bCancelMouseExited

    private void bCancelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseEntered
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bCancelMouseEntered

    private void bShutdownMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bShutdownMousePressed
        if (bShutdown.isEnabled()) {
            driver.dispatchCommand("M36");
            dispose();
            ShutdownMenu shutdown = new ShutdownMenu();
            shutdown.setVisible(true);
        }
    }//GEN-LAST:event_bShutdownMousePressed

    private void bShutdownMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bShutdownMouseExited
        bShutdown.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_12.png")));
    }//GEN-LAST:event_bShutdownMouseExited

    private void bShutdownMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bShutdownMouseEntered
        bShutdown.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_12.png")));
    }//GEN-LAST:event_bShutdownMouseEntered

    private void bChangeFilamentMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bChangeFilamentMousePressed
        if (bChangeFilament.isEnabled()) {
            FilamentCodeInsertion p = new FilamentCodeInsertion();
            p.setVisible(true);

            bChangeFilament.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_12.png")));
            showNoFilamentLabel();
        }
    }//GEN-LAST:event_bChangeFilamentMousePressed

    private void bChangeFilamentMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bChangeFilamentMouseExited
        bChangeFilament.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_12.png")));
    }//GEN-LAST:event_bChangeFilamentMouseExited

    private void bChangeFilamentMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bChangeFilamentMouseEntered
        bChangeFilament.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_12.png")));
    }//GEN-LAST:event_bChangeFilamentMouseEntered

    private void bResumeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bResumeMouseEntered
        bResume.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bResumeMouseEntered

    private void bResumeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bResumeMouseExited
        bResume.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bResumeMouseExited

    private void bResumeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bResumeMousePressed
        if (bResume.isEnabled()) {
            PrintSplashAutonomous p = new PrintSplashAutonomous(
                    true, false
            );
            dispose();
            p.setVisible(true);
        }
    }//GEN-LAST:event_bResumeMousePressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bCancel;
    private javax.swing.JLabel bChangeFilament;
    private javax.swing.JLabel bResume;
    private javax.swing.JLabel bShutdown;
    private javax.swing.JLabel jLabelNoFilament;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lChangeFilament;
    private javax.swing.JLabel lChangeFilamentDesc;
    private javax.swing.JLabel lShutdown;
    private javax.swing.JLabel lShutdownDesc;
    private javax.swing.JLabel lSubTitle;
    private javax.swing.JLabel lTitle;
    private javax.swing.JPanel pBottom;
    private javax.swing.JPanel pChangeFilament;
    private javax.swing.JPanel pMaintenance;
    private javax.swing.JPanel pShutdown;
    private javax.swing.JPanel pTop;
    // End of variables declaration//GEN-END:variables
}
