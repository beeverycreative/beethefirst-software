package replicatorg.drivers.commands;

import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;
import replicatorg.drivers.StopException;

/**
 *
 * @author jgrego
 */
public class CalibrationStep implements DriverCommand {

    private final Thread thread;

    public CalibrationStep() {
        this.thread = null;
    }

    public CalibrationStep(Thread thread) {
        this.thread = thread;
    }

    @Override
    public String getCommand() {
        return "G132";
    }

    @Override
    public void setCommand(String newCommand) {
    }

    @Override
    public boolean isPrintingCommand() {
        return false;
    }

    @Override
    public void run(Driver driver) throws RetryException, StopException {
        driver.dispatchCommand("G132", COM.DEFAULT);
        driver.setBusy(true);

        if (thread != null) {
            thread.start();
        }
    }

}
