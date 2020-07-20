package esa.s1pdgs.cpoc.ipf.preparation.worker.model;

import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.publish.Publisher;
import esa.s1pdgs.cpoc.ipf.preparation.worker.query.AuxQueryHandler;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;

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

	public final ProductTypeAdapter typeAdapter() {
		return typeAdapter;
	}

	public final String productName() {
		return job.getProductName();
	}
	
	public final String processMode() {
		return job.getProduct().getMetadata().getOrDefault("processMode", "NOMINAL").toString();
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
}
