package fr.viveris.s1pdgs.common;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test enumeration EdrsSessionFileType
 */
public class EdrsSessionFileTypeTest {

	/**
	 * To check the raised custom exceptions
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Test from valid extension
	 */
	@Test
	public void testValueFromExtension() {
		assertEquals(EdrsSessionFileType.RAW, EdrsSessionFileType.valueFromExtension(FileExtension.RAW));
		assertEquals(EdrsSessionFileType.SESSION, EdrsSessionFileType.valueFromExtension(FileExtension.XML));
	}

	/**
	 * Test with unknown SAFE
	 */
	@Test
	public void testValueFromUnknownExtension() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(containsString("UNKNOWN"));
		EdrsSessionFileType.valueFromExtension(FileExtension.UNKNOWN);
	}

	/**
	 * Test with extension SAFE
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testValueFromSafeExtension() {
		EdrsSessionFileType.valueFromExtension(FileExtension.SAFE);
	}

	/**
	 * Test with extension EOF
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testValueFromEofExtension() {
		EdrsSessionFileType.valueFromExtension(FileExtension.EOF);
	}

	/**
	 * Test with extension XSD
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testValueFromXsdExtension() {
		EdrsSessionFileType.valueFromExtension(FileExtension.XSD);
	}

	/**
	 * Test with extension DAT
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testValueFromDatExtension() {
		EdrsSessionFileType.valueFromExtension(FileExtension.DAT);
	}
}
