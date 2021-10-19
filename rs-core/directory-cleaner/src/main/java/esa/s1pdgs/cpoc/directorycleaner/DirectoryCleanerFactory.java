package esa.s1pdgs.cpoc.directorycleaner;

import esa.s1pdgs.cpoc.directorycleaner.client.MyOceanFtpDirectoryCleaner;
import esa.s1pdgs.cpoc.directorycleaner.client.MyOceanFtpsDirectoryCleaner;
import esa.s1pdgs.cpoc.directorycleaner.config.DirectoryCleanerProperties;
import esa.s1pdgs.cpoc.directorycleaner.config.DirectoryCleanerProperties.Protocol;

public class DirectoryCleanerFactory {

	public static DirectoryCleaner newDirectoryCleaner(String type, DirectoryCleanerProperties config) {
		if ("myocean".equalsIgnoreCase(type)) {
			return createMyoceanDirectoryCleaner(config);
		}

		throw new IllegalArgumentException("type " + type + " not supported!");
	}

	// --------------------------------------------------------------------------

	private static MyOceanFtpDirectoryCleaner createMyoceanDirectoryCleaner(DirectoryCleanerProperties config) {
		final Protocol protocol = config.getProtocol();

		if (Protocol.FTP.equals(protocol)) {
			return new MyOceanFtpDirectoryCleaner(config);
		} else if (Protocol.FTPS.equals(protocol)) {
			return new MyOceanFtpsDirectoryCleaner(config);
		}

		return null;
	}

}
