package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

public class ProductionEvent extends AbstractMessage {
	private String productName = NOT_DEFINED;
	private String mode = "NOMINAL";
	private OQCFlag oqcFlag = OQCFlag.NOT_CHECKED;
	private String timeliness;

	public ProductionEvent() {
		super();
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}

	public ProductionEvent(final String productName, final String keyObjectStorage, final ProductFamily family) {
		this(productName, keyObjectStorage, family, null);	
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}

	public ProductionEvent(final String productName, final String keyObjectStorage, final ProductFamily family, final String mode) {
		this(productName, keyObjectStorage, family, mode, OQCFlag.NOT_CHECKED, null, UUID.randomUUID());
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}
	
	public ProductionEvent(
			final String productName, 
			final String keyObjectStorage, 
			final ProductFamily family, 
			final String mode, 
			final OQCFlag oqcFlag,
			final String timeliness,
			final UUID reportUid
	) {
		super(family, keyObjectStorage);
		this.productName = productName;
		this.mode = mode;
		this.uid = reportUid;
		this.oqcFlag = oqcFlag;
		this.timeliness = timeliness;
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}

	public String getMode() {
		return mode;
	}

	public void setMode(final String mode) {
		this.mode = mode;
	}
	
	public OQCFlag getOqcFlag() {
		return oqcFlag;
	}

	public void setOqcFlag(final OQCFlag oqcFlag) {
		this.oqcFlag = oqcFlag;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(final String productName) {
		this.productName = productName;
	}
	
	public String getTimeliness() {
		return timeliness;
	}

	public void setTimeliness(final String timeliness) {
		this.timeliness = timeliness;
	}

	@Override
	public int hashCode() {
		return Objects.hash(creationDate, hostname, keyObjectStorage, mode, timeliness, oqcFlag, productFamily, productName, uid,
				allowedActions, demandType, debug, retryCounter);
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
		final ProductionEvent other = (ProductionEvent) obj;
		return Objects.equals(creationDate, other.creationDate) 
				&& Objects.equals(hostname, other.hostname)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage) 
				&& Objects.equals(mode, other.mode)
				&& oqcFlag == other.oqcFlag 
				&& productFamily == other.productFamily
				&& Objects.equals(timeliness, other.timeliness)
				&& Objects.equals(uid, other.uid)
				&& Objects.equals(productName, other.productName)
				&& Objects.equals(allowedActions, other.getAllowedActions())
		        && demandType == other.demandType
		        && debug == other.debug
		        && retryCounter == other.retryCounter;
	}

	@Override
	public String toString() {
		return "ProductionEvent [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", productName=" + productName
				+ ", mode=" + mode + ", oqcFlag=" + oqcFlag + ", uid=" + uid +", timeliness=" + timeliness + "]";
	}
}
