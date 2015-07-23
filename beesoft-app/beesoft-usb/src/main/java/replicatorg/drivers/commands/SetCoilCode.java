package replicatorg.drivers.commands;

import pt.beeverycreative.beesoft.filaments.FilamentControler;
import replicatorg.drivers.Driver;

public class SetCoilCode implements DriverCommand {

    String coilCode = FilamentControler.NO_FILAMENT_CODE;
    String coilText = "";

    public SetCoilCode(String coilCode, String coilText) {
        this.coilCode = coilCode;
        this.coilText = coilText;
    }

    @Override
    public void run(Driver driver) {
        driver.setCoilCode(coilCode, coilText);
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
