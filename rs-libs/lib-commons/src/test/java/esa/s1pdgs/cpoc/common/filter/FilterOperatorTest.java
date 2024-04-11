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
 * Test the enumeration FilterOperator
 * 
 * @author Viveris Technologies
 */
public class FilterOperatorTest {

    /**
     * Test default method of enumeration
     */
    @Test
    public void testValueOf() {
        assertEquals(6, FilterOperator.values().length);
        assertEquals(FilterOperator.EQ, FilterOperator.valueOf("EQ"));
        assertEquals(FilterOperator.LT, FilterOperator.valueOf("LT"));
        assertEquals(FilterOperator.LTE, FilterOperator.valueOf("LTE"));
        assertEquals(FilterOperator.GT, FilterOperator.valueOf("GT"));
        assertEquals(FilterOperator.GTE, FilterOperator.valueOf("GTE"));
        assertEquals(FilterOperator.NEQ, FilterOperator.valueOf("NEQ"));
    }
}
