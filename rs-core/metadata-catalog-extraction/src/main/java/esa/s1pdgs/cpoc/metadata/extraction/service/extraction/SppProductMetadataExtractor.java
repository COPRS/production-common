package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.elastic.EsServices;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
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
	public ProductMetadata extract(ReportingFactory reportingFactory, CatalogJob catJob) throws AbstractCodedException {
		return newProductMetadataFromProductName(catJob.getProductName(), fileDescriptorBuilder.getPattern());
	}

	static ProductMetadata newProductMetadataFromProductName(String productName, Pattern fileNamePattern)
			throws MetadataExtractionException, MetadataMalformedException {

		ProductMetadata metadata = new ProductMetadata();

		Matcher m = fileNamePattern.matcher(productName);

		if (m.matches()) {
			metadata.put("productFamily", ProductFamily.SPP_OBS.name());
			metadata.put("productName", productName);
			metadata.put(MissionId.FIELD_NAME, m.group(1));
			metadata.put("satelliteId", m.group(2));
			metadata.put("productType", SPP_PRODUCT_TYPE);
			metadata.put("processLevel", m.group(6));
			metadata.put("productClass", m.group(7));
			metadata.put("polarisation", m.group(8));
			metadata.put("startTime", DateUtils.convertToMetadataDateTimeFormat(m.group(9)));
			metadata.put("stopTime", DateUtils.convertToMetadataDateTimeFormat(m.group(10)));
			metadata.put("absoluteOrbitNumber", m.group(11));
			metadata.put("url", productName);
		} else {
			throw new MetadataExtractionException(
					new Exception("metadata could not be extracted from productname: " + productName));
		}
		return metadata;
	}

}
