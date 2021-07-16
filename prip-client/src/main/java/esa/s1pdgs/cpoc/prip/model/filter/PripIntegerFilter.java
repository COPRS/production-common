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

	private PripIntegerFilter(String fieldName, RelationalOperator operator, Long value, boolean nested, String path) {
		super(fieldName, operator, value, nested, path);
	}

	// --------------------------------------------------------------------------

	@Override
	public PripIntegerFilter copy() {
		return new PripIntegerFilter(this.getFieldName(), this.getRelationalOperator(), this.getValue(), this.isNested(), this.getPath());
	}

}
