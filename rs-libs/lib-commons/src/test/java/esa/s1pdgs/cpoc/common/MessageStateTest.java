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

package esa.s1pdgs.cpoc.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.MessageState;

/**
 * Test the enumeration MessageState
 * 
 * @author Viveris Technologies
 */
public class MessageStateTest {

    /**
     * Test values and valueOf
     */
    @Test
    public void basic() {
        assertEquals(5, MessageState.values().length);
        
        assertEquals(MessageState.ACK_KO, MessageState.valueOf("ACK_KO"));
        assertEquals(MessageState.ACK_OK, MessageState.valueOf("ACK_OK"));
        assertEquals(MessageState.ACK_WARN, MessageState.valueOf("ACK_WARN"));
        assertEquals(MessageState.READ, MessageState.valueOf("READ"));
        assertEquals(MessageState.SEND, MessageState.valueOf("SEND"));
    }
}
