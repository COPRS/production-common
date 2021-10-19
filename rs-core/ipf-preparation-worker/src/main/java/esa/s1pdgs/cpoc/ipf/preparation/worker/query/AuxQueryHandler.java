package esa.s1pdgs.cpoc.ipf.preparation.worker.query;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;

public class AuxQueryHandler {	
    private final MetadataClient metadataClient;
	private final ProductMode mode;
	private final InputTimeoutChecker timeoutChecker;
	private final TaskTableAdapter taskTableAdapter;

	public AuxQueryHandler(
			final MetadataClient metadataClient,
			final ProductMode mode,
			final InputTimeoutChecker timeoutChecker,
			final TaskTableAdapter taskTableAdapter
	) {
		this.metadataClient = metadataClient;
		this.mode = mode;
		this.timeoutChecker = timeoutChecker;
		this.taskTableAdapter = taskTableAdapter;
	}
	
	public AuxQuery queryFor(final AppDataJob job) {
		return new AuxQuery(
				metadataClient,
				job,
				mode,
				timeoutChecker,
				taskTableAdapter
		);
	}	
}
