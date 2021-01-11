package esa.s1pdgs.cpoc.mdc.worker.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	private final static Pattern MISSION_ID_PATTERN = Pattern.compile("^([a-z][0-9])[0-9a-z_]_.*$", Pattern.CASE_INSENSITIVE);
	
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
		metadata.put("productFamily", message.getBody().getProductFamily().name());
		metadata.put("productName", message.getBody().getProductName());
		metadata.put("productType", message.getBody().getProductFamily().name());
		metadata.put("insertionTime", message.getBody().getCreationDate());
		metadata.put("url", message.getBody().getKeyObjectStorage());
		
		Matcher matcher = MISSION_ID_PATTERN.matcher(message.getBody().getProductName());
		if (matcher.matches()) {
			metadata.put("missionId", matcher.group(1));			
		} else {
			metadata.put("missionId", esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage.NOT_DEFINED);
		}
		
		return metadata;
	}

}
