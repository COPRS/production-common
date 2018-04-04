package fr.viveris.s1pdgs.scaler.monitoring.k8s;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.fabric8.kubernetes.api.model.NodeAddress;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.PodList;

@Service
public class K8SAdministration {

	private final K8SProperties properties;

	private final NodeService nodeService;

	private final PodService podService;

	@Autowired
	public K8SAdministration(final K8SProperties properties, final NodeService nodeService,
			final PodService podService) {
		this.properties = properties;
		this.nodeService = nodeService;
		this.podService = podService;
	}

	/**
	 * Get the names of the K8S nodes which shall be deleted.<br/>
	 * <ul>
	 * A node shall be deleted if:
	 * <li>it is configured to host wrappers</li>
	 * <li>it is set to be deleted (wrapper config set to unused)</li>
	 * <li>He has no running wrapper pods</li>
	 * </ul>
	 * 
	 * @return
	 */
	public List<String> getWrapperNodesToDelete() {
		List<String> nodeNames = new ArrayList<>();

		// Build labels
		Map<String, String> labels = new HashMap<>();
		labels.put(properties.getLabelWrapperConfig().getLabel(), properties.getLabelWrapperConfig().getValue());
		labels.put(properties.getLabelWrapperStateUnused().getLabel(),
				properties.getLabelWrapperStateUnused().getValue());
		// Get nodes labellised unused
		NodeList unusedNodes = this.nodeService.getNodesWithLabels(labels);
		if (unusedNodes != null && !CollectionUtils.isEmpty(unusedNodes.getItems())) {
			// Get wrapper pods
			PodList l1WrapperPods = this.podService.getPodsWithLabel(properties.getLabelWrapperApp().getLabel(),
					properties.getLabelWrapperApp().getValue());
			// Extract the list of used host IPs
			Map<String, Integer> hostIps = new HashMap<>();
			if (l1WrapperPods != null && !CollectionUtils.isEmpty(l1WrapperPods.getItems())) {
				l1WrapperPods.getItems().forEach(pod -> {
					if (hostIps.containsKey(pod.getStatus().getHostIP())) {
						Integer i = hostIps.get(pod.getStatus().getHostIP());
						hostIps.put(pod.getStatus().getHostIP(), i + 1);
					} else {
						hostIps.put(pod.getStatus().getHostIP(), 0);
					}
				});
			}
			// Build the return
			unusedNodes.getItems().forEach(node -> {
				boolean found = false;
				if (!CollectionUtils.isEmpty(node.getStatus().getAddresses())) {
					for (int i = 0; i < node.getStatus().getAddresses().size(); i++) {
						NodeAddress nodeAddress = node.getStatus().getAddresses().get(i);
						if (hostIps.containsKey(nodeAddress.getAddress())) {
							found = true;
						}
					}
				}
				if (!found) {
					nodeNames.add(node.getMetadata().getName());
				}
			});
		}

		return nodeNames;
	}
}
