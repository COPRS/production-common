package fr.viveris.s1pdgs.mdcatalog.services.kafka;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.mdcatalog.services.s3.ConfigFilesS3Services;
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
	
	private MetadataBuilder mdBuilder = new MetadataBuilder();

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
					metadataToIndex = mdBuilder.buildEdrsSessionFileMetadata((EdrsSessionFileDescriptor)(metadata.getMetadataToIndex()));
				}
				else if(metadata.getFamilyType().equals("METADATA")) {
					if(configFilesS3Services.exist(metadata.getMetadataToIndex().getKeyObjectStorage())) {
						metadataFile = new File(metadata.getMetadataToIndex().getKeyObjectStorage());
						configFilesS3Services.downloadFile(metadata.getMetadataToIndex().getKeyObjectStorage(), metadataFile);
						metadataToIndex = mdBuilder.buildConfigFileMetadata((ConfigFileDescriptor)(metadata.getMetadataToIndex()), metadataFile);
						if (!metadataFile.delete()) {
							LOGGER.error("[processConfigFile] File {} not removed from local storage", metadataFile.getPath());
						}
					}
					else {
						LOGGER.error("File {} does not exists", metadata.getMetadataToIndex().getProductName());
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
			LOGGER.error("Invalid action {} for metadata {}", metadata.getAction(), metadata.getMetadataToIndex().getProductName());
		}
	}
	
	public CountDownLatch getLatchMetadata() {
		return this.latchMetadata;
	}
}
