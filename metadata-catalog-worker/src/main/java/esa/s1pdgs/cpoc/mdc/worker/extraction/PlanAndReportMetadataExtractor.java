package esa.s1pdgs.cpoc.mdc.worker.extraction;

import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mdc.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class PlanAndReportMetadataExtractor extends AbstractMetadataExtractor {
	
	public PlanAndReportMetadataExtractor(
			final EsServices esServices, 
			final MetadataBuilder mdBuilder,
			final FileDescriptorBuilder fileDescriptorBuilder, 
			final String localDirectory,
			final ProcessConfiguration processConfiguration, 
			final ObsClient obsClient
	) {
		super(esServices, mdBuilder, fileDescriptorBuilder, localDirectory, processConfiguration, obsClient);
	}

	@Override
	public JSONObject extract(ReportingFactory reportingFactory, GenericMessageDto<CatalogJob> message)
			throws AbstractCodedException {
		JSONObject metadata = new JSONObject();
		metadata.put("productName", message.getBody().getProductName());
		metadata.put("insertionTime", message.getBody().getCreationDate());
		metadata.put("keyObjectStorage", message.getBody().getKeyObjectStorage());
		return metadata;
	}

}
