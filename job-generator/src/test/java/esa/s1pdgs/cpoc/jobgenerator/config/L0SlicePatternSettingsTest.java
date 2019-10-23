package esa.s1pdgs.cpoc.jobgenerator.config;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertEquals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.jobgenerator.config.L0SlicePatternSettings;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@ActiveProfiles("l1")
public class L0SlicePatternSettingsTest {

	@Autowired
	private L0SlicePatternSettings l0SlicePatternSettings;


	@Test
	public void testSettings() {
		assertEquals(2, l0SlicePatternSettings.getMGroupSatId());
		assertEquals(1, l0SlicePatternSettings.getMGroupMissionId());
		assertEquals(4, l0SlicePatternSettings.getMGroupAcquisition());
		assertEquals(6, l0SlicePatternSettings.getGroupPolarisation());
		assertEquals(7, l0SlicePatternSettings.getMGroupStartTime());
		assertEquals(8, l0SlicePatternSettings.getMGroupStopTime());
		assertEquals(
				"^([0-9a-z]{2})([0-9a-z]){1}_(([0-9a-z]{2})_RAW__0([0-9a-z_])([0-9a-z_]{2}))_([0-9a-z]{15})_([0-9a-z]{15})_([0-9a-z_]{6})\\w{1,}\\.SAFE(/.*)?$",
				l0SlicePatternSettings.getRegexp());
	}
	
	@Test
	public void testPatternMatch() {
		String file = "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE";
		Pattern p = Pattern.compile(l0SlicePatternSettings.getRegexp(), Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(file);
		if (m.matches()) {
			assertEquals("Satellite identifier does not match", "A", m.group(l0SlicePatternSettings.getMGroupSatId()));
			assertEquals("Mission identifier does not match", "S1", m.group(l0SlicePatternSettings.getMGroupMissionId()));
			assertEquals("Acquisition does not match", "IW", m.group(l0SlicePatternSettings.getMGroupAcquisition()));
			assertEquals("Start time does not match", "20171213T121623", m.group(l0SlicePatternSettings.getMGroupStartTime()));
			assertEquals("Stop time does not match", "20171213T121656", m.group(l0SlicePatternSettings.getMGroupStopTime()));
		} else {
			fail("The file shall match the regular expression");
		}
	}
	
	@Test
	public void testPatternNotMatch() {
		String file = "SA_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE";
		Pattern p = Pattern.compile(l0SlicePatternSettings.getRegexp(), Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(file);
		if (m.matches()) {
			fail("The file shall not match the regular expression");
		}
	}

}
