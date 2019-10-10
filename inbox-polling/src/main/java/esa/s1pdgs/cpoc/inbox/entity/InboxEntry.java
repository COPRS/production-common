package esa.s1pdgs.cpoc.inbox.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class InboxEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String name;
	private String relativePath;
	private String pickupPath;
	private String url;
	private String missionId;
	private String satelliteId;
	private String stationCode;

	public InboxEntry() {
	}

	public InboxEntry(String name, String relativePath, String pickupPath, String missionId, String satelliteId,
			String stationCode) {
		this.name = name;
		this.relativePath = relativePath;
		this.pickupPath = pickupPath;
		this.missionId = missionId;
		this.satelliteId = satelliteId;
		this.stationCode = stationCode;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getPickupPath() {
		return pickupPath;
	}

	public void setPickupPath(String pickupPath) {
		this.pickupPath = pickupPath;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMissionId() {
		return missionId;
	}

	public void setMissionId(String missionId) {
		this.missionId = missionId;
	}

	public String getSatelliteId() {
		return satelliteId;
	}

	public void setSatelliteId(String satelliteId) {
		this.satelliteId = satelliteId;
	}

	public String getStationCode() {
		return stationCode;
	}

	public void setStationCode(String stationCode) {
		this.stationCode = stationCode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((missionId == null) ? 0 : missionId.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((pickupPath == null) ? 0 : pickupPath.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((relativePath == null) ? 0 : relativePath.hashCode());
		result = prime * result + ((satelliteId == null) ? 0 : satelliteId.hashCode());
		result = prime * result + ((stationCode == null) ? 0 : stationCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InboxEntry other = (InboxEntry) obj;
		if (missionId == null) {
			if (other.missionId != null)
				return false;
		} else if (!missionId.equals(other.missionId))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (pickupPath == null) {
			if (other.pickupPath != null)
				return false;
		} else if (!pickupPath.equals(other.pickupPath))
			return false;
		if (relativePath == null) {
			if (other.relativePath != null)
				return false;
		} else if (!relativePath.equals(other.relativePath))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (satelliteId == null) {
			if (other.satelliteId != null)
				return false;
		} else if (!satelliteId.equals(other.satelliteId))
			return false;
		if (stationCode == null) {
			if (other.stationCode != null)
				return false;
		} else if (!stationCode.equals(other.stationCode))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"InboxEntry [id=%s, name=%s, relativePath=%s, pickupPath=%s, url=%s, missionId=%s, satelliteId=%s, stationCode=%s]",
				id, name, relativePath, pickupPath, url, missionId, satelliteId, stationCode);
	}
}
