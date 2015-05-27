package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;

public class SelectTool implements DriverCommand {

    int toolNumber;

    public SelectTool(int toolNumber) {
        this.toolNumber = toolNumber;
    }

    @Override
    public void run(Driver driver) {
        driver.getMachine().selectTool(toolNumber);
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
