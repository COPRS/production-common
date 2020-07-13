package esa.s1pdgs.cpoc.mdc.worker.extraction.files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.FileExtension;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.mdc.worker.Utils;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.AuxDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.S3FileDescriptor;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;

public class TestFileDescriptorBuilder {
	private static final String PATTERN_EDRS = "^(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$";
	private static final String PATTERN_AUX = "^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$";
	private static final String PATTERN_PROD = "^(S1|AS)(A|B)_(S[1-6]|IW|EW|WV|GP|HK|N[1-6]|EN|IM)_(SLC|GRD|OCN|RAW)(F|H|M|_)_(0|1|2)(A|C|N|S|_)(SH|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$";
	
	private static final String PATTERN_S3 = "^([a-zA-Z0-9][a-zA-Z0-9])(\\w{1})_((OL|SL|SR|DO|MW|GN|SY|TM|AX)_(0|1|2|_)_\\w{6})_(\\d{8}T\\d{6})_(\\d{8}T\\d{6})_(\\d{8}T\\d{6})_(\\w{17})_(\\w{3})_(\\w{8})\\.(\\w{1,4})\\/?(.+)?$";
    private final File inputDir = new File("src/test/resources/workDir/");
	private final File testDir = FileUtils.createTmpDir();
	
	@Before
	public void setUp() throws Exception {
        Utils.copyFolder(inputDir.toPath(), testDir.toPath());
	}
	
	@After
	public void tearDown() {
		FileUtils.delete(testDir.getPath());
	}
	
    @Test
    public void testBuildAuxDescriptor() throws Exception {
        final AuxDescriptor expected = new AuxDescriptor();
        expected.setExtension(FileExtension.XML);
        expected.setFilename("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        expected.setKeyObjectStorage("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        expected.setMissionId("S1");
        expected.setSatelliteId("A");
        expected.setProductClass("OPER");
        expected.setProductName("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        expected.setProductType("AUX_OBMEMC");
        expected.setRelativePath("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        expected.setProductFamily(ProductFamily.AUXILIARY_FILE);

        final File file = new File(testDir, "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        final FileDescriptorBuilder uut = newDescriptorForPattern(PATTERN_AUX);
        final AuxDescriptor result = uut.buildAuxDescriptor(file);
        assertNotNull("File descriptor should not be null", result);
        assertEquals("File descriptor are not equals", expected, result);
    }

    @Test
    public void testBuildComposedConfigFileDescriptor() throws Exception {
        final AuxDescriptor expected = new AuxDescriptor();
        expected.setExtension(FileExtension.SAFE);
        expected.setFilename("manifest.safe");
        expected.setKeyObjectStorage("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE");
        expected.setMissionId("S1");
        expected.setSatelliteId("A");
        expected.setProductClass(null);
        expected.setProductName("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE");
        expected.setProductType("AUX_INS");
        expected.setRelativePath("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE/manifest.safe");
        expected.setProductFamily(ProductFamily.AUXILIARY_FILE);

        final File file = new File(testDir,"S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE/manifest.safe");
        final FileDescriptorBuilder uut = newDescriptorForPattern(PATTERN_AUX);
        final AuxDescriptor result = uut.buildAuxDescriptor(file);
        assertNotNull("File descriptor should not be null", result);
        assertEquals("File descriptor are not equals", expected, result);  
    }
    
    @Test
    public void testBuildEdrsSessionFileDescriptorRaw() throws Exception {
        final EdrsSessionFileDescriptor expected = new EdrsSessionFileDescriptor();
        expected.setExtension(FileExtension.RAW);
        expected.setFilename("DCS_02_L20171109180334512000176_ch2_DSDB_00034.raw");
        expected.setKeyObjectStorage("512000176/ch02/DCS_02_L20171109180334512000176_ch2_DSDB_00034.raw");
        expected.setProductName("DCS_02_L20171109180334512000176_ch2_DSDB_00034.raw");
        expected.setEdrsSessionFileType(EdrsSessionFileType.RAW);
        expected.setRelativePath("512000176/ch02/DCS_02_L20171109180334512000176_ch2_DSDB_00034.raw");
        expected.setChannel(2);
        expected.setSessionIdentifier("512000176");
        expected.setStationCode("WILE");
        expected.setSatelliteId("B");
        expected.setMissionId("S1");
        expected.setProductFamily(ProductFamily.EDRS_SESSION);

        final Map<String,String> fromPath = new HashMap<>();
        fromPath.put("missionId","S1");
        fromPath.put("satelliteId","B");
        fromPath.put("sessionId","512000176");
        fromPath.put("stationCode","WILE");
        fromPath.put("channelId","2");
        
        final CatalogJob job = new CatalogJob();
        job.setProductName("512000176");
        job.setKeyObjectStorage("512000176/ch02/DCS_02_L20171109180334512000176_ch2_DSDB_00034.raw");
        job.setRelativePath("512000176/ch02/DCS_02_L20171109180334512000176_ch2_DSDB_00034.raw");
        job.setProductFamily(ProductFamily.EDRS_SESSION);
        
        final FileDescriptorBuilder uut = newDescriptorForPattern(PATTERN_EDRS);
        final EdrsSessionFileDescriptor result = uut.buildEdrsSessionFileDescriptor(
        		new File(testDir, "512000176/ch02/DCS_02_L20171109180334512000176_ch2_DSDB_00034.raw"),
        		fromPath,
        		job
        );
        assertNotNull("File descriptor should not be null", result);
        assertEquals("File descriptor are not equals", expected, result);
    }
    
    @Test
    public void testBuildEdrsSessionFileDescriptorSession() throws Exception {
        final EdrsSessionFileDescriptor expected = new EdrsSessionFileDescriptor();
        expected.setExtension(FileExtension.XML);
        expected.setFilename("DCS_93_S1B__SGS__________017076_ch1_DSIB.xml");
        expected.setKeyObjectStorage("L20180724144436762001030/ch01/DCS_93_S1B__SGS__________017076_ch1_DSIB.xml"); 
        expected.setProductName("DCS_93_S1B__SGS__________017076_ch1_DSIB.xml");
        expected.setEdrsSessionFileType(EdrsSessionFileType.SESSION);
        expected.setRelativePath("L20180724144436762001030/ch01/DCS_93_S1B__SGS__________017076_ch1_DSIB.xml");
        expected.setChannel(1);
        expected.setSessionIdentifier("L20180724144436762001030");
        expected.setStationCode("WILE");
        expected.setSatelliteId("B");
        expected.setMissionId("S1");
        expected.setProductFamily(ProductFamily.EDRS_SESSION);
        
        final Map<String,String> fromPath = new HashMap<>();
        fromPath.put("missionId","S1");
        fromPath.put("satelliteId","B");
        fromPath.put("sessionId","L20180724144436762001030");
        fromPath.put("stationCode","WILE");
        fromPath.put("channelId","1");
        
        final CatalogJob job = new CatalogJob();
        job.setProductName("L20180724144436762001030");
        job.setKeyObjectStorage("L20180724144436762001030/ch01/DCS_93_S1B__SGS__________017076_ch1_DSIB.xml");
        job.setRelativePath("L20180724144436762001030/ch01/DCS_93_S1B__SGS__________017076_ch1_DSIB.xml");
        job.setProductFamily(ProductFamily.EDRS_SESSION);
  
        final FileDescriptorBuilder uut = newDescriptorForPattern(PATTERN_EDRS);
        final EdrsSessionFileDescriptor result = uut.buildEdrsSessionFileDescriptor(
        		new File(testDir, "L20180724144436762001030/ch01/DCS_93_S1B__SGS__________017076_ch1_DSIB.xml"),
        		fromPath,
        		job
        );
        assertNotNull("File descriptor should not be null", result);
        assertEquals("File descriptor are not equals", expected, result);
    }

    @Test
    public void testBuildL0OutputFileDescriptor() throws Exception {
        final CatalogJob dto = Utils.newCatalogJob(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                ProductFamily.L0_SLICE, 
                "FAST"
        );
        final OutputFileDescriptor expected = new OutputFileDescriptor();
        expected.setExtension(FileExtension.SAFE);
        expected.setFilename("manifest.safe");
        expected.setKeyObjectStorage("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        expected.setMissionId("S1");
        expected.setSatelliteId("A");
        expected.setProductName("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        expected.setRelativePath("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe");
        expected.setSwathtype("IW");
        expected.setResolution("_");
        expected.setProductClass("S");
        expected.setProductType("IW_RAW__0S");
        expected.setPolarisation("DV");
        expected.setDataTakeId("021735");
        expected.setMode("FAST");
        expected.setProductFamily(ProductFamily.L0_SLICE);

        final File file = new File(
        		testDir,
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe"
        );
        final FileDescriptorBuilder uut = newDescriptorForPattern(PATTERN_PROD);
        final OutputFileDescriptor result = uut.buildOutputFileDescriptor(file, dto, ProductFamily.L0_SLICE);

        assertNotNull("File descriptor should not be null", result);
        assertEquals("File descriptor are not equals", expected, result);
    }

    @Test
    public void testBuildL0SegmentFileDescriptor() throws Exception {
        final CatalogJob dto = Utils.newCatalogJob(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                ProductFamily.L0_SEGMENT, 
                "FAST"
        );
        final OutputFileDescriptor expected = new OutputFileDescriptor();
        expected.setExtension(FileExtension.SAFE);
        expected.setFilename("manifest.safe");
        expected.setKeyObjectStorage("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        expected.setMissionId("S1");
        expected.setSatelliteId("A");
        expected.setProductName("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        expected.setRelativePath("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe");
        expected.setSwathtype("IW");
        expected.setResolution("_");
        expected.setProductClass("S");
        expected.setProductType("IW_RAW__0S");
        expected.setPolarisation("DV");
        expected.setDataTakeId("021735");
        expected.setMode("FAST");
        expected.setProductFamily(ProductFamily.L0_SEGMENT);

        final File file = new File(testDir, "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe");

        final FileDescriptorBuilder uut = newDescriptorForPattern(PATTERN_PROD); 
        final OutputFileDescriptor result = uut.buildOutputFileDescriptor(file, dto, ProductFamily.L0_SEGMENT);
        assertNotNull("File descriptor should not be null", result);
        assertEquals("File descriptor are not equals", expected, result);
    }

    @Test
    public void testBuildL1OutputFileDescriptor() throws Exception {
        final CatalogJob dto = Utils.newCatalogJob(
                "S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE",
                "S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE",
                ProductFamily.L1_SLICE, 
                "NRT"
        );        
        final OutputFileDescriptor expected = new OutputFileDescriptor();
        expected.setExtension(FileExtension.SAFE);
        expected.setFilename("manifest.safe");
        expected.setKeyObjectStorage("S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE");
        expected.setMissionId("S1");
        expected.setSatelliteId("A");
        expected.setProductName("S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE");
        expected.setRelativePath("S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE/manifest.safe");
        expected.setSwathtype("IW");
        expected.setResolution("H");
        expected.setProductClass("S");
        expected.setProductType("IW_GRDH_1S");
        expected.setPolarisation("DV");
        expected.setDataTakeId("023A69");
        expected.setMode("NRT");
        expected.setProductFamily(ProductFamily.L1_SLICE);

        final File file = new File(testDir, "S1A_IW_GRDH_1SDV_20180227T145618_20180227T145643_020794_023A69_D7EC.SAFE/manifest.safe");
        final FileDescriptorBuilder uut = newDescriptorForPattern(PATTERN_PROD); 
      
        final OutputFileDescriptor result = uut.buildOutputFileDescriptor(file, dto, ProductFamily.L1_SLICE);
        assertNotNull("File descriptor should not be null", result);
        assertEquals("File descriptor are not equals", expected, result);
    }
    
    @Test
    public void testBuildS3FileDescriptor() throws Exception {
    	final CatalogJob dto = Utils.newCatalogJob(
                "S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3",
                "S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3",
                ProductFamily.S3_AUXILIARY_FILE, 
                "NRT"
        );  
    	
        final S3FileDescriptor expected = new S3FileDescriptor();
        expected.setProductType("AX___BA__AX");
		expected.setProductClass("AX");
		expected.setRelativePath("S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3/xfdumanifest.xml");
		expected.setFilename("xfdumanifest.xml");
		expected.setExtension(FileExtension.SEN3);
		expected.setProductName("S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3");
		expected.setMissionId("S3");
		expected.setSatelliteId("A");
		expected.setKeyObjectStorage("S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3");
		expected.setProductFamily(ProductFamily.S3_AUXILIARY_FILE);
		expected.setInstanceId("_________________");
		expected.setGeneratingCentre("WER");
		expected.setClassId("D_AL____");
		expected.setMode("NRT");

        final File file = new File(testDir, "S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3/xfdumanifest.xml");
        final FileDescriptorBuilder uut = newDescriptorForPattern(PATTERN_S3);
        final S3FileDescriptor result = uut.buildS3FileDescriptor(file, dto, ProductFamily.S3_AUXILIARY_FILE);
        assertNotNull("File descriptor should not be null", result);
        assertEquals("File descriptor are not equals", expected, result);
    }

    @Test(expected = AbstractCodedException.class)
    public void testBuildFileDescriptorPatternFail() throws Exception {
        final File invalidAux = new File(testDir,"S1A_OPER_OUX_OBMEMC_PDMC_20140201T000000.xml");
        final FileDescriptorBuilder uut = newDescriptorForPattern(PATTERN_AUX); 
        uut.buildAuxDescriptor(invalidAux);
    }

    private final FileDescriptorBuilder newDescriptorForPattern(final String pattern) {
    	return new FileDescriptorBuilder(testDir, Pattern.compile(pattern,Pattern.CASE_INSENSITIVE));
    }

}
