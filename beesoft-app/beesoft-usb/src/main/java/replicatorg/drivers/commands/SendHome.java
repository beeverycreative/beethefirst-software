package replicatorg.drivers.commands;

import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;
import replicatorg.drivers.StopException;

/**
 *
 * @author dev
 */
public class SendHome implements DriverCommand {

    private final String orientation;

    public SendHome() {
        this.orientation = "";
    }

    public SendHome(String orientation) {
        this.orientation = orientation;
    }

    @Override
    public String getCommand() {
        if (orientation.equals("X")) {
            return "G28 X";
        } else if (orientation.equals("Y")) {
            return "G28 Y";
        } else if (orientation.equals("Z")) {
            return "G28 Z";
        } else if (orientation.equals("XY")) {
            return "G28 XY";
        } else {
            return "G28";
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
        // not sure if more orientations are possible, add as convenient
        if (orientation.equals("X")) {
            driver.dispatchCommand("G28 X", COM.NO_RESPONSE);
        } else if (orientation.equals("Y")) {
            driver.dispatchCommand("G28 Y", COM.NO_RESPONSE);
        } else if (orientation.equals("Z")) {
            driver.dispatchCommand("G28 Z", COM.NO_RESPONSE);
        } else if (orientation.equals("XY")) {
            driver.dispatchCommand("G28 XY", COM.NO_RESPONSE);
        } else {
            driver.dispatchCommand("G28", COM.NO_RESPONSE);
        }
    }

}
