
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
        return "";
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
        switch (orientation) {
            case "X":
                driver.dispatchCommand("G28 X", COM.NO_RESPONSE);
                break;
                
            case "Y":
                driver.dispatchCommand("G28 Y", COM.NO_RESPONSE);
                break;
                
            case "Z":
                driver.dispatchCommand("G28 Z", COM.NO_RESPONSE);
                break;
                
            case "XY":
                driver.dispatchCommand("G28 XY", COM.NO_RESPONSE);
                break;
                
            default:
                driver.dispatchCommand("G28", COM.NO_RESPONSE);
                break;
        }
    }

}
