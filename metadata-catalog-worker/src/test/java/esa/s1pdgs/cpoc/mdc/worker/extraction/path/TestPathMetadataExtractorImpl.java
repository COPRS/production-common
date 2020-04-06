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

and from XBIP:

S1B/20200120162933019903/DCS_01_S1B_20200120162933019903_ch2_DSDB_00035.raw
 * 
 * 

	 */

	@Test
	public final void testNominal() {		
		runWith("WILE/S1B/L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSDB_00027.raw");			
	}
	
	@Test
	public final void testNoStationFolder() {		
		runWith("S1B/L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSDB_00027.raw");			
	}
	
	@Test
	public final void testNoLeadingZeroInChannelFolder() {		
		runWith("S1B/L20180724144436762001030/ch1/DCS_02_L20180724144436762001030_ch1_DSDB_00027.raw");			
	}
	
	@Test
	public final void testWithUnderscoreInChannelFolder() {		
		runWith("S1B/L20180724144436762001030/ch_1/DCS_02_L20180724144436762001030_ch1_DSDB_00027.raw");			
	}
	
	@Test
	public final void testNoChannelFolder() {		
		runWith("S1B/L20180724144436762001030/DCS_02_L20180724144436762001030_ch1_DSDB_00027.raw");			
	}
	
	@Test
	public final void testDsib() {		
		runWith("S1B/L20180724144436762001030/ch_1/DCS_04_20200401153105031936_ch1_DSIB.xml");			
	}
	

	private void runWith(final String path) {		
		final CatalogJob job = new CatalogJob();
		job.setRelativePath(path);	
		
		final String regex = "^([a-z_]{4}/)?([0-9a-z_]{2})([0-9a-z_]{1})/([0-9a-z_]+)/(ch[0|_]?[1-2]/)?"
				+ "(DCS_[0-9]{2}_([a-zA-Z0-9_]*)_ch([12])_(DSDB|DSIB).*\\.(raw|aisp|xml))$";
		
//		final String regex = "^([a-z_]{4}/)?([0-9a-z_]{2})([0-9a-z_]{1})/([0-9a-z_]+)/(ch[0|_]?[1-2]/)?"
//				+ "(DCS_[0-9]{2}_([a-zA-Z0-9_]{24})_ch([12])_(DSDB|DSIB).*\\.(raw|aisp|xml))$";
		
		// (DCS_[0-9]{2}_([a-zA-Z0-9_]{24})_(ch[12])_DSIB\\.xml)
		// (DCS_[0-9]{2}_([a-zA-Z0-9_]{24})_(ch[12])_DSDB_([0-9]{5})\\.(raw|aisp))
		
		// (DCS_[0-9]{2}_([a-zA-Z0-9_]{24})_(ch[12])_(DSDB|_DSIB).*\\.(raw|aisp))
		
		System.out.println(regex);
		
		final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		
		final Map<String,Integer> conf = new HashMap<>();
//		conf.put("stationCode", 1);
		conf.put("missionId", 2);
		conf.put("satelliteId", 3);
		conf.put("sessionId", 4);
		conf.put("channelId", 8);
		
		final PathMetadataExtractor uut = new PathMetadataExtractorImpl(pattern, conf);

		
		final Map<String,String> actual = uut.metadataFrom(job);
//		assertEquals("WILE", actual.get("stationCode"));
		assertEquals("S1", actual.get("missionId"));
		assertEquals("B", actual.get("satelliteId"));
		assertEquals("L20180724144436762001030", actual.get("sessionId"));
		assertEquals("1", actual.get("channelId"));
	}

}
