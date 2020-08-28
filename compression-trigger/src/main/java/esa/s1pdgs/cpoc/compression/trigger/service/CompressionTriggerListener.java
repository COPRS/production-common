package esa.s1pdgs.cpoc.compression.trigger.service;

import java.util.Collections;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.compression.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionDirection;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class CompressionTriggerListener<E extends AbstractMessage> implements MqiListener<E> {
	private static final Logger LOG = LogManager.getLogger(CompressionTriggerListener.class);

	private final CompressionJobMapper<E> mapper;
	private final ErrorRepoAppender errorAppender;
	private final ProcessConfiguration processConfig;

	public CompressionTriggerListener(
			final CompressionJobMapper<E> mapper,
			final ErrorRepoAppender errorAppender,
			final ProcessConfiguration processConfig) {
		this.mapper = mapper;
		this.errorAppender = errorAppender;
		this.processConfig = processConfig;
	}

	@Override
	public final MqiMessageEventHandler onMessage(final GenericMessageDto<E> message) throws Exception {
		final E event = message.getBody();

		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(event.getUid())
				.newReporting("CompressionTrigger");

		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(event.getProductFamily(), event.getKeyObjectStorage()),
				new ReportingMessage("Start handling of event for %s", event.getKeyObjectStorage()));

		return new MqiMessageEventHandler.Builder<CompressionJob>(ProductCategory.COMPRESSION_JOBS)
				.onSuccess(res -> reporting
						.end(new ReportingMessage("Finished handling of event for %s", event.getKeyObjectStorage())))
				.onError(e -> reporting.error(new ReportingMessage("Error on handling event for %s: %s",
						event.getKeyObjectStorage(), LogUtils.toString(e))))
				.publishMessageProducer(() -> {
					final CompressionJob job = mapper.toCompressionJob(event, reporting.getUid());
					if(job.getCompressionDirection() == CompressionDirection.UNDEFINED) {
						LOG.info(String.format("Skipping compressionJob for %s, productFamily: %s", event.getKeyObjectStorage(), event.getProductFamily()));
						return new MqiPublishingJob<CompressionJob>(Collections.emptyList());
					}
					job.setUid(reporting.getUid());
					return new MqiPublishingJob<CompressionJob>(Collections.singletonList(publish(message, job)));
				}).newResult();
	}

	final GenericPublicationMessageDto<CompressionJob> publish(final GenericMessageDto<E> mess,
			final CompressionJob job) {
		final GenericPublicationMessageDto<CompressionJob> messageDto = new GenericPublicationMessageDto<CompressionJob>(
				mess.getId(), job.getProductFamily(), job);
		messageDto.setInputKey(mess.getInputKey());
		messageDto.setOutputKey(job.getOutputProductFamily().name());
		return messageDto;
	}

	@Override
	public final void onTerminalError(final GenericMessageDto<E> message, final Exception error) {
		LOG.error(error);
		errorAppender.send(new FailedProcessingDto(processConfig.getHostname(), new Date(),
				String.format("Error on handling ProductionEvent for %s: %s", message.getBody().getKeyObjectStorage(),
						LogUtils.toString(error)),
				message));
	}

}
