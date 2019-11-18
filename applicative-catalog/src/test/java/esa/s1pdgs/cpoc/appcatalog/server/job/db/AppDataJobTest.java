package esa.s1pdgs.cpoc.appcatalog.server.job.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class AppDataJobTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        AppDataJob obj = new AppDataJob();
        
        AppDataJobProduct product = new AppDataJobProduct();
        product.setSessionId("session-id");
        AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("tast-table-1");
        AppDataJobGeneration gen2 = new AppDataJobGeneration();
        gen2.setTaskTable("tast-table-2");
        AppDataJobGeneration gen3 = new AppDataJobGeneration();
        gen3.setTaskTable("tast-table-3");
        GenericMessageDto<ProductionEvent> message1 = new GenericMessageDto<ProductionEvent>(1, "topic1", null);
        GenericMessageDto<ProductionEvent> message2 = new GenericMessageDto<ProductionEvent>(2, "topic1", null);
        
        // check default constructor
        assertEquals(0, obj.getMessages().size());
        assertEquals(AppDataJobState.WAITING, obj.getState());
        assertNull(obj.getCreationDate());
        assertNull(obj.getLastUpdateDate());
        
        obj.setId(123);
        obj.setLevel(ApplicationLevel.L1);
        obj.setPod("pod-name");
        obj.setState(AppDataJobState.DISPATCHING);
        obj.setCreationDate(new Date());
        obj.setLastUpdateDate(new Date());
        obj.setProduct(product);
        obj.setMessages(Arrays.asList(message1, message2));
        obj.setGenerations(Arrays.asList(gen1, gen2, gen3));
        obj.setCategory(ProductCategory.AUXILIARY_FILES);

        // check setters
        assertEquals(123, obj.getId());
        assertEquals(ApplicationLevel.L1, obj.getLevel());
        assertEquals(2, obj.getMessages().size());
        assertEquals(AppDataJobState.DISPATCHING, obj.getState());
        assertNotNull(obj.getCreationDate());
        assertNotNull(obj.getLastUpdateDate());
        assertEquals("pod-name", obj.getPod());
        assertEquals(ProductCategory.AUXILIARY_FILES, obj.getCategory());
        
        // check toString
        String str = obj.toString();
        assertTrue(str.contains("id: 123"));
        assertTrue(str.contains("level: L1"));
        assertTrue(str.contains("state: DISPATCHING"));
        assertTrue(str.contains("pod: pod-name"));
        assertTrue(str.contains("creationDate: "));
        assertTrue(str.contains("lastUpdateDate: "));
        assertTrue(str.contains("category: AUXILIARY_FILES"));
        assertTrue(str.contains("product: " + product.toString()));
        assertTrue(str.contains("messages: " + Arrays.asList(message1, message2).toString()));
        assertTrue(str.contains("generations: " + Arrays.asList(gen1, gen2, gen3).toString()));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(AppDataJob.class)
                .usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
