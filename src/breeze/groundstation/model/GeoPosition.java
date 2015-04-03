package breeze.groundstation.model;

import breeze.groundstation.main.Utils;

public class GeoPosition {
	private double lat;
	private double lon;
	private int altCm;
	private long timeReceivedData;
	
	public GeoPosition(double pLat, double pLon, int pAltCm) {
		lat = pLat;
		lon = pLon;
		altCm = pAltCm;
		timeReceivedData = Utils.micros();
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public int getAltCm() {
		return altCm;
	}

	public void setAltCm(int altCm) {
		this.altCm = altCm;
	}

	public long getTimeReceivedData() {
		return timeReceivedData;
	}

	public void setTimeReceivedData(long timeReceivedData) {
		this.timeReceivedData = timeReceivedData;
	}
	
	
}
