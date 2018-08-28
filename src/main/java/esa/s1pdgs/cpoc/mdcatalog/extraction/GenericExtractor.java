package esa.s1pdgs.cpoc.mdcatalog.extraction;

import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.mdcatalog.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.mdcatalog.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.mdcatalog.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * @author Viveris Technologies
 * @param <T>
 */
public abstract class GenericExtractor<T> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(GenericExtractor.class);

    /**
     * Elasticsearch services
     */
    protected final EsServices esServices;

    /**
     * Access to the MQI server
     */
    protected final GenericMqiService<T> mqiService;

    /**
     * Application status
     */
    protected final AppStatus appStatus;

    /**
     * Metadata builder
     */
    protected final MetadataBuilder mdBuilder;

    /**
     * Local directory for configurations files
     */
    protected final String localDirectory;

    /**
     * 
     */
    protected final MetadataExtractorConfig extractorConfig;

    /**
     * Builder of file descriptors
     */
    protected final FileDescriptorBuilder fileDescriptorBuilder;

    /**
     * Product category
     */
    protected final ProductCategory category;

    /**
     * @param esServices
     * @param mqiService
     * @param appStatus
     * @param localDirectory
     * @param extractorConfig
     * @param pattern
     */
    public GenericExtractor(final EsServices esServices,
            final GenericMqiService<T> mqiService, final AppStatus appStatus,
            final String localDirectory,
            final MetadataExtractorConfig extractorConfig, final String pattern,
            final ProductCategory category) {
        this.localDirectory = localDirectory;
        this.fileDescriptorBuilder =
                new FileDescriptorBuilder(this.localDirectory,
                        Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
        this.extractorConfig = extractorConfig;
        this.mdBuilder = new MetadataBuilder(this.extractorConfig);
        this.esServices = esServices;
        this.mqiService = mqiService;
        this.appStatus = appStatus;
        this.category = category;
    }

    /**
     * Consume a message from a product category and extract metadata
     */
    public void genericExtract() {

        // ----------------------------------------------------------
        // Read Message
        // ----------------------------------------------------------
        LOGGER.trace("[MONITOR] [step 0] [{}] Waiting message", category);
        GenericMessageDto<T> message = null;
        try {
            message = mqiService.next();
        } catch (AbstractCodedException ace) {
            LOGGER.error("[MONITOR] [step 0] [{}] [code {}] {}", category,
                    ace.getCode().getCode(), ace.getLogMessage());
            message = null;
        }
        if (message == null || message.getBody() == null) {
            LOGGER.trace(
                    "[MONITOR] [step 0] [{}] No message received: continue",
                    category);
            return;
        }

        // ----------------------------------------------------------
        // Process Message
        // ----------------------------------------------------------
        T dto = message.getBody();
        LOGGER.info(
                "[REPORT] [Step 0] [{}] [s1pdgsTask MetadataExtraction] [START] [productName {}] Starting metadata extraction",
                category, extractProductNameFromDto(dto));
        appStatus.setProcessing(category, message.getIdentifier());

        try {
            JSONObject metadata = extractMetadata(message);

            // Publish metadata
            LOGGER.info(
                    "[MONITOR] [step 4] [{}] [productName {}] Publishing metadata",
                    category, extractProductNameFromDto(dto));
            if (!esServices.isMetadataExist(metadata)) {
                esServices.createMetadata(metadata);
            }

            // Acknowledge
            ackPositively(message);

        } catch (AbstractCodedException e1) {
            String errorMessage = String.format(
                    "[MONITOR] [%s] [productName %s] [code %s] %s", category,
                    extractProductNameFromDto(dto), e1.getCode().getCode(),
                    e1.getLogMessage());
            ackNegatively(message, errorMessage);
        } catch (Exception e) {
            String errorMessage = String.format(
                    "[MONITOR] [%s] [productName %s] [code %s] [msg %s]",
                    category, extractProductNameFromDto(dto),
                    ErrorCode.INTERNAL_ERROR.getCode(), e.getMessage());
            ackNegatively(message, errorMessage);
        } finally {
            this.cleanProcessing(message);
        }

        LOGGER.info(
                "[MONITOR] [step 6] [{}] [productName {}] Checking status consumer",
                category, extractProductNameFromDto(dto));
        if (appStatus.isFatalError()) {
            System.exit(-1);
        } else {
            appStatus.setWaiting(category);
        }

        LOGGER.info("[MONITOR] [step 0] [{}] [productName {}] End", category,
                extractProductNameFromDto(dto));
    }

    /**
     * Acknowledge negatively the message processing and set app status in error
     * 
     * @param dto
     * @param errorMessage
     */
    protected void ackNegatively(final GenericMessageDto<T> message,
            final String errorMessage) {
        LOGGER.info(
                "[REPORT] [step 5] [{}] [s1pdgsTask MetadataExtraction] [STOP KO] [productName {}] Acknowledging negatively",
                category, extractProductNameFromDto(message.getBody()));
        LOGGER.error(errorMessage);
        try {
            mqiService.ack(new AckMessageDto(message.getIdentifier(), Ack.ERROR,
                    errorMessage, false));
        } catch (AbstractCodedException ace) {
            LOGGER.error(
                    "[MONITOR] [step 5] [{}] [productName {}] [code {}] {}",
                    category, extractProductNameFromDto(message.getBody()),
                    ace.getCode().getCode(), ace.getLogMessage());
        }
        appStatus.setError(category);
    }

    /**
     * Acknowledge positively the message processing
     * 
     * @param dto
     */
    protected void ackPositively(final GenericMessageDto<T> message) {
        LOGGER.info(
                "[REPORT] [step 5] [{}] [s1pdgsTask MetadataExtraction] [STOP OK] [productName {}] Acknowledging positively",
                category, extractProductNameFromDto(message.getBody()));
        try {
            mqiService.ack(new AckMessageDto(message.getIdentifier(), Ack.OK,
                    null, false));
        } catch (AbstractCodedException ace) {
            LOGGER.error(
                    "[MONITOR] [step 5] [{}] [productName {}] [code {}] {}",
                    category, extractProductNameFromDto(message.getBody()),
                    ace.getCode().getCode(), ace.getLogMessage());
            appStatus.setError(category);
        }
    }

    /**
     * Extract product name from the DTO
     * 
     * @param dto
     * @return
     */
    protected abstract String extractProductNameFromDto(T dto);

    /**
     * Extract the metadata of a message
     * 
     * @param message
     * @return
     * @throws AbstractCodedException
     */
    protected abstract JSONObject extractMetadata(GenericMessageDto<T> message)
            throws AbstractCodedException;

    /**
     * Clean the working directory after extracting a metadata
     * 
     * @param message
     */
    protected abstract void cleanProcessing(GenericMessageDto<T> message);

}
