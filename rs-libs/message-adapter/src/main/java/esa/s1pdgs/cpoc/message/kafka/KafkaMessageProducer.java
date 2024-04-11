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

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.message.MessageProducer;

public class KafkaMessageProducer<M> implements MessageProducer<M> {

    private final KafkaTemplate<String, M> template;

    public KafkaMessageProducer(KafkaTemplate<String, M> template) {
        this.template = template;
    }

    @Override
    public void send(String topic, M message) {
        try {
            template.send(topic, message).get();
        } catch (final Exception e) {
            final Throwable cause = Exceptions.unwrap(e);
            throw new RuntimeException(
                    String.format(
                            "Error on publishing %s %s to %s: %s",
                            message.getClass().getSimpleName(),
                            message,
                            topic,
                            Exceptions.messageOf(cause)
                    ),
                    cause
            );
        }
    }

    @Override
    public String toString() {
        return "KafkaMessageProducer";
    }
}
