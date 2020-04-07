package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
}


