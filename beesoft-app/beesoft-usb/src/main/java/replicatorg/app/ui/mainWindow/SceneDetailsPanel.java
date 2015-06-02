package replicatorg.app.ui.mainWindow;

import java.awt.Color;
import javax.swing.DefaultComboBoxModel;
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
        jLabel1.setFont(GraphicDesignComponents.getSSProLight("20"));
        jLabel2.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel3.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel4.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel5.setFont(GraphicDesignComponents.getSSProBold("12"));
        jLabel6.setFont(GraphicDesignComponents.getSSProBold("12"));
        jLabel7.setFont(GraphicDesignComponents.getSSProBold("12"));
        jLabel8.setFont(GraphicDesignComponents.getSSProBold("12"));
        jLabel9.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel10.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jTextField1.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jTextArea1.setFont(GraphicDesignComponents.getSSProRegular("12"));
    }

    private void setTextLanguage() {
        jLabel1.setText(Languager.getTagValue(1, "SceneDetails", "Scene"));
//        jLabel2.setText(Languager.getTagValue(1,"", ""));
        jLabel3.setText(Languager.getTagValue(1, "SceneDetails", "Scene_Name"));
        jLabel4.setText(Languager.getTagValue(1, "SceneDetails", "Scene_Notes"));
        jLabel5.setText(Languager.getTagValue(1, "SceneDetails", "Scene_NModels"));
        jLabel6.setText(Languager.getTagValue(1, "SceneDetails", "Scene_LastPrintDate"));
        jLabel7.setText(Languager.getTagValue(1, "SceneDetails", "Scene_LastPrintTime"));
        
        jTextArea1.setText(Languager.getTagValue(1, "SceneDetails", "Scene_Notes_Placeholder"));        
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
        jTextField1.setText(bed.getName());
        
        if (bed.getDescription() != null && !bed.getDescription().trim().isEmpty()) {
            jTextArea1.setText(bed.getDescription());
        }
        
        jLabel8.setText(String.valueOf(bed.getNumberModels()));
        jLabel9.setText(ProperDefault.get("dateLastPrint"));

        String durT = ProperDefault.get("durationLastPrint");
        int duration;

        if (!durT.equals("NA")) {
            duration = Integer.valueOf(durT);
            String hours = minutesToHours(duration).split("\\:")[0];
            String minutes = minutesToHours(duration).split("\\:")[1];
            int min = Integer.valueOf(minutes.trim());

            if (duration >= 120) {
                if (min > 1) {
                    jLabel10.setText(hours + " " + Languager.getTagValue(1, "Print", "PrintHours") + " " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinutes"));
                } else if (min == 1) {
                    jLabel10.setText(hours + " " + Languager.getTagValue(1, "Print", "PrintHours") + " " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinute"));
                } else {
                    jLabel10.setText(hours + " " + Languager.getTagValue(1, "Print", "PrintHour"));
                }
            } else if (duration >= 60 && duration < 120) {
                if (min > 1) {
                    jLabel10.setText(hours + " " + Languager.getTagValue(1, "Print", "PrintHour") + " " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinutes"));
                } else if (min == 1) {
                    jLabel10.setText(hours + " " + Languager.getTagValue(1, "Print", "PrintHour") + " " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinute"));
                } else {
                    jLabel10.setText(hours + " " + Languager.getTagValue(1, "Print", "PrintHour"));
                }
            } else {
                jLabel10.setText(" " + minutes + " " + Languager.getTagValue(1, "Print", "PrintMinutes"));
            }
        } else {
            jLabel10.setText(durT);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        jLabel2.setText("jLabel2");

        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(null);
        setMinimumSize(new java.awt.Dimension(215, 500));
        setPreferredSize(new java.awt.Dimension(215, 500));

        jPanel1.setBackground(new java.awt.Color(240, 243, 244));
        jPanel1.setMinimumSize(new java.awt.Dimension(199, 515));
        jPanel1.setPreferredSize(new java.awt.Dimension(199, 515));

        jLabel10.setText("NA");
        jLabel10.setName("durationLastPrint"); // NOI18N

        jTextField1.setText("Untitled");
        jTextField1.setName("nameTextField"); // NOI18N
        jTextField1.setPreferredSize(new java.awt.Dimension(40, 27));
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(35, 31, 32));
        jLabel3.setText("Name");
        jLabel3.setName("nameTitle"); // NOI18N

        jLabel9.setText("NA");
        jLabel9.setName("dateLastPrint"); // NOI18N

        jLabel6.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(35, 31, 32));
        jLabel6.setText("Date of last print");
        jLabel6.setName("dateLastPrintTitle"); // NOI18N

        jLabel8.setText("0");
        jLabel8.setName("nModels"); // NOI18N

        jLabel4.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(35, 31, 32));
        jLabel4.setText("Description");
        jLabel4.setName("descriptionTitle"); // NOI18N

        jLabel5.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(35, 31, 32));
        jLabel5.setText("Number of Models");
        jLabel5.setName("nModelsTitle"); // NOI18N

        jLabel7.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(35, 31, 32));
        jLabel7.setText("Duration of last print");
        jLabel7.setName("durationLastPrintTitle"); // NOI18N

        jLabel1.setFont(new java.awt.Font("Ubuntu", 0, 20)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(145, 145, 145));
        jLabel1.setText("Scene");
        jLabel1.setName("sceneTitle"); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextArea1MouseClicked(evt);
            }
        });
        jTextArea1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextArea1FocusLost(evt);
            }
        });
        jTextArea1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextArea1KeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(9, 9, 9))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addGap(1, 1, 1)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel4)
                .addGap(1, 1, 1)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addGap(1, 1, 1)
                .addComponent(jLabel8)
                .addGap(15, 15, 15)
                .addComponent(jLabel6)
                .addGap(1, 1, 1)
                .addComponent(jLabel9)
                .addGap(15, 15, 15)
                .addComponent(jLabel7)
                .addGap(1, 1, 1)
                .addComponent(jLabel10)
                .addContainerGap(168, Short.MAX_VALUE))
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

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
        bed.setName(jTextField1.getText());
    }//GEN-LAST:event_jTextField1KeyReleased

    private void jTextArea1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea1KeyReleased
        bed.setDescription(jTextArea1.getText());
    }//GEN-LAST:event_jTextArea1KeyReleased

    private void jTextArea1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextArea1MouseClicked
        if (jTextArea1.getText().equals(Languager.getTagValue(1, "SceneDetails", "Scene_Notes_Placeholder"))) {
            jTextArea1.setText("");
            jTextArea1.setForeground(Color.black);
        }
    }//GEN-LAST:event_jTextArea1MouseClicked

    private void jTextArea1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea1FocusLost
                
        if (jTextArea1.getText().equals("")) {
            jTextArea1.setText(Languager.getTagValue(1, "SceneDetails", "Scene_Notes_Placeholder"));
            jTextArea1.setForeground(Color.gray);
        }
    }//GEN-LAST:event_jTextArea1FocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
