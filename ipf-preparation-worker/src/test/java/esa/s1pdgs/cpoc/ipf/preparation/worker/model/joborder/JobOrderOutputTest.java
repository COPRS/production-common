package esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderOutput;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.enums.JobOrderDestination;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.enums.JobOrderFileNameType;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class JobOrderOutputTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {

		JobOrderOutput obj = new JobOrderOutput();
		obj.setDestination(JobOrderDestination.PROC);
		obj.setFamily(ProductFamily.AUXILIARY_FILE);
		obj.setFileName("file");
		obj.setFileNameType(JobOrderFileNameType.PHYSICAL);
		obj.setFileType("type");
		obj.setMandatory(true);

		JobOrderOutput clone = new JobOrderOutput(obj);
		assertEquals(obj.isMandatory(), clone.isMandatory());
		assertEquals(obj.getFileType(), clone.getFileType());
		assertEquals(obj.getFileNameType(), clone.getFileNameType());
		assertEquals(obj.getFileName(), clone.getFileName());
		assertEquals(obj.getFamily(), clone.getFamily());
		assertEquals(obj.getDestination(), clone.getDestination());
	}

	/**
	 * Test to string
	 */
	@Test
	public void testToString() {

		JobOrderOutput obj = new JobOrderOutput();
		obj.setDestination(JobOrderDestination.PROC);
		obj.setFamily(ProductFamily.AUXILIARY_FILE);
		obj.setFileName("file");
		obj.setFileNameType(JobOrderFileNameType.PHYSICAL);
		obj.setFileType("type");
		obj.setMandatory(true);

		String str = obj.toString();
		assertTrue(str.contains("mandatory: true"));
		assertTrue(str.contains("fileType: type"));
		assertTrue(str.contains("fileNameType: PHYSICAL"));
		assertTrue(str.contains("fileName: file"));
		assertTrue(str.contains("family: AUXILIARY_FILE"));
		assertTrue(str.contains("destination: PROC"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(JobOrderOutput.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
