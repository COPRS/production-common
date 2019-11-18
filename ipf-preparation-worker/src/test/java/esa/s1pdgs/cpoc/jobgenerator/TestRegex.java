package esa.s1pdgs.cpoc.jobgenerator;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

public class TestRegex {
	
	private static final String pattern = "^([0-9a-zA-Z]{2})([0-9a-zA-Z]){1}_(SM|IW|EW)_RAW__0([0-9a-zA-Z_]{3})_([0-9a-zA-Z]{15})_([0-9a-zA-Z]{15})_([0-9a-zA-Z_]{6})\\w{1,}\\.SAFE$";

	private static final Pattern uut = Pattern.compile(pattern);
	
	private static final List<String> NAMES = Arrays.asList(
			"S1A_EW_RAW__0CDV_20180227T144704_20180227T144706_020793_023A68_401B.SAFE",
			"S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE"
	);
	
	@Test
	public final void testRegex() {
		for (final String name : NAMES) {
			assertEquals(true, uut.matcher(name).matches());
		}
		assertEquals(false, uut.matcher("S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE").matches());
	}
}
