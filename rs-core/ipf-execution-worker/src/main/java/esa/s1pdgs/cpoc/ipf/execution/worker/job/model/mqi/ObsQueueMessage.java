package esa.s1pdgs.cpoc.ipf.execution.worker.job.model.mqi;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.OQCFlag;

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
    
    private OQCFlag oqcFlag;
    
    public ObsQueueMessage(final ProductFamily family, final String productName,
            final String keyObs, final String processMode) {
    	this (family, productName, keyObs, processMode, OQCFlag.NOT_CHECKED);
    }

    /**
     * @param family
     * @param productName
     * @param keyObs
     */
    public ObsQueueMessage(final ProductFamily family, final String productName,
            final String keyObs, final String processMode, final OQCFlag oqcFlag) {
        super(family, productName);
        this.keyObs = keyObs;
        this.processMode = processMode;
        this.oqcFlag = oqcFlag;
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

    public OQCFlag getOqcFlag() {
		return oqcFlag;
	}

	public void setOqcFlag(OQCFlag oqcFlag) {
		this.oqcFlag = oqcFlag;
	}

	/**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String superStr = super.toStringForExtendedClasses();
        return String.format("{%s, keyObs: %s, processMode: %s, ocqFlag: %s}", superStr,
                keyObs, processMode, oqcFlag);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int superHash = super.hashCode();
        return Objects.hash(superHash, keyObs, processMode, oqcFlag);
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
                    && Objects.equals(processMode, other.processMode) && Objects.equals(oqcFlag, other.oqcFlag);
        }
        return ret;
    }
}
