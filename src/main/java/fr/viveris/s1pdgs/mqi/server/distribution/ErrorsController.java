package fr.viveris.s1pdgs.mqi.server.distribution;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.viveris.s1pdgs.mqi.server.publication.MessagePublicationController;

/**
 * Controller around errors
 * 
 * @author Viveris Technologies
 */
@RestController
@RequestMapping(path = "/errors")
public class ErrorsController {

    /**
     * Message publication controller
     */
    protected final MessagePublicationController publication;

    /**
     * Constructor
     * 
     * @param publication
     */
    public ErrorsController(final MessagePublicationController publication) {
        this.publication = publication;
    }

    /**
     * Publish a message
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, path = "/publish")
    public ResponseEntity<Void> publish(@RequestBody() final String message) {
        ResponseEntity<Void> ret =
                new ResponseEntity<>(HttpStatus.GATEWAY_TIMEOUT);
        if (publication.publishError(message)) {
            ret = new ResponseEntity<>(HttpStatus.OK);
        }
        return ret;
    }

}
