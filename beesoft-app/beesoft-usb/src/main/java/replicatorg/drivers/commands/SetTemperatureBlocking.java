package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;

public class SetTemperatureBlocking implements DriverCommand {

    double temperature;

    public SetTemperatureBlocking(double temperature) {
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
