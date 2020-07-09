package esa.s1pdgs.cpoc.ipf.preparation.worker.model;

import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.ipf.preparation.worker.generator.state.JobStateTransistionFailed;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderSensingTime;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.publish.Publisher;
import esa.s1pdgs.cpoc.ipf.preparation.worker.query.AuxQueryHandler;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;

public class JobGen {	
	private final AppDataJob job;
	private final ProductTypeAdapter typeAdapter;
	private final List<List<String>> tasks;
	private final TaskTableAdapter taskTableAdapter;
    private final AuxQueryHandler auxQueryHandler;
    private final JobOrder jobOrder;
    private final Publisher publisher;
			
	public JobGen(
			final AppDataJob job, 
			final ProductTypeAdapter typeAdapter, 
			final List<List<String>> tasks,
			final TaskTableAdapter taskTableAdapter,
			final AuxQueryHandler auxQueryHandler,
			final JobOrder jobOrder,
			final Publisher publisher
			
	) {
		this.job = job;
		this.typeAdapter = typeAdapter;
		this.tasks = tasks;
		this.taskTableAdapter = taskTableAdapter;
		this.auxQueryHandler = auxQueryHandler;
		this.jobOrder = jobOrder;
		this.publisher = publisher;
	}

	public String id() {
		return String.valueOf(job.getId());
	}
	
	public final AppDataJob job() {
		return job;
	}
	
	public final JobOrder jobOrder() {
		return jobOrder;
	}
	
	public final List<List<String>> tasks() {
		return tasks;
	}
	
	public final TaskTableAdapter taskTableAdapter() {
		return taskTableAdapter;
	}
	
	public final String productName() {
		return job.getProduct().getProductName();
	}
	
	public final String processMode() {
		return job.getProduct().getProcessMode();
	}
	
	public AppDataJobGenerationState state() {
		return job.getGeneration().getState();
	}
	
	public void state(final AppDataJobGenerationState state) {
		job.getGeneration().setState(state);
	}
	
	public AppDataJobGeneration generation() {
		return job.getGeneration();
	}
	
	public final String timeliness() {
		try {
			return String.valueOf(job.getMessages().get(0).getBody().getMetadata().getOrDefault("timeliness", ""));
		} catch (final Exception e) {
			// fall through: just don't care if the mess above fails and return an empty value
		}
		return "";
	}

	public final void buildJobOrder(final String workingDir) {
		jobOrder.getProcs().stream()
			.filter(proc -> proc != null && !CollectionUtils.isEmpty(proc.getInputs()))
			.flatMap(proc -> proc.getInputs().stream()).forEach(input -> {
				input.getFilenames().forEach(filename -> filename.setFilename(workingDir + filename.getFilename()));
				input.getTimeIntervals().forEach(interval -> interval.setFileName(workingDir + interval.getFileName()));
			});
		jobOrder.getProcs().stream()
				.filter(proc -> proc != null && !CollectionUtils.isEmpty(proc.getOutputs()))
				.flatMap(proc -> proc.getOutputs().stream()).forEach(output -> output.setFileName(workingDir + output.getFileName()));

		// Apply implementation build job
		jobOrder.getConf().setSensingTime(new JobOrderSensingTime(
						DateUtils.convertToAnotherFormat(job.getProduct().getStartTime(),
								AppDataJobProduct.TIME_FORMATTER, JobOrderSensingTime.DATETIME_FORMATTER),
						DateUtils.convertToAnotherFormat(job.getProduct().getStopTime(),
								AppDataJobProduct.TIME_FORMATTER, JobOrderSensingTime.DATETIME_FORMATTER)));
		typeAdapter.customJobOrder(this);
	}
	
	public final void customJobDto(final IpfExecutionJob execJob) {
		typeAdapter.customJobDto(this, execJob);
	}
	

	public final JobGen mainInputSearch() throws JobStateTransistionFailed {
		return perform(typeAdapter.mainInputSearch(this), "querying input " + productName());
	}
	
	public final JobGen auxSearch() throws JobStateTransistionFailed {	
		return perform(auxQueryHandler.queryFor(this), "querying required AUX");
	}
	
	public final JobGen send() throws JobStateTransistionFailed {
		// FIXME applied same dirty workaround viveris did, auxSearch needs to be cleaned up
		return perform(publisher.send(auxSearch()), "publishing Job");
	}
	
	private JobGen perform(final Callable<JobGen> command, final String name) throws JobStateTransistionFailed {
		try {
			return command.call();
		} 
		// expected
		catch (final IpfPrepWorkerInputsMissingException e) {
			// TODO once there is some time for refactoring, cleanup the created error message of 
			// IpfPrepWorkerInputsMissingException to be more descriptive
			throw new JobStateTransistionFailed(e.getLogMessage());
		}
		catch (final Exception e) {
			throw new RuntimeException(
					String.format("Fatal error on %s: %s", name, Exceptions.messageOf(e)),
					e
			);
		}
	}
}
