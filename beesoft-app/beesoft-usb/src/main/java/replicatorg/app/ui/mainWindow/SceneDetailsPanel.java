package replicatorg.app.ui.mainWindow;

import java.awt.Color;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.model.PrintBed;

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
public class SceneDetailsPanel extends javax.swing.JPanel {

    private PrintBed bed;
    private final String[] categories;
    private final DefaultComboBoxModel comboModel;
    private static final String FORMAT = "%2d:%2d";

    public SceneDetailsPanel() {
        initComponents();
        setFont();
        setTextLanguage();
        categories = fullFillCombo();
        comboModel = new DefaultComboBoxModel(categories);
    }

    private void setFont() {
        //jLabel1.setFont(GraphicDesignComponents.getSSProLight("20"));
        //jLabel2.setFont(GraphicDesignComponents.getSSProRegular("12"));
        //jLabel3.setFont(GraphicDesignComponents.getSSProRegular("12"));
        //jLabel4.setFont(GraphicDesignComponents.getSSProRegular("12"));
        //jLabel5.setFont(GraphicDesignComponents.getSSProBold("12"));
        //jLabel6.setFont(GraphicDesignComponents.getSSProBold("12"));
        //jLabel7.setFont(GraphicDesignComponents.getSSProBold("12"));
        //jLabel8.setFont(GraphicDesignComponents.getSSProBold("12"));
        //jLabel9.setFont(GraphicDesignComponents.getSSProRegular("12"));
        //jLabel10.setFont(GraphicDesignComponents.getSSProRegular("12"));
        //jTextField1.setFont(GraphicDesignComponents.getSSProRegular("12"));
        //jTextArea1.setFont(GraphicDesignComponents.getSSProRegular("12"));
    }

    private void setTextLanguage() {
        //jLabel1.setText(Languager.getTagValue(1, "SceneDetails", "Scene"));
//        jLabel2.setText(Languager.getTagValue(1,"", ""));
        //jLabel3.setText(Languager.getTagValue(1, "SceneDetails", "Scene_Name"));
        //jLabel4.setText(Languager.getTagValue(1, "SceneDetails", "Scene_Notes"));
        //jLabel5.setText(Languager.getTagValue(1, "SceneDetails", "Scene_NModels"));
        //jLabel6.setText(Languager.getTagValue(1, "SceneDetails", "Scene_LastPrintDate"));
        //jLabel7.setText(Languager.getTagValue(1, "SceneDetails", "Scene_LastPrintTime"));

        //jTextField1.setText(Languager.getTagValue(1, "SceneDetails", "Scene_Name_Hint"));
        //jTextField1.setForeground(Color.gray);

       // jTextArea1.setText(Languager.getTagValue(1, "SceneDetails", "Scene_Notes_Placeholder"));
       // jTextArea1.setForeground(Color.gray);
    }

    private String[] fullFillCombo() {
        return Base.getMainWindow().getCategoriesManager().getCategories();
    }

    private int getModelCategoryIndex() {
        String modelCategory = Base.getMainWindow().getBed().getCategory();
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(modelCategory)) {
                return i;
            }
        }

        return 0;
    }

    public void updateBed(PrintBed bed) {
        this.bed = bed;
        this.updateBedInfo();
    }

    private String minutesToHours(int t) {
        int hours = t / 60; //since both are ints, you get an int
        int minute = t % 60;

        return String.format(FORMAT, hours, minute);
    }

    public void updateBedInfo() {
        String bedName, bedDescription;

        bedName = bed.getName();
        bedDescription = bed.getDescription();

        if (!bedName.equals("Untitled")) {
          //  jTextField1.setForeground(Color.black);
          //  jTextField1.setText(bed.getName());
        }

        if (bedDescription != null && !bedDescription.trim().isEmpty()
                && !bedDescription.equals("NA")) {
          //  jTextArea1.setForeground(Color.black);
          //  jTextArea1.setText(bed.getDescription());
        }

       // jLabel8.setText(String.valueOf(bed.getNumberModels()));
       // jLabel9.setText(ProperDefault.get("dateLastPrint"));

        String durT = ProperDefault.get("durationLastPrint");
        int duration;

        if (!durT.equals("NA")) {
            duration = Integer.valueOf(durT);
            String hours = minutesToHours(duration).split("\\:")[0];
            String minutes = minutesToHours(duration).split("\\:")[1];
            int min = Integer.valueOf(minutes.trim());

            if (duration >= 120) {
                if (min > 1) {
          //          jLabel10.setText(hours + " " + Languager.getTagValue(1, "Print", "PrintHours") + " " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinutes"));
                } else if (min == 1) {
           //         jLabel10.setText(hours + " " + Languager.getTagValue(1, "Print", "PrintHours") + " " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinute"));
                } else {
          //          jLabel10.setText(hours + " " + Languager.getTagValue(1, "Print", "PrintHour"));
                }
            } else if (duration >= 60 && duration < 120) {
                if (min > 1) {
          //          jLabel10.setText(hours + " " + Languager.getTagValue(1, "Print", "PrintHour") + " " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinutes"));
                } else if (min == 1) {
          //          jLabel10.setText(hours + " " + Languager.getTagValue(1, "Print", "PrintHour") + " " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinute"));
                } else {
          //          jLabel10.setText(hours + " " + Languager.getTagValue(1, "Print", "PrintHour"));
                }
            } else {
          //      jLabel10.setText(" " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinutes"));
            }
        } else {
          //  jLabel10.setText(durT);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();

        jLabel2.setText("jLabel2");

        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(null);
        setMinimumSize(new java.awt.Dimension(215, 500));
        setPreferredSize(new java.awt.Dimension(215, 500));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMinimumSize(new java.awt.Dimension(199, 515));
        jPanel1.setPreferredSize(new java.awt.Dimension(199, 515));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 199, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 515, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
