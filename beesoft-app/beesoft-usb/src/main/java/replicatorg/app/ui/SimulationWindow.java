/*
 Part of the ReplicatorG project - http://www.replicat.org
 Copyright (c) 2008 Zach Smith

 Forked from Arduino: http://www.arduino.cc

 Based on Processing http://www.processing.org
 Copyright (c) 2004-05 Ben Fry and Casey Reas
 Copyright (c) 2001-04 Massachusetts Institute of Technology

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
 
 $Id: MainWindow.java 370 2008-01-19 16:37:19Z mellis $
 */

package replicatorg.app.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;

import replicatorg.util.Point5d;

public abstract class SimulationWindow extends JFrame {
	protected Rectangle2D.Double simulationBounds;
	
	public SimulationWindow() {
		super("Build Simulation");

		// make it most of our screen.
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int myWidth = screen.width - 40;
		if (myWidth > 1024)
			myWidth = 1024;
		int myHeight = screen.height - 40;
		if (myHeight > 768)
			myHeight = 768;

		this.setBounds(20, 20, myWidth, myHeight);

		// default behavior
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);

		// no resizing... yet
		// this.setResizable(false);

		// no menu bar.
		this.setMenuBar(null);

		invalidate();
	}

	public void setSimulationBounds(Rectangle2D.Double bounds) {
		this.simulationBounds = bounds;
	}
	
	public abstract void queuePoint(Point5d p);
}
