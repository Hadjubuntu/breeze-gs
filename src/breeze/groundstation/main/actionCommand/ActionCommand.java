package breeze.groundstation.main.actionCommand;

import breeze.groundstation.main.Utils;
import breeze.groundstation.model.UAVState;
import breeze.groundstation.serialPort.SerialPortDriverInterface;

public abstract class ActionCommand {
	protected int _iterContinuous;
	private long _lastTimeCommandedUs ;
	
	public ActionCommand() {
		_lastTimeCommandedUs = 0;
		_iterContinuous = 0; 
	}
	
	public abstract void makeAction(UAVState uav, SerialPortDriverInterface serialPort) ;
	
	public boolean checkActionToBeDoneAndUpdate() {
		long time = Utils.micros();
		
		if (time - _lastTimeCommandedUs > getDtBetweenCommandUs()) { 
			_lastTimeCommandedUs = Utils.micros();
			if (_iterContinuous < 20) {
				_iterContinuous ++;
			}
			return true;
		}
		else {
			return false;
		}
	}
	public abstract long getDtBetweenCommandUs();
}
