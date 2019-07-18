package esa.s1pdgs.cpoc.inbox;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.inbox.config.InboxPollingConfigurationProperties;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class TestApplication {			
	@Autowired 
	private InboxPollingService service;
	
	@Test
	public void applicationContextTest() throws InterruptedException {
		Application.main(new String[] {});
		service.pollAll();
	}
}
