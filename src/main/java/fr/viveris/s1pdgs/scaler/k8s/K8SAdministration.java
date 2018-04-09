package fr.viveris.s1pdgs.scaler.k8s;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import fr.viveris.s1pdgs.scaler.k8s.model.NodeDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.PodDesc;
import fr.viveris.s1pdgs.scaler.k8s.services.NodeService;
import fr.viveris.s1pdgs.scaler.k8s.services.PodService;

@Service
public class K8SAdministration {

	private final WrapperProperties wrapperProperties;

	private final NodeService nodeService;

	private final PodService podService;

	@Autowired
	public K8SAdministration(final WrapperProperties wrapperProperties, final NodeService nodeService,
			final PodService podService) {
		this.wrapperProperties = wrapperProperties;
		this.nodeService = nodeService;
		this.podService = podService;
	}

	/**
	 * Get the external identifiers of the K8S nodes which shall be deleted.<br/>
	 * <ul>
	 * A node shall be deleted if:
	 * <li>it is configured to host wrappers</li>
	 * <li>it is set to be deleted (wrapper config set to unused)</li>
	 * <li>He has no running wrapper pods</li>
	 * </ul>
	 * 
	 * @return
	 */
	public List<String> getExternalIdsOfWrapperNodesToDelete() {
		List<String> externalIds = new ArrayList<>();

		// Build labels
		Map<String, String> labels = new HashMap<>();
		labels.put(wrapperProperties.getLabelWrapperConfig().getLabel(),
				wrapperProperties.getLabelWrapperConfig().getValue());
		labels.put(wrapperProperties.getLabelWrapperStateUnused().getLabel(),
				wrapperProperties.getLabelWrapperStateUnused().getValue());
		// Get nodes labellised unused
		List<NodeDesc> unusedNodes = this.nodeService.getNodesWithLabels(labels);
		if (!CollectionUtils.isEmpty(unusedNodes)) {
			// Get wrapper pods
			List<PodDesc> l1WrapperPods = this.podService.getPodsWithLabel(
					wrapperProperties.getLabelWrapperApp().getLabel(),
					wrapperProperties.getLabelWrapperApp().getValue());
			// Extract the list of used host IPs
			Map<String, Integer> usedNodeNames = new HashMap<>();
			if (!CollectionUtils.isEmpty(l1WrapperPods)) {
				l1WrapperPods.forEach(podDesc -> {
					if (usedNodeNames.containsKey(podDesc.getNodeName())) {
						Integer i = usedNodeNames.get(podDesc.getNodeName());
						usedNodeNames.put(podDesc.getNodeName(), i + 1);
					} else {
						usedNodeNames.put(podDesc.getNodeName(), 0);
					}
				});
			}
			// Build the return
			unusedNodes.forEach(nodeDesc -> {
				if (!usedNodeNames.containsKey(nodeDesc.getName())) {
					externalIds.add(nodeDesc.getExternalId());
				}
			});
		}

		return externalIds;
	}
	
	public void setWrapperNodeUsable(String nodeName) {
		this.nodeService.editLabelToNode(nodeName, wrapperProperties.getLabelWrapperStateUsed().getLabel(), wrapperProperties.getLabelWrapperStateUsed().getValue());
	}
	
	public void setWrapperNodeUnusable(String nodeName) {
		this.nodeService.editLabelToNode(nodeName, wrapperProperties.getLabelWrapperStateUnused().getLabel(), wrapperProperties.getLabelWrapperStateUnused().getValue());
	}
	
	public void launchWrapperPodsPool() {
		
	}
}
