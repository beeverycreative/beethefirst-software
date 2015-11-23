package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;

public class SetCoilText implements DriverCommand {

    private final String coilText;
    
    public SetCoilText() {
        this.coilText = "none";
    }

    public SetCoilText(String coilText) {
        this.coilText = coilText;
    }

    @Override
    public void run(Driver driver) {
        driver.setCoilText(coilText);
    }

    @Override
    public String getCommand() {
        return "M1000 " + coilText;
    }

    @Override
    public void setCommand(String newCommand) {
    }

    @Override
    public boolean isPrintingCommand() {
        return false;
    }
}
