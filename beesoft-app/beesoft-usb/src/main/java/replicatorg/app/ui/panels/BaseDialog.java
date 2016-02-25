package replicatorg.app.ui.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import javax.swing.BorderFactory;
import replicatorg.app.Base;
import replicatorg.drivers.Driver;
import replicatorg.machine.model.MachineModel;
import replicatorg.machine.model.ToolModel;

/**
 *
 * @author dpacheco
 */
public abstract class BaseDialog extends javax.swing.JDialog {

    protected static final int TEMPERATURE_GOAL = 200;
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

    protected void updateHeatBar(int currentTemperature) {

    }

    protected class PrintingFeedbackThread extends Thread {

        private final Driver driver = Base.getMainWindow().getMachineInterface().getDriver();
        private final MachineModel model = driver.getMachine();
        private boolean stop = false;

        public PrintingFeedbackThread() {
            super(PrintingFeedbackThread.class.getSimpleName());
        }

        @Override
        public void run() {
            while (stop == false) {
                if (driver.isBusy() || model.getMachinePrinting()) {
                    showMessage();
                } else {
                    resetFeedbackComponents();
                }
                Base.hiccup(100);
            }
        }

        public void kill() {
            stop = true;
            this.interrupt();
        }
    }

    protected class BusyFeedbackThread extends Thread {

        private final Driver driver = Base.getMainWindow().getMachineInterface().getDriver();
        private boolean stop = false;

        public BusyFeedbackThread() {
            super(BusyFeedbackThread.class.getSimpleName());
        }

        @Override
        public void run() {
            while (stop == false) {
                if (driver.isBusy()) {
                    showMessage();
                } else {
                    resetFeedbackComponents();
                }

                Base.hiccup(100);
            }
        }

        public void kill() {
            stop = true;
            this.interrupt();
        }
    }

    protected class TemperatureThread extends Thread {

        private final Driver driver = Base.getMainWindow().getMachineInterface().getDriver();
        private final ToolModel currentTool = driver.getMachine().currentTool();
        private boolean stop = false;

        public TemperatureThread() {
            super(TemperatureThread.class.getSimpleName());
        }

        @Override
        public void run() {
            while (stop == false) {
                int currentTemperature;

                showMessage();
                while (stop == false) {
                    driver.readTemperature();
                    currentTemperature = currentTool.getExtruderTemperature();
                    updateHeatBar(currentTemperature);
                    Base.hiccup(Base.HEATING_POLL_TIME_MS);
                }
            }
        }

        public void kill() {
            stop = true;
            this.interrupt();
        }
    }

}
