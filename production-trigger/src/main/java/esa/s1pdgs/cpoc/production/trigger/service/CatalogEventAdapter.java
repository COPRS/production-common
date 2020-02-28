package esa.s1pdgs.cpoc.production.trigger.service;

import java.util.List;

import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

public final class CatalogEventAdapter {

	private final CatalogEvent event;

	public CatalogEventAdapter(final CatalogEvent event) {
		this.event = event;
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
		return getStringValue("swathtype");
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
