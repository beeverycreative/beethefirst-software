/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package replicatorg.drivers.commands;


import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;
import replicatorg.drivers.Driver;

/**
 *
 * @author rui
 */
public class DispatchCommand implements DriverCommand {

    String command;
    Enum comtype = null;

    public DispatchCommand(String command) {
        this.command = command;
    }

    public DispatchCommand(String command, Enum comtype) {
        this.command = command;
        this.comtype = comtype;
    }

    @Override
    public void run(Driver driver) {
        if (comtype != null) {
            driver.dispatchCommand(command, comtype);
        } else if (comtype == null) {
            driver.dispatchCommand(command, COM.DEFAULT);
        }

    }
}
