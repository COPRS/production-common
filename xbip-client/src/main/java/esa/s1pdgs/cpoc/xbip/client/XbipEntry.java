package esa.s1pdgs.cpoc.xbip.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Date;

public interface XbipEntry {
	String getName();
	Path getPath();
	URI getUri();
	Date getLastModified();
	InputStream read() throws IOException;
}
