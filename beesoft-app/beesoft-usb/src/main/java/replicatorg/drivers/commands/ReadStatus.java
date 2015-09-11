package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;

public class ReadStatus implements DriverCommand {

    @Override
    public void run(Driver driver) {
        driver.readStatus();
    }

    @Override
    public String getCommand() {
        return "M625";
    }

    @Override
    public void setCommand(String newCommand) {
    }

    @Override
    public boolean isPrintingCommand() {
        return false;
    }
}
