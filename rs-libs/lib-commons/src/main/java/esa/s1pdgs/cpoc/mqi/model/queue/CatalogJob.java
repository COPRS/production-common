package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

public class CatalogJob extends AbstractMessage {
	private String productName = NOT_DEFINED;
	private String relativePath = NOT_DEFINED;
	private String missionId = NOT_DEFINED;
	private long productSizeByte = 0L;
	private String mode = "NOMINAL";
	private OQCFlag oqcFlag = OQCFlag.NOT_CHECKED;
	private String timeliness;
	private String stationName;
	private Map<String, String> additionalMetadata = new HashMap<>();

	public CatalogJob() {
		super();
		setAllowedActions(Arrays.asList(AllowedAction.RESTART));
	}

	public CatalogJob(final String productName, final String keyObjectStorage, final ProductFamily family) {
		this(productName, keyObjectStorage, family, null);	
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}

	public CatalogJob(final String productName, final String keyObjectStorage, final ProductFamily family, final String mode) {
		this(productName, keyObjectStorage, family, mode, OQCFlag.NOT_CHECKED, null, UUID.randomUUID());
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}

	public CatalogJob(
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
	
	public CatalogJob(ProductFamily family, String obsKey, String relativePath, long productSizeByte, String missionId,
			String stationName, String mode, String timeliness) {
		super();
		this.productFamily = family;
		this.keyObjectStorage = obsKey;
		this.relativePath = relativePath;
		this.productSizeByte = productSizeByte;
		this.missionId = missionId;
		this.stationName = stationName;
		this.mode = mode;
		this.timeliness = timeliness;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(final String productName) {
		this.productName = productName;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(final String relativePath) {
		this.relativePath = relativePath;
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

	public String getTimeliness() {
		return timeliness;
	}

	public void setTimeliness(final String timeliness) {
		this.timeliness = timeliness;
	}

	public String getStationName() {
		return stationName;
	}

	public void setStationName(final String stationName) {
		this.stationName = stationName;
	}

	public Map<String, String> getAdditionalMetadata() {
		return additionalMetadata;
	}

	public void setAdditionalMetadata(final Map<String, String> additionalMetadata) {
		this.additionalMetadata = additionalMetadata;
	}

	public String getMissionId() {
		return missionId;
	}

	public void setMissionId(String missionId) {
		this.missionId = missionId;
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
		result = prime * result + Objects.hash(additionalMetadata, mode, oqcFlag, productName, relativePath,
				stationName, timeliness, missionId, productSizeByte);
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
		return Objects.equals(additionalMetadata, other.additionalMetadata) && Objects.equals(mode, other.mode)
				&& oqcFlag == other.oqcFlag && Objects.equals(productName, other.productName)
				&& Objects.equals(relativePath, other.relativePath) && Objects.equals(stationName, other.stationName)
				&& Objects.equals(timeliness, other.timeliness) && Objects.equals(missionId, other.missionId)
				&& Objects.equals(productSizeByte, other.productSizeByte);
	}

	@Override
	public String toString() {
		return "CatalogJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", productName=" + productName
				+ ", relativePath=" + relativePath + ", mode=" + mode + ", oqcFlag=" + oqcFlag + ", timeliness="
				+ timeliness + ", uid=" + uid + ", stationName=" + stationName + ", missionId=" + missionId
				+ ", productSizeByte=" + productSizeByte + ", additionalMetadata=" + additionalMetadata + "]";
	}

}
