package esa.s1pdgs.cpoc.reqrepo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 10, controlledShutdown = false, brokerProperties = {"listeners=PLAINTEXT://localhost:9093", "port=9093"})
public class TestApplication {		
    @Autowired
    private EmbeddedKafkaBroker kafkaEmbedded;
    
	@Test
	public void applicationContextTest() throws InterruptedException {
		Application.main(new String[] {});
	}
}
