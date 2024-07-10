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

package esa.s1pdgs.cpoc.message.kafka.config;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * Check the initialization of the kafka properties
 *
 * @author Viveris Technologies
 */
@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class KafkaPropertiesTest {

    /**
     * Properties to test
     */
    @Autowired
    private KafkaProperties properties;

    /**
     * Test the initialization
     */
    @Test
    public void testInitialization() {
        assertEquals("mqi-0", properties.getHostname());
        assertEquals("mqi-server", properties.getClientId());
        assertEquals("t-pdgs-errors", properties.getErrorTopic());

        // Producer
        assertEquals(10, properties.getMaxRetries());
    }

    /**
     * Test setters
     */
    @Test
    public void testSetters() {
        properties.setBootstrapServers("url:port");
        properties.setErrorTopic("test-error-topic");
        properties.setClientId("client-id");
        properties.setHostname("host-test");
        properties.setMaxRetries(5);

        // General
        assertEquals("url:port", properties.getBootstrapServers());
        assertEquals("client-id", properties.getClientId());
        assertEquals("host-test", properties.getHostname());
        assertEquals("test-error-topic", properties.getErrorTopic());

        // Producer
        assertEquals(5, properties.getMaxRetries());
    }
}
