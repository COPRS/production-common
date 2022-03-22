package esa.s1pdgs.cpoc.directorycleaner.client;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.common.utils.ArrayUtil;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.directorycleaner.DirectoryCleaner;
import esa.s1pdgs.cpoc.directorycleaner.config.DirectoryCleanerProperties;
import esa.s1pdgs.cpoc.directorycleaner.util.LogPrintWriter;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class MyOceanFtpDirectoryCleaner implements DirectoryCleaner {

	protected final Logger logger = LoggerFactory.getLogger(MyOceanFtpDirectoryCleaner.class);

	private static final int DEFAULT_PORT = 21;

	protected final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	protected final DirectoryCleanerProperties config;

	// --------------------------------------------------------------------------

	public MyOceanFtpDirectoryCleaner(final DirectoryCleanerProperties config) {
		this.config = config;
	}

	// --------------------------------------------------------------------------

	@Override
	public void cleanDirectories() {
		this.logger.info("start cleaning directories on host: " + this.config.getHostname());
		final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.UNDEFINED).newReporting("MyOceanCleaner");
		reporting.begin(new ReportingMessage("Start cleaning directories on host: %s", this.config.getHostname()));

		FTPClient ftpClient = null;
		try {
			ftpClient = this.initFtp();
			this.clean(ftpClient);

		} catch (final Exception e) {
			this.logger.error("error cleaning directories on host " + this.config.getHostname() + ": "
					+ StringUtil.stackTraceToString(e), e);
			reporting.error(new ReportingMessage("Error cleaning directories on host %s: %s", this.config.getHostname(),
					StringUtil.stackTraceToString(e)));
			return;

		} finally {
			if (null != ftpClient) {
				// logout
				try {
					ftpClient.logout();
					assertPositiveCompletion(ftpClient);
				} catch (final IOException e) {
					this.logger.error("error logging out from ftp server: " + StringUtil.stackTraceToString(e), e);
				}
				// disconnect
				try {
					ftpClient.disconnect();
					assertPositiveCompletion(ftpClient);
				} catch (final IOException e) {
					this.logger.error("error disconnecting from ftp server: " + StringUtil.stackTraceToString(e), e);
				}
			}
		}

		this.logger.info("end cleaning directories on host: " + this.config.getHostname());
		reporting.end(new ReportingMessage("End cleaning directories on host: %s", this.config.getHostname()));
	}

	// --------------------------------------------------------------------------

	protected FTPClient initFtp() throws Exception {
		final FTPClient ftpClient = this.connectAndLogin();

		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		ftpClient.setRemoteVerificationEnabled(false);

		if (this.config.isFtpPassiveMode()) {
			this.logger.debug("using passive mode");
			ftpClient.enterLocalPassiveMode();
		} else {
			this.logger.debug("using active mode");
			ftpClient.enterLocalActiveMode();
		}

		assertPositiveCompletion(ftpClient);

		return ftpClient;
	}

	protected FTPClient connectAndLogin() throws Exception {
		final FTPClient ftpClient = new FTPClient();
		ftpClient.addProtocolCommandListener(new PrintCommandListener(new LogPrintWriter(this.logger::debug), true));
		final int port = (this.config.getPort() > 0) ? this.config.getPort() : DEFAULT_PORT;

		// connect
		ftpClient.connect(this.config.getHostname(), port);
		assertPositiveCompletion(ftpClient);

		// login
		if (!ftpClient.login(this.config.getUsername(), this.config.getPassword())) {
			throw new RuntimeException(
					"Could not authenticate user '" + this.config.getUsername() + "' with:" + this.config);
		}

		return ftpClient;
	}

	protected boolean exceedsRetentionTime(final Calendar timestamp) {
		final TimeZone timezone = timestamp.getTimeZone();
		final ZoneId zoneId = timezone != null ? timezone.toZoneId() : ZoneId.systemDefault();
		final LocalDateTime fileTimestamp = LocalDateTime.ofInstant(timestamp.toInstant(), zoneId);
		final LocalDateTime now = LocalDateTime.now(zoneId);

		return fileTimestamp.isBefore(now.minusDays(this.config.getRetentionTimeInDays()));
	}

	protected void clean(final FTPClient ftpClient) throws IOException {
		final String startPath;

		if (StringUtil.isNotBlank(this.config.getPath())) {
			final Path path = Paths.get(this.config.getPath());

			if (null == path) {
				throw new RuntimeException(String.format("Error parsing path %s", this.config.getPath()));
			}
			startPath = path.toString();
		} else {
			startPath = "/";
		}

		this.cleanRecursively(ftpClient, startPath, null);
	}

	protected void cleanRecursively(final FTPClient ftpClient, final String currentDirectoryPath, final FTPFile currentDirectory)
			throws IOException {
		// move down the directory structure (depth-first)
		final FTPFile[] subdirectories = ftpClient.listDirectories(currentDirectoryPath);
		for (final FTPFile subDir : ArrayUtil.nullToEmpty(subdirectories)) {
			final String subDirName = subDir.getName();
			assertNotNull(subDirName, "error obtaining name of sub directory from: " + this.config);
			final Path subDirPath = Paths.get(currentDirectoryPath, subDirName);
			assertNotNull(subDirPath, "error assembling path for sub directory " + subDirName + " for: " + this.config);

			// dig deeper
			this.cleanRecursively(ftpClient, subDirPath.toString(), subDir);
		}

		this.deleteOldFilesFromDirectory(ftpClient, currentDirectoryPath);
		this.deleteOldAndEmptyDirectory(ftpClient, currentDirectoryPath, currentDirectory);
	}

	protected void deleteOldFilesFromDirectory(final FTPClient ftpClient, final String directoryPath) throws IOException {
		final FTPFile[] files = ftpClient.listFiles(directoryPath, FTPFile::isFile);

		for (final FTPFile file : ArrayUtil.nullToEmpty(files)) {
			final Calendar timestamp = file.getTimestamp();

			if (null == timestamp) {
				this.logger.info( String.format("omitting file removal check because couldn't obtain timestamp from file %s: %s",
						file.getName(), this.config));
				continue;
			}

			if (this.exceedsRetentionTime(timestamp)) {
				this.dateFormat.setTimeZone(timestamp.getTimeZone());
				this.logger.debug("Attempting to delete file %s because timestamp %s exceeds retention time of %s days: %s",
						file.getName(), this.dateFormat.format(timestamp.getTime()), this.config.getRetentionTimeInDays(),
						this.config);
				final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.UNDEFINED).newReporting("MyOceanCleanerFileRemoval");
				reporting.begin(new ReportingMessage(
						"Attempting to delete file %s because timestamp %s exceeds retention time of %s days: %s",
						file.getName(), this.dateFormat.format(timestamp.getTime()), this.config.getRetentionTimeInDays(),
						this.config));

				final String fileName = file.getName();
				if (null == fileName) {
					this.logger.warn("Couldn't obtain filename of file to delete in directory " + directoryPath + ": " + this.config);
					reporting.error(new ReportingMessage("Couldn't obtain filename of file to delete in directory %s: %s", directoryPath, this.config));
					continue;
				}
				final Path filePath = Paths.get(directoryPath, fileName);
				if (null == filePath) {
					this.logger.warn("Couldn't assemble path for file to delete " + fileName + ": " + this.config);
					reporting.error(new ReportingMessage("Couldn't assemble path for file %s in directory %s for deletion:", fileName, directoryPath, this.config));
					continue;
				}

				boolean successfullyDeleted = false;
				try {
					successfullyDeleted = ftpClient.deleteFile(filePath.toString());
				} catch (final IOException e) {
					this.logger.warn("Error on deleting file %s on %s: %s", filePath, this.config, StringUtil.stackTraceToString(e));
					reporting.error(new ReportingMessage("Error on deleting file %s on %s: %s", filePath, this.config, StringUtil.stackTraceToString(e)));
					continue;
				}

				if (successfullyDeleted) {
					this.logger.info("File %s successfully deleted on: %s", filePath, this.config);
					reporting.end(new ReportingMessage("File %s successfully deleted on: %s", filePath, this.config));
				} else {
					this.logger.warn("File %s couldn't be deleted on: %s", filePath, this.config);
					reporting.error(new ReportingMessage("File %s couldn't be deleted on: %s", filePath, this.config));
				}
			}
		}
	}

	protected void deleteOldAndEmptyDirectory(final FTPClient ftpClient, final String directoryPath, final FTPFile currentDirectory)
			throws IOException {
		if (null == currentDirectory) {
			return;
		}

		final Calendar timestamp = currentDirectory.getTimestamp();
		if (null == timestamp) {
			this.logger.info(String.format(
					"omitting directory removal check because wasn't able to obtain timestamp from directory %s: %s",
					directoryPath, this.config));
			return;
		}

		if (this.exceedsRetentionTime(timestamp) //
				&& ArrayUtil.isEmpty(ftpClient.listDirectories(directoryPath))
				&& ArrayUtil.isEmpty(ftpClient.listFiles(directoryPath))) {
			try {
				Thread.sleep(1111); // just for safety ...
			} catch (final InterruptedException e) {}
			if (ArrayUtil.isEmpty(ftpClient.listDirectories(directoryPath))
					&& ArrayUtil.isEmpty(ftpClient.listFiles(directoryPath))) {
				this.logger.debug("removing directory %s from: %s", directoryPath, this.config);
				ftpClient.removeDirectory(directoryPath);
			}
		}
	}

	// --------------------------------------------------------------------------

	protected static void assertPositiveCompletion(final FTPClient client) throws IOException {
		if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
			throw new IOException("Error on command execution. Reply was: " + client.getReplyString());
		}
	}

	protected static void assertNotNull(final String str, final String errorMessage) {
		if (null == str) {
			throw new RuntimeException(errorMessage);
		}
	}

	protected static void assertNotNull(final Path path, final String errorMessage) {
		if (null == path) {
			throw new RuntimeException(errorMessage);
		}
		assertNotNull(path.toString(), errorMessage);
	}

}
