package fr.viveris.s1pdgs.scaler.openstack.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Description of a server
 * 
 * @author Viveris Technologies
 */
public class ServerDesc {

    /**
     * 
     */
    private String identifier;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private String imageRef;

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
    private boolean bootableOnVolume;

    /**
     * 
     */
    private String bootVolume;

    /**
     * 
     */
    private String bootDeviceName;

    /**
     * 
     */
    public ServerDesc() {
        this.networks = new ArrayList<>();
        this.securityGroups = new ArrayList<>();
        this.bootableOnVolume = false;
    }

    /**
     * @return the id
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(final String name) {
        this.name = name;
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
     * @return the bootableOnVolume
     */
    public boolean isBootableOnVolume() {
        return bootableOnVolume;
    }

    /**
     * @return the bootVolume
     */
    public String getBootVolume() {
        return bootVolume;
    }

    /**
     * @return the bootDeviceName
     */
    public String getBootDeviceName() {
        return bootDeviceName;
    }

    public void setBootableOnVolumeInformation(final String bootVolume,
            final String bootDeviceName) {
        this.bootableOnVolume = true;
        this.bootDeviceName = bootDeviceName;
        this.bootVolume = bootVolume;
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
    public void addSecurityGroups(final List<String> securityGroups) {
        this.securityGroups.addAll(securityGroups);
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
    public void addNetwork(String network) {
        this.networks.add(network);
    }

    /**
     * @param networks
     *            the networks to set
     */
    public void addNetworks(List<String> networks) {
        this.networks.addAll(networks);
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

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "{identifier: %s, name: %s, imageRef: %s, flavor: %s, keySecurity: %s, securityGroups: %s, networks: %s, availableZone: %s, bootableOnVolume: %s, bootVolume: %s, bootDeviceName: %s}",
                identifier, name, imageRef, flavor, keySecurity, securityGroups,
                networks, availableZone, bootableOnVolume, bootVolume,
                bootDeviceName);
    }

    /**
     * hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(identifier, name, imageRef, flavor, keySecurity,
                securityGroups, networks, availableZone, bootableOnVolume,
                bootVolume, bootDeviceName);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            ServerDesc other = (ServerDesc) obj;
            ret = Objects.equals(identifier, other.identifier)
                    && Objects.equals(name, other.name)
                    && Objects.equals(imageRef, other.imageRef)
                    && Objects.equals(flavor, other.flavor)
                    && Objects.equals(keySecurity, other.keySecurity)
                    && Objects.equals(securityGroups, other.securityGroups)
                    && Objects.equals(networks, other.networks)
                    && Objects.equals(availableZone, other.availableZone)
                    && bootableOnVolume == other.bootableOnVolume
                    && Objects.equals(bootVolume, other.bootVolume)
                    && Objects.equals(bootDeviceName, other.bootDeviceName);
        }
        return ret;
    }

    /**
     * @return
     */
    public static ServerDescBuilder builder() {
        return new ServerDescBuilder();
    }

    /**
     * @author Viveris Technologies
     */
    public static class ServerDescBuilder {

        /**
         * 
         */
        private ServerDesc server;

        /**
         * 
         */
        ServerDescBuilder() {
            this(new ServerDesc());
        }

        /**
         * @param m
         */
        ServerDescBuilder(final ServerDesc server) {
            this.server = server;
        }

        /**
         * @param id
         * @return
         */
        public ServerDescBuilder identifier(final String identifier) {
            this.server.setIdentifier(identifier);
            return this;
        }

        /**
         * @param name
         * @return
         */
        public ServerDescBuilder name(final String name) {
            this.server.setName(name);
            return this;
        }

        /**
         * @param bootVolume
         * @param bootDeviceName
         * @return
         */
        public ServerDescBuilder bootOnVolumeInformation(
                final String bootVolume, final String bootDeviceName) {
            this.server.setBootableOnVolumeInformation(bootVolume,
                    bootDeviceName);
            return this;
        }

        /**
         * @param flavor
         * @return
         */
        public ServerDescBuilder flavor(final String flavor) {
            this.server.setFlavor(flavor);
            return this;
        }

        /**
         * @param keySecurity
         * @return
         */
        public ServerDescBuilder keySecurity(final String keySecurity) {
            this.server.setKeySecurity(keySecurity);
            return this;
        }

        /**
         * @param securityGroups
         * @return
         */
        public ServerDescBuilder securityGroups(
                final List<String> securityGroups) {
            this.server.addSecurityGroups(securityGroups);
            return this;
        }

        /**
         * @param network
         * @return
         */
        public ServerDescBuilder network(final String network) {
            this.server.addNetwork(network);
            return this;
        }

        /**
         * @param networks
         * @return
         */
        public ServerDescBuilder networks(final List<String> networks) {
            this.server.addNetworks(networks);
            return this;
        }

        /**
         * @param availableZone
         * @return
         */
        public ServerDescBuilder availableZone(final String availableZone) {
            this.server.setAvailableZone(availableZone);
            return this;
        }

        /**
         * @param imageRef
         * @return
         */
        public ServerDescBuilder imageRef(final String imageRef) {
            this.server.setImageRef(imageRef);
            return this;
        }

        /**
         * @return
         */
        public ServerDesc build() {
            return this.server;
        }
    }
}
