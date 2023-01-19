package esa.s1pdgs.cpoc.prip.model.filter;

import java.time.LocalDateTime;

import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;

/**
 * Date time filter for querying the persistence repository.
 */
public class PripDateTimeFilter extends PripRangeValueFilter<LocalDateTime> {

	public PripDateTimeFilter(String fieldName) {
		super(fieldName);
	}
	
	public PripDateTimeFilter(PripMetadata.FIELD_NAMES fieldName) {
		this(fieldName.fieldName());
	}
	
	public PripDateTimeFilter(String fieldName, RelationalOperator operator, LocalDateTime value) {
		super(fieldName,operator, value);
	}

	@Override
	public String toString() {
		return this.getFieldName() + " " + (null != this.getRelationalOperator() ? this.getRelationalOperator().getOperator() : "NO_OP") + " "
				+ (null != this.getValue() ? DateUtils.formatToMetadataDateTimeFormat(this.getValue()) : null);
	}

}
