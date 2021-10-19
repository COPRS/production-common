package esa.s1pdgs.cpoc.ipf.preparation.worker.generator.state;

@FunctionalInterface
public interface JobGenerationStateTransitionFunction {
	public void performTransitionOn(final JobGenerationTransition job) throws JobStateTransistionFailed;		
}
