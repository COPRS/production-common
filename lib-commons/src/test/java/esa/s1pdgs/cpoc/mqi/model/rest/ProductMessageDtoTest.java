package esa.s1pdgs.cpoc.mqi.model.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the ProductMessageDto
 * 
 * @author Viveris Technologies
 */
public class ProductMessageDtoTest {

    /**
     * Test getters, setters and constructors
     */
    @Test
    public void testGettersSettersConstructors() {
        ProductionEvent body = new ProductionEvent("product-name", "key-obs", ProductFamily.AUXILIARY_FILE, null);
        GenericMessageDto<ProductionEvent> dto =
                new GenericMessageDto<ProductionEvent>(123, "input-key", body);
        assertEquals(123, dto.getId());
        assertEquals(body, dto.getBody());
        assertEquals("input-key", dto.getInputKey());

        dto = new GenericMessageDto<ProductionEvent>();
        dto.setId(321);
        dto.setBody(body);
        dto.setInputKey("othey-input");
        assertEquals(321, dto.getId());
        assertEquals(body, dto.getBody());
        assertEquals("othey-input", dto.getInputKey());
    }

    /**
     * Test the toString function
     */
    @Test
    public void testToString() {
        ProductionEvent body = new ProductionEvent("product-name", "key-obs", ProductFamily.AUXILIARY_FILE, null);
        GenericMessageDto<ProductionEvent> dto =
                new GenericMessageDto<ProductionEvent>(123, "input-key", body);
        String str = dto.toString();
        assertTrue("toString should contain the identifier",
                str.contains("id: 123"));
        assertTrue("toString should contain the body",
                str.contains("body: " + body.toString()));
        assertTrue("toString should contain the input key",
                str.contains("inputKey: input-key"));
    }

    /**
     * Check equals and hashcode methods
     */
    @Test
    public void checkEquals() {
        EqualsVerifier.forClass(GenericMessageDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
