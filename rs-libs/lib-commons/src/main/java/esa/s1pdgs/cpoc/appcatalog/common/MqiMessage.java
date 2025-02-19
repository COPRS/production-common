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

package esa.s1pdgs.cpoc.appcatalog.common;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;

/**
 * Object Stored in the database
 * 
 * @author Viveris Technologies
 */
public class MqiMessage extends AbstractRequest {
	
	// dirty workaround to have the mongodb id mapped
	private long id;
	
    /**
     * Pod who read the message
     */
    private String readingPod;
    /**
     * Date where the pod read the message
     */
    private Date lastReadDate;
    /**
     * Default Constructor
     */
    public MqiMessage() {
        super();
    }

    public MqiMessage(final ProductCategory category, final String topic,
            final int partition, final long offset, final String group, final MessageState state,
            final String readingPod, final Date lastReadDate, final String sendingPod,
            final Date lastSendDate, final Date lastAckDate, final int nbRetries, final Object dto,
            final Date creationDate) {
        super();
		this.category = category;
		this.topic = topic;
		this.partition = partition;
		this.offset = offset;
		this.group = group;
		this.state = state;
		this.sendingPod = sendingPod;
		this.lastSendDate = lastSendDate;
		this.lastAckDate = lastAckDate;
		this.nbRetries = nbRetries;
		this.dto = dto;
		this.creationDate = creationDate;
        this.readingPod = readingPod;
        this.lastReadDate = lastReadDate;
    }
    
    @JsonIgnore
	public long getId() {
		return id;
	}

    @JsonIgnore
	public void setId(final long id) {
		this.id = id;
	}
    
	/**
	 * @return the dto
	 */
	public Object getDto() {
	    return dto;
	}

	/**
	 * @param dto the dto to set
	 */
	public void setDto(final Object dto) {
	    this.dto = dto;
	}

    /**
     * @return the readingPod
     */
    public String getReadingPod() {
        return readingPod;
    }
    /**
     * @param readingPod the readingPod to set
     */
    public void setReadingPod(final String readingPod) {
        this.readingPod = readingPod;
    }
    /**
     * @return the lastReadDate
     */
    public Date getLastReadDate() {
        return lastReadDate;
    }
    /**
     * @param lastReadDate the lastReadDate to set
     */
    public void setLastReadDate(final Date lastReadDate) {
        this.lastReadDate = lastReadDate;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{\"category\":\"" + category + "\", \"id\":\"" + id
                + "\", \"topic\":\"" + topic + "\", \"partition\":\"" + partition + "\", \"offset\":\""
                + offset + "\", \"group\":\"" + group + "\", \"state\":\"" + state
                + "\", \"readingPod\":\"" + readingPod + "\", \"lastReadDate\":\""
                + lastReadDate + "\", \"sendingPod\":\"" + sendingPod + "\", \"lastSendDate\":\""
                + lastSendDate + "\", \"lastAckDate\":\"" + lastAckDate + "\", \"nbRetries\":\""
                + nbRetries + "\", \"dto\":\"" + dto + "\" , \"creationDate\":\"" + creationDate + "\"}";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((category == null) ? 0 : category.hashCode());
        result = prime * result
                + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = prime * result + ((dto == null) ? 0 : dto.hashCode());
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result
                + ((lastAckDate == null) ? 0 : lastAckDate.hashCode());
        result = prime * result
                + ((lastReadDate == null) ? 0 : lastReadDate.hashCode());
        result = prime * result
                + ((lastSendDate == null) ? 0 : lastSendDate.hashCode());
        result = prime * result + nbRetries;
        result = prime * result + (int) (offset ^ (offset >>> 32));
        result = prime * result + partition;
        result = prime * result
                + ((readingPod == null) ? 0 : readingPod.hashCode());
        result = prime * result
                + ((sendingPod == null) ? 0 : sendingPod.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + ((topic == null) ? 0 : topic.hashCode());
        return result;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MqiMessage other = (MqiMessage) obj;
        if (category != other.category)
            return false;
        if (creationDate == null) {
            if (other.creationDate != null)
                return false;
        } else if (!creationDate.equals(other.creationDate))
            return false;
        if (dto == null) {
            if (other.dto != null)
                return false;
        } else if (!dto.equals(other.dto))
            return false;
        if (group == null) {
            if (other.group != null)
                return false;
        } else if (!group.equals(other.group))
            return false;
        if (id != other.id)
            return false;
        if (lastAckDate == null) {
            if (other.lastAckDate != null)
                return false;
        } else if (!lastAckDate.equals(other.lastAckDate))
            return false;
        if (lastReadDate == null) {
            if (other.lastReadDate != null)
                return false;
        } else if (!lastReadDate.equals(other.lastReadDate))
            return false;
        if (lastSendDate == null) {
            if (other.lastSendDate != null)
                return false;
        } else if (!lastSendDate.equals(other.lastSendDate))
            return false;
        if (nbRetries != other.nbRetries)
            return false;
        if (offset != other.offset)
            return false;
        if (partition != other.partition)
            return false;
        if (readingPod == null) {
            if (other.readingPod != null)
                return false;
        } else if (!readingPod.equals(other.readingPod))
            return false;
        if (sendingPod == null) {
            if (other.sendingPod != null)
                return false;
        } else if (!sendingPod.equals(other.sendingPod))
            return false;
        if (state != other.state)
            return false;
        if (topic == null) {
            if (other.topic != null)
                return false;
        } else if (!topic.equals(other.topic))
            return false;
        return true;
    }


    

}