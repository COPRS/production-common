package esa.s1pdgs.cpoc.ipf.preparation.worker.type.segment;

import java.util.Optional;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.appcat.AppCatJobService;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.CatalogEventAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;

public final class L0SegmentTypeAdapter extends AbstractProductTypeAdapter implements ProductTypeAdapter {
	private final MetadataClient metadataClient;
	private final long timeoutInputSearchMs;

	public L0SegmentTypeAdapter(
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
	public final Callable<Void> mainInputSearch(final AppDataJob job) {
		return new L0SegmentPolarisationQuery(job, metadataClient, timeoutInputSearchMs);
	}
	
	@Override
	public final void customAppDataJob(final AppDataJob job) {
		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(job);
		final L0SegmentProduct product = L0SegmentProduct.of(job);
		product.setAcquistion(eventAdapter.swathType());
		product.setDataTakeId(eventAdapter.datatakeId());
		product.setProductName("l0_segments_for_" + eventAdapter.datatakeId());
	}

	@Override
	public final void customJobOrder(final JobGen job) {
		final AppDataJobProductAdapter product = new AppDataJobProductAdapter(job.job().getProduct());		
        updateProcParam(
        		job.jobOrder(), 
        		"Mission_Id",
        		product.getMissionId()+ product.getSatelliteId()
        );		
	}

	@Override
	public final void customJobDto(final JobGen job, final IpfExecutionJob dto) {
        // NOTHING TO DO		
	}



}
