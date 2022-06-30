package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.report;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

public class ProductMetadataCustomObjectFiller {
	
	final CatalogEvent catalogEvent;
	final MetadataExtractionReportingOutput output;
	
	public ProductMetadataCustomObjectFiller(final CatalogEvent catalogEvent, MetadataExtractionReportingOutput output) {
		this.catalogEvent = catalogEvent;
		this.output = output;
	}
	
	public void fillCustomObject() {
		
		// Sentinel-1 Custom Object
		if ((catalogEvent.getProductFamily() == ProductFamily.L0_SEGMENT) || 
				(catalogEvent.getProductFamily() == ProductFamily.L0_SLICE) || 
				(catalogEvent.getProductFamily() == ProductFamily.L0_ACN)) {
			
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
			fillwithKeyMapping("coordinates_object", "coordinates");

		} else if ((catalogEvent.getProductFamily() == ProductFamily.S2_L0_DS) || 
					(catalogEvent.getProductFamily() == ProductFamily.S2_L0_GR)) {
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
			fillwithKeyMapping("coordinates_object", "coordinates");
			
		} else if (catalogEvent.getProductFamily() == ProductFamily.S3_L0) {
			fillwithKeyMapping("beginning_date_time_date", "startTime");
			fillwithKeyMapping("ending_date_time_date", "stopTime");
			fillwithKeyMapping("platform_short_name_string", "platformShortName");
			fillwithKeyMapping("platform_serial_identifier_string", "platformSerialIdentifier");
			fillwithKeyMapping("instrument_short_name_string", "instrumentName");
			fillwithKeyMapping("orbit_number_integer", "orbitNumber");
			fillwithKeyMapping("processing_level_integer", "processingLevel");
			fillwithKeyMapping("product_type_string", "productType");
			fillwithKeyMapping("cycle_number_integer", "cycleNumber");
			fillwithKeyMapping("processor_name_string", "procName");
			fillwithKeyMapping("processor_version_string", "procVersion");
			fillwithKeyMapping("coordinates_object", "sliceCoordinates");
		}
	}
	
	private void fillwithKeyMapping(final String toKey, final String fromKey) {
		if ((catalogEvent.getMetadata().get(fromKey) != null)) {
			output.getProductMetadataCustomObject().put(toKey, catalogEvent.getMetadata().get(fromKey));
		}
	}
	

}
