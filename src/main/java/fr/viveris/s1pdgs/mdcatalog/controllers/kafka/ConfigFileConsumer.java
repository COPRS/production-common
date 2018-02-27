/**
 * 
 */
package fr.viveris.s1pdgs.mdcatalog.controllers.kafka;

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

import fr.viveris.s1pdgs.mdcatalog.model.ConfigFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaConfigFileDto;
import fr.viveris.s1pdgs.mdcatalog.services.es.EsServices;
import fr.viveris.s1pdgs.mdcatalog.services.files.FileDescriptorBuilder;
import fr.viveris.s1pdgs.mdcatalog.services.files.MetadataBuilder;
import fr.viveris.s1pdgs.mdcatalog.services.s3.ConfigFilesS3Services;

/**
 * KAFKA consumer.
 * Consume on a topic defined in configuration file
 * @author Olivier Bex-Chauvet
 *
 */
@Service
public class ConfigFileConsumer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFileConsumer.class);

	/**
	 * Elasticsearch services
	 */
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
	 * Metadata builder
	 */
	private MetadataBuilder mdBuilder;
	
	/**
	 * Local directory for configurations files
	 */
	@Value("${file.config-files.local-directory}")
	private String configLocalDirectory;
	
	/**
	 * Builder of file descriptors
	 */
	private FileDescriptorBuilder fileDescriptorBuilder;
	
	/**
	 * Count down latch which allows the POJO to signal that a message is received
	 */
	private CountDownLatch latchMetadata = new CountDownLatch(1);

	/**
	 * Local file for the metadata to extract
	 */
	private File metadataFile;
	
	/**
	 * Message listener container. Read a message
	 * 
	 * @param payload
	 */
	@KafkaListener(topics = "${kafka.topic.config-files}", groupId = "${kafka.group-id}")
	public void receive(KafkaConfigFileDto metadata) {
		LOGGER.debug("[receive] Consume message {}", metadata);
		this.latchMetadata.countDown();
		this.fileDescriptorBuilder = new FileDescriptorBuilder(configLocalDirectory, 
				Pattern.compile(PATTERN_CONFIG, Pattern.CASE_INSENSITIVE));
		this.mdBuilder = new MetadataBuilder();
		// Create metadata
		try {
			JSONObject metadataToIndex = new JSONObject();
			if(configFilesS3Services.exist(metadata.getProductName())) {
				metadataFile = new File(configLocalDirectory + metadata.getKeyObjectStorage());
				configFilesS3Services.downloadFile(metadata.getKeyObjectStorage(), metadataFile);
				ConfigFileDescriptor configFileDescriptor = fileDescriptorBuilder.buildConfigFileDescriptor(metadataFile);
				metadataToIndex = mdBuilder.buildConfigFileMetadata(configFileDescriptor, metadataFile);
				if (!metadataFile.delete()) {
					LOGGER.error("[processConfigFile] File {} not removed from local storage", metadataFile.getPath());
				}
			}
			else {
				LOGGER.error("File {} does not exists", metadata.getProductName());
			}
			if (!esServices.isMetadataExist(metadataToIndex)) {
				esServices.createMetadata(metadataToIndex);
			}
			LOGGER.info("Metadata created for {}", metadataToIndex.getString("productName"));
		} catch (Exception e){
			LOGGER.error(e.getMessage());
		}
	}
	
	public CountDownLatch getLatchMetadata() {
		return this.latchMetadata;
	}
	
}
