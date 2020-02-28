package esa.s1pdgs.cpoc.scaler.openstack;

import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.scaler.config.OpenStackServerProperties;

/**
 * 
 * @author Cyrielle
 *
 */
@Component
public class OpenStackClientFactory {

    /**
     * Build the open stack client
     * 
     * @return
     */
    public OSClientV3 osClient(final OpenStackServerProperties osProperties) {
        return OSFactory.builderV3().endpoint(osProperties.getEndpoint())
                .credentials(osProperties.getCredentialUsername(),
                        osProperties.getCredentialPassword(),
                        Identifier.byId(osProperties.getDomainId()))
                .scopeToProject(Identifier.byId(osProperties.getProjectId()))
                .authenticate();
    }

}
