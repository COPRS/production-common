package esa.s1pdgs.cpoc.ipf.preparation.worker.tasks.edrssession;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.AbstractJobOrderConf;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.ipf.preparation.worker.service.XmlConverter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.service.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.tasks.AbstractJobsGenerator;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;

public class L0AppJobsGenerator extends AbstractJobsGenerator<CatalogEvent> {

    public final static DateTimeFormatter JO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    
	private static final Logger LOGGER = LogManager.getLogger(L0AppJobsGenerator.class);
	
	private Map<String,Map<String,String>> aiopProperties;
	
    public L0AppJobsGenerator(XmlConverter xmlConverter,
    		MetadataClient metadataClient, ProcessSettings l0ProcessSettings,
            IpfPreparationWorkerSettings taskTablesSettings,
            final OutputProducerFactory outputFactory,
            final AppCatalogJobClient<CatalogEvent> appDataService,
            final AiopProperties aiopProperties, final ProcessConfiguration processConfiguration) {
        super(xmlConverter, metadataClient, l0ProcessSettings,
                taskTablesSettings, outputFactory, appDataService, processConfiguration);
        
        this.aiopProperties = new HashMap<>();
        Map<String, String> stationCodes = aiopProperties.getStationCodes();
        for (String key : stationCodes.keySet()) {
        	Map<String, String> map = new HashMap<>();
        	map.put("PT_Assembly", aiopProperties.getPtAssembly().get(key));
        	map.put("Processing_Mode", aiopProperties.getProcessingMode().get(key));
        	map.put("Reprocessing_Mode", aiopProperties.getReprocessingMode().get(key));
        	map.put("Timeout", aiopProperties.getTimeout().get(key));
        	map.put("Descramble", aiopProperties.getDescramble().get(key));
        	map.put("RSEncode", aiopProperties.getRsEncode().get(key));
        	
        	if (null == map.get("PT_Assembly") || null == map.get("Processing_Mode") ||
        	    null == map.get("Reprocessing_Mode") || null == map.get("Timeout") ||
        	    null == map.get("Descramble") || null == map.get("RSEncode")) {
        	    	throw new RuntimeException(String.format("Invalid AIOP configuration for %s: Station_Code=%s, PT_Assembly=%s, Processing_Mode=%s, Reprocessing_Mode=%s, Timeout=%s, Descramble=%s, RSEncode=%s",
        	    			key, stationCodes.get(key), map.get("PT_Assembly"), map.get("Processing_Mode"), map.get("Reprocessing_Mode"), map.get("Timeout"), map.get("Descramble"), map.get("RSEncode")));
        	    }
        	
        	LOGGER.trace("Initializing AIOP parameter {} for station {} ", map, stationCodes.get(key));
        	this.aiopProperties.put(stationCodes.get(key), map);
        }
    }

    @Override
    protected void preSearch(JobGeneration job)
            throws IpfPrepWorkerInputsMissingException {
        Map<String, String> missingRaws = new HashMap<>();
        if (job.getAppDataJob() != null
                && job.getAppDataJob().getProduct() != null) {
            // Channel 1
            job.getAppDataJob().getProduct().getRaws1().forEach(raw -> {
                try {
                    EdrsSessionMetadata file = this.metadataClient
                            .getEdrsSession("RAW", new File(raw.getFilename()).getName());
                    if (file != null) {
                        raw.setKeyObs(file.getKeyObjectStorage());
                    } else {
                        missingRaws.put(raw.getFilename(), "No raw with name");
                    }
                } catch (MetadataQueryException me) {
                    missingRaws.put(raw.getFilename(), me.getMessage());
                }
            });
            // Channel 2
            job.getAppDataJob().getProduct().getRaws2().forEach(raw -> {
                try {
                    EdrsSessionMetadata file = this.metadataClient
                            .getEdrsSession("RAW", new File(raw.getFilename()).getName());
                    if (file != null) {
                        raw.setKeyObs(file.getKeyObjectStorage());
                    } else {
                        missingRaws.put(raw.getFilename(), "No raw with name");
                    }
                } catch (MetadataQueryException me) {
                    missingRaws.put(raw.getFilename(), me.getMessage());
                }
            });
        }
        if (!missingRaws.isEmpty()) {
            throw new IpfPrepWorkerInputsMissingException(missingRaws);
        }
    }

    @Override
    protected void customJobOrder(JobGeneration job) {
    	AbstractJobOrderConf conf = job.getJobOrder().getConf();
    	AppDataJobProduct product = job.getAppDataJob().getProduct();
    	boolean reprocessing = false; // currently no reprocessing supported
    	LOGGER.info("Configuring AIOP with station parameters for stationCode {} for product {}", product.getStationCode(), product.getProductName());

    	// collect parameters
    	
    	Map<String,String> aiopParams = new HashMap<>();
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
    	
    	for (Entry<String,String> entrySet : aiopProperties.get(stationCode).entrySet()) {
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
    	
    	for (Entry<String, String> newParam : aiopParams.entrySet()) {
    		boolean found = false;
    		if (null != conf.getProcParams()) {
        		for (JobOrderProcParam existingParam : conf.getProcParams()) {
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
    protected void customJobDto(JobGeneration job, IpfExecutionJob dto) {
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
            int nb = Math.max(nb1, nb2);
            for (int i = 0; i < nb; i++) {
                if (i < nb1) {
                    AppDataJobFile raw =
                            job.getAppDataJob().getProduct().getRaws1().get(i);
                    dto.addInput(
                            new LevelJobInputDto(
                                    ProductFamily.EDRS_SESSION.name(),
                                    dto.getWorkDirectory() + "ch01/"
                                            + raw.getFilename(),
                                    raw.getKeyObs()));
                }
                if (i < nb2) {
                    AppDataJobFile raw =
                            job.getAppDataJob().getProduct().getRaws2().get(i);
                    dto.addInput(
                            new LevelJobInputDto(
                                    ProductFamily.EDRS_SESSION.name(),
                                    dto.getWorkDirectory() + "ch02/"
                                            + raw.getFilename(),
                                    raw.getKeyObs()));
                }
            }
        }
    }

}
