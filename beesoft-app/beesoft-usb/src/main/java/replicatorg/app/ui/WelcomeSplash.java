package replicatorg.app.ui;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import static java.lang.Thread.sleep;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import replicatorg.app.Base;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.mainWindow.UpdateChecker;
import replicatorg.app.ui.panels.TourWelcome;
import replicatorg.app.ui.panels.Warning;
import replicatorg.app.ui.panels.WelcomeQuickguide;

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
public class WelcomeSplash extends javax.swing.JFrame {

    private MainWindow window;
    private WelcomeQuickguide guideWizard;
    private ImageIcon image;
    private JLabel label;
    private JProgressBar bar;
    private int newWidth = 600;
    private int newHeight = 333;
    private int duration = 80;

    /**
     * Welcome Splash init
     *
     * @param wind MainWindow for visual and feature control
     */
    public WelcomeSplash(MainWindow wind) {
        initComponents();
        Base.writeLog("Welcome Splash started ...");
        window = wind;
        // Loads Splash image
        image = new ImageIcon(Base.getImage("images/welcomeSplash.png", this));
        Image img = image.getImage();
        // Loads Splash image for dimensios getter
        BufferedImage img2 = Base.getImage("images/welcomeSplash.png", this);
        // Gets Screen Dimension
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        // Calculates ratio for modular adjustement to every screen
        double screenRatio = 0.75;
        newWidth = (int) (d.width * screenRatio);
        double scale = d.width * screenRatio / img2.getWidth();
        newHeight = (int) (img2.getHeight() * scale);

        // Scales original image to new size values with Smooth conversion
        Image newimg = img.getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_SMOOTH);
        image = new ImageIcon(newimg);
        // Sets bar preferences and size.
        // Bar width is equal to image width. Height has value = 5
        jProgressBar1.setMaximum(newWidth - 5);
        jProgressBar1.setPreferredSize(new Dimension(newWidth - 5, 5));
        jLabel1.setIcon(image);
        jLabel1.setPreferredSize(new Dimension(newWidth, newHeight));
        jLabel1.setSize(new Dimension(newWidth, newHeight));
        jPanel1.setPreferredSize(new Dimension(newWidth, newHeight));
        jPanel1.setSize(new Dimension(newWidth, newHeight));
        this.setPreferredSize(new Dimension(newWidth, newHeight + jProgressBar1.getHeight()));
        this.setSize(new Dimension(newWidth, newHeight + jProgressBar1.getHeight()));
        this.setLocationRelativeTo(null);
        // Thread to update JProgress Bar
        new Thread() {
            @Override
            public void run() {
                int i = 0;
                // Increment value for each bar update
                int inc = 0;

                // Number of seconds to hold splash screen
                while (i < getDuration()) {
                    jProgressBar1.setValue(inc);
                    i++;
                    inc += getWidth() / 80;
                    try {
                        sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(WelcomeSplash.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
                changeState();
            }
        }.start();

    }

    /**
     * Monitor changes in MainWindow and performs several operations at startup.
     */
    public void changeState() {
        window.setVisible(true);
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setVisible(false);
        Base.writeLog("BEESOFT main window loaded ... ");

        //GuideWizard
        if (Boolean.valueOf(ProperDefault.get("firstTime"))) {
            Base.writeLog("BEESOFT tour loaded ... ");

            TourWelcome p = new TourWelcome();
            p.setVisible(true);
        }

        /**
         * Check is 3DModels folder exists. If not, create it.
         */
        Base.getAppDataDirectory();
        Base.copy3DFiles();
        Base.cleanDirectoryTempFiles(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER);
        window.setEnabled(true);

        // Checks for software and firmware updates
        if (!Boolean.valueOf(ProperDefault.get("firstTime"))) {
            UpdateChecker advise = new UpdateChecker();


            if (advise.isUpdateBetaAvailable()) {
                advise.setMessage("AvailableBeta");
                advise.setVisible(true);
            } else if (advise.isUpdateStableAvailable()) {
                advise.setMessage("AvailableStable");
                advise.setVisible(true);
            } else {
                advise.dispose();
            }
        }

        /**
         * Error occured during driver initialization
         */
        if (Base.errorOccured == true) {
            Warning flashError = new Warning("close");
            flashError.setMessage("ErrorUpdating");
            flashError.setVisible(true);
        }

        Base.updateVersions();
    }

    /**
     * Get Splash Width.
     *
     * @return splash width
     */
    public int getWidth() {
        return newWidth;
    }

    /**
     * Set Splash Screen duration.
     *
     * @param milis Duration
     */
    public void setDuration(int milis) {
        this.duration = milis;
    }

    /**
     * Get Splash Screen Duration.
     *
     * @return duration
     */
    public int getDuration() {
        return duration;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jProgressBar1 = new javax.swing.JProgressBar();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        jProgressBar1.setBorderPainted(false);
        jProgressBar1.setPreferredSize(new java.awt.Dimension(150, 5));

        jLabel1.setBackground(new java.awt.Color(255, 128, 0));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                .addGap(0, 199, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JProgressBar jProgressBar1;
    // End of variables declaration//GEN-END:variables
}