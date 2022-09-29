package esa.s1pdgs.cpoc.ingestion.trigger.config;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.function.context.PollableBean;

import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

@TestConfiguration
public class TestConfig {

	private static final Logger LOG = LoggerFactory.getLogger(TestConfig.class);
	
	// import environment proxy settings for downloading embedded mongodb-*.tgz
	{
		for (String protocol : List.of("http", "https")) {
			String value = System.getenv(protocol + "_proxy");
			if (null != value) {
				System.setProperty(protocol + ".proxyHost",
						value.substring(value.indexOf("://") + 3, value.lastIndexOf(":")));			
				System.setProperty(protocol + ".proxyPort",
						value.substring(value.lastIndexOf(":") + 1).replace("/", ""));			
			}
		}	
	}

	@PollableBean
	public Supplier<List<IngestionJob>> newInboxServiceNOP() {
		return new IngestionTriggerServiceNOP();
	}

	class IngestionTriggerServiceNOP implements Supplier<List<IngestionJob>> {
		@Override
		public List<IngestionJob> get() {
			LOG.debug("NOP background polling");
			return Collections.emptyList();
		}
	}
}
