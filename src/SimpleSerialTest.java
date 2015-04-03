import java.io.IOException;
import java.io.UnsupportedEncodingException;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;


public class SimpleSerialTest {

	private static SerialPort serialPort;
	public static int BAUD_RATE = 57600;


	public static void main(String[] args) {
		SimpleSerialTest test = new SimpleSerialTest();
	}

	public SimpleSerialTest() {
		String[] portNames = SerialPortList.getPortNames();
		for(int i = 0; i < portNames.length; i++){
			System.out.println(portNames[i]);
		}


		serialPort = new SerialPort("/dev/ttyUSB0");
		try {
			serialPort.openPort();//Open serial port
			serialPort.setParams(BAUD_RATE, 
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);


			int mask = SerialPort.MASK_RXCHAR ;//Prepare mask
            serialPort.setEventsMask(mask);//Set mask
			serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener

			String str = "att_cmd|3000|2000|500|\n";   
			serialPort.writeBytes(str.getBytes());//Write data to port

			for (int i = 0; i < 10; i ++) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			serialPort.closePort();//Close serial port
		}
		catch (SerialPortException ex) {
			System.out.println(ex);
		}
	}

	/*
	 * In this class must implement the method serialEvent, through it we learn about 
	 * events that happened to our port. But we will not report on all events but only 
	 * those that we put in the mask. In this case the arrival of the data and change the 
	 * status lines CTS and DSR
	 */
	static class SerialPortReader implements SerialPortEventListener {

		public void serialEvent(SerialPortEvent event) {
			if(event.isRXCHAR()){//If data is available
				try {
					byte buffer[] = serialPort.readBytes();
					String str = new String(buffer, "UTF-8");
					System.out.println(str);
				}
				catch (SerialPortException ex) {
					System.out.println(ex);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

//
//				try {
//					char inChar = (char)_serialInput.read();
//					// add it to the inputString:
//					if ((int)inChar != 65535) {
//						inputString += inChar;
//					}
//					// if the incoming character is a newline, set a flag
//					// so the main loop can do something about it:
//					if (inChar == '\n') {
//						//System.out.println("ECHO= " + inputString);
//
//						inputFromUAV.add(inputString);
//
//						// Manage each command from UAV
//						lastInputString = inputString;
//						inputReady = true;
//						inputTaken = false;
//
//						inputString = "";					 
//					}
//
//
//				} catch (IOException e) {}
			}
			
		}
	}

}
