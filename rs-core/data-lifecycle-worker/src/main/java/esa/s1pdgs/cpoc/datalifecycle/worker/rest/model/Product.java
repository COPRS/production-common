package esa.s1pdgs.cpoc.datalifecycle.worker.rest.model;

public class Product {

	private String productname;

	private String pathInUncompressedStorage;
	private Boolean persistentInUncompressedStorage;
	private String evictionTimeInUncompressedStorage;
	private String productFamilyInUncompressedStorage;

	private String pathInCompressedStorage;
	private Boolean persistentInCompressedStorage;
	private String evictionTimeInCompressedStorage;
	private String productFamilyInCompressedStorage;

	private Boolean availableInLta;
	private String lastModificationTime;

	public Product() {
		super();
	}

	public Product(String productname, String pathInUncompressedStorage, Boolean persistentInUncompressedStorage,
			String evictionTimeInUncompressedStorage, String productFamilyInUncompressedStorage,
			String pathInCompressedStorage, Boolean persistentInCompressedStorage,
			String evictionTimeInCompressedStorage, String productFamilyInCompressedStorage, Boolean availableInLta,
			String lastModificationTime) {
		super();
		this.productname = productname;
		this.pathInUncompressedStorage = pathInUncompressedStorage;
		this.persistentInUncompressedStorage = persistentInUncompressedStorage;
		this.evictionTimeInUncompressedStorage = evictionTimeInUncompressedStorage;
		this.productFamilyInUncompressedStorage = productFamilyInUncompressedStorage;
		this.pathInCompressedStorage = pathInCompressedStorage;
		this.persistentInCompressedStorage = persistentInCompressedStorage;
		this.evictionTimeInCompressedStorage = evictionTimeInCompressedStorage;
		this.productFamilyInCompressedStorage = productFamilyInCompressedStorage;
		this.availableInLta = availableInLta;
		this.lastModificationTime = lastModificationTime;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public String getPathInUncompressedStorage() {
		return pathInUncompressedStorage;
	}

	public void setPathInUncompressedStorage(String pathInUncompressedStorage) {
		this.pathInUncompressedStorage = pathInUncompressedStorage;
	}

	public Boolean isPersistentInUncompressedStorage() {
		return persistentInUncompressedStorage;
	}

	public void setPersistentInUncompressedStorage(Boolean persistentInUncompressedStorage) {
		this.persistentInUncompressedStorage = persistentInUncompressedStorage;
	}

	public String getEvictionTimeInUncompressedStorage() {
		return evictionTimeInUncompressedStorage;
	}

	public void setEvictionTimeInUncompressedStorage(String evictionTimeInUncompressedStorage) {
		this.evictionTimeInUncompressedStorage = evictionTimeInUncompressedStorage;
	}

	public String getProductFamilyInUncompressedStorage() {
		return productFamilyInUncompressedStorage;
	}

	public void setProductFamilyInUncompressedStorage(String productFamilyInUncompressedStorage) {
		this.productFamilyInUncompressedStorage = productFamilyInUncompressedStorage;
	}

	public String getPathInCompressedStorage() {
		return pathInCompressedStorage;
	}

	public void setPathInCompressedStorage(String pathInCompressedStorage) {
		this.pathInCompressedStorage = pathInCompressedStorage;
	}

	public Boolean isPersistentInCompressedStorage() {
		return persistentInCompressedStorage;
	}

	public void setPersistentInCompressedStorage(Boolean persistentInCompressedStorage) {
		this.persistentInCompressedStorage = persistentInCompressedStorage;
	}

	public String getEvictionTimeInCompressedStorage() {
		return evictionTimeInCompressedStorage;
	}

	public void setEvictionTimeInCompressedStorage(String evictionTimeInCompressedStorage) {
		this.evictionTimeInCompressedStorage = evictionTimeInCompressedStorage;
	}

	public String getProductFamilyInCompressedStorage() {
		return productFamilyInCompressedStorage;
	}

	public void setProductFamilyInCompressedStorage(String productFamilyInCompressedStorage) {
		this.productFamilyInCompressedStorage = productFamilyInCompressedStorage;
	}

	public Boolean isAvailableInLta() {
		return availableInLta;
	}

	public void setAvailableInLta(Boolean availableInLta) {
		this.availableInLta = availableInLta;
	}

	public String getLastModificationTime() {
		return lastModificationTime;
	}

	public void setLastModificationTime(String lastModificationTime) {
		this.lastModificationTime = lastModificationTime;
	}

	@Override
	public String toString() {
		return "Product [productname=" + productname + ", pathInUncompressedStorage=" + pathInUncompressedStorage
				+ ", persistentInUncompressedStorage=" + persistentInUncompressedStorage
				+ ", evictionTimeInUncompressedStorage=" + evictionTimeInUncompressedStorage
				+ ", productFamilyInUncompressedStorage=" + productFamilyInUncompressedStorage
				+ ", pathInCompressedStorage=" + pathInCompressedStorage + ", persistentInCompressedStorage="
				+ persistentInCompressedStorage + ", evictionTimeInCompressedStorage=" + evictionTimeInCompressedStorage
				+ ", productFamilyInCompressedStorage=" + productFamilyInCompressedStorage + ", availableInLta="
				+ availableInLta + ", lastModificationTime=" + lastModificationTime + "]";
	}

}
