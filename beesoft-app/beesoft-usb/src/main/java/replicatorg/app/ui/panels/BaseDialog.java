package replicatorg.app.ui.panels;

import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author dpacheco
 */
public abstract class BaseDialog extends javax.swing.JDialog {

    protected int posX = 0, posY = 0;
        
    public BaseDialog(Window window, ModalityType mt) {
        super(window, mt);
    }
    
    protected void enableDrag() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                posX = e.getX();
                posY = e.getY();
            }
        });

        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent evt) {
                //sets frame position when mouse dragged			
                setLocation(evt.getXOnScreen() - posX, evt.getYOnScreen() - posY);

            }
        });
    }

}
