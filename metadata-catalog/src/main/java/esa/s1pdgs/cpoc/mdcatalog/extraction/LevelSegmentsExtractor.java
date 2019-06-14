package esa.s1pdgs.cpoc.mdcatalog.extraction;

import java.io.File;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mdcatalog.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.L0OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.obs.ObsService;
import esa.s1pdgs.cpoc.mdcatalog.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelSegmentDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;

/**
 * KAFKA consumer. Consume on a topic defined in L1 slices
 * 
 * @author Olivier Bex-Chauvet
 */
@Service
public class LevelSegmentsExtractor extends GenericExtractor<LevelSegmentDto> {
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
            final ErrorRepoAppender errorAppender,
            final ProcessConfiguration processConfiguration,
            @Value("${file.file-with-manifest-ext}") final String fileManifestExt) {
        super(esServices, mqiService, appStatus, localDirectory,
                extractorConfig, PATTERN_CONFIG,
                errorAppender,
                ProductCategory.LEVEL_SEGMENTS, processConfiguration, LevelSegmentDto.class);
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
    		final Reporting.Factory reportingFactory, 
            final GenericMessageDto<LevelSegmentDto> message)
            throws AbstractCodedException {
    	
        final LevelSegmentDto dto = message.getBody();
        final String keyObs = getKeyObs(message);        
        final String productName = extractProductNameFromDto(dto);
        final ProductFamily family = message.getBody().getFamily();
        
        reportingFactory.product(family.toString(), productName);
        
        final File metadataFile = download(reportingFactory, obsService, family, productName, keyObs);  

    	final L0OutputFileDescriptor l0SegmentDesc = extractFromFilename(
    			reportingFactory, 
    			() -> fileDescriptorBuilder.buildL0SegmentFileDescriptor(metadataFile, dto)
    	);
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
