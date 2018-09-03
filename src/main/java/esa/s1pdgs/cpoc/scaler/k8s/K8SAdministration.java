package esa.s1pdgs.cpoc.scaler.k8s;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.common.errors.k8s.K8sUnknownResourceException;
import esa.s1pdgs.cpoc.common.errors.k8s.PodResourceException;
import esa.s1pdgs.cpoc.common.errors.k8s.WrapperStopException;
import esa.s1pdgs.cpoc.scaler.k8s.model.PodDesc;
import esa.s1pdgs.cpoc.scaler.k8s.model.PodStatus;
import esa.s1pdgs.cpoc.scaler.k8s.services.NodeService;
import esa.s1pdgs.cpoc.scaler.k8s.services.PodService;
import esa.s1pdgs.cpoc.scaler.k8s.services.WrapperService;

@Service
public class K8SAdministration {

    private final WrapperProperties wrapperProperties;

    private final NodeService nodeService;

    private final PodService podService;

    private final WrapperService wrapperService;

    @Autowired
    public K8SAdministration(final WrapperProperties wrapperProperties,
            final NodeService nodeService, final PodService podService,
            final WrapperService wrapperService) {
        this.wrapperProperties = wrapperProperties;
        this.nodeService = nodeService;
        this.podService = podService;
        this.wrapperService = wrapperService;
    }

    public void setWrapperNodeUsable(String nodeName) {
        this.nodeService.editLabelToNode(nodeName,
                wrapperProperties.getLabelWrapperStateUsed().getLabel(),
                wrapperProperties.getLabelWrapperStateUsed().getValue());
    }

    public void setWrapperNodeUnusable(String nodeName) {
        this.nodeService.editLabelToNode(nodeName,
                wrapperProperties.getLabelWrapperStateUnused().getLabel(),
                wrapperProperties.getLabelWrapperStateUnused().getValue());
    }

    public void launchWrapperPodsPool(int nbPods, AtomicInteger uniquePODID)
            throws PodResourceException, K8sUnknownResourceException {
        for (int i = 0; i < nbPods; i++) {
            this.podService.createPodFromTemplate(
                    wrapperProperties.getPodTemplateFile(),
                    uniquePODID.getAndIncrement());
        }
    }

    public void stopWrapperPods(List<String> wrapperIps)
            throws WrapperStopException {
        if (!CollectionUtils.isEmpty(wrapperIps)) {
            for (String w : wrapperIps) {
                this.wrapperService.stopWrapper(w);
            }
        }
    }

    public List<String> deleteTerminatedWrapperPods()
            throws PodResourceException, K8sUnknownResourceException {
        List<String> deletedPodsName = new ArrayList<>();
        List<PodDesc> pods = this.podService.getPodsWithLabelAndStatusPhase(
                wrapperProperties.getLabelWrapperApp().getLabel(),
                wrapperProperties.getLabelWrapperApp().getValue(),
                PodStatus.Succeeded.name());
        if (!CollectionUtils.isEmpty(pods)) {
            for (PodDesc pod : pods) {
                String podName = pod.getName();
                String[] podNameParts = podName.split("-");
                String suffixe = "-" + podNameParts[podNameParts.length - 2]
                        + "-" + podNameParts[podNameParts.length - 1];
                Boolean ret = this.podService.deletePodFromTemplate(
                        wrapperProperties.getPodTemplateFile(), suffixe);
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
