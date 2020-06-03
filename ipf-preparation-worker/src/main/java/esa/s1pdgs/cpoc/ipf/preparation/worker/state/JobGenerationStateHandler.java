package esa.s1pdgs.cpoc.ipf.preparation.worker.state;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGeneration;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

public class JobGenerationStateHandler {
	
	@FunctionalInterface
	public static interface JobGenerationStateTransitionFunction 
	{
		public JobGeneration performTransitionOn(final JobGeneration job) throws JobStateTransistionFailed, Exception;		
	}
	
	public enum JobGenerationStateTransitions {
		INITIAL_2_PRIMARYCHECK(AppDataJobGenerationState.INITIAL, AppDataJobGenerationState.PRIMARY_CHECK),
		PRIMARYCHECK_2_READY(AppDataJobGenerationState.PRIMARY_CHECK, AppDataJobGenerationState.READY),
		READY_2_SENT(AppDataJobGenerationState.READY, AppDataJobGenerationState.SENT),
		NULL(null,null);
		
		private final AppDataJobGenerationState inputState;
		private final AppDataJobGenerationState outputState;
		
		private JobGenerationStateTransitions(
				final AppDataJobGenerationState inputState,
				final AppDataJobGenerationState outputState
		) {
			this.inputState = inputState;
			this.outputState = outputState;
		}
		
		public static JobGenerationStateTransitions ofInputState(final AppDataJobGenerationState inputState) {
			for (final JobGenerationStateTransitions transition : JobGenerationStateTransitions.values()) {
				if (inputState == transition.inputState) {
					return transition;
				}
			}
			return NULL;
		}

		public final AppDataJobGenerationState getInputState() {
			return inputState;
		}

		public final AppDataJobGenerationState getOutputState() {
			return outputState;
		}	
		
		public final String toDescription() {
			return "state '" + inputState + "' -> state '" + outputState + "'";
		}
	}
	
	static final Logger LOGGER = LogManager.getLogger(JobGenerationStateHandler.class);

	private final Map<JobGenerationStateTransitions, JobGenerationStateTransitionFunction> transitions;
	private final AppCatalogJobClient<CatalogEvent> appCatClient;

	public final JobGeneration performTransitionFor(final JobGeneration job) throws JobStateTransistionFailed, Exception {
		final AppDataJobGenerationState inputState = job.getGeneration().getState();
		final JobGenerationStateTransitions transition = JobGenerationStateTransitions.ofInputState(inputState);
		if (transition == null) {
			
		}
		final JobGenerationStateTransitionFunction transitionFunction = transitions.get(transition);
		if (transitionFunction == null) {
			
		}
		
		@SuppressWarnings("unchecked")
		final AppDataJob<CatalogEvent> appDataJob = job.getAppDataJob();
		LOGGER.info("Start job {} state transition, {}", appDataJob.getId(), transition.toDescription());
		
		try {
			final JobGeneration updatedJob = transitionFunction.performTransitionOn(job);
			
			// FIXME: handle updates internally
			// This is pretty dirty here but we have to make sure that exception scenario
			// below also sees the updated AppDataJob
			job.setAppDataJob(updatedJob.getAppDataJob());
			
			
			updateState(job, transition.getOutputState());
			LOGGER.info("End job {} state transition, {}", appDataJob.getId(), transition.toDescription());
			return updatedJob;
		}
		catch (final JobStateTransistionFailed e) {
			// keep old state
			LOGGER.info(
					"Prerequisites for job {} state transition {} not met - staying in state {}: {}",
					appDataJob.getId(), 
					transition.toDescription(),
					transition.getInputState(), 
					e.getMessage()
			);
			updateState(job, transition.getInputState());			
		}
		// everything else is considered fatal
		catch (final Exception e) {
			throw new RuntimeException(
					String.format(format, args),
					e
			);		
		}
		return job;
	}
	
	private JobGeneration updateState(
			final JobGeneration job, 
			final AppDataJobGenerationState newState
	) throws AbstractCodedException {		
		LOGGER.info("Job generation before update: {} - {} - {} - {}", job.getAppDataJob().getId(),
				job.getGeneration().getTaskTable(), newState, job.getGeneration());
		
		final AppDataJob<CatalogEvent> modifiedJob = appCatClient.patchTaskTableOfJob(
				job.getAppDataJob().getId(),
				job.getGeneration().getTaskTable(),
				newState
		);

		if (modifiedJob == null) {
			throw new InternalErrorException("Catalog query returned null");
		}

		LOGGER.info("Modified job generations: {}", modifiedJob.getGenerations());
		final AppDataJob<CatalogEvent> updatedJob = new AppDataJob<>()
		job.updateAppDataJob(modifiedJob, taskTableXmlName);
		LOGGER.info("Job generation after update: {}", job.getGeneration());

		// Log functional logs, not clear when this is called
		if (job.getAppDataJob().getState() == AppDataJobState.TERMINATED) {
			final AppDataJob<CatalogEvent> jobDto = job.getAppDataJob();
			final List<String> taskTables = jobDto.getGenerations().stream().map(g -> g.getTaskTable())
					.collect(Collectors.toList());

			LOGGER.info("{} [s1pdgsTask {}JobGeneration] [STOP OK] [productName {}] [outputs {}] Job finished",
					this.prefixLogMonitor, this.taskTable.getLevel(), job.getAppDataJob().getProduct().getProductName(),
					taskTables);
		}
		LOGGER.debug("== Job order {} updated to state {}", job.getAppDataJob().getId(), newState);

	}
	
}
