package esa.s1pdgs.cpoc.ingestion.trigger.entity;

import static java.lang.String.format;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Objects;

@Document(collection = "inboxEntry")
public class InboxEntry {

	@Transient
	public static final String ENTRY_SEQ_KEY = "inboxEntry";

	@Id
	private ObjectId id; //necessary for repository.delete(entry)

	private String name;
	private String relativePath;
	private String pickupURL;
	private Date lastModified;
	private long size;
	private String stationName;
	private String processingPod;

	public InboxEntry() {
	}

	public InboxEntry(final String name, final String relativePath, final String pickupURL, final Date lastModified,
					  final long size) {
		this.name = name;
		this.relativePath = relativePath;
		this.pickupURL = pickupURL;
		this.lastModified = lastModified;
		this.size = size;
	}

	public InboxEntry(final String name, final String relativePath, final String pickupURL, final Date lastModified,
					  final long size, final String processingPod) {
		this.name = name;
		this.relativePath = relativePath;
		this.pickupURL = pickupURL;
		this.lastModified = lastModified;
		this.size = size;
		this.processingPod = processingPod;
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

	public String getPickupURL() {
		return pickupURL;
	}

	public void setPickupURL(final String pickupURL) {
		this.pickupURL = pickupURL;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(final Date lastModified) {
		this.lastModified = lastModified;
	}

	public long getSize() {
		return size;
	}

	public void setSize(final long size) {
		this.size = size;
	}
	
	public String getStationName() {
		return stationName;
	}

	public void setStationName(final String stationName) {
		this.stationName = stationName;
	}

	public String getProcessingPod() { return processingPod; }

	public void setProcessingPod(String processingPod) { this.processingPod = processingPod; }

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
		// WARNING: Don't take 'id' into account when implementing equals/hashCode
		// because it's always 0 when created from Inbox
		final InboxEntry other = (InboxEntry) obj;
		return Objects.equals(name, other.name) && 
				Objects.equals(pickupURL, other.pickupURL)
				&& Objects.equals(relativePath, other.relativePath)
				&& Objects.equals(stationName, other.stationName)
				&& Objects.equals(processingPod, other.processingPod);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, pickupURL, relativePath, stationName, processingPod);
	}

	@Override
	public String toString() {
		return format("InboxEntry [name=%s, relativePath=%s, pickupURL=%s, lastModified=%s, size=%s, stationName=%s, processingPod=%s]", name,
				relativePath, pickupURL, lastModified, size, stationName, processingPod);
	}
}
