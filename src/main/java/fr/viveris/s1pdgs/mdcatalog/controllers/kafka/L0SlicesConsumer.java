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
import fr.viveris.s1pdgs.mdcatalog.model.L0OutputFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.ProductFamily;
import fr.viveris.s1pdgs.mdcatalog.model.ResumeDetails;
import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaL0SliceDto;
import fr.viveris.s1pdgs.mdcatalog.model.exception.AbstractCodedException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.AbstractCodedException.ErrorCode;
import fr.viveris.s1pdgs.mdcatalog.services.es.EsServices;
import fr.viveris.s1pdgs.mdcatalog.services.files.FileDescriptorBuilder;
import fr.viveris.s1pdgs.mdcatalog.services.files.MetadataBuilder;
import fr.viveris.s1pdgs.mdcatalog.services.s3.ObsService;

@Service
public class L0SlicesConsumer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(L0SlicesConsumer.class);

	/**
	 * Pattern for L0 output files
	 */
	private static final String PATTERN_L0_OUTPUT = "^([0-9a-z]{2})([0-9a-z]){1}_(S[1-6]|IW|EW|WM|N[1-6]|EN|Z[1-6]|ZE|ZI|ZW|RF|GP|HK)_(RAW)(_)_(0)(A|C|N|S|_)(SH|SV|HH|HV|VV|VH|DH|DV|__)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$";

	/**
	 * Amazon S3 service for configuration files
	 */
	private final ObsService obsService;

	/**
	 * Elasticsearch services
	 */
	private final EsServices esServices;

	/**
	 * Builder of file descriptors
	 */
	private final FileDescriptorBuilder fileDescriptorBuilder;

	/**
	 * Metadata builder
	 */
	private final MetadataBuilder mdBuilder;

	/**
	 * 
	 */
	private final MetadataExtractorConfig extractorConfig;

	/**
	 * Local directory to upload files
	 */
	private final String localDirectory;

	/**
	 * Manifest filename
	 */
	private final String manifestFilename;
	private final String fileWithManifestExt;
	private final String topicName;

	/**
	 * 
	 * @param esServices
	 * @param l0SlicesS3Services
	 * @param localDirectory
	 * @param extractorConfig
	 * @param manifestFilename
	 * @param fileWithManifestExt
	 * @param topicName
	 */
	@Autowired
	public L0SlicesConsumer(final EsServices esServices, final ObsService obsService,
			@Value("${file.l0-slices.local-directory}") final String localDirectory,
			final MetadataExtractorConfig extractorConfig,
			@Value("${file.manifest-filename}") final String manifestFilename,
			@Value("${file.file-with-manifest-ext}") final String fileWithManifestExt,
			@Value("${kafka.topic.l0-slices}") final String topicName) {
		this.localDirectory = localDirectory;
		this.fileDescriptorBuilder = new FileDescriptorBuilder(this.localDirectory,
				Pattern.compile(PATTERN_L0_OUTPUT, Pattern.CASE_INSENSITIVE));
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
	 * @param l0SlicesS3Services
	 * @param localDirectory
	 * @param extractorConfig
	 * @param fileDescriptorBuilder
	 * @param metadataBuilder
	 * @param manifestFilename
	 * @param fileWithManifestExt
	 * @param topicName
	 */
	protected L0SlicesConsumer(final EsServices esServices, final ObsService obsService, final String localDirectory,
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

	@KafkaListener(topics = "${kafka.topic.l0-slices}", groupId = "${kafka.group-id}", containerFactory = "l0SlicesKafkaListenerContainerFactory")
	public void receive(KafkaL0SliceDto dto) {
		int step = 0;
		LOGGER.info("[MONITOR] [step 0] [l0-slice] [productName {}] Starting metadata extraction",
				dto.getProductName());

		File metadataFile = null;
		try {
			step++;
			// Build key object storage
			String keyObs = dto.getKeyObjectStorage();
			if (dto.getKeyObjectStorage().toLowerCase().endsWith(this.fileWithManifestExt.toLowerCase())) {
				keyObs += "/" + manifestFilename;
			}
			// Upload file
			LOGGER.info("[MONITOR] [step 1] [l0-slice] [productName {}] Downloading file {}", dto.getProductName(),
					keyObs);
			metadataFile = this.obsService.downloadFile(ProductFamily.L0_PRODUCT, keyObs, this.localDirectory);

			// Extract metadata from name
			step++;
			LOGGER.info("[MONITOR] [step 2] [l0-slice] [productName {}] Building file descriptor",
					dto.getProductName());
			L0OutputFileDescriptor descriptor = this.fileDescriptorBuilder.buildL0OutputFileDescriptor(metadataFile);

			// Build metadata from file and extracted
			step++;
			LOGGER.info("[MONITOR] [step 3] [l0-slice] [productName {}] Building metadata", dto.getProductName());
			JSONObject metadata = mdBuilder.buildL0SliceOutputFileMetadata(descriptor, metadataFile);

			// Publish metadata
			step++;
			LOGGER.info("[MONITOR] [step 4] [l0-slice] [productName {}] Publishing metadata", dto.getProductName());
			if (!esServices.isMetadataExist(metadata)) {
				esServices.createMetadata(metadata);
			}

		} catch (AbstractCodedException e1) {
			LOGGER.error("[MONITOR] [step {}] [l0-slice] [productName {}] [code {}] [resuming {}] {}", step,
					dto.getProductName(), e1.getCode().getCode(), new ResumeDetails(topicName, dto),
					e1.getLogMessage());
		} catch (Exception e) {
			LOGGER.error("[MONITOR] [step {}] [l0-slice] [productName {}] [code {}] [resuming {}] [msg {}]", step,
					dto.getProductName(), ErrorCode.INTERNAL_ERROR.getCode(), new ResumeDetails(topicName, dto),
					e.getMessage());
		} finally {
			String log = "[MONITOR] [step 5] [l0-slice] [productName " + dto.getProductName()
					+ "] Removing downloaded file";
			this.deleteFile(metadataFile, log);
		}
		LOGGER.info("[MONITOR] [step 0] [l0-slice] [productName {}] End", dto.getProductName());
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
