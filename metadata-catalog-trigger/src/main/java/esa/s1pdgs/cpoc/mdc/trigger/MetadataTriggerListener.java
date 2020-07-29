package esa.s1pdgs.cpoc.mdc.trigger;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mdc.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public final class MetadataTriggerListener<E extends AbstractMessage> implements MqiListener<E> {
	private static final Logger LOG = LogManager.getLogger(MetadataTriggerListener.class);
	
	private final CatalogJobMapper<E> mapper;
	private final ErrorRepoAppender errorAppender;
	private final ProcessConfiguration processConfig;

	public MetadataTriggerListener(
			final CatalogJobMapper<E> mapper, 
			final ErrorRepoAppender errorAppender,
			final ProcessConfiguration processConfig
	) {
		this.mapper = mapper;
		this.errorAppender = errorAppender;
		this.processConfig = processConfig;
	}

	@Override
	public final MqiMessageEventHandler onMessage(final GenericMessageDto<E> message) throws Exception {
		final E dto = message.getBody();
		final String eventType = dto.getClass().getSimpleName();
		
		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(dto.getUid())
				.newReporting("MetadataTrigger");
		
		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(dto.getProductFamily(), dto.getKeyObjectStorage()),
				new ReportingMessage("Received %s", eventType)
		);	
		
		return new MqiMessageEventHandler.Builder<CatalogJob>(ProductCategory.CATALOG_JOBS)
				.onSuccess(res -> reporting.end(new ReportingMessage("Created CatalogJob for %s", eventType)))
				.onError(e -> reportError(eventType, reporting, e))
				.messageHandling(() -> newPublicationMessage(reporting, message))
				.newResult();
	}
	
	@Override
	public final void onTerminalError(final GenericMessageDto<E> message, final Exception error) {
		LOG.error(error);
		errorAppender.send(new FailedProcessingDto(
				processConfig.getHostname(), 
				new Date(), 
				error.getMessage(), 
				message
		));
	}
	
	private final List<GenericPublicationMessageDto<CatalogJob>> newPublicationMessage(
			final Reporting reporting, 
			final GenericMessageDto<E> message
	) {
		final CatalogJob job = mapper.toCatJob(message.getBody(), reporting.getUid());			
    	final GenericPublicationMessageDto<CatalogJob> messageDto = new GenericPublicationMessageDto<CatalogJob>(
    			message.getId(), 
    			job.getProductFamily(), 
    			job
    	);
    	messageDto.setInputKey(message.getInputKey());
    	messageDto.setOutputKey(job.getProductFamily().name());
    	return Collections.singletonList(messageDto);
	}

	private final void reportError(final String eventType, final Reporting reporting, final Exception e) {			
		reporting.error(new ReportingMessage(String.format("Error on handling %s: %s", eventType, LogUtils.toString(e))));			
	}

}