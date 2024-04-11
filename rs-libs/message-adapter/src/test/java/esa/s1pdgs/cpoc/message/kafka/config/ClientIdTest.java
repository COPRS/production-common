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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class ClientIdTest {

    @Test
    public void clientIdForRawIdAndTopic() {
        assertThat(KafkaConsumerClientId.clientIdForRawIdAndTopic("worker0-host0", "topic33"), is(equalTo("worker0-host0-topic33")));
    }

    @Test
    public void rawIdForTopic() {
        assertThat(KafkaConsumerClientId.rawIdForTopic("worker0-host0-topic33-0", "topic33"), is(equalTo("worker0-host0")));
        assertThat(KafkaConsumerClientId.rawIdForTopic("worker0-host0-topic33-1", "topic33"), is(equalTo("worker0-host0")));
        assertThat(KafkaConsumerClientId.rawIdForTopic("worker0-host0-topic33-99", "topic33"), is(equalTo("worker0-host0")));
    }
}