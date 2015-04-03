package breeze.groundstation.main.actionCommand;

import breeze.groundstation.model.UAVState;
import breeze.groundstation.serialPort.SerialPortDriverInterface;

public class ActionAutomodeSwitch extends ActionCommand {

	@Override
	public void makeAction(UAVState uav, SerialPortDriverInterface serialPort) {
		uav.switchMode();
		String str_bytes = "auto|"+uav.automodeToString()+'\n';		
		serialPort.writeToSerial(str_bytes.getBytes());	
	}

	@Override
	public long getDtBetweenCommandUs() {
		return 1000000L;
	}

}
