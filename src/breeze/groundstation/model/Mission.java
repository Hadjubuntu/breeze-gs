package breeze.groundstation.model;

import java.util.ArrayList;

public class Mission {
	private ArrayList<GeoPosition> waypoints ;
	

	// Constructor
	//---------------------------------------------------
	public Mission() {
		waypoints = new ArrayList<GeoPosition>();
	}
	
	public ArrayList<GeoPosition> getWaypoints() {
		return waypoints;
	}
	
	
	public void addWaypoint(int pos, double lat, double lon, double altMeters) {
		if (pos == -1) {
			pos = waypoints.size();
		}
		
		waypoints.add(pos, new GeoPosition(lat, lon, (int)(altMeters * 100)));
	}

	public void uploadToUav() {
		
	}
}
