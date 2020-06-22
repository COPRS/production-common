package esa.s1pdgs.cpoc.common.utils;

import org.apache.commons.logging.Log;

public class LogUtils {

    public static void traceLog(final Log logger, final String message) {
        if (logger.isTraceEnabled()) {
            logger.trace(message);
        }
    }
    
    public static final String toString(final Throwable throwable) {
    	return Exceptions.toString(throwable);
    }
}
