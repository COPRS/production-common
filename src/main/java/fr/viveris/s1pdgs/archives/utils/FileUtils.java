package fr.viveris.s1pdgs.archives.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {

	public static void writeFile(File fileToComplete, String data) throws IOException {
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		try {
			fileWriter = new FileWriter(fileToComplete);
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(data);
			bufferedWriter.flush();
			bufferedWriter.close();
			fileWriter.close();
		} finally {
			if (fileWriter != null) {
				fileWriter.close();
			}
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
		}
	}

}
