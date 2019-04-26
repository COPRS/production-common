package test;

import static org.mockito.Mockito.doReturn;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.scaler.openstack.OpenStackServerProperties;
import esa.s1pdgs.cpoc.scaler.openstack.OpenStackServerProperties.ServerProperties;
import esa.s1pdgs.cpoc.scaler.openstack.OpenStackServerProperties.VolumeProperties;

public class MockPropertiesTest {

    /**
     * Openstack properties
     */
    @Mock
    protected OpenStackServerProperties osProperties;
    protected OpenStackServerProperties osPropertiesExp;

    /**
     * Openstack properties
     */
    @Mock
    protected ServerProperties osServerProperties;
    protected ServerProperties osServerPropertiesExp;

    /**
     * Openstack properties
     */
    @Mock
    protected VolumeProperties osVolumeProperties;
    protected VolumeProperties osVolumePropertiesExp;
    
    /**
     * 
     * @return
     */
    protected void initTest() {
        // Init mocks
        MockitoAnnotations.initMocks(this);
        
        // Init OS
        this.initExpectedOsProperties();
        this.mockOsProperties();
    }
    
    /**
     * Init expected OS objects
     */
    protected void initExpectedOsProperties() {
        osPropertiesExp = new OpenStackServerProperties();
        osPropertiesExp.setEndpoint("endpoint");
        osPropertiesExp.setDomainId("domainId");
        osPropertiesExp.setProjectId("projectId");
        osPropertiesExp.setCredentialUsername("credentialUsername");
        osPropertiesExp.setCredentialPassword("credentialPassword");
        
        osServerPropertiesExp = new ServerProperties();
        osServerPropertiesExp.setPrefixName("prefix-serv");
        osServerPropertiesExp.setImageRef("image-ref");
        osServerPropertiesExp.setBootDeviceName("device-name");
        osServerPropertiesExp.setFlavor("flavor");
        osServerPropertiesExp.setKeySecurity("key-sec");
        osServerPropertiesExp.getSecurityGroups().add("sec-group1");
        osServerPropertiesExp.getSecurityGroups().add("sec-group2");
        osServerPropertiesExp.getNetworks().add("network1");
        osServerPropertiesExp.getNetworks().add("network2");
        osServerPropertiesExp.setAvailableZone("zone");
        osServerPropertiesExp.setFloatActivation(true);
        osServerPropertiesExp.setFloatingNetwork("float");
        osServerPropertiesExp.setBootableOnVolume(true);
        
        osVolumePropertiesExp = new VolumeProperties();
        osVolumePropertiesExp.setPrefixName("prefix-vol");
        osVolumePropertiesExp.setDescription("desc");
        osVolumePropertiesExp.setVolumeType("volumetype");
        osVolumePropertiesExp.setZone("zone");
        osVolumePropertiesExp.setSize(2);
    }
    
    /**
     * Mock open stack properties
     */
    protected void mockOsProperties() {
        doReturn(osPropertiesExp.getEndpoint()).when(osProperties).getEndpoint();
        doReturn(osPropertiesExp.getDomainId()).when(osProperties).getDomainId();
        doReturn(osPropertiesExp.getProjectId()).when(osProperties).getProjectId();
        doReturn(osPropertiesExp.getCredentialUsername()).when(osProperties).getCredentialUsername();
        doReturn(osPropertiesExp.getCredentialPassword()).when(osProperties).getCredentialPassword();
        doReturn(osVolumeProperties).when(osProperties).getVolumeWrapper();
        doReturn(osServerProperties).when(osProperties).getServerWrapper();

        doReturn(osServerPropertiesExp.getPrefixName()).when(osServerProperties).getPrefixName();
        doReturn(osServerPropertiesExp.getImageRef()).when(osServerProperties).getImageRef();
        doReturn(osServerPropertiesExp.getBootDeviceName()).when(osServerProperties).getBootDeviceName();
        doReturn(osServerPropertiesExp.getFlavor()).when(osServerProperties).getFlavor();
        doReturn(osServerPropertiesExp.getKeySecurity()).when(osServerProperties).getKeySecurity();
        doReturn(osServerPropertiesExp.getSecurityGroups()).when(osServerProperties).getSecurityGroups();
        doReturn(osServerPropertiesExp.getNetworks()).when(osServerProperties).getNetworks();
        doReturn(osServerPropertiesExp.getAvailableZone()).when(osServerProperties).getAvailableZone();
        doReturn(osServerPropertiesExp.isFloatActivation()).when(osServerProperties).isFloatActivation();
        doReturn(osServerPropertiesExp.getFloatingNetwork()).when(osServerProperties).getFloatingNetwork();
        doReturn(osServerPropertiesExp.isBootableOnVolume()).when(osServerProperties).isBootableOnVolume();
        

        doReturn(osVolumePropertiesExp.getPrefixName()).when(osVolumeProperties).getPrefixName();
        doReturn(osVolumePropertiesExp.getDescription()).when(osVolumeProperties).getDescription();
        doReturn(osVolumePropertiesExp.getVolumeType()).when(osVolumeProperties).getVolumeType();
        doReturn(osVolumePropertiesExp.getZone()).when(osVolumeProperties).getZone();
        doReturn(osVolumePropertiesExp.getSize()).when(osVolumeProperties).getSize();
        
    }
}
