package esa.s1pdgs.cpoc.outbox.cleaner.config;

public class FtpClientConfig {
	public enum Protocol {
		FTP, FTPS;

		public static Protocol fromString(String protocol) {
			if (FTP.name().equalsIgnoreCase(protocol)) {
				return FTP;
			}
			if (FTPS.name().equalsIgnoreCase(protocol)) {
				return FTPS;
			}

			throw new IllegalArgumentException(String.format("protocol not supported: %s", protocol));
		}
	}

	private String hostname = null;
	private Protocol protocol = Protocol.FTP;
	private int port = -1; // --> not defined
	private boolean implicitSsl = true;
	private boolean ftpPassiveMode = false;
	private String username = null;
	private String passw = null;
	private String path = null;

	private String keyFile = null;
	private String keystoreFile = null;
	private String keystorePass = "changeit";

	// per default, use java keystore and password
	private String truststoreFile = System.getProperty("java.home") + "/lib/security/cacerts";
	private String truststorePass = "changeit";

	// --------------------------------------------------------------------------

	@Override
	public String toString() {
		return "FtpConfig [protocol=" + this.protocol + ", path=" + this.path + ", username=" + this.username
				+ ", passw=<NOT_SHOWN>, keyFile=" + this.keyFile + ", hostname=" + this.hostname + ", port=" + this.port
				+ ", ftpPassiveMode=" + this.ftpPassiveMode + ", keystoreFile=" + this.keystoreFile
				+ ", keystorePass=<NOT_SHOWN>, truststoreFile=" + this.truststoreFile
				+ ", truststorePass=<NOT_SHOWN>, implicitSsl=" + this.implicitSsl + "]";
	}

	// --------------------------------------------------------------------------

	public Protocol getProtocol() {
		return this.protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassw() {
		return this.passw;
	}

	public void setPassw(String passw) {
		this.passw = passw;
	}

	public String getKeyFile() {
		return this.keyFile;
	}

	public void setKeyFile(String keyFile) {
		this.keyFile = keyFile;
	}

	public String getHostname() {
		return this.hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getKeystoreFile() {
		return this.keystoreFile;
	}

	public void setKeystoreFile(String keystoreFile) {
		this.keystoreFile = keystoreFile;
	}

	public String getKeystorePass() {
		return this.keystorePass;
	}

	public void setKeystorePass(String keystorePass) {
		this.keystorePass = keystorePass;
	}

	public String getTruststoreFile() {
		return this.truststoreFile;
	}

	public void setTruststoreFile(String truststoreFile) {
		this.truststoreFile = truststoreFile;
	}

	public String getTruststorePass() {
		return this.truststorePass;
	}

	public void setTruststorePass(String truststorePass) {
		this.truststorePass = truststorePass;
	}

	public boolean isImplicitSsl() {
		return this.implicitSsl;
	}

	public void setImplicitSsl(boolean implicitSsl) {
		this.implicitSsl = implicitSsl;
	}

	public boolean isFtpPassiveMode() {
		return this.ftpPassiveMode;
	}

	public void setFtpPassiveMode(boolean ftpPassiveMode) {
		this.ftpPassiveMode = ftpPassiveMode;
	}
}
