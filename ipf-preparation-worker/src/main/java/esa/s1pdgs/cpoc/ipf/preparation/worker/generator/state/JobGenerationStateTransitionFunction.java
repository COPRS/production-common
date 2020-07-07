package esa.s1pdgs.cpoc.ipf.preparation.worker.generator.state;

import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;

@FunctionalInterface
public interface JobGenerationStateTransitionFunction 
{
	public JobGen performTransitionOn(final JobGen job) throws JobStateTransistionFailed;		
}
