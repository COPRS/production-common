package esa.s1pdgs.cpoc.production.trigger.consumption;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.production.trigger.appcat.AppCatAdapter;

public final class EdrsSessionConsumer implements ProductTypeConsumptionHandler {	
	private static final String TYPE = "EdrsSession";
	private static final Logger LOGGER = LogManager.getLogger(EdrsSessionConsumer.class);
	
    @Override
	public final String type() {
		return TYPE;
	}

	@Override
	public final boolean isReady(final AppDataJob job, final String productName) {
		if (job.getMessages().size() != 2) {
			 LOGGER.info("AppDataJob {} ({}) for {} {} not ready to be dispatched (# of messages {}/2)", 
					 job.getId(), job.getState(), TYPE, productName, job.getMessages().size());
			 return false;
		}
		if (job.getState() != AppDataJobState.WAITING) {
             LOGGER.info("AppDataJob {} ({}) for {} {} already dispatched", job.getId(), 
            		job.getState(), TYPE, productName);
			 return false;
		}
		return true;
	}
    
	@Override
	public Optional<AppDataJob> findAssociatedJobFor(final AppCatAdapter appCat, final CatalogEventAdapter catEvent) 
			throws AbstractCodedException {
		return appCat.findJobForSession(catEvent.sessionId());
	}
	
	@Override
	public final AppDataJobProduct newProductFor(final GenericMessageDto<CatalogEvent> mqiMessage) {
		final CatalogEvent event = mqiMessage.getBody();		
		final CatalogEventAdapter eventAdapter = new CatalogEventAdapter(event);
		
        final AppDataJobProduct productDto = new AppDataJobProduct();
        productDto.setProductType(event.getProductType());
        productDto.setSessionId(eventAdapter.sessionId());
        productDto.setMissionId(eventAdapter.missionId());
        productDto.setStationCode(eventAdapter.stationCode());
        productDto.setProductName(eventAdapter.sessionId());
        productDto.setSatelliteId(eventAdapter.satelliteId());
        productDto.setStartTime(eventAdapter.startTime());
        productDto.setStopTime(eventAdapter.stopTime());
        addRawsFor(productDto, eventAdapter);
        return productDto;
	}
	 
    @Override
    public boolean mergeMessageInto(final GenericMessageDto<CatalogEvent> mqiMessage, final AppDataJob job) {	 	
		final CatalogEventAdapter eventAdapter = new CatalogEventAdapter(mqiMessage.getBody());
		
		LOGGER.trace("== existing message {}", job.getMessages());
		if (job.getMessages().size() != 1) {
	        LOGGER.warn("Expected one message at job {} but was {}. Ignoring incoming message {}...",
	        		job.getId(), job.getMessages().size(), mqiMessage.getId());
	        return false;
		}
		final int channelIdOfExistingMess =  preExistingChannelIdFor(job);
		LOGGER.trace("channel id of existing message at job {} is {}", job.getId(), channelIdOfExistingMess);
		if (channelIdOfExistingMess == eventAdapter.channelId()) {
	        LOGGER.warn("Message for channel {} at job {} has already been consumed before. "
	        		+ "Ignoring incoming message {}...",
	        		channelIdOfExistingMess, job.getId(), mqiMessage.getId());	        
	        return false;
		}
	
		LOGGER.info("Adding message {} and raws for channel {} to appDataJob {}", mqiMessage.getId(), 
				eventAdapter.channelId(), job.getId());        	
    	// add the new message and the new raws
    	job.getMessages().add(mqiMessage);
    	addRawsFor(job.getProduct(), eventAdapter);
    	return true;
	}
    
    private final int preExistingChannelIdFor(final AppDataJob job) {
		final GenericMessageDto<CatalogEvent> firstMess = job.getMessages().get(0);        
		LOGGER.trace("== firstMessage {}", firstMess.toString());
	 	return new CatalogEventAdapter(firstMess.getBody()).channelId();
    }
             
	private final void addRawsFor(final AppDataJobProduct product, final CatalogEventAdapter eventAdapter) {
        if (eventAdapter.channelId() == 1) {
            LOGGER.debug ("== ch1 ");    
            product.setRaws1(raws(eventAdapter));
        } else {
        	LOGGER.debug ("== ch2 ");
        	product.setRaws2(raws(eventAdapter));
        }
	}

	private final List<AppDataJobFile> raws(final CatalogEventAdapter eventAdapter) {
    	return eventAdapter.listValues("rawNames").stream()
    			.map(s -> new AppDataJobFile(s))
                .collect(Collectors.toList());
    }
}
