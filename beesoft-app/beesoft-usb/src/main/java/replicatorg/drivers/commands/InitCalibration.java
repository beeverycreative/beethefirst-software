package replicatorg.drivers.commands;

import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;
import replicatorg.drivers.StopException;

/**
 *
 * @author jgrego
 */
public class InitCalibration implements DriverCommand {

    private final boolean repeatingCalibration;

    public InitCalibration() {
        repeatingCalibration = false;
    }

    public InitCalibration(boolean repeat) {
        this.repeatingCalibration = repeat;
    }

    @Override
    public String getCommand() {
        if (repeatingCalibration == false) {
            return "G131 S0";
        } else {
            return "G131 S0 Z0";
        }
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
        if (repeatingCalibration == false) {
            driver.dispatchCommand("G131 S0", COM.DEFAULT);
        } else {
            driver.dispatchCommand("G131 S0 Z0", COM.DEFAULT);
        }
    }

}
