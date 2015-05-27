package replicatorg.model;

import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.picking.*;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.*;
import javax.swing.JPanel;
import javax.vecmath.*;
import net.miginfocom.swing.MigLayout;
import replicatorg.app.Base;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.mainWindow.ModelsDetailsPanel;
import replicatorg.app.ui.mainWindow.ModelsOperationCenter;
import replicatorg.app.ui.mainWindow.ModelsOperationCenterScale;
import replicatorg.app.ui.mainWindow.SceneDetailsPanel;
import replicatorg.app.ui.modeling.EditingModel;
import replicatorg.app.ui.modeling.Tool;
import replicatorg.app.ui.modeling.ToolPanel;
import replicatorg.machine.Machine;
import replicatorg.machine.MachineInterface;
import replicatorg.machine.model.BuildVolume;
import replicatorg.machine.model.MachineModel;
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
public class CAMPanel extends MouseAdapter implements MouseListener, Cloneable {

    private Raster drawRaster;
    private OffScreenCanvas3D c;
    private SimpleUniverse universe;
    private Canvas3D canvas;
    private BranchGroup branchRoot;
    private BoundingSphere bounds;
    private PickCanvas pickCanvas;
    private JPanel panel;
    private PrintBed bed;
    private String modelationType;
    BuildVolume buildVol;
    Tool currentTool = null;
    ToolPanel toolPanel;
    private static Vector3d CAMERA_TRANSLATION_DEFAULT = new Vector3d(0, 0, 300);
    private final static double ELEVATION_ANGLE_DEFAULT = 1; // 1 rad = 57 degrees
    private final static double TURNTABLE_ANGLE_DEFAULT = 0;
//    private final static double CAMERA_DISTANCE_DEFAULT = 300d; // 30cm
    private Vector3d cameraTranslation = new Vector3d(CAMERA_TRANSLATION_DEFAULT);
    private double elevationAngle = ELEVATION_ANGLE_DEFAULT;
    private double turntableAngle = TURNTABLE_ANGLE_DEFAULT;
    private ModelsDetailsPanel mDP;
    private SceneDetailsPanel sDP;
    final double wireBoxCoordinates[] = {
        0, 0, 0, 0, 0, 1,
        0, 1, 0, 0, 1, 1,
        1, 1, 0, 1, 1, 1,
        1, 0, 0, 1, 0, 1,
        0, 0, 0, 0, 1, 0,
        0, 0, 1, 0, 1, 1,
        1, 0, 1, 1, 1, 1,
        1, 0, 0, 1, 1, 0,
        0, 0, 0, 1, 0, 0,
        0, 0, 1, 1, 0, 1,
        0, 1, 1, 1, 1, 1,
        0, 1, 0, 1, 1, 0,};

    @Override
    public void mouseClicked(MouseEvent e) {
        PolygonAttributes polygonA = null;
        pickCanvas.setShapeLocation(e);
        PickResult result = pickCanvas.pickClosest();

        Node n;

        if (result == null) {
//            System.out.println("Nothing picked");
        } else {
            n = result.getObject();
//            System.out.println("Result Name = "+n.getName());
            Primitive p = (Primitive) result.getNode(PickResult.PRIMITIVE);
            Shape3D s;
            s = (Shape3D) result.getNode(PickResult.SHAPE3D);

//            System.out.println("Picked model1: " + s.getUserData());
            if (s.getUserData() != null) {
                Model model = bed.getModel(s);
//                System.out.println("Model= " + model);
                if (model != null) {

                    polygonA = model.getPolygonAttributes();
                    if (polygonA.getPolygonMode() == 2) {
//                        renderCanvas();
                        unPickAll();
                        model.setPolygonAttributes(PolygonAttributes.POLYGON_LINE);
                        model.getEditer().updateModelPicked();
//                        rebuildScene();      
                        bed.addPickedModel(model);
                        evaluateModelsBed();
//                        redrawBoundingBox(model,1);

                    } else {

                        model.setPolygonAttributes(PolygonAttributes.POLYGON_FILL);
                        model.getEditer().updateModelUnPicked();
//                        rebuildScene();
                        bed.removePickedModel(model);
                        evaluateModelsBed();
//                        redrawBoundingBox(model,0);
                    }
                }
            } else {
//                System.out.println("null");
            }

        }
    }

    public enum DragMode {

        NONE,
        ROTATE_VIEW,
        TRANSLATE_VIEW,
        ROTATE_OBJECT,
        SCALE_OBJECT,
        TRANSLATE_OBJECT,
        CONTROL_VIEW
    };

    public CAMPanel(JPanel cardPanel, PrintBed bed) {
        this.bed = bed;
        this.panel = new JPanel();
        this.bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000000.0);
        this.panel.setLayout(new MigLayout(""));
        this.panel.setOpaque(false);
        createCanvas(cardPanel.getSize());
        createUniverse();
        this.panel.add(canvas, "dock west");
        this.toolPanel = new ToolPanel(this);
        modelationType = "move";
        createScene();
        this.canvas.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                updateVP();

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        if (Base.getMainWindow().getBed().getPickedModels().size() > 0) {
                            if (modelationType.equals("move")) {
                                boolean advOption = Base.getMainWindow().getCanvas().getControlTool(1).getAdvOption();
                                if (!advOption) {
                                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().translateObject(0, Math.PI / 2, 0);
                                } else {
                                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().translateObject(0, 5, 0);
                                }
                            }
                            if (modelationType.equals("rotate")) {
                                boolean advOption = Base.getMainWindow().getCanvas().getControlTool(2).getAdvOption();
                                if (!advOption) {
                                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().rotateObject(new AxisAngle4d(1d, 0d, 0d, -Math.PI / 2));
                                } else {
                                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().rotateObject(new AxisAngle4d(1d, 0d, 0d, -0.087));
                                }
                            }
                            if (modelationType.equals("scale")) {
                                boolean modelOnPlatform = Base.getMainWindow().getBed().getFirstPickedModel().getEditer().isOnPlatform();
                                Model model = Base.getMainWindow().getBed().getFirstPickedModel();
                                EditingModel editModel = model.getEditer();
                                double value = model.getXscalePercentage() + 5;
                                editModel.scaleX(value, modelOnPlatform, true);
                                editModel.scaleY(value, modelOnPlatform, true);
                                editModel.scaleZ(value, modelOnPlatform, true);
                                String val = String.format("%3.1f", value);

                                ModelsOperationCenterScale mOCS = Base.getMainWindow().getCanvas().getControlTool(3).getModelsScaleCenter();

                                DecimalFormat df = new DecimalFormat("#.00");

                                double width = 0, depth = 0, height = 0;

                                if (ProperDefault.get("measures").equals("inches")) {
                                    width = Units_and_Numbers.millimetersToInches(width);
                                    depth = Units_and_Numbers.millimetersToInches(depth);
                                    height = Units_and_Numbers.millimetersToInches(height);
                                } else if (ProperDefault.get("measures").equals("mm")) {
                                    width = model.getEditer().getWidth();
                                    depth = model.getEditer().getDepth();
                                    height = model.getEditer().getHeight();
                                }

                                mOCS.setXValue(df.format(width));
                                mOCS.setYValue(df.format(depth));
                                mOCS.setZValue(df.format(height));

                            } else if (modelationType.equals("mirror")) {
                                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().mirrorZ();
                            }
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if (Base.getMainWindow().getBed().getPickedModels().size() > 0) {
                            if (modelationType.equals("move")) {
                                boolean advOption = Base.getMainWindow().getCanvas().getControlTool(1).getAdvOption();
                                if (!advOption) {
                                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().translateObject(0, -Math.PI / 2, 0);
                                } else {
                                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().translateObject(0, -5, 0);
                                }
                            }
                            if (modelationType.equals("rotate")) {
                                boolean advOption = Base.getMainWindow().getCanvas().getControlTool(2).getAdvOption();
                                if (!advOption) {
                                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().rotateObject(new AxisAngle4d(1d, 0d, 0d, Math.PI / 2));
                                } else {
                                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().rotateObject(new AxisAngle4d(1d, 0d, 0d, 0.087));
                                }
                            }
                                                        if (modelationType.equals("scale")) {
                                boolean modelOnPlatform = Base.getMainWindow().getBed().getFirstPickedModel().getEditer().isOnPlatform();
                                Model model = Base.getMainWindow().getBed().getFirstPickedModel();
                                EditingModel editModel = model.getEditer();
                                double value = model.getXscalePercentage() + 5;
                                editModel.scaleX(value, modelOnPlatform, true);
                                editModel.scaleY(value, modelOnPlatform, true);
                                editModel.scaleZ(value, modelOnPlatform, true);
                                String val = String.format("%3.1f", value);

                                ModelsOperationCenterScale mOCS = Base.getMainWindow().getCanvas().getControlTool(3).getModelsScaleCenter();

                                DecimalFormat df = new DecimalFormat("#.00");

                                double width = 0, depth = 0, height = 0;

                                if (ProperDefault.get("measures").equals("inches")) {
                                    width = Units_and_Numbers.millimetersToInches(width);
                                    depth = Units_and_Numbers.millimetersToInches(depth);
                                    height = Units_and_Numbers.millimetersToInches(height);
                                } else if (ProperDefault.get("measures").equals("mm")) {
                                    width = model.getEditer().getWidth();
                                    depth = model.getEditer().getDepth();
                                    height = model.getEditer().getHeight();
                                }

                                mOCS.setXValue(df.format(width));
                                mOCS.setYValue(df.format(depth));
                                mOCS.setZValue(df.format(height));

                            }
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (Base.getMainWindow().getBed().getPickedModels().size() > 0) {
                            if (modelationType.equals("move")) {
                                boolean advOption = Base.getMainWindow().getCanvas().getControlTool(1).getAdvOption();
                                if (!advOption) {
                                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().translateObject(Math.PI / 2, 0, 0);
                                } else {
                                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().translateObject(5, 0, 0);
                                }
                            }
                            if (modelationType.equals("rotate")) {
                                boolean advOption = Base.getMainWindow().getCanvas().getControlTool(2).getAdvOption();
                                if (!advOption) {
                                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().rotateObject(new AxisAngle4d(0d, 1d, 0d, Math.PI / 2));
                                } else {
                                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().rotateObject(new AxisAngle4d(0d, 1d, 0d, 0.087));
                                }
                            }
                            if (modelationType.equals("mirror")) {
                                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().mirrorY();
                            }
                            if (modelationType.equals("scale")) {
                                boolean modelOnPlatform = Base.getMainWindow().getBed().getFirstPickedModel().getEditer().isOnPlatform();
                                Model model = Base.getMainWindow().getBed().getFirstPickedModel();
                                EditingModel editModel = model.getEditer();
                                double value = model.getXscalePercentage() + 5;
                                editModel.scaleX(value, modelOnPlatform, true);
                                editModel.scaleY(value, modelOnPlatform, true);
                                editModel.scaleZ(value, modelOnPlatform, true);
                                String val = String.format("%3.1f", value);

                                ModelsOperationCenterScale mOCS = Base.getMainWindow().getCanvas().getControlTool(3).getModelsScaleCenter();

                                DecimalFormat df = new DecimalFormat("#.00");

                                double width = 0, depth = 0, height = 0;

                                if (ProperDefault.get("measures").equals("inches")) {
                                    width = Units_and_Numbers.millimetersToInches(width);
                                    depth = Units_and_Numbers.millimetersToInches(depth);
                                    height = Units_and_Numbers.millimetersToInches(height);
                                } else if (ProperDefault.get("measures").equals("mm")) {
                                    width = model.getEditer().getWidth();
                                    depth = model.getEditer().getDepth();
                                    height = model.getEditer().getHeight();
                                }

                                mOCS.setXValue(df.format(width));
                                mOCS.setYValue(df.format(depth));
                                mOCS.setZValue(df.format(height));

                            }
                        }
                        break;
                    case KeyEvent.VK_LEFT:
                        if (Base.getMainWindow().getBed().getPickedModels().size() > 0) {
                            if (modelationType.equals("move")) {
                                boolean advOption = Base.getMainWindow().getCanvas().getControlTool(1).getAdvOption();
                                if (!advOption) {
                                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().translateObject(-Math.PI / 2, 0, 0);
                                } else {
                                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().translateObject(-5, 0, 0);
                                }
                            }
                            if (modelationType.equals("rotate")) {
                                boolean advOption = Base.getMainWindow().getCanvas().getControlTool(2).getAdvOption();
                                if (!advOption) {
                                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().rotateObject(new AxisAngle4d(0d, 1d, 0d, -Math.PI / 2));
                                } else {
                                    Base.getMainWindow().getBed().getFirstPickedModel().getEditer().rotateObject(new AxisAngle4d(0d, 1d, 0d, -0.087));
                                }
                            }
                            if (modelationType.equals("mirror")) {
                                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().mirrorX();
                            }
                                                        if (modelationType.equals("scale")) {
                                boolean modelOnPlatform = Base.getMainWindow().getBed().getFirstPickedModel().getEditer().isOnPlatform();
                                Model model = Base.getMainWindow().getBed().getFirstPickedModel();
                                EditingModel editModel = model.getEditer();
                                double value = model.getXscalePercentage() + 5;
                                editModel.scaleX(value, modelOnPlatform, true);
                                editModel.scaleY(value, modelOnPlatform, true);
                                editModel.scaleZ(value, modelOnPlatform, true);
                                String val = String.format("%3.1f", value);

                                ModelsOperationCenterScale mOCS = Base.getMainWindow().getCanvas().getControlTool(3).getModelsScaleCenter();

                                DecimalFormat df = new DecimalFormat("#.00");

                                double width = 0, depth = 0, height = 0;

                                if (ProperDefault.get("measures").equals("inches")) {
                                    width = Units_and_Numbers.millimetersToInches(width);
                                    depth = Units_and_Numbers.millimetersToInches(depth);
                                    height = Units_and_Numbers.millimetersToInches(height);
                                } else if (ProperDefault.get("measures").equals("mm")) {
                                    width = model.getEditer().getWidth();
                                    depth = model.getEditer().getDepth();
                                    height = model.getEditer().getHeight();
                                }

                                mOCS.setXValue(df.format(width));
                                mOCS.setYValue(df.format(depth));
                                mOCS.setZValue(df.format(height));

                            }
                        }
                        break;
                    case KeyEvent.VK_DELETE:
                        Base.getMainWindow().handleCAMDelete();
                        break;
                    case KeyEvent.VK_BACK_SPACE:
                        Base.getMainWindow().handleCAMDelete();
                        break;
                    case KeyEvent.VK_ENTER:
                        break;

                }
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });
        this.branchRoot.compile();
        this.universe.addBranchGraph(branchRoot);
        this.panel.addKeyListener(toolPanel);
        this.canvas.addMouseListener(this);
        this.pickCanvas = new PickCanvas(canvas, branchRoot);
        this.pickCanvas.setMode(PickTool.BOUNDS);
        this.pickCanvas.setTolerance(10);
        updateVP();
    }

    public JPanel getPanel() {
        return panel;
    }

    public void canvasToFront() {
        this.canvas.requestFocus();
        this.canvas.requestFocusInWindow();
    }

    public void evaluateModelsBed() {
        if (bed.getNumberPickedModels() > 0) {
            mDP = new ModelsDetailsPanel();
            mDP.updateBed(bed);
            Base.getMainWindow().updateDetailsCenter(mDP);
            Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenter());
        } else {
            sDP = new SceneDetailsPanel();
            sDP.updateBed(bed);
            setModelationType("move");
            Base.getMainWindow().updateDetailsCenter(sDP);
            Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenter());
        }
    }

    public ModelsDetailsPanel getModelsPanel() {
        return mDP;
    }

    public Model getModel() {
        return bed.getFirstPickedModel();
    }

    public EditingModel getModelEditing() {
        if (getModel() == null) {
            return new EditingModel();
        }

        return getModel().getEditer();
    }

    public void updateBed(PrintBed bed) {
        this.bed = bed;
//        renderCanvas();
        rebuildScene();
    }

    public void updateBedImportedModels(PrintBed bed) {
        this.bed = bed;
//        renderCanvas();

        branchRoot.detach();

        if (!bed.isEmpty()) {
            setModel(bed.getModel(bed.getNumberModels() - 1));
        }

        branchRoot.compile();
        universe.addBranchGraph(branchRoot);
        this.pickCanvas = new PickCanvas(canvas, branchRoot);

    }

    public void updateBedDeletedModels(PrintBed bed) {
        this.bed = bed;
//        renderCanvas();

        branchRoot.detach();

        if (!bed.isEmpty()) {
            removeModel(bed.getFirstPickedModel());
        }

        branchRoot.compile();
        universe.addBranchGraph(branchRoot);
        this.pickCanvas = new PickCanvas(canvas, branchRoot);

    }

    public void pickAll() {
        if (bed.getNumberModels() != bed.getNumberPickedModels()) {
            for (int i = 0; i < bed.getNumberModels(); i++) {
                Model model = bed.getModel(i);
                if (!bed.getPickedModels().contains(model)) {
//                    System.out.println("Now picked "+model.getShape().getUserData());
                    model.setPolygonAttributes(PolygonAttributes.POLYGON_LINE);
                    model.getEditer().updateModelPicked();
                    bed.addPickedModel(model);
//                    System.out.println("*********** Models added ***********");
//                    System.err.println(model.getShape().getUserData());
//                    rebuildScene();
                }
            }
        }
    }

    public void unPickAll() {

        for (int i = 0; i < bed.getNumberModels(); i++) {
            Model model = bed.getModel(i);
            model.setPolygonAttributes(PolygonAttributes.POLYGON_FILL);
            model.getEditer().updateModelColor();
            bed.removePickedModel(model);
//            rebuildScene();

        }
    }

    private void getBuildVolume() {
        MachineInterface mc = Base.getMachineLoader().getMachineInterface();
        if (mc instanceof Machine) {
            MachineModel mm = mc.getModel();
            buildVol = mm.getBuildVolume();
        }
    }

    public Tool getControlTool(int index) {
        return toolPanel.getTool(index);
    }

    public void updateTool() {
        getControlTool(1).updateLockHeight();
    }

    public void setControlTool(int index) {
        if (index == 0) {
            toolPanel.setTool(index);
        }
        if (index == 1) {
            toolPanel.setTool(index);
            //this.setTool(new MoveTool(toolPanel));
        }
        if (index == 2) {
            toolPanel.setTool(index);
            //this.setTool(new RotationTool(toolPanel));
        }
        if (index == 3) {
            toolPanel.setTool(index);
            //this.setTool(new ScalingTool(toolPanel));
        } else {
            toolPanel.setTool(index);
            //this.setTool(new MirrorTool(toolPanel));
        }
    }

    public void setModelationType(String type) {
        this.modelationType = type;
    }

    public void setTool(Tool tool) {
        if (currentTool == tool) {
            return;
        }
        if (currentTool != null) {
            if (currentTool instanceof MouseListener) {
                canvas.removeMouseListener((MouseListener) currentTool);
            }
            if (currentTool instanceof MouseMotionListener) {
                canvas.removeMouseMotionListener((MouseMotionListener) currentTool);
            }
            if (currentTool instanceof MouseWheelListener) {
                canvas.removeMouseWheelListener((MouseWheelListener) currentTool);
            }
            if (currentTool instanceof KeyListener) {
                canvas.removeKeyListener((KeyListener) currentTool);
            }
        }
        currentTool = tool;
        if (currentTool != null) {
            if (currentTool instanceof MouseListener) {
                canvas.addMouseListener((MouseListener) currentTool);
            }
            if (currentTool instanceof MouseMotionListener) {
                canvas.addMouseMotionListener((MouseMotionListener) currentTool);
            }
            if (currentTool instanceof MouseWheelListener) {
                canvas.addMouseWheelListener((MouseWheelListener) currentTool);
            }
            if (currentTool instanceof KeyListener) {
                canvas.addKeyListener((KeyListener) currentTool);
            }
        }
    }

    public void adjustViewAngle(double deltaYaw, double deltaPitch) {
        turntableAngle += deltaYaw;
        elevationAngle += deltaPitch;
        updateVP();
    }

    public void adjustViewTranslation(double deltaX, double deltaY) {
        cameraTranslation.x += deltaX;
        cameraTranslation.y += deltaY;
        updateVP();
    }

    public void adjustZoom(double deltaZoom) {
        if (cameraTranslation.z + deltaZoom < 1060.0 && cameraTranslation.z + deltaZoom > 10.0) {
            cameraTranslation.z += deltaZoom;
            updateVP();
        }
    }

    public void resetView() {
        cameraTranslation.set(CAMERA_TRANSLATION_DEFAULT);
        elevationAngle = ELEVATION_ANGLE_DEFAULT;
        turntableAngle = TURNTABLE_ANGLE_DEFAULT;
        adjustZoom(-(cameraTranslation.z - 300));
        updateVP();
    }

    public void rebuildScene() {
        branchRoot.removeAllChildren();
        branchRoot.detach();
        createScene();
        importBedModels();
        branchRoot.compile();
        universe.addBranchGraph(branchRoot);
        this.pickCanvas = new PickCanvas(canvas, branchRoot);
    }

    public void setModel(Model model) {
        BranchGroup branchGroup = model.getGroup();
        branchRoot.addChild(branchGroup);
    }

    private void removeModel(Model model) {
        BranchGroup branchGroup = model.getGroup();
        branchRoot.removeChild(branchGroup);
    }

    public void redrawBoundingBox(Model model, int opt) {
        if (opt == 1) {
//            renderCanvas();
//            branchRoot.removeAllChildren();
            Node n = model.getEditer().getBoundingBoxGroup();
//            Group m = makeLabel("Test Label", new Vector3d(new Vector3d(model.getEditer().getLowerPoint3D().x,
//                    model.getEditer().getLowerPoint3D().y-10,
//                    model.getEditer().getLowerPoint3D().z)));           
            branchRoot.detach();
//            createScene();
//            importBedModels();

            if (n != null) {
                branchRoot.removeChild(model.getEditer().getBoundingBoxGroup());
            }

            model.getEditer().makeBoundingBox();
            n = model.getEditer().getBoundingBoxGroup();
            branchRoot.addChild(n);
//            branchRoot.addChild(m);
            branchRoot.compile();
            universe.addBranchGraph(branchRoot);
            this.pickCanvas = new PickCanvas(canvas, branchRoot);
        } else if (opt == 0) {
            Node n = model.getEditer().getBoundingBoxGroup();
//            renderCanvas();
            branchRoot.removeChild(n);
//            rebuildScene();
        }

    }

    private void createCanvas(Dimension d) {
//        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
//        template.setSceneAntialiasing(GraphicsConfigTemplate3D.REQUIRED);
//        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().
//        getDefaultScreenDevice().getBestConfiguration(template);
//
//        BufferedImage bImage = new BufferedImage(1000, 1000,
//        BufferedImage.TYPE_INT_ARGB);
//
//    ImageComponent2D buffer = new ImageComponent2D(
//        ImageComponent.FORMAT_RGBA, bImage);
//    buffer.setCapability(ImageComponent2D.ALLOW_IMAGE_READ);
//
//     drawRaster = new Raster(new Point3f(0.0f, 0.0f, 0.0f),
//        Raster.RASTER_COLOR, 0, 0, 1000, 1000, buffer, null);
//
//    drawRaster.setCapability(Raster.ALLOW_IMAGE_WRITE);
//        
//        
//        canvas = new OnScreenCanvas3D(gc,false);
//        //canvas.setSize((int)((d.getWidth()-200-265)/2),(int)(d.getHeight()/2));
////        canvas.setFocusable(true);
////        canvas.setDoubleBufferEnable(true);
//         
////        Map map = canvas.queryProperties();
////        for(int i = 0; i < map.size(); i++)
////        {
////            System.out.println(map.keySet().toArray()[i] + " " + map.values().toArray()[i]);
////        } 
////        canvas.requestFocus();
//        
//         c = new OffScreenCanvas3D(gc, true, drawRaster);
//        // set the offscreen to match the onscreen
//          Screen3D sOn = canvas.getScreen3D();
//          Screen3D sOff = c.getScreen3D();
//          sOff.setSize(sOn.getSize());
//          sOff.setPhysicalScreenWidth(sOn.getPhysicalScreenWidth());
//          sOff.setPhysicalScreenHeight(sOn.getPhysicalScreenHeight());    

        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        canvas = new Canvas3D(config);
        //canvas.setSize((int)((d.getWidth()-200-265)/2),(int)(d.getHeight()/2));
        canvas.setFocusable(true);
        canvas.requestFocus();

    }

    public void setCanvasSize(Dimension d) {
//        panel.setSize((int)((d.getWidth())-200-261),(int)(d.getHeight()));
        if (!Base.isMacOS()) {
            canvas.setSize((int) ((d.getWidth()) - 200 - 200), (int) (d.getHeight()));
        } else {
            canvas.setSize((int) ((d.getWidth()) - 200 - 205), (int) (d.getHeight()));
        }

        updateVP();
    }

    public void setCanvasSizePrintSplash(Dimension d) {
        canvas.setSize((int) ((d.getWidth())), (int) (d.getHeight()));
        updateVP();
    }

    private void createScene() {
        getBuildVolume();
        branchRoot = new BranchGroup();
        branchRoot.setCapability(BranchGroup.ALLOW_DETACH);
        branchRoot.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        branchRoot.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        branchRoot.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        branchRoot.setCapability(BranchGroup.ALLOW_BOUNDS_READ);
        branchRoot.setCapability(BranchGroup.ALLOW_BOUNDS_WRITE);
        BranchGroup branchGroup = new BranchGroup();
        branchGroup.setCapability(BranchGroup.ALLOW_DETACH);
        branchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        branchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        branchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        branchGroup.addChild(makeAmbientLight());
        branchGroup.addChild(makeDirectedLight1());
        branchGroup.addChild(makeDirectedLight2());
        //branchGroup.addChild(makeBoundingBox());
        branchGroup.addChild(makeBackground());
        branchGroup.addChild(makeBaseGrid());
//        branchGroup.addChild(makeCoordinateSystem());
//        Shape3D shape = new Shape3D(drawRaster);
//        branchGroup.addChild(shape);
        branchGroup.setPickable(false);

//        ColorCube cube = new ColorCube(10);
//        PolygonAttributes polyAttribs = new PolygonAttributes( PolygonAttributes.POLYGON_LINE, PolygonAttributes.CULL_NONE, 0 );
//        Appearance app = new Appearance();
//        app.setPolygonAttributes(polyAttribs );
//        cube.setAppearance(app);
//        branchGroup.addChild(cube);

        branchRoot.addChild(branchGroup);

    }

    private void renderCanvas() {
        c.print(false);
    }

    private void createUniverse() {
        universe = new SimpleUniverse(canvas);

        // attach the same view to the offscreen canvas
//          View v = universe.getViewer().getView();
//          v.addCanvas3D(c);
//            // tell onscreen about the offscreen so it knows to
//            // render to the offscreen at postswap
//            canvas.setOffScreenCanvas(c);
//
//            v.stopView();
//            // Make sure that image are render completely
//            // before grab it in postSwap().
//            canvas.setImageReady();
//            v.startView();  

        universe.getViewingPlatform().setNominalViewingTransform();
        universe.getViewer().getView().setSceneAntialiasingEnable(true);
        universe.getViewer().getView().setFrontClipDistance(1d);
        universe.getViewer().getView().setBackClipDistance(1000d);
        universe.getViewer().getView().setMinimumFrameCycleTime(10);
        updateVP();
    }

    public void importBedModels() {
        for (int i = 0; i < bed.getNumberModels(); i++) {
            // REDSOFT: This causes bug
//            bed.getModel(i).getEditer().evaluateModelOutOfBounds();

//            System.out.println("ImportBedModel = "+bed.getModel(i).getShape().getUserData().toString());
            setModel(bed.getModel(i));
        }
    }

    public Appearance createWireframeAppearance() {
        Appearance app = new Appearance();
        Color awtColor = new Color(255, 255, 0);// use any Color you like
        Color3f color = new Color3f(awtColor);
        ColoringAttributes ca = new ColoringAttributes();
        ca.setColor(color);
        app.setColoringAttributes(ca);
        PolygonAttributes pa = new PolygonAttributes();
        pa.setPolygonMode(pa.POLYGON_LINE);
        pa.setCullFace(pa.CULL_NONE);
        app.setPolygonAttributes(pa);
        return app;
    }

    private void updateVP() {
        TransformGroup viewTG = universe.getViewingPlatform().getViewPlatformTransform();
        Transform3D t3d = new Transform3D();
        Transform3D trans = new Transform3D();
        Transform3D rotZ = new Transform3D();
        Transform3D rotX = new Transform3D();
        trans.setTranslation(cameraTranslation);
        Transform3D drop = new Transform3D();
        Transform3D raise = new Transform3D();
        drop.setTranslation(new Vector3d(0, 0, 0));
        raise.invert(drop);
        rotX.rotX(elevationAngle);
        rotZ.rotZ(turntableAngle);
        t3d.mul(drop);
        t3d.mul(rotZ);
        t3d.mul(rotX);
        t3d.mul(raise);
        t3d.mul(trans);
        viewTG.setTransform(t3d);

    }

    public Transform3D getViewTransform() {
        TransformGroup viewTG = universe.getViewingPlatform().getViewPlatformTransform();
        Transform3D t = new Transform3D();
        viewTG.getTransform(t);
        return t;
    }

    public void viewXY() {
        turntableAngle = 0d;
        elevationAngle = 0d;
        updateVP();
    }

    public void viewYZ() {
        turntableAngle = Math.PI / 2;
        elevationAngle = Math.PI / 2;
        updateVP();
    }

    public void viewXZ() {
        turntableAngle = 0d;
        elevationAngle = Math.PI / 2;
        updateVP();
    }

    public void usePerspective(boolean perspective) {
        universe.getViewer().getView().setProjectionPolicy(perspective ? View.PERSPECTIVE_PROJECTION : View.PARALLEL_PROJECTION);
    }

    public Node makeAmbientLight() {
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new Color3f(0.5f, 0.5f, 0.5f));
        ambient.setInfluencingBounds(bounds);
        return ambient;
    }

    public Node makeDirectedLight1() {
        Color3f color = new Color3f(0.5f, 0.5f, 0.5f);
        Vector3f direction = new Vector3f(1f, 0.7f, -0.2f);
        DirectionalLight light = new DirectionalLight(color, direction);
        light.setInfluencingBounds(bounds);
        return light;
    }

    public Node makeDirectedLight2() {
        Color3f color = new Color3f(0.5f, 0.5f, 0.5f);
        Vector3f direction = new Vector3f(-1f, -0.7f, -0.2f);
        DirectionalLight light = new DirectionalLight(color, direction);
        light.setInfluencingBounds(bounds);
        return light;
    }

    public Shape3D makeBoxFrame(Point3d ll, Vector3d dim) {
        Appearance edges = new Appearance();
        edges.setLineAttributes(new LineAttributes(2, LineAttributes.PATTERN_SOLID, true));
        edges.setColoringAttributes(new ColoringAttributes(1f, .796f, .0196f, ColoringAttributes.NICEST));
        double[] coords = new double[wireBoxCoordinates.length];
        for (int i = 0; i < wireBoxCoordinates.length;) {
            coords[i] = (wireBoxCoordinates[i] * dim.x) + ll.x;
            i++;
            coords[i] = (wireBoxCoordinates[i] * dim.y) + ll.y;
            i++;
            coords[i] = (wireBoxCoordinates[i] * dim.z) + ll.z;
            i++;
        }
        LineArray wires = new LineArray(wireBoxCoordinates.length / 3, GeometryArray.COORDINATES);
        wires.setCoordinates(0, coords);

        return new Shape3D(wires, edges);
    }
    Font3D labelFont = null;

    public BranchGroup makeLabel(String s, Vector3d where) {
        if (labelFont == null) {
            labelFont = new Font3D(Font.decode("Arial"), new FontExtrusion());
        }
        Text3D text = new Text3D(labelFont, s);

        Color3f eColor = new Color3f(0.0f, 0.0f, 0.0f);

        Material m = new Material(eColor, eColor, eColor, eColor, 100.0f);
        Appearance a = new Appearance();
        m.setLightingEnable(true);
        a.setMaterial(m);

        BranchGroup group = new BranchGroup();
        TransformGroup tg = new TransformGroup();
        Transform3D transform = new Transform3D();
        transform.setTranslation(where);
        tg.setTransform(transform);
        OrientedShape3D os = new OrientedShape3D();
        os.setAlignmentAxis(0.0f, 0.0f, 1.0f);
        os.setAlignmentMode(OrientedShape3D.ROTATE_ABOUT_POINT);
        os.setConstantScaleEnable(true);
        os.setScale(0.025);
        os.setGeometry(text);

        os.setAppearance(a);

        tg.addChild(os);
        group.addChild(tg);
        return group;
    }

    public Group makeAxes(Point3d origin) {
        Group g = new Group();
        g.addChild(makeLabel("X", new Vector3d(0, 0, 0)));
        g.addChild(makeLabel("Y", new Vector3d(0, 0, 0)));
        g.addChild(makeLabel("Z", new Vector3d(0d, 0d, 0d)));
        return g;
    }

    private void loadPoint(Point3d point, double[] array, int idx) {
        array[idx] = point.x;
        array[idx + 1] = point.y;
        array[idx + 2] = point.z;
    }

    private Shape3D makePlatform(Point3d lower, Point3d upper) {
        Color3f color = new Color3f(0.847f, 0.949f, 0.957f);
        ColoringAttributes ca = new ColoringAttributes();
        ca.setColor(color);
        Appearance solid = new Appearance();
        solid.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, 0.13f));
        //solid.setMaterial(m);
        solid.setColoringAttributes(ca);
        PolygonAttributes pa = new PolygonAttributes();
        pa.setPolygonMode(PolygonAttributes.POLYGON_FILL);
        pa.setCullFace(PolygonAttributes.CULL_NONE);
        pa.setBackFaceNormalFlip(true);
        solid.setPolygonAttributes(pa);

        double[] coords = new double[4 * 3];
        loadPoint(lower, coords, 0);
        loadPoint(new Point3d(lower.x, upper.y, upper.z), coords, 3);
        loadPoint(upper, coords, 6);
        loadPoint(new Point3d(upper.x, lower.y, upper.z), coords, 9);

        QuadArray plat = new QuadArray(4, GeometryArray.COORDINATES);
        plat.setCoordinates(0, coords);

        return new Shape3D(plat, solid);

    }

    public Node makeBackground() {
        //TODO: Color value is hardcoded here |v| danger
        Color backgroundColor = new Color(0xFF, 0xFF, 0xFF);
        //Color backgroundColor = new Color(169,214,219);
        ProperDefault.put("ui.backgroundColor", String.valueOf(backgroundColor.getRGB()));
//		Base.preferences.putInt("ui.backgroundColor", backgroundColor.getRGB());
//		Base.preferences.getInt("ui.backgroundColor",backgroundColor.getRGB());		
        Background bg = new Background(backgroundColor.getRed() / 255f, backgroundColor.getGreen() / 255f, backgroundColor.getBlue() / 255f);
        bg.setApplicationBounds(bounds);
        return bg;
    }

    private Node makeBaseGrid() {
        if (buildVol instanceof BuildVolume) {
            Group baseGroup = new Group();
            double gridSpacing = 10.0; // Dim grid has hash marks at 10mm intervals.
            // Set up the appearance object for the central crosshairs.
            Appearance crosshairAppearance = new Appearance();
            crosshairAppearance.setLineAttributes(new LineAttributes(2f, LineAttributes.PATTERN_SOLID, true));
            crosshairAppearance.setColoringAttributes(new ColoringAttributes(.694f, .694f, .694f, ColoringAttributes.NICEST));
            // Set up the crosshair lines
            LineArray crosshairLines = new LineArray(2 * 2, GeometryArray.COORDINATES);
            crosshairLines.setCoordinate(0, new Point3d(0, -buildVol.getY() / 2, 0));
            crosshairLines.setCoordinate(1, new Point3d(0, buildVol.getY() / 2, 0));
            crosshairLines.setCoordinate(2, new Point3d(-buildVol.getX() / 2, 0, 0));
            crosshairLines.setCoordinate(3, new Point3d(buildVol.getX() / 2, 0, 0));
            Shape3D crosshairs = new Shape3D(crosshairLines, crosshairAppearance);

            // Set up the appearance object for the measurement hash marks.
            Appearance hashAppearance = new Appearance(); // color float value = rgv_value/255
            hashAppearance.setLineAttributes(new LineAttributes(2f, LineAttributes.PATTERN_SOLID, true));
            hashAppearance.setColoringAttributes(new ColoringAttributes(.694f, .694f, .694f, ColoringAttributes.NICEST));
            // hashes in each direction on x axis
            int xHashes = (int) ((buildVol.getX() - 0.0001) / (2 * gridSpacing));
            // hashes in each direction on y axis
            int yHashes = (int) ((buildVol.getY() - 0.0001) / (2 * gridSpacing));
            // Set up hash lines
            LineArray hashLines = new LineArray(8 + 2 * (2 * xHashes + 2 * yHashes), GeometryArray.COORDINATES);

            int idx = 0;
            double offset = 0;

            // -1 to get rid of the last line
            // Avoids displaying a 2mm gap between the last line and buildVolumeX coordinates limit
            for (int i = 0; i < xHashes - 1; i++) {
                offset += gridSpacing;
                hashLines.setCoordinate(idx++, new Point3d(offset, -buildVol.getY() / 2, 0));
                hashLines.setCoordinate(idx++, new Point3d(offset, buildVol.getY() / 2, 0));
                hashLines.setCoordinate(idx++, new Point3d(-offset, -buildVol.getY() / 2, 0));
                hashLines.setCoordinate(idx++, new Point3d(-offset, buildVol.getY() / 2, 0));
            }
            offset = 0;
            for (int i = 0; i < yHashes; i++) {
                offset += gridSpacing;
                hashLines.setCoordinate(idx++, new Point3d(-buildVol.getX() / 2, offset, 0));
                hashLines.setCoordinate(idx++, new Point3d(buildVol.getX() / 2, offset, 0));
                hashLines.setCoordinate(idx++, new Point3d(-buildVol.getX() / 2, -offset, 0));
                hashLines.setCoordinate(idx++, new Point3d(buildVol.getX() / 2, -offset, 0));

            }

            hashLines.setCoordinate(idx++, new Point3d(-buildVol.getX() / 2, buildVol.getY() / 2, 0));
            hashLines.setCoordinate(idx++, new Point3d(buildVol.getX() / 2, buildVol.getY() / 2, 0));
            hashLines.setCoordinate(idx++, new Point3d(buildVol.getX() / 2, -buildVol.getY() / 2, 0));
            hashLines.setCoordinate(idx++, new Point3d(-buildVol.getX() / 2, -buildVol.getY() / 2, 0));


            //HashLines to close grid
            hashLines.setCoordinate(idx++, new Point3d(buildVol.getX() / 2, -buildVol.getY() / 2, 0));
            hashLines.setCoordinate(idx++, new Point3d(buildVol.getX() / 2, buildVol.getY() / 2, 0));
            hashLines.setCoordinate(idx++, new Point3d(-buildVol.getX() / 2, buildVol.getY() / 2, 0));
            hashLines.setCoordinate(idx++, new Point3d(-buildVol.getX() / 2, -buildVol.getY() / 2, 0));

            Shape3D hashes = new Shape3D(hashLines, hashAppearance);

            baseGroup.addChild(hashes);
            baseGroup.addChild(crosshairs);
            return baseGroup;
        }
        return null;
    }

    private Node makeCoordinateSystem() {
        BranchGroup coordSyst = new BranchGroup();

        // Create X axis
        LineArray axisXLines = new LineArray(10, LineArray.COORDINATES | LineArray.COLOR_3);
        coordSyst.addChild(new Shape3D(axisXLines));

        // Create X axis with arrow
        Point3f x1 = new Point3f(-10.0f, 0.0f, 0.0f);
        Point3f x2 = new Point3f(10.0f, 0.0f, 0.0f);

        axisXLines.setCoordinate(0, x1);
        axisXLines.setCoordinate(1, x2);
        axisXLines.setCoordinate(2, x2);
        axisXLines.setCoordinate(3, new Point3f(0.1f, 0.1f, 0.5f));
        axisXLines.setCoordinate(4, x2);
        axisXLines.setCoordinate(5, new Point3f(-0.1f, 0.1f, 0.1f));
        axisXLines.setCoordinate(6, x2);
        axisXLines.setCoordinate(7, new Point3f(0.1f, -0.1f, 0.5f));
        axisXLines.setCoordinate(8, x2);
        axisXLines.setCoordinate(9, new Point3f(-0.1f, -0.1f, 0.1f));

        // Create Y axis  
        LineArray axisYLines = new LineArray(10, LineArray.COORDINATES | LineArray.COLOR_3);
        coordSyst.addChild(new Shape3D(axisYLines));

        axisYLines.setCoordinate(0, new Point3f(0.0f, -10.0f, 0.0f));
        axisYLines.setCoordinate(1, new Point3f(0.0f, 10.0f, 0.0f));

        // Create Z axis with arrow
        Point3f z1 = new Point3f(0.0f, 0.0f, -10.0f);
        Point3f z2 = new Point3f(0.0f, 0.0f, 10.0f);

        LineArray axisZLines = new LineArray(10, LineArray.COORDINATES | LineArray.COLOR_3);
        coordSyst.addChild(new Shape3D(axisZLines));

        axisZLines.setCoordinate(0, z1);
        axisZLines.setCoordinate(1, z2);
        axisZLines.setCoordinate(2, z2);
        axisZLines.setCoordinate(3, new Point3f(0.1f, 0.1f, 8f));
        axisZLines.setCoordinate(4, z2);
        axisZLines.setCoordinate(5, new Point3f(-0.1f, 0.1f, 8f));
        axisZLines.setCoordinate(6, z2);
        axisZLines.setCoordinate(7, new Point3f(0.1f, -0.1f, 8f));
        axisZLines.setCoordinate(8, z2);
        axisZLines.setCoordinate(9, new Point3f(-0.1f, -0.1f, 8f));

        TransformGroup tg = new TransformGroup();
        Transform3D transform = new Transform3D();
        transform.setTranslation(new Vector3d(-100, 0, 80));
        tg.setTransform(transform);

        tg.addChild(coordSyst);

        return tg;
    }

    @Override
    public Object clone() {
        try {
            return (CAMPanel) super.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(CAMPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}

class OffScreenCanvas3D extends Canvas3D {

    Raster drawRaster;
    boolean printing = false;

    public OffScreenCanvas3D(GraphicsConfiguration gconfig,
            boolean offscreenflag, Raster drawRaster) {

        super(gconfig, offscreenflag);
        this.drawRaster = drawRaster;
    }

    public void print(boolean toWait) {

        if (!toWait) {
            printing = true;
        }

        BufferedImage bImage = new BufferedImage(200, 200,
                BufferedImage.TYPE_INT_ARGB);

        ImageComponent2D buffer = new ImageComponent2D(
                ImageComponent.FORMAT_RGBA, bImage);
        buffer.setCapability(ImageComponent2D.ALLOW_IMAGE_READ);

        this.setOffScreenBuffer(buffer);
        this.renderOffScreenBuffer();

        if (toWait) {
            this.waitForOffScreenRendering();
            drawOffScreenBuffer();
        }
    }

    public void postSwap() {

        if (printing) {
            super.postSwap();
            drawOffScreenBuffer();
            printing = false;
        }
    }

    void drawOffScreenBuffer() {

        BufferedImage bImage = this.getOffScreenBuffer().getImage();
        ImageComponent2D newImageComponent = new ImageComponent2D(
                ImageComponent.FORMAT_RGBA, bImage);

        drawRaster.setImage(newImageComponent);
    }
}

class OnScreenCanvas3D extends Canvas3D {

    OffScreenCanvas3D c;
    boolean print = false;
    boolean imageReady = false;

    public OnScreenCanvas3D(GraphicsConfiguration gconfig, boolean offscreenflag) {
        super(gconfig, offscreenflag);
    }

    public void setOffScreenCanvas(OffScreenCanvas3D c) {
        this.c = c;
    }

    public void setImageReady() {
        imageReady = true;
    }

    public void postSwap() {
        if (imageReady && !print) {
            c.print(false);
            print = true;
        }
    }
}
