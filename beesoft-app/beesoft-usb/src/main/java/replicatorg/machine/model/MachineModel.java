/*
 MachineModel.java

 A class to model a 3-axis machine.

 Part of the ReplicatorG project - http://www.replicat.org
 Copyright (c) 2008 Zach Smith

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
// TODO: Separate the configuration portion of this from the machine control portion!
package replicatorg.machine.model;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;
import org.w3c.dom.Node;
import pt.beeverycreative.beesoft.filaments.FilamentControler;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.util.AutonomousData;
import replicatorg.util.Point5d;

public class MachineModel {

    //our xml config info
    protected Node xml = null;

    //our machine space
    //private Point3d currentPosition;
    @SuppressWarnings("unused")
    private EnumMap<AxisId, Endstops> endstops = new EnumMap<AxisId, Endstops>(AxisId.class);

    // Which axes exist on this machine
    private Set<AxisId> axes = EnumSet.noneOf(AxisId.class);

    //feedrate information
    private Point5d maximumFeedrates;
    private Point5d homingFeedrates;
    private Point5d stepsPerMM;
    private Point5d timeOut;

    //our drive status
    protected boolean drivesEnabled = true;
    protected int gearRatio = 0;

    //our tool models
    protected Vector<ToolModel> tools;
    protected final AtomicReference<ToolModel> currentTool = new AtomicReference<ToolModel>();
    protected final ToolModel nullTool = new ToolModel();

    //our clamp models	
    protected Vector<ClampModel> clamps;

    //our wipe models @Noah
    protected Vector<WipeModel> wipes = new Vector<WipeModel>();

    private boolean machineReady = false;
    private boolean machineBusy = false;
    private boolean machinePaused = false;
    private boolean machinePowerSaving = false;
    private boolean machineShutdown = false;
    private boolean machinePrinting = true;
    private boolean machineOperational = false;
    private double zValue;

    // Filament code currently on the printer
    private String coilText = "";
    private int nozzleType = 0;
    private String resolution = "lowRes";

    private AutonomousData autonomousData;
    private String lastStatusString = "";

    /**
     * ***********************************
     * Creates the model object. ***********************************
     */
    public MachineModel() {
        clamps = new Vector<ClampModel>();
        tools = new Vector<ToolModel>();

        //currentPosition = new Point3d();
        maximumFeedrates = new Point5d();
        homingFeedrates = new Point5d();
        timeOut = new Point5d();
        stepsPerMM = new Point5d(1, 1, 1, 1, 1); //use ones, because we divide by this!
        zValue = 0.0;
        currentTool.set(nullTool);
    }

    /**
     * ***********************************
     * Reporting available axes ***********************************
     */
    /**
     * Return a set enumerating all the axes that this machine has available.
     */
    public Set<AxisId> getAvailableAxes() {
        return axes;
    }

    /**
     * Report whether this machine has the specified axis.
     *
     * @param id The axis to check
     * @return true if the axis is available, false otherwise
     */
    public boolean hasAxis(AxisId id) {
        return axes.contains(id);
    }

    /**
     * ***********************************
     * Convert steps to millimeter units ***********************************
     */
    public Point5d stepsToMM(Point5d steps) {
        Point5d temp = new Point5d();
        temp.div(steps, stepsPerMM);

        return temp;
    }

    /**
     * Get steps-mm conversion value
     */
    public Point5d getStepsPerMM() {
        return stepsPerMM;
    }

    /**
     * ***********************************
     * Convert millimeters to machine steps ***********************************
     */
    public Point5d mmToSteps(Point5d mm) {
        Point5d temp = new Point5d();
        temp.mul(mm, stepsPerMM);
        temp.round(); // integer step counts please

        return temp;
    }

    /**
     * ***********************************
     * Convert millimeters to machine steps, factoring in previous rounding
     * error and providing carryover error ***********************************
     */
    public Point5d mmToSteps(Point5d mm, Point5d excess) {
        Point5d temp = new Point5d();
        temp.mul(mm, stepsPerMM);
        temp.add(excess);
        temp.round(excess); // integer step counts please
        return temp;
    }

    /**
     * ***********************************
     * Drive interface functions ***********************************
     */
    public void enableDrives() {
        drivesEnabled = true;
    }

    public void disableDrives() {
        drivesEnabled = false;
    }

    public boolean areDrivesEnabled() {
        return drivesEnabled;
    }

    /**
     * ***********************************
     * Gear Ratio functions ***********************************
     */
    public void changeGearRatio(int ratioIndex) {
        gearRatio = ratioIndex;
    }

    public double getzValue() {
        return zValue;
    }

    public void setzValue(double zValue) {
        this.zValue = zValue;
    }

    /**
     * ***********************************
     * Clamp interface functions ***********************************
     */
    public ClampModel getClamp(int index) {
        try {
            ClampModel c = (ClampModel) clamps.get(index);
            return c;
        } catch (ArrayIndexOutOfBoundsException e) {
            Base.LOGGER.severe("Cannot get non-existant clamp (#" + index + ".");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * ***********************************
     * Tool interface functions ***********************************
     */
    public void selectTool(int index) {
        synchronized (currentTool) {
            try {
                currentTool.set((ToolModel) tools.get(index));
                if (currentTool.get() == null) {
                    Base.LOGGER.severe("Cannot select non-existant tool (#" + index + ").");
                    currentTool.set(nullTool);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                if (xml != null) {
                    Base.LOGGER.severe("Cannot select non-existant tool (#" + index + ").");
                } else {
                    // If this machine is not configured, it's presumed it's a null machine
                    // and it's expected that toolheads are not specified.
                }
                currentTool.set(nullTool);
            }
        }
    }

    public ToolModel currentTool() {
        return currentTool.get();
    }

    public ToolModel getTool(int index) {
        try {
            return tools.get(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            Base.LOGGER.severe("Cannot get nonexistent tool (#" + index + ".");
            //e.printStackTrace();
        }
        return null;
    }

    public Vector<ToolModel> getTools() {
        return tools;
    }

    public Vector<WipeModel> getWipes() {
        return wipes;
    }

    public WipeModel getWipeByIndex(int index) {
        for (WipeModel wm : wipes) {
            if (wm.getIndex() == index) {
                return wm;
            }
        }
        return null;
    }

    public void addTool(ToolModel t) {
        tools.add(t);
    }

    public void setTool(int index, ToolModel t) {
        try {
            tools.set(index, t);
        } catch (ArrayIndexOutOfBoundsException e) {
            Base.LOGGER.severe("Cannot set non-existant tool (#" + index + ".");
            e.printStackTrace();
        }
    }

    public Point5d getMaximumFeedrates() {
        return maximumFeedrates;
    }

    public Point5d getHomingFeedrates() {
        return homingFeedrates;
    }

    public Point5d getTimeOut() {
        return timeOut;
    }

    /**
     * returns the endstop configuration for the given axis
     */
    public Endstops getEndstops(AxisId axis) {
        return this.endstops.get(axis);
    }

    public void setLastStatusString(String status) {
        this.lastStatusString = status;
    }

    public String getLastStatusString() {
        return lastStatusString;
    }

    public void setMachineReady(boolean machReady) {
        this.machineReady = machReady;
    }

    public boolean getMachineReady() {
        return machineReady;
    }

    public void setMachineBusy(boolean b) {
        machineBusy = b;
    }

    public boolean getMachineBusy() {
        return machineBusy;
    }

    public void setMachinePaused(boolean machinePaused) {
        this.machinePaused = machinePaused;
    }

    public boolean getMachinePaused() {
        return machinePaused;
    }

    public void setMachineShutdown(boolean machineShutdown) {
        this.machineShutdown = machineShutdown;
    }

    public boolean getMachineShutdown() {
        return machineShutdown;
    }

    public void setMachinePowerSaving(boolean machinePowerSaving) {
        this.machinePowerSaving = machinePowerSaving;
    }

    public boolean getMachinePowerSaving() {
        return machinePowerSaving;
    }

    public void setMachinePrinting(boolean machinePrinting) {
        this.machinePrinting = machinePrinting;
    }

    public boolean getMachinePrinting() {
        return machinePrinting;
    }

    public void setMachineOperational(boolean machineOperational) {
        this.machineOperational = machineOperational;
    }

    public boolean getMachineOperational() {
        return machineOperational;
    }

    /* Get and Setter CoilCode/BEECODE */
    public String getCoilText() {
        return coilText;
    }

    public void setCoilText(String coilText) {
        this.coilText = coilText;
    }

    public void setNozzleType(int nozzleType) {
        this.nozzleType = nozzleType;
    }

    public int getNozzleType() {
        return nozzleType;
    }

    /* Get and Setter Resolution/BEECODE */
    public String getResolution() {
        return this.resolution;
    }

    public void setResolution(String res) {
        this.resolution = res;
    }

    public void setAutonomousData(AutonomousData data) {
        this.autonomousData = data;
    }

    public AutonomousData getAutonomousData() {
        return autonomousData;
    }

}
