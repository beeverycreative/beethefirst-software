package replicatorg.drivers.commands;

import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;
import replicatorg.drivers.StopException;

/**
 *
 * @author jgrego
 */
public class ResumePause implements DriverCommand {

    private final double temperature;
    private final double coefficient;

    public ResumePause() {
        this.temperature = -1.0;
        this.coefficient = -1.0;
    }

    public ResumePause(double coefficient, double temperature) {
        this.temperature = temperature;
        this.coefficient = coefficient;
    }

    @Override
    public String getCommand() {
        if (temperature > 0 && coefficient > 0) {
            return "M643 W" + coefficient + " S" + temperature;
        } else if (coefficient > 0) {
            return "M643 W" + coefficient;
        } else if (temperature > 0) {
            return "M643 S" + temperature;
        } else {
            return "M643";
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
        if (temperature > 0 && coefficient > 0) {
            driver.dispatchCommand("M643 W" + coefficient + " S" + temperature, COM.DEFAULT);
        } else if (coefficient > 0) {
            driver.dispatchCommand("M643 W" + coefficient, COM.DEFAULT);
        } else if (temperature > 0) {
            driver.dispatchCommand("M643 S" + temperature, COM.DEFAULT);
        } else {
            driver.dispatchCommand("M643", COM.DEFAULT);
        }
    }

}
