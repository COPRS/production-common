package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.AiopProperties;

public final class AiopPropertiesAdapter {
	private static final Logger LOGGER = LogManager.getLogger(AiopPropertiesAdapter.class); 
	
	private static final DateTimeFormatter JO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
	
	private Map<String,Map<String,String>> aiopProperties;
	private long minimalWaitingTimeSec;	
	private boolean disableTimeout;
	
	public AiopPropertiesAdapter(
			final Map<String, Map<String, String>> aiopProperties, 
			final long minimalWaitingTimeSec,
			final boolean disableTimeout
		) {
		this.aiopProperties = aiopProperties;
		this.minimalWaitingTimeSec = minimalWaitingTimeSec;
		this.disableTimeout = disableTimeout;
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
        		props.getDisableTimeout()
        );
	}
	
	public final boolean isTimedOut(final AppDataJob job) {
        // S1PRO-1101: if timeout for primary search is reached -> just start the job 
		return (!disableTimeout && checkTimeoutReached(job));

	}
	
	public final Map<String,String> aiopPropsFor(final AppDataJob job) {		
    	final AppDataJobProduct product = job.getProduct();
    	final boolean reprocessing = false; // currently no reprocessing supported
    	LOGGER.info("Configuring AIOP with station parameters for stationCode {} for product {}", product.getStationCode(), product.getProductName());

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
    	return aiopParams;
	}
	
    
	private boolean checkTimeoutReached(final AppDataJob job) {

		final String stationCode = job.getProduct().getStationCode();
		final Map<String, String> propForStationCode = aiopProperties.get(stationCode);
		if (propForStationCode == null) {
			LOGGER.warn("no configuration found for station code -> not timeout check");
			return false;
		}
		final long timeoutForDownlinkStationMs = Long.valueOf(propForStationCode.get("TimeoutSec")) * 1000;
		final long minimalWaitingTimeMs = this.minimalWaitingTimeSec * 1000;

		// the creation date of the job is used for the start of waiting
		final long startToWaitMs = job.getGeneration().getCreationDate().toInstant().toEpochMilli();

		// the "stop time" of the product (DSIB) is the downlink-end time
		final String downlinkEndTimeUTC = job.getProduct().getStopTime();
		final long currentTimeMs = System.currentTimeMillis();

		if (timeoutReachedForPrimarySearch(downlinkEndTimeUTC, currentTimeMs, startToWaitMs, minimalWaitingTimeMs,
				timeoutForDownlinkStationMs)) {
			final AppDataJobProduct product = job.getProduct();
			LOGGER.warn("Timeout reached for stationCode {} and product {}", product.getStationCode(),
					product.getProductName());
			return true;
		}
		return false;
	}

	boolean timeoutReachedForPrimarySearch(final String downlinkEndTimeUTC, final long currentTimeMs, final long startToWaitMs,
			final long minimalWaitingTimeMs, final long timeoutForDownlinkStationMs) {

		boolean timeout = false;

		final long downlinkEndTimeMs = DateUtils.parse(downlinkEndTimeUTC).toInstant(ZoneOffset.UTC).toEpochMilli();

		final long timeoutEndTimestampMs = Math.max(startToWaitMs + minimalWaitingTimeMs,
				downlinkEndTimeMs + timeoutForDownlinkStationMs);

		final String timeoutEndTimestampUTC = DateUtils.formatToMetadataDateTimeFormat(
				Instant.ofEpochMilli(timeoutEndTimestampMs).atOffset(ZoneOffset.UTC).toLocalDateTime());

		final String currentTimeUTC = DateUtils.formatToMetadataDateTimeFormat(
				Instant.ofEpochMilli(currentTimeMs).atOffset(ZoneOffset.UTC).toLocalDateTime());

		if (LOGGER.isTraceEnabled()) {

			final String startToWaitUTC = DateUtils.formatToMetadataDateTimeFormat(
					Instant.ofEpochMilli(startToWaitMs).atOffset(ZoneOffset.UTC).toLocalDateTime());

			LOGGER.trace("downlink-end time: {}", downlinkEndTimeUTC);
			LOGGER.trace("starting-to-wait time: {}", startToWaitUTC);
			LOGGER.trace("minimal waiting time in millis: {}", minimalWaitingTimeMs);
			LOGGER.trace("timeout for downlink station in millis: {}", timeoutForDownlinkStationMs);
			LOGGER.trace("timeout-end timestamp in epoch millis MAX({} + {}, {} + {}) = {}", startToWaitUTC,
					minimalWaitingTimeMs, downlinkEndTimeUTC, timeoutForDownlinkStationMs, timeoutEndTimestampUTC);

		}

		if (currentTimeMs > timeoutEndTimestampMs) {
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
