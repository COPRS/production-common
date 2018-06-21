package fr.viveris.s1pdgs.level0.wrapper.model.kafka;

import java.util.Objects;

import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;

/**
 * @author Viveris Technologies
 */
public class ObsQueueMessage extends QueueMessage {

    /**
     * Key in OBS
     */
    private final String keyObs;

    /**
     * @param family
     * @param productName
     * @param keyObs
     */
    public ObsQueueMessage(final ProductFamily family, final String productName,
            final String keyObs) {
        super(family, productName);
        this.keyObs = keyObs;
    }

    /**
     * @return the keyObs
     */
    public String getKeyObs() {
        return keyObs;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String superStr = super.toStringForExtendedClasses();
        return String.format("{%s, keyObs: %s}", superStr, keyObs);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int superHash = super.hashCode();
        return Objects.hash(superHash, keyObs);
    }

    /**
     * @see java.lang.Object#equals()
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            ObsQueueMessage other = (ObsQueueMessage) obj;
            // field comparison
            ret = super.equals(other) && Objects.equals(keyObs, other.keyObs);
        }
        return ret;
    }
}
