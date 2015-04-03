package breeze.groundstation.algo;

import breeze.groundstation.model.GeoPosition;

public class GSMath {
	
	public static int R = 6378100;
	public static double M_PI = Math.PI;
	
	public GSMath() {
		
	}

	public static double pow2(double a) {
	    return a*a;
	}
	
	public static double toDeg(double v) {
		return (v * 180.0 / M_PI);
	}

	public static double toRad(double v) {
		return (v * M_PI / 180.0);
	}
	
	public static double geoDistance(GeoPosition pos1, GeoPosition pos2) {
		double dlon = pos2.getLon() - pos1.getLon();
		double dlat = pos2.getLat() - pos1.getLat();
		double a = pow2(Math.sin(toRad(dlat/2))) + Math.cos(toRad(pos1.getLat())) * Math.cos(toRad(pos2.getLat())) * pow2(Math.sin(toRad(dlon/2)));
		double c = 2 * Math.atan2( Math.sqrt(a), Math.sqrt(1-a) );
		return R * c;
	}
}
