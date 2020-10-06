package esa.s1pdgs.cpoc.mdc.worker.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
import esa.s1pdgs.cpoc.metadata.model.L1SliceMetadata;

@RestController
@RequestMapping(path = "/l1Slice")
public class L1SliceMetadataController extends AbstractMetadataController<L1SliceMetadata> {
	
	@Autowired
    public L1SliceMetadataController(final EsServices esServices) {
		super(L1SliceMetadata.class, esServices);
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{family}/{productName:.+}/overpassCoverage")
    public ResponseEntity<Integer> getOverpassCoverage(
            @PathVariable(name = "family") ProductFamily family,
            @PathVariable(name = "productName") String productName) {		
		return getResponse(productName, family, () -> esServices.getOverpassCoverage(family, productName));
    }
}
