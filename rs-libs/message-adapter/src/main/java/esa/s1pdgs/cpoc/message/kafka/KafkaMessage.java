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

package esa.s1pdgs.cpoc.message.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import esa.s1pdgs.cpoc.message.Message;

public class KafkaMessage<M> implements Message<M> {

    private final M data;
    private final ConsumerRecord<String, M> kafkaRecord;

    public KafkaMessage(M data, ConsumerRecord<String, M> kafkaRecord) {
        this.data = data;
        this.kafkaRecord = kafkaRecord;
    }

    @Override
    public M data() {
        return data;
    }

    @Override
    public Object internalMessage() {
        return kafkaRecord;
    }
}
