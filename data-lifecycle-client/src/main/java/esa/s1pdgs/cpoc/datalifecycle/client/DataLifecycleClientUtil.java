package esa.s1pdgs.cpoc.datalifecycle.client;

import org.apache.commons.io.FilenameUtils;

public class DataLifecycleClientUtil {
	
	public static String getFileName(final String obsKey) {
		return FilenameUtils.getName(obsKey);
	}
	
	public static String getProductName(final String obsKey) {
		if (FilenameUtils.getExtension(obsKey).equalsIgnoreCase("ZIP")) {
			return FilenameUtils.getBaseName(obsKey);
		}else {
			return FilenameUtils.getName(obsKey);
		}
	}

}
