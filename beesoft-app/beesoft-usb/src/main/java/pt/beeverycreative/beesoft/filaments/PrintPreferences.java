package pt.beeverycreative.beesoft.filaments;

import pt.beeverycreative.beesoft.drivers.usb.PrinterInfo;
import replicatorg.app.Base;
import replicatorg.drivers.Driver;
import replicatorg.machine.model.MachineModel;

/**
 *
 * @author jgrego
 */
public class PrintPreferences {

    private final Driver driver = Base.getMainWindow().getMachineInterface().getDriver();
    private final MachineModel model = driver.getMachine();
    private final PrinterInfo printer;
    private final String resolution;
    private final String coilText;
    private final int density;
    private final int nozzleSize;
    private final boolean raftPressed;
    private final boolean supportPressed;
    private final String gcodeToPrint;

    // default preferences, to be used in autonomous mode
    public PrintPreferences() {
        resolution = "medium";
        coilText = model.getCoilText();
        density = 5;
        nozzleSize = 400;
        raftPressed = false;
        supportPressed = false;
        printer = driver.getConnectedDevice();
        gcodeToPrint = "";
    }
    
    public PrintPreferences(String gcodeToPrint) {
        resolution = "medium";
        coilText = model.getCoilText();
        density = 5;
        nozzleSize = 400;
        raftPressed = false;
        supportPressed = false;
        printer = driver.getConnectedDevice();
        this.gcodeToPrint = gcodeToPrint;
    }

    public PrintPreferences(String resolution, String coilText, int density,
            int nozzleSize, boolean raftPressed, boolean supportPressed) {
        this.resolution = resolution;
        this.coilText = coilText;
        this.density = density;
        this.nozzleSize = nozzleSize;
        this.raftPressed = raftPressed;
        this.supportPressed = supportPressed;
        printer = driver.getConnectedDevice();
        gcodeToPrint = "";
    }

    public PrintPreferences(String resolution, String coilText, int density,
            int nozzleSize, boolean raftPressed, boolean supportPressed,
            String gcodeToPrint) {
        this.resolution = resolution;
        this.coilText = coilText;
        this.density = density;
        this.nozzleSize = nozzleSize;
        this.raftPressed = raftPressed;
        this.supportPressed = supportPressed;
        this.gcodeToPrint = gcodeToPrint;
        printer = driver.getConnectedDevice();
    }

    public PrintPreferences(String resolution, String coilText, int density,
            int nozzleSize, boolean raftPressed, boolean supportPressed,
            PrinterInfo printer) {
        this.resolution = resolution;
        this.coilText = coilText;
        this.density = density;
        this.nozzleSize = nozzleSize;
        this.raftPressed = raftPressed;
        this.supportPressed = supportPressed;
        this.printer = printer;
        gcodeToPrint = "";
    }

    public String getResolution() {
        return resolution;
    }

    public String getCoilText() {
        return coilText;
    }

    public int getDensity() {
        return density;
    }

    public int getNozzleSize() {
        return nozzleSize;
    }

    public boolean isRaftPressed() {
        return raftPressed;
    }

    public boolean isSupportPressed() {
        return supportPressed;
    }

    public String getGcodeToPrint() {
        return gcodeToPrint;
    }

    public PrinterInfo getPrinter() {
        return printer;
    }
}
