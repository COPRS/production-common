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

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.FileExtension;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.metadata.PathMetadataExtractorImpl;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.metadata.extraction.Utils;
import esa.s1pdgs.cpoc.metadata.extraction.config.MetadataExtractorConfig;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.ExtractMetadata;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class TestEdrsMetadataExtractor {

	private static final String PATTERN = "(WILE|MTI_|SGS_|INU_)/S1(A|B)/([A-Za-z0-9]+)/ch0?(1|2)/(.+DSIB\\.(xml|XML)|.+DSDB.*\\.(raw|RAW|aisp|AISP))";

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
	protected EdrsMetadataExtractor extractor;

	/**
	 * Job to process
	 */
	private CatalogJob inputMessage;

	private final File testDir = FileUtils.createTmpDir();

	@Mock
	XmlConverter xmlConverter;

	/**
	 * Initialization
	 * 
	 * @throws AbstractCodedException
	 */
	@Before
	public void init() throws AbstractCodedException {
		MockitoAnnotations.initMocks(this);

		inputMessage = Utils.newCatalogJob("D_123_ch01_DSDB.RAW", "WILE/S1A/123/ch01/D_123_ch01_DSDB.RAW",
				ProductFamily.EDRS_SESSION, null, "WILE/S1A/123/ch01/D_123_ch01_DSDB.RAW");
		inputMessage.setStationName("WILE");


		final FileDescriptorBuilder fileDescriptorBuilder = new FileDescriptorBuilder(testDir,
				Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE));

		final ExtractMetadata extract = new ExtractMetadata(extractorConfig.getTypeOverlap(),
				extractorConfig.getTypeSliceLength(), Collections.<String, String>emptyMap(),
				extractorConfig.getPacketStoreTypes(), extractorConfig.getPacketstoreTypeTimelinesses(),
				extractorConfig.getTimelinessPriorityFromHighToLow(), extractorConfig.getXsltDirectory(), xmlConverter);
		final MetadataBuilder mdBuilder = new MetadataBuilder(extract);

		final Map<String, Integer> conf = new HashMap<>();
		conf.put("stationCode", 1);
		conf.put("missionId", 2);
		conf.put("satelliteId", 3);
		conf.put("sessionId", 4);
		conf.put("channelId", 5);

		extractor = new EdrsMetadataExtractor(mdBuilder, fileDescriptorBuilder, testDir.getPath(),
				new ProcessConfiguration(), obsClient,
				new PathMetadataExtractorImpl(
						Pattern.compile("^([a-z_]{4})/([0-9a-z_]{2})([0-9a-z_]{1})/([0-9a-z_]+)/ch0?([1-2])/.+",
								Pattern.CASE_INSENSITIVE),
						conf));
	}

	@Test
	public void testExtractMetadata() throws MetadataExtractionException, AbstractCodedException {
		final EdrsSessionFileDescriptor expectedDescriptor = new EdrsSessionFileDescriptor();

		expectedDescriptor.setFilename("D_123_ch01_DSDB.RAW");
		expectedDescriptor.setRelativePath("WILE/S1A/123/ch01/D_123_ch01_DSDB.RAW");
		expectedDescriptor.setProductName("D_123_ch01_DSDB.RAW");
		expectedDescriptor.setExtension(FileExtension.RAW);
		expectedDescriptor.setEdrsSessionFileType(EdrsSessionFileType.RAW);
		expectedDescriptor.setMissionId("S1");
		expectedDescriptor.setSatelliteId("A");
		expectedDescriptor.setChannel(1);
		expectedDescriptor.setSessionIdentifier("123");
		expectedDescriptor.setStationCode("WILE");
		expectedDescriptor.setKeyObjectStorage("WILE/S1A/123/ch01/D_123_ch01_DSDB.RAW");
		expectedDescriptor.setProductFamily(ProductFamily.EDRS_SESSION);

		final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.S1)
				.newReporting("TestMetadataExtraction");

		final ProductMetadata expected = extractor.mdBuilder.buildEdrsSessionFileRaw(expectedDescriptor);
		final ProductMetadata result = extractor.extract(reporting, inputMessage);

		Iterator<String> it = expected.keys().iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (!"insertionTime".equals(key)) {
				assertEquals(expected.get(key), result.get(key));
			}
		}

	}

}
