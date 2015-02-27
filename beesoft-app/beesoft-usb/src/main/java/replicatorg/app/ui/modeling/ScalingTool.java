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
        }
        else
            super.mousePressed(e);

    }

    @Override
    public void mouseReleased(MouseEvent e) {
//         Base.getMainWindow().getCanvas().redrawBoundingBox(parent.getModelEditing().model, 1);
    }

    @Override
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

                    if(scaleDragChange > 0.1){   
                            
                        double currentScale = parent.getModelEditing().model.getTransform().getScale();
                        double targetScale = scaleDragChange / currentScale;

                        Model model = parent.getModelEditing().model;
                        double minimumSize = model.getEditer().getMINIMUM_SIZE_LIMIT();
                        double maximumSize = model.getEditer().getMAXIMUM_SIZE_LIMIT();
                        
                        if (!(model.minDimension()< minimumSize && targetScale < minimumSize) ) {

                          if (mOCS.isXLocked() && mOCS.isYLocked() && mOCS.isZLocked()) {
                                
                                parent.getModelEditing().scale(targetScale, isOnPlatform);
                                
                                model.updateXscale(targetScale);
                                model.updateYscale(targetScale);
                                model.updateZscale(targetScale);                              
                                
                            } else if (mOCS.isXLocked() && mOCS.isYLocked() && mOCS.isZLocked() == false) { //X & Y
                                
                                parent.getModelEditing().scaleXY(targetScale, isOnPlatform);      
                                
                                model.updateXscale(targetScale);
                                model.updateYscale(targetScale);                              
                                
                            } else if (mOCS.isXLocked() == false && mOCS.isYLocked() && mOCS.isZLocked() == false) { // Y

                                parent.getModelEditing().scaleAxisLock(targetScale, isOnPlatform, "y");

                                model.updateYscale(targetScale);
                               
                            } else if (mOCS.isXLocked() && mOCS.isYLocked() == false && mOCS.isZLocked() == false) { //X
                                
                                parent.getModelEditing().scaleAxisLock(targetScale, isOnPlatform, "x");
                                
                                model.updateXscale(targetScale);                             
                                
                            } else if (mOCS.isXLocked() && mOCS.isYLocked()== false && mOCS.isZLocked()) { //X & Z
                                
                                parent.getModelEditing().scaleXZ(targetScale, isOnPlatform);  
                                
                                model.updateXscale(targetScale);
                                model.updateZscale(targetScale);                               
                                
                            } else if (mOCS.isXLocked() == false && mOCS.isYLocked() && mOCS.isZLocked()) { // Y & Z
                                
                                parent.getModelEditing().scaleYZ(targetScale, isOnPlatform);            
                                
                                model.updateYscale(targetScale);
                                model.updateZscale(targetScale);                              
                                
                            } else if (mOCS.isXLocked() == false && mOCS.isYLocked() == false && mOCS.isZLocked()) { // Z

                                parent.getModelEditing().scaleAxisLock(targetScale, isOnPlatform, "z");       
                                
                                model.updateZscale(targetScale);                               
                            }                               
                            
                            Base.getMainWindow().getCanvas().getModelsPanel().updateDimensions();
 
                            if (super.mOCS.isScalePercentage()) {
                                super.mOCS.setXValue(model.getScaleXinPercentage());
                                super.mOCS.setYValue(model.getScaleYinPercentage());
                                super.mOCS.setZValue(model.getScaleZinPercentage());
                            } else {
                                DecimalFormat df = new DecimalFormat("#.0");
                                                    
                                double width = model.getEditer().getWidth();
                                if (ProperDefault.get("measures").equals("inches")) {
                                    width = UnitConverter.millimetersToInches(width);
                                }
                                double depth = model.getEditer().getDepth();
                                if (ProperDefault.get("measures").equals("inches")) {
                                    depth = UnitConverter.millimetersToInches(depth);
                                }
                                double height = model.getEditer().getHeight();
                                if (ProperDefault.get("measures").equals("inches")) {
                                    height = UnitConverter.millimetersToInches(height);
                                } 
                                super.mOCS.setXValue(df.format(width));
                                super.mOCS.setYValue(df.format(depth));
                                super.mOCS.setZValue(df.format(height));
                            } 
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