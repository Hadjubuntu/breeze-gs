package breeze.groundstation.main.actionCommand;

import breeze.groundstation.main.Utils;
import breeze.groundstation.model.UAVState;
import breeze.groundstation.serialPort.SerialPortDriverInterface;

public class ActionFlaps extends ActionCommand {
	private int _flapsPercent ;
	
	public ActionFlaps(int flapsPercent) {
		_flapsPercent = Utils.constrain(flapsPercent, 0, 100);
	}

	@Override
	public void makeAction(UAVState uav, SerialPortDriverInterface serialPort) {
		uav.setFlapsPecent(_flapsPercent);
		String str_bytes = "flaps|"+_flapsPercent+'\n';		
		serialPort.writeToSerial(str_bytes.getBytes());
	}

	@Override
	public long getDtBetweenCommandUs() {
		return 0;
	}

}
