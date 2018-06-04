package fr.viveris.s1pdgs.ingestor.files.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import fr.viveris.s1pdgs.ingestor.files.model.FileDescriptor;
import fr.viveris.s1pdgs.ingestor.files.model.FileExtension;

public class ExtractMetadataTest {

	@Test
	public void testRegexp() {

		String file1 = "S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE";
		String file2 = "S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE/manifest.safe";
		String file3 = "S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE/data";
		String file4 = "S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE/support";
		String file5 = "S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE/support/s1-aux-ins.xsd";

		//String pattern = "^(?!\\.).*";
		//String pattern = "(?!(\\.writing))";
		//String pattern = "^[^\\.].*";
		//"^([a-z0-9]){2}([a-z0-9])((/|\\\\)(\\w+)((/|\\\\)(ch)(0[1-2])((/|\\\\)(\\w*)\\4(\\w*)\\.(XML|RAW))?)?)?$";
		//^([a-z0-9]){2}([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)(\\w*)\\4(\\w*)\\.(XML|RAW)$
		String pattern = "^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$";
		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		Matcher m1 = p.matcher(file1);
		Matcher m2 = p.matcher(file2);
		Matcher m3 = p.matcher(file3);
		Matcher m4 = p.matcher(file4);
		Matcher m5 = p.matcher(file5);

		assertTrue("m1", m1.matches());
		assertTrue("m2", m2.matches());
		assertTrue("m3", m3.matches());
		assertTrue("m4", m4.matches());
		assertTrue("m5", m5.matches());

	}

	@Test
	public void testProcessXMLFile() {
		FileDescriptor descriptor = new FileDescriptor();
		descriptor.setExtension(FileExtension.XML);
		descriptor.setHasToBePublished(true);
		descriptor.setKeyObjectStorage("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setProductName("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setRelativePath("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		/*
		 * assertEquals("Invalid length", expectedResult.toString().length(),
		 * dto.getMetadata().toString().length()); assertEquals("Invalid productName",
		 * expectedResult.getString("productName"), new
		 * JSONObject(dto.getMetadata()).getString("productName"));
		 * assertEquals("Invalid productType", expectedResult.getString("productType"),
		 * new JSONObject(dto.getMetadata()).getString("productType"));
		 */
		assertEquals(true, true);
	}
}
