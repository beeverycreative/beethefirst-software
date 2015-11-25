package replicatorg.app.ui.mainWindow;

import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.vecmath.AxisAngle4d;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ui.GraphicDesignComponents;

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
public class ModelsOperationCenterRotate extends javax.swing.JPanel {

    private boolean fiveDegrees_pressed;
    private double cambioFactor = 0.0174532925;

    public ModelsOperationCenterRotate() {
        initComponents();
        this.fiveDegrees_pressed = Base.getMainWindow().getCanvas().getControlTool(2).getAdvOption();
        if (fiveDegrees_pressed) {
            fiveDegrees_pressed = false; //trigger value for handler to work
            handleAdvancedOption();
        }
        //jRadioButton1.setSelected(true);
        Base.getMainWindow().getCanvas().setControlTool(2);
        setFont();
        setTextLanguage();
        Base.getMainWindow().getCanvas().setModelationType("rotate");
    }

    private void setFont() {
      /*  jLabel1.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel2.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel3.setFont(GraphicDesignComponents.getSSProLight("33"));
        jLabel4.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel5.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel9.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel10.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel11.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel12.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel13.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel14.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel15.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel23.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jTextField4.setFont(GraphicDesignComponents.getSSProRegular("13"));
        jTextField5.setFont(GraphicDesignComponents.getSSProRegular("13"));
        jTextField5.setFont(GraphicDesignComponents.getSSProRegular("13"));
        jRadioButton1.setFont(GraphicDesignComponents.getSSProRegular("13"));
        jRadioButton2.setFont(GraphicDesignComponents.getSSProRegular("13"));
        jRadioButton3.setFont(GraphicDesignComponents.getSSProRegular("13"));
    */
        }

    private void setTextLanguage() {
   /*     jLabel1.setText(Languager.getTagValue(1, "MainWindowButtons", "Move"));
        jLabel2.setText(Languager.getTagValue(1, "MainWindowButtons", "Rotate"));
        jLabel3.setText(Languager.getTagValue(1, "ModelDetails", "Model"));
        jLabel4.setText(Languager.getTagValue(1, "MainWindowButtons", "Scale"));
        jLabel5.setText(Languager.getTagValue(1, "MainWindowButtons", "Mirror"));
        jLabel9.setText(Languager.getTagValue(1, "MainWindowButtons", "Rotate") + " 90 " + Languager.getTagValue(1, "MainWindowButtons", "Degrees"));
        jLabel10.setText(Languager.getTagValue(1, "MainWindowButtons", "Rotate") + " -90 " + Languager.getTagValue(1, "MainWindowButtons", "Degrees"));
        jLabel11.setText(Languager.getTagValue(1, "MainWindowButtons", "Rotate") + " 90 " + " " + Languager.getTagValue(1, "MainWindowButtons", "Axis2"));
        jLabel12.setText(Languager.getTagValue(1, "MainWindowButtons", "Rotate"));
        jLabel23.setText(Languager.getTagValue(1, "MainWindowButtons", "Rotate") + " 5 " + Languager.getTagValue(1, "MainWindowButtons", "Degrees"));
    */
        }

    public void rotate2TheRight() {
        if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            Base.getMainWindow().getBed().getFirstPickedModel().getEditer().rotateObject(new AxisAngle4d(0d, 0d, 1d, Math.PI / 2));  
            Base.getMainWindow().getBed().setGcodeOK(false);
        } else {
            Base.getMainWindow().showFeedBackMessage("modelNotPicked");
        }
    }
    
    public void rotate2TheLeft() {
        if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            Base.getMainWindow().getBed().getFirstPickedModel().getEditer().rotateObject(new AxisAngle4d(0d, 0d, 1d, -Math.PI / 2));  
            Base.getMainWindow().getBed().setGcodeOK(false);
        } else {
            Base.getMainWindow().showFeedBackMessage("modelNotPicked");
        }
    }    
    
    private void rotateRight() {
 /*       if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            if (jRadioButton1.isSelected()) {
                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().rotateObject(new AxisAngle4d(1d, 0d, 0d, -Math.PI / 2));
            } else if (jRadioButton2.isSelected()) {
                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().rotateObject(new AxisAngle4d(0d, 1d, 0d, Math.PI / 2));
            } else {
                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().rotateObject(new AxisAngle4d(0d, 0d, 1d, -Math.PI / 2));
            }
            Base.getMainWindow().getBed().setGcodeOK(false);
        } else {
            Base.getMainWindow().showFeedBackMessage("modelNotPicked");
        }*/
    }

    private void rotateLeft() {
    /*    if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            if (jRadioButton1.isSelected()) {
                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().rotateObject(new AxisAngle4d(1d, 0d, 0d, Math.PI / 2));
            } else if (jRadioButton2.isSelected()) {
                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().rotateObject(new AxisAngle4d(0d, 1d, 0d, -Math.PI / 2));
            } else {
                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().rotateObject(new AxisAngle4d(0d, 0d, 1d, Math.PI / 2));
            }
            Base.getMainWindow().getBed().setGcodeOK(false);
        } else {
            Base.getMainWindow().showFeedBackMessage("modelNotPicked");
        }*/
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
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(new java.awt.Dimension(190, 375));
        setMinimumSize(new java.awt.Dimension(190, 375));
        setPreferredSize(new java.awt.Dimension(190, 375));

        jLabel6.setName("moreOptionsCheckbox"); // NOI18N

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/Untitled-1-04.png"))); // NOI18N

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/Untitled-1-01.png"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel18)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addContainerGap(16, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(114, 114, 114)
                    .addComponent(jLabel17)
                    .addContainerGap(76, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGap(231, 231, 231)
                .addComponent(jLabel6)
                .addGap(0, 0, 0)
                .addComponent(jLabel18)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(240, 240, 240)
                    .addComponent(jLabel17)
                    .addContainerGap(344, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    // End of variables declaration//GEN-END:variables

    private void handleAdvancedOption() {
        if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
            if (!fiveDegrees_pressed) {
          //      jLabel22.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_checked.png")));
                fiveDegrees_pressed = true;
                Base.getMainWindow().getCanvas().getControlTool(2).setAdvOption(true);
            } else {
          //      jLabel22.setIcon(new ImageIcon(GraphicDesignComponents.getImage("mainWindow", "c_unchecked.png")));
                fiveDegrees_pressed = false;
                Base.getMainWindow().getCanvas().getControlTool(2).setAdvOption(false);
            }
        } else {
            Base.getMainWindow().showFeedBackMessage("modelNotPicked");
        }
    }
}
