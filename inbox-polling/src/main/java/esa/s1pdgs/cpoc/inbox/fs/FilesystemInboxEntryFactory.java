package esa.s1pdgs.cpoc.inbox.fs;

import java.io.File;

import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.inbox.InboxEntryFactory;
import esa.s1pdgs.cpoc.inbox.config.InboxPathInformation;
import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;

@Component
public class FilesystemInboxEntryFactory implements InboxEntryFactory {

	@Override
	public InboxEntry newInboxEntry(InboxPathInformation inboxPathInformation, String inboxPath) {
		final File file = new File(inboxPath.replace("file://", ""));

		InboxEntry inboxEntry = new InboxEntry();
		inboxEntry.setName(file.getName());
		inboxEntry.setUrl("file://" + file.getPath());
		inboxEntry.setMissionId(inboxPathInformation.getMissionId());
		inboxEntry.setSatelliteId(inboxPathInformation.getSatelliteId());
		inboxEntry.setStationCode(inboxPathInformation.getStationCode());

		return inboxEntry;
	}
}
