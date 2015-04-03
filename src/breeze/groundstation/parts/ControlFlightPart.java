/*******************************************************************************
 * Copyright (c) 2010 - 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <lars.Vogel@gmail.com> - Bug 419770
 *******************************************************************************/
package breeze.groundstation.parts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.annotation.PostConstruct;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import breeze.groundstation.gui.FuncGui;
import breeze.groundstation.gui.GSPanel;
import breeze.groundstation.gui.HUD;
import breeze.groundstation.gui.ImageOnOff;
import breeze.groundstation.gui.Rect;
import breeze.groundstation.main.Arduino;
import breeze.groundstation.main.GSController;
import breeze.groundstation.main.KeyboardController;
import breeze.groundstation.serialPort.SerialPortDriver;
import breeze.groundstation.serialPort.SerialPortDriverInterface;
import breeze.groundstation.serialPort.SerialPortObserver;

public class ControlFlightPart  implements SerialPortObserver {

	private static final String parent = null;

	private GSController _gsController;

	private JLabel airspeedVms;
	private JLabel   labelThrust;
	private Rect rectThrustBg;
	private Rect rectFlapsBg;
	private JLabel   labelFlaps;

	// Model-view controller loop to monitor the state of the serial port
	private SerialPortDriverInterface _portDriver;

	// Graphical elements
	private JLabel _serialPortLabel;

	private HUD HUD_Uav;
	private GSPanel _UAVStatePanel;
	private JLabel labelUAV;
	private ImageOnOff _imageUAVState;
	private GSPanel _HUDPanel;
	private Button btnSwitchManualStabilized;
	private Combo comboNavigationMethod;

	public void setController(GSController pGsController, 
			SerialPortDriverInterface portDriver) {

		_gsController = pGsController;

		_portDriver = portDriver;
		_portDriver.registerObserver(this);

	}

	public void repaint() {
		HUD_Uav.repaint();
		HUD_Uav.getParent().repaint();
		_imageUAVState.repaint();
	}


	@PostConstruct
	public void createComposite(Composite parent, MApplication app, EModelService service) {
		setController(GSController.getInstance(), SerialPortDriver.getInstance());
		parent.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));

		_serialPortLabel = new JLabel("", SwingConstants.LEFT);
		_serialPortLabel.setForeground(Color.WHITE);

		GSPanel portStatusPanel = new GSPanel(new GridLayout(1, 1));
		portStatusPanel.add(_serialPortLabel);
		portStatusChanged();

		// UAV state information
		//----------------------------------------------------------
		_UAVStatePanel = new GSPanel(new GridLayout(1, 3));
		_imageUAVState = new ImageOnOff(0.5, "/home/adrien/UAV/BreezeWorkspace/breeze.groundstation/img/uav_green.png", "/home/adrien/UAV/BreezeWorkspace/breeze.groundstation/img/uav_red.png");

		labelUAV = new JLabel("UAV");
		labelUAV.setFont(new Font("Arial", Font.BOLD, 14));
		labelUAV.setForeground(new Color(200, 200, 250));
		_UAVStatePanel.add(_imageUAVState);
		_UAVStatePanel.add(labelUAV);

		GridLayout subLayout = new GridLayout(1, 1);
		GSPanel subPanel = new GSPanel(subLayout);
		subPanel.add(_UAVStatePanel);

		Composite composite2 = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		composite2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Frame frame2 = SWT_AWT.new_Frame(composite2);
		frame2.add(subPanel);

		// HUD information display
		//----------------------------------------------------------
		_HUDPanel = new GSPanel(new GridLayout(1, 2));
		HUD_Uav = new HUD(new Color(0, 30, 0, 130));
		FuncGui alt_uav = new FuncGui(GSController.getInstance().getUav(), new Color(0, 30, 0, 130));

		_HUDPanel.add(HUD_Uav);
		_HUDPanel.add(alt_uav);


		// Flight controls (Thrust, Flaps, ..)
		GSPanel flightControlPanel = new GSPanel(new GridLayout(3, 1));

		airspeedVms = new JLabel();
		airspeedVms.setForeground(new Color(250, 250, 250));
		airspeedVms.setFont(new Font("Arial", Font.PLAIN, 13));
		flightControlPanel.add(airspeedVms);

		rectThrustBg = new Rect(new Color(0, 0, 200, 250));
		rectThrustBg.setSize(0, 35);
		labelThrust = new JLabel("Thrust 0%");
		labelThrust.setForeground(new Color(250, 250, 250));
		rectThrustBg.add(labelThrust);
		flightControlPanel.add(rectThrustBg);


		rectFlapsBg = new Rect(new Color(100, 200, 0, 250));
		rectFlapsBg.setSize(0, 35);
		labelFlaps = new JLabel("Flaps 0%");
		labelFlaps.setForeground(new Color(250, 250, 250));
		rectFlapsBg.add(labelFlaps);
		flightControlPanel.add(rectFlapsBg);



		//--------------------------------------------
		// Add main panel 
		GridLayout gridMain = new GridLayout(1, 1);
		GSPanel mainPanel = new GSPanel(gridMain);
		mainPanel.add(_HUDPanel);
		//	mainPanel.add(portStatusPanel);


		Composite composite = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		Frame frame = SWT_AWT.new_Frame(composite);
		frame.add(mainPanel);


		//-------------------------------------------
		// Add flight control panel
		Composite composite3 = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		composite3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Frame frame3 = SWT_AWT.new_Frame(composite3);
		frame3.add(flightControlPanel);



		Display display = parent.getDisplay();
		display.addFilter(SWT.KeyDown, new Listener() {
			@Override
			public void handleEvent(Event e) {
				KeyboardController.getInstance().processPressedKey(e.character);	
			}			
		});
		display.addFilter(SWT.KeyUp, new Listener() {
			@Override
			public void handleEvent(Event e) {
				KeyboardController.getInstance().processReleasedKey(e.character);	
			}			
		});



		comboNavigationMethod= new Combo(parent, SWT.BORDER);
		comboNavigationMethod.add("Navigation method L1");
		comboNavigationMethod.add("Navigation using GPS heading");
		comboNavigationMethod.select(1);
		comboNavigationMethod.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comboNavigationMethod.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				GSController.getInstance().switchNavigationMethod(comboNavigationMethod.getSelectionIndex());

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

				GSController.getInstance().switchNavigationMethod(comboNavigationMethod.getSelectionIndex());
				//
			}
		});



		btnSwitchManualStabilized = new Button(parent, SWT.FLAT);
		btnSwitchManualStabilized.setVisible(true);
		btnSwitchManualStabilized.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnSwitchManualStabilized.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				_gsController.switchManualStabilized();
				setManualStabilized();
			}});

		// Init values
		setManualStabilized();

		Button btnCycleComPort = new Button(parent, SWT.FLAT);
		btnCycleComPort.setText("Cycle COM port");
		btnCycleComPort.setVisible(true);
		btnCycleComPort.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnCycleComPort.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				_portDriver.cyclePort();
			}});



		Button btnSaveLogger = new Button(parent, SWT.FLAT);
		btnSaveLogger.setText("Save logger");
		btnSaveLogger.setVisible(true);
		btnSaveLogger.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnSaveLogger.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				_gsController.saveLogger();
			}});

		Button btnReplayLogger = new Button(parent, SWT.FLAT);
		btnReplayLogger.setText("Replay logger");
		btnReplayLogger.setVisible(true);
		btnReplayLogger.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnReplayLogger.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				_gsController.replayLogger();
			}});



		Button btnShutdownUav = new Button(parent, SWT.FLAT);
		btnShutdownUav.setText("Shutdown UAV");
		btnShutdownUav.setVisible(true);
		btnShutdownUav.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnShutdownUav.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				_gsController.shutdownUavCommand();
			}});



		// Finally, wire controller
		Arduino.getInstance().postConstruct(this, app, service);
	}



	@Override
	public void portStatusChanged() {
		if (_serialPortLabel != null && _portDriver != null) {
			_serialPortLabel.setText("Port currently in use: " + _portDriver.getPortName());
		}
	}


	

	public void setThrustPercent(int throttlePercent) {
		int csize = (int)((throttlePercent/100.0)*250.0);
		rectThrustBg.setSize(csize, 35);
		
		if (GSController.getInstance().getUav().isAutospeed() == false) {
			labelThrust.setText("thrust " + throttlePercent + "%");
		}
		else {
			labelThrust.setText("V m/s goal " + GSController.getInstance().getUav().throttleToVmsGoal() + "m/s");
		}
		//labelThrust.setBounds(500, 125, 100, 30);
		this.rectThrustBg.getParent().repaint();
	}

	public void setFlapsPercent(int flapsPercent) {
		int csize = (int)((flapsPercent/100.0)*250.0);
		rectFlapsBg.setSize(csize, 35);
		labelFlaps.setText("flaps " + flapsPercent + "%");
		this.rectFlapsBg.getParent().repaint();
	}

	public void setAttitude(double pRoll, double pPitch, double pDesiredRoll, double pDesiredPitch) {
		HUD_Uav.setAttitude(pRoll, pPitch, pDesiredRoll, pDesiredPitch);
		repaint();
	}

	@Focus
	public void setFocus() {

	}

	@Persist
	public void save() {
	}


	public void setManualStabilized() {
		if (btnSwitchManualStabilized != null) {
			String str = "stabilized manual";
			if (_gsController.getUav().isManual_StabilizedFlight()) {
				str = "direct control";
			}
			if (btnSwitchManualStabilized != null) {
				btnSwitchManualStabilized.setText("Switch to " + str);	
			}
		}
	}

	public void updateUAVState() {
		_imageUAVState.setState(GSController.getInstance().isUAVConnected());
	}

	public void setAutomode(boolean autoMode) {
		if (autoMode) {
			labelUAV.setText("UAV - auto");
		}
		else {
			labelUAV.setText("UAV - manual");
		}
	}

	public void setUAVData(double pAirspeedVmsValue, int cap, int angleDiffToTarget, int pBytePerSecond) {
		airspeedVms.setText(pAirspeedVmsValue + " m/s \t | \t heading = " + cap + "° \t | \t target=" + angleDiffToTarget + "° | Byte/s=" + pBytePerSecond);
	}

}