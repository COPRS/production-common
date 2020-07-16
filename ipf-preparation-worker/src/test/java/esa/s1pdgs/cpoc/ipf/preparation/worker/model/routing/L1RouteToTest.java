package esa.s1pdgs.cpoc.ipf.preparation.worker.model.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRouteTo;
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
		LevelProductsRouteTo obj = new LevelProductsRouteTo(tts);
		
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
		
		LevelProductsRouteTo obj = new LevelProductsRouteTo();
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
		EqualsVerifier.forClass(LevelProductsRouteTo.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
