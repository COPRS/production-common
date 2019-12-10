package esa.s1pdgs.cpoc.ipf.preparation.trigger.tasks;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

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
		return Integer.parseInt(getStringValue("channelId"));
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
		return getStringValue("datatakeId");
	}
	
	public final String swathType() {
		return getStringValue("swathtype");
	}
	
	public final String processMode() {
		return getStringValue("processMode", null);
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
		return event.getMetadata().findValuesAsText(name);
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
	
    private final String getStringValue(final String key, final String defaultValue)
    {
    	final JsonNode textNode = event.getMetadata().findValue(key);
    	if (textNode == null) {
    		return defaultValue;
    	}
    	return textNode.asText();
    }
}
