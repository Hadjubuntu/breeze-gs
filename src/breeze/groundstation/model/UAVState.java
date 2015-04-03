package breeze.groundstation.model;

import java.util.ArrayList;

import breeze.groundstation.parts.MissionManagerPart;

public class UAVState {

	private int throttlePercent;
	private int flapsPercent;
	private boolean autoMode;
	private boolean manual_StabilizedFlight;
	private boolean autospeed ;


	private int aileronCentidegrees;
	private int gouvernCentidegrees;
	private int rubberCentidegrees;

	private double roll; // degrees
	private double pitch; // degrees

	private int cap; // degrees
	private int angleDiffToTarget; // Degrees
	private int altitude; // cm

	private double airspeedVms; // m/s

	private ArrayList<GeoPosition> positions;
	
	private FlightState flightState;
	private int currentWP;

	public UAVState() {
		init();
	}

	public void init() {
		positions = new ArrayList<GeoPosition>();

		autoMode = false;
		manual_StabilizedFlight = true;
		autospeed = false;
		throttlePercent = 0;
		flapsPercent = 0;

		aileronCentidegrees = 0;
		gouvernCentidegrees = 0;
		rubberCentidegrees = 0;

		roll = 0.0;
		pitch = 0.0;
		flightState = FlightState.SETUP;
		currentWP = 0;
		cap = 0;
		angleDiffToTarget = 0;
	}
	
	public double throttleToVmsGoal() {
		return (double)(Math.round(1.7*14*throttlePercent)/100.0);
	}

	public boolean isManual_StabilizedFlight() {
		return manual_StabilizedFlight;
	}

	public void setManual_StabilizedFlight(boolean manual_StabilizedFlight) {
		this.manual_StabilizedFlight = manual_StabilizedFlight;
	}

	public void setThrottlePercent(int throttlePercent) {
		if (throttlePercent > 100) {
			throttlePercent = 100;
		}
		else if (throttlePercent < 0) {
			throttlePercent = 0;
		}

		this.throttlePercent = throttlePercent;
	}

	public void switchMode() {
		autoMode = !autoMode;
	}

	public void setFlapsPecent(int pFlapsPercent) {
		flapsPercent = pFlapsPercent;		
	}

	public String automodeToString() {
		if (autoMode) {
			return "on";
		}
		else {
			return "off";
		}
	}
	

	public String autospeedToString() {
		if (autospeed) {
			return "1";
		}
		else {
			return "0";
		}
	}
	public void switchAutospeed() {
		autospeed = !autospeed;
	}

	

	public void setAutoMode(boolean autoMode) {
		this.autoMode = autoMode;
	}

	public void incrThrottle(int dThrottle) {
		throttlePercent += dThrottle;
		if (throttlePercent > 100) {
			throttlePercent = 100;
		}
	}

	public void decrThrottle(int dThrottle) {
		throttlePercent -= dThrottle;
		if (throttlePercent < 0) {
			throttlePercent = 0;
		}
	}

	public int getThrottlePercent() {
		return throttlePercent;
	}

	public int getFlapsPercent() {
		return flapsPercent;
	}

	public boolean isAutoMode() {
		return autoMode;
	}

	public int getAileronCentidegrees() {
		return aileronCentidegrees;
	}
	
	

	public FlightState getFlightState() {
		return flightState;
	}

	public void setFlightState(FlightState flightState) {
		this.flightState = flightState;
	}

	public int getCurrentWP() {
		return currentWP;
	}

	public void setCurrentWP(int currentWP) {
		this.currentWP = currentWP;
	}

	public int getGouvernCentidegrees() {
		return gouvernCentidegrees;
	}

	public int getRubberCentidegrees() {
		return rubberCentidegrees;
	}

	public void shutdown() {
		aileronCentidegrees = 0;
		flapsPercent = 0;
		gouvernCentidegrees = 0;
		rubberCentidegrees = 0;
		throttlePercent = 0;
	}

	public double getRoll() {
		return roll;
	}

	public void setRoll(double roll) {
		this.roll = roll;
	}

	public double getPitch() {
		return pitch;
	}

	public void setPitch(double pitch) {
		this.pitch = pitch;
	}

	public int getCap() {
		return cap;
	}

	public void setCap(int cap) {
		this.cap = cap;
	}

	public int getAngleDiffToTarget() {
		return angleDiffToTarget;
	}

	public void setAngleDiffToTarget(int angleDiffToTarget) {
		this.angleDiffToTarget = angleDiffToTarget;
	}

	public int getAltitude() {
		return altitude;
	}

	public void setAltitude(int altitude) {
		this.altitude = altitude;
	}

	public double getAirspeedVms() {
		return airspeedVms;
	}

	public void setAirspeedVms(double airspeedVms) {
		this.airspeedVms = airspeedVms;
	}	

	public void addPosition(double pLat, double pLon, int pAltCm) {
		GeoPosition lastGeoPosition = new GeoPosition(pLat, pLon, pAltCm);
		positions.add(lastGeoPosition);
		if (MissionManagerPart.INSTANCE != null) {
			MissionManagerPart.INSTANCE.updateDestToWP(lastGeoPosition, currentWP);
		}
	}

	public ArrayList<GeoPosition> getPosition() {
		return positions;
	}

	public boolean isAutospeed() {
		return autospeed;
	}

	public void setAutospeed(boolean autospeed) {
		this.autospeed = autospeed;
	}

	public void updateState() {
		if (flightState.equals(FlightState.SETUP) && altitude > 300) {
			flightState = FlightState.CRUISE;
		}
		else if (flightState.equals(FlightState.CRUISE) && altitude < 300) {
			flightState = FlightState.LANDING;
		}
	}

	
	
}
