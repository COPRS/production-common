package esa.s1pdgs.cpoc.metadata.model;

/**
 * 
 * @author Cyrielle Gailliard
 *
 */
public class EdrsSessionMetadata extends AbstractMetadata {

	/**
	 * @param productName
	 * @param productType
	 * @param keyObjectStorage
	 * @param validityStart
	 * @param validityStop
	 * @param missionId
     * @param satelliteId
     * @param stationCode
	 */
	public EdrsSessionMetadata(final String productName, final String productType, final String keyObjectStorage,
			final String validityStart, final String validityStop, final String missionId, final String satelliteId, final String stationCode) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop, missionId, satelliteId, stationCode);
	}
	
	public EdrsSessionMetadata() {
		super();
	}
	
	@Override
	public String toString() {
		return String.format("{%s}", super.toAbstractString());
	}
}
