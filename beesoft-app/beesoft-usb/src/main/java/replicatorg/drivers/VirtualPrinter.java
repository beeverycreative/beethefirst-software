package replicatorg.drivers;

// A fake, but agreeable driver.

import java.io.File;
import java.util.EnumSet;

import javax.vecmath.Point3d;

import org.w3c.dom.Node;

import replicatorg.app.exceptions.BuildFailureException;
import replicatorg.app.ui.mainWindow.ButtonsPanel;
import replicatorg.app.ui.panels.PrintSplashAutonomous;
import replicatorg.app.util.AutonomousData;
import replicatorg.machine.model.AxisId;
import replicatorg.machine.model.MachineModel;
import replicatorg.util.Point5d;

public class VirtualPrinter implements Driver, DriverQueryInterface {

	final String firmwareInfo = "No Firmware";
	final Version version = new Version(0,0);
	final Version minimumVersion = new Version(0,0);
	final Version preferredVersion = new Version(0,0);
	final boolean hasSoftStop = true;
	final boolean hasEStop = true;
	
	double toolTemperature = 0;
	double toolTemperatureSetting = 0;
	double platformTemperature = 0;
	double platformTemperatureSetting = 0;
	boolean isInitialized = false;
	MachineModel machineModel = new MachineModel();
	Point5d currentPosition = new Point5d();
	Point5d maximumFeedrates = new Point5d(1,1,1,1,1);
	Point3d[] currentOffset = new Point3d[]{
			new Point3d(),
			new Point3d(),
			new Point3d(),
			new Point3d(),
			new Point3d()};
	
	@Override
	public void loadXML(Node xml) {
		// TODO Auto-generated method stub
		machineModel.loadXML(xml);
	}

	@Override
	public boolean isPassthroughDriver() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void executeGCodeLine(String code) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isFinished() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isBufferEmpty() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void assessState() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasError() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DriverError getError() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkErrors() throws BuildFailureException {
		// TODO Auto-generated method stub

	}

	@Override
	public void initialize() throws VersionException {
		isInitialized = true;
	}

	@Override
	public void uninitialize() {
		isInitialized = false;
	}

	@Override
	public boolean isInitialized() {
		// TODO Auto-generated method stub
		return isInitialized;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public MachineModel getMachine() {
		return machineModel;
	}

	@Override
	public void setMachine(MachineModel m) {
		machineModel = m;
	}

	@Override
	public String getDriverName() {
		// TODO Auto-generated method stub
		return "VirtualPrinter";
	}

	@Override
	public String getFirmwareInfo() {
		// TODO Auto-generated method stub
		return firmwareInfo;
	}

	@Override
	public Version getVersion() {
		return version;
	}

	@Override
	public void updateManualControl() {
		// TODO Auto-generated method stub

	}

	@Override
	public Version getMinimumVersion() {
		// TODO Auto-generated method stub
		return minimumVersion;
	}

	@Override
	public Version getPreferredVersion() {
		// TODO Auto-generated method stub
		return preferredVersion;
	}

	@Override
	public void setCurrentPosition(Point5d p) throws RetryException {
		currentPosition = p;
	}

	@Override
	public void storeHomePositions(EnumSet<AxisId> axes) throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void recallHomePositions(EnumSet<AxisId> axes) throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean positionLost() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Point5d getCurrentPosition(boolean update) {
		// Note that we don't have a 
		if (currentPosition == null) {
			currentPosition = new Point5d();
		}
		return currentPosition;
	}

	@Override
	public void invalidatePosition() {
		currentPosition = null;
	}

	@Override
	public void queuePoint(Point5d p) throws RetryException {
		currentPosition = p;
	}

	@Override
	public Point3d getOffset(int i) {
		return currentOffset[i];
	}

	@Override
	public void setOffsetX(int i, double j) {
		currentOffset[i].x = j;

	}

	@Override
	public void setOffsetY(int i, double j) {
		currentOffset[i].y = j;

	}

	@Override
	public void setOffsetZ(int i, double j) {
		currentOffset[i].z = j;

	}

	@Override
	public Point5d getPosition() {
		// TODO Auto-generated method stub
		return currentPosition;
	}

	@Override
	public void requestToolChange(int toolIndex, int timeout)
			throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void selectTool(int toolIndex) throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFeedrate(double feed) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getCurrentFeedrate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void homeAxes(EnumSet<AxisId> axes, boolean positive, double feedrate)
			throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delay(long millis) throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void openClamp(int clampIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeClamp(int clampIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableDrives() throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableDrives() throws RetryException {
		// TODO Auto-generated method stub

	}


	public void enableAxes(EnumSet<AxisId> axes) throws RetryException {
		// Not all drivers support this method.
	}
	
	public void disableAxes(EnumSet<AxisId> axes) throws RetryException {
		// Not all drivers support this method.
	}

	@Override
	public void changeGearRatio(int ratioIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void readToolStatus() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getToolStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setMotorDirection(int dir) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMotorRPM(double rpm) throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMotorSpeedPWM(int pwm) throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public double getMotorRPM() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMotorSpeedPWM() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void enableMotor() throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableMotor(long millis) throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableMotor() throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSpindleRPM(double rpm) throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSpindleSpeedPWM(int pwm) throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSpindleDirection(int dir) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getSpindleRPM() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSpindleSpeedPWM() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void enableSpindle() throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableSpindle() throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTemperature(double temperature) throws RetryException {
		toolTemperature = temperature;
		toolTemperatureSetting = temperature;
	}

	@Override
	public void readTemperature() {
	}

	@Override
	public double getTemperature() {
		return toolTemperature;
	}

	@Override
	public double getTemperatureSetting() {
		return toolTemperatureSetting;
	}

	@Override
	public void setPlatformTemperature(double temperature)
			throws RetryException {
		platformTemperature = temperature;
		platformTemperatureSetting = temperature;
	}

	@Override
	public void readPlatformTemperature() {
	}

	@Override
	public double getPlatformTemperature() {
		return platformTemperature;
	}

	@Override
	public double getPlatformTemperatureSetting() {
		return platformTemperatureSetting;
	}

	@Override
	public void setChamberTemperature(double temperature) {
		// TODO Auto-generated method stub

	}

	@Override
	public void readChamberTemperature() {
		// TODO Auto-generated method stub

	}

	@Override
	public double getChamberTemperature() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void enableFloodCoolant() {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableFloodCoolant() {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableMistCoolant() {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableMistCoolant() {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableFan() throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableFan() throws RetryException {
		// TODO Auto-generated method stub

	}
	
	@Override 
	public void setAutomatedBuildPlatformRunning(boolean state) throws RetryException {
		//TODO: manually added
	}
	
	@Override
	public void openValve() throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeValve() throws RetryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void openCollet() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeCollet() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void unpause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop(boolean abort) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasSoftStop() {
		// TODO Auto-generated method stub
		return hasSoftStop;
	}

	@Override
	public boolean hasEmergencyStop() {
		// TODO Auto-generated method stub
		return hasEStop;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean heartbeat() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Point5d getMaximumFeedrates() {
		// TODO Auto-generated method stub
		return maximumFeedrates;
	}

	@Override
	public boolean hasAutomatedBuildPlatform() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void setDriverName(String name) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public double read() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getTotalExtrudedValue() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resetExtrudeSession() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getFirmwareVersion() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getBootloaderVersion() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getSerialNumber() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String dispatchCommand(String command) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String dispatchCommand(String command, Enum comtype) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isReady(boolean forceCheck) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isBusy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void readStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean getMachineStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public void setBusy(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setMachineReady(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resetToolTemperature() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isBootloader() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void readZValue() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setCoilCode(String coilCode) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateCoilCode() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String readResponse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void hiccup() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void hiccup(int mili, int nano) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addMachineListener(ButtonsPanel buttons) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCoilCode() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean printerRestarted() {
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
    public void stopTransfer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int sendCommandBytes(byte[] next) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String gcodeTransfer(File gcode, String estimatedTime, int nLines, PrintSplashAutonomous psAutonomous) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AutonomousData getPrintSessionsVariables() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getTransferPercentage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String startPrintAutonomous() {
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
    public int getLastLineNumber() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String gcodeSimpleTransfer(File gcode, PrintSplashAutonomous psAutonomous) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void readLastLineNumber() {
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
}
