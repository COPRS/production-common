package fr.viveris.s1pdgs.mdcatalog.controllers.kafka;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.mdcatalog.config.MetadataExtractorConfig;
import fr.viveris.s1pdgs.mdcatalog.model.L1OutputFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.ProductFamily;
import fr.viveris.s1pdgs.mdcatalog.model.ResumeDetails;
import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaL1ADto;
import fr.viveris.s1pdgs.mdcatalog.model.exception.AbstractCodedException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.AbstractCodedException.ErrorCode;
import fr.viveris.s1pdgs.mdcatalog.services.es.EsServices;
import fr.viveris.s1pdgs.mdcatalog.services.files.FileDescriptorBuilder;
import fr.viveris.s1pdgs.mdcatalog.services.files.MetadataBuilder;
import fr.viveris.s1pdgs.mdcatalog.services.s3.ObsService;

/**
 * KAFKA consumer. Consume on a topic defined in L1 Annotaions
 * 
 * @author Olivier Bex-Chauvet
 *
 */
@Service
public class L1AConsumer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(L1AConsumer.class);

	/**
	 * Pattern for configuration files to extract data
	 */
	private final static String PATTERN_CONFIG = "^(S1A|S1B|ASA)_(S[1-6]|IW|EW|WM|N[1-6]|EN|IM)_(SLC|GRD|OCN)(F|H|M|_)_(1|2)(A|S)(SH|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$";

	/**
	 * Elasticsearch services
	 */
	private final EsServices esServices;

	/**
	 * Amazon S3 service for configuration files
	 */
	private final ObsService obsService;

	/**
	 * Metadata builder
	 */
	private final MetadataBuilder mdBuilder;

	/**
	 * Local directory for configurations files
	 */
	private final String localDirectory;

	/**
	 * 
	 */
	private final MetadataExtractorConfig extractorConfig;

	/**
	 * Builder of file descriptors
	 */
	private final FileDescriptorBuilder fileDescriptorBuilder;

	/**
	 * Manifest filename
	 */
	private final String manifestFilename;
	private final String fileWithManifestExt;
	private final String topicName;

	/**
	 * 
	 * @param esServices
	 * @param l1AS3Services
	 * @param localDirectory
	 * @param extractorConfig
	 * @param manifestFilename
	 * @param fileWithManifestExt
	 * @param topicName
	 */
	@Autowired
	public L1AConsumer(final EsServices esServices, final ObsService obsService,
			@Value("${file.l1-acns.local-directory}") final String localDirectory,
			final MetadataExtractorConfig extractorConfig,
			@Value("${file.manifest-filename}") final String manifestFilename,
			@Value("${file.file-with-manifest-ext}") final String fileWithManifestExt,
			@Value("${kafka.topic.l1-acns}") final String topicName) {
		this.localDirectory = localDirectory;
		this.fileDescriptorBuilder = new FileDescriptorBuilder(this.localDirectory,
				Pattern.compile(PATTERN_CONFIG, Pattern.CASE_INSENSITIVE));
		this.extractorConfig = extractorConfig;
		this.mdBuilder = new MetadataBuilder(this.extractorConfig);
		this.esServices = esServices;
		this.obsService = obsService;
		this.manifestFilename = manifestFilename;
		this.fileWithManifestExt = fileWithManifestExt;
		this.topicName = topicName;
	}

	/**
	 * 
	 * @param esServices
	 * @param l1AS3Services
	 * @param localDirectory
	 * @param extractorConfig
	 * @param fileDescriptorBuilder
	 * @param metadataBuilder
	 * @param manifestFilename
	 * @param fileWithManifestExt
	 * @param topicName
	 */
	protected L1AConsumer(final EsServices esServices, final ObsService obsService, final String localDirectory,
			final MetadataExtractorConfig extractorConfig, final FileDescriptorBuilder fileDescriptorBuilder,
			final MetadataBuilder metadataBuilder, final String manifestFilename, final String fileWithManifestExt,
			final String topicName) {
		this.localDirectory = localDirectory;
		this.fileDescriptorBuilder = fileDescriptorBuilder;
		this.extractorConfig = extractorConfig;
		this.mdBuilder = metadataBuilder;
		this.esServices = esServices;
		this.obsService = obsService;
		this.manifestFilename = manifestFilename;
		this.fileWithManifestExt = fileWithManifestExt;
		this.topicName = topicName;
	}

	/**
	 * Message listener container. Read a message
	 * 
	 * @param payload
	 */
	@KafkaListener(topics = "${kafka.topic.l1-acns}", groupId = "${kafka.group-id}", containerFactory = "l1AKafkaListenerContainerFactory")
	public void receive(KafkaL1ADto dto) {
		int step = 0;
		LOGGER.info("[MONITOR] [step 0] [l1-acn] [productName {}] Starting metadata extraction", dto.getProductName());

		File metadataFile = null;
		// Create metadata
		try {
			step++;
			// Build key object storage
			String keyObs = dto.getKeyObjectStorage();
			if (dto.getKeyObjectStorage().toLowerCase().endsWith(this.fileWithManifestExt.toLowerCase())) {
				keyObs += "/" + manifestFilename;
			}
			// Upload file
			LOGGER.info("[MONITOR] [step 1] [l1-acn] [productName {}] Downloading file {}", dto.getProductName(),
					keyObs);
			metadataFile = obsService.downloadFile(ProductFamily.L1_ACN, keyObs, this.localDirectory);

			// Extract metadata from name
			step++;
			LOGGER.info("[MONITOR] [step 2] [l1-acn] [productName {}] Building file descriptor", dto.getProductName());
			L1OutputFileDescriptor l1AFileDescriptor = fileDescriptorBuilder.buildL1OutputFileDescriptor(metadataFile);

			// Build metadata from file and extracted
			step++;
			LOGGER.info("[MONITOR] [step 3] [l1-acn] [productName {}] Building metadata", dto.getProductName());
			JSONObject metadata = mdBuilder.buildL1AcnOutputFileMetadata(l1AFileDescriptor, metadataFile);

			// Publish metadata
			step++;
			LOGGER.info("[MONITOR] [step 4] [l1-acn] [productName {}] Publishing metadata", dto.getProductName());
			if (!esServices.isMetadataExist(metadata)) {
				esServices.createMetadata(metadata);
			}
		} catch (AbstractCodedException e1) {
			LOGGER.error("[MONITOR] [step {}] [l1-acn] [productName {}] [code {}] [resuming {}] {}", step,
					dto.getProductName(), e1.getCode().getCode(), new ResumeDetails(topicName, dto),
					e1.getLogMessage());
		} catch (Exception e) {
			LOGGER.error("[MONITOR] [step {}] [l1-acn] [productName {}] [code {}] [resuming {}] [msg {}]", step,
					dto.getProductName(), ErrorCode.INTERNAL_ERROR.getCode(), new ResumeDetails(topicName, dto),
					e.getMessage());
		} finally {
			String log = "[MONITOR] [step 5] [l1-acn] [productName " + dto.getProductName()
					+ "] Removing downloaded file";
			this.deleteFile(metadataFile, log);
		}
		LOGGER.info("[MONITOR] [step 0] [l1-acn] [productName {}] End", dto.getProductName());
	}

	protected void deleteFile(File metadataFile, String log) {
		if (metadataFile != null) {
			LOGGER.info(log);
			File parent = metadataFile.getParentFile();
			metadataFile.delete();
			// Remove upper directory if needed
			if (!this.localDirectory.endsWith(parent.getName() + "/")) {
				parent.delete();
			}
		}
	}
}
