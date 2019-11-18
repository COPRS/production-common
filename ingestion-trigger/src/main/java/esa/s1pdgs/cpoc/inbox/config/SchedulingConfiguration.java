package esa.s1pdgs.cpoc.inbox.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import esa.s1pdgs.cpoc.inbox.InboxPollingService;
import esa.s1pdgs.cpoc.inbox.PollingTrigger;

// make scheduling configurable via property to allow disabling it in unit tests
@ConditionalOnProperty(value = "scheduling.enable", havingValue = "true", matchIfMissing = true)
@EnableScheduling
@Configuration
public class SchedulingConfiguration {
	private final InboxPollingService pollingService;
	
	@Autowired
	public SchedulingConfiguration(InboxPollingService pollingService) {
		this.pollingService = pollingService;
	}

	@Bean
	public PollingTrigger pollingTrigger()
	{
		return new PollingTrigger(pollingService);
	}
}
