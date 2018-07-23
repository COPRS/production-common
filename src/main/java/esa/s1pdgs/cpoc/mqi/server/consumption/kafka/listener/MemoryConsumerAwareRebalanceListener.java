package esa.s1pdgs.cpoc.mqi.server.consumption.kafka.listener;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;

import esa.s1pdgs.cpoc.appcatalog.client.GenericAppCatalogMqiService;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * Rebalance listener when messages are in memory
 * 
 * @author Viveris Technologies
 */
public class MemoryConsumerAwareRebalanceListener
        implements ConsumerAwareRebalanceListener {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(MemoryConsumerAwareRebalanceListener.class);

    /**
     * Service for checking if a message is processing or not by another
     */
    private final GenericAppCatalogMqiService<?> service;

    /**
     * Group name
     */
    private final String group;
    
    /**
     * Default mode
     */
    private final int defaultMode;

    /**
     * Default constructor
     */
    public MemoryConsumerAwareRebalanceListener(
            final GenericAppCatalogMqiService<?> service, final String group, final int defaultMode) {
        super();
        this.service = service;
        this.group = group;
        this.defaultMode = defaultMode;
    }

    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * @return the defaultMode
     */
    public int getDefaultMode() {
        return defaultMode;
    }

    /**
     * 
     */
    @Override
    public void onPartitionsRevokedBeforeCommit(final Consumer<?, ?> consumer,
            final Collection<TopicPartition> partitions) {
        LOGGER.info("onPartitionsRevokedBeforeCommit call");
    }

    /**
     * 
     */
    @Override
    public void onPartitionsRevokedAfterCommit(final Consumer<?, ?> consumer,
            final Collection<TopicPartition> partitions) {
        LOGGER.info("onPartitionsRevokedAfterCommit call");
    }

    /**
     * 
     */
    @Override
    public void onPartitionsAssigned(final Consumer<?, ?> consumer,
            final Collection<TopicPartition> partitions) {
        LOGGER.info("onPartitionsAssigned call");
        // We seek the consumer on the right offset
        Iterator<TopicPartition> topicPartitionIterator = partitions.iterator();
        while (topicPartitionIterator.hasNext()) {
            TopicPartition topicPartition = topicPartitionIterator.next();
            LOGGER.debug("Current offset is {} committed offset is -> {}",
                    consumer.position(topicPartition),
                    consumer.committed(topicPartition));
            long startingOffset = defaultMode;
            try {
                startingOffset =
                        service.getEarliestOffset(topicPartition.topic(),
                                topicPartition.partition(), group);
            } catch (AbstractCodedException ace) {
                LOGGER.error("{} Set default mode: -2");
            }
            if (startingOffset == -2) {
                LOGGER.debug("Leaving it alone");
            } else if (startingOffset == 0) {
                LOGGER.debug("Setting offset to begining");
                consumer.seekToBeginning(Arrays.asList(topicPartition));
            } else if (startingOffset == -1) {
                LOGGER.debug("Setting offset to end");
                consumer.seekToEnd(Arrays.asList(topicPartition));
            } else {
                LOGGER.debug("Resetting offset to {}", startingOffset);
                consumer.seek(topicPartition, startingOffset);
            }
        }
    }

}
