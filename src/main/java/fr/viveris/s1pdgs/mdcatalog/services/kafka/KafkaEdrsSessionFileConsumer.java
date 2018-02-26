/**
 * 
 */
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

import fr.viveris.s1pdgs.mdcatalog.model.EdrsSessionFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaEdrsSessionDto;
import fr.viveris.s1pdgs.mdcatalog.services.es.EsServices;
import fr.viveris.s1pdgs.mdcatalog.services.files.FileDescriptorBuilder;
import fr.viveris.s1pdgs.mdcatalog.services.files.MetadataBuilder;

/**
 * KAFKA consumer.
 * Consume on a topic defined in configuration file
 * @author Olivier Bex-Chauvet
 *
 */
@Service
public class KafkaEdrsSessionFileConsumer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaEdrsSessionFileConsumer.class);

	/**
	 * Elasticsearch services
	 */
	@Autowired
	private EsServices esServices;
	
	/**
	 * Pattern for ERDS session files to extract data
	 */
	private final static String PATTERN_SESSION = "^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$";
	
	/**
	 * Metadata builder
	 */
	private MetadataBuilder mdBuilder;
	
	/**
	 * Local directory for sessions files
	 */
	@Value("${file.session-files.local-directory}")
	private String sessionLocalDirectory;
	
	/**
	 * Builder of file descriptors
	 */
	private FileDescriptorBuilder fileDescriptorBuilder;
	
	/**
	 * Count down latch which allows the POJO to signal that a message is received
	 */
	private CountDownLatch latch = new CountDownLatch(1);

	/**
	 * Local file for the metadata to extract
	 */
	private File metadataFile;
	
	/**
	 * Message listener container. Read a message
	 * 
	 * @param payload
	 */
	@KafkaListener(topics = "${kafka.topic.edrs-sessions}", groupId = "${kafka.group-id}", containerFactory="edrsSessionsKafkaListenerContainerFactory")
	public void receive(KafkaEdrsSessionDto metadata) {
		LOGGER.debug("[receive] Consume message {}", metadata);
		this.latch.countDown();
		/*this.fileDescriptorBuilder = new FileDescriptorBuilder(sessionLocalDirectory, 
				Pattern.compile(PATTERN_SESSION, Pattern.CASE_INSENSITIVE));
		this.mdBuilder = new MetadataBuilder();
		// Create metadata
		try {
			JSONObject metadataToIndex;
			metadataFile = new File(sessionLocalDirectory + metadata.getObjectStorageKey());
			EdrsSessionFileDescriptor edrsFileDescriptor = fileDescriptorBuilder.buildEdrsSessionFileDescriptor(metadataFile);
			metadataToIndex = mdBuilder.buildEdrsSessionFileMetadata(edrsFileDescriptor);
			if (!esServices.isMetadataExist(metadataToIndex)) {
				esServices.createMetadata(metadataToIndex);
			}
			LOGGER.info("Metadata created for {}", metadataToIndex.getString("productName"));
		} catch (Exception e){
			LOGGER.error(e.getMessage());
		}*/
	}
	
	public CountDownLatch getLatch() {
		return this.latch;
	}
}
