package esa.s1pdgs.cpoc.scaler.openstack.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.scaler.openstack.model.VolumeDesc;
import esa.s1pdgs.cpoc.scaler.openstack.model.VolumeDesc.VolumeDescBuilder;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class VolumeDescTest {

    @Test
    public void testBuilder() {
        VolumeDescBuilder builder = new VolumeDescBuilder();
        VolumeDesc desc = builder.volumeType("type")
                .description("description")
                .id("id")
                .imageRef("image-ref")
                .zone("zone")
                .name("name")
                .size(10)
                .bootable(false)
           .build();
        
        assertEquals("id", desc.getId());
        assertEquals("name", desc.getName());
        assertEquals("image-ref", desc.getImageRef());
        assertEquals(false, desc.isBootable());
        assertEquals(10, desc.getSize());
        assertEquals("zone", desc.getZone());
        assertEquals("description", desc.getDescription());
        assertEquals("type", desc.getVolumeType());

        String str = desc.toString();
        assertTrue(str.contains("id: id"));
    }

    /**
     * Check equals and hashcode methods
     */
    @Test
    public void checkEquals() {
        EqualsVerifier.forClass(VolumeDesc.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
