package esa.s1pdgs.cpoc.mdc.worker.extraction;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.mdc.worker.Utils;
import esa.s1pdgs.cpoc.mdc.worker.config.MetadataExtractorConfig;
import esa.s1pdgs.cpoc.mdc.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.ExtractMetadata;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
import esa.s1pdgs.cpoc.mdc.worker.status.AppStatusImpl;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class TestPlanAndReportMetadataExtractor {
	private static final String PATTERN = "^(S1[ABCD_]_OPER_REP_MP_MP__PDMC_|S1[ABCD]_OPER_MPL_SP.{4}_PDMC_|S1[ABCD_]_OPER_MPL_FS.{4}_PDMC_|S1[ABCD]_OPER_REP_PASS_[1-9]_.{4}_|S[12]__OPER_SRA_EDRS_[AC]_PDMC_|EDR_OPER_MPL_RQ[1-9]_O[AC]_|EDR_OPER_MPL_[LM]AS_O[AC]_|EDR_OPER_MPL_CR[1-9]_O[AC]_|EDR_OPER_MPL_SS[1-9]_O[AC]_|EDR_OPER_MPL_ER[1-9]_O[AC]_|EDR_OPER_SER_SR[1-9]_O[AC]_|S1[ABCD]_OPER_MPL_ORBOEM_|EDR_OPER_MPL_GOB_P[AC]_|EDR_OPER_MPL_GOB_R[AC]_|S1[ABCD]_OPER_REP__SUP___|S1[ABCD]_OPER_REP_STNACQ_.{4}_|S1[ABCD_]_OPER_REP_STNUNV_.{4}_|S[123][ABCD_]_OPER_SRA_BANSEG_PDMC_|S1[ABCD]_OPER_TLM__REQ_[A-O]_|S1[ABCD]_OPER_REP__SMPR__|S1[ABCD]_OPER_MPL__SSC___|S1[ABCD]_OPER_TLM__PSCAT_|S1[ABCD]_OPER_MPL_OCMSAR_|S1[ABCD]_OPER_REP__MACP__|S1[ABCD]_OPER_REP__MCSF__|S1[ABCD]_OPER_MPL__NPPF__|S1[ABCD]_OPER_MPL__NPIF__|S1[ABCD]_OPER_REP_NPIFCC_|S[123][ABCD_]_OPER_SRA_GSUNAV_PDMC_|S1[ABCD]_OPER_OBS_MIMG___|S1[ABCD]_OPER_AUX_RDB____MPC__|S1[ABCD]_OPER_MPL_SESDB[ABCD]_|S1[ABCD]_OPER_REP__CHF___|S1[AB]_OPER_REP__FCHF__).*\\.(xml|XML|EOF|TGZ)$";
	
    /**
     * Elasticsearch services
     */
    @Mock
    protected EsServices esServices;

    /**
     * Elasticsearch services
     */
    @Mock
    protected ObsClient obsClient;

    /**
     * MQI service
     */
    @Mock
    private GenericMqiClient mqiService;

    /**
     * 
     */
    @Mock
    protected MetadataExtractorConfig extractorConfig;

    /**
     * Application status
     */
    @Mock
    protected AppStatusImpl appStatus;

    /**
     * Extractor
     */
    protected PlanAndReportMetadataExtractor extractor;

    /**
     * Job to process
     */
    private GenericMessageDto<CatalogJob> inputMessage;

    
	final Reporting reporting = ReportingUtils.newReportingBuilder().newReporting("TestMetadataExtraction");
    
	private static final File inputDir = new File("src/test/resources/workDir/");
	
    private final File testDir = FileUtils.createTmpDir();

    @Mock
    XmlConverter xmlConverter;
    
    /**
     * Initialization
     * 
     * @throws AbstractCodedException
     */
    @Before
    public void init() throws Exception {
    	MockitoAnnotations.initMocks(this);
        Utils.copyFolder(inputDir.toPath(), testDir.toPath());

        // "EW:8.2F||IW:7.4F||SM:7.7F||WM:0.0F"
        final Map<String, Float> typeOverlap = new HashMap<String, Float>();
        typeOverlap.put("EW", 8.2F);
        typeOverlap.put("IW", 7.4F);
        typeOverlap.put("SM", 7.7F);
        typeOverlap.put("WV", 0.0F);
        // "EW:60.0F||IW:25.0F||SM:25.0F||WM:0.0F"
        final Map<String, Float> typeSliceLength = new HashMap<String, Float>();
        typeSliceLength.put("EW", 60.0F);
        typeSliceLength.put("IW", 25.0F);
        typeSliceLength.put("SM", 25.0F);
        typeSliceLength.put("WV", 0.0F);

        final Map<String, String> packetStoreTypes = new HashMap<>();
        packetStoreTypes.put("S1A-0", "Emergency");
        packetStoreTypes.put("S1A-1", "Emergency");
        packetStoreTypes.put("S1A-2", "RFC");
        packetStoreTypes.put("S1A-20", "WV");
        packetStoreTypes.put("S1A-22", "Standard");
        packetStoreTypes.put("S1A-37", "PassThrough");
        packetStoreTypes.put("S1B-0", "Emergency");
        packetStoreTypes.put("S1B-1", "Emergency");
        packetStoreTypes.put("S1B-2", "RFC");
        packetStoreTypes.put("S1B-20", "WV");
        packetStoreTypes.put("S1B-22", "Standard");
        packetStoreTypes.put("S1B-37", "PassThrough");
        final Map<String, String> packetStoreTypesTimelinesses = new HashMap<>();
        packetStoreTypesTimelinesses.put("Emergency", "PT");
        packetStoreTypesTimelinesses.put("HKTM", "NRT");
        packetStoreTypesTimelinesses.put("NRT", "NRT");
        packetStoreTypesTimelinesses.put("GPS", "NRT");
        packetStoreTypesTimelinesses.put("PassThrough", "PT");
        packetStoreTypesTimelinesses.put("Standard", "FAST24");
        packetStoreTypesTimelinesses.put("RFC", "FAST24");
        packetStoreTypesTimelinesses.put("WV", "FAST24");
        packetStoreTypesTimelinesses.put("Filler", "FAST24");
        packetStoreTypesTimelinesses.put("Spare", "FAST24");        
        final List<String> timelinessPriorityFromHighToLow = Arrays.asList("PT", "NRT", "FAST24");
        
        doReturn("config/xsltDir/").when(extractorConfig).getXsltDirectory();
        doReturn(typeOverlap).when(extractorConfig).getTypeOverlap();
        doReturn(typeSliceLength).when(extractorConfig).getTypeSliceLength();
        doReturn(packetStoreTypes).when(extractorConfig).getPacketStoreTypes();
        doReturn(packetStoreTypesTimelinesses).when(extractorConfig).getPacketstoreTypeTimelinesses();
        doReturn(timelinessPriorityFromHighToLow).when(extractorConfig).getTimelinessPriorityFromHighToLow();

        doNothing().when(appStatus).setError(Mockito.any(), Mockito.anyString());
        doReturn(true).when(mqiService).ack(Mockito.any(), Mockito.any());
        
        inputMessage = new GenericMessageDto<CatalogJob>(123, "",
                Utils.newCatalogJob(
                        "S1A_OPER_REP__SUP___20181208T070000_20181208T111500_0001.EOF",
                        "S1A_OPER_REP__SUP___20181208T070000_20181208T111500_0001.EOF",
                        ProductFamily.PLAN_AND_REPORT, "NRT"));
        
        Date creationDate = Date.from(Instant.now());
        inputMessage.getBody().setCreationDate(creationDate);
        
		final FileDescriptorBuilder fileDescriptorBuilder = new FileDescriptorBuilder(
				testDir, 
				Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE)
		);
		
		final ExtractMetadata extract = new ExtractMetadata(
				extractorConfig.getTypeOverlap(), 
				extractorConfig.getTypeSliceLength(),
				extractorConfig.getPacketStoreTypes(),
				extractorConfig.getPacketstoreTypeTimelinesses(),
				extractorConfig.getTimelinessPriorityFromHighToLow(),
				extractorConfig.getXsltDirectory(), 
				xmlConverter
		);		
		final MetadataBuilder mdBuilder = new MetadataBuilder(extract);

	    extractor = new PlanAndReportMetadataExtractor(
	    			esServices, 
	    			mdBuilder, 
	    			fileDescriptorBuilder, 
	    			testDir.getPath(), 
	    			new ProcessConfiguration(), 
	    			obsClient
	    );
    }
    
    @Test
    public void testExtraction() throws MetadataExtractionException, AbstractCodedException {
    	doReturn(Collections.emptyList()).when(obsClient).download(Mockito.anyList(), Mockito.any());
    	
    	final JSONObject result = extractor.extract(reporting, inputMessage);
   	
    	assertEquals(inputMessage.getBody().getProductName(), result.get("productName"));
    	assertEquals(inputMessage.getBody().getCreationDate(), result.get("insertionTime"));
    	assertEquals(inputMessage.getBody().getKeyObjectStorage(), result.get("keyObjectStorage"));

    	verify(obsClient, times(0)).download(Mockito.any(), Mockito.any()); // no unnecessary download shall happen
    }

}
