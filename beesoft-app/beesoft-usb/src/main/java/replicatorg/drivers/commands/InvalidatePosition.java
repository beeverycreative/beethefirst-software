package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;

public class InvalidatePosition implements DriverCommand {

    @Override
    public void run(Driver driver) {
        driver.invalidatePosition();
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
}
