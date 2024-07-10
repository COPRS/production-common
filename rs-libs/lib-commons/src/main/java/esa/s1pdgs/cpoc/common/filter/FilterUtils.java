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

package esa.s1pdgs.cpoc.common.filter;

/**
 * Filter utilities
 * @author Viveris Technologies
 *
 */
public class FilterUtils {

    /**
     * Extract filter criterion from key and value
     * @param filterKey
     * @param filterValue
     * @return
     */
    public static FilterCriterion extractCriterion(final String filterKey,
            final Object filterValue) {
        FilterCriterion criterion = new FilterCriterion(filterKey, filterValue);
        for (FilterOperator op : FilterOperator.values()) {
            if (filterKey.endsWith("[" + op.name().toLowerCase() + "]")) {
                criterion =
                        new FilterCriterion(
                                filterKey.substring(0,
                                        filterKey.length()
                                                - op.name().length() - 2),
                                filterValue, op);
                break;
            }
        }
        return criterion;
    }
}
