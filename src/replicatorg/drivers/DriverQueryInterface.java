package replicatorg.drivers;

import javax.vecmath.Point3d;

import replicatorg.util.Point5d;

/**
 * Interface for querying a Driver about its current state. This is used by
 * the parser when devolving GCodes into DriverCommands.
 * 
 * @author matt.mets
 */
public interface DriverQueryInterface {
	public Point3d getOffset(int i);
	
	public Point5d getMaximumFeedrates();

	public double getSpindleRPM();
	
	public double getMotorRPM();
	
	public int getMotorSpeedPWM();
	
	public double getTemperature();
	public double getTemperatureSetting();

	public boolean hasAutomatedBuildPlatform();
	
	public double getPlatformTemperature();
	public double getPlatformTemperatureSetting();

	public Point5d getCurrentPosition(boolean b);

	public boolean isPassthroughDriver();
        
        public boolean isBootloader();        
	
	public Version getVersion();
	public Version getPreferredVersion();
        
        public void setMachineReady(boolean b);
        
        public boolean getMachineStatus();
        //this one is on the copping block
        public boolean isReady(boolean forceCheck); 

        public boolean isBusy();
        
        public void setBusy(boolean b);

        public void resetToolTemperature();
        
        public String getCoilCode();
    
        public void setCoilCode(String CoilCode);
         


}
