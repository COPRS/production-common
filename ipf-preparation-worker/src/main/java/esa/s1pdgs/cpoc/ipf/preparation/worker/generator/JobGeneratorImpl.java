package esa.s1pdgs.cpoc.ipf.preparation.worker.generator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.ipf.preparation.worker.appcat.AppCatAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.generator.state.JobGenerationStateTransitions;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.publish.Publisher;
import esa.s1pdgs.cpoc.ipf.preparation.worker.query.AuxQueryHandler;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public final class JobGeneratorImpl implements JobGenerator {
	private final TaskTableAdapter tasktableAdapter;
	private final ProductTypeAdapter typeAdapter;	
    private final AppCatAdapter appCat;
    private final ProcessSettings settings;
    private final ErrorRepoAppender errorAppender;
    private final Publisher publisher;
    private final List<List<String>> tasks;
    private final AuxQueryHandler auxQueryHandler;
        
	public JobGeneratorImpl(
			final TaskTableAdapter tasktableAdapter,
			final ProductTypeAdapter typeAdapter, 
			final AppCatAdapter appCat,
			final ProcessSettings settings,
			final ErrorRepoAppender errorAppender,
			final Publisher publisher,
			final List<List<String>> tasks,
			final AuxQueryHandler auxQueryHandler
	) {
		this.tasktableAdapter = tasktableAdapter;
		this.typeAdapter = typeAdapter;
		this.appCat = appCat;
		this.settings = settings;
		this.errorAppender = errorAppender;
		this.publisher = publisher;
		this.tasks = tasks;
		this.auxQueryHandler = auxQueryHandler;				
	}

	@Override
	public final void run() {
		try {
			final AppDataJob job = appCat.next(tasktableAdapter.file().getName());
			if (job == null) {
				LOGGER.trace("Found no applicable job to handle");
				return;
			}
			try {			
				LOGGER.debug("Trying job generation for appDataJob {}", job.getId());
				final AppDataJobGeneration oldGen = new AppDataJobGeneration(job.getGeneration());
				final JobGen jobGenNew = JobGenerationStateTransitions.ofInputState(oldGen.getState())
						.performTransitionOn(newJobGenFor(job));						
				appCat.update(oldGen, jobGenNew.job());
			}
			catch (final Exception e) {
				final Throwable error = Exceptions.unwrap(e);
				
				final List<GenericMessageDto<?>> messages = new ArrayList<>(job.getMessages());
				
				final String ids = messages.stream()
						.map(m -> String.valueOf(m.getId()))
						.collect(Collectors.joining(", "));	
	
				// 751: Error handling of job comprises of sending all messages of this job to the failedRequest repo
				LOGGER.error("Error on handling job {} and creating failed request for it's messages {}: {}",
						job.getId(), ids, LogUtils.toString(error));

				errorAppender.send(new FailedProcessingDto(
						settings.getHostname(), 
						new Date(), 
						LogUtils.toString(error), 
						messages
				));
				appCat.terminate(job);	
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

	private JobGen newJobGenFor(final AppDataJob job) {
		return new JobGen(
				job, 
				typeAdapter, 
				tasks,
				tasktableAdapter, 
				auxQueryHandler, 
				tasktableAdapter.newJobOrder(settings), 
				publisher
		);
	}
}
