package esa.s1pdgs.cpoc.ipf.preparation.worker.generator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
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
		public final void mainInputSearch() throws JobStateTransistionFailed {
			perform(() -> {
						final Product queried = typeAdapter.mainInputSearch(job);
						appCatService.updateProduct(job.getId(), queried);
						return null;
					},
					"querying input " + job.getProductName()
			);
		}
		
		@Override
		public final void auxSearch() throws JobStateTransistionFailed {
			perform(() -> {
						final List<AppDataJobTaskInputs> queried = auxQueryHandler.queryFor(job);
						appCatService.updateAux(job.getId(), queried);
						return null;
					},	
					"querying required AUX"
			);
		}
		
		@Override
		public final void send() throws JobStateTransistionFailed {
			perform(() -> {
						publisher.send(job);
						return null;
					}, 
					"publishing Job"
			);
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
    private final List<List<String>> tasks;
    private final AuxQueryHandler auxQueryHandler;
        
	public JobGeneratorImpl(
			final TaskTableAdapter tasktableAdapter,
			final ProductTypeAdapter typeAdapter, 
			final AppCatJobService appCatService,
			final ProcessSettings settings,
			final ErrorRepoAppender errorAppender,
			final Publisher publisher,
			final List<List<String>> tasks,
			final AuxQueryHandler auxQueryHandler
	) {
		this.tasktableAdapter = tasktableAdapter;
		this.typeAdapter = typeAdapter;
		this.appCatService = appCatService;
		this.settings = settings;
		this.errorAppender = errorAppender;
		this.publisher = publisher;
		this.tasks = tasks;
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
				LOGGER.error("Error on handling job {} and creating failed request for one of its messages {}: {}",
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
