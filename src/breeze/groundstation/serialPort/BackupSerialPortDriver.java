//package breeze.groundstation.serialPort;
//
//
///*
//import gnu.io.CommPortIdentifier;
//import gnu.io.SerialPort;
//import gnu.io.SerialPortEvent;
//import gnu.io.SerialPortEventListener;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Enumeration;
//import java.util.List;
//import java.util.TooManyListenersException;
//
//import breeze.groundstation.main.GSController;
//import breeze.groundstation.main.Utils;
//
//
//
//public class SerialPortDriver implements SerialPortDriverInterface, SerialPortEventListener {
//
//	public static int BAUD_RATE = 57600;
//
//	private GSController gsController;
//	private static SerialPortDriver _serialPortDriver;
//	private SerialPort _port = null;
//	private OutputStream _serialOutput = null;
//	private InputStream _serialInput = null;
//	private String _portName = null;
//
//
//	private ArrayList<String> inputFromUAV;
//	private String inputString;
//	private String lastInputString;
//	private boolean inputTaken;
//	private boolean inputReady;
//
//	private byte[] buffer= new byte[1024];
//	
//	private int nPacketStats;
//	private int[] packetLengthStats = new int[2];
//	private long[] packetTimeUs = new long[2];
//
//	ArrayList<SerialPortObserver> _observers = new ArrayList<SerialPortObserver>();
//
//
//	private SerialPortDriver() {
//		cycle();
//		notifyObservers();
//		inputString = "";
//		inputTaken = false;
//		inputReady = false;
//		lastInputString = "";
//
//		inputFromUAV = new ArrayList<String>();
//		
//		nPacketStats = 0;
//		packetLengthStats[0] = 0;
//		packetLengthStats[1] = 0;
//		packetTimeUs[0] = 0;
//		packetTimeUs[1] = 0;
//		
//		
//	}
//
//	private void closeCurrentPort() {
//		if (_port != null) {
//			try {
//				_port.close();
//			}
//			catch (Exception e) {
//				System.out.println("ERROR: Unable to close the serial port.");
//				System.out.println(e.getMessage());
//				e.printStackTrace();
//			}
//		}
//		_port = null;
//		_serialOutput = null;
//		_portName = null;
//	}
//
//	private Enumeration<CommPortIdentifier> getSerialPorts() {
//		List<CommPortIdentifier> list = new ArrayList<CommPortIdentifier>();
//		Enumeration ports = CommPortIdentifier.getPortIdentifiers();
//		while (ports.hasMoreElements()) {
//			CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
//
//			if (port.getPortType() == CommPortIdentifier.PORT_SERIAL && !port.isCurrentlyOwned()) {
//				if (port.getName().equals("COM12") || port.getName().equals("/dev/ttyUSB0")) {
//					System.out.println("COM12 or ttyUSB0 is available : GroundStation RF is connected!");
//					list.add(port);
//				}
//			}
//		}
//		return Collections.enumeration(list);
//	}
//
//	private void cycle() {
//		try {
//			/* Close the old port.
//			 */
//			String oldPortName = _portName;
//			closeCurrentPort();
//
//			/* Loop through the list of serial ports until getting to the one currently
//			 * in use.
//			 */
//			Enumeration<CommPortIdentifier> ports = getSerialPorts();
//			while (ports.hasMoreElements()) {
//				CommPortIdentifier port = ports.nextElement();
//				if (port.getName().equals(oldPortName)) {
//					break;
//				}
//			}
//
//			/* If the previous loop got to the end of the list of available ports, set it back
//			 * at the beginning.
//			 */
//			if (!ports.hasMoreElements()) {
//				ports = getSerialPorts();
//			}
//
//			/* Take the next serial port in line and attempt to open it.
//			 */
//			while (ports.hasMoreElements()) {
//				CommPortIdentifier port = ports.nextElement();
//				try {
//					_port = (SerialPort) port.open("Serial port", 5000);
//				}
//				catch (Exception e)
//				{
//					System.out.println("ERROR: Unable to open port " + port.getName() + ". Trying the next one.");
//					System.out.println(e.getMessage());
//					e.printStackTrace();
//					closeCurrentPort();
//					continue;
//				}
//				_port.setSerialPortParams(BAUD_RATE, 8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
//				_portName = port.getName();
//				_serialOutput = _port.getOutputStream();
//				_serialInput = _port.getInputStream();
//
//				// we could read from "in" in a separate thread, but the API gives us events
//				try {
//					_port.addEventListener(this);
//					_port.notifyOnDataAvailable(true);
//				} catch (TooManyListenersException e) {System.out.println("couldn't add listener");}
//				break;
//			}
//
//			if (_port == null) {
//				System.out.println("ERROR: Unable to open any serial port.");
//			}
//		}
//		catch (Exception e) {
//			System.out.println("ERROR: Unable to recycle port.");
//			System.out.println(e.getMessage());
//			e.printStackTrace();
//			closeCurrentPort();
//		}
//	}
//
//	public static SerialPortDriver getInstance() {
//		if (_serialPortDriver == null) {
//			_serialPortDriver = new SerialPortDriver();
//		}
//		return _serialPortDriver;
//	}
//
//	@Override
//	public String getPortName() {
//		if (_portName != null) {
//			return _portName;
//		}
//		else {
//			return "NONE";
//		}
//	}
//
//	@Override
//	public void cyclePort() {
//		cycle();
//		notifyObservers();
//	}
//
//	@Override
//	public void writeToSerial(byte key) {
//		try {
//			if (_serialOutput != null) {
//				_serialOutput.write(key);
//				_serialOutput.flush();
//			}
//		}
//		catch (Exception e) {
//			System.out.println("ERROR: Unable to write to port.");
//			System.out.println(e.getMessage());
//			e.printStackTrace();
//			cycle();
//		}
//		notifyObservers();
//	}
//
//	@Override
//	public void writeToSerial(byte[] keys) {		
//		packetLengthStats[nPacketStats] = keys.length;
//		packetTimeUs[nPacketStats] = Utils.micros();
//		nPacketStats = (nPacketStats == 0) ? 1 : 0;
//		
//		try {
//			if (_serialOutput != null) {
//				_serialOutput.write(keys);
//				//_serialOutput.flush(); // If we do so, its blocking the incoming data
//			}
//		}
//		catch (Exception e) {
//			System.out.println("ERROR: Unable to write to port.");
//			System.out.println(e.getMessage());
//			e.printStackTrace();
//			cycle();
//		}
//		notifyObservers();
//	}
//
//	@Override
//	public void registerObserver(SerialPortObserver observer) {
//		_observers.add(observer);
//	}
//
//	@Override
//	public void removeObserver(SerialPortObserver observer) {
//		int index = _observers.indexOf(observer);
//		if (index >= 0) {
//			_observers.remove(index);
//		}
//	}
//
//	public void notifyObservers() {
//		for (SerialPortObserver observer : _observers) {
//			observer.portStatusChanged();
//		}
//	}
//
//	public ArrayList<String> getInputFromUAV() {
//		return inputFromUAV;
//	}
//
//
//	/**
//	 * Ground station received incoming data from UAV
//	 */
//	@Override
//	public void serialEvent(SerialPortEvent event) {
//
//		if (event.getEventType()== SerialPortEvent.DATA_AVAILABLE) {
//			try {
//				char inChar = (char)_serialInput.read();
//				// add it to the inputString:
//				if ((int)inChar != 65535) {
//					inputString += inChar;
//				}
//				// if the incoming character is a newline, set a flag
//				// so the main loop can do something about it:
//				if (inChar == '\n') {
//					//System.out.println("ECHO= " + inputString);
//
//					inputFromUAV.add(inputString);
//
//					// Manage each command from UAV
//					lastInputString = inputString;
//					inputReady = true;
//					inputTaken = false;
//
//					inputString = "";					 
//				}
//				
//
//			} catch (IOException e) {}
//		} 
//	}
//
//
//
//	public String getLastInputString() {
//		return lastInputString;
//	}
//
//
//	public boolean isInputTaken() {
//		return inputTaken;
//	}
//
//	public void setInputTaken(boolean inputTaken) {
//		this.inputTaken = inputTaken;
//	}
//
//	public boolean isInputReady() {
//		return inputReady;
//	}
//
//	public void setInputReady(boolean inputReady) {
//		this.inputReady = inputReady;
//	}
//
//	@Override
//	public void setGroundStationController(GSController controller) {
//		gsController = controller;		
//	}
//
//	public int[] getPacketLengthStats() {
//		return packetLengthStats;
//	}
//
//	public long[] getPacketTimeUs() {
//		return packetTimeUs;
//	}
//	
//	
//}
//*/