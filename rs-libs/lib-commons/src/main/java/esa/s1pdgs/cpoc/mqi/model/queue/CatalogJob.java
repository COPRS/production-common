package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

public class CatalogJob extends AbstractMessage {
	public static final String ADDITIONAL_METADATA_FLAG_KEY = "isPathExtracted";
	
	private static final String PRODUCT_NAME_KEY = "productName";
	private static final String RELATIVE_PATH_KEY = "relativePath";
	private static final String MODE_KEY = "mode";
	
	private long productSizeByte = 0L;
	private OQCFlag oqcFlag = OQCFlag.NOT_CHECKED;
	private String stationName;

	public CatalogJob() {
		super();
		setAllowedActions(Arrays.asList(AllowedAction.RESTART));
	}

	public CatalogJob(final String productName, final String keyObjectStorage, final ProductFamily family) {
		this(productName, keyObjectStorage, family, null);
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}

	public CatalogJob(final String productName, final String keyObjectStorage, final ProductFamily family,
			final String mode) {
		this(productName, keyObjectStorage, family, mode, OQCFlag.NOT_CHECKED, null, UUID.randomUUID());
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}

	public CatalogJob(final String productName, final String keyObjectStorage, final ProductFamily family,
			final String mode, final OQCFlag oqcFlag, final String timeliness, final UUID reportUid) {
		super(family, keyObjectStorage);
		this.metadata.put(PRODUCT_NAME_KEY, productName);
		this.metadata.put(MODE_KEY, mode);
		this.uid = reportUid;
		this.oqcFlag = oqcFlag;
		this.timeliness = timeliness;
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}

	public CatalogJob(ProductFamily family, String obsKey, String relativePath, long productSizeByte, String missionId,
			String stationName, String mode, String timeliness) {
		super();
		this.productFamily = family;
		this.keyObjectStorage = obsKey;
		this.metadata.put(RELATIVE_PATH_KEY, relativePath);
		this.productSizeByte = productSizeByte;
		this.missionId = missionId;
		this.stationName = stationName;
		this.metadata.put(MODE_KEY, mode);
		this.timeliness = timeliness;
	}

	public CatalogJob(ProductFamily family, String productName, String storagePath, String relativePath,
			long productSizeByte, String missionId, String stationName, String mode, String timeliness) {
		super(family, productName);
		this.productFamily = family;
		this.metadata.put(PRODUCT_NAME_KEY, productName);
		this.storagePath = storagePath;
		this.metadata.put(RELATIVE_PATH_KEY, relativePath);
		this.productSizeByte = productSizeByte;
		this.missionId = missionId;
		this.stationName = stationName;
		this.metadata.put(MODE_KEY, mode);
		this.timeliness = timeliness;
	}

	@JsonIgnore
	public String getProductName() {
		return metadata.getOrDefault(PRODUCT_NAME_KEY, "").toString();
	}

	public void setProductName(final String productName) {
		this.metadata.put(PRODUCT_NAME_KEY, productName);
	}

	@JsonIgnore
	public String getRelativePath() {
		return metadata.getOrDefault(RELATIVE_PATH_KEY, "").toString();
	}

	public void setRelativePath(final String relativePath) {
		this.metadata.put(RELATIVE_PATH_KEY, relativePath);
	}

	@JsonIgnore
	public String getMode() {
		if (metadata.get(MODE_KEY) == null) {
			return null;
		}
		return metadata.getOrDefault(MODE_KEY, "").toString();
	}

	public void setMode(final String mode) {
		this.metadata.put(MODE_KEY, mode);
	}

	public OQCFlag getOqcFlag() {
		return oqcFlag;
	}

	public void setOqcFlag(final OQCFlag oqcFlag) {
		this.oqcFlag = oqcFlag;
	}

	public String getStationName() {
		return stationName;
	}

	public void setStationName(final String stationName) {
		this.stationName = stationName;
	}

	public long getProductSizeByte() {
		return productSizeByte;
	}

	public void setProductSizeByte(long productSizeByte) {
		this.productSizeByte = productSizeByte;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(oqcFlag, stationName, timeliness, productSizeByte);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CatalogJob other = (CatalogJob) obj;
		return oqcFlag == other.oqcFlag && Objects.equals(stationName, other.stationName)
				&& Objects.equals(timeliness, other.timeliness)
				&& Objects.equals(productSizeByte, other.productSizeByte);
	}

	@Override
	public String toString() {
		return "CatalogJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", storagePath=" + storagePath + ", creationDate=" + creationDate + ", podName=" + podName
				+ ", oqcFlag=" + oqcFlag + ", timeliness=" + timeliness + ", uid=" + uid + ", stationName="
				+ stationName + ", missionId=" + missionId + ", productSizeByte=" + productSizeByte + "]";
	}

}
