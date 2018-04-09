package fr.viveris.s1pdgs.scaler.k8s.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

public class WrapperNodeMonitor {

	private NodeDesc description;

	private int nbWrapperPods;

	private List<WrapperPodMonitor> wrapperPods;

	private long maxWrapperPodsExecutionTime;

	private Map<PodLogicalStatus, Long> maxWrapperPodsExecutionTimePerLogicalStatus;

	private Map<PodLogicalStatus, Integer> nbWrapperPdsPerLogicalStatus;

	public WrapperNodeMonitor() {
		this.wrapperPods = new ArrayList<>();
		this.maxWrapperPodsExecutionTime = 0;
		this.maxWrapperPodsExecutionTimePerLogicalStatus = new HashMap<>();
		this.nbWrapperPods = 0;
		this.nbWrapperPdsPerLogicalStatus = new HashMap<>();
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
	 * @param description the description to set
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
			if (this.maxWrapperPodsExecutionTimePerLogicalStatus.containsKey(wrapperPod.getLogicalStatus())) {
				long time = this.maxWrapperPodsExecutionTimePerLogicalStatus.get(wrapperPod.getLogicalStatus());
				this.maxWrapperPodsExecutionTimePerLogicalStatus.put(wrapperPod.getLogicalStatus(),
						Math.max(time, wrapperPod.getPassedExecutionTime()));
			} else {
				this.maxWrapperPodsExecutionTimePerLogicalStatus.put(wrapperPod.getLogicalStatus(),
						wrapperPod.getPassedExecutionTime());
			}
		}
	}

	/**
	 * @return the maxWrapperPodsExecutionTime
	 */
	public long getMaxWrapperPodsExecutionTime() {
		return maxWrapperPodsExecutionTime;
	}

	/**
	 * @return the maxWrapperPodsExecutionTimePerLogicalStatus
	 */
	public Map<PodLogicalStatus, Long> getMaxWrapperPodsExecutionTimePerLogicalStatus() {
		return maxWrapperPodsExecutionTimePerLogicalStatus;
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

}
