package fr.viveris.s1pdgs.jobgenerator.model.joborder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.common.ProductFamily;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.enums.JobOrderFileNameType;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 */
public class JobOrderInputTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {

		JobOrderTimeInterval input2 = new JobOrderTimeInterval("start", "stop", "fileName");

		JobOrderInput obj = new JobOrderInput();
		obj.setFileType("name");
		obj.setFileNameType(JobOrderFileNameType.REGEXP);
		obj.addTimeInterval(input2);
		obj.addFilename("filename", "keyObjectStorage");
		obj.setFamily(ProductFamily.L0_ACN);

		JobOrderInput clone = new JobOrderInput(obj);
		assertEquals(obj.getFileType(), clone.getFileType());
		assertEquals(obj.getFileNameType(), clone.getFileNameType());
		assertEquals(obj.getFamily(), clone.getFamily());
		assertEquals(obj.getNbFilenames(), clone.getNbFilenames());
		assertEquals(obj.getNbTimeIntervals(), clone.getNbTimeIntervals());
		assertEquals(obj.getTimeIntervals().get(0), clone.getTimeIntervals().get(0));
		assertEquals(obj.getFilenames().get(0), clone.getFilenames().get(0));

		obj = new JobOrderInput();
		obj.setFileType("name");
		obj.setFileNameType(JobOrderFileNameType.REGEXP);
		obj.setFamily(ProductFamily.L0_ACN);

		JobOrderInput clone2 = new JobOrderInput(obj);
		assertEquals(obj.getFileType(), clone2.getFileType());
		assertEquals(obj.getFileNameType(), clone2.getFileNameType());
		assertEquals(obj.getFamily(), clone.getFamily());
		assertEquals(0, clone2.getNbFilenames());
		assertEquals(0, clone2.getNbTimeIntervals());
	}

	/**
	 * Test to string
	 */
	@Test
	public void testToString() {

		JobOrderInputFile input1 = new JobOrderInputFile("filename", "keyObjectStorage");
		JobOrderTimeInterval input2 = new JobOrderTimeInterval("start", "stop", "fileName");

		JobOrderInput obj = new JobOrderInput();
		obj.setFileType("name");
		obj.setFileNameType(JobOrderFileNameType.REGEXP);
		obj.addTimeInterval(input2);
		obj.addFilename("filename", "keyObjectStorage");
		obj.setFamily(ProductFamily.L0_ACN);

		String str = obj.toString();
		assertTrue(str.contains("fileType: name"));
		assertTrue(str.contains("fileNameType: REGEXP"));
		assertTrue(str.contains("filenames: "));
		assertTrue(str.contains(input1.toString()));
		assertTrue(str.contains("nbFilenames: 1"));
		assertTrue(str.contains("timeIntervals: "));
		assertTrue(str.contains(input2.toString()));
		assertTrue(str.contains("nbTimeIntervals: 1"));
		assertTrue(str.contains("family: L0_ACN"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(JobOrderInput.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
