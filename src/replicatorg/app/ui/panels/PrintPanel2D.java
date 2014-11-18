package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import static java.awt.Frame.ICONIFIED;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.app.util.ExtensionFilter;

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
public class PrintPanel2D extends javax.swing.JFrame {

    private static String imageToPrint = null;
    private static final String translator_PATH = Base.getApplicationDirectory().getAbsolutePath().concat("/translator/pngTranslator23D.py");
    private static final String BIN_PATH_UNIX = "python";
    private static final String BIN_PATH_WINDOWS = Base.getApplicationDirectory().getAbsolutePath().concat("\\translator\\pngTranslator23D.py");
    private static final String PROCESS_PARSING = "Parsing";
    private static final String PROCESS_PROCESSING = "Processing";
    private static final String PROCESS_COMPLETED = "Process completed";
    private static final String ERROR = "error";
    
    public PrintPanel2D() {
        initComponents();
        setTextLanguage();
        setFont();
        centerOnScreen();
        evaluateInitialConditions();
    }

    private void setFont() {
        lTitle.setFont(GraphicDesignComponents.getSSProBold("14"));
        bCancel.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bOk.setFont(GraphicDesignComponents.getSSProRegular("12"));
        b_chooseGCode.setFont(GraphicDesignComponents.getSSProRegular("12"));
        b_generateSTL.setFont(GraphicDesignComponents.getSSProRegular("12"));
        lTypeImage.setFont(GraphicDesignComponents.getSSProRegular("12"));
        b_logo.setFont(GraphicDesignComponents.getSSProRegular("12"));
        b_portrait.setFont(GraphicDesignComponents.getSSProRegular("12"));
    }

    private void setTextLanguage() {
        lTitle.setText(Languager.getTagValue(1, "Print", "Print2D_Title"));
        lTypeImage.setText(Languager.getTagValue(1, "Print", "Print_TypeImage"));
        bCancel.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line3"));
        bOk.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line10"));
        b_chooseGCode.setText(Languager.getTagValue(1, "Print", "Print_SelectImage"));
        b_generateSTL.setText(Languager.getTagValue(1, "Print", "Print2D_GenerateSTL"));
        b_logo.setText(Languager.getTagValue(1, "Print", "Print_LogoType"));
        b_portrait.setText(Languager.getTagValue(1, "Print", "Print_PortraitType"));
    }

    private void evaluateInitialConditions(){
        l_GCodePath.setText("");
        buttonGroup2.add(b_logo);
        buttonGroup2.add(b_portrait);
        jPanel2.setVisible(false);
        loading.setVisible(false);
        jProgressBar1.setVisible(false);
        jProgressBar1.setForeground(new Color(255, 203, 5));
    }
    
    private void centerOnScreen() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        // Determine the new location of the window
        int w = this.getSize().width;
        int h = this.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;

        // Move the window
        this.setLocationRelativeTo(null);
        this.setLocationRelativeTo(Base.getMainWindow());
        Base.setMainWindowNOK();
    }

    private void doCancel() {
        dispose();
        Base.bringAllWindowsToFront();
        Base.getMainWindow().setEnabled(true);
    }
    
    /** Stores gcode path of the file to be printed
     *
     * @param gcodeFilePath path for the gcode to be printed
     */
    public void set2DImagePath(String imageFilePath) {

        if (imageFilePath != null) {
            this.imageToPrint = imageFilePath;
            Base.isPrintingFromGCode = true;
        } else {
            this.imageToPrint = null;
            Base.isPrintingFromGCode = false;
        }
    }
    
    /**
     * Select GCode file to open.
     *
     * @return path to file
     */
    private File selectFile() {
        File directory = null;
        String loadDir = ProperDefault.get("ui.open_dir0");

        if (loadDir != null) {
            directory = new File(loadDir);
        }
        JFileChooser fc = new JFileChooser(directory);
        FileFilter defaultFilter;


        String[] extensions = {".png"};
        fc.addChoosableFileFilter(defaultFilter = new ExtensionFilter(extensions, "PNG files"));
        fc.addChoosableFileFilter(new ExtensionFilter(".png", "PNG files"));
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(defaultFilter);
        fc.setDialogTitle("Open a image file...");
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setFileHidingEnabled(false);
        int rv = fc.showOpenDialog(this);
        if (rv == JFileChooser.APPROVE_OPTION) {
            fc.getSelectedFile().getName();
            ProperDefault.put("ui.open_dir0", fc.getCurrentDirectory().getAbsolutePath());
            Base.writeLog("Image File selected " + fc.getSelectedFile().getAbsolutePath());
            return fc.getSelectedFile();
        } else {
            return null;
        }

    }   
    
    /**
     * Opens new GCode file to be printed
     */
    public void handleNewModel() {
        Base.writeLog("Opening model ...");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String path = null;
                if (path == null) { // "open..." selected from the menu
                    File file = selectFile();
                    set2DImagePath(file.getAbsolutePath());
                    l_GCodePath.setText("<html><b>" + Languager.getTagValue(1, "Print", "Print_GCode_Selected") + "</b>" + file.getName() + "</html>");
                    BufferedImage bimg = null;
                    Image img = null;
                    try {
                        bimg = ImageIO.read(file);
                    } catch (IOException ex) {
                        Base.writeLog("Error loading 2D image" + imageToPrint + " .../n"+ex.getMessage());
                    }
                    img = bimg.getScaledInstance(l_2DImage.getWidth(), l_2DImage.getHeight(),Image.SCALE_SMOOTH);
                    ImageIcon icon = new ImageIcon(img);
                    l_2DImage.setIcon(icon);
                    jPanel2.setVisible(true);
                }
                Base.logger.info("Loading 2D image" + imageToPrint + " ...");
                Base.writeLog("Loading 2D image" + imageToPrint + " ...");
            }
        });
    }
    
    /**
     * Return type of conversion to be done.
     * @return value indicating which button is selected.
     */
    public String parseTypeConversion(){
        if(b_logo.isSelected()){
            return "1";
        } else {
            return "0";
        }
    }

    /**
     * Generates STL from PNG image.
     * Uses internal var.
     * @return 
     */
    public String generateSTL() {

        //Validates if estimator dir exists
        if (new File(translator_PATH).exists()) {
            String[] command = new String[4];

            if (Base.isMacOS() || Base.isLinux()) {
                command[0] = BIN_PATH_UNIX;
                command[1] = translator_PATH;
                command[2] = imageToPrint;
                command[3] = parseTypeConversion();
            } else {
                command[0] = BIN_PATH_WINDOWS;
                command[1] = translator_PATH;
                command[2] = imageToPrint;
                command[3] = parseTypeConversion();
            }

            System.out.println(command[0]+" "+command[1]+" "+command[2]+" "+command[3]);
            ProcessBuilder probuilder = new ProcessBuilder(command);
            Process process = null;
            
            try {
                process = probuilder.start();
            } catch (IOException ex) {
                Base.writeLog("Error starting process to generate STL");
                return "Error";
            }

            //Read out dir output
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String output = null;
            try {
                while ((output = br.readLine()) != null) {
                    System.out.println(output+"\n");
                    if(output.contains(PROCESS_PARSING)) {
                        jProgressBar1.setValue(34);
                    }
                    if(output.contains(PROCESS_PROCESSING)) {
                        jProgressBar1.setValue(67);
                    }
                    if(output.contains(PROCESS_COMPLETED)) {
                        jProgressBar1.setValue(100);
                    }
                    if(output.toLowerCase().contains(ERROR)) {
                        jProgressBar1.setVisible(false);
                        throw new IOException();
                    }
                }
            } catch (IOException ex) {
                Base.writeLog("Error reading gcode estimater output");
            }

            //Wait to get exit value
            try {
                int exitValue = process.waitFor();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            Base.writeLog("Error starting process to estimate print duration");
            return "Error";
        }

        return "";
    }
    
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup2 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        lTitle = new javax.swing.JLabel();
        b_chooseGCode = new javax.swing.JLabel();
        l_GCodePath = new javax.swing.JLabel();
        l_2DImage = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        b_logo = new javax.swing.JRadioButton();
        lTypeImage = new javax.swing.JLabel();
        b_portrait = new javax.swing.JRadioButton();
        b_generateSTL = new javax.swing.JLabel();
        loading = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jPanel3 = new javax.swing.JPanel();
        bOk = new javax.swing.JLabel();
        bCancel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));

        jPanel4.setBackground(new java.awt.Color(255, 203, 5));
        jPanel4.setMinimumSize(new java.awt.Dimension(62, 26));
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

        lTitle.setText("Please select a PNG image you want to print");

        b_chooseGCode.setForeground(new java.awt.Color(0, 0, 0));
        b_chooseGCode.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_16.png"))); // NOI18N
        b_chooseGCode.setText("Choose GCode to Print");
        b_chooseGCode.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        b_chooseGCode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b_chooseGCodeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                b_chooseGCodeMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                b_chooseGCodeMousePressed(evt);
            }
        });

        l_GCodePath.setText("jLabel19");

        jPanel2.setBackground(new java.awt.Color(248, 248, 248));

        b_logo.setBackground(new java.awt.Color(248, 248, 248));
        b_logo.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        b_logo.setText("Logo");
        b_logo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_logoActionPerformed(evt);
            }
        });

        lTypeImage.setText("Choose type of image");

        b_portrait.setBackground(new java.awt.Color(248, 248, 248));
        b_portrait.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        b_portrait.setSelected(true);
        b_portrait.setText("Portrait");
        b_portrait.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_portraitActionPerformed(evt);
            }
        });

        b_generateSTL.setForeground(new java.awt.Color(0, 0, 0));
        b_generateSTL.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_16.png"))); // NOI18N
        b_generateSTL.setText("Generate STL");
        b_generateSTL.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        b_generateSTL.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b_generateSTLMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                b_generateSTLMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                b_generateSTLMousePressed(evt);
            }
        });

        loading.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/loading.gif"))); // NOI18N

        jProgressBar1.setBackground(new java.awt.Color(186, 186, 186));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lTypeImage)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(b_logo)
                        .addGap(18, 18, 18)
                        .addComponent(b_portrait))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(b_generateSTL)
                        .addGap(18, 18, 18)
                        .addComponent(loading)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(lTypeImage)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(b_logo)
                    .addComponent(b_portrait))
                .addGap(50, 50, 50)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(loading, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(b_generateSTL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(73, 73, 73)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 107, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lTitle)
                                .addGap(0, 207, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(b_chooseGCode)
                                .addGap(18, 18, 18)
                                .addComponent(l_GCodePath, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(117, 117, 117))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(l_2DImage, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lTitle)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(b_chooseGCode)
                    .addComponent(l_GCodePath))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(l_2DImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(255, 203, 5));
        jPanel3.setMinimumSize(new java.awt.Dimension(20, 46));
        jPanel3.setPreferredSize(new java.awt.Dimension(423, 127));

        bOk.setForeground(new java.awt.Color(0, 0, 0));
        bOk.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_21.png"))); // NOI18N
        bOk.setText("Print");
        bOk.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bOk.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bOkMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bOkMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bOkMousePressed(evt);
            }
        });

        bCancel.setForeground(new java.awt.Color(0, 0, 0));
        bCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bCancel.setText("Cancel");
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

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bOk)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bOk)
                    .addComponent(bCancel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 638, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bOkMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bOkMouseEntered
        
    }//GEN-LAST:event_bOkMouseEntered

    private void bOkMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bOkMouseExited
     
    }//GEN-LAST:event_bOkMouseExited

    private void bOkMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bOkMousePressed

       
    }//GEN-LAST:event_bOkMousePressed

    private void bCancelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseEntered
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bCancelMouseEntered

    private void bCancelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseExited
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bCancelMouseExited

    private void bCancelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMousePressed
        doCancel();   
    }//GEN-LAST:event_bCancelMousePressed

    private void jLabel13MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel13MousePressed
        setState(ICONIFIED);
        Base.getMainWindow().deactivateCameraControls();
    }//GEN-LAST:event_jLabel13MousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        doCancel();
    }//GEN-LAST:event_jLabel15MousePressed

    private void b_chooseGCodeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_b_chooseGCodeMouseEntered
        b_chooseGCode.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_16.png")));
    }//GEN-LAST:event_b_chooseGCodeMouseEntered

    private void b_chooseGCodeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_b_chooseGCodeMouseExited
        b_chooseGCode.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_16.png")));
    }//GEN-LAST:event_b_chooseGCodeMouseExited

    private void b_chooseGCodeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_b_chooseGCodeMousePressed
        handleNewModel();
    }//GEN-LAST:event_b_chooseGCodeMousePressed

    private void b_logoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_logoActionPerformed
        b_logo.setSelected(true);
    }//GEN-LAST:event_b_logoActionPerformed

    private void b_portraitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_portraitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_b_portraitActionPerformed

    private void b_generateSTLMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_b_generateSTLMouseEntered
         b_generateSTL.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_16.png")));
    }//GEN-LAST:event_b_generateSTLMouseEntered

    private void b_generateSTLMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_b_generateSTLMouseExited
         b_generateSTL.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_16.png")));
    }//GEN-LAST:event_b_generateSTLMouseExited

    private void b_generateSTLMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_b_generateSTLMousePressed
        b_generateSTL.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_pressed_16.png")));
        loading.setVisible(true);
        jProgressBar1.setVisible(true);
        
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                generateSTL();
                b_generateSTL.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_16.png")));
                loading.setVisible(false);
                jProgressBar1.setVisible(false);
            }
        });
        System.out.println("here");
    }//GEN-LAST:event_b_generateSTLMousePressed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bCancel;
    private javax.swing.JLabel bOk;
    private javax.swing.JLabel b_chooseGCode;
    private javax.swing.JLabel b_generateSTL;
    private javax.swing.JRadioButton b_logo;
    private javax.swing.JRadioButton b_portrait;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JLabel lTitle;
    private javax.swing.JLabel lTypeImage;
    private javax.swing.JLabel l_2DImage;
    private javax.swing.JLabel l_GCodePath;
    private javax.swing.JLabel loading;
    // End of variables declaration//GEN-END:variables

}
