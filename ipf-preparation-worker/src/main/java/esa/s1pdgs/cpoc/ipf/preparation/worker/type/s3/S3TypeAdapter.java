package esa.s1pdgs.cpoc.ipf.preparation.worker.type.s3;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.S3TypeAdapterSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;

public class S3TypeAdapter extends AbstractProductTypeAdapter implements ProductTypeAdapter {

	private MetadataClient metadataClient;
	
	private S3TypeAdapterSettings settings;
	
	public S3TypeAdapter(
			final MetadataClient metadataClient,
			final S3TypeAdapterSettings settings
	) {
		this.metadataClient = metadataClient;
		this.settings = settings;
	}
	
	@Override
	public Product mainInputSearch(AppDataJob job) throws IpfPrepWorkerInputsMissingException {
		S3Product returnValue = S3Product.of(job);
		
		// Umsetzung der MarginTTWFX 
		
		return returnValue;
	}

	@Override
	public void customAppDataJob(AppDataJob job) {
		// Nothing to do currently
	}

	@Override
	public void customJobOrder(AppDataJob job, JobOrder jobOrder) {
		// Nothing to do currently
	}

	@Override
	public void customJobDto(AppDataJob job, IpfExecutionJob dto) {
		// Nothing to do currently
	}

}
