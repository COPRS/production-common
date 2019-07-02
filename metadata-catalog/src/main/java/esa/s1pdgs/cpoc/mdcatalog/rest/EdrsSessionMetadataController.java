package esa.s1pdgs.cpoc.mdcatalog.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataNotPresentException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;

@RestController
@RequestMapping(path = "/edrsSession")
public class EdrsSessionMetadataController {

	private static final Logger LOGGER = LogManager.getLogger(EdrsSessionMetadataController.class);

	private final EsServices esServices;

	@Autowired
	public EdrsSessionMetadataController(final EsServices esServices) {
		this.esServices = esServices;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{productType}/{productName:.+}")
	public ResponseEntity<EdrsSessionMetadata> get(@PathVariable(name = "productType") String productType,
			@PathVariable(name = "productName") String productName) {
		try {
			EdrsSessionMetadata response = esServices.getEdrsSession(productType, productName);

			if (response != null) {
				return new ResponseEntity<EdrsSessionMetadata>(response, HttpStatus.OK);
			} else {
				LOGGER.warn("[productType {}] [productName {}] Not found", productType, productName);
				return new ResponseEntity<EdrsSessionMetadata>(HttpStatus.NOT_FOUND);
			}

		} catch (MetadataNotPresentException em) {
			LOGGER.warn("[productType {}] [productName {}] [code {}] {}", productType, productName, em.getCode().getCode(),
					em.getLogMessage());
			return new ResponseEntity<EdrsSessionMetadata>(HttpStatus.NOT_FOUND);
		} catch (AbstractCodedException ace) {
			LOGGER.error("[productType {}] [productName {}] [code {}] {}", productType, productName, ace.getCode().getCode(),
					ace.getLogMessage());
			return new ResponseEntity<EdrsSessionMetadata>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception exc) {
			LOGGER.error("[productType {}] [productName {}] [code {}] [msg {}]", productType, productName,
					ErrorCode.INTERNAL_ERROR.getCode(), exc.getMessage());
			return new ResponseEntity<EdrsSessionMetadata>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
