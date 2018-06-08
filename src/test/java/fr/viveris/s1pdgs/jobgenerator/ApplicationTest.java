package fr.viveris.s1pdgs.jobgenerator;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class ApplicationTest {

	/**
	 * Embedded Kafka
	 */
	@ClassRule
	public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, "t-pdgs-l0-jobs");

	@Test
	public void applicationContextTest() {
		Application.main(new String[] {});
	}
}
