package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.List;

public class IpfPreparationJob extends AbstractMessage {

	private String ipfName;
	private String ipfVersion;
	private List<String> preselectednputs;

	public String getIpfName() {
		return ipfName;
	}

	public void setIpfName(String ipfName) {
		this.ipfName = ipfName;
	}

	public String getIpfVersion() {
		return ipfVersion;
	}

	public void setIpfVersion(String ipfVersion) {
		this.ipfVersion = ipfVersion;
	}

	public List<String> getPreselectednputs() {
		return preselectednputs;
	}

	public void setPreselectednputs(List<String> preselectednputs) {
		this.preselectednputs = preselectednputs;
	}

}
