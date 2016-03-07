package replicatorg.app.ui.modeling;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import replicatorg.app.Base;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.MainWindow;

import replicatorg.machine.MachineInterface;
import replicatorg.machine.model.BuildVolume;
import replicatorg.model.CAMPanel;
import replicatorg.model.Model;

/**
 * A wrapper for displaying and editing an underlying model object.
 *
 * @author phooky Copyright (c) 2013 BEEVC - Electronic Systems
 *
 */
public class EditingModel implements Serializable {

    private final double MINIMUM_SIZE_LIMIT = Float.parseFloat(ProperDefault.get("editor.xmin"));

    public class ReferenceFrame {

        public Point3d origin;
        public Vector3d zAxis;

        public ReferenceFrame() {
            origin = new Point3d();
            zAxis = new Vector3d(0d, 0d, 1d);
        }
    }
    /**
     * The underlying model being edited.
     */
    protected Model model;
    /**
     * Material definition for the model, maintained so that we can update the
     * color without reloading.
     */
    Material objectMaterial = null;
    /**
     * The group which represents the displayable subtree.
     */
    protected BranchGroup group = null;
    /**
     * The transform group for the shape. The enclosed transform should be
     * applied to the shape before: * bounding box calculation * saving out the
     * STL for skeining
     */
    private TransformGroup shapeTransform = new TransformGroup();
    /**
     * Last Scale Value
     */
    private double scale = 100;
    /**
     * Model bounding box
     */
    private Group boundingBox;
    private Point3d centroid = null;
    private Point3d bottom = null;

    public EditingModel(Model model) {
        this.model = model;
        model.setEditListener(this);
    }

    public EditingModel() {
        this.model = null;
    }
    /**
     * Cache of the original shape from the model.
     */
    Shape3D originalShape;

    /**
     * Create the branchgroup that will display the object.
     *
     * @param model
     * @return
     */
    public BranchGroup makeShape2(Model model) {
        originalShape = model.getShape();
        if (originalShape.getGeometry() == null) {
            BranchGroup wrapper = new BranchGroup();
            wrapper.setCapability(BranchGroup.ALLOW_DETACH);
            wrapper.compile();
            return wrapper;
        }

        Shape3D solidShape = (Shape3D) originalShape.cloneTree();
        solidShape.setPickable(true);
        solidShape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
        solidShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        solidShape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
        solidShape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        solidShape.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);
        solidShape.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
        solidShape.setCapability(Shape3D.ALLOW_PICKABLE_READ);
        solidShape.getGeometry().setCapability(GeometryArray.ALLOW_COUNT_READ);
        solidShape.getGeometry().setCapability(GeometryArray.ALLOW_COORDINATE_READ);
        solidShape.getGeometry().setCapability(GeometryArray.ALLOW_NORMAL_READ);

        for (Enumeration e = solidShape.getAllGeometries(); e.hasMoreElements();) {

            Geometry g = (Geometry) e.nextElement();

            g.setCapability(Geometry.ALLOW_INTERSECT);

        }
        objectMaterial = new Material();
        objectMaterial.setCapability(Material.ALLOW_COMPONENT_WRITE);

        updateModelColor();
        Appearance solid = new Appearance();
        solid.setMaterial(objectMaterial);
        PolygonAttributes pa = new PolygonAttributes();
        pa.setPolygonMode(PolygonAttributes.POLYGON_FILL);
        pa.setCullFace(PolygonAttributes.CULL_NONE);
        pa.setBackFaceNormalFlip(true);
        solid.setPolygonAttributes(pa);
        solidShape.setAppearance(solid);

        BranchGroup wrapper = new BranchGroup();

        shapeTransform = new TransformGroup();
        shapeTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        shapeTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        shapeTransform.setCapability(TransformGroup.ALLOW_CHILDREN_READ);

        wrapper.addChild(shapeTransform);

        shapeTransform.addChild(solidShape);
        wrapper.setCapability(BranchGroup.ALLOW_DETACH);
        wrapper.compile();
        return wrapper;
    }

    public void setShape(Model m) {
        this.model = m;
        this.group = makeShape2(model);
    }

    public Model getModel() {
        return model;
    }

    public boolean modelInvalidPosition() {
        double xLimit, yLimit;
        BuildVolume machineVolume;
        
        machineVolume = Base.getMainWindow().getCanvas().getBuildVolume();
        xLimit = machineVolume.getX() / 2;
        yLimit = machineVolume.getY() / 2;
        Point3d lower = new Point3d();
        Point3d upper = new Point3d();
        getBoundingBox().getLower(lower);
        getBoundingBox().getUpper(upper);

        return Math.abs(lower.x) > xLimit || Math.abs(upper.x) > xLimit ||
                Math.abs(lower.y) > yLimit || Math.abs(upper.y) > yLimit;
                //|| lower.z != 0 || upper.z > zLimit;

    }
     
    public void centerAndToBed() {
        BoundingBox bb = getBoundingBox(shapeTransform);
        Point3d lower = new Point3d();
        Point3d upper = new Point3d();
        bb.getLower(lower);
        bb.getUpper(upper);
        double zoff = -lower.z;
        double yoff;
        double xoff;

        ArrayList<Model> models = Base.getMainWindow().getBed().getModels();
        int nmodelsInBed = models.size();
        if (nmodelsInBed > 1) { // If there are more models in bed controls their positioning

            // Gets the model inserted before
            Model mp = models.get(models.size() - 2);
            double offset_prev_model = mp.getEditer().getWidth() / 2.0;
            Point3d centroid_prev = mp.getEditer().getCentroid();

            Model mc = models.get(models.size() - 1);
            double offset_curr_model = mc.getEditer().getWidth() / 2.0;

            xoff = centroid_prev.x + offset_curr_model + offset_prev_model + 5;
            yoff = (upper.y + lower.y) / 2.0d;

            translateObjectWithoutValidation(xoff, yoff, zoff);

        } else {
            xoff = (upper.x + lower.x) / 2.0d;
            yoff = (upper.y + lower.y) / 2.0d;

            //Uses negative x and y values to place the model correctly in the center
            translateObjectWithoutValidation(-xoff, -yoff, zoff);
        }

        BoundingBox bb2 = getBoundingBox(shapeTransform);
        Point3d lower2 = new Point3d();
        bb2.getLower(lower2);
        double zoff2 = -lower2.z;
        translateObject(0, 0d, zoff2);
        evaluateModelOutOfBounds();
    }

    public boolean evaluateModelOutOfBounds() {
        boolean invalidPosition;
        //if (modelOutBonds() || !modelInBed()) {
        invalidPosition = modelInvalidPosition();

        if (invalidPosition) {
            updateModelOverSize();
            showMessage();
            return true;
        } else {
            if (Base.getMainWindow().getBed().getNumberPickedModels() > 0) {
                Base.getMainWindow().getBed().getFirstPickedModel().getEditer().updateModelPickedLite(true);
            }
        }
        return false;
    }
    
    public void updateModelColor() {
        if (objectMaterial != null) {
            Color modelColor = new Color(214, 214, 214);
            objectMaterial.setAmbientColor(new Color3f(modelColor));
            objectMaterial.setDiffuseColor(new Color3f(modelColor));
        }
    }

    private void showMessage() {
        MainWindow editor = Base.getMainWindow();

        if (modelInvalidPosition()) {
            editor.showFeedBackMessage("outOfBounds");
        }

        /*
         if (!modelInBed()) {
         editor.showFeedBackMessage("notInBed");
         } else if (modelOutBonds() || modelTooBig()) {
         editor.showFeedBackMessage("outOfBounds");
         }
         */
    }

    public void updateModelPicked() {
        if (objectMaterial != null) {

            //if (!modelTooBig() && !modelOutBonds() && modelInBed()) {
            if (modelInvalidPosition() == false) {
                Color modelColor = new Color(253, 204, 6);
                objectMaterial.setAmbientColor(new Color3f(modelColor));
                objectMaterial.setDiffuseColor(new Color3f(modelColor));
            } else {
                updateModelOverSize();
                showMessage();
            }
        }
    }

    public void updateModelPickedLite(boolean validPosition) {
        if (objectMaterial != null) {
            if (validPosition) {
                Color modelColor = new Color(253, 204, 6);
                objectMaterial.setAmbientColor(new Color3f(modelColor));
                objectMaterial.setDiffuseColor(new Color3f(modelColor));
            } else {
                updateModelOverSize();
                showMessage();
            }
        }
    }

    public void updateModelUnPicked() {
        if (objectMaterial != null) {

            //if (modelTooBig() && modelOutBonds() && !modelInBed()) {
            if(modelInvalidPosition()) {
                showMessage();
                Color modelColor = new Color(224, 59, 42);
                objectMaterial.setAmbientColor(new Color3f(modelColor));
                objectMaterial.setDiffuseColor(new Color3f(modelColor));
            } else {
                updateModelColor();
            }
        }
    }

    public void updateModelOverSize() {
        if (objectMaterial != null) {
            Color modelColor = new Color(224, 59, 42);
            objectMaterial.setAmbientColor(new Color3f(modelColor));
            objectMaterial.setDiffuseColor(new Color3f(modelColor));
        }
    }

    public BranchGroup getGroup() {
        if (group == null) {
            group = makeShape2(model);
        }
        return group;
    }

    public ReferenceFrame getReferenceFrame() {
        Transform3D translate = new Transform3D();
        shapeTransform.getTransform(translate);
        ReferenceFrame rf = new ReferenceFrame();
        translate.transform(rf.origin);
        translate.transform(rf.zAxis);
        return rf;
    }

    /**
     * Transform the given transform to one that operates on the centroid of the
     * object.
     *
     * @param transform
     * @return
     */
    public Transform3D transformOnCentroid(Transform3D transform) {
        Transform3D old = new Transform3D();
        Transform3D t1 = new Transform3D();
        Transform3D t2 = new Transform3D();

        Vector3d t1v = new Vector3d(getCentroid());
        t1v.negate();
        t1.setTranslation(t1v);
        Vector3d t2v = new Vector3d(getCentroid());
        t2.setTranslation(t2v);
        shapeTransform.getTransform(old);

        Transform3D composite = new Transform3D();
        composite.mul(t2);
        composite.mul(transform);
        composite.mul(t1);
        composite.mul(old);
        return composite;
    }

    /**
     * Transform the given transform to one that operates on the centroid of the
     * object.
     *
     * @param transform
     * @return
     */
    public Transform3D transformOnBottom(Transform3D transform) {
        Transform3D old = new Transform3D();
        Transform3D t1 = new Transform3D();
        Transform3D t2 = new Transform3D();

        Vector3d t1v = new Vector3d(getBottom());
        t1v.negate();
        t1.setTranslation(t1v);
        Vector3d t2v = new Vector3d(getBottom());
        t2.setTranslation(t2v);
        shapeTransform.getTransform(old);

        Transform3D composite = new Transform3D();
        composite.mul(t2);
        composite.mul(transform);
        composite.mul(t1);
        composite.mul(old);
        return composite;
    }

    public void rotateObject(double turntable, double elevation) {
        if (model != null) {
            // Skip identity translations
            if (turntable == 0.0 && elevation == 0.0) {
                return;
            }
            Transform3D r1 = new Transform3D();
            Transform3D r2 = new Transform3D();
            r1.rotX(elevation);
            r2.rotZ(turntable);
            r2.mul(r1);
            r2 = transformOnCentroid(r2);
            model.setTransform(r2, "rotation", isNewOp());
            evaluateModelOutOfBounds();
        }
    }

    public void rotateObject(AxisAngle4d angle) {
        if (model != null) {
            Transform3D t = new Transform3D();
            t.setRotation(angle);
            t = transformOnCentroid(t);
            model.setTransform(t, "rotation", isNewOp());
            evaluateModelOutOfBounds();
        }
    }

    public void modelTransformChanged() {
        shapeTransform.setTransform(model.getTransform());
    }

    public void modelTransformChanged(Transform3D edit) {
        shapeTransform.setTransform(edit);
    }

    public void translateObject(double x, double y, double z) {
        if (model != null) {
            // Skip identity translations
            if (x == 0.0 && y == 0.0 && z == 0.0) {
                return;
            }
            invalidateBounds();
            Transform3D translate = new Transform3D();
            translate.setZero();
            translate.setTranslation(new Vector3d(x, y, z));
            Transform3D old = new Transform3D();
            shapeTransform.getTransform(old);
            old.add(translate);
            model.setTransform(old, "move", isNewOp());
            evaluateModelOutOfBounds();
        }
    }

    public void translateObjectWithoutValidation(double x, double y, double z) {
        if (model != null) {
            // Skip identity translations
            if (x == 0.0 && y == 0.0 && z == 0.0) {
                return;
            }
            invalidateBounds();
            Transform3D translate = new Transform3D();
            translate.setZero();
            translate.setTranslation(new Vector3d(x, y, z));
            Transform3D old = new Transform3D();
            shapeTransform.getTransform(old);
            old.add(translate);
            model.setTransform(old, "move", isNewOp());
        }
    }

    private BoundingBox getBoundingBox(Group group) {
        return getBoundingBox(group, new Transform3D());
    }

    protected BoundingBox getBoundingBox(Shape3D shape, Transform3D transformation) {
        BoundingBox bb = null;
        Enumeration<?> geometries = shape.getAllGeometries();
        while (geometries.hasMoreElements()) {
            Geometry g = (Geometry) geometries.nextElement();
            if (g instanceof GeometryArray) {
                GeometryArray ga = (GeometryArray) g;
                Point3d p = new Point3d();
                for (int i = 0; i < ga.getVertexCount(); i++) {
                    ga.getCoordinate(i, p);
                    transformation.transform(p);
                    if (bb == null) {
                        bb = new BoundingBox(p, p);
                    }
                    bb.combine(p);
                }
            }
        }
        return bb;
    }

    public Group getBoundingBoxGroup() {
        return boundingBox;
    }

    public Node makeBoundingBox() {

        CAMPanel cam = Base.getMainWindow().getCanvas();
        BranchGroup boxGroup = new BranchGroup();
//
//        if (buildVol == null) {
//            Shape3D boxframe = makeBoxFrame(model.getEditer().getLowerPoint3D(), new Vector3d(model.getEditer().getWidth(), model.getEditer().getDepth(), model.getEditer().getHeight()));
//            boxGroup.addChild(boxframe);
//            boxGroup.addChild(model.getShape());
//        } else {
//            Vector3d boxdims = new Vector3d(buildVol.getX(), buildVol.getY(), buildVol.getZ());
        Shape3D boxframe = cam.makeBoxFrame(getLowerPoint3D(), new Vector3d(getWidth(), getDepth(), getHeight()));

//			Appearance ap = new Appearance();
//			Color3f col = new Color3f(0.0f, 0.0f, 1.0f);
//			ColoringAttributes ca = new ColoringAttributes(col, ColoringAttributes.NICEST);
//			ap.setColoringAttributes(ca);
//			TransparencyAttributes t_attr = new TransparencyAttributes(TransparencyAttributes.BLENDED,0.5f,TransparencyAttributes.BLEND_SRC_ALPHA,TransparencyAttributes.BLEND_ONE);
//			ap.setTransparencyAttributes( t_attr );
//			Box box = new Box(buildVol.getX()/2,buildVol.getY()/2,buildVol.getZ()/2, ap);
//			boxGroup.addChild(box);
        boxframe.setPickable(false);
        boxGroup.addChild(boxframe);

        Appearance sides = new Appearance();
        sides.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, 0.9f));
        //Color3f color = new Color3f((int)(0.475*255),(int)(0.721*255),()(0.851*255));
        //Color3f color = new Color3f(0.00f,0.00f,0.01f); 
        Color3f color = new Color3f(new Color(255, 203, 5));
        Material m = new Material(color, color, color, color, 64.0f);
        sides.setMaterial(m);

//            Box box = new Box(buildVol.getX() / 2, buildVol.getY() / 2, buildVol.getZ() / 2, sides);
//            Transform3D tf = new Transform3D();
//            tf.setTranslation(new Vector3d(0, 0, buildVol.getZ() / 2));
//            TransformGroup tg = new TransformGroup(tf);
//            tg.addChild(box);
//
//            boxGroup.addChild(tg);
//            boxGroup.addChild(shape);
//        }
        boxGroup.setCapability(BranchGroup.ALLOW_DETACH);
//            boxGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
//            boxGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
//            boxGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

        boxGroup.setPickable(false);
        boundingBox = boxGroup;

        return boxGroup;
    }

    /**
     * Width is the size of the object along the X axis
     *
     * @return model width
     */
    public double getWidth() {
        Point3d lower = new Point3d();
        Point3d upper = new Point3d();
        getBoundingBox().getLower(lower);
        getBoundingBox().getUpper(upper);

        return upper.x - lower.x;
    }

    /**
     * Get lower Point3D from model
     *
     * @return Point3d coordinates
     */
    public Point3d getLowerPoint3D() {
        Point3d lower = new Point3d();
        getBoundingBox().getLower(lower);

        return lower;
    }

    /**
     * Get higher Point3D from model
     *
     * @return Point3d coordinates
     */
    public Point3d getHigherPoint3D() {
        Point3d higher = new Point3d();
        getBoundingBox().getUpper(higher);

        return higher;
    }

    /**
     * Depth is the size of the object along the Y axis
     *
     * @return model depth
     */
    public double getDepth() {
        Point3d lower = new Point3d();
        Point3d upper = new Point3d();
        getBoundingBox().getLower(lower);
        getBoundingBox().getUpper(upper);

        return upper.y - lower.y;
    }

    /**
     * Height is the size of the object along the Z axis
     *
     * @return model height
     */
    public double getHeight() {
        Point3d lower = new Point3d();
        Point3d upper = new Point3d();
        getBoundingBox().getLower(lower);
        getBoundingBox().getUpper(upper);

        return upper.z - lower.z;
    }

    /**
     * Returns last scale value made over the object
     *
     * @return scale
     */
    public double getScale() {
        return scale;
    }

    /**
     * Flip the object tree around the Z axis. This is particularly useful when
     * breaking a print into two parts.
     */
    public void flipZ() {
        if (model != null) {
            Transform3D flipZ = new Transform3D();
            flipZ.rotY(Math.PI);
            flipZ = transformOnCentroid(flipZ);
            model.setTransform(flipZ, "flip", isNewOp());
        }
    }

    public void mirrorX() {
        if (model != null) {
            Transform3D t = new Transform3D();
            Vector3d v = new Vector3d(-1d, 1d, 1d);
            t.setScale(v);
            t = transformOnCentroid(t);
            model.setTransform(t, "mirror X", isNewOp());
        }
    }

    public void mirrorY() {
        if (model != null) {
            Transform3D t = new Transform3D();
            Vector3d v = new Vector3d(1d, -1d, 1d);
            t.setScale(v);
            t = transformOnCentroid(t);
            model.setTransform(t, "mirror Y", isNewOp());
        }
    }

    public void mirrorZ() {
        if (model != null) {
            Transform3D t = new Transform3D();
            Vector3d v = new Vector3d(1d, 1d, -1d);
            t.setScale(v);
            t = transformOnCentroid(t);
            model.setTransform(t, "mirror Z", isNewOp());
        }
    }

    /**
     * Validates model size to avoid agressive transformations. This case the
     * minimum size allowed is 5x5 mm. Bellow that is impracticable
     *
     * @return <li> true, if bigger
     * <li> false, if not
     */
    private boolean validSizeConstraints(double newScale) {
        float minX = Float.parseFloat(ProperDefault.get("editor.xmin"));
        float minY = Float.parseFloat(ProperDefault.get("editor.ymin"));
        float minZ = Float.parseFloat(ProperDefault.get("editor.zmin"));
        return getWidth() * newScale >= minX && getDepth() * newScale >= minY && getHeight() * newScale >= minZ;
    }

    public boolean isOnPlatform() {
        BoundingBox bb = getBoundingBox();
        Point3d lower = new Point3d();
        bb.getLower(lower);
        return lower.z < 0.001d && lower.z > -0.001d;
    }

    public void updateDimensions(double targetX, double targetY, double targetZ, boolean isOnPlatform) {
        Transform3D t = new Transform3D();

        t.setScale(new Vector3d(targetX / getWidth(), targetY / getDepth(), targetZ / getHeight()));
        if (isOnPlatform) {
            t = transformOnBottom(t);
        } else {
            t = transformOnCentroid(t);
        }
        shapeTransform.setTransform(t);
        model.setTransform(t, "resize", isNewOp());

        evaluateModelOutOfBounds();
        Base.getMainWindow().getCanvas().getModelsPanel().updateDimensions();
    }

    public void scale(double scale, boolean isOnPlatform, boolean evaluateOutOfBounds) {

        if (model != null && validSizeConstraints(scale)) {
            this.scale = scale / 100;
            Transform3D t = new Transform3D();
            t.setScale(scale);
            if (isOnPlatform) {
                t = transformOnBottom(t);
            } else {
                t = transformOnCentroid(t);
            }
            shapeTransform.setTransform(t);
            model.setTransform(t, "resize", isNewOp());
            if (evaluateOutOfBounds) {
                evaluateModelOutOfBounds();
            }
            Base.getMainWindow().getCanvas().getModelsPanel().updateDimensions();
        }
    }

    public void scaleX(double scale2, boolean isOnPlatform, boolean arrowsScale) {
        if (model != null) {
            double currentScale = model.getXscalePercentage();
            double newScale = scale2;
            double realScale = newScale / currentScale;

            if (model.minDimension() >= MINIMUM_SIZE_LIMIT || realScale >= MINIMUM_SIZE_LIMIT) {

                this.scale = scale2;

                Transform3D t = new Transform3D();
                t.setNonUniformScale(realScale, 1, 1);
                if (isOnPlatform) {
                    t = transformOnBottom(t);
                } else {
                    t = transformOnCentroid(t);
                }

                shapeTransform.setTransform(t);
                model.setTransform(t, "resize", isNewOp());
                //boolean invalid = evaluateModelOutOfBoundsX();
                boolean invalid = modelInvalidPosition();
                Base.getMainWindow().getCanvas().getModelsPanel().updateDimensions();
                model.setXscale(newScale / 100.0);

                /**
                 * Tests if newScale represents a invalid model volume
                 */
                if (invalid && arrowsScale == false) {
                    double reductionToFit = calculateOverplus('X');
                    double scaleToFit = (newScale / reductionToFit);
                    this.scaleX(scaleToFit, isOnPlatform, false);
                } else {
                    evaluateModelOutOfBounds();

                }
            }
        }
    }

    public void scaleXY(double scale, boolean isOnPlatform) {
        if (model != null && validSizeConstraints(scale)) {
            this.scale = scale / 100;
            Transform3D t = new Transform3D();
            t.setNonUniformScale(scale, scale, 1);
            if (isOnPlatform) {
                t = transformOnBottom(t);
            } else {
                t = transformOnCentroid(t);
            }
            shapeTransform.setTransform(t);
            model.setTransform(t, "resize", isNewOp());
            evaluateModelOutOfBounds();
            Base.getMainWindow().getCanvas().getModelsPanel().updateDimensions();
        }
    }

    public void scaleY(double scale, boolean isOnPlatform, boolean arrowsScale) {
        if (model != null) {
            double currentScale = model.getYscalePercentage();
            double newScale = scale;
            double realScale = newScale / currentScale;

            if (!(model.minDimension() < MINIMUM_SIZE_LIMIT && realScale < MINIMUM_SIZE_LIMIT)) {
                Transform3D t = new Transform3D();
                t.setNonUniformScale(1, realScale, 1);
                if (isOnPlatform) {
                    t = transformOnBottom(t);
                } else {
                    t = transformOnCentroid(t);
                }

                shapeTransform.setTransform(t);
                model.setTransform(t, "resize", isNewOp());
                //boolean invalid = evaluateModelOutOfBoundsY();
                boolean invalid = modelInvalidPosition();
                Base.getMainWindow().getCanvas().getModelsPanel().updateDimensions();
                model.setYscale(newScale / 100.0);

                /**
                 * Tests if newScale represents a invalid model volume
                 */
                if (invalid && arrowsScale == false) {
                    double reductionToFit = calculateOverplus('Y');
                    double scaleToFit = (newScale / reductionToFit);
                    this.scaleY(scaleToFit, isOnPlatform, false);
                } else {
                    evaluateModelOutOfBounds();
                }
            }
        }
    }

    public void scaleYZ(double scale, boolean isOnPlatform) {
        if (model != null && validSizeConstraints(scale)) {
            this.scale = scale / 100;
            Transform3D t = new Transform3D();
            t.setNonUniformScale(1, scale, scale);
            if (isOnPlatform) {
                t = transformOnBottom(t);
            } else {
                t = transformOnCentroid(t);
            }
            shapeTransform.setTransform(t);
            model.setTransform(t, "resize", isNewOp());
            evaluateModelOutOfBounds();
            Base.getMainWindow().getCanvas().getModelsPanel().updateDimensions();
        }
    }

    public void scaleZ(double scale, boolean isOnPlatform, boolean arrowsScale) {
        if (model != null) {
            double currentScale = model.getZscalePercentage();
            double newScale = scale;
            double realScale = newScale / currentScale;

            if (!(model.minDimension() < MINIMUM_SIZE_LIMIT && realScale < MINIMUM_SIZE_LIMIT)) {
                Transform3D t = new Transform3D();
                t.setNonUniformScale(1, 1, realScale);
                if (isOnPlatform) {
                    t = transformOnBottom(t);
                } else {
                    t = transformOnCentroid(t);
                }

                shapeTransform.setTransform(t);
                model.setTransform(t, "resize", isNewOp());
                //boolean invalid = evaluateModelOutOfBoundsZ();
                boolean invalid = modelInvalidPosition();
                Base.getMainWindow().getCanvas().getModelsPanel().updateDimensions();
                model.setZscale(newScale / 100.0);
                /**
                 * Tests if newScale represents a invalid model volume
                 */
                if (invalid && arrowsScale == false) {
                    double reductionToFit = calculateOverplus('Z');
                    double scaleToFit = (newScale / reductionToFit);
                    this.scaleZ(scaleToFit, isOnPlatform, false);
                } else {
                    evaluateModelOutOfBounds();
                }
            }
        }
    }

    public void scaleXZ(double scale, boolean isOnPlatform) {
        if (model != null && validSizeConstraints(scale)) {
            this.scale = scale / 100;
            Transform3D t = new Transform3D();
            t.setNonUniformScale(scale, 1, scale);
            if (isOnPlatform) {
                t = transformOnBottom(t);
            } else {
                t = transformOnCentroid(t);
            }
            shapeTransform.setTransform(t);
            model.setTransform(t, "resize", isNewOp());
            evaluateModelOutOfBounds();
            Base.getMainWindow().getCanvas().getModelsPanel().updateDimensions();
        }
    }

    public void scaleAxisLock(double scale, boolean isOnPlatform, String axis) {
        if (model != null && validSizeConstraints(scale)) {
            this.scale = scale / 100;
            Transform3D t = new Transform3D();
            if (axis.equals("x")) {
                t.setNonUniformScale(scale, 1, 1);
            } else if (axis.equals("y")) {
                t.setNonUniformScale(1, scale, 1);
            } else if (axis.equals("z")) {
                t.setNonUniformScale(1, 1, scale);
            } else {
                t.setNonUniformScale(1, 1, 1);
            }

            if (isOnPlatform) {
                t = transformOnBottom(t);
            } else {
                t = transformOnCentroid(t);
            }
            shapeTransform.setTransform(t);
            model.setTransform(t, "resize", isNewOp());
            evaluateModelOutOfBounds();
            Base.getMainWindow().getCanvas().getModelsPanel().updateDimensions();
        }
    }

    private double calculateOverplus(char axis) {
        double diffDistance;
        double diffFactor;
        double excess;
        double max;

        BuildVolume machineVolume = Base.getMainWindow().getCanvas().getBuildVolume();

        if (axis == 'X') {
            excess = Math.abs(getHigherPoint3D().x - machineVolume.getX() / 2);
            max = Math.max(getHigherPoint3D().x, machineVolume.getX() / 2);
            diffDistance = (excess / max) * 100;
            diffFactor = (diffDistance + 100) / 100;

            return diffFactor;

        }
        if (axis == 'Y') {
            excess = Math.abs(getHigherPoint3D().y - machineVolume.getY() / 2);
            max = Math.max(getHigherPoint3D().y, machineVolume.getY() / 2);

            diffDistance = (excess / max) * 100;
            diffFactor = (diffDistance + 100) / 100;

            return diffFactor;
        }
        if (axis == 'Z') {
            excess = Math.abs(getHigherPoint3D().z - machineVolume.getZ());
            max = Math.max(getHigherPoint3D().z, machineVolume.getZ() / 2);
            diffDistance = (excess / max) * 100;
            diffFactor = (diffDistance + 100) / 100;

            return diffFactor;
        }
        return 0;
    }

    private BoundingBox getBoundingBox(Group group, Transform3D transformation) {
        BoundingBox bb = new BoundingBox(new Point3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE),
                new Point3d(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE));
        transformation = new Transform3D(transformation);
        if (group instanceof TransformGroup) {
            Transform3D nextTransform = new Transform3D();
            ((TransformGroup) group).getTransform(nextTransform);
            transformation.mul(nextTransform);
        }
        for (int i = 0; i < group.numChildren(); i++) {
            Node n = group.getChild(i);
            if (n instanceof Shape3D) {
                bb.combine(getBoundingBox((Shape3D) n, transformation));
            } else if (n instanceof Group) {
                bb.combine(getBoundingBox((Group) n, transformation));
            }
        }
        return bb;
    }

    private BoundingBox getBoundingBox() {
        return getBoundingBox(shapeTransform);
    }

    private void invalidateBounds() {
        centroid = null;
        bottom = null;
    }

    private void validateBounds() {
        if (centroid == null) {
            BoundingBox bb = getBoundingBox();
            Point3d p1 = new Point3d();
            Point3d p2 = new Point3d();
            bb.getLower(p1);
            bb.getUpper(p2);
            p2.interpolate(p1, 0.5d);
            centroid = p2;
            bottom = new Point3d(centroid.x, centroid.y, p1.z);
        }
    }

    public Point3d getCentroid() {
        validateBounds();
        return centroid;
    }

    public Point3d getBottom() {
        validateBounds();
        return bottom;
    }

    /**
     * Center the object tree and raise its lowest point to Z=0.
     */
    public void center() {
        BoundingBox bb = getBoundingBox(shapeTransform);
        Point3d lower = new Point3d();
        Point3d upper = new Point3d();
        bb.getLower(lower);
        bb.getUpper(upper);
        double zoff = -lower.z;
        double xoff = -(upper.x + lower.x) / 2.0d;
        double yoff = -(upper.y + lower.y) / 2.0d;

        MachineInterface mc = Base.getMachineLoader().getMachineInterface();
        translateObject(xoff, yoff, zoff);
        //translateObject((xoff + buildVol.getX()/2), (yoff  + buildVol.getY()/2), zoff);
    }

    /**
     * Raise the object's lowest point to Z=0.
     */
    public void putOnPlatform() {
        BoundingBox bb = getBoundingBox(shapeTransform);
        Point3d lower = new Point3d();
        bb.getLower(lower);
        double zoff = -lower.z;
        translateObject(0, 0d, zoff);
    }

    /**
     * Lay the object flat with the Z object. It computes this by finding the
     * bottommost point, and then rotating the object to make the surface with
     * the lowest angle to the Z plane parallel to it.
     *
     * In the future, we will want to add a convex hull pass to this.
     *
     */
    public void layFlat() {
        // Compute transformation
        Transform3D t = new Transform3D();
        shapeTransform.getTransform(t);
        Enumeration<?> geometries = originalShape.getAllGeometries();
        while (geometries.hasMoreElements()) {
            Geometry g = (Geometry) geometries.nextElement();
            double lowest = Double.MAX_VALUE;
            Vector3d flattest = new Vector3d(1d, 0d, 0d);
            if (g instanceof GeometryArray) {
                GeometryArray ga = (GeometryArray) g;
                Point3d p1 = new Point3d();
                Point3d p2 = new Point3d();
                Point3d p3 = new Point3d();
                for (int i = 0; i < ga.getVertexCount();) {
                    ga.getCoordinate(i++, p1);
                    ga.getCoordinate(i++, p2);
                    ga.getCoordinate(i++, p3);
                    t.transform(p1);
                    t.transform(p2);
                    t.transform(p3);
                    double triLowest = Math.min(p1.z, Math.min(p2.z, p3.z));
                    if (triLowest < lowest) {
                        // Clear any prior triangles
                        flattest = new Vector3d(1d, 0d, 0d);
                        lowest = triLowest;
                    }
                    if (triLowest == lowest) {
                        // This triangle is a candidate!
                        Vector3d v1 = new Vector3d(p2);
                        v1.sub(p1);
                        Vector3d v2 = new Vector3d(p3);
                        v2.sub(p2);
                        Vector3d v = new Vector3d();
                        v.cross(v1, v2);
                        v.normalize();
                        if (v.z < flattest.z) {
                            flattest = v;
                        }
                    }
                }
            }
            Transform3D flattenTransform = new Transform3D();
            Vector3d downZ = new Vector3d(0d, 0d, -1d);
            double angle = Math.acos(flattest.dot(downZ));
            Vector3d cross = new Vector3d();
            cross.cross(flattest, downZ);
            flattenTransform.setRotation(new AxisAngle4d(cross, angle));
            flattenTransform = transformOnCentroid(flattenTransform);
            shapeTransform.setTransform(flattenTransform);
            model.setTransform(flattenTransform, "Lay flat", isNewOp());
            invalidateBounds();
        }
    }
    boolean inDrag = false;
    boolean firstDrag = false;

    private boolean isNewOp() {
        if (!inDrag) {
            return true;
        }
        if (firstDrag) {
            firstDrag = false;
            return true;
        }
        return false;
    }

    public void startDrag() {
        inDrag = true;
        firstDrag = true;
    }

    public void endDrag() {
        inDrag = false;
    }
}
