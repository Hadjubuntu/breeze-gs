package breeze.groundstation.main;

import java.util.HashMap;

import org.lwjgl.input.Controllers;

import breeze.groundstation.io.Sound;
import breeze.groundstation.main.actionCommand.ActionAutomodeSwitch;
import breeze.groundstation.main.actionCommand.ActionAutospeedSwitch;
import breeze.groundstation.main.actionCommand.ActionFlaps;

public class JoystickController {


	public int joystickRoll;
	public int joystickPitch;
	public int joystickRudder;

	private org.lwjgl.input.Controller _joystickLWJGL;
	private HashMap<Integer, Long> _joystickButtonTimeUs;

	private static JoystickController instance;
	private GSController _gs;

	private JoystickController() {
		_gs = GSController.getInstance();
	}

	public static JoystickController getInstance() {
		if (instance == null) {
			instance = new JoystickController();
		}

		return instance;
	}

	public void init() {
		//--------------------------------------------------------------
		// Joystick LWJGL
		_joystickLWJGL = null;
		_joystickButtonTimeUs = new HashMap<Integer, Long>();
		try {
			Controllers.create();
			System.out.println("There is : " + Controllers.getControllerCount() + " controllers.");

			if (Controllers.getControllerCount() > 0) {

				for (int i = 0; i < Controllers.getControllerCount(); i ++) {
					_joystickLWJGL = Controllers.getController(i);

					if (_joystickLWJGL.getName().equals("T.Flight Stick X") || _joystickLWJGL.getName().equals("Thrustmaster T.Flight Stick X")) {
						System.out.println("Joystick has "+_joystickLWJGL.getButtonCount() +" buttons. Its name is "+_joystickLWJGL.getName());

						for (int k = 0; k < _joystickLWJGL.getButtonCount()+1; k++) {
							_joystickButtonTimeUs.put(k, (long) 0);
						}
					}					
				}
			}
		}
		catch (org.lwjgl.LWJGLException e) {System.err.println("Couldn't initialize Controllers: "+e.getMessage());}
	}

	public boolean checkJoystickValuesCalibrated(double[] v) {
		boolean res = true;

		for (int i = 0; i < v.length; i ++) {
			if (v[i] != _joystickLWJGL.getAxisValue(i)) {
				res = false;
				break;
			}
		}

		return res;
	}

	public void calibration() {
		if (_joystickLWJGL != null) {
			System.out.println("Joystick calibration waiting -- User must move the joystick first");
			_joystickLWJGL.poll();

			double[] joystickValues = new double[_joystickLWJGL.getAxisCount()];

			for (int i = 0; i < _joystickLWJGL.getAxisCount(); i ++) {
				joystickValues[i] = _joystickLWJGL.getAxisValue(i);
			}

			while (checkJoystickValuesCalibrated(joystickValues)) {
				// Wait until joystick value change for calibration
				_joystickLWJGL.poll();

				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			System.out.println("Joystick is calibrated");
			System.out.println("Waiting for thrust to be at zero");
			if ((_joystickLWJGL.getAxisValue(3) < 0.98)) {
				Sound.getInstance().play("stick_zero");
			}
			while (_joystickLWJGL.getAxisValue(3) < 0.98) { // Warning : zero on throttle means 1 on joystick
				_joystickLWJGL.poll();

				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Joystick ready to use for UAV controls");
		}
	}

	public void process30Hz() {
		if (_joystickLWJGL != null) {
			// Poll joystick position
			_joystickLWJGL.poll();

			// Atitude
			//---------------------------------------------

			joystickRoll = (int)(_joystickLWJGL.getAxisValue(0)*GSParameters.getInstance().getMaxCentiRoll()); // Asus1215n : 3
			joystickPitch = (int)(_joystickLWJGL.getAxisValue(1)*GSParameters.getInstance().getMaxCentiPitch()); // Asus1215n : 2

			double zValue = _joystickLWJGL.getAxisValue(2);	 // Asus1215n : 1				
			joystickRudder = (int)(Utils.sign(zValue)*Math.pow(zValue, 2)*GSParameters.MAX_CENTI_RUDDER); // x^2 to have more sensibility in lower values

			String token = "ac";

			if (_gs.getUav().isManual_StabilizedFlight()) {
				token = "ac";
			}
			else {
				token = "sc";
			}

			// To help the user during takeoff, if he uses the button 0 (gachette)
			// Then we set roll and yaw to 0
			if (_joystickLWJGL.isButtonPressed(0)) {
				joystickRoll = 0;
				joystickRudder = 0;
			}

			// To trim the airplane, we can use POV
			// System.out.println("yaw = " + joystickRudder + "pov = " + _joystickLWJGL.getPovX() + ", " + _joystickLWJGL.getPovY());

			// Thrust
			//----------------------------------------------
			float joystickThrust = _joystickLWJGL.getAxisValue(3); // Asus1215n : 0

			int deciThrustPercent = 0;
			// Joystick go from 1.0 to 0.0 for 0 to 50, and 0 to -1 for 50 to 100
			if (joystickThrust >= 0.0) {
				deciThrustPercent = (int)((1000 - joystickThrust*1000.0)/2.0);
			}
			else {
				deciThrustPercent = (int)((-joystickThrust*500.0) + 500);
			}

			// DeciThrustPercent goes from 0 to 1000
			// We map this output from 150 to 1000 in order to have no cut-off for low percent thrust value
			//deciThrustPercent = (int)((deciThrustPercent + 150)*(100.0/115.0));

			_gs.getUav().setThrottlePercent((int)(deciThrustPercent/10.0));		


			// Prepare and send packet
			//------------------------------------------------------
			String str_bytes = token + "|" + joystickRoll + "|" + joystickPitch + "|" + joystickRudder + "|"  + deciThrustPercent + "\n";


			if (_gs._serialPort != null && _gs.getUav().isAutoMode() == false) {
				_gs._serialPort.writeToSerial(str_bytes.getBytes());
			}				
		}


	}

	public void process10Hz() {
		// Button check				
		if (_joystickLWJGL != null) {
			//										for (int kl = 0; kl < _joystickLWJGL.getButtonCount(); kl ++) {
			//											if (_joystickLWJGL.isButtonPressed(kl)) {
			//												System.out.println("Button " + kl + " = " +_joystickLWJGL.isButtonPressed(kl)) ;
			//											}
			//										}
			if (_joystickLWJGL.isButtonPressed(9)) {
				_gs.stackActionCommands.add(new ActionFlaps(0));						
			}
			else if (_joystickLWJGL.isButtonPressed(8)) {
				_gs.stackActionCommands.add(new ActionFlaps(30));	
			}		
			else if (_joystickLWJGL.isButtonPressed(6)) {
				_gs.stackActionCommands.add(new ActionFlaps(60));						
			}
			else if (_joystickLWJGL.isButtonPressed(7)) {
				_gs.stackActionCommands.add(new ActionFlaps(90));						
			}
			else if (_joystickLWJGL.isButtonPressed(1)) {
				long ctime = Utils.micros();
				// Prevent from changing to fast
				if ((ctime - _joystickButtonTimeUs.get(1)) > Utils.S_TO_US) {
					_gs.stackActionCommands.add(new ActionAutomodeSwitch());
					_joystickButtonTimeUs.put(1, ctime);		
				}
			}
			else if (_joystickLWJGL.isButtonPressed(3)) {
				long ctime = Utils.micros();
				// Prevent from changing to fast
				if ((ctime - _joystickButtonTimeUs.get(3)) > Utils.S_TO_US) {
					_gs.stackActionCommands.add(new ActionAutospeedSwitch());
					_joystickButtonTimeUs.put(3, ctime);		
				}
			}

		}
	}

}
