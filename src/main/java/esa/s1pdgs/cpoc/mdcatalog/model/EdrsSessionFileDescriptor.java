package esa.s1pdgs.cpoc.mdcatalog.model;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;

/**
 * Class describing a ERDS session file (RAW or SESSIONS)
 * 
 * @author Cyrielle Gailliard
 *
 */
public class EdrsSessionFileDescriptor extends AbstractFileDescriptor {

	/**
	 * Product type: RAW or SESSION here
	 */
	private EdrsSessionFileType productType;

	/**
	 * Session identifier
	 */
	private String sessionIdentifier;

	/**
	 * Channel number
	 */
	private int channel;

	public EdrsSessionFileDescriptor() {
		super();
	}

	/**
	 * @return the productType
	 */
	public EdrsSessionFileType getProductType() {
		return productType;
	}

	/**
	 * @param productType
	 *            the productType to set
	 */
	public void setProductType(EdrsSessionFileType productType) {
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

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EdrsSessionFileDescriptor other = (EdrsSessionFileDescriptor) obj;
		if (channel != other.channel)
			return false;
		if (productType != other.productType)
			return false;
		if (sessionIdentifier == null) {
			if (other.sessionIdentifier != null)
				return false;
		} else if (!sessionIdentifier.equals(other.sessionIdentifier))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + channel;
		result = prime * result + ((productType == null) ? 0 : productType.hashCode());
		result = prime * result + ((sessionIdentifier == null) ? 0 : sessionIdentifier.hashCode());
		return result;
	}

}
