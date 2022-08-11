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

public class SppMbuProductMetadataExtractor extends AbstractMetadataExtractor {

	private final static String SPP_MBU_PRODUCT_TYPE = "REP_MBU_";

	public SppMbuProductMetadataExtractor(EsServices esServices, MetadataBuilder mdBuilder,
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
			metadata.put("productFamily", ProductFamily.SPP_MBU.name());
			metadata.put("productName", productName);
			metadata.put(MissionId.FIELD_NAME, m.group(1).toUpperCase());
			metadata.put("satelliteId", m.group(2).toUpperCase());
			metadata.put("mode", m.group(3).toUpperCase());
			metadata.put("columnId", m.group(4).toUpperCase());
			metadata.put("productType", SPP_MBU_PRODUCT_TYPE);
			metadata.put("polarisation", m.group(8).toUpperCase());
			metadata.put("startTime", DateUtils.convertToMetadataDateTimeFormat(m.group(9).toUpperCase()));
			metadata.put("stopTime", DateUtils.convertToMetadataDateTimeFormat(m.group(10).toUpperCase()));
			metadata.put("absoluteOrbitNumber", m.group(11));
			metadata.put("missionDataTakeId", m.group(12).toUpperCase());
			metadata.put("idInColumn", m.group(13).toUpperCase());
			metadata.put("url", productName);
		} else {
			throw new MetadataExtractionException(
					new Exception("metadata could not be extracted from productname: " + productName));
		}
		return metadata;
	}

}
