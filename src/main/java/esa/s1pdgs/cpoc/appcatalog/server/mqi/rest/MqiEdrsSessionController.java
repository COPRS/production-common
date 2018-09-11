/**
 * 
 */
package esa.s1pdgs.cpoc.appcatalog.server.mqi.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.appcatalog.server.mqi.db.MqiMessageService;
import esa.s1pdgs.cpoc.appcatalog.server.status.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;

/**
 * REST server for managing MQI messages in DB for the product category
 * EDRS_SESSIONS
 * 
 * @author Viveris Technologies
 */
@RestController
@RequestMapping(path = "/mqi/edrs_sessions")
public class MqiEdrsSessionController
        extends GenericMqiController<EdrsSessionDto> {

    /**
     * @param mongoDBServices
     * @param maxRetries
     */
    @Autowired
    public MqiEdrsSessionController(final MqiMessageService mongoDBServices,
            @Value("${mqi.max-retries}") final int maxRetries,
            final AppStatus appStatus) {
        super(mongoDBServices, maxRetries, ProductCategory.EDRS_SESSIONS, 
                appStatus);
    }
}
