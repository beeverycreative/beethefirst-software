package pt.beeverycreative.beesoft.filaments;

import pt.beeverycreative.beesoft.drivers.usb.PrinterInfo;
import replicatorg.app.Base;

/**
 *
 * @author jgrego
 */
public class PrintPreferences {
    private final String resolution;
    private final String coilText;
    private final int density;
    private final int nozzleSize;
    private final boolean raftPressed;
    private final boolean supportPressed;
    private String gcodeToPrint = "";
    private PrinterInfo printer = 
            Base.getMainWindow().getMachine().getDriver().getConnectedDevice();
    
    // default preferences, to be used in autonomous mode
    public PrintPreferences() {
        resolution = "medium";
        coilText = Base.getMainWindow().getMachine().getDriver().getCoilText();
        density = 5;
        nozzleSize = 400;
        raftPressed = false;
        supportPressed = false;
    }
    
    public PrintPreferences(String resolution, String coilText, int density, 
            int nozzleSize, boolean raftPressed, boolean supportPressed) {
        this.resolution = resolution;
        this.coilText = coilText;
        this.density = density;
        this.nozzleSize = nozzleSize;
        this.raftPressed = raftPressed;
        this.supportPressed = supportPressed;
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
    }
    
    public PrintPreferences(String resolution, String coilText, int density, 
            int nozzleSize, boolean raftPressed, boolean supportPressed, 
            String gcodeToPrint, PrinterInfo printer) {
        this.resolution = resolution;
        this.coilText = coilText;
        this.density = density;
        this.nozzleSize = nozzleSize;
        this.raftPressed = raftPressed;
        this.supportPressed = supportPressed;
        this.gcodeToPrint = gcodeToPrint;
        this.printer = printer;
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
