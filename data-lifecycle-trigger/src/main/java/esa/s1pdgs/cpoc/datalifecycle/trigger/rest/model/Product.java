package esa.s1pdgs.cpoc.datalifecycle.trigger.rest.model;

public class Product {

	private String productname;

	private String pathInUncompressedStorage;
	private boolean persistentInUncompressedStorage;
	private String evictionTimeInUncompressedStorage;
	private String productFamilyInUncompressedStorage;

	private String pathInCompressedStorage;
	private boolean persistentInCompressedStorage;
	private String evictionTimeInCompressedStorage;
	private String productFamilyInCompressedStorage;

	private boolean availableInLta;
	private String lastModificationTime;

	public Product() {
		super();
	}

	public Product(String productname, String pathInUncompressedStorage, boolean persistentInUncompressedStorage,
			String evictionTimeInUncompressedStorage, String productFamilyInUncompressedStorage,
			String pathInCompressedStorage, boolean persistentInCompressedStorage,
			String evictionTimeInCompressedStorage, String productFamilyInCompressedStorage, boolean availableInLta,
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

	public boolean isPersistentInUncompressedStorage() {
		return persistentInUncompressedStorage;
	}

	public void setPersistentInUncompressedStorage(boolean persistentInUncompressedStorage) {
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

	public boolean isPersistentInCompressedStorage() {
		return persistentInCompressedStorage;
	}

	public void setPersistentInCompressedStorage(boolean persistentInCompressedStorage) {
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

	public boolean isAvailableInLta() {
		return availableInLta;
	}

	public void setAvailableInLta(boolean availableInLta) {
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
