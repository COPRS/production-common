package esa.s1pdgs.cpoc.scaler.openstack;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * OpenStack configuration
 * 
 * @author Viveris Technologies
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "openstack.server")
public class OpenStackServerProperties {

    // -------------------------
    // Authentication
    // -------------------------
    private String endpoint;
    private String domainId;
    private String projectId;
    private String credentialUsername;
    private String credentialPassword;

    // -------------------------
    // Volumes
    // -------------------------
    private VolumeProperties volumeWrapper;
    private ServerProperties serverWrapper;

    /**
     * 
     */
    public OpenStackServerProperties() {
        super();
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @param endpoint
     *            the endpoint to set
     */
    public void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @return the domainId
     */
    public String getDomainId() {
        return domainId;
    }

    /**
     * @param domainId
     *            the domainId to set
     */
    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    /**
     * @return the projectId
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * @param projectId
     *            the projectId to set
     */
    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    /**
     * @return the credentialUsername
     */
    public String getCredentialUsername() {
        return credentialUsername;
    }

    /**
     * @param credentialUsername
     *            the credentialUsername to set
     */
    public void setCredentialUsername(final String credentialUsername) {
        this.credentialUsername = credentialUsername;
    }

    /**
     * @return the credentialPassword
     */
    public String getCredentialPassword() {
        return credentialPassword;
    }

    /**
     * @param credentialPassword
     *            the credentialPassword to set
     */
    public void setCredentialPassword(final String credentialPassword) {
        this.credentialPassword = credentialPassword;
    }

    /**
     * @return the volumeWrapper
     */
    public VolumeProperties getVolumeWrapper() {
        return volumeWrapper;
    }

    /**
     * @param volumeWrapper
     *            the volumeWrapper to set
     */
    public void setVolumeWrapper(final VolumeProperties volumeWrapper) {
        this.volumeWrapper = volumeWrapper;
    }

    /**
     * @return the serverWrapper
     */
    public ServerProperties getServerWrapper() {
        return serverWrapper;
    }

    /**
     * @param serverWrapper
     *            the serverWrapper to set
     */
    public void setServerWrapper(final ServerProperties serverWrapper) {
        this.serverWrapper = serverWrapper;
    }

    /**
     * 
     */
    public static class VolumeProperties {

        /**
         * 
         */
        private String prefixName;

        /**
         * 
         */
        private String description;

        /**
         * 
         */
        private String volumeType;

        /**
         * 
         */
        private String zone;

        /**
         * 
         */
        private int size;

        /**
         * 
         */
        public VolumeProperties() {
            super();
        }

        /**
         * @return the prefixName
         */
        public String getPrefixName() {
            return prefixName;
        }

        /**
         * @param prefixName
         *            the prefixName to set
         */
        public void setPrefixName(final String prefixName) {
            this.prefixName = prefixName;
        }

        /**
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * @param description
         *            the description to set
         */
        public void setDescription(final String description) {
            this.description = description;
        }

        /**
         * @return the volumeType
         */
        public String getVolumeType() {
            return volumeType;
        }

        /**
         * @param volumeType
         *            the volumeType to set
         */
        public void setVolumeType(final String volumeType) {
            this.volumeType = volumeType;
        }

        /**
         * @return the zone
         */
        public String getZone() {
            return zone;
        }

        /**
         * @param zone
         *            the zone to set
         */
        public void setZone(final String zone) {
            this.zone = zone;
        }

        /**
         * @return the size
         */
        public int getSize() {
            return size;
        }

        /**
         * @param size
         *            the size to set
         */
        public void setSize(final int size) {
            this.size = size;
        }

    }

    /**
     * 
     */
    public static class ServerProperties {

        /**
         * 
         */
        private String prefixName;

        /**
         * 
         */
        private String bootDeviceName;

        /**
         * 
         */
        private String flavor;

        /**
         * 
         */
        private String keySecurity;

        /**
         * 
         */
        private List<String> securityGroups;

        /**
         * 
         */
        private List<String> networks;

        /**
         * 
         */
        private String availableZone;

        /**
         * 
         */
        private boolean floatActivation;

        /**
         * 
         */
        private String floatingNetwork;

        /**
         * 
         */
        private boolean bootableOnVolume;

        /**
         * 
         */
        private String imageRef;

        /**
         * 
         */
        public ServerProperties() {
            networks = new ArrayList<>();
            securityGroups = new ArrayList<>();
        }

        /**
         * @return the prefixName
         */
        public String getPrefixName() {
            return prefixName;
        }

        /**
         * @param prefixName
         *            the prefixName to set
         */
        public void setPrefixName(final String prefixName) {
            this.prefixName = prefixName;
        }

        /**
         * @return the imageRef
         */
        public String getImageRef() {
            return imageRef;
        }

        /**
         * @param imageRef
         *            the imageRef to set
         */
        public void setImageRef(final String imageRef) {
            this.imageRef = imageRef;
        }

        /**
         * @return the bootDeviceName
         */
        public String getBootDeviceName() {
            return bootDeviceName;
        }

        /**
         * @param bootDeviceName
         *            the bootDeviceName to set
         */
        public void setBootDeviceName(final String bootDeviceName) {
            this.bootDeviceName = bootDeviceName;
        }

        /**
         * @return the flavor
         */
        public String getFlavor() {
            return flavor;
        }

        /**
         * @param flavor
         *            the flavor to set
         */
        public void setFlavor(final String flavor) {
            this.flavor = flavor;
        }

        /**
         * @return the keySecurity
         */
        public String getKeySecurity() {
            return keySecurity;
        }

        /**
         * @param keySecurity
         *            the keySecurity to set
         */
        public void setKeySecurity(final String keySecurity) {
            this.keySecurity = keySecurity;
        }

        /**
         * @return the securityGroup
         */
        public List<String> getSecurityGroups() {
            return securityGroups;
        }

        /**
         * @param securityGroup
         *            the securityGroup to set
         */
        public void setSecurityGroups(final List<String> securityGroups) {
            this.securityGroups = securityGroups;
        }

        /**
         * @return the networks
         */
        public List<String> getNetworks() {
            return networks;
        }

        /**
         * @param networks
         *            the networks to set
         */
        public void setNetworks(final List<String> networks) {
            this.networks = networks;
        }

        /**
         * @return the availableZone
         */
        public String getAvailableZone() {
            return availableZone;
        }

        /**
         * @param availableZone
         *            the availableZone to set
         */
        public void setAvailableZone(final String availableZone) {
            this.availableZone = availableZone;
        }

        /**
         * @return the floatingActivation
         */
        public boolean isFloatActivation() {
            return floatActivation;
        }

        /**
         * @param floatActivation
         *            the floatActivation to set
         */
        public void setFloatActivation(final boolean floatActivation) {
            this.floatActivation = floatActivation;
        }

        /**
         * @return the floatingNetwork
         */
        public String getFloatingNetwork() {
            return floatingNetwork;
        }

        /**
         * @param floatingNetwork
         *            the floatingNetwork to set
         */
        public void setFloatingNetwork(final String floatingNetwork) {
            this.floatingNetwork = floatingNetwork;
        }

        /**
         * @return the bootableOnVolume
         */
        public boolean isBootableOnVolume() {
            return bootableOnVolume;
        }

        /**
         * @param bootableOnVolume
         *            the bootableOnVolume to set
         */
        public void setBootableOnVolume(final boolean bootableOnVolume) {
            this.bootableOnVolume = bootableOnVolume;
        }

    }
}
