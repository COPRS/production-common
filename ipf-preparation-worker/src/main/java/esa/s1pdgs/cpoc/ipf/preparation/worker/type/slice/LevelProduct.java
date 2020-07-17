package esa.s1pdgs.cpoc.ipf.preparation.worker.type.slice;

import java.util.Map;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderSensingTime;

public final class LevelProduct extends AbstractProductTypeAdapter implements ProductTypeAdapter {
	private final MetadataClient metadataClient;
	private final Map<String, Float> sliceOverlap;
	private final Map<String, Float> sliceLength;

	public LevelProduct(
			final MetadataClient metadataClient,
			final Map<String, Float> sliceOverlap,
			final Map<String, Float> sliceLength
	) {
		this.metadataClient = metadataClient;
		this.sliceOverlap = sliceOverlap;
		this.sliceLength = sliceLength;
	}

	@Override
	public final Callable<JobGen> mainInputSearch(final JobGen job) {
		return new LevelProductInputQuery(job, metadataClient);
	}

	@Override
	public void customAppDataJob(final AppDataJob job) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public final void customJobOrder(final JobGen job) {
		final AppDataJobProductAdapter product = new AppDataJobProductAdapter(job.job().getProduct());
		
		
		// Rewrite job order sensing time
		final String jobOrderStart = DateUtils.convertToAnotherFormat(job.job().getProduct().getSegmentStartDate(),
				AppDataJobProduct.TIME_FORMATTER, JobOrderSensingTime.DATETIME_FORMATTER);
		final String jobOrderStop = DateUtils.convertToAnotherFormat(job.job().getProduct().getSegmentStopDate(),
				AppDataJobProduct.TIME_FORMATTER, JobOrderSensingTime.DATETIME_FORMATTER);
		
		job.jobOrder().getConf().setSensingTime(new JobOrderSensingTime(jobOrderStart, jobOrderStop));

		updateProcParam(
				job.jobOrder(), 
				"Mission_Id",
				job.job().getProduct().getMissionId() + job.job().getProduct().getSatelliteId());
		updateProcParam(
				job.jobOrder(), 
				"Slice_Number", 
				String.valueOf(job.job().getProduct().getNumberSlice()));
		updateProcParam(
				job.jobOrder(), 
				"Total_Number_Of_Slices",
				String.valueOf(job.job().getProduct().getTotalNbOfSlice()));
		updateProcParam(
				job.jobOrder(), 
				"Slice_Overlap",
				String.valueOf(sliceOverlap.get(job.job().getProduct().getAcquisition())));
		updateProcParam(
				job.jobOrder(), 
				"Slice_Length",
				String.valueOf(sliceLength.get(job.job().getProduct().getAcquisition())));
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
