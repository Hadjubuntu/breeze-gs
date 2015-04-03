package breeze.groundstation.gui;


import java.util.Random;

import org.eclipse.swt.widgets.Display;

import breeze.groundstation.main.GSController;
import breeze.groundstation.main.JoystickController;
import breeze.groundstation.main.Utils;
import breeze.groundstation.model.MissionListener;
import breeze.groundstation.model.UAVState;
import breeze.groundstation.parts.ControlFlightPart;
import breeze.groundstation.parts.MapVectorizedPart;
import breeze.groundstation.parts.MissionManagerPart;


public class GuiUpdater extends Thread implements MissionListener {
	private boolean guiUpdateRun;
	private UAVState model;
	private ControlFlightPart gui;
	private boolean tokenUpdateMission;
	private MissionManagerPart guiMission;

	public GuiUpdater(UAVState pModel, ControlFlightPart pGui) {
		guiUpdateRun = true;
		tokenUpdateMission = false;
		model = pModel;
		gui = pGui;
		guiMission = null;
	}

	public void setGuiMission(MissionManagerPart pGui) {
		guiMission = pGui;
		guiMission.addMissionListener(this);
	}

	public void run() {
		int frame = 0;
		Random rand = new Random();

		double mockLat = 48.9186812250776 - 1/1000.0;
		double mockLon = 2.138045955273005 - 1/1000.0;
		double alt = 0.0;

		while (guiUpdateRun) {


			if (gui != null && model != null) {
				// Update throttle
				gui.setThrustPercent(model.getThrottlePercent());
				gui.setFlapsPercent(model.getFlapsPercent());
				gui.setAttitude(model.getRoll(), model.getPitch(), JoystickController.getInstance().joystickRoll/100.0, JoystickController.getInstance().joystickPitch/100.0);

				GSController.getInstance().updateRFStats();
				gui.setUAVData(model.getAirspeedVms(), model.getCap(), model.getAngleDiffToTarget(), GSController.getInstance().getStatBytePerSecond());

				gui.setAutomode(model.isAutoMode());

				// If user user autospeed and UAV is flying altitude > 5m, then alert on a v_speed_goal threshold
				if (frame % 10 == 0) {
					if (model.isAutospeed() && model.getAltitude() > 500 && model.throttleToVmsGoal() < 8) {
							// Make alert
					}
				}

				if (frame % 50 == 0) {
					mockLat += (rand.nextDouble() / 40000.0);
					mockLon += (rand.nextDouble() / 40000.0);
					alt += (rand.nextDouble() / 10);

					//	model.addPosition(mockLat, mockLon, (int)(rand.nextDouble()*100*10) + 20*100);
					MapVectorizedPart.doRepaint();

					
					String mockRF = "att|" 
							+ Utils.toCenti(rand.nextDouble()*45) + "|"
							+ Utils.toCenti(rand.nextDouble()*45) + "|"
							+ (int)(rand.nextDouble()*360.0) + "|"// cap
							+ ((int)(alt*100)) + "|"
							+ Utils.toCenti(rand.nextDouble()*15.0) + "|"// v ms
							+ Utils.toPow6(mockLat) + "|"//  latitude
							+ Utils.toPow6(mockLon) + "|"// longitude
							+ "0|0|0|" // angle diff and flight state and current wp
							;

					GSController.getInstance().logAttitude(mockRF, false);
					
					/**
					 * 	_uav.setRoll(Double.valueOf(datas[1]) / 100.0);
			_uav.setPitch(Double.valueOf(datas[2]) / 100.0);
			_uav.setCap(Integer.valueOf(datas[3]));
			_uav.setAltitude(Integer.valueOf(datas[4]));
			_uav.setAirspeedVms(Double.valueOf(datas[5])/100.0);
			_uav.addPosition(Long.valueOf(datas[6])/(10.0*Utils.toPow6(1)), Long.valueOf(datas[7])/(10.0*Utils.toPow6(1)), Integer.valueOf(datas[4]));
			//_uav.setFlightState(FlightState.fromInteger(Integer.valueOf(datas[8])));
			_uav.setAngleDiffToTarget(Integer.valueOf(datas[8]));
			_uav.setAutoMode((Integer.valueOf(datas[9]) == 1) ? true : false);
			_uav.setCurrentWP(Integer.valueOf(datas[10]));
					 */

				}

				if (frame % 5 == 0 && guiMission != null) {
					try {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {

								gui.updateUAVState();

								// Update frame
								guiMission.update();

								// Update waypoints of the mission if event
								if (tokenUpdateMission) {
									MapVectorizedPart.setCurrentMission(guiMission.getMission());
									tokenUpdateMission = false;
								}
							}});
					}
					catch(Exception e) {}
				}

				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				frame ++;
				if (frame > 1000000) {
					frame = 0;
				}
			}
		}
	}

	@Override
	public void newMissionLoaded() {
		tokenUpdateMission = true;		
	}
}
