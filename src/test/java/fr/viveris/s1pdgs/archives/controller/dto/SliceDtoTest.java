package fr.viveris.s1pdgs.archives.controller.dto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.archives.model.ProductFamily;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class SliceDtoTest {

	
	/**
	 * Test toString
	 */
	@Test
	public void testToString() {
		SliceDto obj = new SliceDto("name", "kobs", ProductFamily.BLANK);
		
		String str = obj.toString();
		assertTrue(str.contains("productName=name"));
		assertTrue(str.contains("keyObjectStorage=kobs"));
		assertTrue(str.contains("familyName=BLANK"));
	}
	
	
	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(SliceDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
