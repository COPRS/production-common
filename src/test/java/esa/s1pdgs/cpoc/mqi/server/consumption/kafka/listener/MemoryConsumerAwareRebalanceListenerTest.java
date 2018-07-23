package esa.s1pdgs.cpoc.mqi.server.consumption.kafka.listener;

import static org.junit.Assert.assertEquals;

import org.apache.kafka.clients.consumer.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appcatalog.client.GenericAppCatalogMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.AuxiliaryFileDto;

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

    @Mock
    private GenericAppCatalogMqiService<AuxiliaryFileDto> service;

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
    public void testConstructor() {

        MemoryConsumerAwareRebalanceListener listener =
                new MemoryConsumerAwareRebalanceListener(service, "groupname", -2);
        assertEquals(-2, listener.getDefaultMode());
        assertEquals("groupname", listener.getGroup());
    }
}
