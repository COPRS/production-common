package fr.viveris.s1pdgs.scaler.k8s;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import fr.viveris.s1pdgs.scaler.k8s.model.NodeDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.PodDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.WrapperDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.WrapperNodeMonitor;
import fr.viveris.s1pdgs.scaler.k8s.model.WrapperPodMonitor;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.WrapperException;
import fr.viveris.s1pdgs.scaler.k8s.services.NodeService;
import fr.viveris.s1pdgs.scaler.k8s.services.PodService;
import fr.viveris.s1pdgs.scaler.k8s.services.WrapperService;

@Service
public class K8SMonitoring {

	private final WrapperProperties wrapperProperties;

	private final NodeService nodeService;

	private final PodService podService;

	private final WrapperService wrapperService;

	public K8SMonitoring(final WrapperProperties wrapperProperties, final NodeService nodeService,
			final PodService podService, final WrapperService wrapperService) {
		this.wrapperProperties = wrapperProperties;
		this.nodeService = nodeService;
		this.podService = podService;
		this.wrapperService = wrapperService;
	}

	public List<WrapperNodeMonitor> monitorL1Wrappers() throws WrapperException {
		List<WrapperNodeMonitor> monitors = new ArrayList<>();

		// Retrieve nodes dedicated to L1
		List<NodeDesc> nodes = this.nodeService.getNodesWithLabel(wrapperProperties.getLabelWrapperConfig().getLabel(),
				wrapperProperties.getLabelWrapperConfig().getValue());

		// Retrieve pods dedicated to L1
		List<PodDesc> pods = this.podService.getPodsWithLabel(wrapperProperties.getLabelWrapperApp().getLabel(),
				wrapperProperties.getLabelWrapperApp().getValue());

		// Reorganize pods per nodes
		Map<String, List<PodDesc>> podsPerNodes = new HashMap<>();
		if (!CollectionUtils.isEmpty(pods)) {
			pods.forEach(pod -> {
				if (!podsPerNodes.containsKey(pod.getNodeName())) {
					podsPerNodes.put(pod.getName(), new ArrayList<>());
				}
				podsPerNodes.get(pod.getNodeName()).add(pod);
			});
		}

		// build monitor
		if (!CollectionUtils.isEmpty(nodes)) {
			for (NodeDesc node : nodes) {
				WrapperNodeMonitor nodeMonitor = new WrapperNodeMonitor(node);
				if (podsPerNodes.containsKey(node.getName())) {
					for (PodDesc pod : podsPerNodes.get(node.getName())) {
						WrapperPodMonitor podMonitor = new WrapperPodMonitor(pod.getName());
						WrapperDesc wrapper = this.wrapperService.getWrapperStatus(pod.getName());
						podMonitor.setLogicalStatus(wrapper.getStatus());
						podMonitor.setPassedExecutionTime(wrapper.getTimeSinceLastChange());
						podMonitor.setRemainingExecutionTime(wrapperProperties.getExecutionTime().getAverageS()
								- wrapper.getTimeSinceLastChange());
						// TODO
						podMonitor.setStatus(null);
						nodeMonitor.addWrapperPod(podMonitor);
					}
				}
				monitors.add(nodeMonitor);
			}
		}

		return monitors;
	}
}
