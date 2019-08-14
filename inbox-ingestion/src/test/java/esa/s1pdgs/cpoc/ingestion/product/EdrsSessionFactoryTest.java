package esa.s1pdgs.cpoc.ingestion.product;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;

public class EdrsSessionFactoryTest {

	private final static String RELATIVE_PATH_1 = "L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSDB_00008.raw";
	private final static String RELATIVE_PATH_2 = "L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSIB.xml";
	private final static String RELATIVE_PATH_3 = "L20180724144436762001030/ch02/DCS_02_L20180724144436762001030_ch2_DSDB_00031.raw";
	private final static String RELATIVE_PATH_4 = "L20180724144436762001030/ch02/DCS_02_L20180724144436762001030_ch2_DSIB.xml";

	@Test
	public void extractChannelId() {

		EdrsSessionFactory uut = new EdrsSessionFactory();
		Assert.assertEquals(1, uut.extractChannelId(RELATIVE_PATH_1));
		Assert.assertEquals(1, uut.extractChannelId(RELATIVE_PATH_2));
		Assert.assertEquals(2, uut.extractChannelId(RELATIVE_PATH_3));
		Assert.assertEquals(2, uut.extractChannelId(RELATIVE_PATH_4));

	}

	@Test
	public void extractEdrsSessionFileType() {
		EdrsSessionFactory uut = new EdrsSessionFactory();
		Assert.assertEquals(EdrsSessionFileType.RAW, uut.extractEdrsSessionFileType(RELATIVE_PATH_1));
		Assert.assertEquals(EdrsSessionFileType.SESSION, uut.extractEdrsSessionFileType(RELATIVE_PATH_2));
		Assert.assertEquals(EdrsSessionFileType.RAW, uut.extractEdrsSessionFileType(RELATIVE_PATH_3));
		Assert.assertEquals(EdrsSessionFileType.SESSION, uut.extractEdrsSessionFileType(RELATIVE_PATH_4));
	}
	
	@Test
	public void testRegexFrom() {
		String regex = "(.+DSIB\\.(xml|XML)|.+DSDB.*\\.(raw|RAW|aisp|AISP))";
		
	    Assert.assertTrue(Pattern.matches(regex, RELATIVE_PATH_1));
	    Assert.assertTrue(Pattern.matches(regex, RELATIVE_PATH_2));
	    Assert.assertTrue(Pattern.matches(regex, RELATIVE_PATH_3));
	    Assert.assertTrue(Pattern.matches(regex, RELATIVE_PATH_4));
	}

}
