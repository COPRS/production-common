package esa.s1pdgs.cpoc.common.time;

import java.time.LocalDateTime;
import java.util.Objects;

public final class TimeInterval implements Comparable<TimeInterval> {
	private final LocalDateTime start;
	private final LocalDateTime stop;
		
	public TimeInterval(final LocalDateTime start, final LocalDateTime stop) {
		this.start = start;
		this.stop = stop;
	}

	public final LocalDateTime getStart() {
		return start;
	}

	public final LocalDateTime getStop() {
		return stop;
	}

	@Override
	public final int hashCode() {
		return Objects.hash(start, stop);
	}

	@Override
	public final boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TimeInterval other = (TimeInterval) obj;
		return Objects.equals(start, other.start) && 
				Objects.equals(stop, other.stop);
	}

	@Override
	public final String toString() {
		return "TimeInterval [start=" + start + ", stop=" + stop + "]";
	}

	@Override
	public int compareTo(final TimeInterval other) {
		final int retval = start.compareTo(other.start);
		if (retval == 0) {
			return stop.compareTo(other.stop);
		}
		return retval;
	};
	
	public final boolean intersects(final TimeInterval interval) {
		return TimeIntervals.intersects(this, interval);
	}
}
