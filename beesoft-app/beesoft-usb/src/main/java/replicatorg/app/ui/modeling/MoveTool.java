package replicatorg.app.ui.modeling;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.media.j3d.Transform3D;
import javax.swing.Icon;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.vecmath.Vector3d;
import replicatorg.app.Base;
import replicatorg.app.ui.MainWindow;
import replicatorg.model.CAMPanel.DragMode;
import static replicatorg.model.CAMPanel.DragMode.TRANSLATE_OBJECT;

/*
 *  Copyright (c) 2013 BEEVC - Electronic Systems
 */
public class MoveTool extends Tool {
    
    private final MainWindow frame = Base.getMainWindow();

    public MoveTool(ToolPanel parent) {
        super(parent);
    }

    Transform3D vt;

    @Override
    public Icon getButtonIcon() {
        return null;
    }

    @Override
    public String getButtonName() {
        return "Move";
    }

    JFormattedTextField transX, transY, transZ;

    @Override
    public JPanel getControls() {
        return null;
    }

    @Override
    public String getInstructions() {
        return Base.isMacOS()
                ? "<html><body>Drag to move object<br>Shift-drag to rotate view<br>Mouse wheel to zoom</body></html>"
                : "<html><body>Left drag to move object<br>Right drag to rotate view<br>Mouse wheel to zoom</body></html>";
    }

    @Override
    public String getTitle() {
        return "Move Object";
    }

    @Override
    public void mouseReleased(MouseEvent e) {
//            Base.getMainWindow().getCanvas().redrawBoundingBox(parent.getModelEditing().model, 1);
        frame.setCursor(null);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (parent.getModelEditing().model != null) {
            Base.getMainWindow().getBed().setGcodeOK(false);
            if (startPoint == null) {
                return;
            }
            DragMode mode = DragMode.NONE;
            if (Base.isMacOS()) {
                if (button == MouseEvent.BUTTON1 && !e.isShiftDown()) {
                    mode = DragMode.TRANSLATE_OBJECT;
                }
            } else {
                if (button == MouseEvent.BUTTON1) {
                    mode = DragMode.TRANSLATE_OBJECT;
                }
            }

            double xd = (e.getX() - startPoint.x);
            double yd = -(e.getY() - startPoint.y);

            switch (mode) {
                case NONE:
                    super.mouseDragged(e);
                    break;
                case TRANSLATE_OBJECT:

                    if (advOption) {
                        doTranslate(0.5 * xd, 0.5 * yd);
                        break;
                    } else {
                        doTranslate(0.25 * xd, 0.25 * yd);
                        break;
                    }

            }
            startPoint = e.getPoint();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Set up view transform
        vt = parent.preview.getViewTransform();
        startPoint = e.getPoint();

        frame.setCursor(frame.getToolkit().createCustomCursor(
                new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), 
                new Point(0, 0), "null"));

        super.mousePressed(e);
    }

    void doTranslate(double deltaX, double deltaY) {
        Vector3d v = new Vector3d(deltaX, deltaY, 0d);
        vt.transform(v);
        if (lockZ) {
            v.z = 0d;
        }
        parent.getModelEditing().translateObject(v.x, v.y, v.z);
    }

}
