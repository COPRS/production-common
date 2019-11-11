package esa.s1pdgs.cpoc.mqi.server.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appstatus.AbstractAppStatus;
import esa.s1pdgs.cpoc.appstatus.Status;

@Component
public class AppStatusImpl extends AbstractAppStatus {
	
	/**
	 * Logger
	 */
	protected static final Logger LOGGER = LogManager.getLogger(AppStatusImpl.class);

	@Autowired
	public AppStatusImpl(@Value("${application.max-error-counter}")final int maxErrorCounter) {
		super(new Status(0, maxErrorCounter));
	}

	@Override
	public void forceStopping() {
		if (this.isShallBeStopped()) {
            System.exit(0);
        }
	}
}