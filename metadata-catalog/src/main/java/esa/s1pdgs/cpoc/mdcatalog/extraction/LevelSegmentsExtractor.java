package esa.s1pdgs.cpoc.mdcatalog.extraction;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mdcatalog.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.mdcatalog.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting;

/**
 * KAFKA consumer. Consume on a topic defined in L1 slices
 * 
 * @author Olivier Bex-Chauvet
 */
@Service
public class LevelSegmentsExtractor extends GenericExtractor<ProductDto> {
	
	private static final Logger LOGGER = LogManager.getLogger(LevelSegmentsExtractor.class);

	
    /**
     * Pattern for configuration files to extract data
     */
    public final static String PATTERN_CONFIG =
            "^(S1|AS)(A|B)_(S[1-6]|RF|GP|HK|IW|EW|WV|N[1-6]|EN|IM)_(SLC|GRD|OCN|RAW)(F|H|M|_)_(0)(A|C|N|S|_)(SH|__|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$";

    /**
     * Amazon S3 service for configuration files
     */
    private final ObsClient obsClient;

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
            final ObsClient obsClient,
            final GenericMqiClient mqiService,
            final AppStatus appStatus,
            final MetadataExtractorConfig extractorConfig,
            @Value("${file.product-categories.level-segments.local-directory}") final String localDirectory,
            @Value("${file.manifest-filename}") final String manifestFilename,
            final ErrorRepoAppender errorAppender,
            final ProcessConfiguration processConfiguration,
            @Value("${file.file-with-manifest-ext}") final String fileManifestExt,
            final XmlConverter xmlConverter) {
        super(esServices, mqiService, appStatus, localDirectory,
                extractorConfig, PATTERN_CONFIG,
                errorAppender,
                ProductCategory.LEVEL_SEGMENTS, processConfiguration,
                xmlConverter);
        this.obsClient = obsClient;
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
    		final Reporting.Factory reportingFactory, 
            final GenericMessageDto<ProductDto> message)
            throws AbstractCodedException {
    	
        final ProductDto dto = message.getBody();
        final String keyObs = getKeyObs(message);        
        final String productName = extractProductNameFromDto(dto);
        final ProductFamily family = message.getBody().getFamily();
        
        LOGGER.debug("starting to download metadatafile for for product: {}",productName);
        
        final File metadataFile = download(reportingFactory, obsClient, family, productName, keyObs);  

        LOGGER.debug("segment metadata file dowloaded:{} for product: {}",metadataFile.getAbsolutePath(),productName);
        
    	final OutputFileDescriptor l0SegmentDesc = extractFromFilename(
    			reportingFactory, 
    			() -> fileDescriptorBuilder.buildOutputFileDescriptor(metadataFile, dto, dto.getFamily())
    	);
    	LOGGER.debug("OutputFileDescriptor:{} for product: {}",l0SegmentDesc.toString(),productName);
    	
    	return extractFromFile(
    			reportingFactory, 
    			() -> mdBuilder.buildL0SegmentOutputFileMetadata(l0SegmentDesc, metadataFile)
    	); 
    }

    /**
     * Get the OBS key of the file used for extracting metadata for this product
     * 
     * @param message
     * @return
     */
    protected String getKeyObs(
            final GenericMessageDto<ProductDto> message) {
        String keyObs = message.getBody().getKeyObjectStorage();
        if (keyObs.toLowerCase().endsWith(fileManifestExt.toLowerCase())) {
            keyObs += "/" + manifestFilename;
        }
        return keyObs;
    }

    /**
     * @see GenericExtractor#extractProductNameFromDto(Object)
     */
    @Override
    protected String extractProductNameFromDto(final ProductDto dto) {
        return dto.getProductName();
    }

    /**
     * @see GenericExtractor#cleanProcessing(GenericMessageDto)
     */
    @Override
    protected void cleanProcessing(
            final GenericMessageDto<ProductDto> message) {
        // TODO Auto-generated method stub
        File metadataFile = new File(localDirectory, getKeyObs(message));
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
