package esa.s1pdgs.cpoc.ipf.preparation.worker.type.slice;

import java.util.Map;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.CatalogEventAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderSensingTime;

public final class LevelSliceTypeAdapter extends AbstractProductTypeAdapter implements ProductTypeAdapter {
	private final MetadataClient metadataClient;
	private final Map<String, Float> sliceOverlap;
	private final Map<String, Float> sliceLength;

	public LevelSliceTypeAdapter(
			final MetadataClient metadataClient,
			final Map<String, Float> sliceOverlap,
			final Map<String, Float> sliceLength
	) {
		this.metadataClient = metadataClient;
		this.sliceOverlap = sliceOverlap;
		this.sliceLength = sliceLength;
	}

	@Override
	public final Callable<Void> mainInputSearch(final AppDataJob job) {
		return new LevelSliceInputQuery(job, metadataClient);
	}

	@Override
	public void customAppDataJob(final AppDataJob job) {
		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(job);
		final LevelSliceProduct product = LevelSliceProduct.of(job);
		
		product.setAcquisition(eventAdapter.swathType()); 
        product.setPolarisation(eventAdapter.polarisation());
	}

	@Override
	public final void customJobOrder(final JobGen job) {
		final LevelSliceProduct product = LevelSliceProduct.of(job.job());
		
		// Rewrite job order sensing time
		final String jobOrderStart = DateUtils.convertToAnotherFormat(product.getSegmentStartDate(),
				AppDataJobProduct.TIME_FORMATTER, JobOrderSensingTime.DATETIME_FORMATTER);
		final String jobOrderStop = DateUtils.convertToAnotherFormat(product.getSegmentStopDate(),
				AppDataJobProduct.TIME_FORMATTER, JobOrderSensingTime.DATETIME_FORMATTER);
		
		job.jobOrder().getConf().setSensingTime(new JobOrderSensingTime(jobOrderStart, jobOrderStop));

		updateProcParam(
				job.jobOrder(), 
				"Mission_Id",
				product.getMissionId() + product.getSatelliteId());
		updateProcParam(
				job.jobOrder(), 
				"Slice_Number", 
				String.valueOf(product.getNumberSlice()));
		updateProcParam(
				job.jobOrder(), 
				"Total_Number_Of_Slices",
				String.valueOf(product.getTotalNbOfSlice()));
		updateProcParam(
				job.jobOrder(), 
				"Slice_Overlap",
				String.valueOf(sliceOverlap.get(product.getAcquisition())));
		updateProcParam(
				job.jobOrder(), 
				"Slice_Length",
				String.valueOf(sliceLength.get(product.getAcquisition())));
		updateProcParam(
				job.jobOrder(), 
				"Slicing_Flag", 
				"TRUE"
		);
	}

	@Override
	public final void customJobDto(final JobGen job, final IpfExecutionJob dto) {
		// NOTHING TO DO
		
	}	
}
