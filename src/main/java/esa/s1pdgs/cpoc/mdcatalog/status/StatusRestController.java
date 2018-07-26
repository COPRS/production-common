package esa.s1pdgs.cpoc.mdcatalog.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mdcatalog.status.dto.GlobalStatusDto;
import esa.s1pdgs.cpoc.mdcatalog.status.dto.StatusPerCategoryDto;

/**
 * @author Viveris Technologies
 */
@RestController
@RequestMapping(path = "/wrapper")
public class StatusRestController {

	/**
	 * Logger
	 */
	protected static final Logger LOGGER = LogManager.getLogger(StatusRestController.class);

	/**
	 * Application status
	 */
	private final AppStatus appStatus;

	/**
	 * Constructor
	 * 
	 * @param appStatus
	 */
	@Autowired
	public StatusRestController(final AppStatus appStatus) {
		this.appStatus = appStatus;
	}

	/**
	 * Get application status
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/status")
	public ResponseEntity<GlobalStatusDto> getStatusRest() {
		GlobalStatusDto globalStatus = new GlobalStatusDto(appStatus.getGlobalAppState());
		for (StatusPerCategory catStatus : appStatus.getStatus().values()) {
			long currentTimestamp = System.currentTimeMillis();
			long timeSinceLastChange = currentTimestamp - catStatus.getDateLastChangeMs();
			globalStatus.addStatusPerCategory(new StatusPerCategoryDto(catStatus.getState(), timeSinceLastChange,
					catStatus.getErrorCounter(), catStatus.getCategory()));
		}
		if (appStatus.isFatalError()) {
			return new ResponseEntity<GlobalStatusDto>(globalStatus, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<GlobalStatusDto>(globalStatus, HttpStatus.OK);
	}

	/**
	 * True if the application is processing the message for the given category,
	 * false else
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/{category}/process/{messageId}")
	public ResponseEntity<Boolean> isProcessing(@PathVariable(name = "category") final String category,
			@PathVariable(name = "messageId") final long messageId) {
		boolean ret = false;
		if (appStatus.getProcessingMsgId(ProductCategory.valueOf(category.toUpperCase())) == messageId) {
			ret = true;
		}
		return new ResponseEntity<Boolean>(ret, HttpStatus.OK);
	}

}
