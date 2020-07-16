package esa.s1pdgs.cpoc.production.trigger.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.production.trigger.service.GenericConsumer;
import esa.s1pdgs.cpoc.production.trigger.taskTableMapping.TasktableMapper;

@Configuration
public class TriggerConfig {		
	private final ProcessSettings processSettings; 
	private final GenericMqiClient mqiService;
	private final List<MessageFilter> messageFilter;
	private final ErrorRepoAppender errorRepoAppender;
	private final AppStatus appStatus;
	private final MetadataClient metadataClient;
	private final TasktableMapper taskTableMapper;
	
	@Autowired
	public TriggerConfig(
			final RestTemplateBuilder restTemplateBuilder,
			final ProcessSettings processSettings, 
			final GenericMqiClient mqiService,
			final List<MessageFilter> messageFilter,
			final ErrorRepoAppender errorRepoAppender, 
			final AppStatus appStatus,
			final MetadataClient metadataClient,
			final TasktableMapper taskTableMapper
	) {
		this.processSettings = processSettings;
		this.mqiService = mqiService;
		this.messageFilter = messageFilter;
		this.errorRepoAppender = errorRepoAppender;
		this.appStatus = appStatus;
		this.metadataClient = metadataClient;
		this.taskTableMapper = taskTableMapper;
	}
	
	@Bean	
	@Autowired
	public GenericConsumer newConsumer(final AppCatalogJobClient appCatClient) {		
		return new GenericConsumer(
				processSettings, 
				mqiService, 
				messageFilter,
				appStatus,
				errorRepoAppender, 
				metadataClient,
				taskTableMapper
		);
	}
}
