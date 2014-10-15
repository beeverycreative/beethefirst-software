package replicatorg.machine;

import replicatorg.drivers.commands.DriverCommand;
import replicatorg.machine.Machine.RequestType;

public class MachineCommand {

	final RequestType type;
	final String remoteName;
	final DriverCommand command;

	public MachineCommand(RequestType type, String remoteName) {
		this.type = type;
		this.remoteName = remoteName;
		
		this.command = null;
	}
	public MachineCommand(RequestType type, DriverCommand command) {
		this.type = type;
		this.command = command;
		this.remoteName = null;
	}
}