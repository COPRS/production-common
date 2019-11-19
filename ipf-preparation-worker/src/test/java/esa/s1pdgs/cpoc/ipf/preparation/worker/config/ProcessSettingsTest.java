package esa.s1pdgs.cpoc.ipf.preparation.worker.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class ProcessSettingsTest {

	@Autowired
	private ProcessSettings l0ProcessSettings;

	@Test
	public void testSettings() {
        assertEquals(ApplicationLevel.L0, l0ProcessSettings.getLevel());
        assertEquals("hostname", l0ProcessSettings.getHostname());
        
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
