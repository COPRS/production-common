package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.List;
import java.util.Objects;

public class IpfPreparationJob extends AbstractMessage {
	private String ipfName;
	private String ipfVersion;
	private List<String> preselectednputs;

	public String getIpfName() {
		return ipfName;
	}

	public void setIpfName(final String ipfName) {
		this.ipfName = ipfName;
	}

	public String getIpfVersion() {
		return ipfVersion;
	}

	public void setIpfVersion(final String ipfVersion) {
		this.ipfVersion = ipfVersion;
	}

	public List<String> getPreselectednputs() {
		return preselectednputs;
	}

	public void setPreselectednputs(final List<String> preselectednputs) {
		this.preselectednputs = preselectednputs;
	}

	@Override
	public int hashCode() {
		return Objects.hash(creationDate, hostname, ipfName, ipfVersion, keyObjectStorage, preselectednputs,
				productFamily);
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
		final IpfPreparationJob other = (IpfPreparationJob) obj;
		return Objects.equals(creationDate, other.creationDate) 
				&& Objects.equals(hostname, other.hostname)
				&& Objects.equals(ipfName, other.ipfName) 
				&& Objects.equals(ipfVersion, other.ipfVersion)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(preselectednputs, other.preselectednputs) 
				&& productFamily == other.productFamily;
	}

	@Override
	public String toString() {
		return "IpfPreparationJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", ipfName=" + ipfName + ", ipfVersion="
				+ ipfVersion + ", preselectednputs=" + preselectednputs + "]";
	}
}
