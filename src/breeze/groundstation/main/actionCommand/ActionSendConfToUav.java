package breeze.groundstation.main.actionCommand;

import breeze.groundstation.model.UAVState;
import breeze.groundstation.serialPort.SerialPortDriverInterface;

public class ActionSendConfToUav extends ActionCommand {
	private int _paramId;
	private double _paramValue;
	
	public ActionSendConfToUav(int pParamId, double pParamValue) {
		_paramId = pParamId;
		_paramValue = pParamValue;
	}
	
	@Override
	public void makeAction(UAVState uav, SerialPortDriverInterface serialPort) {
		String str_bytes = "conf|"+_paramId+"|"+_paramValue+"|\n";		
		serialPort.writeToSerial(str_bytes.getBytes());
	}
	
	@Override
	public long getDtBetweenCommandUs() {
		return 0;
	}
}
