package fr.viveris.s1pdgs.mqi.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import fr.viveris.s1pdgs.common.ProductCategory;
import fr.viveris.s1pdgs.mqi.server.ApplicationProperties.ProductCategoryConsumptionProperties;
import fr.viveris.s1pdgs.mqi.server.ApplicationProperties.ProductCategoryProperties;
import fr.viveris.s1pdgs.mqi.server.ApplicationProperties.ProductCategoryPublicationProperties;

/**
 * Check the initialization of the application properties
 * 
 * @author Viveris Technologies
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class ApplicationPropertiesTest {

    /**
     * Embedded Kafka
     */
    @ClassRule
    public static KafkaEmbedded embeddedKafka =
            new KafkaEmbedded(1, true, "t-pdgs-l0-jobs");

    /**
     * Properties to test
     */
    @Autowired
    private ApplicationProperties properties;

    /**
     * Test initialization
     */
    @Test
    public void testInitialization() {
        assertEquals(5, properties.getProductCategories().size());
        assertEquals(500, properties.getWaitNextMs());

        // Check consumption

        // Auxiliary files
        assertFalse(properties.getProductCategories()
                .get(ProductCategory.AUXILIARY_FILES).getConsumption()
                .isEnable());
        // EDRS sessions
        assertTrue(properties.getProductCategories()
                .get(ProductCategory.EDRS_SESSIONS).getConsumption()
                .isEnable());
        assertEquals("t-pdgs-edrs-sessions",
                properties.getProductCategories()
                        .get(ProductCategory.EDRS_SESSIONS).getConsumption()
                        .getTopics());
        // Level jobs
        assertTrue(properties.getProductCategories()
                .get(ProductCategory.LEVEL_JOBS).getConsumption().isEnable());
        assertEquals("t-pdgs-l0-jobs", properties.getProductCategories()
                .get(ProductCategory.LEVEL_JOBS).getConsumption().getTopics());
        // Level reports
        assertFalse(properties.getProductCategories()
                .get(ProductCategory.LEVEL_REPORTS).getConsumption()
                .isEnable());
        // Level products
        assertFalse(properties.getProductCategories()
                .get(ProductCategory.LEVEL_PRODUCTS).getConsumption()
                .isEnable());

        // Check publication

        // Auxiliary files
        assertFalse(properties.getProductCategories()
                .get(ProductCategory.AUXILIARY_FILES).getPublication()
                .isEnable());
        // EDRS sessions
        assertFalse(properties.getProductCategories()
                .get(ProductCategory.EDRS_SESSIONS).getPublication()
                .isEnable());
        // Level jobs
        assertFalse(properties.getProductCategories()
                .get(ProductCategory.LEVEL_JOBS).getPublication().isEnable());
        // Level reports
        assertTrue(properties.getProductCategories()
                .get(ProductCategory.LEVEL_REPORTS).getPublication()
                .isEnable());
        assertEquals("./src/test/resources/routing-files/level-reports.xml",
                properties.getProductCategories()
                        .get(ProductCategory.LEVEL_REPORTS).getPublication()
                        .getRoutingFile());
        // Level products
        assertTrue(properties.getProductCategories()
                .get(ProductCategory.LEVEL_PRODUCTS).getPublication()
                .isEnable());
        assertEquals("./src/test/resources/routing-files/level-products.xml",
                properties.getProductCategories()
                        .get(ProductCategory.LEVEL_PRODUCTS).getPublication()
                        .getRoutingFile());
    }

    /**
     * Test setters
     */
    @Test
    public void testGetterSetters() {
        ProductCategoryProperties catProp = new ProductCategoryProperties();
        ProductCategoryConsumptionProperties consProp =
                new ProductCategoryConsumptionProperties();
        consProp.setEnable(true);
        consProp.setTopics("test-topic");
        catProp.setConsumption(consProp);
        ProductCategoryPublicationProperties pubProp =
                new ProductCategoryPublicationProperties();
        pubProp.setEnable(true);
        pubProp.setRoutingFile("routing-file");
        catProp.setPublication(pubProp);

        Map<ProductCategory, ProductCategoryProperties> map = new HashMap<>();
        map.put(ProductCategory.AUXILIARY_FILES, catProp);
        properties.setProductCategories(map);
        properties.setWaitNextMs(1000);

        assertEquals(1, properties.getProductCategories().size());
        assertEquals(1000, properties.getWaitNextMs());
        // Auxiliary files
        assertTrue(properties.getProductCategories()
                .get(ProductCategory.AUXILIARY_FILES).getConsumption()
                .isEnable());
        assertEquals("test-topic",
                properties.getProductCategories()
                        .get(ProductCategory.AUXILIARY_FILES).getConsumption()
                        .getTopics());
        // Auxiliary files
        assertTrue(properties.getProductCategories()
                .get(ProductCategory.AUXILIARY_FILES).getPublication()
                .isEnable());
        assertEquals("routing-file",
                properties.getProductCategories()
                        .get(ProductCategory.AUXILIARY_FILES).getPublication()
                        .getRoutingFile());

    }
}
