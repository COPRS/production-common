/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.ipf.execution.worker.job.file;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties.TypeEstimationMapping;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import esa.s1pdgs.cpoc.report.MissingOutput;

public class OutputEstimation {

	private static final Logger LOGGER = LogManager.getLogger(OutputEstimation.class);

	private final ApplicationProperties properties;

	private final String prefixMonitorLogs;

	private final OutputUtils outputUtils;

	private final String listFile;

	List<MissingOutput> missingOutputs;

	public OutputEstimation(final ApplicationProperties properties, final String prefixMonitorLogs,
			final String listFile, final List<MissingOutput> missingOutputs) {
		this.properties = properties;
		this.prefixMonitorLogs = prefixMonitorLogs;
		this.listFile = listFile;
		this.missingOutputs = missingOutputs;

		this.outputUtils = new OutputUtils(this.properties, this.prefixMonitorLogs);
	}

	public void estimateWithoutError(IpfExecutionJob job) throws InternalErrorException {

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
				findMissingType(job, typeEstimationMapping.getRegexp(),
						properties.getProductTypeEstimationOutputFamily(), productsInWorkDir,
						typeEstimationMapping.getCount());
			}
		} else if (inputProductFamily == ProductFamily.S3_GRANULES) {
			findMissingType(job, s3L0TypeFromGranulesType(inputProductType), ProductFamily.S3_L0, productsInWorkDir, 1);
		} else if (inputProductFamily == ProductFamily.L0_SEGMENT) {
			findMissingTypesForASP(job, inputProductType, productsInWorkDir);
		} else if (inputProductFamily == ProductFamily.L0_SLICE || inputProductFamily == ProductFamily.L0_ACN
				|| inputProductFamily == ProductFamily.S3_L0 || inputProductFamily == ProductFamily.S3_L1_NRT
				|| inputProductFamily == ProductFamily.S3_L1_NTC || inputProductFamily == ProductFamily.S3_L1_STC
				|| inputProductFamily == ProductFamily.S3_L2_NRT || inputProductFamily == ProductFamily.S3_L2_NTC
				|| inputProductFamily == ProductFamily.S3_L2_STC || inputProductFamily == ProductFamily.S3_PUG) {
			findMissingTypesFromJob(job, productsInWorkDir);
		}
	}

	public void estimateWithError(IpfExecutionJob job) throws InternalErrorException {

		ProductFamily inputProductFamily = job.getPreparationJob().getCatalogEvent().getProductFamily();
		String inputProductType = (String) job.getPreparationJob().getCatalogEvent().getMetadata().get("productType");

		LOGGER.debug("output estimation for input family {} with error", inputProductFamily);

		if (inputProductFamily == ProductFamily.EDRS_SESSION) {
			for (String productType : properties.getProductTypeEstimatedCount().keySet()) {
				TypeEstimationMapping typeEstimationMapping = properties.getProductTypeEstimatedCount()
						.get(productType);
				addMissingOutput(job, typeEstimationMapping.getRegexp(),
						properties.getProductTypeEstimationOutputFamily(), typeEstimationMapping.getCount());
			}

		} else if (inputProductFamily == ProductFamily.S3_GRANULES) {
			addMissingOutput(job, s3L0TypeFromGranulesType(inputProductType), ProductFamily.S3_L0, 1);
		} else if (inputProductFamily == ProductFamily.L0_SEGMENT) {
			addMissingOutputForASP(job, inputProductType);
		} else if (inputProductFamily == ProductFamily.L0_SLICE || inputProductFamily == ProductFamily.L0_ACN
				|| inputProductFamily == ProductFamily.S3_L0 || inputProductFamily == ProductFamily.S3_L1_NRT
				|| inputProductFamily == ProductFamily.S3_L1_NTC || inputProductFamily == ProductFamily.S3_L1_STC
				|| inputProductFamily == ProductFamily.S3_L2_NRT || inputProductFamily == ProductFamily.S3_L2_NTC
				|| inputProductFamily == ProductFamily.S3_L2_STC || inputProductFamily == ProductFamily.S3_PUG) {
			addMissingOutputFromJob(job);
		}
	}

	void findMissingType(final IpfExecutionJob job, final String productTypeOrRegexp, final ProductFamily productFamily,
			final List<String> productsInWorkDir, final int estimatedCount) {

		String regexp = typeToRegexp(productTypeOrRegexp);
		String productType = regexpToType(regexp);

		LOGGER.debug("finding type {}", productType);

		int productTypeCount = 0;

		for (final String line : productsInWorkDir) {

			String productName = outputUtils.getProductName(line);
			if (productName.matches(regexp)) {
				productTypeCount++;
			}
		}

		LOGGER.debug("count is {} for type {}, estimated {}", productTypeCount, productType, estimatedCount);

		if (productTypeCount < estimatedCount) {

			addMissingOutput(job, productType, productFamily, estimatedCount);
		}
	}

	void addMissingOutput(final IpfExecutionJob job, final String productType, final ProductFamily productFamily,
			final int estimatedCount) {

		LOGGER.debug("adding type {} as missing, estimated count is {}", productType, estimatedCount);

		MissingOutput missingOutput = new MissingOutput();
		missingOutput.setProductMetadataCustomObject(productMetadataCustomObjectFor(job, productFamily, productType));
		missingOutput.setEstimatedCountInteger(estimatedCount);
		missingOutput.setEndToEndProductBoolean(productFamily.isEndToEndFamily());

		missingOutputs.add(missingOutput);
	}

	void findMissingTypesForASP(final IpfExecutionJob job, final String inputProductType,
			final List<String> productsInWorkDir) throws InternalErrorException {

		String inputSwathType = (String) job.getPreparationJob().getCatalogEvent().getMetadata().get("swathtype");

		findMissingType(job, inputProductType, ProductFamily.L0_SLICE, productsInWorkDir, determineCountForASPType(
				inputSwathType, job.getPreparationJob().getStartTime(), job.getPreparationJob().getStopTime()));
		if(!"RF_RAW__0S".equals(inputProductType)) { //for RFC no ACN is expected #RS-708 
			findMissingType(job, inputProductType.substring(0, inputProductType.length() - 1) + "A", ProductFamily.L0_ACN,
					productsInWorkDir, 1);
			findMissingType(job, inputProductType.substring(0, inputProductType.length() - 1) + "C", ProductFamily.L0_ACN,
					productsInWorkDir, 1);
			findMissingType(job, inputProductType.substring(0, inputProductType.length() - 1) + "N", ProductFamily.L0_ACN,
					productsInWorkDir, 1);
		}
	}

	void addMissingOutputForASP(final IpfExecutionJob job, final String inputProductType)
			throws InternalErrorException {

		String inputSwathType = (String) job.getPreparationJob().getCatalogEvent().getMetadata().get("swathtype");

		addMissingOutput(job, inputProductType, ProductFamily.L0_SLICE, determineCountForASPType(inputSwathType,
				job.getPreparationJob().getStartTime(), job.getPreparationJob().getStopTime()));
		if(!"RF_RAW__0S".equals(inputProductType)) { //for RFC no ACN expected #RS-708
			addMissingOutput(job, inputProductType.substring(0, inputProductType.length() - 1) + "A", ProductFamily.L0_ACN,
					1);
			addMissingOutput(job, inputProductType.substring(0, inputProductType.length() - 1) + "C", ProductFamily.L0_ACN,
					1);
			addMissingOutput(job, inputProductType.substring(0, inputProductType.length() - 1) + "N", ProductFamily.L0_ACN,
					1);
		}
	}

	void findMissingTypesFromJob(final IpfExecutionJob job, final List<String> productsInWorkDir) {
		for (LevelJobOutputDto o : job.getOutputs()) {
			ProductFamily family = ProductFamily.fromValue(o.getFamily());
			String type = regexpToType(typeToRegexp(o.getRegexp()));
			if (family != ProductFamily.BLANK) {
				findMissingType(job, o.getRegexp(), family, productsInWorkDir, getCountFromConfigOrDefault(type));
			}
		}
	}

	void addMissingOutputFromJob(final IpfExecutionJob job) {
		for (LevelJobOutputDto o : job.getOutputs()) {
			ProductFamily family = ProductFamily.fromValue(o.getFamily());
			String type = regexpToType(typeToRegexp(o.getRegexp()));
			if (family != ProductFamily.BLANK) {
				addMissingOutput(job, type, family, getCountFromConfigOrDefault(type));
			}
		}
	}

	private int getCountFromConfigOrDefault(String type) {
		int estimatedCount = 1;

		for (TypeEstimationMapping t : properties.getProductTypeEstimatedCount().values()) {
			if (t.getRegexp().equals(type)) {
				estimatedCount = t.getCount();
				break;
			}
		}
		return estimatedCount;
	}

	int determineCountForASPType(final String inputSwathType, final String inputStartTime, final String inputStopTime)
			throws InternalErrorException {

		int estimatedCount = 1;

		if (!"SM".equals(inputSwathType) && !"IW".equals(inputSwathType) && !"EW".equals(inputSwathType)) {
			estimatedCount = 1;

		} else {

			LocalDateTime startTime = DateUtils.parse(inputStartTime);
			LocalDateTime stopTime = DateUtils.parse(inputStopTime);
			Duration duration = Duration.between(startTime, stopTime);

			int sliceLength = 0;
			int sliceOverlap = 0;

			if ("SM".equals(inputSwathType)) {
				sliceLength = 25000;
				sliceOverlap = 7700;
			} else if ("IW".equals(inputSwathType)) {
				sliceLength = 25000;
				sliceOverlap = 7400;
			} else if ("EW".equals(inputSwathType)) {
				sliceLength = 60000;
				sliceOverlap = 8200;
			}

			if (sliceLength == 0) {
				throw new InternalErrorException("Slice length is 0 and would cause a division by zero");
			}

			double c = (duration.toMillis() - sliceOverlap) * 1f / sliceLength;

			if (((c % 1) * sliceLength) < sliceOverlap) {
				estimatedCount = Math.max(1, (int) Math.floor(c));
			} else {
				estimatedCount = Math.max(1, (int) Math.ceil(c));
			}

		}

		LOGGER.info("estimated count for swathtype {} calculated to be: {}", inputSwathType, estimatedCount);
		return estimatedCount;
	}

	Map<String, Object> productMetadataCustomObjectFor(final IpfExecutionJob job, final ProductFamily productFamily,
			final String productType) {

		Map<String, Object> customObject = new HashMap<>();

		customObject.put("product_type_string", productType);
		customObject.put("platform_serial_identifier_string",
				job.getPreparationJob().getCatalogEvent().getSatelliteId());

		switch (productFamily) {
		case L0_SEGMENT:
			customObject.put("platform_short_name_string", "SENTINEL-1");
			customObject.put("product_class_string", "S");
			customObject.put("slice_product_flag_boolean", false);
			customObject.put("processing_level_integer", 0);
			break;
		case L0_SLICE:
		case L0_ACN:
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
			break;
		case L1_SLICE:
		case L1_ACN:
			customObject.put("platform_short_name_string", "SENTINEL-1");
			customObject.put("beginning_date_time_date",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("startTime"));
			customObject.put("ending_date_time_date",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("stopTime"));
			customObject.put("operational_mode_string",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("opertationalMode"));
			customObject.put("product_class_string", productClassOf(productType));
			customObject.put("datatake_id_integer",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("missionDataTakeId"));
			customObject.put("slice_product_flag_boolean", true);
			customObject.put("polarisation_channels_string",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("polarisationChannels"));
			customObject.put("processing_level_integer", 1);
			customObject.put("swath_identifier_integer",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("swathIdentifier"));
			break;
		case L2_SLICE:
		case L2_ACN:
			customObject.put("platform_short_name_string", "SENTINEL-1");
			customObject.put("beginning_date_time_date",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("startTime"));
			customObject.put("ending_date_time_date",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("stopTime"));
			customObject.put("operational_mode_string",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("opertationalMode"));
			customObject.put("product_class_string", productClassOf(productType));
			customObject.put("datatake_id_integer",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("missionDataTakeId"));
			customObject.put("slice_product_flag_boolean", true);
			customObject.put("polarisation_channels_string",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("polarisationChannels"));
			customObject.put("processing_level_integer", 2);
			customObject.put("swath_identifier_integer",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("swathIdentifier"));
			break;
		case S3_GRANULES:
			customObject.put("platform_short_name_string", "SENTINEL-3");
			customObject.put("instrument_short_name_string", instrumentShortNameOf(productType));
			customObject.put("processing_level_integer", 0);
			break;
		case S3_L0:
			customObject.put("platform_short_name_string", "SENTINEL-3");
			customObject.put("instrument_short_name_string", instrumentShortNameOf(productType));
			customObject.put("processing_level_integer", 0);
			customObject.put("orbit_number_integer",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("orbitNumber"));
			break;
		case S3_L1_NRT:
		case S3_L1_NTC:
		case S3_L1_STC:
			customObject.put("beginning_date_time_date",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("startTime"));
			customObject.put("ending_date_time_date",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("stopTime"));
			customObject.put("platform_short_name_string", "SENTINEL-3");
			customObject.put("instrument_short_name_string", instrumentShortNameOf(productType));
			customObject.put("processing_level_integer", 1);
			customObject.put("orbit_number_integer",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("orbitNumber"));
			break;
		case S3_L2_NRT:
		case S3_L2_NTC:
		case S3_L2_STC:
			customObject.put("beginning_date_time_date",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("startTime"));
			customObject.put("ending_date_time_date",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("stopTime"));
			customObject.put("platform_short_name_string", "SENTINEL-3");
			customObject.put("instrument_short_name_string", instrumentShortNameOf(productType));
			customObject.put("processing_level_integer", 2);
			customObject.put("orbit_number_integer",
					job.getPreparationJob().getCatalogEvent().getMetadata().get("orbitNumber"));
			break;
		}

		return customObject;
	}
	
	public List<MissingOutput> getMissingOutputs() {
		return this.missingOutputs;
	}
	
	public void setMissingOutputs(List<MissingOutput> missingOutputs) {
		this.missingOutputs = missingOutputs;
	}

	static String productClassOf(final String productType) {
		return productType.substring(productType.length() - 1);
	}

	static String instrumentShortNameOf(final String productType) {
		return productType.substring(0, 2);
	}

	static String s3L0TypeFromGranulesType(final String inputProductType) {

		String outputS3L0Type = null;

		if ("OL_0_CR___G".equals(inputProductType)) {

			outputS3L0Type = "OL_0_CR[01]___";

		} else {
			outputS3L0Type = inputProductType.substring(0, inputProductType.length() - 1) + "_";
		}
		return outputS3L0Type;
	}

	static String typeToRegexp(final String productType) {

		Assert.notNull(productType, "productType is null");

		String regex = productType;

		if (regex.contains("/")) {
			regex = regex.substring(regex.lastIndexOf("/") + 1);
		}

		if (!regex.startsWith("^.*") && !regex.endsWith(".*$")) {
			regex = "^.*" + regex + ".*$";
		}

		return regex;
	}

	static String regexpToType(final String regex) {

		Assert.notNull(regex, "regex is null");

		String type = regex;

		if (type.startsWith("^.*") && type.endsWith(".*$")) {
			type = type.substring(3, type.length() - 3);
		}

		return type;
	}

}
