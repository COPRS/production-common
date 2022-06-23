package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.elastic.EsServices;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
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
	public JSONObject extract(ReportingFactory reportingFactory, CatalogJob message)
			throws AbstractCodedException {
		JSONObject metadata = new JSONObject();		
		metadata.put("productFamily", message.getProductFamily().name());
		metadata.put("productName", message.getKeyObjectStorage());
		metadata.put("productType", message.getProductFamily().name());
		
		if (message.getCreationDate() != null) {
			String formattedCreationDate = DateUtils.formatToMetadataDateTimeFormat(
					message.getCreationDate().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
			metadata.put("insertionTime", formattedCreationDate);
		}
		metadata.put("url", message.getKeyObjectStorage());
		
		Matcher matcher = MISSION_ID_PATTERN.matcher(message.getKeyObjectStorage());
		if (matcher.matches()) {
			metadata.put(MissionId.FIELD_NAME, matcher.group(1));			
		} else {
			metadata.put(MissionId.FIELD_NAME, esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage.NOT_DEFINED);
		}
		
		return metadata;
	}

}
