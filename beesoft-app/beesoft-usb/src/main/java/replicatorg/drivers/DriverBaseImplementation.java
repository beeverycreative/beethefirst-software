/*
 DriverBaseImplementation.java

 A basic driver implementation to build from.

 Part of the ReplicatorG project - http://www.replicat.org
 Copyright (c) 2008 Zach Smith
 Copyright (c) 2013 BEEVC - Electronic Systems

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package replicatorg.drivers;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import javax.vecmath.Point3d;

import org.w3c.dom.Node;
import pt.beeverycreative.beesoft.drivers.usb.PrinterInfo;

import replicatorg.app.Base;
import replicatorg.app.exceptions.BuildFailureException;
import replicatorg.app.ui.mainWindow.ButtonsPanel;
import replicatorg.app.ui.panels.PrintSplashAutonomous;
import replicatorg.machine.model.AxisId;
import replicatorg.machine.model.MachineModel;
import replicatorg.util.Point5d;

public abstract class DriverBaseImplementation implements Driver {
//	// our gcode parser
//	private GCodeParser parser;

    // models for our machine
    protected MachineModel machine;
    // Driver name
    protected String driverName;
    // our firmware version info
    private final String firmwareName = "Unknown";
    protected Version version = new Version(0, 0);
    protected Version preferredVersion = new Version(0, 0);
    protected Version minimumVersion = new Version(0, 0);
    // our point offsets
    private Point3d[] offsets;
    // are we initialized?
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    // our error variable.
    ConcurrentLinkedQueue<DriverError> errorList;
    // how fast are we moving in mm/minute
    private double currentFeedrate;
    // what is our mode of positioning?
    protected int positioningMode = 0;
    static public int ABSOLUTE = 0;
    static public int INCREMENTAL = 1;
    /**
     * Support for emergency stop is not assumed until it is detected. Detection
     * of this feature should be in initialization.
     */
    protected boolean hasEmergencyStop = false;
    /**
     * Support for soft stop (e.g. for continuous jog) is not assumed until it
     * is detected. Detection of this feature should be in initialization.
     */
    protected boolean hasSoftStop = false;
    protected boolean isBootloader = true;

    /**
     * Creates the driver object.
     */
    public DriverBaseImplementation() {
        errorList = new ConcurrentLinkedQueue<DriverError>();

        // initialize our offsets
        offsets = new Point3d[7];
        for (int i = 0; i < 7; i++) {
            offsets[i] = new Point3d();  // Constructs and initializes a Point3d to (0,0,0)
        }
        // TODO: do this properly.
        machine = new MachineModel();
        // This must be initialize anyway so, it doesnt matter what name does it have. 
        // The truth is that the name come from the xml
        driverName = "virtualprinter";
    }

    @Override
    public PrinterInfo getConnectedDevice() {
        return null;
    }

    @Override
    public void loadXML(Node xml) {
    }

    @Override
    public void updateManualControl() {
    }

    @Override
    public boolean isPassthroughDriver() {
        return false;
    }

    @Override
    public void executeGCodeLine(String code) {
        Base.logger.log(Level.SEVERE, "Ignoring executeGCode command: {0}", code);
    }

    @Override
    public String dispatchCommand(String code) {
        Base.logger.log(Level.SEVERE, "Ignoring executeGCode command: {0}", code);
        return "";
    }

    @Override
    public int sendCommandBytes(byte[] next) {
        Base.logger.log(Level.SEVERE, "Ignoring sending bytes{0}", Arrays.toString(next));
        return -1;
    }

    @Override
    public String dispatchCommand(String code, Enum comtype) {
        Base.logger.log(Level.SEVERE, "Ignoring executeGCode command: {0}:{1}", new Object[]{code, comtype});
        return "";
    }

    @Override
    public double read() {
        return 30;
    }

    @Override
    public String getFirmwareVersion() {
        return "";
    }

    @Override
    public String getBootloaderVersion() {
        return "";
    }

    @Override
    public String getSerialNumber() {
        return "";
    }

    @Override
    public double getTotalExtrudedValue() {
        return 0.0;
    }

    @Override
    public void resetExtrudeSession() {
    }

    @Override
    public boolean isBootloader() {
        return true;
    }

    public boolean printerRestarted() {
        return false;
    }

    public void read(String code) {
        Base.logger.log(Level.SEVERE, "Ignoring executeGCode command: {0}", code);
    }

    @Override
    public void dispose() {
        if (Base.logger.isLoggable(Level.FINE)) {
            Base.logger.log(Level.FINE, "Disposing of driver {0}", getDriverName());
        }
//		parser = null;
    }

    @Override
    public void initialize() throws VersionException {
        setInitialized(true);
    }

    @Override
    public void uninitialize() {
        setInitialized(false);
    }

    public void setInitialized(boolean status) {
        synchronized (isInitialized) {
            isInitialized.set(status);
            if (!status) {
                invalidatePosition();
            } //isBootloader = true;
            // Triggers listener for main window buttons
            // Base.getMainWindow().getButtons().updateFromMachine(Base.getMainWindow().getMachine());
        }
    }

    @Override
    public boolean isInitialized() {
        return isInitialized.get();
    }

    @Override
    public void hiccup() {
    }

    @Override
    public void hiccup(int mili, int nano) {
    }

    @Override
    public void assessState() {
    }

    protected void setError(DriverError newError) {
        errorList.add(newError);
    }

    protected void setError(String e) {
        setError(new DriverError(e, true));
    }

    @Override
    public boolean hasError() {
        return (errorList.size() > 0);
    }

    @Override
    public DriverError getError() {
        return errorList.remove();
    }

    @Deprecated
    @Override
    public void checkErrors() throws BuildFailureException {
        if (errorList.size() > 0) {
            throw new BuildFailureException(getError().getMessage());
        }
    }

    @Override
    public boolean isFinished() {
        return true;
    }

    @Override
    public boolean isBufferEmpty() {
        return true;
    }

    @Override
    public String getFirmwareInfo() {
        return firmwareName + " v" + getVersion();
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public Version getMinimumVersion() {
        return minimumVersion;
    }

    @Override
    public Version getPreferredVersion() {
        return preferredVersion;
    }
    protected final AtomicReference<Point5d> currentPosition
            = new AtomicReference<Point5d>(null);

    @Override
    public void setCurrentPosition(Point5d p) throws RetryException {
        currentPosition.set(p);
    }

    @Override
    public void invalidatePosition() {
//		System.err.println("invalidating position.");
        currentPosition.set(null);

    }

    public void invalidateAxes(EnumSet<AxisId> home, boolean positive) throws RetryException {

        if (home.size() > 1) {
            invalidatePosition();
            return;
        }

        Point5d temp = new Point5d(currentPosition.get());

        for (AxisId axis : home) {
            temp.setAxis(axis, 0.0);
        }

        currentPosition.set(temp);

    }

    protected Point5d reconcilePosition() throws RetryException {
        throw new RuntimeException("Position reconcilliation requested, but not implemented for this driver");
    }

    @Override
    public boolean positionLost() {
        return (currentPosition.get() == null);
    }

    @Override
    public Point5d getCurrentPosition(boolean forceUpdate) {
        synchronized (currentPosition) {
            // If we are lost, or an explicit update has been requested, poll the machine for it's state. 
            if (positionLost() || forceUpdate) {
                try {
                    // Try to reconcile our position. 
                    Point5d newPoint = reconcilePosition();
                    //currentPosition.set(newPoint);

                } catch (RetryException e) {
                    Base.logger.severe("Attempt to reconcile machine position failed, due to Retry Exception");
                }
            }

            // If we are still lost, just return a zero position.
            if (positionLost()) {
                return new Point5d();
            }

            return new Point5d(currentPosition.get());
        }
    }
    
    @Override
    public Point5d getCurrentPosition2() {
        synchronized(currentPosition) {
            if(currentPosition.get() == null) {
                try {
                    currentPosition.wait(5000);
                } catch (InterruptedException ex) {
                }
            }
            
            return currentPosition.get();
        }
    }

    @Override
    public Point5d getPosition() {
        return getCurrentPosition(false);
    }
    
    @Override
    public void getPosition2() {
        
    }

    @Override
    public void queuePoint(Point5d p) throws RetryException {
        setInternalPosition(p);
    }

    protected void setInternalPosition(Point5d position) {
        currentPosition.set(position);
    }

    @Override
    public void setFeedrate(double feed) {
        currentFeedrate = feed;
    }

    @Override
    public double getCurrentFeedrate() {
        return currentFeedrate;
    }

    public Point5d getDelta(Point5d p) {
        Point5d delta = new Point5d();
        Point5d current = getCurrentPosition(false);

        delta.sub(p, current); // delta = p - current
        delta.absolute(); // absolute value of each component

        return delta;
    }

    @Override
    public MachineModel getMachine() {
        return machine;
    }

    @Override
    public void setMachine(MachineModel m) {
        machine = m;
    }

    public void enableDrives() throws RetryException {
        machine.enableDrives();
    }

    public void disableDrives() throws RetryException {
        machine.disableDrives();
    }

    @Override
    public void setTemperature(double temperature) throws RetryException {
        machine.currentTool().setTargetTemperature(temperature);
    }

    @Override
    public void setTemperatureBlocking(double temperature) throws RetryException {
        machine.currentTool().setTargetTemperature(temperature);
    }

    public void resetTemperature() throws RetryException {
        machine.currentTool().setTargetTemperature(0);
    }

    @Override
    public void readTemperature() {
    }

    @Override
    public void readZValue() {
    }

    @Override
    public String readResponse() {
        return "";
    }

    @Override
    public double getTemperature() {
        return machine.currentTool().getExtruderTemperature();
    }

    @Override
    public void setDriverName(String name) {
        this.driverName = name;
    }

    @Override
    public String getDriverName() {
        return driverName;
    }

    @Override
    public boolean isReady(boolean forceCheck) {
        return false;
    }

    public void reset() {
    }

    @Override
    public void readStatus() {
    }

    @Override
    public void setMachineReady(boolean b) {
        machine.setMachineReady(b);
    }

    @Override
    public boolean getMachineStatus() {
        return machine.getMachineReady();
    }

    @Override
    public boolean getMachinePaused() {
        return machine.getMachinePaused();
    }

    @Override
    public void setMachinePaused(boolean machinePaused) {
        machine.setMachinePaused(machinePaused);
    }

    @Override
    public boolean isBusy() {
        return machine.getMachineBusy();

    }

    @Override
    public void setBusy(boolean machineBusy) {
        machine.setMachineBusy(machineBusy);
    }

    @Override
    public void resetToolTemperature() {
        machine.currentTool().setCurrentTemperature(0, 0);
    }

    @Override
    public void addMachineListener(ButtonsPanel buttons) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setCoilText(String coilText) {
        machine.setCoilText(coilText);
    }

    @Override
    public String getCoilText() {
        return machine.getCoilText();
    }

    @Override
    public void updateCoilText() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAutonomous(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isAutonomous() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isONShutdown() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stopTransfer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String gcodeTransfer(File gcode, String estimatedTime, int nLines, PrintSplashAutonomous psAutonomous) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void getPrintSessionsVariables() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getTransferPercentage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void startPrintAutonomous() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDriverError(boolean errorOccured) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDriverError() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isTransferMode() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void readLastLineNumber() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String gcodeSimpleTransfer(File gcode, PrintSplashAutonomous psAutonomous) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Point5d getActualPosition() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String setElapsedTime(long time) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Point5d getShutdownPosition() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resetBootloaderVersion() {

    }
}
