package esa.s1pdgs.cpoc.prip.model.filter;

/**
 * Double filter for querying the persistence repository.
 */
public class PripDoubleFilter extends PripRangeValueFilter<Double> {

	public PripDoubleFilter(String fieldName) {
		super(fieldName);
	}

	public PripDoubleFilter(String fieldName, RelationalOperator operator, Double value) {
		super(fieldName, operator, value);
	}

	public PripDoubleFilter(final PripDoubleFilter filter) {
		this(filter.getFieldName(), filter.getRelationalOperator(), filter.getValue());
	}

	// --------------------------------------------------------------------------

	@Override
	public PripDoubleFilter copy() {
		return new PripDoubleFilter(this);
	}

}
