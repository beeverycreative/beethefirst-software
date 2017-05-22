package replicatorg.model;

import org.scijava.java3d.loaders.IncorrectFormatException;
import org.scijava.java3d.loaders.Loader;
import org.scijava.java3d.loaders.ParsingErrorException;
import org.scijava.java3d.utils.picking.PickTool;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scijava.java3d.Appearance;
import org.scijava.java3d.BranchGroup;
import org.scijava.java3d.Node;
import org.scijava.java3d.PolygonAttributes;
import org.scijava.java3d.Shape3D;
import org.scijava.java3d.Transform3D;
import org.j3d.renderer.java3d.loaders.STLLoader;
import replicatorg.app.Base;
import replicatorg.app.ui.modeling.EditingModel;
import static replicatorg.model.PrintBed.StlToByteArray;
import replicatorg.model.j3d.StlAsciiWriter;

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
public class Model implements Serializable, Cloneable {

    private static final long serialVersionUID = 7526471155622776147L;
    private String name;
    private String description;
    private byte[] stream;
    //Internal scale in order to keep the object scalling absolute
    private double xScale = 1.0;
    private double yScale = 1.0;
    private double zScale = 1.0;
    private double xScaleInit = xScale;
    private double yScaleInit = yScale;
    private double zScaleInit = zScale;
    private transient Shape3D shape = null;
    private transient EditingModel editListener = null;
    private transient PolygonAttributes pa;
    private transient boolean picked;
    private transient Appearance appear;
    private transient EditingModel editModel;
    private transient Transform3D transform;
    private transient Transform3D initial;
    private transient ModelUndoEntry undo;

    public Model(File stl) {
        this.name = stl.getName();
        this.description = " ";
        this.stream = PrintBed.StlToByteArray(stl);
        this.pa = new PolygonAttributes();
        this.pa.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
        this.pa.setCapability(PolygonAttributes.ALLOW_MODE_READ);
        this.picked = false;
        loadShape(stl.getAbsolutePath());

        transform = new Transform3D();
        initial = new Transform3D(transform);
        this.appear = new Appearance();
        this.pa.setCullFace(PolygonAttributes.POLYGON_FILL);
        this.pa.setCullFace(PolygonAttributes.CULL_NONE);
        this.appear.setPolygonAttributes(pa);
        this.shape.setAppearance(appear);
        this.shape.setUserData(String.valueOf(Base.getModelID()));
        this.shape.setPickable(true);
        this.shape.setCapability(Node.ENABLE_PICK_REPORTING);
        PickTool.setCapabilities(shape, PickTool.INTERSECT_FULL);
        editModel = new EditingModel(this);
        editModel.setShape(this);
    }

    public void saveModelPositions() {
        FileOutputStream ostream = null;
        StlAsciiWriter saw;
        File f = PrintBed.toFile(stream);
        try {
            ostream = new FileOutputStream(f);
            saw = new StlAsciiWriter(ostream);
            saw.writeShape(shape, transform);
            ostream.close();
        } catch (IOException ex) {
            Logger.getLogger(PrintBed.class.getName()).log(Level.SEVERE, null, ex);
        }

        stream = StlToByteArray(f);
    }

    public String getName() {
        return name;
    }

    public byte[] getStream() {
        return stream;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public String getDescription() {
        return description;
    }

    public EditingModel getEditer() {
        return editModel;
    }

    public ModelUndoEntry getUndoManager() {
        return undo;
    }

    public Shape3D getShape() {
        return shape;
    }

    public BranchGroup getGroup() {
        return editModel.getGroup();
    }

    public void updateScale() {
        this.xScaleInit = xScale;
        this.yScaleInit = yScale;
        this.zScaleInit = zScale;

    }

    public void setPolygonAttributes(int value) {
        this.pa.setPolygonMode(value);

        if (value == 1) {
            picked = true;
        } else {
            picked = false;
        }
    }

    public PolygonAttributes getPolygonAttributes() {
        return pa;
    }

    public boolean isPicked() {
        // IF Mode == PolygonAttributes.POLYGON_LINE == Wireframe
        return this.pa.getPolygonMode() == 1;
    }
    Map<String, Loader> loaderExtensionMap = new HashMap<String, Loader>();

    {
        loaderExtensionMap.put("stl", new STLLoader());
    }

    private void loadShape(String filePath) {
        String suffix = null;
        int idx = filePath.lastIndexOf('.');
        if (idx > 0) {
            suffix = filePath.substring(idx + 1);
        }
        // Attempt to find loader based on suffix
        Shape3D candidate = null;
        if (suffix != null) {
            Loader loadCandidate = loaderExtensionMap.get(suffix.toLowerCase());
            if (loadCandidate != null) {
                try {
                    candidate = (Shape3D) loadCandidate.load(filePath).getSceneGroup().getChild(0);
                } catch (FileNotFoundException ex) {
                    Base.getMainWindow().showFeedBackMessage("modelMeshError");
                } catch (IncorrectFormatException ex) {
                    Base.getMainWindow().showFeedBackMessage("modelMeshError");
                } catch (ParsingErrorException ex) {
                    Base.getMainWindow().showFeedBackMessage("modelMeshError");
                }
            }
        }

        if (candidate != null) {
            this.shape = candidate;
        }
    }

    public void setEditListener(EditingModel eModel) {
        editListener = eModel;
    }

    public void setTransform(Transform3D t, String description, boolean newOp) {
        Base.writeLog("Performed transformation " + description + " to model " + name, this.getClass());
        
        // commented because the equals only seems to recognize that the movel has moved but not that it has been resized
        /*
        if (transform.equals(t)) {
            return;
        }
        */
        
        undo = new ModelUndoEntry(transform, t, description, newOp);
        undo.setEditListener(editModel);
        transform.set(t);
        Base.getMainWindow().getBed().setGcodeOK(false);
        Base.getMainWindow().getBed().setSceneDifferent(true);
        if (editListener != null) {
            editListener.modelTransformChanged();
        }
    }

    public void doEdit(Transform3D edit) {
        transform.set(edit);
        editListener.modelTransformChanged();
    }

    public Transform3D getTransform() {
        return transform;
    }

    public void resetPosition() {
        setTransform(initial, "reset", true);
    }

    @Override
    public Model clone() {
        try {
            final Model result = (Model) super.clone();
            // copy fields that need to be copied here!
            return result;
        } catch (final CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    private void writeObject(ObjectOutputStream aOutputStream) {
        try {
            aOutputStream.defaultWriteObject();
            aOutputStream.writeObject(this);
        } catch (IOException ex) {
            Logger.getLogger(PrintBed.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readObject(ObjectInputStream aInputStream) {

        try {
            aInputStream.defaultReadObject();
            this.pa = new PolygonAttributes();
            this.pa.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
            this.pa.setCapability(PolygonAttributes.ALLOW_MODE_READ);
            this.picked = false;
            loadShape(PrintBed.toFile(stream).getAbsolutePath());

            transform = new Transform3D();
            initial = new Transform3D(transform);
            this.appear = new Appearance();
            this.pa.setCullFace(PolygonAttributes.POLYGON_FILL);
            this.pa.setCullFace(PolygonAttributes.CULL_NONE);
            this.appear.setPolygonAttributes(pa);
            this.shape.setAppearance(appear);
            this.shape.setUserData(String.valueOf(Base.getModelID()));
            this.shape.setPickable(true);
            this.shape.setCapability(Node.ENABLE_PICK_REPORTING);
            PickTool.setCapabilities(shape, PickTool.INTERSECT_FULL);
            editModel = new EditingModel(this);
            editModel.setShape(this);
        } catch (IOException ex) {
            Logger.getLogger(PrintBed.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PrintBed.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String getScaleXinPercentage() {
        return String.format("%3.1f", xScale * 100);
    }

    public String getScaleYinPercentage() {
        return String.format("%3.1f", yScale * 100);
    }

    public String getScaleZinPercentage() {
        return String.format("%3.1f", zScale * 100);
    }

    public double getXscalePercentage() {
        return xScale * 100;
    }

    public double getYscalePercentage() {
        return yScale * 100;
    }

    public double getZscalePercentage() {
        return zScale * 100;
    }

    public void resetScale() {
        xScale = xScaleInit;
        yScale = yScaleInit;
        zScale = zScaleInit;
    }

    public void setXscale(double scale) {
        this.xScale = scale;
    }

    public void setYscale(double scale) {
        this.yScale = scale;

    }

    public void setZscale(double scale) {
        this.zScale = scale;
    }

    public double getScaleFloor() {
        return Math.min(zScale, Math.min(xScale, yScale));
    }

    public double minDimension() {
        return Math.min(editModel.getDepth(), Math.min(editModel.getHeight(), editModel.getWidth()));
    }

    public void updateXscale(double targetScale) {
        this.xScale = targetScale * this.xScale;
    }

    public void updateZscale(double targetScale) {
        this.zScale = targetScale * this.zScale;

    }

    public void updateYscale(double targetScale) {
        this.yScale = targetScale * this.yScale;
    }
}
