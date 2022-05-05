package esa.s1pdgs.cpoc.datalifecycle.worker;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

public class TestApplication {

	@Test
	public void applicationContextTest() {
		Application.main(new String[] {});
	}
}
