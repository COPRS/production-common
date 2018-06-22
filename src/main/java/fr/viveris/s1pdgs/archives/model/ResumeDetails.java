package fr.viveris.s1pdgs.archives.model;

import java.util.Objects;

/**
 * Details to resume an error
 * 
 * @author Viveris Technologies
 */
public class ResumeDetails {

    /**
     * Topic name
     */
    private final String topicName;

    /**
     * DTO
     */
    private final Object dto;

    /**
     * Constructor
     * 
     * @param topicName
     * @param dto
     */
    public ResumeDetails(final String topicName, final Object dto) {
        this.topicName = topicName;
        this.dto = dto;
    }

    /**
     * @return the topicName
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * @return the dto
     */
    public Object getDto() {
        return dto;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{topicName: %s, dto: %s}", topicName, dto);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(topicName, dto);
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
            final ResumeDetails other = (ResumeDetails) obj;
            ret = Objects.equals(topicName, other.topicName)
                    && Objects.equals(dto, other.dto);
        }
        return ret;
    }

}
