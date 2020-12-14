package esa.s1pdgs.cpoc.dissemination.worker.outbox;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

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
	public void transfer(final List<ObsObject> obsObjects, final ReportingFactory reportingFactory) throws Exception {
		final FTPClient ftpClient = new FTPClient();
		ftpClient.addProtocolCommandListener(new PrintCommandListener(new LogPrintWriter(this.logger::debug), true));

		final int port = (this.config.getPort() > 0) ? this.config.getPort(): DEFAULT_PORT;

		ftpClient.connect(this.config.getHostname(), port);
		assertPositiveCompletion(ftpClient);

		this.performTransfer(obsObjects, ftpClient, reportingFactory);
	}

	protected void performTransfer(final List<ObsObject> obsObjects, final FTPClient ftpClient,
			final ReportingFactory reportingFactory) throws IOException, SdkClientException {

		if (!ftpClient.login(this.config.getUsername(), this.config.getPassword())) {
			throw new RuntimeException("Could not authenticate user " + this.config.getUsername());
		}

		try {
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

			if (this.config.isFtpPasv()) {
				this.logger.debug("using passive mode");
				ftpClient.enterLocalPassiveMode();
			} else {
				this.logger.debug("using active mode");
				ftpClient.enterLocalActiveMode();
			}
			assertPositiveCompletion(ftpClient);

			for (final ObsObject obsObject : obsObjects) {
				final Path path = this.evaluatePathFor(obsObject);

				for (final String entry : this.entries(obsObject)) {

					final Path dest = path.resolve(entry);
					String currentPath = "";

					final Path parentPath = dest.getParent();
					if (parentPath == null) {
						throw new RuntimeException("Invalid destination " + dest);
					}
					// create parent directories if required
					for (final Path pathElement : parentPath) {
						currentPath = currentPath + "/" + pathElement;

						this.logger.debug("current path is {}", currentPath);

						final boolean directoryExists = ftpClient.changeWorkingDirectory(currentPath);
						if (directoryExists) {
							continue;
						}
						this.logger.debug("creating directory {}", currentPath);
						ftpClient.makeDirectory(currentPath);
						assertPositiveCompletion(ftpClient);
					}

					try (final InputStream in = this.stream(obsObject.getFamily(), entry)) {
						this.logger.info("Uploading {} to {}", entry, dest);
						ftpClient.storeFile(dest.toString(), in);
						assertPositiveCompletion(ftpClient);
					}
				}
			}
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

	static void assertPositiveCompletion(final FTPClient client) throws IOException {
		if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
			throw new IOException("Error on command execution. Reply was: " + client.getReplyString());
		}
	}

}
