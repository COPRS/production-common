package fr.viveris.s1pdgs.mdcatalog.services.kafka;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.mdcatalog.services.s3.ConfigFilesS3Services;
import fr.viveris.s1pdgs.mdcatalog.services.files.FileDescriptorBuilder;
import fr.viveris.s1pdgs.mdcatalog.model.ConfigFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.EdrsSessionFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaMetadataDto;
import fr.viveris.s1pdgs.mdcatalog.services.es.EsServices;
import fr.viveris.s1pdgs.mdcatalog.services.files.MetadataBuilder;

/**
 * KAFKA consumer.
 * Consume on a topic defined in configuration file
 * @author Cyrielle Gailliard
 *
 */
@Service
public class KafkaConsumer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumer.class);

	@Autowired
	private EsServices esServices;
	/**
	 * Amazon S3 service for configuration files
	 */
	@Autowired
	private ConfigFilesS3Services configFilesS3Services;
	/**
	 * Pattern for configuration files to extract data
	 */
	private final static String PATTERN_CONFIG = "^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$";

	/**
	 * Pattern for ERDS session files to extract data
	 */
	private final static String PATTERN_SESSION = "^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$";
	
	
	private MetadataBuilder mdBuilder = new MetadataBuilder();
	
	@Value("${file.config-files.local-directory}")
	private String configLocalDirectory;
	
	@Value("${file.session-files.local-directory}")
	private String sessionLocalDirectory;
	/**
	 * Builder of file descriptors
	 */
	private FileDescriptorBuilder fileDescriptorBuilder = new FileDescriptorBuilder(configLocalDirectory, sessionLocalDirectory , Pattern.compile(PATTERN_CONFIG, Pattern.CASE_INSENSITIVE), Pattern.compile(PATTERN_SESSION, Pattern.CASE_INSENSITIVE));

	/**
	 * Count down latch which allows the POJO to signal that a message is received
	 */
	private CountDownLatch latchMetadata = new CountDownLatch(1);

	private File metadataFile;

	/**
	 * Message listener container. Read a message
	 * 
	 * @param payload
	 */
	@KafkaListener(topics = "${kafka.topic.metadata}", groupId = "${kafka.group-id}")
	public void receive(KafkaMetadataDto metadata) {
		LOGGER.debug("[receive] Consume message {}", metadata);
		this.latchMetadata.countDown();
		
		// Create metadata
		if (metadata.getAction().equals("CREATE")) {
			try {
				JSONObject metadataToIndex = new JSONObject();
				if(metadata.getFamilyType().equals("SESSION") || metadata.getFamilyType().equals("RAW")) {
					metadataFile = new File(sessionLocalDirectory + metadata.getMetadataToIndex());
					EdrsSessionFileDescriptor edrsFileDescriptor = fileDescriptorBuilder.buildEdrsSessionFileDescriptor(metadataFile);
					metadataToIndex = mdBuilder.buildEdrsSessionFileMetadata(edrsFileDescriptor);
				}
				else if(metadata.getFamilyType().equals("METADATA")) {
					if(configFilesS3Services.exist(metadata.getMetadataToIndex())) {
						metadataFile = new File(configLocalDirectory + metadata.getMetadataToIndex());
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Local file {}", metadataFile);
						}
						configFilesS3Services.downloadFile(metadata.getMetadataToIndex(), metadataFile);
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Local file {} downloaded", metadataFile);
						}
						ConfigFileDescriptor configFileDescriptor = fileDescriptorBuilder.buildConfigFileDescriptor(metadataFile);
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("ConfigFileDescriptor {}", configFileDescriptor);
						}
						metadataToIndex = mdBuilder.buildConfigFileMetadata(configFileDescriptor, metadataFile);
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("metadataToIndex {}", metadataToIndex);
						}
						if (!metadataFile.delete()) {
							LOGGER.error("[processConfigFile] File {} not removed from local storage", metadataFile.getPath());
						}
					}
					else {
						LOGGER.error("File {} does not exists", metadata.getMetadataToIndex());
					}
				}
				if (!esServices.isMetadataExist(metadataToIndex)) {
					esServices.createMetadata(metadataToIndex);
				}
				LOGGER.info("Metadata created for {}", metadataToIndex.getString("productName"));
			} catch (Exception e){
				LOGGER.error(e.getMessage());
			}
		} else {
			LOGGER.error("Invalid action {} for metadata {}", metadata.getAction(), metadata.getMetadataToIndex());
		}
	}
	
	public CountDownLatch getLatchMetadata() {
		return this.latchMetadata;
	}
}
