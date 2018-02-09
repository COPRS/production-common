package fr.viveris.s1pdgs.ingestor.services.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import fr.viveris.s1pdgs.ingestor.model.ConfigFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.ErdsSessionFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.ErdsSessionFileType;
import fr.viveris.s1pdgs.ingestor.model.FileExtension;
import fr.viveris.s1pdgs.ingestor.model.dto.KafkaMetadataDto;
import fr.viveris.s1pdgs.ingestor.model.exception.FileException;
import fr.viveris.s1pdgs.ingestor.model.exception.MetadataExtractionException;
import fr.viveris.s1pdgs.ingestor.model.exception.TestFileException;

public class MetadataBuilderTest {

	@Mock
	private ExtractMetadata extractor;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	private void mockExtractorProcessEOFFIle(JSONObject result) throws MetadataExtractionException {
		Mockito.doAnswer(i -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new TestFileException(e.getMessage());
			}
			return result;
		}).when(extractor).processEOFFile(Mockito.any(ConfigFileDescriptor.class), Mockito.any(File.class));
	}

	private void mockExtractorprocessEOFFileWithoutNamespace(JSONObject result) throws MetadataExtractionException {
		Mockito.doAnswer(i -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new TestFileException(e.getMessage());
			}
			return result;
		}).when(extractor).processEOFFileWithoutNamespace(Mockito.any(ConfigFileDescriptor.class),
				Mockito.any(File.class));
	}

	private void mockExtractorprocessXMLFile(JSONObject result) throws MetadataExtractionException {
		Mockito.doAnswer(i -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new TestFileException(e.getMessage());
			}
			return result;
		}).when(extractor).processXMLFile(Mockito.any(ConfigFileDescriptor.class), Mockito.any(File.class));
	}

	private void mockExtractorprocessSAFEFile(JSONObject result) throws MetadataExtractionException {
		Mockito.doAnswer(i -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new TestFileException(e.getMessage());
			}
			return result;
		}).when(extractor).processSAFEFile(Mockito.any(ConfigFileDescriptor.class), Mockito.any(File.class));
	}

	private void mockExtractorprocessRAWFile(JSONObject result) throws MetadataExtractionException {
		Mockito.doAnswer(i -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new TestFileException(e.getMessage());
			}
			return result;
		}).when(extractor).processRAWFile(Mockito.any(ErdsSessionFileDescriptor.class));
	}

	private void mockExtractorprocessSESSIONFile(JSONObject result) throws MetadataExtractionException {
		Mockito.doAnswer(i -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new TestFileException(e.getMessage());
			}
			return result;
		}).when(extractor).processSESSIONFile(Mockito.any(ErdsSessionFileDescriptor.class));
	}

	@Test
	public void testBuildConfigFileMetadataXml() throws JSONException, MetadataExtractionException {

		ConfigFileDescriptor descriptor = new ConfigFileDescriptor();
		descriptor.setDirectory(false);
		descriptor.setExtension(FileExtension.XML);
		descriptor.setFilename("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setHasToBeStored(true);
		descriptor.setHasToExtractMetadata(true);
		descriptor.setKeyObjectStorage("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setProductType("AUX_OBMEMC");
		descriptor.setRelativePath("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");

		JSONObject expectedResult = new JSONObject("{'productName': S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml}");

		this.mockExtractorProcessEOFFIle(null);
		this.mockExtractorprocessEOFFileWithoutNamespace(null);
		this.mockExtractorprocessXMLFile(expectedResult);
		this.mockExtractorprocessSAFEFile(null);
		this.mockExtractorprocessRAWFile(null);
		this.mockExtractorprocessSESSIONFile(null);

		File file = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");

		try {
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			KafkaMetadataDto dto = metadataBuilder.buildConfigFileMetadata(descriptor, file);
			assertEquals("Action should be CREATE", "CREATE", dto.getAction());
			assertNotNull("Metadata should not be null", dto.getMetadata());
			assertEquals("Metadata are not equals", expectedResult.toString(), dto.getMetadata());
		} catch (FileException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

	@Test
	public void testBuildConfigFileMetadataInvalidExtension() throws MetadataExtractionException {

		this.mockExtractorProcessEOFFIle(null);
		this.mockExtractorprocessEOFFileWithoutNamespace(null);
		this.mockExtractorprocessXMLFile(null);
		this.mockExtractorprocessSAFEFile(null);
		this.mockExtractorprocessRAWFile(null);
		this.mockExtractorprocessSESSIONFile(null);

		ConfigFileDescriptor descriptor = new ConfigFileDescriptor();
		descriptor.setDirectory(false);
		descriptor.setFilename("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setHasToBeStored(true);
		descriptor.setHasToExtractMetadata(true);
		descriptor.setKeyObjectStorage("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductClass("OPER");
		descriptor.setProductName("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		descriptor.setProductType("AUX_OBMEMC");
		descriptor.setRelativePath("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");

		File file = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");

		try {
			descriptor.setExtension(FileExtension.DAT);
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			metadataBuilder.buildConfigFileMetadata(descriptor, file);
			fail("An exception should occur");
		} catch (MetadataExtractionException fe) {
			assertEquals("Raised exception shall concern S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
					"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml", fe.getProductName());
		}

		try {
			descriptor.setExtension(FileExtension.RAW);
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			metadataBuilder.buildConfigFileMetadata(descriptor, file);
			fail("An exception should occur");
		} catch (MetadataExtractionException fe) {
			assertEquals("Raised exception shall concern S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
					"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml", fe.getProductName());
		}

		try {
			descriptor.setExtension(FileExtension.XSD);
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			metadataBuilder.buildConfigFileMetadata(descriptor, file);
			fail("An exception should occur");
		} catch (MetadataExtractionException fe) {
			assertEquals("Raised exception shall concern S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
					"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml", fe.getProductName());
		}

		try {
			descriptor.setExtension(FileExtension.UNKNOWN);
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			metadataBuilder.buildConfigFileMetadata(descriptor, file);
			fail("An exception should occur");
		} catch (MetadataExtractionException fe) {
			assertEquals("Raised exception shall concern S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
					"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml", fe.getProductName());
		}
	}

	@Test
	public void testBuildSessionMetadataSession() throws JSONException, MetadataExtractionException {
		// Mock the extractor
		JSONObject expectedResult = new JSONObject(
				"{\"insertionTime\":\"2018-02-07T13:26:12\",\"missionId\":\"S1\",\"sessionId\":\"SESSION1\",\"productName\":\"DCS_02_SESSION1_ch1_DSIB.xml\",\"satelliteId\":\"A\",\"productType\":\"SESSION\",\"url\":\"SESSION1/DCS_02_SESSION1_ch1_DSIB.xml\"}");
		this.mockExtractorProcessEOFFIle(null);
		this.mockExtractorprocessEOFFileWithoutNamespace(null);
		this.mockExtractorprocessXMLFile(null);
		this.mockExtractorprocessSAFEFile(null);
		this.mockExtractorprocessRAWFile(null);
		this.mockExtractorprocessSESSIONFile(expectedResult);

		// Build the parameters
		ErdsSessionFileDescriptor descriptor = new ErdsSessionFileDescriptor();
		descriptor.setExtension(FileExtension.XML);
		descriptor.setFilename("DCS_02_SESSION1_ch1_DSIB.xml");
		descriptor.setKeyObjectStorage("S1A/SESSION1/ch01/DCS_02_SESSION1_ch1_DSIB.xml");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName("DCS_02_SESSION1_ch1_DSIB.xml");
		descriptor.setProductType(ErdsSessionFileType.SESSION);
		descriptor.setRelativePath("S1A/SESSION1/ch01/DCS_02_SESSION1_ch1_DSIB.xml");
		descriptor.setChannel(1);
		descriptor.setSessionIdentifier("SESSION1");

		try {
			MetadataBuilder metadataBuilder = new MetadataBuilder(extractor);
			KafkaMetadataDto dto = metadataBuilder.buildErdsSessionFileMetadata(descriptor, null);

			assertEquals("Action should be CREATE", "CREATE", dto.getAction());
			assertNotNull("Metadata should not be null", dto.getMetadata());
			assertEquals("Metadata are not equals", expectedResult.toString(), dto.getMetadata());
		} catch (FileException fe) {
			fail("Exception occurred: " + fe.getMessage());
		}
	}

}
