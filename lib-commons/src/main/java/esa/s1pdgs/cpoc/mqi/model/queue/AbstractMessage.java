package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.ControlAction;
import esa.s1pdgs.cpoc.mqi.model.control.ControlDemandType;

/**
 * This is supposed to be the basic element that is used in all other
 * job and event messages. It is containing all data that is shared
 * accross all of them.
 * 
 * @author florian_sievert
 *
 */
public abstract class AbstractMessage {
	@JsonIgnore
	public static final String DEFAULT_HOSTNAME = System.getenv("HOSTNAME");
	
	@JsonIgnore
	public static final String NOT_DEFINED = "NOT_DEFINED";
	
	// use a noticeable UUID default value to make it apparent that it has not been set and to have something 
	// to grep for in the logs
	@JsonIgnore
	public static final String DEFAULT_UUID = "00000000-0000-0000-0000-000000000000";

	// use some sane defaults
	protected ProductFamily productFamily = ProductFamily.BLANK;
	protected String keyObjectStorage = NOT_DEFINED;
	protected UUID uid = UUID.fromString(DEFAULT_UUID);
	
	/*
	 * WARNING: the fields below are just for informational purposes and will not be evaluated
	 * in any functional way.
	 */
	
	/* Most of the subsystems are not setting these
	 * values at the moment. Lets see if this automatic
	 * approach is working. 
	 */
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC")
	protected Date creationDate = new Date();
	protected String hostname = DEFAULT_HOSTNAME;
	
	protected ControlAction controlAction = ControlAction.NO_ACTION;
	
	protected ControlDemandType controlDemandType = ControlDemandType.NOMINAL;
	
	protected int controlRetryCounter = 0;
	
	protected boolean controlDebug = false;
	
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

	public String getHostname() {
		return hostname;
	}

	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}

	public UUID getUid() {
		return uid;
	}

	public void setUid(final UUID uid) {
		this.uid = uid;
	}

	public ControlAction getControlAction() {
		return controlAction;
	}

	public void setControlAction(ControlAction controlAction) {
		this.controlAction = controlAction;
	}

	public ControlDemandType getControlDemandType() {
		return controlDemandType;
	}

	public void setControlDemandType(ControlDemandType controlDemandType) {
		this.controlDemandType = controlDemandType;
	}

	public int getControlRetryCounter() {
		return controlRetryCounter;
	}

	public void increaseControlRetryCounter() {
		++this.controlRetryCounter;
	}

	public boolean isControlDebug() {
		return controlDebug;
	}

	public void setControlDebug(boolean controlDebug) {
		this.controlDebug = controlDebug;
	}

	@Override
	public String toString() {
		return "AbstractMessage [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage + ", uid="
				+ uid + ", creationDate=" + creationDate + ", hostname=" + hostname + ", controlAction=" + controlAction 
				+ ", controlDemandType=" + controlDemandType + ", controlRetryCounter=" + controlRetryCounter 
				+ ", controlDebug=" + controlDebug + "]";
	}	
	
	
}
