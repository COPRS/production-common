package esa.s1pdgs.cpoc.ingestor.files.services;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import esa.s1pdgs.cpoc.ingestor.FileUtils;
import esa.s1pdgs.cpoc.ingestor.exceptions.FilePathException;
import esa.s1pdgs.cpoc.ingestor.files.services.AuxiliaryFileDescriptorService;

/**
 * Test the FileDescriptorService for auxiliary files
 * 
 * @author Cyrielle Gailliard
 *
 */
public class AuxiliaryFileDescriptorServiceTest {

	/**
	 * Wanted family
	 */
	protected static final String FAMILY = "CONFIG";

	/**
	 * Service to test
	 */
	private AuxiliaryFileDescriptorService service;

	/**
	 * To check the raised custom exceptions
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Initialization
	 */
	@Before
	public void init() {
		service = new AuxiliaryFileDescriptorService(FileUtils.TEST_DIR_ABS_PATH_SEP);
	}

	/**
	 * Check given str matches with pattern of the service
	 * 
	 * @param str
	 */
	private void checkMatchOk(String str) {
		Matcher matcher = service.getPattern().matcher(str);
		assertTrue(str + " should match with pattern", matcher.matches());
	}

	/**
	 * Check given str does not match with pattern of the service
	 * 
	 * @param str
	 */
	private void checkMatchKo(String str) {
		Matcher matcher = service.getPattern().matcher(str);
		assertFalse(str + " should not match with pattern", matcher.matches());
	}

	/**
	 * Test getters and constructors
	 */
	@Test
	public void testGettersContructor() {
		assertEquals(FAMILY, service.getFamily());
		assertEquals(FileUtils.TEST_DIR_ABS_PATH_SEP, service.getDirectory());
	}

	/**
	 * Test the pattern of the service
	 */
	@Test
	public void testPattern() {
		checkMatchOk("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE");
		checkMatchOk("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE/manifest.safe");
		checkMatchOk("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE/data");
		checkMatchOk("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE/support");
		checkMatchOk("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE/support/s1-aux-ins.xsd");
		checkMatchOk("S1B_OPER_MPL_ORBPRE_20171217T200330_20171224T200330_0001.EOF");
		checkMatchOk("S1B_OPER_AUX_RESORB_OPOD_20171218T190257_V20171218T144200_20171218T175930.EOF");
		checkMatchOk("S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml");
		checkMatchOk("S1B_TEST_AUX_OBMEMC_PDMC_20140212T000000.xml");
		checkMatchOk("S1B_AUX_PP1_V20160422T000000_G20160922T094703.SAFE");
		checkMatchOk("S1B_AUX_INS_V20160422T000000_G20160922T094114.SAFE");
		checkMatchOk("S1B_AUX_CAL_V20160422T000000_G20170116T134142.SAFE");
		checkMatchOk("S1B_AUX_CAL_V20160422T000000_G20170116T134142.SAFE/manifest.safe");
		checkMatchOk("S1B_AUX_CAL_V20160422T000000_G20170116T134142.SAFE/support");
		checkMatchOk("S1B_AUX_CAL_V20160422T000000_G20170116T134142.SAFE/support/s1-aux-ins.xsd");
		checkMatchOk("S1A_OPER_MPL_ORBPRE_20171215T200330_20171222T200330_0001.EOF");
		checkMatchOk("S1A_OPER_AUX_RESORB_OPOD_20171214T042134_V20171213T233734_20171214T025504.EOF");
		checkMatchOk("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		checkMatchOk("S1A_AUX_PP1_V20150519T120000_G20170328T093825.SAFE");
		checkMatchOk("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE");
		checkMatchOk("S1A_AUX_CAL_V20140908T000000_G20140909T130257.SAFE");
		checkMatchOk("S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF");

		checkMatchKo("S1B_AUX_CALL_V20160422T000000_G20170116T134142.SAFE/manifest.safe");
		checkMatchKo("SA_OPER_MPL_ORBPRE_20171215T200330_20171222T200330_0001.EOF");
		checkMatchKo("S1_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF");
		checkMatchKo("S1A_OPER_AUX_RES_OPOD_20171214T042134_V20171213T233734_20171214T025504.EOF");
		checkMatchKo("S1B_AUX_INS_V20160422T000000_G20160922T094114.SAF");
	}

	/**
	 * Test buildDescriptor when relative path does not match the pattern
	 */
	@Test
	public void testBuildDescriptorWhenNoMatch() throws FilePathException {
		thrown.expect(FilePathException.class);
		thrown.expect(hasProperty("productName", is(FileUtils.RPATH_AUX_NO_MATCH)));
		thrown.expect(hasProperty("path", is(FileUtils.RPATH_AUX_NO_MATCH)));
		thrown.expect(hasProperty("family", is(FAMILY)));
		service.buildDescriptor(FileUtils.RPATH_AUX_NO_MATCH);
	}

	/**
	 * Test buildDescriptor when relative path matches the pattern
	 */
	@Test
	public void testBuildDescriptor() throws FilePathException {

		assertEquals("Invalid file descriptor for RPATH_AUX_INS_MANIFEST",
				FileUtils.getFileDescriptorForAuxiliary(FileUtils.RPATH_AUX_INS_MANIFEST),
				service.buildDescriptor(FileUtils.RPATH_AUX_INS_MANIFEST));
		assertEquals("Invalid file descriptor for RPATH_AUX_INS_SUPPORT_XSD",
				FileUtils.getFileDescriptorForAuxiliary(FileUtils.RPATH_AUX_INS_SUPPORT_XSD),
				service.buildDescriptor(FileUtils.RPATH_AUX_INS_SUPPORT_XSD));
		assertEquals("Invalid file descriptor for RPATH_AUX_PP1_MANIFEST",
				FileUtils.getFileDescriptorForAuxiliary(FileUtils.RPATH_AUX_PP1_MANIFEST),
				service.buildDescriptor(FileUtils.RPATH_AUX_PP1_MANIFEST));
		assertEquals("Invalid file descriptor for RPATH_AUX_PP1_SUPPORT_XSD",
				FileUtils.getFileDescriptorForAuxiliary(FileUtils.RPATH_AUX_PP1_SUPPORT_XSD),
				service.buildDescriptor(FileUtils.RPATH_AUX_PP1_SUPPORT_XSD));
		assertEquals("Invalid file descriptor for RPATH_AUX_CAL_MANIFEST",
				FileUtils.getFileDescriptorForAuxiliary(FileUtils.RPATH_AUX_CAL_MANIFEST),
				service.buildDescriptor(FileUtils.RPATH_AUX_CAL_MANIFEST));
		assertEquals("Invalid file descriptor for RPATH_MPL_ORBPRE",
				FileUtils.getFileDescriptorForAuxiliary(FileUtils.RPATH_MPL_ORBPRE),
				service.buildDescriptor(FileUtils.RPATH_MPL_ORBPRE));
		assertEquals("Invalid file descriptor for RPATH_MPL_ORBSCT",
				FileUtils.getFileDescriptorForAuxiliary(FileUtils.RPATH_MPL_ORBSCT),
				service.buildDescriptor(FileUtils.RPATH_MPL_ORBSCT));
		assertEquals("Invalid file descriptor for RPATH_AUX_OBMEMC",
				FileUtils.getFileDescriptorForAuxiliary(FileUtils.RPATH_AUX_OBMEMC),
				service.buildDescriptor(FileUtils.RPATH_AUX_OBMEMC));
	}

}
