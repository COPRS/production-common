package esa.s1pdgs.cpoc.ipf.execution.worker.job.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import esa.s1pdgs.cpoc.report.MissingOutput;

public class OutputEstimation {

	private static final Logger LOGGER = LogManager.getLogger(OutputEstimation.class);

	private final ApplicationProperties properties;

	private final List<LevelJobOutputDto> authorizedOutputs;

	private final String prefixMonitorLogs;

	private final OutputUtils outputUtils;

	public OutputEstimation(final ApplicationProperties properties, List<LevelJobOutputDto> authorizedOutputs,
			final String prefixMonitorLogs) {
		this.properties = properties;
		this.authorizedOutputs = authorizedOutputs;
		this.prefixMonitorLogs = prefixMonitorLogs;

		this.outputUtils = new OutputUtils(this.properties, this.prefixMonitorLogs);
	}

	public List<MissingOutput> getMissingTypes(final String listFile, final String workDir, final MissionId missionId,
			final ApplicationLevel appLevel, final CatalogEvent catalogEvent) throws InternalErrorException {

		List<MissingOutput> missingOutputs = new ArrayList<>();

		for (String productType : properties.getProductTypeEstimatedCount().keySet()) {

			List<String> productsInWorkDir = outputUtils.extractFiles(listFile, workDir);

			int productTypeCount = 0;

			for (final String line : productsInWorkDir) {
				
				String productName = outputUtils.getProductName(line);
				if(productName.contains(productType)) {
					productTypeCount++;
				}
			}
			
			int estimatedCount = properties.getProductTypeEstimatedCount().get(productType);

			if (productTypeCount < estimatedCount) {

				LevelJobOutputDto levelJobOutputDto = outputUtils.levelJobOutputDtoOfProductType(productType,
						authorizedOutputs);
				if (levelJobOutputDto == null) {
					LOGGER.warn("product type {} is not in joborder, skipping");
					continue;
				}

				ProductFamily productFamily = outputUtils.familyOf(levelJobOutputDto, appLevel);

				MissingOutput missingOutput = new MissingOutput();
				missingOutput.setProductMetadataCustomObject(
						productMetadataCustomObjectFor(missionId, productFamily, productType, catalogEvent));
				missingOutput.setEstimatedCountInteger(estimatedCount);
				missingOutput.setEndToEndProductBoolean(productFamily.isEndToEndFamily());

				missingOutputs.add(missingOutput);

			}
		}
		return missingOutputs;
	}

	public List<MissingOutput> getAllEstimatedTypes(final MissionId missionId, final ApplicationLevel appLevel,
			final CatalogEvent catalogEvent) {

		List<MissingOutput> missingOutputs = new ArrayList<>();

		for (String productType : properties.getProductTypeEstimatedCount().keySet()) {

			LevelJobOutputDto levelJobOutputDto = outputUtils.levelJobOutputDtoOfProductType(productType,
					authorizedOutputs);
			if (levelJobOutputDto == null) {
				LOGGER.warn("product type {} is not in joborder, skipping");
				continue;
			}

			ProductFamily productFamily = outputUtils.familyOf(levelJobOutputDto, appLevel);

			MissingOutput missingOutput = new MissingOutput();
			missingOutput.setProductMetadataCustomObject(
					productMetadataCustomObjectFor(missionId, productFamily, productType, catalogEvent));
			missingOutput.setEstimatedCountInteger(properties.getProductTypeEstimatedCount().get(productType));
			missingOutput.setEndToEndProductBoolean(productFamily.isEndToEndFamily());

			missingOutputs.add(missingOutput);
		}
		return missingOutputs;
	}

	private Map<String, Object> productMetadataCustomObjectFor(final MissionId missionId,
			final ProductFamily productFamily, final String productType, final CatalogEvent catalogEvent) {

		Map<String, Object> customObject = new HashMap<>();

		customObject.put("product_type_string", productType);
		customObject.put("platform_serial_identifier_string", catalogEvent.getSatelliteId());
		switch (missionId) {
		case S1:
			customObject.put("platform_short_name_string", "SENTINEL-1");
			if (productFamily == ProductFamily.L0_SEGMENT) {

				customObject.put("product_class_string", productClassOf(productType));
				customObject.put("slice_product_flag_boolean", false);
				customObject.put("processing_level_integer", 0);

			} else if (productFamily == ProductFamily.L0_SLICE || productFamily == ProductFamily.L0_ACN) {

				customObject.put("product_class_string", productClassOf(productType));
				customObject.put("slice_product_flag_boolean", true);
				customObject.put("processing_level_integer", 0);
				customObject.put("operational_mode_string", null);// TODO
				customObject.put("datatake_id_integer", null);// TODO
				customObject.put("polarisation_channels_string", null);// TODO
				customObject.put("swath_identifier_integer", null);
			}
			break;
		case S3:
			customObject.put("platform_short_name_string", "SENTINEL-3");
			if (productFamily == ProductFamily.S3_GRANULES || productFamily == ProductFamily.S3_L0) {

				customObject.put("instrument_short_name_string", null); // TODO
				customObject.put("processing_level_integer", 0);
			}
			break;
		default:
			break;
		}

		return customObject;
	}

	private String productClassOf(String productType) {
		return productType.substring(productType.length() - 1);
	}

}
