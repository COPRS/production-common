package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

public class CatalogJob extends AbstractMessage {	
	private String productName = NOT_DEFINED;
	private String relativePath = NOT_DEFINED;
	private String mode = "NOMINAL";
    private OQCFlag oqcFlag = OQCFlag.NOT_CHECKED;
    private String timeliness;
    private String stationName;
	private Map<String,String> additionalMetadata = new HashMap<>();
    
    public CatalogJob() {
    	super();
    	setAllowedActions(Arrays.asList(AllowedAction.RESTART));
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


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(additionalMetadata, mode, oqcFlag, productName, relativePath, stationName, timeliness);
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
				&& Objects.equals(timeliness, other.timeliness);
	}

	@Override
	public String toString() {
		return "CatalogJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", productName=" + productName
				+ ", relativePath=" + relativePath + ", mode=" + mode + ", oqcFlag=" + oqcFlag
				+ ", timeliness=" + timeliness + ", uid=" + uid + ", stationName=" + stationName + 
				", additionalMetadata="  + additionalMetadata+ "]";
	}

}
