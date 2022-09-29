
package esa.s1pdgs.cpoc.mdc.worker.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("new")
public class SearchControllerConfigTest {
	
	@Autowired
	private SearchControllerConfig config;

	@Test
	public void testConfig() {
		System.out.println(config.getAuxPatternConfig());
	}
}
