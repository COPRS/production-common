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

package esa.s1pdgs.cpoc.errorrepo;

import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;
import esa.s1pdgs.cpoc.message.MessageProducer;

public class MessageErrorRepoAppender implements ErrorRepoAppender {

    private final String topic;
    private final MessageProducer<FailedProcessing> messageProducer;

    public MessageErrorRepoAppender(final String topic, final MessageProducer<FailedProcessing> messageProducer) {
        this.topic = topic;
        this.messageProducer = messageProducer;
    }

    @Override
    public void send(final FailedProcessing errorRequest) {
        try {
            messageProducer.send(topic, errorRequest);
        } catch (final Exception e) {
            throw new RuntimeException(
                    String.format("Error appending message to error queue '%s': %s", topic, Exceptions.messageOf(e)),
                    e
            );
        }
    }
}
