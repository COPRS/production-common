package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.report;

import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

public class ProductMetadataCustomObjectFiller {
	
	final CatalogEvent catalogEvent;
	final MetadataExtractionReportingOutput output;
	
	public ProductMetadataCustomObjectFiller(final CatalogEvent catalogEvent, MetadataExtractionReportingOutput output) {
		this.catalogEvent = catalogEvent;
		this.output = output;
	}
	
	public void fillCustomObject() {
		
		switch (catalogEvent.getProductFamily()) {
		    // Session files Custom Object
			case EDRS_SESSION:
				fillwithKeyMapping("platform_short_name_string", "platformShortName");
				fillwithKeyMapping("platform_serial_identifier_string", "platformSerialIdentifier");
				output.getProductMetadataCustomObject().put("channel_identifier_integer", (Integer) catalogEvent.getMetadata().get("channelId"));
				break;
			// Sentinel-1 Custom Object
			case L0_SEGMENT:
			case L0_SLICE:
			case L0_ACN:
				output.getProductMetadataCustomObject().put("processing_level_integer", 0);
				fillwithKeyMapping("beginning_date_time_date", "startTime");
				fillwithKeyMapping("ending_date_time_date", "stopTime");
				fillwithKeyMapping("platform_short_name_string", "platformShortName");
				fillwithKeyMapping("platform_serial_identifier_string", "platformSerialIdentifier");
				fillwithKeyMapping("operational_mode_string", "operationalMode");
				fillwithKeyMapping("product_class_string", "productClass");
				fillwithKeyMapping("product_consolidation_string", "productConsolidation");
				fillwithKeyMapping("product_sensing_consolidation_string", "productSensingConsolidation");
				fillwithKeyMapping("datatake_id_integer", "missionDataTakeId");
				fillwithKeyMapping("slice_product_flag_boolean", "sliceProductFlag");
				fillwithKeyMapping("polarisation_channels_string", "polarisationChannels");
				fillwithKeyMapping("orbit_number_integer", "absoluteStartOrbit");
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
				output.getProductMetadataCustomObject().put("processing_level_integer", 1);
				fillwithKeyMapping("beginning_date_time_date", "startTime");
				fillwithKeyMapping("ending_date_time_date", "stopTime");
				fillwithKeyMapping("platform_short_name_string", "platformShortName");
				fillwithKeyMapping("platform_serial_identifier_string", "platformSerialIdentifier");
				fillwithKeyMapping("instrument_short_name_string", "instrumentShortName");
				fillwithKeyMapping("product_type_string", "productType");
				break;
			case L2_ACN:
			case L2_SLICE:
				output.getProductMetadataCustomObject().put("processing_level_integer", 2);
				fillwithKeyMapping("beginning_date_time_date", "startTime");
				fillwithKeyMapping("ending_date_time_date", "stopTime");
				fillwithKeyMapping("platform_short_name_string", "platformShortName");
				fillwithKeyMapping("platform_serial_identifier_string", "platformSerialIdentifier");
				fillwithKeyMapping("instrument_short_name_string", "instrumentShortName");
				fillwithKeyMapping("product_type_string", "productType");
				break;
			// Sentinel-2 Custom Object
			case S2_L0_DS:
			case S2_L0_GR:
				output.getProductMetadataCustomObject().put("processing_level_integer", 0);
				fillwithKeyMapping("product_group_id", "productGroupId");
				fillwithKeyMapping("beginning_date_time_date", "startTime");
				fillwithKeyMapping("ending_date_time_date", "stopTime");
				fillwithKeyMapping("orbit_number_integer", "orbitNumber");
				fillwithKeyMapping("product_type_string", "productType");
				fillwithKeyMapping("platform_serial_identifier_string", "platformSerialIdentifier");
				fillwithKeyMapping("platform_short_name_string", "platfomShortName");
				fillwithKeyMapping("processor_version_string", "processorVersion");
				fillwithKeyMapping("quality_status_integer", "qualityStatus");
				fillwithKeyMapping("instrument_short_name_string", "instrumentShortName");
				break;
			case S2_L1A_DS:
			case S2_L1A_GR:
			case S2_L1B_DS:
			case S2_L1B_GR:
			case S2_L1C_DS:
			case S2_L1C_TC:
			case S2_L1C_TL:
				output.getProductMetadataCustomObject().put("processing_level_integer", 1);
				fillwithKeyMapping("beginning_date_time_date", "startTime");
				fillwithKeyMapping("ending_date_time_date", "stopTime");
				fillwithKeyMapping("platform_short_name_string", "platformShortName");
				fillwithKeyMapping("platform_serial_identifier_string", "platformSerialIdentifier");
				fillwithKeyMapping("instrument_short_name_string", "instrumentShortName");
				fillwithKeyMapping("product_type_string", "productType");
				break;
			case S2_L2A_DS:
			case S2_L2A_TL:
				output.getProductMetadataCustomObject().put("processing_level_integer", 2);
				fillwithKeyMapping("beginning_date_time_date", "startTime");
				fillwithKeyMapping("ending_date_time_date", "stopTime");
				fillwithKeyMapping("platform_short_name_string", "platformShortName");
				fillwithKeyMapping("platform_serial_identifier_string", "platformSerialIdentifier");
				fillwithKeyMapping("instrument_short_name_string", "instrumentShortName");
				fillwithKeyMapping("product_type_string", "productType");
				break;
			// Sentinel-3 Custom Object
			case S3_L0:
				output.getProductMetadataCustomObject().put("processing_level_integer", 0);
				fillwithKeyMapping("beginning_date_time_date", "startTime");
				fillwithKeyMapping("ending_date_time_date", "stopTime");
				fillwithKeyMapping("platform_short_name_string", "platformShortName");
				fillwithKeyMapping("platform_serial_identifier_string", "platformSerialIdentifier");
				fillwithKeyMapping("instrument_short_name_string", "instrumentName");
				fillwithKeyMapping("orbit_number_integer", "orbitNumber");
				fillwithKeyMapping("product_type_string", "productType");
				fillwithKeyMapping("cycle_number_integer", "cycleNumber");
				fillwithKeyMapping("processor_name_string", "procName");
				fillwithKeyMapping("processor_version_string", "procVersion");
				break;
			case S3_L1_NRT:
			case S3_L1_NTC:
			case S3_L1_STC:
				output.getProductMetadataCustomObject().put("processing_level_integer", 1);
				fillwithKeyMapping("beginning_date_time_date", "startTime");
				fillwithKeyMapping("ending_date_time_date", "stopTime");
				fillwithKeyMapping("platform_short_name_string", "platformShortName");
				fillwithKeyMapping("platform_serial_identifier_string", "platformSerialIdentifier");
				fillwithKeyMapping("instrument_short_name_string", "instrumentShortName");
				fillwithKeyMapping("product_type_string", "productType");
				break;
			case S3_L2_NRT:
			case S3_L2_NTC:
			case S3_L2_STC:
				output.getProductMetadataCustomObject().put("processing_level_integer", 2);
				fillwithKeyMapping("beginning_date_time_date", "startTime");
				fillwithKeyMapping("ending_date_time_date", "stopTime");
				fillwithKeyMapping("platform_short_name_string", "platformShortName");
				fillwithKeyMapping("platform_serial_identifier_string", "platformSerialIdentifier");
				fillwithKeyMapping("instrument_short_name_string", "instrumentShortName");
				fillwithKeyMapping("product_type_string", "productType");
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
	
	private void fillwithKeyMapping(final String toKey, final String fromKey) {
		if ((catalogEvent.getMetadata().get(fromKey) != null)) {
			output.getProductMetadataCustomObject().put(toKey, catalogEvent.getMetadata().get(fromKey));
		}
	}
	

}
