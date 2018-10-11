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
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.L0OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.obs.ObsService;
import esa.s1pdgs.cpoc.mdcatalog.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelSegmentDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * KAFKA consumer. Consume on a topic defined in L1 slices
 * 
 * @author Olivier Bex-Chauvet
 */
@Service
public class LevelSegmentsExtractor extends GenericExtractor<LevelSegmentDto> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(LevelSegmentsExtractor.class);

    /**
     * Pattern for configuration files to extract data
     */
    public final static String PATTERN_CONFIG =
            "^(S1|AS)(A|B)_(S[1-6]|IW|EW|WV|N[1-6]|EN|IM)_(SLC|GRD|OCN|RAW)(F|H|M|_)_(0)(A|C|N|S|_)(SH|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$";

    /**
     * Amazon S3 service for configuration files
     */
    private final ObsService obsService;

    /**
     * Manifest filename
     */
    private final String manifestFilename;

    /**
     * 
     */
    private final String fileManifestExt;

    @Autowired
    public LevelSegmentsExtractor(final EsServices esServices,
            final ObsService obsService,
            @Qualifier("mqiServiceForLevelSegments") final GenericMqiService<LevelSegmentDto> mqiService,
            final AppStatus appStatus,
            final MetadataExtractorConfig extractorConfig,
            @Value("${file.product-categories.level-segments.local-directory}") final String localDirectory,
            @Value("${file.manifest-filename}") final String manifestFilename,
            @Value("${file.file-with-manifest-ext}") final String fileManifestExt) {
        super(esServices, mqiService, appStatus, localDirectory,
                extractorConfig, PATTERN_CONFIG,
                ProductCategory.LEVEL_SEGMENTS);
        this.obsService = obsService;
        this.manifestFilename = manifestFilename;
        this.fileManifestExt = fileManifestExt;
    }

    /**
     * Consume a message from the AUXILIARY_FILES product category and extract
     * metadata
     * 
     * @see GenericExtractor#genericExtract()
     */
    @Scheduled(fixedDelayString = "${file.product-categories.level-segments.fixed-delay-ms}", initialDelayString = "${file.product-categories.level-segments.init-delay-poll-ms}")
    public void extract() {
        super.genericExtract();
    }

    /**
     * @see GenericExtractor#extractMetadata(GenericMessageDto)
     */
    @Override
    protected JSONObject extractMetadata(
            final GenericMessageDto<LevelSegmentDto> message)
            throws AbstractCodedException {
        LevelSegmentDto dto = message.getBody();
        // Upload file
        String keyObs = getKeyObs(message);
        LOGGER.info(
                "[MONITOR] [step 1] [LEVEL_PRODUCTS] [family {}] [productName {}] Downloading file {}",
                message.getBody().getFamily(), extractProductNameFromDto(dto),
                keyObs);
        File metadataFile = obsService.downloadFile(
                message.getBody().getFamily(), keyObs, this.localDirectory);

        // Extract description from pattern
        LOGGER.info(
                "[MONITOR] [step 2] [LEVEL_PRODUCTS] [L0_SEGMENT] [productName {}] Extracting from filename",
                extractProductNameFromDto(dto));
        L0OutputFileDescriptor l0SegmentDesc = fileDescriptorBuilder
                .buildL0SegmentFileDescriptor(metadataFile, dto);
        // Build metadata from file and extracted
        LOGGER.info(
                "[MONITOR] [step 3] [LEVEL_PRODUCTS] [L0_SEGMENT] [productName {}] Extracting from file",
                extractProductNameFromDto(dto));
        return mdBuilder.buildL0SegmentOutputFileMetadata(l0SegmentDesc,
                metadataFile);
    }

    /**
     * Get the OBS key of the file used for extracting metadata for this product
     * 
     * @param message
     * @return
     */
    protected String getKeyObs(
            final GenericMessageDto<LevelSegmentDto> message) {
        String keyObs = message.getBody().getKeyObs();
        if (keyObs.toLowerCase().endsWith(fileManifestExt.toLowerCase())) {
            keyObs += "/" + manifestFilename;
        }
        return keyObs;
    }

    /**
     * @see GenericExtractor#extractProductNameFromDto(Object)
     */
    @Override
    protected String extractProductNameFromDto(final LevelSegmentDto dto) {
        return dto.getName();
    }

    /**
     * @see GenericExtractor#cleanProcessing(GenericMessageDto)
     */
    @Override
    protected void cleanProcessing(
            final GenericMessageDto<LevelSegmentDto> message) {
        // TODO Auto-generated method stub
        File metadataFile = new File(localDirectory + getKeyObs(message));
        if (metadataFile.exists()) {
            File parent = metadataFile.getParentFile();
            metadataFile.delete();
            // Remove upper directory if needed
            if (!localDirectory.endsWith(parent.getName() + "/")) {
                parent.delete();
            }
        }
    }
}
