package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.machine.MachineInterface;

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
public class CalibrationWelcome extends BaseDialog {

    private final MachineInterface machine;
    private final DisposeFeedbackThread2 disposeThread;
    private boolean repeatCalibration;

    public CalibrationWelcome(boolean repeatCalibration) {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        setFont();
        setTextLanguage();
        machine = Base.getMachineLoader().getMachineInterface();
        this.repeatCalibration = repeatCalibration;
        disposeThread = new DisposeFeedbackThread2(this, machine);
        evaluateInitialConditions();
        enableDrag();
        moveToA();
        Base.systemThreads.add(disposeThread);
        centerOnScreen();
    }

    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProRegular("14"));
        jLabel3.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel4.setFont(GraphicDesignComponents.getSSProRegular("10"));
        jLabel5.setFont(GraphicDesignComponents.getSSProRegular("14"));
        bMinus005.setFont(GraphicDesignComponents.getSSProRegular("10"));
        bPlus005.setFont(GraphicDesignComponents.getSSProRegular("10"));
        bMinus05.setFont(GraphicDesignComponents.getSSProRegular("10"));
        bPlus05.setFont(GraphicDesignComponents.getSSProRegular("10"));
        bNext.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bExit.setFont(GraphicDesignComponents.getSSProRegular("12"));
    }

    private void setTextLanguage() {
        int fileKey = 1;
        jLabel1.setText(Languager.getTagValue(fileKey, "CalibrationWizard", "Title1"));
        String warning = "<html><br><b>" + Languager.getTagValue(fileKey, "CalibrationWizard", "Info_Warning") + "</b></html>";
        jLabel3.setText(splitString(Languager.getTagValue(fileKey, "CalibrationWizard", "Info") + warning));
        jLabel4.setText(Languager.getTagValue(fileKey, "CalibrationWizard", "Buttons_Info"));
        jLabel5.setText(Languager.getTagValue(fileKey, "FeedbackLabel", "MovingMessage"));
        bMinus005.setText("0.05 " + Languager.getTagValue(fileKey, "MainWindowButtons", "MM").toLowerCase());
        bPlus005.setText("0.05 " + Languager.getTagValue(fileKey, "MainWindowButtons", "MM").toLowerCase());
        bMinus05.setText("0.5 " + Languager.getTagValue(fileKey, "MainWindowButtons", "MM").toLowerCase());
        bPlus05.setText("0.5 " + Languager.getTagValue(fileKey, "MainWindowButtons", "MM").toLowerCase());
        bNext.setText(Languager.getTagValue(fileKey, "OptionPaneButtons", "Line7"));
        bExit.setText(Languager.getTagValue(fileKey, "OptionPaneButtons", "Line3"));
    }

    private String splitString(String s) {
        int width = 425;
        return buildString(s.split("\\."), width);
    }

    private String buildString(String[] parts, int width) {
        String text = "";
        String ihtml = "<html>";
        String ehtml = "</html>";
        String br = "<br>";

        for (int i = 0; i < parts.length; i++) {
            if (i + 1 < parts.length) {
                if (getStringPixelsWidth(parts[i]) + getStringPixelsWidth(parts[i + 1]) < width) {
                    text = text.concat(parts[i]).concat(".").concat(parts[i + 1]).concat(".").concat(br);
                    i++;
                } else {
                    text = text.concat(parts[i]).concat(".").concat(br);
                }
            } else {
                text = text.concat(parts[i]).concat(".");
            }
        }

        return ihtml.concat(text).concat(ehtml);
    }

    private int getStringPixelsWidth(String s) {
        Graphics g = getGraphics();
        FontMetrics fm = g.getFontMetrics(GraphicDesignComponents.getSSProRegular("10"));
        return fm.stringWidth(s);
    }

    private void enableMessageDisplay() {
        jPanel7.setBackground(new Color(255, 205, 3));
        jLabel5.setForeground(new Color(0, 0, 0));
    }

    private void disableMessageDisplay() {
        jPanel7.setBackground(new Color(248, 248, 248));
        jLabel5.setForeground(new Color(248, 248, 248));
    }

    private void evaluateInitialConditions() {
        enableMessageDisplay();
        disposeThread.start();
    }

    public void setZUse(boolean use) {
        this.repeatCalibration = use;
    }

    public void resetFeedbackComponents() {
        bMinus05.setEnabled(true);
        bMinus005.setEnabled(true);
        bPlus05.setEnabled(true);
        bPlus005.setEnabled(true);
        bNext.setEnabled(true);

        disableMessageDisplay();
//
    }

    public void showMessage() {
        bMinus05.setEnabled(false);
        bMinus005.setEnabled(false);
        bPlus05.setEnabled(false);
        bPlus005.setEnabled(false);
        bNext.setEnabled(false);

        enableMessageDisplay();
        jLabel5.setText(Languager.getTagValue(1, "FeedbackLabel", "MovingMessage"));
    }

    private void moveToA() {
        Base.writeLog("Initializing and Calibrating A", this.getClass());
        machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
        machine.runCommand(new replicatorg.drivers.commands.InitCalibration(repeatCalibration));
        machine.runCommand(new replicatorg.drivers.commands.RelativePositioning());
        machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
    }

    private void doCancel() {
        Base.writeLog("Cancelling calibration process", this.getClass());
        Base.getMainWindow().getButtons().updatePressedStateButton("quick_guide");
        Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
        machine.runCommand(new replicatorg.drivers.commands.AbsolutePositioning());
        machine.runCommand(new replicatorg.drivers.commands.EmergencyStop());

        if (ProperDefault.get("maintenance").equals("1")) {
            ProperDefault.remove("maintenance");
        }

        disposeThread.interrupt();
        Base.bringAllWindowsToFront();
        dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        bX = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        bMinus005 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        bMinus05 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        bPlus05 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        bPlus005 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        bNext = new javax.swing.JLabel();
        bExit = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(567, 501));
        setUndecorated(true);
        setResizable(false);

        jPanel3.setBackground(new java.awt.Color(248, 248, 248));

        jPanel5.setBackground(new java.awt.Color(248, 248, 248));
        jPanel5.setMinimumSize(new java.awt.Dimension(62, 26));
        jPanel5.setPreferredSize(new java.awt.Dimension(70, 30));

        bX.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_pressed_9.png"))); // NOI18N
        bX.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bXMousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addComponent(bX, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(bX, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(9, Short.MAX_VALUE))
        );

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/calibracao.png"))); // NOI18N

        jSeparator2.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator2.setForeground(new java.awt.Color(222, 222, 222));
        jSeparator2.setMinimumSize(new java.awt.Dimension(4, 1));
        jSeparator2.setPreferredSize(new java.awt.Dimension(50, 1));

        jLabel3.setText("Suspendisse potenti.");
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jPanel2.setBackground(new java.awt.Color(248, 248, 248));
        jPanel2.setMaximumSize(new java.awt.Dimension(192, 17));

        jLabel1.setBackground(new java.awt.Color(248, 248, 248));
        jLabel1.setText("CALIBRACAO");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel1)
                .addGap(0, 0, 0))
        );

        jPanel7.setBackground(new java.awt.Color(255, 203, 5));

        jLabel5.setText("Moving...Please wait.");
        jLabel5.setMaximumSize(new java.awt.Dimension(140, 17));
        jLabel5.setMinimumSize(new java.awt.Dimension(140, 17));
        jLabel5.setPreferredSize(new java.awt.Dimension(140, 17));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        jLabel4.setText("jLabel4");

        bMinus005.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_3.png"))); // NOI18N
        bMinus005.setText("Afinar -0.05");
        bMinus005.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_hover_3.png"))); // NOI18N
        bMinus005.setEnabled(false);
        bMinus005.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bMinus005.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bMinus005MousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bMinus005MouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bMinus005MouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(bMinus005)
                .addGap(0, 0, 0))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(bMinus005)
                .addGap(0, 0, 0))
        );

        bMinus05.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_3D.png"))); // NOI18N
        bMinus05.setText("Afinar -0.5");
        bMinus05.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_hover_3D.png"))); // NOI18N
        bMinus05.setEnabled(false);
        bMinus05.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bMinus05.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bMinus05MousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bMinus05MouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bMinus05MouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(bMinus05)
                .addGap(0, 0, 0))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(bMinus05)
                .addGap(0, 0, 0))
        );

        bPlus05.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_3_invertedD.png"))); // NOI18N
        bPlus05.setText("Afinar 0.5");
        bPlus05.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_hover_3_invertedD.png"))); // NOI18N
        bPlus05.setEnabled(false);
        bPlus05.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bPlus05.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bPlus05MousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bPlus05MouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bPlus05MouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(bPlus05)
                .addGap(0, 0, 0))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(bPlus05)
                .addGap(0, 0, 0))
        );

        bPlus005.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_3_inverted.png"))); // NOI18N
        bPlus005.setText("Afinar 0.05");
        bPlus005.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_hover_3_inverted.png"))); // NOI18N
        bPlus005.setEnabled(false);
        bPlus005.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bPlus005.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bPlus005MousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bPlus005MouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bPlus005MouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(bPlus005)
                .addGap(0, 0, 0))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(bPlus005)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addGap(0, 28, Short.MAX_VALUE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 369, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap())))
            .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(24, 24, 24)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel4.setBackground(new java.awt.Color(255, 203, 5));
        jPanel4.setMinimumSize(new java.awt.Dimension(20, 38));
        jPanel4.setPreferredSize(new java.awt.Dimension(567, 27));

        bNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bNext.setText("SEGUINTE");
        bNext.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_21.png"))); // NOI18N
        bNext.setEnabled(false);
        bNext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNext.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bNextMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bNextMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bNextMouseEntered(evt);
            }
        });

        bExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bExit.setText("SAIR");
        bExit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bExit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bExitMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bExitMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bExitMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bExit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bNext)
                .addGap(12, 12, 12))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bNext)
                    .addComponent(bExit))
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bMinus005MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bMinus005MouseEntered
        bMinus005.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_3.png")));
    }//GEN-LAST:event_bMinus005MouseEntered

    private void bMinus005MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bMinus005MouseExited
        bMinus005.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3.png")));
    }//GEN-LAST:event_bMinus005MouseExited

    private void bPlus005MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPlus005MouseEntered
        bPlus005.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_3_inverted.png")));
    }//GEN-LAST:event_bPlus005MouseEntered

    private void bPlus005MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPlus005MouseExited
        bPlus005.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3_inverted.png")));
    }//GEN-LAST:event_bPlus005MouseExited

    private void bExitMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExitMouseEntered
        bExit.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bExitMouseEntered

    private void bExitMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExitMouseExited
        bExit.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bExitMouseExited

    private void bNextMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNextMouseEntered
        bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bNextMouseEntered

    private void bNextMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNextMouseExited
        bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bNextMouseExited

    private void bMinus005MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bMinus005MousePressed
        Base.writeLog("Moving table -0.05mm in the Z axis", this.getClass());
        bMinus005.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_3.png")));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G0 Z-0.05"));
    }//GEN-LAST:event_bMinus005MousePressed

    private void bPlus005MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPlus005MousePressed
        Base.writeLog("Moving table 0.05mm in the Z axis", this.getClass());
        bPlus005.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_3_inverted.png")));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G0 Z0.05"));
    }//GEN-LAST:event_bPlus005MousePressed

    private void bMinus05MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bMinus05MouseEntered
        bMinus05.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_3D.png")));
    }//GEN-LAST:event_bMinus05MouseEntered

    private void bMinus05MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bMinus05MouseExited
        bMinus05.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3D.png")));
    }//GEN-LAST:event_bMinus05MouseExited

    private void bMinus05MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bMinus05MousePressed
        Base.writeLog("Moving table -0.5mm in the Z axis", this.getClass());
        bMinus05.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_3D.png")));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G0 Z-0.5"));
    }//GEN-LAST:event_bMinus05MousePressed

    private void bPlus05MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPlus05MouseEntered
        bPlus05.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_3_invertedD.png")));
    }//GEN-LAST:event_bPlus05MouseEntered

    private void bPlus05MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPlus05MouseExited
        bPlus05.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3_invertedD.png")));
    }//GEN-LAST:event_bPlus05MouseExited

    private void bPlus05MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bPlus05MousePressed
        Base.writeLog("Moving table 0.5mm in the Z axis", this.getClass());
        bPlus05.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_3_invertedD.png")));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G0 Z0.5"));
    }//GEN-LAST:event_bPlus05MousePressed

    private void bNextMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNextMousePressed
        if (bNext.isEnabled()) {
            Base.writeLog("Next button pressed, moving to next panel", this.getClass());
            machine.runCommand(new replicatorg.drivers.commands.AbsolutePositioning());
            CalibrationScrew1 p = new CalibrationScrew1();
            dispose();
            if (disposeThread.isAlive()) {
                disposeThread.stop();
            }
            p.setVisible(true);
        }
    }//GEN-LAST:event_bNextMousePressed

    private void bExitMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExitMousePressed
        doCancel();
    }//GEN-LAST:event_bExitMousePressed

    private void bXMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bXMousePressed
        doCancel();
    }//GEN-LAST:event_bXMousePressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bExit;
    private javax.swing.JLabel bMinus005;
    private javax.swing.JLabel bMinus05;
    private javax.swing.JLabel bNext;
    private javax.swing.JLabel bPlus005;
    private javax.swing.JLabel bPlus05;
    private javax.swing.JLabel bX;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JSeparator jSeparator2;
    // End of variables declaration//GEN-END:variables
}

class DisposeFeedbackThread2 extends Thread {

    private final MachineInterface machine;
    private final CalibrationWelcome calibrationPanel;

    public DisposeFeedbackThread2(CalibrationWelcome filIns, MachineInterface mach) {
        super("Calibration Welcome Thread");
        this.machine = mach;
        this.calibrationPanel = filIns;
    }

    @Override
    public void run() {
        while (this.isInterrupted() == false) {
            machine.getModel().setMachineReady(false);
            machine.runCommand(new replicatorg.drivers.commands.ReadStatus());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(DisposeFeedbackThread.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (machine.getModel().getMachineReady()
                    && machine.getDriver().isBusy() == false) {
                calibrationPanel.resetFeedbackComponents();
                break;
            } else {
                calibrationPanel.showMessage();
            }
        }
    }
}
