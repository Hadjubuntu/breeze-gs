package breeze.groundstation.main.actionCommand;

import breeze.groundstation.model.UAVState;
import breeze.groundstation.serialPort.SerialPortDriverInterface;

public class ActionRequestConfigurationFromUAV extends ActionCommand {

	@Override
	public void makeAction(UAVState uav, SerialPortDriverInterface serialPort) {
		String str_bytes = "request_conf|\n";		
		serialPort.writeToSerial(str_bytes.getBytes());
	}
	
	@Override
	public long getDtBetweenCommandUs() {
		return 0;
	}

}
