package esa.s1pdgs.cpoc.mqi.model.queue.util;

import java.util.List;

import org.springframework.util.Assert;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.MessageDto;

public final class CatalogEventAdapter {
	
	public static final String NOT_DEFINED = "NOT_DEFINED";

	private final CatalogEvent event;

	public CatalogEventAdapter(final CatalogEvent event) {
		this.event = event;
	}
	
	public static final CatalogEventAdapter of(final AppDataJob job) {
		Assert.isTrue(!job.getMessages().isEmpty(), "Missing message in job " + job.getId());		
		final GenericMessageDto<CatalogEvent> mqiMessage = job.getMessages().get(0);				
		return new CatalogEventAdapter(mqiMessage.getBody());
	}
	
	public static final CatalogEventAdapter of(final MessageDto<CatalogEvent> mqiMessage) {
		return new CatalogEventAdapter(mqiMessage.getDto());
	}
	
	public final String productType() {
		return getStringValue("productType", "NOT_KNOWN");
	}
	
	public final String sessionId() {
		return getStringValue("sessionId");
	}
	
	public final int channelId() {
		return getIntegerValue("channelId");
	}
	
	public final String stationCode() {
		return getStringValue("stationCode");
	}
	
	public final String satelliteId() {
		return getStringValue("satelliteId");
	}
	
	public final String missionId() {
		return getStringValue("missionId");
	}
	
	public final String datatakeId() {
		return getStringValue("dataTakeId");
	}
	
	public final String swathType() {
		return getStringValue("swathtype","UNDEFINED");
	}
	
	public final String processMode() {
		return getStringValue("processMode", "NOMINAL");
	}
	
	public final String startTime() {
		return getStringValue("startTime");		
	}
	
	public final String stopTime() {
		return getStringValue("stopTime");		
	}
	
	public final String polarisation() {
		return getStringValue("polarisation");
	}
	
	public final String timeliness() {
		return getStringValue("timeliness", null);
	}
	
	public final String productConsolidation() {
		return getStringValue("productConsolidation", NOT_DEFINED);
	}
	
	public final String productSensingConsolidation() {
		return getStringValue("productSensingConsolidation", NOT_DEFINED);
	}
	
	public final String productSensingStartDate() {
		return getStringValue("startTime", NOT_DEFINED);
	}
	
	public final String productSensingStopDate() {
		return getStringValue("stopTime", NOT_DEFINED);
	}
	
	public final List<String> listValues(final String name) {
		return (List<String>) event.getMetadata().get(name);
	}

    private final String getStringValue(final String key)
    {
		final String value = getStringValue(key, null);
    	if (value == null) {
    		throw new IllegalArgumentException(
    				String.format("Missing metadata element '%s'", key)
    		);
    	}
    	return value;
    }
    
    private final Integer getIntegerValue(final String key) {
    	final Integer value = (int) event.getMetadata().get(key);
    	return value;
    }
	
    private final String getStringValue(final String key, final String defaultValue)
    {
    	final String text = (String) event.getMetadata().get(key);    	
    	if (text == null) {
    		return defaultValue;
    	}
    	return text;
    }
}
