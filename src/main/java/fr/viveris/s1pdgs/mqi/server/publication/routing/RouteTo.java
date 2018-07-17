package fr.viveris.s1pdgs.mqi.server.publication.routing;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Destination of a message
 * 
 * @author Viveris Technologies
 */
@XmlRootElement(name = "route_to")
@XmlAccessorType(XmlAccessType.NONE)
public class RouteTo {

    /**
     * Product family
     */
    @XmlElement(name = "topic")
    private String topic;

    /**
     * Default constructor
     */
    public RouteTo() {
        this.topic = "";
    }

    /**
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @param topic
     *            the topic to set
     */
    public void setTopic(final String topic) {
        this.topic = topic;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{topic: %s}", topic);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(topic);
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
            RouteTo other = (RouteTo) obj;
            ret = Objects.equals(topic, other.topic);
        }
        return ret;
    }
}
