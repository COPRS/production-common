package esa.s1pdgs.cpoc.ipf.preparation.worker.generator.state;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;

public enum JobGenerationStateTransitions implements JobGenerationStateTransitionFunction {
	INITIAL_2_PRIMARYCHECK(
			AppDataJobGenerationState.INITIAL, 
			AppDataJobGenerationState.PRIMARY_CHECK, 
			j -> j.mainInputSearch(AppDataJobGenerationState.PRIMARY_CHECK)
	),
	PRIMARYCHECK_2_READY(
			AppDataJobGenerationState.PRIMARY_CHECK, 
			AppDataJobGenerationState.READY, 
			j -> j.auxSearch(AppDataJobGenerationState.READY)
	),
	READY_2_SENT(
			AppDataJobGenerationState.READY, 
			AppDataJobGenerationState.SENT,  
			j -> j.send(AppDataJobGenerationState.SENT)
	);
	
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
	public final void performTransitionOn(final JobGenerationTransition job) throws JobStateTransistionFailed {
		LOGGER.debug("Performing {} on {}", toDescription(), job);
		try {
			function.performTransitionOn(job);
			LOGGER.info("{} {} succeeded", job, toDescription());
		} catch (final JobStateTransistionFailed e) {
			LOGGER.info("Prerequisites for {} not met for {}: {}",  toDescription(), job, e.getMessage());
			throw e;
		}
	}
}
