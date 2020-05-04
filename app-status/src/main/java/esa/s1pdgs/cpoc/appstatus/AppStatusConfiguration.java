package esa.s1pdgs.cpoc.appstatus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppStatusConfiguration {	
	@Bean
	@ConditionalOnMissingBean
	public AppStatus defaultAppStatusImpl(
			@Value("${status.max-error-counter-processing:100}") final int maxErrorCounterProcessing, 
			@Value("${status.max-error-counter-mqi:100}") final int maxErrorCounterNextMessage,
			@Value("${status.block-system-exit:false}") final boolean blockSystemExit
	) {
		return new DefaultAppStatusImpl(
				maxErrorCounterProcessing, 
				maxErrorCounterNextMessage,
				systemExitProvider(blockSystemExit)
		);
	}
	
	private Runnable systemExitProvider(final boolean blockSystemExit) {
		// allow preventing system exit being called for e.g. unit tests
		if (blockSystemExit) {
			return ()->{};
		}
		return () -> System.exit(0);
	}
}
