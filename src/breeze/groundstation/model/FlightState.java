package breeze.groundstation.model;

public enum FlightState {
	SETUP,
	TAKEOFF,
	CLIMB,
	CRUISE,
	LANDING;
	
	public static FlightState fromInteger(int x) {
        switch(x) {
        case 0:
            return SETUP;
        case 1:
            return TAKEOFF;
        case 2:
            return CLIMB;
        case 3:
            return CRUISE;
        case 4:
            return LANDING;
        }
        return null;
    }
}
