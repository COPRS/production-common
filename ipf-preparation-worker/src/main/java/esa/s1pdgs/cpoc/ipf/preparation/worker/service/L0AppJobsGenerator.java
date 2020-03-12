package esa.s1pdgs.cpoc.ipf.preparation.worker.service;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.AiopProperties;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGeneration;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.AbstractJobOrderConf;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;

public class L0AppJobsGenerator extends AbstractJobsGenerator {

    public final static DateTimeFormatter JO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    
	private static final Logger LOGGER = LogManager.getLogger(L0AppJobsGenerator.class);
	
	private Map<String,Map<String,String>> aiopProperties;
	
	private long minimalWaitingTimeSec;
	
	private boolean disableTimeout;
	
    public L0AppJobsGenerator(
    		final XmlConverter xmlConverter,
    		final MetadataClient metadataClient, 
    		final ProcessSettings l0ProcessSettings,
            final IpfPreparationWorkerSettings taskTablesSettings,
            final AppCatalogJobClient<CatalogEvent> appDataService,
            final AiopProperties aiopProperties, 
            final ProcessConfiguration processConfiguration,
            final MqiClient mqiClient,
			final BiFunction<String,TaskTableInput, Long> inputWaitTimeout, 
			final Supplier<LocalDateTime> dateSupplier,
			final String taskTableXmlName,
			final TaskTable taskTable,
			final ProductMode mode
    ) {
        super(xmlConverter, 
        		metadataClient, 
        		l0ProcessSettings, 
        		taskTablesSettings, 
        		appDataService, 
        		processConfiguration, 
        		mqiClient,
        		inputWaitTimeout,
        		dateSupplier,
        		taskTableXmlName,
        		taskTable,
        		mode    
        );
        
        minimalWaitingTimeSec = aiopProperties.getMinimalWaitingTimeSec();
        disableTimeout = aiopProperties.getDisableTimeout();
        
        this.aiopProperties = new HashMap<>();
        final Map<String, String> stationCodes = aiopProperties.getStationCodes();
        for (final String key : stationCodes.keySet()) {
        	final Map<String, String> map = new HashMap<>();
        	map.put("PT_Assembly", aiopProperties.getPtAssembly().get(key));
        	map.put("Processing_Mode", aiopProperties.getProcessingMode().get(key));
        	map.put("Reprocessing_Mode", aiopProperties.getReprocessingMode().get(key));
        	map.put("TimeoutSec", aiopProperties.getTimeoutSec().get(key));
        	map.put("Descramble", aiopProperties.getDescramble().get(key));
        	map.put("RSEncode", aiopProperties.getRsEncode().get(key));
        	
        	if (null == map.get("PT_Assembly") || null == map.get("Processing_Mode") ||
        	    null == map.get("Reprocessing_Mode") || null == map.get("TimeoutSec") ||
        	    null == map.get("Descramble") || null == map.get("RSEncode")) {
        	    	throw new RuntimeException(String.format("Invalid AIOP configuration for %s: Station_Code=%s, PT_Assembly=%s, Processing_Mode=%s, Reprocessing_Mode=%s, TimeoutSec=%s, Descramble=%s, RSEncode=%s",
        	    			key, stationCodes.get(key), map.get("PT_Assembly"), map.get("Processing_Mode"), map.get("Reprocessing_Mode"), map.get("TimeoutSec"), map.get("Descramble"), map.get("RSEncode")));
        	    }
        	
        	LOGGER.trace("Initializing AIOP parameter {} for station {} ", map, stationCodes.get(key));
        	this.aiopProperties.put(stationCodes.get(key), map);
        }
    }

    @Override
    protected void preSearch(final JobGeneration job) throws IpfPrepWorkerInputsMissingException {
    	
        final Map<String, String> missingRaws = new HashMap<>();
        
        if (job.getAppDataJob() != null && job.getAppDataJob().getProduct() != null) {
        	
            // Channel 1
            job.getAppDataJob().getProduct().getRaws1().forEach(raw -> {
                try {
                    final EdrsSessionMetadata file = this.metadataClient.getEdrsSession(
                    		"RAW", 
                    		new File(raw.getFilename()).getName()
                    );
                    if (file != null) {
                        raw.setKeyObs(file.getKeyObjectStorage());
                    } else {
                        missingRaws.put(raw.getFilename(), "No raw with name");
                    }
                } catch (final MetadataQueryException me) {
                    missingRaws.put(raw.getFilename(), me.getMessage());
                }
            });
            // Channel 2
            job.getAppDataJob().getProduct().getRaws2().forEach(raw -> {
                try {
                    final EdrsSessionMetadata file = this.metadataClient.getEdrsSession(
                    		"RAW", 
                    		new File(raw.getFilename()).getName()
                    );
                    if (file != null) {
                        raw.setKeyObs(file.getKeyObjectStorage());
                    } else {
                        missingRaws.put(raw.getFilename(), "No raw with name");
                    }
                } catch (final MetadataQueryException me) {
                    missingRaws.put(raw.getFilename(), me.getMessage());
                }
            });
            
            // S1PRO-1101: if timeout for primary search is reached -> just start the job 
 			if (!disableTimeout && checkTimeoutReached(job)) {
 				return;
 			}
        }
        
	    if (!missingRaws.isEmpty()) {
            throw new IpfPrepWorkerInputsMissingException(missingRaws);
        }
	    
	    // S1PRO-1065: Make sure that there is at least one RAW for each channel
	    if (job.getAppDataJob().getProduct().getRaws1().isEmpty()) {
	    	  throw new IpfPrepWorkerInputsMissingException(
	    			  Collections.singletonMap(
	    					  job.getAppDataJob().getProduct().getProductName(), 
	    					  "No raws for channel 1"
	    			  )
	    	);
	    }
	    if (job.getAppDataJob().getProduct().getRaws2().isEmpty()) {
	    	  throw new IpfPrepWorkerInputsMissingException(
	    			  Collections.singletonMap(
	    					  job.getAppDataJob().getProduct().getProductName(), 
	    					  "No raws for channel 2"
	    			  )
	    	);
	    }	    
    }

    @Override
    protected void customJobOrder(final JobGeneration job) {
    	final AbstractJobOrderConf conf = job.getJobOrder().getConf();
    	final AppDataJobProduct product = job.getAppDataJob().getProduct();
    	final boolean reprocessing = false; // currently no reprocessing supported
    	LOGGER.info("Configuring AIOP with station parameters for stationCode {} for product {}", product.getStationCode(), product.getProductName());

    	// collect parameters
    	
    	final Map<String,String> aiopParams = new HashMap<>();
    	aiopParams.put("Mission_Id", product.getMissionId() + product.getSatelliteId());
    	aiopParams.put("Processing_Station", product.getStationCode());
    	//FIXME
    	aiopParams.put("DownlinkTime",DateUtils.convertToAnotherFormat(product.getStartTime(),
    			AppDataJobProduct.TIME_FORMATTER,
                 JO_TIME_FORMATTER));
    	  
    	
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
    	
    	LOGGER.trace("Existing parameters: {}", conf.getProcParams());
    	LOGGER.trace("New AIOP parameters: {}", aiopParams);
    	
    	for (final Entry<String, String> newParam : aiopParams.entrySet()) {
    		boolean found = false;
    		if (null != conf.getProcParams()) {
        		for (final JobOrderProcParam existingParam : conf.getProcParams()) {
    				if (newParam.getKey().equals(existingParam.getName())) {
    					found = true;
    					existingParam.setValue(newParam.getValue());
    				}
        		}
        	}
    		if (!found) {
        		conf.addProcParam(new JobOrderProcParam(newParam.getKey(), newParam.getValue()));
			}
		}    	
    	LOGGER.debug("Configured AIOP for product {} with configuration {}", product.getProductName(), conf);
    }

    @Override
    protected void customJobDto(final JobGeneration job, final IpfExecutionJob dto) {
        // Add input relative to the channels
        if (job.getAppDataJob().getProduct() != null) {
            int nb1 = 0;
            int nb2 = 0;

            // Retrieve number of channels and sort them per alphabetic order
            nb1 = job.getAppDataJob().getProduct().getRaws1().size();
            job.getAppDataJob().getProduct().getRaws1().stream().sorted(
                    (p1, p2) -> p1.getFilename().compareTo(p2.getFilename()));

            nb2 = job.getAppDataJob().getProduct().getRaws2().size();
            job.getAppDataJob().getProduct().getRaws2().stream().sorted(
                    (p1, p2) -> p1.getFilename().compareTo(p2.getFilename()));

            // Add raw to the job order, one file per channel
            final int nb = Math.max(nb1, nb2);
            for (int i = 0; i < nb; i++) {
                if (i < nb1) {
                    final AppDataJobFile raw = job.getAppDataJob().getProduct().getRaws1().get(i);
                    dto.addInput(
                            new LevelJobInputDto(
                                    ProductFamily.EDRS_SESSION.name(),
                                    dto.getWorkDirectory() + "ch01/" + raw.getFilename(),
                                    raw.getKeyObs()));
                }
                if (i < nb2) {
                    final AppDataJobFile raw = job.getAppDataJob().getProduct().getRaws2().get(i);
                    dto.addInput(
                            new LevelJobInputDto(
                                    ProductFamily.EDRS_SESSION.name(),
                                    dto.getWorkDirectory() + "ch02/" + raw.getFilename(),
                                    raw.getKeyObs()));
                }
            }
        }
    }
    
	private boolean checkTimeoutReached(final JobGeneration job) {

		final String stationCode = job.getAppDataJob().getProduct().getStationCode();
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
		final String downlinkEndTimeUTC = job.getAppDataJob().getProduct().getStopTime();
		final long currentTimeMs = System.currentTimeMillis();

		if (timeoutReachedForPrimarySearch(downlinkEndTimeUTC, currentTimeMs, startToWaitMs, minimalWaitingTimeMs,
				timeoutForDownlinkStationMs)) {
			final AppDataJobProduct product = job.getAppDataJob().getProduct();
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
