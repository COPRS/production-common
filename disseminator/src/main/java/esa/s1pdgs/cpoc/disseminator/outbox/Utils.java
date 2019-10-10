package esa.s1pdgs.cpoc.disseminator.outbox;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {
	static final InputStream getInputStream(final String _filename) throws Exception {
		final File input = new File(_filename);

		InputStream result = Utils.class.getClassLoader().getResourceAsStream(_filename);

		// not resolvable via classpath --> try to resolve it via filesystem
		if ((result == null) && input.exists()) {			
			result = new BufferedInputStream(new FileInputStream(input));
		}

		// still not resolvable --> error
		if (result == null) {
			throw new IOException("Resource " + _filename + " could not be found");
		}
		return result;
	}
}
