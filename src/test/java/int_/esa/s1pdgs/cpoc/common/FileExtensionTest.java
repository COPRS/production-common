package int_.esa.s1pdgs.cpoc.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import int_.esa.s1pdgs.cpoc.common.FileExtension;

/**
 * Test enumeration FileExtension
 */
public class FileExtensionTest {
	
	/**
	 * Test when extension is valid
	 */
	@Test
	public void testValidExtensions() {
		assertEquals(FileExtension.XML, FileExtension.valueOfIgnoreCase("xml"));
		assertEquals(FileExtension.DAT, FileExtension.valueOfIgnoreCase("DAT"));
		assertEquals(FileExtension.XSD, FileExtension.valueOfIgnoreCase("xsd"));
		assertEquals(FileExtension.EOF, FileExtension.valueOfIgnoreCase("EoF"));
		assertEquals(FileExtension.SAFE, FileExtension.valueOfIgnoreCase("Safe"));
	}
	
	/**
	 * Test when extension is invalid
	 */
	@Test
	public void testInvalidExtensions() {
		assertEquals(FileExtension.UNKNOWN, FileExtension.valueOfIgnoreCase("xmld"));
		assertEquals(FileExtension.UNKNOWN, FileExtension.valueOfIgnoreCase("UNKNOWN"));
	}
}
