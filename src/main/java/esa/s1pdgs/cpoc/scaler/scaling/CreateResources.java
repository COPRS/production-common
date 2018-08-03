package esa.s1pdgs.cpoc.scaler.scaling;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import esa.s1pdgs.cpoc.scaler.k8s.K8SAdministration;
import esa.s1pdgs.cpoc.scaler.openstack.OpenStackAdministration;

public class CreateResources implements Callable<String> {

	private final K8SAdministration k8SAdministration;

	private final OpenStackAdministration osAdministration;
	
	private AtomicInteger uniqueVMID;
	
	private AtomicInteger uniquePODID;
	
	public CreateResources(final K8SAdministration k8SAdministration, final OpenStackAdministration osAdministration, AtomicInteger uVMID, AtomicInteger uPODID) {
		this.k8SAdministration = k8SAdministration;
		this.osAdministration = osAdministration;
		this.uniqueVMID = uVMID;
		this.uniquePODID = uPODID;
	}

	@Override
	public String call() throws Exception {
		String result = this.osAdministration.createServerForL1Wrappers("[MONITOR] [Step 4] 2 - ", uniqueVMID);
		this.k8SAdministration.launchWrapperPodsPool(1, uniquePODID);
		return result;
	}

}
