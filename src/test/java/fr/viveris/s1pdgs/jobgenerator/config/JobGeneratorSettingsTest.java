package fr.viveris.s1pdgs.jobgenerator.config;

import static org.junit.Assert.assertEquals;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.model.ProductFamily;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class JobGeneratorSettingsTest {

	// Embedded KAFKA
	@ClassRule
	public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, "t-pdgs-l0-jobs");
	
	@Autowired
	private JobGeneratorSettings taskTablesSettings;

	@Test
	public void testSettings() {
		assertEquals("Invalid directory", "./l0_config/task_tables/", taskTablesSettings.getDirectoryoftasktables());
		assertEquals("Invalid directory", 500, taskTablesSettings.getScheduledfixedrate());
		assertEquals("Invalid maxnumberoftasktables", 2, taskTablesSettings.getMaxnumberoftasktables());
		assertEquals("Invalid maxnumberofjobs", 20, taskTablesSettings.getMaxnumberofjobs());
		assertEquals("Invalid waitmedataraw tempo", 2000, taskTablesSettings.getWaitprimarycheck().getTempo());
		assertEquals("Invalid waitmedataraw retries", 2, taskTablesSettings.getWaitprimarycheck().getRetries());
		assertEquals("Invalid waitmedataraw tempo", 3000, taskTablesSettings.getWaitmetadatainput().getTempo());
		assertEquals("Invalid waitmedatainput retries", 5, taskTablesSettings.getWaitmetadatainput().getRetries());
		assertEquals("Invalid default output family", "L0_ACN", taskTablesSettings.getDefaultoutputfamily());
		assertEquals("Invalid size of output families", 18, taskTablesSettings.getOutputfamilies().size());
		assertEquals("Invalid 1st output family", ProductFamily.L0_PRODUCT, taskTablesSettings.getOutputfamilies().get("SM_RAW__0S"));
		assertEquals("Invalid directory", ProductFamily.L0_REPORT, taskTablesSettings.getOutputfamilies().get("REP_EFEP_"));
		assertEquals("Invalid directory", ProductFamily.L1_PRODUCT, taskTablesSettings.getOutputfamilies().get("IW_GRDH_1S"));
		assertEquals("Invalid directory", ProductFamily.L1_ACN, taskTablesSettings.getOutputfamilies().get("IW_GRDH_1A"));
		assertEquals("Invalid size of output familiestype overlap", 4, taskTablesSettings.getTypeOverlap().size());
		assertEquals("Invalid value of output familiestype overlap", 7.4F , taskTablesSettings.getTypeOverlap().get("IW").floatValue(), 0);
		assertEquals("Invalid size of output typeslicelength overlap", 4, taskTablesSettings.getTypeSliceLength().size());
		assertEquals("Invalid value of output familiestype overlap", 0.0F, taskTablesSettings.getTypeSliceLength().get("WM").floatValue(), 0);
		assertEquals("Invalid size of link-producttype-metadataindex", 2, taskTablesSettings.getLinkProducttypeMetadataindex().size());
		assertEquals("Invalid value of link-producttype-metadataindex", "AUX_TUT", taskTablesSettings.getLinkProducttypeMetadataindex().get("AUX_TUTU"));
		
	}

}
