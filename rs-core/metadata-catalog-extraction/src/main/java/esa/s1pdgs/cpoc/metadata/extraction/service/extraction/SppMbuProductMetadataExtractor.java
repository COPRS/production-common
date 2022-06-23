package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.elastic.EsServices;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class SppMbuProductMetadataExtractor extends AbstractMetadataExtractor {

	private final static String SPP_MBU_PRODUCT_TYPE = "REP_MBU_";

	public SppMbuProductMetadataExtractor(EsServices esServices, MetadataBuilder mdBuilder,
			FileDescriptorBuilder fileDescriptorBuilder, String localDirectory,
			ProcessConfiguration processConfiguration, ObsClient obsClient) {
		super(esServices, mdBuilder, fileDescriptorBuilder, localDirectory, processConfiguration, obsClient);
	}

	@Override
	public JSONObject extract(ReportingFactory reportingFactory, CatalogJob catJob) throws AbstractCodedException {
		return putMetadataToJSON(catJob.getKeyObjectStorage(), fileDescriptorBuilder.getPattern());
	}

	static JSONObject putMetadataToJSON(String productName, Pattern fileNamePattern)
			throws MetadataExtractionException {

		JSONObject metadataJSON = new JSONObject();

		Matcher m = fileNamePattern.matcher(productName);

		if (m.matches()) {
			metadataJSON.put("productFamily", ProductFamily.SPP_MBU.name());
			metadataJSON.put("productName", productName);
			metadataJSON.put(MissionId.FIELD_NAME, m.group(1).toUpperCase());
			metadataJSON.put("satelliteId", m.group(2).toUpperCase());
			metadataJSON.put("mode", m.group(3).toUpperCase());
			metadataJSON.put("columnId", m.group(4).toUpperCase());
			metadataJSON.put("productType", SPP_MBU_PRODUCT_TYPE);
			metadataJSON.put("polarisation", m.group(8).toUpperCase());
			metadataJSON.put("startTime", DateUtils.convertToMetadataDateTimeFormat(m.group(9).toUpperCase()));
			metadataJSON.put("stopTime", DateUtils.convertToMetadataDateTimeFormat(m.group(10).toUpperCase()));
			metadataJSON.put("absoluteOrbitNumber", m.group(11));
			metadataJSON.put("missionDataTakeId", m.group(12).toUpperCase());
			metadataJSON.put("idInColumn", m.group(13).toUpperCase());
			metadataJSON.put("url", productName);
		} else {
			throw new MetadataExtractionException(
					new Exception("metadata could not be extracted from productname: " + productName));
		}
		return metadataJSON;
	}

}
