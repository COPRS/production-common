package fr.viveris.s1pdgs.mdcatalog.controllers.rest;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.viveris.s1pdgs.mdcatalog.controllers.rest.dto.SearchMetadataDto;
import fr.viveris.s1pdgs.mdcatalog.model.metadata.SearchMetadata;
import fr.viveris.s1pdgs.mdcatalog.services.es.EsServices;

@RestController
@RequestMapping(path = "/metadata")
public class SearchMetadataController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchMetadataController.class);

	private final EsServices esServices;

	@Autowired
	public SearchMetadataController(final EsServices esServices) {
		this.esServices = esServices;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/search")
	public ResponseEntity<SearchMetadataDto> search(@RequestParam(name = "productType") String productType,
			@RequestParam(name = "mode") String mode, @RequestParam(name = "satellite") String satellite,
			@RequestParam(name = "t0") String startDate, @RequestParam(name = "t1") String stopDate,
			@RequestParam(name = "insConfId", defaultValue = "-1") int insConfId,
			@RequestParam(value = "dt0", defaultValue = "0.0") double dt0,
			@RequestParam(value = "dt1", defaultValue = "0.0") double dt1) {
		try {
			if (mode.equals("LatestValCover")) {
				SearchMetadata f = esServices.lastValCover(productType, convertDateForSearch(startDate, -dt0),
						convertDateForSearch(stopDate, dt1), satellite, insConfId);
				SearchMetadataDto response = null;
				if (f != null) {
					response = new SearchMetadataDto(f.getProductName(), f.getProductType(), f.getKeyObjectStorage(),
							f.getValidityStart(), f.getValidityStop());
				}
				return new ResponseEntity<SearchMetadataDto>(response, HttpStatus.OK);
			} else {
				LOGGER.error("[productType {}] [mode {}] Unknown mode", productType, mode);
				return new ResponseEntity<SearchMetadataDto>(HttpStatus.BAD_REQUEST);
			}
		} catch (ParseException e) {
			LOGGER.error("[productType {}] [mode {}] Exception occured: {}", productType, mode, e.getMessage());
			return new ResponseEntity<SearchMetadataDto>(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			LOGGER.error("[productType {}] [mode {}] Exception occured: {}", productType, mode, e.getMessage());
			return new ResponseEntity<SearchMetadataDto>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	private String convertDateForSearch(String dateStr, double delta) throws ParseException {
		LocalDateTime time = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		LocalDateTime timePlus = time.plusSeconds(Math.round(delta));
		return timePlus.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}
}
