package fr.viveris.s1pdgs.jobgenerator.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import esa.s1pdgs.cpoc.common.ProductFamily;

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
		assertEquals("Invalid size of output families", 18, jobGenSettings.getOutputfamilies().size());
	}

	/**
	 * Test the settings initialization by parsing the application file
	 */
	@Test
	public void testMaps() {
		// Test map product type / family
		assertEquals("Invalid value of outputfamiliesstr",
				"SM_RAW__0S:L0_PRODUCT||IW_RAW__0S:L0_PRODUCT||EW_RAW__0S:L0_PRODUCT||WV_RAW__0S:L0_PRODUCT||RF_RAW__0S:L0_PRODUCT||AN_RAW__0S:L0_PRODUCT||EN_RAW__0S:L0_PRODUCT||ZS_RAW__0S:L0_PRODUCT||ZE_RAW__0S:L0_PRODUCT||ZI_RAW__0S:L0_PRODUCT||ZW_RAW__0S:L0_PRODUCT||GP_RAW__0_:BLANK||HK_RAW__0_:BLANK||REP_ACQNR:L0_REPORT||REP_L0PSA_:L0_REPORT||REP_EFEP_:L0_REPORT||IW_GRDH_1S:L1_PRODUCT||IW_GRDH_1A:L1_ACN",
				jobGenSettings.getOutputfamiliesstr());
		assertEquals("Invalid 1st output family", ProductFamily.L0_PRODUCT,
				jobGenSettings.getOutputfamilies().get("SM_RAW__0S"));
		assertEquals("Invalid directory", ProductFamily.L0_REPORT, jobGenSettings.getOutputfamilies().get("REP_EFEP_"));
		assertEquals("Invalid directory", ProductFamily.L1_PRODUCT,
				jobGenSettings.getOutputfamilies().get("IW_GRDH_1S"));
		assertEquals("Invalid directory", ProductFamily.L1_ACN, jobGenSettings.getOutputfamilies().get("IW_GRDH_1A"));
		// Test map acquisition / overloap
		assertEquals("Invalid value of typeoverlapstr", "EW:8.2F||IW:7.4F||SM:7.7F||WM:0.0F",
				jobGenSettings.getTypeoverlapstr());
		assertEquals("Invalid size of output familiestype overlap", 4, jobGenSettings.getTypeOverlap().size());
		assertEquals("Invalid value of output familiestype overlap", 7.4F,
				jobGenSettings.getTypeOverlap().get("IW").floatValue(), 0);
		// Test map acquisition / slice length
		assertEquals("Invalid value of typeslicelenstr", "EW:60.0F||IW:25.0F||SM:25.0F||WM:0.0F",
				jobGenSettings.getTypeslicelenstr());
		assertEquals("Invalid size of output typeslicelength overlap", 4, jobGenSettings.getTypeSliceLength().size());
		assertEquals("Invalid value of output familiestype overlap", 0.0F,
				jobGenSettings.getTypeSliceLength().get("WM").floatValue(), 0);
		// Test map product type / index type
		assertEquals("Invalid value of mapTypeMetaStr", "AUX_RES:AUX_RESORB||AUX_TUTU:AUX_TUT",
				jobGenSettings.getMapTypeMetaStr());
		assertEquals("Invalid size of mapTypeMeta", 2, jobGenSettings.getMapTypeMeta().size());
		assertEquals("Invalid value of mapTypeMeta", "AUX_TUT", jobGenSettings.getMapTypeMeta().get("AUX_TUTU"));

	}
	
	@Test
	public void testSimulateMapStrEmpty() {
		JobGeneratorSettings settings = new JobGeneratorSettings();
		
		// Test map product type / family: Force str to null
		settings.setOutputfamiliesstr(null);
		settings.setTypeoverlapstr("");
		settings.setTypeslicelenstr(null);
		settings.setMapTypeMetaStr("");
		
		// Reinit maps
		settings.initMaps();
		assertTrue("Map outputfamilies should be empty", settings.getOutputfamilies().size() == 0);
		assertTrue("Map typeOverlap should be empty", settings.getTypeOverlap().size() == 0);
		assertTrue("Map typeSliceLength should be empty", settings.getTypeSliceLength().size() == 0);
		assertTrue("Map mapTypeMeta should be empty", settings.getMapTypeMeta().size() == 0);
	}

	@Test
	public void testInvalidMapMapping() {
		JobGeneratorSettings settings = new JobGeneratorSettings();
		
		// Test map when invalid key value separator
		settings.setOutputfamiliesstr("o1:f1||o2");
		settings.setTypeoverlapstr("EW:8.2F||IW:7.4F||SM7.7F||WM:0.0:F");
		settings.setTypeslicelenstr("EW0.0F||IW25.0F||SM:25.0F||WM:0:0F");
		settings.setMapTypeMetaStr("AUX_RESAUX_RESORB||AUX_TUTU:AUX_TUT");
		
		// Reinit maps
		settings.initMaps();
		assertTrue("Map outputfamilies should be empty", settings.getOutputfamilies().size() == 1);
		assertTrue("Map typeOverlap should be empty", settings.getTypeOverlap().size() == 2);
		assertTrue("Map typeSliceLength should be empty", settings.getTypeSliceLength().size() == 1);
		assertTrue("Map mapTypeMeta should be empty", settings.getMapTypeMeta().size() == 1);
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
		assertTrue("Should contain typeoverlapstr", settings.contains("typeoverlapstr"));
		assertTrue("Should contain typeOverlap", settings.contains("typeOverlap"));
		assertTrue("Should contain typeslicelenstr", settings.contains("typeslicelenstr"));
		assertTrue("", settings.contains("EW:8.2F||IW:7.4F||SM:7.7F||WM:0.0F"));
		assertTrue("Should contain typeSliceLength", settings.contains("typeSliceLength"));
		assertTrue("Should contain mapTypeMetaStr", settings.contains("mapTypeMetaStr"));
		assertTrue("Should contain mapTypeMeta", settings.contains("mapTypeMeta"));
	}
}
