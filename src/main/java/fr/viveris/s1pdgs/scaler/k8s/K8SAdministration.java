package fr.viveris.s1pdgs.scaler.k8s;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import fr.viveris.s1pdgs.scaler.k8s.model.PodDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.PodStatus;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.PodResourceException;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.UnknownKindExecption;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.UnknownVolumeNameException;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.WrapperException;
import fr.viveris.s1pdgs.scaler.k8s.services.NodeService;
import fr.viveris.s1pdgs.scaler.k8s.services.PodService;
import fr.viveris.s1pdgs.scaler.k8s.services.WrapperService;

@Service
public class K8SAdministration {

	private final WrapperProperties wrapperProperties;

	private final NodeService nodeService;

	private final PodService podService;

	private final WrapperService wrapperService;

	@Autowired
	public K8SAdministration(final WrapperProperties wrapperProperties, final NodeService nodeService,
			final PodService podService, final WrapperService wrapperService) {
		this.wrapperProperties = wrapperProperties;
		this.nodeService = nodeService;
		this.podService = podService;
		this.wrapperService = wrapperService;
	}

	public void setWrapperNodeUsable(String nodeName) {
		this.nodeService.editLabelToNode(nodeName, wrapperProperties.getLabelWrapperStateUsed().getLabel(),
				wrapperProperties.getLabelWrapperStateUsed().getValue());
	}

	public void setWrapperNodeUnusable(String nodeName) {
		this.nodeService.editLabelToNode(nodeName, wrapperProperties.getLabelWrapperStateUnused().getLabel(),
				wrapperProperties.getLabelWrapperStateUnused().getValue());
	}

	public void launchWrapperPodsPool()
			throws FileNotFoundException, PodResourceException, UnknownKindExecption, UnknownVolumeNameException {
		for (int i = 0; i < this.wrapperProperties.getNbPoolingPods(); i++) {
			this.podService.createPod();
		}
	}

	public void stopWrapperPods(List<String> wrapperIps) throws WrapperException {
		if (!CollectionUtils.isEmpty(wrapperIps)) {
			for (String w : wrapperIps) {
				this.wrapperService.stopWrapper(w);
			}
		}
	}

	public List<String> deleteTerminatedWrapperPods() throws FileNotFoundException, PodResourceException, UnknownKindExecption {
		List<String> deletedPodsName = new ArrayList<>();
		List<PodDesc> pods = this.podService.getPodsWithLabelAndStatusPhase(
				wrapperProperties.getLabelWrapperApp().getLabel(), wrapperProperties.getLabelWrapperApp().getValue(),
				PodStatus.Succeeded.name());
		if (!CollectionUtils.isEmpty(pods)) {
			for (PodDesc pod : pods) {
				String podName = pod.getName();
				String suffixe = podName.substring(podName.lastIndexOf('-') + 1);
				Boolean ret = this.podService.deletePod(suffixe);
				if (ret != null && ret) {
					deletedPodsName.add(pod.getName());
				} else {
					deletedPodsName.add("KO-" + pod.getName());
				}
			}
		}
		return deletedPodsName;
	}
}
