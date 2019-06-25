package esa.s1pdgs.cpoc.appcatalog.common.rest.model.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.ProductMessageDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class AppDataJobDtoTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        AppDataJobDto<ProductDto> obj = new AppDataJobDto<ProductDto>();
        
        AppDataJobProductDto product = new AppDataJobProductDto();
        product.setSessionId("session-id");
        AppDataJobGenerationDto gen1 = new AppDataJobGenerationDto();
        gen1.setTaskTable("tast-table-1");
        AppDataJobGenerationDto gen2 = new AppDataJobGenerationDto();
        gen2.setTaskTable("tast-table-2");
        AppDataJobGenerationDto gen3 = new AppDataJobGenerationDto();
        gen3.setTaskTable("tast-table-3");
        ProductMessageDto message1 = new ProductMessageDto(1, "topic1", null);
        ProductMessageDto message2 = new ProductMessageDto(2, "topic1", null);
        
        // check default constructor
        assertEquals(0, obj.getMessages().size());
        assertEquals(AppDataJobDtoState.WAITING, obj.getState());
        assertNull(obj.getCreationDate());
        assertNull(obj.getLastUpdateDate());
        
        obj.setIdentifier(123);
        obj.setLevel(ApplicationLevel.L1);
        obj.setPod("pod-name");
        obj.setState(AppDataJobDtoState.DISPATCHING);
        obj.setCreationDate(new Date());
        obj.setLastUpdateDate(new Date());
        obj.setProduct(product);
        obj.setMessages(Arrays.asList(message1, message2));
        obj.setGenerations(Arrays.asList(gen1, gen2, gen3));

        // check setters
        assertEquals(123, obj.getIdentifier());
        assertEquals(ApplicationLevel.L1, obj.getLevel());
        assertEquals(2, obj.getMessages().size());
        assertEquals(AppDataJobDtoState.DISPATCHING, obj.getState());
        assertNotNull(obj.getCreationDate());
        assertNotNull(obj.getLastUpdateDate());
        assertEquals(product, obj.getProduct());
        assertEquals(Arrays.asList(gen1, gen2, gen3), obj.getGenerations());
        assertEquals("pod-name", obj.getPod());
        
        // check toString
        String str = obj.toString();
        assertTrue(str.contains("identifier: 123"));
        assertTrue(str.contains("level: L1"));
        assertTrue(str.contains("state: DISPATCHING"));
        assertTrue(str.contains("pod: pod-name"));
        assertTrue(str.contains("creationDate: "));
        assertTrue(str.contains("lastUpdateDate: "));
        assertTrue(str.contains("product: " + product.toString()));
        assertTrue(str.contains("messages: " + Arrays.asList(message1, message2).toString()));
        assertTrue(str.contains("generations: " + Arrays.asList(gen1, gen2, gen3).toString()));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(AppDataJobDto.class)
                .usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
