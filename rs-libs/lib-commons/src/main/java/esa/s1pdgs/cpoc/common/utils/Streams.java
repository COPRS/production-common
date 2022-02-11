package esa.s1pdgs.cpoc.common.utils;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Streams {
	public static final InputStream getInputStream(final String _filename) {
		final File input = new File(_filename);

		InputStream result = Streams.class.getClassLoader().getResourceAsStream(_filename);

		// not resolvable via classpath --> try to resolve it via filesystem
		if ((result == null) && input.exists()) {
			try {
				result = new BufferedInputStream(new FileInputStream(input));
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		// still not resolvable --> error
		if (result == null) {
			throw new RuntimeException("Resource " + _filename + " could not be found");
		}
		return result;
	}

	public static void close(final Iterable<? extends Closeable> objects) {
		for (final Closeable closable : objects) {
			close(closable);
		}		
	}
	
	public static final void close(final Closeable closable) {
		try {
			closable.close();
		} catch (final IOException e) {
			// ignore exception on close
		}
	}
}
