package esa.s1pdgs.cpoc.ipf.execution.worker.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;

public class SystemUtils {

    public static String getCmdMkdir() {
        boolean isWindows = System.getProperty("os.name").toLowerCase()
                .startsWith("windows");
        String command = "mkdir";
        if (isWindows) {
            command = "C:\\Program Files\\Git\\usr\\bin\\mkdir.exe";
        }
        return command;
    }

    public static String getCmdRmdir() {
        boolean isWindows = System.getProperty("os.name").toLowerCase()
                .startsWith("windows");
        String command = "rmdir";
        if (isWindows) {
            command = "C:\\Program Files\\Git\\usr\\bin\\rmdir.exe";
        }
        return command;
    }

    public static String getCmdLs() {
        boolean isWindows = System.getProperty("os.name").toLowerCase()
                .startsWith("windows");
        String command = "ls";
        if (isWindows) {
            command = "C:\\Program Files\\Git\\usr\\bin\\ls.exe";
        }
        return command;
    }

    public static String getCmdFalse() {
        boolean isWindows = System.getProperty("os.name").toLowerCase()
                .startsWith("windows");
        String command = "false";
        if (isWindows) {
            command = "C:\\Program Files\\Git\\usr\\bin\\false.exe";
        }
        return command;
    }

    public static String getCmdTrue() {
        boolean isWindows = System.getProperty("os.name").toLowerCase()
                .startsWith("windows");
        String command = "true";
        if (isWindows) {
            command = "C:\\Program Files\\Git\\usr\\bin\\true.exe";
        }
        return command;
    }

    public static String getCmdSleep() {
        boolean isWindows = System.getProperty("os.name").toLowerCase()
                .startsWith("windows");
        String command = "sleep";
        if (isWindows) {
            command = "C:\\Program Files\\Git\\usr\\bin\\sleep.exe";
        }
        return command;
    }
    
    
	public static final InputStream getInputStream(final String _filename) throws IOException {
		final File input = new File(_filename);

		InputStream result = SystemUtils.class.getClassLoader().getResourceAsStream(_filename);

		// not resolvable via classpath --> try to resolve it via filesystem
		if ((result == null) && input.exists()) {
			result = newFileInputStream(input);
		}

		// still not resolvable --> error
		if (result == null) {
			throw new IOException("Resource " + _filename + " could not be found");
		}
		return result;
	}
	
	public static final InputStream newFileInputStream(final File _in) throws IOException {
		return new BufferedInputStream(new FileInputStream(_in));
	}
	
	public static final OutputStream newFileOutputStream(final File _in) throws IOException {
		return new BufferedOutputStream(new FileOutputStream(_in));
	}
}
