package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

public class CompressionJob extends AbstractMessage {
	// use some sane defaults
	private ProductFamily outputProductFamily = ProductFamily.BLANK;
	private String outputKeyObjectStorage = NOT_DEFINED;
	private CompressionDirection compressionDirection = CompressionDirection.UNDEFINED;

	public CompressionJob() {
		super();
		setAllowedActions(Arrays.asList(AllowedAction.RESTART));
	}

	public CompressionJob(final String inputKeyObjectStorage, final ProductFamily inputProductFamily,
			final String outputKeyObjectStorage, final ProductFamily outputProductFamily,
			final CompressionDirection compressionDirection) {
		super(inputProductFamily, inputKeyObjectStorage);
		this.outputKeyObjectStorage = outputKeyObjectStorage;
		this.outputProductFamily = outputProductFamily;
		this.compressionDirection = compressionDirection;
		setAllowedActions(Arrays.asList(AllowedAction.RESTART));
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
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((compressionDirection == null) ? 0 : compressionDirection.hashCode());
		result = prime * result + ((outputKeyObjectStorage == null) ? 0 : outputKeyObjectStorage.hashCode());
		result = prime * result + ((outputProductFamily == null) ? 0 : outputProductFamily.hashCode());
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
		CompressionJob other = (CompressionJob) obj;
		if (compressionDirection != other.compressionDirection)
			return false;
		if (outputKeyObjectStorage == null) {
			if (other.outputKeyObjectStorage != null)
				return false;
		} else if (!outputKeyObjectStorage.equals(other.outputKeyObjectStorage))
			return false;
		if (outputProductFamily != other.outputProductFamily)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CompressionJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", storagePath=" + storagePath + ", creationDate=" + creationDate + ", podName=" + podName
				+ ", outputProductFamily=" + outputProductFamily + ", outputKeyObjectStorage=" + outputKeyObjectStorage
				+ ", compressionDirection=" + compressionDirection + ", uid=" + uid + ", rsChainVersion="
				+ rsChainVersion + "]";
	}
}
