package esa.s1pdgs.cpoc.scaler.k8s.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.scaler.k8s.model.NodeDesc;
import esa.s1pdgs.cpoc.scaler.k8s.model.converter.K8SNodeToNodeDesc;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.client.KubernetesClient;

@Service
public class NodeService {

	private final KubernetesClient k8sClient;

	private final K8SNodeToNodeDesc nodeConverter;

	@Autowired
	public NodeService(final KubernetesClient k8sClient) {
		this.k8sClient = k8sClient;
		this.nodeConverter = new K8SNodeToNodeDesc();
	}

	/**
	 * Get nodes with labels
	 * 
	 * @param labels
	 * @return
	 */
	public List<NodeDesc> getNodesWithLabels(Map<String, String> labels) {
		List<NodeDesc> r = new ArrayList<>();
		NodeList nodes = k8sClient.nodes().withLabels(labels).list();
		if (nodes != null && !CollectionUtils.isEmpty(nodes.getItems())) {
			nodes.getItems().forEach(node -> {
				r.add(this.nodeConverter.apply(node));
			});
		}
		return r;
	}

	/**
	 * Get nodes with labels
	 * 
	 * @param labels
	 * @return
	 */
	public List<NodeDesc> getNodesWithLabel(String label, String value) {
		List<NodeDesc> r = new ArrayList<>();
		NodeList nodes = k8sClient.nodes().withLabel(label, value).list();
		if (nodes != null && !CollectionUtils.isEmpty(nodes.getItems())) {
			nodes.getItems().forEach(node -> {
				r.add(this.nodeConverter.apply(node));
			});
		}
		return r;
	}

	public void editLabelToNode(String nodeName, String label, String value) {
		k8sClient.nodes().withName(nodeName).edit().editMetadata().removeFromLabels(label).addToLabels(label, value)
				.endMetadata().done();
	}
}
