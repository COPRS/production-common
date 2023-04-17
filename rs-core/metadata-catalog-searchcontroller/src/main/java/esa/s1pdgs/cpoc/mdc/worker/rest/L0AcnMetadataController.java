package esa.s1pdgs.cpoc.mdc.worker.rest;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;

@RestController
@RequestMapping(path = "/l0Acn")
public class L0AcnMetadataController extends AbstractMetadataController<L0AcnMetadata> {

	private static final Logger LOGGER = LogManager.getLogger(L0AcnMetadataController.class);

	@Autowired
	public L0AcnMetadataController(final EsServices esServices) {
		super(L0AcnMetadata.class, esServices);
	}

	/**
	 * Queries the elastic search for products of a given start or stop orbit.
	 */
	@RequestMapping(path = "/{productType}/startOrStopOrbit", method = RequestMethod.GET)
	public ResponseEntity<List<L0AcnMetadata>> getProductsForStartOrStopOrbit(@PathVariable(name = "productType") final String productType,
			@RequestParam(name = "productFamily") final String productFamily,
			@RequestParam(name = "satellite") final String satellite,
			@RequestParam(name = "orbitNumber") final long orbitNumber) {

		try {
			LOGGER.info("Received Start/Stop Orbit search query for family '{}', product type '{}', orbitNumber '{}'",
					productFamily.toString(), productType, orbitNumber);

			final List<L0AcnMetadata> result = esServices.getL0AcnForStartOrStopOrbit(
					ProductFamily.fromValue(productFamily), 
					productType,
					satellite, 
					orbitNumber
			);
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (final AbstractCodedException e) {
			LOGGER.error("Error on performing Start/Stop Orbit search for product type {} and orbit {}: [code {}] {}", productType,
					orbitNumber, e.getCode().getCode(), e.getLogMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			LOGGER.error("Error on performing Start/Stop Orbit search for product type {} and orbit {}: {}", productType, orbitNumber,
					LogUtils.toString(e));
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
