package fr.viveris.s1pdgs.ingestor.services.file;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import fr.viveris.s1pdgs.ingestor.model.ConfigFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.ErdsSessionFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.ErdsSessionFileType;
import fr.viveris.s1pdgs.ingestor.model.FileExtension;
import fr.viveris.s1pdgs.ingestor.model.dto.KafkaMetadataDto;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class MetadataBuilderTest {
	
	@Autowired
	private MetadataBuilder metadataBuilder;

	// Embedded KAFKA
	@ClassRule
	public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, "test");
	
	@Test
	public void testBuildConfigFileMetadataAuxObmemc() throws JSONException {
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
		
		File file = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		
		JSONObject expectedResult = new JSONObject("{\"validityStopTime\":\"9999-12-31T23:59:59\",\"productClass\":\"OPER\",\"missionid\":\"S1\",\"creationTime\":\"2014-02-12T12:28:19\",\"insertionTime\":\"2018-02-07T11:08:52\",\"satelliteid\":\"A\",\"validityStartTime\":\"2014-02-01T00:00:00\",\"productName\":\"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml\",\"productType\":\"AUX_OBMEMC\"}");
		
		KafkaMetadataDto dto = metadataBuilder.buildConfigFileMetadata(descriptor, file);
		
		assertEquals("Invalid length", expectedResult.toString().length(), dto.getMetadata().toString().length());
		assertEquals("Invalid productName", expectedResult.getString("productName"), new JSONObject(dto.getMetadata()).getString("productName"));
		assertEquals("Invalid productType", expectedResult.getString("productType"), new JSONObject(dto.getMetadata()).getString("productType"));	
	}
	
	@Test
	public void testBuildSessionMetadataSession() throws JSONException {
		ErdsSessionFileDescriptor descriptor = new ErdsSessionFileDescriptor();
		descriptor.setExtension(FileExtension.XML);
		descriptor.setFilename("DCS_02_SESSION1_ch1_DSIB.xml");
		descriptor.setKeyObjectStorage("SESSION1/DCS_02_SESSION1_ch1_DSIB.xml");
		descriptor.setMissionId("S1");
		descriptor.setSatelliteId("A");
		descriptor.setProductName("DCS_02_SESSION1_ch1_DSIB.xml");
		descriptor.setProductType(ErdsSessionFileType.SESSION);
		descriptor.setRelativePath("SESSION1/DCS_02_SESSION1_ch1_DSIB.xml");
		descriptor.setChannel(1);
		descriptor.setSessionIdentifier("SESSION1");
		
		File file = new File("erds_sessions/S1A/SESSION1/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		
		JSONObject expectedResult = new JSONObject("{\"insertionTime\":\"2018-02-07T13:26:12\",\"sessionid\":\"SESSION1\",\"productName\":\"DCS_02_SESSION1_ch1_DSIB.xml\",\"productType\":\"SESSION\"}");
		
		KafkaMetadataDto dto = metadataBuilder.buildErdsSessionFileMetadata(descriptor, file);
		assertEquals("Invalid length", expectedResult.toString().length(), dto.getMetadata().toString().length());
		assertEquals("Invalid productName", expectedResult.getString("productName"), new JSONObject(dto.getMetadata()).getString("productName"));
		assertEquals("Invalid productType", expectedResult.getString("productType"), new JSONObject(dto.getMetadata()).getString("productType"));
	}

}
