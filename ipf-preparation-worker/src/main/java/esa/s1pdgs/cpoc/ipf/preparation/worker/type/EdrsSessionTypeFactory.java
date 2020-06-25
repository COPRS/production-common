package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import java.io.File;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobsGeneratorFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobsGeneratorFactory.JobGenType;
import esa.s1pdgs.cpoc.ipf.preparation.worker.service.JobDispatcher;
import esa.s1pdgs.cpoc.ipf.preparation.worker.service.JobGenerator;
import esa.s1pdgs.cpoc.ipf.preparation.worker.service.XmlConverter;

public class EdrsSessionTypeFactory implements ProductTypeFactory {	
	
	final ProcessSettings processSettings,
	final JobsGeneratorFactory factory, 
	final ThreadPoolTaskScheduler taskScheduler,
	final XmlConverter xmlConverter,

	@Override
	public JobGenType type() {
		return JobGenType.LEVEL_0;
	}
	


	@Override
	public JobDispatcher newJobDispatcher() {
		return new L0AppJobDispatcher(settings, processSettings, factory, taskScheduler, appCatClient);
	}


	@Override
	public JobGenerator newJobGenerator(final File taskTableFile) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
