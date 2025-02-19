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

/**
 * Test the enumeration ProductFamily
 * 
 * @author Viveris Technologies
 */
public class AppStateTest {

    /**
     * Test default method of enumeration
     */
    @Test
    public void testValueOf() {
        assertEquals(5, AppState.values().length);
        assertEquals(AppState.WAITING, AppState.valueOf("WAITING"));
        assertEquals(AppState.PROCESSING, AppState.valueOf("PROCESSING"));
        assertEquals(AppState.STOPPING, AppState.valueOf("STOPPING"));
        assertEquals(AppState.FATALERROR, AppState.valueOf("FATALERROR"));
        assertEquals(AppState.ERROR, AppState.valueOf("ERROR"));
    }

}
