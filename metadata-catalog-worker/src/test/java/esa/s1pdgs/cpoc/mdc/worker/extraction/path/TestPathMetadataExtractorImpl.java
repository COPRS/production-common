package esa.s1pdgs.cpoc.mdc.worker.extraction.path;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;

import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;

public class TestPathMetadataExtractorImpl {	
	/*
	 * Example rel paths:
	 * WILE/S1B/L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSIB.xml
WILE/S1B/L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSDB_00027.raw
WILE/S1B/L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSDB_00003.raw
	 */

	@Test
	public final void test() {
		final String regex = "^([a-z_]{4})/([0-9a-z_]{2})([0-9a-z_]{1})/([0-9a-z_]+)/ch0?([1-2])/.+";
		final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		
		final Map<String,Integer> conf = new HashMap<>();
		conf.put("stationCode", 1);
		conf.put("missionId", 2);
		conf.put("satelliteId", 3);
		conf.put("sessionId", 4);
		conf.put("channelId", 5);
		
		final PathMetadataExtractor uut = new PathMetadataExtractorImpl(pattern, conf);
		final CatalogJob job = new CatalogJob();
		job.setRelativePath("WILE/S1B/L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSDB_00027.raw");
		
		final Map<String,String> actual = uut.metadataFrom(job);
		assertEquals("WILE", actual.get("stationCode"));
		assertEquals("S1", actual.get("missionId"));
		assertEquals("B", actual.get("satelliteId"));
		assertEquals("L20180724144436762001030", actual.get("sessionId"));
		assertEquals("1", actual.get("channelId"));		
	}

}
