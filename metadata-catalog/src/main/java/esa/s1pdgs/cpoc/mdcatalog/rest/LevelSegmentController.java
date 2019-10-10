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

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataNotPresentException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;

@RestController
@RequestMapping(path = "/level_segment")
public class LevelSegmentController {

    private static final Logger LOGGER =
            LogManager.getLogger(LevelSegmentController.class);

    private final EsServices esServices;

    @Autowired
    public LevelSegmentController(final EsServices esServices) {
        this.esServices = esServices;
    }
 
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{family}/{productName:.+}")
    public ResponseEntity<LevelSegmentMetadata> get(
            @PathVariable(name = "family") ProductFamily family,
            @PathVariable(name = "productName") String productName) {
        try {
			LevelSegmentMetadata response = esServices.getLevelSegment(family, productName);
			
			if (response == null) {
				throw new MetadataNotPresentException(productName);
			}
			return new ResponseEntity<LevelSegmentMetadata>(response, HttpStatus.OK);

        } catch (MetadataNotPresentException em) {
            LOGGER.warn("[{}] [productName {}] [code {}] {}", family,
                    productName, em.getCode().getCode(), em.getLogMessage());
            return new ResponseEntity<LevelSegmentMetadata>(
                    HttpStatus.NOT_FOUND);
        } catch (AbstractCodedException ace) {
            LOGGER.error("[{}] [productName {}] [code {}] {}", family,
                    productName, ace.getCode().getCode(), ace.getLogMessage());
            return new ResponseEntity<LevelSegmentMetadata>(
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            LOGGER.error("[{}] [productName {}] [code {}] [msg {}]", family,
                    productName, ErrorCode.INTERNAL_ERROR.getCode(),
                    LogUtils.toString(e));
            return new ResponseEntity<LevelSegmentMetadata>(
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
