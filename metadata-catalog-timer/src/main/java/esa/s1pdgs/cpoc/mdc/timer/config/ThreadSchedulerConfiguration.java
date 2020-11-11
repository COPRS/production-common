package esa.s1pdgs.cpoc.mdc.timer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ThreadSchedulerConfiguration {
	
	private MetadataCatalogTimerSettings settings;
	
	public ThreadSchedulerConfiguration(final MetadataCatalogTimerSettings settings) {
		this.settings = settings;
	}

	@Bean(name = "catEventTaskScheduler", destroyMethod = "shutdown")
	public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
		final ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		if (settings.getConfig().entrySet().size() == 0) {
			throw new IllegalStateException("No timer based triggers defined");
		}
		threadPoolTaskScheduler.setPoolSize(settings.getConfig().entrySet().size());
		threadPoolTaskScheduler.setThreadNamePrefix("catEventTaskScheduler");
		return threadPoolTaskScheduler;
	}
}
