package fr.viveris.s1pdgs.scaler.k8s.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import fr.viveris.s1pdgs.scaler.k8s.model.PodDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.converter.K8SPodToPodDesc;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;

@Service
public class PodService {

	private final KubernetesClient k8sClient;
	
	private final K8SPodToPodDesc podConverter;

	@Autowired
	public PodService(final KubernetesClient k8sClient) {
		this.k8sClient = k8sClient;
		this.podConverter = new K8SPodToPodDesc();
	}

	public List<PodDesc> getPodsWithLabel(String label, String value) {
		List<PodDesc> r = new ArrayList<>();
		PodList pods = k8sClient.pods().withLabel(label, value).list();
		if (pods != null && !CollectionUtils.isEmpty(pods.getItems())) {
			pods.getItems().forEach(pod -> {
				r.add(this.podConverter.apply(pod));
			});
		}
		return r;
	}
	
	public void createPod() {
		
	}

}
