package esa.s1pdgs.cpoc.preparation.worker.type.pdu.generator;

import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.preparation.worker.config.ProcessProperties;
import esa.s1pdgs.cpoc.preparation.worker.config.type.PDUProperties.PDUTypeProperties;
import esa.s1pdgs.cpoc.preparation.worker.model.pdu.PDUType;

public interface PDUGenerator {

	public List<AppDataJob> generateAppDataJobs(final IpfPreparationJob job, final int primaryCheckMaxTimelifeS)
			throws MetadataQueryException;

	/**
	 * Create a suitable instance of PDUGenerator for the given settings
	 * 
	 * @param settings PDUTypeSettings used to choose which generator to create
	 * @param mdClient MetadataClient for constructor of PDUGenerator
	 * @return instance of PDUGenerator, or null if none is suitable
	 */
	public static PDUGenerator getPDUGenerator(final ProcessProperties processSettings,
			PDUTypeProperties settings, MetadataClient mdClient) {
		if (settings.getType() == PDUType.FRAME) {
			return new PDUFrameGenerator(processSettings, settings, mdClient);
		} else if (settings.getType() == PDUType.STRIPE) {
			return new PDUStripeGenerator(processSettings, settings, mdClient);
		}

		return null;
	}
}
