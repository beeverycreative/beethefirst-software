package replicatorg.app.ui.panels;

import java.awt.Window;
import javax.swing.ImageIcon;
import pt.beeverycreative.beesoft.filaments.PrintPreferences;
import replicatorg.app.Base;
import replicatorg.app.PrintEstimator;
import replicatorg.app.Printer;

public abstract class BasePrintEstimateExport extends BaseDialog {

    protected final ImageIcon loadingIcon = new ImageIcon(
            getClass().getResource("/replicatorg/app/ui/panels/loading.gif")
    );

    public BasePrintEstimateExport(Window window, ModalityType mt) {
        super(window, mt);
    }

    protected abstract void showLoadingIcon(boolean show);
    protected abstract void updateEstimationPanel(String time, String cost);

    /**
     * Gets print parameters from selected configurations on window.
     *
     * @return print parameters.
     */
    protected abstract PrintPreferences getPreferences();

    protected class GCodeEstimateExportThread extends Thread {

        private final Printer printer;
        private PrintEstimator estimator;

        // estimate
        public GCodeEstimateExportThread() {
            printer = new Printer(getPreferences());
        }

        // export
        public GCodeEstimateExportThread(String targetGCodePath) {
            printer = new Printer(getPreferences(), targetGCodePath);
        }

        /**
         * Runs GCode generator process. Updates window fields to display print
         * model cost estimation.
         */
        private void generateGCode() {
            if (printer.isReadyToGenerateGCode()) {
                printer.generateGCode();
                estimator = new PrintEstimator(printer.getGCode());
                updateEstimationPanel(estimator.getEstimatedTime(), estimator.getEstimatedCost());
            } else {
                Base.writeLog("runEstimator(): failed estimation", this.getClass());
            }
        }

        @Override
        public void run() {
            showLoadingIcon(true);
            if (Base.getMainWindow().isOkToGoOnSave() == false) {
                Base.getMainWindow().handleSave(true);
            }
            generateGCode();
            Base.cleanDirectoryTempFiles(Base.getAppDataDirectory().getAbsolutePath() + "/" + Base.MODELS_FOLDER + "/");
            showLoadingIcon(false);
        }

        public void kill() {
            this.interrupt();
            printer.endGCodeGeneration();
            estimator.stopEstimation();
        }
    }

}
