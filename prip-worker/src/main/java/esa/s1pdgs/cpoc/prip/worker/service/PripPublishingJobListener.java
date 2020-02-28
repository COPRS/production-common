package esa.s1pdgs.cpoc.prip.worker.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
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

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
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
import esa.s1pdgs.cpoc.prip.worker.report.PripReportingInput;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

@Service
public class PripPublishingJobListener implements MqiListener<PripPublishingJob> {

	private static final Logger LOGGER = LogManager.getLogger(PripPublishingJobListener.class);

	private final GenericMqiClient mqiClient;
	private final ObsClient obsClient;
	private final long pollingIntervalMs;
	private final long pollingInitialDelayMs;
	private final PripMetadataRepository pripMetadataRepo;
	private final AppStatus appStatus;

	@Autowired
	public PripPublishingJobListener(
			final GenericMqiClient mqiClient, 
			final ObsClient obsClient,
			final PripMetadataRepository pripMetadataRepo,
			@Value("${prip-worker.publishing-job-listener.polling-interval-ms}") final long pollingIntervalMs,
			@Value("${prip-worker.publishing-job-listener.polling-initial-delay-ms}") final long pollingInitialDelayMs,
			final AppStatus appStatus
	) {
		this.mqiClient = mqiClient;
		this.obsClient = obsClient;
		this.pripMetadataRepo = pripMetadataRepo;
		this.pollingIntervalMs = pollingIntervalMs;
		this.pollingInitialDelayMs = pollingInitialDelayMs;
		this.appStatus = appStatus;
	}
	
	@PostConstruct
	public void initService() {
		if (pollingIntervalMs > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(new MqiConsumer<PripPublishingJob>(
					mqiClient, 
					ProductCategory.PRIP_JOBS, 
					this,
					pollingIntervalMs, 
					pollingInitialDelayMs, 
					appStatus
			));
		}
	}
	


	@Override
	public void onMessage(final GenericMessageDto<PripPublishingJob> message) {

		LOGGER.debug("starting saving PRIP metadata, got message: {}", message);

		final PripPublishingJob publishingJob = message.getBody();
		
		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(publishingJob.getUid())
				.newReporting("PripWorker");
		
		final String name = removeZipSuffix(publishingJob.getKeyObjectStorage());
		
		reporting.begin(
				new PripReportingInput(name, new Date()),
				new ReportingMessage("Publishing file %s in PRIP", name)
		);
		
		try {
			final LocalDateTime creationDate = LocalDateTime.now();

			final PripMetadata pripMetadata = new PripMetadata();
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
			reporting.end(new ReportingMessage("Finished publishing file %s in PRIP", name));
		} catch (final Exception e) {
			reporting.end(new ReportingMessage("Error on publishing file %s in PRIP: %s", name, LogUtils.toString(e)));
			throw e;
		}
	}

	private long getContentLength(final ProductFamily family, final String key) {
		long contentLength = 0;
		try {
			contentLength = obsClient.size(new ObsObject(family, key));

		} catch (final ObsException e) {
			LOGGER.warn(String.format("could not determine content length of %s", key), e);
		}
		return contentLength;
	}

	private List<Checksum> getChecksums(final ProductFamily family, final String key) {
		final Checksum checksum = new Checksum();
		checksum.setAlgorithm("");
		checksum.setValue("");
		try {
			final String value = obsClient.getChecksum(new ObsObject(family, key));
			checksum.setAlgorithm(Checksum.DEFAULT_ALGORITHM);
			checksum.setValue(value);
		} catch (final ObsException e) {
			LOGGER.warn(String.format("could not determine checksum of %s", key), e);

		}
		return Arrays.asList(checksum);
	}
	
	static final String removeZipSuffix(final String name) {
		if (name.toLowerCase().endsWith(".zip")){
			return name.substring(0, name.length() - ".zip".length());
		}
		return name;
	}
}
