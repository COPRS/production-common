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

package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
import esa.s1pdgs.cpoc.metadata.extraction.Utils;
import esa.s1pdgs.cpoc.metadata.extraction.config.MetadataExtractorConfig;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.ExtractMetadata;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFactory;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class TestLevelSegmentMetadataExtractor {
	private static final String PATTERN = "^(S1|AS)(A|B)_(S[1-6]|RF|GP|HK|IW|EW|WV|N[1-6]|EN|IM)_(SLC|GRD|OCN|RAW)(F|H|M|_)_(0)(A|C|N|S|_)(SH|__|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$";

	/**
	 * Elasticsearch services
	 */
	@Mock
	protected ObsClient obsClient;

	/**
	 * 
	 */
	@Mock
	protected MetadataExtractorConfig extractorConfig;

	/**
	 * Extractor
	 */
	protected LevelSegmentMetadataExtractor extractor;

	/**
	 * Job to process
	 */
	private CatalogJob inputMessageSafe;

	final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.UNDEFINED)
			.newReporting("TestMetadataExtraction");

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

		inputMessageSafe = Utils.newCatalogJob("S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE",
				"S1A_AUX_CAL_V20140402T000000_G20140402T133909.SAFE", ProductFamily.L0_SEGMENT, "NRT");

		final FileDescriptorBuilder fileDescriptorBuilder = new FileDescriptorBuilder(testDir,
				Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE));

		final ExtractMetadata extract = new ExtractMetadata(extractorConfig.getTypeOverlap(),
				extractorConfig.getTypeSliceLength(), Collections.<String, String>emptyMap(),
				extractorConfig.getPacketStoreTypes(), extractorConfig.getPacketstoreTypeTimelinesses(),
				extractorConfig.getTimelinessPriorityFromHighToLow(), extractorConfig.getXsltDirectory(), xmlConverter);
		final MetadataBuilder mdBuilder = new MetadataBuilder(extract);

		extractor = new LevelSegmentMetadataExtractor(mdBuilder, fileDescriptorBuilder, testDir.getPath(),
				new ProcessConfiguration(), obsClient);
	}

	@Test
	public void testExtractMetadataL0Segment()
			throws MetadataExtractionException, AbstractCodedException {

		final List<File> files = Arrays
				.asList(new File(testDir, "S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE"
						+ File.separator + "manifest.safe"));

		inputMessageSafe = Utils.newCatalogJob(
				"S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE",
				"S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE", ProductFamily.L0_SEGMENT,
				"FAST");

		doReturn(files).when(obsClient).download(Mockito.anyList(), Mockito.any());

		final OutputFileDescriptor descriptor = new OutputFileDescriptor();
		descriptor.setExtension(FileExtension.SAFE);
		descriptor.setFilename("manifest.safe");
		descriptor.setKeyObjectStorage("S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName("S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE");
		descriptor.setRelativePath("S1A_WV_RAW__0SSV_20180913T214325_20180913T214422_023685_0294F4_41D5.SAFE");
		descriptor.setSwathtype("WV");
		descriptor.setResolution("_");
		descriptor.setProductClass("S");
		descriptor.setProductType("WV_RAW__0S");
		descriptor.setPolarisation("SV");
		descriptor.setDataTakeId("0294F4");
		descriptor.setProductFamily(ProductFamily.L0_SEGMENT);
		descriptor.setMode("FAST");

		final ProductMetadata expected = extractor.mdBuilder.buildL0SegmentOutputFileMetadata(descriptor, files.get(0),
				ReportingFactory.NULL);

		final ProductMetadata result = extractor.extract(reporting, inputMessageSafe);

		Iterator<String> it = expected.keys().iterator();;
		while (it.hasNext()) {
			String key = it.next();
			if (!("insertionTime".equals(key) || "segmentCoordinates".equals(key) || "creationTime".equals(key))) {
				assertEquals(expected.get(key), result.get(key));
			}
		}
		verify(obsClient, times(1)).download(Mockito.any(), Mockito.any());
	}

}
