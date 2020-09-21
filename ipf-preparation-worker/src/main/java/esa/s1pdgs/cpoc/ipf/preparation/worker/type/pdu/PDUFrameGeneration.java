package esa.s1pdgs.cpoc.ipf.preparation.worker.type.pdu;

import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.PDUSettings.PDUTypeSettings;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.S3Metadata;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;

public class PDUFrameGeneration {

	private final PDUTypeSettings settings;
	private final MetadataClient mdClient;

	public PDUFrameGeneration(final PDUTypeSettings settings, final MetadataClient mdClient) {
		this.settings = settings;
		this.mdClient = mdClient;
	}

	public List<AppDataJob> generateAppDataJobs(IpfPreparationJob job) throws MetadataQueryException {
		// Get metadata for product
		S3Metadata metadata = mdClient.getS3MetadataForProduct(job.getProductFamily(),
				job.getEventMessage().getBody().getProductName());

		List<S3Metadata> productsOfThisOrbit = mdClient.getProductsForOrbit(job.getProductFamily(),
				job.getEventMessage().getBody().getProductType(), metadata.getSatelliteId(),
				Long.parseLong(metadata.getAbsoluteStartOrbit()));

		// Check if this product is the first of its orbit
		// TODO: How can we produce PDUs again, when the generation of jobs on the first
		// product failed?

		return null;
	}
}
