package esa.s1pdgs.cpoc.dlq.manager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest	
@ComponentScan("esa.s1pdgs.cpoc")
public class TestApplication {

	@Test
	public void applicationContextTest() {
		Application.main(new String[] {});
	}
}