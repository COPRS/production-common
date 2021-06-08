package esa.s1pdgs.cpoc.prip.model.filter;

/**
 * Integer filter for querying the persistence repository.
 */
public class PripIntegerFilter extends PripRangeValueFilter<Long> {

	public PripIntegerFilter(String fieldName) {
		super(fieldName);
	}

	public PripIntegerFilter(String fieldName, RelationalOperator operator, Long value) {
		super(fieldName, operator, value);
	}

	public PripIntegerFilter(final PripIntegerFilter filter) {
		this(filter.getFieldName(), filter.getRelationalOperator(), filter.getValue());
	}

	// --------------------------------------------------------------------------

	@Override
	public PripIntegerFilter copy() {
		return new PripIntegerFilter(this);
	}

}
