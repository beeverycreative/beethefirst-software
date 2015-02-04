package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;
import replicatorg.util.Point5d;

public class QueuePoint implements DriverCommand {

    Point5d destination;

    public QueuePoint(Point5d destination) {
        this.destination = destination;
    }

    @Override
    public void run(Driver driver) throws RetryException {
        driver.queuePoint(destination);
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
