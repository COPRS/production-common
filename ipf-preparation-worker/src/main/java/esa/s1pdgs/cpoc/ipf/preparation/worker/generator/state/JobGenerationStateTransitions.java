package esa.s1pdgs.cpoc.ipf.preparation.worker.generator.state;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;

public enum JobGenerationStateTransitions implements JobGenerationStateTransitionFunction {
	INITIAL_2_PRIMARYCHECK(AppDataJobGenerationState.INITIAL, AppDataJobGenerationState.PRIMARY_CHECK, j -> j.mainInputSearch()),
	PRIMARYCHECK_2_READY(AppDataJobGenerationState.PRIMARY_CHECK, AppDataJobGenerationState.READY, j -> j.auxSearch()),
	READY_2_SENT(AppDataJobGenerationState.READY, AppDataJobGenerationState.SENT,  j -> j.send());
	
	private static final Logger LOGGER = LogManager.getLogger(JobGenerationStateTransitions.class);
	
	private final AppDataJobGenerationState inputState;
	private final AppDataJobGenerationState outputState;
	private final JobGenerationStateTransitionFunction function;
	
	private JobGenerationStateTransitions(
			final AppDataJobGenerationState inputState,
			final AppDataJobGenerationState outputState,
			final JobGenerationStateTransitionFunction function
	) {
		this.inputState = inputState;
		this.outputState = outputState;
		this.function = function;
	}
	
	public static JobGenerationStateTransitions ofInputState(final AppDataJobGenerationState inputState) {
		for (final JobGenerationStateTransitions transition : JobGenerationStateTransitions.values()) {
			if (inputState == transition.inputState) {
				return transition;
			}
		}
		throw new IllegalArgumentException();
	}
	
	public final String toDescription() {
		return "transition [state '" + inputState + "' -> state '" + outputState + "']";
	}

	@Override
	public final JobGen performTransitionOn(final JobGen job) {
		LOGGER.debug("Performing {} on job {}", toDescription(), job.id());
		try {
			final JobGen newStateJobGen = function.performTransitionOn(job);
			newStateJobGen.state(outputState);
			LOGGER.info("Job {} {} succeeded", job.id(), toDescription());
			return newStateJobGen;
		} catch (final JobStateTransistionFailed e) {
			LOGGER.info("Prerequisites for {} not met for job {}: {}",  toDescription(), job.id(), e.getMessage());
			// fall through
		}
		// return unaltered job
		return job;
	}
}
