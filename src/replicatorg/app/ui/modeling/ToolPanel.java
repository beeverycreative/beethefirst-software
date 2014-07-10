package replicatorg.app.ui.modeling;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;
import replicatorg.model.CAMPanel;

/*
 *  Copyright (c) 2013 BEEVC - Electronic Systems
 */
public class ToolPanel extends JPanel implements KeyListener {

	public JButton createToolButton(String text, String iconPath) {
		//ImageIcon icon = new ImageIcon(Base.getImage(iconPath, this));
		JButton button = new JButton(text);//,icon);
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		return button;
	}

	public JButton createToolButton(final Tool tool) {
		JButton button = new JButton(tool.getButtonName(), tool.getButtonIcon());
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setTool(tool);
			}
		});
		return button;
	}

	final CAMPanel preview;
	final JPanel subPanel = new JPanel(new MigLayout("fillx,filly,ins 0, gap 0"));
	
	Tool[] tools = { 
			new ViewTool(this),
			new MoveTool(this),
			new RotationTool(this),
                        new ScalingTool(this),
			new MirrorTool(this)
	};
	
	JLabel titleLabel;
	JPanel toolControls = null;
	int ctr=0;
	void setTool(Tool tool) {

		// Default to the view tool
		if (tool == null) { tool = tools[0]; }
                tool.updateLockHeight();
		// Connect this tool to the preview panel's mouse and keyboard handlers
		preview.setTool(tool);
                
		// Add subpanel
		if (toolControls != null) {
			subPanel.remove(toolControls);
		}
		toolControls = tool.getControls();
		if (toolControls != null) {
			subPanel.add(toolControls,"spanx,spany,growx,growy,width 100%");
		} else {
		}
		validate();
		repaint();
	}
        
        public void setTool(int index)
        {
            setTool(tools[index]);
            
        }
        
        public Tool getTool(int index)
        {
            return tools[index];
        }
	
	EditingModel getModelEditing() {return preview.getModelEditing(); }
	
	final JLabel infoLabel = new JLabel();
	
	public ToolPanel(final CAMPanel preview) {
		this.preview = preview;

		this.setBorder(BorderFactory.createEtchedBorder());
                this.setVisible(false);

		JPanel toolButtons = new JPanel(new MigLayout("ins 0,novisualpadding,wrap 1","0[100%]0"));

		int column = 0;
		final int COL_COUNT = 2;
		for (Tool t : tools) {
			column++;
			JButton b = createToolButton(t);
			if (column == COL_COUNT) {
				toolButtons.add(b,"growx,growy,wrap");
				column = 0;
			} else { 
				toolButtons.add(b,"growx,growy");
			}
			
		}
                setTool(tools[1]);
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}
}
