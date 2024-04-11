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

package esa.s1pdgs.cpoc.mqi.model.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the DTO object AckMessageDto
 * 
 * @author Viveris Technologies
 */
public class AckMessageDtoTest {

    /**
     * Test getters, setters and constructors
     */
    @Test
    public void testGettersSettersConstructors() {
        AckMessageDto dto = new AckMessageDto(123, Ack.ERROR, "ack-message", true);
        assertEquals(123, dto.getMessageId());
        assertEquals(Ack.ERROR, dto.getAck());
        assertEquals("ack-message", dto.getMessage());
        assertTrue(dto.isStop());

        dto = new AckMessageDto();
        dto.setMessageId(321);
        dto.setAck(Ack.OK);
        dto.setMessage("other-message");
        dto.setStop(true);
        assertEquals(321, dto.getMessageId());
        assertEquals(Ack.OK, dto.getAck());
        assertEquals("other-message", dto.getMessage());
        assertTrue(dto.isStop());
    }

    /**
     * Test the toString function
     */
    @Test
    public void testToString() {
        AckMessageDto dto = new AckMessageDto(123, Ack.ERROR, "ack-message", true);
        String str = dto.toString();
        assertTrue("toString should contain the identifier",
                str.contains("messageId: 123"));
        assertTrue("toString should contain the ack",
                str.contains("ack: ERROR"));
        assertTrue("toString should contain the message",
                str.contains("message: ack-message"));
        assertTrue("toString should contain stop",
                str.contains("stop: true"));
    }

    /**
     * Check equals and hashcode methods
     */
    @Test
    public void checkEquals() {
        EqualsVerifier.forClass(AckMessageDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
