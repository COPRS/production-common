package esa.s1pdgs.cpoc.mdcatalog.rest;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataNotPresentException;
import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;
import esa.s1pdgs.cpoc.mdcatalog.rest.dto.L0AcnMetadataDto;
import esa.s1pdgs.cpoc.mdcatalog.rest.dto.L0SliceMetadataDto;

@RestController
@RequestMapping(path = "/l0Slice")
public class L0SliceMetadataController {

	private static final Logger LOGGER = LogManager.getLogger(L0SliceMetadataController.class);

	private final EsServices esServices;

	@Autowired
	public L0SliceMetadataController(final EsServices esServices) {
		this.esServices = esServices;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{productName:.+}")
	public ResponseEntity<L0SliceMetadataDto> get(@PathVariable(name = "productName") String productName) {
		try {
			L0SliceMetadata f = esServices.getL0Slice(productName);

			L0SliceMetadataDto response = new L0SliceMetadataDto(f.getProductName(), f.getProductType(),
					f.getKeyObjectStorage(), f.getValidityStart(), f.getValidityStop());
			response.setNumberSlice(f.getNumberSlice());
			response.setInstrumentConfigurationId(f.getInstrumentConfigurationId());
			response.setDatatakeId(f.getDatatakeId());
			return new ResponseEntity<L0SliceMetadataDto>(response, HttpStatus.OK);
			
		} catch (MetadataNotPresentException em) {
			LOGGER.warn("[L0_SLICE] [productName {}] [code {}] {}", productName,
					em.getCode().getCode(), em.getLogMessage());
			return new ResponseEntity<L0SliceMetadataDto>(HttpStatus.NOT_FOUND);
		} catch (AbstractCodedException ace) {
			LOGGER.error("[L0_SLICE] [productName {}] [code {}] {}", productName,
					ace.getCode().getCode(), ace.getLogMessage());
			return new ResponseEntity<L0SliceMetadataDto>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception exc) {
			LOGGER.error("[L0_SLICE] [productName {}] [code {}] [msg {}]", productName,
					ErrorCode.INTERNAL_ERROR.getCode(), exc.getMessage());
			return new ResponseEntity<L0SliceMetadataDto>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{productName:.+}/acns")
	public ResponseEntity<List<L0AcnMetadataDto>> getAcns(
			@PathVariable(name = "productName") String productName,
			@RequestParam(name = "processMode", defaultValue = "NONE") String processMode,
			@RequestParam(value = "mode", defaultValue = "ALL") String mode) {
		try {
			// Retrieve slice
			L0SliceMetadata f = esServices.getL0Slice(productName);
			if (f == null) {
				LOGGER.warn("[L0_SLICE] [productName {}] Not found", productName);
				return new ResponseEntity<List<L0AcnMetadataDto>>(HttpStatus.NOT_FOUND);
			}
			String productType = f.getProductType();
			String productTypeWithoutLastChar = productType.substring(0, productType.length() - 1);

			// Retrieve ACN
			List<L0AcnMetadataDto> r = new ArrayList<>();

			LOGGER.info("Call getACN for {}A {}", productTypeWithoutLastChar, f.getDatatakeId());
			L0AcnMetadata l0a = esServices.getL0Acn(productTypeWithoutLastChar + "A", f.getDatatakeId(), processMode);
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
            LOGGER.info("Call getACN for {}C {}", productTypeWithoutLastChar, f.getDatatakeId());
			L0AcnMetadata l0c = esServices.getL0Acn(productTypeWithoutLastChar + "C", f.getDatatakeId(), processMode);
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
            LOGGER.info("Call getACN for {}N {}", productTypeWithoutLastChar, f.getDatatakeId());
			L0AcnMetadata l0n = esServices.getL0Acn(productTypeWithoutLastChar + "N", f.getDatatakeId(), processMode);
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

			if(l0a == null && l0c == null && l0n == null) {
				return new ResponseEntity<List<L0AcnMetadataDto>>(HttpStatus.NOT_FOUND);
			}
			
			return new ResponseEntity<List<L0AcnMetadataDto>>(r, HttpStatus.OK);

		} catch (MetadataNotPresentException em) {
			LOGGER.warn("[L0_SLICE] [productName {}] [code {}] {}", productName,
					em.getCode().getCode(), em.getLogMessage());
			return new ResponseEntity<List<L0AcnMetadataDto>>(HttpStatus.NOT_FOUND);
		} catch (AbstractCodedException ace) {
			LOGGER.error("[L0_SLICE] [productName {}] [code {}] {}", productName,
					ace.getCode().getCode(), ace.getLogMessage());
			return new ResponseEntity<List<L0AcnMetadataDto>>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			LOGGER.error("[L0_SLICE] [productName {}] [code {}] [msg {}]", productName,
					ErrorCode.INTERNAL_ERROR.getCode(), e.getMessage());
			return new ResponseEntity<List<L0AcnMetadataDto>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
