package esa.s1pdgs.cpoc.xbip.client;

import java.io.IOException;
import java.util.List;

public interface XbipClient {	
	List<XbipEntry> list(XbipEntryFilter filter) throws IOException;
}
