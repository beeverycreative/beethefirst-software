package replicatorg.machine;

import javax.usb.UsbClaimException;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import javax.usb.UsbNotActiveException;
import org.w3c.dom.Node;

import replicatorg.drivers.Driver;
import replicatorg.drivers.DriverQueryInterface;
import replicatorg.drivers.RetryException;
import replicatorg.drivers.SimulationDriver;
import replicatorg.drivers.StopException;
import replicatorg.drivers.VersionException;
import replicatorg.drivers.commands.DriverCommand;
import replicatorg.machine.Machine.JobTarget;
import replicatorg.machine.model.MachineModel;
import replicatorg.model.GCodeSource;
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
     * in the future; instead use getDriverQueryInterface() *
     */
    //@Deprecated
    public Driver getDriver();

    /**
     * Get an interface to use to query the driver *
     */
    public DriverQueryInterface getDriverQueryInterface();

    /**
     * Get the simulation driver instance. Note that this interface will not be
     * supported in the future *
     */
    public SimulationDriver getSimulatorDriver();

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
    public void connect(String portName);

    /**
     * Disconnect the machine from the separately configured communication
     * channel *
     */
    public void disconnect();

    /**
     * Get information about the machine configuration, which also happens to be
     * a control interface to the machine.
     *
     * @return a Machine
     */
    public MachineModel getModel();
    
    public String getZValue();

    public String getMachineName();

    // Job level commands
    /**
     * Estimate the time required to process a job
     *
     * @param source GCode source of job to estimate
     * @throws UsbException
     * @throws UsbDisconnectedException
     * @throws UsbNotActiveException
     * @throws UsbClaimException
     * @throws VersionException
     * @throws StopException
     * @throws RetryException
     */
    public void estimate(GCodeSource source) throws VersionException, UsbClaimException, UsbNotActiveException, UsbDisconnectedException, UsbException, RetryException, StopException;

    /**
     * Run the job in a simulator
     */
    /**
     * Simulate the job on screen TODO: Pull this out of the driver and let the
     * UI handle it.
     *
     * @param source
     * @throws UsbException
     * @throws UsbDisconnectedException
     * @throws UsbNotActiveException
     * @throws UsbClaimException
     * @throws VersionException
     * @throws StopException
     * @throws RetryException
     */
    public void simulate(GCodeSource source) throws VersionException, UsbClaimException, UsbNotActiveException, UsbDisconnectedException, UsbException, RetryException, StopException;

    public boolean buildDirect(String arg);

    public boolean buildRemote(String remoteName);

    public void buildToFile(GCodeSource source, String path);

    public void upload(GCodeSource source, String remoteName);

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

    /**
     * Run a command on the driver *
     */
    public void runCommand(DriverCommand command);

    public int getStopwatch();

    public void setStopwatch(int stopwatch);

    // Query the machine controller
    public MachineState getMachineState();

    public int getLinesProcessed();

    public JobTarget getTarget();

    public boolean isPaused();

    public boolean isConnected();

    public boolean isSimulating();

    public boolean isInteractiveTarget();

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

    public void setLastBEECode(String code);
    
    public void setLastPrintedPoint(Point5d point);
}
