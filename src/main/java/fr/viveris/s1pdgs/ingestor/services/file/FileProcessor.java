package fr.viveris.s1pdgs.ingestor.services.file;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import fr.viveris.s1pdgs.ingestor.model.ConfigFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.ErdsSessionFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.ErdsSessionFileType;
import fr.viveris.s1pdgs.ingestor.model.dto.KafkaMetadataDto;
import fr.viveris.s1pdgs.ingestor.model.dto.KafkaSessionDto;
import fr.viveris.s1pdgs.ingestor.model.exception.FilePathException;
import fr.viveris.s1pdgs.ingestor.model.exception.IgnoredFileException;
import fr.viveris.s1pdgs.ingestor.model.exception.KafkaMetadataPublicationException;
import fr.viveris.s1pdgs.ingestor.model.exception.KafkaSessionPublicationException;
import fr.viveris.s1pdgs.ingestor.model.exception.MetadataExtractionException;
import fr.viveris.s1pdgs.ingestor.model.exception.ObjectStorageException;
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
	FileDescriptorBuilder fileDescriptorBuilder;

	/**
	 * Builder of metadata
	 */
	MetadataBuilder metadataBuilder;

	@Autowired
	public FileProcessor(@Value("${file.config-files.local-directory}") final String configLocalDirectory,
			@Value("${file.session-files.local-directory}") final String sessionLocalDirectory) {
		this.metadataBuilder = new MetadataBuilder();
		this.fileDescriptorBuilder = new FileDescriptorBuilder(configLocalDirectory, sessionLocalDirectory);
	}

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
			try {
				ConfigFileDescriptor descriptor = fileDescriptorBuilder.buildConfigFileDescriptor(file);
				// Store in object storage
				if (descriptor.isHasToBeStored()) {
					if (!configFilesS3Services.exist(descriptor.getRelativePath())) {
						configFilesS3Services.uploadFile(descriptor.getRelativePath(), file);
						LOGGER.info("[processConfigFile] {} successfully published is object storage",
								descriptor.getRelativePath());
					} else {
						throw new IgnoredFileException(descriptor.getProductName(),
								new Exception("File already exist in object storage"));
					}
				}
				// Extract metadata
				if (descriptor.isHasToExtractMetadata()) {
					KafkaMetadataDto extractedMetadata = metadataBuilder.buildConfigFileMetadata(descriptor, file);
					senderMetadata.send(extractedMetadata);
					LOGGER.info("[processConfigFile] Metadata for {} successfully sended",
							descriptor.getRelativePath());
				}

			} catch (IgnoredFileException | MetadataExtractionException | FilePathException
					| KafkaMetadataPublicationException ce) {
				LOGGER.error(ce.getMessage());
			} finally {
				// Remove file
				if (!file.isDirectory()) {
					if (!file.delete()) {
						LOGGER.error("[processConfigFile] File {} not removed from local storage", file.getPath());
					}
				}
			}
		} catch (ObjectStorageException fe) {
			LOGGER.error(fe.getMessage());
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
			try {
				ErdsSessionFileDescriptor descriptor = fileDescriptorBuilder.buildErdsSessionFileDescriptor(file);
				// Store in object storage
				if (!sessionFilesS3Services.exist(descriptor.getRelativePath())) {
					sessionFilesS3Services.uploadFile(descriptor.getRelativePath(), file);
				} else {
					throw new IgnoredFileException(descriptor.getProductName(),
							new Exception("File already exist in object storage"));
				}
				// Extract metadata
				KafkaMetadataDto extractedMetadata = metadataBuilder.buildErdsSessionFileMetadata(descriptor, file);
				senderMetadata.send(extractedMetadata);

				// Publish session file
				if (descriptor.getProductType() == ErdsSessionFileType.SESSION) {
					KafkaSessionDto dtoSession = new KafkaSessionDto();
					dtoSession.setProductName(descriptor.getProductName());
					dtoSession.setKeyObjectStorage(descriptor.getKeyObjectStorage());
					dtoSession.setChannel(descriptor.getChannel());
					dtoSession.setSessionIdentifier(descriptor.getSessionIdentifier());
					senderSession.send(dtoSession);
				}

			} catch (IgnoredFileException | MetadataExtractionException | FilePathException
					| KafkaSessionPublicationException | KafkaMetadataPublicationException ce) {
				LOGGER.error(ce.getMessage());
			} finally {
				// Remove file
				if (!file.isDirectory()) {
					if (!file.delete()) {
						LOGGER.error("[processConfigFile] File {} not removed from local storage", file.getPath());
					}
				}
			}
		} catch (ObjectStorageException fe) {
			LOGGER.error(fe.getMessage());
		}
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("End processing of ERDS session file {}", file.getPath());
		}
	}
}
