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
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiCategoryNotAvailable;

/**
 * Test the class KafkaSendException
 * 
 * @author Viveris Technologies
 */
public class MqiCategoryNotAvailableTest {

    /**
     * Test the KafkaSendException
     */
    @Test
    public void test() {
        MqiCategoryNotAvailable e1 = new MqiCategoryNotAvailable(ProductCategory.EDRS_SESSIONS,
                "consumer");

        assertEquals(ProductCategory.EDRS_SESSIONS, e1.getCategory());
        assertEquals("consumer", e1.getType());
        assertEquals(ErrorCode.MQI_CATEGORY_NOT_AVAILABLE, e1.getCode());
        assertEquals("No consumer available for category EDRS_SESSIONS", e1.getMessage());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[category EDRS_SESSIONS]"));
        assertTrue(str1.contains("[msg No consumer available for category EDRS_SESSIONS]"));
    }

}
