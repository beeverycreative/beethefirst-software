/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;
import replicatorg.drivers.StopException;

/**
 *
 * @author jgrego
 */
public class GetStandaloneVariables implements DriverCommand {

    @Override
    public String getCommand() {
        return "M32";
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
        driver.getPrintSessionsVariables();
    }
    
}
