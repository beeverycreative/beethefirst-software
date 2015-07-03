package replicatorg.drivers.commands;

import pt.beeverycreative.beesoft.filaments.FilamentControler;
import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;

public class SetCoilCode implements DriverCommand {

    String coilCode = FilamentControler.NO_FILAMENT_CODE;

    public SetCoilCode(String coilCode) {
        this.coilCode = coilCode;
    }

    @Override
    public void run(Driver driver) {

        driver.setCoilCode(coilCode);

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
