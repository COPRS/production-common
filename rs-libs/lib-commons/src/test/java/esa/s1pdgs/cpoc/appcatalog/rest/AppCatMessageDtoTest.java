package esa.s1pdgs.cpoc.appcatalog.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductCategory;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object MqiGenericMessageDto
 * 
 * @author Viveris Technologies
 */
public class AppCatMessageDtoTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
    	AppCatMessageDto<String> obj = new AppCatMessageDto<>();
        assertNull(obj.getDto());

        AppCatMessageDto<String> obj1 = new AppCatMessageDto<String>(
                ProductCategory.AUXILIARY_FILES);
        assertNull(obj1.getDto());
        assertEquals(ProductCategory.AUXILIARY_FILES, obj1.getCategory());

        AppCatMessageDto<String> obj2 = new AppCatMessageDto<String>(
                ProductCategory.AUXILIARY_FILES, 1000, "topic-name", 2, 3210);
        assertNull(obj2.getDto());
        assertEquals(ProductCategory.AUXILIARY_FILES, obj2.getCategory());
        assertEquals(1000, obj2.getId());
        assertEquals("topic-name", obj2.getTopic());
        assertEquals(2, obj2.getPartition());
        assertEquals(3210, obj2.getOffset());

        AppCatMessageDto<String> obj3 = new AppCatMessageDto<String>(
                ProductCategory.AUXILIARY_FILES, 1000, "topic-name", 2, 3210,
                "dto-object");
        assertEquals(ProductCategory.AUXILIARY_FILES, obj3.getCategory());
        assertEquals(1000, obj3.getId());
        assertEquals("topic-name", obj3.getTopic());
        assertEquals(2, obj3.getPartition());
        assertEquals(3210, obj3.getOffset());
        assertEquals("dto-object", obj3.getDto());
    }

    /**
     * Test to string
     */
    @Test
    public void testToString() {
    	AppCatMessageDto<String> obj = new AppCatMessageDto<String>(
                ProductCategory.AUXILIARY_FILES, 1000, "topic-name", 2, 3210,
                "dto-object");
        String str = obj.toString();
        assertTrue(str.contains(obj.toStringForExtend()));
        assertTrue(str.contains("dto: dto-object"));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(AppCatMessageDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
