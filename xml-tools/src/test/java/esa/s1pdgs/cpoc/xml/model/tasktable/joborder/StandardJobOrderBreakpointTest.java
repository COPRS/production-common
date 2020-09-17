package esa.s1pdgs.cpoc.xml.model.tasktable.joborder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.joborder.StandardJobOrderBreakpoint;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 */
public class StandardJobOrderBreakpointTest {
	
	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {
		
		StandardJobOrderBreakpoint obj = new StandardJobOrderBreakpoint();
		obj.setEnable("true");
		List<String> files = Arrays.asList("file1","file2");
		obj.addFiles(files);
		
		StandardJobOrderBreakpoint clone = new StandardJobOrderBreakpoint(obj);
		assertEquals(obj.getNbFiles(), clone.getNbFiles());
		assertEquals(2, clone.getNbFiles());
		assertEquals(obj.getEnable(), clone.getEnable());
		assertEquals(obj.getFiles().get(0), clone.getFiles().get(0));
		
		obj = new StandardJobOrderBreakpoint("false", null);
		assertEquals(0, obj.getNbFiles());
		assertEquals(0, obj.getFiles().size());
		assertEquals("false", obj.getEnable());
		
		obj = new StandardJobOrderBreakpoint("faflse", new ArrayList<>());
		assertEquals(0, obj.getNbFiles());
		assertEquals(0, obj.getFiles().size());
		assertEquals("faflse", obj.getEnable());
	}
	
	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		
		StandardJobOrderBreakpoint obj = new StandardJobOrderBreakpoint();
		obj.setEnable("true");
		List<String> files = Arrays.asList("file1","file2");
		obj.addFiles(files);
		
		String str = obj.toString();
		assertTrue(str.contains("files: " + files.toString()));
		assertTrue(str.contains("nbFiles: 2"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(StandardJobOrderBreakpoint.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
