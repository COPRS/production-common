package fr.viveris.s1pdgs.scaler.k8s.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.CollectionUtils;

public class PodDesc {
	
	private String name;
	
	private String nodeName;
	
	private String nodeIpAddress;
	
	private Map<AddressType, String> addresses;
	
	private Map<String, String> labels;

	public PodDesc(String name) {
		this.name = name;
		this.addresses = new HashMap<>();
		this.labels = new HashMap<>();
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
	 * @return the nodeName
	 */
	public String getNodeName() {
		return nodeName;
	}

	/**
	 * @param nodeName the nodeName to set
	 */
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	/**
	 * @return the nodeIpAddress
	 */
	public String getNodeIpAddress() {
		return nodeIpAddress;
	}

	/**
	 * @param nodeIpAddress the nodeIpAddress to set
	 */
	public void setNodeIpAddress(String nodeIpAddress) {
		this.nodeIpAddress = nodeIpAddress;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{name: " + name + ", nodeName: " + nodeName + ", nodeIpAddress: " + nodeIpAddress + ", addresses: "
				+ addresses + ", labels: " + labels + "}";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addresses == null) ? 0 : addresses.hashCode());
		result = prime * result + ((labels == null) ? 0 : labels.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nodeIpAddress == null) ? 0 : nodeIpAddress.hashCode());
		result = prime * result + ((nodeName == null) ? 0 : nodeName.hashCode());
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
		PodDesc other = (PodDesc) obj;
		if (addresses == null) {
			if (other.addresses != null)
				return false;
		} else if (!addresses.equals(other.addresses))
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
		if (nodeIpAddress == null) {
			if (other.nodeIpAddress != null)
				return false;
		} else if (!nodeIpAddress.equals(other.nodeIpAddress))
			return false;
		if (nodeName == null) {
			if (other.nodeName != null)
				return false;
		} else if (!nodeName.equals(other.nodeName))
			return false;
		return true;
	}

}
