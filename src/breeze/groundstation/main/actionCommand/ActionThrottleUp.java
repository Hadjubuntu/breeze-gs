package breeze.groundstation.main.actionCommand;

import breeze.groundstation.model.UAVState;
import breeze.groundstation.serialPort.SerialPortDriverInterface;

public class ActionThrottleUp extends ActionCommand {
	public long getDtBetweenCommandUs() {
		return 200000;
	}

	@Override
	public void makeAction(UAVState uav, SerialPortDriverInterface serialPort) {
		int dThrottle = _iterContinuous+1;
		uav.incrThrottle(dThrottle);
		String str_bytes = "thro|"+uav.getThrottlePercent()+'\n';		
		serialPort.writeToSerial(str_bytes.getBytes());
	}
}
