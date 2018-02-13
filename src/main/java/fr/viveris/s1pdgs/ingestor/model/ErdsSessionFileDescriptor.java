package fr.viveris.s1pdgs.ingestor.model;

import java.util.Objects;

/**
 * Class describing a ERDS session file (RAW or SESSIONS)
 * 
 * @author Cyrielle Gailliard
 *
 */
public class ErdsSessionFileDescriptor extends AbstractFileDescriptor {

	/**
	 * Product type: RAW or SESSION here
	 */
	private ErdsSessionFileType productType;

	/**
	 * Session identifier
	 */
	private String sessionIdentifier;

	/**
	 * Channel number
	 */
	private int channel;

	public ErdsSessionFileDescriptor() {
		super();
	}

	/**
	 * @return the productType
	 */
	public ErdsSessionFileType getProductType() {
		return productType;
	}

	/**
	 * @param productType
	 *            the productType to set
	 */
	public void setProductType(ErdsSessionFileType productType) {
		this.productType = productType;
	}

	/**
	 * @return the sessionIdentifier
	 */
	public String getSessionIdentifier() {
		return sessionIdentifier;
	}

	/**
	 * @param sessionIdentifier
	 *            the sessionIdentifier to set
	 */
	public void setSessionIdentifier(String sessionIdentifier) {
		this.sessionIdentifier = sessionIdentifier;
	}

	/**
	 * @return the channel
	 */
	public int getChannel() {
		return channel;
	}

	/**
	 * @param channel
	 *            the channel to set
	 */
	public void setChannel(int channel) {
		this.channel = channel;
	}

	/**
	 * String formatting
	 */
	public String toString() {
		String info = String.format(
				"{ 'relativePath': %s, 'filename': %s, 'extension': %s, 'sessionIdentifier': %s, 'productName': %s, 'productType': %s, 'channel': %d, 'missionId': %s, 'satelliteId': %s, 'keyObjectStorage': %s}",
				relativePath, filename, extension, sessionIdentifier, productName, productType, channel, missionId,
				satelliteId, keyObjectStorage);
		return info;
	}

	@Override
	public boolean equals(Object o) {
		// self check
		if (this == o)
			return true;
		// null check
		if (o == null)
			return false;
		// type check and cast
		if (getClass() != o.getClass())
			return false;
		ErdsSessionFileDescriptor sessionFile = (ErdsSessionFileDescriptor) o;
		// field comparison
		return Objects.equals(keyObjectStorage, sessionFile.getKeyObjectStorage());
	}

	@Override
	public int hashCode() {
		return Objects.hash(relativePath, filename, extension, productName, productType, channel, sessionIdentifier,
				missionId, satelliteId, keyObjectStorage);
	}

}
