package fr.viveris.s1pdgs.ingestor.files;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.Message;

import fr.viveris.s1pdgs.ingestor.exceptions.AbstractFileException.ErrorCode;
import fr.viveris.s1pdgs.ingestor.exceptions.AlreadyExistObjectStorageException;
import fr.viveris.s1pdgs.ingestor.exceptions.FileRuntimeException;
import fr.viveris.s1pdgs.ingestor.exceptions.FileTerminatedException;
import fr.viveris.s1pdgs.ingestor.exceptions.IgnoredFileException;
import fr.viveris.s1pdgs.ingestor.files.model.FileDescriptor;
import fr.viveris.s1pdgs.ingestor.files.services.AbstractFileDescriptorService;
import fr.viveris.s1pdgs.ingestor.files.services.ObsServices;
import fr.viveris.s1pdgs.ingestor.kafka.PublicationServices;

public abstract class AbstractFileProcessor<T> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(AbstractFileProcessor.class);

	/**
	 * Amazon S3 service for configuration files
	 */
	private final ObsServices obsService;

	/**
	 * KAFKA producer on the topic "metadata"
	 */
	private final PublicationServices<T> publisher;

	/**
	 * Builder of file descriptors
	 */
	private final AbstractFileDescriptorService extractor;

	public AbstractFileProcessor(final ObsServices obsService, final PublicationServices<T> publisher,
			final AbstractFileDescriptorService extractor) {
		this.obsService = obsService;
		this.publisher = publisher;
		this.extractor = extractor;
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
	public void processFile(Message<File> message) {
		File file = message.getPayload();
		if (!file.isDirectory()) {
			int step = 0;
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("[MONITOR] [step 0] Start processing of file {} for family {}", file.getPath(),
						extractor.getFamily());
			}

			// Build model file
			try {
				String productName = file.getName();
				try {
					step++;
					FileDescriptor descriptor = extractor.extractDescriptor(file);
					productName = descriptor.getProductName();
					// Store in object storage
					LOGGER.info("[MONITOR] [step 1] [productName {}] Starting uploading file in OBS", productName);
					if (!obsService.exist(descriptor.getKeyObjectStorage())) {
						obsService.uploadFile(descriptor.getKeyObjectStorage(), file);
					} else {
						throw new AlreadyExistObjectStorageException(descriptor.getProductName(),
								new Exception("File already exist in object storage"));
					}
					// Send metadata
					step++;
					if (descriptor.isHasToBePublished()) {
						LOGGER.info("[MONITOR] [step 2] [productName {}] Starting publishing file in topic",
								productName);
						publisher.send(buildDto(descriptor));
					}
				} catch (IgnoredFileException ce) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("[MONITOR] [step {}] [productName {}] [code {}] {}", step, ce.getProductName(),
								ce.getCode().getCode(), ce.getLogMessage());
					}
				} catch (FileTerminatedException fte) {
					LOGGER.error("[MONITOR] [step {}] [productName {}] [code {}] {}", step, fte.getProductName(),
							fte.getCode().getCode(), fte.getLogMessage());
				}
				// Remove file
				step++;
				LOGGER.info("[MONITOR] [step 3] [productName {}] Starting removing file", productName);
				if (!file.delete()) {
					LOGGER.error("[MONITOR] [step 3] [code {}] [file {}] File cannot be removed from FTP storage",
							ErrorCode.INGESTOR_CLEAN.getCode(), file.getPath());
				}

			} catch (FileRuntimeException fre) {
				LOGGER.error("[MONITOR] [step {}] [productName {}] [code {}] {}", step, fre.getProductName(),
						fre.getCode().getCode(), fre.getLogMessage());
			}
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("[MONITOR] [step 0] End processing of configuration file {}", file.getPath());
			}
		}
	}

	protected abstract T buildDto(final FileDescriptor descriptor);

}
