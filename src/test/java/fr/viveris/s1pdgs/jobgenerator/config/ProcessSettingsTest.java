package fr.viveris.s1pdgs.jobgenerator.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import fr.viveris.s1pdgs.jobgenerator.config.ProcessSettings;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class ProcessSettingsTest {

	@Autowired
	private ProcessSettings l0ProcessSettings;

	// Embedded KAFKA
	@ClassRule
	public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, "t-pdgs-l0-jobs");

	@Test
	public void testSettings() {
		assertEquals("INFO", l0ProcessSettings.getLoglevelstderr());
		assertEquals("DEBUG", l0ProcessSettings.getLoglevelstdout());
		assertEquals("WILE", l0ProcessSettings.getProcessingstation());
		
		assertTrue(2 == l0ProcessSettings.getParams().size());
		assertTrue(l0ProcessSettings.getParams().containsKey("Processing_Mode"));
		assertTrue(l0ProcessSettings.getParams().containsKey("Timeout"));
		assertEquals("NRT", l0ProcessSettings.getParams().get("Processing_Mode"));
		assertEquals("10", l0ProcessSettings.getParams().get("Timeout"));
		
		assertTrue(5 == l0ProcessSettings.getOutputregexps().size());
		assertTrue(l0ProcessSettings.getOutputregexps().containsKey("AN_RAW__0S"));
		assertTrue(l0ProcessSettings.getOutputregexps().containsKey("REP_EFEP_"));
		assertEquals("^S1[A-B]_N[1-6]_RAW__0S.*$", l0ProcessSettings.getOutputregexps().get("AN_RAW__0S"));
		assertEquals("^S1[A|B|_]_OPER_REP_PASS.*.EOF$", l0ProcessSettings.getOutputregexps().get("REP_EFEP_"));
	}

}
