package esa.s1pdgs.cpoc.prip.worker;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.prip.worker.Application;


@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class TestApplication {

	@Test
	public void applicationContextTest() throws InterruptedException {
		Application.main(new String[] {});
	}
}