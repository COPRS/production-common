/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import java.util.Map;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.index.seqno.SequenceNumbers;
import org.elasticsearch.index.shard.ShardId;
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
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.metadata.extraction.Utils;
import esa.s1pdgs.cpoc.metadata.extraction.service.elastic.ElasticsearchDAO;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan("esa.s1pdgs.cpoc")
public class ITExtractionService {

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
		ProductMetadata metadata = ProductMetadata.ofJson(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString());
		
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
		ProductMetadata metadata = ProductMetadata.ofJson(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString());
		
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
					((List<?>)metadata.get("rawNames")).get(i));
		}
		assertEquals(35, ((List<?>)metadata.get("rawNames")).size());
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
		ProductMetadata metadata = ProductMetadata.ofJson(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString());
		
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
		@SuppressWarnings("unchecked")
		Map<String,Object> segmentCoordinates = (Map<String,Object>)metadata.get("segmentCoordinates");
		assertEquals(3, segmentCoordinates.size());
		assertEquals("counterclockwise", segmentCoordinates.get("orientation"));
		assertEquals("polygon", segmentCoordinates.get("type"));
		@SuppressWarnings("unchecked")
		List<List<List<Double>>> coordinates = (List<List<List<Double>>>)segmentCoordinates.get("coordinates");
		assertEquals(1, coordinates.size());
		List<List<Double>> points = coordinates.get(0);
		assertEquals(5, points.size());
		assertEquals(-103.1979, points.get(0).get(0));
		assertEquals(30.4307, points.get(0).get(1));
		assertEquals(-105.6059, points.get(1).get(0));
		assertEquals(17.9991, points.get(1).get(1));
		assertEquals(-103.3109, points.get(2).get(0));
		assertEquals(17.7208, points.get(2).get(1));
		assertEquals(-100.6624, points.get(3).get(0));
		assertEquals(30.1622, points.get(3).get(1));
		assertEquals(-103.1979, points.get(4).get(0));
		assertEquals(30.4307, points.get(4).get(1));
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
		ProductMetadata metadata = ProductMetadata.ofJson(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString());
		
		assertEquals("S1B_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DS.SAFE",
				indexRequest.id());
		
		assertEquals(72627, metadata.getInt("missionDataTakeId"));
		assertEquals(ProductFamily.L0_SLICE.name(), metadata.getString("productFamily"));
		assertEquals(0, metadata.getInt("qualityNumOfCorruptedElements"));
		assertEquals(false, metadata.getBoolean("sliceProductFlag"));
		assertEquals("measurementData", metadata.getString("qualityDataObjectID"));
		assertEquals("", metadata.getString("theoreticalSliceLength"));
		@SuppressWarnings("unchecked")
		Map<String,Object> sliceCoordinates = (Map<String,Object>)metadata.get("sliceCoordinates");
		assertEquals(3, sliceCoordinates.size());
		assertEquals("counterclockwise", sliceCoordinates.get("orientation"));
		assertEquals("polygon", sliceCoordinates.get("type"));
		@SuppressWarnings("unchecked")
		List<List<List<Double>>> coordinates = (List<List<List<Double>>>)sliceCoordinates.get("coordinates");
		assertEquals(1, coordinates.size());
		List<List<Double>> points = coordinates.get(0);
		assertEquals(5, points.size());
		assertEquals(-94.8783, points.get(0).get(0));
		assertEquals(73.8984, points.get(0).get(1));
		assertEquals(-98.2395, points.get(1).get(0));
		assertEquals(67.6029, points.get(1).get(1));
		assertEquals(-88.9623, points.get(2).get(0));
		assertEquals(66.8368, points.get(2).get(1));
		assertEquals(-82.486, points.get(3).get(0));
		assertEquals(72.8925, points.get(3).get(1));
		assertEquals(-94.8783, points.get(4).get(0));
		assertEquals(73.8984, points.get(4).get(1));
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
		assertEquals("HV", ((List<?>)metadata.get("polarisationChannels")).get(0));
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
		ProductMetadata metadata = ProductMetadata.ofJson(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString());

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
		assertEquals(16, metadata.length());
	}
	
	@Test
	public void testExtractionService_onS2GIP_shallPersistValidRecord() throws IOException, MetadataMalformedException {
		doReturn(newGetResponse_withExistsFalse()).when(mockElasticsearchDAO).get(Mockito.any(GetRequest.class));
		doReturn(newIndexResponse_withCreatedTrue()).when(mockElasticsearchDAO).index(Mockito.any(IndexRequest.class));
		ArgumentCaptor<IndexRequest> argumentCaptor = ArgumentCaptor.forClass(IndexRequest.class);

		extractionService.apply(newCatalogJob(
				"S2B_OPER_GIP_R2DEFI_MPC__20170206T103039_V20170101T000000_21000101T000000_B8A.TGZ",
				NOT_DEFINED, ProductFamily.S2_AUX, "NRT", null)); // timeliness will only be persisted if not null
		
		verify(mockElasticsearchDAO).index(argumentCaptor.capture());
		IndexRequest indexRequest = argumentCaptor.getValue();
		ProductMetadata metadata = ProductMetadata.ofJson(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString());

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
		assertEquals(14, metadata.length());
	}
	
	@Test
	public void testExtractionService_onS2HKTM_shallPersistValidRecord() throws IOException, AbstractCodedException {
		List<File> files1 = List.of(new File(testDir, "S2A_OPER_PRD_HKTM___20191203T051837_20191203T051842_0001.SAFE/manifest.safe"));
		doReturn(files1).when(mockObsClient).download(Mockito.any(), Mockito.any());
		doReturn(newGetResponse_withExistsFalse()).when(mockElasticsearchDAO).get(Mockito.any(GetRequest.class));
		doReturn(newIndexResponse_withCreatedTrue()).when(mockElasticsearchDAO).index(Mockito.any(IndexRequest.class));
		ArgumentCaptor<IndexRequest> argumentCaptor = ArgumentCaptor.forClass(IndexRequest.class);

		extractionService.apply(newCatalogJob(
				"S2A_OPER_PRD_HKTM___20191203T051837_20191203T051842_0001.SAFE",
				NOT_DEFINED, ProductFamily.S2_HKTM, "NRT", null)); // timeliness will only be persisted if not null
		
		verify(mockObsClient, times(1)).download(Mockito.any(), Mockito.any());
		verify(mockElasticsearchDAO).index(argumentCaptor.capture());
		IndexRequest indexRequest = argumentCaptor.getValue();
		ProductMetadata metadata = ProductMetadata.ofJson(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString());

		assertEquals("S2A_OPER_PRD_HKTM___20191203T051837_20191203T051842_0001.SAFE",
				indexRequest.id());
		
		assertEquals("S2A_OPER_PRD_HKTM___20191203T051837_20191203T051842_0001.SAFE", metadata.getString("productName"));
		assertEquals(ProductFamily.S2_HKTM.name(), metadata.getString("productFamily"));
		assertEquals("S2", metadata.getString("missionId"));
		assertEquals("A", metadata.getString("satelliteId"));
		assertEquals("OPER", metadata.getString("productClass"));
		assertEquals("PRD_HKTM__", metadata.getString("productType"));
		assertEquals("2019-12-03T05:18:37.000850Z", metadata.getString("validityStartTime"));
		assertEquals("2019-12-03T05:18:42.000319Z", metadata.getString("validityStopTime"));
		assertEquals("2019-12-03T05:18:41.000342Z", metadata.getString("creationTime"));
		assertEquals("NRT", metadata.getString("timeliness"));
		assertEquals(DateUtils.convertToMetadataDateTimeFormat(
				metadata.getString("insertionTime")), metadata.getString("insertionTime")); // check format
		assertEquals("S2A_OPER_PRD_HKTM___20191203T051837_20191203T051842_0001.SAFE", metadata.getString("url"));
		assertEquals("HKTM", metadata.getString("instrumentShortName"));
		assertEquals(23225, metadata.getLong("orbitNumber"));
		assertEquals(23225, metadata.getLong("lastOrbitNumber"));
		assertEquals(59, metadata.getLong("relativeOrbitNumber"));
		assertEquals("A", metadata.getString("platformSerialIdentifier"));
		assertEquals("Svalbard", metadata.getString("site"));
		assertEquals("SENTINEL-2", metadata.getString("platfomShortName"));
		assertEquals("NOMINAL", metadata.getString("processMode"));
		assertEquals(20, metadata.length());
	}
	
	@Test
	public void testExtractionService_onS2L0C_shallPersistValidRecord() throws IOException, AbstractCodedException {
		List<File> files1 = List.of(new File(testDir, "S2A_OPER_MSI_L0__GR_SGS__20191001T101733_S20191001T083650_D01_N02.08/S2A_OPER_MTD_L0__GR_SGS__20191001T101733_S20191001T083650_D01.xml"));
		doReturn(files1).when(mockObsClient).download(Mockito.any(), Mockito.any());
		doReturn(newGetResponse_withExistsFalse()).when(mockElasticsearchDAO).get(Mockito.any(GetRequest.class));
		doReturn(newIndexResponse_withCreatedTrue()).when(mockElasticsearchDAO).index(Mockito.any(IndexRequest.class));
		ArgumentCaptor<IndexRequest> argumentCaptor = ArgumentCaptor.forClass(IndexRequest.class);

		extractionService.apply(newCatalogJob(
				"S2A_OPER_MSI_L0__GR_SGS__20191001T101733_S20191001T083650_D01_N02.08",
				NOT_DEFINED, ProductFamily.S2_L0_GR, "NRT", null)); // timeliness will only be persisted if not null
		
		verify(mockObsClient, times(1)).download(Mockito.any(), Mockito.any());
		verify(mockElasticsearchDAO).index(argumentCaptor.capture());
		IndexRequest indexRequest = argumentCaptor.getValue();
		ProductMetadata metadata = ProductMetadata.ofJson(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString());
		
		assertEquals("S2A_OPER_MSI_L0__GR_SGS__20191001T101733_S20191001T083650_D01_N02.08",
				indexRequest.id());
		
	    assertEquals(ProductFamily.S2_L0_GR.name(), metadata.getString("productFamily"));
	    assertEquals("2019-10-01T10:17:33.000000Z", metadata.getString("creationTime"));
	    assertEquals("MSI", metadata.getString("instrumentShortName"));
	    assertEquals("S2A_OPER_MSI_L0__DS_SGS__20191001T101733_S20191001T083647_N02.08", metadata.getString("productGroupId"));
	    assertEquals("S2A_OPER_MSI_L0__GR_SGS__20191001T101733_S20191001T083650_D01_N02.08", metadata.getString("productName"));
	    assertEquals("", metadata.getString("orbitNumber"));
	    assertEquals(0, metadata.getInt("qualityInfo"));
	    assertEquals("NOMINAL", metadata.getString("qualityStatus"));
	    assertEquals("2019-10-01T08:36:50.840000Z", metadata.getString("startTime"));
	    assertEquals("2019-10-01T08:36:50.840000Z", metadata.getString("stopTime"));
	    assertEquals("MSI_L0__GR", metadata.getString("productType"));
	    assertEquals("OPER", metadata.getString("productClass"));
	    assertEquals("S2", metadata.getString("missionId"));
	    assertEquals("A", metadata.getString("satelliteId"));
	    assertEquals("", metadata.getString("relativeOrbitNumber"));
	    assertEquals("", metadata.getString("processorVersion"));
	    assertEquals("S2A_OPER_MSI_L0__GR_SGS__20191001T101733_S20191001T083650_D01_N02.08", metadata.getString("url"));
	    assertEquals("S2A", metadata.getString("platformSerialIdentifier"));
	    assertEquals("", metadata.getString("operationalMode"));
	    assertEquals("RS", metadata.getString("processingCenter"));
	    assertEquals("NRT", metadata.getString("timeliness"));
	    assertEquals("NOMINAL", metadata.getString("processMode"));
	    @SuppressWarnings("unchecked")
		Map<String,Object> coordinates = (Map<String,Object>)metadata.get("coordinates");
		assertEquals(3, coordinates.size());
		assertEquals("counterclockwise", coordinates.get("orientation"));
		assertEquals("Polygon", coordinates.get("type"));
		@SuppressWarnings("unchecked")
		List<List<List<Double>>> coordinates2 = (List<List<List<Double>>>)coordinates.get("coordinates");
		assertEquals(1, coordinates2.size());
		List<List<Double>> points = coordinates2.get(0);
		assertEquals(5, points.size());
		assertEquals(36.012719203864, points.get(0).get(0));
		assertEquals(48.4255437011909, points.get(0).get(1));
		assertEquals(35.8754856182544, points.get(1).get(0));
		assertEquals(48.0615335354928, points.get(1).get(1));
		assertEquals(36.2301956943322, points.get(2).get(0));
		assertEquals(48.0129223197342, points.get(2).get(1));
		assertEquals(36.3680125728466, points.get(3).get(0));
		assertEquals(48.3755327752305, points.get(3).get(1));
		assertEquals(36.012719203864, points.get(4).get(0));
		assertEquals(48.4255437011909, points.get(4).get(1));
	    assertEquals(DateUtils.convertToMetadataDateTimeFormat(
				metadata.getString("insertionTime")), metadata.getString("insertionTime")); // check format
		assertEquals(25, metadata.length());
	}	

}
