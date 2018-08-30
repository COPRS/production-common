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
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.ConfigFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.obs.ObsService;
import esa.s1pdgs.cpoc.mdcatalog.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.AuxiliaryFileDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class AuxiliaryFilesExtractorTest {

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
    private GenericMqiService<AuxiliaryFileDto> mqiService;

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
    protected AuxiliaryFilesExtractor extractor;

    /**
     * Job to process
     */
    private GenericMessageDto<AuxiliaryFileDto> inputMessage;

    /**
     * Job to process
     */
    private GenericMessageDto<AuxiliaryFileDto> inputMessageSafe;

    /**
     * Job to process
     */
    private GenericMessageDto<AuxiliaryFileDto> inputMessageAux;

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

        doNothing().when(appStatus).setError(Mockito.any(), "PROCESSING");
        doReturn(true).when(mqiService).ack(Mockito.any());

        inputMessage = new GenericMessageDto<AuxiliaryFileDto>(123, "",
                new AuxiliaryFileDto("product-name", "key-obs"));

        inputMessageSafe = new GenericMessageDto<AuxiliaryFileDto>(123, "",
                new AuxiliaryFileDto(
                        "S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE",
                        "S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE"));

        inputMessageAux = new GenericMessageDto<AuxiliaryFileDto>(123, "",
                new AuxiliaryFileDto(
                        "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
                        "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml"));

        extractor = new AuxiliaryFilesExtractor(esServices, obsService,
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

        extractor = new AuxiliaryFilesExtractor(esServices, obsService,
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
    public void testExtractMetadata()
            throws MetadataExtractionException, AbstractCodedException {

        File file = new File(
                (new File("./test/workDir/")).getAbsolutePath() + File.separator
                        + "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");

        doReturn(file).when(obsService).downloadFile(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());

        ConfigFileDescriptor expectedDescriptor = new ConfigFileDescriptor();
        expectedDescriptor.setExtension(FileExtension.XML);
        expectedDescriptor
                .setFilename("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        expectedDescriptor.setKeyObjectStorage(
                "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        expectedDescriptor.setMissionId("S1");
        expectedDescriptor.setSatelliteId("A");
        expectedDescriptor.setProductClass("OPER");
        expectedDescriptor
                .setProductName("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        expectedDescriptor.setProductType("AUX_OBMEMC");
        expectedDescriptor.setRelativePath(
                "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");

        JSONObject expected = extractor.mdBuilder
                .buildConfigFileMetadata(expectedDescriptor, file);
        JSONObject result = extractor.extractMetadata(inputMessageAux);
        for (String key : expected.keySet()) {
            if (!"insertionTime".equals(key)) {
                assertEquals(expected.get(key), result.get(key));
            }
        }

        verify(obsService, times(1)).downloadFile(
                Mockito.eq(ProductFamily.AUXILIARY_FILE),
                Mockito.eq("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml"),
                Mockito.eq(extractor.localDirectory));

    }

}
