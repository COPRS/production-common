package esa.s1pdgs.cpoc.directorycleaner;

import esa.s1pdgs.cpoc.directorycleaner.client.MyOceanFtpDirectoryCleaner;
import esa.s1pdgs.cpoc.directorycleaner.client.MyOceanFtpsDirectoryCleaner;
import esa.s1pdgs.cpoc.directorycleaner.config.FtpClientConfig;
import esa.s1pdgs.cpoc.directorycleaner.config.FtpClientConfig.Protocol;

public class DirectoryCleanerFactory {

	private static final String ENV_VAR_PROTOCOL = "MYO_CLEAN_PROTOCOL";
	private static final String ENV_VAR_HOSTNAME = "MYO_CLEAN_HOSTNAME";
	private static final String ENV_VAR_PORT = "MYO_CLEAN_PORT";
	private static final String ENV_VAR_USER = "MYO_CLEAN_USER";
	private static final String ENV_VAR_PASS = "MYO_CLEAN_PASS";
	private static final String ENV_VAR_PATH = "MYO_CLEAN_PATH";
	private static final String ENV_VAR_PASSIVE_FTP = "MYO_CLEAN_PASSIVE_FTP";

	private static final String ENV_VAR_IMPLICIT_SSL = "MYO_CLEAN_IMPLICIT_SSL";
	private static final String ENV_VAR_SSL_KEYFILE = "MYO_CLEAN_SSL_KEYFILE";
	private static final String ENV_VAR_KEYSTORE_FILE = "MYO_CLEAN_KEYSTORE_FILE";
	private static final String ENV_VAR_KEYSTORE_PASS = "MYO_CLEAN_KEYSTORE_PASS";

	private static final String ENV_VAR_TRUSTSTORE_FILE = "MYO_CLEAN_TRUSTSTORE_FILE";
	private static final String ENV_VAR_TRUSTSTORE_PASS = "MYO_CLEAN_TRUSTSTORE_PASS";

	private static final String ENV_VAR_RETENTION_TIME_DAYS = "MYO_CLEAN_RETENTION_DAYS";

	public static DirectoryCleaner newDirectoryCleaner(String type) {
		if ("myocean".equalsIgnoreCase(type)) {
			return createMyoceanDirectoryCleaner();
		}

		throw new IllegalArgumentException("type " + type + " not supported!");
	}

	// --------------------------------------------------------------------------

	private static MyOceanFtpDirectoryCleaner createMyoceanDirectoryCleaner() {
		final Protocol protocol = geProtocol();
		final int retentionTime = getRetentionTime();

		if (Protocol.FTP.equals(protocol)) {
			return new MyOceanFtpDirectoryCleaner(assembleFtpConfig(), retentionTime);
		} else if (Protocol.FTPS.equals(protocol)) {
			return new MyOceanFtpsDirectoryCleaner(assembleFtpsConfig(), retentionTime);
		}

		return null;
	}

	private static FtpClientConfig assembleFtpConfig() {
		return assembleFtpConfig(new FtpClientConfig());
	}

	private static FtpClientConfig assembleFtpConfig(FtpClientConfig ftpConfig) {
		ftpConfig.setProtocol(geProtocol());
		ftpConfig.setHostname(getMandatoryValueFromEnvironment(ENV_VAR_HOSTNAME));
		ftpConfig.setPort(Integer.valueOf(getValueFromEnvironmentOrDefault(ENV_VAR_PORT, "-1")));
		ftpConfig.setUsername(getValueFromEnvironmentOrDefault(ENV_VAR_USER, "s1pdgs"));
		ftpConfig.setPassw(getValueFromEnvironmentOrDefault(ENV_VAR_PASS, null));
		ftpConfig.setPath(getValueFromEnvironmentOrDefault(ENV_VAR_PATH, "public"));
		ftpConfig.setFtpPassiveMode(parseBoolean(getValueFromEnvironmentOrDefault(ENV_VAR_PASSIVE_FTP, "false")));

		return ftpConfig;
	}

	private static FtpClientConfig assembleFtpsConfig() {
		final FtpClientConfig ftpConfig = assembleFtpConfig();

		ftpConfig.setImplicitSsl(parseBoolean(getValueFromEnvironmentOrDefault(ENV_VAR_IMPLICIT_SSL, "true")));
		ftpConfig.setKeyFile(getValueFromEnvironmentOrDefault(ENV_VAR_SSL_KEYFILE, null));
		ftpConfig.setKeystoreFile(getValueFromEnvironmentOrDefault(ENV_VAR_KEYSTORE_FILE, null));
		ftpConfig.setKeystorePass(getValueFromEnvironmentOrDefault(ENV_VAR_KEYSTORE_PASS, "changeit"));
		ftpConfig.setTruststoreFile(getValueFromEnvironmentOrDefault(ENV_VAR_TRUSTSTORE_FILE,
				System.getProperty("java.home") + "/lib/security/cacerts"));
		ftpConfig.setTruststorePass(getValueFromEnvironmentOrDefault(ENV_VAR_TRUSTSTORE_PASS, "changeit"));

		return ftpConfig;
	}

	private static Protocol geProtocol() {
		return Protocol.fromString(getMandatoryValueFromEnvironment(ENV_VAR_PROTOCOL));
	}

	private static int getRetentionTime() {
		return Integer.valueOf(getValueFromEnvironmentOrDefault(ENV_VAR_RETENTION_TIME_DAYS, "7"));
	}

	private static String getMandatoryValueFromEnvironment(String varName) {
		final String valueStr = System.getenv(varName);

		if (null != valueStr) {
			return valueStr;
		}

		throw new IllegalArgumentException("unsupported value for environment variable " + varName + ": " + valueStr);
	}

	private static String getValueFromEnvironmentOrDefault(String varName, String defaultValue) {
		final String valueStr = System.getenv(varName);

		if (null != valueStr) {
			return valueStr;
		}

		return defaultValue;
	}

	private static boolean parseBoolean(String booleanStr) {
		if (null != booleanStr && !booleanStr.isEmpty()) {
			if ("true".equalsIgnoreCase(booleanStr) || "yes".equalsIgnoreCase(booleanStr)
					|| "on".equalsIgnoreCase(booleanStr) || "1".equals(booleanStr)) {
				return true;
			} else if ("false".equalsIgnoreCase(booleanStr) || "no".equalsIgnoreCase(booleanStr)
					|| "off".equalsIgnoreCase(booleanStr) || "0".equals(booleanStr)) {
				return false;
			}
		}

		throw new IllegalArgumentException("value cannot be parsed to boolean: " + booleanStr);
	}

}
