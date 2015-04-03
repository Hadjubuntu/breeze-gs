package breeze.groundstation.main;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;

import breeze.groundstation.gui.GuiUpdater;
import breeze.groundstation.handlers.WindowCloseHandler;
import breeze.groundstation.model.UAVState;
import breeze.groundstation.parts.ControlFlightPart;
import breeze.groundstation.parts.MissionManagerPart;
import breeze.groundstation.serialPort.SerialPortDriver;
import breeze.groundstation.serialPort.SerialPortDriverInterface;

public class Arduino implements Runnable {

	private static GuiUpdater _guiUpdater;
	private static UAVState uav = new UAVState();
	private static GSController GroundStationController;
	private static SerialPortDriverInterface portDriver;
	private static Arduino INSTANCE;

	public static synchronized Arduino getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Arduino();
		}
		return INSTANCE;
	}

	public Arduino() {
		_guiUpdater = null;
	}

	public void run() {    	
		GroundStationController = GSController.getInstance();
		GroundStationController.setUavState(uav);
		GroundStationController.start();

		portDriver = SerialPortDriver.getInstance();
	}

	public GSController getGSController() {
		return GroundStationController;
	}

	public SerialPortDriverInterface getPortDriver() {
		return portDriver; 
	}

	public void postConstruct(ControlFlightPart samplePart, MApplication app, EModelService service) {
		_guiUpdater = new GuiUpdater(uav, samplePart);
		_guiUpdater.start();

		WindowCloseHandler close_handler = new WindowCloseHandler();
		java.util.List<MTrimmedWindow> wins = 
				service.findElements(app, null, null, null);
		for(Object win : wins){
			if(win instanceof MTrimmedWindow)
			{
				((MTrimmedWindow)win).getContext().set(IWindowCloseHandler.class, close_handler);
			}
			else if(win instanceof MWindow)          
			{  
				((MWindow)win).getContext().set(IWindowCloseHandler.class, close_handler);
			}
		}
	}


	public void postConstructMission(MissionManagerPart guiPart) {
		_guiUpdater.setGuiMission(guiPart);
	}
}

