package esa.s1pdgs.cpoc.ingestion.trigger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

// Test filter regex
public class TestRegex {

	private final String REGEX = "(WILE|MTI_|SGS_|INU_)/S1(A|B)/([A-Za-z0-9_]+)/ch0(1|2)/([0-9a-zA-Z_]+DSIB\\.(xml|XML)|[0-9a-zA-Z_]+DSDB.*\\.(raw|RAW|aisp|AISP))$";
	
	@Test
	public final void testMatches() {
		assertTrue("WILE/S1A/L20180724144436762001030/ch02/DCS_02_L20180724144436762001030_ch2_DSIB.xml".matches(REGEX));
		assertTrue("WILE/S1A/L20180724144436762001030/ch02/DCS_02_L20180724144436762001030_ch2_DSDB_00028.raw".matches(REGEX));
	}
	
	@Test
	public final void testDoesntMatch() {
		assertFalse("WILE/S1A/.L20180724144436762001030/ch02/.DCS_02_L20180724144436762001030_ch2_DSIB.xml".matches(REGEX));
		assertFalse("WILE/S1A/L20180724144436762001030/ch02/.DCS_02_L20180724144436762001030_ch2_DSDB_00028.raw".matches(REGEX));
	}
}
