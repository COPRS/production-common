package esa.s1pdgs.cpoc.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.logging.Log;

public class LogUtils {

    public static void traceLog(Log logger, String message) {
        if (logger.isTraceEnabled()) {
            logger.trace(message);
        }
    }
    
    public static final String toString(final Throwable throwable) {
        final StringWriter writer     = new StringWriter();        
        try (final PrintWriter printWriter = new PrintWriter(writer)){
        	throwable.printStackTrace(printWriter);
        }
        return writer.toString();
    }
}
