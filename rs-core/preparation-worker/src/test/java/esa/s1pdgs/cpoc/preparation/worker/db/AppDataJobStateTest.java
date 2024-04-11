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

package esa.s1pdgs.cpoc.preparation.worker.db;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;

/**
 * Available states of a job
 */
public class AppDataJobStateTest {

    /**
     * Test values and valueOf
     */
    @Test
    public void basic() {
        assertEquals(4, AppDataJobState.values().length);
        
        assertEquals(AppDataJobState.DISPATCHING, AppDataJobState.valueOf("DISPATCHING"));
        assertEquals(AppDataJobState.GENERATING, AppDataJobState.valueOf("GENERATING"));
        assertEquals(AppDataJobState.TERMINATED, AppDataJobState.valueOf("TERMINATED"));
        assertEquals(AppDataJobState.WAITING, AppDataJobState.valueOf("WAITING"));
    }
}
