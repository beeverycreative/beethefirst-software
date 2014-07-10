package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;

public class StartPrintAutonomous implements DriverCommand {
    
       
    @Override
    public void run(Driver driver) {

            driver.startPrintAutonomous();

    }
}
