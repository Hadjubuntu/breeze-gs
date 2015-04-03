package breeze.groundstation.parts;

import javax.annotation.PostConstruct;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import breeze.groundstation.io.Sound;
import breeze.groundstation.main.GSController;
import breeze.groundstation.main.GSParameters;


public class ConfigurationPart {

	private static String parametersFields[] = {"Ground navigation Kp", "Stabilization G_Tau", 
		"Roll Kp gain", "Roll Kd", "Pitch Kp gain", "Pitch Kd",
		"Autothrust K", "Autothrust Kp", "Autothrust Kd", "Autothrust Ki", "Autothrust max sum error"};
	public  int MAX_PARAMETERS_ON_BOARD =  500;
	private GridData gridData;
	private double parameters[];
	private Label labelParam[];
	private Text textValue[];
	
	private Button soundOn;

	public ConfigurationPart() {
		parameters = new double[MAX_PARAMETERS_ON_BOARD];
		labelParam = new Label[MAX_PARAMETERS_ON_BOARD];
		textValue = new Text[MAX_PARAMETERS_ON_BOARD];
	}

	public void createParameterField(Composite parent, final int pos, String label, double value, boolean hasValue) {
		labelParam[pos] = new Label(parent, SWT.NONE);
		labelParam[pos].setText(label);
		labelParam[pos].setLayoutData(gridData);
		textValue[pos] = new Text(parent, SWT.NONE);
		if (hasValue) {
			textValue[pos].setText(String.valueOf(value));
		}
		else {
			textValue[pos].setText("_");
		}
		textValue[pos].addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent arg0) {
				textValue[pos].setBackground(new Color(Display.getCurrent(), 81,86,88));				
			}
		});
	}

	@PostConstruct
	public void createControls(Composite parent) {
		GridData gridDataFill = new GridData(GridData.FILL_BOTH);
		
		parent.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));
		GridLayout gridLayout = new GridLayout(1, true);
		GridLayout gridLayout2 = new GridLayout(2, true);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		

		Group gsUAVConfigurationGroup = new Group(parent, SWT.PUSH);
		gsUAVConfigurationGroup.setText("UAV configuration");
		gsUAVConfigurationGroup.setLayoutData(gridDataFill);

		Button btnRefresh = new Button(gsUAVConfigurationGroup, SWT.PUSH);
		btnRefresh.setText("Update (From UAV to GS)");
		btnRefresh.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				GSController.getInstance().requestConfigurationFromUAV();
			}});

		Button btnCommit = new Button(gsUAVConfigurationGroup, SWT.PUSH);
		btnCommit.setText("Commit (From GS to UAV)");
		btnCommit.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				startThreadCommitConf();
			}
		});

		int i = 0;
		for (String field : parametersFields) {
			createParameterField(gsUAVConfigurationGroup, i, field, 0.0, false);
			i ++;
		}

		Group gsConfigurationGroup = new Group(parent, SWT.PUSH);
		gsConfigurationGroup.setText("Ground Station configuration");
		gsConfigurationGroup.setLayoutData(gridDataFill);
		
		Label labelMaxRoll = new Label(gsConfigurationGroup, SWT.NONE);
		labelMaxRoll.setText("Max abs roll (deg)");
		labelMaxRoll.setLayoutData(gridData);
		final Spinner spinnerMaxAbsRoll = new Spinner(gsConfigurationGroup, SWT.NONE);
		spinnerMaxAbsRoll.setMinimum(10);
		spinnerMaxAbsRoll.setMaximum(180);
		spinnerMaxAbsRoll.setSelection((int)(GSParameters.getInstance().getMaxCentiRoll()/100));
		spinnerMaxAbsRoll.addModifyListener(new ModifyListener() {			
			@Override
			public void modifyText(ModifyEvent e) {				
				if (spinnerMaxAbsRoll.getSelection() >= 10 && spinnerMaxAbsRoll.getSelection() <= 180) {
					GSParameters.getInstance().setMaxCentiRoll(spinnerMaxAbsRoll.getSelection());
				}
			}
		});
		
		Label labelMaxPitch = new Label(gsConfigurationGroup, SWT.NONE);
		labelMaxPitch.setText("Max abs pitch (deg)");
		labelMaxPitch.setLayoutData(gridData);
		final Spinner spinnerMaxAbsPitch = new Spinner(gsConfigurationGroup, SWT.NONE);
		spinnerMaxAbsPitch.setMinimum(10);
		spinnerMaxAbsPitch.setMaximum(180);
		spinnerMaxAbsPitch.setSelection((int)(GSParameters.getInstance().getMaxCentiPitch()/100));
		spinnerMaxAbsPitch.addModifyListener(new ModifyListener() {			
			@Override
			public void modifyText(ModifyEvent e) {				
				if (spinnerMaxAbsPitch.getSelection() >= 10 && spinnerMaxAbsPitch.getSelection() <= 180) {
					GSParameters.getInstance().setMaxCentiPitch(spinnerMaxAbsPitch.getSelection());
				}
			}
		});
		
		Label labelSoundOnOff = new Label(gsConfigurationGroup, SWT.NONE);
		labelSoundOnOff.setText("Sound on GS");
		labelSoundOnOff.setLayoutData(gridData);
		soundOn = new Button(gsConfigurationGroup, SWT.NONE);
		updateSoundButton();
		soundOn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				switchSoundState();
				updateSoundButton();
			}		
		});
		
		// Update Layout for groups
		GridLayoutFactory.createFrom(gridLayout).generateLayout(parent);
		GridLayoutFactory.createFrom(gridLayout2).generateLayout(gsUAVConfigurationGroup);
		GridLayoutFactory.createFrom(gridLayout2).generateLayout(gsConfigurationGroup);

		// Post construct by registering this part to the controller
		GSController.getInstance().registerConfigurationPart(this);
	}	
	
	private void switchSoundState() {
		Sound.getInstance().switchState();
	}

	public void updateSoundButton() {
		if (Sound.getInstance().isOn()) {
			soundOn.setText("On");
		} else {
			soundOn.setText("Off");
		}
	}

	// Create a new thread to send configuration
	// at 2Hz, each 500 ms send the next parameter (id, value)
	// to the UAV
	private void startThreadCommitConf() {

		// SerialPortDriver serialPort = GSController.getInstance().getSerialPort();

		Thread commitConfiguration = new Thread() {
			public void run() {
				final int n = parametersFields.length;

				// Firstly set parameters value from GUI input
				Display.getDefault().asyncExec(new Runnable() {

					public void run() {

						for (int i = 0; i < n; i ++) {
							parameters[i] = Double.valueOf(textValue[i].getText());
						}
					}
				});

				// Then send it sequencelly

				for (int i = 0; i < n; i ++) {
					GSController.getInstance().sendConfParameter(i, parameters[i]);
					guiNotifyUpdateParameter(i);

					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}


		};

		commitConfiguration.start();
	}

	private void guiNotifyUpdateParameter(final int i) {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				Device device = Display.getCurrent ();
				textValue[i].setBackground(new Color(device, 100, 100, 250));		
			}});
	}
	/**
	 * Update GUI with parameter's value
	 * 
	 * @param id ID of the parameter
	 */
	private void updateField(final int id) {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				textValue[id].setText(String.valueOf(parameters[id]));
			}
		});
	}

	/**
	 * Called from RF function to map ID to parameter's value
	 * 
	 * @param inputString RF line
	 */
	public void setParameter(String inputString) {
		String datas[] = inputString.split("\\|");
		int id = Integer.valueOf(datas[1]);
		double value = Long.valueOf(datas[2])/10000.0;
		if (id < MAX_PARAMETERS_ON_BOARD) {
			parameters[id] = value;
			updateField(id);
		}
	}

}
