package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;

public class SetTemperatureBlocking implements DriverCommand {

    private final int temperature;

    public SetTemperatureBlocking(int temperature) {
        this.temperature = temperature;
    }

    @Override
    public void run(Driver driver) throws RetryException {
        driver.setTemperatureBlocking(temperature);
    }

    @Override
    public String getCommand() {
        return "M109 S" + temperature;
    }

    @Override
    public void setCommand(String newCommand) {
    }

    @Override
    public boolean isPrintingCommand() {
        return false;
    }
}
