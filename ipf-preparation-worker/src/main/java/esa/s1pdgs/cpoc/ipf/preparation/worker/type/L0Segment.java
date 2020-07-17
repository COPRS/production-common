package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import java.util.Optional;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.appcat.AppCatJobService;
import esa.s1pdgs.cpoc.ipf.preparation.worker.appcat.CatalogEventAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;

public final class L0Segment extends AbstractProductTypeAdapter implements ProductTypeAdapter {
	private final MetadataClient metadataClient;
	private final long timeoutInputSearchMs;

	public L0Segment(
			final MetadataClient metadataClient,
			final long timeoutInputSearchMs
	) {
		this.metadataClient = metadataClient;
		this.timeoutInputSearchMs = timeoutInputSearchMs;
	}
	
	@Override
	public final Optional<AppDataJob> findAssociatedJobFor(final AppCatJobService appCat, final CatalogEventAdapter catEvent)
			throws AbstractCodedException {
		return appCat.findJobForDatatakeId(catEvent.datatakeId());
	}

	@Override
	public final Callable<JobGen> mainInputSearch(final JobGen job) {
		return new L0SegmentPolarisationQuery(job, metadataClient, timeoutInputSearchMs);
	}

	@Override
	public final void customJobOrder(final JobGen job) {
		final AppDataJobProductAdapter product = new AppDataJobProductAdapter(job.job().getProduct());		
        updateProcParam(
        		job.jobOrder(), 
        		"Mission_Id",
        		product.getMissionId()+ product.getStringValue("satelliteId")
        );		
	}

	@Override
	public final void customJobDto(final JobGen job, final IpfExecutionJob dto) {
        // NOTHING TO DO		
	}



}
