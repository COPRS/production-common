package fr.viveris.s1pdgs.jobgenerator.model.l1routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object L1RouteTo
 * 
 * @author Cyrielle Gailliard
 *
 */
public class L1RouteToTest {
	
	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {

		List<String> tts = Arrays.asList("tt1.xml","tt2.xml","tt3.xml");
		L1RouteTo obj = new L1RouteTo(tts);
		
		assertTrue(obj.getTaskTables().size() == 3);
		assertEquals("tt1.xml", obj.getTaskTables().get(0));
		assertEquals("tt2.xml", obj.getTaskTables().get(1));
		assertEquals("tt3.xml", obj.getTaskTables().get(2));
	}
	
	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		
		L1RouteTo obj = new L1RouteTo();
		List<String> tts = Arrays.asList("tt1.xml","tt2.xml","tt3.xml");
		obj.setTaskTables(tts);
		
		String str = obj.toString();
		assertTrue(str.contains("taskTables: " + tts.toString()));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(L1RouteTo.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
