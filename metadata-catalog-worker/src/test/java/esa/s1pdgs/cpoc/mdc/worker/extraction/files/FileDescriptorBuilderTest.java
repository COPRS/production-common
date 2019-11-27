package esa.s1pdgs.cpoc.mdc.worker.extraction.files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.regex.Pattern;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.FileExtension;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataFilePathException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataIgnoredFileException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataIllegalFileExtension;
import esa.s1pdgs.cpoc.mdc.worker.Utils;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.ConfigFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;

public class FileDescriptorBuilderTest {
	private static final String PATTERN = "^(S1|AS)(A|B)_(S[1-6]|IW|EW|WV|GP|HK|N[1-6]|EN|IM)_(SLC|GRD|OCN|RAW)(F|H|M|_)_(0|1|2)(A|C|N|S|_)(SH|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$";
    
	private FileDescriptorBuilder fileDescriptorBuilder;
    
    private final File testDir = new File("src/test/resources/workDir");

    @Test
    public void testBuildConfigFileDescriptor() {
        final ConfigFileDescriptor expectedResult = new ConfigFileDescriptor();
        expectedResult.setExtension(FileExtension.XML);
        expectedResult
                .setFilename("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        expectedResult.setKeyObjectStorage(
                "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        expectedResult.setMissionId("S1");
        expectedResult.setSatelliteId("A");
        expectedResult.setProductClass("OPER");
        expectedResult
                .setProductName("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        expectedResult.setProductType("AUX_OBMEMC");
        expectedResult.setRelativePath(
                "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        expectedResult.setProductFamily(ProductFamily.AUXILIARY_FILE);

        final File file = new File(testDir,"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");

        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(
                        "^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$",
                        Pattern.CASE_INSENSITIVE));
        try {
            final ConfigFileDescriptor result = fileDescriptorBuilder.buildConfigFileDescriptor(file);

            assertNotNull("File descriptor should not be null", result);
            assertEquals("File descriptor are not equals",
                    expectedResult.toString(), result.toString());
        } catch (final AbstractCodedException fe) {
            fe.printStackTrace();
            fail("Exception occurred: " + fe.getMessage());   
        }
    }

    @Test
    public void testBuildComposedConfigFileDescriptor() {
        final ConfigFileDescriptor expectedResult = new ConfigFileDescriptor();
        expectedResult.setExtension(FileExtension.SAFE);
        expectedResult.setFilename("manifest.safe");
        expectedResult.setKeyObjectStorage(
                "S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE");
        expectedResult.setMissionId("S1");
        expectedResult.setSatelliteId("A");
        expectedResult.setProductClass(null);
        expectedResult.setProductName(
                "S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE");
        expectedResult.setProductType("AUX_INS");
        expectedResult.setRelativePath(
                "S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE/manifest.safe");
        expectedResult.setProductFamily(ProductFamily.AUXILIARY_FILE);

        final File file = new File(testDir,"S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE/manifest.safe");

        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(
                        "^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$",
                        Pattern.CASE_INSENSITIVE));
        try {
            final ConfigFileDescriptor result =
                    fileDescriptorBuilder.buildConfigFileDescriptor(file);

            assertNotNull("File descriptor should not be null", result);
            assertEquals("File descriptor are not equals",
                    expectedResult.toString(), result.toString());
        } catch (final AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }
    }

    @Test
    public void testBuildEdrsSessionFileDescriptor() {
        EdrsSessionFileDescriptor expectedResult =
                new EdrsSessionFileDescriptor();
        expectedResult.setExtension(FileExtension.RAW);
        expectedResult.setFilename(
                "DCS_02_L20171109180334512000176_ch2_DSDB_00034.raw");
        expectedResult.setKeyObjectStorage(
                "512000176/ch02/DCS_02_L20171109180334512000176_ch2_DSDB_00034.raw");
        expectedResult.setProductName(
                "DCS_02_L20171109180334512000176_ch2_DSDB_00034.raw");
        expectedResult.setEdrsSessionFileType(EdrsSessionFileType.RAW);
        expectedResult.setRelativePath(
                "512000176/ch02/DCS_02_L20171109180334512000176_ch2_DSDB_00034.raw");
        expectedResult.setChannel(2);
        expectedResult.setSessionIdentifier("512000176");

        File file = new File(testDir,
                "512000176/ch02/DCS_02_L20171109180334512000176_ch2_DSDB_00034.raw");

        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(
                        "^(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$",
                        Pattern.CASE_INSENSITIVE));
        try {
            final EdrsSessionFileDescriptor result =
                    fileDescriptorBuilder.buildEdrsSessionFileDescriptor(file);

            assertNotNull("File descriptor should not be null", result);
            assertEquals("File descriptor are not equals",
                    expectedResult.toString(), result.toString());
        } catch (final AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }

        expectedResult = new EdrsSessionFileDescriptor();
        expectedResult.setExtension(FileExtension.XML);
        expectedResult.setFilename("DCS_93_S1B__SGS__________017076_ch1_DSIB.xml");
        expectedResult.setKeyObjectStorage(
                "L20180724144436762001030/ch01/DCS_93_S1B__SGS__________017076_ch1_DSIB.xml");
    

        expectedResult.setProductName("DCS_93_S1B__SGS__________017076_ch1_DSIB.xml");
        expectedResult.setEdrsSessionFileType(EdrsSessionFileType.SESSION);
        expectedResult.setRelativePath(
                "L20180724144436762001030/ch01/DCS_93_S1B__SGS__________017076_ch1_DSIB.xml");
        expectedResult.setChannel(1);
        expectedResult.setSessionIdentifier("L20180724144436762001030");

        file = new File(testDir,"L20180724144436762001030/ch01/DCS_93_S1B__SGS__________017076_ch1_DSIB.xml");
    
        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(
                        "^(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$",
                        Pattern.CASE_INSENSITIVE));
        try {
            final EdrsSessionFileDescriptor result =
                    fileDescriptorBuilder.buildEdrsSessionFileDescriptor(file);

            assertNotNull("File descriptor should not be null", result);
            assertEquals("File descriptor are not equals",
                    expectedResult.toString(), result.toString());
        } catch (final AbstractCodedException fe) {
        	fe.printStackTrace();
            fail("Exception occurred: " + fe.getMessage());
        }
    }


    
    @Test
    public void testBuildL0OutputFileDescriptor() {
        final CatalogJob dto = Utils.newCatalogJob(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                ProductFamily.L0_SLICE, "FAST");
        final OutputFileDescriptor expectedResult = new OutputFileDescriptor();
        expectedResult.setExtension(FileExtension.SAFE);
        expectedResult.setFilename("manifest.safe");
        expectedResult.setKeyObjectStorage(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        expectedResult.setMissionId("S1");
        expectedResult.setSatelliteId("A");
        expectedResult.setProductName(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        expectedResult.setRelativePath(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe");
        expectedResult.setSwathtype("IW");
        expectedResult.setResolution("_");
        expectedResult.setProductClass("S");
        expectedResult.setProductType("IW_RAW__0S");
        expectedResult.setPolarisation("DV");
        expectedResult.setDataTakeId("021735");
        expectedResult.setMode("FAST");

        final File file = new File(testDir,
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe");

        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(PATTERN,
                        Pattern.CASE_INSENSITIVE));
        try {
            final OutputFileDescriptor result =
                    fileDescriptorBuilder.buildOutputFileDescriptor(file, dto, ProductFamily.L0_SLICE);

            assertNotNull("File descriptor should not be null", result);
            assertEquals("File descriptor are not equals",
                    expectedResult.toString(), result.toString());
        } catch (final AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }
    }

    @Test
    public void testBuildL0SegmentFileDescriptor() {
        final CatalogJob dto = Utils.newCatalogJob(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                ProductFamily.L0_SEGMENT, "FAST");
        final OutputFileDescriptor expectedResult = new OutputFileDescriptor();
        expectedResult.setExtension(FileExtension.SAFE);
        expectedResult.setFilename("manifest.safe");
        expectedResult.setKeyObjectStorage(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        expectedResult.setMissionId("S1");
        expectedResult.setSatelliteId("A");
        expectedResult.setProductName(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        expectedResult.setRelativePath(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe");
        expectedResult.setSwathtype("IW");
        expectedResult.setResolution("_");
        expectedResult.setProductClass("S");
        expectedResult.setProductType("IW_RAW__0S");
        expectedResult.setPolarisation("DV");
        expectedResult.setDataTakeId("021735");
        expectedResult.setMode("FAST");

        final File file = new File(testDir,
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe");

        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(PATTERN,
                        Pattern.CASE_INSENSITIVE));
        try {
            final OutputFileDescriptor result =
                    fileDescriptorBuilder.buildOutputFileDescriptor(file, dto, ProductFamily.L0_SEGMENT);

            assertNotNull("File descriptor should not be null", result);
            assertEquals("File descriptor are not equals",
                    expectedResult.toString(), result.toString());
        } catch (final AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }
    }

    @Test
    public void testBuildL1OutputFileDescriptor() {
        final CatalogJob dto = Utils.newCatalogJob(
                "S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE",
                "S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE",
                ProductFamily.L1_SLICE, "NRT");
        
        final OutputFileDescriptor expectedResult = new OutputFileDescriptor();
        expectedResult.setExtension(FileExtension.SAFE);
        expectedResult.setFilename("manifest.safe");
        expectedResult.setKeyObjectStorage(
                "S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE");
        expectedResult.setMissionId("S1");
        expectedResult.setSatelliteId("A");
        expectedResult.setProductName(
                "S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE");
        expectedResult.setRelativePath(
                "S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE/manifest.safe");
        expectedResult.setSwathtype("IW");
        expectedResult.setResolution("H");
        expectedResult.setProductClass("S");
        expectedResult.setProductType("IW_GRDH_1S");
        expectedResult.setPolarisation("DV");
        expectedResult.setDataTakeId("023A69");
        expectedResult.setMode("NRT");

        final File file = new File(testDir,
                "S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE/manifest.safe");

        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(PATTERN,
                        Pattern.CASE_INSENSITIVE));
        try {
            final OutputFileDescriptor result =
                    fileDescriptorBuilder.buildOutputFileDescriptor(file, dto, ProductFamily.L1_SLICE);

            assertNotNull("File descriptor should not be null", result);
            assertEquals("File descriptor are not equals",
                    expectedResult.toString(), result.toString());
        } catch (final AbstractCodedException fe) {
            fail("Exception occurred: " + fe.getMessage());
        }
    }

    @Test
    public void testBuildFileDescriptorPatternFail() {
        // Auxiliary file
        File file = new File(testDir,"S1A_OPER_OUX_OBMEMC_PDMC_20140201T000000.xml");
        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(
                        "^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$",
                        Pattern.CASE_INSENSITIVE));
        try {
            fileDescriptorBuilder.buildConfigFileDescriptor(file);
            fail("An exception should occur");
        } catch (final AbstractCodedException fe) {
            assertEquals(
                    "Raised exception shall concern S1A_OPER_OUX_OBMEMC_PDMC_20140201T000000.xml",
                    AbstractCodedException.ErrorCode.METADATA_FILE_PATH,
                    fe.getCode());
        }
        // Edrs Session file
        file = new File(testDir,"S1A/SESSION1/ch03/DCS_02_SESSION1_ch1_DSIB.xml");
        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(
                        "^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$",
                        Pattern.CASE_INSENSITIVE));
        try {
            fileDescriptorBuilder.buildEdrsSessionFileDescriptor(file);
            fail("An exception should occur");
        } catch (final AbstractCodedException fe) {
            assertEquals(
                    "Raised exception shall concern S1A/SESSION1/ch03/DCS_02_SESSION1_ch1_DSIB.xml",
                    AbstractCodedException.ErrorCode.METADATA_FILE_PATH,
                    fe.getCode());
        }
        // L0
        final CatalogJob dto = Utils.newCatalogJob(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                ProductFamily.L0_SLICE, "FAST");
        file = new File(testDir,"S1A_IR_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe");
        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(PATTERN,
                        Pattern.CASE_INSENSITIVE));
        try {
            fileDescriptorBuilder.buildOutputFileDescriptor(file, dto, ProductFamily.L0_SLICE);
            fail("An exception should occur");
        } catch (final AbstractCodedException fe) {
            assertEquals(
                    "Raised exception shall concern S1A_IR_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe",
                    AbstractCodedException.ErrorCode.METADATA_FILE_PATH,
                    fe.getCode());
        }
        // L1
        final CatalogJob dto1 = Utils.newCatalogJob(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                ProductFamily.L1_SLICE, "FAST");
        file = new File(testDir,
                "S1A_IW_GRDH_1ZDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE/manifest.safe");
        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(PATTERN,
                        Pattern.CASE_INSENSITIVE));
        try {
            fileDescriptorBuilder.buildOutputFileDescriptor(file, dto1, ProductFamily.L1_SLICE);
            fail("An exception should occur");
        } catch (final AbstractCodedException fe) {
            assertEquals(
                    "Raised exception shall concern S1A_IW_GRDH_1ZDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE/manifest.safe",
                    AbstractCodedException.ErrorCode.METADATA_FILE_PATH,
                    fe.getCode());
        }
    }

    @Test
    public void testBuildFileDescriptorFileNameFail() {
        // Auxiliary file
        File file = new File("/S1A_OPER_AUX.xml");
        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(
                        "^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$",
                        Pattern.CASE_INSENSITIVE));
        try {
            fileDescriptorBuilder.buildConfigFileDescriptor(file);
            fail("An exception should occur");
        } catch (final AbstractCodedException fe) {
            assertEquals("Raised exception shall concern S1A_OPER_AUX.xml",
                    AbstractCodedException.ErrorCode.METADATA_FILE_PATH,
                    fe.getCode());
        }
        // Edrs Session file
        file = new File("/S1A/SESSION1");
        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(
                        "^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$",
                        Pattern.CASE_INSENSITIVE));
        try {
            fileDescriptorBuilder.buildEdrsSessionFileDescriptor(file);
            fail("An exception should occur");
        } catch (final AbstractCodedException fe) {
            assertEquals("Raised exception shall concern S1A/SESSION1",
                    AbstractCodedException.ErrorCode.METADATA_FILE_PATH,
                    fe.getCode());
        }
        // L0
        final CatalogJob dto = Utils.newCatalogJob(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                ProductFamily.L0_SLICE, "FAST");
        file = new File("/S1A_IW_RAW__0SDV");
        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(
                        "^([0-9a-z]{2})([0-9a-z]){1}_(S[1-6]|IW|EW|WM|N[1-6]|EN|Z[1-6]|ZE|ZI|ZW|RF|GP|HK)_(RAW)(_)_(0)(A|C|N|S|_)(SH|SV|HH|HV|VV|VH|DH|DV|__)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?",
                        Pattern.CASE_INSENSITIVE));
        try {
            fileDescriptorBuilder.buildOutputFileDescriptor(file, dto, ProductFamily.L0_SLICE);
            fail("An exception should occur");
        } catch (final AbstractCodedException fe) {
            assertEquals(
                    "Raised exception shall concern S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe",
                    AbstractCodedException.ErrorCode.METADATA_FILE_PATH,
                    fe.getCode());
        }
        // L1
        final CatalogJob dto1 = Utils.newCatalogJob(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                ProductFamily.L0_SLICE, "FAST");
        file = new File("/S1A_IW_GRDH_1SDV");
        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(
                        "^(S1A|S1B|ASA)_(S[1-6]|IW|EW|WM|N[1-6]|EN|IM)_(SLC|GRD|OCN)(F|H|M|_)_(1|2)(A|S)(SH|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$",
                        Pattern.CASE_INSENSITIVE));
        try {
            fileDescriptorBuilder.buildOutputFileDescriptor(file, dto1, ProductFamily.L1_SLICE);
            fail("An exception should occur");
        } catch (final AbstractCodedException fe) {
            assertEquals(
                    "Raised exception shall concern S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE/manifest.safe",
                    AbstractCodedException.ErrorCode.METADATA_FILE_PATH,
                    fe.getCode());
        }
    }

    @Test
    public void testBuildFileDescriptorDirectoryFail() {
        // Auxiliary file
        File file = new File(testDir,"S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE");
        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(
                        "^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$",
                        Pattern.CASE_INSENSITIVE));
        try {
            fileDescriptorBuilder.buildConfigFileDescriptor(file);
            fail("An exception should occur " + file.getName()
                    + " shall be a directory : " + file.isDirectory());
        } catch (final AbstractCodedException fe) {
            assertEquals(
                    "Raised exception shall concern S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE",
                    AbstractCodedException.ErrorCode.METADATA_IGNORE_FILE,
                    fe.getCode());
        }
        // Edrs Session file
        file = new File(testDir,"S1A/SESSION1/ch01");
        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(
                        "^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$",
                        Pattern.CASE_INSENSITIVE));
        try {
            fileDescriptorBuilder.buildEdrsSessionFileDescriptor(file);
            fail("An exception should occur " + file.getName()
                    + " shall be a directory : " + file.isDirectory());
        } catch (final AbstractCodedException fe) {
            assertEquals("Raised exception shall concern S1A/SESSION1/ch01",
                    AbstractCodedException.ErrorCode.METADATA_IGNORE_FILE,
                    fe.getCode());
        }
        // L0
        final CatalogJob dto = Utils.newCatalogJob(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                ProductFamily.L0_SLICE, "FAST");
        file = new File(testDir,"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(PATTERN,Pattern.CASE_INSENSITIVE));
        try {
            fileDescriptorBuilder.buildOutputFileDescriptor(file, dto, ProductFamily.L0_SLICE);
            fail("An exception should occur " + file.getName()
                    + " shall be a directory : " + file.isDirectory());
        } catch (final AbstractCodedException fe) {
            assertEquals(
                    "Raised exception shall concern S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                    AbstractCodedException.ErrorCode.METADATA_IGNORE_FILE,
                    fe.getCode());
        }
        // L1
        final CatalogJob dto1 = Utils.newCatalogJob(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                ProductFamily.L0_SLICE, "FAST");
        file = new File(testDir, "S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE");
        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(PATTERN,
                        Pattern.CASE_INSENSITIVE));
        try {
            fileDescriptorBuilder.buildOutputFileDescriptor(file, dto1, ProductFamily.L1_SLICE);
            fail("An exception should occur " + file.getName()
                    + " shall be a directory : " + file.isDirectory());
        } catch (final AbstractCodedException fe) {
            assertEquals(
                    "Raised exception shall concern S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE",
                    AbstractCodedException.ErrorCode.METADATA_IGNORE_FILE,
                    fe.getCode());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildEdrsSessionFileDescriptorExtensionFail()
            throws MetadataFilePathException, MetadataIgnoredFileException,
            MetadataIllegalFileExtension {
        // Edrs Session file
        final File file = new File(testDir,"S1A/SESSION1/ch01/DCS_02_SESSION1_ch1_DSIB.xmll");
        fileDescriptorBuilder = new FileDescriptorBuilder(
        		testDir,
                Pattern.compile(
                        "^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW|XMLL))$",
                        Pattern.CASE_INSENSITIVE));

        fileDescriptorBuilder.buildEdrsSessionFileDescriptor(file);
    }

}
