/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package replicatorg.drivers.commands;

import replicatorg.drivers.Driver;

public class ReadStatus implements DriverCommand {

	@Override
	public void run(Driver driver) {
		driver.readStatus();
	}
}
