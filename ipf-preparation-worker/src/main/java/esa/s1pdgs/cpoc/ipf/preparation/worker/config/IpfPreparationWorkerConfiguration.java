package esa.s1pdgs.cpoc.ipf.preparation.worker.config;

import java.time.LocalDateTime;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings.InputWaitingConfig;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutCheckerImpl;

@Configuration
public class IpfPreparationWorkerConfiguration {
	private final IpfPreparationWorkerSettings ipfPreparationWorkerSettings;
	
	@Autowired
	public IpfPreparationWorkerConfiguration(final IpfPreparationWorkerSettings ipfPreparationWorkerSettings) {
		this.ipfPreparationWorkerSettings = ipfPreparationWorkerSettings;
	}
		
	@Bean
	public Function<TaskTable, InputTimeoutChecker> timeoutCheckerFor() {
		return t -> inputWaitTimeoutFor(t);		
	}

	private final InputTimeoutChecker inputWaitTimeoutFor(final TaskTable taskTable) {
		for (final InputWaitingConfig config : ipfPreparationWorkerSettings.getInputWaiting()) {
			if (taskTable.getProcessorName().equals(config.getProcessorNameRegexp()) &&
				taskTable.getVersion().matches(config.getProcessorVersionRegexp())) 
			{			
				return new InputTimeoutCheckerImpl(config, () -> LocalDateTime.now());				
			}					
		}
		// default: always time out
		return (timeliness, input) -> true;
	}
}
