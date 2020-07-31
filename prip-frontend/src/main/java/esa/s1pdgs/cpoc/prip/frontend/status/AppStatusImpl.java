package esa.s1pdgs.cpoc.prip.frontend.status;

import java.io.IOException;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appstatus.AbstractAppStatus;
import esa.s1pdgs.cpoc.appstatus.Status;

@Component
public class AppStatusImpl extends AbstractAppStatus {
	private RestHighLevelClient restHighLevelClient;
	
	/**
     * Constructor
     * 
     * @param maxErrorCounter
     */
    @Autowired
    public AppStatusImpl(
    		@Value("${status.max-error-counter:100}") final int maxErrorCounter,
            @Qualifier("systemExitCall") final Runnable systemExitCall,
    		final RestHighLevelClient restHighLevelClient
   ) {
    	super(new Status(maxErrorCounter, 0), systemExitCall);
    	this.restHighLevelClient = restHighLevelClient;
    }

    /**
	 * @return kubernetes readiness
	 */
	@Override
	public boolean getKubernetesReadiness() {		
		boolean elasticSearchReadiness;
		try {
			elasticSearchReadiness = restHighLevelClient.ping(RequestOptions.DEFAULT);
		} catch (final IOException e) {
			elasticSearchReadiness = false;
		}
		return elasticSearchReadiness;
	}
	
    /**
     * Stop the application if someone asks for forcing stop
     */
    @Override
	@Scheduled(fixedDelayString = "${status.delete-fixed-delay-ms:3000}")
    public void forceStopping() {
        if (isShallBeStopped()) {
            System.exit(0);
        }
    }
}
