package fr.viveris.s1pdgs.archives.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Viveris TEchnologies
 */
public class FileUtils {

    /**
     * @param fileToComplete
     * @param data
     * @throws IOException
     */
    public static void writeFile(final File fileToComplete, final String data)
            throws IOException {
        final FileWriter fileWriter = new FileWriter(fileToComplete);
        final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        try {
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
