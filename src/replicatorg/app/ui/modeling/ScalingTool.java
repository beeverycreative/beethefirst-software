package replicatorg.app.ui.modeling;

import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

import replicatorg.app.Base;
import replicatorg.model.CAMPanel.DragMode;
import static replicatorg.model.CAMPanel.DragMode.NONE;
import static replicatorg.model.CAMPanel.DragMode.SCALE_OBJECT;
import replicatorg.model.Model;

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

    public void mousePressed(MouseEvent e) {

        if (parent.getModelEditing().model != null) {
            super.mousePressed(e);
            // Reset scale to current value (in case there have been undos, etc. since
            // last update)
            scaleDragChange = parent.getModelEditing().model.getTransform().getScale();
            isOnPlatform = parent.getModelEditing().isOnPlatform();
        }
        else
            super.mousePressed(e);

    }

    public void mouseReleased(MouseEvent e) {
//         Base.getMainWindow().getCanvas().redrawBoundingBox(parent.getModelEditing().model, 1);
    }

    public void mouseDragged(MouseEvent e) {
        if (parent.getModelEditing().model != null ) {
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
                    
                    scaleDragChange += (0.005 * (xd+yd > 0 ? (Math.sqrt(xd*xd+yd*yd)) : -(Math.sqrt(xd*xd+yd*yd)))) ;

                    if(scaleDragChange > 0.1)
                    {   
                            
//                        System.out.println("scaleDragChange " + scaleDragChange);
                        double currentScale = parent.getModelEditing().model.getTransform().getScale();
//                        System.out.println("currentScale " + currentScale);
                        double targetScale = scaleDragChange / currentScale;
//                        System.out.println("targetScale " + targetScale);
//                        System.out.println("");
                        Model model = parent.getModelEditing().model;
                        double minimumSize = model.getEditer().getMINIMUM_SIZE_LIMIT();
                        double maximumSize = model.getEditer().getMAXIMUM_SIZE_LIMIT();
                        
                        if (!(model.minDimension()< minimumSize && targetScale < minimumSize) ) {
                            parent.getModelEditing().scale(targetScale, isOnPlatform);
                            model.updateXscale(targetScale);
                            model.updateYscale(targetScale);
                            model.updateZscale(targetScale);
                            Base.getMainWindow().getCanvas().getModelsPanel().updateDimensions();
                            super.mOCS.setXValue(model.getScaleXinPercentage());
                            super.mOCS.setYValue(model.getScaleYinPercentage());
                            super.mOCS.setZValue(model.getScaleZinPercentage());
                            //Base.getMainWindow().getCanvas().getModelsPanel().updateScale?();   
                        }
                        
                    }

                    break;
            }
            startPoint = p;
        }
        else
        {
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