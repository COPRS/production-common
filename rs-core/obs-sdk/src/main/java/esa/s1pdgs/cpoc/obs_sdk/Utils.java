package esa.s1pdgs.cpoc.obs_sdk;

import java.io.InputStream;

import com.amazonaws.util.IOUtils;

public class Utils {	
	public static final void closeQuietly(Iterable<InputStream> streams) {
		for (final InputStream in : streams) {	
			IOUtils.drainInputStream(in);
			IOUtils.closeQuietly(in, null);		
		}
	}
}
