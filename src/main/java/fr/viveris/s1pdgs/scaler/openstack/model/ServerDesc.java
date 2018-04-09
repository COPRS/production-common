package fr.viveris.s1pdgs.scaler.openstack.model;

import java.util.ArrayList;
import java.util.List;

public class ServerDesc {

	private String id;
	private String name;
	private String bootVolume;
	private String bootDeviceName;
	private String flavor;
	private String keySecurity;
	private String securityGroup;
	private List<String> networks;
	private String availableZone;

	public ServerDesc() {
		this.networks = new ArrayList<>();
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
	 * @return the bootVolume
	 */
	public String getBootVolume() {
		return bootVolume;
	}

	/**
	 * @param bootVolume the bootVolume to set
	 */
	public void setBootVolume(String bootVolume) {
		this.bootVolume = bootVolume;
	}

	/**
	 * @return the bootDeviceName
	 */
	public String getBootDeviceName() {
		return bootDeviceName;
	}

	/**
	 * @param bootDeviceName the bootDeviceName to set
	 */
	public void setBootDeviceName(String bootDeviceName) {
		this.bootDeviceName = bootDeviceName;
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
	public String getSecurityGroup() {
		return securityGroup;
	}

	/**
	 * @param securityGroup the securityGroup to set
	 */
	public void setSecurityGroup(String securityGroup) {
		this.securityGroup = securityGroup;
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

		public ServerDescBuilder bootVolume(String bootVolume) {
			this.m.bootVolume = bootVolume;
			return this;
		}

		public ServerDescBuilder bootDeviceName(String bootDeviceName) {
			this.m.bootDeviceName = bootDeviceName;
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

		public ServerDescBuilder securityGroup(String securityGroup) {
			this.m.securityGroup = securityGroup;
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
		
		public ServerDesc build() {
			return this.m;
		}
	}
}
