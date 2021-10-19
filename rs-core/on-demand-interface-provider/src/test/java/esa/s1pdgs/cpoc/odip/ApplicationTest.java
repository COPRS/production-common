package esa.s1pdgs.cpoc.odip;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.odip.Application;
import esa.s1pdgs.cpoc.odip.config.OdipConfigurationProperties;


@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class ApplicationTest {

	@Autowired
	private OdipConfigurationProperties properties;
	
	@Test
	public void applicationContextTest() {
		Application.main(new String[] {});
		
		System.out.println(properties);
	}
}
