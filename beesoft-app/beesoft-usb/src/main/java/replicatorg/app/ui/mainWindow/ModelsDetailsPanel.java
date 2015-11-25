package replicatorg.app.ui.mainWindow;

import java.awt.Color;
import java.text.DecimalFormat;
import javax.swing.ImageIcon;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.app.ui.modeling.EditingModel;
import replicatorg.model.Model;
import replicatorg.model.PrintBed;
import replicatorg.util.Units_and_Numbers;

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
public class ModelsDetailsPanel extends javax.swing.JPanel {

    private PrintBed bed;
    private boolean panelDisabled;
    private boolean oneModel;
    private DimensionsThread updateThread;

    public ModelsDetailsPanel() {
        initComponents();
        setFont();
        panelDisabled = false;
        oneModel = true;
        setTextLanguage();
//        updateThread = new DimensionsThread(this);
//        updateThread.start();
    }

    private void setFont() {
      //  jLabel1.setFont(GraphicDesignComponents.getSSProLight("20"));
      //  jLabel2.setFont(GraphicDesignComponents.getSSProRegular("12"));
       // jLabel3.setFont(GraphicDesignComponents.getSSProBold("12"));
       // jLabel4.setFont(GraphicDesignComponents.getSSProRegular("12"));
      //  jLabel5.setFont(GraphicDesignComponents.getSSProRegular("12"));
       // jLabel6.setFont(GraphicDesignComponents.getSSProBold("12"));
       // jLabel7.setFont(GraphicDesignComponents.getSSProBold("12"));
      //  jLabel8.setFont(GraphicDesignComponents.getSSProBold("12"));
        //deleteButton.setFont(GraphicDesignComponents.getSSProBold("12"));
       // jLabel9.setFont(GraphicDesignComponents.getSSProRegular("12"));
       // jLabel11.setFont(GraphicDesignComponents.getSSProRegular("12"));
       // jLabel12.setFont(GraphicDesignComponents.getSSProRegular("12"));
       // jLabel13.setFont(GraphicDesignComponents.getSSProBold("12"));
       // jLabel14.setFont(GraphicDesignComponents.getSSProBold("12"));
       // jLabel15.setFont(GraphicDesignComponents.getSSProLight("12"));

       // jTextField1.setFont(GraphicDesignComponents.getSSProLight("12"));
       // jTextArea1.setFont(GraphicDesignComponents.getSSProLight("12"));
        //jTextField3.setFont(GraphicDesignComponents.getSSProLight("12"));
        //jTextField4.setFont(GraphicDesignComponents.getSSProLight("12"));
        //jTextField5.setFont(GraphicDesignComponents.getSSProLight("12"));

    }

    private void setTextLanguage() {
      //  jLabel1.setText(Languager.getTagValue(1, "ModelDetails", "Model"));
      // jLabel15.setText(Languager.getTagValue(1, "ModelDetails", "Model_N"));
      //  jLabel5.setText(Languager.getTagValue(1, "ModelDetails", "Model_Selected"));

        if (ProperDefault.get("measures").equals("inches")) {
       //     jLabel11.setText(Languager.getTagValue(1, "ModelDetails", "Model_Dimensions")
       //             + " (" + Languager.getTagValue(1, "MainWindowButtons", "Inches") + ")");
        } else {
       //    jLabel11.setText(Languager.getTagValue(1, "ModelDetails", "Model_Dimensions")
       //             + " (" + Languager.getTagValue(1, "MainWindowButtons", "MM") + ")");
        }
      // jLabel12.setText(Languager.getTagValue(1, "ModelDetails", "Model_Name"));
      //  jLabel4.setText(Languager.getTagValue(1, "ModelDetails", "Model_Notes"));

     //  jLabel6.setText(Languager.getTagValue(1, "ApplicationMenus", "Edit_PutPlatform"));
      //  jLabel7.setText(Languager.getTagValue(1, "ApplicationMenus", "Edit_Center"));
     //   jLabel8.setText(Languager.getTagValue(1, "ApplicationMenus", "Edit_Reset"));
       // deleteButton.setText(Languager.getTagValue(1, "ApplicationMenus", "Edit_Delete"));
        
       // jTextArea1.setText(Languager.getTagValue(1, "ModelDetails", "Model_Notes_Placeholder"));        

    }

    public void updateBed(PrintBed bed) {
        this.bed = bed;
        this.updateBedInfo();
        evaluateBed();
    }

    public void updateBedInfo() {
//        evaluateBed();
        Model model = Base.getMainWindow().getBed().getFirstPickedModel();

        DecimalFormat df = new DecimalFormat("#.00");
        EditingModel modelEditer = Base.getMainWindow().getBed().getFirstPickedModel().getEditer();

        if (ProperDefault.get("measures").equals("inches")) {
         // jTextField3.setText(String.valueOf(df.format(Units_and_Numbers.millimetersToInches(modelEditer.getWidth()))));
         //   jTextField4.setText(String.valueOf(df.format(Units_and_Numbers.millimetersToInches(modelEditer.getDepth()))));
         //   jTextField5.setText(String.valueOf(df.format(Units_and_Numbers.millimetersToInches(modelEditer.getHeight()))));
        } else {
         //   jTextField3.setText(String.valueOf(df.format(modelEditer.getWidth())));
         //   jTextField4.setText(String.valueOf(df.format(modelEditer.getDepth())));
         //  jTextField5.setText(String.valueOf(df.format(modelEditer.getHeight())));
        }

      //  jTextField1.setText(String.valueOf(model.getName()));
        
        if (model.getDescription() != null && !model.getDescription().trim().isEmpty()) {
      //     jTextArea1.setForeground(Color.black);
      //     jTextArea1.setText(String.valueOf(model.getDescription()));
        }
    }

    private void evaluateBed() {
        if (bed.getNumberPickedModels() > 1) {
           // jLabel11.setEnabled(false);
           // jLabel12.setEnabled(false);
//           jLabel6.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_8.png")));
          //  jLabel7.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_8.png")));
          //  jLabel8.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_8.png")));
           // deleteButton.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_8.png")));
           // jTextField3.setEnabled(false);
           // jTextField4.setEnabled(false);
           // jTextField5.setEnabled(false);
        //   jTextField1.setEnabled(false);
        //    jTextArea1.setEnabled(false);
            panelDisabled = true;
         //   jLabel9.setText(String.valueOf(bed.getNumberPickedModels()));
            if (bed.getNumberPickedModels() > 1) {
                oneModel = false;
            }
        }
       // jLabel9.setText(String.valueOf(bed.getNumberPickedModels()));
    }

    public void updateDimensions() {
        if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            DecimalFormat df = new DecimalFormat("#0.00");
            EditingModel modelEditer = Base.getMainWindow().getBed().getFirstPickedModel().getEditer();

            if (ProperDefault.get("measures").equals("inches")) {
             //   jTextField3.setText(String.valueOf(df.format(Units_and_Numbers.millimetersToInches(modelEditer.getWidth()))));
             //   jTextField4.setText(String.valueOf(df.format(Units_and_Numbers.millimetersToInches(modelEditer.getDepth()))));
             //   jTextField5.setText(String.valueOf(df.format(Units_and_Numbers.millimetersToInches(modelEditer.getHeight()))));
            } else {
             //   jTextField3.setText(String.valueOf(df.format(modelEditer.getWidth())));
             //   jTextField4.setText(String.valueOf(df.format(modelEditer.getDepth())));
             //   jTextField5.setText(String.valueOf(df.format(modelEditer.getHeight())));
            }
        }

    }

    public void killThread() {
        updateThread.stop();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        jLabel2.setText("jLabel2");

        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(null);
        setMinimumSize(new java.awt.Dimension(215, 515));
        setPreferredSize(new java.awt.Dimension(215, 515));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMinimumSize(new java.awt.Dimension(199, 515));
        jPanel1.setPreferredSize(new java.awt.Dimension(199, 515));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/Untitled-1-02.png"))); // NOI18N

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/Untitled-1-03.png"))); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel1))
                .addContainerGap(17, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 499, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public void resetModel() { 
        try {
          // this.jLabel8MousePressed(null);
        } catch (Exception ex) {
            
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}

class DimensionsThread extends Thread {

    ModelsDetailsPanel window;

    public DimensionsThread(ModelsDetailsPanel w) {
        super("Dimensions Thread");
        this.window = w;
    }

    @Override
    public void run() {

        while (true) {
            window.updateDimensions();
        }

    }
}
