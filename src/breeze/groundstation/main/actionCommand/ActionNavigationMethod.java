package breeze.groundstation.main.actionCommand;

import breeze.groundstation.model.UAVState;
import breeze.groundstation.serialPort.SerialPortDriverInterface;

public class ActionNavigationMethod extends ActionCommand {
	private int methodNav = 0;
	
	public ActionNavigationMethod(int pMethod) {
		methodNav = pMethod;
	}

	@Override
	public void makeAction(UAVState uav, SerialPortDriverInterface serialPort) {
		String str_bytes = "nav_method|"+methodNav+'\n';
		serialPort.writeToSerial(str_bytes.getBytes());	
	}

	@Override
	public long getDtBetweenCommandUs() {
		return 1000000L;
	}

}
