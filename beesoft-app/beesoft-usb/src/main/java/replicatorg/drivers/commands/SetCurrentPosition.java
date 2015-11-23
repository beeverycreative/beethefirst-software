package replicatorg.drivers.commands;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;
import replicatorg.util.Point5d;

public class SetCurrentPosition implements DriverCommand {

    Point5d point;

    public SetCurrentPosition(Point5d point) {
        this.point = point;
    }

    @Override
    public void run(Driver driver) throws RetryException {
        driver.setCurrentPosition(point);
    }

    @Override
    public String getCommand() {
        DecimalFormat df;
        DecimalFormatSymbols dfs;

        dfs = DecimalFormatSymbols.getInstance();
        dfs.setDecimalSeparator('.');
        df = new DecimalFormat("#.######", dfs);
        
        return "G92 X" + df.format(point.x()) + " Y" + df.format(point.y()) 
                + " Z" + df.format(point.z());
    }

    @Override
    public void setCommand(String newCommand) {
    }

    @Override
    public boolean isPrintingCommand() {
        return false;
    }
}
