package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;

public class SetFeedrate implements DriverCommand {

    double feedrate;

    public SetFeedrate(double feedrate) {
        this.feedrate = feedrate;
    }

    @Override
    public void run(Driver driver) throws RetryException {
        driver.setFeedrate(feedrate);
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