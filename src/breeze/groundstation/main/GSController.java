package breeze.groundstation.main;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.lwjgl.input.Controllers;

import breeze.groundstation.io.IOManager;
import breeze.groundstation.io.Sound;
import breeze.groundstation.main.actionCommand.ActionAutomodeSwitch;
import breeze.groundstation.main.actionCommand.ActionAutospeedSwitch;
import breeze.groundstation.main.actionCommand.ActionCommand;
import breeze.groundstation.main.actionCommand.ActionFlaps;
import breeze.groundstation.main.actionCommand.ActionNavigationMethod;
import breeze.groundstation.main.actionCommand.ActionRequestConfigurationFromUAV;
import breeze.groundstation.main.actionCommand.ActionSendConfToUav;
import breeze.groundstation.main.actionCommand.ActionShutdownUav;
import breeze.groundstation.main.actionCommand.ActionThrottleDown;
import breeze.groundstation.main.actionCommand.ActionThrottleUp;
import breeze.groundstation.model.UAVState;
import breeze.groundstation.parts.ConfigurationPart;
import breeze.groundstation.serialPort.SerialPortDriver;
import breeze.groundstation.serialPort.SerialPortDriverInterface;

public class GSController extends Thread {
	public static int DELAY_50Hz_US = 20000;
	public static int DELAY_20Hz_US = 50000;
	public static int DELAY_30Hz_US = 30000;
	public static int DELAY_10Hz_US = 100000;
	public static int TIME_ELAPSED_UAV_CONNECTED_US = Utils.S_TO_US * 3;

	public boolean working ;

	private long currentTime;
	private long last50HzExecution;
	private long last30HzExecution;
	private long last20HzExecution;
	private long last10HzExecution;
	private long last30HzExecutionLoopDephased;

	private long lastTimeReceivedLog;


	// Command stays in the list
	public ArrayList<ActionCommand> actionCommands ;
	// Command destroyed just afeter execution
	public ArrayList<ActionCommand> stackActionCommands;
	public SerialPortDriverInterface _serialPort;
	private UAVState _uav;
	private boolean previousUAVConnectedState ;


	private ArrayList<String> attitudeLogger;
	private ArrayList<String> msgLogger;

	private int statBytePerSecond ;

	private ConfigurationPart _configurationPart;
	
	private FlightWarning flightWarning;

	private static GSController INSTANCE;
	public static synchronized GSController getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new GSController();
		}
		return INSTANCE;
	}

	public GSController() {
		working = true;

		actionCommands = new ArrayList<ActionCommand>();
		stackActionCommands = new ArrayList<ActionCommand>();
		attitudeLogger = new ArrayList<String>();
		msgLogger = new ArrayList<String>();
		currentTime = Utils.micros();
		last30HzExecution = 0;
		last20HzExecution = 0;
		last10HzExecution = 0;
		lastTimeReceivedLog = 0;
		last30HzExecutionLoopDephased = currentTime+10000; // Create a phase
		statBytePerSecond = 0;
		
		previousUAVConnectedState = false;

		// Initialize flight warning controller
		flightWarning = new FlightWarning(_uav);


		_serialPort = SerialPortDriver.getInstance();
	}

	

	/**
	 * Main functuon of the controller
	 * It's a loop which update and survey the COM with UAV
	 */
	public void run() {

		// Initialize joystick controller
		JoystickController.getInstance().init();
		
		// First calibrate
		//------------------------------------------------
		JoystickController.getInstance().calibration();

		// Loop over received / to send message
		//------------------------------------------------
		while (working) {

			currentTime = Utils.micros();

			//-------------------------------
			// 50 Hz
			if (currentTime - last50HzExecution > DELAY_50Hz_US) {
				// FIFO method
				ArrayList<String> inputFromUAV = _serialPort.getInputFromUAV();
				ArrayList<String> copyInputFromUAV =  new ArrayList<String>(inputFromUAV);
				for (int k=0; k < copyInputFromUAV.size(); k ++) {

					String inputString = copyInputFromUAV.get(k);

					// Attitude and position logger
					//--------------------------------------
					if (inputString.startsWith("att|")) {
						logAttitude(inputString, true);
					}
					else if (inputString.startsWith("msg|")) {
						System.out.println("UAV sends message : " + inputString);
						msgLogger.add(inputString);
					}
					else if (inputString.startsWith("config|")) {
						_configurationPart.setParameter(inputString);
					}
					else {
						System.out.println("Unknow message type = " + inputString);
					}

					try {
						inputFromUAV.remove(k);
					}
					catch (Exception e) {
						// Manage concurrent
					}
				}

		
				last50HzExecution = currentTime;
			}

			//----------------------------
			// 30 Hz

			if (currentTime - last30HzExecutionLoopDephased > DELAY_30Hz_US) {

				//--------------------------------------------
				// Update joystick attitude at 30 Hz
				JoystickController.getInstance().process30Hz();

				last30HzExecutionLoopDephased = currentTime;
			}


			//--------------------------------------------
			// Process actions stored in stack
			if (currentTime - last30HzExecution > DELAY_30Hz_US) {
				if (_uav.isAutoMode() == false) {
					ArrayList<ActionCommand> copyActionCommandsList = new ArrayList<ActionCommand>(actionCommands);

					for (ActionCommand currentCommand : copyActionCommandsList) {
						if (currentCommand.checkActionToBeDoneAndUpdate()) {
							// Create new action to send to UAV
							currentCommand.makeAction(_uav, _serialPort);
						}
					}
				}
				// For stack action, do it and destroy
				ArrayList<ActionCommand> copyStackActions =  new ArrayList<ActionCommand>(stackActionCommands);
				for (int i = 0; i < copyStackActions.size(); i ++) {
					if (_uav.isAutoMode() == false || copyStackActions.get(i) instanceof ActionAutomodeSwitch) {
						copyStackActions.get(i).makeAction(_uav, _serialPort);

						try {
							stackActionCommands.remove(i);
						}
						catch (Exception e) {
							// Manage concurrent
						}
					}
				}

				last30HzExecution = currentTime;
			}


			if (currentTime - last10HzExecution > DELAY_10Hz_US) {
				

				//--------------------------------------------
				// Update uav flight state
				_uav.updateState();
				
				//--------------------------------------------
				// Check for new warning
				flightWarning.update();
				
				//--------------------------------------------
				// Update joystick button at 10 Hz
				JoystickController.getInstance().process10Hz();
				

				//--------------------------------------------
				// Check for connection or deconnection between gcs and UAV
				if (previousUAVConnectedState != isUAVConnected()) {
					previousUAVConnectedState = isUAVConnected();
					
					if (previousUAVConnectedState == true) {
						Sound.getInstance().play("drone_connected");
					}
					else {
						Sound.getInstance().play("connection_losted");
					}
				}

				last10HzExecution = currentTime;
			}

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}




	public void shutdownUavCommand() {
		stackActionCommands.add(new ActionShutdownUav());
	}

	public boolean hasActionCommand(String typeName) {
		for (ActionCommand action : actionCommands) {
			if (action.getClass().getName().equals(typeName)) {
				return true;
			}
		}

		return false;
	}

	public void deleteActionCommand(String typeName) {
		for (int i = 0; i < actionCommands.size(); i++) {
			if (actionCommands.get(i).getClass().getName().equals(typeName)) {
				actionCommands.remove(i);
			}
		}
	}


	public void setUavState(UAVState uavState) {
		_uav = uavState;
	}


	public boolean isUAVConnected() {
		return (Utils.micros()-lastTimeReceivedLog < TIME_ELAPSED_UAV_CONNECTED_US);
	}

	public void logAttitude(String msgReceived, boolean saveData) {
		//System.out.println("log:\t\t"+((Utils.micros()-lastTimeReceivedLog)/Utils.S_TO_US)+"s\t\t" + msgReceived);
		lastTimeReceivedLog = Utils.micros();
		String dateString = ""; // TODO write hour-minute-seconds to get this information

		// Parse message received and set uav state
		String datas[] = msgReceived.split("\\|");
		boolean succeed = true;

		try {
			_uav.setRoll(Double.valueOf(datas[1]) / 100.0);
			_uav.setPitch(Double.valueOf(datas[2]) / 100.0);
			_uav.setCap(Integer.valueOf(datas[3]));
			_uav.setAltitude(Integer.valueOf(datas[4]));
			_uav.setAirspeedVms(Double.valueOf(datas[5])/100.0);
			_uav.addPosition(Long.valueOf(datas[6])/(10.0*Utils.toPow6(1)), Long.valueOf(datas[7])/(10.0*Utils.toPow6(1)), Integer.valueOf(datas[4]));
			//_uav.setFlightState(FlightState.fromInteger(Integer.valueOf(datas[8])));
			_uav.setAngleDiffToTarget(Integer.valueOf(datas[8]));
			_uav.setAutoMode((Integer.valueOf(datas[9]) == 1) ? true : false);
			_uav.setCurrentWP(Integer.valueOf(datas[10]));

		}
		catch (Exception e) {
			e.printStackTrace();
			succeed = false;
		}

		if (saveData && succeed) {
			// Then store in arraylist, for saving the file
			if (attitudeLogger.size() < 3000) {
				// Add others informations before saving data
				msgReceived = msgReceived.replace("\n", "");
				msgReceived += lastTimeReceivedLog + "|" + JoystickController.getInstance().joystickRoll + "|" + JoystickController.getInstance().joystickPitch + "|" + dateString +  "\n";

				attitudeLogger.add(msgReceived);
			}
		}
	}

	public void saveLogger() {
		Thread threadSaver = new Thread() {
			public void run() {
				if (attitudeLogger.size() > 0) {
					Date dNow = new Date();
					SimpleDateFormat ft = new SimpleDateFormat("dd_MM_yyyy__HH-mm-ss");
					String filepath = "/home/adrien/uav-flight-log-" + ft.format(dNow) + ".csv" ;
					IOManager.saveData(filepath, attitudeLogger);
				}
				//String filepathMsg = "/home/adrien/uav-flight-msg-" + ft.format(dNow) + ".csv" ;
				//IOManager.saveData(filepathMsg, msgLogger);
			}
		};

		threadSaver.start();
	}


	public UAVState getUav() {
		return _uav;
	}

	public void switchManualStabilized() {
		_uav.setManual_StabilizedFlight(!_uav.isManual_StabilizedFlight());		
	}


	public void switchNavigationMethod(int selectionIndex) {	
		stackActionCommands.add(new ActionNavigationMethod(selectionIndex));	
	}

	public void requestConfigurationFromUAV() {
		stackActionCommands.add(new ActionRequestConfigurationFromUAV());	
	}

	public void sendConfParameter(int pParamId, double pParamValue) {
		stackActionCommands.add(new ActionSendConfToUav(pParamId, pParamValue));		
	}

	public void replayLogger() {
		ThreadReplay replay = new ThreadReplay() ;
		replay.setVariables();
		replay.start();
	}

	public void updateRFStats() {
		int lengthStats[] = _serialPort.getPacketLengthStats();
		long date[] = _serialPort.getPacketTimeUs();

		if (date[0] > 0 && date[1] > 0) {
			int dt = (int)(Math.abs(date[1] - date[0]));
			int lengthTotal = (int)((lengthStats[0] + lengthStats[1])/2.0);

			statBytePerSecond = (int)(lengthTotal * (Utils.S_TO_US / dt));
		}
	}

	public int getStatBytePerSecond() {
		return statBytePerSecond;
	}


	public void registerConfigurationPart(ConfigurationPart configurationPart) {
		_configurationPart = configurationPart;		
	}







}
