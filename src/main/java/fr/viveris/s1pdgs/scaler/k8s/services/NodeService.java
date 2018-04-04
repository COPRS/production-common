package fr.viveris.s1pdgs.scaler.k8s.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.client.KubernetesClient;

public class NodeService {

	private final KubernetesClient k8sClient;

	@Autowired
	public NodeService(final KubernetesClient k8sClient) {
		this.k8sClient = k8sClient;
	}

	public NodeList getNodesWithLabels(Map<String, String> labels) {
		return k8sClient.nodes().withLabels(labels).list();
	}

	public void getWorkersWithWrapperStateLabel() {
		NodeList nodes = k8sClient.nodes().withLabel("").list();
		if (!CollectionUtils.isEmpty(nodes.getItems())) {
			nodes.getItems().forEach(node -> {
				
			});
		}
	}

	public void getWorkersWithWrapperStateLabel(String labelValue) {
		NodeList nodes = k8sClient.nodes().withLabel("", labelValue).list();
		if (!CollectionUtils.isEmpty(nodes.getItems())) {
			nodes.getItems().forEach(node -> {

			});
		}
	}

	public void removeLabelFromNode(String nodeName, String label) {
		k8sClient.nodes().withName(nodeName).edit().editMetadata().removeFromLabels(label).endMetadata().done();
	}

	public void addWrapperStateLabelToNode(String nodeName, String label, String value) {
		k8sClient.nodes().withName(nodeName).edit().editMetadata().addToLabels("", value).endMetadata().done();
	}
	
	
}
