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

import esa.s1pdgs.cpoc.mqi.model.rest.Ack;

/**
 * Available states of a MQI message:
 * <li>READ: message read by a KAFKA coonsumer</li>
 * <li>SEND: message send to a application by a MQI server</li>
 * <li>ACK_OK: message successfully processing by an application</li>
 * <li>ACK_KO: message processing by an application but error occurred</li>
 */
public enum MessageState {
    READ, SEND, ACK_OK, ACK_KO, ACK_WARN;
	
	public static MessageState of(Ack ack) {  
		switch (ack) {
			case OK:
				return ACK_OK;
			case ERROR:
				return ACK_KO;
			case WARN:
				return ACK_WARN;
			default:
				throw new IllegalArgumentException(String.format("Invalid Ack %s", ack));
		}
	}
}
