package fr.viveris.s1pdgs.jobgenerator.model.product;

import java.util.Date;

import fr.viveris.s1pdgs.jobgenerator.model.ProductMode;

public abstract class AbstractProduct<T> {

	private String identifier;
	
	private String satelliteId;
	
	private String missionId;

	private Date startTime;

	private Date stopTime;
	
	private int instrumentConfigurationId;
	
	private T object;
	
	private ProductMode mode;
	
	private String productType;

	/**
	 * @param identifier
	 * @param satelliteId
	 * @param missionId
	 * @param startTime
	 * @param stopTime
	 * @param object
	 */
	public AbstractProduct(String identifier, String satelliteId, String missionId, Date startTime, Date stopTime, T object, String productType) {
		super();
		this.identifier = identifier;
		this.satelliteId = satelliteId;
		this.missionId = missionId;
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.object = object;
		this.instrumentConfigurationId = -1;
		this.mode = ProductMode.SLICING;
		this.productType = productType;
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the satelliteId
	 */
	public String getSatelliteId() {
		return satelliteId;
	}

	/**
	 * @param satelliteId the satelliteId to set
	 */
	public void setSatelliteId(String satelliteId) {
		this.satelliteId = satelliteId;
	}

	/**
	 * @return the missionId
	 */
	public String getMissionId() {
		return missionId;
	}

	/**
	 * @param missionId the missionId to set
	 */
	public void setMissionId(String missionId) {
		this.missionId = missionId;
	}

	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the stopTime
	 */
	public Date getStopTime() {
		return stopTime;
	}

	/**
	 * @param stopTime the stopTime to set
	 */
	public void setStopTime(Date stopTime) {
		this.stopTime = stopTime;
	}

	/**
	 * @return the object
	 */
	public T getObject() {
		return object;
	}

	/**
	 * @param object the object to set
	 */
	public void setObject(T object) {
		this.object = object;
	}
	/**
	 * @return the instrumentConfigurationId
	 */
	public int getInstrumentConfigurationId() {
		return instrumentConfigurationId;
	}
	/**
	 * @param instrumentConfigurationId the instrumentConfigurationId to set
	 */
	public void setInstrumentConfigurationId(int instrumentConfigurationId) {
		this.instrumentConfigurationId = instrumentConfigurationId;
	}
	/**
	 * @return the mode
	 */
	public ProductMode getMode() {
		return mode;
	}
	/**
	 * @param mode the mode to set
	 */
	public void setMode(ProductMode mode) {
		this.mode = mode;
	}
	
	/**
	 * @return the productType
	 */
	public String getProductType() {
		return productType;
	}
	/**
	 * @param productType the productType to set
	 */
	public void setProductType(String productType) {
		this.productType = productType;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AbstractProduct [identifier=" + identifier + ", satelliteId=" + satelliteId + ", missionId=" + missionId
				+ ", startTime=" + startTime + ", stopTime=" + stopTime + ", instrumentConfigurationId="
				+ instrumentConfigurationId + ", object=" + object + ", mode=" + mode + ", productType=" + productType
				+ "]";
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + instrumentConfigurationId;
		result = prime * result + ((missionId == null) ? 0 : missionId.hashCode());
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((productType == null) ? 0 : productType.hashCode());
		result = prime * result + ((satelliteId == null) ? 0 : satelliteId.hashCode());
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
		result = prime * result + ((stopTime == null) ? 0 : stopTime.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractProduct<?> other = (AbstractProduct<?>) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		if (instrumentConfigurationId != other.instrumentConfigurationId)
			return false;
		if (missionId == null) {
			if (other.missionId != null)
				return false;
		} else if (!missionId.equals(other.missionId))
			return false;
		if (mode != other.mode)
			return false;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		if (productType == null) {
			if (other.productType != null)
				return false;
		} else if (!productType.equals(other.productType))
			return false;
		if (satelliteId == null) {
			if (other.satelliteId != null)
				return false;
		} else if (!satelliteId.equals(other.satelliteId))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		if (stopTime == null) {
			if (other.stopTime != null)
				return false;
		} else if (!stopTime.equals(other.stopTime))
			return false;
		return true;
	}

}
