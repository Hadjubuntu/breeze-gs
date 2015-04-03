package breeze.groundstation.main;

import breeze.groundstation.io.Sound;
import breeze.groundstation.model.FlightState;
import breeze.groundstation.model.UAVState;

public class FlightWarning {

	private UAVState _uav;
	private long _lastLandingAltitudeWarning ;
	private long _lastCruiseAltitudeInfo ;

	public FlightWarning(UAVState pUav) {
		_uav = pUav;
		_lastLandingAltitudeWarning = 0;
		_lastCruiseAltitudeInfo = 0;
	}

	public void update() {
		long ctime = Utils.micros();

		if (_uav != null) {
			if (_uav.getFlightState().equals(FlightState.LANDING)) {
				if (ctime - _lastLandingAltitudeWarning > Utils.S_TO_US * 2) {
					int altMeters = ((int)(_uav.getAltitude()/100.0)) ;

					if (altMeters < 10) {
						String metersString = altMeters  + "_meters";
						Sound.getInstance().play(metersString);
					}
				}
			}
			else if (_uav.getFlightState().equals(FlightState.CRUISE)) {
				if (ctime - _lastCruiseAltitudeInfo > Utils.S_TO_US * 15) {

					int altMeters = ((int)(_uav.getAltitude()/100.0)) ;
					if (altMeters < 10) {
						String metersString = altMeters + "_meters";
						Sound.getInstance().play(metersString);
					}
					else if (altMeters < 20) {
						String metersString = "over_10_meters";
						Sound.getInstance().play(metersString);
					}
					else if (altMeters >= 20) {
						String metersString = "over_20_meters";
						Sound.getInstance().play(metersString);
					}
				}
			}
		}
	}
}
