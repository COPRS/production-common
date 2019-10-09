package esa.s1pdgs.cpoc.prip.service.metadata;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.prip.model.Checksum;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;

@Service
public class PripMetadataListener implements MqiListener<ProductDto> {

	private static final Logger LOGGER = LogManager.getLogger(PripMetadataListener.class);
	
	private final GenericMqiClient mqiClient;
	
	private final ObsClient obsClient;
	
	private final long pollingIntervalMs;
	
	private final long pollingInitialDelayMs;
	
	private final PripMetadataRepository pripMetadataRepo;
	
	@Autowired
	public PripMetadataListener(
			final GenericMqiClient mqiClient,
			final ObsClient obsClient,
			final PripMetadataRepository pripMetadataRepo,
			@Value("${metadatalistener.polling-interval-ms}") final long pollingIntervalMs,
			@Value("${metadatalistener.polling-initial-delay-ms}") final long pollingInitialDelayMs) {
		this.mqiClient = mqiClient;
		this.obsClient = obsClient;
		this.pripMetadataRepo = pripMetadataRepo;
		this.pollingIntervalMs = pollingIntervalMs;
		this.pollingInitialDelayMs = pollingInitialDelayMs;
	}
	
	@PostConstruct
	public void initService() {
		if (pollingIntervalMs > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(new MqiConsumer<ProductDto>(mqiClient, ProductCategory.COMPRESSED_PRODUCTS, this,
					pollingIntervalMs, pollingInitialDelayMs, esa.s1pdgs.cpoc.status.AppStatus.NULL));
		}
	}

	
	@Override
	public void onMessage(GenericMessageDto<ProductDto> message) {
		
		LOGGER.debug("starting saving PRIP metadata, got message: {}", message);
		
		ProductDto productDto = message.getBody();
		LocalDateTime creationDate = LocalDateTime.now();
		
		PripMetadata pripMetadata = new PripMetadata();
		pripMetadata.setId(UUID.randomUUID());
		pripMetadata.setObsKey(productDto.getKeyObjectStorage());
		pripMetadata.setName(productDto.getProductName());
		pripMetadata.setContentType(PripMetadata.DEFAULT_CONTENTTYPE);
		pripMetadata.setContentLength(getContentLength());
		pripMetadata.setCreationDate(creationDate);
		pripMetadata.setEvictionDate(creationDate.plusDays(PripMetadata.DEFAULT_EVICTION_DAYS));
		pripMetadata.setChecksums(getChecksums());
		
		pripMetadataRepo.save(pripMetadata);
		
		LOGGER.debug("end of saving PRIP metadata: {}", pripMetadata);
	}

	private long getContentLength() {
		//TODO: obsClient.getContentLength
		return 0;
	}

	private List<Checksum> getChecksums() {
		//TODO: obsClient.getChecksum
		Checksum checksum = new Checksum();
		checksum.setAlgorithm(Checksum.DEFAULT_ALGORITHM);
		checksum.setValue("");
		return Arrays.asList(checksum);
	}

}
