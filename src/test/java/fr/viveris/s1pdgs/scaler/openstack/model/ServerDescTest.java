package fr.viveris.s1pdgs.scaler.openstack.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import fr.viveris.s1pdgs.scaler.openstack.model.ServerDesc.ServerDescBuilder;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class ServerDescTest {

    @Test
    public void testBuilder() {
        ServerDescBuilder builder = new ServerDescBuilder();
        ServerDesc desc = builder.availableZone("zone")
                .bootOnVolumeInformation("volume", "device").flavor("flavor")
                .identifier("id").imageRef("image-ref").keySecurity("key")
                .name("name").network("network1")
                .networks(Arrays.asList("network2", "network3"))
                .securityGroups(Arrays.asList("group1", "group2")).build();

        assertEquals("id", desc.getIdentifier());
        assertEquals("name", desc.getName());
        assertEquals("image-ref", desc.getImageRef());
        assertEquals(true, desc.isBootableOnVolume());
        assertEquals("device", desc.getBootDeviceName());
        assertEquals("flavor", desc.getFlavor());
        assertEquals("key", desc.getKeySecurity());
        assertEquals(2, desc.getSecurityGroups().size());
        assertEquals("group1", desc.getSecurityGroups().get(0));
        assertEquals("group2", desc.getSecurityGroups().get(1));
        assertEquals(3, desc.getNetworks().size());
        assertEquals("network1", desc.getNetworks().get(0));
        assertEquals("network2", desc.getNetworks().get(1));
        assertEquals("network3", desc.getNetworks().get(2));
        assertEquals("zone", desc.getAvailableZone());

        String str = desc.toString();
        assertTrue(str.contains("identifier: id"));
    }

    /**
     * Check equals and hashcode methods
     */
    @Test
    public void checkEquals() {
        EqualsVerifier.forClass(ServerDesc.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }
}
