package esa.s1pdgs.cpoc.mdc.worker.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataNotPresentException;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;

@RestController
@RequestMapping(path = "/l0Slice")
public class L0SliceMetadataController extends AbstractMetadataController<L0SliceMetadata> {
	
	@Autowired
    public L0SliceMetadataController(final EsServices esServices) {
		super(L0SliceMetadata.class, esServices);
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{family}/{productName:.+}/seaCoverage")
    public ResponseEntity<Integer> getSeaCoverage(
            @PathVariable(name = "family") ProductFamily family,
            @PathVariable(name = "productName") String productName) {		
		return getResponse(productName, family, () -> esServices.getSeaCoverage(family, productName));
    }

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{productName:.+}")
	public ResponseEntity<L0SliceMetadata> get(@PathVariable(name = "productName") String productName) {
		return getResponse(productName, ProductFamily.L0_SLICE, () -> esServices.getL0Slice(productName));	
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{productName:.+}/acns")
	public ResponseEntity<List<L0AcnMetadata>> getAcns(
			@PathVariable(name = "productName") String productName,
			@RequestParam(name = "processMode", defaultValue = "NOMINAL") String processMode,
			@RequestParam(value = "mode", defaultValue = "ALL") String mode) {
		
		final Callable<List<L0AcnMetadata>> acnsSupplier = () -> {
			final L0SliceMetadata slice = esServices.getL0Slice(productName);
			if (slice == null) {
				throw new MetadataNotPresentException(productName);
			}
			return getL0AcnFor(slice, processMode, mode);
		};
		return getResponse(productName, ProductFamily.L0_ACN, acnsSupplier);
	}
		
	private List<L0AcnMetadata> getL0AcnFor(final L0SliceMetadata slice, final String processingMode, final String mode) throws MetadataNotPresentException {
		final List<L0AcnMetadata> result = new ArrayList<>();
			
		final String productType = slice.getProductType();
		final String productTypeWithoutLastChar = productType.substring(0, productType.length() - 1);		
		final String datatakeId = slice.getDatatakeId();
		
		// Retrieve ACN		
		for (final String acnType : Arrays.asList("A", "C", "N")) {
			logger.info("Call getACN for {}{} {} (processMode: {})", productTypeWithoutLastChar, acnType, datatakeId, processingMode);
			
			final L0AcnMetadata l0acn = esServices.getL0Acn(productTypeWithoutLastChar + acnType, datatakeId, processingMode);
			
			if (l0acn != null) {
				logger.debug("Got ACN for {}{} {} '{}' (processMode: {})", productTypeWithoutLastChar, acnType, datatakeId, 
						l0acn.getProductName(), processingMode);
				result.add(l0acn);
				
				if ("ONE".equalsIgnoreCase(mode)) {
					logger.debug("Got ACN for '{}' (processMode: {}): {}", datatakeId, processingMode, result);
					return result;
				}
			}
		}		
		if (result.isEmpty()) {
			throw new MetadataNotPresentException("ACN for " + slice.getProductName());
		}		
		logger.debug("Got ACNs for '{}' (mode: {}): {}", datatakeId, processingMode, result);
		return result;		
	}
}
