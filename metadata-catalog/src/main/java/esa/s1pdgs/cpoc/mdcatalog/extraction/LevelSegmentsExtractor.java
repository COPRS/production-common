package esa.s1pdgs.cpoc.mdcatalog.extraction;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mdcatalog.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.mdcatalog.status.AppStatusImpl;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting;

/**
 * KAFKA consumer. Consume on a topic defined in L1 slices
 * 
 * @author Olivier Bex-Chauvet
 */
@Service
public class LevelSegmentsExtractor extends GenericExtractor<ProductionEvent> implements MqiListener<ProductionEvent> {
	
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

    /**
     * 
     */
    private final long pollingIntervalMs;
    
    private final long pollingInitialDelayMs;
    
	@Autowired
	public LevelSegmentsExtractor(final EsServices esServices, final ObsClient obsClient,
			final GenericMqiClient mqiClient, final AppStatusImpl appStatus, final MetadataExtractorConfig extractorConfig,
			@Value("${file.product-categories.level-segments.local-directory}") final String localDirectory,
			@Value("${file.manifest-filename}") final String manifestFilename, final ErrorRepoAppender errorAppender,
			final ProcessConfiguration processConfiguration,
			@Value("${file.file-with-manifest-ext}") final String fileManifestExt, final XmlConverter xmlConverter,
			@Value("${file.product-categories.level-segments.fixed-delay-ms}") final long pollingIntervalMs,
			@Value("${file.product-categories.level-segments.init-delay-poll-ms}") final long pollingInitialDelayMs) {
		super(esServices, mqiClient, appStatus, localDirectory, extractorConfig, PATTERN_CONFIG, errorAppender,
				ProductCategory.LEVEL_SEGMENTS, processConfiguration, xmlConverter);
		this.obsClient = obsClient;
		this.manifestFilename = manifestFilename;
		this.fileManifestExt = fileManifestExt;
		this.pollingIntervalMs = pollingIntervalMs;
		this.pollingInitialDelayMs = pollingInitialDelayMs;
	}

	@PostConstruct
	public void initService() {
		appStatus.setWaiting(category);
		if (pollingIntervalMs > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(new MqiConsumer<ProductionEvent>(mqiClient, category, this, pollingIntervalMs,
					pollingInitialDelayMs, esa.s1pdgs.cpoc.appstatus.AppStatus.NULL));
		}
	}

    @Override
    public void onMessage(final GenericMessageDto<ProductionEvent> message) {
    	super.genericExtract(message);
    	
    }

    /**
     * @see GenericExtractor#extractMetadata(GenericMessageDto)
     */
    @Override
    protected JSONObject extractMetadata(
    		final Reporting.Factory reportingFactory, 
            final GenericMessageDto<ProductionEvent> message)
            throws AbstractCodedException {
    	
        final ProductionEvent dto = message.getBody();
        final String keyObs = getKeyObs(message);        
        final String productName = extractProductNameFromDto(dto);
        final ProductFamily family = message.getBody().getProductFamily();
        
        LOGGER.debug("starting to download metadatafile for for product: {}",productName);
        
        final File metadataFile = download(reportingFactory, obsClient, family, productName, keyObs);  

        LOGGER.debug("segment metadata file dowloaded:{} for product: {}",metadataFile.getAbsolutePath(),productName);
        
    	final OutputFileDescriptor l0SegmentDesc = extractFromFilename(
    			reportingFactory, 
    			() -> fileDescriptorBuilder.buildOutputFileDescriptor(metadataFile, dto, dto.getProductFamily())
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
            final GenericMessageDto<ProductionEvent> message) {
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
    protected String extractProductNameFromDto(final ProductionEvent dto) {
        return dto.getProductName();
    }

    /**
     * @see GenericExtractor#cleanProcessing(GenericMessageDto)
     */
    @Override
    protected void cleanProcessing(
            final GenericMessageDto<ProductionEvent> message) {
        // TODO Auto-generated method stub
        final File metadataFile = new File(localDirectory, getKeyObs(message));
        if (metadataFile.exists()) {
            final File parent = metadataFile.getParentFile();
            metadataFile.delete();
            // Remove upper directory if needed
            if (!localDirectory.endsWith(parent.getName() + "/")) {
                parent.delete();
            }
        }
    }
}
