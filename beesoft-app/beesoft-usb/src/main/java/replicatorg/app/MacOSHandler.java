
package replicatorg.app;

import javax.swing.JOptionPane;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import java.util.prefs.Preferences;
import replicatorg.app.ui.panels.About;
import replicatorg.app.ui.panels.PreferencesPanel;

public class MacOSHandler extends ApplicationAdapter
{
  private Base handler;
  
  public MacOSHandler(Base handler)
  {
    this.handler = handler;
  }

  public void handleQuit(ApplicationEvent e)
  {
    System.exit(0);
  }

  public void handleAbout(ApplicationEvent e)
  {
      About p = new About();
      p.setVisible(true);
  }

  public void handlePreferences(ApplicationEvent e)
  {
      PreferencesPanel p = new PreferencesPanel();
      p.setVisible(true);
  }
}
