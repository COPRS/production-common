package fr.viveris.s1pdgs.scaler.k8s.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

public class WrapperNodeMonitor {

	private NodeDesc description;

	private int nbWrapperPods;

	private List<WrapperPodMonitor> wrapperPods;

	private Map<PodLogicalStatus, Integer> nbWrapperPdsPerLogicalStatus;

	private Map<PodLogicalStatus, Long> maxWrapperPodsRemainingExecTimePerLogicalStatus;

	public WrapperNodeMonitor() {
		this.wrapperPods = new ArrayList<>();
		this.maxWrapperPodsRemainingExecTimePerLogicalStatus = new HashMap<>();
		this.nbWrapperPods = 0;
		this.nbWrapperPdsPerLogicalStatus = new HashMap<>();
		PodLogicalStatus[] values = PodLogicalStatus.values();
		for (PodLogicalStatus v : values) {
			this.maxWrapperPodsRemainingExecTimePerLogicalStatus.put(v, 0L);
			this.nbWrapperPdsPerLogicalStatus.put(v, 0);
		}
	}

	/**
	 * @param name
	 * @param externalId
	 */
	public WrapperNodeMonitor(NodeDesc description) {
		this();
		this.description = description;
	}

	/**
	 * @return the description
	 */
	public NodeDesc getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(NodeDesc description) {
		this.description = description;
	}

	/**
	 * @return the wrapperPods
	 */
	public List<WrapperPodMonitor> getWrapperPods() {
		return wrapperPods;
	}

	/**
	 * @param wrapperPods
	 *            the wrapperPods to set
	 */
	public void addWrapperPods(List<WrapperPodMonitor> wrapperPods) {
		if (!CollectionUtils.isEmpty(wrapperPods)) {
			wrapperPods.forEach(wrapperPod -> {
				this.addWrapperPod(wrapperPod);
			});
		}
	}

	/**
	 * @param wrapperPods
	 *            the wrapperPods to set
	 */
	public void addWrapperPod(WrapperPodMonitor wrapperPod) {
		if (wrapperPod != null) {
			this.wrapperPods.add(wrapperPod);
			this.nbWrapperPods++;
			if (this.nbWrapperPdsPerLogicalStatus.containsKey(wrapperPod.getLogicalStatus())) {
				int nb = this.nbWrapperPdsPerLogicalStatus.get(wrapperPod.getLogicalStatus());
				this.nbWrapperPdsPerLogicalStatus.put(wrapperPod.getLogicalStatus(), nb + 1);
			} else {
				this.nbWrapperPdsPerLogicalStatus.put(wrapperPod.getLogicalStatus(), 1);
			}
			if (this.maxWrapperPodsRemainingExecTimePerLogicalStatus.containsKey(wrapperPod.getLogicalStatus())) {
				long time = this.maxWrapperPodsRemainingExecTimePerLogicalStatus.get(wrapperPod.getLogicalStatus());
				this.maxWrapperPodsRemainingExecTimePerLogicalStatus.put(wrapperPod.getLogicalStatus(),
						Math.max(time, wrapperPod.getRemainingExecutionTime()));
			} else {
				this.maxWrapperPodsRemainingExecTimePerLogicalStatus.put(wrapperPod.getLogicalStatus(),
						wrapperPod.getRemainingExecutionTime());
			}
		}
	}

	/**
	 * @return the nbWrapperPods
	 */
	public int getNbWrapperPods() {
		return nbWrapperPods;
	}

	/**
	 * @return the nbWrapperPdsPerLogicalStatus
	 */
	public Map<PodLogicalStatus, Integer> getNbWrapperPdsPerLogicalStatus() {
		return nbWrapperPdsPerLogicalStatus;
	}

	/**
	 * @return the maxWrapperPodsRemainingExecTimePerLogicalStatus
	 */
	public Map<PodLogicalStatus, Long> getMaxWrapperPodsRemainingExecTimePerLogicalStatus() {
		return maxWrapperPodsRemainingExecTimePerLogicalStatus;
	}

	public long getMaxRemainingExecTimeForActivesPods() {
		long max = -1;
		if (!CollectionUtils.isEmpty(this.maxWrapperPodsRemainingExecTimePerLogicalStatus)) {
			for (PodLogicalStatus status : maxWrapperPodsRemainingExecTimePerLogicalStatus.keySet()) {
				if (status != PodLogicalStatus.STOPPING) {
					max = Math.max(max, maxWrapperPodsRemainingExecTimePerLogicalStatus.get(status));
				}
			}
		}
		return max;
	}

	public List<WrapperPodMonitor> getActivesPods() {
		if (!CollectionUtils.isEmpty(this.wrapperPods)) {
			return this.wrapperPods.stream().filter(pod -> pod.getLogicalStatus() != PodLogicalStatus.STOPPING)
					.collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	public int getNbPodsPerK8SStatus(PodStatus status) {
		int nb = 0;
		if (!CollectionUtils.isEmpty(this.wrapperPods)) {
			for (WrapperPodMonitor wrapperPodMonitor : wrapperPods) {
				if (wrapperPodMonitor.getStatus() == status) {
					nb++;
				}
			}
		}
		return nb;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{description: " + description + ", nbWrapperPods: " + nbWrapperPods + ", wrapperPods: " + wrapperPods
				+ ", nbWrapperPdsPerLogicalStatus: " + nbWrapperPdsPerLogicalStatus
				+ ", maxWrapperPodsRemainingExecTimePerLogicalStatus: "
				+ maxWrapperPodsRemainingExecTimePerLogicalStatus + "}";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((maxWrapperPodsRemainingExecTimePerLogicalStatus == null) ? 0
				: maxWrapperPodsRemainingExecTimePerLogicalStatus.hashCode());
		result = prime * result
				+ ((nbWrapperPdsPerLogicalStatus == null) ? 0 : nbWrapperPdsPerLogicalStatus.hashCode());
		result = prime * result + nbWrapperPods;
		result = prime * result + ((wrapperPods == null) ? 0 : wrapperPods.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WrapperNodeMonitor other = (WrapperNodeMonitor) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (maxWrapperPodsRemainingExecTimePerLogicalStatus == null) {
			if (other.maxWrapperPodsRemainingExecTimePerLogicalStatus != null)
				return false;
		} else if (!maxWrapperPodsRemainingExecTimePerLogicalStatus
				.equals(other.maxWrapperPodsRemainingExecTimePerLogicalStatus))
			return false;
		if (nbWrapperPdsPerLogicalStatus == null) {
			if (other.nbWrapperPdsPerLogicalStatus != null)
				return false;
		} else if (!nbWrapperPdsPerLogicalStatus.equals(other.nbWrapperPdsPerLogicalStatus))
			return false;
		if (nbWrapperPods != other.nbWrapperPods)
			return false;
		if (wrapperPods == null) {
			if (other.wrapperPods != null)
				return false;
		} else if (!wrapperPods.equals(other.wrapperPods))
			return false;
		return true;
	}

}
