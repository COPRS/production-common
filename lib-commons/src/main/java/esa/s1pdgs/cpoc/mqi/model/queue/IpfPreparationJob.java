package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.mqi.model.control.ControlAction;

public class IpfPreparationJob extends AbstractMessage {
	private String ipfName;
	private String ipfVersion;
	private List<String> preselectednputs;
	
	// Used to provide the information required for job generation from production-trigger to preparation-worker, e.g.
	// start/stop time, tasktable name, ...
	private AppDataJob appDataJob;
	
	public IpfPreparationJob() {
		super();
		setAllowedControlActions(Arrays.asList(ControlAction.RESTART));
	}

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
	
	public AppDataJob getAppDataJob() {
		return appDataJob;
	}

	public void setAppDataJob(final AppDataJob appDataJob) {
		this.appDataJob = appDataJob;
	}

	@Override
	public int hashCode() {
		return Objects.hash(appDataJob, creationDate, hostname, ipfName, ipfVersion, keyObjectStorage, preselectednputs,
				productFamily, uid);
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
				&& Objects.equals(appDataJob, other.appDataJob)				
				&& Objects.equals(hostname, other.hostname)
				&& Objects.equals(ipfName, other.ipfName) 
				&& Objects.equals(ipfVersion, other.ipfVersion)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(preselectednputs, other.preselectednputs) 
				&& Objects.equals(uid, other.uid)
				&& productFamily == other.productFamily;
	}

	@Override
	public String toString() {
		return "IpfPreparationJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", ipfName=" + ipfName + ", ipfVersion="
				+ ipfVersion + ", preselectednputs=" + preselectednputs + ", appDataJob=" + appDataJob + ", uid=" + uid +"]";
	}
}
