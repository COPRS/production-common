package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import esa.s1pdgs.cpoc.ingestion.trigger.xbip.XbipInboxEntryFactory;

// Test filter regex
public class TestRegex {	
	private final String NEW_REGEX = "WILE/S1(A|B)/([A-Za-z0-9_]+)/ch0?(1|2)/([0-9a-zA-Z_]+DSIB\\.(xml|XML)|[0-9a-zA-Z_]+DSDB.*\\.(raw|RAW|aisp|AISP))$";
		
	@Test
	public final void testMatches() {
		assertTrue("WILE/S1A/L20180724144436762001030/ch02/DCS_02_L20180724144436762001030_ch2_DSIB.xml".matches(NEW_REGEX));
		assertTrue("WILE/S1A/L20180724144436762001030/ch02/DCS_02_L20180724144436762001030_ch2_DSDB_00028.raw".matches(NEW_REGEX));
	}
	
	@Test
	public final void testDoesntMatch() {
		assertFalse("WILE/S1A/.L20180724144436762001030/ch02/.DCS_02_L20180724144436762001030_ch2_DSIB.xml".matches(NEW_REGEX));
		assertFalse("WILE/S1A/L20180724144436762001030/ch02/.DCS_02_L20180724144436762001030_ch2_DSDB_00028.raw".matches(NEW_REGEX));
	}
	
	
	@Test
	public final void testNewRegex() {
		//          WILE/S1B/S1B__MPS__________017080/ch01/DCS_95_S1B__MPS__________017080_ch1_DSDB_00001.raw
		assertTrue("WILE/S1B/S1B__MPS__________017080/ch01/DCS_95_S1B__MPS__________017080_ch1_DSDB_00002.raw".matches(NEW_REGEX));
	}
	
	//private final String NEW_REGEX = "WILE/S1(A|B)/([A-Za-z0-9_]+)/ch0?(1|2)/([0-9a-zA-Z_]+DSIB\\.(xml|XML)|[0-9a-zA-Z_]+DSDB.*\\.(raw|RAW|aisp|AISP))$
	@Test
	public final void testAnotherProduct() {
		assertTrue("WILE/S1B/L20180724144436762001030/ch02/DCS_02_L20180724144436762001030_ch2_DSDB_00025.raw".matches(NEW_REGEX));
	}

	@Test
	public final void testFlorian() {
		assertTrue("S1A_OPER_REP_MP_MP__PDMC_20200303T093232_V20200303T170000_20200319T190000.xml".matches("^S1[ABCD_]_OPER_REP_MP_MP__PDMC.*$"));
	}
	
	public static final Pattern SESSION_PATTERN = Pattern.compile("^([a-z_]{4}/)?"
			+ "([0-9a-z_]{2})([0-9a-z_]{1})/([0-9a-z_]+)/(ch[0|_]?[1-2]/)?"
			+ "(DCS_[0-9]{2}_([a-zA-Z0-9_]*)_ch([12])_(DSDB|DSIB).*\\.(raw|aisp|xml))", 
			Pattern.CASE_INSENSITIVE
	);	
	
	@Test
	public final void testMagicRegex() {
		final String path = "S1A/DCS_04_20200407131322032022_dat/ch_2/DCS_04_20200407131322032022_ch2_DSDB_00057.raw";
		final Matcher matcher = XbipInboxEntryFactory.SESSION_PATTERN.matcher(path);		
		assertTrue(matcher.matches());	
		
		final String actual = matcher.group(4);		
		assertEquals("DCS_04_20200407131322032022_dat", actual);
		
		System.out.println(path.substring(path.indexOf(actual)));
		
		
	}
}


