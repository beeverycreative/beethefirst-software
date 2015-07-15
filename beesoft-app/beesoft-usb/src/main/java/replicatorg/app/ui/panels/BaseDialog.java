package replicatorg.app.ui.panels;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import replicatorg.app.Base;

/**
 *
 * @author dpacheco
 */
public abstract class BaseDialog extends javax.swing.JDialog {

    protected int posX = 0, posY = 0;
        
    public BaseDialog(Window window, ModalityType mt) {
        super(window, mt);
    }
    
    /**
     * At the moment this method is disabled due to a bug in Windows
     */
    protected void enableDrag() {
//        this.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mousePressed(MouseEvent e) {
//                posX = e.getX();
//                posY = e.getY();
//            }
//        });
//
//        this.addMouseMotionListener(new MouseAdapter() {
//            @Override
//            public void mouseDragged(MouseEvent evt) {
//                //sets frame position when mouse dragged			
//                setLocation(evt.getXOnScreen() - posX, evt.getYOnScreen() - posY);
//
//            }
//        });
    }    
        
    protected void centerOnScreen() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        // Determine the new location of the window
        int w = this.getSize().width;
        int h = this.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;

        // Move the window
        this.setLocation(x, y);
        this.setLocationRelativeTo(Base.getMainWindow());
    }

}
