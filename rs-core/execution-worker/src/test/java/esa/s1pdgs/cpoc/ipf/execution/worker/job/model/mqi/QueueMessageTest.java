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

package esa.s1pdgs.cpoc.ipf.execution.worker.job.model.mqi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.model.mqi.QueueMessage;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object QueueMessage
 * 
 * @author Viveris Technologies
 */
public class QueueMessageTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        QueueMessage obj =
                new QueueMessage(ProductFamily.AUXILIARY_FILE, "product-name");
        assertEquals(ProductFamily.AUXILIARY_FILE, obj.getFamily());
        assertEquals("product-name", obj.getProductName());
    }

    /**
     * Test to string
     */
    @Test
    public void testToString() {
        QueueMessage obj =
                new QueueMessage(ProductFamily.AUXILIARY_FILE, "product-name");
        String str = obj.toString();
        assertTrue(str.startsWith("{"));
        assertTrue(str.contains("family: AUXILIARY_FILE"));
        assertTrue(str.contains("productName: product-name"));
        assertTrue(str.endsWith("}"));

        str = obj.toStringForExtendedClasses();
        assertFalse(str.startsWith("{"));
        assertTrue(str.contains("family: AUXILIARY_FILE"));
        assertTrue(str.contains("productName: product-name"));
        assertFalse(str.endsWith("}"));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(QueueMessage.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
