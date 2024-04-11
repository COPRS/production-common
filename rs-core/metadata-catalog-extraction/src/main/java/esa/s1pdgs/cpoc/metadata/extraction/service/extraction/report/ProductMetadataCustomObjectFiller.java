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

package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.report;

import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

public class ProductMetadataCustomObjectFiller {
	
	final CatalogEvent catalogEvent;
	final MissionId missionId;
	final MetadataExtractionReportingOutput output;
	
	public ProductMetadataCustomObjectFiller(final CatalogEvent catalogEvent, final MissionId missionId, MetadataExtractionReportingOutput output) {
		this.catalogEvent = catalogEvent;
		this.missionId = missionId;
		this.output = output;
	}
	
	public void fillCustomObject() {
		
		switch (catalogEvent.getProductFamily()) {
		    // Session files Custom Object
			case EDRS_SESSION:
				output.getProductMetadataCustomObject().put("platform_short_name_string", MissionId.toPlatformShortName(missionId));
				fillwithKeyMapping("platform_serial_identifier_string", "satelliteId");
				output.getProductMetadataCustomObject().put("channel_identifier_integer", (Integer) catalogEvent.getMetadata().get("channelId"));
				break;
			// Sentinel-1 Custom Object
			case L0_SEGMENT:
			case L0_SLICE:
			case L0_ACN:
				fillwithKeyMapping("beginning_date_time_date", "startTime");
				fillwithKeyMapping("ending_date_time_date", "stopTime");
				output.getProductMetadataCustomObject().put("platform_short_name_string", MissionId.toPlatformShortName(missionId));
				fillwithKeyMapping("platform_serial_identifier_string", "satelliteId");
				fillwithKeyMapping("operational_mode_string", "operationalMode");
				fillwithKeyMapping("product_class_string", "productClass");
				fillwithKeyMapping("product_consolidation_string", "productConsolidation");
				fillwithKeyMapping("product_sensing_consolidation_string", "productSensingConsolidation");
				fillwithKeyMapping("datatake_id_integer", "missionDataTakeId");
				fillwithKeyMapping("slice_product_flag_boolean", "sliceProductFlag");
				fillwithKeyMapping("polarisation_channels_string", "polarisationChannels");
				fillwithKeyMapping("orbit_number_integer", "absoluteStartOrbit");
				output.getProductMetadataCustomObject().put("processing_level_integer", 0);
				fillwithKeyMapping("instrument_short_name_string", "instrumentShortName");
				fillwithKeyMapping("swath_identifier_integer", "swathIdentifier");
				fillwithKeyMapping("slice_number_integer", "sliceNumber");
				fillwithKeyMapping("total_slice_integer", "totalNumberOfSlice");
				fillwithKeyMapping("packet_store_integer", "packetStoreID");
				fillwithKeyMapping("processor_name_string", "processorName");
				fillwithKeyMapping("processor_version_string", "processorVersion");
				fillwithKeyMapping("product_type_string", "productType");
				break;
			case L1_ACN:
			case L1_SLICE:
				fillwithKeyMapping("beginning_date_time_date", "startTime");
				fillwithKeyMapping("ending_date_time_date", "stopTime");
				output.getProductMetadataCustomObject().put("platform_short_name_string", MissionId.toPlatformShortName(missionId));
				fillwithKeyMapping("platform_serial_identifier_string", "satelliteId");
				fillwithKeyMapping("operational_mode_string", "operationalMode");
				fillwithKeyMapping("swath_identifier_integer", "swathIdentifier");
				fillwithKeyMapping("orbit_number_integer", "absoluteStartOrbit");
				output.getProductMetadataCustomObject().put("processing_level_integer", 1);
				fillwithKeyMapping("instrument_short_name_string", "instrumentShortName");
				fillwithKeyMapping("datatake_id_integer", "missionDataTakeId");
				fillwithKeyMapping("product_class_string", "productClass");
				fillwithKeyMapping("product_type_string", "productType");
				fillwithKeyMapping("polarisation_channels_string", "polarisationChannels");
				fillwithKeyMapping("slice_product_flag_boolean", "sliceProductFlag");
				fillwithKeyMapping("slice_number_integer", "sliceNumber");
				fillwithKeyMapping("total_slice_integer", "totalNumberOfSlice");
				break;
			case L2_ACN:
			case L2_SLICE:
				fillwithKeyMapping("beginning_date_time_date", "startTime");
				fillwithKeyMapping("ending_date_time_date", "stopTime");
				output.getProductMetadataCustomObject().put("platform_short_name_string", MissionId.toPlatformShortName(missionId));
				fillwithKeyMapping("platform_serial_identifier_string", "satelliteId");
				fillwithKeyMapping("operational_mode_string", "operationalMode");
				fillwithKeyMapping("swath_identifier_integer", "swathIdentifier");
				fillwithKeyMapping("orbit_number_integer", "absoluteStartOrbit");
				output.getProductMetadataCustomObject().put("processing_level_integer", 2);
				fillwithKeyMapping("instrument_short_name_string", "instrumentShortName");
				fillwithKeyMapping("datatake_id_integer", "missionDataTakeId");
				fillwithKeyMapping("product_class_string", "productClass");
				fillwithKeyMapping("product_type_string", "productType");
				fillwithKeyMapping("polarisation_channels_string", "polarisationChannels");
				fillwithKeyMapping("slice_product_flag_boolean", "sliceProductFlag");
				fillwithKeyMapping("slice_number_integer", "sliceNumber");
				fillwithKeyMapping("total_slice_integer", "totalNumberOfSlice");
				break;
			// Sentinel-2 Custom Object
			case S2_L0_DS:
			case S2_L0_GR:
				fillwithKeyMapping("product_group_id", "productGroupId");
				fillwithKeyMapping("beginning_date_time_date", "startTime");
				fillwithKeyMapping("ending_date_time_date", "stopTime");
				fillwithKeyMapping("orbit_number_integer", "orbitNumber");
				fillwithKeyMapping("product_type_string", "productType");
				fillwithKeyMapping("platform_serial_identifier_string", "satelliteId");
				output.getProductMetadataCustomObject().put("platform_short_name_string", MissionId.toPlatformShortName(missionId));
				output.getProductMetadataCustomObject().put("processing_level_integer", 0);
				fillwithKeyMapping("processor_version_string", "processorVersion");
				fillwithKeyMapping("quality_status_integer", "qualityInfo");
				fillwithKeyMapping("quality_status_string", "qualityStatus");
				fillwithKeyMapping("instrument_short_name_string", "instrumentShortName");
				break;
			case S2_L1A_DS:
			case S2_L1A_GR:
			case S2_L1B_DS:
			case S2_L1B_GR:
			case S2_L1C_DS:
			case S2_L1C_TC:
			case S2_L1C_TL:
				fillwithKeyMapping("product_group_id", "productGroupId");
				fillwithKeyMapping("beginning_date_time_date", "startTime");
				fillwithKeyMapping("ending_date_time_date", "stopTime");
				fillwithKeyMapping("orbit_number_integer", "orbitNumber");
				fillwithKeyMapping("product_type_string", "productType");
				fillwithKeyMapping("platform_serial_identifier_string", "satelliteId");
				output.getProductMetadataCustomObject().put("platform_short_name_string", MissionId.toPlatformShortName(missionId));
				output.getProductMetadataCustomObject().put("processing_level_integer", 1);
				fillwithKeyMapping("quality_status_integer", "qualityInfo");
				fillwithKeyMapping("instrument_short_name_string", "instrumentShortName");
				fillwithKeyMapping("cloud_cover_double", "cloudPercentage");
				fillwithKeyMapping("quality_status_string", "qualityStatus");
				break;
			case S2_L2A_DS:
			case S2_L2A_TL:
			case S2_L2A_TC:
				fillwithKeyMapping("product_group_id", "productGroupId");
				fillwithKeyMapping("beginning_date_time_date", "startTime");
				fillwithKeyMapping("ending_date_time_date", "stopTime");
				fillwithKeyMapping("orbit_number_integer", "orbitNumber");
				fillwithKeyMapping("product_type_string", "productType");
				fillwithKeyMapping("platform_serial_identifier_string", "satelliteId");
				output.getProductMetadataCustomObject().put("platform_short_name_string", MissionId.toPlatformShortName(missionId));
				output.getProductMetadataCustomObject().put("processing_level_integer", 2);
				fillwithKeyMapping("quality_status_integer", "qualityInfo");
				fillwithKeyMapping("instrument_short_name_string", "instrumentShortName");
				fillwithKeyMapping("cloud_cover_double", "cloudPercentage");
				fillwithKeyMapping("quality_status_string", "qualityStatus");
				break;
			// Sentinel-3 Custom Object
			case S3_GRANULES:
			case S3_L0:
				s3l0CustomObject();
				break;
			case S3_L1_NRT:
			case S3_L1_NTC:
			case S3_L1_STC:
				s3l1CustomObject();
				break;
			case S3_L2_NRT:
			case S3_L2_NTC:
			case S3_L2_STC:
				s3l2CustomObject();
				break;
			case S3_PUG:
				String processingLevel = catalogEvent.getProductName().substring(7, 8);
				if (processingLevel.equals("0")) {
					s3l0CustomObject();
				} else if (processingLevel.equals("1")) {
					s3l1CustomObject();
				} else if (processingLevel.equals("2")) {
					s3l2CustomObject();
				}
				break;
			case AUXILIARY_FILE:
			case S2_AUX:
			case S3_AUX:
				fillwithKeyMapping("satellite_string", "satelliteId");
				fillwithKeyMapping("product_class_string", "productClass");
				fillwithKeyMapping("validity_start_date", "validityStartTime");
				fillwithKeyMapping("validity_stop_date", "validityStopTime");
				fillwithKeyMapping("product_type_string","productType");
				break;
			default: //no metadata custom object
		}
	}
	
	private void s3l0CustomObject() {
		fillwithKeyMapping("beginning_date_time_date", "startTime");
		fillwithKeyMapping("ending_date_time_date", "stopTime");
		output.getProductMetadataCustomObject().put("platform_short_name_string", MissionId.toPlatformShortName(missionId));
		fillwithKeyMapping("platform_serial_identifier_string", "satelliteId");
		fillwithKeyMapping("instrument_short_name_string", "instrumentName");
		fillwithKeyMapping("orbit_number_integer", "orbitNumber");
		output.getProductMetadataCustomObject().put("processing_level_integer", 0);
		fillwithKeyMapping("product_type_string", "productType");
		fillwithKeyMapping("cycle_number_integer", "cycleNumber");
		fillwithKeyMapping("processor_name_string", "procName");
		fillwithKeyMapping("processor_version_string", "procVersion");
	}
	
	private void s3l1CustomObject() {
		fillwithKeyMapping("beginning_date_time_date", "startTime");
		fillwithKeyMapping("ending_date_time_date", "stopTime");
		output.getProductMetadataCustomObject().put("platform_short_name_string", MissionId.toPlatformShortName(missionId));
		fillwithKeyMapping("platform_serial_identifier_string", "satelliteId");
		fillwithKeyMapping("instrument_short_name_string", "instrumentShortName");
		fillwithKeyMapping("orbit_number_integer", "orbitNumber");
		output.getProductMetadataCustomObject().put("processing_level_integer", 1);
		fillwithKeyMapping("product_type_string", "productType");
		fillwithKeyMapping("cloud_cover_double", "cloudPercentage");
	}
	
	private void s3l2CustomObject() {
		fillwithKeyMapping("beginning_date_time_date", "startTime");
		fillwithKeyMapping("ending_date_time_date", "stopTime");
		output.getProductMetadataCustomObject().put("platform_short_name_string", MissionId.toPlatformShortName(missionId));
		fillwithKeyMapping("platform_serial_identifier_string", "satelliteId");
		fillwithKeyMapping("instrument_short_name_string", "instrumentShortName");
		fillwithKeyMapping("orbit_number_integer", "orbitNumber");
		output.getProductMetadataCustomObject().put("processing_level_integer", 2);
		fillwithKeyMapping("product_type_string", "productType");
		fillwithKeyMapping("cloud_cover_double", "cloudPercentage");
	}
	
	private void fillwithKeyMapping(final String toKey, final String fromKey) {
		if ((catalogEvent.getMetadata().get(fromKey) != null)) {
			output.getProductMetadataCustomObject().put(toKey, catalogEvent.getMetadata().get(fromKey));
		}
	}
	

}
