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

package esa.s1pdgs.cpoc.mqi.model.queue.util;

import java.util.List;

import org.springframework.util.Assert;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public final class CatalogEventAdapter {
	
	public static final String NOT_DEFINED = "NOT_DEFINED";

	private final CatalogEvent event;

	public CatalogEventAdapter(final CatalogEvent event) {
		this.event = event;
	}
	
	public static final CatalogEventAdapter of(final AppDataJob job) {
		Assert.isTrue(!job.getCatalogEvents().isEmpty(), "Missing CatalogEvent in job " + job.getId());		
		final CatalogEvent event = job.getCatalogEvents().get(0);				
		return new CatalogEventAdapter(event);
	}
	
	public static final CatalogEventAdapter of(final CatalogEvent catalogEvent) {
		return new CatalogEventAdapter(catalogEvent);
    }
	
	public final String productName() {
		return getStringValue("productName", "NOT_KNOWN");
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
		return getStringValue(MissionId.FIELD_NAME);
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

	public final String validityStartTime() {
		return getStringValue("validityStartTime");
	}

	public final String validityStopTime() {
		return getStringValue("validityStopTime");
	}
	
	public final String polarisation() {
		return getStringValue("polarisation");
	}
	
	public final String timeliness() {
		return getStringValue("timeliness", "");
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
	
	public final List<String> rawNames() {
		return listValues("rawNames");
	}
	
	public final Long qualityNumOfMissingElements() {
		return getLongValue("qualityNumOfMissingElements");
	}
	 
	public final Long qualityNumOfCorruptedElements() {
		return getLongValue("qualityNumOfCorruptedElements");
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
    
	private final Long getLongValue(final String key) {
		if (event.getMetadata().get(key) == null) {
			return null;
		} else {
			return (long) event.getMetadata().get(key);
		}
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
