package esa.s1pdgs.cpoc.jobgenerator.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings;

/**
 * Test the class JobGeneratorSettings
 * 
 * @author Cyrielle Gailliard
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class JobGeneratorSettingsTest {

	/**
	 * Job generator settings
	 */
	@Autowired
	private JobGeneratorSettings jobGenSettings;

	/**
	 * Test the settings initialization by parsing the application file
	 */
	@Test
	public void testGeneralSettings() {
		assertEquals("Invalid directory", "./test/data/l0_config/task_tables/", jobGenSettings.getDiroftasktables());
		assertEquals("Invalid directory", 500, jobGenSettings.getJobgenfixedrate());
		assertEquals("Invalid maxnumberoftasktables", 2, jobGenSettings.getMaxnboftasktable());
		assertEquals("Invalid maxnumberofjobs", 20, jobGenSettings.getMaxnumberofjobs());
		assertEquals("Invalid waitmedataraw tempo", 2000, jobGenSettings.getWaitprimarycheck().getTempo());
		assertEquals("Invalid waitmedataraw retries", 2, jobGenSettings.getWaitprimarycheck().getRetries());
		assertEquals("Invalid waitmedataraw tempo", 3000, jobGenSettings.getWaitmetadatainput().getTempo());
		assertEquals("Invalid waitmedatainput retries", 5, jobGenSettings.getWaitmetadatainput().getRetries());
		assertEquals("Invalid default output family", "L0_ACN", jobGenSettings.getDefaultfamily());
		assertEquals("Invalid size of output families", 8, jobGenSettings.getInputfamilies().size());
        assertEquals("Invalid size of input families", 18, jobGenSettings.getOutputfamilies().size());
	}

	/**
	 * Test the settings initialization by parsing the application file
	 */
	@Test
	public void testMaps() {
		// Test map product type / family
		assertEquals("Invalid value of inputfamiliesstr",
				"MPL_ORBPRE:AUXILIARY_FILE||MPL_ORBSCT:AUXILIARY_FILE||AUX_OBMEMC:AUXILIARY_FILE||AUX_CAL:AUXILIARY_FILE||AUX_PP1:AUXILIARY_FILE||AUX_INS:AUXILIARY_FILE||AUX_RESORB:AUXILIARY_FILE||AUX_RES:AUXILIARY_FILE",
				jobGenSettings.getInputfamiliesstr());
		assertEquals("Invalid 1st output family", ProductFamily.AUXILIARY_FILE,
				jobGenSettings.getInputfamilies().get("MPL_ORBSCT"));
		assertEquals("Invalid directory", ProductFamily.AUXILIARY_FILE, jobGenSettings.getInputfamilies().get("AUX_RESORB"));
		
        assertEquals("Invalid value of outputfamiliesstr",
                "SM_RAW__0S:L0_SLICE||IW_RAW__0S:L0_SLICE||EW_RAW__0S:L0_SLICE||WV_RAW__0S:L0_SLICE||RF_RAW__0S:L0_SLICE||AN_RAW__0S:L0_SLICE||EN_RAW__0S:L0_SLICE||ZS_RAW__0S:L0_SLICE||ZE_RAW__0S:L0_SLICE||ZI_RAW__0S:L0_SLICE||ZW_RAW__0S:L0_SLICE||GP_RAW__0_:BLANK||HK_RAW__0_:BLANK||REP_ACQNR:L0_REPORT||REP_L0PSA_:L0_REPORT||REP_EFEP_:L0_REPORT||IW_GRDH_1S:L1_SLICE||IW_GRDH_1A:L1_ACN",
                jobGenSettings.getOutputfamiliesstr());
        assertEquals("Invalid 1st output family", ProductFamily.L0_SLICE,
                jobGenSettings.getOutputfamilies().get("SM_RAW__0S"));
        assertEquals("Invalid directory", ProductFamily.L0_REPORT, jobGenSettings.getOutputfamilies().get("REP_EFEP_"));
        assertEquals("Invalid directory", ProductFamily.L1_SLICE,
                jobGenSettings.getOutputfamilies().get("IW_GRDH_1S"));
        assertEquals("Invalid directory", ProductFamily.L1_ACN, jobGenSettings.getOutputfamilies().get("IW_GRDH_1A"));
		// Test map acquisition / overloap
		assertEquals("Invalid size of output familiestype overlap", 4, jobGenSettings.getTypeOverlap().size());
		assertEquals("Invalid value of output familiestype overlap", 7.4F,
				jobGenSettings.getTypeOverlap().get("IW").floatValue(), 0);
		// Test map acquisition / slice length
		assertEquals("Invalid size of output typeslicelength overlap", 4, jobGenSettings.getTypeSliceLength().size());
		assertEquals("Invalid value of output familiestype overlap", 0.0F,
				jobGenSettings.getTypeSliceLength().get("WM").floatValue(), 0);
		// Test map product type / index type
		assertEquals("Invalid size of mapTypeMeta", 10, jobGenSettings.getMapTypeMeta().size());
		assertEquals("Invalid value of mapTypeMeta", "AUX_TUT", jobGenSettings.getMapTypeMeta().get("AUX_TUTU"));

	}
	
	@Test
	public void testSimulateMapStrEmpty() {
		JobGeneratorSettings settings = new JobGeneratorSettings();
		
		// Test map product type / family: Force str to null
		settings.setOutputfamiliesstr(null);
		
		// Reinit maps
		settings.initMaps();
		assertTrue("Map outputfamilies should be empty", settings.getOutputfamilies().size() == 0);
	}

	@Test
	public void testInvalidMapMapping() {
		JobGeneratorSettings settings = new JobGeneratorSettings();
		
		// Test map when invalid key value separator
		settings.setOutputfamiliesstr("o1:f1||o2");
		
		// Reinit maps
		settings.initMaps();
		assertTrue("Map outputfamilies should be empty", settings.getOutputfamilies().size() == 1);
	}
	
	@Test
	public void testToString() {
		String settings = this.jobGenSettings.toString();
		assertTrue("Should contain maxnboftasktable", settings.contains("maxnboftasktable"));
		assertTrue("Should contain maxnumberofjobs", settings.contains("maxnumberofjobs"));
		assertTrue("Should contain waitprimarycheck", settings.contains("waitprimarycheck"));
		assertTrue("Should contain waitmetadatainput", settings.contains("waitmetadatainput"));
		assertTrue("Should contain diroftasktables", settings.contains("diroftasktables"));
		assertTrue("Should contain jobgenfixedrate", settings.contains("jobgenfixedrate"));
		assertTrue("Should contain defaultfamily", settings.contains("defaultfamily"));
		assertTrue("Should contain outputfamiliesstr", settings.contains("outputfamiliesstr"));
		assertTrue("Should contain outputfamilies", settings.contains("outputfamilies"));
		assertTrue("Should contain typeOverlap", settings.contains("typeOverlap"));
		assertTrue("Should contain typeSliceLength", settings.contains("typeSliceLength"));
		assertTrue("Should contain mapTypeMeta", settings.contains("mapTypeMeta"));
	}
}
