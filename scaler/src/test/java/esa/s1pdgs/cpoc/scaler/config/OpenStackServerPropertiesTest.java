package esa.s1pdgs.cpoc.scaler.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.scaler.config.OpenStackServerProperties;
import esa.s1pdgs.cpoc.scaler.config.OpenStackServerProperties.ServerProperties;
import esa.s1pdgs.cpoc.scaler.config.OpenStackServerProperties.VolumeProperties;

/**
 * Test the properties of OpenStack
 * 
 * @author Viveris Technologies
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class OpenStackServerPropertiesTest {

    /**
     * Properties to test
     */
    @Autowired
    private OpenStackServerProperties properties;

    /**
     * Test the parsing of the properties
     */
    @Test
    public void testInit() {
        // Check global properties
        assertEquals(
                "https://iam.eu-west-0.prod-cloud-ocb.orange-business.com/v3",
                properties.getEndpoint());
        assertEquals("d04910ec738940b09f447805fab037bb",
                properties.getDomainId());
        assertEquals("c49bc6063b8e4349995f9e72654ceffd",
                properties.getProjectId());
        assertEquals("patrice.caubet", properties.getCredentialUsername());
        assertEquals("Tlse2018", properties.getCredentialPassword());

        // Check server
        VolumeProperties volume = properties.getVolumeWrapper();
        assertEquals("k8s-volume-l1", volume.getPrefixName());
        assertEquals("Bootable volume for node for wrapper L1",
                volume.getDescription());
        assertEquals("SSD", volume.getVolumeType());
        assertEquals("eu-west-0b", volume.getZone());
        assertEquals(40, volume.getSize());

        // Check volume
        ServerProperties server = properties.getServerWrapper();
        assertEquals("k8s-node-w-l1", server.getPrefixName());
        assertEquals("79a60603-11be-4183-83ab-21197977253e",
                server.getImageRef());
        assertEquals("/dev/vda", server.getBootDeviceName());
        assertEquals("h1.2xlarge.4", server.getFlavor());
        assertEquals("viv_k8s", server.getKeySecurity());
        assertEquals(2, server.getSecurityGroups().size());
        assertEquals("k8s_secgroup", server.getSecurityGroups().get(0));
        assertEquals("key_2", server.getSecurityGroups().get(1));
        assertEquals(2, server.getNetworks().size());
        assertEquals("01f954b1-0290-4bb4-9ee6-9ef46c2983fb",
                server.getNetworks().get(0));
        assertEquals("tutu", server.getNetworks().get(1));
        assertEquals("eu-west-0b", server.getAvailableZone());
        assertEquals(true, server.isFloatActivation());
        assertEquals("0a2228f2-7f8a-45f1-8e09-9039e1d09975",
                server.getFloatingNetwork());
        assertEquals(false, server.isBootableOnVolume());
    }

    @Test
    public void testSetters() {
        properties.setEndpoint("endpoint");
        properties.setDomainId("domain-id");
        properties.setProjectId("project-id");
        properties.setCredentialPassword("crd-passwd");
        properties.setCredentialUsername("cred-username");
        assertEquals("endpoint", properties.getEndpoint());
        assertEquals("domain-id", properties.getDomainId());
        assertEquals("project-id", properties.getProjectId());
        assertEquals("cred-username", properties.getCredentialUsername());
        assertEquals("crd-passwd", properties.getCredentialPassword());
    }
}
