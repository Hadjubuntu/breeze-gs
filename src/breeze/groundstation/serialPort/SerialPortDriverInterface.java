/*
 * SerialPortDriverInterface.java
 * Copyright (C) 2012 A. Savio
 * 
 * A copy of the LGPL v 2.1 may be found at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html on November 21st 2007
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package breeze.groundstation.serialPort;

import java.util.ArrayList;

import breeze.groundstation.main.GSController;


public interface SerialPortDriverInterface {

	
    public String getPortName();
    public void cyclePort();
    public void writeToSerial(byte key);
    public void registerObserver(SerialPortObserver observer);
    public void removeObserver(SerialPortObserver observer);
	public void writeToSerial(byte[] keys);
	public void setGroundStationController(GSController controller);
	
	public ArrayList<String> getInputFromUAV();
	public String getLastInputString();
	public boolean isInputReady();
	public boolean isInputTaken();
	public void setInputTaken(boolean v);
	public void setInputReady(boolean v);
	
	public int[] getPacketLengthStats();
	public long[] getPacketTimeUs();
}
