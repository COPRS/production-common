package esa.s1pdgs.cpoc.xml.model.tasktable.joborder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInputFile;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class JobOrderInputFileTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {

		JobOrderInputFile obj = new JobOrderInputFile();
		obj.setFilename("file");
		obj.setKeyObjectStorage("kobs");

		JobOrderInputFile clone = new JobOrderInputFile(obj);
		assertEquals(obj.getKeyObjectStorage(), clone.getKeyObjectStorage());
		assertEquals(obj.getFilename(), clone.getFilename());
	}

	/**
	 * Test to string
	 */
	@Test
	public void testToString() {

		JobOrderInputFile obj = new JobOrderInputFile();
		obj.setFilename("file");
		obj.setKeyObjectStorage("kobs");

		String str = obj.toString();
		assertTrue(str.contains("filename: file"));
		assertTrue(str.contains("keyObjectStorage: kobs"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(JobOrderInputFile.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
