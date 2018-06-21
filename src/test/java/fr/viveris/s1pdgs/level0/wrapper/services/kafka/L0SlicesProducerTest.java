package fr.viveris.s1pdgs.level0.wrapper.services.kafka;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L0SliceDto;

/**
 * @author Viveris Technologies
 */
public class L0SlicesProducerTest {

    /**
     * Topic name
     */
    private static final String TOPIC_NAME = "topic-config";

    /**
     * KAFKA client
     */
    @Mock
    private KafkaTemplate<String, L0SliceDto> kafkaTemplate;

    /**
     * Service to test
     */
    private L0SlicesProducer service;

    /**
     * Test initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        service = new L0SlicesProducer(kafkaTemplate, TOPIC_NAME);
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
        L0SliceDto dto = new L0SliceDto("product-name", "key-obs");
        assertEquals("getProductName should return the product name",
                "product-name", service.extractProductName(dto));
    }
}
