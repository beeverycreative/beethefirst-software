package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;

public class AssessState implements DriverCommand {

    @Override
    public void run(Driver driver) {
        driver.assessState();
    }

    @Override
    public String getCommand() {
        return "";
    }

    @Override
    public boolean isPrintingCommand() {
        return false;
    }

    @Override
    public void setCommand(String newCommand) {
    }

}
