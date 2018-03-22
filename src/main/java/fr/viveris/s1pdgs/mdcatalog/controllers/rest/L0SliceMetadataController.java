package fr.viveris.s1pdgs.mdcatalog.controllers.rest;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.viveris.s1pdgs.mdcatalog.controllers.rest.dto.L0AcnMetadataDto;
import fr.viveris.s1pdgs.mdcatalog.controllers.rest.dto.L0SliceMetadataDto;
import fr.viveris.s1pdgs.mdcatalog.model.metadata.L0AcnMetadata;
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
			String l0sliceProductType = productType;
			if ("blank".equalsIgnoreCase(productType)) {
				l0sliceProductType = productName.substring(4, 14);
			}
			L0SliceMetadata f = esServices.getL0Slice(l0sliceProductType, productName);

			if (f != null) {
				L0SliceMetadataDto response = new L0SliceMetadataDto(f.getProductName(), f.getProductType(),
						f.getKeyObjectStorage(), f.getValidityStart(), f.getValidityStop());
				response.setNumberSlice(f.getNumberSlice());
				response.setInstrumentConfigurationId(f.getInstrumentConfigurationId());
				response.setDatatakeId(f.getDatatakeId());
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

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{productType}/{productName:.+}/acns")
	public ResponseEntity<List<L0AcnMetadataDto>> getAcns(@PathVariable(name = "productType") String productType,
			@PathVariable(name = "productName") String productName,
			@RequestParam(value = "mode", defaultValue = "ALL") String mode) {
		try {
			// retrieve product type
			String l0sliceProductType = productType;
			if ("blank".equalsIgnoreCase(productType)) {
				l0sliceProductType = productName.substring(4, 14);
			}

			// Build product type for its ACN
			String l0aProductType = l0sliceProductType.replaceAll("0S", "0A");
			String l0cProductType = l0sliceProductType.replaceAll("0S", "0C");
			String l0nProductType = l0sliceProductType.replaceAll("0S", "0N");
			
			LOGGER.info("A {} C {} N {}", l0aProductType, l0cProductType, l0nProductType);

			// Retrieve slice
			L0SliceMetadata f = esServices.getL0Slice(l0sliceProductType, productName);
			if (f == null) {
				LOGGER.error("[productType {}] [productName {}] Not found", l0sliceProductType, productName);
				return new ResponseEntity<List<L0AcnMetadataDto>>(HttpStatus.NOT_FOUND);
			}

			// Retrieve ACN
			List<L0AcnMetadataDto> r = new ArrayList<>();
			
			LOGGER.info("Call getACN for {} {}", l0aProductType, f.getDatatakeId());
			L0AcnMetadata l0a = esServices.getL0Acn(l0aProductType, f.getDatatakeId());
			if (l0a != null) {
				L0AcnMetadataDto l0aDto = new L0AcnMetadataDto(l0a.getProductName(), l0a.getProductType(),
						l0a.getKeyObjectStorage(), l0a.getValidityStart(), l0a.getValidityStop());
				l0aDto.setInstrumentConfigurationId(l0a.getInstrumentConfigurationId());
				l0aDto.setNumberOfSlices(l0a.getNumberOfSlices());
				l0aDto.setDatatakeId(l0a.getDatatakeId());
				r.add(l0aDto);
				if ("ONE".equalsIgnoreCase(mode)) {
					return new ResponseEntity<List<L0AcnMetadataDto>>(r, HttpStatus.OK);
				}
			}
			LOGGER.info("Call getACN for {} {}", l0cProductType, f.getDatatakeId());
			L0AcnMetadata l0c = esServices.getL0Acn(l0cProductType, f.getDatatakeId());
			if (l0c != null) {
				L0AcnMetadataDto l0cDto = new L0AcnMetadataDto(l0c.getProductName(), l0c.getProductType(),
						l0c.getKeyObjectStorage(), l0c.getValidityStart(), l0c.getValidityStop());
				l0cDto.setInstrumentConfigurationId(l0c.getInstrumentConfigurationId());
				l0cDto.setNumberOfSlices(l0c.getNumberOfSlices());
				l0cDto.setDatatakeId(l0c.getDatatakeId());
				r.add(l0cDto);
				if ("ONE".equalsIgnoreCase(mode)) {
					return new ResponseEntity<List<L0AcnMetadataDto>>(r, HttpStatus.OK);
				}
			}
			LOGGER.info("Call getACN for {} {}", l0nProductType, f.getDatatakeId());
			L0AcnMetadata l0n = esServices.getL0Acn(l0nProductType, f.getDatatakeId());
			if (l0n != null) {
				L0AcnMetadataDto l0nDto = new L0AcnMetadataDto(l0n.getProductName(), l0n.getProductType(),
						l0n.getKeyObjectStorage(), l0n.getValidityStart(), l0n.getValidityStop());
				l0nDto.setInstrumentConfigurationId(l0n.getInstrumentConfigurationId());
				l0nDto.setNumberOfSlices(l0n.getNumberOfSlices());
				l0nDto.setDatatakeId(l0n.getDatatakeId());
				r.add(l0nDto);
				if ("ONE".equalsIgnoreCase(mode)) {
					return new ResponseEntity<List<L0AcnMetadataDto>>(r, HttpStatus.OK);
				}
			}
			
			return new ResponseEntity<List<L0AcnMetadataDto>>(r, HttpStatus.OK);

		} catch (Exception e) {
			LOGGER.error("[productType {}] [productName {}] Exception occured: {}", productType, productName,
					e.getMessage());
			return new ResponseEntity<List<L0AcnMetadataDto>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
