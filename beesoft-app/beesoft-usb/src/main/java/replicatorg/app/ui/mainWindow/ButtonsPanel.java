package replicatorg.app.ui.mainWindow;

import javax.swing.ImageIcon;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import static replicatorg.app.ui.GraphicDesignComponents.getSSProLight;
import static replicatorg.app.ui.GraphicDesignComponents.getSSProRegular;
import replicatorg.app.ui.panels.PrintPanel;
import replicatorg.app.ui.panels.TourWelcome;
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
public class ButtonsPanel extends javax.swing.JPanel {

    private boolean models_pressed, maintenance_pressed, print_pressed;
    private final replicatorg.app.ui.MainWindow editor;
    private boolean jLabel1Bool = false;
    private boolean jLabel5Bool = true;
    private boolean jLabel4Bool = true;
    private boolean jLabel6Bool = false;
    private final int NUMBER_PRINTS_LIMIT = 15;
    private boolean mainWindowEnabled;

    public ButtonsPanel(replicatorg.app.ui.MainWindow mainWindow) {
        initComponents();
        setFont();
        editor = mainWindow;
        setTextLanguage();
        evaluateMaintenanceStatus();
        models_pressed = false;
        maintenance_pressed = false;
        print_pressed = false;
    }

    private void setTextLanguage() {
       // jLabel6.setText(Languager.getTagValue(1, "MainWindowButtons", "Models"));
        //jLabel4.setText(Languager.getTagValue(1, "MainWindowButtons", "Maintenance"));
        //jLabel5.setText(Languager.getTagValue(1, "MainWindowButtons", "QuickWizard"));
        //jLabel1.setText(Languager.getTagValue(1, "MainWindowButtons", "Print"));
    }

    public void setLogo(String iconPath) {
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/"+iconPath)));
    }
    
    public void setMessage(String message) {

        if(message.equals("is connecting")) {
            jLabel3.setText(Languager.getTagValue(1, "FeedbackLabel", "PrinterStatusConnecting"));
        } else if (message.equals("is connected")) {
            jLabel3.setText(Languager.getTagValue(1, "FeedbackLabel", "PrinterStatusReady"));
        } else if (message.equals("power saving")) {
            jLabel3.setText(Languager.getTagValue(1, "FeedbackLabel", "PrinterStatusPowerSaving"));
        } else if (message.equals("is disconnected")) {
            jLabel3.setText(Languager.getTagValue(1, "FeedbackLabel", "PrinterStatusDisconnected"));
        } 

    }

    private void setFont() {
        jLabel3.setFont(getSSProLight("18"));
       // jLabel6.setFont(getSSProLight("13"));
       // jLabel5.setFont(getSSProLight("13"));
       // jLabel4.setFont(getSSProLight("13"));
       // jLabel1.setFont(getSSProRegular("13"));

    }

    public void resetVariables() {
        models_pressed = false;
        maintenance_pressed = false;
        print_pressed = false;
    }

    public void goFilamentChange() {
        
        if (!maintenance_pressed) {
            if (Integer.valueOf(ProperDefault.get("nTotalPrints")) < NUMBER_PRINTS_LIMIT) {
             //   jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_pressed_7.png")));
                jLabel4Bool = false;
            } else if (Integer.valueOf(ProperDefault.get("nTotalPrints")) > NUMBER_PRINTS_LIMIT) {
            //    jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_pressed_7_btn.png")));
                jLabel4Bool = false;
            }

            maintenance_pressed = true;
        }
    }

    private double gramsCalculator(double meters) {
        double grams = meters * 12 / 4;

        if (grams > 0) {
            return (int) meters * 12 / 4;
        }

        return 0;
    }

    private void evaluateMaintenanceStatus() {
        if (Integer.valueOf(ProperDefault.get("nTotalPrints")) < NUMBER_PRINTS_LIMIT) {
         //   jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
        } else if (Integer.valueOf(ProperDefault.get("nTotalPrints")) > NUMBER_PRINTS_LIMIT) {
         //   jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7_btn.png")));
        }

    }

    public void updatePressedStateButton(String button) {
        if (button.equals("models")) {
           // jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
            jLabel6Bool = false;
            models_pressed = false;
        }
       
        if (button.equals("maintenance")) {
            if (Integer.valueOf(ProperDefault.get("nTotalPrints")) <= NUMBER_PRINTS_LIMIT) {
              //  jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
                jLabel4Bool = true;
            } else if (Integer.valueOf(ProperDefault.get("nTotalPrints")) > NUMBER_PRINTS_LIMIT) {
              //  jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7_btn.png")));
                jLabel4Bool = true;
            }

            maintenance_pressed = false;
        }

        if (button.equals("quick_guide")) {
         //   jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
            jLabel5Bool = true;
        }

        if (button.equals("print")) {
         //   jLabel1.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_2.png")));
            jLabel1Bool = false;
            print_pressed = false;
        }

    }

    public void connect() {
        updateFromState(Base.getMainWindow().getMachineInterface(), null);
    }

    private void updateFromState(final MachineInterface s, final MachineInterface machine) {

        if (!s.isConnected()) {

            setMessage("is disconnected");
        } else {

          //  jLabel5.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
            jLabel5Bool = true;

            
            if (Integer.valueOf(ProperDefault.get("nTotalPrints")) <= NUMBER_PRINTS_LIMIT) {
         //       jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7.png")));
                jLabel4Bool = true;
            } else if (Integer.valueOf(ProperDefault.get("nTotalPrints")) > NUMBER_PRINTS_LIMIT) {
          //      jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_7_btn.png")));
                jLabel4Bool = true;
            }

        //    jLabel1.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_simple_2.png")));
            //jLabel1Bool = true;
            setMessage("is connected");
        }
    }

    public void updateFromMachine(final MachineInterface machine) {
        if (machine != null) {
            updateFromState(machine, machine);
        }
    }

    public void setMainWindowEnabled(boolean editorEnabled) {
        this.mainWindowEnabled = editorEnabled;
    }

    public void blockModelsButton(boolean block) {
        models_pressed = block;
    }

    public boolean areIOFunctionsBlocked() {
        return models_pressed;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(240, 243, 244)));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/logo_beethefirst.png"))); // NOI18N

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/intel.png"))); // NOI18N

        jLabel3.setText("default: not connected");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 1054, Short.MAX_VALUE)
                .addGap(126, 126, 126))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(33, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addContainerGap())
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(240, 243, 244)));
        jPanel3.setPreferredSize(new java.awt.Dimension(681, 48));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public PrintPanel startPrint() {
               
        MachineInterface machine = Base.getMainWindow().getMachineInterface();

        if (machine.getModel().getMachineBusy()) {
            editor.showFeedBackMessage("moving");
        } else {//&& Base.isPrinting == false 
            if (!print_pressed && editor.validatePrintConditions()
                    || Boolean.valueOf(ProperDefault.get("localPrint"))) {
               // jLabel1.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "b_pressed_2.png")));
                jLabel1Bool = false;
                print_pressed = true;
                return editor.handlePrintPanel();
            }
        } 
        
        return null;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    // End of variables declaration//GEN-END:variables
}
