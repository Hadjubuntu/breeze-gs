package breeze.groundstation.main;

public class Utils {
	
	public static int S_TO_US = 1000000;
	public static int S_TO_MS = 1000;
	
	public static long micros() {
		return (long)(System.nanoTime()/1000.0);
	}
	
	public static double sign(final double x) {
	      if (Double.isNaN(x)) {
	          return Double.NaN;
	      }
	      return (x == 0.0) ? 0.0 : (x > 0.0) ? 1.0 : -1.0;
	  }
	
	public static int constrain(int flapsPercent, int min, int max) {
		if (flapsPercent < min) {
			flapsPercent = min;
		}
		else if (flapsPercent > max) {
			flapsPercent = max;
		}
		return flapsPercent;
	}
	
	public static String distHumanText(double distMeters) {
		if (distMeters > 1000) {
			return (int)((distMeters/1000.0)) + "km";
		}
		else if (distMeters < 1) {
			return (int)(distMeters*100) + "cm";
		}
		
		return (int)(distMeters)+"m";
	}

	public static int toCenti(double d) {
		return (int)(d*100.0);
	}

	public static int toPow6(double input) {
		return (int)(input * 1000000);
	}
}
