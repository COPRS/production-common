package esa.s1pdgs.cpoc.jobgenerator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import esa.s1pdgs.cpoc.jobgenerator.tasks.dispatcher.AbstractJobsDispatcher;

@Configuration
@ComponentScan(basePackages = "esa.s1pdgs.cpoc.jobgenerator.service", basePackageClasses = { AbstractJobsDispatcher.class })
public class ThreadPoolTaskSchedulerConfig {


	@Value("${job-generator.maxnboftasktable}")
	protected int poolSize;

	@Bean(name="jobGenerationTaskScheduler", destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(poolSize);
        threadPoolTaskScheduler.setThreadNamePrefix("JobGenerationTaskScheduler");
        return threadPoolTaskScheduler;
    }

}
