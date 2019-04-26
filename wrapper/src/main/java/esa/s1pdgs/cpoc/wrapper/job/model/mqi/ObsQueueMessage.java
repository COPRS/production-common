package esa.s1pdgs.cpoc.wrapper.job.model.mqi;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * @author Viveris Technologies
 */
public class ObsQueueMessage extends QueueMessage {

    /**
     * Key in OBS
     */
    private final String keyObs;

    /**
     * Process mode
     */
    private String processMode;

    /**
     * @param family
     * @param productName
     * @param keyObs
     */
    public ObsQueueMessage(final ProductFamily family, final String productName,
            final String keyObs, final String processMode) {
        super(family, productName);
        this.keyObs = keyObs;
        this.processMode = processMode;
    }

    /**
     * @return the keyObs
     */
    public String getKeyObs() {
        return keyObs;
    }

    /**
     * @return the processMode
     */
    public String getProcessMode() {
        return processMode;
    }

    /**
     * @param processMode
     *            the processMode to set
     */
    public void setProcessMode(String processMode) {
        this.processMode = processMode;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String superStr = super.toStringForExtendedClasses();
        return String.format("{%s, keyObs: %s, processMode: %s}", superStr,
                keyObs, processMode);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int superHash = super.hashCode();
        return Objects.hash(superHash, keyObs, processMode);
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
            ret = super.equals(other) && Objects.equals(keyObs, other.keyObs)
                    && Objects.equals(processMode, other.processMode);
        }
        return ret;
    }
}
