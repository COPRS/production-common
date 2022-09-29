package esa.s1pdgs.cpoc.mqi.model.queue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class TestIpfPreparationJob {	
	@Test
	public void checkEquals() {
		EqualsVerifier.forClass(IpfPreparationJob.class)
			.usingGetClass()
			.suppress(Warning.NONFINAL_FIELDS)
			.verify();
	}
}
