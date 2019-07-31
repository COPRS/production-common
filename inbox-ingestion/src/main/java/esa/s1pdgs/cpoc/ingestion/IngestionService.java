package esa.s1pdgs.cpoc.ingestion;

import java.io.File;
import java.util.Arrays;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.ingestion.config.IngestionServiceConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.config.IngestionTypeConfiguration;
import esa.s1pdgs.cpoc.ingestion.product.Product;
import esa.s1pdgs.cpoc.ingestion.product.ProductService;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;

@Service
public class IngestionService {
	static final Logger LOG = LogManager.getLogger(IngestionService.class);

	private final GenericMqiClient client;
	private final ErrorRepoAppender errorRepoAppender;
	private final IngestionServiceConfigurationProperties properties;
    private final ProductService productService;

	@Autowired
	public IngestionService(
			final GenericMqiClient client,
			final ErrorRepoAppender errorRepoAppender,
			final IngestionServiceConfigurationProperties properties,
			final ProductService productService
	) {
		this.client = client;
		this.errorRepoAppender = errorRepoAppender;
		this.properties = properties;
		this.productService = productService;
	}
	
	public void poll() {		
		final Reporting.Factory reportingFactory = new LoggerReporting.Factory(LOG, "Ingestion");	
		
		try {
			final GenericMessageDto<IngestionDto> message = client.next(ProductCategory.INGESTION);			
			if (message == null || message.getBody() == null) {
				LOG.trace(" No message received: continue");
				return;
			}
			final IngestionDto ingestion = message.getBody();
			LOG.debug("received Ingestion: {}", ingestion.getProductName());
			
			final Reporting reporting = reportingFactory
					.product(ingestion.getFamily().toString(), ingestion.getProductName())
					.newReporting(0);	
			
			reporting.reportStart("Start processing of " + ingestion.getProductName());	
			
			Product<? extends AbstractDto> result = null;
			try {
				try {	
					final ProductFamily family = getFamilyFor(ingestion);
					// update family information
					reportingFactory.product(family.toString(), ingestion.getProductName());											
					result = productService.ingest(family, ingestion, reportingFactory);
					reporting.reportStop("End processing of " + ingestion.getProductName());
					
				// is thrown if product shall be marked as invalid	
				} catch (ProductException e) {		
					message.getBody().setFamily(ProductFamily.INVALID);
					
					// TODO move to invalid bucket
					
					final FailedProcessingDto failed = new FailedProcessingDto(
							properties.getHostname(), 
							new Date(), 
							e.getMessage(), 
							message
					);
					errorRepoAppender.send(failed);	
				}
				publish(result, message, reportingFactory);				
				delete(ingestion, reportingFactory);
				reporting.reportStop("End processing of " + ingestion.getProductName());
			} catch (Exception e) {
				reporting.reportError("{}", LogUtils.toString(e));
			}			
			client.ack(new AckMessageDto(message.getIdentifier(), Ack.OK, null, false), ProductCategory.INGESTION);
		// on communication errors with Mqi --> just dump warning and retry on next polling attempt
		} catch (AbstractCodedException ace) {
			LOG.warn("Error Code: {}, Message: {}", ace.getCode().getCode(), ace.getLogMessage());			
		// any other error --> dump prominently into log file but continue	
		} catch (Exception e) {
			LOG.error("Unexpected Error on Ingestion", e);
		}	
	}

	void publish(
			final Product<? extends AbstractDto> product, 
			final GenericMessageDto<IngestionDto> message,
			final Reporting.Factory reportingFactory
	) throws InternalErrorException {		
		if (product != null) {				
			final GenericPublicationMessageDto<? extends AbstractDto> result = new GenericPublicationMessageDto<>(
					message.getIdentifier(), 
					product.getFamily(),
					product.getDto()
			);			
			result.setInputKey(message.getInputKey());
			result.setOutputKey(product.getFamily().toString());
			
			LOG.info("publishing : {}", result);

			final Reporting reporting = reportingFactory
				.product(product.getFamily().toString(), message.getBody().getProductName())
				.newReporting(3);
			
			final ProductCategory category = ProductCategory.fromProductFamily(product.getFamily());
			
			reporting.reportStart("Start publishing file in topic");
			try {
				client.publish(result, category);
				reporting.reportStop("End publishing file in topic");				
			} catch (AbstractCodedException e) {
				reporting.reportError("[code {}] {}", e.getCode().getCode(), e.getLogMessage());
			}
		}
	}
	
	void delete(final IngestionDto ingestion, final Reporting.Factory reportingFactory) 
			throws InternalErrorException, InterruptedException {
		final File file = new File(ingestion.getProductUrl().replace("file://", ""));
		if (file.exists()) {			
			final Reporting reporting = reportingFactory.newReporting(2);		
			reporting.reportStart("Start removing file " + file.getPath());

			FileUtils.deleteWithRetries(file, properties.getMaxRetries(), properties.getTempoRetryMs());
		}
	}

//	public Product<? extends AbstractDto> handle(
//			final GenericMessageDto<IngestionDto> ingestionMessage,
//			final Reporting.Factory reportingFactory
//	) {		
//		final IngestionDto ingestion = ingestionMessage.getBody();		
//		
//		Reporting reporting = reportingFactory
//				.product(ingestion.getFamily().toString(), ingestion.getProductName())
//				.newReporting(0);	
//				
//		Product<? extends AbstractDto> result = null;		
//		try {	
//			final ProductFamily family = getFamilyFor(ingestion);
//			
//			reporting = reportingFactory
//					.product(family.toString(), ingestion.getProductName())
//					.newReporting(0);
//			reporting.reportStart("Start processing of " + ingestion.getProductName());				
//			result = productService.ingest(family, ingestion, reportingFactory);
//			reporting.reportStop("End processing of " + ingestion.getProductName());
//			
//		// is thrown if product shall be marked as invalid	
//		} catch (ProductException e) {		
//			ingestionMessage.getBody().setFamily(ProductFamily.INVALID);
//			
//			final FailedProcessingDto failed = new FailedProcessingDto(
//					properties.getHostname(), 
//					new Date(), 
//					e.getMessage(), 
//					ingestionMessage
//			);
//			errorRepoAppender.send(failed);	
//		}
//		return result;
//	}

	ProductFamily getFamilyFor(final IngestionDto dto) throws ProductException {
		for (final IngestionTypeConfiguration config : properties.getTypes()) {			
			if (dto.getProductName().matches(config.getRegex())) {
				LOG.debug("Found {} for {}", config, dto);
				try {
					return ProductFamily.valueOf(config.getFamily());
				} catch (Exception e) {
					throw new ProductException(
							String.format(
									"Invalid %s for %s (allowed: %s)", 
									config,
									dto,
									Arrays.toString(ProductFamily.values())
							)
					);
				}
			}
		}
		throw new ProductException(
				String.format(
						"No matching config found for %s in: %s", 
						dto,
						properties.getTypes()
				)
		);
	}

}
