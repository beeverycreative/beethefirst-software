package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;

public class GetPosition implements DriverCommand {

    @Override
    public void run(Driver driver) throws RetryException {
        driver.getPosition();
    }

    @Override
    public String getCommand() {
        return "M121";
    }

    @Override
    public void setCommand(String newCommand) {
    }

    @Override
    public boolean isPrintingCommand() {
        return false;
    }
}
