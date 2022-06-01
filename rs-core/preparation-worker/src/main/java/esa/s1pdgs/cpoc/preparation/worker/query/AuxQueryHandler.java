package esa.s1pdgs.cpoc.preparation.worker.query;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;

public class AuxQueryHandler {
	private final MetadataClient metadataClient;
	private final ProductMode mode;

	public AuxQueryHandler(final MetadataClient metadataClient, final ProductMode mode) {
		this.metadataClient = metadataClient;
		this.mode = mode;
	}

	public AuxQuery queryFor(final AppDataJob job, final TaskTableAdapter taskTableAdapter) {
		return new AuxQuery(metadataClient, job, mode, taskTableAdapter);
	}
}
