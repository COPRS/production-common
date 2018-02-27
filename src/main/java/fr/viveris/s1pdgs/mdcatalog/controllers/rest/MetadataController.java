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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.viveris.s1pdgs.mdcatalog.controllers.rest.dto.MetadataFileDto;
import fr.viveris.s1pdgs.mdcatalog.model.MetadataFile;
import fr.viveris.s1pdgs.mdcatalog.services.es.EsServices;

@RestController
public class MetadataController {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetadataController.class);

	private final EsServices esServices;

	@Autowired
	public MetadataController(final EsServices esServices) {
		this.esServices = esServices;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{productType}/search")
	public ResponseEntity<MetadataFileDto> search(@PathVariable(name = "productType") String productType,
			@RequestParam(name = "mode") String mode, @RequestParam(name = "satellite") String satellite,
			@RequestParam(name = "t0") String startDate, @RequestParam(name = "t1") String stopDate,
			@RequestParam(value = "dt0", defaultValue = "0") int dt0,
			@RequestParam(value = "dt1", defaultValue = "0") int dt1) {
		try {
			if (mode.equals("LatestValCover")) {
				MetadataFile f = esServices.lastValCover(productType, convertDateForSearch(startDate, -dt0),
						convertDateForSearch(stopDate, dt1), satellite);

				MetadataFileDto response = new MetadataFileDto(f.getProductName(), f.getProductType(),
						f.getKeyObjectStorage(), f.getValidityStart(), f.getValidityStop());

				return new ResponseEntity<MetadataFileDto>(response, HttpStatus.OK);
			} else {
				return new ResponseEntity<MetadataFileDto>(HttpStatus.BAD_REQUEST);
			}
		} catch (ParseException e) {
			LOGGER.error("Exception occured", e);
			return new ResponseEntity<MetadataFileDto>(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			LOGGER.error("Exception occured", e);
			return new ResponseEntity<MetadataFileDto>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{productType}/{productName:.+}")
	public ResponseEntity<MetadataFileDto> get(@PathVariable(name = "productType") String productType,
			@PathVariable(name = "productName") String productName) {
		try {
			MetadataFile f = esServices.get(productType, productName);

			MetadataFileDto response = new MetadataFileDto(f.getProductName(), f.getProductType(),
					f.getKeyObjectStorage(), f.getValidityStart(), f.getValidityStop());

			return new ResponseEntity<MetadataFileDto>(response, HttpStatus.OK);

		} catch (Exception e) {
			LOGGER.error("Exception occured", e);
			return new ResponseEntity<MetadataFileDto>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private String convertDateForSearch(String dateStr, int delta) throws ParseException {
		LocalDateTime time = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		LocalDateTime timePlus = time.plusSeconds(delta);
		return timePlus.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}
}
