package esa.s1pdgs.cpoc.inbox.polling.fs;

import java.io.File;
import java.util.Objects;

import org.springframework.data.annotation.Id;

import esa.s1pdgs.cpoc.inbox.polling.InboxEntry;

public class FilesystemInboxEntry implements InboxEntry {
	
	@Id
	private long id;
	
	private final File entry;
	
	public FilesystemInboxEntry(File entry) {
		this.entry = entry;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return entry.getName();
	}

	@Override
	public int hashCode() {
		return Objects.hash(entry);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(getClass()))
		{
			return Objects.equals(entry, ((FilesystemInboxEntry) obj).entry);
		}
		return false;
	}

	@Override
	public String toString() {
		return "FilesystemInboxEntry [entry=" + entry + "]";
	}

	@Override
	public String getUrl() {
		return "file://" + entry.getPath();
	}
}
