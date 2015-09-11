package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;

public class SendStandaloneVariables implements DriverCommand {

    private final int minutes;
    private final int lines;

    public SendStandaloneVariables() {
        this.minutes = 0;
        this.lines = 0;
    }

    public SendStandaloneVariables(int lines) {
        this.minutes = 0;
        this.lines = lines;
    }

    public SendStandaloneVariables(int minutes, int lines) {
        this.minutes = minutes;
        this.lines = lines;
    }

    @Override
    public String getCommand() {
        return "M31 A" + minutes + " L" + lines;
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
        driver.dispatchCommand("M31 A" + minutes + " L" + lines);
    }
}
