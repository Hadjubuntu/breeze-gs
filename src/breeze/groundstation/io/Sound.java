package breeze.groundstation.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import javazoom.jl.player.Player;

public class Sound {
	private static Sound INSTANCE;

	private boolean SoundOnOff = true;
	private static String SOUND_PATTERN = "/home/adrien/UAV/BreezeWorkspace/breeze.groundstation/sounds/{NAME}.mp3";


	private static Player player = null; 

	public Sound() {

	}	

	public static Sound getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Sound();
		}

		return INSTANCE;
	}

	public boolean isOn() {
		return SoundOnOff;
	}

	public void play(String nameType) { 
		if (SoundOnOff == false) {
			return;
		}

		String filename = SOUND_PATTERN;
		filename = filename.replace("{NAME}", nameType);

		File soundFile = new File(filename) ;
		if (soundFile.exists()) {

			try {
				FileInputStream fis     = new FileInputStream(filename);
				BufferedInputStream bis = new BufferedInputStream(fis);
				player = new Player(bis);
			}
			catch (Exception e) {
				System.out.println("Problem playing file " + filename);
				System.out.println(e);
			}

			// run in new thread to play in background
			new Thread() {
				public void run() {
					try { player.play(); }
					catch (Exception e) { System.out.println(e); }
				}
			}.start();
		}
		else {
			System.out.println("Sound file doesn\'t exists.");
		}
	}

	public void switchState() {
		SoundOnOff = !SoundOnOff;
	}
}
