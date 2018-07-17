package fr.viveris.s1pdgs.mqi.server.consumption.kafka.listener;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashSet;
import java.util.Set;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import fr.viveris.s1pdgs.mqi.server.consumption.kafka.listener.MemoryConsumerAwareRebalanceListener;

/**
 * Test the MemoryConsumerAwareRebalanceListener
 * @author Viveris Technologies
 *
 */
public class MemoryConsumerAwareRebalanceListenerTest {

    /**
     * Mock consumer
     */
    @Mock
    private Consumer<?, ?> consumer;

    /**
     * Initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Check when consumer is in pause before rebalance, it must be in pause after
     */
    @Test
    public void testRebalanceConsumerPause() {

        MemoryConsumerAwareRebalanceListener listener =
                new MemoryConsumerAwareRebalanceListener();

        Set<TopicPartition> pausedPartitions = new HashSet<>();
        pausedPartitions.add(new TopicPartition("topic", 0));
        Set<TopicPartition> partitions = new HashSet<>();
        partitions.add(new TopicPartition("topic", 0));
        partitions.add(new TopicPartition("topic", 1));
        partitions.add(new TopicPartition("topic", 2));
        // Mock consumer
        doNothing().when(consumer).pause(Mockito.any());
        doReturn(pausedPartitions).when(consumer).paused();

        // Before rebalance
        listener.onPartitionsRevokedBeforeCommit(consumer, partitions);
        assertTrue(listener.isPaused());
        verify(consumer, times(1)).paused();
        verifyNoMoreInteractions(consumer);
        
        listener.onPartitionsRevokedAfterCommit(consumer, partitions);
        assertTrue(listener.isPaused());
        verify(consumer, times(2)).paused();
        verifyNoMoreInteractions(consumer);
        
        listener.onPartitionsAssigned(consumer, partitions);
        verify(consumer, times(1)).pause(Mockito.eq(partitions));
        verifyNoMoreInteractions(consumer);
    }

    /**
     * Check when consumer is active before rebalance, it must be active after
     */
    @Test
    public void testRebalanceConsumerActive() {

        MemoryConsumerAwareRebalanceListener listener =
                new MemoryConsumerAwareRebalanceListener();

        Set<TopicPartition> partitions = new HashSet<>();
        partitions.add(new TopicPartition("topic", 0));
        partitions.add(new TopicPartition("topic", 1));
        partitions.add(new TopicPartition("topic", 2));
        // Mock consumer
        doNothing().when(consumer).pause(Mockito.any());
        doReturn(null).when(consumer).paused();

        // Before rebalance
        listener.onPartitionsRevokedBeforeCommit(consumer, partitions);
        assertFalse(listener.isPaused());
        verify(consumer, times(1)).paused();
        verifyNoMoreInteractions(consumer);
        
        listener.onPartitionsRevokedAfterCommit(consumer, partitions);
        assertFalse(listener.isPaused());
        verify(consumer, times(2)).paused();
        verifyNoMoreInteractions(consumer);
        
        listener.onPartitionsAssigned(consumer, partitions);
        verify(consumer, never()).pause(Mockito.eq(partitions));
        verifyNoMoreInteractions(consumer);
    }
}
