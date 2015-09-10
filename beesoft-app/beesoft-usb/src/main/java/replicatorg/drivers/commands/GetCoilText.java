package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;
import replicatorg.drivers.StopException;

/**
 *
 * @author jgrego
 */
public class GetCoilText implements DriverCommand {

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
        driver.updateCoilText();
    }
    
}
