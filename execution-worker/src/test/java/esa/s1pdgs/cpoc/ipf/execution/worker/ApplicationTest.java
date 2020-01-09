package esa.s1pdgs.cpoc.ipf.execution.worker;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.execution.worker.Application;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class ApplicationTest {
    
	@Test
	public void applicationContextTest() {
		Application.main(new String[] {});
	}
}
