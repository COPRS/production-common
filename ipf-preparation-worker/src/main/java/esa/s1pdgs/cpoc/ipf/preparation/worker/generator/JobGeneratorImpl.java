package esa.s1pdgs.cpoc.ipf.preparation.worker.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.ipf.preparation.worker.appcat.AppCatJobService;
import esa.s1pdgs.cpoc.ipf.preparation.worker.appcat.AppCatJobUpdateFailed;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.generator.state.JobGenerationStateTransitions;
import esa.s1pdgs.cpoc.ipf.preparation.worker.generator.state.JobGenerationTransition;
import esa.s1pdgs.cpoc.ipf.preparation.worker.generator.state.JobStateTransistionFailed;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.publish.Publisher;
import esa.s1pdgs.cpoc.ipf.preparation.worker.query.AuxQuery;
import esa.s1pdgs.cpoc.ipf.preparation.worker.query.AuxQueryHandler;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public final class JobGeneratorImpl implements JobGenerator {	
	final class JobGeneration implements JobGenerationTransition {
		private final AppDataJob job;
	    
		JobGeneration(final AppDataJob job) {
			this.job = job;
		}

		@Override
		public final void mainInputSearch(final AppDataJobGenerationState outputState) throws JobStateTransistionFailed {			
			AppDataJobGenerationState newState = job.getGeneration().getState();
			
			Product queried = null;
			try {
				queried = perform(
						() -> typeAdapter.mainInputSearch(job),
						"querying input " + job.getProductName()
				);
				job.setProduct(queried.toProduct());	
				
				performVoid(
					() -> typeAdapter.validateInputSearch(job), 
					"validating availability of input products for " + job.getProductName()
				);
				newState = outputState;
			}
			finally
			{
				appCatService.updateProduct(job.getId(), queried, newState);
			}			
		}
		
		@Override
		public final void auxSearch(final AppDataJobGenerationState outputState) throws JobStateTransistionFailed {
			AppDataJobGenerationState newState = job.getGeneration().getState();			
			final AuxQuery auxQuery = auxQueryHandler.queryFor(job);
			
			List<AppDataJobTaskInputs> queried = Collections.emptyList();
		
			try {
				queried = perform(() -> auxQuery.queryAux(), "querying required AUX");
				job.setAdditionalInputs(queried);
				
				performVoid(
					() -> auxQuery.validate(job), 
					"validating availability of AUX for " + job.getProductName()
				);
				newState = outputState;
			}
			finally
			{
				appCatService.updateAux(job.getId(), queried, newState);
			}				
		}
		
		@Override
		public final void send(final AppDataJobGenerationState outputState) throws JobStateTransistionFailed {
			AppDataJobGenerationState newState = job.getGeneration().getState();						
			try {
				performVoid(
					() -> publisher.send(job), 
					"publishing Job for " + job.getProductName()
				);
				newState = outputState;
			}
			finally
			{
				appCatService.updateSend(job.getId(), newState);
			}
		}
		
		@Override
		public final String toString() {
			return "AppDataJob " + job.getId();
		}
	}
	
	private final TaskTableAdapter tasktableAdapter;
	private final ProductTypeAdapter typeAdapter;	
	private final AppCatJobService appCatService;
    private final ProcessSettings settings;
    private final ErrorRepoAppender errorAppender;
    private final Publisher publisher;
    private final AuxQueryHandler auxQueryHandler;
        
	public JobGeneratorImpl(
			final TaskTableAdapter tasktableAdapter,
			final ProductTypeAdapter typeAdapter, 
			final AppCatJobService appCatService,
			final ProcessSettings settings,
			final ErrorRepoAppender errorAppender,
			final Publisher publisher,
			final AuxQueryHandler auxQueryHandler
	) {
		this.tasktableAdapter = tasktableAdapter;
		this.typeAdapter = typeAdapter;
		this.appCatService = appCatService;
		this.settings = settings;
		this.errorAppender = errorAppender;
		this.publisher = publisher;
		this.auxQueryHandler = auxQueryHandler;				
	}

	@Override
	public final void run() {
		try {
			final String tasktableName = tasktableAdapter.file().getName();
			
			final AppDataJob job = appCatService.next(tasktableName);
			if (job == null) {
				LOGGER.trace("Found no applicable job to handle for tasktable {}", tasktableName);
				return;
			}
			try {			
				LOGGER.debug("Trying job generation for appDataJob {}", job.getId());
				JobGenerationStateTransitions.ofInputState(job.getGeneration().getState())
						.performTransitionOn(new JobGeneration(job));
			}
			catch (final Exception e) {
				final Throwable error = Exceptions.unwrap(e);
				
				final List<GenericMessageDto<?>> messages = new ArrayList<>(job.getMessages());
				
				final String ids = messages.stream()
						.map(m -> String.valueOf(m.getId()))
						.collect(Collectors.joining(", "));	
	
				// 751: Adding just one of the messages should be sufficient to re-create the job on restart
				// scenario
				LOGGER.error("Error on handling job {}. Failed request will be created for one of its messages {}. Error was: {}",
						job.getId(), ids, LogUtils.toString(error));

				errorAppender.send(new FailedProcessingDto(
						settings.getHostname(), 
						new Date(), 
						LogUtils.toString(error), 
						messages.get(0)
				));
				appCatService.terminate(job);	
			}		
			// TODO check if it makes sense to evaluate the error counter here to limit the amount of
			// failed transition attempts	
		} 
		// on app-cat connection issues
		catch (final AbstractCodedException e) {
			// no use for further actions here as app-cat is mandatory for operation. Hence, we simply dump the message
			// and wait for the next attempt
			final String errorMessage = Exceptions.messageOf(e);
			LOGGER.error("Omitting job generation attempt due to error on app-cat access: {}", errorMessage);
		}
		// on any other error
		catch (final Exception e) {
			// also skip to t iteration here as it is likely induced by temporal problems (e.g. kafka down)
			final String errorMessage = Exceptions.messageOf(e);
			LOGGER.error("Omitting job generation attempt due to unexpected error: {}", errorMessage);
		}
	}
	
	
	private static final void performVoid(final ThrowingRunnable command, final String name) throws JobStateTransistionFailed {
		perform(
				() -> {command.run(); return null;}, 
				name
		);
	}
	
	private static final <E> E perform(final Callable<E> command, final String name) throws JobStateTransistionFailed {
		try {
			return command.call();
		} 
		// expected on failed transition
		catch (final IpfPrepWorkerInputsMissingException e) {
			// TODO once there is some time for refactoring, cleanup the created error message of 
			// IpfPrepWorkerInputsMissingException to be more descriptive
			throw new JobStateTransistionFailed(e.getLogMessage());
		}
		// expected on updating AppDataJob in persistence -> simply retry next time
		catch (final AppCatJobUpdateFailed e) {
			throw new JobStateTransistionFailed(
					String.format("Error on persisting change of '%s': %s", name, Exceptions.messageOf(e)), 
					e
			);
		}
		catch (final Exception e) {
			throw new RuntimeException(
					String.format("Fatal error on %s: %s", name, Exceptions.messageOf(e)),
					e
			);
		}
	}
}
