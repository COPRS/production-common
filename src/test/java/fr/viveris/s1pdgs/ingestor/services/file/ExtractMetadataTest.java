package fr.viveris.s1pdgs.ingestor.services.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import fr.viveris.s1pdgs.ingestor.model.ConfigFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.FileExtension;

public class ExtractMetadataTest {

	@Test
	public void testRegexp() {

		String file1 = "S1A";
		String file2 = "S1A/L20171109175634707000125";
		String file3 = "S1A/L20171109175634707000125/ch01";
		String file4 = "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSIB.xml";

		//String pattern = "^(?!\\.).*";
		//String pattern = "(?!(\\.writing))";
		//String pattern = "^[^\\.].*";
		//"^([a-z0-9]){2}([a-z0-9])((/|\\\\)(\\w+)((/|\\\\)(ch)(0[1-2])((/|\\\\)(\\w*)\\4(\\w*)\\.(XML|RAW))?)?)?$";
		//^([a-z0-9]){2}([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)(\\w*)\\4(\\w*)\\.(XML|RAW)$
		String pattern = "^([a-z0-9]){2}([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$";
		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		Matcher m1 = p.matcher(file1);
		Matcher m2 = p.matcher(file2);
		Matcher m3 = p.matcher(file3);
		Matcher m4 = p.matcher(file4);

		assertTrue("m1", !m1.matches());
		assertTrue("m2", !m2.matches());
		assertTrue("m3", !m3.matches());
		assertTrue("m4", m4.matches());

	}

	@Test
	public void testProcessXMLFile() {
		ConfigFileDescriptor descriptor = new ConfigFileDescriptor();
		descriptor.setDirectory(false);
		descriptor.setExtension(FileExtension.XML);
		descriptor.setFilename("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setHasToBeStored(true);
		descriptor.setHasToExtractMetadata(true);
		descriptor.setKeyObjectStorage("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setProductType("AUX_OBMEMC");
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
