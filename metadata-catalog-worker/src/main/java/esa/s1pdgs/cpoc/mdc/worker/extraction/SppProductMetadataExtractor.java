package esa.s1pdgs.cpoc.mdc.worker.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.mdc.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class SppProductMetadataExtractor extends AbstractMetadataExtractor {

	private final static String SPP_PRODUCT_TYPE = "___OBS__SS";

	public SppProductMetadataExtractor(EsServices esServices, MetadataBuilder mdBuilder,
			FileDescriptorBuilder fileDescriptorBuilder, String localDirectory,
			ProcessConfiguration processConfiguration, ObsClient obsClient) {
		super(esServices, mdBuilder, fileDescriptorBuilder, localDirectory, processConfiguration, obsClient);
	}

	@Override
	public JSONObject extract(ReportingFactory reportingFactory, GenericMessageDto<CatalogJob> message)
			throws AbstractCodedException {

		final CatalogJob catJob = message.getBody();
		return putMetadataToJSON(catJob.getProductName(), fileDescriptorBuilder.getPattern());
	}

	static JSONObject putMetadataToJSON(String productName, Pattern fileNamePattern)
			throws MetadataExtractionException {

		JSONObject metadataJSON = new JSONObject();
		
		Matcher m = fileNamePattern.matcher(productName);

		if (m.matches()) {
			metadataJSON.put("productFamily", ProductFamily.SPP_OBS.name());
			metadataJSON.put("productName", productName);
			metadataJSON.put("missionId", m.group(1));
			metadataJSON.put("satelliteId", m.group(2));
			metadataJSON.put("productType", SPP_PRODUCT_TYPE);
			metadataJSON.put("processLevel", m.group(6));
			metadataJSON.put("productClass", m.group(7));
			metadataJSON.put("polarisation", m.group(8));
			metadataJSON.put("startTime", DateUtils.convertToMetadataDateTimeFormat(m.group(9)));
			metadataJSON.put("stopTime", DateUtils.convertToMetadataDateTimeFormat(m.group(10)));
			metadataJSON.put("absoluteOrbitNumber", m.group(11));
		} else {
			throw new MetadataExtractionException(
					new Exception("metadata could not be extracted from productname: " + productName));
		}
		return metadataJSON;
	}

}
