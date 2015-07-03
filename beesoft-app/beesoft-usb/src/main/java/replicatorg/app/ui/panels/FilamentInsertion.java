package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import replicatorg.app.Base;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.machine.MachineInterface;
import replicatorg.util.Point5d;

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
public class FilamentInsertion extends BaseDialog {

    private final MachineInterface machine;
    private boolean bLoadMouseClickedReady = true;
    private boolean bUnloadMouseClickedReady = true;
    private final boolean jLabel17GoBack = false;
    private final DisposeFeedbackThread disposeThread;
    private boolean jLabel18MouseClickedReady;
    private boolean unloadPressed;
    private String previousColor = "";

    public FilamentInsertion() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        setFont();
        setTextLanguage();
        centerOnScreen();
        Base.getMainWindow().setEnabled(false);
        machine = Base.getMachineLoader().getMachineInterface();
        moveToPosition();
        enableDrag();

        previousColor = machine.getModel().getCoilCode();
        disposeThread = new DisposeFeedbackThread(this, machine);
        disposeThread.start();
        Base.systemThreads.add(disposeThread);
        jLabel17.setVisible(false);
        if (Base.printPaused == true) {
            bExit.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
        }
        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());
    }

    public void resetFeedbackComponents() {
        if (!bLoadMouseClickedReady) {
            bLoad.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3.png")));
            bLoadMouseClickedReady = true;
            if (unloadPressed == false) {
                bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
            } else {
                if (Base.printPaused == true) {
                    bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
                    bExit.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
                } else {
                    bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                }
            }
            jLabel18MouseClickedReady = true;
        }

        if (!bUnloadMouseClickedReady) {
            jLabel3.setText(Languager.getTagValue(1, "FilamentWizard", "Exchange_Info_Title2"));
            jLabel2.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "unloadImage.png")));
            jLabel4.setText(splitString(Languager.getTagValue(1, "Print", "Print_Unloaded1").concat(" ").concat(Languager.getTagValue(1, "Print", "Print_Unloaded2")).concat(Languager.getTagValue(1, "Print", "Print_Unloaded3"))));
            bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3_inverted.png")));
            jLabel2.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "retirar_filamento-04.png")));
            bUnloadMouseClickedReady = true;
            if (unloadPressed == false) {
                bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
            } else {
                if (Base.printPaused == true) {
                    bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
                    bExit.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
                } else {
                    bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                }
            }
            jLabel18MouseClickedReady = true;
//            jLabel5.setVisible(false);
        }

        if (!jLabel18MouseClickedReady) {
            if (unloadPressed == false) {
                bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
            } else {
                if (Base.printPaused == true) {
                    bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
                    bExit.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
                } else {
                    bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
                }
            }
            jLabel18MouseClickedReady = true;
        }


        disableMessageDisplay();
    }

    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProRegular("14"));
        jLabel3.setFont(GraphicDesignComponents.getSSProBold("12"));
        jLabel4.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bLoad.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bUnload.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel7.setFont(GraphicDesignComponents.getSSProRegular("14"));
        jLabel17.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bNext.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bExit.setFont(GraphicDesignComponents.getSSProRegular("12"));

    }

    private void setTextLanguage() {
        jLabel1.setText(Languager.getTagValue(1, "FilamentWizard", "Title2"));
        jLabel3.setText(Languager.getTagValue(1, "FilamentWizard", "Exchange_Info_Title"));
        jLabel4.setText(splitString(Languager.getTagValue(1, "FilamentWizard", "Exchange_Info2")));
        bLoad.setText(Languager.getTagValue(1, "FilamentWizard", "LoadButton"));
        bUnload.setText(Languager.getTagValue(1, "FilamentWizard", "UnloadButton"));
        jLabel7.setText(Languager.getTagValue(1, "FeedbackLabel", "MovingMessage"));
        jLabel17.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line4"));
        bNext.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line7"));
        bExit.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line3"));

    }

    public void showMessage() {
        // Active during movement
        // REDSOFT: Implement This timer
        enableMessageDisplay();
        jLabel7.setText(Languager.getTagValue(1, "FeedbackLabel", "MovingMessage"));
    }

    private void enableMessageDisplay() {
        jPanel5.setBackground(new Color(255, 205, 3));
        jLabel7.setForeground(new Color(0, 0, 0));
    }

    private void disableMessageDisplay() {
        jPanel5.setBackground(new Color(248, 248, 248));
        jLabel7.setForeground(new Color(248, 248, 248));
    }

    private void moveToPosition() {
        Point5d rest = machine.getTablePoints("rest");

        double acLow = machine.getAcceleration("acLow");
        double acHigh = machine.getAcceleration("acHigh");
        double spHigh = machine.getFeedrate("spHigh");

        if (Base.printPaused == false) {
            machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
            machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spHigh));
            machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28", COM.BLOCK));
            machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acLow));
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(rest));
            machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acHigh));
            machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
        } else {

            machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
            machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acLow));
            machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F" + spHigh + " X-85 Y-65"));
            machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acHigh));
            machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
        }
    }

    private void finalizeHeat() {
        machine.runCommand(new replicatorg.drivers.commands.SetTemperature(0));
    }

    private String splitString(String s) {
        int width = 436;
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

    private void doCancel() {

        if (Base.printPaused) {
            return;
        } else {
            dispose();
            Base.getMainWindow().handleStop();
            Base.bringAllWindowsToFront();
            Base.getMainWindow().getButtons().updatePressedStateButton("quick_guide");
            Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
            Base.maintenanceWizardOpen = false;
            disposeThread.stop();
            Base.enableAllOpenWindows();
            Point5d b = machine.getTablePoints("safe");
            double acLow = machine.getAcceleration("acLow");
            double acHigh = machine.getAcceleration("acHigh");
            double spHigh = machine.getFeedrate("spHigh");

            if (Base.printPaused == false) {
                machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acLow));
                machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spHigh));
                machine.runCommand(new replicatorg.drivers.commands.QueuePoint(b));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acHigh));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28", COM.BLOCK));
                machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
                finalizeHeat();
            } else {
                machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
                machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spHigh));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acLow));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 X-85 Y-60"));
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acHigh));
                machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
            }
        }

        if (ProperDefault.get("maintenance").equals("1")) {
            ProperDefault.remove("maintenance");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        bLoad = new javax.swing.JLabel();
        bUnload = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel4 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        bNext = new javax.swing.JLabel();
        bExit = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(567, 490));
        setUndecorated(true);
        setPreferredSize(new java.awt.Dimension(571, 490));
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));
        jPanel1.setPreferredSize(new java.awt.Dimension(541, 570));

        jLabel1.setText("INSERIR FILAMENTO");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jLabel3.setText("Como descarregar ou carregar o filamento");

        jLabel4.setText("Suspendisse potenti. ");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jPanel3.setBackground(new java.awt.Color(248, 248, 248));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/infografia-01.png"))); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(532, 233));

        bLoad.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_3.png"))); // NOI18N
        bLoad.setText("Load");
        bLoad.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bLoad.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bLoadMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bLoadMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bLoadMousePressed(evt);
            }
        });

        bUnload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_3_inverted.png"))); // NOI18N
        bUnload.setText("Unload");
        bUnload.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bUnload.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bUnloadMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bUnloadMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bUnloadMousePressed(evt);
            }
        });

        jSeparator2.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator2.setForeground(new java.awt.Color(222, 222, 222));
        jSeparator2.setMinimumSize(new java.awt.Dimension(4, 1));
        jSeparator2.setPreferredSize(new java.awt.Dimension(50, 1));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(bLoad)
                    .addComponent(bUnload))
                .addContainerGap())
            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(125, 125, 125)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 404, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bLoad)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bUnload)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );

        jPanel4.setBackground(new java.awt.Color(248, 248, 248));
        jPanel4.setMinimumSize(new java.awt.Dimension(62, 26));
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
                .addGap(43, 43, 43)
                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(14, 14, 14))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(255, 203, 5));
        jPanel5.setPreferredSize(new java.awt.Dimension(169, 17));

        jLabel7.setText("Moving...Please wait.");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel7))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                .addGap(6, 6, 6))
        );

        jPanel2.setBackground(new java.awt.Color(255, 203, 5));
        jPanel2.setMinimumSize(new java.awt.Dimension(20, 38));
        jPanel2.setPreferredSize(new java.awt.Dimension(567, 38));

        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        jLabel17.setText("ANTERIOR");
        jLabel17.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel17.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel17MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel17MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel17MousePressed(evt);
            }
        });

        bNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_21.png"))); // NOI18N
        bNext.setText("SEGUINTE");
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

        bExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bExit.setText("SAIR");
        bExit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bExit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bExitMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bExitMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bExitMousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bExit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 343, Short.MAX_VALUE)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bNext)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(bNext)
                    .addComponent(bExit))
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 571, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 571, Short.MAX_VALUE)
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

    private void bLoadMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bLoadMouseEntered
        if (bLoadMouseClickedReady) {
            bLoad.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_3.png")));
        }
    }//GEN-LAST:event_bLoadMouseEntered

    private void bLoadMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bLoadMouseExited
        if (bLoadMouseClickedReady) {
            bLoad.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3.png")));
        }
    }//GEN-LAST:event_bLoadMouseExited

    private void bUnloadMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bUnloadMouseEntered
        if (bUnloadMouseClickedReady) {
            bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_3_inverted.png")));
        }    }//GEN-LAST:event_bUnloadMouseEntered

    private void bUnloadMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bUnloadMouseExited
        if (bUnloadMouseClickedReady) {
            bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3_inverted.png")));
        }    }//GEN-LAST:event_bUnloadMouseExited

    private void bExitMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExitMouseEntered
        if (Base.printPaused) {
            return;
        } else {
            bExit.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
        }

    }//GEN-LAST:event_bExitMouseEntered

    private void bExitMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExitMouseExited
        if (Base.printPaused) {
            return;
        } else {
            bExit.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
        }
    }//GEN-LAST:event_bExitMouseExited

    private void bNextMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNextMouseEntered
        if (Base.printPaused && unloadPressed) {
            return;
        } else {
            if (jLabel18MouseClickedReady) {
                bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
            }
        }
    }//GEN-LAST:event_bNextMouseEntered

    private void bNextMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNextMouseExited
        if (Base.printPaused && unloadPressed) {
            return;
        } else {
            if (jLabel18MouseClickedReady) {
                bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
            }
        }
    }//GEN-LAST:event_bNextMouseExited

    private void jLabel17MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel17MouseEntered
        jLabel17.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_jLabel17MouseEntered

    private void jLabel17MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel17MouseExited
        jLabel17.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_jLabel17MouseExited

    private void bNextMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNextMousePressed
        if (!machine.getDriver().isBusy()) {

            if (unloadPressed == false) {
                dispose();
                disposeThread.stop();
                FilamentCodeInsertion p = new FilamentCodeInsertion(previousColor);
                p.setVisible(true);
            } else {
                if (Base.printPaused == false) {
                    disposeThread.stop();
                    dispose();
                    Base.bringAllWindowsToFront();
                    finalizeHeat();
                    Base.getMainWindow().getButtons().updatePressedStateButton("quick_guide");
                    Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
                    Base.maintenanceWizardOpen = false;
                    Base.enableAllOpenWindows();

                    Point5d b = machine.getTablePoints("safe");
                    double acLow = machine.getAcceleration("acLow");
                    double acHigh = machine.getAcceleration("acHigh");
                    double spHigh = machine.getFeedrate("spHigh");

                    machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acLow));
                    machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spHigh));
                    machine.runCommand(new replicatorg.drivers.commands.QueuePoint(b));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 x" + acHigh));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
                }
            }
        }
    }//GEN-LAST:event_bNextMousePressed

    private void jLabel17MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel17MousePressed
        if (jLabel17GoBack) {
            dispose();
            FilamentHeating p = new FilamentHeating();
            p.setVisible(true);
        }

    }//GEN-LAST:event_jLabel17MousePressed

    private void bExitMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bExitMousePressed
        doCancel();
    }//GEN-LAST:event_bExitMousePressed

    private void bLoadMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bLoadMousePressed
        if (!machine.getDriver().isBusy()) {

            jLabel2.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "infografia-01.png")));
            jLabel4.setText(splitString(Languager.getTagValue(1, "FilamentWizard", "Exchange_Info2")));
            bNext.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line7"));
            unloadPressed = false;

            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        Base.writeLog("Load filament pressed");
                        machine.getDriver().setBusy(true);
                        showMessage();
                        bLoad.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_3.png")));
                        bLoadMouseClickedReady = false;
                        bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
                        jLabel18MouseClickedReady = false;

                        Base.writeLog("Loading Filament");

                        //machine.runCommand(new replicatorg.drivers.commands.SetMotorDirection(DriverCommand.AxialDirection.CLOCKWISE));
                        machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
                        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G92 E"));
                        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 P500", COM.BLOCK));
                        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 S0 P500", COM.BLOCK));
                        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 P500", COM.BLOCK));
                        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 S0 P500", COM.BLOCK));
                        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 P500", COM.BLOCK));
                        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 S0 P500", COM.BLOCK));
                        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F300 E100", COM.BLOCK));
                        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G92 E", COM.BLOCK));
                        machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            if (Base.printPaused == true) {
                bExit.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
                bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
            }

        }
    }//GEN-LAST:event_bLoadMousePressed

    private void bUnloadMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bUnloadMousePressed
        if (!machine.getDriver().isBusy()) {
            Base.writeLog("Unload filament pressed");
            machine.getDriver().setBusy(true);
            showMessage();
            bUnload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_3_inverted.png")));
            bUnloadMouseClickedReady = false;
            bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
            jLabel2.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "unload-01.png")));
            jLabel4.setText(splitString(Languager.getTagValue(1, "FilamentWizard", "Exchange_Info3")));

            jLabel18MouseClickedReady = false;
            ProperDefault.put("filamentCoilRemaining", String.valueOf("0"));
            ProperDefault.put("coilCode", String.valueOf("N/A"));
            bNext.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line6"));
            unloadPressed = true;

            Base.writeLog("Unloading Filament");

            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {

                    //Set fillament as NONE
                    machine.runCommand(new replicatorg.drivers.commands.SetCoilCode(FilamentControler.NO_FILAMENT_CODE));

                    machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G92 E", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 P500", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 S0 P500", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 P500", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 S0 P500", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 P500", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M300 S0 P500", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F250 E50", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G92 E", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F1000 E-23", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F800 E2", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F2000 E-23", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G1 F200 E-50", COM.BLOCK));
                    machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G92 E", COM.BLOCK));

                    machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
//                    unloadPressed = true;

                }
            });

            if (Base.printPaused == true) {
                bExit.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
                bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
            }
        }
    }//GEN-LAST:event_bUnloadMousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        doCancel();
    }//GEN-LAST:event_jLabel15MousePressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bExit;
    private javax.swing.JLabel bLoad;
    private javax.swing.JLabel bNext;
    private javax.swing.JLabel bUnload;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JSeparator jSeparator2;
    // End of variables declaration//GEN-END:variables
}

class DisposeFeedbackThread extends Thread {

    private final MachineInterface machine;
    private final FilamentInsertion filamentPanel;

    public DisposeFeedbackThread(FilamentInsertion filIns, MachineInterface mach) {
        super("Filament Insertion Thread");
        this.machine = mach;
        this.filamentPanel = filIns;
    }

    @Override
    public void run() {

        while (true) {
            machine.runCommand(new replicatorg.drivers.commands.ReadStatus());
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
                Logger.getLogger(DisposeFeedbackThread.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (!machine.getDriver().getMachineStatus()) {
                filamentPanel.showMessage();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DisposeFeedbackThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (machine.getDriver().getMachineStatus()
                    && !machine.getDriver().isBusy()) {
                filamentPanel.resetFeedbackComponents();
            }

        }
    }
}