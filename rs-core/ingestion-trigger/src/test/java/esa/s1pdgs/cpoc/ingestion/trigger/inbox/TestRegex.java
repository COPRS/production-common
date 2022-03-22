package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public final void tes() {
		final String fool = "1590759627";
		System.out.println(fool);
		System.out.println(System.currentTimeMillis());
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
	
	private static final Pattern SESSION_PATTERN = Pattern.compile("^([a-z_]{4}/)?"
			+ "([0-9a-z_]{2})([0-9a-z_]{1})/([0-9a-z_]+)/(ch[0|_]?[1-2]/)?"
			+ "(DCS_[0-9]{2}_([a-zA-Z0-9_]*)_ch([12])_(DSDB|DSIB).*\\.(raw|aisp|xml))", 
			Pattern.CASE_INSENSITIVE
	);	
	
	private static final Pattern SESSION_PATTERN_CASE_SENS = Pattern.compile("^([A-Za-z_]{4}/)?([0-9A-Za-z_]{2})([0-9A-Za-z_]{1})/"
			+ "([0-9A-Za-z_]+)/(ch[0|_]?[1-2]/)?(DCS_[0-9]{2}_([a-zA-Z0-9_]*)_ch([12])_(DSDB|DSIB).*\\.(raw|aisp|xml|RAW|AISP|XML))$");
	
	@Test
	public final void tesPAtternCaseSens() {
		final String otherName = 
							"S1B/L20191204153633245000201/DCS_02_L20191204153633245000201_ch1_DSDB_00003.raw";
		final Matcher matcher = SESSION_PATTERN_CASE_SENS.matcher(otherName);
		
		System.out.println(SESSION_PATTERN_CASE_SENS);
		if (!matcher.matches()) {
			throw new RuntimeException();
		}
		System.out.println(matcher.group(7));
	}
			
	@Test
	public final void tesPAtternt() {
		final String name = "S1B/L20180724144436762001030/DCS_02_L20180724144436762001030_ch2_DSDB_00025.raw";
		final Matcher matcher = SESSION_PATTERN.matcher(name);
		
		System.out.println(SESSION_PATTERN);
		if (!matcher.matches()) {
			throw new RuntimeException();
		}
		System.out.println(matcher.group(7));
	}
	
	@Test
	public void testDefaultRegexFor_RF_HK_GP_Products() {
		final String regex = "^S1([A-Z_]{1}).*(GP|HK|RF).*SAFE(.zip)?$";
		
		assertTrue("S1A_RF_RAW__0SDH_20200120T123147_20200120T123148_030884_038B5A_8A67.SAFE.zip".matches(regex));
		assertTrue("S1A_RF_RAW__0SDH_20200120T123147_20200120T123148_030884_038B5A_8A67.SAFE".matches(regex));
		assertFalse("S1A_IW_RAW__0SDH_20200120T123147_20200120T123148_030884_038B5A_8A67.SAFE".matches(regex));

	}

}


