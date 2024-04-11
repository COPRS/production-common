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

package esa.s1pdgs.cpoc.mqi.model.rest;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Exchanged message for consumption
 * 
 * @author Viveris Technologies
 * @param <T>
 *            the message
 */
public class GenericMessageDto<T> implements MessageDto<T> {

    /**
     * Message identifier
     */
    private long id;

    /**
     * Input key
     */
    private String inputKey;

    /**
     * Message body
     */
    private T body;

    /**
     * Default constructor
     */
    public GenericMessageDto() {
        super();
    }

    /**
     * Constructor using fields
     * 
     * @param identifier
     * @param body
     */
    public GenericMessageDto(final long id, final String inputKey,
            final T body) {
        this();
        this.id = id;
        this.inputKey = inputKey;
        this.body = body;
    }

    /**
     * @return the identifier
     */
    public long getId() {
        return id;
    }

    /**
     * @param identifier
     *            the identifier to set
     */
    public void setId(final long id) {
        this.id = id;
    }

    /**
     * @return the inputKey
     */
    public String getInputKey() {
        return inputKey;
    }

    /**
     * @param inputKey
     *            the inputKey to set
     */
    public void setInputKey(final String inputKey) {
        this.inputKey = inputKey;
    }

    /**
     * @return the body
     */
    public T getBody() {
        return body;
    }

    /**
     * @param body
     *            the body to set
     */
    public void setBody(final T body) {
        this.body = body;
    }
    
    @Override
    @JsonIgnore
	public T getDto() {
		return body;
	}

	/**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{id: %s, inputKey: %s, body: %s}",
                id, inputKey, body);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, inputKey, body);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            final GenericMessageDto<?> other = (GenericMessageDto<?>) obj;
            ret = id == other.id
                    && Objects.equals(inputKey, other.inputKey)
                    && Objects.equals(body, other.body);
        }
        return ret;
    }
}