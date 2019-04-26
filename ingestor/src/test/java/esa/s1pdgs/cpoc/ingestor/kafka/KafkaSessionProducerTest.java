package esa.s1pdgs.cpoc.ingestor.kafka;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;

public class KafkaSessionProducerTest {

    /**
     * Topic name
     */
    private static final String TOPIC_NAME = "topic-config";

    /**
     * KAFKA client
     */
    @Mock
    private KafkaTemplate<String, EdrsSessionDto> kafkaTemplate;

    /**
     * Service to test
     */
    private KafkaSessionProducer service;

    /**
     * Test initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        service = new KafkaSessionProducer(kafkaTemplate, TOPIC_NAME);
    }

    /**
     * Test constructor
     */
    @Test
    public void testConstructor() {
        assertEquals(TOPIC_NAME, service.getKafkaTopic());
    }

    /**
     * Test the function get product name
     */
    @Test
    public void testGetProductName() {
        EdrsSessionDto dto = new EdrsSessionDto("key-obs", 2,
                EdrsSessionFileType.RAW, "S1", "B");
        assertEquals("getProductName should return the product name", "key-obs",
                service.extractProductName(dto));
    }

}
