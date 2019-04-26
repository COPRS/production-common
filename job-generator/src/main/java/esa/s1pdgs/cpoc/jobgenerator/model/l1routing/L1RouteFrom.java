package esa.s1pdgs.cpoc.jobgenerator.model.l1routing;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class used the from route<br/>
 * Used for mapping the file routing.xml in java objects
 * 
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "l1_from")
@XmlAccessorType(XmlAccessType.NONE)
public class L1RouteFrom {

	/**
	 * Acquisition (IW, EW, SM, EM)
	 */
	@XmlElement(name = "acquisition")
	private String acquisition;

	/**
	 * Satellite identifier (S1A, S1B)
	 */
	@XmlElement(name = "satellite_id")
	private String satelliteId;

	/**
	 * Default constructor
	 */
	public L1RouteFrom() {
		super();
	}

	/**
	 * Constructor using field
	 * 
	 * @param acquisition
	 * @param satelliteId
	 */
	public L1RouteFrom(final String acquisition, final String satelliteId) {
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
	 * @param acquisition
	 *            the acquisition to set
	 */
	public void setAcquisition(final String acquisition) {
		this.acquisition = acquisition;
	}

	/**
	 * @return the satelliteId
	 */
	public String getSatelliteId() {
		return satelliteId;
	}

	/**
	 * @param satelliteId
	 *            the satelliteId to set
	 */
	public void setSatelliteId(final String satelliteId) {
		this.satelliteId = satelliteId;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{acquisition: %s, satelliteId: %s}", acquisition, satelliteId);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(acquisition, satelliteId);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			L1RouteFrom other = (L1RouteFrom) obj;
			ret = Objects.equals(acquisition, other.acquisition) && Objects.equals(satelliteId, other.satelliteId);
		}
		return ret;
	}

}
