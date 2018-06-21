package fr.viveris.s1pdgs.level0.wrapper.services.kafka;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.ReportDto;
import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;

/**
 * @author Viveris Technologies
 */
public class L1ReportProducerTest {

    /**
     * Topic name
     */
    private static final String TOPIC_NAME = "topic-config";

    /**
     * KAFKA client
     */
    @Mock
    private KafkaTemplate<String, ReportDto> kafkaTemplate;

    /**
     * Service to test
     */
    private L1ReportProducer service;

    /**
     * Test initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        service = new L1ReportProducer(kafkaTemplate, TOPIC_NAME);
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
        ReportDto dto = new ReportDto("product-name", "key-obs",
                ProductFamily.L1_REPORT);
        assertEquals("getProductName should return the product name",
                "product-name", service.extractProductName(dto));
    }
}
