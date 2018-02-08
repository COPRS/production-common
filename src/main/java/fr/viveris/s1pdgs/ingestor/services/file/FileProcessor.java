package fr.viveris.s1pdgs.ingestor.services.file;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonClientException;

import fr.viveris.s1pdgs.ingestor.model.ConfigFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.ErdsSessionFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.ErdsSessionFileType;
import fr.viveris.s1pdgs.ingestor.model.dto.KafkaMetadataDto;
import fr.viveris.s1pdgs.ingestor.model.dto.KafkaSessionDto;
import fr.viveris.s1pdgs.ingestor.services.kafka.KafkaMetadataProducer;
import fr.viveris.s1pdgs.ingestor.services.kafka.KafkaSessionProducer;
import fr.viveris.s1pdgs.ingestor.services.s3.ConfigFilesS3Services;
import fr.viveris.s1pdgs.ingestor.services.s3.SessionFilesS3Services;

/**
 * Service for processing files provided by FTP
 * 
 * @author Cyrielle Gailliard
 *
 */
@Component
public class FileProcessor {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(FileProcessor.class);

	/**
	 * Amazon S3 service for configuration files
	 */
	@Autowired
	ConfigFilesS3Services configFilesS3Services;

	/**
	 * KAFKA producer on the topic "metadata"
	 */
	@Autowired
	KafkaMetadataProducer senderMetadata;

	/**
	 * Amazon S3 service for ERDS session files
	 */
	@Autowired
	SessionFilesS3Services sessionFilesS3Services;

	/**
	 * KAFKA producer on the topic "session"
	 */
	@Autowired
	KafkaSessionProducer senderSession;

	/**
	 * Builder of file descriptors
	 */
	@Autowired
	FileDescriptorBuilder fileDescriptorBuilder;

	/**
	 * Builder of metadata
	 */
	@Autowired
	MetadataBuilder metadataBuilder;

	/**
	 * Local directory when ERDS session files are temporarly stored
	 */
	@Value("${file.session-files.local-directory}")
	public String sessionLocalDirectory;

	/**
	 * Local directory when configuration files are temporarly stored
	 */
	@Value("${file.config-files.local-directory}")
	public String configLocalDirectory;

	/**
	 * Process configuration files.
	 * <ul>
	 * <li>Store in the object storage</li>
	 * <li>Publish metadata</li>
	 * </ul>
	 * 
	 * @param message
	 */
	public void processConfigFile(Message<File> message) {
		File file = message.getPayload();
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Start processing of configuration file {}", file.getPath());
		}
		// Build model file
		try {
			ConfigFileDescriptor descriptor = fileDescriptorBuilder.buildConfigFileDescriptor(file);
			// Store in object storage
			if (descriptor.isHasToBeStored()) {
				configFilesS3Services.uploadFile(descriptor.getRelativePath(), file);
			}
			// Extract metadata
			if (descriptor.isHasToExtractMetadata()) {
				KafkaMetadataDto extractedMetadata = metadataBuilder.buildConfigFileMetadata(descriptor, file);
				try {
					senderMetadata.send(extractedMetadata);
					LOGGER.info("[processConfigFile] Metadata for {} successfully sended",
							descriptor.getRelativePath());
				} catch (CancellationException | InterruptedException | ExecutionException e) {
					LOGGER.error("[processConfigFile] Metadata not published in message queuing for {}",
							descriptor.getRelativePath());
				}
			}
			// Remove file
			LOGGER.info("[processConfigFile] Try for {} remove file",
					descriptor.getRelativePath());
			if (!file.isDirectory()) {
				Files.delete(Paths.get(file.getPath()));
				if (!file.delete()) {
					LOGGER.error("[processConfigFile] File {} not removed from local storage",
							descriptor.getRelativePath());
				} else {
					LOGGER.info("[processConfigFile] File {} successfully deleted",
							descriptor.getRelativePath());
				}
			} else {
				LOGGER.info("[processConfigFile] File {} is a directory, must not remove",
						descriptor.getRelativePath());
			}
		} catch (AmazonClientException ae) {
			LOGGER.error("Processing of configuration file {} failed (step: object storage): {}", file.getPath(),
					ae.getMessage());
		} catch (IllegalArgumentException iae) {
			LOGGER.error("Processing of configuration file {} bypassed: {}", iae.getMessage());
		} catch (Exception e) {
			LOGGER.error("Processing of configuration file {} failed: {}", e.getMessage());
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing of configuration file {} succeeded", file.getPath());
		}
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("End processing of configuration file {}", file.getPath());
		}
	}

	/**
	 * Process ERDS session files.
	 * <ul>
	 * <li>Store in the object storage</li>
	 * <li>Publish metadata</li>
	 * <li>Publish session</li>
	 * </ul>
	 * 
	 * @param message
	 */
	public void processSessionFile(Message<File> message) {
		File file = message.getPayload();
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Starting processing of ERDS session file {}", file.getPath());
		}
		// Build model file
		try {
			ErdsSessionFileDescriptor descriptor = fileDescriptorBuilder.buildErdsSessionFileDescriptor(file);
			// Store in object storage
			sessionFilesS3Services.uploadFile(descriptor.getRelativePath(), file);
			// Extract metadata
			KafkaMetadataDto extractedMetadata = metadataBuilder.buildErdsSessionFileMetadata(descriptor, file);
			try {
				senderMetadata.send(extractedMetadata);
			} catch (CancellationException | InterruptedException | ExecutionException e) {
				LOGGER.error("[processConfigFile] Metadata not published in message queuing for {}",
						descriptor.getRelativePath());
			}
			// Publish session file
			if (descriptor.getProductType() == ErdsSessionFileType.SESSION) {
				try {
					// TODO use a transformer
					KafkaSessionDto dtoSession = new KafkaSessionDto();
					dtoSession.setProductName(descriptor.getProductName());
					dtoSession.setKeyObjectStorage(descriptor.getKeyObjectStorage());
					dtoSession.setChannel(descriptor.getChannel());
					dtoSession.setSessionIdentifier(descriptor.getSessionIdentifier());
					senderSession.send(dtoSession);
				} catch (CancellationException | InterruptedException | ExecutionException e) {
					LOGGER.error("[processConfigFile] Session not published in message queuing for {}",
							descriptor.getRelativePath());
				}
			}
			// Remove file
			if (!file.delete()) {
				LOGGER.error("[processConfigFile] File {} not removed from local storage",
						descriptor.getRelativePath());
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Processing of ERDS file {} succeeded", file.getPath());
			}
		} catch (AmazonClientException ae) {
			LOGGER.error("Processing of ERDS session file {} failed (step: object storage): {}", file.getPath(),
					ae.getMessage());
		} catch (IllegalArgumentException iae) {
			LOGGER.error("Processing of file {} bypassed: {}", iae.getMessage());
		}
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("End processing of ERDS session file {}", file.getPath());
		}
	}
}
