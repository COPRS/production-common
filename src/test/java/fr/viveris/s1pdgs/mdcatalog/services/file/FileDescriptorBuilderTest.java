package fr.viveris.s1pdgs.mdcatalog.services.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.junit.Test;

import fr.viveris.s1pdgs.mdcatalog.model.ConfigFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.EdrsSessionFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.EdrsSessionFileType;
import fr.viveris.s1pdgs.mdcatalog.model.FileExtension;
import fr.viveris.s1pdgs.mdcatalog.model.L0OutputFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.L1OutputFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.exception.AbstractCodedException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.FilePathException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.IgnoredFileException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.IllegalFileExtension;
import fr.viveris.s1pdgs.mdcatalog.services.files.FileDescriptorBuilder;

public class FileDescriptorBuilderTest {

	
	private FileDescriptorBuilder fileDescriptorBuilder;
	
	@Test
	public void testConfigFileDescriptorBuilder() {
		String expectedConfig = "localDirectory : "+ Paths.get("").toAbsolutePath()+"/test/workDir/" + ", pattern : ^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$";
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$", 
						Pattern.CASE_INSENSITIVE));
		assertEquals("File descriptor builder are not equals", expectedConfig, fileDescriptorBuilder.toString());
		
		expectedConfig = "localDirectory : "+ Paths.get("").toAbsolutePath()+"/test/workDir/" + ", pattern : ^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$";
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$", 
						Pattern.CASE_INSENSITIVE));
		assertEquals("File descriptor builder are not equals", expectedConfig, fileDescriptorBuilder.toString());
		
		expectedConfig = "localDirectory : "+ Paths.get("").toAbsolutePath()+"/test/workDir/" + ", pattern : ^([0-9a-z]{2})([0-9a-z]){1}_(S[1-6]|IW|EW|WM|N[1-6]|EN|Z[1-6]|ZE|ZI|ZW|RF|GP|HK)_(RAW)(_)_(0)(A|C|N|S|_)(SH|SV|HH|HV|VV|VH|DH|DV|__)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?";
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^([0-9a-z]{2})([0-9a-z]){1}_(S[1-6]|IW|EW|WM|N[1-6]|EN|Z[1-6]|ZE|ZI|ZW|RF|GP|HK)_(RAW)(_)_(0)(A|C|N|S|_)(SH|SV|HH|HV|VV|VH|DH|DV|__)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?", 
						Pattern.CASE_INSENSITIVE));
		assertEquals("File descriptor builder are not equals", expectedConfig, fileDescriptorBuilder.toString());
		
		expectedConfig = "localDirectory : "+ Paths.get("").toAbsolutePath()+"/test/workDir/" + ", pattern : ^(S1A|S1B|ASA)_(S[1-6]|IW|EW|WM|N[1-6]|EN|IM)_(SLC|GRD|OCN)(F|H|M|_)_(1|2)(A|S)(SH|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$";
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^(S1A|S1B|ASA)_(S[1-6]|IW|EW|WM|N[1-6]|EN|IM)_(SLC|GRD|OCN)(F|H|M|_)_(1|2)(A|S)(SH|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$", 
						Pattern.CASE_INSENSITIVE));
		assertEquals("File descriptor builder are not equals", expectedConfig, fileDescriptorBuilder.toString());
	}
	
	@Test
	public void testBuildConfigFileDescriptor() {
		ConfigFileDescriptor expectedResult = new ConfigFileDescriptor();
		expectedResult.setExtension(FileExtension.XML);
		expectedResult.setFilename("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		expectedResult.setKeyObjectStorage("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		expectedResult.setMissionId("S1");
		expectedResult.setSatelliteId("A");
		expectedResult.setProductClass("OPER");
		expectedResult.setProductName("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		expectedResult.setProductType("AUX_OBMEMC");
		expectedResult.setRelativePath("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		
		File file = new File("test/workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$", 
						Pattern.CASE_INSENSITIVE));
		try {
			ConfigFileDescriptor result = fileDescriptorBuilder.buildConfigFileDescriptor(file);
			
			assertNotNull("File descriptor should not be null", result);
			assertEquals("File descriptor are not equals", expectedResult.toString(), result.toString());
		} catch (AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testBuildComposedConfigFileDescriptor() {
		ConfigFileDescriptor expectedResult = new ConfigFileDescriptor();
		expectedResult.setExtension(FileExtension.SAFE);
		expectedResult.setFilename("manifest.safe");
		expectedResult.setKeyObjectStorage("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE");
		expectedResult.setMissionId("S1");
		expectedResult.setSatelliteId("A");
		expectedResult.setProductClass(null);
		expectedResult.setProductName("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE");
		expectedResult.setProductType("AUX_INS");
		expectedResult.setRelativePath("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE/manifest.safe");
		
		File file = new File("test/workDir/S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE/manifest.safe");
		
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$", 
						Pattern.CASE_INSENSITIVE));
		try {
			ConfigFileDescriptor result = fileDescriptorBuilder.buildConfigFileDescriptor(file);
			
			assertNotNull("File descriptor should not be null", result);
			assertEquals("File descriptor are not equals", expectedResult.toString(), result.toString());
		} catch (AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testBuildEdrsSessionFileDescriptor() {
		EdrsSessionFileDescriptor expectedResult = new EdrsSessionFileDescriptor();
		expectedResult.setExtension(FileExtension.RAW);
		expectedResult.setFilename("DCS_02_L20171109180334512000176_ch2_DSDB_00034.raw");
		expectedResult.setKeyObjectStorage("S1B/512000176/ch02/DCS_02_L20171109180334512000176_ch2_DSDB_00034.raw");
		expectedResult.setMissionId("S1");
		expectedResult.setSatelliteId("B");
		expectedResult.setProductName("DCS_02_L20171109180334512000176_ch2_DSDB_00034.raw");
		expectedResult.setProductType(EdrsSessionFileType.RAW);
		expectedResult.setRelativePath("S1B/512000176/ch02/DCS_02_L20171109180334512000176_ch2_DSDB_00034.raw");
		expectedResult.setChannel(2);
		expectedResult.setSessionIdentifier("512000176");

		File file = new File("test/workDir/S1B/512000176/ch02/DCS_02_L20171109180334512000176_ch2_DSDB_00034.raw");
		
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$", 
						Pattern.CASE_INSENSITIVE));
		try {
			EdrsSessionFileDescriptor result = fileDescriptorBuilder.buildEdrsSessionFileDescriptor(file);
			
			assertNotNull("File descriptor should not be null", result);
			assertEquals("File descriptor are not equals", expectedResult.toString(), result.toString());
		} catch (AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
		
		expectedResult = new EdrsSessionFileDescriptor();
		expectedResult.setExtension(FileExtension.XML);
		expectedResult.setFilename("DCS_02_SESSION1_ch1_DSIB.xml");
		expectedResult.setKeyObjectStorage("S1A/SESSION1/ch01/DCS_02_SESSION1_ch1_DSIB.xml");
		expectedResult.setMissionId("S1");
		expectedResult.setSatelliteId("A");
		expectedResult.setProductName("DCS_02_SESSION1_ch1_DSIB.xml");
		expectedResult.setProductType(EdrsSessionFileType.SESSION);
		expectedResult.setRelativePath("S1A/SESSION1/ch01/DCS_02_SESSION1_ch1_DSIB.xml");
		expectedResult.setChannel(1);
		expectedResult.setSessionIdentifier("SESSION1");

		file = new File("test/workDir/S1A/SESSION1/ch01/DCS_02_SESSION1_ch1_DSIB.xml");
		
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$", 
						Pattern.CASE_INSENSITIVE));
		try {
			EdrsSessionFileDescriptor result = fileDescriptorBuilder.buildEdrsSessionFileDescriptor(file);
			
			assertNotNull("File descriptor should not be null", result);
			assertEquals("File descriptor are not equals", expectedResult.toString(), result.toString());
		} catch (AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testBuildL0OutputFileDescriptor() {
		L0OutputFileDescriptor expectedResult = new L0OutputFileDescriptor();
		expectedResult.setExtension(FileExtension.SAFE);
		expectedResult.setFilename("manifest.safe");
		expectedResult.setKeyObjectStorage("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
		expectedResult.setMissionId("S1");
		expectedResult.setSatelliteId("A");
		expectedResult.setProductName("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
		expectedResult.setRelativePath("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe");
		expectedResult.setSwathtype("IW");
		expectedResult.setResolution("_");
		expectedResult.setProductClass("S");
		expectedResult.setProductType("IW_RAW__0S");
		expectedResult.setPolarisation("DV");
		expectedResult.setDataTakeId("021735");

		File file = new File("test/workDir/S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe");
		
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^([0-9a-z]{2})([0-9a-z]){1}_(S[1-6]|IW|EW|WM|N[1-6]|EN|Z[1-6]|ZE|ZI|ZW|RF|GP|HK)_(RAW)(_)_(0)(A|C|N|S|_)(SH|SV|HH|HV|VV|VH|DH|DV|__)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?", 
						Pattern.CASE_INSENSITIVE));
		try {
			L0OutputFileDescriptor result = fileDescriptorBuilder.buildL0OutputFileDescriptor(file);
			
			assertNotNull("File descriptor should not be null", result);
			assertEquals("File descriptor are not equals", expectedResult.toString(), result.toString());
		} catch (AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testBuildL1OutputFileDescriptor() {
		L1OutputFileDescriptor expectedResult = new L1OutputFileDescriptor();
		expectedResult.setExtension(FileExtension.SAFE);
		expectedResult.setFilename("manifest.safe");
		expectedResult.setKeyObjectStorage("S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE");
		expectedResult.setMissionId("S1A");
		expectedResult.setSatelliteId(null);
		expectedResult.setProductName("S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE");
		expectedResult.setRelativePath("S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE/manifest.safe");
		expectedResult.setSwathtype("IW");
		expectedResult.setResolution("H");
		expectedResult.setProductClass("S");
		expectedResult.setProductType("IW_GRDH_1S");
		expectedResult.setPolarisation("DV");
		expectedResult.setDataTakeId("023A69");

		File file = new File("test/workDir/S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE/manifest.safe");
		
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^(S1A|S1B|ASA)_(S[1-6]|IW|EW|WM|N[1-6]|EN|IM)_(SLC|GRD|OCN)(F|H|M|_)_(1|2)(A|S)(SH|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$", 
						Pattern.CASE_INSENSITIVE));
		try {
			L1OutputFileDescriptor result = fileDescriptorBuilder.buildL1OutputFileDescriptor(file);
			
			assertNotNull("File descriptor should not be null", result);
			assertEquals("File descriptor are not equals", expectedResult.toString(), result.toString());
		} catch (AbstractCodedException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}
	
	@Test
	public void testBuildFileDescriptorPatternFail() {
		//Auxiliary file
		File file = new File("test/workDir/S1A_OPER_OUX_OBMEMC_PDMC_20140201T000000.xml");
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$", 
						Pattern.CASE_INSENSITIVE));
		try {
			fileDescriptorBuilder.buildConfigFileDescriptor(file);
			fail("An exception should occur");
		} catch (AbstractCodedException fe) {
			assertEquals("Raised exception shall concern S1A_OPER_OUX_OBMEMC_PDMC_20140201T000000.xml",
					"S1A_OPER_OUX_OBMEMC_PDMC_20140201T000000.xml", fe.getProductName());
		}
		//Edrs Session file
		file = new File("test/workDir/S1A/SESSION1/ch03/DCS_02_SESSION1_ch1_DSIB.xml");
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$", 
						Pattern.CASE_INSENSITIVE));
		try {
			fileDescriptorBuilder.buildEdrsSessionFileDescriptor(file);
			fail("An exception should occur");
		} catch (AbstractCodedException fe) {
			assertEquals("Raised exception shall concern S1A/SESSION1/ch03/DCS_02_SESSION1_ch1_DSIB.xml",
					"S1A/SESSION1/ch03/DCS_02_SESSION1_ch1_DSIB.xml", fe.getProductName());
		}
		//L0
		file = new File("test/workDir/S1A_IR_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe");
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^([0-9a-z]{2})([0-9a-z]){1}_(S[1-6]|IW|EW|WM|N[1-6]|EN|Z[1-6]|ZE|ZI|ZW|RF|GP|HK)_(RAW)(_)_(0)(A|C|N|S|_)(SH|SV|HH|HV|VV|VH|DH|DV|__)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?", 
						Pattern.CASE_INSENSITIVE));
		try {
			fileDescriptorBuilder.buildL0OutputFileDescriptor(file);
			fail("An exception should occur");
		} catch (AbstractCodedException fe) {
			assertEquals("Raised exception shall concern S1A_IR_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe",
					"S1A_IR_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe", fe.getProductName());
		}		
		//L1
		file = new File("test/workDir/S1A_IW_GRDH_1ZDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE/manifest.safe");
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^(S1A|S1B|ASA)_(S[1-6]|IW|EW|WM|N[1-6]|EN|IM)_(SLC|GRD|OCN)(F|H|M|_)_(1|2)(A|S)(SH|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$", 
						Pattern.CASE_INSENSITIVE));
		try {
			fileDescriptorBuilder.buildL1OutputFileDescriptor(file);
			fail("An exception should occur");
		} catch (AbstractCodedException fe) {
			assertEquals("Raised exception shall concern S1A_IW_GRDH_1ZDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE/manifest.safe",
					"S1A_IW_GRDH_1ZDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE/manifest.safe", fe.getProductName());
		}
	}
	
	@Test
	public void testBuildFileDescriptorFileNameFail() {
		//Auxiliary file
		File file = new File("/S1A_OPER_AUX.xml");
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$", 
						Pattern.CASE_INSENSITIVE));
		try {
			fileDescriptorBuilder.buildConfigFileDescriptor(file);
			fail("An exception should occur");
		} catch (AbstractCodedException fe) {
			assertEquals("Raised exception shall concern S1A_OPER_AUX.xml",
					"S1A_OPER_AUX.xml", fe.getProductName());
		}
		//Edrs Session file
		file = new File("/S1A/SESSION1");
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$", 
						Pattern.CASE_INSENSITIVE));
		try {
			fileDescriptorBuilder.buildEdrsSessionFileDescriptor(file);
			fail("An exception should occur");
		} catch (AbstractCodedException fe) {
			assertEquals("Raised exception shall concern S1A/SESSION1",
					"SESSION1", fe.getProductName());
		}
		//L0
		file = new File("/S1A_IW_RAW__0SDV");
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^([0-9a-z]{2})([0-9a-z]){1}_(S[1-6]|IW|EW|WM|N[1-6]|EN|Z[1-6]|ZE|ZI|ZW|RF|GP|HK)_(RAW)(_)_(0)(A|C|N|S|_)(SH|SV|HH|HV|VV|VH|DH|DV|__)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?", 
						Pattern.CASE_INSENSITIVE));
		try {
			fileDescriptorBuilder.buildL0OutputFileDescriptor(file);
			fail("An exception should occur");
		} catch (AbstractCodedException fe) {
			assertEquals("Raised exception shall concern S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe",
					"S1A_IW_RAW__0SDV", fe.getProductName());
		}		
		//L1
		file = new File("/S1A_IW_GRDH_1SDV");
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^(S1A|S1B|ASA)_(S[1-6]|IW|EW|WM|N[1-6]|EN|IM)_(SLC|GRD|OCN)(F|H|M|_)_(1|2)(A|S)(SH|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$", 
						Pattern.CASE_INSENSITIVE));
		try {
			fileDescriptorBuilder.buildL1OutputFileDescriptor(file);
			fail("An exception should occur");
		} catch (AbstractCodedException fe) {
			assertEquals("Raised exception shall concern S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE/manifest.safe",
					"S1A_IW_GRDH_1SDV", fe.getProductName());
		}
	}
	
	@Test
	public void testBuildFileDescriptorDirectoryFail() {
		//Auxiliary file
		File file = new File("test/workDir/S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE");
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$", 
						Pattern.CASE_INSENSITIVE));
		try {
			fileDescriptorBuilder.buildConfigFileDescriptor(file);
			fail("An exception should occur " + file.getName() + " shall be a directory : " + file.isDirectory());
		} catch (AbstractCodedException fe) {
			assertEquals("Raised exception shall concern S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE",
					"S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE", fe.getProductName());
		}
		//Edrs Session file
		file = new File("test/workDir/S1A/SESSION1/ch01");
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$", 
						Pattern.CASE_INSENSITIVE));
		try {
			fileDescriptorBuilder.buildEdrsSessionFileDescriptor(file);
			fail("An exception should occur " + file.getName() + " shall be a directory : " + file.isDirectory());
		} catch (AbstractCodedException fe) {
			assertEquals("Raised exception shall concern S1A/SESSION1/ch01",
					"S1A/SESSION1/ch01", fe.getProductName());
		}
		//L0
		file = new File("test/workDir/S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^([0-9a-z]{2})([0-9a-z]){1}_(S[1-6]|IW|EW|WM|N[1-6]|EN|Z[1-6]|ZE|ZI|ZW|RF|GP|HK)_(RAW)(_)_(0)(A|C|N|S|_)(SH|SV|HH|HV|VV|VH|DH|DV|__)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?", 
						Pattern.CASE_INSENSITIVE));
		try {
			fileDescriptorBuilder.buildL0OutputFileDescriptor(file);
			fail("An exception should occur " + file.getName() + " shall be a directory : " + file.isDirectory());
		} catch (AbstractCodedException fe) {
			assertEquals("Raised exception shall concern S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
					"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE", fe.getProductName());
		}		
		//L1
		file = new File("test/workDir/S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE");
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^(S1A|S1B|ASA)_(S[1-6]|IW|EW|WM|N[1-6]|EN|IM)_(SLC|GRD|OCN)(F|H|M|_)_(1|2)(A|S)(SH|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$", 
						Pattern.CASE_INSENSITIVE));
		try {
			fileDescriptorBuilder.buildL1OutputFileDescriptor(file);
			fail("An exception should occur " + file.getName() + " shall be a directory : " + file.isDirectory());
		} catch (AbstractCodedException fe) {
			assertEquals("Raised exception shall concern S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE",
					"S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE", fe.getProductName());
		}
	}
	
	@Test(expected = AbstractCodedException.class)
	public void testBuildEdrsSessionFileDescriptorExtensionFail() throws FilePathException, IgnoredFileException, IllegalFileExtension {
		//Edrs Session file
		File file = new File("test/workDir/S1A/SESSION1/ch01/DCS_02_SESSION1_ch1_DSIB.xmll");
		fileDescriptorBuilder = new FileDescriptorBuilder(Paths.get("").toAbsolutePath()+"/test/workDir/", 
				Pattern.compile("^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW|XMLL))$", 
						Pattern.CASE_INSENSITIVE));
		
		fileDescriptorBuilder.buildEdrsSessionFileDescriptor(file);
	}

}
