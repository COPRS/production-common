package esa.s1pdgs.cpoc.appcatalog.common.rest.model.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericMessageDto;
import esa.s1pdgs.cpoc.common.ApplicationLevel;

/**
 * Class used for exchanging applicative data of a job
 * 
 * @author Viveris Technologies
 */
public class AppDataJob {

    private long identifier;

    private ApplicationLevel level;

    private String pod;

    private AppDataJobState state;

    private Date creationDate;

    private Date lastUpdateDate;

    private String sessionId;

    private List<MqiGenericMessageDto<?>> messages;

    /**
     * 
     */
    public AppDataJob() {
        super();
        this.messages = new ArrayList<>();
        this.state = AppDataJobState.WAITING;
    }

    /**
     * @return the identifier
     */
    public long getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     *            the identifier to set
     */
    public void setIdentifier(long identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the level
     */
    public ApplicationLevel getLevel() {
        return level;
    }

    /**
     * @param level
     *            the level to set
     */
    public void setLevel(ApplicationLevel level) {
        this.level = level;
    }

    /**
     * @return the pod
     */
    public String getPod() {
        return pod;
    }

    /**
     * @param pod
     *            the pod to set
     */
    public void setPod(String pod) {
        this.pod = pod;
    }

    /**
     * @return the state
     */
    public AppDataJobState getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(AppDataJobState state) {
        this.state = state;
    }

    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate
     *            the creationDate to set
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the lastUpdateDate
     */
    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    /**
     * @param lastUpdateDate
     *            the lastUpdateDate to set
     */
    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    /**
     * @return the sessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @param sessionId
     *            the sessionId to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return the messages
     */
    public List<MqiGenericMessageDto<?>> getMessages() {
        return messages;
    }

    /**
     * @param messages
     *            the messages to set
     */
    public void setMessages(List<MqiGenericMessageDto<?>> messages) {
        this.messages = messages;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "{identifier: %s, level: %s, pod: %s, state: %s, creationDate: %s, lastUpdateDate: %s, sessionId: %s, messages: %s}",
                identifier, level, pod, state, creationDate, lastUpdateDate,
                sessionId, messages);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(identifier, level, pod, state, creationDate,
                lastUpdateDate, sessionId, messages);
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
            AppDataJob other = (AppDataJob) obj;
            ret = identifier == other.identifier
                    && Objects.equals(level, other.level)
                    && Objects.equals(pod, other.pod)
                    && Objects.equals(state, other.state)
                    && Objects.equals(creationDate, other.creationDate)
                    && Objects.equals(lastUpdateDate, other.lastUpdateDate)
                    && Objects.equals(sessionId, other.sessionId)
                    && Objects.equals(messages, other.messages);
        }
        return ret;
    }

}
