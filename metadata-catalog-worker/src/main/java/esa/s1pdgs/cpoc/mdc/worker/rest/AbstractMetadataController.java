package esa.s1pdgs.cpoc.mdc.worker.rest;

import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataNotPresentException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
import esa.s1pdgs.cpoc.metadata.model.AbstractMetadata;

public class AbstractMetadataController<E extends AbstractMetadata> {	
	protected final Logger logger = LogManager.getLogger(getClass());
	
	private final Class<E> className;
	protected final EsServices esServices;

	protected AbstractMetadataController(Class<E> className, EsServices esServices) {
		this.className = className;
		this.esServices = esServices;
	}
	
	protected <F> ResponseEntity<F> getResponse(
			final String productName,
			final ProductFamily family,
			final Callable<F> responseSupplier
	) {
        try {
			final F response = responseSupplier.call();			
			if (response == null) {
				throw new MetadataNotPresentException(productName);
			}
			return new ResponseEntity<F>(response, HttpStatus.OK);
        } catch (MetadataNotPresentException e) {
        	logger.warn("{} '{}' of family {} not available [code {}] {}",  
        			className.getSimpleName(), productName, family, e.getCode().getCode(), e.getLogMessage());            
            return new ResponseEntity<F>(HttpStatus.NOT_FOUND);            
        } catch (AbstractCodedException e) {
        	logger.error("Error on getting {} '{}' of family {} [code {}] {}", 
        			className.getSimpleName(), productName, family, e.getCode().getCode(), e.getLogMessage());
            return new ResponseEntity<F>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
        	logger.error("Error on getting {} '{}' of family {}: {}", 
        			className.getSimpleName(), productName, family, LogUtils.toString(e));
            return new ResponseEntity<F>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}
}
