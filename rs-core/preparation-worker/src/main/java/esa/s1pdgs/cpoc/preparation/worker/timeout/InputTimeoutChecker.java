package esa.s1pdgs.cpoc.preparation.worker.timeout;

import java.time.LocalDateTime;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;

/**
 * Checks for a specific tasktable/joborder input if the timeout has been expired
 */
@FunctionalInterface
public interface InputTimeoutChecker {
	public static final InputTimeoutChecker NULL = (x,y) -> true;
	
	boolean isTimeoutExpiredFor(final AppDataJob job, final TaskTableInput input);
	
	default LocalDateTime timeoutFor(final AppDataJob job, final TaskTableInput input) {
		return null;
	}
}
