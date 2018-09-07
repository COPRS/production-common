package esa.s1pdgs.cpoc.mdcatalog.extraction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.FileExtension;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.L0OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.L1OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.obs.ObsService;
import esa.s1pdgs.cpoc.mdcatalog.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class LevelProductsExtractorTest {

    /**
     * Elasticsearch services
     */
    @Mock
    protected EsServices esServices;

    /**
     * Elasticsearch services
     */
    @Mock
    protected ObsService obsService;

    /**
     * MQI service
     */
    @Mock
    private GenericMqiService<LevelProductDto> mqiService;

    /**
     * 
     */
    @Mock
    protected MetadataExtractorConfig extractorConfig;

    /**
     * Application status
     */
    @Mock
    protected AppStatus appStatus;

    /**
     * Extractor
     */
    protected LevelProductsExtractor extractor;

    /**
     * Job to process
     */
    private GenericMessageDto<LevelProductDto> inputMessage;

    /**
     * Job to process
     */
    private GenericMessageDto<LevelProductDto> inputMessageSafe;

    /**
     * Job to process
     */
    private GenericMessageDto<LevelProductDto> inputMessageAux;

    /**
     * Initialization
     * 
     * @throws AbstractCodedException
     */
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);

        // "EW:8.2F||IW:7.4F||SM:7.7F||WM:0.0F"
        Map<String, Float> typeOverlap = new HashMap<String, Float>();
        typeOverlap.put("EW", 8.2F);
        typeOverlap.put("IW", 7.4F);
        typeOverlap.put("SM", 7.7F);
        typeOverlap.put("WM", 0.0F);
        // "EW:60.0F||IW:25.0F||SM:25.0F||WM:0.0F"
        Map<String, Float> typeSliceLength = new HashMap<String, Float>();
        typeSliceLength.put("EW", 60.0F);
        typeSliceLength.put("IW", 25.0F);
        typeSliceLength.put("SM", 25.0F);
        typeSliceLength.put("WM", 0.0F);
        doReturn("config/xsltDir/").when(extractorConfig).getXsltDirectory();
        doReturn(typeOverlap).when(extractorConfig).getTypeOverlap();
        doReturn(typeSliceLength).when(extractorConfig).getTypeSliceLength();

        doNothing().when(appStatus).setError(Mockito.any(), Mockito.anyString());
        doReturn(true).when(mqiService).ack(Mockito.any());

        inputMessage = new GenericMessageDto<LevelProductDto>(123, "",
                new LevelProductDto("product-name", "key-obs",
                        ProductFamily.L0_ACN));

        inputMessageSafe = new GenericMessageDto<LevelProductDto>(123, "",
                new LevelProductDto(
                        "S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE",
                        "S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE",
                        ProductFamily.L0_ACN));

        inputMessageAux = new GenericMessageDto<LevelProductDto>(123, "",
                new LevelProductDto(
                        "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
                        "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
                        ProductFamily.L0_ACN));

        extractor = new LevelProductsExtractor(esServices, obsService,
                mqiService, appStatus, extractorConfig,
                (new File("./test/workDir/")).getAbsolutePath()
                        + File.separator,
                "manifest.safe", ".safe");
    }

    @Test
    public void testGetKeyObs() {
        assertEquals("key-obs", extractor.getKeyObs(inputMessage));
        assertEquals(
                "S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE/manifest.safe",
                extractor.getKeyObs(inputMessageSafe));
    }

    @Test
    public void testExtractProductName() {
        assertEquals("product-name",
                extractor.extractProductNameFromDto(inputMessage.getBody()));
    }

    @Test
    public void testCleanProcessing() throws IOException {
        File newWorkDir = new File("./test/workDir2");
        newWorkDir.mkdirs();

        (new File(
                "./test/workDir2/S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE"))
                        .mkdirs();
        (new File(
                "./test/workDir2/S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE/manifest.safe"))
                        .createNewFile();
        (new File(
                "./test/workDir2/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml"))
                        .createNewFile();

        extractor = new LevelProductsExtractor(esServices, obsService,
                mqiService, appStatus, extractorConfig, "./test/workDir2/",
                "manifest.safe", ".safe");
        assertTrue((new File(
                "./test/workDir2/S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE"))
                        .exists());
        assertTrue((new File(
                "./test/workDir2/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml"))
                        .exists());

        extractor.cleanProcessing(inputMessage);
        assertTrue((new File(
                "./test/workDir2/S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE"))
                        .exists());
        assertTrue((new File(
                "./test/workDir2/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml"))
                        .exists());

        extractor.cleanProcessing(inputMessageSafe);
        assertFalse((new File(
                "./test/workDir2/S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE"))
                        .exists());
        assertTrue((new File(
                "./test/workDir2/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml"))
                        .exists());

        extractor.cleanProcessing(inputMessageAux);
        assertFalse((new File(
                "./test/workDir2/S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE"))
                        .exists());
        assertFalse((new File(
                "./test/workDir2/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml"))
                        .exists());

        FileUtils.delete("./test/workDir2");
    }

    @Test
    public void testExtractMetadataL0Product()
            throws MetadataExtractionException, AbstractCodedException {

        File file = new File((new File("./test/workDir/")).getAbsolutePath()
                + File.separator
                + "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE"
                + File.separator + "manifest.safe");

        inputMessageSafe = new GenericMessageDto<LevelProductDto>(123, "",
                new LevelProductDto(
                        "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                        "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                        ProductFamily.L0_PRODUCT));

        doReturn(file).when(obsService).downloadFile(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());

        L0OutputFileDescriptor descriptor = new L0OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductName(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setRelativePath(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setSwathtype("IW");
        descriptor.setResolution("_");
        descriptor.setProductClass("S");
        descriptor.setProductType("IW_RAW__0S");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("021735");

        JSONObject expected = extractor.mdBuilder
                .buildL0SliceOutputFileMetadata(descriptor, file);
        JSONObject result = extractor.extractMetadata(inputMessageSafe);
        for (String key : expected.keySet()) {
            if (!("insertionTime".equals(key) || "sliceCoordinates".equals(key) 
                    || "creationTime".equals(key))) {
                assertEquals(expected.get(key), result.get(key));
            }
        }

        verify(obsService, times(1)).downloadFile(
                Mockito.eq(ProductFamily.L0_PRODUCT),
                Mockito.eq(
                        "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe"),
                Mockito.eq(extractor.localDirectory));

    }

    @Test
    public void testExtractMetadataL0Acn()
            throws MetadataExtractionException, AbstractCodedException {

        File file = new File((new File("./test/workDir/")).getAbsolutePath()
                + File.separator
                + "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE"
                + File.separator + "manifest.safe");

        inputMessageSafe = new GenericMessageDto<LevelProductDto>(123, "",
                new LevelProductDto(
                        "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                        "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                        ProductFamily.L0_ACN));

        doReturn(file).when(obsService).downloadFile(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());

        L0OutputFileDescriptor descriptor = new L0OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("manifest.safe");
        descriptor.setKeyObjectStorage(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setMissionId("S1");
        descriptor.setSatelliteId("A");
        descriptor.setProductName(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setRelativePath(
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setSwathtype("IW");
        descriptor.setResolution("_");
        descriptor.setProductClass("S");
        descriptor.setProductType("IW_RAW__0S");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("021735");

        JSONObject expected = extractor.mdBuilder
                .buildL0SliceOutputFileMetadata(descriptor, file);
        JSONObject result = extractor.extractMetadata(inputMessageSafe);
        for (String key : expected.keySet()) {
            if (!("insertionTime".equals(key) || "sliceCoordinates".equals(key) 
                    || "creationTime".equals(key))) {
                assertEquals(expected.get(key), result.get(key));
            }
        }

        verify(obsService, times(1)).downloadFile(
                Mockito.eq(ProductFamily.L0_ACN),
                Mockito.eq(
                        "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe"),
                Mockito.eq(extractor.localDirectory));

    }

    @Test
    public void testExtractMetadataL1Product()
            throws MetadataExtractionException, AbstractCodedException {

        File file = new File((new File("./test/workDir/")).getAbsolutePath()
                + File.separator
                + "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE"
                + File.separator + "manifest.safe");

        inputMessageSafe = new GenericMessageDto<LevelProductDto>(123, "",
                new LevelProductDto(
                        "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                        "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                        ProductFamily.L1_PRODUCT));

        doReturn(file).when(obsService).downloadFile(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());

        L1OutputFileDescriptor descriptor = new L1OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setKeyObjectStorage("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setMissionId("S1A");
        descriptor.setProductName("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setRelativePath("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setSwathtype("IW");
        descriptor.setResolution("_");
        descriptor.setProductClass("S");
        descriptor.setProductType("IW_RAW__0S");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("021735");

        JSONObject expected = extractor.mdBuilder
                .buildL1SliceOutputFileMetadata(descriptor, file);
        JSONObject result = extractor.extractMetadata(inputMessageSafe);
        for (String key : expected.keySet()) {
            if (!("insertionTime".equals(key) || "sliceCoordinates".equals(key) 
                    || "creationTime".equals(key))) {
                assertEquals(expected.get(key), result.get(key));
            }
        }

        verify(obsService, times(1)).downloadFile(
                Mockito.eq(ProductFamily.L1_PRODUCT),
                Mockito.eq(
                        "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe"),
                Mockito.eq(extractor.localDirectory));

    }

    @Test
    public void testExtractMetadataL1Acn()
            throws MetadataExtractionException, AbstractCodedException {

        File file = new File((new File("./test/workDir/")).getAbsolutePath()
                + File.separator
                + "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE"
                + File.separator + "manifest.safe");

        inputMessageSafe = new GenericMessageDto<LevelProductDto>(123, "",
                new LevelProductDto(
                        "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                        "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                        ProductFamily.L1_ACN));

        doReturn(file).when(obsService).downloadFile(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());

        L1OutputFileDescriptor descriptor = new L1OutputFileDescriptor();
        descriptor.setExtension(FileExtension.SAFE);
        descriptor.setFilename("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setKeyObjectStorage("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setMissionId("S1A");
        descriptor.setProductName("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setRelativePath("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE");
        descriptor.setSwathtype("IW");
        descriptor.setResolution("_");
        descriptor.setProductClass("S");
        descriptor.setProductType("IW_RAW__0S");
        descriptor.setPolarisation("DV");
        descriptor.setDataTakeId("021735");

        JSONObject expected = extractor.mdBuilder
                .buildL1SliceOutputFileMetadata(descriptor, file);
        JSONObject result = extractor.extractMetadata(inputMessageSafe);
        for (String key : expected.keySet()) {
            if (!("insertionTime".equals(key) || "sliceCoordinates".equals(key) 
                    || "creationTime".equals(key))) {
                assertEquals(expected.get(key), result.get(key));
            }
        }

        verify(obsService, times(1)).downloadFile(
                Mockito.eq(ProductFamily.L1_ACN),
                Mockito.eq(
                        "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE/manifest.safe"),
                Mockito.eq(extractor.localDirectory));

    }

}
