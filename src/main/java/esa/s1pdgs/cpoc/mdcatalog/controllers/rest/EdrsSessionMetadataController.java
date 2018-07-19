package esa.s1pdgs.cpoc.mdcatalog.controllers.rest;

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

import esa.s1pdgs.cpoc.mdcatalog.controllers.rest.dto.EdrsSessionMetadataDto;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataNotPresentException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.mdcatalog.model.metadata.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.mdcatalog.services.es.EsServices;

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
	public ResponseEntity<EdrsSessionMetadataDto> get(@PathVariable(name = "productType") String productType,
			@PathVariable(name = "productName") String productName) {
		try {
			EdrsSessionMetadata f = esServices.getEdrsSession(productType, productName);

			if (f != null) {
				EdrsSessionMetadataDto response = new EdrsSessionMetadataDto(f.getProductName(), f.getProductType(),
						f.getKeyObjectStorage(), f.getValidityStart(), f.getValidityStop());
				return new ResponseEntity<EdrsSessionMetadataDto>(response, HttpStatus.OK);
			} else {
				LOGGER.warn("[productType {}] [productName {}] Not found", productType, productName);
				return new ResponseEntity<EdrsSessionMetadataDto>(HttpStatus.NOT_FOUND);
			}

		} catch (MetadataNotPresentException em) {
			LOGGER.warn("[productType {}] [productName {}] [code {}] {}", productType, productName, em.getCode().getCode(),
					em.getLogMessage());
			return new ResponseEntity<EdrsSessionMetadataDto>(HttpStatus.NOT_FOUND);
		} catch (AbstractCodedException ace) {
			LOGGER.error("[productType {}] [productName {}] [code {}] {}", productType, productName, ace.getCode().getCode(),
					ace.getLogMessage());
			return new ResponseEntity<EdrsSessionMetadataDto>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception exc) {
			LOGGER.error("[productType {}] [productName {}] [code {}] [msg {}]", productType, productName,
					ErrorCode.INTERNAL_ERROR.getCode(), exc.getMessage());
			return new ResponseEntity<EdrsSessionMetadataDto>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
