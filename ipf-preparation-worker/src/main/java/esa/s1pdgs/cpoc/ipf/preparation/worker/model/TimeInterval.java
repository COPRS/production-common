package esa.s1pdgs.cpoc.ipf.preparation.worker.model;

import java.time.LocalDateTime;

/**
 * Model class for time intervals
 * 
 * @author Julian Kaping
 */
public class TimeInterval {
	private LocalDateTime start;
	private LocalDateTime stop;

	public TimeInterval(LocalDateTime start, LocalDateTime stop) {
		this.start = start;
		this.stop = stop;
	}

	public LocalDateTime getStart() {
		return start;
	}

	public LocalDateTime getStop() {
		return stop;
	}

	@Override
	public String toString() {
		return "TimeInterval [start=" + start + ", stop=" + stop + "]";
	}

	/**
	 * Checks if this interval intersects with the given interval
	 * 
	 * @param other range to check whether this interval intersects with
	 * @return true, if intervals intersect
	 */
	public boolean intersects(final TimeInterval other) {
		return !other.getStart().isAfter(stop) && !other.getStop().isBefore(start);
	}
}
