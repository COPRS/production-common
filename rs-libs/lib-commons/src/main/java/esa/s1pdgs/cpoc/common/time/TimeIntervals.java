package esa.s1pdgs.cpoc.common.time;

public final class TimeIntervals {
	public static final boolean intersects(final TimeInterval int1, final TimeInterval int2) {
		return !int2.getStart().isAfter(int1.getStop()) && 
			!int2.getStop().isBefore(int1.getStart());
	}
}
