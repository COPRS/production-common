package esa.s1pdgs.cpoc.ipf.preparation.worker.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings.CategoryConfig;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings.InputWaitingConfig;

/**
 * Test the class JobGeneratorSettings
 * 
 * @author Cyrielle Gailliard
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class IpfPreparationWorkerSettingsTest {

	/**
	 * Job generator settings
	 */
	@Autowired
	private IpfPreparationWorkerSettings jobGenSettings;

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
		assertEquals("Invalid waitmedataraw retries", 2, jobGenSettings.getWaitprimarycheck().getMaxTimelifeS());
		assertEquals("Invalid waitmedataraw tempo", 3000, jobGenSettings.getWaitmetadatainput().getTempo());
		assertEquals("Invalid waitmedatainput retries", 5, jobGenSettings.getWaitmetadatainput().getMaxTimelifeS());
		assertEquals("Invalid default output family", "L0_ACN", jobGenSettings.getDefaultfamily());
		assertEquals("Invalid size of output families", 8, jobGenSettings.getInputfamilies().size());
        assertEquals("Invalid size of input families", 18, jobGenSettings.getOutputfamilies().size());
        assertEquals("Invalid size of product categories", 4, jobGenSettings.getProductCategories().size());
        assertEquals("Invalid size of input waiting", 3, jobGenSettings.getInputWaiting().size());
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
		IpfPreparationWorkerSettings settings = new IpfPreparationWorkerSettings();
		
		// Test map product type / family: Force str to null
		settings.setOutputfamiliesstr(null);
		
		// Reinit maps
		settings.initMaps();
		assertTrue("Map outputfamilies should be empty", settings.getOutputfamilies().size() == 0);
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
		assertTrue("Should contain productCategories", settings.contains("productCategories"));
		assertTrue("Should contain inputWaiting", settings.contains("inputWaiting"));
	}
	
	@Test
	public void testProductCategoriesAttributes() {
		Map<ProductCategory, CategoryConfig> productCategories = jobGenSettings.getProductCategories();
		assertEquals(500, productCategories.get(ProductCategory.AUXILIARY_FILES).getFixedDelayMs());
		assertEquals(2000, productCategories.get(ProductCategory.AUXILIARY_FILES).getInitDelayPollMs());
		assertEquals(500, productCategories.get(ProductCategory.EDRS_SESSIONS).getFixedDelayMs());
		assertEquals(2000, productCategories.get(ProductCategory.EDRS_SESSIONS).getInitDelayPollMs());
		assertEquals(500, productCategories.get(ProductCategory.LEVEL_SEGMENTS).getFixedDelayMs());
		assertEquals(2000, productCategories.get(ProductCategory.LEVEL_SEGMENTS).getInitDelayPollMs());
		assertEquals(500, productCategories.get(ProductCategory.LEVEL_PRODUCTS).getFixedDelayMs());
		assertEquals(2000, productCategories.get(ProductCategory.LEVEL_PRODUCTS).getInitDelayPollMs());
	}

	@Test
	public void testInputWaitingAttributes() 	
	{
		List<InputWaitingConfig> inputWaiting = jobGenSettings.getInputWaiting();
		assertEquals(".._RAW__0_(SLC|GRD).*_1", inputWaiting.get(0).getProcessorNameRegexp());
		assertEquals(".*", inputWaiting.get(0).getProcessorVersionRegexp());
		assertEquals("Orbit", inputWaiting.get(0).getInputIdRegexp());
		assertEquals("(PT|NRT)", inputWaiting.get(0).getTimelinessRegexp());
		assertEquals(0, inputWaiting.get(0).getWaitingInSeconds());
		assertEquals(3600, inputWaiting.get(0).getDelayInSeconds());

		assertEquals(".._RAW__0_(SLC|GRD).*_1", inputWaiting.get(1).getProcessorNameRegexp());
		assertEquals(".*", inputWaiting.get(1).getProcessorVersionRegexp());
		assertEquals("Orbit", inputWaiting.get(1).getInputIdRegexp());
		assertEquals("FAST24", inputWaiting.get(1).getTimelinessRegexp());
		assertEquals(57600, inputWaiting.get(1).getWaitingInSeconds());
		assertEquals(3600, inputWaiting.get(1).getDelayInSeconds());

		assertEquals(".._RAW__0_OCN__2", inputWaiting.get(2).getProcessorNameRegexp());
		assertEquals(".*", inputWaiting.get(2).getProcessorVersionRegexp());
		assertEquals("Orbit", inputWaiting.get(2).getInputIdRegexp());
		assertEquals("(PT|NRT|FAST24)", inputWaiting.get(2).getTimelinessRegexp());
		assertEquals(57600, inputWaiting.get(2).getWaitingInSeconds());
		assertEquals(3600, inputWaiting.get(2).getDelayInSeconds());
	}

}
