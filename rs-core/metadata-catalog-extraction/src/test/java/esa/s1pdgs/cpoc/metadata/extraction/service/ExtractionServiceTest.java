package esa.s1pdgs.cpoc.metadata.extraction.service;

import static esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage.NOT_DEFINED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.index.seqno.SequenceNumbers;
import org.elasticsearch.index.shard.ShardId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.metadata.extraction.Utils;
import esa.s1pdgs.cpoc.metadata.extraction.service.elastic.ElasticsearchDAO;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan("esa.s1pdgs.cpoc")
public class ExtractionServiceTest {

	@MockBean
	ObsClient mockObsClient;
	
	@MockBean
	RestHighLevelClient mockRestHighLevelClient;
	
	@MockBean
	ElasticsearchDAO mockElasticsearchDAO;

	@Autowired
	ExtractionService extractionService;
	
	private final File inputDir = new File("src/test/resources/workDir/");
	private File testDir;
	
	@Before
	public void setUp() throws Exception {
		testDir = Files.createDirectories(new File("/tmp/local-catalog").toPath()).toFile();;
        Utils.copyFolder(inputDir.toPath(), testDir.toPath());
	}
	
	@After
	public void tearDown() {
		FileUtils.delete(testDir.getPath());
	}
	
	@BeforeEach
	public void beforeEach() {
		MockitoAnnotations.openMocks(this);
	}

	private static GetResponse newGetResponse_withExistsFalse() {
		final boolean exists = false;
		return new GetResponse(new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO,
				SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, exists, null, null, null));
	}
	
	private static IndexResponse newIndexResponse_withCreatedTrue() {
		final boolean created = true;
		return new IndexResponse(new ShardId("foo", "bar", 23), "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO,
				SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, created);
	}
	
	private static CatalogJob newCatalogJob(final String productName, final String relativePath,
			final ProductFamily productFamily, final String timeliness, final String stationName) {
		return new CatalogJob(productFamily, productName, NOT_DEFINED, relativePath, 0L, NOT_DEFINED,
				stationName, "NOMINAL", timeliness);
	}
		
	@Test
	public void testExtractionService_onS1AUX_shallPersistValidRecord() throws IOException, AbstractCodedException {
		List<File> files = List.of(new File(testDir, "S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF"));
		doReturn(files).when(mockObsClient).download(Mockito.any(), Mockito.any());
		doReturn(newGetResponse_withExistsFalse()).when(mockElasticsearchDAO).get(Mockito.any(GetRequest.class));
		doReturn(newIndexResponse_withCreatedTrue()).when(mockElasticsearchDAO).index(Mockito.any(IndexRequest.class));
		ArgumentCaptor<IndexRequest> argumentCaptor = ArgumentCaptor.forClass(IndexRequest.class);

		extractionService.apply(newCatalogJob(
				"S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF",
				NOT_DEFINED, ProductFamily.AUXILIARY_FILE, "NRT", null)); // timeliness will only be persisted if not null
		
		verify(mockObsClient, times(1)).download(Mockito.any(), Mockito.any());
		verify(mockElasticsearchDAO).index(argumentCaptor.capture());
		IndexRequest indexRequest = argumentCaptor.getValue();
		JSONObject metadata = new JSONObject(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString(4));
		
		assertEquals("S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF",
				indexRequest.id());
		
		assertEquals(ProductFamily.AUXILIARY_FILE.name(), metadata.getString("productFamily"));
		assertEquals(19683, metadata.getInt("absolutOrbit"));
		assertEquals("OPER", metadata.getString("productClass"));
		assertEquals("S1", metadata.getString("missionId"));
		assertEquals("2017-12-13T14:38:38.000000Z", metadata.getString("creationTime"));
		assertEquals("A", metadata.getString("satelliteId"));
		assertEquals("1.4.0", metadata.getString("processorVersion"));
		assertEquals("0001", metadata.getString("version"));
		assertEquals("S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF", metadata.getString("productName"));
		assertEquals("S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF", metadata.getString("url"));
		assertEquals("A", metadata.getString("platformSerialIdentifier"));
		assertEquals("2017-12-13T13:45:07.000000Z", metadata.getString("validityStopTime"));
		assertEquals("OPOD", metadata.getString("site"));
		assertEquals("NRT", metadata.getString("timeliness"));
		assertEquals("2017-12-13T10:27:47.593331Z", metadata.getString("selectedOrbitFirstAzimuthTimeUtc"));
		assertEquals("Sentinel-1", metadata.getString("platformShortName"));
		assertEquals("2017-12-13T10:27:37.000000Z", metadata.getString("validityStartTime"));
		assertEquals("AUX_RESORB", metadata.getString("productType"));
		assertEquals(DateUtils.convertToMetadataDateTimeFormat(
				metadata.getString("insertionTime")), metadata.getString("insertionTime")); // check format
		assertEquals(19, metadata.length());
	}
	
	@Test
	public void testExtractionService_onS1EDRS_shallPersistValidRecord() throws IOException, AbstractCodedException {
		List<File> files = List.of(new File("foobar")); // this time only used for downloading
		doReturn(files).when(mockObsClient).download(Mockito.any(), Mockito.any());
		doReturn(newGetResponse_withExistsFalse()).when(mockElasticsearchDAO).get(Mockito.any(GetRequest.class));
		doReturn(newIndexResponse_withCreatedTrue()).when(mockElasticsearchDAO).index(Mockito.any(IndexRequest.class));
		ArgumentCaptor<IndexRequest> argumentCaptor = ArgumentCaptor.forClass(IndexRequest.class);
		
		extractionService.apply(newCatalogJob(
				"L20171109175634707000125/DCS_02_SESSION1_ch1_DSIB.xml",
				"S1B/L20171109175634707000125/DCS_02_SESSION1_ch1_DSIB.xml",
				ProductFamily.EDRS_SESSION, "NRT", "WILE")); // timeliness will only be persisted if not null
		
		verify(mockObsClient, times(1)).download(Mockito.any(), Mockito.any());
		verify(mockElasticsearchDAO).index(argumentCaptor.capture());
		IndexRequest indexRequest = argumentCaptor.getValue();
		JSONObject metadata = new JSONObject(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString(4));
		
		assertEquals("DCS_02_SESSION1_ch1_DSIB.xml", indexRequest.id());
		
		assertEquals(ProductFamily.EDRS_SESSION.name(), metadata.getString("productFamily"));
		assertEquals("WILE", metadata.getString("stationCode"));
		assertEquals("S1", metadata.getString("missionId"));
		assertEquals("B", metadata.getString("satelliteId"));
		assertEquals("SESSION1", metadata.getString("sessionId"));
		assertEquals("DCS_02_SESSION1_ch1_DSIB.xml", metadata.getString("productName"));
		assertEquals("L20171109175634707000125/DCS_02_SESSION1_ch1_DSIB.xml", metadata.getString("url"));
		assertEquals("NRT", metadata.getString("timeliness"));
		assertEquals("2017-12-13T14:59:48.000000Z", metadata.getString("startTime"));
		assertEquals("2017-12-13T15:17:25.000000Z", metadata.getString("stopTime"));
		assertEquals(1, metadata.getInt("channelId"));
		assertEquals("SESSION", metadata.getString("productType"));
		for (int i=0; i < 35; i++) {
			assertEquals(String.format("DCS_02_L20171109175634707000125_ch1_DSDB_%05d.raw", i+1),
					metadata.getJSONArray("rawNames").get(i));
		}
		assertEquals(35, metadata.getJSONArray("rawNames").length());
		assertEquals(DateUtils.convertToMetadataDateTimeFormat(
				metadata.getString("insertionTime")), metadata.getString("insertionTime")); // check format
		assertEquals(14, metadata.length());
	}
	
	@Test
	public void testExtractionService_onS1L0Segment_shallPersistValidRecord() throws IOException, AbstractCodedException {
		List<File> files = List.of(new File(testDir, "S1A_IW_RAW__0SVH_20200120T124746_20200120T125111_030884_038B5E_9470.SAFE/manifest.safe"));
		doReturn(files).when(mockObsClient).download(Mockito.any(), Mockito.any());
		doReturn(newGetResponse_withExistsFalse()).when(mockElasticsearchDAO).get(Mockito.any(GetRequest.class));
		doReturn(newIndexResponse_withCreatedTrue()).when(mockElasticsearchDAO).index(Mockito.any(IndexRequest.class));
		ArgumentCaptor<IndexRequest> argumentCaptor = ArgumentCaptor.forClass(IndexRequest.class);
		
		extractionService.apply(newCatalogJob(
				"S1A_IW_RAW__0SVH_20200120T124746_20200120T125111_030884_038B5E_9470.SAFE",
				NOT_DEFINED, ProductFamily.L0_SEGMENT, "unused (uses mdextractor.packetstore-type-timelinesses.Standard instead)", null));
		
		verify(mockObsClient, times(1)).download(Mockito.any(), Mockito.any());
		verify(mockElasticsearchDAO).index(argumentCaptor.capture());
		IndexRequest indexRequest = argumentCaptor.getValue();
		JSONObject metadata = new JSONObject(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString(4));
		
		assertEquals("S1A_IW_RAW__0SVH_20200120T124746_20200120T125111_030884_038B5E_9470.SAFE",
				indexRequest.id());
		
		assertEquals(232286, metadata.getInt("missionDataTakeId"));
		assertEquals(ProductFamily.L0_SEGMENT.name(), metadata.getString("productFamily"));
		assertEquals(0, metadata.getInt("qualityNumOfCorruptedElements"));
		assertEquals("2021-04-19T14:45:16.778287Z", metadata.getString("creationTime"));
		assertEquals("VH", metadata.getString("polarisation"));
		assertEquals(30884, metadata.getInt("absoluteStopOrbit"));
		assertEquals("_", metadata.getString("resolution"));
		assertEquals(5, metadata.getInt("circulationFlag"));
		assertEquals("S1A_IW_RAW__0SVH_20200120T124746_20200120T125111_030884_038B5E_9470.SAFE", metadata.getString("productName"));
		assertEquals("038B5E", metadata.getString("dataTakeId"));
		assertEquals("FULL", metadata.getString("productConsolidation"));
		assertEquals(30884, metadata.getInt("absoluteStartOrbit"));
		assertEquals("2020-01-20T12:51:11.706993Z", metadata.getString("validityStopTime"));
		assertEquals("6", metadata.getString("instrumentConfigurationId"));
		assertEquals(12, metadata.getInt("relativeStopOrbit"));
		assertEquals(12, metadata.getInt("relativeStartOrbit"));
		assertEquals("2020-01-20T12:47:46.019051Z", metadata.getString("startTime"));
		assertEquals("2020-01-20T12:51:11.706993Z", metadata.getString("stopTime"));
		assertEquals(0, metadata.getInt("qualityNumOfMissingElements"));
		assertEquals("IW_RAW__0S", metadata.getString("productType"));
		assertEquals("S", metadata.getString("productClass"));
		assertEquals("S1", metadata.getString("missionId"));
		assertEquals("IW", metadata.getString("swathtype"));
		assertEquals("DESCENDING", metadata.getString("pass"));
		assertEquals("A", metadata.getString("satelliteId"));
		assertEquals(2672620.053, metadata.getDouble("stopTimeANX"));
		assertEquals("S1A_IW_RAW__0SVH_20200120T124746_20200120T125111_030884_038B5E_9470.SAFE", metadata.getString("url"));
		assertEquals("", metadata.getString("productSensingConsolidation"));
		assertEquals("FAST24", metadata.getString("timeliness")); // obtained from mdextractor.packetstore-type-timelinesses.Standard
		assertEquals(2466932.111, metadata.getDouble("startTimeANX"));
		assertEquals("2020-01-20T12:47:46.019051Z", metadata.getString("validityStartTime"));
		assertEquals("NOMINAL", metadata.getString("processMode"));
		JSONObject segmentCoordinates = metadata.getJSONObject("segmentCoordinates");
		assertEquals(3, segmentCoordinates.length());
		assertEquals("counterclockwise", segmentCoordinates.getString("orientation"));
		assertEquals("polygon", segmentCoordinates.getString("type"));
		JSONArray coordinates = segmentCoordinates.getJSONArray("coordinates");
		assertEquals(1, coordinates.length());
		JSONArray points = (JSONArray)coordinates.get(0);
		assertEquals(5, points.length());
		assertEquals(-103.1979, ((JSONArray)points.get(0)).getDouble(0));
		assertEquals(30.4307, ((JSONArray)points.get(0)).getDouble(1));
		assertEquals(-105.6059, ((JSONArray)points.get(1)).getDouble(0));
		assertEquals(17.9991, ((JSONArray)points.get(1)).getDouble(1));
		assertEquals(-103.3109, ((JSONArray)points.get(2)).getDouble(0));
		assertEquals(17.7208, ((JSONArray)points.get(2)).getDouble(1));
		assertEquals(-100.6624, ((JSONArray)points.get(3)).getDouble(0));
		assertEquals(30.1622, ((JSONArray)points.get(3)).getDouble(1));
		assertEquals(-103.1979, ((JSONArray)points.get(4)).getDouble(0));
		assertEquals(30.4307, ((JSONArray)points.get(4)).getDouble(1));
		assertEquals(DateUtils.convertToMetadataDateTimeFormat(
				metadata.getString("insertionTime")), metadata.getString("insertionTime")); // check format
		assertEquals(34, metadata.length());
	}

	@Test
	public void testExtractionService_onS1L0Slice_shallPersistValidRecord() throws IOException, AbstractCodedException {
		List<File> files = List.of(new File(testDir, "S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE/manifest.safe"));
		doReturn(files).when(mockObsClient).download(Mockito.any(), Mockito.any());
		doReturn(newGetResponse_withExistsFalse()).when(mockElasticsearchDAO).get(Mockito.any(GetRequest.class));
		doReturn(newIndexResponse_withCreatedTrue()).when(mockElasticsearchDAO).index(Mockito.any(IndexRequest.class));
		ArgumentCaptor<IndexRequest> argumentCaptor = ArgumentCaptor.forClass(IndexRequest.class);

		extractionService.apply(newCatalogJob(
				"S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE",
				NOT_DEFINED, ProductFamily.L0_SLICE, "NRT", null)); // timeliness will only be persisted if not null
		
		verify(mockObsClient, times(1)).download(Mockito.any(), Mockito.any());
		verify(mockElasticsearchDAO).index(argumentCaptor.capture());
		IndexRequest indexRequest = argumentCaptor.getValue();
		JSONObject metadata = new JSONObject(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString(4));
		
		assertEquals("S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE",
				indexRequest.id());
		
		assertEquals(72627, metadata.getInt("missionDataTakeId"));
		assertEquals(ProductFamily.L0_SLICE.name(), metadata.getString("productFamily"));
		assertEquals(0, metadata.getInt("qualityNumOfCorruptedElements"));
		assertEquals(false, metadata.getBoolean("sliceProductFlag"));
		assertEquals("measurementData", metadata.getString("qualityDataObjectID"));
		assertEquals("", metadata.getString("theoreticalSliceLength"));
		JSONObject sliceCoordinates = metadata.getJSONObject("sliceCoordinates");
		assertEquals(3, sliceCoordinates.length());
		assertEquals("counterclockwise", sliceCoordinates.getString("orientation"));
		assertEquals("polygon", sliceCoordinates.getString("type"));
		JSONArray coordinates = sliceCoordinates.getJSONArray("coordinates");
		assertEquals(1, coordinates.length());
		JSONArray points = (JSONArray)coordinates.get(0);
		assertEquals(5, points.length());
		assertEquals(-94.8783, ((JSONArray)points.get(0)).getDouble(0));
		assertEquals(73.8984, ((JSONArray)points.get(0)).getDouble(1));
		assertEquals(-98.2395, ((JSONArray)points.get(1)).getDouble(0));
		assertEquals(67.6029, ((JSONArray)points.get(1)).getDouble(1));
		assertEquals(-88.9623, ((JSONArray)points.get(2)).getDouble(0));
		assertEquals(66.8368, ((JSONArray)points.get(2)).getDouble(1));
		assertEquals(-82.486, ((JSONArray)points.get(3)).getDouble(0));
		assertEquals(72.8925, ((JSONArray)points.get(3)).getDouble(1));
		assertEquals(-94.8783, ((JSONArray)points.get(4)).getDouble(0));
		assertEquals(73.8984, ((JSONArray)points.get(4)).getDouble(1));
		assertEquals("2018-09-17T14:47:54.825363Z", metadata.getString("creationTime"));
		assertEquals("SAR", metadata.getString("instrumentShortName"));
		assertEquals("DV", metadata.getString("polarisation"));
		assertEquals(1, metadata.getInt("sliceNumber"));
		assertEquals(9809, metadata.getInt("absoluteStopOrbit"));
		assertEquals(183276, metadata.getInt("qualityNumOfElements"));
		assertEquals("_", metadata.getString("resolution"));
		assertEquals(7, metadata.getInt("circulationFlag"));
		assertEquals("S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE", metadata.getString("productName"));
		assertEquals("021735", metadata.getString("dataTakeId"));
		assertEquals("FULL", metadata.getString("productConsolidation"));
		assertEquals(9809, metadata.getInt("absoluteStartOrbit"));
		assertEquals("2018-02-27T12:53:00.422905Z", metadata.getString("validityStopTime"));
		assertEquals("1", metadata.getString("instrumentConfigurationId"));
		assertEquals("SENTINEL-1", metadata.getString("platformShortName"));
		assertEquals(158, metadata.getInt("relativeStopOrbit"));
		assertEquals(158, metadata.getInt("relativeStartOrbit"));
		assertEquals("2018-02-27T12:51:14.794304Z", metadata.getString("startTime"));
		assertEquals("2018-02-27T12:53:00.422905Z", metadata.getString("stopTime"));
		assertEquals(0, metadata.getInt("qualityNumOfMissingElements"));
		assertEquals("IW_RAW__0S", metadata.getString("productType"));
		assertEquals("S", metadata.getString("productClass"));
		assertEquals("S1", metadata.getString("missionId"));
		assertEquals("IW", metadata.getString("swathtype"));
		assertEquals("DESCENDING", metadata.getString("pass"));
		assertEquals("B", metadata.getString("satelliteId"));
		assertEquals(1849446.881, metadata.getDouble("stopTimeANX"));
		assertEquals("HV", metadata.getJSONArray("polarisationChannels").get(0));
		assertEquals("73.8984,-94.8783 67.6029,-98.2395 66.8368,-88.9623 72.8925,-82.4860 73.8984,-94.8783", metadata.getString("coordinates"));
		assertEquals("S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE", metadata.getString("url"));
		assertEquals("B", metadata.getString("platformSerialIdentifier"));
		assertEquals("EW", metadata.getString("operationalMode"));
		assertEquals("DLR-Oberpfaffenhofen", metadata.getString("site"));
		assertEquals("NRT", metadata.getString("timeliness"));
		assertEquals("NOT_CHECKED", metadata.getString("oqcFlag"));
		assertEquals("", metadata.getString("sliceOverlap"));
		assertEquals(1743818.281, metadata.getDouble("startTimeANX"));
		assertEquals("2018-02-27T12:51:14.794304Z", metadata.getString("validityStartTime"));
		assertEquals("NOMINAL", metadata.getString("processMode"));
		assertEquals(62, metadata.getInt("cycleNumber"));
		assertEquals(DateUtils.convertToMetadataDateTimeFormat(
				metadata.getString("insertionTime")), metadata.getString("insertionTime")); // check format
		assertEquals(48, metadata.length());
	}

	@Test
	public void testExtractionService_onS2AUX_shallPersistValidRecord() throws IOException, AbstractCodedException {
		doReturn(newGetResponse_withExistsFalse()).when(mockElasticsearchDAO).get(Mockito.any(GetRequest.class));
		doReturn(newIndexResponse_withCreatedTrue()).when(mockElasticsearchDAO).index(Mockito.any(IndexRequest.class));
		ArgumentCaptor<IndexRequest> argumentCaptor = ArgumentCaptor.forClass(IndexRequest.class);

		extractionService.apply(newCatalogJob(
				"S2A_OPER_AUX_SADATA_EPAE_20190222T003515_V20190221T190438_20190221T204519_A019158_WF_LN.zip",
				NOT_DEFINED, ProductFamily.S2_AUX, "NRT", null)); // timeliness will only be persisted if not null
		
		verify(mockObsClient, times(0)).download(Mockito.any(), Mockito.any());
		verify(mockElasticsearchDAO).index(argumentCaptor.capture());
		IndexRequest indexRequest = argumentCaptor.getValue();
		JSONObject metadata = new JSONObject(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString(4));

		assertEquals("S2A_OPER_AUX_SADATA_EPAE_20190222T003515_V20190221T190438_20190221T204519_A019158_WF_LN.zip",
				indexRequest.id());
		
		assertEquals("S2A_OPER_AUX_SADATA_EPAE_20190222T003515_V20190221T190438_20190221T204519_A019158_WF_LN.zip",
				metadata.getString("productName"));
		assertEquals("S2A_OPER_AUX_SADATA_EPAE_20190222T003515_V20190221T190438_20190221T204519_A019158_WF_LN.zip",
				metadata.getString("url"));
		assertEquals(ProductFamily.S2_AUX.name(), metadata.getString("productFamily"));
		assertEquals("S2", metadata.getString("missionId"));
		assertEquals("A", metadata.getString("satelliteId"));
		assertEquals("OPER", metadata.getString("productClass"));
		assertEquals("AUX_SADATA", metadata.getString("productType"));
		assertEquals("2019-02-22T00:35:15.000000Z", metadata.getString("creationTime"));
		assertEquals("2019-02-21T19:04:38.000000Z", metadata.getString("validityStartTime"));
		assertEquals("2019-02-21T20:45:19.000000Z", metadata.getString("validityStopTime"));
		assertEquals(19158, metadata.getInt("absolutOrbit"));
		assertEquals("F", metadata.getString("completenessId"));
		assertEquals("N", metadata.getString("degradationId"));
		assertEquals("NRT", metadata.getString("timeliness"));
		assertEquals(DateUtils.convertToMetadataDateTimeFormat(
				metadata.getString("insertionTime")), metadata.getString("insertionTime")); // check format
		assertEquals(15, metadata.length());
	}
	
	@Test
	public void testExtractionService_onS2GIP_shallPersistValidRecord() throws IOException {
		doReturn(newGetResponse_withExistsFalse()).when(mockElasticsearchDAO).get(Mockito.any(GetRequest.class));
		doReturn(newIndexResponse_withCreatedTrue()).when(mockElasticsearchDAO).index(Mockito.any(IndexRequest.class));
		ArgumentCaptor<IndexRequest> argumentCaptor = ArgumentCaptor.forClass(IndexRequest.class);

		extractionService.apply(newCatalogJob(
				"S2B_OPER_GIP_R2DEFI_MPC__20170206T103039_V20170101T000000_21000101T000000_B8A.TGZ",
				NOT_DEFINED, ProductFamily.S2_AUX, "NRT", null)); // timeliness will only be persisted if not null
		
		verify(mockElasticsearchDAO).index(argumentCaptor.capture());
		IndexRequest indexRequest = argumentCaptor.getValue();
		JSONObject metadata = new JSONObject(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString(4));

		assertEquals("S2B_OPER_GIP_R2DEFI_MPC__20170206T103039_V20170101T000000_21000101T000000_B8A.TGZ",
				indexRequest.id());
		
		assertEquals("S2B_OPER_GIP_R2DEFI_MPC__20170206T103039_V20170101T000000_21000101T000000_B8A.TGZ",
				metadata.getString("productName"));
		assertEquals("S2B_OPER_GIP_R2DEFI_MPC__20170206T103039_V20170101T000000_21000101T000000_B8A.TGZ",
				metadata.getString("url"));
		assertEquals(ProductFamily.S2_AUX.name(), metadata.getString("productFamily"));
		assertEquals("S2", metadata.getString("missionId"));
		assertEquals("B", metadata.getString("satelliteId"));
		assertEquals("OPER", metadata.getString("productClass"));
		assertEquals("GIP_R2DEFI", metadata.getString("productType"));
		assertEquals("2017-02-06T10:30:39.000000Z", metadata.getString("creationTime"));
		assertEquals("2017-01-01T00:00:00.000000Z", metadata.getString("validityStartTime"));
		assertEquals("2100-01-01T00:00:00.000000Z", metadata.getString("validityStopTime"));
		assertEquals("8A", metadata.getString("bandIndexId"));
		assertEquals("NRT", metadata.getString("timeliness"));
		assertEquals(DateUtils.convertToMetadataDateTimeFormat(
				metadata.getString("insertionTime")), metadata.getString("insertionTime")); // check format
		assertEquals(13, metadata.length());
	}
	
	@Test
	public void testExtractionService_onS2HKTM_shallPersistValidRecord() throws IOException, AbstractCodedException {
		doReturn(newGetResponse_withExistsFalse()).when(mockElasticsearchDAO).get(Mockito.any(GetRequest.class));
		doReturn(newIndexResponse_withCreatedTrue()).when(mockElasticsearchDAO).index(Mockito.any(IndexRequest.class));
		ArgumentCaptor<IndexRequest> argumentCaptor = ArgumentCaptor.forClass(IndexRequest.class);

		extractionService.apply(newCatalogJob(
				"S2A_OPER_PRD_HKTM___20191203T051837_20191203T051842_0001.tar",
				NOT_DEFINED, ProductFamily.S2_HKTM, "NRT", null)); // timeliness will only be persisted if not null
		
		verify(mockObsClient, times(0)).download(Mockito.any(), Mockito.any());
		verify(mockElasticsearchDAO).index(argumentCaptor.capture());
		IndexRequest indexRequest = argumentCaptor.getValue();
		JSONObject metadata = new JSONObject(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString(4));

		assertEquals("S2A_OPER_PRD_HKTM___20191203T051837_20191203T051842_0001.tar",
				indexRequest.id());
		
		assertEquals("S2A_OPER_PRD_HKTM___20191203T051837_20191203T051842_0001.tar", metadata.getString("productName"));
		assertEquals(ProductFamily.S2_HKTM.name(), metadata.getString("productFamily"));
		assertEquals("S2", metadata.getString("missionId"));
		assertEquals("A", metadata.getString("satelliteId"));
		assertEquals("OPER", metadata.getString("productClass"));
		assertEquals("PRD_HKTM__", metadata.getString("productType"));
		assertEquals("2019-12-03T05:18:37.000000Z", metadata.getString("validityStartTime"));
		assertEquals("2019-12-03T05:18:42.000000Z", metadata.getString("validityStopTime"));
		assertEquals("NRT", metadata.getString("timeliness"));
		assertEquals(DateUtils.convertToMetadataDateTimeFormat(
				metadata.getString("insertionTime")), metadata.getString("insertionTime")); // check format
		assertEquals("S2A_OPER_PRD_HKTM___20191203T051837_20191203T051842_0001.tar", metadata.getString("url"));
		assertEquals(11, metadata.length());
	}
	
	@Test
	public void testExtractionService_onS2L0C_shallPersistValidRecord() throws IOException, AbstractCodedException {
		List<File> files1 = List.of(new File(testDir, "S2A_OPER_MSI_L0__GR_SGS__20191001T101733_S20191001T083650_D01_N02.08/manifest.safe"));
		List<File> files2 = List.of(new File(testDir, "S2A_OPER_MSI_L0__GR_SGS__20191001T101733_S20191001T083650_D01_N02.08/Inventory_Metadata.xml"));
		doReturn(files1, files2).when(mockObsClient).download(Mockito.any(), Mockito.any());
		doReturn(newGetResponse_withExistsFalse()).when(mockElasticsearchDAO).get(Mockito.any(GetRequest.class));
		doReturn(newIndexResponse_withCreatedTrue()).when(mockElasticsearchDAO).index(Mockito.any(IndexRequest.class));
		ArgumentCaptor<IndexRequest> argumentCaptor = ArgumentCaptor.forClass(IndexRequest.class);

		extractionService.apply(newCatalogJob(
				"S2A_OPER_MSI_L0__GR_SGS__20191001T101733_S20191001T083650_D01_N02.08",
				NOT_DEFINED, ProductFamily.S2_L0_GR, "NRT", null)); // timeliness will only be persisted if not null
		
		verify(mockObsClient, times(2)).download(Mockito.any(), Mockito.any());
		verify(mockElasticsearchDAO).index(argumentCaptor.capture());
		IndexRequest indexRequest = argumentCaptor.getValue();
		JSONObject metadata = new JSONObject(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString(4));
		
		assertEquals("S2A_OPER_MSI_L0__GR_SGS__20191001T101733_S20191001T083650_D01_N02.08",
				indexRequest.id());
		
	    assertEquals(ProductFamily.S2_L0_GR.name(), metadata.getString("productFamily"));
	    assertEquals("2019-10-01T10:17:33.000000Z", metadata.getString("creationTime"));
	    assertEquals("MSI", metadata.getString("instrumentShortName"));
	    assertEquals("GS2A_20191001T082741_022326_N02.08", metadata.getString("productGroupId"));
	    assertEquals("S2A_OPER_MSI_L0__GR_SGS__20191001T101733_S20191001T083650_D01_N02.08", metadata.getString("productName"));
	    assertEquals("022326", metadata.getString("orbitNumber"));
	    assertEquals(100, metadata.getInt("qualityInfo"));
	    assertEquals(100, metadata.getInt("qualityStatus"));
	    assertEquals("2019-10-01T08:36:50.000000Z", metadata.getString("startTime"));
	    assertEquals("2019-10-01T08:36:50.000000Z", metadata.getString("stopTime"));
	    assertEquals("MSI_L0__GR", metadata.getString("productType"));
	    assertEquals("OPER", metadata.getString("productClass"));
	    assertEquals("S2", metadata.getString("missionId"));
	    assertEquals("A", metadata.getString("satelliteId"));
	    assertEquals("", metadata.getString("relativeOrbitNumber"));
	    assertEquals("2.08", metadata.getString("processorVersion"));
	    assertEquals("S2A_OPER_MSI_L0__GR_SGS__20191001T101733_S20191001T083650_D01_N02.08", metadata.getString("url"));
	    assertEquals("S2A", metadata.getString("platformSerialIdentifier"));
	    assertEquals("", metadata.getString("operationalMode"));
	    assertEquals("SGS_", metadata.getString("processingCenter"));
	    assertEquals("NRT", metadata.getString("timeliness"));
	    assertEquals("", metadata.getString("platfomShortName"));
	    assertEquals("NOMINAL", metadata.getString("processMode"));
	    JSONObject coordinates = metadata.getJSONObject("coordinates");
		assertEquals(3, coordinates.length());
		assertEquals("counterclockwise", coordinates.getString("orientation"));
		assertEquals("polygon", coordinates.getString("type"));
		assertEquals(1, coordinates.getJSONArray("coordinates").length());
		JSONArray points = (JSONArray)coordinates.getJSONArray("coordinates").get(0);
		assertEquals(5, points.length());
		assertEquals(36.012719203864, ((JSONArray)points.get(0)).getDouble(0));
		assertEquals(48.4255437011909, ((JSONArray)points.get(0)).getDouble(1));
		assertEquals(35.8754856182544, ((JSONArray)points.get(1)).getDouble(0));
		assertEquals(48.0615335354928, ((JSONArray)points.get(1)).getDouble(1));
		assertEquals(36.2301956943322, ((JSONArray)points.get(2)).getDouble(0));
		assertEquals(48.0129223197342, ((JSONArray)points.get(2)).getDouble(1));
		assertEquals(36.3680125728466, ((JSONArray)points.get(3)).getDouble(0));
		assertEquals(48.3755327752305, ((JSONArray)points.get(3)).getDouble(1));
		assertEquals(36.012719203864, ((JSONArray)points.get(4)).getDouble(0));
		assertEquals(48.4255437011909, ((JSONArray)points.get(4)).getDouble(1));
	    assertEquals(DateUtils.convertToMetadataDateTimeFormat(
				metadata.getString("insertionTime")), metadata.getString("insertionTime")); // check format
		assertEquals(25, metadata.length());
	}	

}
