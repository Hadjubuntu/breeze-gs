package breeze.groundstation.parts;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import breeze.groundstation.gui.GSPanel;
import breeze.groundstation.gui.Map;
import breeze.groundstation.main.Arduino;
import breeze.groundstation.model.Mission;

public class MapVectorizedPart {

	public static MapVectorizedPart INSTANCE;
	private Map gsMap;
	private static Mission _mission;


	public MapVectorizedPart() {
		_mission = null;
	}

	@PostConstruct
	public void createControls(Composite parent) {
		INSTANCE = this;
		parent.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));
		GSPanel mapPanel = new GSPanel(new GridLayout(1, 1));

		gsMap = new Map(new Color(20, 20, 50, 130));

		mapPanel.add(gsMap);		

		Composite composite = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		Frame frame = SWT_AWT.new_Frame(composite);
		frame.add(mapPanel);

	}

	public void repaint() {
		if (gsMap != null) {
			gsMap.repaint();
			gsMap.getParent().repaint();
		}
	}


	@Focus
	public void onFocus() {
	}

	public static void doRepaint() {
		if (INSTANCE != null) {
			INSTANCE.repaint();
		}
		
	}

	public static void setCurrentMission(Mission mission) {
		_mission = mission;		
	}
	
	public Mission getCurrentMission() {
		return _mission;
	}
} 