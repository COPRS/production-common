package esa.s1pdgs.cpoc.scaler.k8s.model.converter;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.scaler.k8s.model.AddressType;
import esa.s1pdgs.cpoc.scaler.k8s.model.NodeDesc;
import esa.s1pdgs.cpoc.scaler.k8s.model.VolumeDesc;
import io.fabric8.kubernetes.api.model.Node;

/**
 * Convert TaskTable objects into JobOrder objects
 * 
 * @author Cyrielle Gailliard
 *
 */
public class K8SNodeToNodeDesc implements SuperConverter<Node, NodeDesc> {
	
	@Override
	public NodeDesc apply(Node t) {
		final NodeDesc n = new NodeDesc(t.getMetadata().getName());
		n.setExternalId(t.getSpec().getExternalID());
		if (!CollectionUtils.isEmpty(t.getStatus().getAddresses())) {
			t.getStatus().getAddresses().forEach(nodeAddress -> {
				n.addAddress(AddressType.fromLabel(nodeAddress.getType()), nodeAddress.getAddress());
			});
		}
		if (!CollectionUtils.isEmpty(t.getMetadata().getLabels())) {
			n.addLabels(t.getMetadata().getLabels());
		}
		if (!CollectionUtils.isEmpty(t.getStatus().getVolumesAttached())) {
			t.getStatus().getVolumesAttached().forEach(volume -> {
				n.addVolume(new VolumeDesc(volume.getName()));
			});
		}
		return n;
	}
	
}