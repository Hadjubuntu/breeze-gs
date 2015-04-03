package breeze.groundstation.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class IOManager {
	public static void saveData(String filepath, ArrayList<String> data) {
		FileWriter writer;
		try {
			writer = new FileWriter(filepath);

			for(String str: data) {
				str = str.replace('|', ';');
				if (str.endsWith("\n") == false) {
					str += "\r\n";
				}
				writer.write(str);
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	public static ArrayList<String> readFile(String filepath) {
		ArrayList<String> data = new ArrayList<String>();

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(filepath));

			String line;
			while ((line = br.readLine()) != null) {
				data.add(line);
			}

			br.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static String readFileToString(String filepath) {
		String str = "";
		ArrayList<String> data = readFile(filepath);

		for (String s : data) {
			str += s;	
		}

		return str;
	}
}
