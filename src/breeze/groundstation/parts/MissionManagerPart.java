package breeze.groundstation.parts;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import breeze.groundstation.io.IOManager;
import breeze.groundstation.main.Arduino;
import breeze.groundstation.model.GeoPosition;
import breeze.groundstation.model.Mission;
import breeze.groundstation.model.MissionListener;

public class MissionManagerPart {

	// Variables
	//--------------------------------------------------------

	// Skeleton
	public static MissionManagerPart INSTANCE;

	// Models
	private Mission _mission;
	private GeoPosition _lastGeoPosition;

	// Widgets
	private Label labelPerf;
	
	// Listeners
	private ArrayList<MissionListener> missionListeners;


	// Functions
	//--------------------------------------------------------
	public MissionManagerPart() {
		_mission = null;
		_lastGeoPosition = null;
		labelPerf = null;
		missionListeners = new ArrayList<MissionListener>();
	}

	@PostConstruct
	public void createControls(Composite parent) {
		INSTANCE = this;
		parent.setLayout(new org.eclipse.swt.layout.GridLayout(3, false));


		// [1] Load, save [2] State [3] Performance (dist to destination)
		Button btnLoadMission = new Button(parent, SWT.PUSH);
		btnLoadMission.setVisible(true);
		btnLoadMission.setText("Load");
		btnLoadMission.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		btnLoadMission.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				
				Display display = Display.getCurrent();
				Shell shell = display.getActiveShell();
				FileDialog dialog = new FileDialog(shell);
				String filepath = dialog.open();
				Mission mission = readMissionFile(filepath);
				fireNewMissionLoaded(mission);
			}});


		Label labelState = new Label(parent, SWT.FLAT);
		labelState.setText("State : takeoff");


		labelPerf = new Label(parent, SWT.FLAT);
		labelPerf.setText("Dist WP = 0m");
		
		Arduino.getInstance().postConstructMission(this);

		// Set default mission
		/*Mission defaultMission = new Mission();
		readMissionFile("D:\\drone\\BreezeViewer\\GroundNav_26_11.kml");
		fireNewMissionLoaded(defaultMission);*/
	}



	@Focus
	public void onFocus() {
	}

	public void updateDestToWP(GeoPosition pLastGeoPosition, int pCurrentWP) {
		_lastGeoPosition = pLastGeoPosition;

	}

	public void update() {
		int distMeters = 10;
		//labelPerf.setText("Dist WP = " + distMeters + "m | WP=" + currentWP);

	}
	
	public void fireNewMissionLoaded(Mission pMission) {
		// Update model
		_mission = pMission;
		
		// Update GUI
		for (MissionListener listener : missionListeners) {
			listener.newMissionLoaded();
		}
	}
	
	// IO functions - read
	//----------------------------------------------------------------------------
	public static Mission readMissionFile(String filepath) {
		Mission output = new Mission();
			
		String data = IOManager.readFileToString(filepath);
		String res[] = data.split("<coordinates>");
		if (res.length > 0) {
			res = res[1].split("</coordinates>");
			
			String coordsData = res[0].trim();
			String gpsCoords[] = coordsData.split(" ");
			

			for (String gpsCoord : gpsCoords) {
				String coordsArray[] = gpsCoord.split(",");
				if (coordsArray.length >= 2) {
					output.addWaypoint(-1, Double.parseDouble(coordsArray[1]), Double.parseDouble(coordsArray[0]), 30);
				}
			}
		}
		
		return output;
	}

	public void addMissionListener(MissionListener pListener) {
		missionListeners.add(pListener);
	}

	public Mission getMission() {
		return _mission;
	}
} 