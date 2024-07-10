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

import esa.s1pdgs.cpoc.common.ResumeDetails;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;

/**
 * Test the class KafkaSendException
 * 
 * @author Viveris Technologies
 */
public class MqiPublicationErrorTest {

    /**
     * Test the KafkaSendException
     */
    @Test
    public void testKafkaSendException() {
        MqiPublicationError e1 = new MqiPublicationError("topic-kafka",
                "dto-object", "product-name", "error message",
                new Throwable("throwable message"));

        assertEquals("topic-kafka", e1.getTopic());
        assertEquals("dto-object", e1.getDto());
        assertEquals("product-name", e1.getProductName());
        assertEquals(ErrorCode.MQI_PUBLICATION_ERROR, e1.getCode());
        assertEquals("error message", e1.getMessage());
        assertEquals("throwable message", e1.getCause().getMessage());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[resuming "
                + (new ResumeDetails("topic-kafka", "dto-object")).toString()
                + "]"));
        assertTrue(str1.contains("[productName product-name]"));
        assertTrue(str1.contains("[msg error message]"));
    }

}
