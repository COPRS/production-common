package esa.s1pdgs.cpoc.mqi.model.queue;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the DTO object KafkaConfigFileDto
 * 
 * @author Viveris Technologies
 *
 */
public class IngestionDtoTest {

	/**
	 * Check equals and hashcode methods
	 */
	@Test
	public void checkEquals() {
		EqualsVerifier.forClass(IngestionDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
	
    /**
     * Test toString methods and setters
     */
    @Test
    public void testToStringAndSetters() {
    	IngestionDto dto = new IngestionDto();
        dto.setProductName("product-name");
        dto.setRelativePath("product-name");
        dto.setPickupPath("/fooBar");
 
        String str = dto.toString();
        assertTrue(str.contains("product-name"));
    }
}
