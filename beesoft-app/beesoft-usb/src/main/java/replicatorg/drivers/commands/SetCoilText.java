package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;

public class SetCoilText implements DriverCommand {

    String coilText = "";

    public SetCoilText(String coilText) {
        this.coilText = coilText;
    }

    @Override
    public void run(Driver driver) {
        driver.setCoilText(coilText);
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
