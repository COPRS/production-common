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

package esa.s1pdgs.cpoc.prip.worker;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PripPublishingJobListenerTest {

	@Test
    public void test() {
        final boolean isLineString = "S1A_WV_RAW__0NSV_20200120T114913_20200120T121043_030883_038B58_E294.SAFE"
                .matches("(RF|WV)_RAW__0(A|C|N|S)");
        System.out.println("isLineString: " + isLineString);
        assertFalse(isLineString);
    }
	
	@Test
    public void test2() {
        final boolean isLineString = "S1A_WV_RAW__0NSV_20200120T114913_20200120T121043_030883_038B58_E294.SAFE"
                .matches("S1.*(RF|WV)_RAW__0(A|C|N|S).*");
        System.out.println("isLineString: " + isLineString);
        assertTrue(isLineString);
    }

}
