package fr.viveris.s1pdgs.scaler.task;

import java.util.concurrent.Callable;

import fr.viveris.s1pdgs.scaler.k8s.K8SAdministration;
import fr.viveris.s1pdgs.scaler.openstack.OpenStackAdministration;

public class CreateResources implements Callable<String> {

	private final K8SAdministration k8SAdministration;

	private final OpenStackAdministration osAdministration;
	
	public CreateResources(final K8SAdministration k8SAdministration, final OpenStackAdministration osAdministration) {
		this.k8SAdministration = k8SAdministration;
		this.osAdministration = osAdministration;
	}

	@Override
	public String call() throws Exception {
		String result = this.osAdministration.createServerForL1Wrappers("[MONITOR] [Step 4] 2 - ");
		this.k8SAdministration.launchWrapperPodsPool(1);
		if (Thread.currentThread().isInterrupted()) {
			return null;
		}
		return result;
	}

}
