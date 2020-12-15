package esa.s1pdgs.cpoc.dissemination.worker.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.common.utils.Retries;
import esa.s1pdgs.cpoc.dissemination.worker.config.DisseminationWorkerProperties;
import esa.s1pdgs.cpoc.dissemination.worker.config.DisseminationWorkerProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.dissemination.worker.config.DisseminationWorkerProperties.OutboxConnection;
import esa.s1pdgs.cpoc.dissemination.worker.path.PathEvaluator;
import esa.s1pdgs.cpoc.dissemination.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.dissemination.worker.outbox.FtpOutboxClient;
import esa.s1pdgs.cpoc.dissemination.worker.outbox.FtpsOutboxClient;
import esa.s1pdgs.cpoc.dissemination.worker.outbox.OutboxClient;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.DisseminationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.DisseminationSource;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.report.message.output.OutboxReportingOutput;

@Service
public class DisseminationJobListener implements MqiListener<DisseminationJob> {

	private static final Logger LOG = LogManager.getLogger(DisseminationJobListener.class);

	private static final Map<OutboxConfiguration.Protocol, OutboxClient.Factory> OUTBOX_CLIENT_FACTORIES = new HashMap<>();
	static {
		OUTBOX_CLIENT_FACTORIES.put(OutboxConfiguration.Protocol.FTP, new FtpOutboxClient.Factory());
		OUTBOX_CLIENT_FACTORIES.put(OutboxConfiguration.Protocol.FTPS, new FtpsOutboxClient.Factory());
	}

	private final AppStatus appStatus;
	private final GenericMqiClient mqiClient;
	private final ErrorRepoAppender errorAppender;
	private final ObsClient obsClient;
	private final ProcessConfiguration processConfig;
	private final DisseminationWorkerProperties config;
	private final Map<String, OutboxClient> clientsToOutboxes = new HashMap<>();

	// --------------------------------------------------------------------------

	@Autowired
	public DisseminationJobListener(final AppStatus appStatus, final GenericMqiClient mqiClient,
			final ErrorRepoAppender errorAppender, final ObsClient obsClient,
			final ProcessConfiguration processConfig, final DisseminationWorkerProperties config) {
		this.appStatus = appStatus;
		this.mqiClient = mqiClient;
		this.errorAppender = errorAppender;
		this.obsClient = obsClient;

		this.processConfig = processConfig;
		this.config = config;
	}

	@PostConstruct
	public void initListener() {
		// Init outboxes
		for (final Map.Entry<String, OutboxConfiguration> entry : this.config.getOutboxes().entrySet()) {
			final String outboxName = entry.getKey();
			final OutboxConfiguration outboxConfig = entry.getValue();
			final PathEvaluator pathEvaluator = PathEvaluator.newInstance(outboxConfig);

			final OutboxClient outboxClient = OUTBOX_CLIENT_FACTORIES
					.getOrDefault(outboxConfig.getProtocol(), OutboxClient.Factory.NOT_DEFINED_ERROR)
					.newClient(this.obsClient, outboxConfig, pathEvaluator);

			LOG.info("using {} for outbox '{}'", outboxClient, outboxName);
			this.clientsToOutboxes.put(outboxName, outboxClient);
		}
	}

	// --------------------------------------------------------------------------

	@Override
	public MqiMessageEventHandler onMessage(GenericMessageDto<DisseminationJob> message) throws Exception {
		LOG.debug("incoming message: " + message);

		final DisseminationJob job = message.getBody();
		final List<DisseminationSource> filesToDisseminate = job.getDisseminationSources();

		final Reporting reporting = ReportingUtils.newReportingBuilder().predecessor(job.getUid()).newReporting("Dissemination");

		return new MqiMessageEventHandler.Builder<DisseminationJob>(ProductCategory.DISSEMINATION_JOBS)
				.publishMessageProducer(() -> {
					return this.handleDissemination(job, filesToDisseminate, reporting);
				}) //
				.onError(e -> reporting.error(
						new ReportingMessage("Error processing %s: %s", filesToDisseminate, LogUtils.toString(e)))) //
				.newResult();
	}

	@Override
	public void onTerminalError(GenericMessageDto<DisseminationJob> message, Exception error) {
		LOG.error(error);
		this.errorAppender.send(new FailedProcessingDto( //
				this.processConfig.getHostname(),
				new Date(),
				error.getMessage(),
				message));
	}

	// --------------------------------------------------------------------------

	private MqiPublishingJob<DisseminationJob> handleDissemination(final DisseminationJob job,
			final List<DisseminationSource> filesToDisseminate, final Reporting reporting) throws InterruptedException {

		final Map<OutboxConnection, OutboxClient> matchingOutboxes = this
				.findMatchingOutboxes(job.getKeyObjectStorage());

		// make sure the files already exist in storage
		final List<ObsObject> existingFilesToTransfer = this.assertExist(filesToDisseminate);

		final ObsObject mainFile = new ObsObject(job.getProductFamily(), job.getKeyObjectStorage());

		for (final Map.Entry<OutboxConnection, OutboxClient> entry : matchingOutboxes.entrySet()) {
			final OutboxConnection outboxConnection = entry.getKey();
			final String outboxName = outboxConnection.getOutboxName();
			final OutboxClient outboxClient = entry.getValue();

			reporting.begin(
					ReportingUtils.newFilenameReportingInputFor(job.getProductFamily(), job.getKeyObjectStorage()),
					new ReportingMessage("Start dissemination of {} to outbox {}", existingFilesToTransfer, outboxName));
			try {
				final String targetDirectoryUrl = Retries.performWithRetries(
						() -> outboxClient.transfer(mainFile, existingFilesToTransfer, reporting),
						"Transfer of " + existingFilesToTransfer + " to " + outboxName, //
						this.config.getMaxRetries(), this.config.getTempoRetryMs());

				reporting.end(new OutboxReportingOutput(targetDirectoryUrl),
						new ReportingMessage("End dissemination of {} to outbox {}", existingFilesToTransfer, outboxName));
			} catch (final Exception e) {
				final String messageString = this.errorMessageFor(e, outboxName);
				reporting.error(new ReportingMessage(messageString));
				throw new RuntimeException(messageString, e);
			}
		}

		return new MqiPublishingJob<DisseminationJob>(Collections.emptyList());
	}

	private Map<OutboxConnection, OutboxClient> findMatchingOutboxes(String obsKey) {
		final Map<OutboxConnection, OutboxClient> matchingOutboxes = new HashMap<>();

		// TODO: trying to match all obs keys (zip + manifest) instead of only the main file?

		for (final OutboxConnection outboxConnection : CollectionUtil.nullToEmpty(this.config.getOutboxConnections())) {
			LOG.trace("Checking if product {} matches {}", obsKey, outboxConnection.getMatchRegex());
			if (obsKey.matches(outboxConnection.getMatchRegex())) {
				LOG.debug("Found config {} for product {}", outboxConnection, obsKey);
				matchingOutboxes.put(outboxConnection, this.clientForOutbox(outboxConnection.getOutboxName()));
			}
		}

		if (matchingOutboxes.isEmpty()) {
			LOG.info("no matching outbox(es) found for product {}", obsKey);
		}

		return matchingOutboxes;
	}

	private OutboxClient clientForOutbox(final String outboxName) {
		final OutboxClient outboxClient = this.clientsToOutboxes.get(outboxName);

		// assert there is an outbox configured for the given target
		if (outboxClient == null) {
			throw new DisseminationException(String.format("No outbox configured for '%s'. Available are: %s",
					outboxName, this.clientsToOutboxes.keySet()));
		}
		return outboxClient;
	}

	private List<ObsObject> assertExist(final List<DisseminationSource> files) throws InterruptedException {
		return Retries.performWithRetries(() -> {
			return this.assertExistInObs(files);
		}, "assert files exist in OBS: " + files, this.config.getMaxRetries(), this.config.getTempoRetryMs());
	}

	private List<ObsObject> assertExistInObs(final List<DisseminationSource> files)
			throws ObsServiceException, SdkClientException {
		final List<ObsObject> result = new ArrayList<>();

		for (final DisseminationSource file : files) {
			final ObsObject obsObject = new ObsObject(file.getProductFamily(), file.getKeyObjectStorage());

			if (!this.obsClient.prefixExists(obsObject)) {
				throw new DisseminationException(String.format("OBS file '%s' (%s) does not exist",
						file.getKeyObjectStorage(), file.getProductFamily()));
			}
			result.add(obsObject);
		}

		return result;
	}

	private String errorMessageFor(final Exception e, final String outboxName) {
		final String errMessage = (e instanceof DisseminationException) ? e.getMessage() : LogUtils.toString(e);
		return String.format("Error on dissemination of product to outbox %s: %s", outboxName, errMessage);
	}

}
