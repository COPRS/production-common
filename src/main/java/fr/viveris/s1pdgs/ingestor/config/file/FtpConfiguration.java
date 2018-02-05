package fr.viveris.s1pdgs.ingestor.config.file;

import java.io.File;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.file.filters.AcceptAllFileListFilter;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.ftp.gateway.FtpOutboundGateway;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;

/**
 * Configuration class which defined the FTP gateway to recursively fetch the
 * content on a FTP server for the remote ERDS session files and configuration
 * files
 * 
 * @author Cyrielle Gailliard
 *
 */
@Configuration
@EnableIntegration
public class FtpConfiguration {

	// FTP server configuration
	// -------------------------------------

	/**
	 * FTP server hostname
	 */
	@Value("${ftp.host}")
	private String ftpHost;
	/**
	 * FTP server port
	 */
	@Value("${ftp.port}")
	private int ftpPort;
	/**
	 * FTP server username
	 */
	@Value("${ftp.username}")
	private String ftpUsername;
	/**
	 * FTP server user password
	 */
	@Value("${ftp.password}")
	private String ftpPassword;

	// FTP client configuration
	// -------------------------------------
	/**
	 * FTP client mode
	 * 
	 * @see FTPClient
	 */
	@Value("${ftp.client-mode}")
	private int ftpClientMode;

	// Gateway configuration
	// -------------------------------------
	/**
	 * Local directory for remote files
	 */
	@Value("${ftp.local-directory}")
	private String ftpLocalDirectory;
	/**
	 * MGET command options
	 */
	@Value("${ftp.command-options.mget}")
	private String ftpCommandOptionsMget;

	/**
	 * FTP session factory to defined the FTP server and client configuration
	 * 
	 * @return
	 */
	@Bean
	public SessionFactory<FTPFile> ftpSessionFactory() {
		DefaultFtpSessionFactory sf = new DefaultFtpSessionFactory();
		sf.setHost(ftpHost);
		sf.setPort(ftpPort);
		sf.setUsername(ftpUsername);
		sf.setPassword(ftpPassword);
		sf.setClientMode(ftpClientMode);
		return new CachingSessionFactory<FTPFile>(sf);
	}

	/**
	 * FTP gateway to fetch files. One channel per type of files is used
	 * 
	 * @author Cyrielle
	 *
	 */
	@MessagingGateway(name = "ftpGateway")
	public interface FtpGateway {

		@Gateway(requestChannel = "fetchConfigRecursive")
		public List<File> fetchConfigFiles(String dir);

		@Gateway(requestChannel = "fetchSessionRecursive")
		public List<File> fetchSessionFiles(String dir);
	}

	/**
	 * FTP Outbound gateway which retrieve configuration files via the MGET command
	 * and send files in the channel fetchConfigRecursive
	 * 
	 * @return
	 */
	@Bean
	@ServiceActivator(inputChannel = "fetchConfigRecursive")
	public FtpOutboundGateway gatewayConfig() {
		FtpOutboundGateway ftpOutboundGateway = new FtpOutboundGateway(ftpSessionFactory(), "mget", "payload");
		ftpOutboundGateway.setOptions(ftpCommandOptionsMget);
		ftpOutboundGateway.setFilter(new AcceptAllFileListFilter<>());
		ftpOutboundGateway.setAutoCreateLocalDirectory(true);
		ftpOutboundGateway.setFileExistsMode(FileExistsMode.IGNORE);
		ftpOutboundGateway.setLocalDirectoryExpression(
				new SpelExpressionParser().parseExpression("'" + ftpLocalDirectory + "' + #remoteDirectory"));
		return ftpOutboundGateway;
	}

	/**
	 * FTP Outbound gateway which retrieve ERDS session files via the MGET command
	 * and send files in the channel fetchConfigRecursive
	 * 
	 * @return
	 */
	@Bean
	@ServiceActivator(inputChannel = "fetchSessionRecursive")
	public FtpOutboundGateway gatewaySession() {
		FtpOutboundGateway ftpOutboundGateway = new FtpOutboundGateway(ftpSessionFactory(), "mget", "payload");
		ftpOutboundGateway.setOptions(ftpCommandOptionsMget);
		ftpOutboundGateway.setFilter(new AcceptAllFileListFilter<>());
		ftpOutboundGateway.setAutoCreateLocalDirectory(true);
		ftpOutboundGateway.setFileExistsMode(FileExistsMode.IGNORE);
		ftpOutboundGateway.setLocalDirectoryExpression(
				new SpelExpressionParser().parseExpression("'" + ftpLocalDirectory + "' + #remoteDirectory"));
		return ftpOutboundGateway;
	}
}
