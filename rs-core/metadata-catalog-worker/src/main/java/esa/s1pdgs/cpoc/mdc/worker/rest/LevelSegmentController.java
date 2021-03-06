package esa.s1pdgs.cpoc.mdc.worker.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;

@RestController
@RequestMapping(path = "/level_segment")
public class LevelSegmentController extends AbstractMetadataController<LevelSegmentMetadata> {

    @Autowired
    public LevelSegmentController(final EsServices esServices) {
    	super(LevelSegmentMetadata.class, esServices);
    }
 
//    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{family}/{productName:.+}")
//    public ResponseEntity<LevelSegmentMetadata> get(
//            @PathVariable(name = "family") final ProductFamily family,
//            @PathVariable(name = "productName") final String productName
//    ) {
//    	return getResponse(productName, family, () -> esServices.getLevelSegment(family, productName));
//    }
    
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{dataTakeId}")
    public ResponseEntity<List<LevelSegmentMetadata>> get(
            @PathVariable(name = "dataTakeId") final String dataTakeId
    ) {
    	return getResponse(
    			"Products for dataTakeId=" + dataTakeId, 
    			ProductFamily.L0_SEGMENT, 
    			() -> esServices.getLevelSegmentMetadataFor(dataTakeId)
    	);
    }
    
    
}
