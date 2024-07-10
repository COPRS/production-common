/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.ingestion.worker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

public class TestRegex {
	private static final List<String> AUX_NAMES = Arrays.asList(	
		"S1B_OPER_MPL_ORBPRE_20180222T200325_20180301T200325_0001.EOF",
		"S1B_AUX_INS_V20160422T000000_G20160922T094114.SAFE",
		"S1B_AUX_PP1_V20160422T000000_G20171003T120152.SAFE",
		"S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml",
		"S1B_OPER_MPL_ORBPRE_20180222T200325_20180301T200325_0001.EOF",
		"S1B_OPER_MPL_ORBSCT_20160425T224606_99999999T999999_0013.EOF",
		"S1B_AUX_PP1_V20160422T000000_G20171003T120152.SAFE",
		"S1B_AUX_CAL_V20160422T000000_G20170328T092822.SAFE",
		"S1B_AUX_INS_V20160422T000000_G20160922T094114.SAFE",
		"S1B_OPER_AUX_RESORB_OPOD_20180227T082030_V20180227T040822_20180227T072552.EOF",
		"S1A_AUX_CAL_V20171017T080000_G20180622T082918.SAFE",
		"S1A_AUX_INS_V20171017T080000_G20180313T104658.SAFE",
		"S1A_AUX_PP1_V20171017T080000_G20180627T080350.SAFE",
		"S1A_OPER_AUX_RESORB_OPOD_20180914T023156_V20180913T221524_20180914T013254.EOF",
		"S1A_OPER_MPL_ORBPRE_20180909T200350_20180916T200350_0001.EOF",
		"S1A_OPER_MPL_ORBPRE_20180913T200404_20180920T200404_0001.EOF"
	);
	
	private static final List<String> IGNORED = Arrays.asList("foo.tmp","lost+found");
	
	private static final String PATTERN_STR = "^[0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z]_((OPER|TEST|REPR)_)?(AUX_OBMEMC|AUX_PP1|AUX_PP2|AUX_CAL|AUX_INS|AUX_RESORB|AUX_WND|AUX_ICE|AUX_WAV|MPL_ORBPRE|MPL_ORBSCT)_[^/]*\\.(xml|XML|EOF|SAFE)?$";
	
	@Test
	public final void testAuxRegex() {
		final Pattern auxPattern = Pattern.compile(PATTERN_STR);
		
		for (final String name : AUX_NAMES) {
			if (!auxPattern.matcher(name).matches()) {
				fail(name + " does not match " + auxPattern);
			}
			else {
				System.out.println(name + " matches " + auxPattern);
			}
		}		
	}
	
	@Test
	public final void testAuxRegex_ShallNotMAtchOnSubdirs() {
		final Pattern auxPattern = Pattern.compile(PATTERN_STR);		
		assertEquals(false, auxPattern.matcher("S1A_AUX_INS_V20171017T080000_G20180313T104658.SAFE/data/s1a-aux-ins.xml").matches());
	}
	
	private static final String OTHER_PATTERN = "(.*\\.tmp$|^lost\\+found$)";
	
	@Test
	public final void testIgnorePattern() {
		final Pattern ignorePattern = Pattern.compile(OTHER_PATTERN);
		
		for (final String name : AUX_NAMES) {
			if (ignorePattern.matcher(name).matches()) {
				fail(name + " should not match " + ignorePattern);
			}
			else {
				System.out.println(name + " is fine");
			}
		}
		
		for (final String name : IGNORED) {
			if (!ignorePattern.matcher(name).matches()) {
				fail(name + " does not match " + ignorePattern);
			}
			else {
				System.out.println(name + " is fine");
			}
		}
		
	}
	
	@Test
	public final void testNegativeLookahead() {
		final String pattern = "^S1A_(?!IW|WV|EW|S[1-6])[A-Z][A-Z]_RAW__0S.*";
		assertTrue("S1A_XX_RAW__0S123".matches(pattern));
		assertFalse("S1A_EW_RAW__0S123".matches(pattern));
		assertFalse("S1A_WV_RAW__0S123".matches(pattern));
		assertFalse("S1A_S4_RAW__0S123".matches(pattern));
	}
}
