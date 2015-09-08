package replicatorg.drivers.commands;

import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;
import replicatorg.drivers.StopException;

/**
 *
 * @author jgrego
 */
public class DefineFlowRate implements DriverCommand {
    
    private final double coefficient;
    
    public DefineFlowRate(double coefficient) {
        this.coefficient = coefficient;
    }

    @Override
    public String getCommand() {
        return "";
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
        driver.dispatchCommand("M642 W" + coefficient, COM.NO_RESPONSE);
    }
    
}
