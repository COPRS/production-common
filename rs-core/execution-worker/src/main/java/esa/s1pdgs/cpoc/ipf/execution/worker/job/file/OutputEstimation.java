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
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties.TypeEstimationMapping;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
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
		String inputProductType = (String) job.getPreparationJob().getCatalogEvent().getMetadata().get("productType");

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
				TypeEstimationMapping typeEstimationMapping = properties.getProductTypeEstimatedCount()
						.get(productType);
				findMissingType(typeEstimationMapping.getRegexp(), properties.getProductTypeEstimationOutputFamily(),
						productsInWorkDir, typeEstimationMapping.getCount());
			}
		} else if (inputProductFamily == ProductFamily.S3_GRANULES) {
			findMissingType(inputProductType.substring(0, inputProductType.length() - 1) + "_", ProductFamily.S3_L0,
					productsInWorkDir, 1);

		} else if (inputProductFamily == ProductFamily.L0_SEGMENT) {
			findMissingTypesForASP(inputProductType, productsInWorkDir);
		}
	}

	public void estimateWithError() {

		ProductFamily inputProductFamily = job.getPreparationJob().getCatalogEvent().getProductFamily();
		String inputProductType = (String) job.getPreparationJob().getCatalogEvent().getMetadata().get("productType");

		LOGGER.debug("output estimation for input family {} with error", inputProductFamily);

		if (inputProductFamily == ProductFamily.EDRS_SESSION) {
			for (String productType : properties.getProductTypeEstimatedCount().keySet()) {
				TypeEstimationMapping typeEstimationMapping = properties.getProductTypeEstimatedCount()
						.get(productType);
				addMissingOutput(typeEstimationMapping.getRegexp(), properties.getProductTypeEstimationOutputFamily(),
						typeEstimationMapping.getCount());
			}

		} else if (inputProductFamily == ProductFamily.S3_GRANULES) {
			addMissingOutput(inputProductType.substring(0, inputProductType.length() - 1) + "_", ProductFamily.S3_L0,
					1);

		} else if (inputProductFamily == ProductFamily.L0_SEGMENT) {
			addMissingOutputForASP(inputProductType);
		}
	}

	private void findMissingType(final String productTypeRegexp, final ProductFamily productFamily,
			final List<String> productsInWorkDir, final int estimatedCount) throws InternalErrorException {

		LOGGER.debug("finding type {}", productTypeRegexp);

		int productTypeCount = 0;

		for (final String line : productsInWorkDir) {

			String productName = outputUtils.getProductName(line);
			if (productName.matches("^.*" + productTypeRegexp + ".*$")) {
				productTypeCount++;
			}
		}

		LOGGER.debug("count is {} for type {}, estimated {}", productTypeCount, productTypeRegexp, estimatedCount);

		if (productTypeCount < estimatedCount) {

			addMissingOutput(productTypeRegexp, productFamily, estimatedCount);
		}
	}

	private void addMissingOutput(final String productType, final ProductFamily productFamily,
			final int estimatedCount) {

		LOGGER.debug("adding type {} as missing, estimated count is {}", productType, estimatedCount);

		MissingOutput missingOutput = new MissingOutput();
		missingOutput.setProductMetadataCustomObject(productMetadataCustomObjectFor(productFamily, productType));
		missingOutput.setEstimatedCountInteger(estimatedCount);
		missingOutput.setEndToEndProductBoolean(productFamily.isEndToEndFamily());

		missingOutputs.add(missingOutput);
	}

	private void findMissingTypesForASP(final String inputProductType, final List<String> productsInWorkDir)
			throws InternalErrorException {

		findMissingType(inputProductType, ProductFamily.L0_SLICE, productsInWorkDir,
				determineCountForASPType(inputProductType));
		findMissingType(inputProductType.substring(0, inputProductType.length() - 1) + "A", ProductFamily.L0_ACN,
				productsInWorkDir, 1);
		findMissingType(inputProductType.substring(0, inputProductType.length() - 1) + "C", ProductFamily.L0_ACN,
				productsInWorkDir, 1);
		findMissingType(inputProductType.substring(0, inputProductType.length() - 1) + "N", ProductFamily.L0_ACN,
				productsInWorkDir, 1);
	}

	private void addMissingOutputForASP(final String inputProductType) {

		addMissingOutput(inputProductType, ProductFamily.L0_SLICE, determineCountForASPType(inputProductType));
		addMissingOutput(inputProductType.substring(0, inputProductType.length() - 1) + "A", ProductFamily.L0_ACN, 1);
		addMissingOutput(inputProductType.substring(0, inputProductType.length() - 1) + "C", ProductFamily.L0_ACN, 1);
		addMissingOutput(inputProductType.substring(0, inputProductType.length() - 1) + "N", ProductFamily.L0_ACN, 1);
	}

	private int determineCountForASPType(String inputProductType) {

		if ("RF_RAW__0S".equals(inputProductType) || "WV_RAW__0S".equals(inputProductType)) {
			return 1;
		} else {
			return 1; // TODO
		}

	}

	private Map<String, Object> productMetadataCustomObjectFor(final ProductFamily productFamily,
			final String productType) {

		Map<String, Object> customObject = new HashMap<>();

		customObject.put("product_type_string", productType);
		customObject.put("platform_serial_identifier_string",
				job.getPreparationJob().getCatalogEvent().getSatelliteId());

		if (productFamily == ProductFamily.L0_SEGMENT) {

			customObject.put("platform_short_name_string", "SENTINEL-1");
			customObject.put("product_class_string", "S");
			customObject.put("slice_product_flag_boolean", false);
			customObject.put("processing_level_integer", 0);

		} else if (productFamily == ProductFamily.L0_SLICE || productFamily == ProductFamily.L0_ACN) {

			customObject.put("platform_short_name_string", "SENTINEL-1");
			customObject.put("product_class_string", productClassOf(productType));
			customObject.put("slice_product_flag_boolean", true);
			customObject.put("processing_level_integer", 0);
			customObject.put("operational_mode_string",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("opertationalMode"));
			customObject.put("datatake_id_integer",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("missionDataTakeId"));
			customObject.put("polarisation_channels_string",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("polarisationChannels"));
			customObject.put("swath_identifier_integer",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("swathIdentifier"));

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
