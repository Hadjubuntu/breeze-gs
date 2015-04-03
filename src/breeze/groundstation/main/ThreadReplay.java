package breeze.groundstation.main;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import breeze.groundstation.io.IOManager;

public class ThreadReplay extends Thread {
	public int freqReplay = 1; // 30 Hz
	public int delayMs;
	private boolean replayDone = false;
	private Display display;
	private Shell shell;
	private FileDialog dialog;
	private String filepath;
	private int iStartSearchNearestData = 0;

	public void setVariables() {
		display = Display.getCurrent();
		shell = display.getActiveShell();
		dialog = new FileDialog(shell);

		filepath = dialog.open();
		delayMs = (int)(1.0/freqReplay * 1000);
	}

	public long getTime(String str) {
		String str_array[] = str.split(";");
		return Long.valueOf(str_array[11]);
	}

	public String[] findNearestData(long cTimeUs, ArrayList<String> datas) {
		String dataNearest[] = new String[2];
		int posNearest = 0;
		long dtTimeUs = -1;
		int iter = 0;

		for (int i = iStartSearchNearestData; i < datas.size(); i ++) {
			String data = datas.get(i);
			if (data.startsWith("att")) {
				long timeOfDataUs = getTime(data);

				if (dtTimeUs == -1 || (dtTimeUs > (Math.abs(timeOfDataUs - cTimeUs)) && timeOfDataUs < cTimeUs)) {
					dtTimeUs = Math.abs(timeOfDataUs - cTimeUs);
					posNearest = i;

				}
				else if (dtTimeUs < (Math.abs(timeOfDataUs - cTimeUs))) {
					iter ++;
				}
				else if (iter > 8) {
					break;
				}
			}

		}

		iStartSearchNearestData = posNearest;

		dataNearest[0] = datas.get(posNearest);
		dataNearest[1] = null;

		if (posNearest < datas.size()) {
			dataNearest[1] = datas.get(posNearest+1);
		}


		return dataNearest ;
	}

	public void run() {
		// Get the data
		ArrayList<String> fileData = IOManager.readFile(filepath);


		int i = 0;
		long tInitReplayUs = getTime(fileData.get(0));
		long tEndReplayUs = getTime(fileData.get(fileData.size()-1));
		long tSimulationUs = tInitReplayUs;

		// Replay with Gui
		while (replayDone == false) {

			/**
			 * 
			 * OLD CODE 
			String msgReceived = fileData.get(i);		
			msgReceived = msgReceived.replace(';', '|');
			if (msgReceived.startsWith("att")) {
				GSController.getInstance().logAttitude(msgReceived, false);
			}
			 */

			String nearestData[] = findNearestData(tSimulationUs, fileData);	
			String interpolatedData = interpolation(tSimulationUs, nearestData);
			interpolatedData = interpolatedData.replace(";", "|");

			GSController.getInstance().logAttitude(interpolatedData, false);

			tSimulationUs += delayMs * 1000;

			if (tSimulationUs >= tEndReplayUs) {
				replayDone = true;
			}

			try {
				Thread.sleep(delayMs);
			} catch (InterruptedException e) { }
		}
	}

	private String interpolation(long pTimeUs, String[] nearestData) {
		String outputArray[] = null;

		double dtWithPreviousUs = 0, dtWithNextUs = 0;
		String previousData[] = nearestData[0].split(";");
		String nextData[] = null;

		if (nearestData[1] == null) {
			nextData = nearestData[0].split(";");
		}
		else {
			nextData = nearestData[1].split(";");
		}

		// Copy previous
		outputArray = previousData;

		dtWithPreviousUs = (double) Math.abs(pTimeUs - Long.valueOf(previousData[11]));
		dtWithNextUs = (double) Math.abs(pTimeUs - Long.valueOf(nextData[11]));

		// Activated or desactived for interpolation
		dtWithPreviousUs = 1.0;
		dtWithNextUs = 0.0; // No interpolation
		double totalUs = Math.abs( dtWithPreviousUs + dtWithNextUs);

		for (int k = 1; k < 13; k ++) {
			if (k != 9 && k != 10) {
				double valueInterpolated = 0.0;
				if (totalUs != 0) {
					valueInterpolated = (dtWithNextUs/totalUs*Double.valueOf(previousData[k]) + dtWithPreviousUs/totalUs*Double.valueOf(nextData[k]));
				}

				if (k == 6 || k == 7) {
					long valueInterpolatedLong = (long) valueInterpolated;
					outputArray[k] = String.valueOf(valueInterpolatedLong) ;
				}
				else {
					outputArray[k] = String.valueOf((int)valueInterpolated) ;
				}
			}
		}

		String output = "";
		for (String strPart : outputArray) {
			output += strPart + ";";
		}
		return output;
	}

}
