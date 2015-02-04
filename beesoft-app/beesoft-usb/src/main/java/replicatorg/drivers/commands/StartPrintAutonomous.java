package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;

public class StartPrintAutonomous implements DriverCommand {

    @Override
    public void run(Driver driver) {

        driver.startPrintAutonomous();

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
