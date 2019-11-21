package esa.s1pdgs.cpoc.scaler;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.scaler.Application;

/**
 * Test the properties of development
 * 
 * @author Viveris Technologies
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class ApplicationTest {

	/**
	 * Embedded Kafka
	 */
	@ClassRule
	public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, "t-pdgs-aio-execution-jobs");

	@Test
	public void applicationContextTest() {
		Application.main(new String[] {});
	}
}
