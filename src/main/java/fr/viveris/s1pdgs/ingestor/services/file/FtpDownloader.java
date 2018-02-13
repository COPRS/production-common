package fr.viveris.s1pdgs.ingestor.services.file;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.ingestor.config.file.FtpConfiguration.FtpGateway;

/**
 * Service which download file from FTP server to a local directory
 * @author Cyrielle Gailliard
 *
 */
@Service
public class FtpDownloader {
	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(FtpDownloader.class);

	/**
	 * FTP gateway
	 */
	@Autowired
	private FtpGateway ftpGateway;
	
	/**
	 * Remote directory for configuration files
	 */
	@Value("${ftp.config-files.remote-directory}")
	private String remoteDirectoryConfigFiles;
	
	/**
	 * Remote directory for ERDS session files
	 */
	@Value("${ftp.session-files.remote-directory}")
	private String remoteDirectorySessionFiles;

	/**
	 * Task to periodically download configuration files
	 */
	@Scheduled(fixedRateString = "${ftp.config-files.upload-fixed-rate}")
	public void downloadConfigFiles() {
		List<File> files = ftpGateway.fetchConfigFiles(remoteDirectoryConfigFiles);
		for (File file : files) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Download file " + file.getAbsolutePath() + " / " + file.getName());
			}
		}
	}

	/**
	 * Task to periodically download ERDS session files
	 */
	@Scheduled(fixedRateString = "${ftp.session-files.upload-fixed-rate}")
	public void downloadSessionsChannelFiles() {
		List<File> files = ftpGateway.fetchSessionFiles(remoteDirectorySessionFiles);
		for (File file : files) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Download file " + file.getAbsolutePath() + " / " + file.getName());
			}
		}
	}
}
