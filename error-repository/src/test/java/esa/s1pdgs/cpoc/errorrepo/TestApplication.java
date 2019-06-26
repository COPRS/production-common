package esa.s1pdgs.cpoc.errorrepo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, controlledShutdown = false, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class TestApplication {	
	@Test
	public void applicationContextTest() throws InterruptedException {
		Application.main(new String[] {});
	}
}
