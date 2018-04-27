package fr.viveris.s1pdgs.scaler.k8s.services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import fr.viveris.s1pdgs.scaler.k8s.model.PodDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.converter.K8SPodToPodDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.PodResourceException;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.UnknownKindExecption;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.UnknownVolumeNameException;
import io.fabric8.kubernetes.api.model.DoneablePersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.Resource;

@Service
public class PodService {

	private static final Logger LOGGER = LogManager.getLogger(PodService.class);

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

	public List<PodDesc> getPodsWithLabelAndStatusPhase(String label, String value, String phase) {
		List<PodDesc> r = new ArrayList<>();
		PodList pods = k8sClient.pods().withLabel(label, value).withField("status.phase", phase).list();
		if (pods != null && !CollectionUtils.isEmpty(pods.getItems())) {
			pods.getItems().forEach(pod -> {
				r.add(this.podConverter.apply(pod));
			});
		}
		return r;
	}

	private List<HasMetadata> loadRessourcesFromFile(String fileName, String suffixe)
			throws PodResourceException, UnknownKindExecption, FileNotFoundException {

		List<HasMetadata> resources = null;
		try {
			resources = this.k8sClient.load(new FileInputStream(fileName)).get();
		} catch (FileNotFoundException e) {
			throw e;
		}

		if (CollectionUtils.isEmpty(resources)) {
			throw new PodResourceException("No resources loaded from file: " + "config/template_l1_wrapper_pod.yml");
		}

		for (HasMetadata resource : resources) {
			switch (resource.getKind()) {
			case "PersistentVolumeClaim":
				PersistentVolumeClaim volume = (PersistentVolumeClaim) resource;
				volume.getMetadata().setName(volume.getMetadata().getName() + suffixe);
				break;
			case "Pod":
				Pod pod = (Pod) resource;
				pod.getMetadata().setName(pod.getMetadata().getName() + suffixe);
				if (!CollectionUtils.isEmpty(pod.getSpec().getVolumes())) {
					Volume v = pod.getSpec().getVolumes().get(0);
					v.getPersistentVolumeClaim().setClaimName(v.getPersistentVolumeClaim().getClaimName() + suffixe);
				}
				break;
			default:
				throw new UnknownKindExecption("Unknown kind : " + resource.getKind());
			}
		}

		return resources;
	}

	public void createPod()
			throws FileNotFoundException, PodResourceException, UnknownKindExecption, UnknownVolumeNameException {
		String namespace = "default";

		// Load resources and update names
		String suffixe = "-" + System.currentTimeMillis();
		List<HasMetadata> resources = this.loadRessourcesFromFile("config/template_l1_wrapper_pod.yml", suffixe);

		// create resources
		for (HasMetadata resource : resources) {
			switch (resource.getKind()) {
			case "PersistentVolumeClaim":
				PersistentVolumeClaim volume = (PersistentVolumeClaim) resource;
				NonNamespaceOperation<PersistentVolumeClaim, PersistentVolumeClaimList, DoneablePersistentVolumeClaim, Resource<PersistentVolumeClaim, DoneablePersistentVolumeClaim>> volumes = this.k8sClient
						.persistentVolumeClaims().inNamespace(namespace);
				PersistentVolumeClaim resultVolume = volumes.create(volume);
				LOGGER.info("[MONITOR] [Step 4] Volume created : {}", resultVolume.getMetadata().getName());
				break;
			case "Pod":
				Pod pod = (Pod) resource;
				NonNamespaceOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>> pods = this.k8sClient
						.pods().inNamespace(namespace);
				Pod resultPod = pods.create(pod);
				LOGGER.info("[MONITOR] [Step 4] Pod created : {}", resultPod.getMetadata().getName());
				break;
			default:
				throw new UnknownKindExecption("Unknown kind : " + resource.getKind());
			}
		}
	}

	public Boolean deletePod(String suffixe) throws FileNotFoundException, PodResourceException, UnknownKindExecption {
		String namespace = "default";
		Boolean resultVolume = false;
		Boolean resultPod = false;

		// Load resources and update names
		List<HasMetadata> resources = this.loadRessourcesFromFile("config/template_l1_wrapper_pod.yml", suffixe);

		// create resources
		for (HasMetadata resource : resources) {
			switch (resource.getKind()) {
			case "PersistentVolumeClaim":
				PersistentVolumeClaim volume = (PersistentVolumeClaim) resource;
				NonNamespaceOperation<PersistentVolumeClaim, PersistentVolumeClaimList, DoneablePersistentVolumeClaim, Resource<PersistentVolumeClaim, DoneablePersistentVolumeClaim>> volumes = this.k8sClient
						.persistentVolumeClaims().inNamespace(namespace);
				resultVolume = volumes.delete(volume);
				LOGGER.info("[MONITOR] [Step 4] Volume deleted : {} {}", volume.getMetadata().getName(), resultVolume);
				break;
			case "Pod":
				Pod pod = (Pod) resource;
				NonNamespaceOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>> pods = this.k8sClient
						.pods().inNamespace(namespace);
				resultPod = pods.delete(pod);
				LOGGER.info("[MONITOR] [Step 4] Pod deleted : {} {}", pod.getMetadata().getName(), resultPod);
				break;
			default:
				throw new UnknownKindExecption("Unknown kind : " + resource.getKind());
			}
		}
		
		return resultPod && resultVolume;
	}

}
