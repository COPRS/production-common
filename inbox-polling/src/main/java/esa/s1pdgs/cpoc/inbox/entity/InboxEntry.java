package esa.s1pdgs.cpoc.inbox.entity;

import java.util.Objects;

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
	private String url;
	private String missionId;
	private String satelliteId;
	private String stationCode;

	public InboxEntry() {
	}

	public InboxEntry(String name, String url, String missionId, String satelliteId, String stationCode) {
		this.name = name;
		this.url = url;
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
		return Objects.hash(url);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(getClass())) {
			final InboxEntry other = (InboxEntry) obj;
			return Objects.equals(url, other.url) && Objects.equals(missionId, other.missionId)
					&& Objects.equals(satelliteId, other.satelliteId) && Objects.equals(stationCode, other.stationCode);
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("InboxEntry [id=%s, name=%s, url=%s, missionId=%s, satelliteId=%s, stationCode=%s]", id,
				name, url, missionId, satelliteId, stationCode);
	}
}
