
package replicatorg.drivers.commands;

import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;
import replicatorg.drivers.StopException;

/**
 *
 * @author jgrego
 */
public class AbsolutePositioning implements DriverCommand {

    @Override
    public String getCommand() {
        return "G90";
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
        driver.dispatchCommand("G90", COM.DEFAULT);
    }
    
}
