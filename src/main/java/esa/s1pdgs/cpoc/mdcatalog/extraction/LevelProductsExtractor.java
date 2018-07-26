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
import esa.s1pdgs.cpoc.common.errors.UnknownFamilyException;
import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.L0OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.L1OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.obs.ObsService;
import esa.s1pdgs.cpoc.mdcatalog.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * KAFKA consumer. Consume on a topic defined in L1 slices
 * 
 * @author Olivier Bex-Chauvet
 */
@Service
public class LevelProductsExtractor extends GenericExtractor<LevelProductDto> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(LevelProductsExtractor.class);

    /**
     * Pattern for configuration files to extract data
     */
    private final static String PATTERN_CONFIG =
            "^(S1A|S1B|ASA)_(S[1-6]|IW|EW|WM|N[1-6]|EN|IM)_(SLC|GRD|OCN|RAW)(F|H|M|_)_(0|1|2)(A|C|N|S|_)(SH|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$";

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
    public LevelProductsExtractor(final EsServices esServices,
            final ObsService obsService,
            @Qualifier("mqiServiceForLevelProducts") final GenericMqiService<LevelProductDto> mqiService,
            final AppStatus appStatus,
            final MetadataExtractorConfig extractorConfig,
            @Value("${file.product-categories.level-products.local-directory}") final String localDirectory,
            @Value("${file.manifest-filename}") final String manifestFilename,
            @Value("${file.file-with-manifest-ext}") final String fileManifestExt) {
        super(esServices, mqiService, appStatus, localDirectory,
                extractorConfig, PATTERN_CONFIG, ProductCategory.LEVEL_PRODUCTS);
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
    @Scheduled(fixedDelayString = "${file.product-categories.level-products.fixed-delay-ms}", initialDelayString = "${file.product-categories.level-products.init-delay-poll-ms}")
    public void extract() {
        super.genericExtract();
    }

    /**
     * @see GenericExtractor#extractMetadata(GenericMessageDto)
     */
    @Override
    protected JSONObject extractMetadata(
            final GenericMessageDto<LevelProductDto> message)
            throws AbstractCodedException {
        LevelProductDto dto = message.getBody();
        // Upload file
        String keyObs = getKeyObs(message);
        LOGGER.info(
                "[MONITOR] [step 1] [LEVEL_PRODUCTS] [productName {}] Downloading file {}",
                extractProductNameFromDto(dto), keyObs);
        File metadataFile = obsService.downloadFile(
                message.getBody().getFamily(), keyObs, this.localDirectory);

        // Extract description from pattern
        JSONObject result = null;
        switch (dto.getFamily()) {
            case L0_ACN:
                LOGGER.info(
                        "[MONITOR] [step 2] [LEVEL_PRODUCTS] [L0_ACN] [productName {}] Extracting from filename",
                        extractProductNameFromDto(dto));
                L0OutputFileDescriptor l0AcnDesc = fileDescriptorBuilder
                        .buildL0OutputFileDescriptor(metadataFile);
                // Build metadata from file and extracted
                LOGGER.info(
                        "[MONITOR] [step 3] [LEVEL_PRODUCTS] [L0_ACN] [productName {}] Extracting from file",
                        extractProductNameFromDto(dto));
                result = mdBuilder.buildL0AcnOutputFileMetadata(l0AcnDesc,
                        metadataFile);
                break;
            case L0_PRODUCT:
                LOGGER.info(
                        "[MONITOR] [step 2] [LEVEL_PRODUCTS] [L0_PRODUCT] [productName {}] Extracting from filename",
                        extractProductNameFromDto(dto));
                L0OutputFileDescriptor l0SliceDesc = fileDescriptorBuilder
                        .buildL0OutputFileDescriptor(metadataFile);
                // Build metadata from file and extracted
                LOGGER.info(
                        "[MONITOR] [step 3] [LEVEL_PRODUCTS] [L0_PRODUCT] [productName {}] Extracting from file",
                        extractProductNameFromDto(dto));
                result = mdBuilder.buildL0SliceOutputFileMetadata(l0SliceDesc,
                        metadataFile);
                break;
            case L1_ACN:
                LOGGER.info(
                        "[MONITOR] [step 2] [LEVEL_PRODUCTS] [L1_ACN] [productName {}] Extracting from filename",
                        extractProductNameFromDto(dto));
                L1OutputFileDescriptor l1AcnDesc = fileDescriptorBuilder
                        .buildL1OutputFileDescriptor(metadataFile);
                // Build metadata from file and extracted
                LOGGER.info(
                        "[MONITOR] [step 3] [LEVEL_PRODUCTS] [L1_ACN] [productName {}] Extracting from file",
                        extractProductNameFromDto(dto));
                result = mdBuilder.buildL1AcnOutputFileMetadata(l1AcnDesc,
                        metadataFile);
                break;
            case L1_PRODUCT:
                LOGGER.info(
                        "[MONITOR] [step 2] [LEVEL_PRODUCTS] [L1_PRODUCT] [productName {}] Extracting from filename",
                        extractProductNameFromDto(dto));
                L1OutputFileDescriptor l1SliceDesc = fileDescriptorBuilder
                        .buildL1OutputFileDescriptor(metadataFile);
                // Build metadata from file and extracted
                LOGGER.info(
                        "[MONITOR] [step 3] [LEVEL_PRODUCTS] [L1_PRODUCT] [productName {}] Extracting from file",
                        extractProductNameFromDto(dto));
                result = mdBuilder.buildL1SliceOutputFileMetadata(l1SliceDesc,
                        metadataFile);
                break;
            default:
                throw new UnknownFamilyException(dto.getFamily().name(),
                        "Family not managed by the catalog for the category LEVEL_PRODUCTS");
        }
        return result;
    }

    /**
     * Get the OBS key of the file used for extracting metadata for this product
     * 
     * @param message
     * @return
     */
    private String getKeyObs(final GenericMessageDto<LevelProductDto> message) {
        String keyObs = message.getBody().getKeyObjectStorage();
        if (keyObs.endsWith(fileManifestExt.toLowerCase())) {
            keyObs += "/" + manifestFilename;
        }
        return keyObs;
    }

    /**
     * @see GenericExtractor#extractProductNameFromDto(Object)
     */
    @Override
    protected String extractProductNameFromDto(final LevelProductDto dto) {
        return dto.getProductName();
    }

    /**
     * @see GenericExtractor#cleanProcessing(GenericMessageDto)
     */
    @Override
    protected void cleanProcessing(
            final GenericMessageDto<LevelProductDto> message) {
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
