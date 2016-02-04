package replicatorg.drivers.commands;

import pt.beeverycreative.beesoft.filaments.Nozzle;
import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;
import replicatorg.drivers.StopException;

/**
 *
 * @author jgrego
 */
public class SetInstalledNozzle implements DriverCommand {

    private final Nozzle nozzle;

    public SetInstalledNozzle() {
        nozzle = null;
    }

    public SetInstalledNozzle(Nozzle nozzle) {
        this.nozzle = nozzle;
    }

    @Override
    public String getCommand() {
        if (nozzle != null) {
            return "M1024 S" + nozzle.getSizeInMicrons();
        } else {
            return "M1024 S0";
        }
    }

    @Override
    public void setCommand(String newCommand) {
    }

    @Override
    public boolean isPrintingCommand() {
        return false;
    }

    @Override
    public void run(Driver driver) throws RetryException, StopException {
        if (nozzle != null) {
            driver.setInstalledNozzleSize(nozzle.getSizeInMicrons());
        } else {
            driver.setInstalledNozzleSize(-1);
        }
    }

}
