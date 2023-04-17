package esa.s1pdgs.cpoc.preparation.worker.query;

import java.util.function.Function;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTable;

public class AuxQueryHandler {
	private final MetadataClient metadataClient;
	private final ProductMode mode;
	private final Function<TaskTable, InputTimeoutChecker> timeoutChecker;

	public AuxQueryHandler(final MetadataClient metadataClient, final ProductMode mode,
			final Function<TaskTable, InputTimeoutChecker> timeoutChecker) {
		this.metadataClient = metadataClient;
		this.mode = mode;
		this.timeoutChecker = timeoutChecker;
	}

	public AuxQuery queryFor(final AppDataJob job, final TaskTableAdapter taskTableAdapter) {
		return new AuxQuery(metadataClient, job, mode, taskTableAdapter,
				timeoutChecker.apply(taskTableAdapter.taskTable()));
	}
}
