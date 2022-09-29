package esa.s1pdgs.cpoc.mdc.worker.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;

@RestController
@RequestMapping(path = "/metadata")
public class DeleteMetadataController extends AbstractMetadataController<SearchMetadata> {

	@Autowired
	public DeleteMetadataController(final EsServices esServices) {
		super(SearchMetadata.class, esServices);
	}
	
	/**
	 * Deletes the product with given productName and in the index =
	 * productFamily. Returns true if succeeded. 
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{productFamily}/deleteProduct")
	public ResponseEntity<Boolean> deleteProduct(
			@PathVariable(name = "productFamily") final ProductFamily productFamily,
			@RequestParam(name = "productName") final String productName) {
		
		return getResponse(productName, productFamily, () -> esServices.deleteProduct(productFamily, productName));

	}
}
