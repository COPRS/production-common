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

import java.util.Objects;

/**
 * Filter criterion
 * 
 * @author Viveris Technologies
 */
public class FilterCriterion {

    /**
     * The key of the field to filter
     */
    private String key;

    /**
     * The comparaison value
     */
    private Object value;

    /**
     * The comparaison operator
     */
    private FilterOperator operator;

    /**
     * Constructor
     * 
     * @param key
     * @param value
     */
    public FilterCriterion(final String key, final Object value) {
        this.operator = FilterOperator.EQ;
        this.value = value;
        this.key = key;
    }

    /**
     * Constructor
     * 
     * @param key
     * @param value
     * @param operator
     */
    public FilterCriterion(final String key, final Object value,
            final FilterOperator operator) {
        this.operator = operator;
        this.value = value;
        this.key = key;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(final String key) {
        this.key = key;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(final Object value) {
        this.value = value;
    }

    /**
     * @return the operator
     */
    public FilterOperator getOperator() {
        return operator;
    }

    /**
     * @param operator
     *            the operator to set
     */
    public void setOperator(final FilterOperator operator) {
        this.operator = operator;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{key: %s, value: %s, operator: %s}", key, value,
                operator);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(key, value, operator);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            FilterCriterion other = (FilterCriterion) obj;
            ret = Objects.equals(key, other.key)
                    && Objects.equals(value, other.value)
                    && Objects.equals(operator, other.operator);
        }
        return ret;
    }
}
