package fr.viveris.s1pdgs.level0.wrapper.services.kafka;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L1SliceDto;

/**
 * @author Viveris Technologies
 */
public class L1SlicesProducerTest {

    /**
     * Topic name
     */
    private static final String TOPIC_NAME = "topic-config";

    /**
     * KAFKA client
     */
    @Mock
    private KafkaTemplate<String, L1SliceDto> kafkaTemplate;

    /**
     * Service to test
     */
    private L1SlicesProducer service;

    /**
     * Test initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        service = new L1SlicesProducer(kafkaTemplate, TOPIC_NAME);
    }

    /**
     * Test constructor
     */
    @Test
    public void testConstructor() {
        assertEquals(TOPIC_NAME, service.getTopic());
    }

    /**
     * Test the function get product name
     */
    @Test
    public void testGetProductName() {
        L1SliceDto dto = new L1SliceDto("product-name", "key-obs");
        assertEquals("getProductName should return the product name",
                "product-name", service.extractProductName(dto));
    }
}
