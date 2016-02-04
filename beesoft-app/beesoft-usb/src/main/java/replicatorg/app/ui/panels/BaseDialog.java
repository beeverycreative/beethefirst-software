package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import replicatorg.app.Base;
import replicatorg.machine.MachineInterface;

/**
 *
 * @author dpacheco
 */
public abstract class BaseDialog extends javax.swing.JDialog {

    protected int posX = 0, posY = 0;

    public BaseDialog(Window window, ModalityType mt) {
        super(window, mt);
        this.getRootPane().setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
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
    
    protected void resetFeedbackComponents() {
        
    }
    
    protected void showMessage() {
        
    }

}

class BusyFeedbackThread extends Thread {

    private final MachineInterface machine;
    private final BaseDialog dialog;
    private boolean stop = false;

    public BusyFeedbackThread(BaseDialog child, MachineInterface mach) {
        super("BusyFeedbackThread");
        this.machine = mach;
        this.dialog = child;
    }

    @Override
    public void run() {

        while (stop == false) {

            if (machine.getDriver().isBusy() == false) {
                dialog.resetFeedbackComponents();
                break;
            } else {
                dialog.showMessage();
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
            }
        }
    }

    public void terminate() {
        stop = true;
    }
}
