package esa.s1pdgs.cpoc.ebip.client;

import java.net.URI;
import java.nio.file.Path;
import java.util.Date;

public interface EdipEntry {
	String getName();
	Path getPath();
	long getSize();
	URI getUri();
	Date getLastModified();
}
