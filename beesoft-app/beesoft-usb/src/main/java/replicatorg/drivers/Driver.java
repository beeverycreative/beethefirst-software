/*
 Driver.java

 Provides an interface for driving various machines.

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

import org.w3c.dom.Node;
import pt.beeverycreative.beesoft.drivers.usb.PrinterInfo;
import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver.COM;

import replicatorg.app.exceptions.BuildFailureException;
import replicatorg.app.ui.mainWindow.ButtonsPanel;
import replicatorg.app.ui.panels.PrintSplashAutonomous;
import replicatorg.machine.model.MachineModel;
import replicatorg.util.Point5d;

/**
 * Driver interface with all methods definition.
 */
public interface Driver {

    /**
     * Loads a machine XML to load the driver params.
     *
     * @param xml Machine XML file.
     */
    public void loadXML(Node xml);

    /**
     * Should we bypass the parser?
     *
     * @return true if this driver executes GCodes directly, false if the parser
     * should be used to exercise it's interface.
     */
    public boolean isPassthroughDriver();

    /**
     * Execute a line of GCode directly (ie, don't use the parser)
     *
     * @param code The line of GCode that we should execute
     */
    public void executeGCodeLine(String code);

    /**
     * Execute a line of GCode directly (ie, don't use the parser)
     *
     * @param command
     * 
     * @return 
     */
    public String dispatchCommand(String command);

    /**
     * Dispatch a command to the Driver agent.
     *
     * @param command Command as a string. '\n' is inserted in this method.
     * @param comtype Type of communication we want to do (e.g - with or withoud
     * answer)
     * @return answer for the command sent
     */
    public String dispatchCommand(String command, COM comtype);

    /**
     * Transfers a GCode file. A PrintSplashAutonomous object is requested in
     * order to provide transfer progress feedback to that dialog. TODO: the
     * panel should obtain feedback another way!
     *
     * @param gcode the GCode file that is to be transferred
     * @param psAutonomous PrintSplashAutonomous object
     * @param header header to be included in the file when transferred (e.g.:
     * M31). If null, no header is included
     * @return error message
     */
    public String gcodeTransfer(File gcode, PrintSplashAutonomous psAutonomous, String header);
    public String gcodeTransfer(File gcode);

    /**
     * Start print via Autonomous mode.
     *
     */
    public void startPrintAutonomous();

    /**
     * Get print session variables during autonomous print.
     *
     */
    public void getPrintSessionsVariables();

    /**
     * Read machine temperature.
     *
     * @return actual temperature
     */
    public double read();

    /**
     * Read offset value from flash
     */
    public void readZValue();

    /**
     * Control cancel operation on UI to abort driver operations
     *
     * @param errorOccured type of error
     */
    public void setDriverError(boolean errorOccured);

    /**
     * Checks if driver is at a error condition.
     *
     * @return <li> true, if an error occured
     * <li> false, if is all ok
     */
    public boolean isDriverError();

    /**
     * Returns if BEESOFT is in transfer mode
     *
     * @return boolean indicating if it is in transfer mode
     */
    public boolean isTransferMode();

    /**
     * Stores the BEECODE/COIL CODE/Fillament Code in the printer.
     *
     *
     * @param coilText code to be set on the printer
     */
    public void setCoilText(String coilText);

    public void setInstalledNozzleSize(int microns);
    
    /**
     * Return the BEECODE/COILCODE/Filament Code in the printer. coilCode is
     * AXXX printer stores return AXXX.
     */
    public void updateCoilText();
    
    public void updateNozzleType();

    /**
     * Read data from the read endpoint.
     *
     * @return data available at the endpoint
     */
    public String readResponse();

    /**
     * Get Total Extruded Value since filament change.
     * @return 
     */
    public double getTotalExtrudedValue();

    /**
     * Get Firmware version.
     *
     * @return x.yy.z
     */
    public String getFirmwareVersion();

    /**
     * Get Bootloader version.
     *
     * @return x.yy.z
     */
    public String getBootloaderVersion();

    /**
     * Get SerialNumber version
     *
     * @return xxxxxxxxxx - 10 digits
     */
    public String getSerialNumber();

    /**
     * Resets extrusion variables.
     */
    public void resetExtrudeSession();

    /**
     * Checks if we finished with the last command.
     *
     * @return <li> true, if last command was processed
     * <li> false, if not.
     */
    public boolean isFinished();

    /**
     * Is our buffer empty? If don't have a buffer, its always true.
     *
     * @return <li> true, if buffer is empty
     * <li> false, if not.
     */
    public boolean isBufferEmpty();

    /**
     * Checks if printer is in bootloader
     *
     * @return <li> true, if printer answers as bootloader
     * <li> false, if not.
     */
    public boolean isBootloader();

    /**
     * Holds BEESOFT for 1 nano.
     */
    public void hiccup();

    /**
     * Holds BEESOFT for a period of mili and nano.
     *
     * @param mili miliseconds for application to be held
     * @param nano nanoseconds for application to be held
     */
    public void hiccup(int mili, int nano);

    /**
     * Check that the communication line is still up, the machine is still
     * connected, and that the machine state is still good.
     */
    public void assessState();

    /**
     * Check if the device has reported an error
     *
     * @return <li> true, if driver as an error
     * <li> false, if not.
     */
    public boolean hasError();

    /**
     * Get a string message for the first driver error.
     *
     * @return DriverError type.
     */
    public DriverError getError();

    /**
     * Checks for errors and handles them.
     *
     * @throws BuildFailureException exception due bad BEESOFT driver build
     */
    public void checkErrors() throws BuildFailureException;

    /**
     * Setups driver for use.
     *
     * @throws VersionException exception due driver version
     */
    public void initialize() throws VersionException;

    /**
     * Uninitializes driver (disconnects from machine).
     */
    public void uninitialize();

    /**
     * See if the driver has been successfully initialized.
     *
     * @return <li >true if the driver is initialized
     * <li> false, if not
     */
    public boolean isInitialized();

    /**
     * Clean up the driver.
     */
    public void dispose();

    /**
     * Get Machine object that handles BEESOFT.
     *
     * @return the current and only machine object
     */
    public MachineModel getMachine();

    /**
     * Sets machine agent through the machine model.
     *
     * @param m machine configuration.
     */
    public void setMachine(MachineModel m);

    /**
     * Get version, driver and firmware name information.
     *
     * @return Driver name as a string
     */
    public String getDriverName();

    /**
     * Gets firmware version
     *
     * @return firmware version as a string
     */
    public String getFirmwareInfo();

    /**
     * Gets firmware version.
     *
     * @return firmware version as a Version object to be comparable easily.
     */
    public Version getVersion();

    /**
     * Set driver name information.
     *
     * @param name Driver name.
     */
    public void setDriverName(String name);

    /**
     * Called at regular intervals when under manual control. Allows insertion
     * of machine-specific logic into each manual control panel update.
     */
    public void updateManualControl();

    /**
     * Gets minimum version accepted to be in the printer.
     *
     * @return lowest version accepted
     */
    public Version getMinimumVersion();

    /**
     * Gets prefered version accepted to be in the printer.
     *
     * @return prefered version accepted
     */
    public Version getPreferredVersion();

    /**
     * Tell the machine to consider its current position as being at p. Should
     * not move the machine position.
     *
     * @param p the point to map the current position to
     * @throws RetryException
     */
    public void setCurrentPosition(Point5d p) throws RetryException;

    /**
     * Checks if the machine is lost in space.
     *
     * @return <li> true, if doesn't know the current position
     * <li> false, if not.
     */
    public boolean positionLost();

    /**
     * Get the current machine position
     *
     * @param update True if the driver should be forced to query the machine
     * for its position, instead of using the cached value.
     * @return 5D point with all coordinates.
     */
    public Point5d getCurrentPosition(boolean update);
    
    public Point5d getCurrentPosition2();

    /**
     * Indicate that the currently maintained position may no longer be the
     * machine's position, and that the machine should be queried for its actual
     * location.
     */
    void invalidatePosition();

    /**
     * Queue the next point to move to.
     *
     * @param p The location to move to, in mm.
     * @throws RetryException exception due queue attempts
     */
    public void queuePoint(Point5d p) throws RetryException;

    /**
     * Get the current machine position - bypass version of getCurrentPosition
     *
     * @return 5D point with all coordinates.
     */
    public Point5d getPosition();

    public void getPosition2();
    
    /**
     * Sets the feedrate in mm/minute
     *
     * @param feed motor speed
     */
    public void setFeedrate(double feed);

    /**
     * Gets the feedrate in mm/minute
     *
     * @return current feedrate
     */
    public double getCurrentFeedrate();

    /**
     * Sets machine temperature.
     *
     * @param temperature Goal temperature
     */
    public void setTemperature(int temperature);
    public void setTemperatureBlocking(int temperature);

    /**
     * Read temperature from machine and updates internal variable.
     */
    public void readTemperature();

    /**
     * Reads temperature variable.
     *
     * @return actual machine temperature after synchronization
     */
    public double getTemperature();

    /**
     * Reads status from machine.
     */
    public void readStatus();

    /**
     * Sets if machine is busy - software side
     *
     * @param machineBusy <li> true, if machine is busy
     * <false> to free busy state
     */
    public void setBusy(boolean machineBusy);

    /**
     * Listener for main window buttons
     *
     * @param buttons object that handles main window buttons
     */
    public void addMachineListener(ButtonsPanel buttons);

    /**
     * Sets Autonomous state.
     *
     * @param b <li> true, if machine is in autonomy mode
     * <false> false, to free autonomy mode
     */
    public void setAutonomous(boolean b);

    /**
     * Stops current gcode transfer.
     */
    public void stopTransfer();

    /**
     * Checks if machine is in autonomy mode.
     *
     * @return <li> true, if so
     * <li> false if not
     */
    public boolean isAutonomous();

    /**
     * Checks if machine is in shutdown mode.
     *
     * @return <li> true, if so
     * <li> false if not
     */
    public boolean isONShutdown();

    /**
     * Gets last line number of the gcode processed in firmware (autonomy).
     *
     */
    public void readLastLineNumber();

    /**
     * Get the current machine position more directly than other 2 methods.
     *
     * @return 5D point with all coordinates.
     */
    public Point5d getActualPosition();

    /**
     * Get the machine position when shutdown was order.
     *
     * @return 5D point with all coordinates.
     */
    public Point5d getShutdownPosition();

    /**
     * Sets elapsed time for pause feature.
     *
     * @param time long time until now.
     * @return answer if set went ok
     */
    public String setElapsedTime(long time);

    /**
     * Sets if machine is ready - firmware side
     *
     * @param b <li> true, if machine is ready
     * <li> false,if not
     */
    public void setMachineReady(boolean b);

    /**
     * Checks if machine is ready - firmware side.
     *
     * @return <true> if machine ready
     * <false> if not
     */
    public boolean getMachineReady();

    /**
     * Checks if machine is ready - firmware side.
     *
     * @param forceCheck
     * @return <li> true, if so
     * <li> false if not
     */
    public boolean isReady(boolean forceCheck);

    /**
     * Checks if machine is busy - software side.
     *
     * @return <li> true, if so
     * <li> false if not
     */
    public boolean isBusy();

    /**
     * Set machine temperature to 0.
     */
    public void resetToolTemperature();

    /**
     * Returns coil code.
     *
     * @return string containing coil text
     */
    public String getCoilText();
    
    /**
     * Gets the info object on the currently connected printer
     * 
     * @return 
     */
    public PrinterInfo getConnectedDevice();
    
    public void resetBootloaderAndFirmwareVersion();
    public boolean getMachinePaused();
    public boolean isAlive();
    public void setMachinePaused(boolean machinePaused);
    public void closeFeedback();
    public int getQueueSize();
    public String getLastStatusMessage();
}
