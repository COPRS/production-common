package esa.s1pdgs.cpoc.production.trigger.service.listener;

import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.production.trigger.service.PreparationJobPublishMessageProducer;
import esa.s1pdgs.cpoc.production.trigger.taskTableMapping.TasktableMapper;

public final class CatalogEventListener extends AbstractMqiListener<CatalogEvent> {	
	public CatalogEventListener(
			final TasktableMapper defaultTasktableMapper, 
			final String hostname,
			final ErrorRepoAppender errorRepoAppender,
			final PreparationJobPublishMessageProducer publishMessageProducer
	) {
		super(defaultTasktableMapper, hostname, errorRepoAppender, publishMessageProducer);
	}

	@Override
	public final MqiMessageEventHandler onMessage(final GenericMessageDto<CatalogEvent> mqiMessage) throws Exception {
		return onCatalogEvent(mqiMessage, defaultTasktableMapper, null);
	}
}