package fr.viveris.s1pdgs.scaler.k8s.services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import fr.viveris.s1pdgs.scaler.k8s.model.PodDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.converter.K8SPodToPodDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.PodResourceException;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.UnknownKindExecption;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.UnknownVolumeNameException;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.DoneablePersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.Resource;

@Service
public class PodService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PodService.class);
	
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
	
	public void createPod() throws FileNotFoundException, PodResourceException, UnknownKindExecption, UnknownVolumeNameException {
		String namespace = "default";
		String randomUUID = UUID.randomUUID().toString();
		String volumeName = null;
		try {
			List<HasMetadata> resources = this.k8sClient.load(new FileInputStream("config/template_l1_wrapper_pod.yml")).get();
			if (resources.isEmpty()) {
		        throw new PodResourceException("No resources loaded from file: " + "config/template_l1_wrapper_pod.yml");
			}
			for (HasMetadata resource : resources) {
				switch(resource.getKind()) {
				case "PersistentVolumeClaim":
					PersistentVolumeClaim volume = (PersistentVolumeClaim) resource;
					volumeName = volume.getMetadata().getName();
					if(volumeName!=null) {
						volume.getMetadata().setName(volumeName+"-"+randomUUID);
					}
					else {
						throw new UnknownVolumeNameException("Volume Name is unknown : " + volumeName);
					}
					NonNamespaceOperation<PersistentVolumeClaim, PersistentVolumeClaimList, DoneablePersistentVolumeClaim, 
							Resource<PersistentVolumeClaim, DoneablePersistentVolumeClaim>> volumes = 
							this.k8sClient.persistentVolumeClaims().inNamespace(namespace);
					PersistentVolumeClaim resultVolume = volumes.create(volume);
					LOGGER.info("[MONITOR] [Step 4] Volume created : {}", resultVolume.getMetadata().getName());
					break;
				case "Pod":
					Pod pod = (Pod) resource;
					pod.getMetadata().setName(pod.getMetadata().getName()+"-"+randomUUID);
					for(Volume volumeConf : pod.getSpec().getVolumes()) {
						if(volumeConf.getPersistentVolumeClaim().getClaimName().equals(volumeName)) {
							if(volumeName!=null) {
								volumeConf.getPersistentVolumeClaim().setClaimName(volumeName+"-"+randomUUID);
							}
							else {
								throw new UnknownVolumeNameException("Volume Name is unknown : " + volumeName);
							}
						}
					}
					for(Container containerConf : pod.getSpec().getContainers()) {
						if(containerConf.getName().equals("l1-wrapper")) {
							for(VolumeMount volumeConf : containerConf.getVolumeMounts()) {
								if(volumeConf.getName().equals(volumeName)) {
									if(volumeName!=null) {
										volumeConf.setName(volumeName+"-"+randomUUID);
									}
									else {
										throw new UnknownVolumeNameException("Volume Name is unknown : " + volumeName);
									}
								}
							}
						}
					}
					NonNamespaceOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>> pods = 
							this.k8sClient.pods().inNamespace(namespace);
			        Pod resultPod = pods.create(pod);
			        LOGGER.info("[MONITOR] [Step 4] Pod created : {}", resultPod.getMetadata().getName());
					break;
				default:
					throw new UnknownKindExecption("Unknown kind : " + resource.getKind());
				}
			}
		} catch (FileNotFoundException e) {
			throw e;
		}
	}

}
