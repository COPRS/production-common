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

package esa.s1pdgs.cpoc.preparation.worker.type.edrs;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.preparation.worker.config.type.AiopProperties;

public final class AiopPropertiesAdapter {
	private static final Logger LOGGER = LogManager.getLogger(AiopPropertiesAdapter.class); 
	
	private static final DateTimeFormatter JO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
	
	private Map<String,Map<String,String>> aiopProperties;
	private long minimalWaitingTimeSec;	
	private boolean disableTimeout;
	private String nrtOutputPath;
	private String ptOutputPath;
	
	public AiopPropertiesAdapter(
			final Map<String, Map<String, String>> aiopProperties, 
			final long minimalWaitingTimeSec,
			final boolean disableTimeout,
			final String nrtOutputPath,
			final String ptOutputPath			
		) {
		this.aiopProperties = aiopProperties;
		this.minimalWaitingTimeSec = minimalWaitingTimeSec;
		this.disableTimeout = disableTimeout;
		this.nrtOutputPath = nrtOutputPath;
		this.ptOutputPath = ptOutputPath;
	}

	public static final AiopPropertiesAdapter of(final AiopProperties props) {        
        final Map<String,Map<String,String>> aiopProperties = new HashMap<>();
        
        final Map<String, String> stationCodes = props.getStationCodes();
        for (final String key : stationCodes.keySet()) {
        	final Map<String, String> map = new HashMap<>();
        	map.put("PT_Assembly", props.getPtAssembly().get(key));
        	map.put("Processing_Mode", props.getProcessingMode().get(key));
        	map.put("Reprocessing_Mode", props.getReprocessingMode().get(key));
        	map.put("TimeoutSec", props.getTimeoutSec().get(key));
        	map.put("Descramble", props.getDescramble().get(key));
        	map.put("RSEncode", props.getRsEncode().get(key));
        	
        	if (null == map.get("PT_Assembly") || null == map.get("Processing_Mode") ||
        	    null == map.get("Reprocessing_Mode") || null == map.get("TimeoutSec") ||
        	    null == map.get("Descramble") || null == map.get("RSEncode")) {
        	    	throw new RuntimeException(String.format("Invalid AIOP configuration for %s: Station_Code=%s, PT_Assembly=%s, Processing_Mode=%s, Reprocessing_Mode=%s, TimeoutSec=%s, Descramble=%s, RSEncode=%s",
        	    			key, stationCodes.get(key), map.get("PT_Assembly"), map.get("Processing_Mode"), map.get("Reprocessing_Mode"), map.get("TimeoutSec"), map.get("Descramble"), map.get("RSEncode")));
        	}
        	
        	LOGGER.trace("Initializing AIOP parameter {} for station {} ", map, stationCodes.get(key));
        	aiopProperties.put(stationCodes.get(key), map);
        }
        return new AiopPropertiesAdapter(
        		aiopProperties, 
        		props.getMinimalWaitingTimeSec(),  
        		props.getDisableTimeout(),
        		props.getNrtOutputPath(),
        		props.getPtOutputPath()
        );
	}
	
	public final boolean isTimedOut(final AppDataJob job) {
        // S1PRO-1101: if timeout for primary search is reached -> just start the job 
		return (!disableTimeout && checkTimeoutReached(job));

	}
	
	public final Map<String,String> aiopPropsFor(final AppDataJob job) {		
    	final boolean reprocessing = false; // currently no reprocessing supported
    	
    	final EdrsSessionProduct product = EdrsSessionProduct.of(job);
    	
    	LOGGER.info("Configuring AIOP with station parameters for stationCode {} for product {}", 
    			product.getStationCode(), product.getProductName());

    	final Map<String,String> aiopParams = new HashMap<>();
    	aiopParams.put("Mission_Id", product.getMissionId() + product.getSatelliteId());
    	aiopParams.put("Processing_Station", product.getStationCode());
    	//FIXME
    	aiopParams.put("DownlinkTime",DateUtils.convertToAnotherFormat(product.getStartTime(),
    			AppDataJobProduct.TIME_FORMATTER,
    			JO_TIME_FORMATTER
    	));
    	  
    	
    	//FIXME
    	String stationCode ="WILE";
    	if (product.getStationCode() !=null) {
    		stationCode = product.getStationCode();
    		LOGGER.info("**** stationCode found: {} ****", stationCode);    		
    	} else {
    		LOGGER.warn("**** stationCode is null, choosing default ****");    		
    	}
    	
    	for (final Entry<String,String> entrySet : aiopProperties.get(stationCode).entrySet()) {
    		switch(entrySet.getKey()) {
    			case "Processing_Mode":
    				if (!reprocessing) {
    	    			aiopParams.put(entrySet.getKey(), entrySet.getValue());    					
    				}
    				break;
    			case "Reprocessing_Mode":
    				if (reprocessing) {
    	    			aiopParams.put("Processing_Mode", entrySet.getValue());    					
    				}
    				break;
    			default:
    				aiopParams.put(entrySet.getKey(), entrySet.getValue());
    		} 
    	}    	
    	aiopParams.put("Processing_Station", stationCode);    	
    	aiopParams.put("NRTOutputPath", nrtOutputPath.replace("%WORKING_DIR_NUMBER%", Long.toString(job.getId())));
    	aiopParams.put("PTOutputPath", ptOutputPath.replace("%WORKING_DIR_NUMBER%", Long.toString(job.getId())));
    	return aiopParams;
	}
	
	public Date calculateTimeout(final AppDataJob job) {
		final EdrsSessionProduct product = EdrsSessionProduct.of(job);

		final String stationCode = product.getStationCode();
		final Map<String, String> propForStationCode = aiopProperties.get(stationCode);
		if (propForStationCode == null) {
			LOGGER.warn("no configuration found for station code -> not timeout check");
			return null;
		}
		final long timeoutForDownlinkStationMs = Long.valueOf(propForStationCode.get("TimeoutSec")) * 1000;
		final long minimalWaitingTimeMs = this.minimalWaitingTimeSec * 1000;

		// the creation date of the job is used for the start of waiting
		final long startToWaitMs = job.getGeneration().getCreationDate().toInstant().toEpochMilli();

		// the "stop time" of the product (DSIB) is the downlink-end time
		final String downlinkEndTimeUTC = product.getStopTime();

		final long downlinkEndTimeMs = DateUtils.parse(downlinkEndTimeUTC).toInstant(ZoneOffset.UTC).toEpochMilli();

		final long timeoutEndTimestampMs = Math.max(startToWaitMs + minimalWaitingTimeMs,
				downlinkEndTimeMs + timeoutForDownlinkStationMs);
		
		if (LOGGER.isTraceEnabled()) {
			final String startToWaitUTC = DateUtils.formatToMetadataDateTimeFormat(
					Instant.ofEpochMilli(startToWaitMs).atOffset(ZoneOffset.UTC).toLocalDateTime());
			
			final String timeoutEndTimestampUTC = DateUtils.formatToMetadataDateTimeFormat(
					Instant.ofEpochMilli(timeoutEndTimestampMs).atOffset(ZoneOffset.UTC).toLocalDateTime());

			LOGGER.trace("downlink-end time: {}", downlinkEndTimeUTC);
			LOGGER.trace("starting-to-wait time: {}", startToWaitUTC);
			LOGGER.trace("minimal waiting time in millis: {}", minimalWaitingTimeMs);
			LOGGER.trace("timeout for downlink station in millis: {}", timeoutForDownlinkStationMs);
			LOGGER.trace("timeout-end timestamp in epoch millis MAX({} + {}, {} + {}) = {}", startToWaitUTC,
					minimalWaitingTimeMs, downlinkEndTimeUTC, timeoutForDownlinkStationMs, timeoutEndTimestampUTC);
		}
		
		return new Date(timeoutEndTimestampMs);
	}
	
	private boolean checkTimeoutReached(final AppDataJob job) {
		boolean timeout = false;
		
		final Date timeoutDate = calculateTimeout(job);
		final Date currentDate = new Date();
		
		// Formatting for debug output
		final String currentTimeUTC = DateUtils.formatToMetadataDateTimeFormat(
				currentDate.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime());
		
		final String timeoutEndTimestampUTC = DateUtils.formatToMetadataDateTimeFormat(
				timeoutDate.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime());
		
		if (currentDate.after(timeoutDate)) {
			LOGGER.debug("current time {} is greater than timeout-end timestamp {}", currentTimeUTC,
					timeoutEndTimestampUTC);
			timeout = true;
		} else {
			LOGGER.debug("current time {} is less than timeout-end timestamp {}", currentTimeUTC,
					timeoutEndTimestampUTC);
		}

		LOGGER.debug("timeout reached: {}", timeout);

		return timeout;
	}
}
