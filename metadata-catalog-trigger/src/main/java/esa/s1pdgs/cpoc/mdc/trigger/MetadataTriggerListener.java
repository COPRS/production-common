package esa.s1pdgs.cpoc.mdc.trigger;

import static esa.s1pdgs.cpoc.common.ProductFamily.AUXILIARY_FILE_ZIP;
import static esa.s1pdgs.cpoc.common.ProductFamily.PLAN_AND_REPORT_ZIP;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mdc.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public final class MetadataTriggerListener<E extends AbstractMessage> implements MqiListener<E> {
	private static final Logger LOG = LogManager.getLogger(MetadataTriggerListener.class);
	
	private final CatalogJobMapper<E> mapper;
	private final MqiClient mqiClient;
	private final ErrorRepoAppender errorAppender;
	private final ProcessConfiguration processConfig;

	public MetadataTriggerListener(
			final CatalogJobMapper<E> mapper, 
			final MqiClient mqiClient, 
			final ErrorRepoAppender errorAppender,
			final ProcessConfiguration processConfig
	) {
		this.mapper = mapper;
		this.mqiClient = mqiClient;
		this.errorAppender = errorAppender;
		this.processConfig = processConfig;
	}

	@Override
	public final void onMessage(final GenericMessageDto<E> message) throws Exception {
		final E dto = message.getBody();

		if (this.omitMessage(dto)) {
			return;
		}
		
		final String eventType = dto.getClass().getSimpleName();
		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(dto.getUid())
				.newReporting("MetadataTrigger");
		
		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(dto.getProductFamily(), dto.getKeyObjectStorage()),
				new ReportingMessage("Received %s", eventType)
		);			
		try {				
			final CatalogJob job = mapper.toCatJob(dto, reporting.getUid());
			
	    	final GenericPublicationMessageDto<CatalogJob> messageDto = new GenericPublicationMessageDto<CatalogJob>(
	    			message.getId(), 
	    			job.getProductFamily(), 
	    			job
	    	);
	    	messageDto.setInputKey(message.getInputKey());
	    	messageDto.setOutputKey(job.getProductFamily().name());
			mqiClient.publish(messageDto, ProductCategory.CATALOG_JOBS);
			reporting.end(new ReportingMessage("Created CatalogJob for %s", eventType));
		} 
		catch (final Exception e) {
			final String errorMessage = String.format("Error on handling %s: %s", eventType, LogUtils.toString(e));				
			reporting.error(new ReportingMessage(errorMessage));			
			throw new RuntimeException(errorMessage, e);
		}			
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
	
	// --------------------------------------------------------------------------
	
	private boolean omitMessage(E message) {
		if (message instanceof IngestionEvent && //
				(AUXILIARY_FILE_ZIP == message.getProductFamily()
						|| PLAN_AND_REPORT_ZIP == message.getProductFamily())) {
			// omit ingestion events from zipped backdoor products, they become relevant for metadata catalog after they have been uncompressed
			return true;
		}

		return false;
	}
	
}