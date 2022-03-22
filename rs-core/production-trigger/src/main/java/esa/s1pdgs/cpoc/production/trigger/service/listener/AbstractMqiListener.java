package esa.s1pdgs.cpoc.production.trigger.service.listener;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.production.trigger.service.PreparationJobPublishMessageProducer;
import esa.s1pdgs.cpoc.production.trigger.taskTableMapping.TasktableMapper;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

abstract class AbstractMqiListener<E> implements MqiListener<E> {
    final Logger log = LogManager.getLogger(getClass());	
    
	final TasktableMapper defaultTasktableMapper;
	
	private final String hostname;
	private final ErrorRepoAppender errorRepoAppender;
	private final PreparationJobPublishMessageProducer publishMessageProducer;
	
	AbstractMqiListener(
			final TasktableMapper defaultTasktableMapper, 
			final String hostname,
			final ErrorRepoAppender errorRepoAppender, 
			final PreparationJobPublishMessageProducer publishMessageProducer
	) {
		this.defaultTasktableMapper = defaultTasktableMapper;
		this.hostname = hostname;
		this.errorRepoAppender = errorRepoAppender;
		this.publishMessageProducer = publishMessageProducer;
	}

	@Override
	public final void onTerminalError(final GenericMessageDto<E> message, final Exception error) {
		log.error(error);
        errorRepoAppender.send(
        	new FailedProcessingDto(hostname, new Date(), error.getMessage(), message)
        );
	}
	
	public final MqiMessageEventHandler onCatalogEvent(
			final GenericMessageDto<CatalogEvent> mqiMessage,
			final TasktableMapper ttMapper,
			final String outputProductType
	) throws Exception {
        final CatalogEvent event = mqiMessage.getBody();
        final String productName = event.getProductName();
        
		MissionId mission = MissionId.valueOf((String) event.getMetadata().get(MissionId.FIELD_NAME));

        final Reporting reporting = ReportingUtils.newReportingBuilder(mission)
        		.predecessor(event.getUid())
        		.newReporting("ProductionTrigger");
                
		return new MqiMessageEventHandler.Builder<IpfPreparationJob>(ProductCategory.PREPARATION_JOBS)
				.onSuccess(res -> {
					if (res.size() != 0) {	      
						reporting.end(new ReportingMessage("IpfPreparationJob for product %s created", productName));
					}
				})
				.onError(e -> reporting.error(new ReportingMessage("Error on handling CatalogEvent: %s", LogUtils.toString(e))))
				.publishMessageProducer(() -> publishMessageProducer.createPublishingJob(reporting, mqiMessage, ttMapper, outputProductType))
				.newResult();
	}
}