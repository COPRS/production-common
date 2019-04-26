/**
 * 
 */
package esa.s1pdgs.cpoc.mdcatalog.extraction;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * KAFKA consumer. Consume on a topic defined in configuration file
 * 
 * @author Olivier Bex-Chauvet
 */
@Service
public class EdrsSessionsExtractor extends GenericExtractor<EdrsSessionDto> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(EdrsSessionsExtractor.class);

    /**
     * Pattern for ERDS session files to extract data
     */
    private final static String PATTERN_SESSION =
            "^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$";

    @Autowired
    public EdrsSessionsExtractor(final EsServices esServices,
            @Qualifier("mqiServiceForEdrsSessions") final GenericMqiService<EdrsSessionDto> mqiService,
            final AppStatus appStatus,
            @Value("${file.product-categories.edrs-sessions.local-directory}") final String localDirectory,
            final MetadataExtractorConfig extractorConfig) {
        super(esServices, mqiService, appStatus, localDirectory,
                extractorConfig, PATTERN_SESSION, ProductCategory.EDRS_SESSIONS);
    }

    /**
     * Consume a message from the AUXILIARY_FILES product category and extract
     * metadata
     * 
     * @see GenericExtractor#genericExtract()
     */
    @Scheduled(fixedDelayString = "${file.product-categories.edrs-sessions.fixed-delay-ms}", initialDelayString = "${file.product-categories.edrs-sessions.init-delay-poll-ms}")
    public void extract() {
        super.genericExtract();
    }

    /**
     * @see GenericExtractor#extractProductNameFromDto(Object)
     */
    @Override
    protected String extractProductNameFromDto(final EdrsSessionDto dto) {
        return dto.getObjectStorageKey();
    }

    /**
     * @see GenericExtractor#extractMetadata(GenericMessageDto)
     */
    @Override
    protected JSONObject extractMetadata(
            final GenericMessageDto<EdrsSessionDto> message)
            throws AbstractCodedException {
        LOGGER.info(
                "[MONITOR] [step 2] [EDRS_SESSIONS] [obs {}] Extracting from filename",
                extractProductNameFromDto(message.getBody()));
        EdrsSessionFileDescriptor edrsFileDescriptor = fileDescriptorBuilder
                .buildEdrsSessionFileDescriptor(new File(this.localDirectory
                        + extractProductNameFromDto(message.getBody())));

        // Build metadata from file and extracted
        LOGGER.info(
                "[MONITOR] [step 3] [EDRS_SESSIONS] [obs {}] Extracting from file",
                extractProductNameFromDto(message.getBody()));
        return mdBuilder.buildEdrsSessionFileMetadata(edrsFileDescriptor);
    }

    /**
     * @see GenericExtractor#cleanProcessing(GenericMessageDto)
     */
    @Override
    protected void cleanProcessing(
            final GenericMessageDto<EdrsSessionDto> message) {
        // Nothing to do
    }
}
