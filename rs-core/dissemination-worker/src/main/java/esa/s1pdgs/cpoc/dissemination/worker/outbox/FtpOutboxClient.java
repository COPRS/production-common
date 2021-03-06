package esa.s1pdgs.cpoc.dissemination.worker.outbox;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import esa.s1pdgs.cpoc.dissemination.worker.config.DisseminationWorkerProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.dissemination.worker.path.PathEvaluator;
import esa.s1pdgs.cpoc.dissemination.worker.util.LogPrintWriter;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class FtpOutboxClient extends AbstractOutboxClient {

	private static final int DEFAULT_PORT = 21;

	// --------------------------------------------------------------------------

	public static final class Factory implements OutboxClient.Factory {
		@Override
		public OutboxClient newClient(final ObsClient obsClient, final OutboxConfiguration config, final PathEvaluator pathEvaluator) {
			return new FtpOutboxClient(obsClient, config, pathEvaluator);
		}
	}

	public FtpOutboxClient(final ObsClient obsClient, final OutboxConfiguration config, final PathEvaluator pathEvaluator) {
		super(obsClient, config, pathEvaluator);
	}

	// --------------------------------------------------------------------------

	@Override
	protected final Path evaluatePathFor(final ObsObject mainFile) {
		return this.pathEvaluator.outputPath(this.config.getPath(), mainFile);
	}

	@Override
	public String transfer(final ObsObject mainFile, final List<ObsObject> obsObjects,
			final ReportingFactory reportingFactory) throws Exception {
		final FTPClient ftpClient = new FTPClient();
		ftpClient.addProtocolCommandListener(new PrintCommandListener(new LogPrintWriter(this.logger::debug), true));

		final int port = (this.config.getPort() > 0) ? this.config.getPort(): DEFAULT_PORT;

		ftpClient.connect(this.config.getHostname(), port);
		assertPositiveCompletion(ftpClient);

		return this.performTransfer(mainFile, obsObjects, ftpClient, reportingFactory);
	}

	protected String performTransfer(final ObsObject mainFile, final List<ObsObject> filesToTransfer,
			final FTPClient ftpClient, final ReportingFactory reportingFactory) throws IOException, SdkClientException {

		if (!ftpClient.login(this.config.getUsername(), this.config.getPassword())) {
			throw new RuntimeException("Could not authenticate user " + this.config.getUsername());
		}

		try {
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

			if (this.config.isFtpPassiveMode()) {
				this.logger.debug("using passive mode");
				ftpClient.enterLocalPassiveMode();
			} else {
				this.logger.debug("using active mode");
				ftpClient.enterLocalActiveMode();
			}
			// we connect to s1pro-mock-dissemination-svc (k8s service for mock pod),
			// but initiation of data transfer may come from s1pro-mock-dissemination-ftps (docker image inside pod)
			ftpClient.setRemoteVerificationEnabled(false);

			assertPositiveCompletion(ftpClient);

			final Path outputPath = this.evaluatePathFor(mainFile);
			final String targetDirectoryUrl = this.config.getProtocol().toString().toLowerCase() + "://"
					+ this.config.getHostname() + outputPath.toString();

			Path tempManifestDestinationFilePath = null;
			final Map<Path, Path> tempToFinalDestFilePaths = new HashMap<>();

			for (final ObsObject sourceFile : filesToTransfer) {
				final String sourceFileKey = sourceFile.getKey();
				final String outputFilename = this.pathEvaluator.outputFilename(mainFile, sourceFile);
				final String temporaryOutputFilename = "." + outputFilename;

				final Path finalDestinationFilePath = outputPath.resolve(outputFilename);
				final Path temporaryDestinationFilePath = outputPath.resolve(temporaryOutputFilename);

				this.changeWorkingDirectory(ftpClient, temporaryDestinationFilePath.getParent());

				try (final InputStream in = this.stream(sourceFile.getFamily(), sourceFileKey)) {
					this.logger.info("Uploading {} to {}", sourceFileKey, temporaryDestinationFilePath);
					ftpClient.storeFile(temporaryOutputFilename, in);
					assertPositiveCompletion(ftpClient);
				}

				// remember for renaming
				tempToFinalDestFilePaths.put(temporaryDestinationFilePath, finalDestinationFilePath);
				if (outputFilename.contains("manifest")) {
					tempManifestDestinationFilePath = temporaryDestinationFilePath;
				}
			}

			this.renameFiles(ftpClient, tempToFinalDestFilePaths, tempManifestDestinationFilePath);

			return targetDirectoryUrl;
		} finally {
			try {
				ftpClient.logout();
				assertPositiveCompletion(ftpClient);
			} finally {
				ftpClient.disconnect();
				assertPositiveCompletion(ftpClient);
			}
		}
	}

	private void changeWorkingDirectory(final FTPClient ftpClient, final Path targetDirectory) throws IOException {
		if (targetDirectory == null) {
			throw new RuntimeException("Invalid destination " + targetDirectory);
		}
		// start from ftp root
		ftpClient.changeWorkingDirectory("/");

		// create parent directories if required
		for (final Path pathElement : targetDirectory) {
			final String subDirectory = pathElement.toString();

			this.logger.debug("where we are: {}", ftpClient.printWorkingDirectory());
			this.logger.debug("where we want to go: {}", subDirectory);

			final boolean directoryExists = ftpClient.changeWorkingDirectory(subDirectory);
			if (directoryExists) {
				continue;
			}

			this.logger.debug("creating directory {}", subDirectory);
			ftpClient.makeDirectory(subDirectory);
			assertPositiveCompletion(ftpClient);

			ftpClient.changeWorkingDirectory(subDirectory);
		}
	}

	private void renameFiles(final FTPClient ftpClient, final Map<Path, Path> tempToFinalDestFilePaths,
			final Path tempManifestDestinationFilePath) throws IOException {
		// rename product files
		for (final Map.Entry<Path, Path> entry : tempToFinalDestFilePaths.entrySet()) {
			final String temporaryDestinationFilePath = entry.getKey().toString();
			final Path temporaryDestinationFileName = entry.getKey().getFileName();
			final Path finalDestinationFileName = entry.getValue().getFileName();
			
			if (null != tempManifestDestinationFilePath
					&& tempManifestDestinationFilePath.toString().equals(temporaryDestinationFilePath)) {
				continue; // manifest file comes last
			}
			
			this.logger.info("Renaming {} to {}", temporaryDestinationFileName, finalDestinationFileName);
			if (null == temporaryDestinationFileName || null == finalDestinationFileName) {
				throw new RuntimeException(String.format("Cannot rename %s to %s", temporaryDestinationFileName, finalDestinationFileName));
			}
			this.changeWorkingDirectory(ftpClient, entry.getKey().getParent());
			ftpClient.rename(temporaryDestinationFileName.toString(), finalDestinationFileName.toString());
			assertPositiveCompletion(ftpClient);
		}
		// rename manifest file as the last one, because it indicates we are done
		if (null != tempManifestDestinationFilePath
				&& tempToFinalDestFilePaths.containsKey(tempManifestDestinationFilePath)) {
			final Path temporaryDestinationFileName = tempManifestDestinationFilePath.getFileName();
			final Path finalDestinationFileName = tempToFinalDestFilePaths.get(tempManifestDestinationFilePath).getFileName();

			this.logger.info("Renaming {} to {}", temporaryDestinationFileName, finalDestinationFileName);
			if (null == temporaryDestinationFileName || null == finalDestinationFileName) {
				throw new RuntimeException(String.format("Cannot rename %s to %s", temporaryDestinationFileName, finalDestinationFileName));
			}
			this.changeWorkingDirectory(ftpClient, tempManifestDestinationFilePath.getParent());
			ftpClient.rename(temporaryDestinationFileName.toString(), finalDestinationFileName.toString());
			assertPositiveCompletion(ftpClient);
		}
	}

	static void assertPositiveCompletion(final FTPClient client) throws IOException {
		if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
			throw new IOException("Error on command execution. Reply was: " + client.getReplyString());
		}
	}

}
