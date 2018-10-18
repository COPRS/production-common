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
import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.mdcatalog.es.model.LevelSegmentMetadata;
import esa.s1pdgs.cpoc.mdcatalog.rest.dto.LevelSegmentMetadataDto;

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
    public ResponseEntity<LevelSegmentMetadataDto> get(
            @PathVariable(name = "family") ProductFamily family,
            @PathVariable(name = "productName") String productName) {
        try {
            LevelSegmentMetadata f =
                    esServices.getLevelSegment(family, productName);

            LevelSegmentMetadataDto response =
                    new LevelSegmentMetadataDto(f.getProductName(),
                            f.getProductType(), f.getKeyObjectStorage(),
                            f.getValidityStart(), f.getValidityStop());
            response.setDatatakeId(f.getDatatakeId());
            response.setConsolidation(f.getConsolidation());
            response.setPolarisation(f.getPolarisation());
            return new ResponseEntity<LevelSegmentMetadataDto>(response,
                    HttpStatus.OK);

        } catch (MetadataNotPresentException em) {
            LOGGER.warn("[{}] [productName {}] [code {}] {}", family,
                    productName, em.getCode().getCode(), em.getLogMessage());
            return new ResponseEntity<LevelSegmentMetadataDto>(
                    HttpStatus.NOT_FOUND);
        } catch (AbstractCodedException ace) {
            LOGGER.error("[{}] [productName {}] [code {}] {}", family,
                    productName, ace.getCode().getCode(), ace.getLogMessage());
            return new ResponseEntity<LevelSegmentMetadataDto>(
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception exc) {
            LOGGER.error("[{}] [productName {}] [code {}] [msg {}]", family,
                    productName, ErrorCode.INTERNAL_ERROR.getCode(),
                    exc.getMessage());
            return new ResponseEntity<LevelSegmentMetadataDto>(
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
