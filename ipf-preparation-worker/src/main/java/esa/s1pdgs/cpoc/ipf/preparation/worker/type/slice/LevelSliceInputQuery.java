package esa.s1pdgs.cpoc.ipf.preparation.worker.type.slice;

import java.util.Collections;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;

class LevelSliceInputQuery implements Callable<Void>  {
	private final AppDataJob job;
	private final MetadataClient metadataClient;
	
	public LevelSliceInputQuery(final AppDataJob job, final MetadataClient metadataClient) {
		this.job = job;
		this.metadataClient = metadataClient;
	}
	
	@Override
	public final Void call() throws Exception {		
		final LevelSliceProduct product = LevelSliceProduct.of(job);
		
		// Retrieve instrument configuration id and slice number
		try {
			final L0SliceMetadata file = this.metadataClient.getL0Slice(product.getProductName());
			
			product.setProductType(file.getProductType());
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
		return null;
	}		
}