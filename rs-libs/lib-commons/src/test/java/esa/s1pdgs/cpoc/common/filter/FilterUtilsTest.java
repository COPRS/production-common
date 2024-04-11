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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 * @author Viveris Technologies
 *
 */
public class FilterUtilsTest {

    /**
     * Test extraction when no operator in key
     */
    @Test
    public void testExtractNoOperator() {
        FilterCriterion expected = new FilterCriterion("key-filter", 125, FilterOperator.EQ);
        FilterCriterion actual = FilterUtils.extractCriterion("key-filter", 125);
        assertEquals(expected, actual);
    }

    /**
     * Test extraction when operator lt in key
     */
    @Test
    public void testExtractOperatorLt() {
        FilterCriterion expected = new FilterCriterion("key-filter", 125, FilterOperator.LT);
        FilterCriterion actual = FilterUtils.extractCriterion("key-filter[lt]", 125);
        assertEquals(expected, actual);
    }

    /**
     * Test extraction when operator lte in key
     */
    @Test
    public void testExtractOperatorLte() {
        FilterCriterion expected = new FilterCriterion("key-filter", "125", FilterOperator.LTE);
        FilterCriterion actual = FilterUtils.extractCriterion("key-filter[lte]", "125");
        assertEquals(expected, actual);
    }

    /**
     * Test extraction when operator lt in key
     */
    @Test
    public void testExtractOperatorGt() {
        FilterCriterion expected = new FilterCriterion("key-filter", 125, FilterOperator.GT);
        FilterCriterion actual = FilterUtils.extractCriterion("key-filter[gt]", 125);
        assertEquals(expected, actual);
    }

    /**
     * Test extraction when operator lte in key
     */
    @Test
    public void testExtractOperatorGte() {
        FilterCriterion expected = new FilterCriterion("key-filter", "125", FilterOperator.GTE);
        FilterCriterion actual = FilterUtils.extractCriterion("key-filter[gte]", "125");
        assertEquals(expected, actual);
    }

    /**
     * Test extraction when unknown operator in key
     */
    @Test
    public void testExtractOperatorUnknown() {
        FilterCriterion expected = new FilterCriterion("key-filter[te]", "125", FilterOperator.EQ);
        FilterCriterion actual = FilterUtils.extractCriterion("key-filter[te]", "125");
        assertEquals(expected, actual);
    }
}
