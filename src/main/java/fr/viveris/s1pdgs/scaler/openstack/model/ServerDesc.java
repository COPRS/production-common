package fr.viveris.s1pdgs.scaler.openstack.model;

import java.util.ArrayList;
import java.util.List;

public class ServerDesc {

	private String id;
	private String name;
	private String imageRef;
	private String flavor;
	private String keySecurity;
	private List<String> securityGroups;
	private List<String> networks;
	private String availableZone;
	
	private boolean bootableOnVolume;
	private String bootVolume;
	private String bootDeviceName;

	public ServerDesc() {
		this.networks = new ArrayList<>();
		this.securityGroups = new ArrayList<>();
		this.bootableOnVolume = false;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the imageRef
	 */
	public String getImageRef() {
		return imageRef;
	}

	/**
	 * @param imageRef the imageRef to set
	 */
	public void setImageRef(String imageRef) {
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
	
	public void setBootableOnVolumeInformation(String bootVolume, String bootDeviceName) {
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
	 * @param flavor the flavor to set
	 */
	public void setFlavor(String flavor) {
		this.flavor = flavor;
	}

	/**
	 * @return the keySecurity
	 */
	public String getKeySecurity() {
		return keySecurity;
	}

	/**
	 * @param keySecurity the keySecurity to set
	 */
	public void setKeySecurity(String keySecurity) {
		this.keySecurity = keySecurity;
	}

	/**
	 * @return the securityGroup
	 */
	public List<String> getSecurityGroups() {
		return securityGroups;
	}

	/**
	 * @param securityGroup the securityGroup to set
	 */
	public void addSecurityGroups(List<String> securityGroups) {
		this.securityGroups.addAll(securityGroups);
	}

	/**
	 * @return the networks
	 */
	public List<String> getNetworks() {
		return networks;
	}

	/**
	 * @param networks the networks to set
	 */
	public void setNetworks(List<String> networks) {
		this.networks = networks;
	}

	/**
	 * @return the availableZone
	 */
	public String getAvailableZone() {
		return availableZone;
	}

	/**
	 * @param availableZone the availableZone to set
	 */
	public void setAvailableZone(String availableZone) {
		this.availableZone = availableZone;
	}

	public static ServerDescBuilder builder() {
		return new ServerDescBuilder();
	}
	
	public static class ServerDescBuilder {
		private ServerDesc m;

		ServerDescBuilder() {
			this(new ServerDesc());
		}

		ServerDescBuilder(ServerDesc m) {
			this.m = m;
		}

		public ServerDescBuilder id(String id) {
			this.m.id = id;
			return this;
		}

		public ServerDescBuilder name(String name) {
			this.m.name = name;
			return this;
		}

		public ServerDescBuilder bootOnVolumeInformation(String bootVolume, String bootDeviceName) {
			this.m.setBootableOnVolumeInformation(bootVolume, bootDeviceName);
			return this;
		}

		public ServerDescBuilder flavor(String flavor) {
			this.m.flavor = flavor;
			return this;
		}

		public ServerDescBuilder keySecurity(String keySecurity) {
			this.m.keySecurity = keySecurity;
			return this;
		}

		public ServerDescBuilder securityGroups(List<String> securityGroups) {
			this.m.securityGroups.addAll(securityGroups);
			return this;
		}

		public ServerDescBuilder network(String network) {
			this.m.networks.add(network);
			return this;
		}

		public ServerDescBuilder networks(List<String> networks) {
			this.m.networks.addAll(networks);
			return this;
		}

		public ServerDescBuilder availableZone(String availableZone) {
			this.m.availableZone = availableZone;
			return this;
		}

		public ServerDescBuilder imageRef(String imageRef) {
			this.m.imageRef = imageRef;
			return this;
		}
		
		public ServerDesc build() {
			return this.m;
		}
	}
}
