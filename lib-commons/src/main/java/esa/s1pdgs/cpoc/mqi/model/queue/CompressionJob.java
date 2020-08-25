package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.ControlAction;

public class CompressionJob extends AbstractMessage {	
	// use some sane defaults
	private ProductFamily outputProductFamily = ProductFamily.BLANK;
	private String outputKeyObjectStorage = NOT_DEFINED;
	private CompressionDirection compressionDirection = CompressionDirection.UNDEFINED;

	public CompressionJob() {
		super();
		setAllowedControlActions(Arrays.asList(ControlAction.RESTART));
	}	

	public CompressionJob(
			final String inputKeyObjectStorage, 
			final ProductFamily inputProductFamily, 
			final String outputKeyObjectStorage, 
			final ProductFamily outputProductFamily,
			final CompressionDirection compressionDirection
	) {
		super(inputProductFamily, inputKeyObjectStorage);
		this.outputKeyObjectStorage = outputKeyObjectStorage;
		this.outputProductFamily = outputProductFamily;
		this.compressionDirection = compressionDirection;
		setAllowedControlActions(Arrays.asList(ControlAction.RESTART));
	}

	public ProductFamily getOutputProductFamily() {
		return outputProductFamily;
	}

	public void setOutputProductFamily(final ProductFamily outputProductFamily) {
		this.outputProductFamily = outputProductFamily;
	}

	public String getOutputKeyObjectStorage() {
		return outputKeyObjectStorage;
	}

	public void setOutputKeyObjectStorage(final String outputKeyObjectStorage) {
		this.outputKeyObjectStorage = outputKeyObjectStorage;
	}

	public CompressionDirection getCompressionDirection() {
		return compressionDirection;
	}

	public void setCompressionDirection(final CompressionDirection compressionDirection) {
		this.compressionDirection = compressionDirection;
	}

	@Override
	public int hashCode() {
		return Objects.hash(compressionDirection, creationDate, hostname, keyObjectStorage, outputKeyObjectStorage,
				outputProductFamily, productFamily, uid,
				allowedControlActions, controlDemandType, controlDebug, controlRetryCounter);
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
		final CompressionJob other = (CompressionJob) obj;
		return compressionDirection == other.compressionDirection 
				&& Objects.equals(creationDate, other.creationDate)
				&& Objects.equals(hostname, other.hostname) 
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(outputKeyObjectStorage, other.outputKeyObjectStorage)
				&& Objects.equals(uid, other.uid)
				&& outputProductFamily == other.outputProductFamily 
				&& productFamily == other.productFamily
				&& Objects.equals(allowedControlActions, other.getAllowedControlActions())
		        && controlDemandType == other.controlDemandType
		        && controlDebug == other.controlDebug
		        && controlRetryCounter == other.controlRetryCounter;
	}

	@Override
	public String toString() {
		return "CompressionJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", outputProductFamily="
				+ outputProductFamily + ", outputKeyObjectStorage=" + outputKeyObjectStorage + ", compressionDirection="
				+ compressionDirection + ", uid=" + uid +"]";
	}
}
