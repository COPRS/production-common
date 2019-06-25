package esa.s1pdgs.cpoc.mqi.server.consumption.kafka.listener;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Arrays;
import java.util.List;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appcatalog.client.mqi.GenericAppCatalogMqiService;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiGetOffsetApiError;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

/**
 * Test the MemoryConsumerAwareRebalanceListener
 * 
 * @author Viveris Technologies
 */
public class MemoryConsumerAwareRebalanceListenerTest {

    /**
     * Mock consumer
     */
    @Mock
    private Consumer<?, ?> consumer;

    /**
     * Service of applicative data
     */
    @Mock
    private GenericAppCatalogMqiService<ProductDto> service;

    /**
     * Listener to test
     */
    private MemoryConsumerAwareRebalanceListener listener;

    /**
     * Partitions
     */
    private List<TopicPartition> partitions;

    /**
     * Initialization
     * 
     * @throws AbstractCodedException
     */
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);

        partitions = Arrays.asList(new TopicPartition("topic", 0),
                new TopicPartition("topic", 1), new TopicPartition("topic2", 2),
                new TopicPartition("topic", 3), new TopicPartition("topic", 4), new TopicPartition("topic", 5));

        doNothing().when(consumer).seek(Mockito.any(), Mockito.anyLong());
        doNothing().when(consumer).seekToBeginning(Mockito.any());
        doNothing().when(consumer).seekToEnd(Mockito.any());

        doReturn(-3L).when(service).getEarliestOffset(Mockito.anyString(),
                Mockito.eq(0), Mockito.anyString());
        doReturn(-2L).when(service).getEarliestOffset(Mockito.anyString(),
                Mockito.eq(1), Mockito.anyString());
        doReturn(-1L).when(service).getEarliestOffset(Mockito.anyString(),
                Mockito.eq(2), Mockito.anyString());
        doReturn(0L).when(service).getEarliestOffset(Mockito.anyString(),
                Mockito.eq(3), Mockito.anyString());
        doReturn(128L).when(service).getEarliestOffset(Mockito.anyString(),
                Mockito.eq(4), Mockito.anyString());
        doThrow(new AppCatalogMqiGetOffsetApiError(
                ProductCategory.AUXILIARY_FILES, "uri", "message"))
                        .when(service).getEarliestOffset(Mockito.anyString(),
                                Mockito.eq(5), Mockito.anyString());

        listener = new MemoryConsumerAwareRebalanceListener(service,
                "groupname", -2);
    }

    /**
     * Check when consumer is in pause before rebalance, it must be in pause
     * after
     */
    @Test
    public void testConstructor() {

        MemoryConsumerAwareRebalanceListener listener =
                new MemoryConsumerAwareRebalanceListener(service, "groupname",
                        -2);
        assertEquals(-2, listener.getDefaultMode());
        assertEquals("groupname", listener.getGroup());
    }

    /**
     * Test onPartitionsRevokedBeforeCommit
     */
    @Test
    public void testonPartitionsRevokedBeforeCommit() {
        listener.onPartitionsRevokedBeforeCommit(consumer, partitions);
        verifyZeroInteractions(service);
    }

    /**
     * Test onPartitionsRevokedAfterCommit
     */
    @Test
    public void testonPartitionsRevokedAfterCommit() {
        listener.onPartitionsRevokedAfterCommit(consumer, partitions);
        verifyZeroInteractions(service);
    }

    /**
     * Test onPartitionsRevokedAfterCommit
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testAssigned() throws AbstractCodedException {
        listener.onPartitionsAssigned(consumer, partitions);

        // Check P0 => startingoffset -3
        verify(service, times(1)).getEarliestOffset(Mockito.eq("topic"),
                Mockito.eq(0), Mockito.eq("groupname"));
        verify(consumer, never()).seek(Mockito.eq(partitions.get(0)),
                Mockito.anyLong());
        verify(consumer, never())
                .seekToBeginning(Mockito.eq(Arrays.asList(partitions.get(0))));
        verify(consumer, never())
                .seekToEnd(Mockito.eq(Arrays.asList(partitions.get(0))));

        // Check P1 => startingoffset -2
        verify(service, times(1)).getEarliestOffset(Mockito.eq("topic"),
                Mockito.eq(1), Mockito.eq("groupname"));
        verify(consumer, never()).seek(Mockito.eq(partitions.get(1)),
                Mockito.anyLong());
        verify(consumer, never())
                .seekToBeginning(Mockito.eq(Arrays.asList(partitions.get(1))));
        verify(consumer, times(1))
                .seekToEnd(Mockito.eq(Arrays.asList(partitions.get(1))));

        // Check P0 => startingoffset -1
        verify(service, times(1)).getEarliestOffset(Mockito.eq("topic2"),
                Mockito.eq(2), Mockito.eq("groupname"));
        verify(consumer, never()).seek(Mockito.eq(partitions.get(2)),
                Mockito.eq(0L));
        verify(consumer, times(1))
                .seekToBeginning(Mockito.eq(Arrays.asList(partitions.get(2))));
        verify(consumer, never())
                .seekToEnd(Mockito.eq(Arrays.asList(partitions.get(2))));

        // Check P0 => startingoffset 0
        verify(service, times(1)).getEarliestOffset(Mockito.eq("topic"),
                Mockito.eq(3), Mockito.eq("groupname"));
        verify(consumer, times(1)).seek(Mockito.eq(partitions.get(3)),
                Mockito.eq(0L));
        verify(consumer, never())
                .seekToBeginning(Mockito.eq(Arrays.asList(partitions.get(3))));
        verify(consumer, never())
                .seekToEnd(Mockito.eq(Arrays.asList(partitions.get(3))));

        // Check P0 => startingoffset 128
        verify(service, times(1)).getEarliestOffset(Mockito.eq("topic"),
                Mockito.eq(4), Mockito.eq("groupname"));
        verify(consumer, times(1)).seek(Mockito.eq(partitions.get(4)),
                Mockito.eq(128L));
        verify(consumer, never())
                .seekToBeginning(Mockito.eq(Arrays.asList(partitions.get(4))));
        verify(consumer, never())
                .seekToEnd(Mockito.eq(Arrays.asList(partitions.get(4))));

        // Check P4 => default offset => startingoffset -2
        verify(service, times(1)).getEarliestOffset(Mockito.eq("topic"),
                Mockito.eq(5), Mockito.eq("groupname"));
        verify(consumer, never()).seek(Mockito.eq(partitions.get(5)),
                Mockito.anyLong());
        verify(consumer, never())
                .seekToBeginning(Mockito.eq(Arrays.asList(partitions.get(5))));
        verify(consumer, times(1))
                .seekToEnd(Mockito.eq(Arrays.asList(partitions.get(5))));
    }
}
