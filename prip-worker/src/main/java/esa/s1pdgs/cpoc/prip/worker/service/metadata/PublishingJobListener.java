package esa.s1pdgs.cpoc.prip.worker.service.metadata;

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
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.PripPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;
import esa.s1pdgs.cpoc.prip.model.Checksum;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;

@Service
public class PublishingJobListener implements MqiListener<PripPublishingJob> {

	private static final Logger LOGGER = LogManager.getLogger(PublishingJobListener.class);

	private final GenericMqiClient mqiClient;

	private final ObsClient obsClient;

	private final long pollingIntervalMs;

	private final long pollingInitialDelayMs;

	private final PripMetadataRepository pripMetadataRepo;

	@Autowired
	public PublishingJobListener(final GenericMqiClient mqiClient, final ObsClient obsClient,
			final PripMetadataRepository pripMetadataRepo,
			@Value("${prip-worker.publishing-job-listener.polling-interval-ms}") final long pollingIntervalMs,
			@Value("${prip-worker.publishing-job-listener.polling-initial-delay-ms}") final long pollingInitialDelayMs) {
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
			service.execute(new MqiConsumer<PripPublishingJob>(mqiClient, ProductCategory.PRIP_JOBS, this,
					pollingIntervalMs, pollingInitialDelayMs, esa.s1pdgs.cpoc.appstatus.AppStatus.NULL));
		}
	}

	@Override
	public void onMessage(GenericMessageDto<PripPublishingJob> message) {

		LOGGER.debug("starting saving PRIP metadata, got message: {}", message);

		PripPublishingJob publishingJob = message.getBody();
		LocalDateTime creationDate = LocalDateTime.now();

		PripMetadata pripMetadata = new PripMetadata();
		pripMetadata.setId(UUID.randomUUID());
		pripMetadata.setObsKey(publishingJob.getKeyObjectStorage());
		pripMetadata.setName(publishingJob.getKeyObjectStorage());
		pripMetadata.setProductFamily(publishingJob.getProductFamily());
		pripMetadata.setContentType(PripMetadata.DEFAULT_CONTENTTYPE);
		pripMetadata.setContentLength(getContentLength(publishingJob.getProductFamily(), publishingJob.getKeyObjectStorage()));
		pripMetadata.setCreationDate(creationDate);
		pripMetadata.setEvictionDate(creationDate.plusDays(PripMetadata.DEFAULT_EVICTION_DAYS));
		pripMetadata.setChecksums(getChecksums(publishingJob.getProductFamily(), publishingJob.getKeyObjectStorage()));

		pripMetadataRepo.save(pripMetadata);

		LOGGER.debug("end of saving PRIP metadata: {}", pripMetadata);
	}

	private long getContentLength(ProductFamily family, String key) {
		long contentLength = 0;
		try {
			contentLength = obsClient.size(new ObsObject(family, key));

		} catch (ObsException e) {
			LOGGER.warn(String.format("could not determine content length of %s", key), e);
		}
		return contentLength;
	}

	private List<Checksum> getChecksums(ProductFamily family, String key) {
		Checksum checksum = new Checksum();
		checksum.setAlgorithm("");
		checksum.setValue("");
		try {
			String value = obsClient.getChecksum(new ObsObject(family, key));
			checksum.setAlgorithm(Checksum.DEFAULT_ALGORITHM);
			checksum.setValue(value);
		} catch (ObsException e) {
			LOGGER.warn(String.format("could not determine checksum of %s", key), e);

		}
		return Arrays.asList(checksum);
	}
}
