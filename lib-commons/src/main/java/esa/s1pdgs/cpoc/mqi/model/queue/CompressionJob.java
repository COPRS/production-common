package esa.s1pdgs.cpoc.mqi.model.queue;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class CompressionJob extends AbstractMessage {
	
	private ProductFamily inputProductFamily;
	private String inputKeyObjectStorage;
	private ProductFamily outputProductFamily;
	private String outputKeyObjectStorage;

	// TODO TAI: Der Name ist unglücklich
	private CompressionDirection compressionDirection;

	public ProductFamily getInputProductFamily() {
		return inputProductFamily;
	}

	public void setInputProductFamily(ProductFamily inputProductFamily) {
		this.inputProductFamily = inputProductFamily;
	}

	public String getInputKeyObjectStorage() {
		return inputKeyObjectStorage;
	}

	public void setInputKeyObjectStorage(String inputKeyObjectStorage) {
		this.inputKeyObjectStorage = inputKeyObjectStorage;
	}

	public ProductFamily getOutputProductFamily() {
		return outputProductFamily;
	}

	public void setOutputProductFamily(ProductFamily outputProductFamily) {
		this.outputProductFamily = outputProductFamily;
	}

	public String getOutputKeyObjectStorage() {
		return outputKeyObjectStorage;
	}

	public void setOutputKeyObjectStorage(String outputKeyObjectStorage) {
		this.outputKeyObjectStorage = outputKeyObjectStorage;
	}

	public CompressionDirection getCompressionDirection() {
		return compressionDirection;
	}
	
	public void setCompressionDirection(CompressionDirection compressionDirection) {
		this.compressionDirection = compressionDirection;
	}

	@Override
	public String toString() {
		return "CompressionJob [inputKeyObjectStorage=" + inputKeyObjectStorage + ", outputKeyObjectStorage=" + outputKeyObjectStorage + ", inputProductFamily="
				+ inputProductFamily + ", outputProductFamily=" + outputProductFamily + ", compressionDirection=" + compressionDirection
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((compressionDirection == null) ? 0 : compressionDirection.hashCode());
		result = prime * result + ((inputKeyObjectStorage == null) ? 0 : inputKeyObjectStorage.hashCode());
		result = prime * result + ((outputKeyObjectStorage == null) ? 0 : outputKeyObjectStorage.hashCode());
		result = prime * result + ((inputProductFamily == null) ? 0 : inputProductFamily.hashCode());
		result = prime * result + ((outputProductFamily == null) ? 0 : outputProductFamily.hashCode());
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
		CompressionJob other = (CompressionJob) obj;
		if (compressionDirection != other.compressionDirection)
			return false;
		if (inputKeyObjectStorage == null) {
			if (other.inputKeyObjectStorage != null)
				return false;
		} else if (!outputKeyObjectStorage.equals(other.outputKeyObjectStorage))
			return false;
		if (inputProductFamily == null) {
			if (other.inputProductFamily != null)
				return false;
		} else if (!outputProductFamily.equals(other.outputProductFamily))
			return false;
		return true;
	}

}
