package esa.s1pdgs.cpoc.prip.model.filter;

/**
 * Integer filter for querying the persistence repository.
 */
public class PripIntegerFilter extends PripRangeValueFilter<Long> {

	public PripIntegerFilter(String fieldName) {
		super(fieldName);
	}

	public PripIntegerFilter(String fieldName, Operator operator, Long value) {
		super(fieldName, operator, value);
	}
	
}
