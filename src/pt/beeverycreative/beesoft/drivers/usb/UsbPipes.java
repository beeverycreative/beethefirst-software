package pt.beeverycreative.beesoft.drivers.usb;

import javax.usb.UsbControlIrp;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbIrp;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbNotClaimedException;
import javax.usb.UsbNotOpenException;
import javax.usb.UsbPipe;

/**
* Copyright (c) 2013 BEEVC - Electronic Systems
* This file is part of BEESOFT software: you can redistribute it and/or modify 
* it under the terms of the GNU General Public License as published by the 
* Free Software Foundation, either version 3 of the License, or (at your option)
* any later version. BEESOFT is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
* or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
* for more details. You should have received a copy of the GNU General
* Public License along with BEESOFT. If not, see <http://www.gnu.org/licenses/>.
*/
public class UsbPipes {
	UsbPipe[] pipes = null;

	public UsbPipes(){
		this.pipes = new UsbPipe[2];
	}

	public UsbPipe getUsbPipeWrite(){
		return this.pipes[0];
	}

	public UsbPipe getUsbPipeRead(){
		return this.pipes[1];
	}
	
	public void setUsbPipeWrite(UsbPipe wPipe){
		this.pipes[0] = wPipe;
	}

	public void setUsbPipeRead(UsbPipe rPipe){
		this.pipes[1] = rPipe;
	}
/*
	@Override
	public void abortAllSubmissions() throws UsbNotActiveException,
			UsbNotOpenException, UsbDisconnectedException {
		abortAllSubmissions();
		
	}

	@Override
	public void addUsbPipeListener(UsbPipeListener arg0) {
		addUsbPipeListener(arg0);
	}

	@Override
	public UsbIrp asyncSubmit(byte[] arg0) throws UsbException,
			UsbNotActiveException, UsbNotOpenException,
			IllegalArgumentException, UsbDisconnectedException {
		return asyncSubmit(arg0);
	}

	@Override
	public void asyncSubmit(UsbIrp arg0) throws UsbException,
			UsbNotActiveException, UsbNotOpenException,
			IllegalArgumentException, UsbDisconnectedException {
		asyncSubmit(arg0);
	}

	@Override
	public void asyncSubmit(List arg0) throws UsbException,
			UsbNotActiveException, UsbNotOpenException,
			IllegalArgumentException, UsbDisconnectedException {
		asyncSubmit(arg0);
	}
*/
	public void close() throws UsbException, UsbNotActiveException,
			UsbNotOpenException, UsbDisconnectedException {
		this.getUsbPipeRead().close();
		this.getUsbPipeWrite().close();
	}

	public UsbControlIrp createUsbControlIrp(byte arg0, byte arg1, short arg2,
			short arg3) {
		
		return createUsbControlIrp(arg0, arg1, arg2, arg3);
	}

	public UsbIrp createUsbIrp() {

		return getUsbPipeWrite().createUsbIrp();
	}

	public UsbEndpoint getUsbEndpoint() {
		
		return getUsbPipeWrite().getUsbEndpoint();
	}

	public boolean isActive() {
		if(this.getUsbPipeRead().isActive() && this.getUsbPipeWrite().isActive())
			return true;
		else
			return false; 
	}

	public boolean isOpen() {
		if(this.getUsbPipeRead().isOpen() && this.getUsbPipeWrite().isOpen())
			return true;
		else
			return false; 
	}

	public void open() throws UsbException, UsbNotActiveException,
			UsbNotClaimedException, UsbDisconnectedException {
		this.getUsbPipeRead().open();
		this.getUsbPipeWrite().open();
		
	}
        
	
/*
	@Override
	public void removeUsbPipeListener(UsbPipeListener arg0) {
		removeUsbPipeListener(arg0);
	}

	@Override
	public int syncSubmit(byte[] arg0) throws UsbException,
			UsbNotActiveException, UsbNotOpenException,
			IllegalArgumentException, UsbDisconnectedException {
		return syncSubmit(arg0);
	}

	@Override
	public void syncSubmit(UsbIrp arg0) throws UsbException,
			UsbNotActiveException, UsbNotOpenException,
			IllegalArgumentException, UsbDisconnectedException {
		syncSubmit(arg0);
		
	}

	@Override
	public void syncSubmit(List arg0) throws UsbException,
			UsbNotActiveException, UsbNotOpenException,
			IllegalArgumentException, UsbDisconnectedException {
		syncSubmit(arg0);
		
	}
	*/
}
