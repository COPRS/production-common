package esa.s1pdgs.cpoc.ingestion.product;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.obs.ObsAdapter;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;

public class EdrsSessionFactoryTest {
	
	@Mock
	ObsAdapter obsAdapter;

	private final static String RELATIVE_PATH_1 = "L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSDB_00008.raw";
	private final static String RELATIVE_PATH_2 = "L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSIB.xml";
	private final static String RELATIVE_PATH_3 = "L20180724144436762001030/ch02/DCS_02_L20180724144436762001030_ch2_DSDB_00031.raw";
	private final static String RELATIVE_PATH_4 = "L20180724144436762001030/ch02/DCS_02_L20180724144436762001030_ch2_DSIB.xml";

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void newProducts() {
		EdrsSessionFactory uut = new EdrsSessionFactory("hostname");
		IngestionDto ingestionDto = new IngestionDto();
		ingestionDto.setMissionId("S1"); 
		ingestionDto.setSatelliteId("B");
		ingestionDto.setStationCode("WILE"); 
		ingestionDto.setPickupPath("pickup/path");
		ingestionDto.setRelativePath("DCS_00_L20000101000000000000000_ch1_DSIB.xml");
		List<Product<EdrsSessionDto>> result = uut.newProducts(new File("pickup/path/DCS_00_L20000101000000000000000_ch1_DSIB.xml"),
				ingestionDto, obsAdapter);
		assertEquals(1L, result.size());
		assertEquals(ProductFamily.EDRS_SESSION, result.get(0).getFamily());
		assertEquals(new File("pickup/path/DCS_00_L20000101000000000000000_ch1_DSIB.xml"), result.get(0).getFile());
		assertEquals(1L, result.get(0).getDto().getChannelId());
		assertEquals("hostname", result.get(0).getDto().getHostname());
		assertEquals("DCS_00_L20000101000000000000000_ch1_DSIB.xml", result.get(0).getDto().getKeyObjectStorage());
		assertEquals("S1", result.get(0).getDto().getMissionId());
		assertEquals("DCS_00_L20000101000000000000000_ch1_DSIB.xml", result.get(0).getDto().getProductName());
		assertEquals("SESSION", result.get(0).getDto().getProductType().name());
		assertEquals("B", result.get(0).getDto().getSatelliteId());
		assertEquals("L20000101000000000000000", result.get(0).getDto().getSessionId());
		assertEquals("WILE", result.get(0).getDto().getStationCode());
	}
	
	@Test
	public void extractChannelId() {
		EdrsSessionFactory uut = new EdrsSessionFactory("hostname");
		Assert.assertEquals(1, uut.extractChannelId(RELATIVE_PATH_1));
		Assert.assertEquals(1, uut.extractChannelId(RELATIVE_PATH_2));
		Assert.assertEquals(2, uut.extractChannelId(RELATIVE_PATH_3));
		Assert.assertEquals(2, uut.extractChannelId(RELATIVE_PATH_4));
	}

	@Test
	public void extractEdrsSessionFileType() {
		EdrsSessionFactory uut = new EdrsSessionFactory("hostname");
		Assert.assertEquals(EdrsSessionFileType.RAW, uut.extractEdrsSessionFileType(RELATIVE_PATH_1));
		Assert.assertEquals(EdrsSessionFileType.SESSION, uut.extractEdrsSessionFileType(RELATIVE_PATH_2));
		Assert.assertEquals(EdrsSessionFileType.RAW, uut.extractEdrsSessionFileType(RELATIVE_PATH_3));
		Assert.assertEquals(EdrsSessionFileType.SESSION, uut.extractEdrsSessionFileType(RELATIVE_PATH_4));
	}
	
	@Test
	public void extractSessionId() {
		EdrsSessionFactory uut = new EdrsSessionFactory("hostname");
		Assert.assertEquals("L20180724144436762001030", uut.extractSessionId(RELATIVE_PATH_1));
		Assert.assertEquals("L20180724144436762001030", uut.extractSessionId(RELATIVE_PATH_2));
		Assert.assertEquals("L20180724144436762001030", uut.extractSessionId(RELATIVE_PATH_3));
		Assert.assertEquals("L20180724144436762001030", uut.extractSessionId(RELATIVE_PATH_4));
	}
	
	@Test
	public void testRegexFrom() {
		String regex = "(.+DSIB\\.(xml|XML)|.+DSDB.*\\.(raw|RAW|aisp|AISP))";
		String regex2= "^(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(xml|raw))$";
		String regex3= ".*(DCS_[0-9]{2}_([a-zA-Z0-9_]{24})_ch([12])_DSIB\\.(xml))";
		
	    Assert.assertTrue(Pattern.matches(regex, "SESSION1/ch01/DCS_02_SESSION1_ch1_DSIB.xml"));
	    Assert.assertTrue(Pattern.matches(regex2, "L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSIB.xml"));
	    Assert.assertTrue(Pattern.matches(regex3, "L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSIB.xml"));
	    
	    Assert.assertTrue(Pattern.matches(regex, RELATIVE_PATH_2));
	    Assert.assertTrue(Pattern.matches(regex, RELATIVE_PATH_3));
	    Assert.assertTrue(Pattern.matches(regex, RELATIVE_PATH_4));
	    
	    Assert.assertTrue(Pattern.matches(EdrsSessionFactory.PATTERN_STR_RAW, RELATIVE_PATH_1));
	    Assert.assertTrue(Pattern.matches(EdrsSessionFactory.PATTERN_STR_XML, RELATIVE_PATH_2));
	    Assert.assertTrue(Pattern.matches(EdrsSessionFactory.PATTERN_STR_RAW, RELATIVE_PATH_3));
	    Assert.assertTrue(Pattern.matches(EdrsSessionFactory.PATTERN_STR_XML, RELATIVE_PATH_4));
	}

}
