package esa.s1pdgs.cpoc.mqi.server.consumption.kafka.listener;

import java.util.Collection;
import java.util.Set;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;
import org.springframework.util.CollectionUtils;

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
     * Indicates if the consumer was is pause before rebalance
     */
    private boolean isPaused;
    
    /**
     * Default constructor
     */
    public MemoryConsumerAwareRebalanceListener() {
        super();
        isPaused = false;
    }

    /**
     * @return the isPaused
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * 
     */
    @Override
    public void onPartitionsRevokedBeforeCommit(final Consumer<?, ?> consumer,
            final Collection<TopicPartition> partitions) {
        LOGGER.info("onPartitionsRevokedBeforeCommit call");
        Set<TopicPartition> pausedP = consumer.paused();
        if (CollectionUtils.isEmpty(pausedP)) {
            isPaused = false;
            LOGGER.info("onPartitionsRevokedBeforeCommit call paused = false");
        } else {
            isPaused = true;
            LOGGER.info("onPartitionsRevokedBeforeCommit call paused = true");
        }
    }

    /**
     * 
     */
    @Override
    public void onPartitionsRevokedAfterCommit(final Consumer<?, ?> consumer,
            final Collection<TopicPartition> partitions) {
        LOGGER.info("onPartitionsRevokedAfterCommit call");
        Set<TopicPartition> pausedP = consumer.paused();
        if (CollectionUtils.isEmpty(pausedP)) {
            isPaused = false;
            LOGGER.info("onPartitionsRevokedAfterCommit call paused = false");
        } else {
            isPaused = true;
            LOGGER.info("onPartitionsRevokedAfterCommit call paused = true");
        }
    }

    /**
     * 
     */
    @Override
    public void onPartitionsAssigned(final Consumer<?, ?> consumer,
            final Collection<TopicPartition> partitions) {
        LOGGER.info("onPartitionsAssigned call");
        if (isPaused) {
            LOGGER.info("onPartitionsAssigned call set pause = true");
            consumer.pause(partitions);
        }
    }

}
