package esa.s1pdgs.cpoc.common.utils;

import org.apache.commons.logging.Log;

public class LogUtils {

    public static void traceLog(Log logger, String message) {
        if (logger.isTraceEnabled()) {
            logger.trace(message);
        }
    }
}
