package fr.viveris.s1pdgs.jobgenerator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher.AbstractJobsDispatcher;

@Configuration
@ComponentScan(basePackages = "fr.viveris.s1pdgs.jobgenerator.service", basePackageClasses = { AbstractJobsDispatcher.class })
public class ThreadPoolTaskSchedulerConfig {

	/**
	 * URI of KAFKA cluster
	 */
	@Value("${job-generator.maxnumberoftasktables}")
	protected int poolSize;

	@Bean(name="jobGenerationTaskScheduler", destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(poolSize);
        threadPoolTaskScheduler.setThreadNamePrefix("JobGenerationTaskScheduler");
        return threadPoolTaskScheduler;
    }

}
