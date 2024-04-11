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

package esa.s1pdgs.cpoc.appcatalog.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object AppCatSendMessageDto
 * 
 * @author Viveris Technologies
 */
public class AppCatSendMessageDtoTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        AppCatSendMessageDto obj = new AppCatSendMessageDto();
        assertFalse(obj.isForce());
        obj.setPod("pod-name");
        obj.setForce(true);
        assertEquals("pod-name", obj.getPod());
        assertTrue(obj.isForce());

        AppCatSendMessageDto obj1 = new AppCatSendMessageDto("pod-name", true);
        assertEquals("pod-name", obj1.getPod());
        assertTrue(obj.isForce());
    }

    /**
     * Test to string
     */
    @Test
    public void testToString() {
        AppCatSendMessageDto obj = new AppCatSendMessageDto("pod-name", true);
        String str = obj.toString();
        assertTrue(str.contains("pod: pod-name"));
        assertTrue(str.contains("force: true"));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(AppCatSendMessageDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
