package esa.s1pdgs.cpoc.compression.process;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class CompressProcessor {
    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(CompressProcessor.class);
    
    public void processTask() {
    	  LOGGER.trace("[MONITOR] [step 0] Waiting message");
    }
}
