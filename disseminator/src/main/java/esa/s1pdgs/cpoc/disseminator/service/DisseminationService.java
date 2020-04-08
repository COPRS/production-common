package esa.s1pdgs.cpoc.disseminator.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.disseminator.DisseminationTriggerListener;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.DisseminationTypeConfiguration;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

@Service
public class DisseminationService {	
	private static final Logger LOG = LogManager.getLogger(DisseminationService.class);
	
	final static List<ProductCategory> SUPPORTED_EVENTS = Arrays.asList(
			ProductCategory.CATALOG_EVENT, ProductCategory.INGESTION_EVENT, ProductCategory.PRODUCTION_EVENT);
	
	private final GenericMqiClient client;
    private final ObsClient obsClient;
	private final DisseminationProperties properties;
	private final ErrorRepoAppender errorAppender;
	private final AppStatus appStatus;
	
	private final List<MessageFilter> messageFilter;
	
	@Autowired
	public DisseminationService(
			final GenericMqiClient client,
			final List<MessageFilter> messageFilter,
		    final ObsClient obsClient,
			final DisseminationProperties properties,
			final ErrorRepoAppender errorAppender,
			final AppStatus appStatus
	) {
		this.client = client;
		this.messageFilter = messageFilter;
		this.obsClient = obsClient;
		this.properties = properties;
		this.errorAppender = errorAppender;
		this.appStatus = appStatus;
	}
	
    @PostConstruct
    public void initService() {
        // Init the list of consumers and start them
    	final ExecutorService service = Executors.newFixedThreadPool(properties.getCategories().size());

    	final Map<Class<?>,DisseminationTriggerListener<?>> disseminationTriggerListeners = SUPPORTED_EVENTS.stream()
    			.collect(Collectors.toMap(s -> s.getDtoClass(), s -> newDisseminationTriggerListenerFor(s)));
    	
		for (final Map.Entry<ProductCategory, List<DisseminationTypeConfiguration>> entry : properties.getCategories().entrySet()) {	
			// start consumer for each category
			LOG.debug("Starting consumer for {}", entry);
			ProductCategory category = entry.getKey();
			DisseminationTriggerListener<?> mqiListener = disseminationTriggerListeners.get(category.getDtoClass());
			if (null == mqiListener) {
				throw new IllegalArgumentException(String.format(
						"Invalid product category %s. Available are %s", 
						category, 
						SUPPORTED_EVENTS
				));
			}
			
			mqiListener.initListener();
			
			service.execute(
					MqiConsumer.valueOf(
					category.getDtoClass(),
					client,
					category,
					mqiListener,
					messageFilter,
					properties.getPollingIntervalMs(),
					0L,
					appStatus
			));
    	}
    }

	private final DisseminationTriggerListener<?> newDisseminationTriggerListenerFor(final ProductCategory category) {
		LOG.debug("Creating DisseminationTriggerListener for category {}", category);
		if (SUPPORTED_EVENTS.contains(category)) {
				return DisseminationTriggerListener.valueOf(category.getDtoClass(), obsClient, properties, errorAppender);
		}
		throw new IllegalArgumentException(String.format(
				"Invalid product category %s. Available are %s", 
				category, 
				SUPPORTED_EVENTS
		));
	}
}

