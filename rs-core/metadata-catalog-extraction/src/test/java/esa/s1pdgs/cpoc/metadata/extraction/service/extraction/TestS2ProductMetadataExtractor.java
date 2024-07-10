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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.metadata.extraction.Utils;
import esa.s1pdgs.cpoc.metadata.extraction.config.MetadataExtractorConfig;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.ExtractMetadata;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.S2FileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class TestS2ProductMetadataExtractor {

	private static final String PATTERN = "^(S2)(A|B|_)_([A-Z0-9]{4})_((MSI)_(L0_|L1A|L1B|L1C|L2A)_(GR|DS|TL|TC))_\\w{4}_(\\d{8}T\\d{6})(.*)$";

	@Mock
	private ObsClient obsClient;

	@Mock
	private MetadataExtractorConfig extractorConfig;

	@Mock
	XmlConverter xmlConverter;

	private S2ProductMetadataExtractor extractor;

	private static final File inputDir = new File("src/test/resources/workDir/");
	private final File testDir = FileUtils.createTmpDir();

	/**
	 * Initialization
	 * 
	 * @throws AbstractCodedException
	 */
	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		Utils.copyFolder(inputDir.toPath(), testDir.toPath());

		doReturn("config/xsltDir/").when(extractorConfig).getXsltDirectory();

		final FileDescriptorBuilder fileDescriptorBuilder = new FileDescriptorBuilder(testDir,
				Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE));

		final ExtractMetadata extract = new ExtractMetadata(extractorConfig.getTypeOverlap(),
				extractorConfig.getTypeSliceLength(), Collections.<String, String>emptyMap(),
				extractorConfig.getPacketStoreTypes(), extractorConfig.getPacketstoreTypeTimelinesses(),
				extractorConfig.getTimelinessPriorityFromHighToLow(), extractorConfig.getXsltDirectory(), xmlConverter);
		final MetadataBuilder mdBuilder = new MetadataBuilder(extract);

		ProcessConfiguration processConfig = new ProcessConfiguration();
		Map<String, String> manifestMap = new HashMap<>();
		manifestMap.put("s2", "[S2PRODUCTNAME].xml");
		processConfig.setManifestFilenames(manifestMap);

		extractor = new S2ProductMetadataExtractor(mdBuilder, fileDescriptorBuilder, testDir.getPath(), false,
				processConfig, obsClient);
	}

	@After
	public void cleanup() {
		FileUtils.delete(testDir.getPath());
	}

	@Test
	public void extract_S2_L0_DS_Metadata() throws AbstractCodedException, SdkClientException {

		final String keyObs = "S2A_OPER_MSI_L0__DS_SGS__20200420T205828_S20200322T173347_N02.08";

		final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.S2)
				.newReporting("TestMetadataExtraction");

		// Prepare OBS returnValues
		final String metadataPath = "S2A_OPER_MTD_L0__DS_SGS__20200420T205828_S20200322T173347.xml";
		final List<String> metadataFilenames = Arrays.asList(metadataPath);
		doReturn(metadataFilenames).when(obsClient).list(Mockito.any(), Mockito.any());

		final List<File> metadataFiles = Arrays.asList(new File(testDir, keyObs + "/" + metadataPath));
		doReturn(metadataFiles).when(obsClient).download(Mockito.any(), Mockito.any());

		// Prepare message
		final CatalogJob message = Utils.newCatalogJob(keyObs, keyObs, ProductFamily.S2_L0_DS, "NRT");

		final S2FileDescriptor expectedDescriptor = new S2FileDescriptor();
		expectedDescriptor.setProductType("MSI_L0__DS");
		expectedDescriptor.setProductClass("OPER");
		expectedDescriptor.setRelativePath(keyObs);
		expectedDescriptor.setFilename(keyObs);
		expectedDescriptor.setProductName(keyObs);
		expectedDescriptor.setKeyObjectStorage(keyObs);
		expectedDescriptor.setMissionId("S2");
		expectedDescriptor.setSatelliteId("A");
		expectedDescriptor.setProductFamily(ProductFamily.S2_L0_DS);
		expectedDescriptor.setMode("NRT");
		expectedDescriptor.setInstrumentShortName("MSI");

		final ProductMetadata expected = extractor.mdBuilder.buildS2ProductFileMetadata(expectedDescriptor,
				metadataFiles, message);

		final ProductMetadata result = extractor.extract(reporting, message);

		Iterator<String> it = expected.keys().iterator();
    
		while (it.hasNext()) {
			String key = it.next();
			if (!"coordinates".equals(key)) {
				assertEquals(expected.get(key), result.get(key));
			}
		}
	}

	@Test
	public void extract_S2_L0_GR_Metadata() throws AbstractCodedException, SdkClientException {

		final String keyObs = "S2A_OPER_MSI_L0__GR_SGS__20200420T205828_S20200322T173347_D01_N02.08";

		final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.S2)
				.newReporting("TestMetadataExtraction");

		// Prepare OBS returnValues
		final String metadataPath = "S2A_OPER_MTD_L0__GR_SGS__20200420T205828_S20200322T173347_D01.xml";
		final List<String> metadataFilenames = Arrays.asList(metadataPath);
		doReturn(metadataFilenames).when(obsClient).list(Mockito.any(), Mockito.any());

		final List<File> metadataFiles = Arrays.asList(new File(testDir, keyObs + "/" + metadataPath));
		doReturn(metadataFiles).when(obsClient).download(Mockito.any(), Mockito.any());

		// Prepare message
		final CatalogJob message = Utils.newCatalogJob(keyObs, keyObs, ProductFamily.S2_L0_GR, "NRT");

		final S2FileDescriptor expectedDescriptor = new S2FileDescriptor();
		expectedDescriptor.setProductType("MSI_L0__GR");
		expectedDescriptor.setProductClass("OPER");
		expectedDescriptor.setRelativePath(keyObs);
		expectedDescriptor.setFilename(keyObs);
		expectedDescriptor.setProductName(keyObs);
		expectedDescriptor.setKeyObjectStorage(keyObs);
		expectedDescriptor.setMissionId("S2");
		expectedDescriptor.setSatelliteId("A");
		expectedDescriptor.setProductFamily(ProductFamily.S2_L0_GR);
		expectedDescriptor.setMode("NRT");
		expectedDescriptor.setInstrumentShortName("MSI");

		final ProductMetadata expected = extractor.mdBuilder.buildS2ProductFileMetadata(expectedDescriptor,
				metadataFiles, message);

		final ProductMetadata result = extractor.extract(reporting, message);

		Iterator<String> it = expected.keys().iterator();

		while (it.hasNext()) {
			String key = it.next();
			if (!"coordinates".equals(key)) {
				assertEquals(expected.get(key), result.get(key));
			}
		}
	}
	
	@Test
	public void extract_S2_L1_TCI() throws AbstractCodedException, SdkClientException {
		final String keyObs = "S2B_OPER_MSI_L1C_TC_EPAE_20191001T102654_A013417_T39UWP_N02.08.jp2";
		
		final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.S2)
				.newReporting("TestMetadataExtraction");
		
		// Prepare OBS returnValues
		final List<String> metadataFilenames = Arrays.asList(keyObs);
		doReturn(metadataFilenames).when(obsClient).list(Mockito.any(), Mockito.any());
				
		final List<File> metadataFiles = Arrays.asList(new File(testDir, keyObs));
		doReturn(metadataFiles).when(obsClient).download(Mockito.any(), Mockito.any());
		
		// Prepare message
		final CatalogJob message = Utils.newCatalogJob(keyObs, keyObs, ProductFamily.S2_L1C_TC, "NRT");
		
		final S2FileDescriptor expectedDescriptor = new S2FileDescriptor();
		expectedDescriptor.setProductType("MSI_L1C_TC");
		expectedDescriptor.setProductClass("OPER");
		expectedDescriptor.setRelativePath(keyObs);
		expectedDescriptor.setFilename(keyObs);
		expectedDescriptor.setProductName(keyObs);
		expectedDescriptor.setKeyObjectStorage(keyObs);
		expectedDescriptor.setMissionId("S2");
		expectedDescriptor.setSatelliteId("B");
		expectedDescriptor.setProductFamily(ProductFamily.S2_L1C_TC);
		expectedDescriptor.setMode("NRT");
		expectedDescriptor.setInstrumentShortName("MSI");
		
		final ProductMetadata expected = extractor.mdBuilder.buildS2L1TCIMetadata(expectedDescriptor,
				metadataFiles.get(0), message);
		
		final ProductMetadata result = extractor.extract(reporting, message);
		
		Iterator<String> it = expected.keys().iterator();

		while (it.hasNext()) {
			String key = it.next();
			if (!"coordinates".equals(key)) {
				assertEquals(expected.get(key), result.get(key));
			}
		}
	}
	
	@Test
	public void extract_S2_L2A_TCI() throws AbstractCodedException, SdkClientException {
		final String keyObs = "S2B_OPER_MSI_L2A_TC_REFS_20230601T211358_A032516_T59GPL_N04.00.jp2";
		
		final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.S2)
				.newReporting("TestMetadataExtraction");
		
		// Prepare OBS returnValues
		final List<String> metadataFilenames = Arrays.asList(keyObs);
		doReturn(metadataFilenames).when(obsClient).list(Mockito.any(), Mockito.any());
				
		final List<File> metadataFiles = Arrays.asList(new File(testDir, keyObs));
		doReturn(metadataFiles).when(obsClient).download(Mockito.any(), Mockito.any());
		
		// Prepare message
		final CatalogJob message = Utils.newCatalogJob(keyObs, keyObs, ProductFamily.S2_L2A_TC, "NRT");
		
		final S2FileDescriptor expectedDescriptor = new S2FileDescriptor();
		expectedDescriptor.setProductType("MSI_L2A_TC");
		expectedDescriptor.setProductClass("OPER");
		expectedDescriptor.setRelativePath(keyObs);
		expectedDescriptor.setFilename(keyObs);
		expectedDescriptor.setProductName(keyObs);
		expectedDescriptor.setKeyObjectStorage(keyObs);
		expectedDescriptor.setMissionId("S2");
		expectedDescriptor.setSatelliteId("B");
		expectedDescriptor.setProductFamily(ProductFamily.S2_L2A_TC);
		expectedDescriptor.setMode("NRT");
		expectedDescriptor.setInstrumentShortName("MSI");
		
		final ProductMetadata expected = extractor.mdBuilder.buildS2L1TCIMetadata(expectedDescriptor,
				metadataFiles.get(0), message);
		
		final ProductMetadata result = extractor.extract(reporting, message);
		
		Iterator<String> it = expected.keys().iterator();

		while (it.hasNext()) {
			String key = it.next();
			if (!"coordinates".equals(key)) {
				assertEquals(expected.get(key), result.get(key));
			}
		}
	}

}
