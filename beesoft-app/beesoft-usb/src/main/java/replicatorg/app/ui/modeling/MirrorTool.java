package replicatorg.app.ui.modeling;


import javax.swing.Icon;
import javax.swing.JPanel;

/*
 *  Copyright (c) 2013 BEEVC - Electronic Systems
 */
public class MirrorTool extends Tool {
	public MirrorTool(ToolPanel parent) {
		super(parent);
	}
	
	@Override
	Icon getButtonIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	String getButtonName() {
		return "Mirror";
	}

	@Override
	JPanel getControls() {
		return null;
	}

	@Override
	public String getInstructions() {
		return "<html><body>Drag to rotate view<br>Mouse wheel to zoom</body></html>";
	}

	@Override
	String getTitle() {
		return "Reflect object";
	}

}
