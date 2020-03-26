package esa.s1pdgs.cpoc.xbip.client;

import java.net.URI;
import java.nio.file.Path;
import java.util.Date;

public interface XbipEntry {
	String getName();
	Path getPath();
	URI getUri();
	Date getLastModified();
}
