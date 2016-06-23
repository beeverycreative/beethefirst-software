package replicatorg.machine;

/**
 * Copyright (c) 2013 BEEVC - Electronic Systems
 */
import java.util.HashMap;
import org.w3c.dom.Node;
import pt.beeverycreative.beesoft.drivers.usb.UsbPassthroughDriver;
import replicatorg.app.Base;
import replicatorg.app.tools.XML;
import replicatorg.drivers.Driver;
import replicatorg.machine.Machine.JobTarget;
import replicatorg.machine.model.MachineModel;
import replicatorg.util.Point5d;

/**
 * The MachineThread is responsible for communicating with the machine.
 */
class MachineThread extends Thread {

    private final AssessStatusThread statusThread = new AssessStatusThread();
    private double jogRateLowerValue;
    private double jogRateMediumValue;
    private double jogRateHigherValue;
    private String lastFeedrate;
    private String lastEString;
    private String lastAcceleration;
    private final HashMap<String, Point5d> tablePoints;
    private Point5d actualPoint = new Point5d();
    private boolean isFilamentChanged;

    private class AssessStatusThread extends Thread {

        public AssessStatusThread() {
            super("Assess Status");
        }

        @Override
        public void run() {
            while (true) {
                synchronized (Base.WELCOME_SPLASH_MONITOR) {
                    if (Base.isWelcomeSplashVisible()) {
                        try {
                            Base.WELCOME_SPLASH_MONITOR.wait();
                        } catch (InterruptedException ex) {
                            return;
                        }
                    }
                }

                DRIVER.initialize();

                if (getModel().getMachinePowerSaving()) {
                    Base.getMainWindow().getButtons().setMessage("power saving");
                } else if (getModel().getMachineOperational()) {
                    Base.getMainWindow().getButtons().setMessage("is connected");
                }

                Base.hiccup(200);
            }
        }

        private MachineModel getModel() {
            return DRIVER.getMachine();
        }
    }

    // Link of machine commands to run
    //ConcurrentLinkedQueue<MachineCommand> pendingQueue;
    //ConcurrentLinkedQueue<MachineCommand> auxiliarQueue;
    // this is the xml config for this machine.
    private final Node machineNode;
    private final Machine controller;
    // our warmup/cooldown commands
    //private Vector<String> warmupCommands;
    //private Vector<String> cooldownCommands;
    private static final Driver DRIVER = new UsbPassthroughDriver();
    // the simulator driver
    private MachineState state = new MachineState(MachineState.State.NOT_ATTACHED);
    MachineModel cachedModel = null;

    public MachineThread(Machine controller, Node machineNode) {
        super("Machine Thread");

        this.tablePoints = new HashMap<String, Point5d>();
        lastFeedrate = "0";

        // save our XML
        this.machineNode = machineNode;
        this.controller = controller;
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

    /**
     * Main machine thread loop.
     */
    @Override
    public void run() {
        statusThread.start();
    }

    public String getLastFeedrate() {
        return lastFeedrate;
    }

    public String getLastE() {
        return lastEString;
    }

    public String getLastAcceleration() {
        return lastAcceleration;
    }

    public Point5d getLastPrintedPoint() {
        return actualPoint;
    }

    public void setLastPrintedPoint(Point5d point) {
        this.actualPoint = point;
    }

    public void setFilamentChanged(boolean changed) {
        this.isFilamentChanged = changed;
    }

    public boolean hasFilamentChanged() {
        return this.isFilamentChanged;
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

    public Driver getDriver() {
        return DRIVER;
    }

    public boolean isConnected() {
        return (DRIVER != null && DRIVER.isInitialized());
    }
    
    public void dispose() {
        if(DRIVER != null) {
            DRIVER.dispose();
        }
    }
}
