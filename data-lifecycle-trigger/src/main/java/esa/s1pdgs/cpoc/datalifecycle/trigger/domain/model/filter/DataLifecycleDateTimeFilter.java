package esa.s1pdgs.cpoc.datalifecycle.trigger.domain.model.filter;

import java.time.LocalDateTime;

import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.model.DataLifecycleMetadata;

/**
 * Date time filter for querying the persistence repository.
 */
public class DataLifecycleDateTimeFilter extends DataLifecycleRangeValueFilter<LocalDateTime> {

	public DataLifecycleDateTimeFilter(String fieldName) {
		super(fieldName);
	}

	public DataLifecycleDateTimeFilter(DataLifecycleMetadata.FIELD_NAME fieldName) {
		this(fieldName.fieldName());
	}

	public DataLifecycleDateTimeFilter(String fieldName, Operator operator, LocalDateTime value) {
		super(fieldName,operator, value);
	}

	// --------------------------------------------------------------------------

	@Override
	public String toString() {
		return this.getFieldName() + " " + (null != this.getOperator() ? this.getOperator().getOperator() : "NO_OP") + " "
				+ (null != this.getValue() ? DateUtils.formatToMetadataDateTimeFormat(this.getValue()) : null);
	}

}
