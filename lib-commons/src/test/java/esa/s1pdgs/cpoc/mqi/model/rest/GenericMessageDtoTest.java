package esa.s1pdgs.cpoc.mqi.model.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the DTO object KafkaConfigFileDto
 * 
 * @author Viveris Technologies
 *
 */
public class GenericMessageDtoTest {

	/**
	 * Test getters, setters and constructors
	 */
	@Test
	public void testGettersSettersConstructors() {
		GenericMessageDto<String> dto = new GenericMessageDto<String>(123, "input-key", "key-obs");
		assertEquals(123, dto.getIdentifier());
		assertEquals("key-obs", dto.getBody());
        assertEquals("input-key", dto.getInputKey());

		dto.setIdentifier(321);;
		dto.setBody("other-key");
		dto.setInputKey("othey-input");
		assertEquals(321, dto.getIdentifier());
		assertEquals("other-key", dto.getBody());
        assertEquals("othey-input", dto.getInputKey());
	}

	/**
	 * Test the toString function
	 */
	@Test
	public void testToString() {
	    GenericMessageDto<String> dto = new GenericMessageDto<String>(123, "input-key", "key-obs");
		String str = dto.toString();
		assertTrue("toString should contain the identifier", str.contains("identifier: 123"));
		assertTrue("toString should contain the body", str.contains("body: key-obs"));
        assertTrue("toString should contain the input key", str.contains("inputKey: input-key"));
	}

	/**
	 * Check equals and hashcode methods
	 */
	@Test
	public void checkEquals() {
		EqualsVerifier.forClass(GenericMessageDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
