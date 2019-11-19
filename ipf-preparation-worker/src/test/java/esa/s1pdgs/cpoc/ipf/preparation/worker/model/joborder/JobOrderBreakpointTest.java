package esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderBreakpoint;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 */
public class JobOrderBreakpointTest {
	
	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {
		
		JobOrderBreakpoint obj = new JobOrderBreakpoint();
		obj.setEnable("true");
		List<String> files = Arrays.asList("file1","file2");
		obj.addFiles(files);
		
		JobOrderBreakpoint clone = new JobOrderBreakpoint(obj);
		assertEquals(obj.getNbFiles(), clone.getNbFiles());
		assertEquals(2, clone.getNbFiles());
		assertEquals(obj.getEnable(), clone.getEnable());
		assertEquals(obj.getFiles().get(0), clone.getFiles().get(0));
		
		obj = new JobOrderBreakpoint("false", null);
		assertEquals(0, obj.getNbFiles());
		assertEquals(0, obj.getFiles().size());
		assertEquals("false", obj.getEnable());
		
		obj = new JobOrderBreakpoint("faflse", new ArrayList<>());
		assertEquals(0, obj.getNbFiles());
		assertEquals(0, obj.getFiles().size());
		assertEquals("faflse", obj.getEnable());
	}
	
	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		
		JobOrderBreakpoint obj = new JobOrderBreakpoint();
		obj.setEnable("true");
		List<String> files = Arrays.asList("file1","file2");
		obj.addFiles(files);
		
		String str = obj.toString();
		assertTrue(str.contains("enable: true"));
		assertTrue(str.contains("files: " + files.toString()));
		assertTrue(str.contains("nbFiles: 2"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(JobOrderBreakpoint.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
