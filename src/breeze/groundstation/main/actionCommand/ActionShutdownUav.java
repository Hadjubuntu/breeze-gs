package breeze.groundstation.main.actionCommand;

import breeze.groundstation.model.UAVState;
import breeze.groundstation.serialPort.SerialPortDriverInterface;

public class ActionShutdownUav extends ActionCommand {

	@Override
	public void makeAction(UAVState uav, SerialPortDriverInterface serialPort) {
		uav.shutdown();
		String str_bytes = "shutdown|\n";		
		serialPort.writeToSerial(str_bytes.getBytes());
	}

	@Override
	public long getDtBetweenCommandUs() {
		return 0;
	}

}
