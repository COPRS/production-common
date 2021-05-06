package esa.s1pdgs.cpoc.production.trigger.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.OnDemandEvent;
import esa.s1pdgs.cpoc.production.trigger.config.ProcessSettings;
import esa.s1pdgs.cpoc.production.trigger.service.listener.CatalogEventListener;
import esa.s1pdgs.cpoc.production.trigger.service.listener.OnDemandEventListener;
import esa.s1pdgs.cpoc.production.trigger.taskTableMapping.TasktableMapper;

public class GenericConsumer {	
    private final ProcessSettings processSettings;
    private final GenericMqiClient mqiClient;
    private final List<MessageFilter> messageFilter;
    private final AppStatus appStatus;    
    private final ErrorRepoAppender errorRepoAppender;    
    private final TasktableMapper taskTableMapper;
    private final PreparationJobPublishMessageProducer pubMessageProducer;
    
    public GenericConsumer(
            final ProcessSettings processSettings,
            final GenericMqiClient mqiService,
            final List<MessageFilter> messageFilter,
            final AppStatus appStatus,
            final ErrorRepoAppender errorRepoAppender,
            final TasktableMapper taskTableMapper,
            final PreparationJobPublishMessageProducer pubMessageProducer
    ) {
        this.processSettings = processSettings;
        this.mqiClient = mqiService;
        this.messageFilter = messageFilter;
        this.appStatus = appStatus;
        this.errorRepoAppender = errorRepoAppender;
		this.taskTableMapper = taskTableMapper;
		this.pubMessageProducer = pubMessageProducer;
    }

    @PostConstruct
	public void initService() {
		appStatus.setWaiting();
				
		// since on demand events are all on the same kafka topic, each production trigger needs to 
		// ignore the events of production trigger that are handling a different applicationLevel
		final List<MessageFilter> onDemandMessageFilter = new ArrayList<>();
		onDemandMessageFilter.add(new MessageFilter() {			
			@Override
			public boolean accept(final AbstractMessage message) {
				if (message.getClass().equals(OnDemandEvent.class)) {
					return ((OnDemandEvent)message).getProductionType() == processSettings.getLevel();
				}
				return false;
			}
		});
		onDemandMessageFilter.addAll(messageFilter);
		
		if (processSettings.getFixedDelayMs() > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(2);
			
			service.execute(new MqiConsumer<CatalogEvent>(
	    			mqiClient, 
	    			ProductCategory.CATALOG_EVENT, 
	    			new CatalogEventListener(
	    					taskTableMapper,
	    					processSettings.getHostname(), 
	    					errorRepoAppender, 
	    					pubMessageProducer
	    			),
	    			messageFilter,
	    			processSettings.getFixedDelayMs(),
					processSettings.getInitialDelayMs(), 
					appStatus
			));
			
			service.execute(new MqiConsumer<OnDemandEvent>(
	    			mqiClient, 
	    			ProductCategory.ON_DEMAND_EVENT, 	    			
	    			new OnDemandEventListener(
	    					taskTableMapper,
	    					processSettings.getHostname(), 
	    					errorRepoAppender, 
	    					pubMessageProducer
	    			),
	    			onDemandMessageFilter,
	    			processSettings.getFixedDelayMs(),
					processSettings.getInitialDelayMs(), 
					appStatus
			));
		}
	} 

}
