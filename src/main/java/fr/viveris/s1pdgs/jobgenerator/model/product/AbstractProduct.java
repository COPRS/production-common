package fr.viveris.s1pdgs.jobgenerator.model.product;

import java.util.Date;
import java.util.Objects;

import fr.viveris.s1pdgs.jobgenerator.model.ProductMode;

/**
 * Class use to describe the product for which a job shall be generated This
 * class is generic:
 * <li>for L0, T is EdrsSession</li>
 * <li>for L1, T is L0Slice</li>
 * 
 * @author Cyrielle Gailliard
 *
 * @param <T>
 */
public abstract class AbstractProduct<T> {

	/**
	 * Product identifier: often the product name
	 */
	private final String identifier;

	/**
	 * Satellite identifier: A or B
	 */
	private final String satelliteId;

	/**
	 * Mission identifier: S1
	 */
	private final String missionId;

	/**
	 * Start time of the product (in metadata)
	 */
	private final Date startTime;

	/**
	 * Stop time of the product (in metadata)
	 */
	private final Date stopTime;

	/**
	 * Instrument configuration id (in metadata). -1 if not exist
	 */
	private int insConfId;

	/**
	 * The object
	 */
	private final T object;

	/**
	 * The mode
	 */
	private ProductMode mode;

	/**
	 * Its type (in metadata)
	 */
	private String productType;

	/**
	 * @param identifier
	 * @param satelliteId
	 * @param missionId
	 * @param startTime
	 * @param stopTime
	 * @param object
	 */
	public AbstractProduct(final String identifier, final String satelliteId, final String missionId,
			final Date startTime, final Date stopTime, final T object, final String productType) {
		super();
		this.identifier = identifier;
		this.satelliteId = satelliteId;
		this.missionId = missionId;
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.object = object;
		this.insConfId = -1;
		this.mode = ProductMode.ALWAYS;
		this.productType = productType;
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @return the satelliteId
	 */
	public String getSatelliteId() {
		return satelliteId;
	}

	/**
	 * @return the missionId
	 */
	public String getMissionId() {
		return missionId;
	}

	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @return the stopTime
	 */
	public Date getStopTime() {
		return stopTime;
	}

	/**
	 * @return the object
	 */
	public T getObject() {
		return object;
	}

	/**
	 * @return the insConfId
	 */
	public int getInsConfId() {
		return insConfId;
	}

	/**
	 * @param insConfId
	 *            the insConfId to set
	 */
	public void setInsConfId(final int insConfId) {
		this.insConfId = insConfId;
	}

	/**
	 * @return the mode
	 */
	public ProductMode getMode() {
		return mode;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(final ProductMode mode) {
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

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return String.format(
				"{identifier: %s, satelliteId: %s, missionId: %s, startTime: %s, stopTime: %s, insConfId: %s, object: %s, mode: %s, productType: %s}",
				identifier, satelliteId, missionId, startTime, stopTime, insConfId, object, mode, productType);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(identifier, satelliteId, missionId, startTime, stopTime, insConfId, object, mode,
				productType);
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
			AbstractProduct<?> other = (AbstractProduct<?>) obj;
			ret = Objects.equals(identifier, other.identifier) && Objects.equals(satelliteId, other.satelliteId)
					&& Objects.equals(missionId, other.missionId) && Objects.equals(startTime, other.startTime)
					&& Objects.equals(stopTime, other.stopTime) && insConfId == other.insConfId
					&& Objects.equals(object, other.object) && Objects.equals(mode, other.mode)
					&& Objects.equals(productType, other.productType);
		}
		return ret;
	}

}
