package esa.s1pdgs.cpoc.ingestor.files;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.Message;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsAlreadyExist;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.errors.processing.IngestorIgnoredFileException;
import esa.s1pdgs.cpoc.ingestor.files.model.FileDescriptor;
import esa.s1pdgs.cpoc.ingestor.files.services.AbstractFileDescriptorService;
import esa.s1pdgs.cpoc.ingestor.kafka.PublicationServices;
import esa.s1pdgs.cpoc.ingestor.obs.ObsService;
import esa.s1pdgs.cpoc.ingestor.status.AppStatus;

public abstract class AbstractFileProcessor<T> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(AbstractFileProcessor.class);

    /**
     * Amazon S3 service for configuration files
     */
    private final ObsService obsService;

    /**
     * KAFKA producer on the topic "metadata"
     */
    private final PublicationServices<T> publisher;

    /**
     * Builder of file descriptors
     */
    private final AbstractFileDescriptorService extractor;

    /**
     * Product family processed
     */
    private final ProductFamily family;
    
    /**
     * Application status for archives
     */
    private final AppStatus appStatus;

    public AbstractFileProcessor(final ObsService obsService,
            final PublicationServices<T> publisher,
            final AbstractFileDescriptorService extractor,
            final ProductFamily family,
            final AppStatus appStatus) {
        this.obsService = obsService;
        this.publisher = publisher;
        this.extractor = extractor;
        this.family = family;
        this.appStatus = appStatus;
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
        if (isValidFile(file)) {
            int step = 0;
            String productName = file.getName();
            LOGGER.info(
                    "[REPORT] [MONITOR] [step 0] [s1pdgsTask Ingestion] [START] Start processing of file [productName {}] for [family {}]",
                    productName, extractor.getFamily());
            this.appStatus.setProcessing(family);
            // Build model file
            try {
                try {
                    step++;
                    FileDescriptor descriptor =
                            extractor.extractDescriptor(file);
                    productName = descriptor.getProductName();
                    // Store in object storage
                    LOGGER.info(
                            "[MONITOR] [step 1] [productName {}] Starting uploading file in OBS",
                            productName);
                    if (!obsService.exist(family,
                            descriptor.getKeyObjectStorage())) {
                        obsService.uploadFile(family,
                                descriptor.getKeyObjectStorage(), file);
                    } else {
                        throw new ObsAlreadyExist(family,
                                descriptor.getProductName(), new Exception(
                                        "File already exist in object storage"));
                    }
                    // Send metadata
                    step++;
                    if (descriptor.isHasToBePublished()) {
                        LOGGER.info(
                                "[REPORT] [MONITOR] [step 2] [productName {}] [s1pdgsTask Ingestion] [STOP OK] Publishing file in topic",
                                productName);
                        publisher.send(buildDto(descriptor));
                    }
                } catch (IngestorIgnoredFileException ce) {
                    LOGGER.debug(
                            "[REPORT] [MONITOR] [s1pdgsTask Ingestion] [STOP KO] [step {}] [productName {}] [code {}] {}",
                            step, productName, ce.getCode().getCode(),
                            ce.getLogMessage());
                } catch (ObsAlreadyExist ace) {
                    LOGGER.error(
                            "[REPORT] [MONITOR] [s1pdgsTask Ingestion] [STOP KO] [step {}] [productName {}] [code {}] {}",
                            step, productName, ace.getCode().getCode(),
                            ace.getLogMessage());
                } catch (ObsException ace) {
                    throw ace;
                } catch (AbstractCodedException ace) {
                    LOGGER.error(
                            "[REPORT] [MONITOR] [s1pdgsTask Ingestion] [STOP KO] [step {}] [productName {}] [code {}] {}",
                            step, productName, ace.getCode().getCode(),
                            ace.getLogMessage());
                    this.appStatus.setError(family);
                }
                // Remove file
                step++;
                LOGGER.info(
                        "[MONITOR] [step 3] [productName {}] Starting removing file",
                        productName);
                try {
                    Files.delete(Paths.get(file.getPath()));
                } catch (Exception e) {
                    LOGGER.error(
                            "[MONITOR] [step 3] [code {}] [file {}] File cannot be removed from FTP storage: {}",
                            AbstractCodedException.ErrorCode.INGESTOR_CLEAN
                                    .getCode(),
                            file.getPath(), e.getMessage());
                    this.appStatus.setError(family);
                }

            } catch (AbstractCodedException ace) {
                LOGGER.error(
                        "[REPORT] [MONITOR] [s1pdgsTask Ingestion] [STOP KO] [step {}] [productName {}] [code {}] {}",
                        step, productName, ace.getCode().getCode(),
                        ace.getLogMessage());
                this.appStatus.setError(family);
            }
            LOGGER.info(
                    "[MONITOR] [step 0] End processing of configuration file {}",
                    file.getPath());
            this.appStatus.setWaiting();
        }
    }
    
    private boolean isValidFile(File file) {
        if (file.isDirectory()) {
            return false;
        } else {
            String path = file.getPath().toLowerCase();
            if (path.endsWith("manifest.safe")) {
                return true;
            } else if (path.endsWith(".safe") || path.endsWith("data") || path.endsWith("support")) {
                return false;
            }
        }
        return true;
    }

    protected abstract T buildDto(final FileDescriptor descriptor);

}
