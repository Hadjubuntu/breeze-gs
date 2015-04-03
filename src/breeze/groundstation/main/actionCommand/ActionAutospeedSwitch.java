package breeze.groundstation.main.actionCommand;

import breeze.groundstation.model.UAVState;
import breeze.groundstation.serialPort.SerialPortDriverInterface;

public class ActionAutospeedSwitch extends ActionCommand {
	
	@Override
	public void makeAction(UAVState uav, SerialPortDriverInterface serialPort) {
		uav.switchAutospeed();
		String str_bytes = "speedauto|"+uav.autospeedToString()+'\n';		
		serialPort.writeToSerial(str_bytes.getBytes());	
	}

	@Override
	public long getDtBetweenCommandUs() {
		return 1000000L;
	}

}
