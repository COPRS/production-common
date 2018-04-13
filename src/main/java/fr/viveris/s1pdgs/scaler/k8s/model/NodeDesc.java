package fr.viveris.s1pdgs.scaler.k8s.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

public class NodeDesc {
	
	private String name;
	
	private String externalId;
	
	private Map<AddressType, String> addresses;
	
	private Map<String, String> labels;
	
	private List<VolumeDesc> volumes;
	
	public NodeDesc(String name) {
		this.name = name;
		this.addresses = new HashMap<>();
		this.labels = new HashMap<>();
		this.volumes = new ArrayList<>();
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
	 * @return the externalId
	 */
	public String getExternalId() {
		return externalId;
	}

	/**
	 * @param externalId the externalId to set
	 */
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	/**
	 * @return the addresses
	 */
	public Map<AddressType, String> getAddresses() {
		return addresses;
	}

	/**
	 * @param addresses the addresses to set
	 */
	public void addAddresses(Map<AddressType, String> addresses) {
		if (!CollectionUtils.isEmpty(addresses)) {
			this.addresses.putAll(addresses);
		}
	}

	/**
	 * @param addresses the addresses to set
	 */
	public void addAddress(AddressType type, String address) {
		this.addresses.put(type, address);
	}

	/**
	 * @return the labels
	 */
	public Map<String, String> getLabels() {
		return labels;
	}

	/**
	 * @param labels the labels to set
	 */
	public void addLabels(Map<String, String> labels) {
		if (!CollectionUtils.isEmpty(labels)) {
			this.labels.putAll(labels);
		}
	}

	/**
	 * @param labels the labels to set
	 */
	public void addLabels(String label, String value) {
		this.labels.put(label, value);
	}

	/**
	 * @return the volumes
	 */
	public List<VolumeDesc> getVolumes() {
		return volumes;
	}

	/**
	 * @param volumes the volumes to set
	 */
	public void addVolumes(List<VolumeDesc> volumes) {
		if (!CollectionUtils.isEmpty(volumes)) {
			this.volumes.addAll(volumes);
		}
	}

	/**
	 * @param volumes the volumes to set
	 */
	public void addVolume(VolumeDesc volume) {
		if (volume != null) {
			this.volumes.add(volume);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{name: " + name + ", externalId: " + externalId + ", addresses: " + addresses + ", labels: " + labels
				+ ", volumes: " + volumes + "}";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addresses == null) ? 0 : addresses.hashCode());
		result = prime * result + ((externalId == null) ? 0 : externalId.hashCode());
		result = prime * result + ((labels == null) ? 0 : labels.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((volumes == null) ? 0 : volumes.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeDesc other = (NodeDesc) obj;
		if (addresses == null) {
			if (other.addresses != null)
				return false;
		} else if (!addresses.equals(other.addresses))
			return false;
		if (externalId == null) {
			if (other.externalId != null)
				return false;
		} else if (!externalId.equals(other.externalId))
			return false;
		if (labels == null) {
			if (other.labels != null)
				return false;
		} else if (!labels.equals(other.labels))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (volumes == null) {
			if (other.volumes != null)
				return false;
		} else if (!volumes.equals(other.volumes))
			return false;
		return true;
	}
	
}
