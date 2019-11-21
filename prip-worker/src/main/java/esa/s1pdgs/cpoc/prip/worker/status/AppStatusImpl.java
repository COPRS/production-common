package esa.s1pdgs.cpoc.prip.worker.status;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appstatus.AbstractAppStatus;
import esa.s1pdgs.cpoc.appstatus.Status;

@Component
public class AppStatusImpl extends AbstractAppStatus {

    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(AppStatusImpl.class);

	private RestHighLevelClient restHighLevelClient;
	
	/**
     * Constructor
     * 
     * @param maxErrorCounter
     */
    @Autowired
    public AppStatusImpl(@Value("${status.max-error-counter:100}") final int maxErrorCounter,
    		RestHighLevelClient restHighLevelClient) {
    	super(new Status(maxErrorCounter, 0));
    	this.restHighLevelClient = restHighLevelClient;
    }

    /**
	 * @return kubernetes readiness
	 */
	@Override
	public boolean getKubernetesReadiness() {		
		boolean elasticSearchReadiness;
		try {
			elasticSearchReadiness = restHighLevelClient.ping();
		} catch (IOException e) {
			elasticSearchReadiness = false;
		}
		return elasticSearchReadiness;
	}
	
    /**
     * Stop the application if someone asks for forcing stop
     */
    @Scheduled(fixedDelayString = "${status.delete-fixed-delay-ms:3000}")
    public void forceStopping() {
        if (isShallBeStopped()) {
            System.exit(0);
        }
    }
}
