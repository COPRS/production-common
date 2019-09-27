/**
 * 
 */
package esa.s1pdgs.cpoc.mdcatalog.extraction;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mdcatalog.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.mdcatalog.extraction.files.LandMaskExtractor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.ConfigFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.mdcatalog.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting;

/**
 * KAFKA consumer. Consume on a topic defined in configuration file
 * 
 * @author Olivier Bex-Chauvet
 */
@Controller
public class AuxiliaryFilesExtractor extends GenericExtractor<ProductDto> implements MqiListener<ProductDto> {

	private static final Logger LOGGER = LogManager.getLogger(AuxiliaryFilesExtractor.class);
	/**
	 * Pattern for configuration files to extract data
	 */
	private final static String PATTERN_CONFIG = "^([0-9a-z][0-9a-z]){1}([0-9a-z_]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_PP2|AUX_CAL|AUX_INS|AUX_RESORB|AUX_WND|AUX_ICE|AUX_WAV|MPL_ORBPRE|MPL_ORBSCT|MSK__LAND_)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$";

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

	@Autowired
	public AuxiliaryFilesExtractor(final EsServices esServices, final ObsClient obsClient,
			final GenericMqiClient mqiService, final AppStatus appStatus, final MetadataExtractorConfig extractorConfig,
			@Value("${file.product-categories.auxiliary-files.local-directory}") final String localDirectory,
			@Value("${file.manifest-filename}") final String manifestFilename, final ErrorRepoAppender errorAppender,
			final ProcessConfiguration processConfiguration,
			@Value("${file.file-with-manifest-ext}") final String fileManifestExt, final XmlConverter xmlConverter,
			@Value("${file.product-categories.auxiliary-files.fixed-delay-ms}") final long pollingIntervalMs) {
		super(esServices, mqiService, appStatus, localDirectory, extractorConfig, PATTERN_CONFIG, errorAppender,
				ProductCategory.AUXILIARY_FILES, processConfiguration, xmlConverter);
		this.obsClient = obsClient;
		this.manifestFilename = manifestFilename;
		this.fileManifestExt = fileManifestExt;
		this.pollingIntervalMs = pollingIntervalMs;
	}

	@PostConstruct
	public void initService() {
		appStatus.setWaiting(category);
		final ExecutorService service = Executors.newFixedThreadPool(1);
		service.execute(
				new MqiConsumer<ProductDto>(mqiClient, category, this, pollingIntervalMs));
	}
    
    @Override
    public void onMessage(GenericMessageDto<ProductDto> message) {
    	super.genericExtract(message);
    	
    }

	/**
	 * @see GenericExtractor#extractMetadata(GenericMessageDto)
	 */
	@Override
	protected JSONObject extractMetadata(final Reporting.Factory reportingFactory,
			final GenericMessageDto<ProductDto> message) throws AbstractCodedException {
		// Upload file
		final String keyObs = getKeyObs(message);
		final String productName = extractProductNameFromDto(message.getBody());

		final File metadataFile = download(reportingFactory, obsClient, ProductFamily.AUXILIARY_FILE, productName,
				keyObs);

		// Extract description from pattern
		final ConfigFileDescriptor configFileDesc = extractFromFilename(reportingFactory,
				() -> fileDescriptorBuilder.buildConfigFileDescriptor(metadataFile));

		// Build metadata from file and extracted
		final JSONObject obj = extractFromFile(reportingFactory,
				() -> mdBuilder.buildConfigFileMetadata(configFileDesc, metadataFile));

		/*
		 * In case we are having a land mask file, we are uploading the geo shape information
		 * to a dedicated elastic search index
		 */
		// TODO: Having this logic in the Auxiliary Extract might not be the best place, maybe a new one for masks would be better
		if (configFileDesc.getProductType().equals("MSK__LAND_")) {			
			try {
				final List<JSONObject> landMasks = new LandMaskExtractor().extract(metadataFile);
				LOGGER.info("Uploading {} land mask polygons", landMasks.size());
				int c=0;
				for (JSONObject land : landMasks) {
					LOGGER.debug("Uploading land mask for {}", land.getString("name"));
					LOGGER.trace("land mask json: {}",land.toString());
					esServices.createGeoMetadata(land,"land"+c);
					LOGGER.debug("Uploading land mask finished for {}", land.getString("name"));
					c++;
				}
			} catch (Exception ex) {
				LOGGER.error("An error occured while ingesting land mark documents: {}", LogUtils.toString(ex));
				throw new InternalErrorException(LogUtils.toString(ex));
			}
		}

		return obj;
	}

	/**
	 * Get the OBS key of the file used for extracting metadata for this product
	 * 
	 * @param message
	 * @return
	 */
	protected String getKeyObs(final GenericMessageDto<ProductDto> message) {
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
	protected void cleanProcessing(final GenericMessageDto<ProductDto> message) {
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
