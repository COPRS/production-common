package esa.s1pdgs.cpoc.ipf.preparation.worker.type.slice;

import java.util.Collections;
import java.util.Map;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
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
	public final Product mainInputSearch(final AppDataJob job) throws IpfPrepWorkerInputsMissingException {	
		final LevelSliceProduct product = LevelSliceProduct.of(job);
		
		// Retrieve instrument configuration id and slice number
		try {
			final L0SliceMetadata file = this.metadataClient.getL0Slice(product.getProductName());
			product.setInsConfId(file.getInstrumentConfigurationId());
			product.setNumberSlice(file.getNumberSlice());
			product.setDataTakeId(file.getDatatakeId());
			
		} catch (final MetadataQueryException e) {
			throw new IpfPrepWorkerInputsMissingException(
					Collections.singletonMap(
							product.getProductName(), 
							"No Slice: " + e.getMessage()
					)
			);
		}
		// Retrieve Total_Number_Of_Slices
		try {
			final L0AcnMetadata acn = this.metadataClient.getFirstACN(
					product.getProductName(),
					product.getProcessMode()
			);
			product.setTotalNbOfSlice(acn.getNumberOfSlices());
			product.setSegmentStartDate(acn.getValidityStart());
			product.setSegmentStopDate(acn.getValidityStop());
		} catch (final MetadataQueryException e) {
			throw new IpfPrepWorkerInputsMissingException(	
					Collections.singletonMap(
							product.getProductName(), 
							"No ACNs: " + e.getMessage()
					)
			);
		}
		return product;
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
