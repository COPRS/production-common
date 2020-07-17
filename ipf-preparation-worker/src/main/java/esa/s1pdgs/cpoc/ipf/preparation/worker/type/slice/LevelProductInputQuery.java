package esa.s1pdgs.cpoc.ipf.preparation.worker.type.slice;

import java.util.Collections;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;

class LevelProductInputQuery implements Callable<JobGen>  {
	private final JobGen job;
	private final MetadataClient metadataClient;
	
	public LevelProductInputQuery(final JobGen job, final MetadataClient metadataClient) {
		this.job = job;
		this.metadataClient = metadataClient;
	}
	
	@Override
	public final JobGen call() throws Exception {
		// Retrieve instrument configuration id and slice number
		try {
			final L0SliceMetadata file = this.metadataClient.getL0Slice(job.productName());
			
			job.job().getProduct().setProductType(file.getProductType());
			job.job().getProduct().setInsConfId(file.getInstrumentConfigurationId());
			job.job().getProduct().setNumberSlice(file.getNumberSlice());
			job.job().getProduct().setDataTakeId(file.getDatatakeId());
			
		} catch (final MetadataQueryException e) {
			throw new IpfPrepWorkerInputsMissingException(
					Collections.singletonMap(
							job.productName(), 
							"No Slice: " + e.getMessage()
					)
			);
		}
		// Retrieve Total_Number_Of_Slices
		try {
			final L0AcnMetadata acn = this.metadataClient.getFirstACN(
					job.job().getProduct().getProductName(),
					job.job().getProduct().getProcessMode()
			);
			job.job().getProduct().setTotalNbOfSlice(acn.getNumberOfSlices());
			job.job().getProduct().setSegmentStartDate(acn.getValidityStart());
			job.job().getProduct().setSegmentStopDate(acn.getValidityStop());
		} catch (final MetadataQueryException e) {
			throw new IpfPrepWorkerInputsMissingException(	
					Collections.singletonMap(
							job.productName(), 
							"No ACNs: " + e.getMessage()
					)
			);
		}
		return job;
	}		
}