package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.filechooser.FileNameExtensionFilter;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.app.ui.MainWindow;
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
public class Gallery extends javax.swing.JDialog {

    private JLabel[] modelsPerCategoryList;
    private JLabel[] modelsPerCategoryNames;
    private String modelPressed = "";
    private DefaultListModel listModel;
    private DefaultComboBoxModel comboModel;
    private File[] listExistingModels;
    private int posX = 0, posY = 0;
    private final String modelsDir = Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER;
    private String extension;
    private final MainWindow mWindow;

    public Gallery() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        initCategoriesList();
        initJLabelsArray();
        setFont();
        setTextLanguage();
        centerOnScreen();
        enableDrag();
        mWindow = Base.getMainWindow();
        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());
    }

    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProRegular("14"));
        jLabel3.setFont(GraphicDesignComponents.getSSProLight("33"));
        jLabel4.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel45.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel5.setFont(GraphicDesignComponents.getSSProRegular("14"));
        jLabel6.setFont(GraphicDesignComponents.getSSProRegular("14"));
        jLabel7.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel8.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel9.setFont(GraphicDesignComponents.getSSProRegular("11"));
        jLabel24.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel29.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel27.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel46.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel47.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel48.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel49.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel50.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jTextField1.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jTextField2.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jTextArea1.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jList1.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jComboBox1.setFont(GraphicDesignComponents.getSSProLight("12"));

        setModelsNameFont();

    }

    private void setTextLanguage() {
        jLabel1.setText(Languager.getTagValue(1, "ApplicationMenus", "Gallery").toUpperCase());
        jLabel3.setText(Languager.getTagValue(1, "Gallery", "All"));
        jLabel45.setText(Languager.getTagValue(1, "Gallery", "Model_Rename"));
        jLabel5.setText(Languager.getTagValue(1, "Gallery", "ModelCategory_title"));
        jLabel6.setText(Languager.getTagValue(1, "Gallery", "ModelProperties_title").toUpperCase());
        jLabel7.setText(Languager.getTagValue(1, "Gallery", "Model_Name"));
        jLabel8.setText(Languager.getTagValue(1, "Gallery", "Model_Description"));
        jLabel9.setText(Languager.getTagValue(1, "Gallery", "EraseModel"));
        jLabel24.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line9"));
        jLabel27.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line3"));
        jLabel29.setText(Languager.getTagValue(1, "OptionPaneButtons", "Line8"));
        jTextField1.setText(Languager.getTagValue(1, "Gallery", "SearchModels_text_bar"));

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
    }

    private void setModelsNameFont() {
        for (int i = 0; i < modelsPerCategoryNames.length; i++) {
            modelsPerCategoryNames[i].setFont(GraphicDesignComponents.getSSProRegular("12"));
        }
    }

    private void initCategoriesList() {
        listModel = new DefaultListModel();
        jList1.setModel(listModel);

        String[] categories = Base.getMainWindow().getCategoriesManager().getCategories();

        for (String categorie : categories) {
            listModel.addElement(categorie);
        }

        String[] categories2 = {Languager.getTagValue(1, "Gallery", "All"),
            Languager.getTagValue(1, "Gallery", "Models"),
            Languager.getTagValue(1, "Gallery", "Scene")};
        comboModel = new DefaultComboBoxModel(categories2);
        jComboBox1.setModel(comboModel);
        jComboBox1.setSelectedIndex(0);
        extension = "all";
        enableProperties(false);
    }

    private void initJLabelsArray() {
        modelsPerCategoryList = new JLabel[]{jLabel12, jLabel16, jLabel17,
            jLabel18,
            jLabel21, jLabel22, jLabel23,
            jLabel28};

        modelsPerCategoryNames = new JLabel[]{jLabel31, jLabel32, jLabel33,
            jLabel34,
            jLabel37, jLabel38, jLabel39,
            jLabel40};

        if (Base.getMainWindow().getButtons().areIOFunctionsBlocked() == false) {
            Base.cleanDirectoryTempFiles(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER);
        }
        resetModelsView(1);
        fullfillModelsSlots(1);
    }

    private void resetModelsView(int pageNumber) {
        for (int i = 0; i < modelsPerCategoryList.length; i++) {
            modelsPerCategoryList[i].setVisible(false);
            modelsPerCategoryNames[i].setVisible(false);
        }

        for (int i = 0; i < 6; i++) {
            if (pageNumber == 1) {
                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination_active.png")));
                jLabel46.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel47.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel48.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel49.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel50.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
            } else if (pageNumber == 2) {
                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel46.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination_active.png")));
                jLabel47.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel48.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel49.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel50.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
            } else if (pageNumber == 3) {
                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel46.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel47.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination_active.png")));
                jLabel48.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel49.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel50.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
            } else if (pageNumber == 4) {
                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel46.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel47.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel48.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination_active.png")));
                jLabel49.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel50.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
            } else if (pageNumber == 5) {
                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel46.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel47.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel48.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel49.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination_active.png")));
                jLabel50.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
            } else if (pageNumber == 6) {
                jLabel4.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel46.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel47.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel48.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel49.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination.png")));
                jLabel50.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "pagination_active.png")));
            }
        }
    }

    private void unpickModels(String actualPicked) {
        for (int i = 0; i < modelsPerCategoryList.length; i++) {
            if (!modelsPerCategoryList[i].getAccessibleContext().getAccessibleName().contains(actualPicked)) {
                modelsPerCategoryList[i].setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "cubo.png")));
            } else if (actualPicked.equals("")) {
                modelsPerCategoryList[i].setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "cubo.png")));

            } else {
                modelPressed = modelsPerCategoryNames[i].getText();
            }

        }
    }

    private void pickSearchedModel(String name) {
        for (int i = 0; i < modelsPerCategoryNames.length; i++) {
            if (modelsPerCategoryNames[i].getText().toLowerCase().contains(name.toLowerCase())) {
                modelsPerCategoryList[i].setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "cubo_selected.png")));
                modelPressed = modelsPerCategoryNames[i].getText();
            }
        }
    }

    private void fullfillModelsSlots(int pageNumber) {
        if (extension.equals("all")) {
            listExistingModels = new File(modelsDir).listFiles(new FileFilter() {
                private final FileNameExtensionFilter filter =
                        new FileNameExtensionFilter("BEESOFT files",
                        "stl", "bee");

                @Override
                public boolean accept(File file) {
                    return filter.accept(file);
                }
            });
        } else {
            listExistingModels = evaluateExistingModels(new File(modelsDir), new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(extension);
                }
            }, true);
        }

        int val = 8 * (pageNumber - 1);
        int j = 0;

        for (int i = val; i < listExistingModels.length; i++) {
            if (i != val + 8) {
                String modelName = listExistingModels[i].getName();
                modelsPerCategoryList[j].setVisible(true);
                modelsPerCategoryList[j].setToolTipText(modelName);
                modelsPerCategoryNames[j].setVisible(true);
                modelsPerCategoryNames[j].setText(modelName);
                modelsPerCategoryNames[j].setToolTipText(modelName);
                j++;
            } else {
                break;
            }

        }
    }

    private void searchByPage() {
        int pageNumber = 1;

        for (int i = 0; i < listExistingModels.length; i++) {
            String modelName;

            if (extension.equals("all")) {
                modelName = listExistingModels[i].getName().split("\\.")[0];
            } else {
                modelName = listExistingModels[i].getName().split(extension)[0];
            }

            if (modelName.contains(jTextField1.getText())) {
                if (i / 8 > 0) {
                    pageNumber = i / 8;
                }
                break;
            }
        }
        unpickModels("");
        resetModelsView(pageNumber);
        fullfillModelsSlots(pageNumber);
        pickSearchedModel(jTextField1.getText());
    }

    public static File[] evaluateExistingModels(File directory, FilenameFilter filter, boolean recurse) {
        Collection<File> files = listFiles(directory, filter, recurse);
        File[] arr = new File[files.size()];
        return files.toArray(arr);
    }

    public static Collection<File> listFiles(File directory, FilenameFilter filter, boolean recurse) {
        // List of files / directories
        Vector<File> files = new Vector<File>();

        // Get files / directories in the directory
        File[] entries = directory.listFiles();

        // Go over entries
        for (File entry : entries) {

            // If there is no filter or the filter accepts the 
            // file / directory, add it to the list
            if (filter == null || filter.accept(directory, entry.getName())) {
                files.add(entry);
            }

            // If the file is a directory and the recurse flag
            // is set, recurse into the directory
            if (recurse && entry.isDirectory()) {
                files.addAll(listFiles(entry, filter, recurse));
            }
        }

        // Return collection of files
        return files;
    }

    private void enableDrag() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                posX = e.getX();
                posY = e.getY();
            }
        });


        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent evt) {
                //sets frame position when mouse dragged			
                setLocation(evt.getXOnScreen() - posX, evt.getYOnScreen() - posY);
            }
        });
    }

    public void renameCategory(String newName) {
        int index = jList1.getSelectedIndex();
        String category = String.valueOf(listModel.get(index));
        listModel.remove(index);
        mWindow.getCategoriesManager().remove(category);
        listModel.add(index, newName);
        mWindow.getCategoriesManager().update(newName);
    }

    private void enableProperties(boolean state) {
        jTextArea1.setEditable(state);
        jTextField2.setEditable(state);
    }

    private void doExit() {
        dispose();
        mWindow.getButtons().updatePressedStateButton("models");
        mWindow.setEnabled(true);
        Base.bringAllWindowsToFront();
    }

    private void evaluatePickedModel(int modelID) {

        String fileName = getPickedFileName(modelID);

        if (fileName != null) {
            if (fileName.toLowerCase().contains(".stl")) {
                extension = ".stl";
                jLabel9.setText(Languager.getTagValue(1, "Gallery", "EraseModel"));
                enableProperties(false);
            } else {
                extension = ".bee";
                jLabel9.setText(Languager.getTagValue(1, "Gallery", "EraseScene"));
                enableProperties(true);
            }
        }
    }

    private String getPickedFileName(int ID) {

        if (ID == 12) {
            return modelsPerCategoryNames[0].getText();
        }
        if (ID == 16) {
            return modelsPerCategoryNames[1].getText();
        }
        if (ID == 17) {
            return modelsPerCategoryNames[2].getText();
        }
        if (ID == 18) {
            return modelsPerCategoryNames[3].getText();
        }
        if (ID == 21) {
            return modelsPerCategoryNames[4].getText();
        }
        if (ID == 22) {
            return modelsPerCategoryNames[5].getText();
        }
        if (ID == 23) {
            return modelsPerCategoryNames[6].getText();
        }
        if (ID == 28) {
            return modelsPerCategoryNames[7].getText();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jPanel7 = new javax.swing.JPanel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel45 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel9 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jLabel38 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jPanel5 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setPreferredSize(new java.awt.Dimension(991, 520));
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));

        jLabel1.setText("BIBLIOTECA");

        jPanel6.setBackground(new java.awt.Color(255, 203, 5));
        jPanel6.setMinimumSize(new java.awt.Dimension(62, 30));
        jPanel6.setRequestFocusEnabled(false);

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_9.png"))); // NOI18N
        jLabel15.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel15MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(9, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(240, 243, 244));

        jLabel5.setText("CATEGORIAS");

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jList1);

        jPanel7.setBackground(new java.awt.Color(255, 203, 5));

        jLabel43.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/plus_yellow.png"))); // NOI18N
        jLabel43.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel43.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel43MousePressed(evt);
            }
        });

        jLabel44.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/minus_yellow.png"))); // NOI18N
        jLabel44.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel44.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel44MousePressed(evt);
            }
        });

        jPanel8.setBackground(new java.awt.Color(255, 203, 5));
        jPanel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jPanel8MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jPanel8MouseExited(evt);
            }
        });

        jLabel45.setBackground(new java.awt.Color(255, 255, 255));
        jLabel45.setForeground(new java.awt.Color(255, 255, 255));
        jLabel45.setText("jLabel45");
        jLabel45.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel45MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel45MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel45MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel45)
                .addContainerGap(54, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jLabel45))
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel43, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel44)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel43, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel44, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/plus.png"))); // NOI18N

        jPanel3.setBackground(new java.awt.Color(240, 243, 244));

        jLabel6.setText("PROPRIEDADES");

        jLabel7.setText("Nome");

        jLabel8.setText("Descricao");

        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_8.png"))); // NOI18N
        jLabel9.setText("APAGAR MODELO");
        jLabel9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel9MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel9MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel9MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addGap(5, 5, 5)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(186, 186, 186)));
        jPanel4.setPreferredSize(new java.awt.Dimension(765, 502));

        jLabel3.setText("Modelos");

        jTextField1.setBackground(new java.awt.Color(240, 243, 244));
        jTextField1.setText("Procurar...");
        jTextField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField1FocusGained(evt);
            }
        });
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField1KeyPressed(evt);
            }
        });

        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/search.png"))); // NOI18N
        jLabel11.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel11MousePressed(evt);
            }
        });

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/pagination_active.png"))); // NOI18N
        jLabel4.setText("1");
        jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel4MouseClicked(evt);
            }
        });

        jLabel46.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/pagination.png"))); // NOI18N
        jLabel46.setText("2");
        jLabel46.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel46.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel46MouseClicked(evt);
            }
        });

        jLabel47.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/pagination.png"))); // NOI18N
        jLabel47.setText("3");
        jLabel47.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel47.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel47MouseClicked(evt);
            }
        });

        jLabel48.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/pagination.png"))); // NOI18N
        jLabel48.setText("4");
        jLabel48.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel48.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel48MouseClicked(evt);
            }
        });

        jLabel49.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/pagination.png"))); // NOI18N
        jLabel49.setText("5");
        jLabel49.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel49.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel49MouseClicked(evt);
            }
        });

        jLabel50.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/pagination.png"))); // NOI18N
        jLabel50.setText("6");
        jLabel50.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel50.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel50MouseClicked(evt);
            }
        });

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));
        jPanel10.setMaximumSize(new java.awt.Dimension(739, 307));
        jPanel10.setMinimumSize(new java.awt.Dimension(739, 307));

        jLabel38.setText("Name");

        jLabel23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/cubo.png"))); // NOI18N
        jLabel23.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel23MousePressed(evt);
            }
        });

        jLabel32.setText("Name");

        jLabel28.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/cubo.png"))); // NOI18N
        jLabel28.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel28MousePressed(evt);
            }
        });

        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/cubo.png"))); // NOI18N
        jLabel18.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel18MousePressed(evt);
            }
        });

        jLabel37.setText("Name");

        jLabel33.setText("Name");

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/cubo.png"))); // NOI18N
        jLabel16.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel16MousePressed(evt);
            }
        });

        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/cubo.png"))); // NOI18N
        jLabel21.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel21MousePressed(evt);
            }
        });

        jLabel39.setText("Name");

        jLabel34.setText("Name");

        jLabel22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/cubo.png"))); // NOI18N
        jLabel22.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel22MousePressed(evt);
            }
        });

        jLabel31.setText("Name");

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/cubo.png"))); // NOI18N
        jLabel12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel12MousePressed(evt);
            }
        });

        jLabel40.setText("Name");

        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/cubo.png"))); // NOI18N
        jLabel17.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel17MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel12)
                    .addComponent(jLabel21)
                    .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(70, 70, 70)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel16)
                    .addComponent(jLabel22)
                    .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(70, 70, 70)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel17)
                    .addComponent(jLabel23)
                    .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(70, 70, 70)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel31)
                        .addGap(61, 61, 61)
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel37))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel33)
                        .addGap(61, 61, 61)
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel39))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel32)
                        .addGap(61, 61, 61)
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel38))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel34)
                        .addGap(61, 61, 61)
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel40)))
                .addContainerGap())
        );

        jLabel23.getAccessibleContext().setAccessibleName("jLabel23");
        jLabel28.getAccessibleContext().setAccessibleName("jLabel28");
        jLabel18.getAccessibleContext().setAccessibleName("jLabel18");
        jLabel16.getAccessibleContext().setAccessibleName("jLabel16");
        jLabel21.getAccessibleContext().setAccessibleName("jLabel21");
        jLabel22.getAccessibleContext().setAccessibleName("jLabel22");
        jLabel12.getAccessibleContext().setAccessibleName("jLabel12");
        jLabel17.getAccessibleContext().setAccessibleName("jLabel17");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox1ItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel11))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel46)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel47)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel48)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel49)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel50))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(0, 19, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField1)
                            .addComponent(jComboBox1))
                        .addGap(22, 22, 22)
                        .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel46)
                            .addComponent(jLabel47)
                            .addComponent(jLabel48)
                            .addComponent(jLabel49)
                            .addComponent(jLabel50)))
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addGap(10, 10, 10)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 790, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(255, 203, 5));
        jPanel5.setMinimumSize(new java.awt.Dimension(20, 40));
        jPanel5.setPreferredSize(new java.awt.Dimension(256, 46));

        jLabel24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_13.png"))); // NOI18N
        jLabel24.setText("OK");
        jLabel24.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel24.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel24MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel24MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel24MousePressed(evt);
            }
        });

        jLabel27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_13.png"))); // NOI18N
        jLabel27.setText("CANCELAR");
        jLabel27.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel27.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel27MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel27MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel27MousePressed(evt);
            }
        });

        jLabel29.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_13.png"))); // NOI18N
        jLabel29.setText("APPLY");
        jLabel29.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel29.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel29MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel29MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel29MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel29)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 741, Short.MAX_VALUE)
                .addComponent(jLabel27)
                .addGap(12, 12, 12)
                .addComponent(jLabel24)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(jLabel27)
                    .addComponent(jLabel29))
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 991, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField1FocusGained
        jTextField1.setText("");
    }//GEN-LAST:event_jTextField1FocusGained

    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
//       if (evt.getValueIsAdjusting() == false) 
//        System.out.print(String.valueOf(jList1.getSelectedValue()));
    }//GEN-LAST:event_jList1ValueChanged

    private void jLabel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseClicked
        resetModelsView(1);
        fullfillModelsSlots(1);
    }//GEN-LAST:event_jLabel4MouseClicked

    private void jLabel46MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel46MouseClicked
        resetModelsView(2);
        fullfillModelsSlots(2);
    }//GEN-LAST:event_jLabel46MouseClicked

    private void jLabel47MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel47MouseClicked
        resetModelsView(3);
        fullfillModelsSlots(3);
    }//GEN-LAST:event_jLabel47MouseClicked

    private void jLabel48MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel48MouseClicked
        resetModelsView(4);
        fullfillModelsSlots(4);
    }//GEN-LAST:event_jLabel48MouseClicked

    private void jLabel49MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel49MouseClicked
        resetModelsView(5);
        fullfillModelsSlots(5);
    }//GEN-LAST:event_jLabel49MouseClicked

    private void jLabel50MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel50MouseClicked
        resetModelsView(6);
        fullfillModelsSlots(6);
    }//GEN-LAST:event_jLabel50MouseClicked

    private void jTextField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            searchByPage();
        }
    }//GEN-LAST:event_jTextField1KeyPressed

    private void jLabel24MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel24MouseEntered
        jLabel24.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_13.png")));
    }//GEN-LAST:event_jLabel24MouseEntered

    private void jLabel24MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel24MouseExited
        jLabel24.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_13.png")));
    }//GEN-LAST:event_jLabel24MouseExited

    private void jLabel27MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel27MouseEntered
        jLabel27.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_13.png")));
    }//GEN-LAST:event_jLabel27MouseEntered

    private void jLabel27MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel27MouseExited
        jLabel27.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_13.png")));
    }//GEN-LAST:event_jLabel27MouseExited

    private void jLabel9MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MouseEntered
        jLabel9.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_8.png")));
    }//GEN-LAST:event_jLabel9MouseEntered

    private void jLabel9MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MouseExited
        jLabel9.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_8.png")));
    }//GEN-LAST:event_jLabel9MouseExited

    private void jPanel8MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel8MouseEntered
        jPanel8.setBackground(new Color(187, 187, 187));
    }//GEN-LAST:event_jPanel8MouseEntered

    private void jPanel8MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel8MouseExited
        jPanel8.setBackground(new Color(255, 203, 5));
    }//GEN-LAST:event_jPanel8MouseExited

    private void jLabel45MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel45MouseEntered
        jLabel45.setBackground(new Color(187, 187, 187));
    }//GEN-LAST:event_jLabel45MouseEntered

    private void jLabel45MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel45MouseExited
        jLabel45.setBackground(new Color(255, 203, 5));
    }//GEN-LAST:event_jLabel45MouseExited

    private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged
        if (comboModel.getSelectedItem().equals(Languager.getTagValue(1, "Gallery", "Models"))) {
            jLabel3.setText(Languager.getTagValue(1, "Gallery", "Models"));
            extension = ".stl";
            unpickModels("");
            resetModelsView(1);
            fullfillModelsSlots(1);
            enableProperties(false);
            jLabel9.setText(Languager.getTagValue(1, "Gallery", "EraseModel"));
        } else if (comboModel.getSelectedItem().equals(Languager.getTagValue(1, "Gallery", "Scene"))) {
            jLabel3.setText(Languager.getTagValue(1, "Gallery", "Scene"));
            extension = ".bee";
            unpickModels("");
            resetModelsView(1);
            fullfillModelsSlots(1);
            enableProperties(true);
            jLabel9.setText(Languager.getTagValue(1, "Gallery", "EraseScene"));
        } else if (comboModel.getSelectedItem().equals(Languager.getTagValue(1, "Gallery", "All"))) {
            jLabel3.setText(Languager.getTagValue(1, "Gallery", "All"));
            extension = "all";
            unpickModels("");
            resetModelsView(1);
            fullfillModelsSlots(1);
            enableProperties(false);
        }
    }//GEN-LAST:event_jComboBox1ItemStateChanged

    private void jLabel29MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel29MouseEntered
        jLabel29.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_13.png")));
    }//GEN-LAST:event_jLabel29MouseEntered

    private void jLabel29MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel29MouseExited
        jLabel29.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_13.png")));
    }//GEN-LAST:event_jLabel29MouseExited

    private void jLabel24MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel24MousePressed

        if (extension.equals(".stl") || modelPressed.contains(".stl") || modelPressed.contains(".STL")) {
            if (!modelPressed.equals("")) {
                mWindow.getBed().addSTL(new File(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/" + modelPressed));
                mWindow.getBed().getModels().get(Base.getMainWindow().getBed().getModels().size() - 1).getEditer().centerAndToBed();
//                Base.getMainWindow().getBed().getModels().get(Base.getMainWindow().getBed().getModels().size()-1).getEditer().center();
                mWindow.getCanvas().updateBed(Base.getMainWindow().getBed());
                mWindow.showFeedBackMessage("importModel");
                mWindow.getBed().setGcodeOK(false);
                mWindow.getBed().setSceneDifferent(true);
            }
        } else if (extension.equals(".bee") || modelPressed.contains(".bee")) {
            if (!modelPressed.equals("")) {
                mWindow.handleOpenScene(new File(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER + "/" + modelPressed).getAbsolutePath());
                mWindow.getCanvas().updateBed(Base.getMainWindow().getBed());
                mWindow.showFeedBackMessage("loadScene");
            }
        }

        mWindow.getButtons().updatePressedStateButton("models");
        mWindow.setEnabled(true);
        
        //Selects the inserted model
        mWindow.selectLastInsertedModel();
        
        Base.bringAllWindowsToFront();
        dispose();
    }//GEN-LAST:event_jLabel24MousePressed

    private void jLabel29MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel29MousePressed
        if (!modelPressed.equals("") && modelPressed.contains(".bee")) {
//             System.out.println(modelPressed);
            ObjectInputStream ois;
            ObjectOutputStream oos;

            try {
                ois = new ObjectInputStream(new FileInputStream(modelsDir + "/" + modelPressed));
                PrintBed bed = (PrintBed) ois.readObject();

                if (!jTextField2.getText().isEmpty()) {
                    bed.setName(jTextField2.getText());
                }
                if (!jTextArea1.getText().isEmpty()) {
                    bed.setDescription(jTextArea1.getText());
                }

                ois.close();

                if (mWindow.getBed().getPrintBedFile().getName().equals(modelPressed)) {
                    mWindow.getCanvas().updateBed(bed);
                }

                oos = new ObjectOutputStream(new FileOutputStream(new File(modelsDir + "/" + modelPressed)));
                oos.writeObject(bed);
                oos.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        mWindow.getButtons().updatePressedStateButton("models");
        mWindow.setEnabled(true);
        dispose();
        Base.bringAllWindowsToFront();
    }//GEN-LAST:event_jLabel29MousePressed

    private void jLabel45MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel45MousePressed
        InputDataReader p = new InputDataReader(this);
        p.setVisible(true);
    }//GEN-LAST:event_jLabel45MousePressed

    private void jLabel44MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel44MousePressed
        int index = jList1.getSelectedIndex();
        String category = String.valueOf(listModel.get(index));
        listModel.remove(index);
        mWindow.getCategoriesManager().remove(category);
    }//GEN-LAST:event_jLabel44MousePressed

    private void jLabel43MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel43MousePressed
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date date = new Date();

        String newOne = "CT" + dateFormat.format(date);
        listModel.addElement(newOne);
        mWindow.getCategoriesManager().update(newOne);
    }//GEN-LAST:event_jLabel43MousePressed

    private void jLabel11MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel11MousePressed
        searchByPage();
    }//GEN-LAST:event_jLabel11MousePressed

    private void jLabel9MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MousePressed

        File fileToDelete = null;

        if (!modelPressed.equals("")) {
            fileToDelete = new File(modelsDir + "/" + modelPressed);
        }

        if (fileToDelete != null && !mWindow.getBed().getPrintBedFile().getName().equals(modelPressed)) {
            fileToDelete.delete();
            resetModelsView(1);
            fullfillModelsSlots(1);
            unpickModels(modelPressed);
            modelPressed = "";
        } else {
            modelPressed = "";
        }
    }//GEN-LAST:event_jLabel9MousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        doExit();
    }//GEN-LAST:event_jLabel15MousePressed

    private void jLabel12MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel12MousePressed
        jLabel12.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "cubo_selected.png")));
        unpickModels("12");
        evaluatePickedModel(12);
    }//GEN-LAST:event_jLabel12MousePressed

    private void jLabel16MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel16MousePressed
        jLabel16.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "cubo_selected.png")));
        unpickModels("16");
        evaluatePickedModel(16);
    }//GEN-LAST:event_jLabel16MousePressed

    private void jLabel17MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel17MousePressed
        jLabel17.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "cubo_selected.png")));
        unpickModels("17");
        evaluatePickedModel(17);
    }//GEN-LAST:event_jLabel17MousePressed

    private void jLabel18MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel18MousePressed
        jLabel18.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "cubo_selected.png")));
        unpickModels("18");
        evaluatePickedModel(18);
    }//GEN-LAST:event_jLabel18MousePressed

    private void jLabel21MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel21MousePressed
        jLabel21.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "cubo_selected.png")));
        unpickModels("21");
        evaluatePickedModel(21);
    }//GEN-LAST:event_jLabel21MousePressed

    private void jLabel22MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel22MousePressed
        jLabel22.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "cubo_selected.png")));
        unpickModels("22");
        evaluatePickedModel(22);
    }//GEN-LAST:event_jLabel22MousePressed

    private void jLabel23MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel23MousePressed
        jLabel23.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "cubo_selected.png")));
        unpickModels("23");
        evaluatePickedModel(23);
    }//GEN-LAST:event_jLabel23MousePressed

    private void jLabel28MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel28MousePressed
        jLabel28.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "cubo_selected.png")));
        unpickModels("28");
        evaluatePickedModel(28);
    }//GEN-LAST:event_jLabel28MousePressed

    private void jLabel27MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel27MousePressed
        doExit();
    }//GEN-LAST:event_jLabel27MousePressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}
