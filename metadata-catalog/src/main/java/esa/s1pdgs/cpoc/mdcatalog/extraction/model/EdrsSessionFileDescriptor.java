package esa.s1pdgs.cpoc.mdcatalog.extraction.model;

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
	private EdrsSessionFileType edrsSessionFileType;

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

	public EdrsSessionFileType getEdrsSessionFileType() {
		return edrsSessionFileType;
	}

	public void setEdrsSessionFileType(EdrsSessionFileType productType) {
		this.edrsSessionFileType = productType;
	}

	public String getSessionIdentifier() {
		return sessionIdentifier;
	}

	public void setSessionIdentifier(String sessionIdentifier) {
		this.sessionIdentifier = sessionIdentifier;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	/**
	 * String formatting
	 */
	public String toString() {
		String info = String.format(
				"{ 'relativePath': %s, 'filename': %s, 'extension': %s, 'sessionIdentifier': %s, 'productName': %s, 'productType': %s, 'channel': %d, 'missionId': %s, 'satelliteId': %s, 'keyObjectStorage': %s}",
				relativePath, filename, extension, sessionIdentifier, productName, edrsSessionFileType, channel, missionId,
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
		if (!(obj instanceof EdrsSessionFileDescriptor))
			return false;
		if (!super.equals(obj))
			return false;
		EdrsSessionFileDescriptor other = (EdrsSessionFileDescriptor) obj;
		if (channel != other.channel)
			return false;
		if (edrsSessionFileType != other.edrsSessionFileType)
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
		result = prime * result + ((edrsSessionFileType == null) ? 0 : edrsSessionFileType.hashCode());
		result = prime * result + ((sessionIdentifier == null) ? 0 : sessionIdentifier.hashCode());
		return result;
	}

}
