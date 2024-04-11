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

package esa.s1pdgs.cpoc.compression.worker.test;

import static org.mockito.Mockito.doReturn;

import org.mockito.Mock;

import esa.s1pdgs.cpoc.compression.worker.config.CompressionWorkerConfigurationProperties;

/**
 * Test class with properties mocked
 * 
 * @author Viveris Technologies
 */
public class MockPropertiesTest {

    /**
     * Topic
     */
    protected static final String TOPIC_NAME = "topic-name";

    /**
     * Application properties
     */
    @Mock
    protected CompressionWorkerConfigurationProperties properties;

    /**
     * Default mock of application properties
     */
    protected void mockDefaultAppProperties() {
        mockTmAppProperties(1800, 600, 300, 60);
    }

    /**
     * Mock timeouts of the application properties
     * 
     * @param tmProcAllTasksS
     * @param tmProcOneTaskS
     * @param tmProcStopS
     * @param tmProcCheckStopS
     */
    protected void mockTmAppProperties(long tmProcAllTasksS,
            long tmProcOneTaskS, long tmProcStopS, long tmProcCheckStopS) {
        doReturn(tmProcAllTasksS).when(properties).getCompressionTimeout();
        doReturn(tmProcStopS).when(properties).getRequestTimeout();
    }

}
