package breeze.groundstation.main;

public class GSParameters {
	public int _gsParam_MaxCentiRoll = 4000;
	public int _gsParam_MaxCentiPitch = 4000;
	public static int MAX_CENTI_RUDDER = 4500;
	
	private static GSParameters INSTANCE = null;
	
	private GSParameters() {
		// read parameters from file
	}
	
	public static GSParameters getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new GSParameters();
		}
		
		return INSTANCE;
	}
	
	public void setMaxCentiRoll(int v) {
		_gsParam_MaxCentiRoll = v;
	}
	public void setMaxCentiPitch(int v) {
		_gsParam_MaxCentiPitch = v;
	}
	
	public int getMaxCentiRoll() {
		return _gsParam_MaxCentiRoll;
	}
	
	public int getMaxCentiPitch() {
		return _gsParam_MaxCentiPitch;
	}
}
