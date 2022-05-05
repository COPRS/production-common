package esa.s1pdgs.cpoc.datalifecycle.worker.rest.model;

public class ProductPatchDto {
	private String operatorName;
	private String evictionTimeInUncompressedStorage;
	private String evictionTimeInCompressedStorage;

	public ProductPatchDto() {
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	public String getEvictionTimeInUncompressedStorage() {
		return evictionTimeInUncompressedStorage;
	}

	public void setEvictionTimeInUncompressedStorage(String evictionTimeInUncompressedStorage) {
		this.evictionTimeInUncompressedStorage = evictionTimeInUncompressedStorage;
	}

	public String getEvictionTimeInCompressedStorage() {
		return evictionTimeInCompressedStorage;
	}

	public void setEvictionTimeInCompressedStorage(String evictionTimeInCompressedStorage) {
		this.evictionTimeInCompressedStorage = evictionTimeInCompressedStorage;
	}

	@Override
	public String toString() {
		return "ProductPatchDto [operatorName=" + operatorName + ", evictionTimeInUncompressedStorage="
				+ evictionTimeInUncompressedStorage + ", evictionTimeInCompressedStorage="
				+ evictionTimeInCompressedStorage + "]";
	}

}
