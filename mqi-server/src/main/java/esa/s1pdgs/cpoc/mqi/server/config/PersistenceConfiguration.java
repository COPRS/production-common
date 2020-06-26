package esa.s1pdgs.cpoc.mqi.server.config;

import static esa.s1pdgs.cpoc.mqi.server.config.MessagePersistenceStrategy.APP_CATALOG_MESSAGE_PERSISTENCE;
import static esa.s1pdgs.cpoc.mqi.server.config.MessagePersistenceStrategy.IN_MEMORY_MESSAGE_PERSISTENCE;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.appcatalog.client.mqi.AppCatalogMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.server.service.AppCatalogMessagePersistence;
import esa.s1pdgs.cpoc.mqi.server.service.InMemoryMessagePersistence;
import esa.s1pdgs.cpoc.mqi.server.service.MessagePersistence;
import esa.s1pdgs.cpoc.mqi.server.service.OtherApplicationService;

/**
 * Configuration of applicative catalog client for data persistence.<br/>
 * Creation of 5 services, one per product category
 * 
 * @author Viveris Technologies
 */
@Configuration
public class PersistenceConfiguration<T extends AbstractMessage> {

    private static final Logger LOG = LogManager.getLogger(PersistenceConfiguration.class);

	final static List<MessagePersistenceStrategy> SUPPORTED_MESSAGE_PERSISTENCE_STRATEGIES =
			Stream.of(MessagePersistenceStrategy.values()).collect(Collectors.toList());
	
	private final String messagePersistenceStrategy;
	
    /**
     * Host URI for the applicative catalog
     */
    private final String hostUriCatalog;
    
    /**
     * 
     */
    private final String portUriOtherApp;

    /**
     * Maximal number of retries when query fails
     */
    private final int maxRetries;

    /**
     * Temporization in ms between 2 retries
     */
    private final int tempoRetryMs;

    /**
     * 
     */
    private final String suffixUriOtherApp;

    /**
     * Constructor
     * 
     */
    @Autowired
    public PersistenceConfiguration(
    		@Value("${persistence.message-persistence-strategy:AppCatalogMessagePersistence}") final String messagePersistenceStrategy,
            @Value("${persistence.host-uri-catalog}") final String hostUriCatalog,
            @Value("${persistence.port-uri-other-app}") final String portUriOtherApp,
            @Value("${persistence.max-retries}") final int maxRetries,
            @Value("${persistence.tempo-retry-ms}") final int tempoRetryMs,
            @Value("${persistence.other-app.suffix-uri}") final String suffixUriOtherApp) {
    	this.messagePersistenceStrategy = messagePersistenceStrategy;
        this.hostUriCatalog = hostUriCatalog;
        this.maxRetries = maxRetries;
        this.tempoRetryMs = tempoRetryMs;
        this.portUriOtherApp = portUriOtherApp;
        this.suffixUriOtherApp = suffixUriOtherApp;
    }

    @Bean
    public MessagePersistence<T> messagePersistence(final RestTemplateBuilder builder, final AppCatalogMqiService<T> mqiService, KafkaProperties properties, @Value("${mqi.dft-offset:-3}") final int defaultOffset) {
        LOG.info("using message persistence strategy {}", messagePersistenceStrategy);
        if (APP_CATALOG_MESSAGE_PERSISTENCE.getValue().equals(messagePersistenceStrategy)) {
            final OtherApplicationService otherAppService = checkProcessingOtherApp(builder);
            return new AppCatalogMessagePersistence<>(mqiService, properties, otherAppService);
        } else if (IN_MEMORY_MESSAGE_PERSISTENCE.getValue().equals(messagePersistenceStrategy)) {
            return new InMemoryMessagePersistence<>(properties, defaultOffset);
        } else {
            throw new IllegalArgumentException(String.format("Unknown message persistence strategy %s. Available are %s.", messagePersistenceStrategy, SUPPORTED_MESSAGE_PERSISTENCE_STRATEGIES));
        }
    }
    
    /**
     * Service for querying MQI for LEVEL_PRODUCT category
     * 
     */
    @Bean
    public AppCatalogMqiService<T> appCatService(final RestTemplateBuilder builder) {
        return new AppCatalogMqiService<>(builder.build(), hostUriCatalog, maxRetries, tempoRetryMs);
    }

    /**
     * Service for checking if a message is processing or not by another app
     */
    @Bean(name = "checkProcessingOtherApp")
    public OtherApplicationService checkProcessingOtherApp(final RestTemplateBuilder builder) {
        return new OtherApplicationService(builder.build(), portUriOtherApp,
                maxRetries, tempoRetryMs, suffixUriOtherApp);
    }
    

}
