/*
 Part of the ReplicatorG project - http://www.replicat.org
 Copyright (c) 2008 Zach Smith

 Forked from Arduino: http://www.arduino.cc

 Based on Processing http://www.processing.org
 Copyright (c) 2004-05 Ben Fry and Casey Reas
 Copyright (c) 2001-04 Massachusetts Institute of Technology
 Copyright (c) 2013 BEEVC - Electronic Systems
 Caixa de entrada - mdomingues@bitbox.pt - bitBOX - Electronic Systems, Lda. Correio
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 $Id: MainWindow.java 370 2008-01-19 16:37:19Z mellis $
 */
package replicatorg.app.ui;

import replicatorg.app.ui.panels.UpdateChecker;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import net.iharder.dnd.FileDrop;
import net.miginfocom.swing.MigLayout;
import replicatorg.app.Base;
import replicatorg.app.Base.InitialOpenBehavior;
import replicatorg.app.MRUList;
import replicatorg.machine.MachineInterface;
import replicatorg.machine.MachineLoader;
import com.apple.mrj.MRJQuitHandler;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.logging.Logger;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
import replicatorg.app.CategoriesList;
import pt.beeverycreative.beesoft.filaments.PrintPreferences;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.mainWindow.*;
import replicatorg.app.ui.panels.*;
import replicatorg.app.util.ExtensionFilter;
import replicatorg.drivers.Driver;
import replicatorg.model.CAMPanel;
import replicatorg.model.Model;
import replicatorg.model.PrintBed;
import replicatorg.util.UnitsAndNumbers;

/*
 *  Copyright (c) 2013 BEEVC - Electronic Systems
 */
public final class MainWindow extends JFrame implements MRJQuitHandler, WindowListener {

    private final MachineLoader machineLoader = Base.getMachineLoader();
    private final JPanel cardPanel = new JPanel(new BorderLayout());
    private final ButtonsPanel buttons = new ButtonsPanel(this);
    private final MRUList mruList = MRUList.getMRUList();
    private final CategoriesList categoriesList = CategoriesList.getMRUList();
    private final MessagesPopUp messagesPP = new MessagesPopUp();
    private boolean oktoGoOnSave = false, newSceneOnDialog = false;
    private SceneDetailsPanel sceneDP;
    private CAMPanel canvas;
    private PrintBed bed;
    private String handleOpenPath;

    public MainWindow() {
        super("BEESOFT");
        final Container pane;
        //noinspection unused
        final FileDrop fileDrop;

        super.setPreferredSize(new Dimension(1000, 650));
        super.setMinimumSize(new Dimension(1000, 650));
        //super.setFocusable(true);
        super.setName("mainWindow");
        //super.setFocusableWindowState(true);
        super.setBackground(new Color(255, 255, 255));
        super.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        super.setIconImage(Base.BEESOFT_ICON);
        setAlwaysOnTop(false);
        setFocusCycleRoot(true);
        bed = PrintBed.makePrintBed(null);
        cardPanel.setBackground(new Color(255, 255, 255));
        messagesPP.setVisible(false);

        super.getContentPane().addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateSizeVariables(e.getComponent().getWidth(), e.getComponent().getHeight());
                MainWindow.this.setSize(MainWindow.this.getWidth() + 1, MainWindow.this.getHeight());
                MainWindow.this.setSize(MainWindow.this.getWidth() - 1, MainWindow.this.getHeight());
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

        // add listener to handle window close box hit event
        super.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleQuitInternal();
            }
        });

        if (Boolean.valueOf(ProperDefault.get("firstTime"))) {
            File model = new File(Base.getApplicationDirectory() + "/" + Base.MODELS_FOLDER + "/BEE.stl");
            if (model.exists() && model.canRead() && model.isFile()) {
                bed.addSTL(model);
                bed.setSceneDifferent(true);
            } else {
                model = new File(Base.getApplicationDirectory() + "/" + Base.MODELS_FOLDER + "/BEE.STL");
                if (model.exists() && model.canRead() && model.isFile()) {
                    if (Base.getMainWindow() != null) {
                        bed.addSTL(model);
                        bed.setSceneDifferent(true);
                    }
                } //no need for else {}

            }
            setOktoGoOnSave(false);
        }
        JMenuBar menubar = new JMenuBar();
        menubar.setBackground(new Color(0xf0, 0xf3, 0xf4));

        menubar.add(buildFileMenu());
        menubar.add(buildEditMenu());
        menubar.add(buildPrinterMenu());
        menubar.add(buildHelpMenu());
        super.setJMenuBar(menubar);

        pane = super.getContentPane();
        MigLayout layout = new MigLayout("nocache,fill,flowy,gap 0 0,ins 0");
        pane.setLayout(layout);
        pane.add(buttons, "growx,dock north");

        //noinspection UnusedAssignment
        fileDrop = new FileDrop(null, cardPanel, (java.io.File[] files) -> {
            bed.addSTL(files[0]);
            bed.setSceneDifferent(true);
            Model m = bed.getModels().get(bed.getModels().size() - 1);
            m.getEditer().centerAndToBed();
            canvas.updateBedImportedModels(bed);

            //Selects the inserted model
            selectLastInsertedModel();
        });

//        splitPane.setResizeWeight(0.86);
        pane.add(cardPanel, "growx,growy,shrinkx,shrinky");
        super.setLocationRelativeTo(null);
        super.pack();
        super.addWindowListener(MainWindow.this);
        // Have UI elements listen to machine state.
        //machineLoader.addMachineListener(this);
        //machineLoader.addMachineListener(machineStatusPanel);
    }

    public PrintBed getBed() {
        return bed;
    }

    public ButtonsPanel getButtons() {
        return buttons;
    }

    public void setOktoGoOnSave(boolean oktoGoOnSave) {
        this.oktoGoOnSave = oktoGoOnSave;
    }

    public boolean isOkToGoOnSave() {
        return this.oktoGoOnSave;
    }

    public CategoriesList getCategoriesManager() {
        return categoriesList;
    }

    public boolean validatePrintConditions() {
        for (int i = 0; i < bed.getModels().size(); i++) {
            if (bed.getModels().get(i).getEditer().modelInvalidPosition()) {
                showFeedBackMessage("MessageOutOfBounds");
                return false;
            }
        }
        return true;
    }

    public void updateModelsOperationCenter(JPanel newMOC) {
        cardPanel.remove(((BorderLayout) cardPanel.getLayout()).getLayoutComponent(BorderLayout.WEST));
        cardPanel.add(newMOC, BorderLayout.WEST);
        cardPanel.validate();

        java.awt.EventQueue.invokeLater(() -> {
            if (Base.getMainWindow().isActive()) {
                setVisible(true);
                toFront();
                requestFocus();
                repaint();
            }
        });
    }

    public void updateDetailsCenter(JPanel newDC) {
        cardPanel.remove(((BorderLayout) cardPanel.getLayout()).getLayoutComponent(BorderLayout.EAST));
        cardPanel.add(newDC, BorderLayout.EAST);
        cardPanel.validate();

        java.awt.EventQueue.invokeLater(() -> {
            if (Base.getMainWindow().isActive()) {
                setVisible(true);
                toFront();
                requestFocus();
                repaint();
            }
        });

    }

    public void showFeedBackMessage(String message) {
        if (!messagesPP.isVisible()) {
            messagesPP.setMessage(message);
        }
    }

    private CAMPanel getPreviewPanel() {
        if (canvas == null) {
            canvas = new CAMPanel(cardPanel, bed);
            canvas.updateBed(bed);
            sceneDP = new SceneDetailsPanel();
            cardPanel.add(new SceneDetailsPanel(), BorderLayout.EAST);
            cardPanel.add(new ModelsOperationCenter(), BorderLayout.WEST);
            cardPanel.add(canvas.getPanel(), BorderLayout.CENTER);
            sceneDP.updateBed(bed);
        }
        return canvas;
    }

    public CAMPanel getCanvas() {
        return canvas;
    }

    public void setCPVisibility(boolean visibility) {
        JMenu menu;
        JMenuItem menuItem;
        String name;
        for (Component c : this.getJMenuBar().getComponents()) {
            menu = (JMenu) c;
            name = menu.getText();
            if (name.equalsIgnoreCase("Printer")) {
                for (int i = 0; i < menu.getItemCount(); ++i) {
                    menuItem = menu.getItem(i);
                    if (menuItem != null) {
                        name = menuItem.getText();
                        if (name.equalsIgnoreCase("Control Panel")) {
                            menuItem.setVisible(visibility);
                            break;
                        }
                    }
                }

            }
        }
    }

    private void updateSizeVariables(int wth, int hth) {
        Dimension d = this.getSize();
        Dimension minD = this.getMinimumSize();
        if (d.width < minD.width) {
            d.width = minD.width;
        }
        if (d.height < minD.height) {
            d.height = minD.height;
        }
        this.setSize(d);
        cardPanel.setSize(wth, hth);
        //canvas.getPanel().setSize(this.realWidth - 200 - 265, this.realHeight);
        if (canvas != null) {
            canvas.setCanvasSize(new Dimension(wth, hth)); //cardPanel.getSize()
        }
    }

    /**
     * Post-constructor setup for the editor area. Loads the last sketch that
     * was used (if any), and restores other MainWindow settings. The complement
     * to "storePreferences", this is called when the application is first
     * launched.
     */
    public void restorePreferences() {

        if (Base.openedAtStartup != null) {
            handleOpen2Scene(Base.openedAtStartup);
        } else {
            // last sketch that was in use, or used to launch the app
            final String prefName = "replicatorg.initialopenbehavior";
            int ordinal = Integer.valueOf(ProperDefault.get(prefName));
            final InitialOpenBehavior openBehavior = InitialOpenBehavior
                    .values()[ordinal];
            if (openBehavior == InitialOpenBehavior.OPEN_NEW) {
                handleNew();
            } else {
                // Get last path opened; MRU keeps this.
                Iterator<String> i = mruList.iterator();
                if (i.hasNext()) {
                    String lastOpened = i.next();
                    if (new File(lastOpened).exists()) {
                        handleOpen2Scene(lastOpened);
                    }
                } else {
                    handleNew();
                    File model = new File(Base.getApplicationDirectory() + "/" + Base.MODELS_FOLDER + "/BEE.stl");
                    if (model.exists() && model.canRead() && model.isFile()) {
                        bed.addSTL(model);
                        bed.setSceneDifferent(true);
                    } else {
                        model = new File(Base.getApplicationDirectory() + "/" + Base.MODELS_FOLDER + "/BEE.STL");
                        if (model.exists() && model.canRead() && model.isFile()) {
                            bed.addSTL(model);
                            bed.setSceneDifferent(true);
                        } //no need for else {}

                    }
                }
            }
        }
    }

    /**
     * Store preferences about the editor's current state. Called when the
     * application is quitting.
     */
    private void storePreferences() {

        // window location information
        Rectangle bounds = getBounds();
        ProperDefault.put("last.window.x", String.valueOf(bounds.x));
        ProperDefault.put("last.window.y", String.valueOf(bounds.y));
        ProperDefault.put("last.window.width", String.valueOf(this.getWidth()));
        ProperDefault.put("last.window.height", String.valueOf(this.getHeight()));

        if (handleOpenPath != null) {
            ProperDefault.put("last.sketch.path", handleOpenPath);
            mruList.update(handleOpenPath);
        }

    }

    private JMenu buildFileMenu() {
        JMenuItem item;
        JMenu menu = new JMenu("File");
        menu.setIcon(GraphicDesignComponents.getMenuItemIcon());
        menu.setFont(GraphicDesignComponents.getSSProLight("13"));
        menu.setText(Languager.getTagValue("ApplicationMenus", "File"));

        item = newJMenuItem("New Scene", 'N');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "File_New"));

        item.addActionListener((ActionEvent e) -> {
            if (bed.isSceneDifferent() && (!oktoGoOnSave)) {
                int answer;
                answer = JOptionPane.showConfirmDialog(null,
                        Languager.getTagValue("ToolPath", "Line6") + "\n" + Languager.getTagValue("ToolPath", "Line7"),
                        Languager.getTagValue("ToolPath", "Line8"), 0, 0);
                if (answer == JOptionPane.YES_OPTION) {
                    if (bed.isSceneDifferent()) {
                        newSceneOnDialog = true;
                        handleSaveAs();
                        bed.setSceneDifferent(false);
                        updateModelsOperationCenter(new ModelsOperationCenter());
                    }
                } else if (answer == JOptionPane.NO_OPTION) {
                    handleNew();
                    updateModelsOperationCenter(new ModelsOperationCenter());
                }

            } else {
                handleNew();
                updateModelsOperationCenter(new ModelsOperationCenter());
            }
        });
        menu.add(item);

        //
        // File menu Work Area section
        //
        item = newJMenuItem("Open Scene...", 'O', false);
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "File_Open"));

        item.addActionListener((ActionEvent e) -> {
            if (bed.isSceneDifferent() && (!oktoGoOnSave)) {
                int answer;
                answer = JOptionPane.showConfirmDialog(null,
                        Languager.getTagValue("ToolPath", "Line6") + "\n" + Languager.getTagValue("ToolPath", "Line7"),
                        Languager.getTagValue("ToolPath", "Line8"), 0, 0);
                if (answer == JOptionPane.YES_OPTION) {
                    if (bed.isSceneDifferent()) {
                        handleSaveAs();
                        handleOpenScene(null);
                        bed.setSceneDifferent(false);
                        updateModelsOperationCenter(new ModelsOperationCenter());
                    }
                } else if (answer == JOptionPane.NO_OPTION) {
                    handleOpenScene(null);
                }

            } else {
                handleOpenScene(null);
            }
        });

        menu.add(item);

        item = newJMenuItem("Save Scene", 'S');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "File_Save"));
        item.addActionListener((ActionEvent e) -> handleSave(false));
        menu.add(item);

        item = newJMenuItem("Save As...", 'S', true);
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "File_Save_as"));
        item.addActionListener((ActionEvent e) -> handleSaveAs());
        menu.add(item);

        menu.addSeparator();

        //
        // File menu Import and Export section
        //
        item = newJMenuItem("Import Model ", 'I');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "Model_Import"));
        item.addActionListener((ActionEvent e) -> handleNewModel());
        menu.add(item);

        item = newJMenuItem("Export G-code file", 'E');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "GCode_Export"));
        item.addActionListener((ActionEvent e) -> {
            MachineInterface machine;
            MainWindow editor;

            machine = Base.getMainWindow().getMachineInterface();
            editor = Base.getMainWindow();

            if (machine.getDriver().getMachine().getMachineBusy()) {
                editor.showFeedBackMessage("moving");
            } else//&& Base.isPrinting == false
            {
                if (editor.validatePrintConditions()
                        || Boolean.valueOf(ProperDefault.get("localPrint"))) {
                    PrintPanel p = new PrintPanel();
                    p.setVisible(true);
                }
            }
        });
        menu.add(item);

        item = newJMenuItem("Print G-code file", 'G');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "GCode_Import"));
        item.addActionListener((ActionEvent e) -> {
            final Driver driver = getMachineInterface().getDriver();
            final String filamentCode = driver.getMachine().getCoilText();

            if (Base.isPrinting) {
                Base.getMainWindow().showFeedBackMessage("btfPrinting");
            } else if (!driver.isInitialized()) {
                Base.getMainWindow().showFeedBackMessage("btfDisconnect");
            } else if (filamentCode.equals(FilamentControler.NO_FILAMENT)
                    || filamentCode.equals(FilamentControler.NO_FILAMENT_2)
                    || !FilamentControler.colorExistsLocally(filamentCode)) {
                Base.getMainWindow().showFeedBackMessage("unknownColor");
            } else {
                handleGCodeImport();
            }
        });
        menu.add(item);

        //
        // File menu Preferences section
        //
        menu.addSeparator();

        item = newJMenuItem("Settings...", 'P', true);
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "File_Preferences"));

        item.addActionListener((ActionEvent e) -> new PreferencesPanel().setVisible(true));
        menu.add(item);

//
        // File menu Quit section
        //
        menu.addSeparator();

        item = newJMenuItem("Quit", 'Q', true);
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "File_Quit"));
        item.addActionListener((ActionEvent e) -> handleQuitInternal());
        menu.add(item);
        return menu;
    }

    private JMenu buildEditMenu() {
        JMenu menu = new JMenu("Edit");
        menu.setIcon(GraphicDesignComponents.getMenuItemIcon());
        menu.setFont(GraphicDesignComponents.getSSProLight("13"));
        menu.setText(Languager.getTagValue("ApplicationMenus", "Edit"));

        JMenuItem item;

        item = newJMenuItem("Undo", 'Z');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "Edit_Undo"));
        item.addActionListener((ActionEvent e) -> bed.undoTransformation());

//        menu.add(item);
        item = newJMenuItem("Redo", 'Y');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "Edit_Redo"));
        item.addActionListener((ActionEvent e) -> bed.redoTransformation());
//        menu.add(item);

//        menu.addSeparator();
        item = newJMenuItem("Duplicate", 'V');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "Edit_Duplicate"));
        item.addActionListener((ActionEvent e) -> {
            if (bed.getPickedModels().size() > 0) {
                bed.duplicateModel();
                bed.setSceneDifferent(true);
                canvas.updateBedImportedModels(bed);
                sceneDP.updateBedInfo();
            }
        });
//        menu.add(item);

        item = newJMenuItem("Delete", 'D');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "Edit_Delete"));
        item.addActionListener((ActionEvent e) -> handleCAMDelete());
        menu.add(item);
        menu.addSeparator();

        item = newJMenuItem("Select all", 'A');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "Edit_SelectAll"));
        item.addActionListener((ActionEvent e) -> {
            canvas.pickAll();
            ModelsDetailsPanel mdp = new ModelsDetailsPanel();
            mdp.updateBed(bed);
            updateDetailsCenter(mdp);
        });
//        menu.add(item);

        item = newJMenuItem("Unselect", 'Z', true);
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "Edit_Unselect"));
        item.addActionListener((ActionEvent e) -> {
            canvas.unPickAll();
            SceneDetailsPanel sdp = new SceneDetailsPanel();
            sdp.updateBed(bed);
            updateDetailsCenter(sdp);
        });
        menu.add(item);
        menu.addSeparator();

        item = newJMenuItem("Put on Platform", 'L');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "Edit_PutPlatform"));
        item.addActionListener((ActionEvent e) -> {
            if (bed.getPickedModels().size() > 0) {
                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().putOnPlatform();
                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().updateModelPicked();
            }
        });
        menu.add(item);

        item = newJMenuItem("Center in Platform", 'C');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "Edit_Center"));
        item.addActionListener((ActionEvent e) -> {
            if (bed.getPickedModels().size() > 0) {
                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().center();
                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().updateModelPicked();
            }
        });
        menu.add(item);

        item = newJMenuItem("Reset Original Position", 'R');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "Edit_Reset"));
        item.addActionListener((ActionEvent e) -> {
            if (bed.getPickedModels().size() > 0) {
                bed.resetTransformation();
                Model model = Base.getMainWindow().getBed().getFirstPickedModel();
                ModelsOperationCenterScale mOCS = Base.getMainWindow().getCanvas().getControlTool(3).getModelsScaleCenter();
                if (mOCS != null) {
                    model.resetScale();
                    DecimalFormat df = new DecimalFormat("#.00");
                    double width1 = model.getEditer().getWidth();
                    if (ProperDefault.get("measures").equals("inches")) {
                        width1 = UnitsAndNumbers.millimetersToInches(width1);
                    }
                    double depth = model.getEditer().getDepth();
                    if (ProperDefault.get("measures").equals("inches")) {
                        depth = UnitsAndNumbers.millimetersToInches(depth);
                    }
                    double height1 = model.getEditer().getHeight();
                    if (ProperDefault.get("measures").equals("inches")) {
                        height1 = UnitsAndNumbers.millimetersToInches(height1);
                    }
                    mOCS.setXValue(df.format(width1));
                    mOCS.setYValue(df.format(depth));
                    mOCS.setZValue(df.format(height1));
                }
                model.getEditer().updateModelPicked();
            }
//                canvas.resetView();
        });
        menu.add(item);

        return menu;
    }

    private JMenu buildPrinterMenu() {
        boolean enableCP;
        JMenuItem item;
        JMenu menu = new JMenu("Printer");
        menu.setIcon(GraphicDesignComponents.getMenuItemIcon());
        menu.setFont(GraphicDesignComponents.getSSProLight("13"));
        menu.setText(Languager.getTagValue("ApplicationMenus", "Printer"));
        enableCP = Boolean.parseBoolean(Base.readConfig("controlpanel.enable"));

        item = newJMenuItem("Maintenance", 'M');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "Printer_Maintenance"));
        item.addActionListener((ActionEvent e) -> {
            if (machineLoader.isConnected()) {
                if (!Base.isPrinting) {
                    Maintenance p = new Maintenance();
                    p.setVisible(true);
                }
            } else {
                showFeedBackMessage("btfDisconnect");
            }
        });
        menu.add(item);

        item = newJMenuItem("Control Panel", 'K');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "Printer_ControlPanel"));
        item.addActionListener((ActionEvent e) -> {
            if (machineLoader.isConnected()) {
                if (!Base.isPrinting) {
                    ControlPanel cp = new ControlPanel();
                    cp.setVisible(true);
                }
            } else {
                showFeedBackMessage("btfDisconnect");
            }
        });
        menu.add(item);
        item.setVisible(enableCP);

        menu.addSeparator();

        item = newJMenuItem("Print ", 'P');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "Printer_Print"));
        item.addActionListener((ActionEvent e) -> {
            final MachineInterface machine;
            final PrintPanel printPanel;

            machine = getMachineInterface();

            if (!machine.isConnected()) {
                showFeedBackMessage("btfDisconnect");
            } else if (!machine.getDriver().getMachineReady() && machine.getDriver().isBusy()) {
                showFeedBackMessage("moving");
            } else if (validatePrintConditions() && Base.getMainWindow().getBed().getNumberModels() > 0
                    || Boolean.valueOf(ProperDefault.get("localPrint"))) {
                printPanel = new PrintPanel();
                printPanel.setVisible(true);
            }
        });
        menu.add(item);

        return menu;
    }

    private JMenu buildHelpMenu() {
        JMenuItem item;
        JMenu menu = new JMenu("Help");
        menu.setIcon(GraphicDesignComponents.getMenuItemIcon());
        menu.setFont(GraphicDesignComponents.getSSProLight("13"));
        menu.setText(Languager.getTagValue("ApplicationMenus", "Help"));

        item = newJMenuItem("FAQ", 'F', true);
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("Help", "FAQ"));
        item.addActionListener((ActionEvent e) -> launchBrowser("https://beeverycreative.com/faq/"));
        menu.add(item);

        item = newJMenuItem("Troubleshooting", 'T', true);
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("Help", "Troubleshooting"));
        item.addActionListener((ActionEvent e) -> launchBrowser("https://beeverycreative.com/troubleshooting/"));
        menu.add(item);

        item = newJMenuItem("Quick Guide ", 'Q');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "Help_QuickGuide"));
        item.addActionListener((ActionEvent e) -> {
            Base.writeLog("BEESOFT tour loaded ... ", this.getClass());

            TourWelcome p = new TourWelcome();
            p.setVisible(true);
        });
        menu.add(item);
        menu.addSeparator();

        item = newJMenuItem("Check for Updates ", 'U');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "Help_Update"));
        item.addActionListener((ActionEvent e) -> new UpdateChecker().setVisible(true));
        menu.add(item);
        menu.addSeparator();

        item = newJMenuItem("About ", 'K');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue("ApplicationMenus", "Help_About"));
        item.addActionListener((ActionEvent e) -> new About().setVisible(true));
        menu.add(item);

        return menu;
    }

    private void launchBrowser(String url) {
        if (java.awt.Desktop.isDesktopSupported()) {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } catch (IOException | java.net.URISyntaxException e) {
                // do nothing
            }
        }
    }

    /**
     * Convenience method, see below.
     *
     * @param title title of the JMenuItem
     * @param what shortcut of the JMenuItem
     * @return new JMenuItem object
     */
    private static JMenuItem newJMenuItem(String title, int what) {
        return newJMenuItem(title, what, false);
    }

    /**
     * A software engineer, somewhere, needs to have his abstraction taken away.
     * In some countries they jail or beat people for writing the sort of API
     * that would require a five line helper function just to set the command
     * key for a menu item.
     *
     * @param title title of the JMenuItem
     * @param what shortcut of the JMenuItem
     * @param shift does the shortcut require a pressed shift button
     * @return new JMenuItem object
     */
    private static JMenuItem newJMenuItem(String title, int what, boolean shift) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.setFont(new Font("Source Sans Pro", Font.PLAIN, 13));
        menuItem.setForeground(new Color(35, 31, 32));

        int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        if (shift) {
            modifiers |= ActionEvent.SHIFT_MASK;
        }
        menuItem.setAccelerator(KeyStroke.getKeyStroke(what, modifiers));
        return menuItem;
    }

    public void handleCAMDelete() {
        if (bed.getPickedModels().size() > 0) {
            canvas.updateBedDeletedModels(bed);
            bed.removeModel();
            bed.setSceneDifferent(true);
            sceneDP.updateBedInfo();
            sceneDP = new SceneDetailsPanel();
            sceneDP.updateBed(bed);
            sceneDP.updateBedInfo();
            updateDetailsCenter(sceneDP);
            Base.getMainWindow().getBed().setGcodeOK(false);
            updateModelsOperationCenter(new ModelsOperationCenter());
        }
    }

    public MachineInterface getMachineInterface() {
        return this.machineLoader.getMachineInterface();
    }

    /**
     * New scene with BEE default model if first time running app.
     *
     */
    private void handleNew() {

        SwingUtilities.invokeLater(() -> {
            bed = PrintBed.makePrintBed(null);
            canvas.updateBed(bed);
            sceneDP = new SceneDetailsPanel();
            updateDetailsCenter(sceneDP);
            sceneDP.updateBed(bed);
        });
    }

    /**
     * Add new model to scene
     */
    public void handleNewModel() {
        Base.writeLog("Opening model ...", this.getClass());
        SwingUtilities.invokeLater(() -> {
            final String path;

            path = selectFile(0);

            if (path != null) {
                handleNewModel(new File(path));
            }
        });
    }

    public void handleNewModel(final File model) {
        Base.writeLog("Loading " + model.getAbsolutePath() + " ...", this.getClass());

        bed.addSTL(model);
        Model m = bed.getModels().get(bed.getModels().size() - 1);
        m.getEditer().centerAndToBed();
        sceneDP.updateBedInfo();
        canvas.updateBedImportedModels(bed);
        showFeedBackMessage("importModel");
        Base.getMainWindow().getBed().setGcodeOK(false);
        bed.setSceneDifferent(true);
        oktoGoOnSave = false;

        //Selects the last inserted model
        Base.getMainWindow().selectLastInsertedModel();
    }

    public void removeAllModels() {
        bed.removeAllModels();
        sceneDP.updateBedInfo();
        canvas.updateBedDeletedModels(bed);
        canvas.rebuildScene();
    }

    /**
     * Prints a G-code file
     */
    private void handleGCodeImport() {
        Base.writeLog("Importing G-code file ...", this.getClass());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final String path;
                path = selectFile(2);
                if (path != null) {
                    //Base.logger.log(Level.INFO, "Loading {0}", path);
                    Base.writeLog("Loading " + path + " ...", this.getClass());

                    //Adds default print preferences, they aren't going to be used
                    //since we're printing from a GCode file
                    PrintPreferences prefs = new PrintPreferences(path);

                    Base.isPrintingFromGCode = true;
                    final PrintSplashAutonomous p = new PrintSplashAutonomous(prefs);
                    EventQueue.invokeLater(() -> {
                        if (Base.getMainWindow().getBed().isSceneDifferent()) {
                            Base.getMainWindow().handleSave(true);
                        }
                    });
                    p.setVisible(true);
                }
            }
        });
    }

    /**
     * Select File to open. It can have three types
     *
     * @param opt 0 for STL files, 1 for BEE files and 2 for G-code files
     * @return path to file
     */
    private String selectFile(int opt) {
        final int rv;
        final String loadDir;
        final File directory;
        final JFileChooser fc;
        final FileFilter defaultFilter;

        // Opens at last directory
        if (opt == 0) {
            loadDir = Base.getAppDataDirectory().getAbsolutePath() + "/3DModels";
        } else {
            loadDir = ProperDefault.get("ui.open_dir");
        }

        if (loadDir != null) {
            directory = new File(loadDir);

            fc = new JFileChooser(directory);

            switch (opt) {
                case 0:
                    fc.addChoosableFileFilter(defaultFilter = new ExtensionFilter(".stl", "STL files"));

                    fc.setAcceptAllFileFilterUsed(true);
                    fc.setFileFilter(defaultFilter);
                    fc.setDialogTitle("Open a model file...");
                    fc.setDialogType(JFileChooser.OPEN_DIALOG);
                    fc.setFileHidingEnabled(false);
                    rv = fc.showOpenDialog(this);
                    if (rv == JFileChooser.APPROVE_OPTION) {
                        ProperDefault.put("ui.open_dir0", fc.getCurrentDirectory().getAbsolutePath());
                        Base.getMainWindow().getButtons().updatePressedStateButton("models");
                        return fc.getSelectedFile().getAbsolutePath();
                    } else {
                        Base.getMainWindow().getButtons().updatePressedStateButton("models");
                        return null;
                    }

                case 1:
                    fc.addChoosableFileFilter(defaultFilter = new ExtensionFilter(".bee", "BEE files"));

                    fc.setAcceptAllFileFilterUsed(true);
                    fc.setFileFilter(defaultFilter);
                    fc.setCurrentDirectory(new File(loadDir));
                    fc.setDialogTitle("Open a scene file...");
                    fc.setDialogType(JFileChooser.OPEN_DIALOG);
                    fc.setFileHidingEnabled(false);
                    rv = fc.showOpenDialog(this);
                    if (rv == JFileChooser.APPROVE_OPTION) {
                        ProperDefault.put("ui.open_dir", fc.getCurrentDirectory().getAbsolutePath());
                        return fc.getSelectedFile().getAbsolutePath();
                    } else {
                        return null;
                    }
                case 2:
                    fc.addChoosableFileFilter(defaultFilter = new ExtensionFilter(".gcode", "G-code files"));

                    fc.setAcceptAllFileFilterUsed(true);
                    fc.setFileFilter(defaultFilter);
                    fc.setCurrentDirectory(new File(loadDir));
                    fc.setDialogTitle("Open a G-code file...");
                    fc.setDialogType(JFileChooser.OPEN_DIALOG);
                    fc.setFileHidingEnabled(false);

                    rv = fc.showOpenDialog(this);
                    if (rv == JFileChooser.APPROVE_OPTION) {
                        return fc.getSelectedFile().getAbsolutePath();
                    } else {
                        return null;
                    }
                default:
                    break;
            }
        }

        return null;
    }

    /**
     * Open a sketch given the full path to the BEE file. Pass in 'null' to
     * prompt the user for the name of the sketch.
     *
     * @param ipath path to the BEE file
     */
    private void handleOpenScene(final String ipath) {
        Base.writeLog("Opening scene ...", this.getClass());
        SwingUtilities.invokeLater(() -> {
            String path = ipath;
            if (path == null) { // "open..." selected from the menu
                path = selectFile(1);
                if (path == null) {
                    return;
                }
            }
            //Base.logger.log(Level.INFO, "Loading {0}", path);
            Base.writeLog("Loading " + path + " ...", this.getClass());
            handleOpenPath = path;

            ObjectInputStream ois;
            try {
                ois = new ObjectInputStream(new FileInputStream(handleOpenPath));
                bed = (PrintBed) ois.readObject();
                bed.reloadModels();
                sceneDP = new SceneDetailsPanel();
                sceneDP.updateBed(bed);
                canvas.updateBed(bed);
                canvas.resetView();
                bed.setSceneDifferent(false);
                updateDetailsCenter(sceneDP);
                showFeedBackMessage("loadScene");
                ois.close();

                //Selects inserted model
                Base.getMainWindow().selectLastInsertedModel();
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    /**
     * Second stage of open that calls handleOpenScene after extension
     * validation. Updates also the mruList.
     *
     * @param path path to the BEE file
     */
    private void handleOpen2Scene(String path) {
        if (path != null && !new File(path).exists()) {
            JOptionPane.showMessageDialog(this, "The file " + path + " could not be found.", "File not found", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (path != null) {
            boolean extensionValid = false;

            // Note: Duplication of extension list from selectFile()
            String[] extensions = {".bee"};
            String lowercasePath = path.toLowerCase();
            for (String extension : extensions) {
                if (lowercasePath.endsWith(extension)) {
                    extensionValid = true;
                }
            }

            if (!extensionValid) {
                return;
            }
        }
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            handleOpenScene(path);
            if (null != path) {
                handleOpenPath = path;
                mruList.update(path);
            }
            Base.writeLog("Open model: " + path, this.getClass());
        } catch (Exception e) {
            Base.writeLog("Couldn't Open model: " + path, this.getClass());
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Handle Save the Scene. This consists in serializing the PrintBed Object.
     *
     * @param force force
     */
    public void handleSave(final boolean force) {
        Runnable saveWork = () -> {
            Base.LOGGER.info("Saving Scene...");
            Base.writeLog("Saving Scene...", this.getClass());

            ObjectOutputStream oos;
            try {
                File fSave = bed.save(force);

                if (fSave != null) {
                    bed.saveModelsPositions();
                    //On curaGenerator now 
//                    bed.setSceneDifferent(false);
                    oos = new ObjectOutputStream(new FileOutputStream(fSave));
                    oos.writeObject(bed);
                    oos.close();

                    Base.writeLog("Scene Saved...", this.getClass());
                    handleOpenPath = bed.getPrintBedFile().getAbsolutePath();
                    mruList.update(handleOpenPath);
                    if (!force) {
                        showFeedBackMessage("saveScene");
                    }

                    if (bed.getNumberPickedModels() == 0) {
                        sceneDP = new SceneDetailsPanel();
                        sceneDP.updateBed(bed);
                        updateDetailsCenter(sceneDP);
                        canvas.unPickAll();

                    } else {
                        updateModelsOperationCenter(new ModelsOperationCenter());
                        sceneDP = new SceneDetailsPanel();
                        sceneDP.updateBed(bed);
                        updateDetailsCenter(sceneDP);
                        canvas.unPickAll();
                    }
//                        canvas.unPickAll();
                    oktoGoOnSave = true;
                } else {
                    if (!force) {
                        showFeedBackMessage("notSaveScene");
                    }
                    oktoGoOnSave = false;
                }

            } catch (IOException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }

        };
        if (force) {
            saveWork.run();
        } else {
            SwingUtilities.invokeLater(saveWork);
        }
    }

    /**
     * Handle Save the Scene at a specific path & file. This consists in
     * serializing the PrintBed Object.
     */
    private void handleSaveAs() {
        SwingUtilities.invokeLater(() -> {
            // TODO: lock sketch?
            Base.LOGGER.info("Saving...");
            Base.writeLog("Saving...", this.getClass());

            if (!bed.saveAs(false)) {
                showFeedBackMessage("notSaveScene");
                oktoGoOnSave = false;
            } else {

//                    //Made on curaGenerator now
//                   bed.setSceneDifferent(false);
                handleOpenPath = bed.getPrintBedFile().getAbsolutePath();
                bed.saveModelsPositions();
                //Opens at last path used
                ProperDefault.put("ui.open_dir", handleOpenPath);
                mruList.update(handleOpenPath);

                ObjectOutputStream oos;
                try {
                    oos = new ObjectOutputStream(new FileOutputStream(handleOpenPath));
                    oos.writeObject(bed);
                    oos.close();
                } catch (IOException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
                Base.writeLog("Save operation complete.", this.getClass());
                Base.writeLog("Scene Saved...", this.getClass());
                sceneDP = new SceneDetailsPanel();
                sceneDP.updateBed(bed);
                updateDetailsCenter(sceneDP);
                showFeedBackMessage("saveScene");
                oktoGoOnSave = true;
                if (newSceneOnDialog) {
                    handleNew();
                    newSceneOnDialog = false;
                }

            }
        }
        );

    }

    /**
     * Quit, but first ask user if it's ok. Also store preferences to disk just
     * in case they want to quit. Final exit() happens in MainWindow since it
     * has the callback from EditorStatus.
     */
    private void handleQuitInternal() {
        // bring down our machine temperature, don't want it to stay hot
        // 		actually, it has been pointed out that we might want it to stay hot,
        //		so I'm taking this out
        //doPreheat(false);

        // cleanup our machine/driver.
        machineLoader.unload();
        Base.disposeAllOpenWindows();
        Base.closeLogs();
        System.exit(0);
    }

    /**
     * Method for the MRJQuitHandler, needs to be dealt with differently than
     * the regular handler because OS X has an annoying implementation <A
     * HREF="http://developer.apple.com/qa/qa2001/qa1187.html">quirk</A> that
     * requires an exception to be thrown in order to properly cancel a quit
     * message.
     */
    @Override
    public void handleQuit() {
        try {
            SwingUtilities.invokeAndWait(this::handleQuitInternal);
        } catch (InvocationTargetException | InterruptedException e) {
            System.exit(0);
        }

        // Throw IllegalStateException so new thread can execute.
        // If showing dialog on this thread in 10.2, we would throw
        // upon JOptionPane.NO_OPTION
        throw new IllegalStateException("Quit Pending User Confirmation");
    }

    /**
     * Clean up files and store UI preferences on shutdown. This is called by
     * the shutdown hook and will be run in virtually all shutdown scenarios.
     * Because it is used in a shutdown hook, there is no reason to call this
     * method explicit.y
     */
    public void onShutdown() {
        storePreferences();
        Base.writeConfig();
        Base.loadProperties();
        Base.cleanDirectoryTempFiles(Base.getAppDataDirectory() + "/" + Base.MODELS_FOLDER);

    }

    /**
     * Here we want to: 1. Create a new machine using the given profile name 2.
     * If the new machine uses a serial port, connect to the serial port 3. If
     * this is a new machine, record a reference to it 4. Hook the machine to
     * the main window.
     *
     */
    public void loadMachine() {

        MachineInterface mi = machineLoader.getMachineInterface("BEETHEFIRST");

        if (mi == null) {
            //Base.logger.log(Level.SEVERE, "could not load machine ''{0}'' please check Driver", name);
            return;
        }

        machineLoader.connect(false); // Just performs a setState 

        if (canvas != null) {
            getPreviewPanel().rebuildScene();
            //updateBuild();
        } else {
            getPreviewPanel();
        }

    }

    /**
     * Selects the last inserted model
     */
    private void selectLastInsertedModel() {

        if (this.getBed().getModels().size() > 0) {
            this.getCanvas().unPickAll();
            Model m = this.getBed().getModel(this.getBed().getModels().size() - 1);
            this.getBed().addPickedModel(m);
            m.getEditer().updateModelPicked();
            this.getCanvas().evaluateModelsBed();
        }
    }

    @Override
    public void toFront() {
        super.setAlwaysOnTop(true);
        super.toFront();
        super.requestFocus();
        super.setAlwaysOnTop(false);
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
        new Thread() {
            @Override
            public void run() {
                // very nasty way of working around the lightweight vs heavyweight problem
                // Canvas vs JMenu
                Base.hiccup(500);
                java.awt.EventQueue.invokeLater(() -> {
                    if (Base.getMainWindow().isActive()) {
                        setVisible(true);
                        toFront();
                        requestFocus();
                        repaint();
                    }
                });
            }
        }.start();
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}
