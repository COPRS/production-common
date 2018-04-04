package fr.viveris.s1pdgs.scaler.k8s.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;

@Service
public class PodService {

	private final KubernetesClient k8sClient;

	@Autowired
	public PodService(final KubernetesClient k8sClient) {
		this.k8sClient = k8sClient;
	}

	public PodList getPodsWithLabel(String label, String value) {
		PodList pods = k8sClient.pods().withLabel(label, value).list();
		return pods;
	}

}
