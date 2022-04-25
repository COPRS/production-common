package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;
import esa.s1pdgs.cpoc.mqi.model.control.DemandType;

/**
 * This is supposed to be the basic element that is used in all other job and
 * event messages. It is containing all data that is shared accross all of them.
 * 
 * @author florian_sievert
 *
 */
//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "_class")
public abstract class AbstractMessage {
	@JsonIgnore
	public static final String DEFAULT_PODNAME = System.getenv("HOSTNAME");

	@JsonIgnore
	public static final String NOT_DEFINED = "NOT_DEFINED";

	// use a noticeable UUID default value to make it apparent that it has not been
	// set and to have something
	// to grep for in the logs
	@JsonIgnore
	public static final String DEFAULT_UUID = "00000000-0000-0000-0000-000000000000";

	// use some sane defaults
	protected ProductFamily productFamily = ProductFamily.BLANK;
	protected String keyObjectStorage = NOT_DEFINED;
	protected String storagePath = NOT_DEFINED;
	protected UUID uid = UUID.fromString(DEFAULT_UUID);

	/*
	 * WARNING: the fields below are just for informational purposes and will not be
	 * evaluated in any functional way.
	 */

	/*
	 * Most of the subsystems are not setting these values at the moment. Lets see
	 * if this automatic approach is working.
	 */
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
	protected Date creationDate = new Date();
	protected String podName = DEFAULT_PODNAME;

	protected List<AllowedAction> allowedActions = Collections.emptyList();

	protected DemandType demandType = DemandType.NOMINAL;

	protected int retryCounter = 0;

	protected boolean debug = false;

	protected String extraParameter1;
	protected String extraParameter2;
	protected String extraParameter3;

	public AbstractMessage() {
	}

	public AbstractMessage(final ProductFamily productFamily, final String keyObjectStorage) {
		this.productFamily = productFamily;
		this.keyObjectStorage = keyObjectStorage;
	}

	public String getKeyObjectStorage() {
		return keyObjectStorage;
	}

	public void setKeyObjectStorage(final String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}

	public String getStoragePath() {
		return storagePath;
	}

	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}

	public ProductFamily getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(final ProductFamily productFamily) {
		this.productFamily = productFamily;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(final Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getPodName() {
		return podName;
	}

	public void setPodName(final String podName) {
		this.podName = podName;
	}

	public UUID getUid() {
		return uid;
	}

	public void setUid(final UUID uid) {
		this.uid = uid;
	}

	public List<AllowedAction> getAllowedActions() {
		return allowedActions;
	}

	public void setAllowedActions(final List<AllowedAction> allowedActions) {
		this.allowedActions = allowedActions;
	}

	public DemandType getDemandType() {
		return demandType;
	}

	public void setDemandType(final DemandType demandType) {
		this.demandType = demandType;
	}

	public int getRetryCounter() {
		return retryCounter;
	}

	public void increaseRetryCounter() {
		++this.retryCounter;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(final boolean debug) {
		this.debug = debug;
	}

	@JsonProperty("extra_parameter1_string")
	public String getExtraParameter1() {
		return extraParameter1;
	}

	public void setExtraParameter1(String extraParameter1) {
		this.extraParameter1 = extraParameter1;
	}

	@JsonProperty("extra_parameter2_string")
	public String getExtraParameter2() {
		return extraParameter2;
	}

	public void setExtraParameter2(String extraParameter2) {
		this.extraParameter2 = extraParameter2;
	}

	@JsonProperty("extra_parameter3_string")
	public String getExtraParameter3() {
		return extraParameter3;
	}

	public void setExtraParameter3(String extraParameter3) {
		this.extraParameter3 = extraParameter3;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((allowedActions == null) ? 0 : allowedActions.hashCode());
		result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result + (debug ? 1231 : 1237);
		result = prime * result + ((demandType == null) ? 0 : demandType.hashCode());
		result = prime * result + ((extraParameter1 == null) ? 0 : extraParameter1.hashCode());
		result = prime * result + ((extraParameter2 == null) ? 0 : extraParameter2.hashCode());
		result = prime * result + ((extraParameter3 == null) ? 0 : extraParameter3.hashCode());
		result = prime * result + ((podName == null) ? 0 : podName.hashCode());
		result = prime * result + ((keyObjectStorage == null) ? 0 : keyObjectStorage.hashCode());
		result = prime * result + ((storagePath == null) ? 0 : storagePath.hashCode());
		result = prime * result + ((productFamily == null) ? 0 : productFamily.hashCode());
		result = prime * result + retryCounter;
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractMessage other = (AbstractMessage) obj;
		if (allowedActions == null) {
			if (other.allowedActions != null)
				return false;
		} else if (!allowedActions.equals(other.allowedActions))
			return false;
		if (creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!creationDate.equals(other.creationDate))
			return false;
		if (debug != other.debug)
			return false;
		if (demandType != other.demandType)
			return false;
		if (extraParameter1 == null) {
			if (other.extraParameter1 != null)
				return false;
		} else if (!extraParameter1.equals(other.extraParameter1))
			return false;
		if (extraParameter2 == null) {
			if (other.extraParameter2 != null)
				return false;
		} else if (!extraParameter2.equals(other.extraParameter2))
			return false;
		if (extraParameter3 == null) {
			if (other.extraParameter3 != null)
				return false;
		} else if (!extraParameter3.equals(other.extraParameter3))
			return false;
		if (podName == null) {
			if (other.podName != null)
				return false;
		} else if (!podName.equals(other.podName))
			return false;
		if (keyObjectStorage == null) {
			if (other.keyObjectStorage != null)
				return false;
		} else if (!keyObjectStorage.equals(other.keyObjectStorage))
			return false;
		if (storagePath == null) {
			if (other.storagePath != null)
				return false;
		} else if (!storagePath.equals(other.storagePath))
			return false;
		if (productFamily != other.productFamily)
			return false;
		if (retryCounter != other.retryCounter)
			return false;
		if (uid == null) {
			if (other.uid != null)
				return false;
		} else if (!uid.equals(other.uid))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AbstractMessage [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", storagePath=" + storagePath + ", uid=" + uid + ", creationDate=" + creationDate + ", podName="
				+ podName + ", allowedActions=" + allowedActions + ", demandType=" + demandType + ", retryCounter="
				+ retryCounter + ", debug=" + debug + ", extraParameter1=" + extraParameter1 + ", extraParameter2="
				+ extraParameter2 + ", extraParameter3=" + extraParameter3 + "]";
	}

}
