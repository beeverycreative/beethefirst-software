package replicatorg.app.ui.mainWindow;

import java.text.DecimalFormat;
import javax.swing.ImageIcon;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.machine.model.BuildVolume;
import replicatorg.model.CAMPanel;
import replicatorg.model.Model;
import replicatorg.util.Units_and_Numbers;
import static replicatorg.util.Units_and_Numbers.sGetDecimalStringAnyLocaleAsDouble;

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
public class ModelsOperationCenterScale extends javax.swing.JPanel {
    
    private double initialWidth, initialDepth, initialHeight;
    private final boolean mm;
    private boolean checkX = true, checkY = true, checkZ = true;
    private double oldX, oldY, oldZ;
    DecimalFormat df = new DecimalFormat("#0.00");
    static Double X_MIN = sGetDecimalStringAnyLocaleAsDouble(ProperDefault.get("editor.xmin"));
    static Double Y_MIN = sGetDecimalStringAnyLocaleAsDouble(ProperDefault.get("editor.ymin"));
    static Double Z_MIN = sGetDecimalStringAnyLocaleAsDouble(ProperDefault.get("editor.zmin"));
    static Double X_MAX = sGetDecimalStringAnyLocaleAsDouble(ProperDefault.get("editor.xmax"));
    static Double Y_MAX = sGetDecimalStringAnyLocaleAsDouble(ProperDefault.get("editor.ymax"));
    static Double Z_MAX = sGetDecimalStringAnyLocaleAsDouble(ProperDefault.get("editor.zmax"));

    public ModelsOperationCenterScale() {
        initComponents();
        this.mm = true;

        Base.getMainWindow().getCanvas().setControlTool(3);
        Base.getMainWindow().getCanvas().getControlTool(3).setModelsOperationScale(this);
        
        setFont();
        setTextLanguage();
        CAMPanel canvas = Base.getMainWindow().getCanvas();
        canvas.setModelationType("scale");
        Model model = canvas.getModel();

        this.initialWidth = model.getEditer().getWidth();
        this.initialHeight = model.getEditer().getHeight();
        this.initialDepth = model.getEditer().getDepth();

        if (ProperDefault.get("measures").equals("inches")) {
         //   iFieldX.setText(df.format(Units_and_Numbers.millimetersToInches(this.initialWidth)));
         //   iFieldY.setText(df.format(Units_and_Numbers.millimetersToInches(this.initialDepth)));
         //   iFieldZ.setText(df.format(Units_and_Numbers.millimetersToInches(this.initialHeight)));
        } else if(ProperDefault.get("measures").equals("mm")) {
        //    iFieldX.setText(df.format(this.initialWidth));
        //    iFieldY.setText(df.format(this.initialDepth));
        //    iFieldZ.setText(df.format(this.initialHeight));
        }

        //oldX = sGetDecimalStringAnyLocaleAsDouble(iFieldX.getText());
        //oldY = sGetDecimalStringAnyLocaleAsDouble(iFieldY.getText());
        //oldZ = sGetDecimalStringAnyLocaleAsDouble(iFieldZ.getText());

    }


    private void setFont() {
       // jLabel1.setFont(GraphicDesignComponents.getSSProRegular("12"));
       // jLabel2.setFont(GraphicDesignComponents.getSSProRegular("12"));
       // jLabel3.setFont(GraphicDesignComponents.getSSProLight("33"));
       // jLabel4.setFont(GraphicDesignComponents.getSSProRegular("12"));
       // jLabel5.setFont(GraphicDesignComponents.getSSProRegular("12"));
       // jLabel12.setFont(GraphicDesignComponents.getSSProRegular("12"));
       // jLabel13.setFont(GraphicDesignComponents.getSSProRegular("12"));
       // jLabel14.setFont(GraphicDesignComponents.getSSProRegular("12"));
       // jLabel15.setFont(GraphicDesignComponents.getSSProRegular("12"));
       // iFieldX.setFont(GraphicDesignComponents.getSSProRegular("13"));
       // iFieldY.setFont(GraphicDesignComponents.getSSProRegular("13"));
       // iFieldZ.setFont(GraphicDesignComponents.getSSProRegular("13"));
       // bScaleToMax.setFont(GraphicDesignComponents.getSSProRegular("12"));
       // bApply.setFont(GraphicDesignComponents.getSSProRegular("12"));
    }

    private void setTextLanguage() {
       // jLabel1.setText(Languager.getTagValue(1, "MainWindowButtons", "Move"));
       // jLabel2.setText(Languager.getTagValue(1, "MainWindowButtons", "Rotate"));
       // jLabel3.setText(Languager.getTagValue(1, "ModelDetails", "Model"));
       // jLabel4.setText(Languager.getTagValue(1, "MainWindowButtons", "Scale"));
       // jLabel5.setText(Languager.getTagValue(1, "MainWindowButtons", "Mirror"));


            if (ProperDefault.get("measures").equals("inches")) {
        //        jLabel12.setText(Languager.getTagValue(1, "MainWindowButtons", "Scale")
        //                + " (" + Languager.getTagValue(1, "MainWindowButtons", "Inches") + ")");
            } else if (ProperDefault.get("measures").equals("mm")){
        //        jLabel12.setText(Languager.getTagValue(1, "MainWindowButtons", "Scale")
        //                + " (" + Languager.getTagValue(1, "MainWindowButtons", "MM") + ")");
            }

        //bScaleToMax.setText(Languager.getTagValue(1, "MainWindowButtons", "ScaleToMax"));
        //bApply.setText(Languager.getTagValue(1, "MainWindowButtons", "Apply"));

    }

    public void setXValue(String val) {
     //   this.iFieldX.setText(val);
        oldX = sGetDecimalStringAnyLocaleAsDouble(val);
    }

    public void setYValue(String val) {
     //   this.iFieldY.setText(val);
        oldY = sGetDecimalStringAnyLocaleAsDouble(val);
    }

    public void setZValue(String val) {
    //    this.iFieldZ.setText(val);
        oldZ = sGetDecimalStringAnyLocaleAsDouble(val);
    }

    private void toggleX() {
        if (checkX) {
            checkX = false;
     //       jLabel17.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_unchecked.png")));
        } else {
            checkX = true;
    //        jLabel17.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_checked.png")));
        }
    }

    private void toggleY() {
        if (checkY) {
            checkY = false;
     //       jLabel19.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_unchecked.png")));
        } else {
            checkY = true;
     //       jLabel19.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_checked.png")));
        }
    }

    private void toggleZ() {
        if (checkZ) {
            checkZ = false;
     //       jLabel20.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_unchecked.png")));
        } else {
            checkZ = true;
     //       jLabel20.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_checked.png")));
        }
    }

    public boolean isXLocked() {
        return this.checkX;
    }

    public boolean isYLocked() {
        return this.checkY;
    }

    public boolean isZLocked() {
        return this.checkZ;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(new java.awt.Dimension(185, 375));
        setMinimumSize(new java.awt.Dimension(190, 375));
        setPreferredSize(new java.awt.Dimension(190, 375));
        setRequestFocusEnabled(false);

        jLabel6.setName("moreOptionsCheckbox"); // NOI18N

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/Untitled-1-04.png"))); // NOI18N

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/Untitled-1-01.png"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel8))
                .addGap(178, 178, 178))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGap(177, 177, 177)
                .addComponent(jLabel6)
                .addGap(0, 0, 0)
                .addComponent(jLabel8)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public void scaleToMax() {
      //  this.bScaleToMaxMousePressed(null);
    }
    
    public void scaleToQuarter() {
        Model model = Base.getMainWindow().getBed().getFirstPickedModel();
        BuildVolume machineVolume = Base.getMainWindow().getMachineInterface().getModel().getBuildVolume();

        double scaleX = model.getEditer().getWidth() / machineVolume.getX();
        double scaleY = model.getEditer().getDepth() / machineVolume.getY();
        double scaleZ = model.getEditer().getHeight() / machineVolume.getZ();

        double scale = Math.max(scaleX, Math.max(scaleZ, scaleY));
        scale = Units_and_Numbers.round(scale, 3) / 1.25;

        //model.getEditer().centerAndToBed();
        model.getEditer().center();
        model.getEditer().scale(scale, true, false);

        if (model.getEditer().modelOutBonds()) {
            //Small adjustment to avoid the model being out of bounds
            scale = 0.975;
            model.getEditer().scale(scale, true, true);
        }         

        //Sets the initial sizing variables to the current values
        this.resetInitialScaleVariables();
        model.resetScale();
        
        this.update_iFields();
    }  
    
    public void scaleToHalf() {
        Model model = Base.getMainWindow().getBed().getFirstPickedModel();
        BuildVolume machineVolume = Base.getMainWindow().getMachineInterface().getModel().getBuildVolume();

        double scaleX = machineVolume.getX() / model.getEditer().getWidth();
        double scaleY = machineVolume.getY() / model.getEditer().getDepth();
        double scaleZ = machineVolume.getZ() / model.getEditer().getHeight();

        double scale = Math.min(scaleX, Math.min(scaleZ, scaleY));
        scale = Units_and_Numbers.round(scale, 3) / 2;

        //model.getEditer().centerAndToBed();
        model.getEditer().center();
        model.getEditer().scale(scale, true, false);

        if (model.getEditer().modelOutBonds()) {
            //Small adjustment to avoid the model being out of bounds
            scale = 0.975;
            model.getEditer().scale(scale, true, true);
        }         

        //Sets the initial sizing variables to the current values
        this.resetInitialScaleVariables();
        model.resetScale();
        
        this.update_iFields();
    }     
            
    /**
     * Sets the initial sizing variables to the new max scale size
     */
    public void resetInitialScaleVariables() {

        Model model = Base.getMainWindow().getBed().getFirstPickedModel();

        this.initialWidth = model.getEditer().getWidth();
        this.initialHeight = model.getEditer().getHeight();
        this.initialDepth = model.getEditer().getDepth();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    // End of variables declaration//GEN-END:variables

    private void refresh_iFields() {
        
//        double x, y, z;
//        if(ProperDefault.get("measures").equals("inches")) {
//            x = Units_and_Numbers.millimetersToInches(this.oldX);
//            y = Units_and_Numbers.millimetersToInches(this.oldY);
//            z = Units_and_Numbers.millimetersToInches(this.oldZ);
//        } else {
//            x = this.oldX;
//            y = this.oldY;
//            z = this.oldZ;
//        }
                
        try {

         //   iFieldX.setText(df.format(this.oldX));
        } catch (Exception e) {
        }
        try {

          //  iFieldY.setText(df.format(this.oldY));
        } catch (Exception e) {
        }
        try {

         //   iFieldZ.setText(df.format(this.oldZ));
        } catch (Exception e) {
        }
    }
    
    /**
     * Updates the axis value fields with the current size values
     */
    private void update_iFields() {
            
        double width, depth, height;
        Model model = Base.getMainWindow().getBed().getFirstPickedModel();
        
        if(ProperDefault.get("measures").equals("inches")) {
            width = Units_and_Numbers.millimetersToInches(model.getEditer().getWidth());
            depth = Units_and_Numbers.millimetersToInches(model.getEditer().getDepth());
            height = Units_and_Numbers.millimetersToInches(model.getEditer().getHeight());
        } else {
            width = model.getEditer().getWidth();
            depth = model.getEditer().getDepth();
            height = model.getEditer().getHeight();
        }

        try {

          //  iFieldX.setText(df.format(width));
          //  this.oldX = model.getEditer().getWidth();
        } catch (Exception e) {
        }
        try {

          //  iFieldY.setText(df.format(depth));
          //  this.oldY = model.getEditer().getDepth();
        } catch (Exception e) {
        }
        try {

          //  iFieldZ.setText(df.format(height));
          //  this.oldZ = model.getEditer().getHeight();
        } catch (Exception e) {
        }
    }    
}
