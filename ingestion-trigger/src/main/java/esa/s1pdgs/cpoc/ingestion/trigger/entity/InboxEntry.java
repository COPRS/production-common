package esa.s1pdgs.cpoc.ingestion.trigger.entity;

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
	private String relativePath;
	private String pickupPath;

	public InboxEntry() {
	}

	public InboxEntry(final String name, final String relativePath, final String pickupPath) {
		this.name = name;
		this.relativePath = relativePath;
		this.pickupPath = pickupPath;
	}

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(final String relativePath) {
		this.relativePath = relativePath;
	}

	public String getPickupPath() {
		return pickupPath;
	}

	public void setPickupPath(final String pickupPath) {
		this.pickupPath = pickupPath;
	}
		
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final InboxEntry other = (InboxEntry) obj;
		return id == other.id 
				&& Objects.equals(name, other.name) 
				&& Objects.equals(pickupPath, other.pickupPath)
				&& Objects.equals(relativePath, other.relativePath);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, pickupPath, relativePath);
	}

	@Override
	public String toString() {
		return "InboxEntry [id=" + id + ", name=" + name + ", relativePath=" + relativePath + ", pickupPath="
				+ pickupPath + "]";
	}
}
