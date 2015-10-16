package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.machine.MachineInterface;
import replicatorg.machine.model.AxisId;
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
public class CalibrationWelcome extends BaseDialog {

    private JLabel val_0;
    private JLabel val_35;
    private JLabel val_65;
    private JLabel val_100;
    private final boolean panelHidden;
    private final MachineInterface machine;

    private boolean jLabel12MouseClickedReady = true;
    private boolean jLabel16MouseClickedReady = true;
    private boolean jLabel9MouseClickedReady = true;
    private boolean jLabel10MouseClickedReady = true;
    private boolean jLabel21MouseClickedReady = true;
    private final DisposeFeedbackThread2 disposeThread;
    private boolean keepZ;
    private double currentValue;
    private double height;
    private final double safeDistance;

    public CalibrationWelcome(boolean repeat) {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        initSlidersLables();
        setFont();
        setTextLanguage();
        initSliderConfigs();
        machine = Base.getMachineLoader().getMachineInterface();
        keepZ = repeat;
        evaluateInitialConditions();
        panelHidden = false;
        currentValue = 0.0;
        safeDistance = 122;
        disposeThread = new DisposeFeedbackThread2(this, machine);
        disposeThread.start();
        enableDrag();
        moveToA();
        Base.maintenanceWizardOpen = true;
        Base.systemThreads.add(disposeThread);
        centerOnScreen();
        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());
    }

    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProRegular("14"));
        jLabel3.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel4.setFont(GraphicDesignComponents.getSSProRegular("10"));
        jLabel5.setFont(GraphicDesignComponents.getSSProRegular("14"));
//        jLabel6.setFont(GraphicDesignComponents.getSSProBold("12"));
//        jLabel7.setFont(GraphicDesignComponents.getSSProBold("12"));
//        jLabel8.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel9.setFont(GraphicDesignComponents.getSSProRegular("10"));
        jLabel10.setFont(GraphicDesignComponents.getSSProRegular("10"));
//        jLabel11.setFont(GraphicDesignComponents.getSSProRegular("12"));        
        jLabel12.setFont(GraphicDesignComponents.getSSProRegular("10"));
        jLabel16.setFont(GraphicDesignComponents.getSSProRegular("10"));
        jLabel20.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel21.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel22.setFont(GraphicDesignComponents.getSSProRegular("12"));
    }

    private void setTextLanguage() {
        int fileKey = 1;
        jLabel1.setText(Languager.getTagValue(fileKey, "CalibrationWizard", "Title1"));
        String warning = "<html><br><b>" + Languager.getTagValue(fileKey, "CalibrationWizard", "Info_Warning") + "</b></html>";
        jLabel3.setText(splitString(Languager.getTagValue(fileKey, "CalibrationWizard", "Info") + warning));
        jLabel4.setText(Languager.getTagValue(fileKey, "CalibrationWizard", "Buttons_Info"));
        jLabel5.setText(Languager.getTagValue(fileKey, "FeedbackLabel", "MovingMessage"));
//        jLabel6.setText(Languager.getTagValue("CalibrationWizard", "CalibrationOldValue"));
//        jLabel7.setText(Languager.getTagValue("CalibrationWizard", "CalibrationCurrentValue"));
        jLabel9.setText("0.05 " + Languager.getTagValue(fileKey, "MainWindowButtons", "MM").toLowerCase());
        jLabel10.setText("0.05 " + Languager.getTagValue(fileKey, "MainWindowButtons", "MM").toLowerCase());
        jLabel12.setText("0.5 " + Languager.getTagValue(fileKey, "MainWindowButtons", "MM").toLowerCase());
        jLabel16.setText("0.5 " + Languager.getTagValue(fileKey, "MainWindowButtons", "MM").toLowerCase());
        jLabel20.setText(Languager.getTagValue(fileKey, "OptionPaneButtons", "Line4"));
        jLabel21.setText(Languager.getTagValue(fileKey, "OptionPaneButtons", "Line7"));
        jLabel22.setText(Languager.getTagValue(fileKey, "OptionPaneButtons", "Line3"));

    }

    private void initSliderConfigs() {

        Hashtable labelTable1 = new Hashtable();
        labelTable1.put(new Integer(0), val_0);
        labelTable1.put(new Integer(35), val_35);
        labelTable1.put(new Integer(65), val_65);
        labelTable1.put(new Integer(100), val_100);

    }

    private void initSlidersLables() {
        val_0 = new JLabel("0.05");
        val_35 = new JLabel("0.5");
        val_65 = new JLabel("5");
        val_100 = new JLabel("10");

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
        Base.getMainWindow().setEnabled(false);
        jLabel20.setVisible(false);
        //turn off blower before heating
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M107"));
        machine.runCommand(new replicatorg.drivers.commands.SetTemperature(120));
        enableMessageDisplay();

        if (ProperDefault.get("maintenance").equals("1")) {
            jLabel20.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
        }
        machine.runCommand(new replicatorg.drivers.commands.ReadZValue());
    }

    public void setZUse(boolean use) {
        this.keepZ = use;
    }

    public void resetFeedbackComponents() {
        if (!jLabel12MouseClickedReady) {
            jLabel12.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3D.png")));
            jLabel12MouseClickedReady = true;
            jLabel21.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
            jLabel21MouseClickedReady = true;
        }

        if (!jLabel16MouseClickedReady) {
            jLabel16.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3_invertedD.png")));
            jLabel16MouseClickedReady = true;
            jLabel21.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
            jLabel21MouseClickedReady = true;
        }

        if (!jLabel9MouseClickedReady) {
            jLabel9.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3.png")));
            jLabel9MouseClickedReady = true;
            jLabel21.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
            jLabel21MouseClickedReady = true;
        }

        if (!jLabel10MouseClickedReady) {
            jLabel10.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3_inverted.png")));
            jLabel10MouseClickedReady = true;
            jLabel21.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
            jLabel21MouseClickedReady = true;
        }

        if (!jLabel21MouseClickedReady) {
            jLabel21.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
            jLabel21MouseClickedReady = true;
        }

        disableMessageDisplay();
//
    }

    public void showMessage() {
        enableMessageDisplay();
        jLabel5.setText(Languager.getTagValue(1, "FeedbackLabel", "MovingMessage"));
        jLabel21.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
        jLabel21MouseClickedReady = false;
    }

    public void showOldValue() {
//        System.out.println("Height = "+height);
//        System.out.println("ZValue = "+machine.getZValue());
//        jLabel8.setText(String.format("%3.2f",- (Double.valueOf(machine.getZValue()) - safeDistance)));
    }

    public void showCurrentValue() {
//        System.out.println(Double.valueOf(machine.getZValue())+currentValue);
//        System.out.println(String.valueOf(height - Double.valueOf(machine.getZValue())+currentValue));
//        jLabel11.setText(String.format("%3.2f",currentValue));
    }

    private void moveToA() {
        if (!keepZ) {
            Point5d current;

            Base.writeLog("Initializing and Calibrating A", this.getClass());

            machine.getDriver().setMachineReady(false);
            machine.getDriver().setBusy(true);
            machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(2000));
            machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28", COM.BLOCK));
            machine.runCommand(new replicatorg.drivers.commands.GetPosition());
            machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));

            /**
             *  //This is important! without this loop, the following line may
             * not work properly current =
             * machine.getDriver().getCurrentPosition(false);
             */
            while (!machine.getDriver().getMachineStatus() && machine.getDriver().isBusy()) {
                try {
                    Thread.sleep(100);
                    machine.runCommand(new replicatorg.drivers.commands.ReadStatus());

                } catch (InterruptedException ex) {
                    Logger.getLogger(CalibrationWelcome.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            //This line is cruacial!!

            current = machine.getDriver().getCurrentPosition(false);

            AxisId axis = AxisId.valueOf("Z");
            Point5d a = machine.getTablePoints("A");

//            System.out.println("current:"+current);
            current.setAxis(axis, (current.axis(axis) - (safeDistance)));
            current.setX(a.x());
            current.setY(a.y());

            height = current.z();

            double acLow = machine.getAcceleration("acLow");
            double acHigh = machine.getAcceleration("acHigh");
            double spHigh = machine.getFeedrate("spHigh");
            machine.getDriver().setBusy(true);
            machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spHigh));
            machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 X" + acLow));
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
            machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 X" + acHigh));
            machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));


        } else {
            Point5d current;

            Base.writeLog("Initializing and repeating Calibrating A", this.getClass());

            machine.getDriver().setMachineReady(false);
            machine.getDriver().setBusy(true);
            machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(2000));
            machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28"));
            machine.runCommand(new replicatorg.drivers.commands.GetPosition());
            machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));


            /**
             *  //This is important! without this loop, the following line may
             * not work properly current =
             * machine.getDriver().getCurrentPosition(false);
             */
            while (!machine.getDriver().getMachineStatus() && machine.getDriver().isBusy()) {
                try {
                    Thread.sleep(100);
                    machine.runCommand(new replicatorg.drivers.commands.ReadStatus());

                } catch (InterruptedException ex) {
                    Logger.getLogger(CalibrationWelcome.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            //This line is cruacial!!

            current = machine.getDriver().getCurrentPosition(false);

            AxisId axis = AxisId.valueOf("Z");
            Point5d a = machine.getTablePoints("A");

            height = current.z();

            current.setZ(0);
            current.setX(a.x());
            current.setY(a.y());


            double acLow = machine.getAcceleration("acLow");
            double acHigh = machine.getAcceleration("acHigh");
            double spHigh = machine.getFeedrate("spHigh");

            machine.getDriver().setBusy(true);
            machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spHigh));
            machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 X" + acLow));
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
            machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 X" + acHigh));
            machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
        }

    }

    private void doCancel() {
        dispose();
        Base.bringAllWindowsToFront();
        disposeThread.stop();
        Base.maintenanceWizardOpen = false;
        Base.getMainWindow().getButtons().updatePressedStateButton("quick_guide");
        Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
        Base.getMainWindow().setEnabled(true);
        machine.runCommand(new replicatorg.drivers.commands.SetTemperature(0));
        Point5d b = machine.getTablePoints("safe");
        double acLow = machine.getAcceleration("acLow");
        double acHigh = machine.getAcceleration("acHigh");
        double spHigh = machine.getFeedrate("spHigh");

        machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 X" + acLow));
        machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(spHigh));
        machine.runCommand(new replicatorg.drivers.commands.QueuePoint(b));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M206 X" + acHigh));
        machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("G28", COM.BLOCK));
        machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));

        if (ProperDefault.get("maintenance").equals("1")) {
            ProperDefault.remove("maintenance");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(567, 501));
        setUndecorated(true);
        setResizable(false);

        jPanel3.setBackground(new java.awt.Color(248, 248, 248));

        jPanel5.setBackground(new java.awt.Color(248, 248, 248));
        jPanel5.setMinimumSize(new java.awt.Dimension(62, 26));
        jPanel5.setPreferredSize(new java.awt.Dimension(70, 30));

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_pressed_9.png"))); // NOI18N
        jLabel15.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel15MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
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

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_3.png"))); // NOI18N
        jLabel9.setText("Afinar");
        jLabel9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel9MousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel9MouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel9MouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel9)
                .addGap(0, 0, 0))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel9)
                .addGap(0, 0, 0))
        );

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_3D.png"))); // NOI18N
        jLabel12.setText("Afinar");
        jLabel12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel12MousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel12MouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel12MouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel12)
                .addGap(0, 0, 0))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel12)
                .addGap(0, 0, 0))
        );

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_3_invertedD.png"))); // NOI18N
        jLabel16.setText("Afinar");
        jLabel16.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel16.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel16MousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel16MouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel16MouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel16)
                .addGap(0, 0, 0))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel16)
                .addGap(0, 0, 0))
        );

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_3_inverted.png"))); // NOI18N
        jLabel10.setText("Afinar");
        jLabel10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel10MousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel10MouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel10MouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel10)
                .addGap(0, 0, 0))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel10)
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

        jLabel20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        jLabel20.setText("ANTERIOR");
        jLabel20.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel20.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel20MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel20MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel20MousePressed(evt);
            }
        });

        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_21.png"))); // NOI18N
        jLabel21.setText("SEGUINTE");
        jLabel21.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel21.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel21MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel21MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel21MousePressed(evt);
            }
        });

        jLabel22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        jLabel22.setText("SAIR");
        jLabel22.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel22.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel22MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel22MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel22MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel20)
                .addGap(10, 10, 10)
                .addComponent(jLabel21)
                .addGap(12, 12, 12))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(jLabel21)
                    .addComponent(jLabel22))
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

    private void jLabel9MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MouseEntered
        jLabel9.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_3.png")));
    }//GEN-LAST:event_jLabel9MouseEntered

    private void jLabel9MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MouseExited
        jLabel9.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3.png")));
    }//GEN-LAST:event_jLabel9MouseExited

    private void jLabel10MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10MouseEntered
        jLabel10.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_3_inverted.png")));
    }//GEN-LAST:event_jLabel10MouseEntered

    private void jLabel10MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10MouseExited
        jLabel10.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3_inverted.png")));
    }//GEN-LAST:event_jLabel10MouseExited

    private void jLabel22MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel22MouseEntered
        jLabel22.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_jLabel22MouseEntered

    private void jLabel22MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel22MouseExited
        jLabel22.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_jLabel22MouseExited

    private void jLabel21MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel21MouseEntered
        if (jLabel21MouseClickedReady) {
            jLabel21.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
        }
    }//GEN-LAST:event_jLabel21MouseEntered

    private void jLabel21MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel21MouseExited
        if (jLabel21MouseClickedReady) {
            jLabel21.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
        }
    }//GEN-LAST:event_jLabel21MouseExited

    private void jLabel20MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel20MouseEntered
        if (!ProperDefault.get("maintenance").equals("1")) {
            jLabel20.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
        }
    }//GEN-LAST:event_jLabel20MouseEntered

    private void jLabel20MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel20MouseExited
        if (!ProperDefault.get("maintenance").equals("1")) {
            jLabel20.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
        }
    }//GEN-LAST:event_jLabel20MouseExited

    private void jLabel9MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MousePressed
        jLabel9.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_3.png")));
        jLabel21.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
        if (!machine.getDriver().isBusy()) {

            machine.getDriver().setBusy(true);
            showMessage();
            jLabel9.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_3.png")));
            jLabel9MouseClickedReady = false;
            jLabel21MouseClickedReady = false;

            Point5d current = machine.getDriver().getCurrentPosition(false);
            AxisId axis = AxisId.valueOf("Z");
            Base.writeLog("Calibrating table in negative axis", this.getClass());
            current.setAxis(axis, (current.axis(axis) + (-0.05)));

            currentValue += -0.05;

            machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
            machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(2000));
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
            machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
        }
    }//GEN-LAST:event_jLabel9MousePressed

    private void jLabel10MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10MousePressed
        jLabel10.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_3_inverted.png")));
        jLabel21.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));

        if (!machine.getDriver().isBusy()) {

            machine.getDriver().setBusy(true);
            showMessage();

            jLabel10MouseClickedReady = false;
            jLabel21MouseClickedReady = false;

            Point5d current = machine.getDriver().getCurrentPosition(false);
            AxisId axis = AxisId.valueOf("Z");
            Base.writeLog("Calibrating table in positive axis", this.getClass());
            current.setAxis(axis, (current.axis(axis) + (0.05)));

            currentValue += 0.05;

            machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
            machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(2000));
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
            machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));

        }
    }//GEN-LAST:event_jLabel10MousePressed

    private void jLabel12MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel12MouseEntered
        jLabel12.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_3D.png")));
    }//GEN-LAST:event_jLabel12MouseEntered

    private void jLabel12MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel12MouseExited
        jLabel12.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3D.png")));
    }//GEN-LAST:event_jLabel12MouseExited

    private void jLabel12MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel12MousePressed
        jLabel12.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_3D.png")));
        jLabel21.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
        if (!machine.getDriver().isBusy()) {

            machine.getDriver().setBusy(true);
            showMessage();
            jLabel12MouseClickedReady = false;
            jLabel21MouseClickedReady = false;

            Point5d current = machine.getDriver().getCurrentPosition(false);
            AxisId axis = AxisId.valueOf("Z");
            Base.writeLog("Calibrating table in negative axis", this.getClass());
            current.setAxis(axis, (current.axis(axis) + (-0.5)));

            currentValue += -0.5;

            machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
            machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(2000));
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
            machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));
        }
    }//GEN-LAST:event_jLabel12MousePressed

    private void jLabel16MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel16MouseEntered
        jLabel16.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_3_invertedD.png")));
    }//GEN-LAST:event_jLabel16MouseEntered

    private void jLabel16MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel16MouseExited
        jLabel16.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_3_invertedD.png")));
    }//GEN-LAST:event_jLabel16MouseExited

    private void jLabel16MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel16MousePressed
        jLabel16.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_3_invertedD.png")));
        jLabel21.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_21.png")));
        if (!machine.getDriver().isBusy()) {

            machine.getDriver().setBusy(true);
            showMessage();

            jLabel16MouseClickedReady = false;
            jLabel21MouseClickedReady = false;

            Point5d current = machine.getDriver().getCurrentPosition(false);
            AxisId axis = AxisId.valueOf("Z");
            Base.writeLog("Calibrating table in positive axis", this.getClass());
            current.setAxis(axis, (current.axis(axis) + (0.5)));

            currentValue += 0.5;

            machine.runCommand(new replicatorg.drivers.commands.SetBusy(true));
            machine.runCommand(new replicatorg.drivers.commands.SetFeedrate(2000));
            machine.runCommand(new replicatorg.drivers.commands.QueuePoint(current));
            machine.runCommand(new replicatorg.drivers.commands.SetBusy(false));

        }
    }//GEN-LAST:event_jLabel16MousePressed

    private void jLabel21MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel21MousePressed
        if (jLabel21MouseClickedReady) {
            dispose();
            disposeThread.stop();
            CalibrationSkrew1 p = new CalibrationSkrew1();
            p.setVisible(true);
        }
    }//GEN-LAST:event_jLabel21MousePressed

    private void jLabel20MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel20MousePressed
        if (!ProperDefault.get("maintenance").equals("1")) {
            dispose();
            disposeThread.stop();
            NozzleClean p = new NozzleClean();
            p.setVisible(true);
        }
    }//GEN-LAST:event_jLabel20MousePressed

    private void jLabel22MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel22MousePressed
        doCancel();
    }//GEN-LAST:event_jLabel22MousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        doCancel();
    }//GEN-LAST:event_jLabel15MousePressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel9;
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

        while (true) {
            calibrationPanel.showOldValue();
            calibrationPanel.showCurrentValue();

            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(DisposeFeedbackThread2.class.getName()).log(Level.SEVERE, null, ex);
            }

            machine.runCommand(new replicatorg.drivers.commands.ReadStatus());

            if (!machine.getDriver().getMachineStatus()) {
                calibrationPanel.showMessage();
                machine.runCommand(new replicatorg.drivers.commands.ReadStatus());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DisposeFeedbackThread2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (machine.getDriver().getMachineStatus()
                    && !machine.getDriver().isBusy()) {
                calibrationPanel.resetFeedbackComponents();
            }

        }
    }
}