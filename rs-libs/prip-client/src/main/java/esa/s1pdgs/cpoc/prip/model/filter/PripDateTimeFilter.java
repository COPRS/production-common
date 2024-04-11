/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
