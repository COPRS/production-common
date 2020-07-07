package esa.s1pdgs.cpoc.ipf.preparation.worker.generator;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;

@Component
public class GracePeriodHandler {
	
	private final IpfPreparationWorkerSettings settings;

	@Autowired
	public GracePeriodHandler(final IpfPreparationWorkerSettings settings) {
		this.settings = settings;
	}
	
	public final boolean isWithinGracePeriod(final AppDataJobGeneration jobGen) {
		final Date gracePeriodOverAt = new Date(jobGen.getLastUpdateDate().getTime() + getGracePeriodMillis(jobGen));
		return new Date().before(gracePeriodOverAt);
	}
	
	private final long getGracePeriodMillis(final AppDataJobGeneration jobGen) {
		if (jobGen.getState() == AppDataJobGenerationState.INITIAL) {
			return settings.getWaitprimarycheck().getTempo();
		} 
		if (jobGen.getState() == AppDataJobGenerationState.PRIMARY_CHECK) {
			return settings.getWaitmetadatainput().getTempo();
		} 
		// default: no grace period
		return 0L;
	}
}
