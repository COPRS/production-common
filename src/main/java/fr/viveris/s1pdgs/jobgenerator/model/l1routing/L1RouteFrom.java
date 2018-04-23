package fr.viveris.s1pdgs.jobgenerator.model.l1routing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "l1_from")
@XmlAccessorType(XmlAccessType.NONE)
public class L1RouteFrom {
	
	@XmlElement(name = "acquisition")
	private String acquisition;
	
	@XmlElement(name = "satellite_id")
	private String satelliteId;

	public L1RouteFrom() {
		
	}

	public L1RouteFrom(String acquisition, String satelliteId) {
		this();
		this.acquisition = acquisition;
		this.satelliteId = satelliteId;
	}

	/**
	 * @return the acquisition
	 */
	public String getAcquisition() {
		return acquisition;
	}

	/**
	 * @param acquisition the acquisition to set
	 */
	public void setAcquisition(String acquisition) {
		this.acquisition = acquisition;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "L1RouteFrom [acquisition=" + acquisition + ", satelliteId=" + satelliteId + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((acquisition == null) ? 0 : acquisition.hashCode());
		result = prime * result + ((satelliteId == null) ? 0 : satelliteId.hashCode());
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
		L1RouteFrom other = (L1RouteFrom) obj;
		if (acquisition == null) {
			if (other.acquisition != null)
				return false;
		} else if (!acquisition.equals(other.acquisition))
			return false;
		if (satelliteId == null) {
			if (other.satelliteId != null)
				return false;
		} else if (!satelliteId.equals(other.satelliteId))
			return false;
		return true;
	}

}
