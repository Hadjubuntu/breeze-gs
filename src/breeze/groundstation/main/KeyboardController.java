package breeze.groundstation.main;

import breeze.groundstation.main.actionCommand.ActionAutomodeSwitch;
import breeze.groundstation.main.actionCommand.ActionFlaps;
import breeze.groundstation.main.actionCommand.ActionThrottleDown;
import breeze.groundstation.main.actionCommand.ActionThrottleUp;

public class KeyboardController {
	
	private static KeyboardController instance;

	private Character _key_throttleUp = 'a';
	private Character _key_throttleDown = 'z';
	private Character _key_automodeSwitch = 'o';
	private Character _key_flaps_0 = '&';
	private Character _key_flaps_20 = 'é';
	private Character _key_flaps_40 = '"';
	private Character _key_flaps_60 = '\'';
	private Character _key_flaps_80 = '(';
	private Character _key_flaps_100 = '-';

	private GSController _gs;

	private KeyboardController() {
		_gs = GSController.getInstance();
	}
	
	public static KeyboardController getInstance() {
		if (instance == null) {
			instance = new KeyboardController();
		}
		
		return instance;
	}


	public void processPressedKey(Character key) {
		if (key.equals(_key_throttleUp)) {
			if (!_gs.hasActionCommand("breeze.groundstation.main.actionCommand.ActionThrottleUp")) {
				_gs.actionCommands.add(new ActionThrottleUp());
			}
		}
		else if (key.equals(_key_throttleDown)) {
			if (!_gs.hasActionCommand("breeze.groundstation.main.actionCommand.ActionThrottleDown")) {
				_gs.actionCommands.add(new ActionThrottleDown());
			}
		}

		// Stack actions
		if (key.equals(_key_automodeSwitch)) {
			_gs.stackActionCommands.add(new ActionAutomodeSwitch());			
		}
		else if (key.equals(_key_flaps_0)) {
			_gs.stackActionCommands.add(new ActionFlaps(0));	
		}
		else if (key.equals(_key_flaps_20)) {
			_gs.stackActionCommands.add(new ActionFlaps(20));	
		}
		else if (key.equals(_key_flaps_40)) {
			_gs.stackActionCommands.add(new ActionFlaps(40));	
		}
		else if (key.equals(_key_flaps_60)) {
			_gs.stackActionCommands.add(new ActionFlaps(60));	
		}
		else if (key.equals(_key_flaps_80)) {
			_gs.stackActionCommands.add(new ActionFlaps(80));	
		}
		else if (key.equals(_key_flaps_100)) {
			_gs.stackActionCommands.add(new ActionFlaps(100));	
		}

	}

	public void processReleasedKey(Character key) {
		if (key.equals(_key_throttleUp)) {
			_gs.deleteActionCommand("breeze.groundstation.main.actionCommand.ActionThrottleUp");
		}
		else if (key.equals(_key_throttleDown)) {
			_gs.deleteActionCommand("breeze.groundstation.main.actionCommand.ActionThrottleDown");
		}
	}

}
