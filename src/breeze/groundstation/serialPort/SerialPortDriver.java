/*
 * SerialPortDriver.java
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


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import breeze.groundstation.main.GSController;
import breeze.groundstation.main.Utils;


/*
 * Remove from VM arguments : -Djava.library.path=/lib/
 */

public class SerialPortDriver implements SerialPortDriverInterface, SerialPortEventListener {

	public static int BAUD_RATE = 57600;

	private GSController gsController;
	private static SerialPortDriver _serialPortDriver;
	private SerialPort _port = null;
	private String _portName = null;


	private ArrayList<String> inputFromUAV;
	private String inputString;
	private String lastInputString;
	private boolean inputTaken;
	private boolean inputReady;

	private byte[] buffer= new byte[1024];

	private int nPacketStats;
	private int[] packetLengthStats = new int[2];
	private long[] packetTimeUs = new long[2];

	ArrayList<SerialPortObserver> _observers = new ArrayList<SerialPortObserver>();


	private SerialPortDriver() {
		cycle();
		notifyObservers();
		inputString = "";
		inputTaken = false;
		inputReady = false;
		lastInputString = "";

		inputFromUAV = new ArrayList<String>();

		nPacketStats = 0;
		packetLengthStats[0] = 0;
		packetLengthStats[1] = 0;
		packetTimeUs[0] = 0;
		packetTimeUs[1] = 0;
	}

	private void closeCurrentPort() {
		if (_port != null) {
			try {
				_port.closePort();
			}
			catch (Exception e) {
				System.out.println("ERROR: Unable to close the serial port.");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
		_port = null;
		_portName = null;
	}

	private Enumeration<String> getSerialPorts() {
		List<String> list = new ArrayList<String>();


		String[] portNames = SerialPortList.getPortNames();
		for(int i = 0; i < portNames.length; i++){
			if (portNames[i].equals("COM12") || portNames[i].equals("/dev/ttyUSB0") || portNames[i].equals("/dev/ttyACM0")) {
				System.out.println("COM12 or ttyUSB0 is available : GroundStation RF is connected!");
				list.add(portNames[i]);
			}
		}
		return Collections.enumeration(list);
	}

	private void cycle() {
		try {
			/* Close the old port.
			 */
			String oldPortName = _portName;
			closeCurrentPort();

			/* Loop through the list of serial ports until getting to the one currently
			 * in use.
			 */
			Enumeration<String> ports = getSerialPorts();
			while (ports.hasMoreElements()) {
				String port = ports.nextElement();
				if (port.equals(oldPortName)) {
					break;
				}
			}

			/* If the previous loop got to the end of the list of available ports, set it back
			 * at the beginning.
			 */
			if (!ports.hasMoreElements()) {
				ports = getSerialPorts();
			}

			/* Take the next serial port in line and attempt to open it.
			 */
			while (ports.hasMoreElements()) {
				String port = ports.nextElement();
				try {
					_port = new SerialPort(port);
					_port.openPort();
				}
				catch (Exception e)
				{
					System.out.println("ERROR: Unable to open port " + port + ". Trying the next one.");
					System.out.println(e.getMessage());
					e.printStackTrace();
					closeCurrentPort();
					continue;
				}
				_port.setParams(BAUD_RATE, 
						SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);
				_portName = port;

				// we could read from "in" in a separate thread, but the API gives us events

				int mask = SerialPort.MASK_RXCHAR ;//Prepare mask
				_port.setEventsMask(mask);//Set mask
				_port.addEventListener(this);

				break;
			}

			if (_port == null) {
				System.out.println("ERROR: Unable to open any serial port.");
			}
		}
		catch (Exception e) {
			System.out.println("ERROR: Unable to recycle port.");
			System.out.println(e.getMessage());
			e.printStackTrace();
			closeCurrentPort();
		}
	}

	public static SerialPortDriver getInstance() {
		if (_serialPortDriver == null) {
			_serialPortDriver = new SerialPortDriver();
		}
		return _serialPortDriver;
	}

	@Override
	public String getPortName() {
		if (_portName != null) {
			return _portName;
		}
		else {
			return "NONE";
		}
	}

	@Override
	public void cyclePort() {
		cycle();
		notifyObservers();
	}

	@Override
	public void writeToSerial(byte key) {
		try {
			if (_port != null) {
				_port.writeByte(key);
			}
		}
		catch (Exception e) {
			System.out.println("ERROR: Unable to write to port.");
			System.out.println(e.getMessage());
			e.printStackTrace();
			cycle();
		}
		notifyObservers();
	}

	@Override
	public void writeToSerial(byte[] keys) {		
		packetLengthStats[nPacketStats] = keys.length;
		packetTimeUs[nPacketStats] = Utils.micros();
		nPacketStats = (nPacketStats == 0) ? 1 : 0;

		try {
			if (_port != null) {
				_port.writeBytes(keys);
				
				// Verbose
				// String strSend = new String(keys, "UTF-8");
				// System.out.println("send= " + strSend);
				//_serialOutput.flush(); // If we do so, its blocking the incoming data
			}
		}
		catch (Exception e) {
			System.out.println("ERROR: Unable to write to port.");
			System.out.println(e.getMessage());
			e.printStackTrace();
			cycle();
		}
		notifyObservers();
	}

	@Override
	public void registerObserver(SerialPortObserver observer) {
		_observers.add(observer);
	}

	@Override
	public void removeObserver(SerialPortObserver observer) {
		int index = _observers.indexOf(observer);
		if (index >= 0) {
			_observers.remove(index);
		}
	}

	public void notifyObservers() {
		for (SerialPortObserver observer : _observers) {
			observer.portStatusChanged();
		}
	}

	public ArrayList<String> getInputFromUAV() {
		return inputFromUAV;
	}


	/**
	 * Ground station received incoming data from UAV
	 */
	@Override
	public void serialEvent(SerialPortEvent event) {
		
		if(event.isRXCHAR()){//If data is available
			try {
				byte buffer[] = _port.readBytes();
				String str = new String(buffer, "UTF-8");
				
				char charac[] = str.toCharArray();
				
				for (char inChar : charac) {
					if ((int)inChar != 65535) {
						inputString += inChar;
					}
					
					// if the incoming character is a newline, set a flag
					// so the main loop can do something about it:
					if (inChar == '\n') {
						inputFromUAV.add(inputString);

						// Manage each command from UAV
						lastInputString = inputString;
						inputReady = true;
						inputTaken = false;

						inputString = "";					 
					}
				}
			}
			catch (SerialPortException ex) {
				System.out.println(ex);
			} catch (UnsupportedEncodingException e) {
				System.out.println(e);
			}
		}
	}



	public String getLastInputString() {
		return lastInputString;
	}


	public boolean isInputTaken() {
		return inputTaken;
	}

	public void setInputTaken(boolean inputTaken) {
		this.inputTaken = inputTaken;
	}

	public boolean isInputReady() {
		return inputReady;
	}

	public void setInputReady(boolean inputReady) {
		this.inputReady = inputReady;
	}

	@Override
	public void setGroundStationController(GSController controller) {
		gsController = controller;		
	}

	public int[] getPacketLengthStats() {
		return packetLengthStats;
	}

	public long[] getPacketTimeUs() {
		return packetTimeUs;
	}


}
