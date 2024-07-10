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

package esa.s1pdgs.cpoc.common.errors.mqi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the class MqiRouteNotAvailable
 * 
 * @author Viveris Technologies
 */
public class MqiRouteNotAvailableTest {

    /**
     * Test the KafkaSendException
     */
    @Test
    public void test() {
        MqiRouteNotAvailable e1 = new MqiRouteNotAvailable(
                ProductCategory.EDRS_SESSIONS, ProductFamily.AUXILIARY_FILE);

        assertEquals(ProductCategory.EDRS_SESSIONS, e1.getCategory());
        assertEquals(ProductFamily.AUXILIARY_FILE, e1.getFamily());
        assertEquals(ErrorCode.MQI_ROUTE_NOT_AVAILABLE, e1.getCode());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[category EDRS_SESSIONS]"));
        assertTrue(str1.contains("[family AUXILIARY_FILE]"));
    }

}
