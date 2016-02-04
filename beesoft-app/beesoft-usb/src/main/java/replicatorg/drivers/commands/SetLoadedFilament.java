package replicatorg.drivers.commands;

import pt.beeverycreative.beesoft.filaments.Filament;
import replicatorg.drivers.Driver;

public class SetLoadedFilament implements DriverCommand {

    private final Filament filament;

    public SetLoadedFilament() {
        this.filament = null;
    }

    public SetLoadedFilament(Filament filament) {
        this.filament = filament;
    }

    @Override
    public void run(Driver driver) {
        if (filament != null) {
            driver.setCoilText(filament.getName());
        } else {
            driver.setCoilText("none");
        }
    }

    @Override
    public String getCommand() {
        if (filament != null) {
            return "M1000 " + filament.getName();
        } else {
            return "M1000 none";
        }
    }

    @Override
    public void setCommand(String newCommand) {
    }

    @Override
    public boolean isPrintingCommand() {
        return false;
    }
}
