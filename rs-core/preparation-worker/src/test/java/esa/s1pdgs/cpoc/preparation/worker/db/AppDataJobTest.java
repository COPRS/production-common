package esa.s1pdgs.cpoc.preparation.worker.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class AppDataJobTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        final AppDataJob obj = new AppDataJob();
        
        final AppDataJobProduct product = new AppDataJobProduct();
        product.getMetadata().put("sessionId", "session-id");
        final AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("tast-table-1");
        final AppDataJobGeneration gen2 = new AppDataJobGeneration();
        gen2.setTaskTable("tast-table-2");
        final AppDataJobGeneration gen3 = new AppDataJobGeneration();
        gen3.setTaskTable("tast-table-3");
        final CatalogEvent event1 = new CatalogEvent();
        final CatalogEvent event2 = new CatalogEvent();
        
        // check default constructor
        assertEquals(0, obj.getCatalogEvents().size());
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
        obj.setCatalogEvents(Arrays.asList(event1, event2));
        obj.setGeneration(gen1);

        // check setters
        assertEquals(123, obj.getId());
        assertEquals(ApplicationLevel.L1, obj.getLevel());
        assertEquals(2, obj.getCatalogEvents().size());
        assertEquals(AppDataJobState.DISPATCHING, obj.getState());
        assertNotNull(obj.getCreationDate());
        assertNotNull(obj.getLastUpdateDate());
        assertEquals("pod-name", obj.getPod());
        
        // check toString
        final String str = obj.toString();
        assertTrue(str.contains("id=123"));
        assertTrue(str.contains("level=L1"));
        assertTrue(str.contains("state=DISPATCHING"));
        assertTrue(str.contains("pod=pod-name"));
        assertTrue(str.contains("creationDate="));
        assertTrue(str.contains("lastUpdateDate="));
        assertTrue(str.contains("product=" + product.toString()));
        assertTrue(str.contains("catalogEvents=" + Arrays.asList(event1, event2).toString()));
        assertTrue(str.contains("generation=" + gen1.toString()));
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
