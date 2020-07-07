package esa.s1pdgs.cpoc.production.trigger.consumption;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public final class L0SegmentConsumer implements ProductTypeConsumptionHandler {   
	public static final String TYPE = "L0Segment";
	private static final Logger LOGGER = LogManager.getLogger(L0SegmentConsumer.class);
	
	@Override
	public final String type() {
		return TYPE;
	}


	@Override
	public AppDataJobProduct newProductFor(final GenericMessageDto<CatalogEvent> mqiMessage) {
		final CatalogEvent event = mqiMessage.getBody();		
		final CatalogEventAdapter eventAdapter = new CatalogEventAdapter(event);
		
        final AppDataJobProduct productDto = new AppDataJobProduct();
        
        // FIXME added product type because it was missing. Check if this has any side effects
        productDto.setProductType(event.getProductType());
        
        productDto.setAcquisition(eventAdapter.swathType());
        productDto.setMissionId(eventAdapter.missionId());
        productDto.setDataTakeId(eventAdapter.datatakeId());
        productDto.setProductName("l0_segments_for_" + eventAdapter.datatakeId());
        productDto.setProcessMode(eventAdapter.processMode());
        productDto.setSatelliteId(eventAdapter.satelliteId());
        return productDto;
	}
}
