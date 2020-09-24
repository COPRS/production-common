package esa.s1pdgs.cpoc.ipf.preparation.worker.type.pdu.generator;

import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.PDUSettings.PDUTypeSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.pdu.PDUType;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;

public interface PDUGenerator {

	public List<AppDataJob> generateAppDataJobs(final IpfPreparationJob job) throws MetadataQueryException;

	/**
	 * Create a suitable instance of PDUGenerator for the given settings
	 * 
	 * @param settings PDUTypeSettings used to choose which generator to create
	 * @param mdClient MetadataClient for constrcutor of PDUGenerator
	 * @return instance of PDUGenerator, or null if none is suitable
	 */
	public static PDUGenerator getPDUGenerator(PDUTypeSettings settings, MetadataClient mdClient) {
		if (settings.getType() == PDUType.FRAME) {
			return new PDUFrameGenerator(settings, mdClient);
		} else if (settings.getType() == PDUType.STRIPE) {
			return new PDUStripeGenerator(settings, mdClient);
		}

		return null;
	}
}
