package esa.s1pdgs.cpoc.ipf.preparation.worker.type.s3;

import java.io.File;
import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.S3TypeAdapterSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.query.QueryUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;

public class S3TypeAdapter extends AbstractProductTypeAdapter implements ProductTypeAdapter {

	private MetadataClient metadataClient;
	private TaskTableFactory ttFactory;
	private ElementMapper elementMapper;
	private ProcessSettings processSettings;
	private IpfPreparationWorkerSettings workerSettings;
	private S3TypeAdapterSettings settings;

	public S3TypeAdapter(final MetadataClient metadataClient, final TaskTableFactory ttFactory,
			final ElementMapper elementMapper, final ProcessSettings processSettings,
			final IpfPreparationWorkerSettings workerSettings, final S3TypeAdapterSettings settings) {
		this.metadataClient = metadataClient;
		this.ttFactory = ttFactory;
		this.elementMapper = elementMapper;
		this.processSettings = processSettings;
		this.workerSettings = workerSettings;
		this.settings = settings;
	}

	@Override
	public Product mainInputSearch(AppDataJob job) throws IpfPrepWorkerInputsMissingException {
		S3Product returnValue = S3Product.of(job);

		// Workaround to implement MarginTTWFX

		// Create tasktable Adapter
		final File ttFile = new File(workerSettings.getDiroftasktables(), job.getTaskTableName());
		final TaskTableAdapter tasktableAdapter = new TaskTableAdapter(ttFile,
				ttFactory.buildTaskTable(ttFile, processSettings.getLevel()), elementMapper);

		// Get Inputs for WFX
		List<AppDataJobTaskInputs> inputs = QueryUtils.buildInitialInputs(settings.getMode(), tasktableAdapter);

		returnValue.setAdditionalInputs(inputs);
		return returnValue;
	}

	@Override
	public void customAppDataJob(AppDataJob job) {
		// Nothing to do currently
	}

	@Override
	public void customJobOrder(AppDataJob job, JobOrder jobOrder) {
		// Nothing to do currently
	}

	@Override
	public void customJobDto(AppDataJob job, IpfExecutionJob dto) {
		// Nothing to do currently
	}

}
