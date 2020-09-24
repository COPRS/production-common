package esa.s1pdgs.cpoc.ipf.preparation.worker.type.pdu.generator;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.PDUSettings.PDUTypeSettings;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;

public class PDUStripeGenerator extends AbstractPDUGenerator implements PDUGenerator {
	private static final Logger LOGGER = LogManager.getLogger(PDUStripeGenerator.class);

	private final PDUTypeSettings settings;
	private final MetadataClient mdClient;

	public PDUStripeGenerator(final PDUTypeSettings settings, final MetadataClient mdClient) {
		this.settings = settings;
		this.mdClient = mdClient;
	}

	@Override
	public List<AppDataJob> generateAppDataJobs(IpfPreparationJob job) throws MetadataQueryException {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}
}
