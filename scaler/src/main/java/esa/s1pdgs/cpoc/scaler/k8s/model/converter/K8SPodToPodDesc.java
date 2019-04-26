package esa.s1pdgs.cpoc.scaler.k8s.model.converter;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.scaler.k8s.model.AddressType;
import esa.s1pdgs.cpoc.scaler.k8s.model.PodDesc;
import esa.s1pdgs.cpoc.scaler.k8s.model.PodStatus;
import io.fabric8.kubernetes.api.model.Pod;

/**
 * Convert TaskTable objects into JobOrder objects
 * 
 * @author Cyrielle Gailliard
 *
 */
public class K8SPodToPodDesc implements SuperConverter<Pod, PodDesc> {
	
	@Override
	public PodDesc apply(Pod t) {
		final PodDesc n = new PodDesc(t.getMetadata().getName());
		n.setNodeName(t.getSpec().getNodeName());
		n.setNodeIpAddress(t.getStatus().getHostIP());
		n.addAddress(AddressType.HOSTNAME, t.getSpec().getHostname());
		n.addAddress(AddressType.INTERNAL_IP, t.getStatus().getPodIP());
		if (!CollectionUtils.isEmpty(t.getMetadata().getLabels())) {
			n.addLabels(t.getMetadata().getLabels());
		}
		n.setStatus(PodStatus.valueOf(t.getStatus().getPhase()));
		return n;
	}
	
}