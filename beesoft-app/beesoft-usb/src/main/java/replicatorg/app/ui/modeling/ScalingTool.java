package replicatorg.app.ui.modeling;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import javax.swing.Icon;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

import replicatorg.app.Base;
import replicatorg.app.ProperDefault;
import replicatorg.model.CAMPanel.DragMode;
import static replicatorg.model.CAMPanel.DragMode.NONE;
import static replicatorg.model.CAMPanel.DragMode.SCALE_OBJECT;
import replicatorg.model.Model;
import replicatorg.util.UnitConverter;

/*
 *  Copyright (c) 2013 BEEVC - Electronic Systems
 */
public class ScalingTool extends Tool {

    public ScalingTool(ToolPanel parent) {
        super(parent);
    }

    @Override
    Icon getButtonIcon() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    String getButtonName() {
        return "Scale";
    }
    // If isAbove is true, scale from the bottom of the object; if false, scale from the rough centroid
    boolean isOnPlatform = false;
//	double previousScale = 1;
    double scaleDragChange = 1;
    JFormattedTextField scaleFactor;

    @Override
    JPanel getControls() {
        return null;
    }

    @Override
    public String getInstructions() {
        return Base.isMacOS()
                ? "<html><body>Drag to scale object<br>Shift-drag to rotate view<br>Mouse wheel to zoom</body></html>"
                : "<html><body>Left drag to scale object<br>Right drag to rotate view<br>Mouse wheel to zoom</body></html>";
    }

    @Override
    String getTitle() {
        return "Scale object";
    }

    @Override
    public void mousePressed(MouseEvent e) {

        if (parent.getModelEditing().model != null) {
            super.mousePressed(e);
            // Reset scale to current value (in case there have been undos, etc. since
            // last update)
            scaleDragChange = parent.getModelEditing().model.getTransform().getScale();
            isOnPlatform = parent.getModelEditing().isOnPlatform();
        } else {
            super.mousePressed(e);
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {
//         Base.getMainWindow().getCanvas().redrawBoundingBox(parent.getModelEditing().model, 1);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (parent.getModelEditing().model != null) {
            Base.getMainWindow().getBed().setGcodeOK(false);
            if (startPoint == null) {
                return;
            }
            Point p = e.getPoint();
            DragMode mode = DragMode.NONE;
            if (Base.isMacOS()) {
                if (button == MouseEvent.BUTTON1 && !e.isShiftDown()) {
                    mode = DragMode.SCALE_OBJECT;
                }
            } else {
                if (button == MouseEvent.BUTTON1) {
                    mode = DragMode.SCALE_OBJECT;
                }
            }
            double xd = (double) (p.x - startPoint.x);
            double yd = -(double) (p.y - startPoint.y);
            switch (mode) {
                case NONE:
                    super.mouseDragged(e);
                    break;
                case SCALE_OBJECT:

                    scaleDragChange += (0.0001 * (xd + yd > 0 ? (Math.sqrt(xd * xd + yd * yd)) : -(Math.sqrt(xd * xd + yd * yd))));

                    if (scaleDragChange > 0.1) {
                        Model model = parent.getModelEditing().model;
                        double currentScale = parent.getModelEditing().model.getTransform().getScale();
                        double targetScale = scaleDragChange / currentScale;
                        double width = model.getEditer().getWidth();
                        double depth = model.getEditer().getDepth();
                        double height = model.getEditer().getHeight();
                        float minX = Float.parseFloat(ProperDefault.get("editor.xmin"));
                        float minY = Float.parseFloat(ProperDefault.get("editor.ymin"));
                        float minZ = Float.parseFloat(ProperDefault.get("editor.zmin"));
                        float maxX = Float.parseFloat(ProperDefault.get("editor.xmax"));
                        float maxY = Float.parseFloat(ProperDefault.get("editor.ymax"));
                        float maxZ = Float.parseFloat(ProperDefault.get("editor.zmax"));

                        if (mOCS.isXLocked()) {
                            width = Math.min(maxX, Math.max(width * targetScale, minX));
                        }

                        if (mOCS.isYLocked()) {
                            depth = Math.min(maxY, Math.max(depth * targetScale, minY));
                        }

                        if (mOCS.isZLocked()) {
                            height = Math.min(maxZ, Math.max(height * targetScale, minZ));
                        }

                        // stops values from increasing (or decreasing) when the limit of a selected value is reached
                        // aka maintains the aspect ratio
                        if ((width >= maxX && mOCS.isXLocked()) || (depth >= maxY && mOCS.isYLocked()) || (height >= maxZ && mOCS.isZLocked())) {
                            break;
                        }
                        parent.getModelEditing().updateDimensions(width, depth, height, isOnPlatform);

                        DecimalFormat df = new DecimalFormat("#.00");

                        if (ProperDefault.get("measures").equals("inches")) {
                            width = UnitConverter.millimetersToInches(model.getEditer().getWidth());
                            depth = UnitConverter.millimetersToInches(model.getEditer().getDepth());
                            height = UnitConverter.millimetersToInches(model.getEditer().getHeight());
                        } else { // although it seems unnecessary and equal to the values assigned above, these values changed with the resize
                            width = model.getEditer().getWidth();
                            depth = model.getEditer().getDepth();
                            height = model.getEditer().getHeight();
                        }

                        super.mOCS.setXValue(df.format(width));
                        super.mOCS.setYValue(df.format(depth));
                        super.mOCS.setZValue(df.format(height));

                    }

                    break;

            }
            startPoint = p;
        } else {
            if (startPoint == null) {
                return;
            }
            Point p = e.getPoint();
            DragMode mode = DragMode.NONE;
            double xd = (double) (p.x - startPoint.x);
            double yd = -(double) (p.y - startPoint.y);
            super.mouseDragged(e);
            startPoint = p;
        }

    }
}
