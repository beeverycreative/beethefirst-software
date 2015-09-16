package replicatorg.drivers.commands;

import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;
import replicatorg.drivers.StopException;

/**
 *
 * @author jgrego
 */
public class FilamentChangeStep implements DriverCommand {

    private final double temperature;
    private boolean firstStep = true;

    public FilamentChangeStep(double temperature) {
        this.temperature = temperature;
    }

    public FilamentChangeStep() {
        this.temperature = -1;
        this.firstStep = false;
    }

    @Override
    public String getCommand() {
        if (firstStep) {
            return "M703 S" + temperature;
        } else {
            return "M703";
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
        if (firstStep) {
            driver.dispatchCommand("M703 S" + temperature, COM.DEFAULT);
        } else {
            driver.dispatchCommand("M703", COM.DEFAULT);
        }
    }

}
