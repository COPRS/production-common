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
            return new ResponseEntity<F>(HttpStatus.NO_CONTENT);            
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
