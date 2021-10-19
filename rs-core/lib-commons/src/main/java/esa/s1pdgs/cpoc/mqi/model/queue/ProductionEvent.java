package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
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
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		result = prime * result + ((oqcFlag == null) ? 0 : oqcFlag.hashCode());
		result = prime * result + ((productName == null) ? 0 : productName.hashCode());
		result = prime * result + ((timeliness == null) ? 0 : timeliness.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductionEvent other = (ProductionEvent) obj;
		if (mode == null) {
			if (other.mode != null)
				return false;
		} else if (!mode.equals(other.mode))
			return false;
		if (oqcFlag != other.oqcFlag)
			return false;
		if (productName == null) {
			if (other.productName != null)
				return false;
		} else if (!productName.equals(other.productName))
			return false;
		if (timeliness == null) {
			if (other.timeliness != null)
				return false;
		} else if (!timeliness.equals(other.timeliness))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "ProductionEvent [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", productName=" + productName
				+ ", mode=" + mode + ", oqcFlag=" + oqcFlag + ", uid=" + uid +", timeliness=" + timeliness + "]";
	}
}
