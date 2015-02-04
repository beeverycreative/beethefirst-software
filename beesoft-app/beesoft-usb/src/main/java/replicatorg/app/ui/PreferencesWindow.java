/**
 * 
 */
package replicatorg.app.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import net.miginfocom.swing.MigLayout;
import replicatorg.app.ProperDefault;
import replicatorg.app.Base;
import replicatorg.app.Base.InitialOpenBehavior;
/**
 * Edit the major preference settings.
 * @author phooky
 *
 */
public class PreferencesWindow extends JFrame implements GuiConstants {
	// gui elements

	// the calling editor, so updates can be applied
	MainWindow editor;

	JTextField fontSizeField;
	JTextField firmwareUpdateUrlField;
	JTextField logPathField;
	
	private void showCurrentSettings() {		
		Font editorFont = Base.getFontPref("editor.font","Monospaced,plain,12");
		fontSizeField.setText(String.valueOf(editorFont.getSize()));
//		String firmwareUrl = Base.preferences.get("replicatorg.updates.url", FirmwareUploader.DEFAULT_UPDATES_URL);
//		firmwareUpdateUrlField.setText(firmwareUrl);
//		String logPath = Base.preferences.get("replicatorg.logpath", "");
//		logPathField.setText(logPath);
	}
	
//	private JCheckBox addCheckboxForPref(Container c, String text, final String pref, boolean defaultVal) {
//		JCheckBox cb = new JCheckBox(text);
//		cb.setSelected(Base.preferences.getBoolean(pref,defaultVal));
//		c.add(cb,"wrap");
//		cb.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				JCheckBox box = (JCheckBox)e.getSource();
//                                ProperDefault.put("pref", String.valueOf(box.isSelected()));
//				Base.preferences.putBoolean(pref,box.isSelected());
//			}
//		});
//		return cb;
//	}

	private void addInitialFilePrefs(Container c) {
		final String prefName = "replicatorg.initialopenbehavior";
		int defaultBehavior = InitialOpenBehavior.OPEN_LAST.ordinal();
		int ordinal = Integer.valueOf(ProperDefault.get(prefName));
		if (ordinal >= InitialOpenBehavior.values().length) {
			ordinal = defaultBehavior;
		}
		final InitialOpenBehavior openBehavior = InitialOpenBehavior.values()[ordinal];
		ButtonGroup bg = new ButtonGroup();
		class RadioAction extends AbstractAction {
			private InitialOpenBehavior behavior;
		    public RadioAction(String text, InitialOpenBehavior behavior) {
		    	super(text);
		    	this.behavior = behavior;
		    }
		    public void actionPerformed(ActionEvent e) {
                        ProperDefault.put(prefName,String.valueOf(behavior.ordinal()));
		    }
		}
		c.add(new JLabel("On SimpleG launch:"),"wrap");
		// We don't have SELECTED_KEY in Java 1.5, so we'll do things the old-fashioned, ugly way.
		JRadioButton b;
		b = new JRadioButton(new RadioAction("Open last opened or save file",InitialOpenBehavior.OPEN_LAST));
    	if (InitialOpenBehavior.OPEN_LAST == openBehavior) { b.setSelected(true); }
		bg.add(b);
		c.add(b,"wrap");
		b = new JRadioButton(new RadioAction("Open new file",InitialOpenBehavior.OPEN_NEW));
    	if (InitialOpenBehavior.OPEN_NEW == openBehavior) { b.setSelected(true); }
		bg.add(b);
		c.add(b,"wrap");
	}

	JComboBox makeDebugLevelDropdown() {
		String levelName = Level.INFO.getName();
                //Base.preferences.get("replicatorg.debuglevel", Level.INFO.getName());
		Level l = Level.parse(levelName);
		if (l == null) { l = Level.INFO; }
		Vector<Level> levels = new Vector<Level>();
		levels.add(Level.ALL);
		levels.add(Level.FINEST);
		levels.add(Level.FINER);
		levels.add(Level.FINE);
		levels.add(Level.INFO);
		levels.add(Level.WARNING);
		final ComboBoxModel model = new DefaultComboBoxModel(levels);
		model.setSelectedItem(l);
		JComboBox cb = new JComboBox(model);
		cb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Level level = (Level)(model.getSelectedItem());
//				Base.preferences.put("replicatorg.debuglevel", level.getName());
				Base.logger.setLevel(level);
			}
		});
		return cb;
	}
	
	public PreferencesWindow() {
		super("Preferences");
		setResizable(true);
		
		Image icon = Base.getImage("images/icon.gif", this);
		setIconImage(icon);
		
		JTabbedPane basicVSadvanced = new JTabbedPane();
		
		JPanel basic = new JPanel();
		
//		Container content = this.getContentPane();
		Container content = basic;
		content.setLayout(new MigLayout("fill"));

		content.add(new JLabel("MainWindow font size: "), "split");
		fontSizeField = new JTextField(4);
		content.add(fontSizeField);
		content.add(new JLabel("  (requires restart of SimpleG)"), "wrap");

//		addCheckboxForPref(content,"Monitor temperature during builds","build.monitor_temp",false);
//		addCheckboxForPref(content,"Automatically connect to machine at startup","replicatorg.autoconnect",true);
//		addCheckboxForPref(content,"Show experimental machine profiles","machine.showExperimental",false);
//		addCheckboxForPref(content,"Review GCode for potential toolhead problems before building","build.safetyChecks",true);
//		addCheckboxForPref(content,"Break Z motion into seperate moves (normally false)","replicatorg.parser.breakzmoves",false);
//		addCheckboxForPref(content,"Show starfield in model preview window","ui.show_starfield",false);
//		addCheckboxForPref(content,"Notifications in System tray","ui.preferSystemTrayNotifications",false);
		
		JPanel advanced = new JPanel();
		content = advanced;
		content.setLayout(new MigLayout("fill"));
		
		JButton modelColorButton;
		modelColorButton = new JButton("Choose model color");
		modelColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Note that this color is also defined in EditingModel.java
				Color modelColor = new Color(Integer.parseInt(ProperDefault.get("ui.modelColor")));
				modelColor = JColorChooser.showDialog(
						null,
		                "Choose Model Color",
		                modelColor);
		                
//		        Base.preferences.putInt("ui.modelColor", modelColor.getRGB());
                        ProperDefault.put("ui.modelColor",String.valueOf(modelColor.getRGB()));
		        Base.getEditor().refreshPreviewPanel();
			}
		});
		modelColorButton.setVisible(true);
		content.add(modelColorButton,"split");
		
		
		JButton backgroundColorButton;
		backgroundColorButton = new JButton("Choose background color");
		backgroundColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Note that this color is also defined in EditingModel.java
				Color backgroundColor = new Color(Integer.parseInt(ProperDefault.get("ui.backgroundColor")));
				backgroundColor = JColorChooser.showDialog(
						null,
		                "Choose Background Color",
		                backgroundColor);
		                
                        ProperDefault.put("ui.backgroundColor",String.valueOf(backgroundColor.getRGB()));        
//		        Base.preferences.putInt("ui.backgroundColor", backgroundColor.getRGB());
		        Base.getEditor().refreshPreviewPanel();
			}
		});
		backgroundColorButton.setVisible(true);
		content.add(backgroundColorButton,"wrap");
		
		
		content.add(new JLabel("Firmware update URL: "),"split");
		firmwareUpdateUrlField = new JTextField(34);
		content.add(firmwareUpdateUrlField,"wrap");

		{
			JLabel arcResolutionLabel = new JLabel("Arc resolution (in mm): ");
			content.add(arcResolutionLabel,"split");
			double value = Double.parseDouble(ProperDefault.get("replicatorg.parser.curve_segment_mm")); 
			JFormattedTextField arcResolutionField = new JFormattedTextField(new Double(value));
			content.add(arcResolutionField,"wrap");
			String arcResolutionHelp = "<html><small><em>" +
				"The arc resolution is the default segment length that the gcode parser will break arc codes <br>"+
				"like G2 and G3 into.  Drivers that natively handle arcs will ignore this setting." +
				"</em></small></html>";
			arcResolutionField.setToolTipText(arcResolutionHelp);
			arcResolutionLabel.setToolTipText(arcResolutionHelp);
//			content.add(new JLabel(arcResolutionHelp),"growx,wrap");
			arcResolutionField.setColumns(10);
			arcResolutionField.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName() == "value") {
						try {
							Double v = (Double)evt.getNewValue();
							if (v == null) return;
                                                        ProperDefault.put("replicatorg.parser.curve_segment_mm",String.valueOf(v.doubleValue()));   
							ProperDefault.put("replicatorg.parser.curve_segment_mm", String.valueOf(v.doubleValue()));
						} catch (ClassCastException cce) {
							Base.logger.warning("Unexpected value type: "+evt.getNewValue().getClass().toString());
						}
					}
				}
			});
		}
		
		{
			JLabel sfTimeoutLabel = new JLabel("Skeinforge timeout: ");
			content.add(sfTimeoutLabel,"split");
			int value = Integer.valueOf(ProperDefault.get("replicatorg.skeinforge.timeout"));
			JFormattedTextField sfTimeoutField = new JFormattedTextField(new Integer(value));
			content.add(sfTimeoutField,"wrap");
			String sfTimeoutHelp = "<html><small><em>" +
				"The Skeinforge timeout is the number of seconds that replicatorg will wait while the<br>" +
				"Skeinforge preferences window is open. If you find that RepG freezes after editing profiles<br>" +
				"you can set this number greater than -1 (-1 means no timeout)." +
				"</em></small></html>";
			sfTimeoutField.setToolTipText(sfTimeoutHelp);
			sfTimeoutLabel.setToolTipText(sfTimeoutHelp);
//			content.add(new JLabel(sfTimeoutHelp),"growx,wrap");
			sfTimeoutField.setColumns(10);
			sfTimeoutField.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName() == "value") {
						try {
							Integer v = (Integer)evt.getNewValue();
							if (v == null) return;
                                                        ProperDefault.put("replicatorg.skeinforge.timeout",String.valueOf(v.intValue()));   
//							Base.preferences.putInt("replicatorg.skeinforge.timeout", v.intValue());
						} catch (ClassCastException cce) {
							Base.logger.warning("Unexpected value type: "+evt.getNewValue().getClass().toString());
						}
					}
				}
			});
		}
		
		{
			content.add(new JLabel("Debugging level (default INFO):"),"split");
//			content.add(makeDebugLevelDropdown(),"wrap");
		}

		{
//			final JCheckBox logCb = addCheckboxForPref(content,"Log to file","replicatorg.useLogFile",false);
			final JLabel logPathLabel = new JLabel("Log file name: "); 
			content.add(logPathLabel,"split");
			logPathField = new JTextField(34);
			content.add(logPathField,"wrap");
//			logPathField.setEnabled(logCb.isSelected());
//			logPathLabel.setEnabled(logCb.isSelected());
//
//			logCb.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					JCheckBox box = (JCheckBox)e.getSource();
//					logPathField.setEnabled(box.isSelected());
//					logPathLabel.setEnabled(box.isSelected());
//				}
//			});

		}
		{
//			JButton b = new JButton("Select Python interpreter...");
//			content.add(b,"spanx,wrap");
//			b.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					SwingPythonSelector sps = new SwingPythonSelector(PreferencesWindow.this);
//					String path = sps.selectFreeformPath();
//					if (path != null) {
//						PythonUtils.setPythonPath(path);
//					}
//				}
//			});
		}
		//"replicatorg.parser.curve_segment_mm"

		addInitialFilePrefs(content);

		JButton delPrefs = new JButton("Restore all defaults (includes driver choice, etc.)");
		content.add(delPrefs,"wrap");
		delPrefs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
//				Base.resetPreferences();
				showCurrentSettings();
			}
		});


		JButton allPrefs = new JButton("View All Prefs");
		content.add(allPrefs);
		allPrefs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFrame advancedPrefs = new AdvancedPrefs();
				advancedPrefs.setVisible(true);
			}
		});
		// [ OK ] [ Cancel ] maybe these should be next to the message?

		JButton button;
		
		button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applyFrame();
				dispose();
			}
		});
		content.add(button, "tag ok");

		basicVSadvanced.add(basic, "Basic");
		basicVSadvanced.add(advanced, "Advanced");
		getContentPane().add(basicVSadvanced);
		
		showCurrentSettings();

		// closing the window is same as hitting cancel button

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});

		ActionListener disposer = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				dispose();
			}
		};
		Base.registerWindowCloseKeys(getRootPane(), disposer);

		pack();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screen.width - getWidth()) / 2,
				(screen.height - getHeight()) / 2);

		// handle window closing commands for ctrl/cmd-W or hitting ESC.

		getContentPane().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				KeyStroke wc = MainWindow.WINDOW_CLOSE_KEYSTROKE;
				if ((e.getKeyCode() == KeyEvent.VK_ESCAPE)
						|| (KeyStroke.getKeyStrokeForEvent(e).equals(wc))) {
					dispose();
				}
			}
		});
	}

	/**
	 * Change internal settings based on what was chosen in the prefs, then send
	 * a message to the editor saying that it's time to do the same.
	 */
	public void applyFrame() {
		// put each of the settings into the table
		String newSizeText = fontSizeField.getText();
		try {
			int newSize = Integer.parseInt(newSizeText.trim());
			String fontName = ProperDefault.get("editor.font");
			if (fontName != null) {
				String pieces[] = fontName.split(",");
				pieces[2] = String.valueOf(newSize);
				StringBuffer buf = new StringBuffer();
				for (String piece : pieces) {
					if (buf.length() > 0) buf.append(",");
					buf.append(piece);
				}
				ProperDefault.put("editor.font", buf.toString());
//                                Base.preferences.put("editor.font", buf.toString());
			}

		} catch (Exception e) {
			Base.logger.warning("ignoring invalid font size " + newSizeText);
		}
		
		//R2C2: don't have firmware update
		//String origUpdateUrl = Base.preferences.get("replicatorg.updates.url", "");
		//if (!origUpdateUrl.equals(firmwareUpdateUrlField.getText())) {
			//FirmwareUploader.invalidateFirmware();
			//Base.preferences.put("replicatorg.updates.url",firmwareUpdateUrlField.getText());
			//FirmwareUploader.checkFirmware(); // Initiate a new firmware check
		//}

		String logPath = logPathField.getText();
//		Base.preferences.put("replicatorg.logpath", logPath);
		Base.setLogFile(logPath);
		
		editor.applyPreferences();
	}

	public void showFrame(MainWindow editor) {
		this.editor = editor;

		// set all settings entry boxes to their actual status
		setVisible(true);
	}

}
