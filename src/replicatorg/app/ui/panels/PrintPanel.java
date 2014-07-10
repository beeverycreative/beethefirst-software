package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import static java.awt.Frame.ICONIFIED;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
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
public class PrintPanel extends javax.swing.JFrame {

    private JLabel quality_prototype;
    private JLabel quality_normal;
    private JLabel quality_artwork;
    private JLabel quality_low;
    private JLabel quality_medium;
    private JLabel quality_solid;
    private ArrayList<String> prefs;
    private boolean raftPressed, supportPressed,autonomousPressed;
    private int posX = 0, posY = 0;
    private Driver driver = Base.getMainWindow().getMachineInterface().getDriver();
    boolean no_Filament = false;
    private static final String NOK = "NOK";
    private Thread t = null;
    private String colorCode = "A0";

    public PrintPanel() {
        initComponents();
        initSlidersLables();
        setFont();
        getCoilCode();
        setTextLanguage();
        initSliderConfigs();
        centerOnScreen();
        prefs = new ArrayList<String>();
        raftPressed = false;
        supportPressed = false;
        autonomousPressed = false;
        evaluateConditions();
        matchChanges();
        enableDrag();
        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());
    }

    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProRegular("14"));
        jLabel2.setFont(GraphicDesignComponents.getSSProBold("14"));
        jLabel3.setFont(GraphicDesignComponents.getSSProBold("14"));
        jLabel5.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel7.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel8.setFont(GraphicDesignComponents.getSSProRegular("10"));
        jLabel9.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel10.setFont(GraphicDesignComponents.getSSProRegular("10"));
        jLabel11.setFont(GraphicDesignComponents.getSSProRegular("10"));
        bCancel.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel12.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel16.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel19.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel20.setFont(GraphicDesignComponents.getSSProRegular("10"));
        jLabel22.setFont(GraphicDesignComponents.getSSProRegular("10"));
        jLabel23.setFont(GraphicDesignComponents.getSSProRegular("10"));

        quality_prototype.setFont(GraphicDesignComponents.getSSProRegular("12"));
        quality_normal.setFont(GraphicDesignComponents.getSSProRegular("12"));
        quality_artwork.setFont(GraphicDesignComponents.getSSProRegular("12"));
        quality_low.setFont(GraphicDesignComponents.getSSProRegular("12"));
        quality_medium.setFont(GraphicDesignComponents.getSSProRegular("12"));
        quality_solid.setFont(GraphicDesignComponents.getSSProRegular("12"));
    }

    private void getCoilCode() {

        String code = Base.getMainWindow().getMachine().getModel().getCoilCode();

        try {
            ;//do nothing
            //driver.updateCoilCode();            
        } catch (Exception e) {
            Base.writeLog("driver.update coil code failed: " + e.getMessage());
        }


        if (code.equals("A0") || code.equals("NOK")) {
            no_Filament = true;
            jLabel22.setFont(GraphicDesignComponents.getSSProBold("10"));
            code = Languager.getTagValue("Print", "Print_Splash_Info9").toUpperCase();

            jLabel22.setText(" " + code);
            jLabel23.setText(Languager.getTagValue("Print", "Print_Splash_Info11"));

        } else {
            colorCode = code;
            code = getColor(Base.getMainWindow().getMachine().getModel().getCoilCode());

            jLabel22.setText(" " + code);
        }


        if (no_Filament == false) {
            jLabel22.setText(" " + code);
            jLabel23.setText(Languager.getTagValue("Print", "Print_Splash_Info11"));
        } else {
            jLabel22.setText(" " + code);
        }

    }

    private void setTextLanguage() {
        jLabel1.setText(Languager.getTagValue("MainWindowButtons", "Print"));
        jLabel2.setText(Languager.getTagValue("Print", "Print_Quality"));
        jLabel3.setText(Languager.getTagValue("Print", "Print_Density"));
        jLabel7.setText(Languager.getTagValue("Print", "Print_Raft"));
        jLabel8.setText(splitString(Languager.getTagValue("Print", "Print_Raft_Info")));
        jLabel9.setText(Languager.getTagValue("Print", "Print_Support"));
        jLabel10.setText(splitString(Languager.getTagValue("Print", "Print_Support_Info")));
        jLabel11.setText(splitString(Languager.getTagValue("Print", "Print_Autonomy_Info")));
        bCancel.setText(Languager.getTagValue("OptionPaneButtons", "Line3"));
        jLabel12.setText(Languager.getTagValue("ToolPath", "Line21"));
        jLabel16.setText(Languager.getTagValue("Print", "Print_Autonomy"));
        jLabel19.setText(Languager.getTagValue("MaintenancePanel", "FilamentChange_button"));
        jLabel20.setText(Languager.getTagValue("MaintenancePanel", "Filament_Type"));
        jLabel23.setText("");


        quality_prototype.setText(Languager.getTagValue("Print", "Print_Quality_Low"));
        quality_normal.setText(Languager.getTagValue("Print", "Print_Quality_Normal"));
        quality_artwork.setText(Languager.getTagValue("Print", "Print_Quality_High"));
        quality_low.setText(Languager.getTagValue("Print", "Print_Density_Low"));
        quality_medium.setText(Languager.getTagValue("Print", "Print_Density_Medium"));
        quality_solid.setText(Languager.getTagValue("Print", "Print_Density_High"));

    }

    private void centerOnScreen() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        // Determine the new location of the window
        int w = this.getSize().width;
        int h = this.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;

        // Move the window
        this.setLocation(x, y);
        this.setLocationRelativeTo(Base.getMainWindow());
        Base.setMainWindowNOK();
    }

    private String splitString(String s) {
        int width = 332;
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
                text = text.concat(parts[i]); //.concat(".");
            }
        }

        return ihtml.concat(text).concat(".").concat(ehtml);
    }

    private int getStringPixelsWidth(String s) {
        Graphics g = getGraphics();
        FontMetrics fm = g.getFontMetrics(GraphicDesignComponents.getSSProRegular("10"));
        return fm.stringWidth(s);
    }

    private void initSliderConfigs() {
        Hashtable labelTable1 = new Hashtable();
        labelTable1.put(new Integer(0), quality_prototype);
//        labelTable1.put( new Integer( 50), quality_normal );
        labelTable1.put(new Integer(100), quality_artwork);
        jSlider1.setLabelTable(labelTable1);

        Hashtable labelTable2 = new Hashtable();
        labelTable2.put(new Integer(0), quality_low);
        labelTable2.put(new Integer(50), quality_medium);
        labelTable2.put(new Integer(100), quality_solid);
        jSlider2.setLabelTable(labelTable2);
    }

    private void initSlidersLables() {
        quality_prototype = new JLabel("prototype");
        quality_normal = new JLabel("normal");
        quality_artwork = new JLabel("artwork");
        quality_low = new JLabel("light");
        quality_medium = new JLabel("medium");
        quality_solid = new JLabel("solid");
    }

    private String parseSlider1() {
        if (jSlider1.getValue() == 0) {
            return "LOW";
        }
        if (jSlider1.getValue() == 50) {
            return "MEDIUM";
        }
        if (jSlider1.getValue() == 100) {
            return "HIGH";
        }

        return "LOW";
    }

    private String parseSlider2() {
        if (jSlider2.getValue() == 0) {
            return "LOW";
        }
        if (jSlider2.getValue() == 50) {
            return "MEDIUM";
        }
        if (jSlider2.getValue() == 100) {
            return "HIGH";
        }

        return "MEDIUM";
    }

    private void evaluateConditions() {
        
        //Force GCode generation
//        Base.getMainWindow().getBed().setGcodeOK(false);

        //|| gramsCalculator(Double.valueOf(ProperDefault.get("filamentCoilRemaining"))) < 100
        if (no_Filament) // Less than a meter(100 grams)
        {
            //jLabel12.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_15.png")));
            jLabel22.setForeground(Color.red);
            jLabel23.setForeground(Color.red);
        }

    }

    private double gramsCalculator(double meters) {
        return meters * 12 / 4;
    }

    private String parseCoilCode() {
        String colorRatio = "1.1164";
        String code = ProperDefault.get("coilCode");

        if (code.contains("301")) {
            colorRatio = "1.1164";
        }
        if (code.contains("302")) {
            colorRatio = "1.0797";
        }
        if (code.contains("303")) {
            colorRatio = "1.2100";
        }
        if (code.contains("304")) {
            colorRatio = "1.0996";
        }
        if (code.contains("305")) {
            colorRatio = "1.1940";
        }
        if (code.contains("306")) {
            colorRatio = "1.2438";
        }
        if (code.contains("321")) {
            colorRatio = "1.2600";
        }
        if (code.contains("322")) {
            colorRatio = "1.1592";
        }

        return colorRatio;
    }

    private String getColor(String coilCode) {
        String color = "NO_FILAMENT";

        if (coilCode.contains("301")) {
            color = "WHITE";
        }
        if (coilCode.contains("302")) {
            color = "BLACK";
        }
        if (coilCode.contains("303")) {
            color = "YELLOW";
        }
        if (coilCode.contains("304")) {
            color = "RED";
        }
        if (coilCode.contains("305")) {
            color = "TURQUOISE";
        }
        if (coilCode.contains("306")) {
            color = "TRANSPARENT";
        }
        if (coilCode.contains("321")) {
            color = "GREEN";
        }
        if (coilCode.contains("322")) {
            color = "ORANGE";
        }

        return color;
    }

    private void enableDrag() {
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                posX = e.getX();
                posY = e.getY();
            }
        });


        this.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent evt) {
                //sets frame position when mouse dragged			
                setLocation(evt.getXOnScreen() - posX, evt.getYOnScreen() - posY);
            }
        });
    }

    private void matchChanges() {

        boolean lastRaft = Base.getMainWindow().getBed().isLasRaft();
        String lastDensity = Base.getMainWindow().getBed().getLastDensity();
        String lastResolution = Base.getMainWindow().getBed().getLastResolution();
        boolean lastSupport = Base.getMainWindow().getBed().isLastSupport();
        boolean lastAutonomous = Base.getMainWindow().getBed().isLastAutonomous();

        if (lastRaft) {
            jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_checked.png")));
            raftPressed = true;
        } else {
            jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_unchecked.png")));
            raftPressed = false;
        }

        if (lastSupport) {
            jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_checked.png")));
            supportPressed = true;
        } else {
            jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_unchecked.png")));
            supportPressed = false;
        }
        
        if (lastAutonomous) {
            jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_checked.png")));
            autonomousPressed = true;
        } else {
            jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_unchecked.png")));
            autonomousPressed = false;
        }        
        
        if (lastDensity.equalsIgnoreCase("LOW")) {
            jSlider2.setValue(0);
        } else if (lastDensity.equalsIgnoreCase("MEDIUM")) {
            jSlider2.setValue(50);
        } else {
            jSlider2.setValue(100);
        }

        if (lastResolution.equalsIgnoreCase("LOW")) {
            jSlider1.setValue(0);
        } else {
            jSlider1.setValue(100);
        }
        
        checkChanges();
    }

    private boolean checkChanges() {
        boolean equal = false;

        boolean lastRaft = Base.getMainWindow().getBed().isLasRaft();
        String lastDensity = Base.getMainWindow().getBed().getLastDensity();
        String lastResolution = Base.getMainWindow().getBed().getLastResolution();
        boolean lastSupport = Base.getMainWindow().getBed().isLastSupport();
        boolean lastAutonomous = Base.getMainWindow().getBed().isLastAutonomous();
        boolean gcodeOK = Base.getMainWindow().getBed().isGcodeOK();

        if (parseSlider1().equals(lastResolution)
                && parseSlider2().equals(lastDensity)
                && raftPressed == lastRaft
                && supportPressed == lastSupport
                && autonomousPressed == lastAutonomous
                && gcodeOK) {
            equal = true;
        }
//        
//        if(!equal)
//        {
//        jLabel6.setVisible(true);
//        jLabel16.setVisible(true);
//        }
//        else
//        {
//            jLabel6.setVisible(false);
//            jLabel16.setVisible(false);
//        }


        return equal;
    }

    private void doCancel() {
        dispose();
        Base.getMainWindow().getButtons().updatePressedStateButton("print");
        Base.bringAllWindowsToFront();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSlider1 = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        jSlider2 = new javax.swing.JSlider();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        bCancel = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setPreferredSize(new java.awt.Dimension(375, 510));
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));
        jPanel1.setPreferredSize(new java.awt.Dimension(374, 435));

        jPanel4.setBackground(new java.awt.Color(255, 203, 5));
        jPanel4.setMinimumSize(new java.awt.Dimension(62, 26));
        jPanel4.setPreferredSize(new java.awt.Dimension(70, 30));
        jPanel4.setRequestFocusEnabled(false);

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_11.png"))); // NOI18N
        jLabel13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel13MousePressed(evt);
            }
        });

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_10.png"))); // NOI18N

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_9.png"))); // NOI18N
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
                .addGap(12, 12, 12)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 7, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, 13, Short.MAX_VALUE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("IMPRIMIR");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("Qualidade");

        jSlider1.setBackground(new java.awt.Color(248, 248, 248));
        jSlider1.setForeground(new java.awt.Color(0, 0, 0));
        jSlider1.setPaintLabels(true);
        jSlider1.setSnapToTicks(true);
        jSlider1.setValue(0);
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });

        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("Densidade");

        jSlider2.setBackground(new java.awt.Color(248, 248, 248));
        jSlider2.setForeground(new java.awt.Color(0, 0, 0));
        jSlider2.setPaintLabels(true);
        jSlider2.setSnapToTicks(true);
        jSlider2.setValue(0);
        jSlider2.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlider2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider2StateChanged(evt);
            }
        });

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/c_unchecked.png"))); // NOI18N
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel4MousePressed(evt);
            }
        });

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/c_unchecked.png"))); // NOI18N
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel5MousePressed(evt);
            }
        });

        jLabel7.setForeground(new java.awt.Color(0, 0, 0));
        jLabel7.setText("Raft");

        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setText("Suspendisse potenti.");

        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setText("Support");

        jLabel10.setForeground(new java.awt.Color(0, 0, 0));
        jLabel10.setText("Suspendisse potenti.");

        jLabel19.setForeground(new java.awt.Color(0, 0, 0));
        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_16.png"))); // NOI18N
        jLabel19.setText("Mudar filamento agora");
        jLabel19.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel19.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel19MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel19MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel19MousePressed(evt);
            }
        });

        jLabel20.setForeground(new java.awt.Color(0, 0, 0));
        jLabel20.setText("Filament Type");

        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/arrow_ajuda_2.png"))); // NOI18N

        jLabel22.setForeground(new java.awt.Color(0, 0, 0));
        jLabel22.setText("NO_FILAMENT");

        jLabel23.setForeground(new java.awt.Color(0, 0, 0));
        jLabel23.setText("a");

        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setText("Suspendisse potenti.");

        jLabel16.setForeground(new java.awt.Color(0, 0, 0));
        jLabel16.setText("Autonomous");

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/c_unchecked.png"))); // NOI18N
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel6MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(108, 108, 108)
                        .addComponent(jLabel19))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16)
                            .addComponent(jLabel11))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel7))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(4, 4, 4)
                                        .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel10)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel20)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel23)
                                            .addComponent(jLabel22))))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addGap(9, 9, 9)
                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel23)
                .addGap(3, 3, 3)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jLabel19)
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(255, 203, 5));
        jPanel2.setMinimumSize(new java.awt.Dimension(20, 26));
        jPanel2.setPreferredSize(new java.awt.Dimension(20, 26));

        bCancel.setForeground(new java.awt.Color(0, 0, 0));
        bCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bCancel.setText("CANCELAR");
        bCancel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
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

        jLabel12.setForeground(new java.awt.Color(0, 0, 0));
        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_15.png"))); // NOI18N
        jLabel12.setText("IMPRIMIR");
        jLabel12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel12MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel12MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel12MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(214, Short.MAX_VALUE)
                .addComponent(bCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel12)
                .addGap(12, 12, 12))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bCancel)
                    .addComponent(jLabel12)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 484, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged

        if (jSlider1.getValue() > 5) {
            jSlider1.setValue(100);
        } else {
            jSlider1.setValue(0);
        }

        checkChanges();


    }//GEN-LAST:event_jSlider1StateChanged

    private void jSlider2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider2StateChanged
        if (jSlider2.getValue() > 1 && jSlider2.getValue() < 49) {
            jSlider2.setValue(50);
        }
        if (jSlider2.getValue() > 51) {
            jSlider2.setValue(100);
        }

        checkChanges();
    }//GEN-LAST:event_jSlider2StateChanged

    private void jLabel19MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel19MouseEntered
        jLabel19.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_16.png")));
    }//GEN-LAST:event_jLabel19MouseEntered

    private void jLabel19MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel19MouseExited
        jLabel19.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_16.png")));
    }//GEN-LAST:event_jLabel19MouseExited

    private void jLabel12MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel12MouseEntered
        if (!no_Filament) {
            jLabel12.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_15.png")));
        }
    }//GEN-LAST:event_jLabel12MouseEntered

    private void jLabel12MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel12MouseExited
        if (!no_Filament) {
            jLabel12.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_15.png")));
        }
    }//GEN-LAST:event_jLabel12MouseExited

    private void bCancelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseEntered
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bCancelMouseEntered

    private void bCancelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseExited
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bCancelMouseExited

    private void jLabel12MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel12MousePressed

        if (t != null) {
            t.stop();
        }

        if (!no_Filament) {
            /*
             * prefs[0] - profile (LOW,HIGH) 
             * prefs[1] - colorRation
             * prefs[2] - infill LOW,MEDIUM,HIGH) 
             * prefs[3] - RAFT (T/F)
             * prefs[4] - SUPPORT (T/F)
             */

            if (!checkChanges()) {
                Base.getMainWindow().getBed().setGcodeOK(false);
            }

            prefs.add(parseSlider1());
//            prefs.add(parseCoilCode());
            prefs.add(getColor(colorCode));
            prefs.add(parseSlider2());
            prefs.add(String.valueOf(raftPressed));
            prefs.add(String.valueOf(supportPressed));
            prefs.add(String.valueOf(autonomousPressed));

            Base.getMainWindow().getBed().setLasRaft(raftPressed);
            Base.getMainWindow().getBed().setLastDensity(parseSlider2());
            Base.getMainWindow().getBed().setLastResolution(parseSlider1());
            Base.getMainWindow().getBed().setLastSupport(supportPressed);
            Base.getMainWindow().getBed().setLastAutonomous(autonomousPressed);

            Base.getMainWindow().setEnabled(false);
            Base.cleanDirectoryTempFiles(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER);
            
            dispose();
            Base.getMainWindow().getCanvas().unPickAll();
      
            if (autonomousPressed == false) {
                final PrintSplashSimple p = new PrintSplashSimple(prefs);
                p.setVisible(true);

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {

                        if (Base.getMainWindow().getBed().isSceneDifferent()) {
                            Base.getMainWindow().handleSave(true);
                        }
                        p.startConditions();
                    }
                });
            } else {
                final PrintSplashAutonomous p = new PrintSplashAutonomous(false, prefs);
                p.setVisible(true);

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {

                        if (Base.getMainWindow().getBed().isSceneDifferent()) {
                            Base.getMainWindow().handleSave(true);
                        }
                        p.startConditions();
                    }
                });
            }

        } else {
            t = new Thread(new Runnable() {
                private int counter = 0;
                private int tries = 0;

                public void run() {
                    while (tries < 3) {
                        counter++;
//                        SwingUtilities.invokeLater(new Runnable(){
//                            public void run() {
                        if (counter % 2 == 0) {
                            jLabel22.setForeground(Color.red);
                            jLabel23.setForeground(Color.red);
                            counter = 0;
                            tries++;
                        } else {
                            jLabel22.setForeground(new Color(0, 0, 0));
                            jLabel23.setForeground(new Color(0, 0, 0));
                        }
//                            }
//                        });
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            t.start();

        }
    }//GEN-LAST:event_jLabel12MousePressed

    private void bCancelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMousePressed
        doCancel();
    }//GEN-LAST:event_bCancelMousePressed

    private void jLabel19MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel19MousePressed
        dispose();
        Base.getMainWindow().getButtons().updatePressedStateButton("print");
        Base.getMainWindow().getButtons().goFilamentChange();
        ProperDefault.put("maintenance", "1");
        FilamentHeating p = new FilamentHeating();
        p.setVisible(true);
    }//GEN-LAST:event_jLabel19MousePressed

    private void jLabel13MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel13MousePressed
        setState(ICONIFIED);
    }//GEN-LAST:event_jLabel13MousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        doCancel();
    }//GEN-LAST:event_jLabel15MousePressed

    private void jLabel6MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MousePressed
        if (!autonomousPressed) {
            jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_checked.png")));
            autonomousPressed = true;
        } else {
            jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_unchecked.png")));
            autonomousPressed = false;
        }

        checkChanges();
    }//GEN-LAST:event_jLabel6MousePressed

    private void jLabel4MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MousePressed
        if (!raftPressed) {
            jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_checked.png")));
            raftPressed = true;
        } else {
            jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_unchecked.png")));
            raftPressed = false;
        }

        checkChanges();
    }//GEN-LAST:event_jLabel4MousePressed

    private void jLabel5MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MousePressed
        if (!supportPressed) {
            jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_checked.png")));
            supportPressed = true;
        } else {
            jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "c_unchecked.png")));
            supportPressed = false;
        }

        checkChanges();
    }//GEN-LAST:event_jLabel5MousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bCancel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JSlider jSlider2;
    // End of variables declaration//GEN-END:variables
}
