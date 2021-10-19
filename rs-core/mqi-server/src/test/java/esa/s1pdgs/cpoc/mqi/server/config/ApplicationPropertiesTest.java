package esa.s1pdgs.cpoc.mqi.server.config;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.server.config.ApplicationProperties.ProductCategoryConsumptionProperties;
import esa.s1pdgs.cpoc.mqi.server.config.ApplicationProperties.ProductCategoryProperties;
import esa.s1pdgs.cpoc.mqi.server.config.ApplicationProperties.ProductCategoryPublicationProperties;

/**
 * Check the initialization of the application properties
 * 
 * @author Viveris Technologies
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@ActiveProfiles("test-properties")
public class ApplicationPropertiesTest {

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
        assertEquals("wrapper-0", properties.getHostname());
        assertEquals(6, properties.getProductCategories().size());
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
        Map<String, Integer> expectedTopics = new HashMap<>();
        expectedTopics.put("t-pdgs-session-file-ingestion-events", 0);
        expectedTopics.put("t-topic-2", 0);
        assertEquals(expectedTopics.keySet(),
                properties.getProductCategories()
                        .get(ProductCategory.EDRS_SESSIONS).getConsumption()
                        .getTopicsWithPriority().keySet());
        // Level jobs
        assertTrue(properties.getProductCategories()
                .get(ProductCategory.LEVEL_JOBS).getConsumption().isEnable());
        expectedTopics = new HashMap<>();
        expectedTopics.put("t-pdgs-aio-execution-jobs", 0);
        assertEquals(expectedTopics.keySet(), properties.getProductCategories()
                .get(ProductCategory.LEVEL_JOBS).getConsumption().getTopicsWithPriority().keySet());
        // Level reports
        assertFalse(properties.getProductCategories()
                .get(ProductCategory.LEVEL_REPORTS).getConsumption()
                .isEnable());
        // Level products
        assertFalse(properties.getProductCategories()
                .get(ProductCategory.LEVEL_PRODUCTS).getConsumption()
                .isEnable());
        // Level segments
        assertFalse(properties.getProductCategories()
                .get(ProductCategory.LEVEL_SEGMENTS).getConsumption()
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
        // Level segments
        assertFalse(properties.getProductCategories()
                .get(ProductCategory.LEVEL_SEGMENTS).getPublication().isEnable());
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
        Map<String, Integer> topicsWithPriority = new HashMap<>();
        topicsWithPriority.put("test-topic", 100);
        topicsWithPriority.put("test-topic-1", 10);
        consProp.setTopicsWithPriority(topicsWithPriority);
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
        properties.setHostname("host-test");

        assertEquals(1, properties.getProductCategories().size());
        assertEquals(1000, properties.getWaitNextMs());
        assertEquals("host-test", properties.getHostname());
        // Auxiliary files
        assertTrue(properties.getProductCategories()
                .get(ProductCategory.AUXILIARY_FILES).getConsumption()
                .isEnable());
        Map<String, Integer> expectedTopics = new HashMap<>();
        expectedTopics.put("test-topic", 0);
        expectedTopics.put("test-topic-1", 0);
        assertEquals(expectedTopics.keySet(),
                properties.getProductCategories()
                        .get(ProductCategory.AUXILIARY_FILES).getConsumption()
                        .getTopicsWithPriority().keySet());
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
