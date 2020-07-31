package esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.server.config.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.listener.GenericMessageListener;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.listener.MemoryConsumerAwareRebalanceListener;
import esa.s1pdgs.cpoc.mqi.server.service.MessagePersistence;

/**
 * Generic consumer
 * 
 * @author Viveris Technologies
 * @param <T>
 */
public class GenericConsumer<T extends AbstractMessage> {
	public static final class Factory<T extends AbstractMessage> {
		protected static final Logger LOGGER = LogManager.getLogger(Factory.class);
		
	    private final KafkaProperties kafkaProperties;
	    private final MessagePersistence<T> messagePersistence;
		private final AppStatus appStatus;
	    	    
		public Factory(
				final KafkaProperties kafkaProperties, 
				final MessagePersistence<T> messagePersistence,
				final AppStatus appStatus
		) {
			this.kafkaProperties = kafkaProperties;
			this.messagePersistence = messagePersistence;
			this.appStatus = appStatus;
		}

   		// use unique clientId to circumvent 'instance already exists' problem
		public final String clientIdForTopic(final String topic) {
			return kafkaProperties.getClientId() + "-" + 
	    			kafkaProperties.getHostname()  + "-" + 
	    			topic;
		}
		
		public final GenericConsumer<T> newConsumerFor(
				final ProductCategory cat, 
				final int prio, 
				final String topic
		) {
			final GenericConsumer<T> consumer = new GenericConsumer<>(cat,topic,prio);			
			final GenericMessageListener<T> listener = newListenerFor(cat, consumer);
	        final ConcurrentMessageListenerContainer<String,T> container = new ConcurrentMessageListenerContainer<>(
	        		consumerFactory(topic, cat.getDtoClass()),
	                containerProperties(topic, listener)
	        );
	        consumer.setContainer(container);	  
			return consumer;
		}
		
		private GenericMessageListener<T> newListenerFor(
				final ProductCategory cat, 
				final GenericConsumer<T> consumer
		) {
			return new GenericMessageListener<>(cat,
					messagePersistence,
					consumer,
					appStatus);
		}
		
	    private ConsumerFactory<String, T> consumerFactory(final String topic, final Class<T> dtoClass) {
	    	final JsonDeserializer<T> deser = new JsonDeserializer<>(dtoClass);
	    	deser.addTrustedPackages("*");	    	
	    	final ErrorHandlingDeserializer<T> deserializer = new ErrorHandlingDeserializer<>(deser);

	    	deserializer.setFailedDeserializationFunction( (failedDeserializationInfo) -> {
	    		LOGGER.error(
	    				"Error on deserializing element from queue '{}'. Expected json of class {} but was: {}", 
	    				topic,
	    				dtoClass.getName(),
	    				new String(failedDeserializationInfo.getData())
	    		);	    		
	    		return null;
	    	});	    		    	
	        return new DefaultKafkaConsumerFactory<>(
	        		consumerConfig(clientIdForTopic(topic)),
	                new StringDeserializer(),
	                deserializer
	        );
	    }

	    private ContainerProperties containerProperties(final String topic, final MessageListener<String, T> messageListener) {
	        final ContainerProperties containerProp = new ContainerProperties(topic);
	        containerProp.setMessageListener(messageListener);
	        containerProp.setPollTimeout(kafkaProperties.getListener().getPollTimeoutMs());
	        containerProp.setAckMode(AckMode.MANUAL_IMMEDIATE);
	        containerProp.setConsumerRebalanceListener(
	                new MemoryConsumerAwareRebalanceListener(
	                		messagePersistence,
	                		kafkaProperties.getConsumer().getGroupId(),
	                		kafkaProperties.getConsumer().getOffsetDftMode()
	                )
	        );
	        return containerProp;
	    }

	    private Map<String, Object> consumerConfig(final String consumerId) {
	        final Map<String, Object> props = new HashMap<>();
	        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
	        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
	        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
	        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumer().getGroupId());
	        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
	        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, kafkaProperties.getConsumer().getMaxPollIntervalMs());
	        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaProperties.getConsumer().getMaxPollRecords());
	        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaProperties.getConsumer().getSessionTimeoutMs());
	        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, kafkaProperties.getConsumer().getHeartbeatIntvMs()); 
	        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, Collections.singletonList(RoundRobinAssignor.class));
	        props.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerId);
	        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getConsumer().getAutoOffsetReset());	        
	        return props;
	    }
	}
	
    /**
     * ProductCategory handled by this consumer
     */
    private final ProductCategory category;

    /**
     * Topic name
     */
    private final String topic;
    
    /**
     * Topic priority
     */
    private final int priority;

    /**
     * Listener container
     */
    private ConcurrentMessageListenerContainer<String, T> container;

    public GenericConsumer(final ProductCategory cat,final String topic,final int priority) {
    	this.category = cat;
        this.topic = topic;
        this.priority = priority;
    }

    /**
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @return the consumedMsgClass
     */
    public Class<T> getConsumedMsgClass() {
        return category.getDtoClass();
    }

    /**
     * Start the consumer
     */
    public void start() {
        container.start();
    }

    /**
     * Resume the consumer
     */
    public void resume() {
        container.resume();
    }

    /**
     * Pause the consumer
     */
    public void pause() {
        container.pause();
    }
    
    public void stop() {
    	container.stop();
    }

    /**
     * Return true if the container is paused
     */
    public boolean isPaused() {
        return container.isContainerPaused();
    }
    
    // accessors for testing
    final void setContainer(final ConcurrentMessageListenerContainer<String, T> container) {
		this.container = container;
	}
    
    final ConcurrentMessageListenerContainer<String, T> container() {
    	return container;
    }
}
