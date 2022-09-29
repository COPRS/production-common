package esa.s1pdgs.cpoc.production.trigger.service.listener;

import java.util.Collections;
import java.util.Map;

import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.OnDemandEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.production.trigger.service.PreparationJobPublishMessageProducer;
import esa.s1pdgs.cpoc.production.trigger.taskTableMapping.TasktableMapper;

public final class OnDemandEventListener extends AbstractMqiListener<OnDemandEvent> {
	public OnDemandEventListener(
			final TasktableMapper defaultTasktableMapper, 
			final String hostname,
			final ErrorRepoAppender errorRepoAppender,
			final PreparationJobPublishMessageProducer publishMessageProducer
	) {
		super(defaultTasktableMapper, hostname, errorRepoAppender, publishMessageProducer);
	}

	@Override
	public final MqiMessageEventHandler onMessage(final GenericMessageDto<OnDemandEvent> message) throws Exception {		
		final OnDemandEvent onDemandEvent = message.getBody();

		return onCatalogEvent(
				toCatalogEvent(message), 
				tasktableMapperFor(onDemandEvent), 
				outputProductTypeFor(onDemandEvent)
		);			
	}
	
	private final String outputProductTypeFor(final OnDemandEvent onDemandEvent) {
		// S1PRO-2601: even if an 'outputProductType' is defined in the request, the debug flag takes a higher
		// precedence and will upload the workingdir into the debug bucket
		if (!onDemandEvent.isDebug()) {
			return onDemandEvent.getOutputProductType();
		}
		// 'null' is a valid scenario here and means, no outputProductType filtering will be performed		
		return null;		
	}
	
	private final TasktableMapper tasktableMapperFor(final OnDemandEvent onDemandEvent) {
		// S1PRO-2601: only use the specified tasktableName if it has been provided with the request, otherwise
		// use default tasktableMapping logic.			
		if (StringUtil.isNotEmpty(onDemandEvent.getTasktableName())) {
			log.debug("Using tasktable {} as specified in OnDemandEvent", onDemandEvent.getTasktableName());
			return p -> Collections.singletonList(onDemandEvent.getTasktableName());
		}
		return defaultTasktableMapper;
	}
	
    // dirty workaround to avoid changing appDataJob persistence
    private final GenericMessageDto<CatalogEvent> toCatalogEvent(final GenericMessageDto<OnDemandEvent> mess) {
    	final OnDemandEvent onDemandEvent = mess.getBody();
    	
    	final CatalogEvent catEvent = new CatalogEvent();
    	final Map<String, Object> metadata = onDemandEvent.getMetadata();
    	catEvent.setMetadata(metadata);	
		catEvent.setMetadataProductName(onDemandEvent.getProductName());
		catEvent.setKeyObjectStorage(onDemandEvent.getKeyObjectStorage());
		catEvent.setProductFamily(onDemandEvent.getProductFamily());
		catEvent.setMetadataProductType(metadata.get("productType").toString());	
		
    	catEvent.setAllowedActions(onDemandEvent.getAllowedActions());
    	catEvent.setDebug(onDemandEvent.isDebug());
    	catEvent.setDemandType(onDemandEvent.getDemandType());
    	
    	return new GenericMessageDto<CatalogEvent>(mess.getId(), mess.getInputKey(), catEvent);
    }
	
}