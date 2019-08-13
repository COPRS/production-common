package esa.s1pdgs.cpoc.validation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.validation.config.ApplicationProperties;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class TestApplication {

	@Autowired
	ApplicationProperties properties;
	
	@Test
	public void applicationContextTest() throws InterruptedException {
		Application.main(new String[] {});
	}
}