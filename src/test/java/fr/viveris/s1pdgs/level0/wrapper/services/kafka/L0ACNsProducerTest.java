package fr.viveris.s1pdgs.level0.wrapper.services.kafka;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L0AcnDto;

/**
 * 
 * @author Viveris Technologies
 *
 */
public class L0ACNsProducerTest {


    /**
     * Topic name
     */
    private static final String TOPIC_NAME = "topic-config";

    /**
     * KAFKA client
     */
    @Mock
    private KafkaTemplate<String, L0AcnDto> kafkaTemplate;

    /**
     * Service to test
     */
    private L0ACNsProducer service;

    /**
     * Test initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        service = new L0ACNsProducer(kafkaTemplate, TOPIC_NAME);
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
        L0AcnDto dto = new L0AcnDto("product-name", "key-obs");
        assertEquals("getProductName should return the product name", "product-name", service.extractProductName(dto));
    }
}
