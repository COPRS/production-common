package esa.s1pdgs.cpoc.disseminator.outbox;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.util.Strings;

import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.path.PathEvaluator;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public final class LocalOutboxClient extends AbstractOutboxClient {
	public static final class Factory implements OutboxClient.Factory {
		@Override
		public OutboxClient newClient(final ObsClient obsClient, final OutboxConfiguration config, final PathEvaluator eval) {
			return new LocalOutboxClient(obsClient, config, eval);
		}
	}
	
	public LocalOutboxClient(final ObsClient obsClient, final OutboxConfiguration config, final PathEvaluator eval) {
		super(obsClient, config, eval);
	}

	@Override
	public final String transfer(final ObsObject obsObject, final ReportingFactory reportingFactory) throws Exception {		
		final Path path = evaluatePathFor(obsObject);
		final Path finalName = path.resolve(obsObject.getKey());
		
		if(config.isSkipExisting() && finalName.toFile().exists()) {
			logger.warn("Skipping transfer, it already exists: {}", finalName);
			return path.toString();
		}
		
		for (final String entry : entries(obsObject)) {
			
			final File destination = path.resolve("." + entry).toFile();
			createParentIfRequired(destination);
			
			try (final InputStream in = stream(obsObject.getFamily(), entry);
				 final OutputStream out = new BufferedOutputStream(new FileOutputStream(destination))
			) {				
				logger.info("Transferring {} to {}", entry, destination);
				IOUtils.copyLarge(in, out, new byte[config.getBufferSize()]);    				
			}
		}
		final Path nameWithDot = path.resolve("." + obsObject.getKey());
		
		if (!Strings.isEmpty(config.getChmodScriptPath())) {
			logger.debug("Executing chmod script {} for {}", config.getChmodScriptPath(), nameWithDot);
			Process process = new ProcessBuilder(config.getChmodScriptPath(), nameWithDot.toFile().getAbsolutePath()).start();
			process.waitFor();
		}
		
		logger.debug("Moving {} to {}", nameWithDot, finalName);
		Files.move(nameWithDot, finalName,
				StandardCopyOption.ATOMIC_MOVE);
		return path.toString();
	}
}