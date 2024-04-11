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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the DTO object KafkaConfigFileDto
 * 
 * @author Viveris Technologies
 */
public class GenericPublicationMessageDtoTest {

    /**
     * Test getters, setters and constructors
     */
    @Test
    public void testGettersSettersConstructors() {
        GenericPublicationMessageDto<String> dto =
                new GenericPublicationMessageDto<String>(123,
                        ProductFamily.L0_ACN, "message");
        assertEquals(123, dto.getInputMessageId());
        assertEquals("message", dto.getMessageToPublish());
        assertNull(dto.getInputKey());
        assertNull(dto.getOutputKey());
        assertEquals(ProductFamily.L0_ACN, dto.getFamily());

        dto = new GenericPublicationMessageDto<String>(ProductFamily.L0_ACN,
                "message");
        assertEquals(0, dto.getInputMessageId());
        assertEquals("message", dto.getMessageToPublish());
        assertNull(dto.getInputKey());
        assertNull(dto.getOutputKey());
        assertEquals(ProductFamily.L0_ACN, dto.getFamily());

        dto = new GenericPublicationMessageDto<String>();
        dto.setInputMessageId(321);
        dto.setInputKey("othey-input");
        dto.setOutputKey("otheyoutput");
        dto.setFamily(ProductFamily.L1_ACN);
        dto.setMessageToPublish("topublish");
        assertEquals(321, dto.getInputMessageId());
        assertEquals("topublish", dto.getMessageToPublish());
        assertEquals("othey-input", dto.getInputKey());
        assertEquals("otheyoutput", dto.getOutputKey());
        assertEquals(ProductFamily.L1_ACN, dto.getFamily());
        
        dto = new GenericPublicationMessageDto<String>();
        dto.setInputMessageId(321);
        dto.setInputKey("othey-input");
        dto.setOutputKey("otheyoutput");
        dto.setFamily(ProductFamily.L2_ACN);
        dto.setMessageToPublish("topublish");
        assertEquals(321, dto.getInputMessageId());
        assertEquals("topublish", dto.getMessageToPublish());
        assertEquals("othey-input", dto.getInputKey());
        assertEquals("otheyoutput", dto.getOutputKey());
        assertEquals(ProductFamily.L2_ACN, dto.getFamily());
        
    }

    /**
     * Test the toString function
     */
    @Test
    public void testToString() {
        GenericPublicationMessageDto<String> dto =
                new GenericPublicationMessageDto<String>();
        dto.setInputMessageId(321);
        dto.setInputKey("othey-input");
        dto.setOutputKey("otheyoutput");
        dto.setFamily(ProductFamily.L1_ACN);
        dto.setMessageToPublish("topublish");
        
        String str = dto.toString();
        assertTrue("toString should contain the identifier",
                str.contains("inputMessageId: 321"));
        assertTrue("toString should contain the body",
                str.contains("messageToPublish: topublish"));
        assertTrue("toString should contain the input key",
                str.contains("inputKey: othey-input"));
        assertTrue("toString should contain the output key",
                str.contains("outputKey: otheyoutput"));
        assertTrue("toString should contain the family",
                str.contains("family: L1_ACN"));
    }

    /**
     * Check equals and hashcode methods
     */
    @Test
    public void checkEquals() {
        EqualsVerifier.forClass(GenericPublicationMessageDto.class)
                .usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
