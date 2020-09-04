package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Collections;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class IpfPreparationJob extends AbstractMessage {	
    private ApplicationLevel level;
    private GenericMessageDto<CatalogEvent> eventMessage;
    private String taskTableName;
    private String startTime;    
    private String stopTime;    

    public IpfPreparationJob() {
		allowedActions = Collections.singletonList(AllowedAction.RESTART);
	}
    
	public ApplicationLevel getLevel() {
		return level;
	}

	public void setLevel(final ApplicationLevel level) {
		this.level = level;
	}

	public GenericMessageDto<CatalogEvent> getEventMessage() {
		return eventMessage;
	}

	public void setEventMessage(final GenericMessageDto<CatalogEvent> eventMessage) {
		this.eventMessage = eventMessage;
	}

	public String getTaskTableName() {
		return taskTableName;
	}

	public void setTaskTableName(final String taskTableName) {
		this.taskTableName = taskTableName;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(final String startTime) {
		this.startTime = startTime;
	}

	public String getStopTime() {
		return stopTime;
	}

	public void setStopTime(final String stopTime) {
		this.stopTime = stopTime;
	}


	@Override
	public int hashCode() {
		return Objects.hash(
				creationDate, 
				hostname, 
				keyObjectStorage,
				productFamily, 
				uid,
			    level,
			    eventMessage,
			    taskTableName,
			    startTime,  
			    stopTime,   
			    allowedActions,
			    demandType,
			    retryCounter,
			    debug
		);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final IpfPreparationJob other = (IpfPreparationJob) obj;
		return Objects.equals(creationDate, other.creationDate) 			
				&& Objects.equals(hostname, other.hostname)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(uid, other.uid)
			    && Objects.equals(level,other.level)	    
			    && Objects.equals(allowedActions, other.allowedActions)
			    && Objects.equals(demandType, other.demandType)
			    && Objects.equals(retryCounter, other.retryCounter)
			    && Objects.equals(debug, other.debug)			    
			    && Objects.equals(eventMessage, other.eventMessage)
			    && Objects.equals(taskTableName,other.taskTableName)
			    && Objects.equals(startTime, other.startTime)
			    && Objects.equals(stopTime, other.stopTime) 							
				&& productFamily == other.productFamily;
	}

	@Override
	public String toString() {
		return "IpfPreparationJob [level=" + level + ", eventMessage=" + eventMessage + ", taskTableName="
				+ taskTableName + ", startTime=" + startTime + ", stopTime=" + stopTime 
				+ ", productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage + ", uid=" + uid
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", allowedActions="
				+ allowedActions + ", demandType=" + demandType + ", retryCounter="
				+ retryCounter + ", debug=" + debug + "]";
	}
}
