package esa.s1pdgs.cpoc.appcatalog.controllers.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.appcatalog.services.mongodb.MongoDBServices;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;

/**
 * REST server for managing MQI messages in DB for the product category
 * LEVEL_JOBS
 * 
 * @author Viveris Technologies
 */
@RestController
@RequestMapping(path = "/mqi/level_jobs")
public class MqiLevelJobController extends GenericMqiController<LevelJobDto> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(MqiLevelJobController.class);

    /**
     * @param mongoDBServices
     * @param maxRetries
     */
    @Autowired
    public MqiLevelJobController(final MongoDBServices mongoDBServices,
            @Value("${mqi.max-retries}") final int maxRetries) {
        super(mongoDBServices, maxRetries, ProductCategory.LEVEL_JOBS);

    }

    /**
     * 
     * @param topic
     * @param pod
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{topic}/nbReading")
    public ResponseEntity<Integer> nbMessages(
            @PathVariable(name = "topic") final String topic,
            @RequestParam("pod") final String pod) {
        try {
            return new ResponseEntity<Integer>(
                    mongoDBServices.countReadingMessages(pod, topic), HttpStatus.OK);
        } catch (Exception exc) {
            LOGGER.error("[earliestOffset] {}", exc.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
