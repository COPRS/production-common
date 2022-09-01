package de.werum.coprs.requestparkinglot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import de.werum.coprs.requestparkinglot.config.TestConfig;

@RunWith(SpringRunner.class)
@SpringBootTest	
@DirtiesContext
@ComponentScan({"de.werum.coprs", "esa.s1pdgs.cpoc"})
@Import(TestConfig.class)
@TestPropertySource(locations="classpath:random-mongodb-port.properties")
public class TestApplication {

	@Test
	public void applicationContextTest() {
		Application.main(new String[] {});
	}
}
