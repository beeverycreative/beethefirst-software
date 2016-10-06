package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ItemEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import pt.beeverycreative.beesoft.filaments.Filament;
import pt.beeverycreative.beesoft.filaments.Filament.Material;
import replicatorg.app.Base;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
import pt.beeverycreative.beesoft.filaments.Nozzle;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.app.ui.popups.Query;
import replicatorg.app.ui.popups.Warning;
import replicatorg.machine.model.MachineModel;

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
public class FilamentCodeInsertion extends BaseDialog {

    private final MachineModel model = Base.getMachineLoader().getMachineInterface().getDriver().getMachine();
    private final ImageIcon bothSupportImage = new ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/troca_filamento_ambos.png"));
    private final ImageIcon supportImage = new ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/troca_filamento_sup.png"));
    private Nozzle currentNozzle = new Nozzle(model.getNozzleType());
    private DefaultComboBoxModel<Filament> comboModel;
    private int firstCompatibleFilamentIndex = 0;

    public FilamentCodeInsertion() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        Base.writeLog("First step of the filament change operation", this.getClass());
        initComponents();
        super.enableDrag();
        super.centerOnScreen();
        setTextLanguage();
        evaluateInitialConditions();
    }

    private void setTextLanguage() {
        jLabel1.setText(Languager.getTagValue("FilamentWizard", "Title1"));
        jLabel3.setText(Languager.getTagValue("FilamentWizard", "Title4"));
        bNext.setText(Languager.getTagValue("OptionPaneButtons", "Line7"));
        bCancel.setText(Languager.getTagValue("OptionPaneButtons", "Line3"));
        lDesc.setText("<html>" + Languager.getTagValue("FilamentWizard", "CodeInsertionDesc") + "</html>");
        lDescA.setText("<html>" + Languager.getTagValue("FilamentWizard", "ADesc") + "</html>");
        lDescB.setText("<html>" + Languager.getTagValue("FilamentWizard", "BDesc") + "</html>");
    }

    private void evaluateInitialConditions() {
        final Filament selectedFilament;
        final FilamentComboItem[] filaments;

        filaments = fullFillCombo();
        comboModel = new DefaultComboBoxModel(filaments);
        jComboBox1.setModel(comboModel);
        jComboBox1.setRenderer(new CustomRenderer(jComboBox1));
        jComboBox1.setSelectedIndex(firstCompatibleFilamentIndex);
        selectedFilament = ((FilamentComboItem) jComboBox1.getSelectedItem()).getFilamentObject();

        if (selectedFilament.getMaterial() == Material.PLA) {
            jLabel2.setIcon(bothSupportImage);
        } else {
            jLabel2.setIcon(supportImage);
        }
    }

    private FilamentComboItem[] fullFillCombo() {
        final Filament[] filaments;
        final FilamentComboItem[] filamentComboItems;
        int index = 0;

        filaments = FilamentControler.getFilamentArray();
        firstCompatibleFilamentIndex = 0;

        if (filaments.length == 0) {
            Base.writeLog("No filaments found for this printer!", this.getClass());
            filamentComboItems = new FilamentComboItem[1];
            filamentComboItems[0] = new FilamentComboItem(new Filament("No filament available"), true);
        } else {
            filamentComboItems = new FilamentComboItem[filaments.length];

            FilamentComboItem temp;
            for (Filament fil : filaments) {
                temp = new FilamentComboItem(fil);
                filamentComboItems[index] = temp;

                if (temp.isCompatible() && firstCompatibleFilamentIndex == 0) {
                    firstCompatibleFilamentIndex = index;
                }

                index++;
            }

            bNext.setEnabled(true);
        }

        return filamentComboItems;

    }

    private void doCancel() {
        Base.writeLog("Filament load/unload canceled", this.getClass());
        dispose();
        Base.getMainWindow().getButtons().updatePressedStateButton("maintenance");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        bNext = new javax.swing.JLabel();
        bCancel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        bX = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jComboBox1 = new javax.swing.JComboBox();
        lDesc = new javax.swing.JLabel();
        lDescA = new javax.swing.JLabel();
        lDescB = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setMinimumSize(new java.awt.Dimension(567, 501));
        setUndecorated(true);

        jPanel2.setBackground(new java.awt.Color(255, 203, 5));
        jPanel2.setMinimumSize(new java.awt.Dimension(20, 38));
        jPanel2.setPreferredSize(new java.awt.Dimension(567, 38));

        bNext.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        bNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bNext.setText("Next");
        bNext.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_disabled_21.png"))); // NOI18N
        bNext.setEnabled(false);
        bNext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNext.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bNextMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bNextMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bNextMouseEntered(evt);
            }
        });

        bCancel.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        bCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_21.png"))); // NOI18N
        bCancel.setText("Cancel");
        bCancel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bCancelMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bCancelMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bCancelMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 408, Short.MAX_VALUE)
                .addComponent(bNext)
                .addGap(12, 12, 12))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bNext)
                    .addComponent(bCancel))
                .addGap(20, 20, 20))
        );

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));
        jPanel1.setPreferredSize(new java.awt.Dimension(567, 468));

        jPanel4.setBackground(new java.awt.Color(248, 248, 248));
        jPanel4.setMinimumSize(new java.awt.Dimension(62, 26));
        jPanel4.setPreferredSize(new java.awt.Dimension(70, 30));
        jPanel4.setRequestFocusEnabled(false);

        bX.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_pressed_9.png"))); // NOI18N
        bX.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bXMousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addComponent(bX, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(bX, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(9, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Source Sans Pro", 0, 14)); // NOI18N
        jLabel1.setText("BEM-VINDO");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jPanel3.setBackground(new java.awt.Color(248, 248, 248));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/troca_filamento_ambos.png"))); // NOI18N

        jLabel3.setFont(new java.awt.Font("Source Sans Pro", 1, 12)); // NOI18N
        jLabel3.setText("Select type");

        jSeparator2.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator2.setForeground(new java.awt.Color(222, 222, 222));
        jSeparator2.setMinimumSize(new java.awt.Dimension(4, 1));
        jSeparator2.setPreferredSize(new java.awt.Dimension(50, 1));

        jComboBox1.setBackground(new java.awt.Color(248, 248, 248));
        jComboBox1.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox1ItemStateChanged(evt);
            }
        });

        lDesc.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        lDesc.setText("<html>Please select the correct code for the filament. You can find that code on the spool, (e.g.: A101 - Transparent).</html>");

        lDescA.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        lDescA.setText("<html> A – External spool holder for all spool weights and all filament types. </html>");

        lDescB.setFont(new java.awt.Font("Source Sans Pro", 0, 12)); // NOI18N
        lDescB.setText("<html>\nB – Internal spool holder for PLA filament and 330g spool only.\n</html>");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 544, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lDescA, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lDescB, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lDescA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(lDescB, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 568, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bNextMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNextMouseEntered
        bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bNextMouseEntered

    private void bNextMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNextMouseExited
        bNext.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bNextMouseExited

    private void bCancelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseEntered
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_21.png")));
    }//GEN-LAST:event_bCancelMouseEntered

    private void bCancelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseExited
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_21.png")));
    }//GEN-LAST:event_bCancelMouseExited

    private void bNextMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bNextMousePressed
        if (bNext.isEnabled()) {
            final Filament fil;

            //fil = ((FilamentComboItem) comboModel.getSelectedItem()).getFilamentObject();
            fil = (Filament) comboModel.getSelectedItem();
            FilamentHeating filamentHeatingPanel = new FilamentHeating(fil);
            dispose();
            filamentHeatingPanel.setVisible(true);
        }
    }//GEN-LAST:event_bNextMousePressed

    private void bCancelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMousePressed
        if (bCancel.isEnabled()) {
            doCancel();
        }
    }//GEN-LAST:event_bCancelMousePressed

    private void bXMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bXMousePressed
        if (bX.isEnabled()) {
            doCancel();
        }
    }//GEN-LAST:event_bXMousePressed

    private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged

        final FilamentComboItem filamentComboItem, tempFilamentComboItem;
        final Filament filamentObject;
        final Nozzle selectedNozzle;
        final Query supportQuery, filamentIncompatibilityQuery;
        final Warning pauseWarning;
        final int selectedIndex;

        if (evt.getStateChange() == ItemEvent.SELECTED) {
            filamentComboItem = (FilamentComboItem) jComboBox1.getSelectedItem();
            filamentObject = filamentComboItem.getFilamentObject();
            selectedIndex = jComboBox1.getSelectedIndex();

            // change image according to the filament's material
            if (filamentObject.getMaterial() == Material.PLA) {
                jLabel2.setIcon(bothSupportImage);
                lDescA.setVisible(true);
                lDescB.setVisible(true);
            } else {
                jLabel2.setIcon(supportImage);
                lDescA.setVisible(false);
                lDescB.setVisible(false);

                if (Boolean.parseBoolean(ProperDefault.get("filament.dont_show_support_query")) == false && this.isVisible()) {
                    supportQuery = new Query("ChangeSupport", new SupportSwitch1(), "filament.dont_show_support_query");
                    supportQuery.setVisible(true);
                }
            }

            // if the selected filament isn't compatible with the current nozzle
            // display a warning informing the user of this fact, and initiate
            // the nozzle switch operation, if that's what the user wants
            if (filamentComboItem.isCompatible() == false) {
                if (Base.printPaused == false) {
                    filamentIncompatibilityQuery = new Query("IncompatibleFilament", new NozzleSwitch4(), null);
                    filamentIncompatibilityQuery.setVisible(true);

                    selectedNozzle = new Nozzle(model.getNozzleType());

                    if (selectedNozzle.equals(currentNozzle)) {
                        jComboBox1.setSelectedIndex(firstCompatibleFilamentIndex);

                    } else {
                        currentNozzle = selectedNozzle;
                        evaluateInitialConditions();
                        tempFilamentComboItem = (FilamentComboItem) jComboBox1.getItemAt(selectedIndex);
                        if (tempFilamentComboItem.isCompatible()) {
                            jComboBox1.setSelectedIndex(selectedIndex);
                        }
                    }
                } else {
                    pauseWarning = new Warning("IncompatibleFilamentPause", false);
                    pauseWarning.setVisible(true);
                    jComboBox1.setSelectedIndex(firstCompatibleFilamentIndex);
                }
            }
        }

    }//GEN-LAST:event_jComboBox1ItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bCancel;
    private javax.swing.JLabel bNext;
    private javax.swing.JLabel bX;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lDesc;
    private javax.swing.JLabel lDescA;
    private javax.swing.JLabel lDescB;
    // End of variables declaration//GEN-END:variables

    private class CustomRenderer extends BasicComboBoxRenderer {

        private static final long serialVersionUID = -1L;
        private final JLabel text;

        public CustomRenderer(JComboBox combo) {
            text = new JLabel();
            text.setOpaque(true);
            text.setFont(combo.getFont());
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            FilamentComboItem filament = (FilamentComboItem) value;

            if (isSelected) {
                setBackground(list.getSelectionBackground());
            } else {
                setBackground(Color.WHITE);
            }

            text.setBackground(getBackground());
            text.setText(filament.toString());
            text.setForeground(Color.GRAY);

            if (filament.isCompatible()) {
                text.setForeground(Color.BLACK);
            }

            return text;
        }

    }

    private class FilamentComboItem {

        private final Filament filamentObject;
        private final boolean compatible;

        public FilamentComboItem(Filament fil) {
            this.filamentObject = fil;
            this.compatible = FilamentControler.isFilamentCompatible(this.filamentObject, currentNozzle);
        }

        public FilamentComboItem(Filament fil, boolean compatible) {
            this.filamentObject = fil;
            this.compatible = compatible;
        }

        public Filament getFilamentObject() {
            return filamentObject;
        }

        public boolean isCompatible() {
            return compatible;
        }

        @Override
        public String toString() {
            return filamentObject.getName();
        }
    }

}
