package esa.s1pdgs.cpoc.ingestion.product;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.IngestionService;
import esa.s1pdgs.cpoc.ingestion.ProductException;
import esa.s1pdgs.cpoc.ingestion.config.IngestionServiceConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.config.IngestionTypeConfiguration;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting;

@Service
public class ProductService {
	private static final Logger LOG = LogManager.getLogger(IngestionService.class);
		
    private final ObsClient obsClient;
	private final IngestionServiceConfigurationProperties properties;
	
	@Autowired
	public ProductService(ObsClient obsClient, IngestionServiceConfigurationProperties properties) {
		this.obsClient = obsClient;
		this.properties = properties;
	}

	public Product<? extends AbstractDto> ingest(
			final ProductFamily family,
			final IngestionDto ingestion, 
			final Reporting.Factory reportingFactory
	) throws ProductException {		

		
		
		return null;	
	}
	


}
