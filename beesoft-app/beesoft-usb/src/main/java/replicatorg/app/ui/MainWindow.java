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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.undo.CompoundEdit;

import net.iharder.dnd.FileDrop;
import net.miginfocom.swing.MigLayout;
import replicatorg.app.Base;
import replicatorg.app.Base.InitialOpenBehavior;
import replicatorg.app.MRUList;
import replicatorg.drivers.OnboardParameters;
import replicatorg.machine.MachineInterface;
import replicatorg.machine.MachineListener;
import replicatorg.machine.MachineLoader;
import replicatorg.machine.MachineProgressEvent;
import replicatorg.machine.MachineState;
import replicatorg.machine.MachineStateChangeEvent;
import replicatorg.machine.MachineToolStatusEvent;
import replicatorg.plugin.toolpath.ToolpathGenerator;
import replicatorg.plugin.toolpath.ToolpathGenerator.GeneratorEvent;

import com.apple.mrj.MRJAboutHandler;
import com.apple.mrj.MRJApplicationUtils;
import com.apple.mrj.MRJOpenDocumentHandler;
import com.apple.mrj.MRJPrefsHandler;
import com.apple.mrj.MRJQuitHandler;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Robot;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import replicatorg.app.CategoriesList;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
import pt.beeverycreative.beesoft.filaments.PrintPreferences;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.mainWindow.ButtonsPanel;
import replicatorg.app.ui.mainWindow.ModelsOperationCenter;
import replicatorg.app.ui.mainWindow.SceneDetailsPanel;
import replicatorg.app.ui.mainWindow.MessagesPopUp;
import replicatorg.app.ui.mainWindow.ModelsDetailsPanel;
import replicatorg.app.ui.mainWindow.ModelsOperationCenterScale;
import replicatorg.app.ui.mainWindow.UpdateChecker;
import replicatorg.app.ui.panels.About;
import replicatorg.app.ui.panels.BuildStatus;
import replicatorg.app.ui.panels.ControlPanel;

import replicatorg.app.ui.panels.Gallery;
import replicatorg.app.ui.panels.Maintenance;
import replicatorg.app.ui.panels.PreferencesPanel;
import replicatorg.app.ui.panels.PrintPanel;
import replicatorg.app.ui.panels.PrintSplashAutonomous;
import replicatorg.app.ui.panels.TourWelcome;
import replicatorg.app.ui.panels.Warning;
import replicatorg.app.ui.panels.WelcomeQuickguide;
import replicatorg.app.util.ExtensionFilter;
import replicatorg.drivers.EstimationDriver;

import replicatorg.model.CAMPanel;
import replicatorg.model.Model;
import replicatorg.model.PrintBed;
import replicatorg.util.Units_and_Numbers;

/*
 *  Copyright (c) 2013 BEEVC - Electronic Systems
 */
public class MainWindow extends JFrame implements MRJAboutHandler,
        MRJQuitHandler, MRJPrefsHandler, MRJOpenDocumentHandler,
        MachineListener, ChangeListener, ToolpathGenerator.GeneratorListener, ComponentListener, MouseListener, MouseMotionListener, WindowListener, FocusListener {

    private static final long serialVersionUID = 4144538738677712284L;
    static final String WINDOW_TITLE_OK = "BEESOFT";
    final static String MODEL_TAB_KEY = "MODEL";
    MachineLoader machineLoader;
    static public final KeyStroke WINDOW_CLOSE_KEYSTROKE = KeyStroke
            .getKeyStroke('W', Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask());
    static final int HANDLE_NEW = 1;
    static final int HANDLE_OPEN = 2;
    static final int HANDLE_QUIT = 3;
    int checkModifiedMode;
    String handleOpenPath;
    boolean handleNewShift;
    PageFormat pageFormat;
    PrinterJob printerJob;
    ButtonsPanel buttons;
    public CAMPanel canvas;
    CardLayout cardLayout = new CardLayout();
    JPanel cardPanel = new JPanel(new BorderLayout());
    private final MRUList mruList;
    private final CategoriesList categoriesList;
    MachineStatusPanel machineStatusPanel;
    MessagePanel console;
    JSplitPane splitPane;
    JLabel lineNumberComponent;
    public PrintBed bed;
    JMenuItem saveMenuItem;
    JMenuItem saveAsMenuItem;
    JMenuItem controlPanelItem;
    JMenuItem controlPanelOldItem;
    JMenuItem redMECControlPanelItem;
    JMenuItem redMECPreHeatItem;
    //
    JMenuItem buildMenuItem;
    JMenuItem controlFilamentItem;
    JMenuItem profilesMenuItem;
    JMenuItem dualstrusionItem;
    JMenuItem combineItem;
    JMenu changeToolheadMenu = new JMenu("Swap Toolhead in .gcode");
    JMenu machineMenu;
    public boolean buildOnComplete = true;
    private boolean preheatMachine = false;
    public boolean building;
    public boolean simulating;
    public boolean debugging;
    private boolean editorEnabled;
    JMenuItem undoItem, redoItem;
    private int realWidth;
    private int realHeight;
    private SceneDetailsPanel sceneDP;
    private String buildTime = "";
    private boolean oktoGoOnSave = false;
    private boolean newSceneOnDialog = false;
    MessagesPopUp messagesPP;
    CompoundEdit compoundEdit;

    public MainWindow() {
        super(WINDOW_TITLE_OK);
        setPreferredSize(new Dimension(1000, 650));
        setMinimumSize(new Dimension(1000, 650));
        setAlwaysOnTop(false);
        setFocusable(true);
        setFocusableWindowState(true);
        setFocusCycleRoot(true);
        editorEnabled = true;
        setName("mainWindow");
        setBackground(new Color(255, 255, 255));
        MRJApplicationUtils.registerAboutHandler(this);
        MRJApplicationUtils.registerPrefsHandler(this);
        MRJApplicationUtils.registerQuitHandler(this);
        MRJApplicationUtils.registerOpenDocumentHandler(this);
        this.addComponentListener(this);
        this.addWindowListener(this);
        this.addMouseMotionListener(this);
        machineLoader = Base.getMachineLoader();
        bed = PrintBed.makePrintBed(null);
        cardPanel.setBackground(new Color(255, 255, 255));
        mruList = MRUList.getMRUList();
        categoriesList = CategoriesList.getMRUList();
        messagesPP = new MessagesPopUp();
        messagesPP.setVisible(false);
//        camCtrl = new CameraControl(this, false);
        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());

        this.getContentPane().addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateSizeVariables(e.getComponent().getWidth(), e.getComponent().getHeight());
                updateGUI();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                //buttons.changeIconsLayout(e);
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

        // add listener to handle window close box hit event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleQuitInternal();
            }
        });

        if (Boolean.valueOf(ProperDefault.get("firstTime")) == true) {
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
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        JMenuBar menubar = new JMenuBar();
        menubar.setBackground(new Color(0xf0, 0xf3, 0xf4));

        menubar.add(buildFileMenu());
        menubar.add(buildEditMenu());
//        menubar.add(buildModelsMenu());
        menubar.add(buildPrinterMenu());
        menubar.add(buildHelpMenu());
        setJMenuBar(menubar);
        buttons = new ButtonsPanel(this);
        Container pane = getContentPane();

        MigLayout layout = new MigLayout("nocache,fill,flowy,gap 0 0,ins 0");
        pane.setLayout(layout);
        pane.add(buttons, "growx,dock north");
        Component border = Box.createVerticalStrut(2);
        border.setBackground(new Color(240, 243, 244));

        machineStatusPanel = new MachineStatusPanel();

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cardPanel,
                console);
        new FileDrop(null, cardPanel, /* dragBorder, */
                new FileDrop.Listener() {
                    @Override
                    public void filesDropped(java.io.File[] files) {
                        bed.addSTL(files[0]);
                        bed.setSceneDifferent(true);
                        Model m = bed.getModels().get(bed.getModels().size() - 1);
                        m.getEditer().centerAndToBed();
                        canvas.updateBedImportedModels(bed);

                        //Selects the inserted model
                        selectLastInsertedModel();

                    }
                });
//        splitPane.setResizeWeight(0.86);
        splitPane.setPreferredSize(new Dimension(1000, 600));
        pane.add(cardPanel, "growx,growy,shrinkx,shrinky");
        setLocationRelativeTo(null);
        pack();
        // Have UI elements listen to machine state.
        machineLoader.addMachineListener(this);
        machineLoader.addMachineListener(machineStatusPanel);
    }

    public PrintBed getBed() {
        return bed;
    }

    public ButtonsPanel getButtons() {
        return buttons;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(String p) {
        buildTime = p;
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
//                Warning p = new Warning();
//                p.setVisible(true);
//                p.setMessage("MessageOutOfBounds");
                showFeedBackMessage("MessageOutOfBounds");
                return false;
            }/*
            else if (!bed.getModels().get(i).getEditer().modelInBed()) {
//                Warning p = new Warning();
//                p.setVisible(true);
//                p.setMessage("MessageNotInBed");
                showFeedBackMessage("MessageNotInBed");
                return false;
            }
            */
        }
        return true;
    }

    public void setMessage(String message) {
        buttons.setMessage(message);
    }

    public void refreshPreviewPanel() {
        if (canvas != null) {
            canvas.updateBed(bed);
        }
    }

    public void updateModelsOperationCenter(JPanel newMOC) {
        cardPanel.remove(((BorderLayout) cardPanel.getLayout()).getLayoutComponent(BorderLayout.WEST));
        cardPanel.add(newMOC, BorderLayout.WEST);
        cardPanel.validate();

    }

    public void updateDetailsCenter(JPanel newDC) {
        cardPanel.remove(((BorderLayout) cardPanel.getLayout()).getLayoutComponent(BorderLayout.EAST));
        cardPanel.add(newDC, BorderLayout.EAST);
        cardPanel.validate();
    }

    public void showFeedBackMessage(String message) {
        if (!messagesPP.isVisible()) {
            messagesPP.setMessage(message);
        }
    }

    private CAMPanel getPreviewPanel() {
        if (canvas == null) {
//            canvas = new PreviewPanel(this);
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

    private void updateGUI() {
        this.setSize(this.getWidth() + 1, this.getHeight());
        this.setSize(this.getWidth() - 1, this.getHeight());
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
        this.realWidth = wth;
        this.realHeight = hth;
        cardPanel.setSize(this.realWidth, this.realHeight);
        //canvas.getPanel().setSize(this.realWidth - 200 - 265, this.realHeight);
        canvas.setCanvasSize(new Dimension(this.realWidth, this.realHeight)); //cardPanel.getSize()
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
                handleNew(false);
            } else {
                // Get last path opened; MRU keeps this.
                Iterator<String> i = mruList.iterator();
                if (i.hasNext()) {
                    String lastOpened = i.next();
                    if (new File(lastOpened).exists()) {
                        handleOpen2Scene(lastOpened);
                    }
                } else {
                    handleNew(false);
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
     * Read and apply new values from the preferences, either because the app is
     * just starting up, or the user just finished messing with things in the
     * Preferences window.
     */
    public void applyPreferences() {

//        textarea.setEditable(true);
        saveMenuItem.setEnabled(true);
        saveAsMenuItem.setEnabled(true);
    }

    /**
     * Store preferences about the editor's current state. Called when the
     * application is quitting.
     */
    public void storePreferences() {

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

        // location for the console/editor area divider
        int location = splitPane.getDividerLocation();
        ProperDefault.put("last.divider.location", String.valueOf(location));
    }
    /**
     * Builds and runs a toolpath generator to slice the model, sets up
     * callbacks so this will be notified when a build is finished.
     *
     * @param skipConfig true if we want to skip skeinforge config, and simply
     * slice the model with the existing settings
     */
    private final JMenu mruMenu = null;

    @Override
    public void handlePrefs() throws IllegalStateException {

    }

    public void activateCameraControls() {
//        camCtrl.setVisible(true);
    }

    public void deactivateCameraControls() {
//        if (camCtrl.isVisible()) {
//        camCtrl.setVisible(false);
//        }
    }

    private class FileOpenActionListener implements ActionListener {

        public String path;

        FileOpenActionListener(String path) {
            this.path = path;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            handleOpenScene(path);
        }
    }

    private void reloadMruMenu() {
        if (mruMenu == null) {
            return;
        }
        mruMenu.removeAll();
        if (mruList != null) {
            int index = 0;
            for (String fileName : mruList) {
                String entry = Integer.toString(index) + ". "
                        + fileName.substring(fileName.lastIndexOf('/') + 1);
                JMenuItem item = new JMenuItem(entry, KeyEvent.VK_0 + index);
                item.addActionListener(new FileOpenActionListener(fileName));
                mruMenu.add(item);
                index = index + 1;
                if (index >= 9) {
                    break;
                }
            }
        }
    }

    protected JMenu buildFileMenu() {
        JMenuItem item;
        JMenu menu = new JMenu("File");
        menu.setIcon(GraphicDesignComponents.getMenuItemIcon());
        menu.setFont(GraphicDesignComponents.getSSProLight("13"));
        menu.setText(Languager.getTagValue(1, "ApplicationMenus", "File"));

        item = newJMenuItem("New Scene", 'N');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "File_New"));

        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Base.getMainWindow().getButtons().areIOFunctionsBlocked() == false) {
                    if (bed.isSceneDifferent() && (oktoGoOnSave == false)) {
                        int answer;
                        answer = JOptionPane.showConfirmDialog(null,
                                Languager.getTagValue(1, "ToolPath", "Line6") + "\n" + Languager.getTagValue(1, "ToolPath", "Line7"),
                                Languager.getTagValue(1, "ToolPath", "Line8"), 0, 0);
                        if (answer == JOptionPane.YES_OPTION) {
                            if (bed.isSceneDifferent()) {
                                newSceneOnDialog = true;
                                handleSaveAs();
                                bed.setSceneDifferent(false);
                                updateModelsOperationCenter(new ModelsOperationCenter());
                            }
                        } else if (answer == JOptionPane.NO_OPTION) {
                            handleNew(false);
                            updateModelsOperationCenter(new ModelsOperationCenter());
                        }

                    } else {
                        handleNew(false);
                        updateModelsOperationCenter(new ModelsOperationCenter());
                    }
                }
            }
        });
        menu.add(item);

        //
        // File menu Work Area section
        //
        item = newJMenuItem("Open Scene...", 'O', false);
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "File_Open"));

        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Base.getMainWindow().getButtons().areIOFunctionsBlocked() == false) {
                    if (bed.isSceneDifferent() && (oktoGoOnSave == false)) {
                        int answer;
                        answer = JOptionPane.showConfirmDialog(null,
                                Languager.getTagValue(1, "ToolPath", "Line6") + "\n" + Languager.getTagValue(1, "ToolPath", "Line7"),
                                Languager.getTagValue(1, "ToolPath", "Line8"), 0, 0);
                        if (answer == JOptionPane.YES_OPTION) {
                            if (bed.isSceneDifferent()) {
                                handleSaveAs();
                                handleOpenScene(null);
                                bed.setSceneDifferent(false);
                                updateModelsOperationCenter(new ModelsOperationCenter());
                            }
                        } else if (answer == JOptionPane.NO_OPTION) {
                            handleOpenScene(null);
                            updateModelsOperationCenter(new ModelsOperationCenter());
                        }

                    } else {
                        handleOpenScene(null);
                        updateModelsOperationCenter(new ModelsOperationCenter());
                    }
                }

            }
        });

        menu.add(item);

        saveMenuItem = newJMenuItem("Save Scene", 'S');
        saveMenuItem.setFont(GraphicDesignComponents.getSSProRegular("12"));
        saveMenuItem.setText(Languager.getTagValue(1, "ApplicationMenus", "File_Save"));
        saveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Base.getMainWindow().getButtons().areIOFunctionsBlocked() == false) {
                    handleSave(false);
                }
            }
        });
        menu.add(saveMenuItem);

        saveAsMenuItem = newJMenuItem("Save As...", 'S', true);
        saveAsMenuItem.setFont(GraphicDesignComponents.getSSProRegular("12"));
        saveAsMenuItem.setText(Languager.getTagValue(1, "ApplicationMenus", "File_Save_as"));
        saveAsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Base.getMainWindow().getButtons().areIOFunctionsBlocked() == false) {
                    handleSaveAs();
                }
            }
        });
        menu.add(saveAsMenuItem);

        menu.addSeparator();

        //
        // File menu Import and Export section
        //
        item = newJMenuItem("Import Model ", 'I');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "Model_Import"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleNewModel();
            }
        });
        menu.add(item);

        item = newJMenuItem("Export G-code file", 'E');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(
                1, "ApplicationMenus", "GCode_Export"
        ));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MachineInterface machine;
                MainWindow editor;

                machine = Base.getMainWindow().getMachineInterface();
                editor = Base.getMainWindow();

                if (machine.getModel().getMachineBusy()) {
                    editor.showFeedBackMessage("moving");
                } else {//&& Base.isPrinting == false 
                    if (editor.validatePrintConditions()
                            || Boolean.valueOf(ProperDefault.get("localPrint"))) {
                        editor.handlePrintPanel();
                    }
                }
            }
        });
        menu.add(item);

        item = newJMenuItem("Print G-code file", 'G');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "GCode_Import"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean noFilament;
                MachineInterface machine = getMachineInterface();

                noFilament = Base.getMainWindow().getMachine().getModel()
                        .getCoilText()
                        .equalsIgnoreCase(FilamentControler.NO_FILAMENT_CODE);

                if (Base.isPrinting == true) {
                    Base.getMainWindow().showFeedBackMessage("btfPrinting");
                } else if (machine.isConnected() == false) {
                    Base.getMainWindow().showFeedBackMessage("btfDisconnect");
                } else {
                    handleGCodeImport();
                }

            }
        });
        menu.add(item);

        //
        // File menu Preferences section
        //
        menu.addSeparator();

        item = newJMenuItem("Settings...", 'P', true);
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "File_Preferences"));

        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PreferencesPanel p = new PreferencesPanel();
                p.setVisible(true);
            }
        });
        menu.add(item);

//
        // File menu Quit section
        //
        menu.addSeparator();

        item = newJMenuItem("Quit", 'Q', true);
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "File_Quit"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleQuitInternal();
            }
        });
        menu.add(item);
        return menu;
    }

    public JMenu buildEditMenu() {
        JMenu menu = new JMenu("Edit");
        menu.setIcon(GraphicDesignComponents.getMenuItemIcon());
        menu.setFont(GraphicDesignComponents.getSSProLight("13"));
        menu.setText(Languager.getTagValue(1, "ApplicationMenus", "Edit"));

        JMenuItem item;

        item = newJMenuItem("Undo", 'Z');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "Edit_Undo"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                bed.getModel(0).getEditer().evaluateCollision();
                bed.undoTransformation();
            }
        });

//        menu.add(item);
        item = newJMenuItem("Redo", 'Y');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "Edit_Redo"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bed.redoTransformation();
            }
        });
//        menu.add(item);

//        menu.addSeparator();
        item = newJMenuItem("Duplicate", 'V');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "Edit_Duplicate"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (bed.getPickedModels().size() > 0) {
                    bed.duplicateModel();
                    bed.setSceneDifferent(true);
                    canvas.updateBedImportedModels(bed);
                    sceneDP.updateBedInfo();
                }
            }
        });
//        menu.add(item);

        item = newJMenuItem("Delete", 'D');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "Edit_Delete"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCAMDelete();
            }
        });
        menu.add(item);
        menu.addSeparator();

        item = newJMenuItem("Select all", 'A');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "Edit_SelectAll"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.pickAll();
                ModelsDetailsPanel mdp = new ModelsDetailsPanel();
                mdp.updateBed(bed);
                updateDetailsCenter(mdp);

            }
        });
//        menu.add(item);

        item = newJMenuItem("Unselect", 'Z', true);
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "Edit_Unselect"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.unPickAll();
                SceneDetailsPanel sdp = new SceneDetailsPanel();
                sdp.updateBed(bed);
                updateDetailsCenter(sdp);
            }
        });
        menu.add(item);
        menu.addSeparator();

        item = newJMenuItem("Put on Platform", 'L');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "Edit_PutPlatform"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (bed.getPickedModels().size() > 0) {
                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().putOnPlatform();
                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().updateModelPicked();
                }
            }
        });
        menu.add(item);

        item = newJMenuItem("Center in Platform", 'C');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "Edit_Center"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (bed.getPickedModels().size() > 0) {
                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().center();
                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().updateModelPicked();
                }
            }
        });
        menu.add(item);

        item = newJMenuItem("Reset Original Position", 'R');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "Edit_Reset"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (bed.getPickedModels().size() > 0) {
                    bed.resetTransformation();
                    Model model = Base.getMainWindow().getBed().getFirstPickedModel();
                    ModelsOperationCenterScale mOCS = Base.getMainWindow().getCanvas().getControlTool(3).getModelsScaleCenter();

                    if (mOCS != null) {
                        model.resetScale();

                        DecimalFormat df = new DecimalFormat("#.00");

                        double width = model.getEditer().getWidth();
                        if (ProperDefault.get("measures").equals("inches")) {
                            width = Units_and_Numbers.millimetersToInches(width);
                        }
                        double depth = model.getEditer().getDepth();
                        if (ProperDefault.get("measures").equals("inches")) {
                            depth = Units_and_Numbers.millimetersToInches(depth);
                        }
                        double height = model.getEditer().getHeight();
                        if (ProperDefault.get("measures").equals("inches")) {
                            height = Units_and_Numbers.millimetersToInches(height);
                        }

                        mOCS.setXValue(df.format(width));
                        mOCS.setYValue(df.format(depth));
                        mOCS.setZValue(df.format(height));
                    }
                    model.getEditer().updateModelPicked();

                }
//                canvas.resetView();
            }
        });
        menu.add(item);

        return menu;
    }

    protected JMenu buildModelsMenu() {
        JMenuItem item;
        JMenu menu = new JMenu("Gallery");
        menu.setIcon(GraphicDesignComponents.getMenuItemIcon());
        menu.setFont(GraphicDesignComponents.getSSProLight("13"));
        menu.setText(Languager.getTagValue(1, "ApplicationMenus", "Gallery"));

        item = newJMenuItem("Import Model from Library", 'G');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "Model_Add"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Gallery p = new Gallery();
                p.setVisible(true);
                SceneDetailsPanel sceneDP = new SceneDetailsPanel();
                sceneDP.updateBed(Base.getMainWindow().getBed());
                updateDetailsCenter(sceneDP);
                canvas.unPickAll();
            }
        });
//        menu.add(item);

        item = newJMenuItem("Import Model ", 'I');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "Model_Import"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleNewModel();
            }
        });
        menu.add(item);

        item = newJMenuItem("Online Models", 'I', true);
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "Model_Online"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Warning p = new Warning();
                p.setVisible(true);
            }
        });
//        menu.add(item);
        return menu;
    }

    protected JMenu buildPrinterMenu() {
        boolean enableCP;
        JMenuItem item;
        JMenu menu = new JMenu("Printer");
        menu.setIcon(GraphicDesignComponents.getMenuItemIcon());
        menu.setFont(GraphicDesignComponents.getSSProLight("13"));
        menu.setText(Languager.getTagValue(1, "ApplicationMenus", "Printer"));
        enableCP = Boolean.parseBoolean(Base.readConfig("controlpanel.enable"));

        item = newJMenuItem("Maintenance", 'M');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "Printer_Maintenance"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (machineLoader.isConnected()) {
                    if (Base.isPrinting == false) {
                        Maintenance p = new Maintenance();
                        p.setVisible(true);
                    }
                } else {
                    showFeedBackMessage("btfDisconnect");
                }
            }
        });
        menu.add(item);

        if (enableCP == true) {
            item = newJMenuItem("Control Panel", 'K');
            item.setFont(GraphicDesignComponents.getSSProRegular("12"));
            item.setText(Languager.getTagValue(1, "ApplicationMenus", "Printer_ControlPanel"));
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (machineLoader.isConnected()) {
                        if (Base.isPrinting == false) {
                            ControlPanel cp = new ControlPanel();
                            cp.setVisible(true);
                        }
                    } else {
                        showFeedBackMessage("btfDisconnect");
                    }
                }
            });
            menu.add(item);
        }

        menu.addSeparator();

        item = newJMenuItem("Print ", 'P');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "Printer_Print"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                if (Base.isPrinting == false) {
                MachineInterface machine = getMachineInterface();
                machine.runCommand(new replicatorg.drivers.commands.ReadStatus());

                try {
                    Thread.sleep(250, 0);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (!machine.getDriver().getMachineReady() && machine.getDriver().isBusy()) {
                    showFeedBackMessage("moving");
                } else {
                    if (validatePrintConditions() && Base.getMainWindow().getBed().getNumberModels() > 0
                            || Boolean.valueOf(ProperDefault.get("localPrint"))) {
                        handlePrintPanel();
                    }
                }
            }
        });
        menu.add(item);

        return menu;
    }

    protected JMenu buildHelpMenu() {
        JMenuItem item;
        JMenu menu = new JMenu("Help");
        menu.setIcon(GraphicDesignComponents.getMenuItemIcon());
        menu.setFont(GraphicDesignComponents.getSSProLight("13"));
        menu.setText(Languager.getTagValue(1, "ApplicationMenus", "Help"));

        item = newJMenuItem("FAQ", 'F', true);
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "Help", "FAQ"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchBrowser("https://beeverycreative.com/faq/");

            }
        });
        menu.add(item);

        item = newJMenuItem("Troubleshooting", 'T', true);
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "Help", "Troubleshooting"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchBrowser("https://beeverycreative.com/troubleshooting/");
            }
        });
        menu.add(item);

        item = newJMenuItem("Quick Guide ", 'Q');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "Help_QuickGuide"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Base.writeLog("BEESOFT tour loaded ... ", this.getClass());

                TourWelcome p = new TourWelcome();
                p.setVisible(true);
            }
        });
        menu.add(item);
        menu.addSeparator();

        item = newJMenuItem("Check for Updates ", 'U');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "Help_Update"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UpdateChecker p = new UpdateChecker();
                p.setVisible(true);
            }
        });
        menu.add(item);
        menu.addSeparator();

        item = newJMenuItem("About ", 'K');
        item.setFont(GraphicDesignComponents.getSSProRegular("12"));
        item.setText(Languager.getTagValue(1, "ApplicationMenus", "Help_About"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                About p = new About();
                p.setVisible(true);
            }
        });
        menu.add(item);

        return menu;
    }

    protected JMenu buildAboutMenu() {

        JMenu menu = new JMenu("About");
        menu.setIcon(GraphicDesignComponents.getMenuItemIcon());
        menu.setFont(GraphicDesignComponents.getSSProLight("13"));
        menu.setText(Languager.getTagValue(1, "ApplicationMenus", "Help_About"));
        menu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                About p = new About();
                p.setVisible(true);
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });
        return menu;
    }

    public void launchBrowser(String url) {
        if (java.awt.Desktop.isDesktopSupported()) {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } catch (IOException e) {
                Base.logger.log(Level.WARNING, "Could not load URL.");
            } catch (java.net.URISyntaxException e) {
                Base.logger.log(Level.WARNING, "bad URI");
            }
        }
    }
    JMenuItem onboardParamsItem = new JMenuItem("Onboard Preferences...");
//	JMenuItem toolheadIndexingItem = new JMenuItem("Set Toolhead Index...");
    JMenuItem realtimeControlItem = new JMenuItem("Open real time controls window...");
    JMenuItem infoPanelItem = new JMenuItem("Machine information...");

    ///  called when the preheat button is toggled
    protected void handlePreheat() {
        preheatMachine = !preheatMachine;
        doPreheat(preheatMachine);
    }

    /**
     * Function enables/disables preheat and updates gui to reflect the state of
     * preheat.
     *
     * @param preheat true/false to indicate if we want preheat running
     */
    public void doPreheat(boolean preheat) {
        int tool0Target = 220;
        MachineInterface machine = Base.getMachineLoader().getMachineInterface();

        if (machine != null && !building) {
            // To heat
            if (preheat) {
                Base.writeLog("Heating ...", this.getClass());
                machine.runCommand(new replicatorg.drivers.commands.SelectTool(0));
                //turn off blower before heating
                machine.runCommand(new replicatorg.drivers.commands.DispatchCommand("M107"));
                machine.runCommand(new replicatorg.drivers.commands.SetTemperature(tool0Target));
            }
            // To cooldown
            if (!preheat) {
                machine.runCommand(new replicatorg.drivers.commands.SelectTool(0));
                machine.runCommand(new replicatorg.drivers.commands.SetTemperature(0));
            }

        } else {
            if (machine != null) {
                machine.runCommand(new replicatorg.drivers.commands.SetTemperature(0));
            }
        }

    }

    /**
     * Convenience method, see below.
     *
     * @param title
     * @param what
     * @return
     */
    static public JMenuItem newJMenuItem(String title, int what) {
        return newJMenuItem(title, what, false);
    }

    /**
     * A software engineer, somewhere, needs to have his abstraction taken away.
     * In some countries they jail or beat people for writing the sort of API
     * that would require a five line helper function just to set the command
     * key for a menu item.
     *
     * @param title
     * @param what
     * @param shift
     * @return
     */
    static public JMenuItem newJMenuItem(String title, int what, boolean shift) {
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

    @Override
    public void handleAbout() {
    }

    public void handleQuickStartWizard() {
        this.setEnabled(false);
        WelcomeQuickguide p = new WelcomeQuickguide();
        p.setVisible(true);
    }

    public void captureScreen(String fileName) throws Exception {

        Robot robot = new Robot();
        // Capture the screen shot of the area of the screen defined by the rectangle
        BufferedImage bi = robot.createScreenCapture(new Rectangle(100, 100));
        ImageIO.write(bi, "jpg", new File("imageTest.jpg"));

    }

    public void handlePrintPanel() {
        this.setEnabled(false);
        boolean localPrint = Boolean.valueOf(ProperDefault.get("localPrint"));
        handleGenBuild();

        PrintPanel p = new PrintPanel();
        p.setVisible(true);
    }

    public void handleMaintenance() {
        this.setEnabled(false);
        Maintenance p = new Maintenance();
        p.setVisible(true);
    }

    public void handleGallery() {
        this.setEnabled(false);
        Gallery p = new Gallery();
        p.setVisible(true);
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

    /**
     * Enables back the MainWindow after Calibration Window Open Operation
     */
    public void enableWindow() {
        this.setEnabled(true);
    }

    public void beginCompoundEdit() {
        compoundEdit = new CompoundEdit();
    }

    public void simulationOver() {
//        message("Done simulating.");
        simulating = false;
        setEditorBusy(false);
    }

    /// Enum to indicate target build intention
    /// generate-from-stl and build, cancel build, or siply build from gcode
    enum BuildFlag {

        NONE(0), /// Canceled or software error
        GEN_AND_BUILD(1), //genrate new gcode and build
        JUST_BUILD(2); //expect someone checked for existing gcode, and build that
        public final int number;

        /// standard constructor. 
        private BuildFlag(int n) {
            number = n;
        }
    };

    public void handleGenBuild() {

        Base.writeLog("Starting building ...", this.getClass());
        buildOnComplete = true;
        doPreheat(Boolean.valueOf(ProperDefault.get("build.doPreheat")));
        //machineLoader.getMachineInterface().runCommand(new replicatorg.drivers.commands.DispatchCommand("M300", COM.BLOCK));

        // build specific stuff
        building = true;
        setEditorBusy(false);
        doPreheat(true);

        // start our building thread.
        message("Building...");
        buildStart = new Date();

        // Set Building State
        this.machineLoader.buildDirect("Print");

    }

    public void handleBuild() {
        if (building) {
            return;
        }
        if (simulating) {
            return;
        }

        BuildFlag buildFlag = BuildFlag.JUST_BUILD;

        if (buildFlag == BuildFlag.NONE) {
            return; //exit ro cancel clicked
        }

        if (buildFlag == BuildFlag.GEN_AND_BUILD) {
            //'rewrite' clicked
            buildOnComplete = true;
            Cursor old = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            doPreheat(Boolean.valueOf(ProperDefault.get("build.doPreheat")));
            //machineLoader.getMachineInterface().getDriver().executeGCodeLine("M300");
            setCursor(old);
            this.setEnabled(false);
        }
        if (buildFlag == BuildFlag.JUST_BUILD) {
            //'use existing' clicked
//            doBuild();
        }
    }

    public MachineInterface getMachine() {
        return this.machineLoader.getMachineInterface();
    }
    private Date buildStart = null;

    @Override
    public void machineStateChanged(MachineStateChangeEvent evt) {

        if (Base.logger.isLoggable(Level.FINE)) {
            Base.logger.log(Level.FINEST, "Machine state changed to {0}", evt.getState().getState());
        }

        if (building) {
            if (evt.getState().canPrint()) {
                final MachineState endState = evt.getState();
                building = false;

                SwingUtilities.invokeLater(new Runnable() {
                    // TODO: Does this work?
                    @Override
                    public void run() {
                        if (endState.canPrint()) {
                            notifyBuildComplete(buildStart, new Date());
                        } else {
                            notifyBuildAborted(buildStart, new Date());
                        }

                        buildingOver();
                    }
                });
            } else if (evt.getState().getState() == MachineState.State.NOT_ATTACHED) {
                building = false; // Don't keep the building state when disconnecting from the machine
                buildingOver();
            }
        }

        boolean showParams = evt.getState().isConfigurable()
                && machineLoader.getDriver() instanceof OnboardParameters
                && ((OnboardParameters) machineLoader.getDriver()).hasFeatureOnboardParameters();

        if (Base.logger.isLoggable(Level.FINE)) {
            if (!showParams) {
                String cause = new String();
                if (evt.getState().isConfigurable()) {
                    if (!machineLoader.isLoaded()) {
                        cause += "[no machine] ";
                    } else {
                        if (!(machineLoader.getDriver() instanceof OnboardParameters)) {
                            cause += "[driver doesn't implement onboard parameters] ";
                        } else if (!machineLoader.getDriver().isInitialized()) {
                            cause += "[machine not initialized] ";
                        } else if (!((OnboardParameters) machineLoader.getDriver()).hasFeatureOnboardParameters()) {
                            cause += "[firmware doesn't support onboard parameters]";
                        }
                    }
                    Base.logger.log(Level.FINEST, "Couldn''t show onboard parameters: {0}", cause);
                }
            }
        }

        onboardParamsItem.setVisible(showParams);
        onboardParamsItem.setEnabled(showParams);

        boolean showRealtimeTuning
                = evt.getState().isConnected();
        realtimeControlItem.setVisible(showRealtimeTuning);
        realtimeControlItem.setEnabled(showRealtimeTuning);

        // TODO: When should this be enabled?
        infoPanelItem.setEnabled(true);

        // Advertise machine name
        String name = "Not Connected";
        if (evt.getState().isConnected() && machineLoader.isLoaded()) {
            name = machineLoader.getMachineInterface().getMachineName();
        }
    }

    public void setEditorBusy(boolean isBusy) {
        // prepare editor window.
//        setVisible(true);
        setEnabled(true);
    }

    @Override
    public void setEnabled(boolean enabled) {

        if (enabled) {
            editorEnabled = true;
            buttons.setMainWindowEnabled(editorEnabled);
        } else {
            editorEnabled = false;
            buttons.setMainWindowEnabled(editorEnabled);
        }
    }

    @Override
    public boolean isEnabled() {
        return editorEnabled;
    }

    /**
     * give a prompt and stuff about the build being done with elapsed time,
     * etc.
     */
    private void notifyBuildComplete(Date started, Date finished) {
        assert started != null;
        assert finished != null;

        long elapsed = (finished.getTime() - started.getTime()) + 1;

        String time_string = EstimationDriver.getBuildTimeString(elapsed);
        buildTime = time_string;
        String message = "Build finished.\n\nCompleted in " + time_string;

        Base.writeLog("Build finished. Completed in " + time_string, this.getClass());

        building = false;
        buildingOver();
        Base.getMachineLoader().buildDirect("Ready");
        setEditorBusy(true);
        Base.cleanDirectoryTempFiles(Base.getAppDataDirectory().getAbsolutePath() + "/" + Base.MODELS_FOLDER + "/");
        ProperDefault.put("dateLastPrint", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        ProperDefault.put("nTotalPrints", String.valueOf(Integer.valueOf(ProperDefault.get("nTotalPrints")) + 1));
        sceneDP.updateBedInfo();

    }

    private void notifyBuildAborted(Date started, Date aborted) {
        assert started != null;
        assert aborted != null;

        long elapsed = aborted.getTime() - started.getTime();

        String message = "Build aborted.\n\n";
        message += "Stopped after "
                + EstimationDriver.getBuildTimeString(elapsed);

        // Highlight the line at which the user aborted...
        int atWhichLine = machineLoader.getMachineInterface().getLinesProcessed();

        BuildStatus p = new BuildStatus();
        p.setVisible(true);
        p.setCompletionMessage(EstimationDriver.getBuildTimeString(elapsed));
    }

    // synchronized public void buildingOver()
    public void buildingOver() {
        message("Done building.");

        // update buttons & menu's
        doPreheat(false);

        building = false;

        setEditorBusy(false);
    }

    class SimulationThread extends Thread {

        MainWindow editor;

        public SimulationThread(MainWindow edit) {
            super("Simulation Thread");

            editor = edit;
        }

        @Override
        public void run() {
            message("Simulating...");
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    simulationOver();
                }
            });
        }
    }

    /**
     * Stops the machine from running, and sets gui to 'no build running' mode
     */
    public void handleStop() {
        Base.writeLog("Stopping ...", this.getClass());

        if (Base.printPaused == false) {
            Base.getMachineLoader().getMachineInterface().killSwitch();
        }

        getMachineInterface().getDriver().dispatchCommand("M112");

        if (getMachineInterface().getDriver().isONShutdown() == true) {
            Base.getMachineLoader().getMachineInterface().stopwatch();
        }

        doStop();
        Base.writeLog("Print stopped ...", this.getClass());
        setEditorBusy(false);
    }

    class EstimationThread extends Thread {

        MainWindow editor;

        public EstimationThread(MainWindow edit) {
            super("Estimation Thread");

            editor = edit;
        }

        @Override
        public void run() {
            message("Estimating...");
            editor.estimationOver();
        }
    }

    public void estimationOver() {
    }

    /**
     * Send stop command to loaded machine, Disables pre-heating, and sets
     * building values to false/off
     */
    public void doStop() {
        if (machineLoader.isLoaded()) {
            machineLoader.getMachineInterface().stopAll();
        }
        doPreheat(false);
        building = false;
        simulating = false;
        buildOnComplete = false;

    }

    public void handleReset() {
        if (machineLoader.isLoaded()) {
            machineLoader.getMachineInterface().reset();
        }
    }

    public void handlePause() {
        doPause();
    }

    /**
     * Pause the applet but don't kill its window.
     */
    public void doPause() {
        if (machineLoader.getMachineInterface().isPaused()) {

            if (simulating) {
                message("Simulating...");
            } else if (building) {
                message("Building...");
            }

        } else {
            int atWhichLine = machineLoader.getMachineInterface().getLinesProcessed();
            message("Paused at line " + atWhichLine + ".");
        }
    }

    /**
     * Called by EditorStatus to complete the job and re-dispatch to handleNew,
     * handleOpenScene, handleQuit.
     */
    public void checkModified2() {

        switch (checkModifiedMode) {
            case HANDLE_NEW:
                handleNew(false);
                break;
            case HANDLE_OPEN:
                handleOpen2Scene(handleOpenPath);
                break;
            case HANDLE_QUIT:
                System.exit(0);
                break;
        }
        checkModifiedMode = 0;
    }

    /**
     * New scene with BEE default model if first time running app.
     *
     * @param shift
     */
    public void handleNew(final boolean shift) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                bed = PrintBed.makePrintBed(null);
                canvas.updateBed(bed);
                sceneDP = new SceneDetailsPanel();
                updateDetailsCenter(sceneDP);
                sceneDP.updateBed(bed);

            }
        });
    }

    /**
     * Add new model to scene
     */
    public void handleNewModel() {
        Base.writeLog("Opening model ...", this.getClass());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String path = null;
                if (path == null) { // "open..." selected from the menu
                    path = selectFile(0);
                    if (path == null) {
                        return;
                    }
                }
                Base.logger.log(Level.INFO, "Loading {0}", path);
                Base.writeLog("Loading " + path + " ...", this.getClass());

                bed.addSTL(new File(path));
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
        });
    }

    /**
     * Prints a G-code file
     */
    public void handleGCodeImport() {
        Base.writeLog("Importing G-code file ...", this.getClass());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String path = null;
                if (path == null) { // "open..." selected from the menu
                    path = selectFile(2);
                    if (path == null) {
                        return;
                    }
                }
                Base.logger.log(Level.INFO, "Loading {0}", path);
                Base.writeLog("Loading " + path + " ...", this.getClass());

                //Adds default print preferences, they aren't going to be used
                //since we're printing from a GCode file
                PrintPreferences prefs = 
                        new PrintPreferences("", FilamentControler.NO_FILAMENT, 
                                20, false, false, path);

                Base.isPrintingFromGCode = true;
                final PrintSplashAutonomous p = new PrintSplashAutonomous(false, prefs);
                p.setVisible(true);

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {

                        if (Base.getMainWindow().getBed().isSceneDifferent()) {
                            Base.getMainWindow().handleSave(true);
                        }
                        p.startConditions();
                    }
                });
            }
        });
    }

    /**
     * This is the implementation of the MRJ open document event, and the
     * Windows XP open document will be routed through this too.
     *
     * @param file
     */
    @Override
    public void handleOpenFile(File file) {
        handleOpenScene(file.getAbsolutePath());
    }

    /**
     * Select File to open. It can have two types
     *
     * @param opt 0 for STL files, 1 for BEE files and 2 for G-code files
     * @return path to file
     */
    private String selectFile(int opt) {
        File directory = null;
        String loadDir = ProperDefault.get("ui.open_dir");

        // Opens at last directory
        if (opt == 0) {
            loadDir = ProperDefault.get("ui.open_dir0");
        }

        if (loadDir != null) {
            directory = new File(loadDir);
        }
        JFileChooser fc = new JFileChooser(directory);
        FileFilter defaultFilter;

        if (opt == 0) {
            String[] extensions = {".stl"};
            fc.addChoosableFileFilter(defaultFilter = new ExtensionFilter(extensions, "STL files"));

            fc.setAcceptAllFileFilterUsed(true);
            fc.setFileFilter(defaultFilter);
            fc.setDialogTitle("Open a model file...");
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            fc.setFileHidingEnabled(false);
            int rv = fc.showOpenDialog(this);
            if (rv == JFileChooser.APPROVE_OPTION) {
                fc.getSelectedFile().getName();
                ProperDefault.put("ui.open_dir0", fc.getCurrentDirectory().getAbsolutePath());
                Base.getMainWindow().getButtons().updatePressedStateButton("models");
                Base.getMainWindow().setEnabled(true);
                return fc.getSelectedFile().getAbsolutePath();
            } else {
                Base.getMainWindow().getButtons().updatePressedStateButton("models");
                Base.getMainWindow().setEnabled(true);
                return null;
            }
        } else if (opt == 1) {
            String[] extensions = {".bee"};
            fc.addChoosableFileFilter(defaultFilter = new ExtensionFilter(extensions, "BEE files"));

            fc.setAcceptAllFileFilterUsed(true);
            fc.setFileFilter(defaultFilter);
            fc.setCurrentDirectory(new File(loadDir));
            fc.setDialogTitle("Open a scene file...");
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            fc.setFileHidingEnabled(false);
            int rv = fc.showOpenDialog(this);
            if (rv == JFileChooser.APPROVE_OPTION) {
                fc.getSelectedFile().getName();
                ProperDefault.put("ui.open_dir", fc.getCurrentDirectory().getAbsolutePath());
                return fc.getSelectedFile().getAbsolutePath();
            } else {
                return null;
            }
        } else if (opt == 2) {
            String[] extensions = {".gcode"};
            fc.addChoosableFileFilter(defaultFilter = new ExtensionFilter(extensions, "G-code files"));

            fc.setAcceptAllFileFilterUsed(true);
            fc.setFileFilter(defaultFilter);
            fc.setCurrentDirectory(new File(loadDir));
            fc.setDialogTitle("Open a G-code file...");
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            fc.setFileHidingEnabled(false);

            int rv = fc.showOpenDialog(this);
            if (rv == JFileChooser.APPROVE_OPTION) {
                fc.getSelectedFile().getName();
                return fc.getSelectedFile().getAbsolutePath();
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * Open a sketch given the full path to the BEE file. Pass in 'null' to
     * prompt the user for the name of the sketch.
     *
     * @param ipath
     */
    public void handleOpenScene(final String ipath) {
        Base.writeLog("Opening scene ...", this.getClass());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String path = ipath;
                if (path == null) { // "open..." selected from the menu
                    path = selectFile(1);
                    if (path == null) {
                        return;
                    }
                }
                Base.logger.log(Level.INFO, "Loading {0}", path);
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
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    /**
     * Second stage of open that calls handleOpenScene after extension
     * validation. Updates also the mruList.
     *
     * @param path
     */
    protected void handleOpen2Scene(String path) {
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
                reloadMruMenu();
            }
            Base.writeLog("Open model: " + path, this.getClass());
        } catch (Exception e) {
            Base.writeLog("Couldn't Open model: " + path, this.getClass());
            error(e);
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Handle Save the Scene. This consists in serializing the PrintBed Object.
     *
     * @param force
     */
    public void handleSave(final boolean force) {
        Runnable saveWork = new Runnable() {
            @Override
            public void run() {
                Base.logger.info("Saving Scene...");
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
                        reloadMruMenu();
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

                } catch (FileNotFoundException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }

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
    public void handleSaveAs() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // TODO: lock sketch?
                Base.logger.info("Saving...");
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
                    reloadMruMenu();

                    ObjectOutputStream oos;
                    try {
                        oos = new ObjectOutputStream(new FileOutputStream(handleOpenPath));
                        oos.writeObject(bed);
                        oos.close();
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
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
                        handleNew(false);
                        newSceneOnDialog = false;
                    }

                }
            }
        });

    }

    /**
     * Quit, but first ask user if it's ok. Also store preferences to disk just
     * in case they want to quit. Final exit() happens in MainWindow since it
     * has the callback from EditorStatus.
     */
    public void handleQuitInternal() {
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
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    handleQuitInternal();
                }
            });
        } catch (InvocationTargetException e) {
            System.exit(0);
        } catch (InterruptedException e) {
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
     * Show an error int the status bar.
     *
     * @param what
     */
    public void error(String what) {
        Base.logger.severe(what);
    }

    public void error(Exception e) {
        if (e == null) {
            Base.logger.severe("MainWindow.error() was passed a null exception.");
            return;
        }

        // not sure if any RuntimeExceptions will actually arrive
        // through here, but gonna check for em just in case.
        String mess = e.getMessage();
        if (mess != null) {
            String rxString = "RuntimeException: ";
            if (mess.indexOf(rxString) == 0) {
                mess = mess.substring(rxString.length());
            }
            String javaLang = "java.lang.";
            if (mess.indexOf(javaLang) == 0) {
                mess = mess.substring(javaLang.length());
            }
        }
        Base.logger.log(Level.SEVERE, mess, e);
    }

    public void message(String msg) {
        Base.logger.info(msg);
    }

    public MachineInterface getMachineInterface() {
        return this.machineLoader.getMachineInterface();
    }

    /**
     * Here we want to: 1. Create a new machine using the given profile name 2.
     * If the new machine uses a serial port, connect to the serial port 3. If
     * this is a new machine, record a reference to it 4. Hook the machine to
     * the main window.
     *
     * @param name name of the machine
     * @param doConnect perform the connect
     */
    public void loadMachine(String name, boolean doConnect) {

        MachineInterface mi = machineLoader.getMachineInterface(name);

        if (mi == null) {
            Base.logger.log(Level.SEVERE, "could not load machine ''{0}'' please check Driver", name);
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
     * TODO: documentation
     *
     * @param name
     * @param doConnect
     */
    public void reloadMachine(String name, boolean doConnect) {
        MachineInterface mi = machineLoader.getMachineInterface(name);

        if (mi == null) {
            Base.logger.log(Level.SEVERE, "could not load machine ''{0}'' please check Driver", name);
            return;
        }

        machineLoader.connect(true); // Just performs a setState 
    }

    @Override
    public void machineProgress(MachineProgressEvent event) {
    }

    @Override
    public void toolStatusChanged(MachineToolStatusEvent event) {
    }
    PrintBed currentElement;

    @Override
    public void stateChanged(ChangeEvent e) {
        // We get a change event when another tab is selected.
//        setCurrentElement(header.getSelectedElement());
    }

    /**
     * Function called automatically when new gcode generation completes does
     * post-processing for newly created gcode
     *
     * @param evt
     */
    @Override
    public void generationComplete(GeneratorEvent evt) {
        // if success, update header and switch to code view
        if (evt.getCompletion() == Completion.SUCCESS) {

            buttons.updateFromMachine(machineLoader.getMachineInterface());

            if (buildOnComplete) {
//                doBuild();
            }
        }

        if (buildOnComplete) // for safety, always reset this
        {
            buildOnComplete = false;
        }
    }

    @Override
    public void updateGenerator(GeneratorEvent evt) {
        // ignore
    }

    /**
     * Selects the last inserted model
     */
    public void selectLastInsertedModel() {

        if (this.getBed().getModels().size() > 0) {
            this.getCanvas().unPickAll();
            Model m = this.getBed().getModel(this.getBed().getModels().size() - 1);
            this.getBed().addPickedModel(m);
            m.getEditer().updateModelPicked();
            this.getCanvas().evaluateModelsBed();
        }
    }

    /**
     * ****************************** LISTENERS
     *
     ******************************
     * @param e
     */
    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void componentResized(ComponentEvent e) {
        //refresh();
//        updateSizeVariables(e.getComponent().getWidth(), e.getComponent().getHeight());
//        camCtrl.setLocation();
//        updateGUI();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
//        camCtrl.setLocation();
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
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
        deactivateCameraControls();
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
//        camCtrl = new CameraControl(this, false);
////        camCtrl.setLocation();
//        if (!messagesPP.isVisible()) {
//            activateCameraControls();
//        }
    }

    @Override
    public void windowActivated(WindowEvent e) {
//        if (!messagesPP.isVisible()) {
//            activateCameraControls();
//        }
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        deactivateCameraControls();
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
    }
}
