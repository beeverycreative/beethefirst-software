package replicatorg.machine;

import org.w3c.dom.Node;
import replicatorg.app.util.AutonomousData;

import replicatorg.drivers.Driver;
import replicatorg.machine.model.MachineModel;
import replicatorg.util.Point5d;

/**
 * Anything that wants to talk to the machine controller should do so through
 * here. The goal here is to make this into a multiprocess-safe message
 * interface, so that everything below this (which is currently the machine
 * driver, simulator, and estimator) explicitly don't have to deal with it.
 *
 * The MachineController that implements this is probably a single thread, with
 * worker threads to handle serial communications or other things.
 *
 * This interface should allow for the configuration and control of a single 3d
 * printer, and another interface is required to provide a directory of
 * available printers, job queueing, etc.
 *
 * @author matt.mets
 *
 */
public interface MachineInterface {

    /**
     * Get the driver instance. Note that this interface will not be supported
     * in the future; instead use getDriver() *
     */
    //@Deprecated
    public Driver getDriver();

    /**
     * Dispose of this machine controller *
     */
    public void dispose();

    // Machine level commands
    /**
     * Tell the driver to reset *
     */
    public void reset();

    /**
     * Connect to the machine using a separately configured communication
     * channel *
     */
    // TODO: generic interface for non-serial machines.
    public void connect(boolean b);
    /**
     * Get information about the machine configuration, which also happens to be
     * a control interface to the machine.
     *
     * @return a Machine
     */
    public MachineModel getModel();
    
    // Job level commands

    public boolean buildDirect(String arg);

    public boolean buildRemote(String remoteName);

    public void pause();

    public void unpause();

    /**
     * Halt all machine motion, but leave other systems (e.g., temperature)
     * running *
     */
    public void stopMotion();

    /**
     * Stop the machine. *
     */
    public void stopAll();
    
    public void killSwitch();

    public int getStopwatch();

    public void setStopwatch(int stopwatch);

    // Query the machine controller
    public MachineState getMachineState();

    public boolean isConnected();

    public Node getMachineNode();

    /**
     * XML parser methods for trail positions and jogRate values *
     */
    public double getJogRateLowerValue();

    public double getJogRateMediumValue();

    public double getJogRateHigherValue();

    public Point5d getTablePoints(String pointName);

    public double getAcceleration(String acceTag);

    public double getFeedrate(String speedTag);

    public String getGCodePrintTest(String code);
    
    public void stopwatch();

    public void resumewatch();
    
    public String getLastFeedrate();

    public String getLastE();

    public String getLastAcceleration();
    
    public Point5d getLastPrintedPoint();
    
    public void setFilamentChanged(boolean isChanged);
    
    public boolean hasFilamentChanged();
    
    public void setLastPrintedPoint(Point5d point);
    
    public AutonomousData getAutonomousData();
}
