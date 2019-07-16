package esa.s1pdgs.cpoc.inbox;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestApplication {		
	@Test
	public void applicationContextTest() throws InterruptedException {
		Application.main(new String[] {});
	}
}
