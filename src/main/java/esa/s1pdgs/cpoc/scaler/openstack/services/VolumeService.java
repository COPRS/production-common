package esa.s1pdgs.cpoc.scaler.openstack.services;

import java.util.HashMap;
import java.util.Map;

import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.storage.block.Volume;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.errors.os.OsEntityException;
import esa.s1pdgs.cpoc.common.errors.os.OsEntityInternaloErrorException;
import esa.s1pdgs.cpoc.common.errors.os.OsVolumeNotAvailableException;
import esa.s1pdgs.cpoc.scaler.openstack.model.VolumeDesc;

@Service
public class VolumeService {

    private final int volumeMaxLoop;
    private final int volumeTempoLoopMs;

    @Autowired
    public VolumeService(
            @Value("${openstack.service.volume.creation.max-loop}") final int volumeMaxLoop,
            @Value("${openstack.service.volume.creation.tempo-loop-ms}") final int volumeTempoLoopMs) {
        this.volumeMaxLoop = volumeMaxLoop;
        this.volumeTempoLoopMs = volumeTempoLoopMs;
    }

    public String createVolumeAndBoot(OSClientV3 osClient, VolumeDesc desc)
            throws OsEntityException {

        // Create volume
        Volume v = osClient.blockStorage().volumes().create(Builders.volume()
                .name(desc.getName()).description(desc.getDescription())
                .imageRef(desc.getImageRef()).volumeType(desc.getVolumeType())
                .zone(desc.getZone()).size(desc.getSize())
                .bootable(desc.isBootable()).build());

        // Wait until volume status available
        int createVolumeCount = 1;
        boolean createVolumeFlag = false;
        while (createVolumeCount < volumeMaxLoop) {
            if (osClient.blockStorage().volumes().get(v.getId())
                    .getStatus() == Volume.Status.AVAILABLE) {
                createVolumeFlag = true;
                break;
            }
            try {
                Thread.sleep(volumeTempoLoopMs);
            } catch (InterruptedException e) {
                throw new OsEntityInternaloErrorException("volumeName",
                        desc.getName(),
                        String.format("reation of volume failed: %s",
                                e.getMessage()),
                        e);
            }
            createVolumeCount++;
        }
        if (!createVolumeFlag) {
            throw new OsVolumeNotAvailableException(desc.getName(),
                    String.format(
                            "Creation of volume not available after %d ms",
                            volumeMaxLoop * volumeTempoLoopMs));
        }
        return v.getId();
    }

    public void deleteVolume(OSClientV3 osClient, String volumeId) {
        osClient.blockStorage().volumes().delete(volumeId);
    }

    public Map<String, String> getVolumeIds(final OSClientV3 osClient,
            final String prefix, final String status) {
        Map<String, String> ret = new HashMap<String, String>();
        Map<String, String> filter = new HashMap<String, String>();
        filter.put("status", status);
        filter.put("name", "^" + prefix + "*");
        for (Volume volume : osClient.blockStorage().volumes().list(filter)) {
            ret.put(volume.getId(), volume.getName());
        }
        return ret;
    }

}
