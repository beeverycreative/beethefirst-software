package replicatorg.app.ui.modeling;


import javax.swing.Icon;
import javax.swing.JPanel;


public class ViewTool extends Tool {
	public ViewTool(ToolPanel parent) {
		super(parent);
	}

	public Icon getButtonIcon() {
		return null;
	}

	public String getButtonName() {
		return "View";
	}

	public JPanel getControls() {
		return null;
	}

	public String getInstructions() {
		return "<html><body>Drag to rotate<br>Mouse wheel to zoom</body></html>";
	}

	public String getTitle() {
		return "Preview";
	}

}
