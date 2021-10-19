package esa.s1pdgs.cpoc.mdc.worker.extraction.model;

import java.util.Objects;

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
	private String sessionIdentifier;
	private String stationCode;
	private int channel;
	
	public String getStationCode() {
		return stationCode;
	}

	public void setStationCode(final String stationCode) {
		this.stationCode = stationCode;
	}

	public EdrsSessionFileType getEdrsSessionFileType() {
		return edrsSessionFileType;
	}

	public void setEdrsSessionFileType(final EdrsSessionFileType productType) {
		this.edrsSessionFileType = productType;
	}

	public String getSessionIdentifier() {
		return sessionIdentifier;
	}

	public void setSessionIdentifier(final String sessionIdentifier) {
		this.sessionIdentifier = sessionIdentifier;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(final int channel) {
		this.channel = channel;
	}

	@Override
	public int hashCode() {
		return Objects.hash(channel, edrsSessionFileType, extension, filename, keyObjectStorage, missionId, mode,
				productClass, productFamily, productName, productType, relativePath, satelliteId, sessionIdentifier,
				stationCode);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final EdrsSessionFileDescriptor other = (EdrsSessionFileDescriptor) obj;
		return channel == other.channel && edrsSessionFileType == other.edrsSessionFileType
				&& extension == other.extension && Objects.equals(filename, other.filename)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(missionId, other.missionId) && Objects.equals(mode, other.mode)
				&& Objects.equals(productClass, other.productClass) && productFamily == other.productFamily
				&& Objects.equals(productName, other.productName) && Objects.equals(productType, other.productType)
				&& Objects.equals(relativePath, other.relativePath) && Objects.equals(satelliteId, other.satelliteId)
				&& Objects.equals(sessionIdentifier, other.sessionIdentifier)
				&& Objects.equals(stationCode, other.stationCode);
	}

	@Override
	public String toString() {
		return "EdrsSessionFileDescriptor [productType=" + productType + ", productClass=" + productClass
				+ ", relativePath=" + relativePath + ", filename=" + filename + ", extension=" + extension
				+ ", productName=" + productName + ", missionId=" + missionId + ", satelliteId=" + satelliteId
				+ ", keyObjectStorage=" + keyObjectStorage + ", productFamily=" + productFamily + ", mode=" + mode
				+ ", edrsSessionFileType=" + edrsSessionFileType + ", sessionIdentifier=" + sessionIdentifier
				+ ", stationCode=" + stationCode + ", channel=" + channel + "]";
	}
}
