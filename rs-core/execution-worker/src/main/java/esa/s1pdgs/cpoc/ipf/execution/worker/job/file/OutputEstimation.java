package esa.s1pdgs.cpoc.ipf.execution.worker.job.file;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import esa.s1pdgs.cpoc.report.MissingOutput;

public class OutputEstimation {

	private static final Logger LOGGER = LogManager.getLogger(OutputEstimation.class);

	private final ApplicationProperties properties;

	private final IpfExecutionJob job;

	private final String prefixMonitorLogs;

	private final OutputUtils outputUtils;

	private final String listFile;

	List<MissingOutput> missingOutputs;

	public OutputEstimation(final ApplicationProperties properties, final IpfExecutionJob job,
			final String prefixMonitorLogs, final String listFile, final List<MissingOutput> missingOutputs) {
		this.properties = properties;
		this.job = job;
		this.prefixMonitorLogs = prefixMonitorLogs;
		this.listFile = listFile;
		this.missingOutputs = missingOutputs;

		this.outputUtils = new OutputUtils(this.properties, this.prefixMonitorLogs);
	}

	public void estimateWithoutError() throws InternalErrorException {

		ProductFamily inputProductFamily = job.getPreparationJob().getCatalogEvent().getProductFamily();
		
		LOGGER.debug("output estimation for input family {} without error", inputProductFamily);
		
		List<String> productsInWorkDir = null;

		if (outputUtils.listFileExists(listFile, job.getWorkDirectory())) {
			LOGGER.debug("listfile exists");
			productsInWorkDir = outputUtils.extractFiles(listFile, job.getWorkDirectory());
		} else {
			LOGGER.debug("missing listfile");
			File dir = new File(job.getWorkDirectory());
			productsInWorkDir = Arrays.asList(dir.listFiles()).stream().map(f -> f.getName())
					.collect(Collectors.toList());
		}

		LOGGER.trace("products in workdir: {}", productsInWorkDir);

		if (inputProductFamily == ProductFamily.EDRS_SESSION) {
			for (String productType : properties.getProductTypeEstimatedCount().keySet()) {
				int estimatedCount = properties.getProductTypeEstimatedCount().get(productType);
				findMissingType(productType, productsInWorkDir, estimatedCount);
			}
		} else {
			if (inputProductFamily == ProductFamily.S3_GRANULES) {
				findMissingType(outputProductTypeFor(inputProductFamily), productsInWorkDir, 1);
			}
		}
	}

	public void estimateWithError() {

		ProductFamily inputProductFamily = job.getPreparationJob().getCatalogEvent().getProductFamily();
		
		LOGGER.debug("output estimation for input family {} with error", inputProductFamily);

		if (inputProductFamily == ProductFamily.EDRS_SESSION) {
			for (String productType : properties.getProductTypeEstimatedCount().keySet()) {
				int estimatedCount = properties.getProductTypeEstimatedCount().get(productType);
				addMissingOutput(productType, estimatedCount);
			}

		} else {
			if (inputProductFamily == ProductFamily.S3_GRANULES) {
				addMissingOutput(outputProductTypeFor(inputProductFamily), 1);
			}
		}
	}

	private void findMissingType(final String productType, final List<String> productsInWorkDir, final int estimatedCount) throws InternalErrorException {

		LOGGER.debug("finding type {}", productType);
		
		int productTypeCount = 0;
		
		for (final String line : productsInWorkDir) {

			String productName = outputUtils.getProductName(line);
			if (productName.contains(productType)) {
				productTypeCount++;
			}
		}
		
		LOGGER.debug("count is {} for type {}, estimated {}", productTypeCount, productType, estimatedCount);

		if (productTypeCount < estimatedCount) {

			addMissingOutput(productType, estimatedCount);
		}
	}

	private void addMissingOutput(final String productType, final int estimatedCount) {
		
		LOGGER.debug("adding type {} as missing, estimated count is {}", productType, estimatedCount);
		
		ProductFamily productFamily = familyFor(productType);

		if (productFamily == null) {
			LOGGER.warn("product type {} is not in joborder, skipping");
			return;
		}

		MissingOutput missingOutput = new MissingOutput();
		missingOutput.setProductMetadataCustomObject(productMetadataCustomObjectFor(productFamily, productType));
		missingOutput.setEstimatedCountInteger(estimatedCount);
		missingOutput.setEndToEndProductBoolean(productFamily.isEndToEndFamily());

		missingOutputs.add(missingOutput);
	}

	private String outputProductTypeFor(final ProductFamily inputProductFamily) {

		String inputProductType = (String) job.getPreparationJob().getCatalogEvent().getMetadata().get("productType");

		if (inputProductFamily == ProductFamily.S3_GRANULES) {
			return inputProductType.replace("G", "_");
		}

		return null;
	}

	private ProductFamily familyFor(final String productType) {
		LevelJobOutputDto levelJobOutputDto = outputUtils.levelJobOutputDtoOfProductType(productType, job.getOutputs());
		if (levelJobOutputDto == null) {
			return null;
		}

		return outputUtils.familyOf(levelJobOutputDto, properties.getLevel());
	}

	private Map<String, Object> productMetadataCustomObjectFor(final ProductFamily productFamily,
			final String productType) {

		Map<String, Object> customObject = new HashMap<>();

		customObject.put("product_type_string", productType);
		customObject.put("platform_serial_identifier_string",
				job.getPreparationJob().getCatalogEvent().getSatelliteId());

		if (productFamily == ProductFamily.L0_SEGMENT) {

			customObject.put("platform_short_name_string", "SENTINEL-1");
			customObject.put("product_class_string", productClassOf(productType));
			customObject.put("slice_product_flag_boolean", false);
			customObject.put("processing_level_integer", 0);

		} else if (productFamily == ProductFamily.L0_SLICE || productFamily == ProductFamily.L0_ACN) {

			customObject.put("platform_short_name_string", "SENTINEL-1");
			customObject.put("product_class_string", productClassOf(productType));
			customObject.put("slice_product_flag_boolean", true);
			customObject.put("processing_level_integer", 0);
			customObject.put("operational_mode_string", null);// TODO
			customObject.put("datatake_id_integer", null);// TODO
			customObject.put("polarisation_channels_string", null);// TODO
			customObject.put("swath_identifier_integer", null);

		} else if (productFamily == ProductFamily.S3_GRANULES) {

			customObject.put("platform_short_name_string", "SENTINEL-3");
			customObject.put("instrument_short_name_string", instrumentShortNameOf(productType));
			customObject.put("processing_level_integer", 0);

		} else if (productFamily == ProductFamily.S3_L0) {

			customObject.put("platform_short_name_string", "SENTINEL-3");
			customObject.put("instrument_short_name_string", instrumentShortNameOf(productType));
			customObject.put("processing_level_integer", 0);

		}

		return customObject;
	}

	private String productClassOf(final String productType) {
		return productType.substring(productType.length() - 1);
	}
	
	private String instrumentShortNameOf(final String productType) {
		return productType.substring(0, 2);
	}

}
