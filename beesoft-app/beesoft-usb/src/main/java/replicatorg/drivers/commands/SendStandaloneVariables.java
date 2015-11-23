package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;

public class SendStandaloneVariables implements DriverCommand {

    private final int minutes;

    public SendStandaloneVariables(int minutes) {
        this.minutes = minutes;
    }

    @Override
    public String getCommand() {
        return "M31 A" + minutes;
    }

    @Override
    public void setCommand(String newCommand) {
    }

    @Override
    public boolean isPrintingCommand() {
        return false;
    }

    @Override
    public void run(Driver driver) throws RetryException {
        driver.dispatchCommand("M31 A" + minutes);
    }
}
