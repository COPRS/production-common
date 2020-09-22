package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;
import esa.s1pdgs.cpoc.mqi.model.control.DemandType;

/**
 * DTO used to (re)submit specific workflows and steps to be executed.
 * 
 * @author nicolas_kukolja
 */
public class OnDemandEvent extends AbstractMessage {

	private String productName;
	private String mode = "NOMINAL";
	private ApplicationLevel productionType;
	private String productType;
	private Map<String, Object> metadata;
	
	public OnDemandEvent() {
		super();
	}

	public OnDemandEvent(final ProductFamily productFamily, final String keyObjectStorage, final String productName,
			final ApplicationLevel productionType, final String mode) {
		super(productFamily, keyObjectStorage);

		this.productName = productName;
		this.productionType = productionType;
		this.mode = mode;

		this.uid = UUID.randomUUID();

		this.allowedActions = Collections.singletonList(AllowedAction.RESUBMIT);
		this.demandType = DemandType.OPERATOR_DEMAND;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(final String productName) {
		this.productName = productName;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(final String mode) {
		this.mode = mode;
	}

	public ApplicationLevel getProductionType() {
		return productionType;
	}

	public void setProductionType(final ApplicationLevel productionType) {
		this.productionType = productionType;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(final String productType) {
		this.productType = productType;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(final Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	@Override
	public int hashCode() {
		return Objects.hash(metadata, mode, productName, productType, productionType, creationDate, hostname,
				keyObjectStorage, productFamily, uid, allowedActions, demandType, debug, retryCounter);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final OnDemandEvent other = (OnDemandEvent) obj;
		return Objects.equals(metadata, other.metadata) && Objects.equals(mode, other.mode)
				&& Objects.equals(productName, other.productName) && Objects.equals(productType, other.productType)
				&& Objects.equals(productionType, other.productionType) && Objects.equals(hostname, other.hostname)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage) && productFamily == other.productFamily
				&& Objects.equals(uid, other.uid) && Objects.equals(allowedActions, other.getAllowedActions())
				&& demandType == other.demandType && debug == other.debug && retryCounter == other.retryCounter;
	}

	@Override
	public String toString() {
		return "OnDemandEvent [productName=" + productName + ", mode=" + mode + ", productionType=" + productionType
				+ ", productType=" + productType + ", metadata=" + metadata + ", productFamily=" + productFamily
				+ ", keyObjectStorage=" + keyObjectStorage + ", uid=" + uid + ", creationDate=" + creationDate
				+ ", hostname=" + hostname + ", allowedActions=" + allowedActions + ", demandType=" + demandType
				+ ", retryCounter=" + retryCounter + ", debug=" + debug + "]";
	}

}
