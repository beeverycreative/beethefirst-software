package replicatorg.app.ui.modeling;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JPanel;
import replicatorg.app.Base;
import replicatorg.model.CAMPanel.DragMode;

/*
 *  Copyright (c) 2013 BEEVC - Electronic Systems
 */
public class RotationTool extends Tool {
    
	public RotationTool(ToolPanel parent) {
		super(parent);
	}
	
	@Override
	Icon getButtonIcon() {
		return null;
	}

	@Override
	String getButtonName() {
		return "Rotate";
	}
	
	@Override
	JPanel getControls() {
		return null;
	}

	@Override
	public String getInstructions() {
		return Base.isMacOS()?
				"<html><body>Drag to rotate object<br>Shift-drag to rotate view<br>Mouse wheel to zoom</body></html>":
				"<html><body>Left drag to rotate object<br>Right drag to rotate view<br>Mouse wheel to zoom</body></html>";
	}

	@Override
	String getTitle() {
		return "Rotate Object";
	}
	
        public void mouseReleased(MouseEvent e) {
//            Base.getMainWindow().getCanvas().redrawBoundingBox(parent.getModelEditing().model, 1);
        }
        
        @Override
	public void mouseDragged(MouseEvent e) {
            if (parent.getModelEditing().model != null) {
                Base.getMainWindow().getBed().setGcodeOK(false);
		if (startPoint == null) return;
		Point p = e.getPoint();
		DragMode mode = DragMode.NONE;
		if (Base.isMacOS()) {
			if (button == MouseEvent.BUTTON1 && !e.isShiftDown()) { mode = DragMode.ROTATE_OBJECT; }
		} else {
			if (button == MouseEvent.BUTTON1) { mode = DragMode.ROTATE_OBJECT; }
		}
		double xd = (double)(p.x - startPoint.x);
		double yd = -(double)(p.y - startPoint.y);
		if (lockZ) { yd = 0; }
		switch (mode) {
		case ROTATE_OBJECT:
                        if(advOption)
                        {
                            parent.getModelEditing().rotateObject(0.02*xd, -0.02*yd);
                            break;
                        }
                    else
                        {
                            parent.getModelEditing().rotateObject(0.05*xd, -0.05*yd);
                            break;  
                        }
		case NONE:
			super.mouseDragged(e);
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
