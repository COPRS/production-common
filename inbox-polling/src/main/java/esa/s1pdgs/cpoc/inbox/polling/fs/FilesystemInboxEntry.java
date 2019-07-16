package esa.s1pdgs.cpoc.inbox.polling.fs;

import java.io.File;
import java.util.Objects;

import esa.s1pdgs.cpoc.inbox.polling.InboxEntry;

public class FilesystemInboxEntry implements InboxEntry {
	private final File entry;

	public FilesystemInboxEntry(File entry) {
		this.entry = entry;
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
}
