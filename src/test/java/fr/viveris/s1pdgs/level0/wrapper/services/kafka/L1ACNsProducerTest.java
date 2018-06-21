package fr.viveris.s1pdgs.level0.wrapper.services.kafka;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L1AcnDto;

/**
 * @author Viveris Technologies
 */
public class L1ACNsProducerTest {

    /**
     * Topic name
     */
    private static final String TOPIC_NAME = "topic-config";

    /**
     * KAFKA client
     */
    @Mock
    private KafkaTemplate<String, L1AcnDto> kafkaTemplate;

    /**
     * Service to test
     */
    private L1ACNsProducer service;

    /**
     * Test initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        service = new L1ACNsProducer(kafkaTemplate, TOPIC_NAME);
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
        L1AcnDto dto = new L1AcnDto("product-name", "key-obs");
        assertEquals("getProductName should return the product name",
                "product-name", service.extractProductName(dto));
    }
}
