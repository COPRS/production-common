package fr.viveris.s1pdgs.mdcatalog.controllers.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.viveris.s1pdgs.mdcatalog.controllers.rest.dto.L0SliceMetadataDto;
import fr.viveris.s1pdgs.mdcatalog.model.metadata.L0SliceMetadata;
import fr.viveris.s1pdgs.mdcatalog.services.es.EsServices;

@RestController
@RequestMapping(path = "/l0Slice")
public class L0SliceMetadataController {

	private static final Logger LOGGER = LoggerFactory.getLogger(L0SliceMetadataController.class);

	private final EsServices esServices;

	@Autowired
	public L0SliceMetadataController(final EsServices esServices) {
		this.esServices = esServices;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{productType}/{productName:.+}")
	public ResponseEntity<L0SliceMetadataDto> get(@PathVariable(name = "productType") String productType,
			@PathVariable(name = "productName") String productName) {
		try {
			// TODO, if productType = blank, extract it from the productName
			L0SliceMetadata f = esServices.getL0Slice(productType, productName);

			if (f != null) {
				L0SliceMetadataDto response = new L0SliceMetadataDto(f.getProductName(), f.getProductType(),
						f.getKeyObjectStorage(), f.getValidityStart(), f.getValidityStop());
				response.setNumberSlice(f.getNumberSlice());
				response.setInstrumentConfigurationId(f.getInstrumentConfigurationId());
				return new ResponseEntity<L0SliceMetadataDto>(response, HttpStatus.OK);
			} else {
				LOGGER.error("[productType {}] [productName {}] Not found", productType, productName);
				return new ResponseEntity<L0SliceMetadataDto>(HttpStatus.NOT_FOUND);
			}

		} catch (Exception e) {
			LOGGER.error("[productType {}] [productName {}] Exception occured: {}", productType, productName,
					e.getMessage());
			return new ResponseEntity<L0SliceMetadataDto>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
