package fr.viveris.s1pdgs.scaler.monitoring.k8s.model;

import java.util.Date;

public class Wrapper {

	private String podName;
	
	private String hostName;
	
	private WrapperStatus wrapperStatus;
	
	private Date podLaunchDate;
	
	public Wrapper() {
	}
	
	public Wrapper(String podName, String hostName, WrapperStatus wrapperStatus, Date podLaunchDate) {
		this.podName = podName;
		this.hostName = hostName;
		this.wrapperStatus = wrapperStatus;
		this.podLaunchDate = podLaunchDate;
	}

	/**
	 * @return the podName
	 */
	public String getPodName() {
		return podName;
	}

	/**
	 * @param podName the podName to set
	 */
	public void setPodName(String podName) {
		this.podName = podName;
	}

	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * @param hostName the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * @return the wrapperStatus
	 */
	public WrapperStatus getWrapperStatus() {
		return wrapperStatus;
	}

	/**
	 * @param wrapperStatus the wrapperStatus to set
	 */
	public void setWrapperStatus(WrapperStatus wrapperStatus) {
		this.wrapperStatus = wrapperStatus;
	}

	/**
	 * @return the podLaunchDate
	 */
	public Date getPodLaunchDate() {
		return podLaunchDate;
	}

	/**
	 * @param podLaunchDate the podLaunchDate to set
	 */
	public void setPodLaunchDate(Date podLaunchDate) {
		this.podLaunchDate = podLaunchDate;
	}

}
