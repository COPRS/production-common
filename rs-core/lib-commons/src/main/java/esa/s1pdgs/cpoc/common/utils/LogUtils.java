package esa.s1pdgs.cpoc.common.utils;

import org.apache.commons.logging.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtils {
	// S1PRO-1561: this is handle to a Logger instance configured in s1pro-configuration to log without any formatting to allow passing
	// through messages that are already formatted (in e.g. JSON)
	public static final Logger PLAINTEXT = LogManager.getLogger(LogUtils.class);
	

    public static void traceLog(final Log logger, final String message) {
        if (logger.isTraceEnabled()) {
            logger.trace(message);
        }
    }
    
    public static final String toString(final Throwable throwable) {
    	return Exceptions.toString(throwable);
    }
}
