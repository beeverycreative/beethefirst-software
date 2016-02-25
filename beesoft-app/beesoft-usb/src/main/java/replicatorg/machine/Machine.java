/*
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
package replicatorg.machine;

import org.w3c.dom.Node;
import replicatorg.app.util.AutonomousData;

import replicatorg.drivers.Driver;
import replicatorg.util.Point5d;

/**
 * The MachineController object controls a single machine. It contains a single
 * machine driver object. All machine operations (building, stopping, pausing)
 * are performed asynchronously by a thread maintained by the MachineController;
 * calls to MachineController ordinarily trigger an operation and return
 * immediately.
 *
 * When the machine is paused, the machine thread waits on notification to the
 * machine thread object.
 *
 * In general, the machine thread should *not* be interrupted, as this can cause
 * synchronization issues. Interruption should only really happen on hanging
 * connections and shutdown.
 *
 * @author phooky
 *
 */
public class Machine implements MachineInterface {

    public enum RequestType {
        // Set up the connection to the machine

        CONNECT, // Establish connection with the target
        DISCONNECT, // Detach from target
        DISCONNECT_REMOTE_BUILD, // Disconnect from a remote build without stopping it.
        RESET, // Reset the driver

        // Start a build
        SIMULATE, // Build to the simulator
        BUILD_DIRECT, // Build in real time on the machine
        BUILD_TO_FILE, // Build, but instruct the machine to save it to the
        // local filesystem
        BUILD_TO_REMOTE_FILE, // Build, but instruct the machine to save it to
        // the machine's filesystem
        BUILD_REMOTE, // Instruct the machine to run a build from it's
        // filesystem

        // Control a build
        PAUSE, // Pause the current build
        UNPAUSE, // Unpause the current build
        STOP_MOTION, // Stop all motion and abort the current build
        STOP_ALL, // Stop everything (motion and actuators) and abort the current build

        // Interactive command
        RUN_COMMAND, // Run a single command on the driver, interleaved with the
        // build.

        SHUTDOWN,	// Stop build (disconnect if building remotely), and stop the thread. 
    }

    public enum JobTarget {

        /**
         * No target selected.
         */
        NONE,
        /**
         * Operations are being simulated.
         */
        SIMULATOR,
        /**
         * Operations are performed on a physical machine.
         */
        MACHINE,
        /**
         * Operations are being captured to an SD card on the machine.
         */
        REMOTE_FILE,
        /**
         * Operations are being captured to a file.
         */
        FILE
    };

//	// Test idea for a print job: specifies a gcode source and a target
//	class JobInformation {
//		JobTarget target;
//		GCodeSource source;
//
//		public JobInformation(JobTarget target, GCodeSource source) {
//
//		}
//	}
    /**
     * Get the machine state. This is a snapshot of the state when the method
     * was called, not a live object.
     *
     * @return a copy of the machine's state object
     */
    @Override
    public MachineState getMachineState() {
        return machineThread.getMachineState();
    }
    MachineThread machineThread;
    final MachineCallbackHandler callbackHandler;
    // TODO: WTF is this here for.
    // this is the xml config for this machine.
    protected Node machineNode;


    /**
     * Creates the machine object.
     * @param mNode
     * @param callbackHandler
     */
    public Machine(Node mNode, MachineCallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;

        machineNode = mNode;
        machineThread = new MachineThread(this, mNode);
        machineThread.start();
    }

    @Override
    public boolean buildRemote(String remoteName) {
        machineThread.scheduleRequest(new MachineCommand(
                RequestType.BUILD_REMOTE, remoteName));
        return true;
    }
    // The estimate function now checks for some sources of error
    // needs a way to return failure
    private String message;
    private long numWarnings;
    private long numErrors;

    /**
     * Begin running a job.
     * @param arg
     * @return 
     */
    @Override
    public boolean buildDirect(String arg) {
        machineThread.scheduleRequest(new MachineCommand(RequestType.BUILD_DIRECT, arg));

        return true;
    }

    /**
     * Allows getting maching node
     *
     * @return Machine Node name
     */
    @Override
    public Node getMachineNode() {
        return machineNode;
    }

    @Override
    public Driver getDriver() {
        return machineThread.getDriver();
    }
    
    @Override
    public void stopMotion() {
        machineThread.scheduleRequest(new MachineCommand(RequestType.STOP_MOTION,
                ""));
    }

    @Override
    public void stopAll() {
        machineThread.scheduleRequest(new MachineCommand(RequestType.STOP_ALL,
                ""));
    }

    @Override
    public void killSwitch() {
        machineThread.killSwitch();
        
    }

    @Override
    synchronized public boolean isConnected() {
        return machineThread.isConnected();
    }

    @Override
    public void pause() {
        machineThread.scheduleRequest(new MachineCommand(RequestType.PAUSE,
                ""));
    }

    @Override
    public void unpause() {
        machineThread.scheduleRequest(new MachineCommand(RequestType.UNPAUSE,
                ""));
    }

    @Override
    public void reset() {
        machineThread.scheduleRequest(new MachineCommand(RequestType.RESET,
                ""));
    }


    @Override
    public void connect(boolean force) {
        // recreate thread if stopped
        // TODO: Evaluate this!
        if (!machineThread.isAlive() || force) {
            machineThread = new MachineThread(this, machineNode);
            machineThread.start();
        }

        machineThread.scheduleRequest(new MachineCommand(RequestType.CONNECT,
                ""));
    }

    @Override
    public double getJogRateLowerValue() {
        return machineThread.getJogRateLowerValue();
    }

    @Override
    public double getJogRateMediumValue() {
        return machineThread.getJogRateMediumValue();
    }

    @Override
    public double getJogRateHigherValue() {
        return machineThread.getJogRateHigherValue();
    }

    @Override
    public int getStopwatch() {
        return machineThread.getStopwatch();
    }

    @Override
    public void setStopwatch(int stopwatch) {
        machineThread.setStopwatch(stopwatch);
    }

    @Override
    public void stopwatch() {
        machineThread.stopwatch();
    }

    @Override
    public void resumewatch() {
        machineThread.resumeWatch();
    }

    @Override
    public String getLastFeedrate() {
        return machineThread.getLastFeedrate();
    }

    @Override
    public String getLastE() {
        return machineThread.getLastE();
    }

    @Override
    public String getLastAcceleration() {
        return machineThread.getLastAcceleration();
    }

    @Override
    public Point5d getLastPrintedPoint() {
        return machineThread.getLastPrintedPoint();
    }

    @Override
    public void setLastPrintedPoint(Point5d point) {
        machineThread.setLastPrintedPoint(point);
    }

    @Override
    public void setFilamentChanged(boolean isChanged) {
        machineThread.setFilamentChanged(isChanged);
    }

    @Override
    public boolean hasFilamentChanged() {
        return machineThread.hasFilamentChanged();
    }

    @Override
    public Point5d getTablePoints(String pointName) {
        return machineThread.getTablePoints(pointName);
    }

    @Override
    public String getGCodePrintTest(String code) {
        return machineThread.getGCodePrintTest(code);
    }

    @Override
    public double getAcceleration(String acceTag) {
        return machineThread.getAcceleration(acceTag);
    }

    @Override
    public double getFeedrate(String speedTag) {
        return machineThread.getFeedrate(speedTag);
    }

    @Override
    public void dispose() {
        if (machineThread != null) {
            machineThread.scheduleRequest(new MachineCommand(
                    RequestType.SHUTDOWN, ""));
            // Wait 5 seconds for the thread to stop.
            try {
                machineThread.join(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void emitStateChange(MachineState current, String message) {
        MachineStateChangeEvent e = new MachineStateChangeEvent(this, current, message);

        callbackHandler.schedule(e);
    }
    
    @Override
    public AutonomousData getAutonomousData() {
        return null;
    }
}
