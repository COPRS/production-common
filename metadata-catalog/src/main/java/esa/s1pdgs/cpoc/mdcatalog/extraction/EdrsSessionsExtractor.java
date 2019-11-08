/**
 * 
 */
package esa.s1pdgs.cpoc.mdcatalog.extraction;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mdcatalog.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.mdcatalog.status.AppStatusImpl;
import esa.s1pdgs.cpoc.mqi.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting;

/**
 * KAFKA consumer. Consume on a topic defined in configuration file
 * 
 * @author Olivier Bex-Chauvet
 */
@Service
public class EdrsSessionsExtractor extends GenericExtractor<EdrsSessionDto> implements MqiListener<EdrsSessionDto> {

    /**
     * Pattern for ERDS session files to extract data
     */
    private final static String PATTERN_SESSION =
            "^(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)(\\w{4})\\.(xml|RAW))$";

    /**
     * Amazon S3 service for configuration files
     */
    private final ObsClient obsClient;
    
    /**
     * 
     */
    private final long pollingIntervalMs;
    
    private final long pollingInitialDelayMs;
    
	@Autowired
	public EdrsSessionsExtractor(final EsServices esServices, final ObsClient obsClient,
			final GenericMqiClient mqiService, final AppStatusImpl appStatus,
			@Value("${file.product-categories.edrs-sessions.local-directory}") final String localDirectory,
			final ErrorRepoAppender errorAppender, final ProcessConfiguration processConfiguration,
			final MetadataExtractorConfig extractorConfig, final XmlConverter xmlConverter,
			@Value("${file.product-categories.edrs-sessions.fixed-delay-ms}") final long pollingIntervalMs,
			@Value("${file.product-categories.edrs-sessions.init-delay-poll-ms}") final long pollingInitialDelayMs) {
		super(esServices, mqiService, appStatus, localDirectory, extractorConfig, PATTERN_SESSION, errorAppender,
				ProductCategory.EDRS_SESSIONS, processConfiguration, xmlConverter);
		this.obsClient = obsClient;
		this.pollingIntervalMs = pollingIntervalMs;
		this.pollingInitialDelayMs = pollingInitialDelayMs;
	}

	@PostConstruct
	public void initService() {
		appStatus.setWaiting(category);
		if (pollingIntervalMs > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(new MqiConsumer<EdrsSessionDto>(mqiClient, category, this, pollingIntervalMs,
					pollingInitialDelayMs, esa.s1pdgs.cpoc.status.AppStatus.NULL));
		}
	}
    
    @Override
    public void onMessage(GenericMessageDto<EdrsSessionDto> message) {
    	super.genericExtract(message);
    	
    }

    /**
     * @see GenericExtractor#extractProductNameFromDto(Object)
     */
    @Override
    protected String extractProductNameFromDto(final EdrsSessionDto dto) {
        return dto.getKeyObjectStorage();
    }

    /**
     * @see GenericExtractor#extractMetadata(GenericMessageDto)
     */
    @Override
    protected JSONObject extractMetadata(
     		final Reporting.Factory reportingFactory, 
            final GenericMessageDto<EdrsSessionDto> message)
            throws AbstractCodedException {
    	
        final String productName = extractProductNameFromDto(message.getBody());
        final ProductFamily family = ProductFamily.EDRS_SESSION;
        
        final EdrsSessionFileDescriptor edrsFileDescriptor = extractFromFilename(
        		reportingFactory, 
        		() -> fileDescriptorBuilder.buildEdrsSessionFileDescriptor(new File(this.localDirectory, productName))
        );

        //FIXME uniform handling of metadata extraction
        edrsFileDescriptor.setMissionId(message.getBody().getMissionId());
        edrsFileDescriptor.setSatelliteId(message.getBody().getSatelliteId());
        edrsFileDescriptor.setSessionIdentifier(message.getBody().getSessionId());
        edrsFileDescriptor.setStationCode(message.getBody().getStationCode());
        

        //Only when it is a DSIB
        if (edrsFileDescriptor.getEdrsSessionFileType()==EdrsSessionFileType.SESSION)
        {
        final String keyObs = message.getBody().getKeyObjectStorage();
        download(reportingFactory, obsClient, family, productName, keyObs);
        }
        
        final JSONObject obj = extractFromFile(
        		reportingFactory,
        		() -> mdBuilder.buildEdrsSessionFileMetadata(edrsFileDescriptor)
        );
		return obj;
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
