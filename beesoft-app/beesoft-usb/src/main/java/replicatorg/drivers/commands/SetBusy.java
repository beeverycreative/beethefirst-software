/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;

public class SetBusy implements DriverCommand {

    boolean machineBusy;

    public SetBusy(boolean machineBusy) {
        this.machineBusy = machineBusy;
    }

    @Override
    public void run(Driver driver) throws RetryException {
        driver.setBusy(machineBusy);
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