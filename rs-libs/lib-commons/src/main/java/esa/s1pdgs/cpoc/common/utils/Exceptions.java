package esa.s1pdgs.cpoc.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

public class Exceptions {	
	public static Throwable unwrap(final Exception e) {
		Throwable res = e;
		
		while (res instanceof ExecutionException) {
			res = res.getCause();
		}
		return res;
	}
	
    public static final String toString(final Throwable throwable) {
        final StringWriter writer     = new StringWriter();        
        try (final PrintWriter printWriter = new PrintWriter(writer)){
        	throwable.printStackTrace(printWriter);
        }
        return writer.toString();
    }
    
	public static final String messageOf(final Throwable e) {
		if (e instanceof AbstractCodedException) {
			return ((AbstractCodedException) e).getLogMessage();
		}
		if (e.getMessage() == null) {
			return "(no errormessage provided)";
		}
		return e.getMessage();
	}
}
