package esa.s1pdgs.cpoc.production.trigger.consumption;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.production.trigger.appcat.AppCatAdapter;

public final class L0SliceConsumer implements ProductTypeConsumptionHandler {  
	private static final String TYPE = "L0Slice";
	private static final Logger LOGGER = LogManager.getLogger(L0SliceConsumer.class);
	
    @Override
	public final String type() {
		return TYPE;
	}
    
	@Override
	public final Optional<AppDataJob> findAssociatedJobFor(final AppCatAdapter appCat, final CatalogEventAdapter catEvent) 
			throws AbstractCodedException {
		return Optional.empty();
	}
	
	@Override
	public boolean isReady(final AppDataJob job, final String productName) {
        if (job.getState() != AppDataJobState.WAITING) {
            LOGGER.info("AppDataJob {} ({}) for {} {} already dispatched", job.getId(), 
            		job.getState(), TYPE, productName);
            return false;
        }
        return true;
	}

	@Override
	public final AppDataJobProduct newProductFor(final GenericMessageDto<CatalogEvent> mqiMessage) {
		final CatalogEvent event = mqiMessage.getBody();		
		final CatalogEventAdapter eventAdapter = new CatalogEventAdapter(event);
		
        final AppDataJobProduct productDto = new AppDataJobProduct();
        
        // FIXME added product type because it was missing. Check if this has any side effects
        productDto.setProductType(event.getProductType());
        
        productDto.setAcquisition(eventAdapter.swathType());
        productDto.setMissionId(eventAdapter.missionId());
        productDto.setProductName(event.getKeyObjectStorage());
        productDto.setProcessMode(eventAdapter.processMode());
        productDto.setSatelliteId(eventAdapter.satelliteId());
        productDto.setStartTime(eventAdapter.startTime());
        productDto.setStopTime(eventAdapter.stopTime());
        //TODO figure out if is relevant for L1/L2.
        //productDto.setStationCode(eventAdapter.stationCode());   
       	productDto.setPolarisation(eventAdapter.polarisation()); 
       	return productDto;
	}
}
