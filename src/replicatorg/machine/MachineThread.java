package replicatorg.machine;

/**
 * Copyright (c) 2013 BEEVC - Electronic Systems
 */
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.usb.UsbClaimException;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import javax.usb.UsbNotActiveException;


import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import replicatorg.app.ProperDefault;

import replicatorg.app.Base;
import replicatorg.app.tools.XML;
import replicatorg.drivers.Driver;
import replicatorg.drivers.DriverError;
import replicatorg.drivers.DriverFactory;
import replicatorg.drivers.OnboardParameters;
import replicatorg.drivers.RetryException;
import replicatorg.drivers.SDCardCapture;
import replicatorg.drivers.SimulationDriver;
import replicatorg.drivers.StopException;
import replicatorg.drivers.VersionException;
import replicatorg.drivers.commands.AssessState;
import replicatorg.drivers.commands.DriverCommand;
import replicatorg.machine.Machine.JobTarget;
import replicatorg.machine.Machine.RequestType;
import replicatorg.machine.model.MachineModel;
import replicatorg.model.GCodeSource;
import replicatorg.model.GCodeSourceCollection;
import replicatorg.model.StringListSource;
import replicatorg.util.Point5d;

/**
 * The MachineThread is responsible for communicating with the machine.
 */
class MachineThread extends Thread {

    AssessStatusThread statusThread;
    private double jogRateLowerValue;
    private double jogRateMediumValue;
    private double jogRateHigherValue;
    private HashMap<String, Point5d> tablePoints;
    private int stopwatch;

    class AssessStatusThread extends Thread {

        MachineThread machineThread;

        public AssessStatusThread(MachineThread machineThread) {
            super("Assess Status");
            this.machineThread = machineThread;
        }

        @Override
        public void run() {
            boolean flag;
            boolean isBuilding;

            while (true) {
                try {

                    // Send out a request, then sleep for a bit, then start over.
                    DriverCommand assessCommand = new AssessState();
                    flag = machineThread.isConnected();

                    if (!flag) {
                        machineThread.notConnectedMessage();
                        Base.diposeAllOpenWindows();
                        machineThread.setState(new MachineState(MachineState.State.NOT_ATTACHED));

                        driver.initialize();

                        machineThread.scheduleRequest(new MachineCommand(
                                RequestType.CONNECT, assessCommand));
                    }
                    machineThread.scheduleRequest(new MachineCommand(
                            RequestType.RUN_COMMAND, assessCommand));

                    sleep(500, 1);

                } catch (InterruptedException e) {
                    Base.status_thread_died = true;
                    Base.writeLog("taking assess status thread down");
                    machineThread.setState(new MachineState(MachineState.State.NOT_ATTACHED));


                } catch (VersionException E) {
                    Base.status_thread_died = true;
                    Base.writeLog("Initialize, probably failed.");
                    machineThread.setState(new MachineState(MachineState.State.NOT_ATTACHED));

                }

            }

        }
    }

    // TODO: Rethink this.
    class Timer {

        private long lastEventTime = 0;
        private boolean enabled = false;
        private long intervalMs = 1000;

        public void start(long interval) {
            enabled = true;
            intervalMs = interval;
        }

        public void stop() {
            enabled = false;
        }

        // send out updates
        public boolean elapsed() {
            if (!enabled) {
                return false;
            }
            long curMillis = System.currentTimeMillis();
            if (lastEventTime + intervalMs <= curMillis) {
                lastEventTime = curMillis;

                return true;
            }
            return false;
        }
    }
    private Timer pollingTimer;
    // Link of machine commands to run
    ConcurrentLinkedQueue<MachineCommand> pendingQueue;
    // this is the xml config for this machine.
    private Node machineNode;
    private Machine controller;
    // our warmup/cooldown commands
    private Vector<String> warmupCommands;
    private Vector<String> cooldownCommands;
    // The name of our machine.
    private String name;
    // Things that belong to a job
    // estimated build time in millis
    private double estimatedBuildTime = 0;
    // Build statistics
    private double startTimeMillis = -1;
    // Our driver object. Null when no driver is selected.
    private Driver driver = null;
    // the simulator driver
    private SimulationDriver simulator;
    private MachineState state = new MachineState(MachineState.State.NOT_ATTACHED);
    MachineModel cachedModel = null;

    public MachineThread(Machine controller, Node machineNode) {
        super("Machine Thread");

        this.tablePoints = new HashMap<String, Point5d>();
        pollingTimer = new Timer();

        pendingQueue = new ConcurrentLinkedQueue<MachineCommand>();

        // save our XML
        this.machineNode = machineNode;
        this.controller = controller;

        // load our various objects
        loadDriver();
        loadTablePositions();
        loadExtraPrefs();
        parseName();

    }

    /**
     * Allows to get Lower XML value for jogRate
     *
     * @return XML jogRate value
     */
    public double getJogRateLowerValue() {

        if (XML.hasChildNode(machineNode, "jogRate")) {
            Node startnode = XML.getChildNodeByName(machineNode, "jogRate");
            jogRateLowerValue = Double.parseDouble(XML.getChildNodeValue(startnode, "jogRateLowerValue"));
        }
        return jogRateLowerValue;
    }

    /**
     * Allows to get Medium XML value for jogRate
     *
     * @return XML jogRate value
     */
    public double getJogRateMediumValue() {

        if (XML.hasChildNode(machineNode, "jogRate")) {
            Node startnode = XML.getChildNodeByName(machineNode, "jogRate");
            jogRateMediumValue = Double.parseDouble(XML.getChildNodeValue(startnode, "jogRateMediumValue"));
        }
        return jogRateMediumValue;
    }

    /**
     * Allows to get Higher XML value for jogRate
     *
     * @return XML jogRate value
     */
    public double getJogRateHigherValue() {

        if (XML.hasChildNode(machineNode, "jogRate")) {
            Node startnode = XML.getChildNodeByName(machineNode, "jogRate");
            jogRateHigherValue = Double.parseDouble(XML.getChildNodeValue(startnode, "jogRateHigherValue"));
        }
        return jogRateHigherValue;
    }

    /**
     * Allows to get XML Table Print Positions for trail movement
     *
     * @param position pretended
     * @return Coordinate Point5D for a specific position
     */
    public Point5d getTablePoints(String pointName) {
        return tablePoints.get(pointName);
    }

    /**
     * Get GCode print test to validate calibration
     *
     * @return GCode sample
     */
    public String getGCodePrintTest(String code) {
        String gcode = "g28";
        if (XML.hasChildNode(machineNode, "gcode")) {
            Node startnode = XML.getChildNodeByName(machineNode, "gcode");
            gcode = XML.getChildNodeByName(startnode, code).getAttributes().getNamedItem("value").getNodeValue();
        }

        return gcode;
    }

    public double getAcceleration(String acceTag) {

        double acc = 0.0;
        if (XML.hasChildNode(machineNode, "movements")) {
            Node startnode = XML.getChildNodeByName(machineNode, "movements");
            acc = Double.parseDouble(XML.getChildNodeValue(startnode, acceTag));
        }
        return acc;
    }

    public double getFeedrate(String speedTag) {

        double feedrate = 0.0;
        if (XML.hasChildNode(machineNode, "movements")) {
            Node startnode = XML.getChildNodeByName(machineNode, "movements");
            feedrate = Double.parseDouble(XML.getChildNodeValue(startnode, speedTag));
        }
        return feedrate;
    }

    private void loadTablePositions() {
        if (XML.hasChildNode(machineNode, "trailPositions")) {
            Node startnode = XML.getChildNodeByName(machineNode, "trailPositions");
            org.w3c.dom.Element element = (org.w3c.dom.Element) startnode;
            NodeList nodeList = element.getChildNodes();

            if (nodeList.getLength() > 0) {
                for (int i = 1; i < nodeList.getLength(); i += 2) {
                    String pointName = nodeList.item(i).getTextContent();
                    tablePoints.put(nodeList.item(i).getNodeName(), new Point5d(Double.parseDouble(pointName.split(",")[0]), Double.parseDouble(pointName.split(",")[1]), Double.parseDouble(pointName.split(",")[2])));
                }
            }
        }
    }

    private void loadExtraPrefs() {
        String[] commands = null;
        String command = null;

        warmupCommands = new Vector<String>();
        if (XML.hasChildNode(machineNode, "warmup")) {
            String warmup = XML.getChildNodeValue(machineNode, "warmup");
            commands = warmup.split("\n");

            for (int i = 0; i < commands.length; i++) {
                command = commands[i].trim();
                warmupCommands.add(command);
            }
        }

        cooldownCommands = new Vector<String>();
        if (XML.hasChildNode(machineNode, "cooldown")) {
            String cooldown = XML.getChildNodeValue(machineNode, "cooldown");
            commands = cooldown.split("\n");

            for (int i = 0; i < commands.length; i++) {
                command = commands[i].trim();
                cooldownCommands.add(command);
            }
        }
    }

    GCodeSource buildGCodeJob(GCodeSource source) {
        Vector<GCodeSource> sources = new Vector<GCodeSource>();
        sources.add(new StringListSource(warmupCommands));
        sources.add(source);
        sources.add(new StringListSource(cooldownCommands));
        return new GCodeSourceCollection(sources);
    }

    private String readyMessage() {
        String message = "is connected";
        Base.getMainWindow().setMessage(message);
        return message;
    }

    private String notConnectedMessage() {
        String message = "is disconnected";
        Base.getMainWindow().setMessage(message);
        return message;
    }

    private String cableRemovedMessage() {
        String message = "has the USB cable unplugged";
        Base.getMainWindow().setMessage(message);
        return message;
    }

    private String buildingMessage() {
        String message = "is building";
        Base.getMainWindow().setMessage(message);
        return message;
    }

    // Respond to a command from the machine controller
    void runCommand(MachineCommand command) throws VersionException, UsbClaimException, UsbNotActiveException, UsbDisconnectedException, UsbException {

        switch (command.type) {
            case CONNECT:

                if (state.getState() == MachineState.State.NOT_ATTACHED && driver.isInitialized()) {
                    setState(new MachineState(MachineState.State.READY), "Connected to " + getMachineName());
                    Base.getMainWindow().getButtons().connect();
                    Base.writeLog("New State: " + state.getState().toString());
//                    System.out.println("1");
                }

                if (state.getState() == MachineState.State.READY && !driver.isInitialized()) {
                    setState(new MachineState(MachineState.State.NOT_ATTACHED), notConnectedMessage());
                    Base.writeLog("New State: " + state.getState().toString());

//                    System.out.println("2");

                }

                if (state.getState() != MachineState.State.BUILDING) {
                    setState(new MachineState(MachineState.State.READY), "Connected to " + getMachineName());
                    //BVC - Machine was already ready, do nothing.
                    //Base.writeLog("New State: " + state.getState().toString());
//                    System.out.println("3");

                    // handle reconect after cable unplug
                    if (command.remoteName != null) {
                        if (command.remoteName.equals("cableDisconnect")) {
                            setState(new MachineState(MachineState.State.READY), "Connected to " + getMachineName());
                            Base.getMainWindow().getButtons().connect();
//                            System.out.println("HERE");
                        }
                    }

                }

                break;
            case DISCONNECT:
                // TODO: This seems wrong
                if (state.isConnected()) {
                    //driver.uninitialize();
                    setState(new MachineState(MachineState.State.ERROR), cableRemovedMessage());

                }
                break;
            case RESET:
                if (state.isConnected()) {
                    driver.reset();
                    readName();
                    setState(new MachineState(MachineState.State.READY),
                            readyMessage());
                }
                break;
            case BUILD_DIRECT:

                if (command.remoteName.equals("Print")) {
                    if (state.canPrint()) {

                        Base.writeLog("Print Started ...");
                        startTimeMillis = System.currentTimeMillis();

                        if (!isSimulating()) {
                            driver.getCurrentPosition(false); // reconcile position
                        }
                        setState(new MachineState(MachineState.State.BUILDING), buildingMessage());
                        Base.writeLog("New State: BUILDING");
                    }
                } else // Ready
                {
                    Base.writeLog("Print ended ...");
                    double printDuration = System.currentTimeMillis() - startTimeMillis;
                    Base.getMainWindow().setBuildTime(String.valueOf(printDuration));

                    Base.getMachineLoader().getDriver().resetExtrudeSession();
                    Base.writeConfig();
                    Base.loadProperties();

                }

                break;
            case SIMULATE:

                break;
            case BUILD_TO_REMOTE_FILE:
                if (state.canPrint()) {
                    if (!(driver instanceof SDCardCapture)) {
                        break;
                    }

                    startTimeMillis = System.currentTimeMillis();

                    pollingTimer.start(1000);

                    // Pad the job with start and end code
                    GCodeSource combinedSource = buildGCodeJob(command.source);

                    // TODO: This shouldn't be done here?
                    driver.invalidatePosition();
                    setState(new MachineState(MachineState.State.BUILDING));

                }
                break;
            case BUILD_TO_FILE:
                /**
                 * We will accept a disconnected machine or a ready machine.
                 */
                if (state.canPrint() || state.getState() == MachineState.State.NOT_ATTACHED) {
                    if (!(driver instanceof SDCardCapture)) {
                        break;
                    }

                    startTimeMillis = System.currentTimeMillis();

                    // There is no need to reconcile the position.
                    pollingTimer.start(1000);

                    // Pad the job with start and end code
                    GCodeSource combinedSource = buildGCodeJob(command.source);

                    if (state.canPrint()) {
                        setState(new MachineState(MachineState.State.BUILDING), buildingMessage());
                    } else {
                        setState(new MachineState(MachineState.State.BUILDING_OFFLINE), buildingMessage());
                    }
                }
                break;
            case BUILD_REMOTE:
                if (state.canPrint()) {
                    if (!(driver instanceof SDCardCapture)) {
                        break;
                    }

                    startTimeMillis = System.currentTimeMillis();

                    pollingTimer.start(1000);


                    // TODO: is this what we wanted?
                    driver.invalidatePosition();

                    setState(new MachineState(MachineState.State.BUILDING), buildingMessage());
                }
                break;
            case PAUSE:
                if (state.getState() == MachineState.State.BUILDING) {
                    setState(new MachineState(MachineState.State.PAUSED), "Build paused");
                }
                break;
            case UNPAUSE:
                if (state.getState() == MachineState.State.PAUSED) {
                    setState(new MachineState(MachineState.State.BUILDING), buildingMessage());
                }
                break;
            case STOP_MOTION:
                driver.stop(false);

                if (state.getState() == MachineState.State.BUILDING) {
                    setState(new MachineState(MachineState.State.READY),
                            readyMessage());
                    Base.writeLog("New State: READY");
                } else if (state.getState() == MachineState.State.BUILDING_OFFLINE) {
                    setState(new MachineState(MachineState.State.NOT_ATTACHED),
                            notConnectedMessage());
                    Base.writeLog("New State: NOT_ATTACHED");
                }
                break;
            case STOP_ALL:
                // TODO: This should be handled at the driver level?
                driver.getMachine().currentTool().setTargetTemperature(0);
                driver.getMachine().currentTool().setPlatformTargetTemperature(0);

                driver.stop(true);

                if (state.getState() == MachineState.State.BUILDING) {
                    setState(new MachineState(MachineState.State.READY),
                            readyMessage());
                    Base.writeLog("New State: READY");
                }
                if (state.getState() == MachineState.State.BUILDING_OFFLINE) {
                    setState(new MachineState(MachineState.State.NOT_ATTACHED),
                            notConnectedMessage());
                    Base.writeLog("New State: NOT_ATTACHED");
                }
                break;
            case DISCONNECT_REMOTE_BUILD:

            case RUN_COMMAND:
                if (state.isConnected()) {
                    boolean completed = false;
                    // TODO: Don't get stuck in a loop here!

                    while (!completed) {
                        try {
                            command.command.run(driver);
                            completed = true;
                        } catch (RetryException e) {
                        } catch (StopException e) {
                        }
                    }
                } else {
                }
                break;
            case SHUTDOWN:
                //TODO: Dispose of everything here.
                setState(new MachineState(MachineState.State.NOT_ATTACHED), notConnectedMessage());
                interrupt();
                break;
            default:
                Base.logger.log(Level.SEVERE, "Ignored command: " + command.type.toString());
        }
    }

    /**
     * Main machine thread loop.
     */
    @Override
    public void run() {
        // This is our main loop.

        statusThread = new AssessStatusThread(this);
        statusThread.start();
        while (true) {
            //check status thread
//            if(Base.status_thread_died==true){
//                        
//                        statusThread.stop();
//                        statusThread = new AssessStatusThread(this);
//                        statusThread.start();       
//                        Base.status_thread_died=false;
//                        continue;
//            }

            // First, check if the driver registered any errors
            if (driver.hasError()) {
                DriverError error = driver.getError();
                if (state.isConnected() && error.getDisconnected()) {
                    // If we were connected, but this error causes us to disconnect,
                    // transition to a disconnected state
                    setState(new MachineState(MachineState.State.NOT_ATTACHED),
                            error.getMessage());
                    Base.writeLog("New State: NOT_ATTACHED. USB driver has errors and got disconnected");
                } else {
                    // Otherwise, transition to an error state, where we can still
                    // configure the machine, but can't print.
                    setState(new MachineState(MachineState.State.NOT_ATTACHED),
                            error.getMessage());
                    Base.writeLog("New State: NOT_ATTACHED. An error has occured");
                }

            }

            if (state.getState() != MachineState.State.BUILDING
                    && state.getState() == MachineState.State.READY) {
                setState(new MachineState(MachineState.State.READY), readyMessage());
            }

            // Check for and run any control requests that might be in the queue.
            while (!pendingQueue.isEmpty()) {
                try {
                    runCommand(pendingQueue.remove());
                    stopwatch++;
                } catch (VersionException e) {
                    e.printStackTrace();
                } catch (UsbClaimException e) {
                    e.printStackTrace();
                } catch (UsbNotActiveException e) {
                    e.printStackTrace();
                } catch (UsbDisconnectedException e) {
                    e.printStackTrace();
                } catch (UsbException e) {
                }
            }

            // If we are building
            if (state.isBuilding() && !state.isPaused()) {

                if (Base.getPrintEnded()) {
                    if (state.getState() == MachineState.State.BUILDING) {

                        setState(new MachineState(MachineState.State.READY),
                                readyMessage());
                        Base.writeLog("New State: READY");
                    } else {
                        setState(new MachineState(MachineState.State.NOT_ATTACHED),
                                notConnectedMessage());
                        Base.writeLog("New State: NOT_ATTACHED");
                    }

                }
            }

            if (!state.isBuilding()) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }

            // If we get interrupted, break out of the main loop.
            if (Thread.interrupted()) {
                break;
            }

        }
        Base.writeLog("MachineThread interrupted, terminating.");
        Base.logger.fine("MachineThread interrupted, terminating.");
        dispose();
    }

    public int getStopwatch() {
        return stopwatch;
    }

    public void killSwitch() {
        pendingQueue.clear();
    }

    public void setStopwatch(int stopwatch) {
        this.stopwatch = stopwatch;
    }

    public boolean scheduleRequest(MachineCommand request) {
        pendingQueue.add(request);
        synchronized (this) {
            notify();
        }

        return true;
    }

    public boolean isReadyToPrint() {
        return state.canPrint();
    }

    /**
     * True if the machine's build is going to the simulator.
     */
    public boolean isSimulating() {
        // TODO: implement this.
        return false;
    }

    // TODO: Put this somewhere else
    public boolean isInteractiveTarget() {
        return false;
    }

    public JobTarget getTarget() {
        return JobTarget.NONE;
    }

    public int getLinesProcessed() {
        return -1;
    }

    public MachineState getMachineState() {
        return state.clone();
    }

    /**
     * Set the a machine state. If the state is not the current state, a state
     * change event will be emitted and the machine thread will be notified.
     *
     * @param state the new state of the machine.
     */
    private void setState(MachineState state) {
        setState(state, null);
    }

    private void setState(MachineState state, String message) {
        MachineState oldState = this.state;
        this.state = state;
        if (!oldState.equals(state)) {
            controller.emitStateChange(state, message);
        }
    }

    public Driver getDriver() {
        return driver;
    }

    public SimulationDriver getSimulator() {
        return simulator;
    }

    public boolean isConnected() {

        return (driver != null && driver.isInitialized() && !driver.isBootloader());
    }

    private void loadDriver() {
        // load our utility drivers
        if (Boolean.valueOf(ProperDefault.get("machinecontroller.simulator"))) {
//         Base.logger.info("Loading simulator.");
            simulator = new SimulationDriver();
            simulator.setMachine(loadModel());
        }
        Node driverXml = null;
        // load our actual driver
        NodeList kids = machineNode.getChildNodes();
        for (int j = 0; j < kids.getLength(); j++) {
            Node kid = kids.item(j);
            if (kid.getNodeName().equals("driver")) {
                driverXml = kid;

            }
        }
        driver = DriverFactory.factory(driverXml);
        driver.setMachine(getModel());
        // Initialization is now handled by the machine thread when it
        // is placed in a connecting state.
    }

    private void dispose() {
        if (driver != null) {
            driver.dispose();
        }
        if (simulator != null) {
            simulator.dispose();
        }

        if (statusThread != null) {
            try {
                statusThread.interrupt();
                statusThread.join(1000);
            } catch (InterruptedException e) {
            }
        }

        setState(new MachineState(MachineState.State.NOT_ATTACHED));
    }

    public void readName() {
        if (driver instanceof OnboardParameters) {
            String n = ((OnboardParameters) driver).getMachineName();
            if (n != null && n.length() > 0) {
                name = n;
            } else {
                parseName(); // Use name from XML file instead of reusing name from last connected machine
            }
        }
    }

    private void parseName() {
        NodeList kids = machineNode.getChildNodes();

        for (int j = 0; j < kids.getLength(); j++) {
            Node kid = kids.item(j);

            if (kid.getNodeName().equals("name")) {
                name = kid.getFirstChild().getNodeValue().trim();
                return;
            }
        }

        name = "Unknown";
    }

    private MachineModel loadModel() {
        MachineModel model = new MachineModel();
        model.loadXML(machineNode);
        return model;
    }

    public MachineModel getModel() {
        if (cachedModel == null) {
            cachedModel = loadModel();
        }
        return cachedModel;
    }

    // TODO: Make this a command.
    public void setEstimatedBuildTime(double estimatedBuildTime) {
        this.estimatedBuildTime = estimatedBuildTime;
    }

    public String getMachineName() {
        return name;
    }
}